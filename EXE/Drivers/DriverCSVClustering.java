package domini;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Driver d'anàlisi interactiu:
 * - Opció 1: KMeans
 * - Opció 2: K-Medoids
 * Demana idEnquesta, k i maxIter i executa l'algorisme triat.
 * Requereix: Analisi, KMeans, KMedoids, CtrlDomini i classes de domini.
 */
class DriverCSVClustering {

    private final CtrlDomini ctrl;
    private final Scanner sc;

    DriverCSVClustering(CtrlDomini ctrl, Scanner sc) {
        this.ctrl = ctrl;
        this.sc = sc;
    }

    public void menu() {
        System.out.println("\n--- ANÀLISI (KMeans / K-Medoids) ---");
        System.out.println("1) KMeans");
        System.out.println("2) K-Medoids");
        System.out.println("0) Tornar");

        int opt = readInt("Opció: "); sc.nextLine();
        if (opt == 0) return;

        boolean useKMeans = (opt == 1);
        if (opt != 1 && opt != 2) {
            System.out.println("Opció no vàlida.");
            return;
        }

        int idE     = readInt("ID d'enquesta a usar (enter): "); sc.nextLine();
        int k       = readInt("Nombre de clústers k: ");         sc.nextLine();
        int maxIter = readInt("maxIter (p.ex. 100): ");          sc.nextLine();

        try {
            // Construeix dataset a partir de l’enquesta escollida
            List<Respostes> dataset = buildDatasetFromEnquesta(idE);

            if (dataset.isEmpty()) {
                System.out.println("No hi ha respostes per a aquesta enquesta.");
                return;
            }
            if (k <= 0 || k > dataset.size()) {
                System.out.println("k fora de rang (1.."+dataset.size()+").");
                return;
            }

            Analisi an = new Analisi(ctrl);
            Analisi.ResultatAnalisi ra;

            if (useKMeans) {
                ra = an.executaKMeans(dataset, k, maxIter);
            } else {
                // useAllPairsSwap = false (ajusta-ho si vols exposar-lo)
                ra = an.executaKMedoids(dataset, k, /*useAllPairsSwap*/ false, maxIter);
            }

            // Mostrar resum d’anàlisi
            System.out.println("\n== RESULTAT ANÀLISI ==");
            System.out.println("Algorisme: " + ra.getAlgorisme());
            System.out.println("k: " + ra.getK());
            System.out.println("Temps (ms): " + ra.getMillis());
            System.out.println("Silhouette: " + ra.getQualitatSilhouette());
            System.out.println("Cost/Inèrcia: " + ra.getInerciaOCost());

            Map<Integer, List<Integer>> clusters = ra.getClusters();
            if (clusters == null || clusters.isEmpty()) {
                // Si per algun motiu ve buit, intenta derivar-ho de l'assignació
                int[] assign = ra.getAssignacio();
                clusters = buildClustersFromAssign(assign);
            }

            printClusters(dataset, clusters);

        } catch (Exception ex) {
            System.out.println("Error durant l'anàlisi: " + ex.getMessage());
        }
    }

    /**
     * Construeix un dataset homogeni per a l’enquesta idE:
     * - Aplega tots els usuaris que tinguin respostes.
     * - Calcula la intersecció d’idP (preguntes) contestades per tots.
     * - Projecta cada Respostes a la mateixa llista de Preguntes (ordenada per idP).
     */
    private List<Respostes> buildDatasetFromEnquesta(int idE) {
        List<Respostes> candidats = new ArrayList<>();
        try {
            for (Usuari u : ctrl.getallUsuaris()) {
                try {
                    Respostes r = ctrl.getRespostes(idE, u.getId());
                    // descarta usuaris sense cap resposta realment guardada
                    if (r.getRespostes() != null && !r.getRespostes().isEmpty()) {
                        candidats.add(r);
                    }
                } catch (Exception ignore) { /* usuari sense respostes per aquesta enquesta */ }
            }
        } catch (Exception e) {
            // No hi ha usuaris al sistema
            return Collections.emptyList();
        }

        if (candidats.isEmpty()) return Collections.emptyList();

        // Intersecció d’idP que apareixen a TOTS els candidats
        Set<Integer> interseccio = null;
        for (Respostes r : candidats) {
            Set<Integer> setIdP = r.getRespostes().stream()
                    .map(Resposta::getIdPregunta)
                    .collect(Collectors.toSet());
            if (interseccio == null) interseccio = new HashSet<>(setIdP);
            else interseccio.retainAll(setIdP);
        }

        if (interseccio == null || interseccio.isEmpty()) {
            System.out.println("[INFO] Cap conjunt comú de preguntes entre usuaris. Dataset buit.");
            return Collections.emptyList();
        }

        // Ordenem idP per consistència
        List<Integer> idPCommons = new ArrayList<>(interseccio);
        Collections.sort(idPCommons);

        // Projectem cada usuari a EXACTAMENT aquestes preguntes
        List<Respostes> ds = new ArrayList<>();
        for (Respostes r : candidats) {
            List<Resposta> sel = new ArrayList<>(idPCommons.size());
            boolean ok = true;
            for (Integer idP : idPCommons) {
                try {
                    sel.add(ctrl.GetResposta(r.getIdE(), r.getIdU(), idP));
                } catch (Exception ex) {
                    ok = false; break;
                }
            }
            if (ok) ds.add(new Respostes(r.getIdE(), r.getIdU(), new ArrayList<>(sel), ctrl));
        }
        if (ds.size() < candidats.size()) {
            System.out.println("[INFO] S'han descartat " + (candidats.size() - ds.size()) +
                    " usuaris per no tenir totes les preguntes comunes contestades.");
        }
        return ds;
    }

    private static Map<Integer, List<Integer>> buildClustersFromAssign(int[] assign) {
        if (assign == null) return Collections.emptyMap();
        Map<Integer, List<Integer>> map = new LinkedHashMap<>();
        for (int i = 0; i < assign.length; i++) {
            map.computeIfAbsent(assign[i], k -> new ArrayList<>()).add(i);
        }
        return map;
    }

    private void printClusters(List<Respostes> dataset, Map<Integer, List<Integer>> clusters) {
        Map<Integer, Integer> idxToUser = new HashMap<>();
        for (int i = 0; i < dataset.size(); i++) idxToUser.put(i, dataset.get(i).getIdU());

        System.out.println("\n===== CLÚSTERS =====");
        for (Map.Entry<Integer, List<Integer>> e : clusters.entrySet()) {
            List<Integer> membres = e.getValue();
            List<Integer> usrs = membres.stream().map(idxToUser::get).collect(Collectors.toList());
            System.out.printf("Cluster=%d -> idx=%s  idU=%s%n", e.getKey(), membres, usrs);
        }
    }

    private int readInt(String m) {
        System.out.print(m);
        while (!sc.hasNextInt()) { System.out.print("Int: "); sc.next(); }
        return sc.nextInt();
    }
}
