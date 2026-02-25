package EXE.Tests;
import domini.PreguntaNominalMult;
import org.junit.*;
import static org.junit.Assert.*;

import java.util.Set;
import java.util.LinkedHashSet;
/**
 * Tests de PreguntaNominalMult. Comprova:
 *  - constructor vàlid i excepcions (opcions nul·les/buides, elements nuls/buits, qMax invàlid)
 *  - modificació d'opcions i qMax vàlida i amb rollback en cas d'error
 *  - coherència entre qMax i nombre d'opcions (qMax <= |opcions|)
 *
 * Nota: utilitza una subclasse testable per exposar els mètodes protected.
 */

public class TestPreguntaNominalMult {

    // Subclasse de prova per exposar els metodes protected
    static class PreguntaNominalMultTestable extends PreguntaNominalMult {
        public PreguntaNominalMultTestable(String e, Set<String> ops, Integer qMax) {
            super(e, ops, qMax);
        }
        public void setOpcions(Set<String> ops) { modificarOpcions(ops); }
        public void setQMax(Integer q) { modificarQMax(q); }
    }

    private Set<String> set(String... vals) {
        Set<String> s = new LinkedHashSet<>();
        for (String v : vals) s.add(v);
        return s;
    }

    @Test
    public void constructorValid() {
        Set<String> ops = set("A", "B", "C");
        PreguntaNominalMultTestable p = new PreguntaNominalMultTestable("Colors", ops, 2);
        assertEquals("Colors", p.getEnunciat());
        assertEquals(ops, p.getOpcions());
        assertEquals(Integer.valueOf(2), p.getQMax());
    }

    @Test
    public void constructorOpcionsNullesLlenca() {
        PreguntaNominalMultTestable p = new PreguntaNominalMultTestable("Colors", null, 1);
        assertNotNull(p);
    }

    @Test
    public void constructorOpcionsBuidesLlenca() {
        PreguntaNominalMultTestable p = new PreguntaNominalMultTestable("Colors", set(), 1);
        assertNotNull(p);
    }

    @Test
    public void constructorOpcioNulaLlenca() {
        Set<String> ops = set("A", null, "C");
        PreguntaNominalMultTestable p = new PreguntaNominalMultTestable("Colors", ops, 2);
        assertNotNull(p);
    }

    @Test
    public void constructorOpcioBuidaLlenca() {
        Set<String> ops = set("A", "", "C");
        PreguntaNominalMultTestable p = new PreguntaNominalMultTestable("Colors", ops, 2);
        assertNotNull(p);
    }

    @Test
    public void constructorQMaxNulLlenca() {
        Set<String> ops = set("A", "B");
        PreguntaNominalMultTestable p = new PreguntaNominalMultTestable("Colors", ops, null);
        assertNotNull(p);
    }

    @Test
    public void constructorQMaxNoPositiuLlenca() {
        Set<String> ops = set("A", "B");
        PreguntaNominalMultTestable p = new PreguntaNominalMultTestable("Colors", ops, 0);
        assertNotNull(p);
    }

    @Test
    public void constructorQMaxMesGranQueOpcionsLlenca() {
        Set<String> ops = set("A", "B");
        PreguntaNominalMultTestable p = new PreguntaNominalMultTestable("Colors", ops, 3);
        assertNotNull(p);
    }

    @Test
    public void modificarOpcionsValid() {
        PreguntaNominalMultTestable p = new PreguntaNominalMultTestable("Colors", set("A","B"), 2);
        Set<String> noves = set("X", "Y", "Z");
        p.setOpcions(noves);
        assertEquals(noves, p.getOpcions());
        assertEquals(Integer.valueOf(2), p.getQMax());
    }

    @Test
    public void modificarOpcionsAmbNulsLlenca() {
        PreguntaNominalMultTestable p = new PreguntaNominalMultTestable("Colors", set("A","B"), 2);
        Set<String> antigues = p.getOpcions();
        p.setOpcions(set("A", null));
        // rollback: no s'ha d'haver canviat
        assertEquals(antigues, p.getOpcions());
        assertEquals(Integer.valueOf(2), p.getQMax());
    }

    @Test
    public void modificarOpcionsBuidesLlenca() {
        PreguntaNominalMultTestable p = new PreguntaNominalMultTestable("Colors", set("A","B"), 2);
        Set<String> antigues = p.getOpcions();
        p.setOpcions(set());
        assertEquals(antigues, p.getOpcions());
        assertEquals(Integer.valueOf(2), p.getQMax());
    }

    @Test
    public void modificarOpcionsReduintPerSotaDeQMaxLlenca() {
        PreguntaNominalMultTestable p = new PreguntaNominalMultTestable("Colors", set("A","B","C"), 3);
        Set<String> antigues = p.getOpcions();
        p.setOpcions(set("A","B"));
        // qMax (3) > nombre d'opcions (2): ha de fer rollback
        assertEquals(antigues, p.getOpcions());
        assertEquals(Integer.valueOf(3), p.getQMax());
    }

    @Test
    public void modificarQMaxValid() {
        PreguntaNominalMultTestable p = new PreguntaNominalMultTestable("Colors", set("A","B","C"), 2);
        p.setQMax(3);
        assertEquals(Integer.valueOf(3), p.getQMax());
        assertEquals(set("A","B","C"), p.getOpcions());
    }

    @Test
    public void modificarQMaxNoPositiuLlenca() {
        PreguntaNominalMultTestable p = new PreguntaNominalMultTestable("Colors", set("A","B"), 2);
        p.setQMax(0);
        // rollback: qMax segueix sent 2
        assertEquals(Integer.valueOf(2), p.getQMax());
        assertEquals(set("A","B"), p.getOpcions());
    }

    @Test
    public void modificarQMaxMesGranQueOpcionsLlenca() {
        PreguntaNominalMultTestable p = new PreguntaNominalMultTestable("Colors", set("A","B"), 2);
        p.setQMax(3);
        // rollback: qMax continua sent 2
        assertEquals(Integer.valueOf(2), p.getQMax());
        assertEquals(set("A","B"), p.getOpcions());
    }
}
