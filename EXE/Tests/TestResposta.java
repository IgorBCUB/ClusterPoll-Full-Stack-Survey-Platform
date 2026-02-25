package EXE.Tests;
import domini.*;
import org.junit.Before;
import org.junit.Test;

import java.util.*;

import static org.junit.Assert.assertEquals;
/**
 * Tests de la classe Resposta. Comprova:
 *  - getters bàsics (idPregunta, contingut)
 *  - càlcul de distància normalitzada entre respostes per a tots els tipus de pregunta:
 *    · PreguntaInteger: distància numèrica normalitzada
 *    · PreguntaOrdinal: distància ordinal normalitzada
 *    · PreguntaNominalUnica: 0 si iguals, 1 si diferents
 *    · PreguntaNominalMult: distància de Jaccard (1 - intersecció/unió)
 *    · PreguntaText: Levenshtein normalitzat per longitud màxima
 */

public class TestResposta {

    private CtrlDomini ctrl;

    @Before
    public void setup() {
        ctrl = new CtrlDomini();
    }

    @Test
    public void getters_basics_ok() {
        Integer idP = 123;
        ArrayList<String> cont = new ArrayList<>(List.of("hola"));

        Resposta r = new Resposta(idP, cont, ctrl);

        assertEquals(idP, r.getIdPregunta());
        assertEquals(cont, r.getContingut());
    }

    @Test
    public void distancia_integer_normalitzada() {
        PreguntaInteger p = ctrl.creaPreguntaInteger("Edat", 0, 100);

        Resposta r0   = new Resposta(p.getId(), new ArrayList<>(List.of("0")), ctrl);
        Resposta r100 = new Resposta(p.getId(), new ArrayList<>(List.of("100")), ctrl);
        Resposta r50  = new Resposta(p.getId(), new ArrayList<>(List.of("50")), ctrl);

        assertEquals(1.0, r0.calcula_distanciaRespostes(r100), 1e-9);
        assertEquals(0.5, r0.calcula_distanciaRespostes(r50), 1e-9);
        assertEquals(0.0, r50.calcula_distanciaRespostes(new Resposta(p.getId(), new ArrayList<>(List.of("50")), ctrl)), 1e-9);
    }

    @Test
    public void distancia_ordinal_normalitzada() {
        Vector<String> ops = new Vector<>(Arrays.asList("A","B","C","D")); // 4 opcions
        PreguntaOrdinal p = ctrl.creaPreguntaOrdinal("Rang", ops);

        Resposta r0 = new Resposta(p.getId(), new ArrayList<>(List.of("0")), ctrl);
        Resposta r3 = new Resposta(p.getId(), new ArrayList<>(List.of("3")), ctrl);
        Resposta r1 = new Resposta(p.getId(), new ArrayList<>(List.of("1")), ctrl);
        Resposta r2 = new Resposta(p.getId(), new ArrayList<>(List.of("2")), ctrl);

        assertEquals(1.0, r0.calcula_distanciaRespostes(r3), 1e-9);
        assertEquals(1.0/3.0, r1.calcula_distanciaRespostes(r2), 1e-9);
    }

    @Test
    public void distancia_nominal_unica() {
        Set<String> ops = new LinkedHashSet<>(Arrays.asList("roig","verd","blau"));
        PreguntaNominalUnica p = ctrl.creaPreguntaNominalUnica("Color", ops);

        Resposta rVerd = new Resposta(p.getId(), new ArrayList<>(List.of("verd")), ctrl);
        Resposta rVerd2 = new Resposta(p.getId(), new ArrayList<>(List.of("verd")), ctrl);
        Resposta rRoig = new Resposta(p.getId(), new ArrayList<>(List.of("roig")), ctrl);

        assertEquals(0.0, rVerd.calcula_distanciaRespostes(rVerd2), 1e-9);
        assertEquals(1.0, rVerd.calcula_distanciaRespostes(rRoig), 1e-9);
    }

    @Test
    public void distancia_nominal_multiple_jaccard() {
        Set<String> ops = new LinkedHashSet<>(Arrays.asList("musica","cine","esport","jocs"));
        PreguntaNominalMult p = ctrl.creaPreguntaNominalMult("Hobbies", ops, 3);

        // {musica, esport} vs {esport, jocs}
        // intersecció = {esport} => 1
        // unió = {musica, esport, jocs} => 3
        // distància = 1 - 1/3 = 2/3
        Resposta r1 = new Resposta(p.getId(), new ArrayList<>(List.of("musica","esport")), ctrl);
        Resposta r2 = new Resposta(p.getId(), new ArrayList<>(List.of("esport","jocs")), ctrl);

        assertEquals(2.0/3.0, r1.calcula_distanciaRespostes(r2), 1e-9);
        assertEquals(0.0, r1.calcula_distanciaRespostes(new Resposta(p.getId(), new ArrayList<>(List.of("musica","esport")), ctrl)), 1e-9);
    }

    @Test
    public void distancia_text_levenshtein_normalitzada() {
        PreguntaText p = ctrl.creaPreguntaText("Comentari");

        // si feu: dist = levenshtein / max(len1, len2)
        // "hola" vs "ola" -> levenshtein=1, maxlen=4 => 0.25
        Resposta rHola = new Resposta(p.getId(), new ArrayList<>(List.of("hola")), ctrl);
        Resposta rOla  = new Resposta(p.getId(), new ArrayList<>(List.of("ola")), ctrl);

        assertEquals(0.25, rHola.calcula_distanciaRespostes(rOla), 1e-9);
        assertEquals(0.0, rHola.calcula_distanciaRespostes(new Resposta(p.getId(), new ArrayList<>(List.of("hola")), ctrl)), 1e-9);
    }
}