package domini;

import java.text.Normalizer;
import java.util.*;

/**
 * Construeix encoders de columna a partir de TOT el dataset i
 * permet codificar cada Respostes a un vector double[] de dimensió fixa.
 *
 * Tipus:
 *  - integer/ordinal: 1 feature normalitzada [0..1]
 *  - nominal_unica: one-hot (una feature per etiqueta detectada)
 *  - nominal_multiple: multi-hot
 *  - string: bossa de caràcters (26 lletres + 10 dígits + 1 altres) + 1 feature de longitud normalitzada
 */
public final class Vectoritzador {

    private final List<ColEncoder> encoders;
    private final int totalDim;

    private Vectoritzador(List<ColEncoder> encoders) {
        this.encoders = encoders;
        int sum = 0;
        for (ColEncoder e : encoders) sum += e.dim();
        this.totalDim = sum;
    }

    public static Vectoritzador build(CtrlDomini ctrl, List<Respostes> dades) {
        if (dades == null || dades.isEmpty()) throw new IllegalArgumentException("Dades buides");
        int m = dades.get(0).getRespostes().size();
        List<ColEncoder> encs = new ArrayList<>(m);

        for (int j = 0; j < m; ++j) {
            Resposta r0 = dades.get(0).getRespostes().get(j);
            int idP = r0.getIdPregunta();
            Pregunta p = ctrl.getPregunta(idP);
            String tipus = p.getTipus(); // "integer","ordinal","nominal_unica","nominal_multiple","string"

            switch (tipus) {
                case "integer": {
                    double min = p.getMinValue();
                    double max = p.getMaxValue();
                    encs.add(new IntEncoder(idP, min, max));
                    break;
                }
                case "ordinal": {
                    PreguntaOrdinal po = (PreguntaOrdinal) p;
                    int M = Math.max(1, po.getNumOpcions() - 1);
                    // Clonem la llista d'opcions per tenir els labels en ordre
                    java.util.List<String> labels = new java.util.ArrayList<>(po.getOpcions());
                    encs.add(new OrdEncoder(idP, M, labels));
                    break;
                }
                case "nominal_unica": {
                    LinkedHashSet<String> labels = new LinkedHashSet<>();
                    for (Respostes rr : dades) {
                        ArrayList<String> cont = rr.getRespostes().get(j).getContingut();
                        if (!cont.isEmpty()) labels.add(cont.get(0));
                    }
                    encs.add(new OneHotEncoder(idP, new ArrayList<>(labels)));
                    break;
                }
                case "nominal_multiple": {
                    LinkedHashSet<String> labels = new LinkedHashSet<>();
                    for (Respostes rr : dades) {
                        ArrayList<String> cont = rr.getRespostes().get(j).getContingut();
                        for (String s : cont) labels.add(s);
                    }
                    encs.add(new MultiHotEncoder(idP, new ArrayList<>(labels)));
                    break;
                }
                default: { // string
                    int minLen = Integer.MAX_VALUE, maxLen = 0;
                    for (Respostes rr : dades) {
                        ArrayList<String> cont = rr.getRespostes().get(j).getContingut();
                        String s = cont.isEmpty() ? "" : cont.get(0);
                        int L = s.length();
                        if (L < minLen) minLen = L;
                        if (L > maxLen) maxLen = L;
                    }
                    if (minLen == Integer.MAX_VALUE) { minLen = 0; maxLen = 0; }
                    encs.add(new TextEncoder(idP, minLen, maxLen));
                    break;
                }
            }
        }
        return new Vectoritzador(encs);
    }

    public int dim() { return totalDim; }

    /** Omple el vector out (de mida dim()) amb la codificació de rset. */
    public void encode(Respostes rset, double[] out) {
        if (out.length != totalDim) throw new IllegalArgumentException("Mida vector incorrecta");
        int off = 0;
        ArrayList<Resposta> row = rset.getRespostes();
        for (int j = 0; j < encoders.size(); ++j) {
            ColEncoder e = encoders.get(j);
            off += e.encode(row.get(j), out, off);
        }
    }

    /* ---------- Encoders ---------- */

    private interface ColEncoder {
        int dim();
        int encode(Resposta r, double[] out, int offset);
    }

    private static final class IntEncoder implements ColEncoder {
        final int idP; final double min, max;
        IntEncoder(int idP, double min, double max) { this.idP = idP; this.min = min; this.max = max; }
        public int dim() { return 1; }
        public int encode(Resposta r, double[] out, int off) {
            int v = Integer.parseInt(r.getContingut().get(0));
            double d = (max == min) ? 0.0 : (v - min) / (double)(max - min);
            out[off] = clamp01(d);
            return 1;
        }
    }

    private static final class OrdEncoder implements ColEncoder {
        final int idP;
        final int denom;
        final java.util.List<String> labels; // opcions en ordre

        OrdEncoder(int idP, int denom, java.util.List<String> labels) {
            this.idP = idP;
            this.denom = Math.max(1, denom);
            this.labels = labels;
        }

        public int dim() { return 1; }

        public int encode(Resposta r, double[] out, int off) {
            String raw = r.getContingut().isEmpty() ? "" : r.getContingut().get(0).trim();
            int idx;

            try {
                // Cas antic: la resposta ve com a índex "0","1","2",...
                idx = Integer.parseInt(raw);
            } catch (NumberFormatException e) {
                // Cas UI: la resposta ve com a etiqueta "Mal","Bé",...
                idx = labels.indexOf(raw);
                if (idx < 0) {
                    // Si no es troba, la posem al nivell 0 per no rebentar.
                    idx = 0;
                }
            }

            out[off] = clamp01(idx / (double) denom);
            return 1;
        }
    }



    private static final class OneHotEncoder implements ColEncoder {
        final int idP; final List<String> labels;
        OneHotEncoder(int idP, List<String> labels) { this.idP = idP; this.labels = labels; }
        public int dim() { return labels.size(); }
        public int encode(Resposta r, double[] out, int off) {
            Arrays.fill(out, off, off + labels.size(), 0.0);
            String raw = r.getContingut().isEmpty() ? "" : r.getContingut().get(0).trim();

            int pos = -1;
            // 1) intentar interpretar com a índex numèric ("0","1",...)
            try {
                int idx = Integer.parseInt(raw);
                if (idx >= 0 && idx < labels.size()) pos = idx;
            } catch (NumberFormatException ignore) {
                // 2) sinó, interpretar com a etiqueta de text
                pos = labels.indexOf(raw);
            }

            if (pos >= 0) out[off + pos] = 1.0;
            return labels.size();
        }
    }


    private static final class MultiHotEncoder implements ColEncoder {
        final int idP; final List<String> labels;
        MultiHotEncoder(int idP, List<String> labels) { this.idP = idP; this.labels = labels; }
        public int dim() { return labels.size(); }
        public int encode(Resposta r, double[] out, int off) {
            Arrays.fill(out, off, off + labels.size(), 0.0);
            for (String s : r.getContingut()) {
                String raw = s.trim();
                int pos = -1;
                // 1) intentar com a índex numèric
                try {
                    int idx = Integer.parseInt(raw);
                    if (idx >= 0 && idx < labels.size()) pos = idx;
                } catch (NumberFormatException ignore) {
                    // 2) sinó, com a etiqueta
                    pos = labels.indexOf(raw);
                }
                if (pos >= 0) out[off + pos] = 1.0;
            }
            return labels.size();
        }
    }


    private static final class TextEncoder implements ColEncoder {
        static final int LETTERS = 26, DIGITS = 10, OTHER = 1, LEN = 1;
        static final int DIM = LETTERS + DIGITS + OTHER + LEN;
        final int idP, minLen, maxLen;
        TextEncoder(int idP, int minLen, int maxLen) { this.idP = idP; this.minLen = minLen; this.maxLen = maxLen; }
        public int dim() { return DIM; }
        public int encode(Resposta r, double[] out, int off) {
            Arrays.fill(out, off, off + DIM, 0.0);
            String raw = r.getContingut().isEmpty() ? "" : r.getContingut().get(0);
            String s = normalizeBasic(raw);
            int L = s.length();

            if (L > 0) {
                int lettersOff = off;
                int digitsOff  = off + LETTERS;
                int otherOff   = off + LETTERS + DIGITS;
                for (int i = 0; i < L; ++i) {
                    char c = s.charAt(i);
                    if (c >= 'a' && c <= 'z') out[lettersOff + (c - 'a')] += 1.0;
                    else if (c >= '0' && c <= '9') out[digitsOff + (c - '0')] += 1.0;
                    else out[otherOff] += 1.0;
                }
                // normalitza per longitud
                for (int i = off; i < off + LETTERS + DIGITS + OTHER; ++i) out[i] /= L;
            }
            double lenN = (maxLen == minLen) ? 0.0 : (L - minLen) / (double)(maxLen - minLen);
            out[off + LETTERS + DIGITS + OTHER] = clamp01(lenN);
            return DIM;
        }
    }

    private static double clamp01(double x) { return x < 0 ? 0 : (x > 1 ? 1 : x); }

    private static String normalizeBasic(String s) {
        String noAccent = Normalizer.normalize(s, Normalizer.Form.NFD)
                .replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
        return noAccent.toLowerCase(Locale.ROOT);
    }
}
