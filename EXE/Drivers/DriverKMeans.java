package exe;

import domini.*;

import java.util.*;

/**
 * Driver senzill per provar KMeans sobre un conjunt de dades sintètic 2D
 * (una integer [0..10] + una ordinal [0..3]) amb 2 clústers clars.
 *
 * Requisit: afegeix prèviament la classe domini.KMeans al vostre projecte
 * (la que hem anat comentant aquests dies).
 */
public class DriverKMeans {
    public static void main(String[] args) {
        // 1) Domini bàsic i preguntes
        CtrlDomini ctrl = new CtrlDomini();
        // Integer [0..10]
        PreguntaInteger pInt = ctrl.creaPreguntaInteger("Score", 0, 10);
        // Ordinal amb 4 opcions (0..3)
        Vector<String> ordOps = new Vector<>(Arrays.asList("Low","Mid","High","Ultra"));
        PreguntaOrdinal pOrd = ctrl.creaPreguntaOrdinal("Level", ordOps);

        // 2) Conjunt de Respostes (6 perfils → 2 clústers clars)
        // Cluster A ~ (1..2, Low/Mid)  ; Cluster B ~ (8..9, High/Ultra)
        List<Respostes> dades = new ArrayList<>();
        dades.add(res(p(1),    o(0), pInt, pOrd, ctrl, /*idE*/0, /*idU*/0));
        dades.add(res(p(2),    o(1), pInt, pOrd, ctrl, 0, 1));
        dades.add(res(p(1),    o(1), pInt, pOrd, ctrl, 0, 2));
        dades.add(res(p(9),    o(3), pInt, pOrd, ctrl, 0, 3));
        dades.add(res(p(8),    o(2), pInt, pOrd, ctrl, 0, 4));
        dades.add(res(p(9),    o(2), pInt, pOrd, ctrl, 0, 5));

        // 3) Executa KMeans(k=2)
        KMeans km = new KMeans(ctrl, /*idAlg=*/1, /*k=*/2);
        km.setRespostes(dades);
        Object raw = km.executa(/*maxIter=*/100, /*unused*/false);

        if (raw instanceof KMeans.Resultat r) {
            System.out.println("== KMEANS RESULT ==");
            System.out.println("k = 2");
            System.out.println("Centroides:");
            int cidx = 0;
            for (double[] mu : r.getCentroids()) {
                System.out.println("  C" + (cidx++) + " = " + Arrays.toString(mu));
            }
            System.out.println("Assignació: " + Arrays.toString(r.getAssignacio()));
            System.out.println("Inèrcia: " + r.getInertia());

            // Silhouette (si teniu Analisi.calculaSilhouette, la podeu usar en lloc d’aquest codi local)
            double sil = calculaSilhouette(dades, r.getAssignacio());
            System.out.println("Silhouette: " + sil);
        } else {
            System.out.println("Resultat inesperat: " + raw);
        }
    }

    // ---------- Helpers per crear Respostes/Resposta ----------

    private static Respostes res(ArrayList<String> vInt,
                                 ArrayList<String> vOrd,
                                 Pregunta pInt, Pregunta pOrd,
                                 CtrlDomini ctrl, int idE, int idU) {
        Respostes rr = new Respostes(idE, idU, new ArrayList<>(), ctrl);
        rr.afegeixResposta(vInt, pInt.getId());
        rr.afegeixResposta(vOrd, pOrd.getId());
        return rr;
    }
    private static ArrayList<String> p(int x) { return new ArrayList<>(Collections.singletonList(String.valueOf(x))); }
    private static ArrayList<String> o(int k) { return new ArrayList<>(Collections.singletonList(String.valueOf(k))); }

    // (Opcional) Silhouette local per al driver, idèntic criteri al vostre domini
    private static double calculaSilhouette(List<Respostes> dades, int[] assign) {
        int n = dades.size();
        double[][] D = new double[n][n];
        for (int i = 0; i < n; ++i) {
            D[i][i] = 0.0;
            for (int j = i + 1; j < n; ++j) {
                double dij = distancia(dades.get(i), dades.get(j));
                D[i][j] = dij;
                D[j][i] = dij;
            }
        }
        Map<Integer, List<Integer>> byC = new HashMap<>();
        for (int i = 0; i < n; ++i) {
            byC.computeIfAbsent(assign[i], kk -> new ArrayList<>()).add(i);
        }
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
    private static double distancia(Respostes a, Respostes b) {
        // mateix criteri que KMedoids: mitjana de distàncies de cada Resposta (ja normalitzades a [0,1])
        ArrayList<Resposta> ra = a.getRespostes();
        ArrayList<Resposta> rb = b.getRespostes();
        double s = 0.0;
        for (int i = 0; i < ra.size(); ++i) s += ra.get(i).calcula_distanciaRespostes(rb.get(i));
        return s / Math.max(1, ra.size());
    }
}
