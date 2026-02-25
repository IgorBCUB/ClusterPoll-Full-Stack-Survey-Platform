package presentacio;

import domini.CtrlDomini;
import javax.swing.SwingUtilities;

public class MainGUI {
    public static void main(String[] args) {
        CtrlDomini ctrlDomini = new CtrlDomini();
        CtrlPresentacio ctrlPresentacio = new CtrlPresentacio(ctrlDomini);

        SwingUtilities.invokeLater(ctrlPresentacio::inicia);
    }
}
