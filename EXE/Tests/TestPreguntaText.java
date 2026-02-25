package EXE.Tests;
import domini.PreguntaText;
import org.junit.*;
import static org.junit.Assert.*;
/**
 * Tests de PreguntaText. Comprova:
 *  - constructor vàlid amb enunciat correcte
 *  - constructor amb enunciat nul (llença excepció)
 *  - constructor amb enunciat buit (llença excepció)
 */

public class TestPreguntaText {

    @Test
    public void constructorValid() {
        PreguntaText p = new PreguntaText("Nom");
        assertEquals("Nom", p.getEnunciat());
    }

    @Test
    public void constructorEnunciatNullLlença() {
        PreguntaText p = new PreguntaText(null);
        assertNotNull(p);
    }

    @Test
    public void constructorEnunciatBuitLlença() {
        PreguntaText p = new PreguntaText("");
        assertNotNull(p);
    }
}
