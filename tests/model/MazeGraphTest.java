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
        GameMap map = createMap("""
                wwwww
                wwwpw
                wwwpw
                wwwww
                wwwww""");
        MazeGraph graph = new MazeGraph(map);
        Map<IPair, MazeVertex> vertices = new HashMap<>();
        for (MazeVertex v : graph.vertices()) {
            vertices.put(v.loc(), v);
        }
        assertEquals(2, vertices.size());
        IPair top = new IPair(3, 1);
        IPair bottom = new IPair(3, 2);
        assertTrue(vertices.containsKey(top));
        assertTrue(vertices.containsKey(bottom));

        MazeVertex vt = vertices.get(top);
        MazeVertex vb = vertices.get(bottom);

        assertNull(vt.edgeInDirection(Direction.LEFT));
        assertNull(vt.edgeInDirection(Direction.UP));
        assertNull(vt.edgeInDirection(Direction.RIGHT));
        MazeEdge topToB = vt.edgeInDirection(Direction.DOWN);
        assertNotNull(topToB);

        double tElev = map.elevations()[3][1];
        double bElev = map.elevations()[3][2];
        assertEquals(vt, topToB.src());
        assertEquals(vb, topToB.dst());
        assertEquals(Direction.DOWN, topToB.direction());
        assertEquals(MazeGraph.edgeWeight(tElev, bElev), topToB.weight());

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
        GameMap map = createMap("""
                pwwwp
                wwwww
                wwwww
                wwwww
                wwwww""");
        MazeGraph graph = new MazeGraph(map);
        Map<IPair, MazeVertex> vertices = new HashMap<>();
        for (MazeVertex v : graph.vertices()) {
            vertices.put(v.loc(), v);
        }
        assertEquals(2, vertices.size());
        IPair left = new IPair(0, 0);
        IPair right = new IPair(4, 0);
        assertTrue(vertices.containsKey(left));
        assertTrue(vertices.containsKey(right));

        MazeVertex vl = vertices.get(left);
        MazeVertex vr = vertices.get(right);

        MazeEdge l2r = vl.edgeInDirection(Direction.LEFT);
        assertNotNull(l2r);
        double lElev = map.elevations()[0][0];
        double rElev = map.elevations()[4][0];
        assertEquals(vl, l2r.src());
        assertEquals(vr, l2r.dst());
        assertEquals(Direction.LEFT, l2r.direction());
        assertEquals(MazeGraph.edgeWeight(lElev, rElev), l2r.weight());

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

        assertEquals(8, vertices.size());
        
        for (MazeVertex v : vertices.values()) {
            int count = 0;
            for (Direction d : Direction.values()) {
                if (v.edgeInDirection(d) != null) count++;
            }
            assertEquals(2, count, "Each ring vertex should have 2 outgoing edges");
        }

        assertNull(vertices.get(new IPair(2,2)).edgeInDirection(Direction.LEFT));
        assertNotNull(vertices.get(new IPair(2,2)).edgeInDirection(Direction.RIGHT));
        assertNull(vertices.get(new IPair(2,2)).edgeInDirection(Direction.UP));
        assertNotNull(vertices.get(new IPair(2,2)).edgeInDirection(Direction.DOWN));

        assertNotNull(vertices.get(new IPair(3,2)).edgeInDirection(Direction.LEFT));
        assertNotNull(vertices.get(new IPair(3,2)).edgeInDirection(Direction.RIGHT));
        assertNull(vertices.get(new IPair(3,2)).edgeInDirection(Direction.UP));
        assertNull(vertices.get(new IPair(3,2)).edgeInDirection(Direction.DOWN));
        
        assertNotNull(vertices.get(new IPair(4,2)).edgeInDirection(Direction.LEFT));
        assertNull(vertices.get(new IPair(4,2)).edgeInDirection(Direction.RIGHT));
        assertNull(vertices.get(new IPair(4,2)).edgeInDirection(Direction.UP));
        assertNotNull(vertices.get(new IPair(4,2)).edgeInDirection(Direction.DOWN));
        
        assertNull(vertices.get(new IPair(4,3)).edgeInDirection(Direction.LEFT));
        assertNull(vertices.get(new IPair(4,3)).edgeInDirection(Direction.RIGHT));
        assertNotNull(vertices.get(new IPair(4,3)).edgeInDirection(Direction.UP));
        assertNotNull(vertices.get(new IPair(4,3)).edgeInDirection(Direction.DOWN));
        
        assertNotNull(vertices.get(new IPair(4,4)).edgeInDirection(Direction.LEFT));
        assertNull(vertices.get(new IPair(4,4)).edgeInDirection(Direction.RIGHT));
        assertNotNull(vertices.get(new IPair(4,4)).edgeInDirection(Direction.UP));
        assertNull(vertices.get(new IPair(4,4)).edgeInDirection(Direction.DOWN));
        
        assertNotNull(vertices.get(new IPair(3,4)).edgeInDirection(Direction.LEFT));
        assertNotNull(vertices.get(new IPair(3,4)).edgeInDirection(Direction.RIGHT));
        assertNull(vertices.get(new IPair(3,4)).edgeInDirection(Direction.UP));
        assertNull(vertices.get(new IPair(3,4)).edgeInDirection(Direction.DOWN));
        
        assertNull(vertices.get(new IPair(2,4)).edgeInDirection(Direction.LEFT));
        assertNotNull(vertices.get(new IPair(2,4)).edgeInDirection(Direction.RIGHT));
        assertNotNull(vertices.get(new IPair(2,4)).edgeInDirection(Direction.UP));
        assertNull(vertices.get(new IPair(2,4)).edgeInDirection(Direction.DOWN));
        
        assertNull(vertices.get(new IPair(2,3)).edgeInDirection(Direction.LEFT));
        assertNull(vertices.get(new IPair(2,3)).edgeInDirection(Direction.RIGHT));
        assertNotNull(vertices.get(new IPair(2,3)).edgeInDirection(Direction.UP));
        assertNotNull(vertices.get(new IPair(2,3)).edgeInDirection(Direction.DOWN));
        
        assertNull(vertices.get(new IPair(3,3)));
    }

    @Test
    @DisplayName("Given a T intersection, the center vertex has three outgoing edges and the others have one.")
    void testTIntersection() {
        GameMap map = createMap("""
                wwwww
                wwwww
                wpppw
                wwwpw
                wwwww""");
        MazeGraph graph = new MazeGraph(map);
        Map<IPair, MazeVertex> vertices = new HashMap<>();
        for (MazeVertex v : graph.vertices()) {
            vertices.put(v.loc(), v);
        }
        assertEquals(4, vertices.size());
        
        MazeVertex center = vertices.get(new IPair(3,2));
        assertNotNull(center);
        
        assertNotNull(center.edgeInDirection(Direction.LEFT));
        assertNull(center.edgeInDirection(Direction.UP));
        assertNotNull(center.edgeInDirection(Direction.DOWN));
        assertNull(center.edgeInDirection(Direction.RIGHT));
        
        MazeVertex left = vertices.get(new IPair(2,2));
        assertNotNull(left);
        assertNotNull(left.edgeInDirection(Direction.LEFT));
        assertNotNull(left.edgeInDirection(Direction.RIGHT));
        assertNull(left.edgeInDirection(Direction.UP));
        assertNull(left.edgeInDirection(Direction.DOWN));
        
        MazeVertex down = vertices.get(new IPair(3,3));
        assertNotNull(down);
        assertNotNull(down.edgeInDirection(Direction.UP));
        assertNull(down.edgeInDirection(Direction.LEFT));
        assertNull(down.edgeInDirection(Direction.RIGHT));
        assertNull(down.edgeInDirection(Direction.DOWN));
    }

    @Test
    @DisplayName("Given a GameMap has a cross intersection, the center vertex has four" +
            " outgoing edges and the others have one.")
    void testCrossIntersection() {
        GameMap map = createMap("""
                wwwww
                wwpww
                wpppw
                wwpww
                wwwww""");
        MazeGraph graph = new MazeGraph(map);
        Map<IPair, MazeVertex> vertices = new HashMap<>();
        for (MazeVertex v : graph.vertices()) {
            vertices.put(v.loc(), v);
        }
        assertEquals(5, vertices.size());
        MazeVertex center = vertices.get(new IPair(2,2));
        int centerEdges = 0;
        for (Direction d : Direction.values()) {
            if (center.edgeInDirection(d) != null) centerEdges++;
        }
        assertEquals(4, centerEdges);

        assertEquals(1, countEdges(vertices.get(new IPair(2,1))));
        assertEquals(1, countEdges(vertices.get(new IPair(1,2))));
        assertEquals(1, countEdges(vertices.get(new IPair(3,2))));
        assertEquals(1, countEdges(vertices.get(new IPair(2,3))));
    }

    @Test
    @DisplayName("Given a vertical tunnel, the top and bottom vertices are connected")
    void testVerticalTunnel() {
        GameMap map = createMap("""
                pwwww
                wwwww
                wwwww
                wwwww
                pwwww""");
        MazeGraph graph = new MazeGraph(map);
        Map<IPair, MazeVertex> vertices = new HashMap<>();
        for (MazeVertex v : graph.vertices()) {
            vertices.put(v.loc(), v);
        }
        assertEquals(2, vertices.size());
        IPair top = new IPair(0, 0);
        IPair bottom = new IPair(0, 4);
        assertTrue(vertices.containsKey(top));
        assertTrue(vertices.containsKey(bottom));

        MazeVertex vt = vertices.get(top);
        MazeVertex vb = vertices.get(bottom);

        MazeEdge t2b = vt.edgeInDirection(Direction.UP);
        assertNotNull(t2b);
        double tElev = map.elevations()[0][0];
        double bElev = map.elevations()[0][4];
        assertEquals(vt, t2b.src());
        assertEquals(vb, t2b.dst());
        assertEquals(Direction.UP, t2b.direction());
        assertEquals(MazeGraph.edgeWeight(tElev, bElev), t2b.weight());

        MazeEdge b2t = vb.edgeInDirection(Direction.DOWN);
        assertNotNull(b2t);
        assertEquals(vb, b2t.src());
        assertEquals(vt, b2t.dst());
        assertEquals(Direction.DOWN, b2t.direction());
        assertEquals(MazeGraph.edgeWeight(bElev, tElev), b2t.weight());
    }

    private int countEdges(MazeVertex v) {
        int count = 0;
        for (Direction d : Direction.values()) {
            if (v.edgeInDirection(d) != null) count++;
        }
        return count;
    }

}
