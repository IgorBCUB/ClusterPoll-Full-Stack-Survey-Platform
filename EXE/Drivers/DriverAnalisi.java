package exe;

import domini.*;
import java.util.*;

public class DriverAnalisi {
    public static void main(String[] args) {
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

        System.out.println("== ANALISI KMEANS ==");
        System.out.println("Algorisme: " + ra.getAlgorisme());
        System.out.println("k: " + ra.getK());
        System.out.println("Temps (ms): " + ra.getMillis());
        System.out.println("Silhouette: " + ra.getQualitatSilhouette());
        System.out.println("Cost/Inercia: " + ra.getInerciaOCost());
        System.out.println("Clusters: " + ra.getClusters());
        System.out.println("Assignacio: " + Arrays.toString(ra.getAssignacio()));
    }

    private static Respostes rr(int idE, int idU, CtrlDomini ctrl, Pregunta pInt, int vInt, Pregunta pOrd, int vOrd) {
        Respostes r = new Respostes(idE, idU, new ArrayList<>(), ctrl);
        int idPInt = pInt.getId();
        int idPOrd = pOrd.getId();
        r.afegeixResposta(new ArrayList<>(Collections.singletonList(String.valueOf(vInt))), idPInt);
        r.afegeixResposta(new ArrayList<>(Collections.singletonList(String.valueOf(vOrd))), idPOrd);
        return r;
    }
}
