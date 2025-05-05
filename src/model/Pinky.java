package model;

import java.awt.Color;
import model.MazeGraph.MazeVertex;

/**
 * Pinky is the pink ghost who aims to get in front of PacMann in CHASE mode
 * and targets the northeast corner of the board in FLEE mode.
 */
public class Pinky extends Ghost {
    
    /**
     * Constructs a new Pinky ghost with the pink color and 4-second initial delay.
     * 
     * @param model the game model this ghost is part of
     */
    public Pinky(GameModel model) {
        super(model, Color.PINK, 4000);
    }
    
    @Override
    protected MazeVertex target() {
        if (state == GhostState.CHASE) {
            // In CHASE state, Pinky targets the coordinates 3 units away from PacMann 
            // in the current direction that PacMann is facing
            return model.graph().closestTo(model.pacMann().nearestVertex().loc().i() + 3, 
                                         model.pacMann().nearestVertex().loc().j() + 3);
        } else { // FLEE state
            // In FLEE state, Pinky targets the northeast corner (width-3, 2)
            return model.graph().closestTo(model.width() - 3, 2);
        }
    }
} 