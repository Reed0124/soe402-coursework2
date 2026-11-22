package soe402.cardgame.model;

import java.util.List;

/**
 * The controlled view of the game that cards are allowed to use.
 *
 * <p>This interface is the seam between the model and the engine, and it exists for two
 * reasons. First, it is dependency inversion: cards depend on a small abstraction instead of
 * the concrete {@code GameEngine}, so the model package has no compile time dependency on the
 * engine package. Second, it keeps card authors honest: a card can deal damage, draw cards or
 * summon a creature, but it cannot reach into the turn structure or the deck order.</p>
 *
 * <p>{@code GameEngine} is the only production implementation; tests provide their own stub,
 * which makes effects testable without starting a whole game.</p>
 */
public interface GameContext {

    /**
     * Deals damage and lets the engine run its consequences: logging, death checks and the
     * win condition.
     *
     * @param target the character that is damaged
     * @param amount positive amount of damage
     * @param source the character that caused the damage, or {@code null} for fatigue
     */
    void dealDamage(GameCharacter target, int amount, GameCharacter source);

    /**
     * Restores health, capped at the target's maximum health.
     *
     * @param target the character that is healed
     * @param amount positive amount of health to restore
     */
    void heal(GameCharacter target, int amount);

    /**
     * Draws cards for a player, applying the hand limit and fatigue rules.
     *
     * @param player the player who draws
     * @param amount how many cards to draw
     */
    void drawCards(Player player, int amount);

    /**
     * Puts a creature onto its controller's board.
     *
     * @param controller the owner of the creature
     * @param creature   the creature being summoned
     */
    void summon(Player controller, Creature creature);

    /**
     * Attaches a timed stat change to a creature.
     *
     * @param target   the creature that receives the modifier
     * @param statMod  the stat change
     * @param duration number of turns the change lasts
     */
    void attachModifier(Creature target, StatMod statMod, int duration);

    /**
     * @param player one of the two players
     * @return the other player
     */
    Player opponentOf(Player player);

    /**
     * @param player one of the two players
     * @return an immutable snapshot of that player's board
     */
    List<Creature> creaturesOwnedBy(Player player);

    /**
     * @return {@code true} once a hero has reached zero health
     */
    boolean isFinished();

    /**
     * Adds a free text line to the game log.
     *
     * @param message the line to add
     */
    void log(String message);
}
