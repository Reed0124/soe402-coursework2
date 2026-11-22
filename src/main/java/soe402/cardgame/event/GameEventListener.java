package soe402.cardgame.event;

/**
 * Observer interface for anything that wants to react to game events.
 *
 * <p>The game log is the only implementation in this project, but the same seam would carry a
 * graphical view, an achievement tracker or a replay recorder without touching the engine.</p>
 */
@FunctionalInterface
public interface GameEventListener {

    /**
     * Called for every published event, in publication order.
     *
     * @param event the event that happened
     */
    void onEvent(GameEvent event);
}
