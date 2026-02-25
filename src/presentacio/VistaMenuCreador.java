package presentacio;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

public class VistaMenuCreador extends JFrame {

    private JButton btnGestioUsuaris;
    private JButton btnGestioEnquestes;
    private JButton btnGestioPreguntes;
    private JButton btnAnalisi;
    private JButton btnSortir;
    private CtrlPresentacio controlador;

    public VistaMenuCreador() {
        setTitle("Menú Creador");
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                controlador.sortirAplicacio();
            }
        });
        setSize(400, 350);
        setLocationRelativeTo(null);
        initComponents();
    }

    private void initComponents() {
        btnGestioUsuaris = new JButton("Gestió d'usuaris");
        btnGestioEnquestes = new JButton("Gestió d'enquestes");
        btnGestioPreguntes = new JButton("Gestió de preguntes");
        btnAnalisi = new JButton("Anàlisi");
        btnSortir = new JButton("Tornar");

        JPanel panel = new JPanel(new GridLayout(5, 1, 10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        panel.add(btnGestioUsuaris);
        panel.add(btnGestioEnquestes);
        panel.add(btnGestioPreguntes);
        panel.add(btnAnalisi);
        panel.add(btnSortir);

        setContentPane(panel);
    }

    public void setListener(CtrlPresentacio controlador) {
        this.controlador = controlador;

        btnGestioUsuaris.addActionListener(e -> controlador.mostrarVistaGestioUsuaris());
        btnGestioEnquestes.addActionListener((ActionEvent e) -> controlador.mostrarVistaGestioEnquestes());
        btnGestioPreguntes.addActionListener((ActionEvent e) -> controlador.mostrarVistaGestioPreguntes());
        btnAnalisi.addActionListener((ActionEvent e) -> controlador.mostrarVistaAnalisi());
        btnSortir.addActionListener((ActionEvent e) -> controlador.tornarAPrincipalDesDeCreador());
    }

    public void mostrar() { setVisible(true); }
    public void amagar() { setVisible(false); }
}
