package presentacio;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

public class VistaAnalisi extends JFrame {

    private JComboBox<String> comboEnquestes;
    private JComboBox<String> comboAlgoritme;
    private JTextField txtK;
    private JTextField txtMaxIter;
    private JButton btnExecutar;
    private JButton btnMostrarResultats;
    private JButton btnEnrere;
    private JTextArea areaResultats;

    private CtrlPresentacio controlador;

    public VistaAnalisi() {
        setTitle("Anàlisi de respostes");
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                controlador.sortirAplicacio();
            }
        });
        setSize(700, 500);
        setLocationRelativeTo(null);
        initComponents();
    }

    private void initComponents() {
        comboEnquestes = new JComboBox<>();
        comboAlgoritme = new JComboBox<>(new String[]{"K-means", "K-medoids"});
        txtK = new JTextField();
        txtMaxIter = new JTextField();
        btnExecutar = new JButton("Executar");
        btnMostrarResultats = new JButton("Mostrar resultats");
        btnEnrere = new JButton("Enrere");

        areaResultats = new JTextArea();
        areaResultats.setEditable(false);
        JScrollPane scrollRes = new JScrollPane(areaResultats);

        JPanel form = new JPanel(new GridLayout(4, 2, 5, 5));
        form.add(new JLabel("Enquesta:"));
        form.add(comboEnquestes);
        form.add(new JLabel("Algoritme:"));
        form.add(comboAlgoritme);
        form.add(new JLabel("K:"));
        form.add(txtK);
        form.add(new JLabel("Max iteracions:"));
        form.add(txtMaxIter);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttons.add(btnEnrere);
        buttons.add(btnExecutar);
        buttons.add(btnMostrarResultats);

        JPanel top = new JPanel(new BorderLayout(5, 5));
        top.add(form, BorderLayout.CENTER);
        top.add(buttons, BorderLayout.SOUTH);

        JPanel main = new JPanel(new BorderLayout(10, 10));
        main.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        main.add(top, BorderLayout.NORTH);
        main.add(scrollRes, BorderLayout.CENTER);

        setContentPane(main);
    }

    public void setListener(CtrlPresentacio controlador) {
        this.controlador = controlador;

        btnExecutar.addActionListener((ActionEvent e) -> controlador.accioExecutarAnalisi(
                getEnquestaSeleccionada(),
                (String) comboAlgoritme.getSelectedItem(),
                txtK.getText(),
                txtMaxIter.getText()
        ));

        btnMostrarResultats.addActionListener((ActionEvent e) -> controlador.accioMostrarResultatsAnalisi());
        btnEnrere.addActionListener((ActionEvent e) -> controlador.tornarAMenuCreadorDesDeAnalisi());
    }

    public void mostrar() { setVisible(true); }
    public void amagar() { setVisible(false); }

    public void setResultatsText(String text) {
        areaResultats.setText(text);
    }

    // ==== nous helpers per al controlador ====
    public void buidarEnquestes() {
        comboEnquestes.removeAllItems();
    }

    public void afegirEnquesta(String etiqueta) {
        comboEnquestes.addItem(etiqueta);
    }

    public String getEnquestaSeleccionada() {
        return (String) comboEnquestes.getSelectedItem();
    }
}
