package soe402.cardgame.event;

/**
 * Something that happened in the game, published by the engine.
 *
 * <p>Events carry only strings and numbers, never model objects. That keeps the event package
 * independent of the model: the engine can publish an event without handing out references to
 * its internal state, and the log can be rendered by anything that understands the vocabulary.
 * Being {@code sealed} means every event type is known here, so a renderer can be checked for
 * completeness by the compiler.</p>
 */
public sealed interface GameEvent {

    /**
     * @param firstPlayer name of the player who takes the first turn
     */
    record GameStarted(String firstPlayer) implements GameEvent {
    }

    /**
     * @param player     name of the active player
     * @param turnNumber the turn counter, starting at one
     */
    record TurnStarted(String player, int turnNumber) implements GameEvent {
    }

    /**
     * @param player the player who drew
     * @param card   the card that was drawn
     */
    record CardDrawn(String player, String card) implements GameEvent {
    }

    /**
     * @param player the player whose hand was full
     * @param card   the card that was destroyed instead of drawn
     */
    record CardBurned(String player, String card) implements GameEvent {
    }

    /**
     * @param player the player who played the card
     * @param card   the card that was played
     */
    record CardPlayed(String player, String card) implements GameEvent {
    }

    /**
     * @param attacker the creature that attacked
     * @param target   the character that was attacked
     */
    record AttackDeclared(String attacker, String target) implements GameEvent {
    }

    /**
     * @param source          who caused the damage, or {@code "Fatigue"} for an empty deck
     * @param target          the character that was damaged
     * @param amount          the amount of damage
     * @param remainingHealth the target's health after the damage
     */
    record DamageDealt(String source, String target, int amount, int remainingHealth)
            implements GameEvent {
    }

    /**
     * @param target          the character that was healed
     * @param amount          the amount of health restored
     * @param remainingHealth the target's health after healing
     */
    record Healed(String target, int amount, int remainingHealth) implements GameEvent {
    }

    /**
     * @param creature the creature that was destroyed
     */
    record CreatureDied(String creature) implements GameEvent {
    }

    /**
     * @param target   the creature that received the modifier
     * @param effect   short description of the stat change
     * @param duration number of turns it lasts
     */
    record ModifierApplied(String target, String effect, int duration) implements GameEvent {
    }

    /**
     * @param player the player whose turn ended
     */
    record TurnEnded(String player) implements GameEvent {
    }

    /**
     * @param winner the name of the winning player
     */
    record GameEnded(String winner) implements GameEvent {
    }
}
