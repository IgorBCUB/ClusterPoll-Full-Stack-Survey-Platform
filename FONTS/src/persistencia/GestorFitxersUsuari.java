package persistencia;

import java.io.*;
import java.util.*;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;

public class GestorFitxersUsuari {
    private final String path = "../../DATA/FitxerUsuaris.csv";

    public ArrayList<ArrayList<String>> carregarUsuaris()
    {
        ArrayList<ArrayList<String>> usuaris = new ArrayList<>();

        File fitxer = new File(path);
        // Si el fitxer no existeix, retornem la llista buida
        if (!fitxer.exists()) return usuaris;

        try (BufferedReader br = new BufferedReader(new FileReader(fitxer))) {
            String linia;

            // Llegim el fitxer línia a línia
            while ((linia = br.readLine()) != null) {

                // Ignorem línies buides
                if (!linia.isBlank()) {
                    // Separem els camps per comes
                    String[] parts = linia.split(",");

                    ArrayList<String> usuari = new ArrayList<>();

                    // Afegim cada camp (id, nom, correu, ...) a la llista
                    for (String camp : parts) {
                        usuari.add(camp);
                    }

                    // Afegim l'usuari a la llista global
                    usuaris.add(usuari);
                }
            }

        } catch (IOException e) {
            System.out.println("Error llegint FitxerUsuaris.csv");
        }

        return usuaris;
    }
    public void guardarUsuaris(ArrayList<ArrayList<String>> usuaris)
    {
        try (PrintWriter pw = new PrintWriter(new FileWriter(path))) {

            // Cada usuari és una línia del CSV, camps separats per comes
            for (ArrayList<String> usuari : usuaris) {
                pw.println(String.join(",", usuari));
            }

        } catch (IOException e) {
            System.out.println("Error escrivint FitxerUsuaris.csv");
        }
    }

}
