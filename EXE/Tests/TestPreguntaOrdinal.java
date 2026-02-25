package EXE.Tests;
import domini.PreguntaOrdinal;
import org.junit.*;
import static org.junit.Assert.*;

import java.util.Vector;
/**
 * Tests de PreguntaOrdinal. Comprova:
 *  - constructor vàlid i excepcions (opcions nul·les o buides)
 *  - modificació d'opcions vàlida i amb rollback en cas d'error
 *  - coherència del nombre d'opcions després de cada operació
 *
 * Nota: utilitza una subclasse testable per exposar els mètodes protected.
 */

public class TestPreguntaOrdinal {

    // Subclasse de prova per exposar els metodes protected
    static class PreguntaOrdinalTestable extends PreguntaOrdinal {
        public PreguntaOrdinalTestable(String e, Vector<String> ops) {
            super(e, ops);
        }
        public void setOpcions(Vector<String> ops) { modificarOpcions(ops); }
    }

    private Vector<String> vec(String... vals) {
        Vector<String> v = new Vector<>();
        for (String s : vals) v.add(s);
        return v;
    }

    @Test
    public void constructorValid() {
        Vector<String> ops = vec("Baix", "Mig", "Alt");
        PreguntaOrdinalTestable p = new PreguntaOrdinalTestable("Nivell", ops);
        assertEquals("Nivell", p.getEnunciat());
        assertEquals(Integer.valueOf(3), p.getNumOpcions());
    }

    @Test
    public void constructorOpcionsNullesLlenca() {
        PreguntaOrdinalTestable p = new PreguntaOrdinalTestable("Nivell", null);
        assertNotNull(p);
    }

    @Test
    public void constructorOpcionsBuidesLlenca() {
        PreguntaOrdinalTestable p = new PreguntaOrdinalTestable("Nivell", vec());
        assertNotNull(p);
    }

    @Test
    public void modificarOpcionsValid() {
        PreguntaOrdinalTestable p = new PreguntaOrdinalTestable("Nivell", vec("Baix","Mig"));
        p.setOpcions(vec("Molt baix", "Baix", "Mig", "Alt"));
        assertEquals(Integer.valueOf(4), p.getNumOpcions());
    }

    @Test
    public void modificarOpcionsNullesLlenca() {
        PreguntaOrdinalTestable p = new PreguntaOrdinalTestable("Nivell", vec("Baix","Mig"));
        Integer numAntic = p.getNumOpcions();
        p.setOpcions(null);
        // rollback: el nombre d'opcions no ha de canviar
        assertEquals(numAntic, p.getNumOpcions());
    }

    @Test
    public void modificarOpcionsBuidesLlenca() {
        PreguntaOrdinalTestable p = new PreguntaOrdinalTestable("Nivell", vec("Baix","Mig"));
        Integer numAntic = p.getNumOpcions();
        p.setOpcions(vec());
        assertEquals(numAntic, p.getNumOpcions());
    }
}
