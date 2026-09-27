import java.util.NoSuchElementException;

/**
 * A generic dynamic (resizable) array implementation, similar in spirit to
 * java.util.ArrayList, but implemented from scratch for Assignment 2.
 *
 * Backing storage is a plain Object[] array that doubles in capacity
 * whenever it becomes full.
 *
 * Instrumentation counters (accessCount, comparisonCount, movementCount)
 * are exposed so that the Benchmark harness can measure the exact number
 * of low-level operations performed, independent of wall-clock time.
 */
public class DynamicArray<T> {

    private Object[] data;
    private int size;

    // ---- instrumentation ----------------------------------------------
    public long accessCount = 0;      // number of element reads (get)
    public long comparisonCount = 0;  // number of equals() comparisons (contains)
    public long movementCount = 0;    // number of element shifts/copies (add/remove/resize)

    private static final int DEFAULT_CAPACITY = 8;

    public DynamicArray() {
        data = new Object[DEFAULT_CAPACITY];
        size = 0;
    }

    public void resetCounters() {
        accessCount = 0;
        comparisonCount = 0;
        movementCount = 0;
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
        while (newCapacity < minCapacity) {
            newCapacity *= 2;
        }
        Object[] newData = new Object[newCapacity];
        for (int i = 0; i < size; i++) {
            newData[i] = data[i];
            movementCount++;   // copying an element during resize is a movement
        }
        data = newData;
    }

    /** Appends x to the end of the array. Amortized O(1). */
    public void add(T x) {
        ensureCapacity(size + 1);
        data[size] = x;
        size++;
    }

    /**
     * Inserts x at the given index, shifting subsequent elements right.
     * Worst case O(n) (index = 0), best case O(1) (index = size).
     *
     * Loop invariant (see README, Section 3) for the shifting loop:
     * before iteration k, all elements originally at positions
     * [index, size-1] with original index >= k have already been copied
     * one slot to the right.
     */
    public void add(int index, T x) {
        if (index < 0 || index > size) {
            throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + size);
        }
        ensureCapacity(size + 1);
        // Shift elements [index, size-1] one position to the right,
        // starting from the last element to avoid overwriting data.
        for (int i = size; i > index; i--) {
            data[i] = data[i - 1];
            movementCount++;
        }
        data[index] = x;
        size++;
    }

    /**
     * Removes and returns the element at the given index, shifting
     * subsequent elements left. Worst case O(n) (index = 0),
     * best case O(1) (index = size-1).
     */
    @SuppressWarnings("unchecked")
    public T remove(int index) {
        if (index < 0 || index >= size) {
            throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + size);
        }
        T removed = (T) data[index];
        for (int i = index; i < size - 1; i++) {
            data[i] = data[i + 1];
            movementCount++;
        }
        data[size - 1] = null;
        size--;
        return removed;
    }

    /** Returns the element at the given index. O(1). */
    @SuppressWarnings("unchecked")
    public T get(int index) {
        if (index < 0 || index >= size) {
            throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + size);
        }
        accessCount++;
        return (T) data[index];
    }

    /**
     * Linear search for x. Worst/average case O(n), best case O(1).
     *
     * Loop invariant (see README, Section 3): before iteration i, x is not
     * equal to any of the elements data[0..i-1].
     */
    public boolean contains(T x) {
        for (int i = 0; i < size; i++) {
            comparisonCount++;
            if (data[i] == null ? x == null : data[i].equals(x)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < size; i++) {
            sb.append(data[i]);
            if (i < size - 1) sb.append(", ");
        }
        return sb.append("]").toString();
    }
}
