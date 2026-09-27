import java.util.NoSuchElementException;

/**
 * A generic binary Min-Heap backed by a dynamic array, implemented from
 * scratch for Assignment 2.
 *
 * The heap property maintained: for every node i (i > 0),
 * heap[parent(i)] <= heap[i].
 */
public class MinHeap<T extends Comparable<T>> {

    private Object[] data;
    private int size;

    // ---- instrumentation ----------------------------------------------
    public long comparisonCount = 0; // number of element comparisons (siftUp/siftDown)

    private static final int DEFAULT_CAPACITY = 8;

    public MinHeap() {
        data = new Object[DEFAULT_CAPACITY];
        size = 0;
    }

    public void resetCounters() {
        comparisonCount = 0;
    }

    public int size() {
        return size;
    }

    public boolean isEmpty() {
        return size == 0;
    }

    private void ensureCapacity(int minCapacity) {
        if (minCapacity <= data.length) return;
        int newCapacity = data.length;
        while (newCapacity < minCapacity) newCapacity *= 2;
        Object[] newData = new Object[newCapacity];
        System.arraycopy(data, 0, newData, 0, size);
        data = newData;
    }

    private int parent(int i) { return (i - 1) / 2; }
    private int left(int i) { return 2 * i + 1; }
    private int right(int i) { return 2 * i + 2; }

    @SuppressWarnings("unchecked")
    private T at(int i) { return (T) data[i]; }

    private void swap(int i, int j) {
        Object tmp = data[i];
        data[i] = data[j];
        data[j] = tmp;
    }

    /**
     * Inserts x into the heap and restores the heap property by
     * "sifting up". O(log n) worst/average case, O(1) best case
     * (new element already >= its parent, e.g. inserted into an
     * otherwise-sorted-ascending heap chain).
     *
     * Loop invariant (see README, Section 3) for the sift-up loop:
     * before each iteration, the subtree rooted at every index except
     * possibly the path from `i` up to the root satisfies the min-heap
     * property, and `i` is the only node that may violate the property
     * with respect to its parent.
     */
    public void insert(T x) {
        ensureCapacity(size + 1);
        data[size] = x;
        int i = size;
        size++;
        siftUp(i);
    }

    private void siftUp(int i) {
        while (i > 0) {
            int p = parent(i);
            comparisonCount++;
            if (at(i).compareTo(at(p)) < 0) {
                swap(i, p);
                i = p;
            } else {
                break;
            }
        }
    }

    /** Returns (without removing) the minimum element. O(1). */
    @SuppressWarnings("unchecked")
    public T peekMin() {
        if (size == 0) throw new NoSuchElementException("Heap is empty");
        return (T) data[0];
    }

    /**
     * Removes and returns the minimum element (the root), then restores
     * the heap property by moving the last element to the root and
     * "sifting down". O(log n) worst/average case, O(1) best case
     * (heap has 0 or 1 elements, or the moved root is already the
     * smallest among its children).
     *
     * Loop invariant (see README, Section 3) for the sift-down loop:
     * before each iteration, both children subtrees of `i` (if they
     * exist) already satisfy the min-heap property; `i` is the only
     * node in the whole heap that may currently violate the property
     * relative to its children.
     */
    @SuppressWarnings("unchecked")
    public T extractMin() {
        if (size == 0) throw new NoSuchElementException("Heap is empty");
        T min = (T) data[0];
        size--;
        data[0] = data[size];
        data[size] = null;
        if (size > 0) siftDown(0);
        return min;
    }

    private void siftDown(int i) {
        while (true) {
            int l = left(i);
            int r = right(i);
            int smallest = i;
            if (l < size) {
                comparisonCount++;
                if (at(l).compareTo(at(smallest)) < 0) smallest = l;
            }
            if (r < size) {
                comparisonCount++;
                if (at(r).compareTo(at(smallest)) < 0) smallest = r;
            }
            if (smallest != i) {
                swap(i, smallest);
                i = smallest;
            } else {
                break;
            }
        }
    }

    /** Validates the heap property recursively; used only by Tests.java. */
    public boolean isValidHeap() {
        return isValidHeap(0);
    }

    private boolean isValidHeap(int i) {
        int l = left(i), r = right(i);
        if (l < size) {
            if (at(l).compareTo(at(i)) < 0) return false;
            if (!isValidHeap(l)) return false;
        }
        if (r < size) {
            if (at(r).compareTo(at(i)) < 0) return false;
            if (!isValidHeap(r)) return false;
        }
        return true;
    }
}
