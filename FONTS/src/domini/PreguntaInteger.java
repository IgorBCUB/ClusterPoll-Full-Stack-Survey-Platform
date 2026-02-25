package domini;

public class PreguntaInteger extends Pregunta {

    private Integer minValue;
    private Integer maxValue;

    public PreguntaInteger(String enunciat,
                           Integer minValue, Integer maxValue) {
        super(enunciat);
        this.minValue = minValue;
        this.maxValue = maxValue;
        validarDefinicio();
    }
    @Override
    protected boolean validarDefinicio() {
        if (!super.validarDefinicio()) return false;
        if (minValue != null && maxValue != null && minValue > maxValue) {
            System.out.println("Error: minValue no pot ser superior a maxValue");
            return false;
        }
        return true;
    }
    protected void modificarMinValue(Integer nouMin) {
        Integer preMin = minValue;
        minValue = nouMin;
        if (!validarDefinicio()) {
            minValue = preMin;
        }
    }
    protected void modificarMaxValue(Integer nouMax) {
        Integer preMax = maxValue;
        maxValue = nouMax;
        if (!validarDefinicio()) {
            maxValue = preMax;
        }
    }

    @Override public Integer getMinValue() { return minValue; }
    @Override public Integer getMaxValue() { return maxValue; }
    @Override public String getTipus() { return "integer"; }
}