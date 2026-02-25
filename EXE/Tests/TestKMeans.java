package EXE.Tests;
import domini.*;
import org.junit.Test;
import static org.junit.Assert.*;

import java.util.*;

/**
 * Tests bàsics de KMeans sobre un dataset sintètic 2D (integer + ordinal),
 * amb 2 clústers separats. Comprovem:
 *  - mida de centroides i assignació
 *  - silhouette raonablement alta (> 0.5 en aquest cas clar)
 *  - inèrcia no negativa
 */
public class TestKMeans {

    @Test
    public void kmeansFuncionaAmbDosClustersNets() {
        CtrlDomini ctrl = new CtrlDomini();
        PreguntaInteger pInt = ctrl.creaPreguntaInteger("Score", 0, 10);
        Vector<String> ordOps = new Vector<>(Arrays.asList("Low","Mid","High","Ultra"));
        PreguntaOrdinal pOrd = ctrl.creaPreguntaOrdinal("Level", ordOps);

        List<Respostes> dades = new ArrayList<>();
        dades.add(rr(0,0, ctrl, pInt, 1, pOrd, 0));
        dades.add(rr(0,1, ctrl, pInt, 2, pOrd, 1));
        dades.add(rr(0,2, ctrl, pInt, 1, pOrd, 1));
        dades.add(rr(0,3, ctrl, pInt, 9, pOrd, 3));
        dades.add(rr(0,4, ctrl, pInt, 8, pOrd, 2));
        dades.add(rr(0,5, ctrl, pInt, 9, pOrd, 2));

        KMeans km = new KMeans(ctrl, /*idAlg=*/1, /*k=*/2);
        km.setRespostes(dades);
        Object raw = km.executa(/*maxIter=*/100, /*unused=*/false);

        assertTrue("El resultat ha de ser de tipus KMeans.Resultat", raw instanceof KMeans.Resultat);
        KMeans.Resultat r = (KMeans.Resultat) raw;

        assertEquals("Han d'haver-hi 2 centroides", 2, r.getCentroids().size());
        assertEquals("Assignació per a tots els punts", dades.size(), r.getAssignacio().length);
        assertTrue("Inèrcia no negativa", r.getInertia() >= 0.0);

        // Silhouette raonablement alta
        double sil = calculaSilhouette(dades, r.getAssignacio());
        assertTrue("Silhouette > 0.5 per dades separades", sil > 0.5);
    }

    // ---------- helpers ----------
    private static Respostes rr(int idE, int idU, CtrlDomini ctrl, Pregunta pInt, int vInt, Pregunta pOrd, int vOrd) {
        Respostes r = new Respostes(idE, idU, new ArrayList<>(), ctrl);
        r.afegeixResposta(new ArrayList<>(Collections.singletonList(String.valueOf(vInt))), pInt.getId());
        r.afegeixResposta(new ArrayList<>(Collections.singletonList(String.valueOf(vOrd))), pOrd.getId());
        return r;
    }

    private static double calculaSilhouette(List<Respostes> dades, int[] assign) {
        int n = dades.size();
        double[][] D = new double[n][n];
        for (int i = 0; i < n; ++i) {
            for (int j = i; j < n; ++j) {
                double dij = distancia(dades.get(i), dades.get(j));
                D[i][j] = dij;
                D[j][i] = dij;
            }
        }
        Map<Integer, List<Integer>> byC = new HashMap<>();
        for (int i = 0; i < n; ++i) byC.computeIfAbsent(assign[i], k -> new ArrayList<>()).add(i);

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
        ArrayList<Resposta> ra = a.getRespostes();
        ArrayList<Resposta> rb = b.getRespostes();
        double s = 0.0;
        for (int i = 0; i < ra.size(); ++i)
            s += ra.get(i).calcula_distanciaRespostes(rb.get(i));
        return s / Math.max(1, ra.size());
    }
}
