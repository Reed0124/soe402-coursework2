package soe402.cardgame.event;

import java.util.ArrayList;
import java.util.List;

/**
 * A minimal synchronous publish/subscribe bus.
 *
 * <p>Listeners are notified in subscription order and in the same thread, so the log is always
 * consistent with the state change that produced it. Copying the listener list before notifying
 * makes it safe for a listener to unsubscribe while handling an event.</p>
 */
public final class EventBus {

    private final List<GameEventListener> listeners = new ArrayList<>();

    /**
     * @param listener the listener to add
     */
    public void subscribe(GameEventListener listener) {
        if (listener != null && !listeners.contains(listener)) {
            listeners.add(listener);
        }
    }

    /**
     * @param listener the listener to remove
     */
    public void unsubscribe(GameEventListener listener) {
        listeners.remove(listener);
    }

    /**
     * Delivers the event to every subscriber.
     *
     * @param event the event to publish
     */
    public void publish(GameEvent event) {
        for (GameEventListener listener : List.copyOf(listeners)) {
            listener.onEvent(event);
        }
    }

    /**
     * @return how many listeners are subscribed
     */
    public int listenerCount() {
        return listeners.size();
    }
}
