package domini;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

public class KMedoids extends Algorisme
{
    public static class Resultat{
        private final List<Integer> medoids;
        private final Map<Integer ,List<Integer>> clusters;
        private final double costTotal;

        public Resultat(List<Integer> medoids  ,
                        Map<Integer,List<Integer>> clusters,
                        double costTotal){

            this.medoids=Collections.unmodifiableList(new ArrayList<>(medoids));
            Map<Integer ,List<Integer>> copia = new HashMap<>();
            for (Map.Entry <Integer , List<Integer>> e : clusters.entrySet()) {
                copia.put(e.getKey() , Collections.unmodifiableList(new ArrayList<>(e.getValue())));

            }
            this.clusters = Collections.unmodifiableMap(copia);
            this.costTotal =costTotal;
        }


        public List<Integer> getMedoids() {return medoids; }
        public Map<Integer,List<Integer>>getClusters() { return clusters; }
        public double getCostTotal() {
            return costTotal; }

    }

    public KMedoids(CtrlDomini ctrlDomini, int idAlgorisme, int k, List<Respostes> respostes){
        super(ctrlDomini, idAlgorisme, k, respostes  );
    }

    public KMedoids(CtrlDomini ctrlDomini, int  idAlgorisme, int k)
    {
        super(ctrlDomini, idAlgorisme, k);

    }

    @Override
    public Object executa(int maxIter, boolean useAllPairsSwap){
        List<Respostes> respostes=getRespostes();
        int k = getK();

        if (k<= 0 || k >  respostes.size()) throw new IllegalArgumentException("Error: k fora de rang.");
        int n =respostes.size();

        double[][] D =precomputaMatriuDistancies(respostes);
        List<Integer>  medoids = inicialitzaMedoids(n, k);


        Map<Integer, Integer> assignacio = assignaTots(D, medoids);

        double cost=costTotal(D, medoids, assignacio);

        boolean millora=true;
        int iter=0;

        while ( millora && iter < maxIter) {
            millora= false;
            double  bestDelta = 0.0;
            int bestMedoid = -1;
            int bestNoMedoid =-1;

            HashSet<Integer>  conjuntMedoids = new HashSet<>(medoids);

            for (int m : new ArrayList<>(medoids))
            {
                for (int h = 0; h < n; ++h) {

                    if (conjuntMedoids.contains(h)) continue;
                    double delta =deltaSwap(D, medoids, assignacio, m, h);
                    if (delta< bestDelta) {
                        bestDelta= delta;
                        bestMedoid=m;
                        bestNoMedoid= h;
                        if (!useAllPairsSwap) break;

                    }
                }
                if  (!useAllPairsSwap && bestMedoid != -1) break;
              }

            if(bestMedoid !=-1){
                medoids.remove(Integer.valueOf(bestMedoid));
                medoids.add(bestNoMedoid);

                assignacio= assignaTots(D, medoids);
                cost+= bestDelta;
                millora= true;
            }

            ++iter;
        }

        Map <Integer, List<Integer>> clusters= construeixClusters(medoids, assignacio);
        return new Resultat(medoids, clusters ,  cost);

    }

    private double[][] precomputaMatriuDistancies(List<Respostes> respostes ){
        int n = respostes.size();
        double[][] D=new double[n][n];
        for (int i =  0 ;i < n; ++i) {
            D[i][i] =  0.0;
            for (int j= i + 1; j < n; ++j) {
                double d=distanciaEntreRespostes(respostes.get(i), respostes.get(j));
                D[i][j] =d;
                D[j][i] =d;

            }
        }
        return D;
    }


    private double distanciaEntreRespostes(Respostes a, Respostes b){
        ArrayList ra=a.getRespostes();
        ArrayList rb  = b.getRespostes();
        int m =ra.size();
        if (m==0)return 0.0;

        double suma  = 0.0;
        for (int i = 0 ; i < m; ++i) {
            Resposta r1= (Resposta) ra.get(i);
            Resposta r2=(Resposta) rb.get(i);
            double d =r1.calcula_distanciaRespostes(r2);
            if (Double.isNaN(d) || Double.isInfinite(d))
            {
                throw new IllegalArgumentException("Error: distància no vàlida a la pregunta " + i + ".");

            }
            if (d<0) d = 0;
            if (d>1) d  = 1;
            suma += d;

        }
        return suma/  m;
    }

    private List<Integer>  inicialitzaMedoids(int n ,   int k) {
        List<Integer> medoids = new ArrayList<>();
        if (k == 1)

        {
            medoids.add(0);
            return medoids;

        }
        double pas= (double) (n - 1)/(double) (k - 1);
        for (int i = 0; i < k; ++i)
        {
            int idx =(int) Math.round(i * pas);
            if (!medoids.contains(idx)) medoids.add(idx);
        }
        int cur=0;
        while  (medoids.size() < k && cur < n) {
            if  (!medoids.contains(cur)) medoids.add(cur);

            ++cur;
        }
        return medoids;
    }

    private Map<Integer,  Integer> assignaTots(double[][] D, List<Integer> medoids) {
        int n=D.length;
        Map<Integer, Integer> assignacio= new HashMap<>();
        for (int i = 0;  i<n; ++i ){
            int millorMedoid= -1;
            double millorD=Double.POSITIVE_INFINITY;
            for (int m :  medoids)
            {
                double d= D[i][m];
                if (d<millorD) {
                    millorD  = d;
                    millorMedoid  = m;

                }
            }
            assignacio.put(i,millorMedoid);
        }
        return assignacio;
    }

    private double costTotal( double[][] D ,List<Integer> medoids, Map<Integer, Integer> assignacio) {
        double cost =  0.0;
        for (Map.Entry<Integer, Integer> e : assignacio.entrySet()){
            int i = e.getKey();
            int m =  e.getValue();
            cost+=D[i][m];

        }

        return cost;
    }

    private double deltaSwap(double[][] D, List<Integer> medoids,
                             Map<Integer, Integer> assignacio, int mOut, int hIn )
    {
        HashSet<Integer> M = new HashSet<>(medoids );
        Map<Integer, Double> bestDist = new HashMap<>();
        Map<Integer, Double> secondBestDist=new HashMap<>();
        Map<Integer, Integer> bestMedoid = new HashMap<>();

        for (Map.Entry<Integer, Integer> e : assignacio.entrySet())
        {
            int i =  e.getKey();
            int m=e.getValue();
            double dBest= D[i][m];
            double dSecond=Double.POSITIVE_INFINITY;

            for (int mm : M)
            {
                if (mm==m) continue;
                double di= D[i][mm];
                if (di <  dSecond) dSecond =  di;

            }
            bestDist.put(i, dBest );
            secondBestDist.put( i,dSecond);
            bestMedoid.put(i, m );

        }

        double delta=0.0;
        for (int i =  0 ;i<D.length ;++i) {
            double dBest = bestDist.get( i );
            double dSecond =secondBestDist.get(i);
            int curBest =bestMedoid.get(i);
            double dToHin= D[i][hIn];

            if (curBest!= mOut) {
                double nouCost =Math.min(dBest, dToHin);
                delta +=(nouCost-dBest);
            }
            else
            {
                double nouCost=Math.min(dToHin, dSecond);
                delta +=(nouCost - dBest);
            }
        }
        return delta;
    }

    private Map<Integer,List<Integer>> construeixClusters( List<Integer> medoids,Map<Integer,Integer> assignacio )
    {
        Map<Integer ,List<Integer>> clusters=new HashMap<>();
        for (int m : medoids) clusters.put(m, new ArrayList<>());
        for (Map.Entry<Integer  , Integer> e : assignacio.entrySet()) {
            int i = e.getKey();

            int m = e.getValue();
            clusters.get(m).add(i);
        }

        return clusters;
    }
}