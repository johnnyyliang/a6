package model;

import java.awt.Color;
import model.MazeGraph.MazeVertex;
import model.MazeGraph.IPair;

/**
 * Blinky is the red ghost who directly targets PacMann in CHASE mode
 * and targets the northwest corner of the board in FLEE mode.
 */
public class Blinky extends Ghost {
    
    /**
     * Constructs a new Blinky ghost with the red color and 2-second initial delay.
     * 
     * @param model the game model this ghost is part of
     */
    public Blinky(GameModel model) {
        super(model, Color.RED, 2000);
    }
    
    @Override
    protected MazeVertex target() {
        if (state == GhostState.CHASE) {
            return model.pacMann().nearestVertex();
        } else {
            return model.graph().closestTo(2, 2);
        }
    }
} 