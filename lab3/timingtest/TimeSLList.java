package timingtest;
import edu.princeton.cs.algs4.Stopwatch;

/**
 * Created by hug.
 */
public class TimeSLList {
    private static void printTimingTable(AList<Integer> Ns, AList<Double> times, AList<Integer> opCounts) {
        System.out.printf("%12s %12s %12s %12s\n", "N", "time (s)", "# ops", "microsec/op");
        System.out.printf("------------------------------------------------------------\n");
        for (int i = 0; i < Ns.size(); i += 1) {
            int N = Ns.get(i);
            double time = times.get(i);
            int opCount = opCounts.get(i);
            double timePerOp = time / opCount * 1e6;
            System.out.printf("%12d %12.2f %12d %12.2f\n", N, time, opCount, timePerOp);
        }
    }

    public static void main(String[] args) {
        timeGetLast();
    }

    public static void timeGetLast() {
        // TODO: YOUR CODE HERE
        int[] Ns = {1000, 2000, 4000, 8000, 16000, 32000, 64000, 128000};
        AList<Integer> opCounts = new AList<>();
        AList<Integer> N = new AList<>();
        AList<Double> times = new AList<>();
        for(int i = 0; i < Ns.length; i++){
            opCounts.addLast(10000);
            N.addLast(Ns[i]);
        }
        for(int i = 0; i < Ns.length; i++){
            SLList<Integer> temp = new SLList<Integer>();
            for(int j = 0; j < Ns[i]; j++){
                temp.addLast(j);
            }
            Stopwatch sw = new Stopwatch();
            for(int j = 0; j < 10000; j++){
                temp.getLast();
            }
            times.addLast(sw.elapsedTime());
        }
        printTimingTable(N, times, opCounts);
    }

}
