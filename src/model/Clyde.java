package model;

import java.awt.Color;
import java.util.Random;
import model.MazeGraph.MazeVertex;

/**
 * Clyde is the orange ghost with erratic behavior who targets PacMann directly
 * when far away but changes to random movement when close.
 */
public class Clyde extends Ghost {
    
    /** Random number generator for generating random coordinates */
    private final Random random;
    
    /**
     * Constructs a new Clyde ghost with the orange color, 8-second initial delay,
     * and a random number generator for erratic behavior.
     * 
     * @param model the game model this ghost is part of
     * @param random random number generator for unpredictable movements
     */
    public Clyde(GameModel model, Random random) {
        super(model, Color.ORANGE, 8000);
        this.random = random;
    }
    
    @Override
    protected MazeVertex target() {
        if (state == GhostState.CHASE) {
            // Calculate Euclidean distance between Clyde and PacMann
            MazeVertex clydePos = nearestVertex();
            MazeVertex pacMannPos = model.pacMann().nearestVertex();
            
            int dx = clydePos.loc().i() - pacMannPos.loc().i();
            int dy = clydePos.loc().j() - pacMannPos.loc().j();
            double distance = Math.sqrt(dx * dx + dy * dy);
            
            // If distance is 10 or more, target PacMann directly (like Blinky)
            if (distance >= 10) {
                return pacMannPos;
            } else {
                // Otherwise, choose random coordinates within the maze
                int randomI = random.nextInt(model.width());
                int randomJ = random.nextInt(model.height());
                return model.graph().closestTo(randomI, randomJ);
            }
        } else { // FLEE state
            // In FLEE state, Clyde targets the southeast corner (width-3, height-3)
            return model.graph().closestTo(model.width() - 3, model.height() - 3);
        }
    }
} 