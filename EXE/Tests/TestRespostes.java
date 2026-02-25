package EXE.Tests;
import domini.*;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThrows;
/**
 * Tests de la classe Respostes. Comprova:
 *  - creació i obtenció de Respostes buides
 *  - afegir i llegir respostes individuals
 *  - obtenció d'una Resposta concreta per idPregunta
 *  - excepcions en duplicats (Respostes i Resposta individual)
 *  - eliminació de Respostes i Resposta individual
 *  - cleanup automàtic de submapes buits
 */

public class TestRespostes {

    private CtrlDomini ctrl;

    private int idCreador;
    private int idContestant;
    private int idE;

    @BeforeClass
    public static void netejaPersistencia() {
        String[] fitxers = {
                "DATA/FitxerUsuaris.csv",
        };

        for (String f : fitxers) {
            try { Files.deleteIfExists(Paths.get(f)); }
            catch (Exception ignored) {}
        }
    }


    @Before
    public void setup() {
        ctrl = new CtrlDomini();

        Usuari creador = ctrl.creaUsuari("CreadorTest", "creador@test.com");
        Usuari contestant = ctrl.creaUsuari("ContestantTest", "contestant@test.com");

        idCreador = creador.getId();
        idContestant = contestant.getId();

        Enquesta e = ctrl.creaEnquesta("Enquesta Test", idCreador);
        idE = e.getIdE();
    }


    @Test
    public void creaRespostes_i_getRespostes_buides_ok() {
        ctrl.creaRespostes(idE, idContestant);

        Respostes rs = ctrl.getRespostes(idE, idContestant);
        assertNotNull(rs);
        assertEquals(0, rs.getRespostes().size());
    }

    @Test
    public void creaGetRespostes_afegirILlegir_ok() {
        ctrl.creaRespostes(idE, idContestant);

        PreguntaInteger pi = ctrl.creaPreguntaInteger("Edat", 0, 120);
        PreguntaText pt = ctrl.creaPreguntaText("Comentari");

        ctrl.afegeixPreguntaAEnquesta(idE, pi.getId());
        ctrl.afegeixPreguntaAEnquesta(idE, pt.getId());

        ctrl.AfegeixResposta(idE, idContestant, pi.getId(), new ArrayList<>(List.of("35")));
        ctrl.AfegeixResposta(idE, idContestant, pt.getId(), new ArrayList<>(List.of("Bon dia")));

        Respostes rs = ctrl.getRespostes(idE, idContestant);
        assertEquals(2, rs.getRespostes().size());
    }

    @Test
    public void getResposta_ok() {
        ctrl.creaRespostes(idE, idContestant);

        PreguntaText p = ctrl.creaPreguntaText("Obs");
        ctrl.afegeixPreguntaAEnquesta(idE, p.getId());

        ctrl.AfegeixResposta(idE, idContestant, p.getId(), new ArrayList<>(List.of("hola")));

        Resposta r = ctrl.GetResposta(idE, idContestant, p.getId());
        assertEquals(p.getId(), r.getIdPregunta());
        assertEquals(List.of("hola"), r.getContingut());
    }


    @Test
    public void creaRespostes_duplicat_llenca() {
        ctrl.creaRespostes(idE, idContestant);

        assertThrows(IllegalStateException.class, () ->
                ctrl.creaRespostes(idE, idContestant)
        );
    }

    @Test
    public void getRespostes_inexistent_retornaBuit() {
        Respostes rs = ctrl.getRespostes(idE, idContestant);
        assertNotNull(rs);
        assertEquals(0, rs.getRespostes().size());
    }

    @Test
    public void afegeixResposta_duplicadaMateixIdP_llenca() {
        ctrl.creaRespostes(idE, idContestant);

        PreguntaText p = ctrl.creaPreguntaText("Obs");
        ctrl.afegeixPreguntaAEnquesta(idE, p.getId());

        ctrl.AfegeixResposta(idE, idContestant, p.getId(), new ArrayList<>(List.of("A")));

        assertThrows(IllegalStateException.class, () ->
                ctrl.AfegeixResposta(idE, idContestant, p.getId(), new ArrayList<>(List.of("B")))
        );
    }

    @Test
    public void eliminarRespostes_i_despresGet_retornaBuit() {
        ctrl.creaRespostes(idE, idContestant);

        PreguntaInteger p = ctrl.creaPreguntaInteger("Val", 0, 10);
        ctrl.afegeixPreguntaAEnquesta(idE, p.getId());
        ctrl.AfegeixResposta(idE, idContestant, p.getId(), new ArrayList<>(List.of("7")));

        ctrl.EliminarRespostes(idE, idContestant);

        Respostes rs = ctrl.getRespostes(idE, idContestant);
        assertNotNull(rs);
        assertEquals(0, rs.getRespostes().size());
    }

    @Test
    public void eliminarResposta_ok_i_cleanupSubmap_retornaBuit() {
        ctrl.creaRespostes(idE, idContestant);

        PreguntaInteger p1 = ctrl.creaPreguntaInteger("X", 0, 10);
        PreguntaInteger p2 = ctrl.creaPreguntaInteger("Y", 0, 10);
        ctrl.afegeixPreguntaAEnquesta(idE, p1.getId());
        ctrl.afegeixPreguntaAEnquesta(idE, p2.getId());

        ctrl.AfegeixResposta(idE, idContestant, p1.getId(), new ArrayList<>(List.of("1")));
        ctrl.AfegeixResposta(idE, idContestant, p2.getId(), new ArrayList<>(List.of("2")));

        ctrl.EliminarResposta(idE, idContestant, p1.getId());
        assertEquals(1, ctrl.getRespostes(idE, idContestant).getRespostes().size());

        ctrl.EliminarResposta(idE, idContestant, p2.getId());

        Respostes rs = ctrl.getRespostes(idE, idContestant);
        assertNotNull(rs);
        assertEquals(0, rs.getRespostes().size());
    }
}