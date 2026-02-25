package presentacio;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

public class VistaGestioPreguntes extends JFrame {

    private JTable taulaPreguntes;
    private JButton btnCrearText;
    private JButton btnCrearInteger;
    private JButton btnCrearOrdinal;
    private JButton btnCrearNomUnica;
    private JButton btnCrearNomMultiple;
    private JButton btnEliminarPregunta;
    private JButton btnModificarPregunta;
    private JButton btnEnrere;

    private CtrlPresentacio controlador;

    public VistaGestioPreguntes() {
        setTitle("Gestió de preguntes");
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                controlador.sortirAplicacio();
            }
        });
        setSize(800, 400);
        setLocationRelativeTo(null);
        initComponents();
    }

    private void initComponents() {
        taulaPreguntes = new JTable();
        JScrollPane scroll = new JScrollPane(taulaPreguntes);

        btnCrearText = new JButton("Crear TEXT");
        btnCrearInteger = new JButton("Crear INTEGER");
        btnCrearOrdinal = new JButton("Crear ORDINAL");
        btnCrearNomUnica = new JButton("Crear NOMINAL única");
        btnCrearNomMultiple = new JButton("Crear NOMINAL múltiple");
        btnEliminarPregunta = new JButton("Eliminar pregunta");
        btnModificarPregunta = new JButton("Modificar pregunta");
        btnEnrere = new JButton("Enrere");

        JPanel buttons = new JPanel(new GridLayout(4, 2, 5, 5));
        buttons.add(btnCrearText);
        buttons.add(btnCrearInteger);
        buttons.add(btnCrearOrdinal);
        buttons.add(btnCrearNomUnica);
        buttons.add(btnCrearNomMultiple);
        buttons.add(btnEliminarPregunta);
        buttons.add(btnModificarPregunta);
        buttons.add(btnEnrere);

        JPanel main = new JPanel(new BorderLayout(10, 10));
        main.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        main.add(scroll, BorderLayout.CENTER);
        main.add(buttons, BorderLayout.EAST);

        setContentPane(main);
    }

    public void setListener(CtrlPresentacio controlador) {
        this.controlador = controlador;

        btnCrearText.addActionListener((ActionEvent e) -> controlador.accioCrearPreguntaText());
        btnCrearInteger.addActionListener((ActionEvent e) -> controlador.accioCrearPreguntaInteger());
        btnCrearOrdinal.addActionListener((ActionEvent e) -> controlador.accioCrearPreguntaOrdinal());
        btnCrearNomUnica.addActionListener((ActionEvent e) -> controlador.accioCrearPreguntaNomUnica());
        btnCrearNomMultiple.addActionListener((ActionEvent e) -> controlador.accioCrearPreguntaNomMultiple());
        btnEliminarPregunta.addActionListener((ActionEvent e) -> controlador.accioEliminarPregunta());
        btnModificarPregunta.addActionListener((ActionEvent e) -> controlador.accioModificarPregunta());
        btnEnrere.addActionListener((ActionEvent e) -> controlador.tornarAMenuCreadorDesDeGestioPreguntes());
    }

    // Carrega info de preguntes a la taula
    public void actualitzarTaulaPreguntes(java.util.List<String[]> dades) {
        String[] columnNames = {"ID", "Tipus", "Enunciat"};
        Object[][] data = new Object[dades.size()][3];
        for (int i = 0; i < dades.size(); ++i) {
            data[i][0] = dades.get(i)[0];
            data[i][1] = dades.get(i)[1];
            data[i][2] = dades.get(i)[2];
        }
        taulaPreguntes.setModel(new javax.swing.table.DefaultTableModel(
                data, columnNames
        ));
    }

    public Integer getIdPreguntaSeleccionada() {
        int fila = taulaPreguntes.getSelectedRow();
        if (fila == -1) return null;
        Object val = taulaPreguntes.getValueAt(fila, 0);
        if (val instanceof Integer) return (Integer) val;
        if (val instanceof String) return Integer.parseInt((String) val);
        return null;
    }

    public void mostrar() { setVisible(true); }
    public void amagar() { setVisible(false); }
}
