package EXE.Tests;
import domini.CtrlDomini;
import domini.KMedoids;
import domini.PreguntaInteger;
import domini.PreguntaNominalMult;
import domini.PreguntaNominalUnica;
import domini.PreguntaOrdinal;
import domini.PreguntaText;
import domini.Resposta;
import domini.Respostes;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Vector;
import java.util.Map;

import static org.junit.Assert.*;
/**
 * Tests de KMedoids amb un dataset complet (integer, ordinal, nominal única, nominal múltiple, text).
 * Comprova:
 *  - execució correcta amb 2 clústers i assignació de medoids
 *  - clusters resultants coherents amb els medoids
 *  - cost total calculat correctament
 *  - excepcions quan k > n o estructura de Respostes inconsistent
 */

public class TestKMedoids {

    private CtrlDomini ctrlDomini;

    //Una pregunta de cada tipus
    private PreguntaText preguntaText;
    private PreguntaInteger preguntaInteger;
    private PreguntaOrdinal preguntaOrdinal;
    private PreguntaNominalUnica preguntaNominalUnica;
    private PreguntaNominalMult preguntaNominalMult;

    @Before
    public void setUp(){
        ctrlDomini=new CtrlDomini();

        //TEXT
        preguntaText =ctrlDomini.creaPreguntaText (
                "Text lliure"

        );

        //INTEGER
        preguntaInteger=ctrlDomini.creaPreguntaInteger(
                "Valor enter"  ,0, 10
        );

        //ORDINAL
        Vector<String >opcionsOrdinal = new Vector<>();
        opcionsOrdinal.add ("Molt dolent");  //0
        opcionsOrdinal.add ( "Dolent");       //1
        opcionsOrdinal.add(  "Normal");       //2
        opcionsOrdinal.add("Bo");           //3
        opcionsOrdinal.add("Molt bo" );      //4
        preguntaOrdinal =ctrlDomini.creaPreguntaOrdinal(
                "Satisfacció"  ,opcionsOrdinal

        );

        //NOMINAL ÚNICA{A,B}
        Set<String>opcionsNomUnica=new  HashSet<>(Arrays.asList("A", "B"));
        preguntaNominalUnica =ctrlDomini.creaPreguntaNominalUnica(
                "Categoria única",  opcionsNomUnica
        );

        //NOMINAL MÚLTIPLE
        Set <String> opcionsNomMult= new HashSet<>(Arrays.asList("X", "Y"));

        preguntaNominalMult=  ctrlDomini.creaPreguntaNominalMult(
                "Categories múltiples", opcionsNomMult, 2

        );
    }

    //Crea Respostes per a un usuari amb una resposta de cada tipus

    private Respostes creaRespostesUsuariComplet(
            int idE,int idU,
            int valorInteger ,
            int valorOrdinal  ,
            String valorNomUnica,

            List<String> valorNomMultiple,
            String valorText
    )
    {
        ArrayList<Resposta> llistaRespostes = new ArrayList<>();

        //INTEGER
        ArrayList<String>contingutInt = new ArrayList<>();
        contingutInt.add (String.valueOf(valorInteger));
        llistaRespostes.add (
                new Resposta( preguntaInteger.getId(), contingutInt, ctrlDomini)

        );

        //ORDINAL
        ArrayList<String>contingutOrd = new ArrayList<>();
        contingutOrd.add( String.valueOf(valorOrdinal));
        llistaRespostes.add (

                new Resposta(preguntaOrdinal.getId(), contingutOrd, ctrlDomini)
        );

        //NOMINAL ÚNICA
        ArrayList<String> contingutNomUnica = new ArrayList<>();
        contingutNomUnica.add(valorNomUnica);
        llistaRespostes.add(
                new Resposta( preguntaNominalUnica.getId(), contingutNomUnica, ctrlDomini)

        );

        //NOMINAL MÚLTIPLE
        ArrayList <String> contingutNomMult = new ArrayList<>(valorNomMultiple);
        llistaRespostes.add (
                new Resposta( preguntaNominalMult.getId(), contingutNomMult,ctrlDomini)
        );

        //TEXT
        ArrayList<String>contingutText = new ArrayList<>();
        contingutText.add (valorText);
        llistaRespostes.add (
                new Resposta (preguntaText.getId(), contingutText, ctrlDomini)
        );

        return new Respostes (idE, idU, llistaRespostes, ctrlDomini);
    }

    //Cas bàsic amb tots els tipus de preguntes:
    @Test
    public void testExecutaClusteringAmbTotsElsTipus()
    {
        List<Respostes> dades=new ArrayList<>();

        //Clúster 1
        //usuari 0
        dades.add(
                creaRespostesUsuariComplet(
                        1 ,0,
                        0 ,          //integer
                        0,          //ordinal
                        "A",        //nominal_unica
                        Arrays.asList("X"), //nominal_multiple
                        "aaaa"      //text
                )
        );

        //usuari 1
        dades.add (

                creaRespostesUsuariComplet(
                        1, 1,
                        1 ,          //integer
                        1,          //ordinal
                        "A",        //nominal_unica
                        Arrays.asList( "X"), //nominal_multiple
                        "aaab"      //text
                )

        );

        //Clúster 2
        //USuari 2
        dades.add (
                creaRespostesUsuariComplet(
                        1,2,
                        9 ,          //integer
                        3,          //ordinal
                        "B",        //nominal_unica
                        Arrays.asList("Y")  , //nominal_multiple
                        "zzzz"      //text
                )
        );

        //Usuari 3
        dades.add (
                creaRespostesUsuariComplet(
                        1,3,
                        10,         //integer
                        4,          //ordinal
                        "B",        //nominal_unica
                        Arrays.asList( "Y"), //nominal_multiple
                        "zzzy"      //text

                )
        );

        //idAlgorisme =1, k = 2
        KMedoids kmedoids=new KMedoids (ctrlDomini, 1, 2,dades);

        Object resultatObjecte = kmedoids.executa(20,   true);

        assertTrue(resultatObjecte instanceof KMedoids.Resultat);

        KMedoids.Resultat resultat  = ( KMedoids.Resultat) resultatObjecte;

        //Comprovem medoids: inicialització de KMedoids amb n=4,k=2 i dona [0,3]
        List<Integer> medoids=resultat.getMedoids();
        assertEquals(2,medoids.size());
        assertTrue( medoids.contains(0));
        assertTrue(medoids.contains(3));

        //Comprovem clusters
        @SuppressWarnings("unchecked" )
        Map<Integer, List<Integer>> clusters=
                (Map<Integer, List<Integer>>) resultat.getClusters();

        assertEquals(2,clusters.size());

        List<Integer> cluster0 = clusters.get(0);

        List<Integer> cluster3 =clusters.get(3);

        assertNotNull(cluster0 );
        assertNotNull(cluster3);

        //Cluster del medoid 0(Usuaris 0 i 1)
        assertTrue(cluster0.contains(0));

        assertTrue(cluster0.contains(1));
        assertEquals(2, cluster0.size() );

        //Cluster del medoid 3(Usuaris 2 i 3)
        assertTrue (cluster3.contains(2));
        assertTrue(cluster3.contains(3));
        assertEquals(2, cluster3.size());


        //El cost total és la suma de les distàncies de cada punt al seu medoid

        double cost = resultat.getCostTotal();
        assertEquals(0.24, cost, 1e-6 );
    }

    /**
     * Igual que abans: k més gran que el nombre de Respostes
     * ha de provocar una IllegalArgumentException a executa().
     */

    @Test(expected=IllegalArgumentException.class)
    public void  testKForaDeRang() {
        List<Respostes> dades= new ArrayList<>();
        dades.add (

                creaRespostesUsuariComplet(
                        1, 0, 0, 0, "A", Arrays.asList("X"), "aaa"
                )
        );
        dades.add (
                creaRespostesUsuariComplet(
                        1, 1, 10,4, "B", Arrays.asList( "Y" ), "zzz"
                )
        );

        //k = 3 > n = 2
        KMedoids kmedoids=new KMedoids(ctrlDomini, 1, 3, dades);
        kmedoids.executa (10, true);
    }

    /**
     * Estructura inconsistent de Respostes (mida diferent de la llista de Resposta)
     * ha de fallar al constructor de KMedoids a través de Algorisme.setRespostes().
     */
    @Test(expected = IllegalArgumentException.class )
    public void testEstructuraRespostesInconsistent()
    {
        Respostes r1 = creaRespostesUsuariComplet(
                1, 0, 0, 0, "A", Arrays.asList("X"), "aaaa"
        );
        ArrayList<Resposta> llistaR2 = new ArrayList<>();


        ArrayList<String> cInt=new ArrayList<>();
        cInt.add("5");

        llistaR2.add(

                new Resposta(preguntaInteger.getId(), cInt, ctrlDomini)
        );

        Respostes r2 =new Respostes(1, 1,llistaR2,  ctrlDomini);

        List<Respostes> dades =new ArrayList<>();
        dades.add(r1);
        dades.add(r2);

        new KMedoids(ctrlDomini , 1, 1, dades);
    }

}
