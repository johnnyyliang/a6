package model;

import java.awt.Color;
import model.MazeGraph.MazeVertex;

/**
 * Pinky is the pink ghost that tries to get in front of PacMann in CHASE mode
 * and targets the northeast corner of the board in FLEE mode.
 */
public class Pinky extends Ghost {
    
    /**
     * Constructs a new Pinky ghost with the pink color and 4-second initial delay.
     */
    public Pinky(GameModel model) {
        super(model, Color.PINK, 4000);
    }
    
    @Override
    protected MazeVertex target() {
        if (state == GhostState.CHASE) {
            return model.graph().closestTo(model.pacMann().nearestVertex().loc().i() + 3, 
                                         model.pacMann().nearestVertex().loc().j() + 3);
        } else {
            return model.graph().closestTo(model.width() - 3, 2);
        }
    }
} 