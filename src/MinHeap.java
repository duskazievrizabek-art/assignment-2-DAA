import java.util.NoSuchElementException;

public class MinHeap<T extends Comparable<T>> {

    private Object[] data;
    private int size;

    public long comparisonCount = 0;

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

    @SuppressWarnings("unchecked")
    public T peekMin() {
        if (size == 0) throw new NoSuchElementException("Heap is empty");
        return (T) data[0];
    }

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
