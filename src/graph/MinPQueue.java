package graph;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;

/**
 * A min priority queue of distinct elements of type `KeyType` associated with (extrinsic) double
 * priorities, implemented using a binary heap paired with a hash table.
 */
public class MinPQueue<KeyType> {

    /**
     * Pairs an element `key` with its associated priority `priority`.
     */
    private record Entry<KeyType>(KeyType key, double priority) {
        // Note: This is equivalent to declaring a static nested class with fields `key` and
        //  `priority` and a corresponding constructor and observers, overriding `equals()` and
        //  `hashCode()` to depend on the fields, and overriding `toString()` to print their values.
        // https://docs.oracle.com/en/java/javase/17/language/records.html
    }

    /**
     * ArrayList representing a binary min-heap of element-priority pairs.  Satisfies
     * `heap.get(i).priority() >= heap.get((i-1)/2).priority()` for all `i` in `[1..heap.size())`.
     */
    private final ArrayList<Entry<KeyType>> heap;

    /**
     * Associates each element in the queue with its index in `heap`.  Satisfies
     * `heap.get(index.get(e)).key().equals(e)` if `e` is an element in the queue. Only maps
     * elements that are in the queue (`index.size() == heap.size()`).
     */
    private final Map<KeyType, Integer> index;

    /**
     * Assert that all the class invariants are satisfied
     */
    private void assertInv() {
        assert heap.size() == index.size();

        for (int i = 0; i < heap.size(); i++) {
            Entry<KeyType> e = heap.get(i);
            assert index.get(e.key()) == i;
            if (i > 0) {
                int par = (i - 1) / 2;
                assert heap.get(i).priority() >= heap.get(par).priority();
            }
        }

        for (Map.Entry<KeyType, Integer> m : index.entrySet()) {
            int i = m.getValue();
            assert i >= 0 && i < heap.size();
            assert heap.get(i).key().equals(m.getKey());
        }
    }
    /**
     * Create an empty queue.
     */
    public MinPQueue() {
        index = new HashMap<>();
        heap = new ArrayList<>();
    }

    /**
     * Return whether this queue contains no elements.
     */
    public boolean isEmpty() {
        return heap.isEmpty();
    }

    /**
     * Return the number of elements contained in this queue.
     */
    public int size() {
        return heap.size();
    }

    /**
     * Return an element associated with the smallest priority in this queue.  This is the same
     * element that would be removed by a call to `remove()` (assuming no mutations in between).
     * Throws NoSuchElementException if this queue is empty.
     */
    public KeyType peek() {
        // Propagate exception from `List::getFirst()` if empty.
        return heap.getFirst().key();
    }

    /**
     * Return the minimum priority associated with an element in this queue.  Throws
     * NoSuchElementException if this queue is empty.
     */
    public double minPriority() {
        return heap.getFirst().priority();
    }

    /**
     * Swap the Entries at indices `i` and `j` in `heap`, updating `index` accordingly.  Requires
     * {@code 0 <= i,j < heap.size()}.
     */
    private void swap(int i, int j) {
        Entry<KeyType> ei = heap.get(i);
        Entry<KeyType> ej = heap.get(j);

        heap.set(i, ej);
        heap.set(j, ei);

        index.put(ej.key(), i);
        index.put(ei.key(), j);
    }

    /**
     * Restore the heap property by repeatedly swapping the element at i with its parent
     */
    private int bubbleUp(int i) {
        while (i > 0) {
            int par = (i - 1) / 2;
            if (heap.get(i).priority() < heap.get(par).priority()) {
                swap(i, par);
                i = par;
            } else {
                break;
            }
        }
        return i;
    }

    /**
     * Restore the heap property by repeatedly swapping the element at i with its smaller child
     */
    private int bubbleDown(int i) {
        int n = heap.size();
        while (true) {
            int left  = 2 * i + 1;
            if (left >= n) break;            
            int right = left + 1;

            int smallest = left;
            if (right < n && heap.get(right).priority() < heap.get(left).priority()) {
                smallest = right;
            }

            if (heap.get(i).priority() > heap.get(smallest).priority()) {
                swap(i, smallest);
                i = smallest;
            } else {
                break;
            }
        }
        return i;
    }

    /**
     * Add element `key` to this queue, associated with priority `priority`.  Requires `key` is not
     * contained in this queue.
     */
    private void add(KeyType key, double priority) {
        assert !index.containsKey(key);

        int pos = heap.size();
        heap.add(new Entry<>(key, priority));
        index.put(key, pos);

        bubbleUp(pos);
        assertInv();
    }

    /**
     * Change the priority associated with element `key` to `priority`.  Requires that `key` is
     * contained in this queue.
     */
    private void update(KeyType key, double priority) {
        assert index.containsKey(key);

        int pos = index.get(key);
        Entry<KeyType> eOld = heap.get(pos);

        if (eOld.priority() == priority) {
            return;             
        }

        heap.set(pos, new Entry<>(key, priority));

        if (priority < eOld.priority()) {
            bubbleUp(pos);
        } else {
            bubbleDown(pos);
        }

        assertInv();
    }

    /**
     * If `key` is already contained in this queue, change its associated priority to `priority`.
     * Otherwise, add it to this queue with that priority.
     */
    public void addOrUpdate(KeyType key, double priority) {
        if (!index.containsKey(key)) {
            add(key, priority);
        } else {
            update(key, priority);
        }
        assertInv();
    }

    /**
     * Remove and return the element associated with the smallest priority in this queue.  If
     * multiple elements are tied for the smallest priority, an arbitrary one will be removed.
     * Throws NoSuchElementException if this queue is empty.
     */
    public KeyType remove() {
        if (heap.isEmpty()) {
            throw new NoSuchElementException();
        }

        KeyType minKey = heap.getFirst().key();

        int last = heap.size() - 1;
        swap(0, last);
        heap.remove(last);
        index.remove(minKey);

        if (!heap.isEmpty()) {
            bubbleDown(0);
        }

        assertInv();
        return minKey;
    }
}