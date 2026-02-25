package domini;

import java.util.*;
import java.util.stream.Collectors;

class DriverKMedoids {

    private final CtrlDomini ctrlDomini;
    private final Scanner scanner;

    public DriverKMedoids(CtrlDomini ctrlDomini, Scanner scanner) {
        this.ctrlDomini = ctrlDomini;
        this.scanner = scanner;
    }

    public void gestioKMedoids() {
        System.out.println("\n--- K-MEDOIDS (PAM) ---");

        int idEnquesta = llegirEnter("ID de l'enquesta: "); scanner.nextLine();

        // Demanem l’ordre de preguntes (idP) que s’usarà per a tots els usuaris
        List<Integer> idPreguntes = llegirEnterCSV(
                "Introdueix els idP de les preguntes (separats per comes, ordre comú): ");
        if (idPreguntes.isEmpty()) {
            System.out.println("Cal almenys 1 idP de pregunta.");
            return;
        }

        int numUsuaris = llegirEnter("\nQuants usuaris vols incloure al clustering? "); scanner.nextLine();
        if (numUsuaris <= 0) {
            System.out.println("Cal almenys 1 usuari.");
            return;
        }

        // Recollim respostes via CtrlDomini (creant el contenidor si cal)
        List<Integer> idUsuaris = new ArrayList<>();
        for (int u = 0; u < numUsuaris; u++) {
            int idU = llegirEnter("\nID d'usuari per al participant #" + (u+1) + ": "); scanner.nextLine();

            // Crear el conjunt si no existeix (si existeix, deixem que llenci i mostrem avís)
            try {
                ctrlDomini.creaRespostes(idEnquesta, idU);
                System.out.println("Creat conjunt Respostes ("+idEnquesta+","+idU+")");
            } catch (IllegalStateException already) {
                System.out.println("Conjunt ja existent per ("+idEnquesta+","+idU+"). Continuo.");
            }

            // Per cada pregunta d’aquest qüestionari, demanem contingut i l’afegim
            for (Integer idP : idPreguntes) {
                System.out.println(" — Resposta per a idP=" + idP + " —");
                System.out.println("   * Valor únic (enter/text) => escriu-lo tal qual");
                System.out.println("   * Múltiples opcions => separa amb comes (p.ex. 'A,B,C')");
                String lin = llegirLinia("   Contingut: ");
                List<String> cont = parseCSVToList(lin);

                try {
                    ctrlDomini.AfegeixResposta(idEnquesta, idU, idP, cont);
                } catch (Exception e) {
                    System.out.println("No s'ha pogut afegir la resposta per idP=" + idP +
                            " (usuari " + idU + "): " + e.getMessage());
                }
            }
            idUsuaris.add(idU);
        }

        // Construïm el dataset a partir del domini
        List<Respostes> dataset = new ArrayList<>();
        for (Integer idU : idUsuaris) {
            try {
                dataset.add(ctrlDomini.getRespostes(idEnquesta, idU));
            } catch (Exception e) {
                System.out.println("No s'han pogut recuperar les Respostes de ("+idEnquesta+","+idU+"): " + e.getMessage());
            }
        }
        if (dataset.isEmpty()) {
            System.out.println("No hi ha dades per clusteritzar.");
            return;
        }

        int k = llegirEnter("\nk (nombre de clusters): "); scanner.nextLine();
        int maxIter = llegirEnter("maxIter (p.ex. 100): "); scanner.nextLine();
        boolean allPairs = llegirSiNo("Provar tots els intercanvis (SWAP exhaustiu)? (s/n): ");

        // Executem K-Medoids (KMedoids pot fer servir ctrlDomini.getPregunta(idP) internament)
        int idAlg = 1;
        KMedoids km = new KMedoids(ctrlDomini, idAlg, k, dataset);

        System.out.println("\nExecutant k-medoids…");
        Object out = km.executa(maxIter, allPairs);
        if (!(out instanceof KMedoids.Resultat)) {
            System.out.println("Sortida inesperada de KMedoids.executa()");
            return;
        }
        KMedoids.Resultat res = (KMedoids.Resultat) out;

        // Resultats
        System.out.println("\n===== RESULTATS K-MEDOIDS =====");
        System.out.printf("Cost total: %.6f%n", res.getCostTotal());

        List<Integer> medoids = res.getMedoids();
        Map<Integer, List<Integer>> clusters = res.getClusters();

        // index del dataset -> idU (per imprimir bonic)
        Map<Integer, Integer> idxToUser = new HashMap<>();
        for (int i = 0; i < dataset.size(); i++) idxToUser.put(i, dataset.get(i).getIdU());

        System.out.println("Medoids (índexs dins dataset): " + medoids);
        System.out.println("Medoids com idU: " + medoids.stream().map(idxToUser::get).collect(Collectors.toList()));

        System.out.println("\nClusters:");
        for (Map.Entry<Integer, List<Integer>> e : clusters.entrySet()) {
            Integer medIdx = e.getKey();
            List<Integer> membres = e.getValue();
            List<Integer> membresIdU = membres.stream().map(idxToUser::get).collect(Collectors.toList());
            System.out.printf("  Medoid idx=%d (idU=%d) -> membres idx=%s  (idU=%s)%n",
                    medIdx, idxToUser.get(medIdx), membres.toString(), membresIdU.toString());
        }
        System.out.println("===== FI =====\n");
    }

    //helpers de lectura

    private int llegirEnter(String missatge) {
        System.out.print(missatge);
        while (!scanner.hasNextInt()) {
            System.out.println("Has d'introduir un número enter.");
            scanner.next();
            System.out.print(missatge);
        }
        return scanner.nextInt();
    }

    private String llegirLinia(String missatge) {
        System.out.print(missatge);
        return scanner.nextLine().trim();
    }

    private boolean llegirSiNo(String missatge) {
        System.out.print(missatge);
        String s = scanner.nextLine().trim().toLowerCase(Locale.ROOT);
        while (!(s.equals("s") || s.equals("n"))) {
            System.out.print("Respon 's' o 'n': ");
            s = scanner.nextLine().trim().toLowerCase(Locale.ROOT);
        }
        return s.equals("s");
    }

    private List<Integer> llegirEnterCSV(String missatge) {
        System.out.print(missatge);
        String lin = scanner.nextLine().trim();
        if (lin.isEmpty()) return new ArrayList<>();
        String[] parts = lin.split(",");
        LinkedHashSet<Integer> set = new LinkedHashSet<>();
        for (String p : parts) {
            String t = p.trim();
            if (!t.isEmpty()) {
                try { set.add(Integer.parseInt(t)); }
                catch (NumberFormatException ex) { System.out.println("Ignorant valor no enter: " + t); }
            }
        }
        return new ArrayList<>(set);
    }

    private List<String> parseCSVToList(String lin) {
        if (lin == null || lin.isEmpty()) return List.of();
        String[] parts = lin.split(",");
        List<String> out = new ArrayList<>();
        for (String p : parts) {
            String t = p.trim();
            if (!t.isEmpty()) out.add(t);
        }
        return out;
    }
}