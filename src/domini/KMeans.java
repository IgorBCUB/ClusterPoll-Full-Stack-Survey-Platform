package domini;

import java.util.*;

/**
 * KMeans sobre la representació vectoritzada de TOTS els tipus de preguntes.
 * Distància: euclidiana sobre double[].
 * Inicialització: RANDOM (pots activar K-Means++ si vols).
 */
public class KMeans extends Algorisme {

    public static class Resultat {
        private final List<double[]> centroids;
        private final Map<Integer, List<Integer>> clusters;
        private final int[] assignacio;
        private final double inertia;

        public Resultat(List<double[]> centroids, Map<Integer, List<Integer>> clusters,
                        int[] assignacio, double inertia) {
            this.centroids = centroids;
            this.clusters = clusters;
            this.assignacio = assignacio;
            this.inertia = inertia;
        }
        public List<double[]> getCentroids() { return centroids; }
        public Map<Integer, List<Integer>> getClusters() { return clusters; }
        public int[] getAssignacio() { return assignacio; }
        public double getInertia() { return inertia; }
    }

    public KMeans(CtrlDomini ctrlDomini, int idAlgorisme, int k) {
        super(ctrlDomini, idAlgorisme, k);
    }

    @Override
    public Object executa(int maxIter, boolean unusedFlag) {
        List<Respostes> R = getRespostes();
        if (R == null || R.isEmpty()) throw new IllegalStateException("No hi ha respostes per executar KMeans");
        int k = getK();
        if (k <= 0 || k > R.size()) throw new IllegalArgumentException("k fora de rang");

        // 1) Vectoritza amb el vectoritzador compartit
        Vectoritzador vec = Vectoritzador.build(ctrlDomini, R);
        List<double[]> X = new ArrayList<>(R.size());
        for (Respostes r : R) {
            double[] v = new double[vec.dim()];
            vec.encode(r, v);
            X.add(v);
        }
        final int n = X.size();
        final int d = vec.dim();
        if (d <= 0) throw new IllegalStateException("Vectorització buida");

        // 2) Inicialització
        Random rng = new Random(42L);
        List<double[]> centroids = initRandom(X, rng, k);
        // List<double[]> centroids = initKMeansPlusPlus(X, rng, k);

        int[] assign = new int[n];
        Arrays.fill(assign, -1);

        // 3) Iteracions
        boolean changed = true;
        int iter = 0;
        while (changed && iter < Math.max(1, maxIter)) {
            changed = assignPoints(X, centroids, assign);
            centroids = recomputeCentroids(X, assign, k, d);
            iter++;
        }

        // 4) Inèrcia i clusters
        double inertia = 0.0;
        Map<Integer, List<Integer>> clusters = new HashMap<>();
        for (int c = 0; c < k; ++c) clusters.put(c, new ArrayList<>());
        for (int i = 0; i < n; ++i) {
            int c = assign[i];
            clusters.get(c).add(i);
            inertia += sqDist(X.get(i), centroids.get(c));
        }

        return new Resultat(centroids, clusters, assign, inertia);
    }

    // --- helpers kmeans ---

    private static List<double[]> initRandom(List<double[]> X, Random rng, int k) {
        int n = X.size(), d = X.get(0).length;
        boolean[] used = new boolean[n];
        List<double[]> cents = new ArrayList<>(k);
        while (cents.size() < k) {
            int i = rng.nextInt(n);
            if (!used[i]) {
                used[i] = true;
                cents.add(Arrays.copyOf(X.get(i), d));
            }
        }
        return cents;
    }

    private static List<double[]> initKMeansPlusPlus(List<double[]> X, Random rng, int k) {
        int n = X.size();
        int d = X.get(0).length;
        List<double[]> cents = new ArrayList<>(k);

        cents.add(Arrays.copyOf(X.get(rng.nextInt(n)), d));

        double[] minD2 = new double[n];
        Arrays.fill(minD2, Double.POSITIVE_INFINITY);

        while (cents.size() < k) {
            double[] last = cents.get(cents.size() - 1);
            for (int i = 0; i < n; ++i) {
                double d2 = sqDist(X.get(i), last);
                if (d2 < minD2[i]) minD2[i] = d2;
            }
            double sum = 0.0;
            for (double v : minD2) sum += v;

            double r = rng.nextDouble() * sum, acc = 0.0;
            int chosen = 0;
            for (int i = 0; i < n; ++i) {
                acc += minD2[i];
                if (acc >= r) { chosen = i; break; }
            }
            cents.add(Arrays.copyOf(X.get(chosen), d));
        }
        return cents;
    }

    private static boolean assignPoints(List<double[]> X, List<double[]> cents, int[] assign) {
        boolean changed = false;
        for (int i = 0; i < X.size(); ++i) {
            double best = Double.POSITIVE_INFINITY;
            int bestC = -1;
            for (int c = 0; c < cents.size(); ++c) {
                double d2 = sqDist(X.get(i), cents.get(c));
                if (d2 < best) { best = d2; bestC = c; }
            }
            if (assign[i] != bestC) {
                assign[i] = bestC;
                changed = true;
            }
        }
        return changed;
    }

    private static List<double[]> recomputeCentroids(List<double[]> X, int[] assign, int k, int d) {
        double[][] sum = new double[k][d];
        int[] cnt = new int[k];

        for (int i = 0; i < X.size(); ++i) {
            int c = assign[i];
            cnt[c]++;
            double[] xi = X.get(i);
            for (int t = 0; t < d; ++t) sum[c][t] += xi[t];
        }

        List<double[]> cents = new ArrayList<>(k);
        for (int c = 0; c < k; ++c) {
            double[] mu = new double[d];
            if (cnt[c] == 0) {
                mu = Arrays.copyOf(X.get(Math.max(0, c % X.size())), d);
            } else {
                for (int t = 0; t < d; ++t) mu[t] = sum[c][t] / cnt[c];
            }
            cents.add(mu);
        }
        return cents;
    }

    private static double sqDist(double[] a, double[] b) {
        double s = 0.0;
        for (int i = 0; i < a.length; ++i) {
            double d = a[i] - b[i];
            s += d * d;
        }
        return s;
    }
}
