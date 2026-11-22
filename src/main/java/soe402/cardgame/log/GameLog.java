package soe402.cardgame.log;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

import soe402.cardgame.event.GameEvent;
import soe402.cardgame.event.GameEventListener;

/**
 * Turns game events into readable lines and keeps them for later inspection.
 *
 * <p>The log is a pure observer: it changes no game state and the engine never asks it
 * questions. That is what makes "the game log" a genuine Observer pattern rather than a set of
 * {@code System.out.println} calls sprinkled through the engine, and it means the same log can
 * be replayed into a file, a GUI or a test assertion.</p>
 *
 * <p>Lines are kept in memory even when no stream is given, so tests can assert on the exact
 * text of the log without capturing standard output.</p>
 */
public final class GameLog implements GameEventListener {

    private final List<String> lines = new ArrayList<>();
    private final PrintStream out;

    /**
     * Creates a log that only keeps lines in memory.
     */
    public GameLog() {
        this(null);
    }

    /**
     * @param out stream that receives every line as it is added, or {@code null} to stay silent
     */
    public GameLog(PrintStream out) {
        this.out = out;
    }

    /**
     * Adds a free text line. Used by cards for details that have no dedicated event.
     *
     * @param message the line to add
     */
    public void log(String message) {
        lines.add(message);
        if (out != null) {
            out.println(message);
        }
    }

    @Override
    public void onEvent(GameEvent event) {
        log(render(event));
    }

    /**
     * @return an immutable snapshot of every line written so far
     */
    public List<String> getLines() {
        return List.copyOf(lines);
    }

    /**
     * @return the whole log as one string, separated by the platform line separator
     */
    public String text() {
        return String.join(System.lineSeparator(), lines);
    }

    /**
     * Removes every line. Useful between demo games or in tests.
     */
    public void clear() {
        lines.clear();
    }

    /**
     * Formats a single event. Kept static and public so it can be unit tested on its own.
     *
     * @param event the event to format
     * @return a single readable line
     */
    public static String render(GameEvent event) {
        if (event instanceof GameEvent.GameStarted started) {
            return "Game started - " + started.firstPlayer() + " goes first.";
        }
        if (event instanceof GameEvent.TurnStarted turn) {
            return "--- Turn " + turn.turnNumber() + ": " + turn.player() + " ---";
        }
        if (event instanceof GameEvent.CardDrawn drawn) {
            return drawn.player() + " draws " + drawn.card();
        }
        if (event instanceof GameEvent.CardBurned burned) {
            return burned.player() + "'s hand is full - " + burned.card() + " is burned";
        }
        if (event instanceof GameEvent.CardPlayed played) {
            return played.player() + " plays " + played.card();
        }
        if (event instanceof GameEvent.AttackDeclared attack) {
            return attack.attacker() + " attacks " + attack.target();
        }
        if (event instanceof GameEvent.DamageDealt damage) {
            String cause = "Fatigue".equals(damage.source())
                    ? "fatigue damage"
                    : "damage from " + damage.source();
            return damage.target() + " takes " + damage.amount() + " " + cause
                    + " (" + damage.remainingHealth() + " health left)";
        }
        if (event instanceof GameEvent.Healed healed) {
            return healed.target() + " is healed for " + healed.amount()
                    + " (" + healed.remainingHealth() + " health)";
        }
        if (event instanceof GameEvent.CreatureDied died) {
            return died.creature() + " dies";
        }
        if (event instanceof GameEvent.ModifierApplied applied) {
            return applied.target() + " gains " + applied.effect()
                    + " for " + applied.duration() + " turns";
        }
        if (event instanceof GameEvent.TurnEnded ended) {
            return ended.player() + " ends the turn";
        }
        if (event instanceof GameEvent.GameEnded ended) {
            return "*** " + ended.winner() + " wins the game ***";
        }
        return event.toString();
    }
}
