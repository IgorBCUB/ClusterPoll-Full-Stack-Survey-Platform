package presentacio;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

public class VistaGestioUsuari extends JFrame {

    private JTable taulaUsuaris;
    private DefaultTableModel modelUsuaris;

    private JButton btnCrear;
    private JButton btnConsultar;
    private JButton btnModificar;
    private JButton btnCrearEnquesta;
    private JButton btnLlistarEnquestes;
    private JButton btnEnrere;

    private CtrlPresentacio controlador;

    public VistaGestioUsuari() {
        setTitle("Gestió d'usuaris");
        setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
        setSize(700, 400);
        setLocationRelativeTo(null);
        initComponents();
    }

    private void initComponents() {
        // Taula usuaris
        modelUsuaris = new DefaultTableModel(
                new Object[]{"ID", "Nom", "Email"}, 0
        ) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // només lectura
            }
        };
        taulaUsuaris = new JTable(modelUsuaris);
        JScrollPane scroll = new JScrollPane(taulaUsuaris);

        // Botons
        btnCrear = new JButton("Crear usuari");
        btnConsultar = new JButton("Consultar usuari");
        btnModificar = new JButton("Modificar usuari");
        btnCrearEnquesta = new JButton("Crear enquesta per usuari");
        btnLlistarEnquestes = new JButton("Llistar enquestes d'un usuari");
        btnEnrere = new JButton("Enrere");

        JPanel panBotons = new JPanel(new GridLayout(3, 2, 5, 5));
        panBotons.add(btnCrear);
        panBotons.add(btnConsultar);
        panBotons.add(btnModificar);
        panBotons.add(btnCrearEnquesta);
        panBotons.add(btnLlistarEnquestes);
        panBotons.add(btnEnrere);

        JPanel main = new JPanel(new BorderLayout(10, 10));
        main.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        main.add(scroll, BorderLayout.CENTER);
        main.add(panBotons, BorderLayout.EAST);

        setContentPane(main);
    }

    public void setListener(CtrlPresentacio controlador) {
        this.controlador = controlador;

        btnCrear.addActionListener(e -> controlador.accioCrearUsuari());
        btnConsultar.addActionListener(e -> controlador.accioConsultarUsuari());
        btnModificar.addActionListener(e -> controlador.accioModificarUsuari());
        btnCrearEnquesta.addActionListener(e -> controlador.accioCrearEnquestaUsuari());
        btnLlistarEnquestes.addActionListener(e -> controlador.accioLlistarEnquestesUsuari());
        btnEnrere.addActionListener(e -> controlador.tornarAMenuCreadorDesDeGestioUsuaris());
    }

    // ===== Helpers per al controlador =====

    public void actualitzarTaulaUsuaris(java.util.List<String[]> dades) {
        modelUsuaris.setRowCount(0);
        for (String[] fila : dades) modelUsuaris.addRow(fila);
    }

    public Integer getIdUsuariSeleccionat() {
        int row = taulaUsuaris.getSelectedRow();
        if (row < 0) return null;
        Object val = modelUsuaris.getValueAt(row, 0);
        try {
            return Integer.parseInt(val.toString());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    public void mostrar() { setVisible(true); }
    public void amagar() { setVisible(false); }
}
