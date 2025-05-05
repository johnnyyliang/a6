package graph;

import graph.Pathfinding.PathEnd;
import java.util.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static graph.SimpleGraph.*;

/**
 * Uses the `SimpleGraph` class to verify the functionality of the `Pathfinding` class.
 */
public class PathfindingTest {

    // TODO 12: Complete the definition of the empty test cases so that they match their
    //  `@DisplayName` descriptions. We have provided two complete test cases as examples, as
    //   well as some helper methods for constructing graphs and checking paths that will likely
    //   be useful.

    /*
     * Text graph format ([weight] is optional):
     * Directed edge: startLabel -> endLabel [weight]
     * Undirected edge (so two directed edges in both directions): startLabel -- endLabel [weight]
     */

    // a small, strongly-connected graph consisting of three vertices and four directed edges
    public static final String graph1 = """
            A -> B 2
            A -- C 6
            B -> C 3
            """;

    @DisplayName("WHEN we compute the `pathInfo` from a vertex `v`, THEN it includes an correct "
            + "entry for each vertex `w` reachable along a non-backtracking path from `v`.")
    @Nested
    class pathInfoTest {

        // Recall that "strongly connected" describes a graph that includes a (directed) path from
        // any vertex to any other vertex
        @DisplayName("In a strongly connected graph with no `previousEdge`.")
        @Test
        void testStronglyConnectedNoPrevious() {
            SimpleGraph g = SimpleGraph.fromText(graph1);
            SimpleVertex va = g.getVertex("A");
            SimpleVertex vb = g.getVertex("B");
            SimpleVertex vc = g.getVertex("C");

            // compute paths from source vertex "A"
            Map<SimpleVertex, PathEnd<SimpleEdge>> paths = Pathfinding.pathInfo(va, null);
            assertEquals(3, paths.size()); // all vertices are reachable
            assertEquals(0, paths.get(va).distance());
            // since the shortest path A -> A is empty, we can't assert anything about its last edge
            assertEquals(2, paths.get(vb).distance());
            assertEquals(g.getEdge(va, vb), paths.get(vb).lastEdge());
            assertEquals(5, paths.get(vc).distance());
            assertEquals(g.getEdge(vb, vc), paths.get(vc).lastEdge());

            // compute paths from source vertex "B"
            paths = Pathfinding.pathInfo(vb, null);
            assertEquals(3, paths.size()); // all vertices are reachable
            assertEquals(9, paths.get(va).distance());
            assertEquals(g.getEdge(vc, va), paths.get(va).lastEdge());
            assertEquals(0, paths.get(vb).distance());
            assertEquals(3, paths.get(vc).distance());
            assertEquals(g.getEdge(vb, vc), paths.get(vc).lastEdge());

            // compute paths from source vertex "C"
            paths = Pathfinding.pathInfo(vc, null);
            assertEquals(3, paths.size()); // all vertices are reachable
            assertEquals(6, paths.get(va).distance());
            assertEquals(g.getEdge(vc, va), paths.get(va).lastEdge());
            assertEquals(8, paths.get(vb).distance());
            assertEquals(g.getEdge(va, vb), paths.get(vb).lastEdge());
            assertEquals(0, paths.get(vc).distance());
        }

        @DisplayName("In a graph that is *not* strongly connected and `pathInfo` is computed "
                + "starting from a vertex that cannot reach all other vertices.")
        @Test
        void testNotStronglyConnected() {
            SimpleGraph g = SimpleGraph.fromText("B -> A 2");
            SimpleVertex va = g.getVertex("A");
            SimpleVertex vb = g.getVertex("B");

            Map<SimpleVertex, PathEnd<SimpleEdge>> paths = Pathfinding.pathInfo(va, null);
            assertEquals(1, paths.size()); // only va is reachable
            assertTrue(paths.containsKey(va));
            assertFalse(paths.containsKey(vb));
        }

        @DisplayName("In a strongly connected graph with a `previousEdge` that prevents some vertex"
                + "from being reached.")
        @Test
        void testStronglyConnectedPreviousStillReachable() {
            // TODO 12a: Complete this test case
            SimpleGraph g = SimpleGraph.fromText(graph1);
            SimpleVertex va = g.getVertex("A");
            SimpleVertex vb = g.getVertex("B");
            SimpleVertex vc = g.getVertex("C");
            
            // Get the edge from C to A to use as previousEdge
            SimpleEdge edgeCA = g.getEdge(vc, va);
            
            // Compute paths from A with a previousEdge C->A
            // This should prevent backtracking from A to C directly
            Map<SimpleVertex, PathEnd<SimpleEdge>> paths = Pathfinding.pathInfo(va, edgeCA);
            
            // All vertices should still be reachable, but through a different path
            assertEquals(3, paths.size());
            
            // A is reachable as starting point
            assertEquals(0, paths.get(va).distance());
            
            // B is reachable directly from A
            assertEquals(2, paths.get(vb).distance());
            assertEquals(g.getEdge(va, vb), paths.get(vb).lastEdge());
            
            // C is reachable only through B now, not directly from A
            assertEquals(5, paths.get(vc).distance());
            assertEquals(g.getEdge(vb, vc), paths.get(vc).lastEdge());
        }

        @DisplayName("In a graph where the shortest path with backtracking is shorter than the "
                + "shortest non-backtracking path.")
        @Test
        void testBacktrackingShorter() {
            // TODO 12b: Complete this test case
            // Create a graph where the shortest path involves backtracking
            String graphText = """
                    A -> B 1
                    B -> C 1
                    C -> B 1
                    B -> D 10
                    """;
            SimpleGraph g = SimpleGraph.fromText(graphText);
            SimpleVertex va = g.getVertex("A");
            SimpleVertex vb = g.getVertex("B");
            SimpleVertex vc = g.getVertex("C");
            SimpleVertex vd = g.getVertex("D");
            
            // Compute paths from A with no previous edge
            Map<SimpleVertex, PathEnd<SimpleEdge>> paths = Pathfinding.pathInfo(va, null);
            
            // All vertices should be reachable
            assertEquals(4, paths.size());
            
            // The shortest path to D should be via B
            assertEquals(11, paths.get(vd).distance());
            assertEquals(g.getEdge(vb, vd), paths.get(vd).lastEdge());
            
            // Now compute paths from B with previous edge A->B
            // This prevents B->A backtracking
            SimpleEdge edgeAB = g.getEdge(va, vb);
            paths = Pathfinding.pathInfo(vb, edgeAB);
            
            // Only B, C, D should be reachable
            assertEquals(3, paths.size());
            assertTrue(paths.containsKey(vb));
            assertTrue(paths.containsKey(vc));
            assertTrue(paths.containsKey(vd));
            assertFalse(paths.containsKey(va));
        }

        @DisplayName("In a graph where some shortest path includes at least 3 edges.")
        @Test
        void testLongerPaths() {
            // TODO 12c: Complete this test case
            // Create a graph with a path of at least 3 edges
            String graphText = """
                    A -> B 1
                    B -> C 2
                    C -> D 3
                    A -> D 10
                    """;
            SimpleGraph g = SimpleGraph.fromText(graphText);
            SimpleVertex va = g.getVertex("A");
            SimpleVertex vb = g.getVertex("B");
            SimpleVertex vc = g.getVertex("C");
            SimpleVertex vd = g.getVertex("D");
            
            // Compute paths from A
            Map<SimpleVertex, PathEnd<SimpleEdge>> paths = Pathfinding.pathInfo(va, null);
            
            // All vertices should be reachable
            assertEquals(4, paths.size());
            
            // Check distances
            assertEquals(0, paths.get(va).distance());
            assertEquals(1, paths.get(vb).distance());
            assertEquals(3, paths.get(vc).distance());
            
            // D should be reached through C (path of 3 edges) since it's shorter (1+2+3=6) 
            // than the direct path (10)
            assertEquals(6, paths.get(vd).distance());
            assertEquals(g.getEdge(vc, vd), paths.get(vd).lastEdge());
            
            // Reconstruct the full path to D
            SimpleVertex current = vd;
            List<SimpleEdge> pathToD = new ArrayList<>();
            while (!current.equals(va)) {
                PathEnd<SimpleEdge> pathEnd = paths.get(current);
                SimpleEdge edge = pathEnd.lastEdge();
                pathToD.add(0, edge); // Add to front of list
                current = edge.src();
            }
            
            // Check the path has 3 edges
            assertEquals(3, pathToD.size());
            
            // Check the sequence of vertices
            assertEquals(va, pathToD.get(0).src());
            assertEquals(vb, pathToD.get(0).dst());
            assertEquals(vb, pathToD.get(1).src());
            assertEquals(vc, pathToD.get(1).dst());
            assertEquals(vc, pathToD.get(2).src());
            assertEquals(vd, pathToD.get(2).dst());
        }
    }

    // Example graph from Prof. Myers's notes
    public static final String graph2 = """
            A -> B 9
            A -> C 14
            A -> D 15
            B -> E 23
            C -> E 17
            C -> D 5
            C -> F 30
            D -> F 20
            D -> G 37
            E -> F 3
            E -> G 20
            F -> G 16""";

    /**
     * Ensures `pathEdges` is a well-formed path: the `dst` of each edge equals the `src` of the
     * subsequent edge, and that the ordered list of all vertices in the path equals
     * `expectedVertices`. Requires `path` is non-empty.
     */
    private void assertPathVertices(List<String> expectedVertices, List<SimpleEdge> pathEdges) {
        ArrayList<String> pathVertices = new ArrayList<>();
        pathVertices.add(pathEdges.getFirst().src().label);
        for (SimpleEdge e : pathEdges) {
            assertEquals(pathVertices.getLast(), e.src().label);
            pathVertices.add(e.dst().label);
        }
        assertIterableEquals(expectedVertices, pathVertices);
    }

    @DisplayName("WHEN a weighted, directed graph is given, THEN `shortestNonBacktracking` returns"
            + "the list of edges in the shortest non-backtracking path from a `src` vertex to a "
            + "`dst` vertex, or null if no such path exists.")
    @Nested
    class testShortestNonBacktrackingPath {

        @DisplayName("When the shortest non-backtracking path consists of multiple edges.")
        @Test
        void testLongPath() {
            SimpleGraph g = SimpleGraph.fromText(graph2);
            List<SimpleEdge> path = Pathfinding.shortestNonBacktrackingPath(g.getVertex("A"),
                    g.getVertex("G"), null);
            assertNotNull(path);
            assertPathVertices(Arrays.asList("A", "C", "E", "F", "G"), path);
        }

        @DisplayName("When the shortest non-backtracking path consists of a single edge.")
        @Test
        void testOneEdgePath() {
            // TODO 12d: Complete this test case
            // Create a graph with direct and indirect paths
            String graphText = """
                    A -> B 5
                    A -> C 1
                    C -> B 10
                    """;
            SimpleGraph g = SimpleGraph.fromText(graphText);
            SimpleVertex va = g.getVertex("A");
            SimpleVertex vb = g.getVertex("B");
            
            // Get the shortest path from A to B
            List<SimpleEdge> path = Pathfinding.shortestNonBacktrackingPath(va, vb, null);
            
            // Path should not be null
            assertNotNull(path);
            
            // Path should have exactly one edge
            assertEquals(1, path.size());
            
            // The edge should be A->B directly
            assertEquals(va, path.get(0).src());
            assertEquals(vb, path.get(0).dst());
            assertEquals(5, path.get(0).weight());
            
            // Verify the path uses the assertPathVertices helper
            assertPathVertices(Arrays.asList("A", "B"), path);
        }

        @DisplayName("Path is empty when `src` and `dst` are the same.")
        @Test
        void testEmptyPath() {
            // TODO 12e: Complete this test case
            SimpleGraph g = SimpleGraph.fromText(graph2);
            SimpleVertex va = g.getVertex("A");
            
            // Get the path from A to A
            List<SimpleEdge> path = Pathfinding.shortestNonBacktrackingPath(va, va, null);
            
            // Path should not be null (but empty)
            assertNotNull(path);
            
            // Path should have zero edges
            assertEquals(0, path.size());
        }

        @DisplayName("Path is null when there is not a path from `src` to `dst` (even without "
                + "accounting for back-tracking.")
        @Test
        void testNoPath() {
            SimpleGraph g = SimpleGraph.fromText("B -> A 2");
            List<SimpleEdge> path = Pathfinding.shortestNonBacktrackingPath(g.getVertex("A"),
                    g.getVertex("B"), null);
            assertNull(path);
        }

        @DisplayName("Path is null when the non-backtracking condition prevents finding a path "
                + "from `src` to `dst`.")
        @Test
        void testNonBacktrackingPreventsPath() {
            // TODO 12f: Complete this test case
            // Create a graph where the only path requires backtracking
            String graphText = """
                    A -> B 1
                    B -> C 1
                    C -> A 1
                    """;
            SimpleGraph g = SimpleGraph.fromText(graphText);
            SimpleVertex va = g.getVertex("A");
            SimpleVertex vb = g.getVertex("B");
            SimpleVertex vc = g.getVertex("C");
            
            // Get an edge to use as previous edge to enforce non-backtracking
            SimpleEdge edgeCA = g.getEdge(vc, va);
            
            // Try to find a path from A to C with previous edge being C->A
            // This should prevent any path that requires backtracking
            List<SimpleEdge> path = Pathfinding.shortestNonBacktrackingPath(va, vc, edgeCA);
            
            // Path should be available through B
            assertNotNull(path);
            assertEquals(2, path.size());
            
            // Now create a graph where backtracking is required for any path
            graphText = """
                    A -> B 1
                    B -> A 1
                    """;
            g = SimpleGraph.fromText(graphText);
            va = g.getVertex("A");
            vb = g.getVertex("B");
            
            // Get edge B->A to use as previous edge
            SimpleEdge edgeBA = g.getEdge(vb, va);
            
            // Try to find a path from A to B with previous edge being B->A
            // This should prevent the only valid path due to non-backtracking
            path = Pathfinding.shortestNonBacktrackingPath(va, vb, edgeBA);
            
            // Path should be null since non-backtracking prevents finding a path
            assertNull(path);
        }

        @DisplayName("When the graph includes multiple shortest paths from `src` to `dst`, one of "
                + "them is returned")
        @Test
        void testMultipleShortestPaths() {
            // TODO 12g: Complete this test case
            // Create a graph with multiple equally-weighted paths
            String graphText = """
                    A -> B 5
                    A -> C 5
                    B -> D 5
                    C -> D 5
                    """;
            SimpleGraph g = SimpleGraph.fromText(graphText);
            SimpleVertex va = g.getVertex("A");
            SimpleVertex vb = g.getVertex("B");
            SimpleVertex vc = g.getVertex("C");
            SimpleVertex vd = g.getVertex("D");
            
            // Get the shortest path from A to D
            List<SimpleEdge> path = Pathfinding.shortestNonBacktrackingPath(va, vd, null);
            
            // Path should not be null
            assertNotNull(path);
            
            // Path should have exactly 2 edges
            assertEquals(2, path.size());
            
            // First edge should be from A to either B or C
            assertEquals(va, path.get(0).src());
            assertTrue(path.get(0).dst().equals(vb) || path.get(0).dst().equals(vc));
            
            // Second edge should be from either B or C to D
            assertTrue(path.get(1).src().equals(vb) || path.get(1).src().equals(vc));
            assertEquals(vd, path.get(1).dst());
            
            // The path should be consistent (if A->B is first, then B->D is second)
            assertEquals(path.get(0).dst(), path.get(1).src());
            
            // Total path weight should be 10
            double totalWeight = path.get(0).weight() + path.get(1).weight();
            assertEquals(10.0, totalWeight);
        }
    }

}
