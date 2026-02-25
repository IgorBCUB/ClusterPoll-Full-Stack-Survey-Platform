package domini;

import java.util.*;
import java.util.stream.Collectors;

class DriverResposta {
    private final CtrlDomini ctrlDomini;
    private final Scanner scanner;

    public DriverResposta(CtrlDomini ctrlDomini, Scanner scanner) {
        this.ctrlDomini = ctrlDomini;
        this.scanner = scanner;
    }

    public void gestioRespostes() {
        boolean tornar = false;
        while (!tornar) {
            System.out.println("\n--- GESTIÓ DE RESPOSTES ---");
            System.out.println("1) Get Respostes i mostrar");
            System.out.println("2) Eliminar Respostes");
            System.out.println("3) Contesta Enquesta (idE,idU)");
            System.out.println("4) Get Resposta (idE,idU,idP)");
            System.out.println("5) Eliminar Resposta (idE,idU,idP)");
            System.out.println("6) Calcula Distància entre Respostes");
            System.out.println("7) Print Respostes (idE,idU)");
            System.out.println("0) Tornar al menú principal");

            int opcio = llegirEnter("Selecciona una opció: ");
            scanner.nextLine(); // neteja salt

            try {
                switch (opcio) {
                    case 1: opcGetRespostes(); break;
                    case 2: opcEliminarRespostes(); break;
                    case 3: ContestaEnquesta(); break;
                    case 4: opcGetResposta(); break;
                    case 5: opcEliminarResposta(); break;
                    case 6: opcCalculaDistancia(); break;
                    case 7: PrintRespostesUsuariEnquesta(); break;
                    case 0: tornar = true; break;
                    default: System.out.println("Opció no vàlida!");
                }
            } catch (Exception e) {
                System.out.println("Error: " + e.getMessage());
            }
        }
    }

    private void PrintRespostesUsuariEnquesta() throws Exception {
        System.out.println("\n--- PRINT RESPOSTES (idE, idU) ---");

        int idE = llegirEnter("Introdueix idE (enquesta): ");
        scanner.nextLine();
        int idU = llegirEnter("Introdueix idU (usuari): ");
        scanner.nextLine();

        ArrayList<Resposta> llista = ctrlDomini.getLlistaRespostes(idE, idU);

        if (llista == null || llista.isEmpty()) {
            System.out.println("No hi ha respostes per aquest usuari en aquesta enquesta.");
            return;
        }

        System.out.println("\nRespostes de l'usuari " + idU + " a l'enquesta " + idE + ":");
        for (int i = 0; i < llista.size(); i++) {

            System.out.println("[" + (i + 1) + "] " + llista.get(i).getContingut());
        }
    }

    private void opcGetRespostes() {
        int idE = llegirEnter("ID Enquesta: "); scanner.nextLine();
        ctrlDomini.getEnquesta(idE);
        int idU = llegirEnter("ID Usuari: ");   scanner.nextLine();
        ctrlDomini.getUsuari(idU);

        Respostes rs = ctrlDomini.getRespostes(idE, idU);
        List<Resposta> l = rs.getRespostes();

        System.out.println("Respostes de (" + idE + "," + idU + "):");
        if (l.isEmpty()) {
            System.out.println("  (buit)");
            return;
        }
        for (Resposta r : l) {
            System.out.printf("  idP=%d  contingut=%s%n", r.getIdPregunta(), r.getContingut());
        }
    }

    private void opcEliminarRespostes() {
        int idE = llegirEnter("ID Enquesta: "); scanner.nextLine();
        ctrlDomini.getEnquesta(idE);
        int idU = llegirEnter("ID Usuari: ");   scanner.nextLine();
        ctrlDomini.getUsuari(idU);

        ctrlDomini.EliminarRespostes(idE, idU);
        System.out.println("Eliminat conjunt Respostes per (" + idE + "," + idU + ")");
    }

    private void ContestaEnquesta() {
        int idE = llegirEnter("ID Enquesta: "); scanner.nextLine();
        Enquesta e = ctrlDomini.getEnquesta(idE);
        int idU = llegirEnter("ID Usuari: "); scanner.nextLine();
        ctrlDomini.getUsuari(idU);

        List<Integer> idsPreguntes = e.getIdsPreguntes();
        if (idsPreguntes == null || idsPreguntes.isEmpty()) {
            System.out.println("Aquesta enquesta no té preguntes.");
            return;
        }
        ctrlDomini.creaRespostes(idE, idU);

        System.out.println("\n== Responent enquesta " + idE + " per l'usuari " + idU + " ==\n");

        for (int pos = 0; pos < idsPreguntes.size(); pos++) {
            Integer idP = idsPreguntes.get(pos);
            Pregunta p = ctrlDomini.getPregunta(idP);
            String tipus = p.getTipus();

            System.out.println("----------------------------------------------------------------");
            System.out.println("Pregunta " + (pos + 1) + "/" + idsPreguntes.size());
            System.out.println("Enunciat: " + p.getEnunciat());
            System.out.println("Tipus: " + tipus);

            List<String> contingut = new ArrayList<>();

            if ("integer".equalsIgnoreCase(tipus) && p instanceof PreguntaInteger) {
                PreguntaInteger pi = (PreguntaInteger) p;
                int min = pi.getMinValue();
                int max = pi.getMaxValue();
                System.out.println("Rang permès: [" + min + ".." + max + "]");
                int val = llegirEnter("Resposta (enter): "); scanner.nextLine();
                if (val < min || val > max) {
                    throw new IllegalArgumentException("Valor fora de rang [" + min + ".." + max + "].");
                }
                contingut.add(Integer.toString(val));
            }
            else if ("ordinal".equalsIgnoreCase(tipus) && p instanceof PreguntaOrdinal) {
                PreguntaOrdinal po = (PreguntaOrdinal) p;
                Vector<String> ops = po.getOpcions();
                for (int i = 0; i < ops.size(); i++) System.out.println("  [" + i + "] " + ops.get(i));
                int idx = llegirEnter("Índex triat: "); scanner.nextLine();
                if (idx < 0 || idx >= ops.size()) {
                    throw new IllegalArgumentException("Índex fora de rang [0.." + (ops.size() - 1) + "].");
                }
                contingut.add(Integer.toString(idx));
            }
            else if ("nominal_unica".equalsIgnoreCase(tipus) && p instanceof PreguntaNominalUnica) {
                PreguntaNominalUnica pu = (PreguntaNominalUnica) p;
                Set<String> ops = pu.getOpcions();
                System.out.println("Opcions disponibles:");
                for (String o : ops) System.out.println("  - " + o);
                String resp = llegirLinia("Resposta (una opció): ");
                if (!ops.contains(resp)) {
                    throw new IllegalArgumentException("Opció invàlida: " + resp);
                }
                contingut.add(resp);
            }
            else if ("nominal_multiple".equalsIgnoreCase(tipus) && p instanceof PreguntaNominalMult) {
                PreguntaNominalMult pm = (PreguntaNominalMult) p;
                Set<String> ops = pm.getOpcions();
                int qMax = pm.getQMax();
                System.out.println("Opcions (tria fins a " + qMax + "):");
                for (String o : ops) System.out.println("  - " + o);
                String lin = llegirLinia("Resposta (CSV): ");
                List<String> sel = parseCSVToList(lin);
                for (String s : sel) {
                    if (!ops.contains(s)) throw new IllegalArgumentException("Opció invàlida: " + s);
                }
                if (sel.size() > qMax) {
                    throw new IllegalArgumentException("Massa opcions (" + sel.size() + "), qMax=" + qMax + ".");
                }
                if (sel.isEmpty()) {
                    throw new IllegalArgumentException("Cal seleccionar almenys una opció.");
                }
                contingut.addAll(sel);
            }
            else {
                String txt = llegirLinia("Resposta en text: ");
                if (txt.isEmpty()) {
                    throw new IllegalArgumentException("La resposta és obligatòria.");
                }
                contingut.add(txt);
            }

            Resposta r = ctrlDomini.AfegeixResposta(idE, idU, idP, contingut);
            System.out.println("Resposta enregistrada (idP=" + r.getIdPregunta() + "): " + r.getContingut());
        }

        System.out.println("\n== Final de l'enquesta. Gràcies! ==\n");
    }

    private void opcGetResposta() {
        int idE = llegirEnter("ID Enquesta: "); scanner.nextLine();
        ctrlDomini.getEnquesta(idE);
        int idU = llegirEnter("ID Usuari: ");   scanner.nextLine();
        ctrlDomini.getUsuari(idU);
        int idP = llegirEnter("ID Pregunta (idP): "); scanner.nextLine();
        ctrlDomini.getPregunta(idP);

        Resposta r = ctrlDomini.GetResposta(idE, idU, idP);
        System.out.println("Resposta trobada: idP=" + r.getIdPregunta() + " contingut=" + r.getContingut());
    }

    private void opcEliminarResposta() {
        int idE = llegirEnter("ID Enquesta: "); scanner.nextLine();
        ctrlDomini.getEnquesta(idE);
        int idU = llegirEnter("ID Usuari: ");   scanner.nextLine();
        ctrlDomini.getUsuari(idU);
        int idP = llegirEnter("ID Pregunta (idP): "); scanner.nextLine();
        ctrlDomini.getPregunta(idP);

        ctrlDomini.EliminarResposta(idE, idU, idP);
        System.out.println("Resposta ("+idE+","+idU+","+idP+") eliminada.");
    }

    private void opcCalculaDistancia() {
        System.out.println("\n--- CALCULAR DISTÀNCIA (creant pregunta i respostes) ---");
        System.out.println("Tria tipus de pregunta a crear:");
        System.out.println("  1) Integer [min..max]");
        System.out.println("  2) Ordinal (llista ordenada d'opcions)");
        System.out.println("  3) Nominal Única (una opció)");
        System.out.println("  4) Nominal Múltiple (diverses opcions; qMax)");
        System.out.println("  5) Text lliure");

        int t = llegirEnter("Opció: "); scanner.nextLine();

        Pregunta p;
        try {
            switch (t) {
                case 1: {
                    String enunciat = llegirLinia("Enunciat: ");
                    int min = llegirEnter("Mínim (enter): "); scanner.nextLine();
                    int max = llegirEnter("Màxim (enter): "); scanner.nextLine();
                    p = ctrlDomini.creaPreguntaInteger(enunciat, min, max);
                    break;
                }
                case 2: {
                    String enunciat = llegirLinia("Enunciat: ");
                    Vector<String> opcions = llegirLlistaVector("Opcions (separades per comes): ");
                    p = ctrlDomini.creaPreguntaOrdinal(enunciat, opcions);
                    break;
                }
                case 3: {
                    String enunciat = llegirLinia("Enunciat: ");
                    Set<String> opcions = llegirOpcionsSet("Opcions (separades per comes): ");
                    p = ctrlDomini.creaPreguntaNominalUnica(enunciat, opcions);
                    break;
                }
                case 4: {
                    String enunciat = llegirLinia("Enunciat: ");
                    Set<String> opcions = llegirOpcionsSet("Opcions (separades per comes): ");
                    int qMax = llegirEnter("qMax (màxim d'opcions triables): "); scanner.nextLine();
                    p = ctrlDomini.creaPreguntaNominalMult(enunciat, opcions, qMax);
                    break;
                }
                case 5:
                default: {
                    String enunciat = llegirLinia("Enunciat: ");
                    p = ctrlDomini.creaPreguntaText(enunciat);
                    break;
                }
            }
        } catch (Exception e) {
            System.out.println("No s'ha pogut crear la pregunta: " + e.getMessage());
            return;
        }

        int idP = p.getId();

        System.out.println("\n- Introdueix RESPOSTA 1 -");
        ArrayList<String> cont1 = llegirContingutPerTipus(p);
        Resposta r1 = new Resposta(idP, cont1, ctrlDomini);

        System.out.println("\n- Introdueix RESPOSTA 2 -");
        ArrayList<String> cont2 = llegirContingutPerTipus(p);
        Resposta r2 = new Resposta(idP, cont2, ctrlDomini);

        try {
            double d = r1.calcula_distanciaRespostes(r2);
            System.out.printf(Locale.ROOT, "Distància = %.6f%n", d);
        } catch (Exception ex) {
            System.out.println("Error calculant la distància: " + ex.getMessage());
        }
    }

    private ArrayList<String> llegirContingutPerTipus(Pregunta p) {
        String tipus = p.getTipus().toLowerCase(Locale.ROOT);
        ArrayList<String> cont = new ArrayList<>();

        if (tipus.contains("integer") || tipus.contains("nominalint")) {
            int v = llegirEnter("Valor enter: "); scanner.nextLine();
            cont.add(Integer.toString(v));
        } else if (tipus.contains("ordinal")) {
            int idx = llegirEnter("Índex (0..n-1): "); scanner.nextLine();
            cont.add(Integer.toString(idx));
        } else if (tipus.contains("unica")) {
            String opt = llegirLinia("Opció triada: ");
            cont.add(opt);
        } else if (tipus.contains("multiple")) {
            System.out.println("Opcions triades (CSV): ");
            String lin = scanner.nextLine().trim();
            if (!lin.isEmpty()) {
                for (String s : lin.split(",")) {
                    String t = s.trim();
                    if (!t.isEmpty()) cont.add(t);
                }
            }
        } else { // text
            String text = llegirLinia("Text: ");
            cont.add(text);
        }
        return cont;
    }

    // Helpers
    private int llegirEnter(String missatge) {
        System.out.print(missatge);
        while (!scanner.hasNextInt()) {
            System.out.println("Has d'introduir un número!");
            scanner.next();
            System.out.print(missatge);
        }
        return scanner.nextInt();
    }

    private String llegirLinia(String missatge) {
        System.out.print(missatge);
        return scanner.nextLine().trim();
    }

    private List<String> parseCSVToList(String lin) {
        if (lin == null || lin.isEmpty()) return List.of();
        return Arrays.stream(lin.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());
    }

    private Vector<String> llegirLlistaVector(String msg) {
        System.out.print(msg);
        String lin = scanner.nextLine().trim();
        Vector<String> l = new Vector<>();
        if (!lin.isEmpty()) {
            for (String s : lin.split(",")) {
                String t = s.trim();
                if (!t.isEmpty()) l.add(t);
            }
        }
        return l;
    }

    private Set<String> llegirOpcionsSet(String msg) {
        System.out.print(msg);
        String lin = scanner.nextLine().trim();
        Set<String> set = new LinkedHashSet<>();
        if (!lin.isEmpty()) {
            for (String s : lin.split(",")) {
                String t = s.trim();
                if (!t.isEmpty()) set.add(t);
            }
        }
        return set;
    }
}