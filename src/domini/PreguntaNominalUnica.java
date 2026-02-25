package domini;
import java.util.Set;

public class PreguntaNominalUnica extends Pregunta {

    private Set<String> opcions;  // conjunt d'opcions no ordenades

    public PreguntaNominalUnica(String enunciat, Set<String> opcions) {
        super(enunciat);
        this.opcions = opcions;
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
        return true;
    }

    protected void modificarOpcions(Set<String> novesOpcions) {
        Set<String> preOpcions = opcions;
        opcions = novesOpcions;
        if (!validarDefinicio()) {
            opcions = preOpcions;
        }
    }

    @Override public Set<String> getOpcions() {
        return opcions;
    }
    public String getTipus() {
        return "nominal_unica";
    }
}
