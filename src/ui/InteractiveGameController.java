package ui;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.beans.PropertyChangeListener;
import javax.swing.Timer;
import javax.swing.event.SwingPropertyChangeSupport;
import model.GameModel;
import model.GameModel.GameState;
import model.MazeGraph.Direction;

public class InteractiveGameController implements KeyListener {

    public enum GameState {RUNNING, PAUSED, LIFESTART, GAMEOVER}

    private GameModel model;
    private final Timer timer;
    private GameState state;

    /**
     * Helper object for managing property change notifications.
     */
    protected SwingPropertyChangeSupport propSupport;

    public InteractiveGameController(GameModel model) {
        state = GameState.LIFESTART;
        timer = new Timer(16, e -> nextFrame());

        boolean notifyOnEdt = true;
        propSupport = new SwingPropertyChangeSupport(this, notifyOnEdt);

        setModel(model);
    }

    public void setModel(GameModel newModel) {
        reset();
        model = newModel;
        model.addPropertyChangeListener("game_state", e -> {
            if (model.state() != GameModel.GameState.PLAYING) {
                stopGame();
            }
        });
    }

    private void stopGame() {
        timer.stop();
        setState(model.state() == GameModel.GameState.READY ? GameState.LIFESTART
                : GameState.GAMEOVER);
    }

    private void nextFrame() {
        // TODO: duration?
        model.updateActors(16);
    }

    @Override
    public void keyTyped(KeyEvent e) {
        // Do nothing
    }

    @Override
    public void keyPressed(KeyEvent e) {
        // TODO 6: Complete the definition of this KeyListener method to respond to key pressed by
        //  the user. When the left arrow or letter 'a' key is pressed, this is interpreted as a
        //  LEFT directional command, and the model should be updated to reflect this. Similarly,
        //  pressing the right arrow or letter 'd' is interpreted as a RIGHT directional command.
        //  The up arrow and letter 'w' are interpreted as UP directional commands. The down arrow
        //  and letter 's' are interpreted as DOWN directional commands. Pressing the space key
        //  should have the same effect as pressing the start/pause button in the GUI. Also, when
        //  directional commands are entered when the game is PAUSED or between lives (in state
        //  LIFESTART), the game should begin RUNNING and the timer should start.
        int key = e.getKeyCode();

        if ((key == KeyEvent.VK_UP) || (key == KeyEvent.VK_W)){
            model.updatePlayerCommand(Direction.UP);
            start();
        } else if ((key == KeyEvent.VK_LEFT) || (key == KeyEvent.VK_A)){
            model.updatePlayerCommand(Direction.LEFT);
            start();
        } else if ((key == KeyEvent.VK_DOWN) || (key == KeyEvent.VK_S)){
            model.updatePlayerCommand(Direction.DOWN);
            start();
        } else if ((key == KeyEvent.VK_RIGHT) || (key == KeyEvent.VK_D)){
            model.updatePlayerCommand(Direction.RIGHT);
            start();
        } else if ((key == KeyEvent.VK_SPACE)) {
            processStartPause();
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        // Do nothing
    }

    /**
     * Processes a press of the start/pause button. Toggles between the RUNNING and PAUSED
     * GameStates.
     */
    public void processStartPause() {
        if (state == GameState.PAUSED) {
            setState(GameState.RUNNING);
            timer.start();
        } else if (state == GameState.RUNNING) {
            timer.stop();
            setState(GameState.PAUSED);
        } else if (state == GameState.LIFESTART) {
//            model.useLife();
            setState(GameState.RUNNING);
            timer.start();
        }
    }

    /**
     * starts the game if game is in PAUSED or LIFESTART state. Helper function for keyPressed()
     */
    public void start(){
        if((state() == GameState.PAUSED) || (state() == GameState.LIFESTART)){
            setState(GameState.RUNNING);
            timer.start();
        }
    }

    public void pause() {
        if (state == GameState.RUNNING) {
            timer.stop();
            setState(GameState.PAUSED);
        }
    }

    public void reset() {
        timer.stop();
        setState(GameState.LIFESTART);
    }

    public GameState state() {
        return state;
    }

    private void setState(GameState newState) {
        GameState oldState = state;
        state = newState;
        propSupport.firePropertyChange("game_state", oldState, state);
    }

    /* Observation interface */

    /**
     * Register `listener` to be notified whenever any property of this model is changed.
     */
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        propSupport.addPropertyChangeListener(listener);
    }

    /**
     * Register `listener` to be notified whenever the property named `propertyName` of this model
     * is changed.
     */
    public void addPropertyChangeListener(String propertyName, PropertyChangeListener listener) {
        propSupport.addPropertyChangeListener(propertyName, listener);
    }

    /**
     * Stop notifying `listener` of property changes for this model (assuming it was added no more
     * than once).  Does not affect listeners who were registered with a particular property name.
     */
    public void removePropertyChangeListener(PropertyChangeListener listener) {
        propSupport.removePropertyChangeListener(listener);
    }

    /**
     * Stop notifying `listener` of changes to the property named `propertyName` for this model
     * (assuming it was added no more than once).  Does not affect listeners who were not registered
     * with `propertyName`.
     */
    public void removePropertyChangeListener(String propertyName, PropertyChangeListener listener) {
        propSupport.removePropertyChangeListener(propertyName, listener);
    }
}
