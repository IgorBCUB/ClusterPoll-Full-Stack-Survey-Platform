package domini;

import java.util.ArrayList;
import java.util.Vector;

public class Resposta {
    private final Integer IdP;//Pregunta a la que respon
    private ArrayList<String> contingut;//Contingut de la resposta
    private final CtrlDomini ctrlDomini;

    //Creadora
    public Resposta(Integer IdP, ArrayList<String> contingut, CtrlDomini ctrlDomini) {
        this.IdP = IdP;
        this.contingut = contingut;
        this.ctrlDomini = ctrlDomini;
    }
    public Integer getIdPregunta() { return IdP; }
    public ArrayList<String> getContingut() { return contingut; }

    //Retorna el tipus de la pregunta a la que respon aquesta resposta
    public String getTipus() {
        Pregunta pregunta = ctrlDomini.getPregunta(IdP);
        return pregunta.getTipus();
    }

    //Calcula la distància entre aquesta resposta i la resposta r
    public double calcula_distanciaRespostes(Resposta r) {
        Pregunta pregunta = ctrlDomini.getPregunta(this.IdP);
        if (this.getTipus().equals("integer")){
            int v1 = Integer.parseInt(this.contingut.get(0));
            int v2 = Integer.parseInt(r.contingut.get(0));

            double numerator = Math.abs(v1 - v2);
            double denominador = pregunta.getMaxValue() - pregunta.getMinValue();
            return numerator/denominador;
        }
        else if (this.getTipus().equals("ordinal")){
            int v1 = Integer.parseInt(this.contingut.get(0));
            int v2 = Integer.parseInt(r.contingut.get(0));
            double numerator = Math.abs(v1 - v2);
            double denominador = pregunta.getNumOpcions() - 1;
            return numerator/denominador;
        }
        else if (this.getTipus().equals("nominal_unica")){
            if (this.contingut.get(0) == r.contingut.get(0)){
                return 0;
            }
            else {
                return 1;
            }
        }
        else if (this.getTipus().equals("nominal_multiple")){
            int interseccio = 0;
            for (String opcio1 : this.contingut){
                for (String opcio2 : r.contingut){
                    if (opcio1.equals(opcio2)){
                        interseccio += 1;
                    }
                }
            }
            int union = this.contingut.size() + r.contingut.size() - interseccio;
            double result = ((double) interseccio /union);
            return 1- result;
        }
        else{
            String s1 = this.contingut.isEmpty() ? "" : this.contingut.get(0);
            String s2 = r.contingut.isEmpty()    ? "" : r.contingut.get(0);

            int L = Math.max(s1.length(), s2.length());
            if (L == 0) return 0.0; // ambdues buides

            int lv = levenshtein(s1, s2);
            return lv / (double) L; // normalització estàndard
        }
    }
    // Càlcul de la distància de Levenshtein entre dues cadenes
    private static int levenshtein(String s1, String s2) {
        int n = s1.length(), m = s2.length();
        int[][] dp = new int[n + 1][m + 1];
        for (int i = 0; i <= n; i++) dp[i][0] = i;
        for (int j = 0; j <= m; j++) dp[0][j] = j;
        for (int i = 1; i <= n; i++) {
            char c1 = s1.charAt(i - 1);
            for (int j = 1; j <= m; j++) {
                char c2 = s2.charAt(j - 1);
                int cost = (c1 == c2) ? 0 : 1;
                dp[i][j] = Math.min(
                        Math.min(dp[i - 1][j] + 1, dp[i][j - 1] + 1),
                        dp[i - 1][j - 1] + cost
                );
            }
        }
        return dp[n][m];
    }
}