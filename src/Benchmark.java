import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Random;

public class Benchmark {

    static final int[] SIZES = {100, 1_000, 10_000, 100_000};
    static final int REPEATS = 5;
    static final long SEED = 42;

    public static void main(String[] args) throws IOException {
        String outDir = args.length > 0 ? args[0] : "results/tables";
        new java.io.File(outDir).mkdirs();

        workload1RandomAccess(outDir + "/workload1_random_access.csv");
        workload2Search(outDir + "/workload2_search.csv");
        workload3InsertRemove(outDir + "/workload3_insert_remove.csv");
        workload4Heap(outDir + "/workload4_heap.csv");

        System.out.println("All benchmarks complete. CSVs written to " + outDir);
    }

    static void workload1RandomAccess(String path) throws IOException {
        System.out.println("== Workload 1: Random Access ==");
        try (PrintWriter w = new PrintWriter(new FileWriter(path))) {
            w.println("structure,n,avg_time_ns,accesses");
            for (int n : SIZES) {
                double daTime = 0, llTime = 0;
                long daAccesses = 0, llAccesses = 0;
                for (int rep = 0; rep < REPEATS; rep++) {
                    Random gen = new Random(SEED);
                    DynamicArray<Integer> da = new DynamicArray<>();
                    LinkedList<Integer> ll = new LinkedList<>();
                    for (int i = 0; i < n; i++) {
                        int v = gen.nextInt();
                        da.add(v);
                        ll.add(v);
                    }
                    Random idxGen = new Random(SEED + 1);
                    int[] indices = new int[10_000];
                    for (int i = 0; i < indices.length; i++) indices[i] = idxGen.nextInt(n);

                    da.resetCounters();
                    long t0 = System.nanoTime();
                    for (int idx : indices) da.get(idx);
                    long t1 = System.nanoTime();
                    daTime += (t1 - t0);
                    daAccesses = da.accessCount;

                    ll.resetCounters();
                    t0 = System.nanoTime();
                    for (int idx : indices) ll.get(idx);
                    t1 = System.nanoTime();
                    llTime += (t1 - t0);
                    llAccesses = ll.accessCount;
                }
                w.println("DynamicArray," + n + "," + (daTime / REPEATS) + "," + daAccesses);
                w.println("LinkedList," + n + "," + (llTime / REPEATS) + "," + llAccesses);
                System.out.printf("n=%d  DynamicArray=%.0fns  LinkedList=%.0fns%n",
                        n, daTime / REPEATS, llTime / REPEATS);
            }
        }
    }

    static void workload2Search(String path) throws IOException {
        System.out.println("== Workload 2: Search ==");
        try (PrintWriter w = new PrintWriter(new FileWriter(path))) {
            w.println("structure,n,avg_time_ns,comparisons");
            for (int n : SIZES) {
                double daTime = 0, llTime = 0;
                long daComparisons = 0, llComparisons = 0;
                for (int rep = 0; rep < REPEATS; rep++) {
                    Random gen = new Random(SEED);
                    DynamicArray<Integer> da = new DynamicArray<>();
                    LinkedList<Integer> ll = new LinkedList<>();
                    for (int i = 0; i < n; i++) {
                        int v = gen.nextInt();
                        da.add(v);
                        ll.add(v);
                    }
                    Random valGen = new Random(SEED + 2);
                    int[] values = new int[1_000];
                    for (int i = 0; i < values.length; i++) {

                        if (valGen.nextBoolean() && n > 0) {
                            values[i] = da.get(valGen.nextInt(n));
                        } else {
                            values[i] = valGen.nextInt();
                        }
                    }

                    da.resetCounters();
                    long t0 = System.nanoTime();
                    for (int v : values) da.contains(v);
                    long t1 = System.nanoTime();
                    daTime += (t1 - t0);
                    daComparisons += da.comparisonCount;

                    ll.resetCounters();
                    t0 = System.nanoTime();
                    for (int v : values) ll.contains(v);
                    t1 = System.nanoTime();
                    llTime += (t1 - t0);
                    llComparisons += ll.comparisonCount;
                }
                w.println("DynamicArray," + n + "," + (daTime / REPEATS) + "," + (daComparisons / REPEATS));
                w.println("LinkedList," + n + "," + (llTime / REPEATS) + "," + (llComparisons / REPEATS));
                System.out.printf("n=%d  DynamicArray=%.0fns  LinkedList=%.0fns%n",
                        n, daTime / REPEATS, llTime / REPEATS);
            }
        }
    }

    static void workload3InsertRemove(String path) throws IOException {
        System.out.println("== Workload 3: Insertion / Removal ==");
        try (PrintWriter w = new PrintWriter(new FileWriter(path))) {
            w.println("structure,n,operation,position,avg_time_ns,movements");
            for (int n : SIZES) {
                runInsertRemove(w, "DynamicArray", n, true);
                runInsertRemove(w, "LinkedList", n, false);
                System.out.println("n=" + n + " done");
            }
        }
    }

    private static void runInsertRemove(PrintWriter w, String structName, int n, boolean isArray) {
        String[] positions = {"begin", "middle"};
        for (String pos : positions) {

            double insTime = 0;
            long insMoves = 0;
            for (int rep = 0; rep < REPEATS; rep++) {
                Object structure = buildStructure(isArray, n);
                long t0 = System.nanoTime();
                for (int i = 0; i < 1_000; i++) {
                    if (isArray) {
                        DynamicArray<Integer> da = (DynamicArray<Integer>) structure;
                        int idx = pos.equals("begin") ? 0 : da.size() / 2;
                        da.add(idx, i);
                    } else {
                        LinkedList<Integer> ll = (LinkedList<Integer>) structure;
                        int idx = pos.equals("begin") ? 0 : ll.size() / 2;
                        ll.add(idx, i);
                    }
                }
                long t1 = System.nanoTime();
                insTime += (t1 - t0);
                insMoves = isArray ? ((DynamicArray<Integer>) structure).movementCount
                                    : ((LinkedList<Integer>) structure).movementCount;
            }
            w.println(structName + "," + n + ",insert," + pos + "," + (insTime / REPEATS) + "," + insMoves);

            double remTime = 0;
            long remMoves = 0;
            for (int rep = 0; rep < REPEATS; rep++) {
                Object structure = buildStructure(isArray, n);
                long t0 = System.nanoTime();
                for (int i = 0; i < 1_000; i++) {
                    if (isArray) {
                        DynamicArray<Integer> da = (DynamicArray<Integer>) structure;
                        if (da.size() == 0) break;
                        int idx = pos.equals("begin") ? 0 : da.size() / 2;
                        da.remove(idx);
                    } else {
                        LinkedList<Integer> ll = (LinkedList<Integer>) structure;
                        if (ll.size() == 0) break;
                        int idx = pos.equals("begin") ? 0 : ll.size() / 2;
                        ll.remove(idx);
                    }
                }
                long t1 = System.nanoTime();
                remTime += (t1 - t0);
                remMoves = isArray ? ((DynamicArray<Integer>) structure).movementCount
                                    : ((LinkedList<Integer>) structure).movementCount;
            }
            w.println(structName + "," + n + ",remove," + pos + "," + (remTime / REPEATS) + "," + remMoves);
        }
    }

    private static Object buildStructure(boolean isArray, int n) {
        Random gen = new Random(SEED);
        if (isArray) {
            DynamicArray<Integer> da = new DynamicArray<>();
            for (int i = 0; i < n; i++) da.add(gen.nextInt());
            da.resetCounters();
            return da;
        } else {
            LinkedList<Integer> ll = new LinkedList<>();
            for (int i = 0; i < n; i++) ll.add(gen.nextInt());
            ll.resetCounters();
            return ll;
        }
    }

    static void workload4Heap(String path) throws IOException {
        System.out.println("== Workload 4: Priority Processing (Min-Heap) ==");
        try (PrintWriter w = new PrintWriter(new FileWriter(path))) {
            w.println("structure,n,phase,avg_time_ns,comparisons,order_ok");
            for (int n : SIZES) {
                double insTime = 0, extTime = 0;
                long insComparisons = 0, extComparisons = 0;
                boolean orderOk = true;
                for (int rep = 0; rep < REPEATS; rep++) {
                    Random gen = new Random(SEED);
                    MinHeap<Integer> heap = new MinHeap<>();
                    int[] values = new int[n];
                    for (int i = 0; i < n; i++) values[i] = gen.nextInt();

                    heap.resetCounters();
                    long t0 = System.nanoTime();
                    for (int v : values) heap.insert(v);
                    long t1 = System.nanoTime();
                    insTime += (t1 - t0);
                    insComparisons = heap.comparisonCount;

                    heap.resetCounters();
                    int prev = Integer.MIN_VALUE;
                    boolean localOk = true;
                    t0 = System.nanoTime();
                    for (int i = 0; i < n; i++) {
                        int m = heap.extractMin();
                        if (m < prev) localOk = false;
                        prev = m;
                    }
                    t1 = System.nanoTime();
                    extTime += (t1 - t0);
                    extComparisons = heap.comparisonCount;
                    orderOk = orderOk && localOk;
                }
                w.println("MinHeap," + n + ",insert," + (insTime / REPEATS) + "," + insComparisons + "," + orderOk);
                w.println("MinHeap," + n + ",extractMin," + (extTime / REPEATS) + "," + extComparisons + "," + orderOk);
                System.out.printf("n=%d  insert=%.0fns  extractMin=%.0fns  orderOk=%b%n",
                        n, insTime / REPEATS, extTime / REPEATS, orderOk);
            }
        }
    }
}
