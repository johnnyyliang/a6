package model;

import java.util.List;
import graph.Pathfinding;
import model.GameModel.Item;
import model.MazeGraph.MazeEdge;
import model.MazeGraph.MazeVertex;
import model.Ghost;
import model.Ghost.GhostState;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

/**
 * An AI-controlled PacMann that navigates the maze to eat all items without user input.
 */
public class PacMannAI extends PacMann {

    // Keep track of ghost positions for more intelligent avoidance
    private Map<Ghost, MazeVertex> lastGhostPositions = new HashMap<>();
    
    // Add heat map approach for better collective ghost awareness
    // Add data structures for heat map
    private static final double DANGER_RADIUS = 5.0;  // Danger influence radius
    private static final double DANGER_FALLOFF = 2.0; // How quickly danger diminishes with distance
    
    /**
     * Construct a PacMann AI character associated with the given model.
     */
    public PacMannAI(GameModel model) {
        super(model);
        reset();
    }

    // Compute danger level from specific ghost types with their targeting algorithms in mind
    private double calculateGhostDanger(Ghost ghost, MazeVertex pacmanPos) {
        if (ghost.state() == GhostState.FLEE) {
            // Fleeing ghosts are not dangerous
            return 0.0;
        }
        
        if (ghost.state() == GhostState.WAIT) {
            // Waiting ghosts are only dangerous if about to exit
            return ghost.waitTimeRemaining() < 500 ? 0.5 : 0.0;
        }
        
        MazeVertex ghostPos = ghost.nearestVertex();
        
        // Basic distance calculation
        int dx = ghostPos.loc().i() - pacmanPos.loc().i();
        int dy = ghostPos.loc().j() - pacmanPos.loc().j();
        double distance = Math.sqrt(dx * dx + dy * dy);
        
        // Base danger is inverse of distance (closer = more dangerous)
        // Make the danger curve steeper for closer ghosts
        double danger = 15.0 / Math.max(0.5, distance);
        
        // Get PacMan's current direction for target prediction
        MazeEdge currentEdge = location.edge();
        int dirX = 0;
        int dirY = 0;
        
        if (currentEdge != null) {
            dirX = currentEdge.dst().loc().i() - currentEdge.src().loc().i();
            dirY = currentEdge.dst().loc().j() - currentEdge.src().loc().j();
            // Normalize
            if (dirX != 0) dirX = dirX > 0 ? 1 : -1;
            if (dirY != 0) dirY = dirY > 0 ? 1 : -1;
        }
        
        // Predict ghost's next position based on its current path
        List<MazeEdge> ghostPath = ghost.guidancePath();
        if (ghostPath != null && !ghostPath.isEmpty()) {
            MazeVertex nextGhostPos = ghostPath.get(0).dst();
            
            // Calculate if ghost is moving toward PacMan
            int nextDx = nextGhostPos.loc().i() - pacmanPos.loc().i();
            int nextDy = nextGhostPos.loc().j() - pacmanPos.loc().j();
            double nextDistance = Math.sqrt(nextDx * nextDx + nextDy * nextDy);
            
            // If ghost is getting closer, increase danger
            if (nextDistance < distance) {
                danger *= 1.5;
            }
            
            // Predict intersection with PacMan's path
            if (currentEdge != null) {
                MazeVertex pacmanNext = currentEdge.dst();
                if (nextGhostPos.equals(pacmanNext)) {
                    // Ghost will be at our next position!
                    danger *= 5.0;
                }
            }
        }
        
        // Adjust danger based on ghost type
        if (ghost instanceof Blinky) {
            // Blinky directly chases - most dangerous when close
            danger *= 1.5;
            
            // Blinky speeds up as game progresses
            int remainingItems = countRemainingDots();
            int totalVertices = countTotalVertices();
            if (totalVertices > 0 && remainingItems < totalVertices / 2) {
                danger *= 1.3;
            }
        } 
        else if (ghost instanceof Pinky) {
            // Pinky targets 4 tiles ahead of PacMan
            if (dirX != 0 || dirY != 0) {
                // Simulate Pinky's target (4 tiles ahead, with up-direction bug)
                int targetX = pacmanPos.loc().i();
                int targetY = pacmanPos.loc().j();
                
                if (dirY < 0) { // Moving up - apply the bug
                    targetX += dirX * 4 - 4; // 4 tiles left due to bug
                    targetY += dirY * 4;
                } else {
                    targetX += dirX * 4;
                    targetY += dirY * 4;
                }
                
                // Calculate if Pinky is heading toward this position
                int pinkyToTargetX = targetX - ghostPos.loc().i();
                int pinkyToTargetY = targetY - ghostPos.loc().j();
                double pinkyTargetDist = Math.sqrt(pinkyToTargetX*pinkyToTargetX + pinkyToTargetY*pinkyToTargetY);
                
                // If Pinky is close to ambush position, increase danger
                if (pinkyTargetDist < 6) {
                    danger *= 1.8;
                }
                
                // If we're continuing forward, extra danger (Pinky targets ahead)
                if (currentEdge != null && ghostPath != null && !ghostPath.isEmpty()) {
                    MazeVertex nextPacmanPos = currentEdge.dst();
                    int continuingDirX = 0, continuingDirY = 0;
                    
                    // Check if we'd be continuing in the same direction
                    for (MazeEdge e : nextPacmanPos.outgoingEdges()) {
                        int edgeDirX = e.dst().loc().i() - nextPacmanPos.loc().i();
                        int edgeDirY = e.dst().loc().j() - nextPacmanPos.loc().j();
                        // Normalize
                        if (edgeDirX != 0) edgeDirX = edgeDirX > 0 ? 1 : -1;
                        if (edgeDirY != 0) edgeDirY = edgeDirY > 0 ? 1 : -1;
                        
                        // If continuing in same direction
                        if (edgeDirX == dirX && edgeDirY == dirY) {
                            // Going directly toward Pinky's target zone
                            if (pinkyTargetDist < 8) {
                                danger *= 1.5;
                            }
                            break;
                        }
                    }
                }
            }
        }
        else if (ghost instanceof Inky) {
            // Find Blinky for Inky's targeting algorithm
            Ghost blinky = null;
            for (Actor a : model.actors()) {
                if (a instanceof Blinky) {
                    blinky = (Ghost)a;
                    break;
                }
            }
            
            if (blinky != null) {
                MazeVertex blinkyPos = blinky.nearestVertex();
                
                // Calculate pivot point (2 tiles ahead of PacMan)
                int pivotX = pacmanPos.loc().i();
                int pivotY = pacmanPos.loc().j();
                
                if (dirY < 0) { // Moving up - apply bug
                    pivotX += dirX * 2 - 2;
                    pivotY += dirY * 2;
                } else {
                    pivotX += dirX * 2;
                    pivotY += dirY * 2;
                }
                
                // Calculate vector from Blinky to pivot, then double it
                int vectorX = pivotX - blinkyPos.loc().i();
                int vectorY = pivotY - blinkyPos.loc().j();
                
                // Target is pivot + vector
                int targetX = pivotX + vectorX;
                int targetY = pivotY + vectorY;
                
                // Calculate if Inky is close to this target
                int inkyToTargetX = targetX - ghostPos.loc().i();
                int inkyToTargetY = targetY - ghostPos.loc().j();
                double inkyTargetDist = Math.sqrt(inkyToTargetX*inkyToTargetX + inkyToTargetY*inkyToTargetY);
                
                // If Inky is getting into position, increase danger
                if (inkyTargetDist < 6) {
                    danger *= 1.5;
                }
                
                // Check if PacMan is between Blinky and Inky (potential trap)
                boolean pacmanBetween = isBetween(blinkyPos, ghostPos, pacmanPos);
                if (pacmanBetween) {
                    danger *= 2.0; // Significantly more dangerous
                }
                
                // If Blinky is also nearby, Inky's pincer attack is more dangerous
                if (distance < 10 && distance(blinkyPos, pacmanPos) < 10) {
                    danger *= 1.5;
                }
            }
        }
        else if (ghost instanceof Clyde) {
            // Clyde targets directly if far, but runs away if within 8 tiles
            if (distance < 8) {
                danger *= 0.5; // Less dangerous when close
                
                // But more dangerous if near the bottom-left corner
                boolean inLowerLeftArea = isInLowerLeftArea(pacmanPos);
                if (inLowerLeftArea) {
                    danger *= 2.5; // Much more dangerous if you're in his target area
                }
            } else {
                // When far, Clyde behaves like Blinky
                danger *= 1.2;
            }
        }
        
        // Account for ghost movement limitations
        // Ghosts slow down in tunnels
        if (isInTunnel(ghostPos)) {
            danger *= 0.6;
        }
        
        // PacMan takes corners faster than ghosts
        if (isAtCorner(pacmanPos)) {
            danger *= 0.7;
        }
        
        // Ghost access restrictions - ghosts can't turn up in certain areas
        boolean ghostCannotTurnUp = isBlockedUpPassage(ghostPos);
        if (ghostCannotTurnUp && dirY < 0) { // If going up and the ghost can't follow
            danger *= 0.5;
        }
        
        // Track ghost movement direction for smarter avoidance
        MazeVertex lastPos = lastGhostPositions.get(ghost);
        if (lastPos != null && !lastPos.equals(ghostPos)) {
            // Ghost is moving - calculate direction
            int moveX = ghostPos.loc().i() - lastPos.loc().i();
            int moveY = ghostPos.loc().j() - lastPos.loc().j();
            
            // If ghost is moving toward PacMann, increase danger
            if ((moveX > 0 && dx < 0) || (moveX < 0 && dx > 0) ||
                (moveY > 0 && dy < 0) || (moveY < 0 && dy > 0)) {
                danger *= 1.4;
            }
        }
        
        // Update last known position
        lastGhostPositions.put(ghost, ghostPos);
        
        return danger;
    }
    
    // Count dots and pellets left on the board
    private int countRemainingDots() {
        int count = 0;
        for (MazeVertex v : model.graph().vertices()) {
            if (model.itemAt(v) != Item.NONE) {
                count++;
            }
        }
        return count;
    }
    
    // Count total vertices in the graph
    private int countTotalVertices() {
        int count = 0;
        for (MazeVertex v : model.graph().vertices()) {
            count++;
        }
        return count;
    }
    
    // Check if a vertex is in the lower left part of the maze
    private boolean isInLowerLeftArea(MazeVertex v) {
        int minI = Integer.MAX_VALUE;
        int minJ = Integer.MAX_VALUE;
        int maxI = Integer.MIN_VALUE;
        int maxJ = Integer.MIN_VALUE;
        
        // Find maze bounds
        for (MazeVertex vertex : model.graph().vertices()) {
            minI = Math.min(minI, vertex.loc().i());
            minJ = Math.min(minJ, vertex.loc().j());
            maxI = Math.max(maxI, vertex.loc().i());
            maxJ = Math.max(maxJ, vertex.loc().j());
        }
        
        int width = maxI - minI;
        int height = maxJ - minJ;
        
        // Check if in bottom-left quadrant
        return v.loc().i() < minI + (width / 3) && v.loc().j() > minJ + (2 * height / 3);
    }
    
    // Calculate distance between two vertices
    private double distance(MazeVertex v1, MazeVertex v2) {
        int dx = v1.loc().i() - v2.loc().i();
        int dy = v1.loc().j() - v2.loc().j();
        return Math.sqrt(dx*dx + dy*dy);
    }
    
    // Check if vertex is likely in a tunnel
    private boolean isInTunnel(MazeVertex v) {
        int connections = 0;
        for (MazeEdge e : v.outgoingEdges()) {
            connections++;
        }
        return connections <= 2; // Tunnels typically have only 1 or 2 connections
    }
    
    // Check if vertex is at a corner (junction with 2 orthogonal paths)
    private boolean isAtCorner(MazeVertex v) {
        int connections = 0;
        for (MazeEdge e : v.outgoingEdges()) {
            connections++;
        }
        
        if (connections == 2) {
            // Get the two edges
            MazeEdge[] edges = new MazeEdge[2];
            int index = 0;
            for (MazeEdge e : v.outgoingEdges()) {
                if (index < 2) {
                    edges[index++] = e;
                }
            }
            
            if (index == 2) {
                // Check if they form a 90-degree angle
                int dx1 = edges[0].dst().loc().i() - v.loc().i();
                int dy1 = edges[0].dst().loc().j() - v.loc().j();
                int dx2 = edges[1].dst().loc().i() - v.loc().i();
                int dy2 = edges[1].dst().loc().j() - v.loc().j();
                
                // Dot product should be 0 for perpendicular vectors
                return (dx1 * dx2 + dy1 * dy2) == 0;
            }
        }
        return false;
    }
    
    // Helper to get the previous edge that PacMann was traversing
    private MazeEdge previousEdge() {
        return (location.progress() == 1) ? location.edge() : null;
    }

    // Helper to detect if an edge is blocked by non-edible ghosts
    private boolean edgeBlockedByGhost(MazeEdge e) {
        for (Actor a : model.actors()) {
            if (a instanceof Ghost g && g.state() != GhostState.FLEE) {
                // Check ghost's current position
                if (g.location().edge().equals(e) || g.location().edge().equals(e.reverse())) {
                    return true;
                }
                
                // Check if ghost is at or near destination vertex
                if (g.nearestVertex().equals(e.dst())) {
                    return true;
                }
                
                // Check ghost's next planned move
                List<MazeEdge> gp = g.guidancePath();
                if (!gp.isEmpty() && (gp.get(0).equals(e.reverse()) || 
                                     (gp.get(0).dst().equals(e.dst())))) {
                    return true;
                }
            }
        }
        return false;
    }
    
    // Check if a vertex is likely in a restricted upward movement area
    private boolean isBlockedUpPassage(MazeVertex v) {
        // This is a heuristic - in the real game, these are hardcoded specific areas
        // For our purpose, we'll use a simple approximation based on connectivity
        for (MazeEdge e : v.outgoingEdges()) {
            MazeVertex dst = e.dst();
            // If there's an upward edge (dst.j < v.j)
            if (dst.loc().j() < v.loc().j()) {
                // Check if this vertex has low connectivity (like the S-shaped passages)
                int connections = 0;
                for (MazeEdge outEdge : v.outgoingEdges()) {
                    connections++;
                }
                return connections <= 2;
            }
        }
        return false;
    }
    
    // Check if one vertex is between two others
    private boolean isBetween(MazeVertex v1, MazeVertex v2, MazeVertex test) {
        // Simple rectangle check
        int minI = Math.min(v1.loc().i(), v2.loc().i());
        int maxI = Math.max(v1.loc().i(), v2.loc().i());
        int minJ = Math.min(v1.loc().j(), v2.loc().j());
        int maxJ = Math.max(v1.loc().j(), v2.loc().j());
        
        int testI = test.loc().i();
        int testJ = test.loc().j();
        
        // Add a small margin
        minI -= 1;
        maxI += 1;
        minJ -= 1;
        maxJ += 1;
        
        return testI >= minI && testI <= maxI && testJ >= minJ && testJ <= maxJ;
    }
    
    // Evaluate the safety of a potential edge based on ghost positions and behaviors
    private double evaluateEdgeSafety(MazeEdge edge, MazeVertex start) {
        if (edgeBlockedByGhost(edge)) {
            return 0.0; // Completely unsafe
        }
        
        MazeVertex destination = edge.dst();
        double safety = 15.0; // Start with maximum safety
        
        // Evaluate danger from each ghost
        for (Actor a : model.actors()) {
            if (a instanceof Ghost g) {
                double danger = calculateGhostDanger(g, destination);
                safety -= danger;
                
                // Also look at ghost's next position
                List<MazeEdge> ghostPath = g.guidancePath();
                if (ghostPath != null && !ghostPath.isEmpty()) {
                    MazeVertex nextGhostPos = ghostPath.get(0).dst();
                    
                    // If ghost's next position is our destination, very unsafe
                    if (nextGhostPos.equals(destination)) {
                        safety -= 20.0; // Practically guarantees this edge won't be chosen
                    }
                    
                    // If ghost is chasing and will be adjacent to destination
                    if (g.state() == GhostState.CHASE) {
                        // Check if ghost's next position will be adjacent
                        for (MazeEdge e : destination.outgoingEdges()) {
                            if (e.dst().equals(nextGhostPos)) {
                                safety -= 10.0; // Unsafe - ghost will be adjacent
                                break;
                            }
                        }
                    }
                }
            }
        }
        
        // Penalize reversing direction more severely
        MazeEdge prev = previousEdge();
        if (prev != null && edge.equals(prev.reverse())) {
            safety -= 3.0;
        }
        
        // Bonus for edges that lead to items
        if (model.itemAt(destination) != Item.NONE) {
            safety += 1.0;
            if (model.itemAt(destination) == Item.PELLET) {
                safety += 2.0; // Extra bonus for power pellets
            }
        }
        
        // Consider edge properties that help PacMan
        if (isInTunnel(destination)) {
            // Tunnels slow ghosts down
            safety += 1.0;
        }
        
        if (isAtCorner(destination)) {
            // PacMan has advantage at corners
            safety += 1.0;
        }
        
        return Math.max(0.0, safety);
    }

    // Check if ghost is actually catchable
    private boolean isGhostCatchable(Ghost g) {
        // Must be in FLEE state with enough time remaining
        return g.state() == GhostState.FLEE && g.fleeTimeRemaining() > 1500;
    }

    // Evaluate danger level of each vertex based on ALL ghosts at once
    private Map<MazeVertex, Double> buildDangerHeatMap() {
        Map<MazeVertex, Double> dangerMap = new HashMap<>();
        
        // Initialize danger levels for all vertices to zero
        for (MazeVertex v : model.graph().vertices()) {
            dangerMap.put(v, 0.0);
        }
        
        // Accumulate danger from all ghosts
        for (Actor a : model.actors()) {
            if (a instanceof Ghost g && g.state() == GhostState.CHASE) {
                MazeVertex ghostPos = g.nearestVertex();
                double ghostDanger = g instanceof Blinky ? 10.0 : 
                                    g instanceof Pinky ? 9.0 : 
                                    g instanceof Inky ? 8.0 : 6.0;
                    
                // Spread danger to nearby vertices based on ghost type
                for (MazeVertex v : model.graph().vertices()) {
                    int dx = v.loc().i() - ghostPos.loc().i();
                    int dy = v.loc().j() - ghostPos.loc().j();
                    double dist = Math.sqrt(dx*dx + dy*dy);
                    
                    if (dist <= DANGER_RADIUS) {
                        // Calculate danger contribution based on distance
                        double influence = ghostDanger * Math.pow(1.0 - (dist / DANGER_RADIUS), DANGER_FALLOFF);
                        
                        // Add to existing danger at this vertex
                        dangerMap.put(v, dangerMap.get(v) + influence);
                    }
                }
                
                // Add extra danger for future ghost positions based on its path
                List<MazeEdge> ghostPath = g.guidancePath();
                if (ghostPath != null && !ghostPath.isEmpty()) {
                    // Add danger to vertices along the ghost's planned path
                    for (int i = 0; i < Math.min(3, ghostPath.size()); i++) {
                        MazeVertex futurePos = i == 0 ? ghostPath.get(0).dst() : 
                                               ghostPath.get(i-1).dst();
                        double pathDanger = ghostDanger * (1.0 - (i * 0.2)); // Diminish for later positions
                        
                        // Add future path danger
                        dangerMap.put(futurePos, dangerMap.get(futurePos) + pathDanger);
                    }
                }
            }
        }
        
        return dangerMap;
    }

    // Evaluate the safety of an entire path to a destination
    private double evaluatePathSafety(List<MazeEdge> path, Map<MazeVertex, Double> dangerMap) {
        if (path == null || path.isEmpty()) {
            return 0.0;
        }
        
        double totalSafety = 10.0;
        double pathLength = 0.0;
        
        // Starting position
        MazeVertex current = path.get(0).src();
        
        // Accumulate danger along path
        for (MazeEdge e : path) {
            MazeVertex next = e.dst();
            
            // Get danger at this vertex
            double vertexDanger = dangerMap.getOrDefault(next, 0.0);
            
            // Weight danger more heavily for vertices closer to PacMann
            double distanceWeight = Math.max(0.5, 1.0 - (pathLength / 15.0));
            totalSafety -= vertexDanger * distanceWeight;
            
            // Add path length
            pathLength += e.weight();
            
            // Update current position
            current = next;
        }
        
        // Add bonus for paths with items
        if (model.itemAt(path.get(path.size()-1).dst()) != Item.NONE) {
            totalSafety += 2.0;
        }
        
        return Math.max(0.0, totalSafety);
    }

    // Find escape routes that avoid high ghost-density areas
    private MazeEdge findEscapeRoute(MazeVertex start, Map<MazeVertex, Double> dangerMap) {
        // Find directions with lower danger
        MazeEdge bestEdge = null;
        double bestSafety = -1.0;
        
        for (MazeEdge e : start.outgoingEdges()) {
            MazeVertex dest = e.dst();
            double danger = dangerMap.getOrDefault(dest, 0.0);
            
            // Don't just look at immediate neighbor, but also its neighbors
            double avgNeighborDanger = 0.0;
            int neighbors = 0;
            
            for (MazeEdge nextEdge : dest.outgoingEdges()) {
                MazeVertex nextVertex = nextEdge.dst();
                avgNeighborDanger += dangerMap.getOrDefault(nextVertex, 0.0);
                neighbors++;
            }
            
            if (neighbors > 0) {
                avgNeighborDanger /= neighbors;
            }
            
            // Combined safety score (immediate + nearby vertices)
            double safety = 10.0 - danger - (avgNeighborDanger * 0.5);
            
            // Prefer edges that don't reverse direction
            MazeEdge prev = previousEdge();
            if (prev != null && e.equals(prev.reverse())) {
                safety -= 2.0;
            }
            
            // Found a safer edge
            if (safety > bestSafety) {
                bestSafety = safety;
                bestEdge = e;
            }
        }
        
        return bestEdge;
    }

    /**
     * Choose the next edge by targeting the nearest DOT or PELLET via shortest non-backtracking path.
     * Takes into account specific ghost behaviors for better avoidance.
     */
    @Override
    public MazeEdge nextEdge() {
        MazeVertex start = nearestVertex();
        
        // Get overall danger map based on all ghosts
        Map<MazeVertex, Double> dangerMap = buildDangerHeatMap();
        
        // Get immediate danger level at current position
        double currentDanger = dangerMap.getOrDefault(start, 0.0);
        
        // 1) If we're in immediate danger, prioritize escape
        if (currentDanger > 8.0) {
            MazeEdge escapeEdge = findSafestEscape(start, dangerMap);
            if (escapeEdge != null) {
                return escapeEdge;
            }
        }
        
        // 2) Chase edible ghosts if safe
        MazeEdge bestGhostEdge = null;
        double bestGhostScore = -1.0;
        
        for (Actor a : model.actors()) {
            if (a instanceof Ghost g && g.state() == GhostState.FLEE && g.fleeTimeRemaining() > 1500) {
                // Target ghost position
                MazeVertex ghostPos = g.nearestVertex();
                
                List<MazeEdge> path = Pathfinding.shortestNonBacktrackingPath(
                        start, ghostPos, previousEdge());
                
                if (path != null && !path.isEmpty()) {
                    // Evaluate path safety
                    double pathSafety = evaluatePathSafety(path, dangerMap);
                    double dist = 0;
                    for (MazeEdge e : path) dist += e.weight();
                    
                    // Score based on distance and flee time
                    double fleeTimeScore = g.fleeTimeRemaining() / 1000.0;
                    double ghostScore = pathSafety + (10.0 - dist) + fleeTimeScore;
                    
                    // Check for other ghosts that might get in the way
                    boolean otherGhostsOnPath = false;
                    for (Actor other : model.actors()) {
                        if (other != a && other instanceof Ghost og && og.state() != GhostState.FLEE) {
                            MazeVertex otherPos = og.nearestVertex();
                            for (MazeEdge e : path) {
                                if (e.dst().equals(otherPos)) {
                                    otherGhostsOnPath = true;
                                    break;
                                }
                            }
                            if (otherGhostsOnPath) break;
                        }
                    }
                    
                    // Penalize paths with non-edible ghosts
                    if (otherGhostsOnPath) {
                        ghostScore -= 5.0;
                    }
                    
                    // Only chase if score is high enough
                    if (ghostScore > bestGhostScore && dist < 10) {
                        bestGhostScore = ghostScore;
                        bestGhostEdge = path.get(0);
                    }
                }
            }
        }
        
        if (bestGhostEdge != null && bestGhostScore > 5.0) {
            return bestGhostEdge;
        }
        
        // 3) Prioritize power pellets when ghosts are nearby
        if (isAnyGhostNearby(8.0)) {
            MazeEdge bestPelletEdge = null;
            double bestPelletScore = -1.0;
            
            for (MazeVertex v : model.graph().vertices()) {
                if (model.itemAt(v) == Item.PELLET) {
                    List<MazeEdge> path = Pathfinding.shortestNonBacktrackingPath(
                            start, v, previousEdge());
                    
                    if (path != null && !path.isEmpty()) {
                        double pathSafety = evaluatePathSafety(path, dangerMap);
                        double dist = 0;
                        for (MazeEdge e : path) dist += e.weight();
                        
                        // Prioritize nearby pellets more when ghosts closing in
                        double score = pathSafety - (dist / 15.0) + 3.0;
                        
                        // Check path for non-edible ghosts
                        boolean dangerousGhostsOnPath = pathHasNonFleeingGhosts(path);
                        if (dangerousGhostsOnPath) {
                            score -= 10.0; // Heavy penalty
                        }
                        
                        if (score > bestPelletScore) {
                            bestPelletScore = score;
                            bestPelletEdge = path.get(0);
                        }
                    }
                }
            }
            
            if (bestPelletEdge != null && bestPelletScore > 0.0) {
                return bestPelletEdge;
            }
        }
        
        // 4) Find safest path to any item
        MazeEdge bestItemEdge = null;
        double bestItemScore = -Double.MAX_VALUE;
        
        for (MazeVertex v : model.graph().vertices()) {
            if (model.itemAt(v) != Item.NONE) {
                List<MazeEdge> path = Pathfinding.shortestNonBacktrackingPath(
                        start, v, previousEdge());
                
                if (path != null && !path.isEmpty()) {
                    double pathSafety = evaluatePathSafety(path, dangerMap);
                    double dist = 0;
                    for (MazeEdge e : path) dist += e.weight();
                    
                    // Scoring: prioritize safety and items
                    double itemBonus = (model.itemAt(v) == Item.PELLET) ? 2.0 : 0.0;
                    double score = pathSafety - (dist / 20.0) + itemBonus;
                    
                    // Check path for non-edible ghosts
                    boolean dangerousGhostsOnPath = pathHasNonFleeingGhosts(path);
                    if (dangerousGhostsOnPath) {
                        score -= 8.0; // Heavy penalty
                    }
                    
                    if (score > bestItemScore) {
                        bestItemScore = score;
                        bestItemEdge = path.get(0);
                    }
                }
            }
        }
        
        if (bestItemEdge != null && bestItemScore > 0.0) {
            return bestItemEdge;
        }
        
        // 5) If in danger, find escape route
        if (dangerMap.getOrDefault(start, 0.0) > 3.0) {
            MazeEdge escapeEdge = findSafestEscape(start, dangerMap);
            if (escapeEdge != null) {
                return escapeEdge;
            }
        }
        
        // 6) Last resort - pick any available edge that's not too dangerous
        MazeEdge safestEdge = null;
        double maxSafety = -Double.MAX_VALUE;
        
        for (MazeEdge e : start.outgoingEdges()) {
            double safety = 10.0 - dangerMap.getOrDefault(e.dst(), 10.0);
            
            // Avoid reversing unless necessary
            MazeEdge prev = previousEdge();
            if (prev != null && e.equals(prev.reverse())) {
                safety -= 3.0;
            }
            
            if (safety > maxSafety) {
                maxSafety = safety;
                safestEdge = e;
            }
        }
        
        if (safestEdge != null) {
            return safestEdge;
        }
        
        // 7) Ultimate fallback - any edge
        for (MazeEdge e : start.outgoingEdges()) {
            return e;
        }
        
        return null;
    }
    
    // Find the safest escape route from the current position
    private MazeEdge findSafestEscape(MazeVertex start, Map<MazeVertex, Double> dangerMap) {
        MazeEdge safestEdge = null;
        double bestSafety = -Double.MAX_VALUE;
        
        for (MazeEdge e : start.outgoingEdges()) {
            MazeVertex dest = e.dst();
            double currentDanger = dangerMap.getOrDefault(dest, 0.0);
            
            // High penalty for edges that lead to immediate ghost encounters
            if (edgeLeadsToGhost(e)) {
                continue;
            }
            
            // Consider the safety of subsequent moves
            double futureSafety = 0.0;
            int safeExits = 0;
            
            // Check how many safe exits exist from this position
            for (MazeEdge exitEdge : dest.outgoingEdges()) {
                if (!exitEdge.equals(e.reverse()) && !edgeLeadsToGhost(exitEdge)) {
                    double exitDanger = dangerMap.getOrDefault(exitEdge.dst(), 0.0);
                    if (exitDanger < 5.0) {
                        safeExits++;
                        futureSafety += (10.0 - exitDanger);
                    }
                }
            }
            
            // Normalize by number of exits
            if (safeExits > 0) {
                futureSafety /= safeExits;
            }
            
            // Overall safety considers current danger and future options
            double safety = (10.0 - currentDanger) + (futureSafety * 0.5);
            
            // Bonus if the destination has an item
            if (model.itemAt(dest) != Item.NONE) {
                safety += 2.0;
            }
            
            // Avoid reversing direction unless necessary
            MazeEdge prev = previousEdge();
            if (prev != null && e.equals(prev.reverse())) {
                safety -= 3.0;
            }
            
            if (safety > bestSafety) {
                bestSafety = safety;
                safestEdge = e;
            }
        }
        
        return safestEdge;
    }
    
    // Check if an edge leads directly to a ghost
    private boolean edgeLeadsToGhost(MazeEdge edge) {
        MazeVertex dst = edge.dst();
        
        for (Actor a : model.actors()) {
            if (a instanceof Ghost g && g.state() == GhostState.CHASE) {
                if (g.nearestVertex().equals(dst)) {
                    return true;
                }
                
                // Also check if ghost is heading to this vertex
                List<MazeEdge> ghostPath = g.guidancePath();
                if (ghostPath != null && !ghostPath.isEmpty() && 
                    ghostPath.get(0).dst().equals(dst)) {
                    return true;
                }
            }
        }
        
        return false;
    }
    
    // Check if a path has any non-fleeing ghosts on it
    private boolean pathHasNonFleeingGhosts(List<MazeEdge> path) {
        // Create set of vertices in the path
        java.util.Set<MazeVertex> pathVertices = new java.util.HashSet<>();
        for (MazeEdge e : path) {
            pathVertices.add(e.dst());
        }
        
        // Check if any non-fleeing ghost is on the path
        for (Actor a : model.actors()) {
            if (a instanceof Ghost g && g.state() != GhostState.FLEE) {
                // Check current position
                if (pathVertices.contains(g.nearestVertex())) {
                    return true;
                }
                
                // Check ghost's upcoming positions too
                List<MazeEdge> ghostPath = g.guidancePath();
                if (ghostPath != null && !ghostPath.isEmpty()) {
                    for (int i = 0; i < Math.min(3, ghostPath.size()); i++) {
                        if (pathVertices.contains(ghostPath.get(i).dst())) {
                            return true;
                        }
                    }
                }
            }
        }
        
        return false;
    }
    
    // Check if any ghost is within the specified distance
    private boolean isAnyGhostNearby(double maxDistance) {
        MazeVertex pacmanPos = nearestVertex();
        
        for (Actor a : model.actors()) {
            if (a instanceof Ghost g && g.state() == GhostState.CHASE) {
                MazeVertex ghostPos = g.nearestVertex();
                if (distance(pacmanPos, ghostPos) < maxDistance) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Override guidancePath to visualize the AI's planned path
     */
    @Override
    public List<MazeEdge> guidancePath() {
        MazeVertex start = nearestVertex();
        MazeEdge previousEdge = (location.progress() == 1) ? location.edge() : null;
        
        // First try to find path to catchable ghost
        for (Actor a : model.actors()) {
            if (a instanceof Ghost g && isGhostCatchable(g)) {
                MazeVertex ghostPos;
                if (g.location().atVertex()) {
                    ghostPos = g.nearestVertex();
                } else {
                    ghostPos = g.location().progress() < 0.5 ? 
                               g.location().edge().src() : 
                               g.location().edge().dst();
                }
                
                List<MazeEdge> path = Pathfinding.shortestNonBacktrackingPath(
                        start, ghostPos, previousEdge);
                        
                if (path != null && !path.isEmpty() && path.size() < 10) {
                    return path;
                }
            }
        }
        
        // Next try to find path to item
        MazeVertex bestItem = null;
        double bestItemDist = Double.POSITIVE_INFINITY;
        List<MazeEdge> bestPath = null;
        
        for (MazeVertex v : model.graph().vertices()) {
            if (model.itemAt(v) != Item.NONE) {
                List<MazeEdge> path = Pathfinding.shortestNonBacktrackingPath(
                        start, v, previousEdge);
                        
                if (path != null && !path.isEmpty()) {
                    double dist = 0;
                    for (MazeEdge e : path) dist += e.weight();
                    
                    if (dist < bestItemDist) {
                        bestItemDist = dist;
                        bestPath = path;
                    }
                }
            }
        }
        
        if (bestPath != null) {
            return bestPath;
        }
        
        // Default to superclass implementation if no path found
        return super.guidancePath();
    }
} 