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
            
            SimpleEdge edgeCA = g.getEdge(vc, va);
            Map<SimpleVertex, PathEnd<SimpleEdge>> paths = Pathfinding.pathInfo(va, edgeCA);
            
            assertEquals(3, paths.size());
            assertEquals(0, paths.get(va).distance());
            
            assertEquals(2, paths.get(vb).distance());
            assertEquals(g.getEdge(va, vb), paths.get(vb).lastEdge());
            
            assertEquals(5, paths.get(vc).distance());
            assertEquals(g.getEdge(vb, vc), paths.get(vc).lastEdge());
        }

        @DisplayName("In a graph where the shortest path with backtracking is shorter than the "
                + "shortest non-backtracking path.")
        @Test
        void testBacktrackingShorter() {
            // TODO 12b: Complete this test case
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
            
            Map<SimpleVertex, PathEnd<SimpleEdge>> paths = Pathfinding.pathInfo(va, null);
            assertEquals(4, paths.size());
            
            assertEquals(11, paths.get(vd).distance());
            assertEquals(g.getEdge(vb, vd), paths.get(vd).lastEdge());

            SimpleEdge edgeAB = g.getEdge(va, vb);
            paths = Pathfinding.pathInfo(vb, edgeAB);
            
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
            
            Map<SimpleVertex, PathEnd<SimpleEdge>> paths = Pathfinding.pathInfo(va, null);
            assertEquals(4, paths.size());
            
            assertEquals(0, paths.get(va).distance());
            assertEquals(1, paths.get(vb).distance());
            assertEquals(3, paths.get(vc).distance());

            assertEquals(6, paths.get(vd).distance());
            assertEquals(g.getEdge(vc, vd), paths.get(vd).lastEdge());
            
            SimpleVertex current = vd;
            List<SimpleEdge> pathToD = new ArrayList<>();
            while (!current.equals(va)) {
                PathEnd<SimpleEdge> pathEnd = paths.get(current);
                SimpleEdge edge = pathEnd.lastEdge();
                pathToD.add(0, edge);
                current = edge.src();
            }
            
            assertEquals(3, pathToD.size());
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
            
            List<SimpleEdge> path = Pathfinding.shortestNonBacktrackingPath(va, vb, null);
            assertNotNull(path);
            assertEquals(1, path.size());
            assertEquals(va, path.get(0).src());
            assertEquals(vb, path.get(0).dst());
            assertEquals(5, path.get(0).weight());
            
            assertPathVertices(Arrays.asList("A", "B"), path);
        }

        @DisplayName("Path is empty when `src` and `dst` are the same.")
        @Test
        void testEmptyPath() {
            // TODO 12e: Complete this test case
            SimpleGraph g = SimpleGraph.fromText(graph2);
            SimpleVertex va = g.getVertex("A");
            
            List<SimpleEdge> path = Pathfinding.shortestNonBacktrackingPath(va, va, null);
            assertNotNull(path);
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
            String graphText = """
                    A -> B 1
                    B -> C 1
                    C -> A 1
                    """;
            SimpleGraph g = SimpleGraph.fromText(graphText);
            SimpleVertex va = g.getVertex("A");
            SimpleVertex vb = g.getVertex("B");
            SimpleVertex vc = g.getVertex("C");
            
            SimpleEdge edgeCA = g.getEdge(vc, va);
            List<SimpleEdge> path = Pathfinding.shortestNonBacktrackingPath(va, vc, edgeCA);
            
            assertNotNull(path);
            assertEquals(2, path.size());
            
            graphText = """
                    A -> B 1
                    B -> A 1
                    """;
            g = SimpleGraph.fromText(graphText);
            va = g.getVertex("A");
            vb = g.getVertex("B");
            
            SimpleEdge edgeBA = g.getEdge(vb, va);
            path = Pathfinding.shortestNonBacktrackingPath(va, vb, edgeBA);
            assertNull(path);
        }

        @DisplayName("When the graph includes multiple shortest paths from `src` to `dst`, one of "
                + "them is returned")
        @Test
        void testMultipleShortestPaths() {
            // TODO 12g: Complete this test case
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
            
            List<SimpleEdge> path = Pathfinding.shortestNonBacktrackingPath(va, vd, null);
            assertNotNull(path);
            assertEquals(2, path.size());
            assertEquals(va, path.get(0).src());
            assertTrue(path.get(0).dst().equals(vb) || path.get(0).dst().equals(vc));
            assertTrue(path.get(1).src().equals(vb) || path.get(1).src().equals(vc));
            assertEquals(vd, path.get(1).dst());
            assertEquals(path.get(0).dst(), path.get(1).src());

            double totalWeight = path.get(0).weight() + path.get(1).weight();
            assertEquals(10.0, totalWeight);
        }
    }

}
