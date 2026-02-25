package domini;

import java.util.Scanner;

public class DriverDomain {
    private CtrlDomini ctrlDomini;
    private Scanner scanner;

    // Instàncies dels drivers específics
    private DriverUsuari driverUsuari;
    private DriverEnquesta driverEnquesta;
    private DriverPregunta driverPregunta;
    private DriverResposta driverResposta;
    private DriverKMedoids driverKMedoids;
    private DriverCSVClustering driverCSV;//Per fer el k-means i k-medoids
    private DriverCSVImporter driverCSVImporter;//Per importar enquestes
    public DriverDomain() {
        this.ctrlDomini = new CtrlDomini();
        this.scanner = new Scanner(System.in);

        //Inicialitzar els drivers
        this.driverUsuari = new DriverUsuari(ctrlDomini, scanner);
        this.driverEnquesta = new DriverEnquesta(ctrlDomini, scanner);
        this.driverPregunta = new DriverPregunta(ctrlDomini, scanner);
        this.driverResposta = new DriverResposta(ctrlDomini, scanner);
        this.driverKMedoids = new DriverKMedoids(ctrlDomini, scanner);
        this.driverCSV = new DriverCSVClustering(ctrlDomini, scanner);
        this.driverCSVImporter = new DriverCSVImporter(ctrlDomini, scanner);
    }

    public void iniciar() {
        System.out.println("=== SISTEMA DE GESTIÓ D'ENQUESTES ===");
        boolean sortir = false;
        while (!sortir) {
            mostrarMenuPrincipal();
            int opcio = llegirEnter("Selecciona una opció: ");
            switch (opcio) {
                case 1:
                    driverUsuari.gestioUsuaris();
                    break;
                case 2:
                    driverEnquesta.gestioEnquestes();
                    break;
                case 3:
                    driverPregunta.gestioPreguntes();
                    break;
                case 4:
                    driverResposta.gestioRespostes();
                    break;
                /*case 5:
                    driverKMedoids.gestioKMedoids();
                    break;*/
                case 5:
                    driverCSV.menu();
                    break;
                case 6:
                    driverCSVImporter.menuImportacionCSV();
                    break;
                case 0:
                    ctrlDomini.tancarAplicacio();
                    sortir = true;
                    System.out.println("Gràcies per utilitzar el sistema!");
                    break;
                default:
                    System.out.println("Opció no vàlida!");
            }
        }
        scanner.close();
    }

    private void mostrarMenuPrincipal() {
        System.out.println("\n--- MENÚ PRINCIPAL ---");
        System.out.println("1. Gestió d'Usuaris");
        System.out.println("2. Gestió d'Enquestes");
        System.out.println("3. Gestió de Preguntes");
        System.out.println("4. Gestió de Respostes");
        //System.out.println("5. Gestió de K-Medoids");
        System.out.println("5. Clustering (KMeans/K-Medoids)");
        System.out.println("6. NouImportCSV");
        System.out.println("0. Sortir");
    }

    //Serveix per basicament fer un print d'un missatge i lleguir un enter, l'he fet així ya que repetia moltes vegades aquest patró en molts llocs (reusar codi).
    private int llegirEnter(String missatge) {
        System.out.print(missatge);
        while (!scanner.hasNextInt()) {
            System.out.println("Has d'introduir un número!");
            scanner.next();
            System.out.print(missatge);
        }
        int valor = scanner.nextInt();
        scanner.nextLine(); // netejar buffer
        return valor;
    }
}