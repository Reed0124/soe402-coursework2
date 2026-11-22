package soe402.cardgame.model;

/**
 * Anything that can be damaged, healed and targeted by a card.
 *
 * <p>Both {@link Player} (a hero) and {@link Creature} implement this interface, which lets
 * spells such as a fireball treat heroes and creatures uniformly. It is the main example of
 * polymorphism in the model: the engine only ever talks to a {@code GameCharacter}.</p>
 */
public interface GameCharacter {

    /**
     * @return the name shown in the log, for example {@code "Alice"} or {@code "Chillwind Yeti"}
     */
    String getDisplayName();

    /**
     * @return the current health, which may be zero or negative once the character is dead
     */
    int getHealth();

    /**
     * @return the maximum health this character can be healed up to
     */
    int getMaxHealth();

    /**
     * Applies raw damage. Death handling is deliberately <em>not</em> done here: the engine
     * is responsible for removing dead creatures and for ending the game.
     *
     * @param amount positive amount of damage
     * @throws IllegalArgumentException if {@code amount} is negative
     */
    void takeDamage(int amount);

    /**
     * Restores health, never above {@link #getMaxHealth()}.
     *
     * @param amount positive amount of health to restore
     * @throws IllegalArgumentException if {@code amount} is negative
     */
    void heal(int amount);

    /**
     * @return {@code true} when the character has no health left
     */
    boolean isDead();
}
