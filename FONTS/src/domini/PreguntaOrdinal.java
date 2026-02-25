package domini;
import java.util.Vector;

public class PreguntaOrdinal extends Pregunta {

    private Vector<String> opcions;

    public PreguntaOrdinal(String enunciat, Vector<String> opcions) {
        super(enunciat);
        this.opcions = opcions;
        validarDefinicio();
    }

    @Override
    protected boolean validarDefinicio() {
        if (!super.validarDefinicio()) return false;
        if (opcions == null || opcions.isEmpty()) {
            System.out.println("Error: la llista d'opcions no pot estar buida");
            return false;
        }
        for (String opcio : opcions) {
            if (opcio == null || opcio.equals("")) {
                System.out.println("Error: hi ha una opció buida o nul·la");
                return false;
            }
        }
        return true;
    }

    protected void modificarOpcions(Vector<String> noves) {
        Vector<String> preOpcions = opcions;
        opcions = noves;
        if (!validarDefinicio()) {
            opcions = preOpcions;
        }
    }

    @Override public Integer getNumOpcions() { return opcions.size(); }
    @Override public Vector<String> getOpcions() {
        return opcions;
    }
    public String getTipus() {
        return "ordinal";
    }
}
