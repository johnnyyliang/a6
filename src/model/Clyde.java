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
     * Constructs a new Clyde ghost (orange) with 8 second delay
     */
    public Clyde(GameModel model, Random random) {
        super(model, Color.ORANGE, 8000);
        this.random = random;
    }
    
    @Override
    protected MazeVertex target() {
        if (state == GhostState.CHASE) {
            MazeVertex clydePos = nearestVertex();
            MazeVertex pacMannPos = model.pacMann().nearestVertex();
            
            int dx = clydePos.loc().i() - pacMannPos.loc().i();
            int dy = clydePos.loc().j() - pacMannPos.loc().j();
            double distance = Math.sqrt(dx * dx + dy * dy);
            
            if (distance >= 10) {
                return pacMannPos;
            } else {
                int randomI = random.nextInt(model.width());
                int randomJ = random.nextInt(model.height());
                return model.graph().closestTo(randomI, randomJ);
            }
        } else {
            return model.graph().closestTo(model.width() - 3, model.height() - 3);
        }
    }
} 