package EXE.Tests;
import domini.*;
import org.junit.Test;
import static org.junit.Assert.*;

import java.util.*;

/**
 * Test d'Analisi.executaKMeans: comprova que retorna un ResultatAnalisi coherent
 * i que la silhouette és positiva en un cas de 2 clústers separats.
 *
 * Nota: cal tenir implementats Analisi i ResultatAnalisi tal com hem parlat.
 */
public class TestAnalisi {

    @Test
    public void analisiKMeansRetornaResultatValid() {
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

        Analisi an = new Analisi(ctrl);
        Analisi.ResultatAnalisi ra = an.executaKMeans(dades, 2, 100);

        assertNotNull(ra);
        assertEquals("KMeans", ra.getAlgorisme());
        assertEquals(2, ra.getK());
        assertNotNull(ra.getAssignacio());
        assertEquals(dades.size(), ra.getAssignacio().length);
        assertTrue(ra.getMillis() >= 0);
        assertTrue("Silhouette positiva en cas clar", ra.getQualitatSilhouette() > 0.3);
        assertNotNull(ra.getInerciaOCost());
        assertNotNull("Clusters no nuls", ra.getClusters());
    }

    private static Respostes rr(int idE, int idU, CtrlDomini ctrl, Pregunta pInt, int vInt, Pregunta pOrd, int vOrd) {
        Respostes r = new Respostes(idE, idU, new ArrayList<>(), ctrl);
        r.afegeixResposta(new ArrayList<>(Collections.singletonList(String.valueOf(vInt))), pInt.getId());
        r.afegeixResposta(new ArrayList<>(Collections.singletonList(String.valueOf(vOrd))), pOrd.getId());
        return r;
    }
}
