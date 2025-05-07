package model;

import java.awt.Color;
import model.MazeGraph.MazeVertex;

/**
 * Inky is the cyan ghost who targets the midpoint between Blinky and PacMann in CHASE mode
 * and targets the southwest corner of the board in FLEE mode.
 */
public class Inky extends Ghost {
    
    /**
     * Constructs a new Inky ghost with the cyan color and 6-second initial delay.
     * 
     * @param model the game model this ghost is part of
     */
    public Inky(GameModel model) {
        super(model, Color.CYAN, 6000);
    }
    
    @Override
    protected MazeVertex target() {
        if (state == GhostState.CHASE) {
            int iMid = (model.blinky().nearestVertex().loc().i()
                    + model.pacMann().nearestVertex().loc().i()) / 2;

            int jMid = (model.blinky().nearestVertex().loc().j()
                    + model.pacMann().nearestVertex().loc().j()) / 2;

            return model.graph().closestTo(iMid, jMid);
        } else {
            return model.graph().closestTo(2, model.height() - 3);
        }
    }
} 