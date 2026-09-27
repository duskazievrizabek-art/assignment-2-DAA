import java.util.ArrayList;
import java.util.PriorityQueue;
import java.util.Random;

public class Tests {

    static int passed = 0;
    static int failed = 0;

    static void check(String name, boolean condition) {
        if (condition) {
            passed++;
            System.out.println("[PASS] " + name);
        } else {
            failed++;
            System.out.println("[FAIL] " + name);
        }
    }

    public static void main(String[] args) {
        testDynamicArray();
        testLinkedList();
        testMinHeap();
        crossValidateAgainstJavaCollections();

        System.out.println();
        System.out.println("=========================================");
        System.out.println("TOTAL: " + (passed + failed) + "  PASSED: " + passed + "  FAILED: " + failed);
        System.out.println("=========================================");
        if (failed > 0) System.exit(1);
    }

    static void testDynamicArray() {
        System.out.println("\n-- DynamicArray --");
        DynamicArray<Integer> a = new DynamicArray<>();

        check("empty: size == 0", a.size() == 0);
        check("empty: isEmpty()", a.isEmpty());
        check("empty: contains(x) == false", !a.contains(5));
        boolean threw = false;
        try { a.get(0); } catch (IndexOutOfBoundsException e) { threw = true; }
        check("empty: get(0) throws", threw);

        a.add(10);
        check("one element: size == 1", a.size() == 1);
        check("one element: get(0) == 10", a.get(0) == 10);
        check("one element: contains(10)", a.contains(10));
        check("one element: !contains(99)", !a.contains(99));

        a = new DynamicArray<>();
        int[] vals = {5, 3, 3, 8, 1, 9, 3};
        for (int v : vals) a.add(v);
        check("multiple: size == 7", a.size() == 7);
        check("duplicates: contains(3)", a.contains(3));
        boolean orderOk = true;
        for (int i = 0; i < vals.length; i++) if (a.get(i) != vals[i]) orderOk = false;
        check("multiple: order preserved after add(x)", orderOk);

        a = new DynamicArray<>();
        for (int i = 0; i < 5; i++) a.add(i);
        a.add(0, -1);
        a.add(a.size(), 99);
        a.add(3, 100);
        check("add(index,x): resulting content correct",
                a.toString().equals("[-1, 0, 1, 100, 2, 3, 4, 99]"));

        a = new DynamicArray<>();
        for (int i = 0; i < 5; i++) a.add(i);
        a.remove(0);
        a.remove(a.size() - 1);
        int mid = a.remove(1);
        check("remove(index): correct element removed", mid == 2);
        check("remove(index): resulting content correct", a.toString().equals("[1, 3]"));

        threw = false;
        try { a.get(-1); } catch (IndexOutOfBoundsException e) { threw = true; }
        check("invalid index: get(-1) throws", threw);
        threw = false;
        try { a.add(-1, 0); } catch (IndexOutOfBoundsException e) { threw = true; }
        check("invalid index: add(-1,x) throws", threw);
        threw = false;
        try { a.remove(100); } catch (IndexOutOfBoundsException e) { threw = true; }
        check("invalid index: remove(100) throws", threw);

        a = new DynamicArray<>();
        for (int i = 0; i < 50_000; i++) a.add(i);
        check("large input: size == 50000", a.size() == 50000);
        check("large input: get(49999) == 49999", a.get(49999) == 49999);
        check("large input: contains(25000)", a.contains(25000));
    }

    static void testLinkedList() {
        System.out.println("\n-- LinkedList --");
        LinkedList<Integer> l = new LinkedList<>();

        check("empty: size == 0", l.size() == 0);
        check("empty: isEmpty()", l.isEmpty());
        check("empty: !contains(x)", !l.contains(5));
        boolean threw = false;
        try { l.get(0); } catch (IndexOutOfBoundsException e) { threw = true; }
        check("empty: get(0) throws", threw);

        l.add(10);
        check("one element: size == 1", l.size() == 1);
        check("one element: get(0) == 10", l.get(0) == 10);

        l = new LinkedList<>();
        int[] vals = {5, 3, 3, 8, 1, 9, 3};
        for (int v : vals) l.add(v);
        check("multiple: size == 7", l.size() == 7);
        check("duplicates: contains(3)", l.contains(3));
        boolean orderOk = true;
        for (int i = 0; i < vals.length; i++) if (l.get(i) != vals[i]) orderOk = false;
        check("multiple: order preserved after add(x)", orderOk);

        l = new LinkedList<>();
        for (int i = 0; i < 5; i++) l.add(i);
        l.add(0, -1);
        l.add(l.size(), 99);
        l.add(3, 100);
        check("add(index,x): resulting content correct",
                l.toString().equals("[-1, 0, 1, 100, 2, 3, 4, 99]"));

        l = new LinkedList<>();
        for (int i = 0; i < 5; i++) l.add(i);
        l.remove(0);
        l.remove(l.size() - 1);
        int mid = l.remove(1);
        check("remove(index): correct element removed", mid == 2);
        check("remove(index): resulting content correct", l.toString().equals("[1, 3]"));

        threw = false;
        try { l.get(-1); } catch (IndexOutOfBoundsException e) { threw = true; }
        check("invalid index: get(-1) throws", threw);
        threw = false;
        try { l.remove(100); } catch (IndexOutOfBoundsException e) { threw = true; }
        check("invalid index: remove(100) throws", threw);

        l = new LinkedList<>();
        for (int i = 0; i < 50_000; i++) l.add(i);
        check("large input: size == 50000", l.size() == 50000);
        check("large input: get(49999) == 49999", l.get(49999) == 49999);
        check("large input: contains(25000)", l.contains(25000));
    }

    static void testMinHeap() {
        System.out.println("\n-- MinHeap --");
        MinHeap<Integer> h = new MinHeap<>();

        check("empty: size == 0", h.size() == 0);
        boolean threw = false;
        try { h.peekMin(); } catch (java.util.NoSuchElementException e) { threw = true; }
        check("empty: peekMin() throws", threw);
        threw = false;
        try { h.extractMin(); } catch (java.util.NoSuchElementException e) { threw = true; }
        check("empty: extractMin() throws", threw);

        h.insert(5);
        check("one element: peekMin == 5", h.peekMin() == 5);
        check("one element: heap valid", h.isValidHeap());
        check("one element: extractMin == 5", h.extractMin() == 5);
        check("after extract: empty again", h.isEmpty());

        h = new MinHeap<>();
        int[] vals = {5, 3, 8, 1, 9, 3, 3, -4, 100, 42};
        for (int v : vals) {
            h.insert(v);
            check("heap property holds after insert(" + v + ")", h.isValidHeap());
        }

        int prev = Integer.MIN_VALUE;
        boolean nonDecreasing = true;
        int countExtracted = 0;
        while (!h.isEmpty()) {
            int m = h.extractMin();
            if (m < prev) nonDecreasing = false;
            prev = m;
            countExtracted++;
            if (!h.isEmpty()) check("heap property holds after extractMin (remaining=" + h.size() + ")", h.isValidHeap());
        }
        check("extractMin returns non-decreasing sequence", nonDecreasing);
        check("extracted count matches inserted count", countExtracted == vals.length);

        h = new MinHeap<>();
        Random rnd = new Random(42);
        for (int i = 0; i < 50_000; i++) h.insert(rnd.nextInt());
        check("large input: heap valid", h.isValidHeap());
        int last = Integer.MIN_VALUE;
        boolean ok = true;
        int cnt = 0;
        while (!h.isEmpty()) {
            int m = h.extractMin();
            if (m < last) ok = false;
            last = m;
            cnt++;
        }
        check("large input: extracted in non-decreasing order", ok);
        check("large input: count == 50000", cnt == 50_000);
    }

    static void crossValidateAgainstJavaCollections() {
        System.out.println("\n-- Cross-validation against java.util collections --");
        Random rnd = new Random(42);

        DynamicArray<Integer> da = new DynamicArray<>();
        ArrayList<Integer> ref = new ArrayList<>();
        boolean arrayOk = true;
        for (int op = 0; op < 20_000; op++) {
            int choice = rnd.nextInt(4);
            if (choice == 0 || ref.isEmpty()) {
                int v = rnd.nextInt(1000);
                da.add(v); ref.add(v);
            } else if (choice == 1) {
                int idx = rnd.nextInt(ref.size() + 1);
                int v = rnd.nextInt(1000);
                da.add(idx, v); ref.add(idx, v);
            } else if (choice == 2) {
                int idx = rnd.nextInt(ref.size());
                Integer r1 = da.remove(idx);
                Integer r2 = ref.remove(idx);
                if (!r1.equals(r2)) arrayOk = false;
            } else {
                int idx = rnd.nextInt(ref.size());
                if (!da.get(idx).equals(ref.get(idx))) arrayOk = false;
            }
        }
        check("DynamicArray matches ArrayList over 20000 random ops", arrayOk && da.size() == ref.size());

        LinkedList<Integer> ll = new LinkedList<>();
        ref = new ArrayList<>();
        boolean listOk = true;
        rnd = new Random(42);
        for (int op = 0; op < 20_000; op++) {
            int choice = rnd.nextInt(4);
            if (choice == 0 || ref.isEmpty()) {
                int v = rnd.nextInt(1000);
                ll.add(v); ref.add(v);
            } else if (choice == 1) {
                int idx = rnd.nextInt(ref.size() + 1);
                int v = rnd.nextInt(1000);
                ll.add(idx, v); ref.add(idx, v);
            } else if (choice == 2) {
                int idx = rnd.nextInt(ref.size());
                Integer r1 = ll.remove(idx);
                Integer r2 = ref.remove(idx);
                if (!r1.equals(r2)) listOk = false;
            } else {
                int idx = rnd.nextInt(ref.size());
                if (!ll.get(idx).equals(ref.get(idx))) listOk = false;
            }
        }
        check("LinkedList matches ArrayList over 20000 random ops", listOk && ll.size() == ref.size());

        MinHeap<Integer> mh = new MinHeap<>();
        PriorityQueue<Integer> pq = new PriorityQueue<>();
        rnd = new Random(42);
        boolean heapOk = true;
        for (int i = 0; i < 20_000; i++) {
            int v = rnd.nextInt();
            mh.insert(v);
            pq.add(v);
        }
        while (!pq.isEmpty()) {
            int a = mh.extractMin();
            int b = pq.poll();
            if (a != b) heapOk = false;
        }
        check("MinHeap matches PriorityQueue extraction order over 20000 elements", heapOk);
    }
}
