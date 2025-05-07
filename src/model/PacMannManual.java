package model;

import model.MazeGraph.MazeEdge;

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
            return nearestVertex().edgeInDirection(currentEdge().direction());

        } else {
            return null;
        }
    }
}
