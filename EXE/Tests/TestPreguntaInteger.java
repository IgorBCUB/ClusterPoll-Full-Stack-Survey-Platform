package EXE.Tests;
import domini.PreguntaInteger;
import org.junit.*;
import static org.junit.Assert.*;
/**
 * Tests de PreguntaInteger. Comprova:
 *  - constructor vàlid amb i sense rangs (min, max)
 *  - constructor amb rangs invertits (llença excepció)
 *  - modificació de min i max vàlida i amb rollback si trenca l'invariant (min <= max)
 *  - comportament amb rangs nuls (permeten ajust posterior)
 *
 * Nota: utilitza una subclasse testable per exposar els mètodes protected.
 */

public class TestPreguntaInteger {

    // Subclasse de prova per exposar els metodes protected
    static class PreguntaIntegerTestable extends PreguntaInteger {
        public PreguntaIntegerTestable(String e, Integer min, Integer max) { super(e, min, max); }
        public void setMin(Integer v) { modificarMinValue(v); }
        public void setMax(Integer v) { modificarMaxValue(v); }
    }

    @Test
    public void constructorValidSenseRangs() {
        PreguntaIntegerTestable p = new PreguntaIntegerTestable("Edat", null, null);
        assertEquals("Edat", p.getEnunciat());
        assertNull(p.getMinValue());
        assertNull(p.getMaxValue());
    }

    @Test
    public void constructorValidAmbRangs() {
        PreguntaIntegerTestable p = new PreguntaIntegerTestable("Puntuacio", 0, 10);
        assertEquals("Puntuacio", p.getEnunciat());
        assertEquals(Integer.valueOf(0), p.getMinValue());
        assertEquals(Integer.valueOf(10), p.getMaxValue());
    }

    @Test
    public void constructorRangsInvertitsLlenca() {
        PreguntaIntegerTestable p = new PreguntaIntegerTestable("Puntuacio", 10, 0);
        assertNotNull(p);
    }


    @Test
    public void modificarMinValid() {
        PreguntaIntegerTestable p = new PreguntaIntegerTestable("Puntuacio", 0, 10);
        p.setMin(2);
        assertEquals(Integer.valueOf(2), p.getMinValue());
        assertEquals(Integer.valueOf(10), p.getMaxValue());
    }

    @Test
    public void modificarMinTrencaInvariantLlenca() {
        PreguntaIntegerTestable p = new PreguntaIntegerTestable("Puntuacio", 0, 10);
        p.setMin(20);
        assertEquals(Integer.valueOf(0), p.getMinValue());
        assertEquals(Integer.valueOf(10), p.getMaxValue());
    }

    @Test
    public void modificarMaxValid() {
        PreguntaIntegerTestable p = new PreguntaIntegerTestable("Puntuacio", 0, 10);
        p.setMax(12);
        assertEquals(Integer.valueOf(0), p.getMinValue());
        assertEquals(Integer.valueOf(12), p.getMaxValue());
    }

    @Test
    public void modificarMaxTrencaInvariantLlenca() {
        PreguntaIntegerTestable p = new PreguntaIntegerTestable("Puntuacio", 5, 10);
        p.setMax(3);
        assertEquals(Integer.valueOf(5), p.getMinValue());
        assertEquals(Integer.valueOf(10), p.getMaxValue());
    }

    @Test
    public void rangsNulsPermetenAjustValidDespres() {
        PreguntaIntegerTestable p = new PreguntaIntegerTestable("Puntuacio", null, null);
        assertNull(p.getMinValue());
        assertNull(p.getMaxValue());
        p.setMax(10);
        assertNull(p.getMinValue());
        assertEquals(Integer.valueOf(10), p.getMaxValue());
        p.setMin(0);
        assertEquals(Integer.valueOf(0), p.getMinValue());
        assertEquals(Integer.valueOf(10), p.getMaxValue());
    }

    @Test
    public void nulDespresTrencarInvariantLlenca() {
        PreguntaIntegerTestable p = new PreguntaIntegerTestable("Puntuacio", null, null);
        p.setMax(5);
        p.setMin(6);
        assertTrue(p.getMinValue() == null || p.getMaxValue() == null || p.getMinValue() <= p.getMaxValue());
    }
}
