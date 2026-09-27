/**
 * A generic singly linked list with a tail pointer, implemented from
 * scratch for Assignment 2 (named LinkedList, but NOT java.util.LinkedList).
 *
 * Keeping a tail pointer makes add(x) (append) O(1). All other operations
 * that require reaching an arbitrary index still require O(n) traversal
 * from the head, which is the key structural difference from DynamicArray.
 */
public class LinkedList<T> {

    private static class Node<T> {
        T value;
        Node<T> next;
        Node(T value) { this.value = value; }
    }

    private Node<T> head;
    private Node<T> tail;
    private int size;

    // ---- instrumentation ----------------------------------------------
    public long accessCount = 0;      // number of node visits during get()
    public long comparisonCount = 0;  // number of equals() comparisons (contains)
    public long movementCount = 0;    // number of pointer re-links / node traversals during add/remove

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

    /** Appends x to the end of the list. O(1) thanks to the tail pointer. */
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

    /**
     * Inserts x at the given index. Requires traversing from head to the
     * node just before `index`, so the operation is O(n) in general,
     * except for index == 0 (O(1)) and index == size (O(1) via tail).
     *
     * Loop invariant (see README, Section 3) for the traversal loop:
     * before iteration k, `prev` points to the node originally at
     * position k-1 (or is null if k == 0), i.e. exactly k nodes have
     * been traversed from head.
     */
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
            movementCount++;   // one pointer hop = one "movement" through the list
        }
        Node<T> node = new Node<>(x);
        node.next = prev.next;
        prev.next = node;
        size++;
    }

    /**
     * Removes and returns the element at the given index. O(n) in
     * general because reaching the predecessor node requires traversal
     * from head, except for index == 0 which is O(1).
     */
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

    /**
     * Returns the element at the given index. Always O(n) (worst, average
     * AND best case in the *general* sense over all indices), because an
     * arbitrary index requires linear traversal from head; there is no
     * random access. (index == 0 is a trivial O(1) special case.)
     */
    public T get(int index) {
        if (index < 0 || index >= size) {
            throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + size);
        }
        Node<T> cur = head;
        for (int i = 0; i < index; i++) {
            cur = cur.next;
            accessCount++;   // one node hop = one access
        }
        accessCount++; // counting the final node reached as an access too
        return cur.value;
    }

    /**
     * Linear search for x, O(n) worst/average case, O(1) best case.
     *
     * Loop invariant (see README, Section 3): before visiting node `cur`,
     * x has not been found in any of the nodes already visited
     * (i.e. all nodes strictly before `cur` in the list).
     */
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
