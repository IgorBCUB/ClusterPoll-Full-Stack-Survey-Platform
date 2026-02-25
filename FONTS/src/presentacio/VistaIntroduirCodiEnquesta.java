package presentacio;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

public class VistaIntroduirCodiEnquesta extends JFrame {

    private JTextField txtCodiEnquesta;
    private JTextField txtIdUsuari;
    private JButton btnComencar;
    private JButton btnEnrere;

    private CtrlPresentacio controlador;

    public VistaIntroduirCodiEnquesta() {
        setTitle("Respondre enquesta");
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                controlador.sortirAplicacio();
            }
        });
        setSize(400, 200);
        setLocationRelativeTo(null);

        initComponents();
    }

    private void initComponents() {
        JLabel lblCodi = new JLabel("Codi enquesta (idE):");
        JLabel lblUsuari = new JLabel("ID usuari (idU):");

        txtCodiEnquesta = new JTextField();
        txtIdUsuari = new JTextField();

        btnComencar = new JButton("Començar");
        btnEnrere = new JButton("Enrere");

        JPanel center = new JPanel(new GridLayout(2, 2, 10, 10));
        center.add(lblCodi);
        center.add(txtCodiEnquesta);
        center.add(lblUsuari);
        center.add(txtIdUsuari);

        JPanel south = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        south.add(btnEnrere);
        south.add(btnComencar);

        JPanel main = new JPanel(new BorderLayout(10, 10));
        main.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        main.add(center, BorderLayout.CENTER);
        main.add(south, BorderLayout.SOUTH);

        setContentPane(main);
    }

    public void setListener(CtrlPresentacio controlador) {
        this.controlador = controlador;

        btnComencar.addActionListener((ActionEvent e) -> {
            String codi = txtCodiEnquesta.getText();
            String idU = txtIdUsuari.getText();
            controlador.iniciarRespostaEnquesta(codi, idU);
        });

        btnEnrere.addActionListener((ActionEvent e) -> {
            setVisible(false);
            controlador.tornarAPrincipalDesDeRespondedor();
        });

    }


    public void mostrar() {
        setVisible(true);
    }
}
