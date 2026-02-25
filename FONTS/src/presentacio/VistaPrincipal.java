package presentacio;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

public class VistaPrincipal extends JFrame {

    private JButton btnCreador;
    private JButton btnRespondedor;
    private CtrlPresentacio controlador;

    public VistaPrincipal() {
        setTitle("Sistema d'enquestes");
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
        btnCreador = new JButton("Sóc Creador");
        btnRespondedor = new JButton("Sóc Responedor");

        JPanel panel = new JPanel(new GridLayout(2, 1, 10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        panel.add(btnCreador);
        panel.add(btnRespondedor);

        setContentPane(panel);
    }

    public void setListener(CtrlPresentacio controlador) {
        this.controlador = controlador;

        btnCreador.addActionListener((ActionEvent e) ->
                controlador.mostrarPantallaCreador());

        btnRespondedor.addActionListener((ActionEvent e) ->
                controlador.mostrarPantallaRespondedor());
    }

    public void mostrar() {
        setVisible(true);
    }

    public void amagar() {
        setVisible(false);
    }
}
