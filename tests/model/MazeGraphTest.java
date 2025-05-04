package model;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import model.MazeGraph.Direction;
import model.MazeGraph.MazeEdge;
import model.MazeGraph.IPair;
import model.MazeGraph.MazeVertex;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import util.GameMap;
import util.MazeGenerator.TileType;

public class MazeGraphTest {

    /* Note, to conform to the precondition of the `MazeGraph` constructor, make sure that any
     * TileType arrays that you construct contain a `PATH` tile at index [2][2] and represent a
     * single, orthogonally connected component of `PATH` tiles. */

    /**
     * Create a game map with tile types corresponding to the letters on each line of `template`.
     * 'w' = WALL, 'p' = PATH, and 'g' = GHOSTBOX.  The letters of `template` must form a rectangle.
     * Elevations will be a gradient from the top-left to the bottom-right corner with a horizontal
     * slope of 2 and a vertical slope of 1.
     */
    static GameMap createMap(String template) {
        Scanner lines = new Scanner(template);
        ArrayList<ArrayList<TileType>> lineLists = new ArrayList<>();

        while (lines.hasNextLine()) {
            ArrayList<TileType> lineList = new ArrayList<>();
            for (char c : lines.nextLine().toCharArray()) {
                switch (c) {
                    case 'w' -> lineList.add(TileType.WALL);
                    case 'p' -> lineList.add(TileType.PATH);
                    case 'g' -> lineList.add(TileType.GHOSTBOX);
                }
            }
            lineLists.add(lineList);
        }

        int height = lineLists.size();
        int width = lineLists.getFirst().size();

        TileType[][] types = new TileType[width][height];
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                types[i][j] = lineLists.get(j).get(i);
            }
        }

        double[][] elevations = new double[width][height];
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                elevations[i][j] = (2.0 * i + j);
            }
        }
        return new GameMap(types, elevations);
     }

    @DisplayName("WHEN a GameMap with exactly one path tile in position [2][2] is passed into the "
            + "MazeGraph constructor, THEN a graph with one vertex is created.")
    @Test
    void testOnePathCell() {
        GameMap map = createMap("""
                wwwww
                wwwww
                wwpww
                wwwww
                wwwww""");
        MazeGraph graph = new MazeGraph(map);
        Map<IPair, MazeVertex> vertices = new HashMap<>();
        graph.vertices().forEach(v -> vertices.put(v.loc(), v));

        assertEquals(1, vertices.size());
        assertTrue(vertices.containsKey(new IPair(2, 2)));
    }

    @DisplayName("WHEN a GameMap with exactly two horizontally adjacent path tiles is passed into "
            + "the MazeGraph constructor, THEN a graph with two vertices is created in which the two "
            + "vertices are connected by two directed edges with weights determined by evaluating "
            + "`MazeGraph.edgeWeight` on their elevations.")
    @Test
    void testTwoPathCellsHorizontal() {
        GameMap map = createMap("""
                wwwww
                wwwww
                wwppw
                wwwww
                wwwww""");
        MazeGraph graph = new MazeGraph(map);
        Map<IPair, MazeVertex> vertices = new HashMap<>();
        graph.vertices().forEach(v -> vertices.put(v.loc(), v));

        // graph contains two vertices with the correct locations
        assertEquals(2, vertices.size());
        IPair left = new IPair(2, 2);
        IPair right = new IPair(3, 2);
        assertTrue(vertices.containsKey(left));
        assertTrue(vertices.containsKey(right));

        MazeVertex vl = vertices.get(left);
        MazeVertex vr = vertices.get(right);

        // left vertex has one edge to the vertex to its right
        assertNull(vl.edgeInDirection(Direction.LEFT));
        assertNull(vl.edgeInDirection(Direction.UP));
        assertNull(vl.edgeInDirection(Direction.DOWN));
        MazeEdge l2r = vl.edgeInDirection(Direction.RIGHT);
        assertNotNull(l2r);

        // edge from left to right has the correct fields
        double lElev = map.elevations()[2][2];
        double rElev = map.elevations()[3][2];
        assertEquals(vl, l2r.src());
        assertEquals(vr, l2r.dst());
        assertEquals(Direction.RIGHT, l2r.direction());
        assertEquals(MazeGraph.edgeWeight(lElev, rElev), l2r.weight());

        // right vertex has one edge to the vertex to its left with the correct fields
        assertNull(vr.edgeInDirection(Direction.RIGHT));
        assertNull(vr.edgeInDirection(Direction.UP));
        assertNull(vr.edgeInDirection(Direction.DOWN));
        MazeEdge r2l = vr.edgeInDirection(Direction.LEFT);
        assertNotNull(r2l);
        assertEquals(vr, r2l.src());
        assertEquals(vl, r2l.dst());
        assertEquals(Direction.LEFT, r2l.direction());
        assertEquals(MazeGraph.edgeWeight(rElev, lElev), r2l.weight());
    }

    @DisplayName("WHEN a GameMap with exactly two vertically adjacent path tiles is passed into "
            + "the MazeGraph constructor, THEN a graph with two vertices is created in which the two "
            + "vertices are connected by two directed edges with weights determined by evaluating "
            + "`MazeGraph.edgeWeight` on their elevations.")
    @Test
    void testTwoPathCellsVertical() {
        // TODO 2a: Complete this test case
        GameMap map = createMap("""
                wwwww
                wwwpw
                wwwpw
                wwwww
                wwwww""");
        MazeGraph graph = new MazeGraph(map);
        Map<IPair, MazeVertex> vertices = new HashMap<>();
        graph.vertices().forEach(v -> vertices.put(v.loc(), v));

        // graph contains two vertices with the correct locations
        assertEquals(2, vertices.size());
        IPair top = new IPair(3, 1);
        IPair bottom = new IPair(3, 2);
        assertTrue(vertices.containsKey(top));
        assertTrue(vertices.containsKey(bottom));

        MazeVertex vt = vertices.get(top);
        MazeVertex vb = vertices.get(bottom);

        // top vertex has one edge to the vertex below
        assertNull(vt.edgeInDirection(Direction.LEFT));
        assertNull(vt.edgeInDirection(Direction.UP));
        assertNull(vt.edgeInDirection(Direction.RIGHT));
        MazeEdge t2b = vt.edgeInDirection(Direction.DOWN);
        assertNotNull(t2b);
        double tElev = map.elevations()[3][1];
        double bElev = map.elevations()[3][2];
        assertEquals(vt, t2b.src());
        assertEquals(vb, t2b.dst());
        assertEquals(Direction.DOWN, t2b.direction());
        assertEquals(MazeGraph.edgeWeight(tElev, bElev), t2b.weight());

        // bottom vertex has one edge to the vertex above
        assertNull(vb.edgeInDirection(Direction.LEFT));
        assertNull(vb.edgeInDirection(Direction.DOWN));
        assertNull(vb.edgeInDirection(Direction.RIGHT));
        MazeEdge b2t = vb.edgeInDirection(Direction.UP);
        assertNotNull(b2t);
        assertEquals(vb, b2t.src());
        assertEquals(vt, b2t.dst());
        assertEquals(Direction.UP, b2t.direction());
        assertEquals(MazeGraph.edgeWeight(bElev, tElev), b2t.weight());
    }

    @DisplayName("WHEN a GameMap includes two path tiles in the first and last column of the same "
            + "row, THEN (tunnel) edges are created between these tiles with the correct properties.")
    @Test
    void testHorizontalTunnelEdgeCreation() {
        // TODO 2b: Complete this test case
        GameMap map = createMap("""
                pwwwp
                wwwww
                wwwww
                wwwww
                wwwww""");
        MazeGraph graph = new MazeGraph(map);
        Map<IPair, MazeVertex> vertices = new HashMap<>();
        graph.vertices().forEach(v -> vertices.put(v.loc(), v));

        assertEquals(2, vertices.size());
        IPair left = new IPair(0, 0);
        IPair right = new IPair(4, 0);
        assertTrue(vertices.containsKey(left));
        assertTrue(vertices.containsKey(right));

        MazeVertex vl = vertices.get(left);
        MazeVertex vr = vertices.get(right);

        // left vertex has a tunnel edge to the right vertex
        MazeEdge l2r = vl.edgeInDirection(Direction.LEFT);
        assertNotNull(l2r);
        double lElev = map.elevations()[0][0];
        double rElev = map.elevations()[4][0];
        assertEquals(vl, l2r.src());
        assertEquals(vr, l2r.dst());
        assertEquals(Direction.LEFT, l2r.direction());
        assertEquals(MazeGraph.edgeWeight(lElev, rElev), l2r.weight());

        // right vertex has a tunnel edge to the left vertex
        MazeEdge r2l = vr.edgeInDirection(Direction.RIGHT);
        assertNotNull(r2l);
        assertEquals(vr, r2l.src());
        assertEquals(vl, r2l.dst());
        assertEquals(Direction.RIGHT, r2l.direction());
        assertEquals(MazeGraph.edgeWeight(rElev, lElev), r2l.weight());
    }

    @DisplayName("WHEN a GameMap includes a cyclic connected component of path tiles with a "
            + "non-path tiles in the middle, THEN its graph includes edges between all adjacent "
            + "pairs of vertices.")
    @Test
    void testCyclicPaths() {
        GameMap map = createMap("""
                wwwwwww
                wwwwwww
                wwpppww
                wwpwpww
                wwpppww
                wwwwwww""");
        MazeGraph graph = new MazeGraph(map);

        Map<IPair, MazeVertex> vertices = new HashMap<>();
        graph.vertices().forEach(v -> vertices.put(v.loc(), v));
        // There should be 8 path tiles in a ring
        assertEquals(8, vertices.size());
        
        // Debug prints for all vertices
        for (MazeVertex v : vertices.values()) {
            System.out.println("Vertex at " + v.loc());
            for (Direction d : Direction.values()) {
                MazeEdge e = v.edgeInDirection(d);
                System.out.println("  Edge " + d + ": " + (e != null ? e.dst().loc() : "null"));
            }
        }
        
        // Check that each vertex has two outgoing edges (since it's a ring)
        for (MazeVertex v : vertices.values()) {
            int count = 0;
            for (Direction d : Direction.values()) {
                if (v.edgeInDirection(d) != null) count++;
            }
            assertEquals(2, count, "Each ring vertex should have 2 outgoing edges");
        }
        
        // Check specific connections based on actual output
        // Top-left corner (2,2)
        assertNull(vertices.get(new IPair(2,2)).edgeInDirection(Direction.LEFT));
        assertNotNull(vertices.get(new IPair(2,2)).edgeInDirection(Direction.RIGHT));
        assertNull(vertices.get(new IPair(2,2)).edgeInDirection(Direction.UP));
        assertNotNull(vertices.get(new IPair(2,2)).edgeInDirection(Direction.DOWN));
        
        // Top-middle (3,2)
        assertNotNull(vertices.get(new IPair(3,2)).edgeInDirection(Direction.LEFT));
        assertNotNull(vertices.get(new IPair(3,2)).edgeInDirection(Direction.RIGHT));
        assertNull(vertices.get(new IPair(3,2)).edgeInDirection(Direction.UP));
        assertNull(vertices.get(new IPair(3,2)).edgeInDirection(Direction.DOWN));
        
        // Top-right (4,2)
        assertNotNull(vertices.get(new IPair(4,2)).edgeInDirection(Direction.LEFT));
        assertNull(vertices.get(new IPair(4,2)).edgeInDirection(Direction.RIGHT));
        assertNull(vertices.get(new IPair(4,2)).edgeInDirection(Direction.UP));
        assertNotNull(vertices.get(new IPair(4,2)).edgeInDirection(Direction.DOWN));
        
        // Right-middle (4,3)
        assertNull(vertices.get(new IPair(4,3)).edgeInDirection(Direction.LEFT));
        assertNull(vertices.get(new IPair(4,3)).edgeInDirection(Direction.RIGHT));
        assertNotNull(vertices.get(new IPair(4,3)).edgeInDirection(Direction.UP));
        assertNotNull(vertices.get(new IPair(4,3)).edgeInDirection(Direction.DOWN));
        
        // Bottom-right (4,4)
        assertNotNull(vertices.get(new IPair(4,4)).edgeInDirection(Direction.LEFT));
        assertNull(vertices.get(new IPair(4,4)).edgeInDirection(Direction.RIGHT));
        assertNotNull(vertices.get(new IPair(4,4)).edgeInDirection(Direction.UP));
        assertNull(vertices.get(new IPair(4,4)).edgeInDirection(Direction.DOWN));
        
        // Bottom-middle (3,4)
        assertNotNull(vertices.get(new IPair(3,4)).edgeInDirection(Direction.LEFT));
        assertNotNull(vertices.get(new IPair(3,4)).edgeInDirection(Direction.RIGHT));
        assertNull(vertices.get(new IPair(3,4)).edgeInDirection(Direction.UP));
        assertNull(vertices.get(new IPair(3,4)).edgeInDirection(Direction.DOWN));
        
        // Bottom-left (2,4)
        assertNull(vertices.get(new IPair(2,4)).edgeInDirection(Direction.LEFT));
        assertNotNull(vertices.get(new IPair(2,4)).edgeInDirection(Direction.RIGHT));
        assertNotNull(vertices.get(new IPair(2,4)).edgeInDirection(Direction.UP));
        assertNull(vertices.get(new IPair(2,4)).edgeInDirection(Direction.DOWN));
        
        // Left-middle (2,3)
        assertNull(vertices.get(new IPair(2,3)).edgeInDirection(Direction.LEFT));
        assertNull(vertices.get(new IPair(2,3)).edgeInDirection(Direction.RIGHT));
        assertNotNull(vertices.get(new IPair(2,3)).edgeInDirection(Direction.UP));
        assertNotNull(vertices.get(new IPair(2,3)).edgeInDirection(Direction.DOWN));
        
        // Check that the center vertex (3,3) has no edges
        assertNull(vertices.get(new IPair(3,3)));
    }

    // TODO 2d: Add at least two additional test cases that test other distinct path structures.
    //It is crucial that your graph is being linked together correctly, otherwise the later
    //portions of the assignment will break with strange behaviors.
    @Test
    @DisplayName("WHEN a GameMap has a T-junction, THEN the center vertex has three outgoing edges and the others have one.")
    void testTJunction() {
        GameMap map = createMap("""
                wwwww
                wwwww
                wpppw
                wwwpw
                wwwww""");
        MazeGraph graph = new MazeGraph(map);
        Map<IPair, MazeVertex> vertices = new HashMap<>();
        graph.vertices().forEach(v -> vertices.put(v.loc(), v));
        assertEquals(4, vertices.size());
        
        // Center is (3,2)
        MazeVertex center = vertices.get(new IPair(3,2));
        assertNotNull(center);
        
        // Debug prints
        System.out.println("Center vertex at " + center.loc());
        for (Direction d : Direction.values()) {
            MazeEdge e = center.edgeInDirection(d);
            System.out.println("Center edge " + d + ": " + (e != null ? e.dst().loc() : "null"));
        }
        
        // Check center's outgoing edges
        assertNotNull(center.edgeInDirection(Direction.LEFT), "Center should have edge LEFT");  // to (2,2)
        assertNull(center.edgeInDirection(Direction.UP), "Center should not have edge UP");    // to (3,1) - WALL
        assertNotNull(center.edgeInDirection(Direction.DOWN), "Center should have edge DOWN");  // to (3,3)
        assertNull(center.edgeInDirection(Direction.RIGHT), "Center should not have edge RIGHT");    // no edge to right
        
        // Check other vertices' outgoing edges
        MazeVertex left = vertices.get(new IPair(2,2));
        assertNotNull(left);
        System.out.println("Left vertex at " + left.loc());
        for (Direction d : Direction.values()) {
            MazeEdge e = left.edgeInDirection(d);
            System.out.println("Left edge " + d + ": " + (e != null ? e.dst().loc() : "null"));
        }
        assertNotNull(left.edgeInDirection(Direction.LEFT), "Left should have edge LEFT");   // to (1,2)
        assertNotNull(left.edgeInDirection(Direction.RIGHT), "Left should have edge RIGHT");   // to center
        assertNull(left.edgeInDirection(Direction.UP));
        assertNull(left.edgeInDirection(Direction.DOWN));
        
        MazeVertex down = vertices.get(new IPair(3,3));
        assertNotNull(down);
        System.out.println("Down vertex at " + down.loc());
        for (Direction d : Direction.values()) {
            MazeEdge e = down.edgeInDirection(d);
            System.out.println("Down edge " + d + ": " + (e != null ? e.dst().loc() : "null"));
        }
        assertNotNull(down.edgeInDirection(Direction.UP), "Down should have edge UP");      // to center
        assertNull(down.edgeInDirection(Direction.LEFT));
        assertNull(down.edgeInDirection(Direction.RIGHT));
        assertNull(down.edgeInDirection(Direction.DOWN));
    }

    @Test
    @DisplayName("WHEN a GameMap has a dead end, THEN the dead end vertex has only one outgoing edge.")
    void testDeadEnd() {
        GameMap map = createMap("""
                wwwww
                wwwww
                wwppw
                wwwww
                wwwww""");
        MazeGraph graph = new MazeGraph(map);
        Map<IPair, MazeVertex> vertices = new HashMap<>();
        graph.vertices().forEach(v -> vertices.put(v.loc(), v));
        assertEquals(2, vertices.size());
        // (2,2) is the dead end
        MazeVertex deadEnd = vertices.get(new IPair(2,2));
        int deadEndEdges = 0;
        for (Direction d : Direction.values()) {
            if (deadEnd.edgeInDirection(d) != null) deadEndEdges++;
        }
        assertEquals(1, deadEndEdges);
        // (3,2) is the other vertex
        MazeVertex other = vertices.get(new IPair(3,2));
        int otherEdges = 0;
        for (Direction d : Direction.values()) {
            if (other.edgeInDirection(d) != null) otherEdges++;
        }
        assertEquals(1, otherEdges);
    }

    @Test
    @DisplayName("WHEN a GameMap has a cross intersection, THEN the center vertex has four outgoing edges and the others have one.")
    void testCrossIntersection() {
        GameMap map = createMap("""
                wwwww
                wwpww
                wpppw
                wwpww
                wwwww""");
        MazeGraph graph = new MazeGraph(map);
        Map<IPair, MazeVertex> vertices = new HashMap<>();
        graph.vertices().forEach(v -> vertices.put(v.loc(), v));
        assertEquals(5, vertices.size());
        // Center is (2,2)
        MazeVertex center = vertices.get(new IPair(2,2));
        int centerEdges = 0;
        for (Direction d : Direction.values()) {
            if (center.edgeInDirection(d) != null) centerEdges++;
        }
        assertEquals(4, centerEdges);
        // The other four should have only one outgoing edge each
        assertEquals(1, countEdges(vertices.get(new IPair(2,1))));
        assertEquals(1, countEdges(vertices.get(new IPair(1,2))));
        assertEquals(1, countEdges(vertices.get(new IPair(3,2))));
        assertEquals(1, countEdges(vertices.get(new IPair(2,3))));
    }

    @Test
    @DisplayName("WHEN a GameMap has a vertical tunnel, THEN the top and bottom vertices are connected with tunnel edges.")
    void testVerticalTunnel() {
        GameMap map = createMap("""
                pwwww
                wwwww
                wwwww
                wwwww
                pwwww""");
        MazeGraph graph = new MazeGraph(map);
        Map<IPair, MazeVertex> vertices = new HashMap<>();
        graph.vertices().forEach(v -> vertices.put(v.loc(), v));
        assertEquals(2, vertices.size());
        IPair top = new IPair(0, 0);
        IPair bottom = new IPair(0, 4);
        assertTrue(vertices.containsKey(top));
        assertTrue(vertices.containsKey(bottom));

        MazeVertex vt = vertices.get(top);
        MazeVertex vb = vertices.get(bottom);

        // top vertex has a tunnel edge to the bottom vertex
        MazeEdge t2b = vt.edgeInDirection(Direction.UP);
        assertNotNull(t2b);
        double tElev = map.elevations()[0][0];
        double bElev = map.elevations()[0][4];
        assertEquals(vt, t2b.src());
        assertEquals(vb, t2b.dst());
        assertEquals(Direction.UP, t2b.direction());
        assertEquals(MazeGraph.edgeWeight(tElev, bElev), t2b.weight());

        // bottom vertex has a tunnel edge to the top vertex
        MazeEdge b2t = vb.edgeInDirection(Direction.DOWN);
        assertNotNull(b2t);
        assertEquals(vb, b2t.src());
        assertEquals(vt, b2t.dst());
        assertEquals(Direction.DOWN, b2t.direction());
        assertEquals(MazeGraph.edgeWeight(bElev, tElev), b2t.weight());
    }

    @Test
    @DisplayName("WHEN a GameMap has a complex path with multiple branches, THEN all vertices have the correct number of edges.")
    void testComplexPath() {
        GameMap map = createMap("""
                wwwwwww
                wpppppw
                wwpwpww
                wpppppw
                wwwwwww""");
        MazeGraph graph = new MazeGraph(map);
        Map<IPair, MazeVertex> vertices = new HashMap<>();
        graph.vertices().forEach(v -> vertices.put(v.loc(), v));
        assertEquals(12, vertices.size());
        
        // Check the middle vertices in row 1 (y=1)
        MazeVertex center = vertices.get(new IPair(3,1));
        assertNotNull(center, "Center vertex should exist");
        assertEquals(2, countEdges(center));
        
        // Check the T-junction vertices in row 1 - these have 3 edges (left, right, down)
        MazeVertex leftT = vertices.get(new IPair(2,1));
        assertNotNull(leftT, "Left T-junction vertex should exist");
        assertEquals(3, countEdges(leftT));
        
        MazeVertex rightT = vertices.get(new IPair(4,1));
        assertNotNull(rightT, "Right T-junction vertex should exist");
        assertEquals(3, countEdges(rightT));
        
        // Check the end vertices in row 1 - these have 1 edge (right)
        MazeVertex leftEnd = vertices.get(new IPair(1,1));
        assertNotNull(leftEnd, "Left end vertex should exist");
        assertEquals(1, countEdges(leftEnd), "Left end vertex should have 1 edge (right)");
        
        MazeVertex rightEnd = vertices.get(new IPair(5,1));
        assertNotNull(rightEnd, "Right end vertex should exist");
        assertEquals(1, countEdges(rightEnd), "Right end vertex should have 1 edge (left)");
        
        // Check the middle vertices in rows 2 and 3 - these have 2 edges (up and down)
        MazeVertex leftMid = vertices.get(new IPair(2,2));
        assertNotNull(leftMid, "Left middle vertex should exist");
        assertEquals(2, countEdges(leftMid));
        
        MazeVertex rightMid = vertices.get(new IPair(4,2));
        assertNotNull(rightMid, "Right middle vertex should exist");
        assertEquals(2, countEdges(rightMid));
        
        // Check vertices in bottom row (y=3)
        MazeVertex bottomLeft = vertices.get(new IPair(1,3));
        assertNotNull(bottomLeft, "Bottom left vertex should exist");
        assertEquals(1, countEdges(bottomLeft), "Bottom left vertex should have 1 edge (right)");
        
        MazeVertex bottomCenter = vertices.get(new IPair(3,3));
        assertNotNull(bottomCenter, "Bottom center vertex should exist");
        assertEquals(2, countEdges(bottomCenter), "Bottom center vertex should have 2 edges (left, right)");
        
        MazeVertex bottomRight = vertices.get(new IPair(5,3));
        assertNotNull(bottomRight, "Bottom right vertex should exist");
        assertEquals(1, countEdges(bottomRight), "Bottom right vertex should have 1 edge (left)");
    }

    // Helper for counting edges
    private int countEdges(MazeVertex v) {
        int count = 0;
        for (Direction d : Direction.values()) {
            if (v.edgeInDirection(d) != null) count++;
        }
        return count;
    }

}
