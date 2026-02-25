package presentacio;

import domini.CtrlDomini;

import javax.swing.*;

public class CtrlPresentacio {

    private CtrlDomini ctrlDomini;

    // Vistes
    private VistaPrincipal vistaPrincipal;
    private VistaIntroduirCodiEnquesta vistaIntroduirCodiEnquesta;
    private VistaMenuCreador vistaMenuCreador;
    private VistaGestioEnquestes vistaGestioEnquestes;
    private VistaGestioPreguntes vistaGestioPreguntes;
    private VistaAnalisi vistaAnalisi;
    private VistaGestioRespostes vistaGestioRespostes;  // nova vista
    private VistaGestioUsuari vistaGestioUsuari;


    public CtrlPresentacio(CtrlDomini ctrlDomini) {
        this.ctrlDomini = ctrlDomini;
        initVistes();
    }
    public void sortirAplicacio() {
        ctrlDomini.tancarAplicacio();
        System.exit(0);
    }

    private void initVistes() {
        vistaPrincipal = new VistaPrincipal();
        vistaPrincipal.setListener(this);

        vistaIntroduirCodiEnquesta = new VistaIntroduirCodiEnquesta();
        vistaIntroduirCodiEnquesta.setListener(this);

        vistaMenuCreador = new VistaMenuCreador();
        vistaMenuCreador.setListener(this);

        vistaGestioEnquestes = new VistaGestioEnquestes();
        vistaGestioEnquestes.setListener(this);

        vistaGestioPreguntes = new VistaGestioPreguntes();
        vistaGestioPreguntes.setListener(this);

        vistaAnalisi = new VistaAnalisi();
        vistaAnalisi.setListener(this);

        vistaGestioUsuari = new VistaGestioUsuari();
        vistaGestioUsuari.setListener(this);
    }

    public void inicia() {
        SwingUtilities.invokeLater(() -> vistaPrincipal.mostrar());
    }

    // ================== Helpers privats ==================
    // ================== Gestió d'Usuaris ==================

    private void carregarUsuarisEnVistaGestioUsuaris() {
        java.util.List<domini.Usuari> usuaris = ctrlDomini.getallUsuaris();
        java.util.List<String[]> dades = new java.util.ArrayList<>();
        for (domini.Usuari u : usuaris) {
            dades.add(new String[] {
                    u.getId().toString(),
                    u.getNom(),
                    u.getEmail()
            });
        }
        vistaGestioUsuari.actualitzarTaulaUsuaris(dades);
    }

    public void mostrarVistaGestioUsuaris() {
        vistaMenuCreador.amagar();
        try {
            carregarUsuarisEnVistaGestioUsuaris();
        } catch (IllegalStateException e) {
            vistaGestioUsuari.actualitzarTaulaUsuaris(new java.util.ArrayList<>());
            JOptionPane.showMessageDialog(null,
                    e.getMessage(),
                    "Informació",
                    JOptionPane.INFORMATION_MESSAGE);
        }
        vistaGestioUsuari.mostrar();
    }

    public void tornarAMenuCreadorDesDeGestioUsuaris() {
        vistaGestioUsuari.amagar();
        vistaMenuCreador.mostrar();
    }

    // Carregar les enquestes del domini a la taula de VistaGestioEnquestes
    private void carregarEnquestesEnVistaGestio() {
        java.util.List<String[]> dades = ctrlDomini.getInfoEnquestes();
        vistaGestioEnquestes.actualitzarTaulaEnquestes(dades);
    }

    // Carregar totes les preguntes a la taula de VistaGestioPreguntes
    private void carregarPreguntesEnVistaGestio() {
        java.util.List<String[]> dades = ctrlDomini.getInfoPreguntes();
        vistaGestioPreguntes.actualitzarTaulaPreguntes(dades);
    }

    // Requerir enquesta seleccionada a la taula
    private Integer requerirEnquestaSeleccionada() {
        Integer idE = vistaGestioEnquestes.getIdEnquestaSeleccionada();
        if (idE == null) {
            JOptionPane.showMessageDialog(null,
                    "Has de seleccionar una enquesta de la taula.",
                    "Cap enquesta seleccionada",
                    JOptionPane.WARNING_MESSAGE);
        }
        return idE;
    }

    // ================== Navegació bàsica ==================

    public void mostrarPantallaRespondedor() {
        vistaPrincipal.amagar();
        vistaIntroduirCodiEnquesta.mostrar();
    }

    public void mostrarPantallaCreador() {
        vistaPrincipal.amagar();
        vistaMenuCreador.mostrar();
    }

    public void accioImportarEnquesta() {
        // Demanar ruta del fitxer
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Selecciona el fitxer CSV d'enquesta");
        int result = chooser.showOpenDialog(null);
        if (result != JFileChooser.APPROVE_OPTION) return;

        java.io.File fitxer = chooser.getSelectedFile();
        if (fitxer == null) return;

        // Demanar títol de la nova enquesta
        String titol = JOptionPane.showInputDialog(
                null,
                "Introdueix el títol de la nova enquesta:",
                "Títol enquesta importada",
                JOptionPane.QUESTION_MESSAGE
        );
        if (titol == null || titol.trim().isEmpty()) return;

        // Demanar id de l'usuari creador
        String txtIdU = JOptionPane.showInputDialog(
                null,
                "Introdueix l'ID de l'usuari creador:",
                "Creador enquesta importada",
                JOptionPane.QUESTION_MESSAGE
        );
        if (txtIdU == null) return;

        try {
            int idU = Integer.parseInt(txtIdU.trim());

            // Cridem al domini perquè faci la importació amb CsvImporter
            Integer idE = ctrlDomini.importarEnquestaDesdeCSV(
                    fitxer.getAbsolutePath(),
                    titol.trim(),
                    idU
            );

            // Refresquem la taula
            carregarEnquestesEnVistaGestio();

            JOptionPane.showMessageDialog(
                    null,
                    "Enquesta importada correctament amb ID " + idE,
                    "Èxit",
                    JOptionPane.INFORMATION_MESSAGE
            );
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(
                    null,
                    "L'ID d'usuari ha de ser un enter.",
                    "Error",
                    JOptionPane.ERROR_MESSAGE
            );
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(
                    null,
                    ex.getMessage(),
                    "Error important enquesta",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }


    public void tornarAPrincipalDesDeCreador() {
        vistaMenuCreador.amagar();
        vistaGestioEnquestes.amagar();
        vistaGestioPreguntes.amagar();
        vistaAnalisi.amagar();
        vistaPrincipal.mostrar();
    }

    // Subpantalles del menú creador

    public void mostrarVistaCrearEnquesta() {
        String titol = vistaGestioEnquestes.demanarTitolEnquestaNova();
        if (titol == null || titol.trim().isEmpty()) return;

        Integer idUsuari = vistaGestioEnquestes.demanarIdUsuariCreador();
        if (idUsuari == null) return;

        try {
            ctrlDomini.creaEnquesta(titol.trim(), idUsuari);
            JOptionPane.showMessageDialog(null,
                    "Enquesta creada correctament.",
                    "Èxit",
                    JOptionPane.INFORMATION_MESSAGE);
            carregarEnquestesEnVistaGestio();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(null,
                    ex.getMessage(),
                    "Error en crear l'enquesta",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    public void mostrarVistaGestioEnquestes() {
        vistaMenuCreador.amagar();
        carregarEnquestesEnVistaGestio();
        vistaGestioEnquestes.mostrar();
    }

    public void mostrarVistaGestioPreguntes() {
        vistaMenuCreador.amagar();
        carregarPreguntesEnVistaGestio();
        vistaGestioPreguntes.mostrar();
    }

    public void mostrarVistaAnalisi() {
        vistaMenuCreador.amagar();
        carregarEnquestesEnVistaAnalisi();
        vistaAnalisi.mostrar();
    }

    public void tornarAMenuCreadorDesDeGestioEnquestes() {
        vistaGestioEnquestes.amagar();
        vistaMenuCreador.mostrar();
    }

    public void tornarAMenuCreadorDesDeGestioPreguntes() {
        vistaGestioPreguntes.amagar();
        vistaMenuCreador.mostrar();
    }

    public void tornarAMenuCreadorDesDeAnalisi() {
        vistaAnalisi.amagar();
        vistaMenuCreador.mostrar();
    }

    // ================== Respondedor ==================

    // ================== Respondedor ==================
    public void iniciarRespostaEnquesta(String codiEnquesta, String idUsuariText) {
        try {
            Integer idE = Integer.parseInt(codiEnquesta.trim());
            Integer idU = Integer.parseInt(idUsuariText.trim());

            // Obtenim l'enquesta i la llista d'ids de preguntes
            domini.Enquesta e = ctrlDomini.getEnquesta(idE);
            java.util.List idsPreguntes = e.getIdsPreguntes(); // retorna List<Integer> no modificable

            if (idsPreguntes == null || idsPreguntes.isEmpty()) {
                JOptionPane.showMessageDialog(null,
                        "Aquesta enquesta no té preguntes.",
                        "Enquesta buida",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }

            // Creem la vista passant-hi els ids de les preguntes i el domini
            vistaGestioRespostes = new VistaGestioRespostes(idE, idU, idsPreguntes, ctrlDomini);
            vistaGestioRespostes.setListener(this);

            vistaIntroduirCodiEnquesta.setVisible(false);
            vistaGestioRespostes.mostrar();

        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(null,
                    "IDs han de ser enters",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(null,
                    ex.getMessage(),
                    "Error iniciant resposta",
                    JOptionPane.ERROR_MESSAGE);
        }
    }


    public void accioEnviarRespostes(int idE, int idU, java.util.Map<Integer, java.util.List<String>> dades) {
        try {
            // Crea el contenidor Respostes per (idE, idU)
            ctrlDomini.creaRespostes(idE, idU);

            // Afegeix cada resposta
            for (java.util.Map.Entry<Integer, java.util.List<String>> entry : dades.entrySet()) {
                Integer idP = entry.getKey();
                java.util.List<String> contingut = entry.getValue();
                ctrlDomini.AfegeixResposta(idE, idU, idP, contingut);
            }

            JOptionPane.showMessageDialog(null,
                    "Respostes guardades correctament.",
                    "Èxit",
                    JOptionPane.INFORMATION_MESSAGE);

            tancarVistaRespostes();

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(null,
                    ex.getMessage(),
                    "Error guardant respostes",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    public void tancarVistaRespostes() {
        if (vistaGestioRespostes != null) {
            vistaGestioRespostes.amagar();
        }
        // després de respondre, tornem a la pantalla principal
        vistaPrincipal.mostrar();
    }

    public void tornarAPrincipalDesDeRespondedor() {
        if (vistaGestioRespostes != null) vistaGestioRespostes.amagar();
        vistaIntroduirCodiEnquesta.setVisible(false);
        vistaPrincipal.mostrar();
    }
    public void accioConsultarEnquestaSeleccionada() {
        Integer idE = requerirEnquestaSeleccionada();
        if (idE == null) return;

        try {
            domini.Enquesta e = ctrlDomini.getEnquesta(idE);
            String titol = e.getTitol();
            java.util.List preguntes = e.getPreguntes(); // llista de Pregunta

            StringBuilder sb = new StringBuilder();
            sb.append("Enquesta ID: ").append(e.getIdE()).append("\n");
            sb.append("Títol: ").append(titol).append("\n");
            sb.append("Nombre de preguntes: ").append(e.getNumPreguntes()).append("\n\n");

            int idx = 1;
            for (Object o : preguntes) {
                domini.Pregunta p = (domini.Pregunta) o;
                String tipus = p.getTipus();
                sb.append(idx).append(". [")
                        .append(tipus).append("] ")
                        .append(p.getEnunciat()).append("\n");

                // Afegim opcions si n'hi ha
                if ("ordinal".equals(tipus)) {
                    domini.PreguntaOrdinal po = (domini.PreguntaOrdinal) p;
                    @SuppressWarnings("unchecked")
                    java.util.Vector<String> opcions = (java.util.Vector<String>) po.getOpcions();
                    sb.append("   Opcions (ordre): ").append(opcions).append("\n");
                } else if ("nominal_unica".equals(tipus)) {
                    domini.PreguntaNominalUnica pu = (domini.PreguntaNominalUnica) p;
                    @SuppressWarnings("unchecked")
                    java.util.Set<String> opcions = (java.util.Set<String>) pu.getOpcions();
                    sb.append("   Opcions: ").append(opcions).append("\n");
                } else if ("nominal_multiple".equals(tipus)) {
                    domini.PreguntaNominalMult pm = (domini.PreguntaNominalMult) p;
                    @SuppressWarnings("unchecked")
                    java.util.Set<String> opcions = (java.util.Set<String>) pm.getOpcions();
                    sb.append("   Opcions (qMax=").append(pm.getQMax()).append("): ")
                            .append(opcions).append("\n");
                }

                sb.append("\n");
                idx++;
            }

            JOptionPane.showMessageDialog(
                    null,
                    sb.toString(),
                    "Detall enquesta " + idE,
                    JOptionPane.INFORMATION_MESSAGE
            );

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(
                    null,
                    ex.getMessage(),
                    "Error consultant l'enquesta",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    //ACCIONS GESTIO USUARI
    public void accioCrearUsuari() {
        String nom = JOptionPane.showInputDialog(
                null,
                "Introdueix el nom de l'usuari:",
                "Crear usuari",
                JOptionPane.QUESTION_MESSAGE
        );
        if (nom == null || nom.trim().isEmpty()) return;

        String correu = JOptionPane.showInputDialog(
                null,
                "Introdueix el correu de l'usuari:",
                "Crear usuari",
                JOptionPane.QUESTION_MESSAGE
        );
        if (correu == null || correu.trim().isEmpty()) return;

        try {
            ctrlDomini.creaUsuari(nom.trim(), correu.trim());
            carregarUsuarisEnVistaGestioUsuaris();
            JOptionPane.showMessageDialog(null,
                    "Usuari creat correctament.",
                    "Èxit",
                    JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(null,
                    ex.getMessage(),
                    "Error creant usuari",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    public void accioConsultarUsuari() {
        Integer idU = vistaGestioUsuari.getIdUsuariSeleccionat();
        if (idU == null) {
            JOptionPane.showMessageDialog(null,
                    "Has de seleccionar un usuari de la taula.",
                    "Cap usuari seleccionat",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        try {
            domini.Usuari u = ctrlDomini.getUsuari(idU);
            JOptionPane.showMessageDialog(null,
                    "ID: " + u.getId() + "\nNom: " + u.getNom() + "\nEmail: " + u.getEmail(),
                    "Detall usuari",
                    JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(null,
                    ex.getMessage(),
                    "Error consultant usuari",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    public void accioModificarUsuari() {
        Integer idU = vistaGestioUsuari.getIdUsuariSeleccionat();
        if (idU == null) {
            JOptionPane.showMessageDialog(null,
                    "Has de seleccionar un usuari de la taula.",
                    "Cap usuari seleccionat",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        try {
            domini.Usuari u = ctrlDomini.getUsuari(idU);

            String nouNom = JOptionPane.showInputDialog(
                    null,
                    "Nou nom (actual: " + u.getNom() + "):",
                    "Modificar usuari",
                    JOptionPane.QUESTION_MESSAGE
            );
            if (nouNom != null && !nouNom.trim().isEmpty()) {
                u.setNom(nouNom.trim());
            }

            String nouEmail = JOptionPane.showInputDialog(
                    null,
                    "Nou email (actual: " + u.getEmail() + "):",
                    "Modificar usuari",
                    JOptionPane.QUESTION_MESSAGE
            );
            if (nouEmail != null && !nouEmail.trim().isEmpty()) {
                u.setEmail(nouEmail.trim());
            }

            carregarUsuarisEnVistaGestioUsuaris();
            JOptionPane.showMessageDialog(null,
                    "Usuari modificat correctament.",
                    "Èxit",
                    JOptionPane.INFORMATION_MESSAGE);

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(null,
                    ex.getMessage(),
                    "Error modificant usuari",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    public void accioCrearEnquestaUsuari() {
        Integer idU = vistaGestioUsuari.getIdUsuariSeleccionat();
        if (idU == null) {
            JOptionPane.showMessageDialog(null,
                    "Has de seleccionar un usuari de la taula.",
                    "Cap usuari seleccionat",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        String titol = JOptionPane.showInputDialog(
                null,
                "Títol de la nova enquesta:",
                "Crear enquesta per usuari",
                JOptionPane.QUESTION_MESSAGE
        );
        if (titol == null || titol.trim().isEmpty()) return;

        try {
            ctrlDomini.creaEnquesta(titol.trim(), idU);
            JOptionPane.showMessageDialog(null,
                    "Enquesta creada correctament per l'usuari " + idU + ".",
                    "Èxit",
                    JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(null,
                    ex.getMessage(),
                    "Error creant enquesta",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    public void accioLlistarEnquestesUsuari() {
        Integer idU = vistaGestioUsuari.getIdUsuariSeleccionat();
        if (idU == null) {
            JOptionPane.showMessageDialog(null,
                    "Has de seleccionar un usuari de la taula.",
                    "Cap usuari seleccionat",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        try {
            java.util.List<domini.Enquesta> enquestes = ctrlDomini.getEnquestesUsuari(idU);
            if (enquestes.isEmpty()) {
                JOptionPane.showMessageDialog(null,
                        "L'usuari no té enquestes.",
                        "Informació",
                        JOptionPane.INFORMATION_MESSAGE);
                return;
            }
            StringBuilder sb = new StringBuilder();
            sb.append("Enquestes de l'usuari ").append(idU).append(":\n\n");
            for (domini.Enquesta e : enquestes) {
                sb.append(e.getIdE()).append(" - ").append(e.getTitol()).append("\n");
            }
            JOptionPane.showMessageDialog(null,
                    sb.toString(),
                    "Enquestes de l'usuari",
                    JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(null,
                    ex.getMessage(),
                    "Error llistant enquestes",
                    JOptionPane.ERROR_MESSAGE);
        }
    }
    // ================== Accions de Gestió d'Enquestes ==================

    public void accioAfegirPreguntaEnquesta() {
        Integer idE = requerirEnquestaSeleccionada();
        if (idE == null) return;

        Integer idP = vistaGestioEnquestes.demanarIdPreguntaPerAfegir();
        if (idP == null) return;

        try {
            ctrlDomini.afegeixPreguntaAEnquesta(idE, idP);
            JOptionPane.showMessageDialog(null,
                    "Pregunta afegida correctament a l'enquesta.",
                    "Èxit",
                    JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(null,
                    ex.getMessage(),
                    "Error en afegir la pregunta",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    public void accioEliminarPreguntaEnquesta() {
        Integer idE = requerirEnquestaSeleccionada();
        if (idE == null) return;

        Integer idP = vistaGestioEnquestes.demanarIdPreguntaPerEliminar();
        if (idP == null) return;

        try {
            ctrlDomini.eliminaPreguntaAEnquesta(idE, idP);
            JOptionPane.showMessageDialog(null,
                    "Pregunta eliminada correctament de l'enquesta.",
                    "Èxit",
                    JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(null,
                    ex.getMessage(),
                    "Error en eliminar la pregunta",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    public void accioEliminarEnquesta() {
        Integer idE = requerirEnquestaSeleccionada();
        if (idE == null) return;

        int op = JOptionPane.showConfirmDialog(null,
                "Segur que vols eliminar aquesta enquesta?",
                "Confirmar eliminació",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);
        if (op != JOptionPane.YES_OPTION) return;

        try {
            ctrlDomini.eliminarEnquesta(idE);
            carregarEnquestesEnVistaGestio();
            JOptionPane.showMessageDialog(null,
                    "Enquesta eliminada correctament.",
                    "Èxit",
                    JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(null,
                    ex.getMessage(),
                    "Error en eliminar l'enquesta",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    public void accioModificarTitolEnquesta() {
        Integer idE = requerirEnquestaSeleccionada();
        if (idE == null) return;

        String nouTitol = vistaGestioEnquestes.demanarNouTitolEnquesta();
        if (nouTitol == null || nouTitol.trim().isEmpty()) return;

        try {
            ctrlDomini.setTitolEnquesta(idE, nouTitol.trim());
            carregarEnquestesEnVistaGestio();
            JOptionPane.showMessageDialog(null,
                    "Títol modificat correctament.",
                    "Èxit",
                    JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(null,
                    ex.getMessage(),
                    "Error en modificar el títol",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    // Crear una nova pregunta de qualsevol tipus i enllaçar-la a l'enquesta seleccionada
    public void accioCrearNovaPreguntaIEnllacar() {
        Integer idE = requerirEnquestaSeleccionada();
        if (idE == null) return;

        String[] opcionsTipus = {
                "TEXT",
                "INTEGER",
                "ORDINAL",
                "NOM_UNICA",
                "NOM_MULTIPLE"
        };
        String tipus = (String) JOptionPane.showInputDialog(
                null,
                "Tria el tipus de pregunta:",
                "Nou tipus de pregunta",
                JOptionPane.QUESTION_MESSAGE,
                null,
                opcionsTipus,
                opcionsTipus[0]
        );
        if (tipus == null) return;

        String enunciat = JOptionPane.showInputDialog(
                null,
                "Introdueix l'enunciat de la pregunta:",
                "Enunciat",
                JOptionPane.QUESTION_MESSAGE
        );
        if (enunciat == null || enunciat.trim().isEmpty()) {
            JOptionPane.showMessageDialog(null,
                    "L'enunciat no pot estar buit.",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }
        enunciat = enunciat.trim();

        Integer idP = null;

        try {
            switch (tipus) {
                case "TEXT": {
                    var p = ctrlDomini.creaPreguntaText(enunciat);
                    if (p == null) throw new IllegalArgumentException("Definició de pregunta de text no vàlida.");
                    idP = p.getId();
                    break;
                }
                case "INTEGER": {
                    String minStr = JOptionPane.showInputDialog(
                            null,
                            "Valor mínim (enter):",
                            "Paràmetres INTEGER",
                            JOptionPane.QUESTION_MESSAGE
                    );
                    if (minStr == null) return;
                    String maxStr = JOptionPane.showInputDialog(
                            null,
                            "Valor màxim (enter):",
                            "Paràmetres INTEGER",
                            JOptionPane.QUESTION_MESSAGE
                    );
                    if (maxStr == null) return;
                    int min = Integer.parseInt(minStr.trim());
                    int max = Integer.parseInt(maxStr.trim());
                    var p = ctrlDomini.creaPreguntaInteger(enunciat, min, max);
                    if (p == null) throw new IllegalArgumentException("Definició de pregunta integer no vàlida.");
                    idP = p.getId();
                    break;
                }
                case "ORDINAL": {
                    String txtOpc = JOptionPane.showInputDialog(
                            null,
                            "Introdueix les opcions ordenades separades per comes:",
                            "Opcions ORDINAL",
                            JOptionPane.QUESTION_MESSAGE
                    );
                    if (txtOpc == null) return;
                    String[] parts = txtOpc.split(",");
                    java.util.Vector<String> opcions = new java.util.Vector<>();
                    for (String s : parts) {
                        String t = s.trim();
                        if (!t.isEmpty()) opcions.add(t);
                    }
                    var p = ctrlDomini.creaPreguntaOrdinal(enunciat, opcions);
                    if (p == null) throw new IllegalArgumentException("Definició de pregunta ordinal no vàlida.");
                    idP = p.getId();
                    break;
                }
                case "NOM_UNICA": {
                    String txtOpc = JOptionPane.showInputDialog(
                            null,
                            "Introdueix les opcions separades per comes:",
                            "Opcions NOMINAL ÚNICA",
                            JOptionPane.QUESTION_MESSAGE
                    );
                    if (txtOpc == null) return;
                    String[] parts = txtOpc.split(",");
                    java.util.Set<String> opcions = new java.util.HashSet<>();
                    for (String s : parts) {
                        String t = s.trim();
                        if (!t.isEmpty()) opcions.add(t);
                    }
                    var p = ctrlDomini.creaPreguntaNominalUnica(enunciat, opcions);
                    if (p == null) throw new IllegalArgumentException("Definició de pregunta nominal única no vàlida.");
                    idP = p.getId();
                    break;
                }
                case "NOM_MULTIPLE": {
                    String txtOpc = JOptionPane.showInputDialog(
                            null,
                            "Introdueix les opcions separades per comes:",
                            "Opcions NOMINAL MÚLTIPLE",
                            JOptionPane.QUESTION_MESSAGE
                    );
                    if (txtOpc == null) return;
                    String[] parts = txtOpc.split(",");
                    java.util.Set<String> opcions = new java.util.HashSet<>();
                    for (String s : parts) {
                        String t = s.trim();
                        if (!t.isEmpty()) opcions.add(t);
                    }
                    String qMaxStr = JOptionPane.showInputDialog(
                            null,
                            "Quantes opcions com a màxim es poden marcar? (enter):",
                            "qMax NOMINAL MÚLTIPLE",
                            JOptionPane.QUESTION_MESSAGE
                    );
                    if (qMaxStr == null) return;
                    int qMax = Integer.parseInt(qMaxStr.trim());
                    var p = ctrlDomini.creaPreguntaNominalMult(enunciat, opcions, qMax);
                    if (p == null) throw new IllegalArgumentException("Definició de pregunta nominal múltiple no vàlida.");
                    idP = p.getId();
                    break;
                }
            }

            if (idP == null) {
                JOptionPane.showMessageDialog(null,
                        "No s'ha pogut crear la pregunta.",
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }

            ctrlDomini.afegeixPreguntaAEnquesta(idE, idP);
            JOptionPane.showMessageDialog(null,
                    "Pregunta creada i afegida correctament a l'enquesta.",
                    "Èxit",
                    JOptionPane.INFORMATION_MESSAGE);

        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(null,
                    "Els valors numèrics han de ser enters.",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(null,
                    ex.getMessage(),
                    "Error creant o afegint la pregunta",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    // ================== Accions de Gestió de Preguntes ==================

    public void accioCrearPreguntaText() {
        String enunciat = JOptionPane.showInputDialog(
                null,
                "Introdueix l'enunciat de la pregunta de text:",
                "Nova pregunta TEXT",
                JOptionPane.QUESTION_MESSAGE
        );
        if (enunciat == null || enunciat.trim().isEmpty()) return;

        try {
            var p = ctrlDomini.creaPreguntaText(enunciat.trim());
            if (p == null) {
                JOptionPane.showMessageDialog(null,
                        "Definició de pregunta de text no vàlida.",
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }
            carregarPreguntesEnVistaGestio();
            JOptionPane.showMessageDialog(null,
                    "Pregunta de text creada correctament (ID " + p.getId() + ").",
                    "Èxit",
                    JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(null,
                    ex.getMessage(),
                    "Error creant pregunta de text",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    public void accioCrearPreguntaInteger() {
        String enunciat = JOptionPane.showInputDialog(
                null,
                "Introdueix l'enunciat de la pregunta INTEGER:",
                "Nova pregunta INTEGER",
                JOptionPane.QUESTION_MESSAGE
        );
        if (enunciat == null || enunciat.trim().isEmpty()) return;

        try {
            String minStr = JOptionPane.showInputDialog(
                    null,
                    "Valor mínim (enter):",
                    "Paràmetres INTEGER",
                    JOptionPane.QUESTION_MESSAGE
            );
            if (minStr == null) return;
            String maxStr = JOptionPane.showInputDialog(
                    null,
                    "Valor màxim (enter):",
                    "Paràmetres INTEGER",
                    JOptionPane.QUESTION_MESSAGE
            );
            if (maxStr == null) return;
            int min = Integer.parseInt(minStr.trim());
            int max = Integer.parseInt(maxStr.trim());

            var p = ctrlDomini.creaPreguntaInteger(enunciat.trim(), min, max);
            if (p == null) {
                JOptionPane.showMessageDialog(null,
                        "Definició de pregunta INTEGER no vàlida.",
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }
            carregarPreguntesEnVistaGestio();
            JOptionPane.showMessageDialog(null,
                    "Pregunta INTEGER creada correctament (ID " + p.getId() + ").",
                    "Èxit",
                    JOptionPane.INFORMATION_MESSAGE);
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(null,
                    "Els valors mínim i màxim han de ser enters.",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(null,
                    ex.getMessage(),
                    "Error creant pregunta INTEGER",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    public void accioCrearPreguntaOrdinal() {
        String enunciat = JOptionPane.showInputDialog(
                null,
                "Introdueix l'enunciat de la pregunta ORDINAL:",
                "Nova pregunta ORDINAL",
                JOptionPane.QUESTION_MESSAGE
        );
        if (enunciat == null || enunciat.trim().isEmpty()) return;

        String txtOpc = JOptionPane.showInputDialog(
                null,
                "Introdueix les opcions ordenades separades per comes:",
                "Opcions ORDINAL",
                JOptionPane.QUESTION_MESSAGE
        );
        if (txtOpc == null) return;

        try {
            String[] parts = txtOpc.split(",");
            java.util.Vector<String> opcions = new java.util.Vector<>();
            for (String s : parts) {
                String t = s.trim();
                if (!t.isEmpty()) opcions.add(t);
            }

            var p = ctrlDomini.creaPreguntaOrdinal(enunciat.trim(), opcions);
            if (p == null) {
                JOptionPane.showMessageDialog(null,
                        "Definició de pregunta ORDINAL no vàlida.",
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }
            carregarPreguntesEnVistaGestio();
            JOptionPane.showMessageDialog(null,
                    "Pregunta ORDINAL creada correctament (ID " + p.getId() + ").",
                    "Èxit",
                    JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(null,
                    ex.getMessage(),
                    "Error creant pregunta ORDINAL",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    public void accioCrearPreguntaNomUnica() {
        String enunciat = JOptionPane.showInputDialog(
                null,
                "Introdueix l'enunciat de la pregunta NOMINAL única:",
                "Nova pregunta NOMINAL única",
                JOptionPane.QUESTION_MESSAGE
        );
        if (enunciat == null || enunciat.trim().isEmpty()) return;

        String txtOpc = JOptionPane.showInputDialog(
                null,
                "Introdueix les opcions separades per comes:",
                "Opcions NOMINAL única",
                JOptionPane.QUESTION_MESSAGE
        );
        if (txtOpc == null) return;

        try {
            String[] parts = txtOpc.split(",");
            java.util.Set<String> opcions = new java.util.HashSet<>();
            for (String s : parts) {
                String t = s.trim();
                if (!t.isEmpty()) opcions.add(t);
            }

            var p = ctrlDomini.creaPreguntaNominalUnica(enunciat.trim(), opcions);
            if (p == null) {
                JOptionPane.showMessageDialog(null,
                        "Definició de pregunta NOMINAL única no vàlida.",
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }
            carregarPreguntesEnVistaGestio();
            JOptionPane.showMessageDialog(null,
                    "Pregunta NOMINAL única creada correctament (ID " + p.getId() + ").",
                    "Èxit",
                    JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(null,
                    ex.getMessage(),
                    "Error creant pregunta NOMINAL única",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    public void accioCrearPreguntaNomMultiple() {
        String enunciat = JOptionPane.showInputDialog(
                null,
                "Introdueix l'enunciat de la pregunta NOMINAL múltiple:",
                "Nova pregunta NOMINAL múltiple",
                JOptionPane.QUESTION_MESSAGE
        );
        if (enunciat == null || enunciat.trim().isEmpty()) return;

        String txtOpc = JOptionPane.showInputDialog(
                null,
                "Introdueix les opcions separades per comes:",
                "Opcions NOMINAL múltiple",
                JOptionPane.QUESTION_MESSAGE
        );
        if (txtOpc == null) return;

        try {
            String[] parts = txtOpc.split(",");
            java.util.Set<String> opcions = new java.util.HashSet<>();
            for (String s : parts) {
                String t = s.trim();
                if (!t.isEmpty()) opcions.add(t);
            }

            String qMaxStr = JOptionPane.showInputDialog(
                    null,
                    "Quantes opcions com a màxim es poden marcar? (enter):",
                    "qMax NOMINAL múltiple",
                    JOptionPane.QUESTION_MESSAGE
            );
            if (qMaxStr == null) return;
            int qMax = Integer.parseInt(qMaxStr.trim());

            var p = ctrlDomini.creaPreguntaNominalMult(enunciat.trim(), opcions, qMax);
            if (p == null) {
                JOptionPane.showMessageDialog(null,
                        "Definició de pregunta NOMINAL múltiple no vàlida.",
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }
            carregarPreguntesEnVistaGestio();
            JOptionPane.showMessageDialog(null,
                    "Pregunta NOMINAL múltiple creada correctament (ID " + p.getId() + ").",
                    "Èxit",
                    JOptionPane.INFORMATION_MESSAGE);
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(null,
                    "qMax ha de ser un enter.",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(null,
                    ex.getMessage(),
                    "Error creant pregunta NOMINAL múltiple",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    public void accioEliminarPregunta() {
        Integer idP = vistaGestioPreguntes.getIdPreguntaSeleccionada();
        if (idP == null) {
            JOptionPane.showMessageDialog(null,
                    "Has de seleccionar una pregunta de la taula.",
                    "Cap pregunta seleccionada",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        int op = JOptionPane.showConfirmDialog(
                null,
                "Segur que vols eliminar aquesta pregunta?",
                "Confirmar eliminació",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
        );
        if (op != JOptionPane.YES_OPTION) return;

        try {
            ctrlDomini.eliminarPregunta(idP);
            carregarPreguntesEnVistaGestio();
            JOptionPane.showMessageDialog(null,
                    "Pregunta eliminada correctament.",
                    "Èxit",
                    JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(null,
                    ex.getMessage(),
                    "Error en eliminar la pregunta",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    public void accioModificarPregunta() {
        Integer idP = vistaGestioPreguntes.getIdPreguntaSeleccionada();
        if (idP == null) {
            JOptionPane.showMessageDialog(null,
                    "Has de seleccionar una pregunta de la taula.",
                    "Cap pregunta seleccionada",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        String nouEnunciat = JOptionPane.showInputDialog(
                null,
                "Introdueix el nou enunciat de la pregunta:",
                "Modificar pregunta",
                JOptionPane.QUESTION_MESSAGE
        );
        if (nouEnunciat == null || nouEnunciat.trim().isEmpty()) return;

        try {
            var p = ctrlDomini.getPregunta(idP);
            p.setEnunciat(nouEnunciat.trim());
            carregarPreguntesEnVistaGestio();
            JOptionPane.showMessageDialog(null,
                    "Pregunta modificada correctament.",
                    "Èxit",
                    JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(null,
                    ex.getMessage(),
                    "Error en modificar la pregunta",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    // ================== Accions d'Anàlisi ==================
    private void carregarEnquestesEnVistaAnalisi() {
        vistaAnalisi.buidarEnquestes();
        java.util.List<String[]> dades = ctrlDomini.getInfoEnquestes(); // {id, títol, ...}

        for (String[] fila : dades) {
            if (fila.length > 1) {
                String id = fila[0];
                String titol = fila[1];
                vistaAnalisi.afegirEnquesta(id + " - " + titol);
            }
        }
    }
    // ================== Accions d'Anàlisi ==================

    public void accioExecutarAnalisi(String enquestaSel,
                                     String algoritme,
                                     String kText,
                                     String maxIterText) {
        if (enquestaSel == null || enquestaSel.isBlank()) {
            JOptionPane.showMessageDialog(null,
                    "Has de seleccionar una enquesta",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        // 1) ID d’enquesta
        String idStr = enquestaSel.split(" - ")[0].trim();
        int idE;
        try {
            idE = Integer.parseInt(idStr);
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(null,
                    "ID d'enquesta invàlid: " + idStr,
                    "Error de format",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        // 2) Validar que K i MaxIter no estiguin buits
        if (kText == null || kText.trim().isEmpty()
                || maxIterText == null || maxIterText.trim().isEmpty()) {
            JOptionPane.showMessageDialog(null,
                    "Has d'introduir valors per K i Max iteracions.",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        int k;
        int maxIter;
        try {
            k = Integer.parseInt(kText.trim());
            maxIter = Integer.parseInt(maxIterText.trim());
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(null,
                    "K i Max iteracions han de ser enters (sense lletres ni comes).",
                    "Error de format",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        // 3) Executar l'anàlisi
        try {
            String resultat;
            if ("K-means".equals(algoritme)) {
                resultat = ctrlDomini.executarKMeansSobreEnquesta(idE, k, maxIter);
            } else { // "K-medoids"
                resultat = ctrlDomini.executarKMedoidsSobreEnquesta(idE, k, maxIter);
            }

            vistaAnalisi.setResultatsText(resultat);

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(null,
                    ex.getMessage(),
                    "Error en l'anàlisi",
                    JOptionPane.ERROR_MESSAGE);
        }
    }


    public void accioMostrarResultatsAnalisi() {
        // Si ja mostres el text a la pròpia vista, pots fer simplement:
        JOptionPane.showMessageDialog(null,
                "Els resultats ja es mostren a la part inferior de la finestra.",
                "Informació",
                JOptionPane.INFORMATION_MESSAGE);
    }
}