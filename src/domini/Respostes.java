package domini;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Objects;

public class Respostes {
    private final int idE;
    private final int idU;
    private final ArrayList<Resposta> respostes; //Conjunt de respostes d'aquest usuari per aquesta enquesta
    private final CtrlDomini ctrlDomini;

    //Creadora principal amb llista de Resposta.
    public Respostes(int idE, int idU, ArrayList<Resposta> respostes, CtrlDomini ctrlDomini) {
        if (idE < 0) throw new IllegalArgumentException("idE no pot ser negatiu");
        if (idU < 0) throw new IllegalArgumentException("idU no pot ser negatiu");
        this.ctrlDomini = ctrlDomini; // si és opcional, no cal comprovar-ho

        Objects.requireNonNull(respostes, "la llista de respostes no pot ser nul·la");
        this.respostes = respostes;

        this.idE = idE;
        this.idU = idU;
    }

    // Creadora buida.
    public Respostes(int idE, int idU, CtrlDomini ctrlDomini) {
        if (idE < 0) throw new IllegalArgumentException("idE no pot ser negatiu");
        if (idU < 0) throw new IllegalArgumentException("idU no pot ser negatiu");
        this.idE = idE;
        this.idU = idU;
        this.ctrlDomini = ctrlDomini;
        this.respostes = new ArrayList<>();
    }

    //Afegeix una nova resposta al conjunt de respostes
    public void afegeixResposta(ArrayList<String> contingut, Integer IdP) {

        Pregunta p = ctrlDomini.getPregunta(IdP);
        String tipus = p.getTipus();
        Resposta resposta = new Resposta(IdP, contingut, ctrlDomini);
        respostes.add(resposta);
    }

    public int getIdE() { return idE; }
    public int getIdU() { return idU; }

    //Retorna la llista de respostes
    public ArrayList<Resposta> getRespostes() {
        return respostes;
    }

    //Calcula la distància entre dues respostes
    public double calcula_distanciaRespostes(Resposta r1, Resposta r2) {

        if (!r1.getTipus().equals(r2.getTipus()))
            throw new IllegalArgumentException("No es pot calcular distància entre tipus diferents");

        double d = r1.calcula_distanciaRespostes(r2);
        if (Double.isNaN(d) || Double.isInfinite(d))
            throw new IllegalStateException("La distància calculada no és vàlida (NaN/Inf)");
        return d;
    }
}