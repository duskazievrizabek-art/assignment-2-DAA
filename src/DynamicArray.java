import java.util.NoSuchElementException;

public class DynamicArray<T> {

    private Object[] data;
    private int size;

    public long accessCount = 0;
    public long comparisonCount = 0;
    public long movementCount = 0;

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
            movementCount++;
        }
        data = newData;
    }

    public void add(T x) {
        ensureCapacity(size + 1);
        data[size] = x;
        size++;
    }

    public void add(int index, T x) {
        if (index < 0 || index > size) {
            throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + size);
        }
        ensureCapacity(size + 1);

        for (int i = size; i > index; i--) {
            data[i] = data[i - 1];
            movementCount++;
        }
        data[index] = x;
        size++;
    }

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

    @SuppressWarnings("unchecked")
    public T get(int index) {
        if (index < 0 || index >= size) {
            throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + size);
        }
        accessCount++;
        return (T) data[index];
    }

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
