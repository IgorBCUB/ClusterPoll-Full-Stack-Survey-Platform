package domini;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;

//Importa el dataset de préstecs al CtrlDomini.
public class LoanCSVImporter
{

    public static class Result{
        public final int idEnquesta;
        public final Map<String,Integer>  colToIdP;
        public final List<Integer>  users;
        public Result(int idE, Map<String, Integer> m, List<Integer> u) {
            idEnquesta = idE; colToIdP=m; users = u;
        }

    }

    private static final Set<String> NUM_COLS = new HashSet<>(Arrays.asList(
            "ApplicantIncome","CoapplicantIncome","LoanAmount","Loan_Amount_Term","Credit_History"
    ));
    private static final String USER_COL = "Loan_ID";
    private static final String LABEL_COL = "Loan_Status";

    public static Result importCSV(Path csv, CtrlDomini ctrl, int idEnquesta, boolean includeLabel) throws IOException {
        List<String> header;
        List<String[]> rows =  new ArrayList<>();

        try (BufferedReader br=Files.newBufferedReader(csv, StandardCharsets.UTF_8)) {
            String h =  br.readLine();
            if (h  == null ) throw new IOException("CSV buit: " + csv);
            header  = parse(h);
            String  line;
            while ((line = br.readLine()) != null)
            {
                if (!line.trim().isEmpty()) rows.add(parse(line).toArray(new String[0]) );
            }

         }

        Map<String,Integer> idx =  new LinkedHashMap<>();
        for (int i=0;i<header.size();i++)idx.put(header.get(i), i);
        if (!idx.containsKey(USER_COL))throw new IllegalArgumentException("Manca columna "+USER_COL);

        List<String> cols =new ArrayList<>(header);
        cols.remove(USER_COL)
        ;



        Map<String,Integer> min = new HashMap<>(), max = new HashMap<>();
        Map<String,LinkedHashSet<String>> cats = new HashMap<>();

        for (String c : cols)
        {
            if (!includeLabel && c.equals(LABEL_COL)) continue;
            if (NUM_COLS.contains(c)) { min.put(c, Integer.MAX_VALUE); max.put(c, Integer.MIN_VALUE); }
            else cats.put(c, new LinkedHashSet<>());

        }
        for (String[] r : rows){
            for (String c : cols){
                if (!includeLabel && c.equals(LABEL_COL)) continue;
                int j = idx.get(c);

                String v = norm(j<r.length ? r[j] : "");
                if (NUM_COLS.contains(c)) {
                    Integer x=toIntOrNull(v);
                    if (x!=null)
                    { min.put(c, Math.min(min.get(c),x)); max.put(c, Math.max(max.get(c),x)); }
                } else
                {
                    cats.get(c).add(v);

                }
            }
        }

        //Crea preguntes via CtrlDomini
        Map<String,Integer> colToIdP = new LinkedHashMap<>();
        for (String c : cols){
            if (!includeLabel && c.equals(LABEL_COL)) continue;
            if (NUM_COLS.contains(c))
            {
                int lo = min.getOrDefault(c, 0);
                int hi = max.getOrDefault(c, lo+1);
                if (lo==Integer.MAX_VALUE) lo = 0;
                if (hi==Integer.MIN_VALUE || hi==lo) hi = lo+1;
                colToIdP.put(c, ctrl.creaPreguntaInteger(c, lo, hi).getId());
            } else
            {
                LinkedHashSet<String> opts = cats.get(c);
                if (opts==null || opts.isEmpty()) opts=new LinkedHashSet<>(List.of("MISSING"));
                colToIdP.put(c, ctrl.creaPreguntaNominalUnica( c , opts).getId());
            }

        }

        //2a passada: crear respostes

         List<Integer> users = new ArrayList<>();
        for(String[] r : rows)
        {
            String loanId = r[idx.get(USER_COL)];
            int idU = mapUserId(loanId);
            users.add(idU);
            try {ctrl.creaRespostes(idEnquesta, idU); } catch (IllegalStateException ignore){}

            for (String c : cols){
                if (!includeLabel && c.equals(LABEL_COL)) continue;
                Integer idP = colToIdP.get(c);
                if (idP==null)  continue;

                int j = idx.get(c );
                String v = norm(j<  r.length ? r[j] : "");

                if (NUM_COLS.contains(c))
                {
                    Integer x=toIntOrNull(v);
                    if (x!=null ) ctrl.AfegeixResposta(idEnquesta, idU, idP, new ArrayList<>(List.of(Integer.toString(x))));
                } else
                {
                    ctrl.AfegeixResposta(idEnquesta, idU, idP, new ArrayList<>(List.of(v)));

                }
            }
        }
        return new Result( idEnquesta, colToIdP, users);
    }

    private static List<String > parse(String line) {
        String[] p = line.split (","  , -1);
        List<String> out = new ArrayList<>(p.length);
        for (String s : p) out.add(s.trim() );
        return out;

    }
    private static String norm(String v) {
        if ( v==null) return "MISSING";
        String t=v.trim();
        if ( t.isEmpty()||t.equalsIgnoreCase("nan")||t.equalsIgnoreCase("null")) return "MISSING";
        return t;

    }
    private static Integer toIntOrNull(String v) {
        if (v==null || v.isEmpty() || v.equalsIgnoreCase("MISSING")) return null;
        try { return (int)Math.round(Double.parseDouble(v)); }
        catch(NumberFormatException e)
        { return null; }

    }
    private static int mapUserId(String loanId) {
        String d = loanId==null? "": loanId.replaceAll("\\D+","");
        if (!d.isEmpty()) try{return Integer.parseInt(d);} catch(Exception ignore){}
        return Math.abs( Objects.toString(loanId,"").hashCode());
    }

    public static String  normalizeYN( String s) {
        if (s==null) return null ;
        String t=s.trim().toUpperCase(Locale.ROOT);
        if (t.equals("Y")||t.equals( "YES")||t.equals("1")||t.equals("APPROVED")) return "Y";
        if (t.equals("N")||t.equals("NO"  ) ||t.equals("0")||t.equals("REJECTED")) return "N";
        if (t.equals("MISSING")||t.isEmpty()) return null;
        return null;
    }

}