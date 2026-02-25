package persistencia;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;

public class GestorFitxersEnquesta {
    private final String PATH_BASE = "../../DATA/";

    public void guardarEnquesta(String titol, ArrayList<ArrayList<String>> dadesCSV) throws IOException {

        String fileName = titol.replaceAll("[^a-zA-Z0-9_\\-]", "_") + ".csv";
        String fullPath = PATH_BASE + fileName;

        try (PrintWriter pw = new PrintWriter(new FileWriter(fullPath))) {

            for (ArrayList<String> fila : dadesCSV) {
                pw.println(String.join(",", fila));
            }

            System.out.println("Fitxer de l'enquesta exportat amb èxit a: " + fullPath);

        } catch (IOException e) {
            throw new IOException("Error d'E/S en guardar l'enquesta '" + titol + "': " + e.getMessage(), e);
        }
    }
}