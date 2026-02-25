package presentacio;

import domini.CtrlDomini;
import domini.Pregunta;
import domini.PreguntaOrdinal;
import domini.PreguntaNominalUnica;
import domini.PreguntaNominalMult;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.*;
import java.util.List;

public class VistaGestioRespostes extends JFrame {

    private final int idE;
    private final int idU;

    private JPanel panelPreguntes;
    private JButton btnEnviar;
    private JButton btnCancel;
    private CtrlPresentacio controlador;

    private static class EntradaPregunta {
        int idP;
        String tipus; // "text", "integer", "ordinal", "nominal_unica", "nominal_multiple"
        JComponent component;
        java.util.List<JCheckBox> checkBoxes; // només per nominal_multiple

        EntradaPregunta(int idP, String tipus, JComponent c) {
            this.idP = idP;
            this.tipus = tipus;
            this.component = c;
            this.checkBoxes = null;
        }

        EntradaPregunta(int idP, String tipus, java.util.List<JCheckBox> boxes) {
            this.idP = idP;
            this.tipus = tipus;
            this.component = null;
            this.checkBoxes = boxes;
        }
    }

    private java.util.List<EntradaPregunta> entrades;

    public VistaGestioRespostes(int idE, int idU, java.util.List idsPreguntes, CtrlDomini ctrlDomini) {
        this.idE = idE;
        this.idU = idU;
        this.entrades = new ArrayList<>();

        setTitle("Respondre enquesta " + idE + " (usuari " + idU + ")");
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                controlador.sortirAplicacio();
            }
        });
        setSize(700, 500);
        setLocationRelativeTo(null);

        initComponents(idsPreguntes, ctrlDomini);
    }

    private void initComponents(java.util.List idsPreguntes, CtrlDomini ctrlDomini) {
        panelPreguntes = new JPanel();
        panelPreguntes.setLayout(new BoxLayout(panelPreguntes, BoxLayout.Y_AXIS));

        for (Object o : idsPreguntes) {
            Integer idP = (Integer) o;
            Pregunta p = ctrlDomini.getPregunta(idP);
            String tipus = p.getTipus(); // "text","integer","ordinal","nominal_unica","nominal_multiple"
            String enunciat = p.getEnunciat();

            JPanel fila = new JPanel(new BorderLayout(5, 5));
            JLabel lblEnunciat = new JLabel(enunciat);
            fila.add(lblEnunciat, BorderLayout.NORTH);

            JComponent compResposta;
            EntradaPregunta ep;

            switch (tipus) {
                case "text": {
                    JTextField txt = new JTextField();
                    compResposta = txt;
                    ep = new EntradaPregunta(idP, tipus, txt);
                    break;
                }
                case "integer": {
                    JTextField txt = new JTextField();
                    compResposta = txt;
                    ep = new EntradaPregunta(idP, tipus, txt);
                    break;
                }
                case "ordinal": {
                    // les opcions estan dins PreguntaOrdinal
                    PreguntaOrdinal po = (PreguntaOrdinal) p;
                    @SuppressWarnings("unchecked")
                    Vector<String> opcions = (Vector<String>) po.getOpcions(); // al domini uses Vector
                    JComboBox<String> combo = new JComboBox<>(opcions);
                    compResposta = combo;
                    ep = new EntradaPregunta(idP, tipus, combo);
                    break;
                }
                case "nominal_unica": {
                    PreguntaNominalUnica pu = (PreguntaNominalUnica) p;
                    @SuppressWarnings("unchecked")
                    Set<String> opcions = (Set<String>) pu.getOpcions();
                    JComboBox<String> combo = new JComboBox<>(opcions.toArray(new String[0]));
                    compResposta = combo;
                    ep = new EntradaPregunta(idP, tipus, combo);
                    break;
                }
                case "nominal_multiple": {
                    PreguntaNominalMult pm = (PreguntaNominalMult) p;
                    @SuppressWarnings("unchecked")
                    Set<String> opcions = (Set<String>) pm.getOpcions();
                    JPanel panelOpcions = new JPanel();
                    panelOpcions.setLayout(new BoxLayout(panelOpcions, BoxLayout.Y_AXIS));
                    java.util.List<JCheckBox> boxes = new ArrayList<>();
                    for (String op : opcions) {
                        JCheckBox cb = new JCheckBox(op);
                        panelOpcions.add(cb);
                        boxes.add(cb);
                    }
                    fila.add(panelOpcions, BorderLayout.CENTER);
                    ep = new EntradaPregunta(idP, tipus, boxes);
                    entrades.add(ep);
                    panelPreguntes.add(fila);
                    panelPreguntes.add(Box.createVerticalStrut(10));
                    continue; // ja hem afegit el component
                }
                default: {
                    // Per si apareix algun tipus desconegut, tractem-ho com un text
                    JTextField txt = new JTextField();
                    compResposta = txt;
                    ep = new EntradaPregunta(idP, tipus, txt);
                    break;
                }
            }

            fila.add(compResposta, BorderLayout.CENTER);
            entrades.add(ep);
            panelPreguntes.add(fila);
            panelPreguntes.add(Box.createVerticalStrut(10));
        }

        JScrollPane scroll = new JScrollPane(panelPreguntes);

        btnEnviar = new JButton("Enviar respostes");
        btnCancel = new JButton("Cancel·lar");

        JPanel south = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        south.add(btnCancel);
        south.add(btnEnviar);

        JPanel main = new JPanel(new BorderLayout(10, 10));
        main.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        main.add(scroll, BorderLayout.CENTER);
        main.add(south, BorderLayout.SOUTH);

        setContentPane(main);
    }

    public void setListener(CtrlPresentacio controlador) {
        this.controlador = controlador;

        btnEnviar.addActionListener((ActionEvent e) -> {
            try {
                Map<Integer, List<String>> dades = recollirRespostes();
                controlador.accioEnviarRespostes(idE, idU, dades);
            } catch (IllegalArgumentException ex) {
                JOptionPane.showMessageDialog(this,
                        ex.getMessage(),
                        "Error en les respostes",
                        JOptionPane.ERROR_MESSAGE);
            }
        });

        btnCancel.addActionListener((ActionEvent e) -> {
            setVisible(false);
            controlador.tancarVistaRespostes();
        });
    }

    private Map<Integer, List<String>> recollirRespostes() {
        Map<Integer, List<String>> resultat = new HashMap<>();

        for (EntradaPregunta ep : entrades) {
            List<String> contingut = new ArrayList<>();
            String tipus = ep.tipus;

            if (ep.checkBoxes != null && "nominal_multiple".equals(tipus)) {
                for (JCheckBox cb : ep.checkBoxes) {
                    if (cb.isSelected()) contingut.add(cb.getText());
                }
            } else if (ep.component instanceof JTextField) {
                String txt = ((JTextField) ep.component).getText();
                if (txt != null && !txt.isBlank()) contingut.add(txt.trim());
            } else if (ep.component instanceof JComboBox) {
                Object sel = ((JComboBox<?>) ep.component).getSelectedItem();
                if (sel != null) contingut.add(sel.toString());
            }

            resultat.put(ep.idP, contingut);
        }

        return resultat;
    }

    public void mostrar() { setVisible(true); }

    public void amagar() { setVisible(false); }
}
