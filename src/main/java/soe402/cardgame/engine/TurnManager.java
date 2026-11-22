package soe402.cardgame.engine;

/**
 * Tracks whose turn it is and which {@link Phase} the turn is in.
 *
 * <p>Keeping the phase in one small object means the rule "you may only act in the main phase"
 * is enforced by a single check instead of being repeated in every action.</p>
 */
public final class TurnManager {

    private Phase phase = Phase.START;
    private int turnNumber;

    /**
     * Starts a new turn, resetting the phase to {@link Phase#START}.
     */
    public void beginTurn() {
        turnNumber++;
        phase = Phase.START;
    }

    /**
     * Moves to the next phase, wrapping from {@link Phase#END} back to {@link Phase#START}.
     */
    public void advance() {
        phase = switch (phase) {
            case START -> Phase.DRAW;
            case DRAW -> Phase.MAIN;
            case MAIN -> Phase.END;
            case END -> Phase.START;
        };
    }

    /**
     * Jumps straight to the end phase.
     */
    public void endTurn() {
        phase = Phase.END;
    }

    public Phase getPhase() {
        return phase;
    }

    public int getTurnNumber() {
        return turnNumber;
    }

    /**
     * @return {@code true} when the player is allowed to play cards or attack
     */
    public boolean isMainPhase() {
        return phase == Phase.MAIN;
    }
}
