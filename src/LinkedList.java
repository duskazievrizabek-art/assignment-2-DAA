public class LinkedList<T> {

    private static class Node<T> {
        T value;
        Node<T> next;
        Node(T value) { this.value = value; }
    }

    private Node<T> head;
    private Node<T> tail;
    private int size;

    public long accessCount = 0;
    public long comparisonCount = 0;
    public long movementCount = 0;

    public LinkedList() {
        head = null;
        tail = null;
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

    public void add(T x) {
        Node<T> node = new Node<>(x);
        if (head == null) {
            head = node;
            tail = node;
        } else {
            tail.next = node;
            tail = node;
        }
        size++;
    }

    public void add(int index, T x) {
        if (index < 0 || index > size) {
            throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + size);
        }
        if (index == 0) {
            Node<T> node = new Node<>(x);
            node.next = head;
            head = node;
            if (tail == null) tail = node;
            size++;
            return;
        }
        if (index == size) {
            add(x);
            return;
        }
        Node<T> prev = head;
        for (int i = 0; i < index - 1; i++) {
            prev = prev.next;
            movementCount++;
        }
        Node<T> node = new Node<>(x);
        node.next = prev.next;
        prev.next = node;
        size++;
    }

    public T remove(int index) {
        if (index < 0 || index >= size) {
            throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + size);
        }
        T removed;
        if (index == 0) {
            removed = head.value;
            head = head.next;
            if (head == null) tail = null;
        } else {
            Node<T> prev = head;
            for (int i = 0; i < index - 1; i++) {
                prev = prev.next;
                movementCount++;
            }
            Node<T> target = prev.next;
            removed = target.value;
            prev.next = target.next;
            if (target == tail) tail = prev;
        }
        size--;
        return removed;
    }

    public T get(int index) {
        if (index < 0 || index >= size) {
            throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + size);
        }
        Node<T> cur = head;
        for (int i = 0; i < index; i++) {
            cur = cur.next;
            accessCount++;
        }
        accessCount++;
        return cur.value;
    }

    public boolean contains(T x) {
        Node<T> cur = head;
        while (cur != null) {
            comparisonCount++;
            if (cur.value == null ? x == null : cur.value.equals(x)) {
                return true;
            }
            cur = cur.next;
        }
        return false;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("[");
        Node<T> cur = head;
        while (cur != null) {
            sb.append(cur.value);
            if (cur.next != null) sb.append(", ");
            cur = cur.next;
        }
        return sb.append("]").toString();
    }
}
