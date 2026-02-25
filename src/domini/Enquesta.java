package domini;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Classe Enquesta
 * Representa una enquesta creada per un usuari amb un conjunt de preguntes(identificades per un id)
 */


public class Enquesta
{

    private final Integer idE; //Identificador únic de l'enquesta
    private String titol;      //Títol de l'enquesta

    //Llista d'identificadors de les preguntes d'aquesta enquesta (en ordre)
    private final ArrayList<Integer> idsPreguntes;

    //Identificador de l'usuari creador de l'enquesta
    private final Integer idCreador;

    //Referència al controlador de domini per obtenir objectes reals
    private final CtrlDomini ctrlDomini;

    /**
     * Constructora d'Enquesta.
     */
    public Enquesta(Integer idE, String titol, Integer idCreador, CtrlDomini ctrlDomini){
        if (idE==null) throw new IllegalArgumentException("Error: idE no pot ser nul.");
        if (titol== null ||titol.isBlank()) throw new IllegalArgumentException ("Error: titol no pot ser nul ni buit.");
        if (idCreador == null) throw new IllegalArgumentException ("Error: idCreador no pot ser nul.");
        if (ctrlDomini ==null)throw new IllegalArgumentException ("Error: CtrlDomini no pot ser nul.");
        if (!ctrlDomini.existeixUsuari(idCreador)) {
            throw new IllegalArgumentException("Error: l'usuari creador amb id " + idCreador + " no existeix.");
        }

        this.idE =idE;
        this.titol=titol;
        this.idCreador=idCreador;
        this.ctrlDomini = ctrlDomini;
        this.idsPreguntes = new ArrayList<>();
    }

    //Getters
    public Integer getIdE() { return idE;}
    public String getTitol() {return titol; }
    public Integer getIdCreador(){return idCreador; }
    public List<Integer> getIdsPreguntes() { return Collections.unmodifiableList(idsPreguntes);}
    public int getNumPreguntes(){  return idsPreguntes.size();  }

    //Retorna la llista d’objectes "Pregunta" de l'enquesta
    public ArrayList<Pregunta> getPreguntes(){
        ArrayList<Pregunta> preguntes= new ArrayList<>();
        for(Integer idP : idsPreguntes){
            preguntes.add(ctrlDomini.getPregunta(idP));
        }
        return preguntes;
    }


    //Retorna el creador Usuari
    public Usuari getCreador()
    {
        return ctrlDomini.getUsuari(idCreador);
    }

    //Setters
    public void setTitol(String nouTitol){
        if (nouTitol==null || nouTitol.isBlank())
            throw new IllegalArgumentException ("Error: el títol no pot ser nul ni buit.");
        this.titol =  nouTitol;
    }

    public void modificarTitol(String nouTitol){
        if (nouTitol == null || nouTitol.isBlank()) {
            throw new IllegalArgumentException( "Error: el títol no pot ser nul ni buit.");
        }

        this.titol= nouTitol;
    }


    //Operacions sobre les preguntes (per id)
    public void afegeixPregunta( Integer idPregunta )
    {
        if(idPregunta == null)
            throw new IllegalArgumentException("Error: l'id de la pregunta no pot ser nul.");
        if (idsPreguntes.contains(idPregunta))
            throw new IllegalArgumentException  ("Error: la pregunta ja és present." );
        if (ctrlDomini.getPregunta(idPregunta) == null)
            throw new IllegalArgumentException("Error: la pregunta no existeix.");

        idsPreguntes.add(idPregunta);

    }

    /*
     Crea una nova Pregunta, la registra al domini (assignant-li id) i l’afegeix a l’enquesta.
     Retorna l’id de la pregunta creada.
     */

    public Integer creaPregunta(String tipus,
                                String enunciat,
                                Object[] params ,
                                Integer index )
    {
        if (tipus == null ||tipus.isBlank())
            throw new IllegalArgumentException("Error: el tipus no pot ser nul ni buit.");
        if (enunciat==null || enunciat.isBlank())
            throw new IllegalArgumentException ("Error: l'enunciat no pot ser nul ni buit.");

        String t =tipus.trim().toLowerCase();
        Pregunta registrada;

        switch(t ) {
            case "integer":
            {
                if(params == null || params.length < 2
                        || !(params[0] instanceof Integer) || !(params[1] instanceof Integer))
                    throw new IllegalArgumentException ("Error: cal min i max (Integer) per a integer.");
                int min =(Integer) params[0];
                int max=(Integer) params[1];
                if (min > max) throw new IllegalArgumentException("Error: min no pot ser > max.");
                registrada= ctrlDomini.creaPreguntaInteger (enunciat, min, max);
                break;

            }
            case "ordinal":{
                if(params == null || params.length < 1 || !(params[0] instanceof java.util.Vector))
                    throw new IllegalArgumentException("Error: cal Vector<String> d'opcions per a ordinal.");
                @SuppressWarnings("unchecked")
                java.util.Vector<String> opcions= (java.util.Vector<String>) params[0];
                if(opcions == null ||  opcions.size() <2 )
                    throw new IllegalArgumentException("Error: calen almenys 2 opcions.");
                registrada= ctrlDomini.creaPreguntaOrdinal(enunciat, opcions);
                break;
            }

            case "nominal_unica":{
                if (params ==null|| params.length < 1 || !(params[0] instanceof java.util.Set))
                    throw new IllegalArgumentException("Error: cal Set<String> d'opcions per a nominal_unica.");
                @SuppressWarnings("unchecked")
                java.util.Set<String> opcions = (java.util.Set<String>) params[0];
                if (opcions==null || opcions.size()< 2)
                    throw new IllegalArgumentException  ("Error: calen almenys 2 opcions.");
                registrada = ctrlDomini.creaPreguntaNominalUnica(enunciat, opcions);
                break;

            }
            case "nominal_multiple" :{
                if (params == null|| params.length < 2
                        || !(params[0]  instanceof java.util.Set) || !(params[1] instanceof Integer))
                    throw new IllegalArgumentException("Error: cal Set<String> i qMax (Integer) per a nominal_multiple.");
                @SuppressWarnings("unchecked")
                java.util.Set<String> opcions=(java.util.Set<String>) params[0];
                Integer qMax = (Integer) params[1];
                if (opcions == null ||  opcions.size()<2)
                    throw new IllegalArgumentException ("Error: calen almenys 2 opcions.");
                if (qMax==null || qMax<= 0 || qMax >opcions.size())
                    throw new IllegalArgumentException ("Error: qMax ha d'estar entre 1 i nombre d'opcions.");
                registrada = ctrlDomini.creaPreguntaNominalMult(enunciat, opcions, qMax);
                break;

            }
            case "text": {
                registrada=ctrlDomini.creaPreguntaText(enunciat);
                break;

            }
            default:

                throw new IllegalArgumentException("Error: tipus de pregunta desconegut: " + tipus);
        }


        Integer idP = registrada.getId();
        if(idsPreguntes.contains(idP))
            throw new IllegalArgumentException("Error: la pregunta ja és present.");
        if(index == null) {
            idsPreguntes.add(idP);
        }
        else
        {
            if(index <0 || index >idsPreguntes.size())
                throw new IndexOutOfBoundsException("Índex fora de rang.");
            idsPreguntes.add (index ,idP);
        }
        return idP;
    }

    public boolean eliminaPregunta(Integer idPregunta){
        if ( idPregunta == null) return false;
        return idsPreguntes.remove(idPregunta);
    }

    public Integer eliminaPregunta(int index ){
        return idsPreguntes.remove(index);

    }

    public void modificaPregunta(int index ,Integer nouIdPregunta )
    {
        if (nouIdPregunta == null )
            throw new IllegalArgumentException("Error: nou id no pot ser nul.");
        if (index < 0 || index>=idsPreguntes.size())
            throw new IndexOutOfBoundsException("Índex fora de rang.");
        if (idsPreguntes.contains(nouIdPregunta) && !idsPreguntes.get(index).equals(nouIdPregunta))
            throw new IllegalArgumentException( "Error: la nova pregunta ja és present.");
         idsPreguntes.set(index, nouIdPregunta);

    }
}

