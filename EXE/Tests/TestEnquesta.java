package EXE.Tests;
import domini.CtrlDomini;
import domini.Enquesta;
import domini.Usuari;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.Vector;

import static org.junit.Assert.*;
/**
 * Tests de la classe Enquesta. Comprova:
 *  - constructora vàlida i excepcions amb paràmetres invàlids (títol nul/buit, idCreador nul, ctrl nul)
 *  - modificació de títol (vàlid i invàlid)
 *  - creació de preguntes de tots els tipus (integer, text, ordinal, nominal única, nominal múltiple)
 *  - inserció de preguntes en posicions concretes i fora de rang
 */
public class TestEnquesta
{

    private CtrlDomini ctrlDomini;
    private Usuari usuari;
    private Enquesta enquesta;

    @Before
    public void setUp()
    {
        ctrlDomini = new CtrlDomini();
        usuari = ctrlDomini.creaUsuari("Arnau", "arnau@exemple.cat");
        enquesta = new Enquesta(1,"Títol prova", usuari.getId(), ctrlDomini);
    }

    @Test
    public void testConstructoraValida() {
        assertEquals(Integer.valueOf(1), enquesta.getIdE());
        assertEquals("Títol prova",enquesta.getTitol());
        assertEquals(usuari.getId()  ,enquesta.getIdCreador());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructoraTitolNul(){
        new Enquesta(  2, null,usuari.getId(),ctrlDomini);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructoraTitolBuit(){
        new Enquesta(3, "   ", usuari.getId(), ctrlDomini);
    }

    @Test (expected=IllegalArgumentException.class)
    public void testConstructoraIdCreadorNul() {
        new Enquesta(4, "Títol", null, ctrlDomini);
    }

    @Test(expected = IllegalArgumentException.class  )
    public void testConstructoraCtrlDominiNul(){
        new Enquesta (5, "Títol",usuari.getId(),null);
    }

    @Test
    public void testModificaTitol(){
        enquesta.modificarTitol("Nou títol");
        assertEquals("Nou títol", enquesta.getTitol() );
    }

    @Test(expected = IllegalArgumentException.class)
    public void testModificaTitolInvalid()
    {
        enquesta.modificarTitol ("   ");
    }

    // Integer: Object[]{min, max}
    @Test
    public void testCreaPreguntaNum() {
        Object[] params = new Object[]{0, 120};
        Integer idP = enquesta.creaPregunta("integer", "Edat", params, null);
        assertNotNull(idP);
        assertTrue(enquesta.getIdsPreguntes().contains(idP));
    }

    // Nominal múltiple: Object[]{Set<String> opcions, Integer qMax}
    @Test
    public void testCreaPreguntaMulti() {
        Set<String> opcions = new HashSet<>(Arrays.asList("A", "B", "C"));
        Object[] params = new Object[]{opcions, 2};
        Integer idP = enquesta.creaPregunta("nominal_multiple", "Tria fins a 2", params, null);
        assertNotNull(idP);
        assertTrue(enquesta.getIdsPreguntes().contains(idP));
    }

    // Text sense paràmetres
    @Test
    public void testCreaPreguntaText(){
        Integer idP = enquesta.creaPregunta("text", "Comentari", null, null);
        assertNotNull(idP);
        assertTrue(enquesta.getIdsPreguntes().contains(idP));

    }

    //Ordinal
    @Test
    public void testCreaPreguntaOrdinal()
    {
        Vector<String> opcions = new Vector<>();
        opcions.add("Molt malament");
        opcions.add("Malament");
        opcions.add("Normal");
        opcions.add("Bé");
        opcions.add("Molt bé");
        Object[] params = new Object[]{opcions};
        Integer idP = enquesta.creaPregunta("ordinal", "Valoració", params, null);
        assertNotNull(idP);
        assertTrue(enquesta.getIdsPreguntes().contains(idP));
    }

    //Nominal única
    @Test
    public void testCreaPreguntaNominalUnica() {
        Set<String> opcions = new HashSet<>(Arrays.asList( "Vermell","Verd" ,  "Blau") );
        Object[] params = new Object[]{opcions};
        Integer idP=enquesta.creaPregunta( "nominal_unica", "Color preferit", params, null);
        assertNotNull(idP );
        assertTrue(enquesta.getIdsPreguntes().contains(idP) );
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreaPreguntaTipusInvalid() {
        enquesta.creaPregunta("num_invalid", "X", null, null);
    }

    @Test
    public void testAfegeixAUnaPosicio() {
        Integer p1 = enquesta.creaPregunta("text", "Q1", null, null);
        Integer p2 = enquesta.creaPregunta("text", "Q2", null, null);
        Integer p3 = enquesta.creaPregunta("text", "Q3", null, 1);

        assertEquals(p1, enquesta.getIdsPreguntes().get(0));
        assertEquals(p3, enquesta.getIdsPreguntes().get(1));
        assertEquals(p2, enquesta.getIdsPreguntes().get(2));
    }

    //Inserció fora de rang
    @Test(expected = IndexOutOfBoundsException.class )
    public void testInsercioPosicioForaRang() {
        enquesta.creaPregunta("text" ,   "Q", null, 5);
     }

}
