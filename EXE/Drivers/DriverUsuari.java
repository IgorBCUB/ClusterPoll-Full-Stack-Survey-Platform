package domini;
import java.util.Scanner;
import java.util.ArrayList;

class DriverUsuari {
    private CtrlDomini ctrlDomini;
    private Scanner scanner;

    public DriverUsuari(CtrlDomini ctrlDomini, Scanner scanner) {
        this.ctrlDomini = ctrlDomini;
        this.scanner = scanner;
    }

    public void gestioUsuaris() {
        boolean tornar = false;
        while (!tornar) {
            System.out.println("\n--- GESTIÓ D'USUARIS ---");
            System.out.println("1. Crear usuari");
            System.out.println("2. Llistar usuaris");
            System.out.println("3. Consultar usuari");
            System.out.println("4. Modificar usuari");
            System.out.println("5. Crear enquesta per usuari");
            System.out.println("6. LListar les enquestes fetes per un usuari");
            System.out.println("0. Tornar al menú principal");

            int opcio = llegirEnter("Selecciona una opció: ");

            switch (opcio) {
                case 1:
                    crearUsuari();
                    break;
                case 2:
                    llistarUsuaris();
                    break;
                case 3:
                    consultarUsuari();
                    break;
                case 4:
                    modificarUsuari();
                    break;
                case 5:
                    crearEnquestaUsuari();
                    break;
                case 6:
                    LListarEnquestesUsuari();
                    break;
                case 0:
                    tornar = true;
                    break;
                default:
                    System.out.println("Opció no vàlida!");
            }
        }
    }

    private void crearUsuari() {
        try {
            System.out.print("Nom de l'usuari: ");
            String nom = scanner.nextLine();
            System.out.print("Email: ");
            String email = scanner.nextLine();
            Usuari usuari = ctrlDomini.creaUsuari(nom, email);
            System.out.println("Usuari creat amb ID: " + usuari.getId());
        } catch (IllegalArgumentException e) {//Captem l'excepcio que es produira a la creadora
            System.out.println("Error de validació: " + e.getMessage());
        }
    }

    private void llistarUsuaris() {
        try {
            System.out.println("\nOrdre de sortida:");
            System.out.println("\nidUsuari, Nom, Email");
            ArrayList<Usuari> usuaris = ctrlDomini.getallUsuaris();
            for(int i = 0; i < usuaris.size(); i++) {
                Usuari usuari = usuaris.get(i);
                System.out.println(usuari.getId() + "        " + usuari.getNom() + "        " + usuari.getEmail());
            }
        } catch (IllegalStateException e) {
            System.out.println("Informació: " + e.getMessage());
        }
    }

    private void consultarUsuari() {
        try {
            int id = llegirEnter("ID de l'usuari a consultar: ");
            Usuari usuari = ctrlDomini.getUsuari(id);
            System.out.println("Usuari ID: " + usuari.getId());
            System.out.println("Nom: " + usuari.getNom());
            System.out.println("Email: " + usuari.getEmail());
        } catch (IllegalArgumentException | IndexOutOfBoundsException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private void modificarUsuari() {
        try {
            int id = llegirEnter("ID de l'usuari a modificar: ");
            Usuari usuari = ctrlDomini.getUsuari(id);
            System.out.print("Si no vols canviar algun atribut clica enter\n");
            System.out.print("Nou nom (actual: " + usuari.getNom() + "): ");
            String nouNom = scanner.nextLine();
            if (!nouNom.isEmpty()) {
                usuari.setNom(nouNom);
            }
            System.out.print("Nou email (actual: " + usuari.getEmail() + "): ");
            String nouEmail = scanner.nextLine();
            if (!nouEmail.isEmpty()) {
                usuari.setEmail(nouEmail);
            }
            System.out.println("Usuari modificat correctament!");
        }
        catch (IllegalArgumentException | IndexOutOfBoundsException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private void crearEnquestaUsuari() {
        try {
            Integer idUsuari = llegirEnter("ID de l'usuari: ");
            Usuari usuari = ctrlDomini.getUsuari(idUsuari);
            System.out.print("Títol de l'enquesta: ");
            String titol = scanner.nextLine();
            Enquesta enquesta = usuari.creaEnquesta(titol);
            System.out.println("Enquesta creada amb ID: " + enquesta.getIdE());
        }
        catch (IllegalArgumentException | IndexOutOfBoundsException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private void LListarEnquestesUsuari() {
        Integer idU = llegirEnter("ID de l'usuari al que vols consultar les seves enquestes: ");
        try{
            System.out.println("\nOrdre de sortida:");
            System.out.println("\nidEnquesta, Titol");
            ArrayList<Enquesta> enquestes = ctrlDomini.getEnquestesUsuari(idU);
            for(int i = 0; i < enquestes.size(); i++) {
                Enquesta enq = enquestes.get(i);
                System.out.println(enq.getIdE() + "        " + enq.getTitol());
            }
        }
        catch (IllegalArgumentException e) {
            System.out.println("Error de paràmetre: " + e.getMessage());
        }
        catch (IndexOutOfBoundsException e) {
            System.out.println("Error: L'usuari amb ID " + idU + " no existeix.");
        }
        catch (IllegalStateException e) {
            System.out.println("Informació: " + e.getMessage());
        }
        catch (Exception e) {
            System.out.println("Error inesperat: " + e.getMessage());
        }
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
        scanner.nextLine(); //Limpiar buffer (si no es fa no es printeja bé)
        return valor;
    }
}