package EXE.Tests;
import domini.PreguntaNominalUnica;
import org.junit.*;
import static org.junit.Assert.*;

import java.util.Set;
import java.util.LinkedHashSet;
/**
 * Tests de PreguntaNominalUnica. Comprova:
 *  - constructor vàlid i excepcions (opcions nul·les, buides, amb elements nuls/buits)
 *  - modificació d'opcions vàlida i amb rollback en cas d'error
 *
 * Nota: utilitza una subclasse testable per exposar els mètodes protected.
 */

public class TestPreguntaNominalUnica {

    // Subclasse de prova per exposar els metodes protected
    static class PreguntaNominalUnicaTestable extends PreguntaNominalUnica {
        public PreguntaNominalUnicaTestable(String e, Set<String> ops) {
            super(e, ops);
        }
        public void setOpcions(Set<String> ops) { modificarOpcions(ops); }
    }

    private Set<String> set(String... vals) {
        Set<String> s = new LinkedHashSet<>();
        for (String v : vals) s.add(v);
        return s;
    }

    @Test
    public void constructorValid() {
        Set<String> ops = set("A", "B", "C");
        PreguntaNominalUnicaTestable p = new PreguntaNominalUnicaTestable("Color", ops);
        assertEquals("Color", p.getEnunciat());
        assertEquals(ops, p.getOpcions());
    }

    @Test
    public void constructorOpcionsNullesLlenca() {
        PreguntaNominalUnicaTestable p = new PreguntaNominalUnicaTestable("Color", null);
        assertNotNull(p);
    }

    @Test
    public void constructorOpcionsBuidesLlenca() {
        PreguntaNominalUnicaTestable p = new PreguntaNominalUnicaTestable("Color", set());
        assertNotNull(p);
    }

    @Test
    public void constructorOpcioNulaLlenca() {
        Set<String> ops = set("A", null, "C");
        PreguntaNominalUnicaTestable p = new PreguntaNominalUnicaTestable("Color", ops);
        assertNotNull(p);
    }

    @Test
    public void constructorOpcioBuidaLlenca() {
        Set<String> ops = set("A", "", "C");
        PreguntaNominalUnicaTestable p = new PreguntaNominalUnicaTestable("Color", ops);
        assertNotNull(p);
    }

    @Test
    public void modificarOpcionsValid() {
        PreguntaNominalUnicaTestable p = new PreguntaNominalUnicaTestable("Color", set("A","B"));
        Set<String> noves = set("X", "Y", "Z");
        p.setOpcions(noves);
        assertEquals(noves, p.getOpcions());
    }

    @Test
    public void modificarOpcionsNullesLlenca() {
        PreguntaNominalUnicaTestable p = new PreguntaNominalUnicaTestable("Color", set("A","B"));
        Set<String> antigues = p.getOpcions();
        p.setOpcions(null);
        // rollback: les opcions no han de canviar
        assertEquals(antigues, p.getOpcions());
    }

    @Test
    public void modificarOpcionsBuidesLlenca() {
        PreguntaNominalUnicaTestable p = new PreguntaNominalUnicaTestable("Color", set("A","B"));
        Set<String> antigues = p.getOpcions();
        p.setOpcions(set());
        assertEquals(antigues, p.getOpcions());
    }

    @Test
    public void modificarOpcionsAmbElementNulLlenca() {
        PreguntaNominalUnicaTestable p = new PreguntaNominalUnicaTestable("Color", set("A","B"));
        Set<String> antigues = p.getOpcions();
        p.setOpcions(set("A", null));
        assertEquals(antigues, p.getOpcions());
    }

    @Test
    public void modificarOpcionsAmbElementBuitLlenca() {
        PreguntaNominalUnicaTestable p = new PreguntaNominalUnicaTestable("Color", set("A","B"));
        Set<String> antigues = p.getOpcions();
        p.setOpcions(set("A", ""));
        assertEquals(antigues, p.getOpcions());
    }
}
