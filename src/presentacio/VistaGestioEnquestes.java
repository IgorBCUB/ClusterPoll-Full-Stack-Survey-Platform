package presentacio;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

public class VistaGestioEnquestes extends JFrame {

    private JTable taulaEnquestes;
    private JButton btnAfegirPregunta;
    private JButton btnEliminarPregunta;
    private JButton btnCrearEnquesta;
    private JButton btnEliminarEnquesta;
    private JButton btnModificarTitol;
    private JButton btnAfegirNovaPregunta; // NOU
    private JButton btnEnrere;
    private JButton btnConsultarEnquesta; // NOU
    private JButton btnImportarEnquesta; // NOU



    private CtrlPresentacio controlador;

    public VistaGestioEnquestes() {
        setTitle("Gestió d'enquestes");
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                controlador.sortirAplicacio();
            }
        });
        setSize(700, 400);
        setLocationRelativeTo(null);
        initComponents();
    }

    private void initComponents() {
        taulaEnquestes = new JTable();
        JScrollPane scroll = new JScrollPane(taulaEnquestes);

        btnAfegirPregunta = new JButton("Afegir pregunta");
        btnEliminarPregunta = new JButton("Eliminar pregunta");
        btnCrearEnquesta = new JButton("Nova enquesta");
        btnEliminarEnquesta = new JButton("Eliminar enquesta");
        btnModificarTitol = new JButton("Modificar títol");
        btnAfegirNovaPregunta = new JButton("Afegir nova pregunta"); // NOU
        btnConsultarEnquesta = new JButton("Consultar enquesta");    // NOU
        btnImportarEnquesta = new JButton("Importar enquesta");      // NOU
        btnEnrere = new JButton("Enrere");

        JPanel buttons = new JPanel(new GridLayout(5, 2, 5, 5));
        buttons.add(btnAfegirPregunta);
        buttons.add(btnEliminarPregunta);
        buttons.add(btnCrearEnquesta);
        buttons.add(btnEliminarEnquesta);
        buttons.add(btnModificarTitol);
        buttons.add(btnAfegirNovaPregunta); // NOU
        buttons.add(btnConsultarEnquesta); // NOU
        buttons.add(btnImportarEnquesta); // NOU
        buttons.add(btnEnrere);

        JPanel main = new JPanel(new BorderLayout(10, 10));
        main.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        main.add(scroll, BorderLayout.CENTER);
        main.add(buttons, BorderLayout.EAST);

        setContentPane(main);
    }

    public void setListener(CtrlPresentacio controlador) {
        this.controlador = controlador;
        btnAfegirPregunta.addActionListener((ActionEvent e) -> controlador.accioAfegirPreguntaEnquesta());
        btnEliminarPregunta.addActionListener((ActionEvent e) -> controlador.accioEliminarPreguntaEnquesta());
        btnCrearEnquesta.addActionListener((ActionEvent e) -> controlador.mostrarVistaCrearEnquesta());
        btnEliminarEnquesta.addActionListener((ActionEvent e) -> controlador.accioEliminarEnquesta());
        btnModificarTitol.addActionListener((ActionEvent e) -> controlador.accioModificarTitolEnquesta());
        btnAfegirNovaPregunta.addActionListener((ActionEvent e) -> controlador.accioCrearNovaPreguntaIEnllacar()); // NOU
        btnConsultarEnquesta.addActionListener(
                (ActionEvent e) -> controlador.accioConsultarEnquestaSeleccionada()
        );
        btnImportarEnquesta.addActionListener(
                (ActionEvent e) -> controlador.accioImportarEnquesta()
        );
        btnEnrere.addActionListener((ActionEvent e) -> controlador.tornarAMenuCreadorDesDeGestioEnquestes());
    }

    public Integer getIdEnquestaSeleccionada() {
        int fila = taulaEnquestes.getSelectedRow();
        if (fila == -1) return null;
        Object val = taulaEnquestes.getValueAt(fila, 0);
        if (val instanceof Integer) return (Integer) val;
        if (val instanceof String) return Integer.parseInt((String) val);
        return null;
    }

    public String demanarTitolEnquestaNova() {
        return JOptionPane.showInputDialog(this,
                "Introdueix el títol de la nova enquesta:",
                "Nova enquesta",
                JOptionPane.QUESTION_MESSAGE);
    }

    public Integer demanarIdUsuariCreador() {
        String txt = JOptionPane.showInputDialog(this,
                "Introdueix l'ID de l'usuari creador:",
                "Nova enquesta",
                JOptionPane.QUESTION_MESSAGE);
        if (txt == null) return null;
        try {
            return Integer.parseInt(txt.trim());
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this,
                    "L'ID d'usuari ha de ser un enter.",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            return null;
        }
    }

    // dadesEnquestes: cada fila = {id, titol}
    public void actualitzarTaulaEnquestes(java.util.List<String[]> dadesEnquestes) {
        String[] columnNames = {"ID", "Títol"};
        Object[][] data = new Object[dadesEnquestes.size()][2];
        for (int i = 0; i < dadesEnquestes.size(); ++i) {
            data[i][0] = dadesEnquestes.get(i)[0];
            data[i][1] = dadesEnquestes.get(i)[1];
        }
        taulaEnquestes.setModel(new javax.swing.table.DefaultTableModel(
                data, columnNames
        ));
    }

    public Integer demanarIdPreguntaPerAfegir() {
        String txt = JOptionPane.showInputDialog(this,
                "Introdueix l'ID de la pregunta a afegir:",
                "Afegir pregunta",
                JOptionPane.QUESTION_MESSAGE);
        if (txt == null) return null;
        try {
            return Integer.parseInt(txt.trim());
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this,
                    "L'ID de la pregunta ha de ser un enter.",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            return null;
        }
    }

    public Integer demanarIdPreguntaPerEliminar() {
        String txt = JOptionPane.showInputDialog(this,
                "Introdueix l'ID de la pregunta a eliminar:",
                "Eliminar pregunta",
                JOptionPane.QUESTION_MESSAGE);
        if (txt == null) return null;
        try {
            return Integer.parseInt(txt.trim());
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this,
                    "L'ID de la pregunta ha de ser un enter.",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            return null;
        }
    }

    public String demanarNouTitolEnquesta() {
        return JOptionPane.showInputDialog(this,
                "Introdueix el nou títol de l'enquesta:",
                "Modificar títol",
                JOptionPane.QUESTION_MESSAGE);
    }

    public JTable getTaulaEnquestes() { return taulaEnquestes; }
    public void mostrar() { setVisible(true); }
    public void amagar() { setVisible(false); }
}
