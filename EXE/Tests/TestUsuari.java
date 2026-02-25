package EXE.Tests;
import domini.CtrlDomini;
import domini.Usuari;
import domini.Enquesta;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;

import static org.junit.Assert.*;
/**
 * Tests de la classe Usuari. Comprova:
 *  - constructora vàlida i excepcions (nom/email nul, buit, invàlid, duplicat)
 *  - setters de nom i email (vàlids i invàlids)
 *  - creació d'enquestes (vàlida i amb títol nul/buit)
 *  - getters bàsics (id, nom, email)
 *  - assignació incremental d'IDs
 *
 *  Nota: no es comprova la obtenció d'enquestes (amb i sense enquestes, accessibilitat)
 *  perquè, en l'ús real de l'aplicació, les enquestes sempre es creen i es gestionen
 *  des del controlador de domini a través de la capa de presentació, i no directament
 *  des d'instàncies d'Usuari.
 */

public class TestUsuari {

    private CtrlDomini ctrlDomini;
    private Usuari usuari;

    @Before
    public void setUp() {
        ctrlDomini = new CtrlDomini();
        usuari = ctrlDomini.creaUsuari("Pere", "pere@exemple.cat");
    }

    @Test
    public void testConstructoraValida() {
        assertEquals(Integer.valueOf(0), usuari.getId());
        assertEquals("Pere", usuari.getNom());
        assertEquals("pere@exemple.cat", usuari.getEmail());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructoraNomNul() {
        ctrlDomini.creaUsuari(null, "email@exemple.cat");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructoraNomBuit() {
        ctrlDomini.creaUsuari("   ", "email@exemple.cat");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructoraEmailNul() {
        ctrlDomini.creaUsuari("Nom", null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructoraEmailBuit() {
        ctrlDomini.creaUsuari("Nom", "   ");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructoraEmailInvalid() {
        ctrlDomini.creaUsuari("Nom", "email-sense-arroba");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructoraEmailDuplicat() {
        ctrlDomini.creaUsuari("AltruUsuari", "pere@exemple.cat");
    }

    @Test
    public void testSetNomValid() {
        usuari.setNom("NouNom");
        assertEquals("NouNom", usuari.getNom());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetNomNul() {
        usuari.setNom(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetNomBuit() {
        usuari.setNom("   ");
    }

    @Test
    public void testSetEmailValid() {
        usuari.setEmail("nou@exemple.cat");
        assertEquals("nou@exemple.cat", usuari.getEmail());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetEmailNul() {
        usuari.setEmail(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetEmailBuit() {
        usuari.setEmail("   ");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetEmailInvalid() {
        usuari.setEmail("email-invalid");
    }

    @Test
    public void testCreaEnquestaValida() {
        Enquesta enquesta = usuari.creaEnquesta("Enquesta de prova");
        assertNotNull(enquesta);
        assertEquals("Enquesta de prova", enquesta.getTitol());
        assertEquals(usuari.getId(), enquesta.getIdCreador());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreaEnquestaTitolNul() {
        usuari.creaEnquesta(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreaEnquestaTitolBuit() {
        usuari.creaEnquesta("   ");
    }

    @Test
    public void testGetEnquestesUsuariSenseEnquestes() {
        try {
            usuari.getEnquestesUsuari();
            fail("Hauria de llançar IllegalStateException");
        } catch (IllegalStateException e) {
            assertEquals("Aquest usuari no ha creat cap enquesta.", e.getMessage());
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetEnquestaUsuariIdNul() {
        usuari.getEnquestaUsuari(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetEnquestaUsuariNoAccessible() {
        //Crear un altre usuari i la seva enquesta
        Usuari altreUsuari = ctrlDomini.creaUsuari("Altru", "altru@exemple.cat");
        Enquesta enquestaAltre = altreUsuari.creaEnquesta("Enquesta d'altru");

        //Intentar accedir des del primer usuari (no hauria de tenir accés)
        usuari.getEnquestaUsuari(enquestaAltre.getIdE());
    }

    @Test
    public void testGetId() {
        assertEquals(Integer.valueOf(0), usuari.getId());
    }

    @Test
    public void testGetNom() {
        assertEquals("Pere", usuari.getNom());
    }

    @Test
    public void testGetEmail() {
        assertEquals("pere@exemple.cat", usuari.getEmail());
    }

    @Test
    public void testMultipleUsuarisIdsIncrementals() {
        Usuari usuari2 = ctrlDomini.creaUsuari("Usuari2", "usuari2@exemple.cat");
        Usuari usuari3 = ctrlDomini.creaUsuari("Usuari3", "usuari3@exemple.cat");

        assertEquals(Integer.valueOf(0), usuari.getId());
        assertEquals(Integer.valueOf(1), usuari2.getId());
        assertEquals(Integer.valueOf(2), usuari3.getId());
    }
}