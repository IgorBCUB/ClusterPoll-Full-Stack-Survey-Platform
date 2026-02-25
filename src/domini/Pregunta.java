package domini;
public abstract class Pregunta {

    private Integer idP; // assignat pel sistema
    private String enunciat;

    protected Pregunta(String enunciat) {
        setEnunciat(enunciat);
    }

    protected boolean validarDefinicio() {
        if (enunciat == null || enunciat.equals("")) {
            System.out.println("Error: l'enunciat no pot estar buit");
            return false;
        }
        return true;
    }

    protected void modificarEnunciat(String nou) {
        String preEnunciat = enunciat;
        enunciat = nou;
        if (!validarDefinicio()) {
            enunciat = preEnunciat;
        }
    }

    protected void setId(Integer nouId) { idP = nouId; }

    public void setEnunciat(String nou) { enunciat = nou; }
    public Integer getId() { return idP; }
    public String getEnunciat() { return enunciat; }
    public abstract String getTipus();
    protected Integer getNumOpcions() { return 0; }  // valor per defecte
    protected Object getOpcions() { return 0; }
    protected Integer getMinValue() { return 0; }
    protected Integer getQMax() { return 0; }
    protected Integer getMaxValue() { return 0; }
}
