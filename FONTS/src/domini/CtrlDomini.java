package domini;
import java.util.*;
import domini.Analisi;
import domini.KMeans;
import domini.KMedoids;
import persistencia.CtrlPersistencia;
import java.util.stream.Collectors;
import java.io.IOException;

public class CtrlDomini {
    //Totes les intancies de les clases creades
    private CtrlPersistencia ctrlPersistencia;
    private ArrayList<Enquesta> enquestes;//Totes les enquestes creades
    private ArrayList<Usuari> usuaris;//Tots els usuaris creats
    private ArrayList<Pregunta> preguntes;//Totes les preguntes creades // ns si s'ha d'inicialitzar com a nul
    private Map<Integer, Map<Integer, Map<Integer, Resposta>>> respostes = new HashMap<>();
    private Integer countP;//Comptador per ID de pregunta // ns si s'ha d'inicialitzar a zero
    private Integer countE;//Sirve para meter los identificadores de  Enquesta i que solo haya uno para cada instancia
    private Integer countU;//Sirve para meter los identificadores de Usuari i que solo haya uno para cada instancia

    public CtrlDomini() {//Creadora
        enquestes = new ArrayList<Enquesta>();
        usuaris = new ArrayList<Usuari>();
        preguntes = new ArrayList<Pregunta>();
        respostes = new HashMap<>();
        countE = 0;
        countU = 0;
        countP = 0;
        ctrlPersistencia = new CtrlPersistencia();
        inicialitzarUsuaris();
    }

    private void inicialitzarUsuaris() {
        // Llista on guardarem tots els usuaris en memòria
        usuaris = new ArrayList<>();

        // 1. Demanem a la capa de persistència totes les files d'usuaris
        ArrayList<ArrayList<String>> dadesUsuaris = ctrlPersistencia.carregarUsuaris();

        // 2. Convertim cada fila (ArrayList<String>) en un objecte Usuari
        for (ArrayList<String> fila : dadesUsuaris) {

            // Com a mínim: nom i correu
            if (fila.size() < 2) {
                throw new IllegalArgumentException(
                        "Fila d'usuari amb nombre de camps invàlid: " + fila
                );
            }

            String nom = fila.get(0);
            String correu = fila.get(1);

            this.creaUsuari(nom, correu);
        }
    }
    public Integer getCountE() {
        return countE;
    }

    private void guardarUsuaris()
    {
        ArrayList<ArrayList<String>> dades = new ArrayList<>();

        for (Usuari u : usuaris) {
            ArrayList<String> fila = new ArrayList<>();
            fila.add(u.getNom());
            fila.add(u.getEmail());
            dades.add(fila);
        }

        ctrlPersistencia.guardarUsuaris(dades);
    }

    private void guardarEnquestes() {
        for (Enquesta e : enquestes) {//Les respostes (1 per cada usuari)
            guardarEnquesta(e);
        }
    }

    private void guardarEnquesta(Enquesta e) {
        // 1. Inicialitzar la estructura de dades: List of Rows, on cada Row és una List of Cells (String)
        ArrayList<ArrayList<String>> dadesCSV = new ArrayList<>();
        String titol = e.getTitol();

        // Asumimos que e.getPreguntes() devuelve las preguntas en el orden de la encuesta
        ArrayList<Pregunta> preguntes = e.getPreguntes();

        // --- CONSTRUIR CABECERA ---
        ArrayList<String> header = new ArrayList<>();
        header.add("ID_Usuari");
        // Afegim els enunciats (sense la coma inicial, ja que persistència ha de gestionar les comes)
        for(Pregunta p : preguntes) {
            header.add(p.getEnunciat());
        }
        dadesCSV.add(header); // Salto de linea implementado: afegim la fila de la cabecera.

        // --- CONSTRUIR FILAS DE DATOS ---
        int ide = e.getIdE();

        // 'usuaris' es la llista de tots els usuaris que tens al CtrlDomini.
        for (Usuari u : usuaris) {
            int idu = u.getId();

            // Ús del nou mètode: retorna un ArrayList<Resposta> o un ArrayList buit.
            ArrayList<Resposta> llistaRespostes = getLlistaRespostes(ide, idu);

            // 1. COMPROVACIÓ INICIAL: Si la llista és buida (perquè no hi ha respostes), saltem l'usuari.
            if (llistaRespostes.isEmpty()) {
                continue;
            }

            // Mapejar les respostes per ID de Pregunta (per assegurar l'ordre i la cerca ràpida)
            Map<Integer, Resposta> mapRespostes = new HashMap<>();
            for (Resposta r : llistaRespostes) {
                mapRespostes.put(r.getIdPregunta(), r);
            }

            // Llista temporal per guardar només el contingut de les respostes de les preguntes
            ArrayList<String> respostesPreguntes = new ArrayList<>();

            // 2. GENERACIÓ DEL CONTINGUT DE LES RESPOSTES (Columna per columna)
            for(Pregunta p : preguntes) {
                Resposta resposta = mapRespostes.get(p.getId());
                String contingutCSV = "";

                if (resposta != null) {
                    ArrayList<String> contingutList = resposta.getContingut();
                    // Uneix els elements (e.g., opcions múltiples) amb comes
                    contingutCSV = String.join(",", contingutList);
                }

                // Afegim la resposta (serà "" si no hi ha resposta o si la resposta és una cadena buida)
                respostesPreguntes.add(contingutCSV);
            }

            // 3. COMPROVACIÓ FINAL MILLORADA: Mirem si alguna resposta té contingut real.
            // Evita que s'afegeixin files com "0,,,,,,,,,,,".
            boolean haRespostAlgunaCosa = respostesPreguntes.stream().anyMatch(s -> !s.isEmpty());

            if (haRespostAlgunaCosa) {
                // Si hi ha dades, construïm i afegim la fila completa.
                ArrayList<String> filaUsuari = new ArrayList<>();
                filaUsuari.add(String.valueOf(idu)); // 1r element: ID Usuari
                filaUsuari.addAll(respostesPreguntes); // 2n al N: respostes
                dadesCSV.add(filaUsuari);
            }
        }
        // 4. Crida a Persistència i gestió de l'IOException
        try {
            ctrlPersistencia.guardarEnquesta(titol, dadesCSV);
        } catch (IOException ex) {
            // En cas d'error d'E/S, es notifica i es llança una excepció
            System.err.println("Error fatal al guardar l'enquesta: " + ex.getMessage());
            throw new RuntimeException("Error al guardar l'enquesta en persistència.", ex);
        }
    }

    public void tancarAplicacio()
    {
        guardarUsuaris();
        guardarEnquestes();
    }
    //Metodos con la clase Enquesta
    public Enquesta creaEnquesta(String titol, Integer idUsuari) {
        Usuari u = getUsuari(idUsuari);      // valida que l’usuari existeix
        Enquesta enq = u.creaEnquesta(titol); // usa el mètode de Usuari que ja registra l’ID
        enquestes.add(enq);                   // guarda-la al repositori global
        countE++;                             // incrementa el comptador global
        return enq;
    }


    public Enquesta getEnquesta(Integer idE){
        if (idE == null)
        {
            throw new IllegalArgumentException("Error: L'ID de l'enquesta no pot ser nul.");
        }
        if (idE < 0) {
            throw new IllegalArgumentException("Error: L'ID de l'enquesta no pot ser negatiu.");
        }
        if (idE >= enquestes.size()) {
            throw new IndexOutOfBoundsException("Error: No existeix cap enquesta amb ID " + idE + ".");
        }
        Enquesta e = (Enquesta) enquestes.get(idE);
        if (e == null) {
            throw new IllegalStateException("Error: L'enquesta amb ID " + idE + " ha estat eliminada.");
        }
        return e;
    }


    public boolean existeixUsuari(Integer idUsuari){
        return idUsuari != null
                && idUsuari >= 0
                && idUsuari < usuaris.size()
                && usuaris.get(idUsuari) != null;
    }

    public void afegeixPreguntaAEnquesta(Integer idE, Integer idP) {
        Enquesta e = getEnquesta(idE);
        e.afegeixPregunta(idP);
    }

    public void eliminaPreguntaAEnquesta(Integer idE, Integer idP) {
        Enquesta e = getEnquesta(idE);
        e.eliminaPregunta(idP);
    }

    public void setTitolEnquesta(Integer idE, String nouTitol) {
        Enquesta e = getEnquesta(idE);
        e.setTitol(nouTitol);
    }

    public java.util.List getInfoEnquestes()
    {
        java.util.List res = new java.util.ArrayList<>();
        for (Object o : enquestes)
        {
            if (o == null) continue; //posició buida (enquesta eliminada)
            Enquesta e = (Enquesta) o;
            String[] fila = new String[2];
            fila[0] = e.getIdE().toString();
            fila[1] = e.getTitol();
            res.add(fila);
        }
        return res;
    }


    //Elimina una enquesta pel seu ID
    public void eliminarEnquesta(Integer idE){
        if (idE == null) {
            throw new IllegalArgumentException("Error: L'ID de l'enquesta no pot ser nul.");
        }
        if (idE < 0 || idE >= enquestes.size()) {
            throw new IndexOutOfBoundsException("Error: No existeix cap enquesta amb ID " + idE + ".");
        }
        enquestes.set(idE, null);
    }



    //Informació resumida de totes les preguntes: {id, tipus, enunciat}
    public java.util.List<String[]> getInfoPreguntes(){
        java.util.List<String[]> res=new java.util.ArrayList<>();
        for (Object o : preguntes)
        {
            Pregunta p = (Pregunta) o;
            String[] fila = new String[3];
            fila[0] = p.getId().toString();
            fila[1] = p.getClass().getSimpleName();
            fila[2] = p.getEnunciat();
            res.add(fila);
        }
        return res;
    }

    //Elimina una pregunta de la llista de preguntes (no toca enquestes)
    public void eliminarPregunta(Integer idP){
        Pregunta p = getPregunta(idP); //valida id
        preguntes.remove(p);
    }



    //Mètodes de la classe Pregunta(creaPreguntaX i getPregunta necessaris per Enquesta)

    public PreguntaText creaPreguntaText(String enunciat) {
        PreguntaText p = new PreguntaText(enunciat);
        if (!p.validarDefinicio()) return null;
        p.setId(countP++);
        preguntes.add(p);
        return p;
    }

    public PreguntaInteger creaPreguntaInteger(String enunciat, int min, int max) {
        PreguntaInteger p = new PreguntaInteger(enunciat, min, max);
        if (!p.validarDefinicio()) return null;
        p.setId(countP++);
        preguntes.add(p);
        return p;
    }

    public PreguntaNominalUnica creaPreguntaNominalUnica(String enunciat, Set<String> opcions) {
        PreguntaNominalUnica p = new PreguntaNominalUnica(enunciat, opcions);
        if (!p.validarDefinicio()) return null;
        p.setId(countP++);
        preguntes.add(p);
        return p;
    }

    public PreguntaNominalMult creaPreguntaNominalMult(String enunciat, Set<String> opcions, int qMax) {
        PreguntaNominalMult p = new PreguntaNominalMult(enunciat, opcions, qMax);
        if (!p.validarDefinicio()) return null;
        p.setId(countP++);
        preguntes.add(p);
        return p;
    }

    public PreguntaOrdinal creaPreguntaOrdinal(String enunciat, Vector<String> opcions) {
        PreguntaOrdinal p = new PreguntaOrdinal(enunciat, opcions);
        if (!p.validarDefinicio()) return null;
        p.setId(countP++);
        preguntes.add(p);
        return p;
    }
    public ArrayList<Pregunta> getPreguntes() {
        return preguntes;
    }
    public Pregunta getPregunta(Integer idP) {
        for (Pregunta p : preguntes) if (p.getId().equals(idP)) return p;
        throw new IllegalArgumentException("No existeix cap pregunta amb aquest id.");
    }

    //Metodos con la clase Usuari
    public Usuari creaUsuari(String nomU, String email) {
        //Validaciones (mirar que sean parametros correctos)
        if (nomU == null || nomU.trim().isEmpty()) {
            throw new IllegalArgumentException("Error: El nom de l'usuari no pot estar buit.");
        }
        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("Error: L'email no pot estar buit.");
        }
        if (!email.contains("@")) {
            throw new IllegalArgumentException("Error: L'email ha de tenir un format vàlid (ha de contenir '@').");
        }
        for (Usuari u : usuaris) {//Verificar si hi ha un usuari emb el mateix email
            if (u.getEmail().equals(email)) {
                throw new IllegalArgumentException("Error: Ja existeix un usuari amb aquest email.");
            }
        }
        Usuari nouUsuari = new Usuari(countU, nomU, email, this);
        ++countU;
        usuaris.add(nouUsuari);
        return nouUsuari;
    }

    public Usuari getUsuari(Integer idU) {
        if (idU == null) {
            throw new IllegalArgumentException("Error: L'ID de l'usuari no pot ser nul.");
        }
        if (idU < 0) {
            throw new IllegalArgumentException("Error: L'ID de l'usuari no pot ser negatiu.");
        }
        if (idU >= usuaris.size()) {
            throw new IndexOutOfBoundsException("Error: No existeix cap usuari amb ID " + idU + ".");
        }
        return usuaris.get(idU);
    }

    public ArrayList<Usuari> getallUsuaris() {
        if (usuaris.isEmpty()) {
            throw new IllegalStateException("Error: No hi ha usuaris registrats al sistema.");
        }
        return usuaris;
    }

    public ArrayList<Enquesta> getEnquestesUsuari(Integer idU) {
        Usuari u = this.getUsuari(idU);
        return u.getEnquestesUsuari();
    }

    //Helpers de Respostes
    private static void requireNonNullPosInteger(Integer id, String nom) {
        if (id == null) throw new NullPointerException(nom + " no pot ser null");
        if (id < 0)      throw new IllegalArgumentException(nom + " no pot ser negatiu");
    }

    private Map<Integer, Map<Integer, Resposta>> submapEUOrCreate(int idE) {
        return respostes.computeIfAbsent(idE, k -> new HashMap<>());
    }

    private Map<Integer, Map<Integer, Resposta>> submapEUOrNull(int idE) {
        return respostes.get(idE);
    }

    //Respostes
    public Respostes creaRespostes(Integer idE, Integer idU) {
        requireNonNullPosInteger(idE, "idE");
        requireNonNullPosInteger(idU, "idU");

        Map<Integer, Map<Integer, Resposta>> byEU = submapEUOrCreate(idE);
        if (byEU.containsKey(idU)) {
            throw new IllegalStateException("Ja existeix Respostes per (" + idE + "," + idU + ")");
        }
        byEU.put(idU, new HashMap<>()); // submap buit per a idP->Resposta

        // Retornem un objecte Respostes "projecció" buit (si vols), o el pots construir a getRespostes
        return new Respostes(idE, idU, this);
    }

    public ArrayList<Resposta> getLlistaRespostes(Integer idE, Integer idU) {
        requireNonNullPosInteger(idE, "idE");
        requireNonNullPosInteger(idU, "idU");

        this.getUsuari(idU); // valida que l’usuari existeix
        this.getEnquesta(idE); // valida que l’enquesta existeix

        // Recuperar respostes
        Map<Integer, Map<Integer, Resposta>> perUsuari = respostes.get(idE);
        if (perUsuari == null) return new ArrayList<>();

        Map<Integer, Resposta> perPregunta = perUsuari.get(idU);
        if (perPregunta == null) return new ArrayList<>();

        return new ArrayList<>(perPregunta.values());
    }
    public Respostes getRespostes(Integer idE, Integer idU) {
        ArrayList<Resposta> llista = getLlistaRespostes(idE, idU);
        return new Respostes(idE, idU, llista, this);
    }


    // Importació d'enquestes
    public Integer importarEnquestaDesdeCSV(String filePath, String titolEnquesta, Integer idCreador) {
        if (filePath == null || filePath.isBlank()) throw new IllegalArgumentException("Error: la ruta del fitxer no pot ser buida.");
        if (titolEnquesta == null || titolEnquesta.isBlank()) throw new IllegalArgumentException("Error: el títol de l'enquesta no pot ser buit.");
        if (idCreador == null || !existeixUsuari(idCreador)) throw new IllegalArgumentException("Error: l'ID del creador no és vàlid.");

        // 1) Leer CSV como dadesCSV desde persistencia
        ArrayList<ArrayList<String>> dadesCSV = ctrlPersistencia.importarEnquestaDesdeCSV(filePath);
        if (dadesCSV == null || dadesCSV.isEmpty()) throw new IllegalArgumentException("CSV buit");

        // 2) Cabecera
        ArrayList<String> header = dadesCSV.get(0);
        if (header.size() < 2) throw new IllegalArgumentException("CSV sense preguntes (calen >=2 columnes: IDUsuari + preguntes)");

        // 3) Crear encuesta
        Enquesta encuesta = creaEnquesta(titolEnquesta, idCreador);
        Integer idE = encuesta.getIdE();

        // 4) Crear preguntas: map columna -> idPregunta
        Map<Integer, Integer> colToIdPregunta = new HashMap<>();
        for (int col = 1; col < header.size(); col++) {
            String enunciado = header.get(col) == null ? "" : header.get(col).trim();
            if (enunciado.isEmpty()) continue;

            String tipo = determinarTipoPregunta(dadesCSV, col);
            Integer idP = crearPreguntaPorTipo(encuesta, tipo, enunciado, col); // con col para tu firma actual
            colToIdPregunta.put(col, idP);
        }

        // 5) Procesar respuestas (una sola vez)
        procesarRespuestas(dadesCSV, idE, colToIdPregunta);

        return idE;
    }

    private void procesarRespuestas(ArrayList<ArrayList<String>> dadesCSV, Integer idEncuesta, Map<Integer, Integer> colToIdPregunta) {

        if (dadesCSV == null || dadesCSV.size() < 2) return; // sin datos
        if (idEncuesta == null) throw new IllegalArgumentException("idEncuesta null");
        if (colToIdPregunta == null || colToIdPregunta.isEmpty()) return;

        // filas 1..n son respuestas
        for (int row = 1; row < dadesCSV.size(); row++) {
            ArrayList<String> fila = dadesCSV.get(row);
            if (fila == null || fila.isEmpty()) continue;

            // Columna 0: identificador de usuario
            String idUsuarioCSV = fila.get(0) != null ? fila.get(0).trim() : "";
            if (idUsuarioCSV.isEmpty()) continue;

            Integer idUsuario = obtenerOCrearUsuario(idUsuarioCSV);

            // crea contenedor de respuestas para ese usuario (si aplica en vuestro dominio)
            creaRespostes(idEncuesta, idUsuario);

            // Para cada pregunta creada (columna->idPregunta)
            for (Map.Entry<Integer, Integer> e : colToIdPregunta.entrySet()) {
                int col = e.getKey();
                int idPregunta = e.getValue();

                if (col >= fila.size()) continue;

                String raw = fila.get(col);
                raw = (raw == null) ? "" : raw.trim();
                if (raw.isEmpty()) continue;

                List<String> contenidoRespuesta = procesarContenidoRespuesta(raw);

                // OJO: si AfegeixResposta espera otra firma, ajusta aquí
                AfegeixResposta(idEncuesta, idUsuario, idPregunta, contenidoRespuesta);
            }
        }
    }


    private String determinarTipoPregunta(ArrayList<ArrayList<String>> dadesCSV, int columna) {
        // recoge valores no vacíos (muestreo limitado para no ir lento)
        Set<String> valores = new HashSet<>();
        int maxRows = Math.min(dadesCSV.size(), 1 + 50); // 50 filas de datos

        for (int row = 1; row < maxRows; row++) { // row=1: saltar cabecera
            ArrayList<String> fila = dadesCSV.get(row);
            if (fila != null && fila.size() > columna) {
                String v = fila.get(columna);
                if (v != null) {
                    v = v.trim();
                    if (!v.isEmpty()) valores.add(v);
                }
            }
        }

        if (valores.isEmpty()) return "text";
        if (esNumerico(valores)) return "integer";
        if (esBooleano(valores)) return "nominal_unica";

        // heurística nominal/ordinal/text similar a la que solíais usar
        int n = valores.size();
        if (n <= 5) return "nominal_unica";
        if (n <= 10) return "ordinal";
        return "text";
    }

    private boolean esNumerico(Set<String> valores) {
        for (String v : valores) {
            try {
                Integer.parseInt(v.trim());
            } catch (NumberFormatException e) {
                return false;
            }
        }
        return true;
    }

    private boolean esBooleano(Set<String> valores) {
        Set<String> lower = valores.stream().map(s -> s.trim().toLowerCase()).collect(Collectors.toSet());

        // Ajusta aquí si vuestro CSV usa otros tokens
        Set<String> allowed = new HashSet<>(Arrays.asList("true", "false", "yes", "no", "si", "sí", "0", "1"));

        return allowed.containsAll(lower);
    }


    private Integer crearPreguntaPorTipo(Enquesta encuesta, String tipo, String enunciado, int columna) {
        Object[] params = null;

        switch (tipo.toLowerCase()) {
            case "integer":
                params = new Object[]{0, 100}; // Rango por defecto
                break;
            case "ordinal":
                Vector<String> opcionesOrdinal = new Vector<>(Arrays.asList("Opción 1", "Opción 2", "Opción 3"));
                params = new Object[]{opcionesOrdinal};
                break;
            case "nominal_unica":
                Set<String> opcionesNominal = new HashSet<>(Arrays.asList("Sí", "No"));
                params = new Object[]{opcionesNominal};
                break;
            case "nominal_multiple":
                Set<String> opcionesMultiple = new HashSet<>(Arrays.asList("Opción A", "Opción B", "Opción C"));
                params = new Object[]{opcionesMultiple, 2}; // qMax = 2
                break;
            case "text":
            default:
                // No necesita parámetros
                break;
        }

        return encuesta.creaPregunta(tipo, enunciado, params, null);
    }

    private List<String> procesarContenidoRespuesta(String contenido) {
        if (contenido == null) return Collections.emptyList();
        contenido = contenido.trim();
        if (contenido.isEmpty()) return Collections.emptyList();

        // Si venía como "a; b; c" o similar, adaptad separador
        // Yo dejo split por ';' como suele hacerse en múltiples respuestas.
        if (contenido.contains(";")) {
            String[] parts = contenido.split(";");
            List<String> out = new ArrayList<>();
            for (String p : parts) {
                String t = p.trim();
                if (!t.isEmpty()) out.add(t);
            }
            return out;
        }
        return Collections.singletonList(contenido);
    }

    private Integer obtenerOCrearUsuario(String identificador) {
        //Primero pasamos el id de string a Integer
        Integer idUsuario = Integer.parseInt(identificador);

        // Verificar si el usuario existe
        try {
            getUsuari(idUsuario);
            System.out.println("Usuario existente encontrado: ID " + idUsuario);
            return idUsuario;
        } catch (Exception e) {
            // Usuario no existe, lo creamos
            String nombreUsuario = "Usuario_" + identificador;//Usuari_11 per exemple
            String emailUsuario = "user_" + identificador + "@encuesta.com";//Serà unic i amb @
            Usuari nouUsuari = creaUsuari(nombreUsuario, emailUsuario);
            System.out.println("Nuevo usuario creado: ID " + nouUsuari.getId() + " - " + nombreUsuario);
            return nouUsuari.getId();
        }
    }

    public void EliminarRespostes(Integer idE, Integer idU) {
        requireNonNullPosInteger(idE, "idE");
        requireNonNullPosInteger(idU, "idU");

        Map<Integer, Map<Integer, Resposta>> byEU = submapEUOrNull(idE);
        if (byEU == null || !byEU.containsKey(idU)) {
            throw new NoSuchElementException("No existeixen Respostes per (" + idE + "," + idU + ")");
        }
        byEU.remove(idU);
        if (byEU.isEmpty()) respostes.remove(idE);
    }

    public Resposta AfegeixResposta(Integer idE, Integer idU, Integer idP,List<String> contingut) {
        requireNonNullPosInteger(idE, "idE");
        requireNonNullPosInteger(idU, "idU");
        requireNonNullPosInteger(idP, "idP");
        Map<Integer, Map<Integer, Resposta>> byEU = submapEUOrNull(idE);
        if (byEU == null || !byEU.containsKey(idU)) {
            throw new NoSuchElementException("Abans cal crear Respostes per (" + idE + "," + idU + ")");
        }
        Map<Integer, Resposta> byP = byEU.get(idU);
        if (byP.containsKey(idP)) {
            throw new IllegalStateException("Ja existeix Resposta per ("+idE+","+idU+","+idP+")");
        }
        Resposta r = new Resposta(idP, new ArrayList<>(contingut),this);
        byP.put(idP, r);
        return r;
    }

    public Resposta GetResposta(Integer idE, Integer idU, Integer idP) {
        requireNonNullPosInteger(idE, "idE");
        requireNonNullPosInteger(idU, "idU");
        requireNonNullPosInteger(idP, "idP");
        Map<Integer, Map<Integer, Resposta>> byEU = submapEUOrNull(idE);
        if (byEU == null) throw new NoSuchElementException("No hi ha respostes per idE=" + idE);
        Map<Integer, Resposta> byP = byEU.get(idU);
        if (byP == null) throw new NoSuchElementException("No hi ha respostes per (" + idE + "," + idU + ")");
        Resposta r = byP.get(idP);
        if (r == null) throw new NoSuchElementException("No existeix Resposta per ("+idE+","+idU+","+idP+")");
        return r;
    }

    public void EliminarResposta(Integer idE, Integer idU, Integer idP) {
        requireNonNullPosInteger(idE, "idE");
        requireNonNullPosInteger(idU, "idU");
        requireNonNullPosInteger(idP, "idP");
        Map<Integer, Map<Integer, Resposta>> byEU = submapEUOrNull(idE);
        if (byEU == null) throw new NoSuchElementException("No hi ha respostes per idE=" + idE);
        Map<Integer, Resposta> byP = byEU.get(idU);
        if (byP == null || !byP.containsKey(idP)) {
            throw new NoSuchElementException("No existeix Resposta per ("+idE+","+idU+","+idP+")");
        }
        byP.remove(idP);
        if (byP.isEmpty()) {
            byEU.remove(idU);
            if (byEU.isEmpty()) respostes.remove(idE);
        }
    }
    // ================== ANÀLISI / CLÚSTERING ==================

    /**
     * Executa K-means sobre totes les respostes d'una enquesta
     * i retorna un resum en format text per a la capa de presentació.
     */
    public java.util.List<Respostes> getRespostesEnquesta(int idE) {
        requireNonNullPosInteger(idE, "idE");
        Map<Integer, Map<Integer, Resposta>> byEU = submapEUOrNull(idE);
        java.util.List<Respostes> llista = new java.util.ArrayList<>();

        if (byEU == null) return llista; // sense respostes

        for (Integer idU : byEU.keySet()) {
            llista.add(getRespostes(idE, idU));  // ja tens aquest mètode
        }
        return llista;
    }
    public String executarKMeansSobreEnquesta(int idEnquesta, int k, int maxIter) {
        // 1) Respostes de l'enquesta
        List<Respostes> dades = getRespostesEnquesta(idEnquesta);
        if (dades.isEmpty()) {
            throw new IllegalStateException("No hi ha respostes per a l'enquesta " + idEnquesta);
        }

        // 2) Analisi + KMeans (façana que ja tens implementada)
        Analisi analisi = new Analisi(this);
        Analisi.ResultatAnalisi r = analisi.executaKMeans(dades, k, maxIter);

        // 3) Construir resum textual a partir de ResultatAnalisi
        StringBuilder sb = new StringBuilder();
        sb.append("Algorisme: ").append(r.getAlgorisme()).append('\n');
        sb.append("Enquesta: ").append(idEnquesta).append('\n');
        sb.append("K: ").append(r.getK()).append('\n');
        sb.append("Temps (ms): ").append(r.getMillis()).append('\n');

        if (r.getQualitatSilhouette() != null) {
            sb.append("Silhouette: ").append(r.getQualitatSilhouette()).append('\n');
        }
        if (r.getInerciaOCost() != null) {
            sb.append("Inèrcia: ").append(r.getInerciaOCost()).append('\n');
        }

        Map<Integer, List<Integer>> clusters = r.getClusters();
        for (Map.Entry<Integer, List<Integer>> e : clusters.entrySet()) {
            sb.append("Clúster ").append(e.getKey()).append(": ")
                    .append(e.getValue())  // índexs de respostes
                    .append('\n');
        }

        return sb.toString();
    }

    /**
     * Executa K-medoids sobre totes les respostes d'una enquesta
     * i retorna un resum en format text.
     */
    public String executarKMedoidsSobreEnquesta(int idEnquesta, int k, int maxIter) {
        List<Respostes> dades = getRespostesEnquesta(idEnquesta);
        if (dades.isEmpty()) {
            throw new IllegalStateException("No hi ha respostes per a l'enquesta " + idEnquesta);
        }

        Analisi analisi = new Analisi(this);
        // decideixes si vols useAllPairsSwap true o false; poso false per defecte
        Analisi.ResultatAnalisi r = analisi.executaKMedoids(dades, k, false, maxIter);

        StringBuilder sb = new StringBuilder();
        sb.append("Algorisme: ").append(r.getAlgorisme()).append('\n');
        sb.append("Enquesta: ").append(idEnquesta).append('\n');
        sb.append("K: ").append(r.getK()).append('\n');
        sb.append("Temps (ms): ").append(r.getMillis()).append('\n');

        if (r.getQualitatSilhouette() != null) {
            sb.append("Silhouette: ").append(r.getQualitatSilhouette()).append('\n');
        }
        if (r.getInerciaOCost() != null) {
            sb.append("Cost total: ").append(r.getInerciaOCost()).append('\n');
        }

        Map<Integer, List<Integer>> clusters = r.getClusters();
        for (Map.Entry<Integer, List<Integer>> e : clusters.entrySet()) {
            sb.append("Clúster ").append(e.getKey()).append(": ")
                    .append(e.getValue())
                    .append('\n');
        }

        return sb.toString();
    }

}