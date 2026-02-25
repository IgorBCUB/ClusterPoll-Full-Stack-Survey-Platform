package domini;

import java.util.ArrayList;
import java.util.List;


public abstract class Algorisme {
    protected final CtrlDomini ctrlDomini;
    private List<Respostes> respostes;
    private final int idAlgorisme;
    private int k;

    protected Algorisme(CtrlDomini ctrlDomini, int idAlgorisme, int k) {
        if (ctrlDomini == null) throw new IllegalArgumentException("Error: CtrlDomini no pot ser nul.");
        if (idAlgorisme < 0) throw new IllegalArgumentException("Error: idAlgorisme ha de ser no negatiu.");
        if (k <= 0) throw new IllegalArgumentException("Error: k ha de ser positiu.");
        this.ctrlDomini = ctrlDomini;
        this.idAlgorisme = idAlgorisme;
        this.k = k;
    }

    protected Algorisme(CtrlDomini ctrlDomini, int idAlgorisme, int k, List<Respostes> respostes) {
        this(ctrlDomini, idAlgorisme, k);
        setRespostes(respostes);
    }

    public final int getIdAlgorisme() { return idAlgorisme; }
    public final int getK() { return k; }
    public final void setK(int k) {
        if (k <= 0) throw new IllegalArgumentException("Error: k ha de ser positiu.");
        this.k = k;
    }

    public final void setRespostes(List<Respostes> respostes) {
        if (respostes == null) throw new IllegalArgumentException("Error: la llista de respostes no pot ser nul·la.");
        if (respostes.isEmpty()) throw new IllegalArgumentException("Error: cal almenys una Respostes.");
        validaEstructura(respostes);
        this.respostes = new ArrayList<>(respostes);
    }

    protected final List<Respostes> getRespostes() {
        if (this.respostes == null) throw new IllegalStateException("Error: cal establir les respostes abans d’executar.");
        return this.respostes;
    }

    // Operació abstracta normal, tipada per clustering
    public abstract Object executa(int maxIter, boolean useAllPairsSwap);

    protected final void validaEstructura(List<Respostes> respostes) {
        Integer idE = respostes.get(0).getIdE();
        int mida = respostes.get(0).getRespostes().size();
        for (int i = 0; i < respostes.size(); ++i) {
            Respostes r = respostes.get(i);
            if (r == null) throw new IllegalArgumentException("Error: element Respostes nul a la posició " + i + ".");
            if (r.getIdE() != idE) throw new IllegalArgumentException("Error: totes les Respostes han de pertànyer a la mateixa enquesta.");
            if (r.getRespostes().size() != mida)
                throw new IllegalArgumentException("Error: totes les Respostes han de tenir el mateix nombre de Resposta i mateix ordre semàntic.");
        }
    }
}