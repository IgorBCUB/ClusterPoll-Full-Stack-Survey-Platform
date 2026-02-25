package domini;
import java.util.ArrayList;
public class Usuari {
    private final Integer idU;
    private String nomU;
    private String email;
    private ArrayList<Integer> enquestescreades;//Enquestes fetes per ell
    private CtrlDomini ctrlDomini;//el controlador de domini


    public Usuari(Integer idU, String nomU, String email, CtrlDomini ctrlDomini) {
        this.idU = idU;
        this.nomU = nomU;
        this.email = email;
        this.enquestescreades = new ArrayList<Integer>();
        this.ctrlDomini = ctrlDomini;//Per accedir a les demés clases
    }

    public Integer getId() {
        return idU;
    }

    public void setNom(String nomU) {
        if (nomU == null || nomU.trim().isEmpty()) {
            throw new IllegalArgumentException("Error: El nom de l'usuari no pot estar buit.");
        }
        this.nomU = nomU;
    }

    public String getNom() {
        return nomU;
    }

    public void setEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("Error: L'email no pot estar buit.");
        }
        if (!email.contains("@")) {
            throw new IllegalArgumentException("Error: L'email ha de tenir un format vàlid (ha de contenir '@').");
        }
        this.email = email;
    }

    public String getEmail() {
        return email;
    }

    public ArrayList<Enquesta> getEnquestesUsuari() {
        if (enquestescreades == null || enquestescreades.isEmpty()) {
            throw new IllegalStateException("Aquest usuari no ha creat cap enquesta.");
        }

        ArrayList<Enquesta> enqs = new ArrayList<Enquesta>();
        for(int i = 0; i < enquestescreades.size(); i++){
            Integer idE = enquestescreades.get(i);
            try {
                enqs.add(this.getEnquestaUsuari(idE));
            } catch (Exception e) {
                System.out.println("Advertencia: No s'ha pogut carregar l'enquesta amb ID " + idE + ": " + e.getMessage());
            }
        }

        if (enqs.isEmpty()) {
            throw new IllegalStateException("Aquest usuari no té enquestes accessibles.");
        }
        return enqs;
    }

    public Enquesta getEnquestaUsuari(Integer idE) {
        if (idE == null) {
            throw new IllegalArgumentException("Error: L'ID de l'enquesta no pot ser nul.");
        }
        if (!enquestescreades.contains(idE)) {
            throw new IllegalArgumentException("Error: L'usuari no té accés a l'enquesta amb ID " + idE + ".");
        }
        try {
            return ctrlDomini.getEnquesta(idE);
        } catch (Exception e) {
            throw new IllegalArgumentException("Error: No s'ha pogut trobar l'enquesta amb ID " + idE + ": " + e.getMessage());
        }
    }

    public Enquesta creaEnquesta(String titol) {
        if (titol == null || titol.trim().isEmpty()) {
            throw new IllegalArgumentException("Error: El títol de l'enquesta no pot estar buit.");
        }
        // Crear enquesta amb l'ID global actual del CtrlDomini
        Integer idE = ctrlDomini.getCountE();   // assegura que tens aquest getter
        Enquesta enq = new Enquesta(idE, titol, this.getId(), ctrlDomini);
        enquestescreades.add(enq.getIdE());
        return enq;
    }
}