package soe402.cardgame.engine;

/**
 * The phases a turn goes through, in order.
 *
 * <p>{@code START} raises the mana ceiling and refreshes creatures, {@code DRAW} draws a card,
 * {@code MAIN} is the only phase in which a player may act, and {@code END} runs the
 * end-of-turn bookkeeping such as modifier countdowns.</p>
 */
public enum Phase {

    /** Mana is refilled and creatures are refreshed. */
    START,

    /** The turn's card is drawn. */
    DRAW,

    /** Cards may be played and creatures may attack. */
    MAIN,

    /** End of turn triggers are resolved. */
    END
}
