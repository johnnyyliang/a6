package model;

import java.util.List;
import model.MazeGraph.Direction;
import model.MazeGraph.MazeEdge;
import model.MazeGraph.MazeVertex;

//TODO 5: Extend this class by defining a (non-abstract) subclass `PacMannManual` in a new file
//  "model/PacMannManual.java".  This class determines PacMann's next edge based on the most recent
//  directional command entered by the player.  See the assignment description for more details.

public class PacMannManual extends PacMann {

    /**
     * Construct a PacMann character controlled with manual inputs associated to the given `model`.
     */
    public PacMannManual(GameModel model) {
        super(model);
        reset();
    }

    /**
     * Returns the next edge based on the most recent directional command entered by the player.
     */
    @Override
    public MazeEdge nextEdge() {
        //MazeEdge nextEdge = nearestVertex().edgeInDirection(playerCommand());
        if (nearestVertex().edgeInDirection(model.playerCommand()) != null){
            return nearestVertex().edgeInDirection(model.playerCommand());

        } else if(nearestVertex().edgeInDirection(currentEdge().direction()) != null){
            return nearestVertex().edgeInDirection(currentEdge().direction()); //TODO MIGHT BE WRONG DIRECTION

        } else {
            return null;
        }
    }
}
