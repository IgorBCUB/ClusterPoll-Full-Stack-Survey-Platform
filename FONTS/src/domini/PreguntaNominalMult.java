package domini;
import java.util.Set;

public class PreguntaNominalMult extends Pregunta {

    private Set<String> opcions;  // conjunt d'opcions possibles
    private Integer qMax;         // màxim d'opcions que es poden triar

    public PreguntaNominalMult(String enunciat, Set<String> opcions, Integer qMax) {
        super(enunciat);
        this.opcions = opcions;
        this.qMax = qMax;
        validarDefinicio();
    }

    @Override
    protected boolean validarDefinicio() {
        if (!super.validarDefinicio()) return false;
        if (opcions == null || opcions.isEmpty()) {
            System.out.println("Error: el conjunt d'opcions no pot estar buit");
            return false;
        }
        for (String opcio : opcions) {
            if (opcio == null || opcio.equals("")) {
                System.out.println("Error: hi ha una opció buida o nul·la");
                return false;
            }
        }
        if (qMax == null || qMax <= 0) {
            System.out.println("Error: qMax ha de ser més gran que 0");
            return false;
        }
        if (qMax > opcions.size()) {
            System.out.println("Error: qMax no pot ser més gran que el nombre d'opcions");
            return false;
        }
        return true;
    }

    protected void modificarOpcions(Set<String> novesOpcions) {
        Set<String> preOpcions = opcions;
        opcions = novesOpcions;
        if (!validarDefinicio()) {
            opcions = preOpcions;
        }
    }

    protected void modificarQMax(Integer nouQMax) {
        Integer preQMax = qMax;
        qMax = nouQMax;
        if (!validarDefinicio()) {
            qMax = preQMax;
        }
    }

    @Override public Set<String> getOpcions() {
        return opcions;
    }
    public Integer getQMax() {
        return qMax;
    }
    public String getTipus() {
        return "nominal_multiple";
    }
}
