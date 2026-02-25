package persistencia;

import java.io.*;
import java.util.*;

public class CsvImporter {

    public ArrayList<ArrayList<String>> importarEnquestaDesdeCSV(String filePath) {
        if (filePath == null || filePath.isBlank()) {
            throw new IllegalArgumentException("Error: la ruta del fitxer no pot ser buida.");
        }

        ArrayList<ArrayList<String>> dadesCSV = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String linea;
            while ((linea = br.readLine()) != null) {
                String[] camps = parseCSVLine(linea);
                dadesCSV.add(new ArrayList<>(Arrays.asList(camps)));
            }
        } catch (IOException e) {
            throw new IllegalArgumentException("Error leyendo archivo CSV: " + e.getMessage(), e);
        }

        if (dadesCSV.isEmpty()) throw new IllegalArgumentException("El archivo CSV está vacío");
        return dadesCSV;
    }

    private String[] parseCSVLine(String linea) {
        List<String> campos = new ArrayList<>();
        StringBuilder campoActual = new StringBuilder();
        boolean entreComillas = false;

        for (char c : linea.toCharArray()) {
            if (c == '"') {
                entreComillas = !entreComillas;
            } else if (c == ',' && !entreComillas) {
                campos.add(campoActual.toString().trim());
                campoActual.setLength(0);
            } else {
                campoActual.append(c);
            }
        }
        campos.add(campoActual.toString().trim());
        return campos.toArray(new String[0]);
    }
}
