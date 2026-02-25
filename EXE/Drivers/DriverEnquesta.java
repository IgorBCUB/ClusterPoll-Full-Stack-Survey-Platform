package domini;
import java.util.Scanner;
import java.util.List;

class DriverEnquesta {
    private CtrlDomini ctrlDomini;
    private Scanner scanner;

    public DriverEnquesta(CtrlDomini ctrlDomini, Scanner scanner) {
        this.ctrlDomini = ctrlDomini;
        this.scanner = scanner;
    }

    public void gestioEnquestes() {
        boolean tornar = false;
        while (!tornar) {
            System.out.println("\n--- GESTIÓ D'ENQUESTES ---");
            System.out.println("1. Crear enquesta");
            System.out.println("2. Consultar enquesta");
            System.out.println("3. Modificar títol enquesta");
            System.out.println("4. Afegir pregunta a enquesta");
            System.out.println("5. Afegir pregunta nova a enquesta");
            System.out.println("6. Eliminar pregunta de enquesta");
            System.out.println("0. Tornar al menú principal");

            int opcio = llegirEnter("Selecciona una opció: ");

            switch (opcio) {
                case 1:
                    crearEnquesta();
                    break;
                case 2:
                    consultarEnquesta();
                    break;
                case 3:
                    modificarTitolEnquesta();
                    break;
                case 4:
                    afegirPreguntaEnquesta();
                    break;
                case 5:
                    afegirPreguntaNovaEnquesta();
                    break;
                case 6:
                    eliminarPreguntaEnquesta();
                    break;
                case 0:
                    tornar = true;
                    break;
                default:
                    System.out.println("Opció no vàlida!");
            }
        }
    }

    private void crearEnquesta() {
        int idUsuari = llegirEnter("ID de l'usuari creador: ");
        scanner.nextLine(); // Limpiar buffer
        System.out.print("Títol de l'enquesta: ");
        String titol = scanner.nextLine();

        try {
            Enquesta enquesta = ctrlDomini.creaEnquesta(titol, idUsuari);
            System.out.println("Enquesta creada amb ID: " + enquesta.getIdE());
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private void consultarEnquesta() {
        int idEnquesta = llegirEnter("ID de l'enquesta a consultar: ");
        try {
            Enquesta enquesta = ctrlDomini.getEnquesta(idEnquesta);
            System.out.println("Enquesta ID: " + enquesta.getIdE());
            System.out.println("Títol: " + enquesta.getTitol());
            System.out.println("Creador ID: " + enquesta.getIdCreador());
            System.out.println("Nombre de preguntes: " + enquesta.getNumPreguntes());

            List<Pregunta> preguntes = enquesta.getPreguntes();
            if (preguntes.isEmpty()) {
                System.out.println("Aquesta enquesta no té preguntes.");
            } else {
                System.out.println("Preguntes de l'enquesta:");
                int i = 1;
                for (Pregunta p : preguntes) {
                    System.out.println("  [" + i + "] ID: " + p.getId() + " | Tipus: " + p.getTipus() + " | Enunciat: " + p.getEnunciat() + "| Opcions: " + p.getOpcions());
                    i++;
                }
            }
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }


    private void modificarTitolEnquesta() {
        int idEnquesta = llegirEnter("ID de l'enquesta: ");
        scanner.nextLine(); // Limpiar buffer
        System.out.print("Nou títol: ");
        String nouTitol = scanner.nextLine();

        try {
            ctrlDomini.setTitolEnquesta(idEnquesta, nouTitol);
            System.out.println("Títol modificat correctament!");
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private void afegirPreguntaEnquesta() {
        int idEnquesta = llegirEnter("ID de l'enquesta: ");
        int idPregunta = llegirEnter("ID de la pregunta a afegir: ");

        try {
            ctrlDomini.afegeixPreguntaAEnquesta(idEnquesta, idPregunta);
            System.out.println("Pregunta afegida correctament!");
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private void eliminarPreguntaEnquesta() {
        int idEnquesta = llegirEnter("ID de l'enquesta: ");
        int idPregunta = llegirEnter("ID de la pregunta a eliminar: ");

        try {
            ctrlDomini.eliminaPreguntaAEnquesta(idEnquesta, idPregunta);
            System.out.println("Pregunta eliminada correctament!");
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private void afegirPreguntaNovaEnquesta() {
        int idEnquesta = llegirEnter("ID de l'enquesta: ");
        scanner.nextLine();
        System.out.print("Tipus (text/integer/ordinal/nominal_unica/nominal_multiple): ");
        String tipus = scanner.nextLine().trim();

        System.out.print("Enunciat: ");
        String enunciat = scanner.nextLine();

        Object[] params = null;

        try {
            switch (tipus.toLowerCase()) {
                case "integer":
                    System.out.print("Min: ");
                    int min = scanner.nextInt();
                    System.out.print("Max: ");
                    int max = scanner.nextInt();
                    params = new Object[]{min, max};
                    scanner.nextLine();
                    break;
                case "ordinal":
                    System.out.print("Opcions (separades per coma): ");
                    String ordOpcions = scanner.nextLine();
                    java.util.Vector<String> vectorOpc = new java.util.Vector<>(List.of(ordOpcions.split(",")));
                    params = new Object[]{vectorOpc};
                    break;
                case "nominal_unica":
                    System.out.print("Opcions (separades per coma): ");
                    String nomUOpcions = scanner.nextLine();
                    java.util.Set<String> setUOpc = new java.util.HashSet<>(List.of(nomUOpcions.split(",")));
                    params = new Object[]{setUOpc};
                    break;
                case "nominal_multiple":
                    System.out.print("Opcions (separades per coma): ");
                    String nomMOpcions = scanner.nextLine();
                    java.util.Set<String> setMOpc = new java.util.HashSet<>(List.of(nomMOpcions.split(",")));
                    System.out.print("qMax: ");
                    int qMax = scanner.nextInt();
                    params = new Object[]{setMOpc, qMax};
                    scanner.nextLine();
                    break;
            }
            Enquesta enquesta = ctrlDomini.getEnquesta(idEnquesta);
            Integer nouIdPregunta = enquesta.creaPregunta(tipus, enunciat, params, null);
            System.out.println("Pregunta afegida amb ID: " + nouIdPregunta);
        } catch (Exception e) {
            System.out.println("Error creant pregunta: " + e.getMessage());
        }
    }

    private int llegirEnter(String missatge) {
        System.out.print(missatge);
        while (!scanner.hasNextInt()) {
            System.out.println("Has d'introduir un número!");
            scanner.next();
            System.out.print(missatge);
        }
        return scanner.nextInt();
    }
}
