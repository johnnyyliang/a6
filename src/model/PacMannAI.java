package model;

import java.util.List;
import graph.Pathfinding;
import model.GameModel;
import model.GameModel.Item;
import model.MazeGraph.MazeEdge;
import model.MazeGraph.MazeVertex;
import model.Ghost;

/**
 * PacMannAI focused on point collection and ghost avoidance.
 */
public class PacMannAI extends PacMann {
    
    private static final double GHOST_DANGER = 5.0;
    private MazeVertex currentTarget;
    private boolean needNewTarget = true;
    
    public PacMannAI(GameModel model) {
        super(model);
        reset();
    }
    
    @Override
    public MazeEdge nextEdge() {
        MazeVertex pos = nearestVertex();
        MazeEdge prev = (location.progress() == 1) ? location.edge() : null;
        
        if (isInDanger(pos)) {
            MazeEdge escape = findSafeMove(pos, prev);
            if (escape != null) {
                needNewTarget = true;
                return escape;
            }
        }
        
        if (needNewTarget || currentTarget == null || model.itemAt(currentTarget) == null) {
            currentTarget = findBestTarget(pos, prev);
            needNewTarget = false;
        }
        
        if (currentTarget != null) {
            List<MazeEdge> path = Pathfinding.shortestNonBacktrackingPath(pos, currentTarget, prev);
            if (path != null && !path.isEmpty() && isPathSafe(path)) {
                return path.get(0);
            }
        }
        
        for (MazeEdge edge : pos.outgoingEdges()) {
            if (prev != null && edge.equals(prev.reverse())) continue;
            if (!isInDanger(edge.dst())) return edge;
        }
        
        if (prev != null) return prev.reverse();
        for (MazeEdge edge : pos.outgoingEdges()) return edge;
        return null;
    }
    
    private MazeVertex findBestTarget(MazeVertex pos, MazeEdge prev) {
        MazeVertex target = findClosestItem(pos, prev, Item.PELLET);
        if (target == null) target = findClosestItem(pos, prev, Item.DOT);
        return target;
    }
    
    private MazeVertex findClosestItem(MazeVertex pos, MazeEdge prev, Item type) {
        MazeVertex best = null;
        double bestDist = Double.POSITIVE_INFINITY;
        
        for (MazeVertex v : model.graph().vertices()) {
            if (model.itemAt(v) == type) {
                List<MazeEdge> path = Pathfinding.shortestNonBacktrackingPath(pos, v, prev);
                if (path != null && !path.isEmpty() && isPathSafe(path)) {
                    double pathLen = calcPathLength(path);
                    if (pathLen < bestDist) {
                        bestDist = pathLen;
                        best = v;
                    }
                }
            }
        }
        return best;
    }
    
    private double calcPathLength(List<MazeEdge> path) {
        double len = 0;
        for (MazeEdge e : path) len += e.weight();
        return len;
    }
    
    private boolean isInDanger(MazeVertex pos) {
        for (Actor a : model.actors()) {
            if (a instanceof Ghost) {
                if (distance(pos, ((Ghost)a).nearestVertex()) < GHOST_DANGER) {
                    return true;
                }
            }
        }
        return false;
    }
    
    private boolean isPathSafe(List<MazeEdge> path) {
        if (path == null || path.isEmpty()) return false;
        
        int steps = Math.min(path.size(), 5);
        MazeVertex pos = path.get(0).src();
        
        for (int i = 0; i < steps; i++) {
            pos = path.get(i).dst();
            if (isInDanger(pos)) return false;
        }
        return true;
    }
    
    private MazeEdge findSafeMove(MazeVertex pos, MazeEdge prev) {
        MazeEdge best = null;
        double bestScore = -Double.MAX_VALUE;
        
        for (MazeEdge e : pos.outgoingEdges()) {
            if (prev != null && e.equals(prev.reverse())) continue;
            
            MazeVertex next = e.dst();
            double score = evaluateSafety(next);
            
            if (score > bestScore) {
                bestScore = score;
                best = e;
            }
        }
        
        return best;
    }
    
    private double evaluateSafety(MazeVertex pos) {
        double minGhostDist = Double.POSITIVE_INFINITY;
        
        for (Actor a : model.actors()) {
            if (a instanceof Ghost) {
                double d = distance(pos, ((Ghost)a).nearestVertex());
                minGhostDist = Math.min(minGhostDist, d);
            }
        }
        
        if (model.itemAt(pos) == Item.PELLET) minGhostDist += 2.0;
        else if (model.itemAt(pos) == Item.DOT) minGhostDist += 1.0;
        
        return minGhostDist;
    }
    
    private double distance(MazeVertex v1, MazeVertex v2) {
        int dx = v1.loc().i() - v2.loc().i();
        int dy = v1.loc().j() - v2.loc().j();
        return Math.hypot(dx, dy);
    }
}
