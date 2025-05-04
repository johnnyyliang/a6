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
            // In CHASE state, Inky targets the midpoint between Blinky and PacMann
            MazeVertex blinkyPos = model.blinky().nearestVertex();
            MazeVertex pacMannPos = model.pacMann().nearestVertex();
            
            // Calculate the midpoint between Blinky and PacMann
            int midpointI = (blinkyPos.loc().i() + pacMannPos.loc().i()) / 2;
            int midpointJ = (blinkyPos.loc().j() + pacMannPos.loc().j()) / 2;
            
            return model.graph().closestTo(midpointI, midpointJ);
        } else { // FLEE state
            // In FLEE state, Inky targets the southwest corner (2, height-3)
            return model.graph().closestTo(2, model.height() - 3);
        }
    }
} 