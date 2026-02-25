package domini;

import java.lang.reflect.Method;
import java.util.*;

/**
 * Façana per executar KMeans / KMedoids i retornar un resum homogeni.
 */
public class Analisi {

    private final CtrlDomini ctrl;

    public Analisi(CtrlDomini ctrl) {
        if (ctrl == null) throw new IllegalArgumentException("CtrlDomini no pot ser nul");
        this.ctrl = ctrl;
    }

    public static final class ResultatAnalisi {
        private final String algorisme;
        private final int k;
        private final Object rawResult;
        private final long millis;
        private final int[] assignacio;
        private final Map<Integer, List<Integer>> clusters;
        private final Double qualitatSilhouette;
        private final Double inerciaOCost;

        public ResultatAnalisi(String algorisme, int k, Object rawResult, long millis,
                               int[] assignacio, Map<Integer, List<Integer>> clusters,
                               Double qualitatSilhouette, Double inerciaOCost) {
            this.algorisme = algorisme;
            this.k = k;
            this.rawResult = rawResult;
            this.millis = millis;
            this.assignacio = assignacio;
            this.clusters = clusters;
            this.qualitatSilhouette = qualitatSilhouette;
            this.inerciaOCost = inerciaOCost;
        }

        public String getAlgorisme() { return algorisme; }
        public int getK() { return k; }
        public Object getRawResult() { return rawResult; }
        public long getMillis() { return millis; }
        public int[] getAssignacio() { return assignacio; }
        public Map<Integer, List<Integer>> getClusters() { return clusters; }
        public Double getQualitatSilhouette() { return qualitatSilhouette; }
        public Double getInerciaOCost() { return inerciaOCost; }
    }

    /* KMEANS: silhouette en el MATEIX espai vectoritzat (euclidià) que usa KMeans */
    public ResultatAnalisi executaKMeans(List<Respostes> dades, int k, int maxIter) {
        if (k <= 0 || k > dades.size())
            throw new IllegalArgumentException("K fora de rang");

        KMeans km = new KMeans(ctrl, 1, k);
        km.setRespostes(dades);

        long t0 = System.nanoTime();
        Object raw = km.executa(maxIter, false);
        long t1 = System.nanoTime();

        int[] assign = null;
        Map<Integer, List<Integer>> clusters = new HashMap<>();
        Double inertia = null;

        if (raw instanceof KMeans.Resultat r) {
            assign = r.getAssignacio();
            inertia = r.getInertia();
            clusters = r.getClusters();
        } else {
            assign = Get_Assignacio(raw);
            clusters = Get_Clusters(raw);
            inertia = Get_Double(raw, "getInertia");
        }

        Double sil;
        if (assign != null) {
            Vectoritzador vec = Vectoritzador.build(ctrl, dades);
            List<double[]> X = new ArrayList<>(dades.size());
            for (Respostes r : dades) {
                double[] v = new double[vec.dim()];
                vec.encode(r, v);
                X.add(v);
            }
            sil = calculaSilhouetteEuclidiana(X, assign);
        } else {
            sil = Double.NaN;
        }

        return new ResultatAnalisi("KMeans", k, raw, (t1 - t0) / 1_000_000,
                assign, clusters, sil, inertia);
    }

    /* KMEDOIDS: silhouette amb la distància del domini (mateixa que KMedoids) */
    public ResultatAnalisi executaKMedoids(List<Respostes> dades, int k, boolean useAllPairsSwap, int maxIter) {
        if (k <= 0 || k > dades.size())
            throw new IllegalArgumentException("K fora de rang");

        KMedoids km = new KMedoids(ctrl, 2, k);
        km.setRespostes(dades);

        long t0 = System.nanoTime();
        Object raw = km.executa(maxIter, useAllPairsSwap);
        long t1 = System.nanoTime();

        int[] assign = Get_Assignacio(raw);
        Map<Integer, List<Integer>> clusters = Get_Clusters(raw);

        if (assign == null && clusters != null && !clusters.isEmpty())
            assign = Get_AssigFromClusters(clusters, dades.size());

        Double cost = Get_Double(raw, "getCostTotal");
        if (cost == null) cost = Get_Double(raw, "getInertia");

        Double sil = (assign != null) ? calculaSilhouetteDomini(dades, assign) : Double.NaN;

        return new ResultatAnalisi("KMedoids", k, raw, (t1 - t0) / 1_000_000,
                assign, clusters, sil, cost);
    }

    /* --- Silhouette en l'espai euclidià (KMeans) --- */
    public static double calculaSilhouetteEuclidiana(List<double[]> X, int[] assign) {
        int n = X.size();
        if (assign.length != n) throw new IllegalArgumentException("assign length != dades size");

        double[][] D = new double[n][n];
        for (int i = 0; i < n; ++i) {
            D[i][i] = 0.0;
            for (int j = i + 1; j < n; ++j) {
                double d2 = 0.0;
                double[] a = X.get(i), b = X.get(j);
                for (int t = 0; t < a.length; ++t) {
                    double d = a[t] - b[t];
                    d2 += d * d;
                }
                double d = Math.sqrt(d2);
                D[i][j] = d;
                D[j][i] = d;
            }
        }
        Map<Integer, List<Integer>> byC = new HashMap<>();
        for (int i = 0; i < n; ++i) byC.computeIfAbsent(assign[i], kk -> new ArrayList<>()).add(i);

        double sum = 0.0;
        for (int i = 0; i < n; ++i) {
            int ci = assign[i];
            List<Integer> Ci = byC.get(ci);

            double ai = 0.0;
            if (Ci.size() > 1) {
                for (int j : Ci) if (j != i) ai += D[i][j];
                ai /= (Ci.size() - 1);
            }
            double bi = Double.POSITIVE_INFINITY;
            for (Map.Entry<Integer, List<Integer>> e : byC.entrySet()) {
                if (e.getKey() == ci) continue;
                List<Integer> C2 = e.getValue();
                if (C2.isEmpty()) continue;
                double avg = 0.0;
                for (int j : C2) avg += D[i][j];
                avg /= C2.size();
                if (avg < bi) bi = avg;
            }
            if (Double.isInfinite(bi)) bi = 0.0;
            double si = (bi == ai && ai == 0.0) ? 0.0 : (bi - ai) / Math.max(ai, bi);
            sum += si;
        }
        return sum / n;
    }

    /* --- Silhouette amb distància del domini (KMedoids) --- */
    public static double calculaSilhouetteDomini(List<Respostes> dades, int[] assign) {
        int n = dades.size();
        if (assign.length != n) throw new IllegalArgumentException("assign length != dades size");

        double[][] D = new double[n][n];
        for (int i = 0; i < n; ++i) {
            D[i][i] = 0.0;
            for (int j = i + 1; j < n; ++j) {
                double dij = distanciaDomini(dades.get(i), dades.get(j));
                D[i][j] = dij;
                D[j][i] = dij;
            }
        }
        Map<Integer, List<Integer>> byC = new HashMap<>();
        for (int i = 0; i < n; ++i) byC.computeIfAbsent(assign[i], kk -> new ArrayList<>()).add(i);

        double sum = 0.0;
        for (int i = 0; i < n; ++i) {
            int ci = assign[i];
            List<Integer> Ci = byC.get(ci);

            double ai = 0.0;
            if (Ci.size() > 1) {
                for (int j : Ci) if (j != i) ai += D[i][j];
                ai /= (Ci.size() - 1);
            }
            double bi = Double.POSITIVE_INFINITY;
            for (Map.Entry<Integer, List<Integer>> e : byC.entrySet()) {
                if (e.getKey() == ci) continue;
                List<Integer> C2 = e.getValue();
                if (C2.isEmpty()) continue;
                double avg = 0.0;
                for (int j : C2) avg += D[i][j];
                avg /= C2.size();
                if (avg < bi) bi = avg;
            }
            if (Double.isInfinite(bi)) bi = 0.0;
            double si = (bi == ai && ai == 0.0) ? 0.0 : (bi - ai) / Math.max(ai, bi);
            sum += si;
        }
        return sum / n;
    }

    private static double distanciaDomini(Respostes a, Respostes b) {
        var ra = a.getRespostes();
        var rb = b.getRespostes();
        int m = ra.size();
        if (m != rb.size()) throw new IllegalArgumentException("Mida desigual de respostes");
        if (m == 0) return 0.0;

        double suma = 0.0;
        for (int i = 0; i < m; ++i) {
            Resposta r1 = ra.get(i);
            Resposta r2 = rb.get(i);
            double d = a.calcula_distanciaRespostes(r1, r2);
            if (Double.isNaN(d) || Double.isInfinite(d)) throw new IllegalArgumentException("Distància no vàlida a la pregunta " + i);
            if (d < 0) d = 0;
            if (d > 1) d = 1;
            suma += d;
        }
        return suma / m;
    }

    // --- Reflexió ---

    private static int[] Get_Assignacio(Object raw) {
        if (raw == null) return null;
        try {
            Method m = raw.getClass().getMethod("getAssignacio");
            Object arr = m.invoke(raw);
            return (int[]) arr;
        } catch (Exception ignore) { return null; }
    }

    @SuppressWarnings("unchecked")
    private static Map<Integer, List<Integer>> Get_Clusters(Object raw) {
        if (raw == null) return Collections.emptyMap();
        try {
            Method m = raw.getClass().getMethod("getClusters");
            Object obj = m.invoke(raw);
            return (Map<Integer, List<Integer>>) obj;
        } catch (Exception ignore) { return Collections.emptyMap(); }
    }

    private static Double Get_Double(Object raw, String method) {
        if (raw == null) return null;
        try {
            Method m = raw.getClass().getMethod(method);
            Object v = m.invoke(raw);
            if (v instanceof Number num) return num.doubleValue();
            return null;
        } catch (Exception ignore) { return null; }
    }

    private static int[] Get_AssigFromClusters(Map<Integer, List<Integer>> clusters, int n) {
        int[] assign = new int[n];
        Arrays.fill(assign, -1);
        for (Map.Entry<Integer, List<Integer>> e : clusters.entrySet()) {
            int c = e.getKey();
            for (int idx : e.getValue()) assign[idx] = c;
        }
        return assign;
    }
}
