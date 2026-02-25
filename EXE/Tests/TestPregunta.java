package EXE.Tests;
import domini.Pregunta;
import org.junit.*;
import static org.junit.Assert.*;
/**
 * Tests de la classe base Pregunta (abstracta). Utilitza una subclasse fake per provar:
 *  - constructor vàlid
 *  - mètode modificarEnunciat accessible via la subclasse fake
 *
 * Nota: la validació d'enunciat la fan les classes derivades, no la base.
 */
public class TestPregunta {

    static class PreguntaFake extends Pregunta {
        public PreguntaFake(String enunciat) {
            super(enunciat);
            // Aquesta pregunta falsa NO força la validacio base aqui,
            // respectant el disseny: la validacio la fan les classes derivades.
        }
        @Override
        public String getTipus() { return "fake"; }
        public void modificarEnunciatPublic(String nou) { modificarEnunciat(nou); }
    }

    @Test
    public void testConstructorValid() {
        PreguntaFake p = new PreguntaFake("Test");
        assertEquals("Test", p.getEnunciat());
    }

    @Test
    public void testModificarEnunciatValid() {
        PreguntaFake p = new PreguntaFake("Original");
        p.modificarEnunciatPublic("Nou");
        assertEquals("Nou", p.getEnunciat());
    }
}