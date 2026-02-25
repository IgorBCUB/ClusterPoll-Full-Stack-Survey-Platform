package domini;

public class PreguntaText extends Pregunta {

    public PreguntaText(String enunciat) {
        super(enunciat);
        validarDefinicio();
    }

    @Override protected boolean validarDefinicio() {
        return super.validarDefinicio();
    }

    public String getTipus() {
        return "string";
    }
}
