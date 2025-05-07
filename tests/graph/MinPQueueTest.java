package graph;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Random;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class MinPQueueTest {



    @DisplayName("WHEN a new MinPQueue is created, THEN its size will be 0 AND it will be empty")
    @Test
    void testNew() {
        MinPQueue<Integer> q = new MinPQueue<>();

        assertEquals(0, q.size());
        assertTrue(q.isEmpty());
    }


    @DisplayName("GIVEN an empty MinPQueue, WHEN an element is added, THEN its size will become 1 "
            + "AND it will no longer be empty")
    @Test
    void testAddToEmpty() {
        MinPQueue<Integer> q = new MinPQueue<>();

        q.addOrUpdate(0, 0);
        assertEquals(1, q.size());
        assertFalse(q.isEmpty());
    }

    @DisplayName("GIVEN a non-empty MinPQueue, WHEN a distinct elements are added, "
            + "THEN its size will increase by 1")
    @Test
    void testAddDistinct() {
        MinPQueue<Integer> q = new MinPQueue<>();
        
        q.addOrUpdate(0, 0);
        int initialSize = q.size();
        
        q.addOrUpdate(1, 1);
        assertEquals(initialSize + 1, q.size());
    }

    @DisplayName("GIVEN a MinPQueue containing an element x whose priority is not the minimum, "
            + "WHEN x's priority is updated to become the unique minimum, "
            + "THEN the queue's size will not change "
            + "AND getting the minimum-priority element will return x "
            + "AND getting the minimum priority will return x's updated priority")
    @Test
    void testUpdateReduce() {
        MinPQueue<Integer> q = new MinPQueue<>();
        
        q.addOrUpdate(0, 2.0); 
        q.addOrUpdate(1, 1.0); 
        
        int initialSize = q.size();
        double newPriority = 0.5; 
        
        q.addOrUpdate(0, newPriority);
        
        assertEquals(initialSize, q.size());
        assertEquals(0, q.peek());
        assertEquals(newPriority, q.minPriority());
    }

    @DisplayName("GIVEN a non-empty MinPQueue, WHEN an element is removed,"
            + " THEN it size will decrease by 1.  IF its size was 1, THEN it will become empty.")
    @Test
    void testRemoveSize() {
        MinPQueue<Integer> q = new MinPQueue<>();
        
        q.addOrUpdate(0, 0);
        q.addOrUpdate(1, 1);
        int initialSize = q.size();
        q.remove();
        assertEquals(initialSize - 1, q.size());
        
        q = new MinPQueue<>();
        q.addOrUpdate(0, 0);
        q.remove();
        assertTrue(q.isEmpty());
    }

    @DisplayName("GIVEN a MinPQueue containing elements whose priorities follow their natural "
            + "ordering, WHEN elements are successively removed, THEN they will be returned in "
            + "ascending order")
    @Test
    void testRemoveElementOrder() {
        MinPQueue<Integer> q = new MinPQueue<>();
        int nElem = 20;

        // Add distinct elements in random order (priority equals element)
        {
            List<Integer> elems = new ArrayList<>();
            for (int i = 0; i < nElem; i += 1) {
                elems.add(i);
            }
            int seed = 1;
            Random rng = new Random(seed);
            Collections.shuffle(elems, rng);
            for (Integer x : elems) {
                q.addOrUpdate(x, x);
            }
        }

        // Remove elements and check order
        int prevElem = q.remove();
        for (int i = 1; i < nElem; ++i) {
            assertEquals(nElem - i, q.size());
            int nextElem = q.peek();
            int removedElem = q.remove();
            assertEquals(nextElem, removedElem);
            assertTrue(nextElem > prevElem);
            prevElem = nextElem;
        }
        assertTrue(q.isEmpty());
    }

    @DisplayName("GIVEN a MinPQueue (whose elements' priorities may have been updated), "
            + "WHEN elements are successively removed, "
            + "THEN the minimum priority will not decrease after each removal")
    @Test
    void testRemovePriorityOrder() {
        MinPQueue<Integer> q = new MinPQueue<>();
        int nUpdates = 100;

        // Add random elements with random priorities to queue and randomly update some elements'
        //  priorities.
        int seed = 1;
        Random rng = new Random(seed);
        int bound = nUpdates / 2;
        for (int i = 0; i < nUpdates; i += 1) {
            int key = rng.nextInt(bound);
            int priority = rng.nextInt(bound);
            q.addOrUpdate(key, priority);
        }

        // Remove until 1 left, but no more than nUpdates times (to prevent infinite loop in test)
        for (int i = 0; q.size() > 1 && i < nUpdates; i += 1) {
            double removedPriority = q.minPriority();
            q.remove();
            assertTrue(q.minPriority() >= removedPriority);
        }
        q.remove();
        assertTrue(q.isEmpty());
    }

    @DisplayName("GIVEN an empty MinPQueue, WHEN attempting to query the next element "
            + "OR query the minimum priority OR remove the next element "
            + "THEN a NoSuchElementException will be thrown")
    @Test
    void testExceptions() {
        MinPQueue<Integer> q = new MinPQueue<>();
        
        assertThrows(NoSuchElementException.class, () -> q.peek());
        assertThrows(NoSuchElementException.class, () -> q.minPriority());
        assertThrows(NoSuchElementException.class, () -> q.remove());
    }
}
