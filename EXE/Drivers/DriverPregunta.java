package domini;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;
import java.util.Vector;

class DriverPregunta {
    private CtrlDomini ctrlDomini;
    private Scanner scanner;

    public DriverPregunta(CtrlDomini ctrlDomini, Scanner scanner) {
        this.ctrlDomini = ctrlDomini;
        this.scanner = scanner;
    }

    public void gestioPreguntes() {
        boolean tornar = false;
        while (!tornar) {
            System.out.println("\n--- GESTIÓ DE PREGUNTES ---");
            System.out.println("1. Crear pregunta de text");
            System.out.println("2. Crear pregunta numèrica");
            System.out.println("3. Crear pregunta opció única");
            System.out.println("4. Crear pregunta opció múltiple");
            System.out.println("5. Crear pregunta ordinal");
            System.out.println("6. Imprimir totes les preguntes");
            System.out.println("0. Tornar al menú principal");

            int opcio = llegirEnter("Selecciona una opció: ");

            switch (opcio) {
                case 1:
                    crearPreguntaText();
                    break;
                case 2:
                    crearPreguntaNumerica();
                    break;
                case 3:
                    crearPreguntaUnica();
                    break;
                case 4:
                    crearPreguntaMultiple();
                    break;
                case 5:
                    crearPreguntaOrdinal();
                    break;
                case 6:
                    imprimirTotesLesPreguntes();
                    break;
                case 0:
                    tornar = true;
                    break;
                default:
                    System.out.println("Opció no vàlida!");
            }
        }
    }

    private void crearPreguntaText() {
        System.out.print("Enunciat de la pregunta: ");
        String enunciat = scanner.nextLine();
        PreguntaText p = ctrlDomini.creaPreguntaText(enunciat);
        if (p != null)
            System.out.println("Pregunta de text creada amb ID: " + p.getId());
        else
            System.out.println("No s'ha pogut crear la pregunta de text per error de dades.");
    }

    private void crearPreguntaNumerica() {
        System.out.print("Enunciat de la pregunta: ");
        String enunciat = scanner.nextLine();
        int min = llegirEnter("Valor mínim: ");
        int max = llegirEnter("Valor màxim: ");
        PreguntaInteger p = ctrlDomini.creaPreguntaInteger(enunciat, min, max);
        if (p != null)
            System.out.println("Pregunta numèrica creada amb ID: " + p.getId());
        else
            System.out.println("No s'ha pogut crear la pregunta numèrica per error de dades.");
    }

    private void crearPreguntaUnica() {
        System.out.print("Enunciat de la pregunta: ");
        String enunciat = scanner.nextLine();
        Set<String> opcions = new HashSet<>();
        int numOpcions = llegirEnter("Quantes opcions vols afegir? ");
        for (int i = 0; i < numOpcions; i++) {
            System.out.print("Opció " + (i + 1) + ": ");
            opcions.add(scanner.nextLine());
        }

        PreguntaNominalUnica p = ctrlDomini.creaPreguntaNominalUnica(enunciat, opcions);
        if (p != null)
            System.out.println("Pregunta d'opció única creada amb ID: " + p.getId());
        else
            System.out.println("No s'ha pogut crear la pregunta d'opció única per error de dades.");
    }

    private void crearPreguntaMultiple() {
        System.out.print("Enunciat de la pregunta: ");
        String enunciat = scanner.nextLine();
        Set<String> opcions = new HashSet<>();
        int numOpcions = llegirEnter("Quantes opcions vols afegir? ");
        for (int i = 0; i < numOpcions; i++) {
            System.out.print("Opció " + (i + 1) + ": ");
            opcions.add(scanner.nextLine());
        }
        int qMax = llegirEnter("Màxim d'opcions a seleccionar: ");
        PreguntaNominalMult p = ctrlDomini.creaPreguntaNominalMult(enunciat, opcions, qMax);
        if (p != null)
            System.out.println("Pregunta d'opció múltiple creada amb ID: " + p.getId());
        else
            System.out.println("No s'ha pogut crear la pregunta d'opció múltiple per error de dades.");
    }

    private void crearPreguntaOrdinal() {
        System.out.print("Enunciat de la pregunta: ");
        String enunciat = scanner.nextLine();
        Vector<String> opcions = new Vector<>();
        int numOpcions = llegirEnter("Quantes opcions vols afegir? ");
        for (int i = 0; i < numOpcions; i++) {
            System.out.print("Opció " + (i + 1) + ": ");
            opcions.add(scanner.nextLine());
        }

        PreguntaOrdinal p = ctrlDomini.creaPreguntaOrdinal(enunciat, opcions);
        if (p != null)
            System.out.println("Pregunta ordinal creada amb ID: " + p.getId());
        else
            System.out.println("No s'ha pogut crear la pregunta ordinal per error de dades.");
    }


    private int llegirEnter(String missatge) {
        System.out.print(missatge);
        while (!scanner.hasNextInt()) {
            System.out.println("Has d'introduir un número!");
            scanner.next();
            System.out.print(missatge);
        }
        int valor = scanner.nextInt();
        scanner.nextLine();
        return valor;
    }

    private void imprimirTotesLesPreguntes() {
        System.out.println("--- Llista de totes les preguntes ---");
        for (Pregunta p : ctrlDomini.getPreguntes()) {
            System.out.println("ID: " + p.getId() +
                    " | Tipus: " + p.getTipus() +
                    " | Enunciat: " + p.getEnunciat());
            // Mostrar opcions si cal
            if (p instanceof PreguntaNominalUnica) {
                System.out.println("Opcions: " + ((PreguntaNominalUnica)p).getOpcions());
            } else if (p instanceof PreguntaNominalMult) {
                System.out.println("Opcions: " + ((PreguntaNominalMult)p).getOpcions());
            } else if (p instanceof PreguntaOrdinal) {
                System.out.println("Opcions: " + ((PreguntaOrdinal)p).getOpcions());
            }
        }
        System.out.println("--- Fi de la llista ---");
    }
}
