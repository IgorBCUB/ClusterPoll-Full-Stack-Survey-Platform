package persistencia;

import java.util.ArrayList;
import java.io.IOException; // Necessari per a guardarEnquesta

public class CtrlPersistencia {
    private GestorFitxersUsuari gestorUsuaris;
    private GestorFitxersEnquesta gestorEnquestes; // DECLARACIÓ

    public CtrlPersistencia() {
        gestorUsuaris = new GestorFitxersUsuari();
        gestorEnquestes = new GestorFitxersEnquesta(); // INICIALITZACIÓ
    }

    public ArrayList<ArrayList<String>> carregarUsuaris() {
        return gestorUsuaris.carregarUsuaris();
    }

    public void guardarUsuaris(ArrayList<ArrayList<String>> usuaris) {
        gestorUsuaris.guardarUsuaris(usuaris);
    }

    /**
     * Mètode per guardar les dades d'una enquesta, delegant la tasca.
     */
    public void guardarEnquesta(String titol, ArrayList<ArrayList<String>> dadesCSV) throws IOException {
        // La crida a gestorEnquestes.guardarEnquesta() ara funciona.
        gestorEnquestes.guardarEnquesta(titol, dadesCSV);
    }

    public ArrayList<ArrayList<String>> importarEnquestaDesdeCSV(String filePath) {
        CsvImporter importer = new CsvImporter();
        return importer.importarEnquestaDesdeCSV(filePath);
    }

}