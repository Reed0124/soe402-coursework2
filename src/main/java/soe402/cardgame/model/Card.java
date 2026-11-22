package soe402.cardgame.model;

import java.util.Objects;

/**
 * Common superclass of every card in the game.
 *
 * <p>A {@code Card} is an immutable <em>definition</em>: its name, mana cost and type never
 * change once it has been created. Everything that changes during a game (current health,
 * whether a creature has attacked, ...) lives in the subclasses that represent a card
 * <em>in play</em>. Each physical copy in a deck is a separate object, so two copies of the
 * same card never share mutable state.</p>
 *
 * <p>Subclasses implement {@link #play(GameContext, Player, GameCharacter)} to define what
 * happens when the card is played. The engine never switches on the concrete card class;
 * it simply calls {@code play}, which is the polymorphic entry point of the whole model.</p>
 *
 * @see Creature
 * @see Spell
 * @see Modifier
 */
public abstract class Card {

    private final String name;
    private final int manaCost;
    private final CardType type;

    /**
     * @param name     unique, non-blank display name of the card
     * @param manaCost non-negative mana cost
     * @param type     category of the card
     * @throws IllegalArgumentException if the name is blank or the cost is negative
     * @throws NullPointerException     if {@code type} is {@code null}
     */
    protected Card(String name, int manaCost, CardType type) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("A card must have a non-blank name");
        }
        if (manaCost < 0) {
            throw new IllegalArgumentException("Mana cost must not be negative, but was " + manaCost);
        }
        this.name = name;
        this.manaCost = manaCost;
        this.type = Objects.requireNonNull(type, "type must not be null");
    }

    public String getName() {
        return name;
    }

    public int getManaCost() {
        return manaCost;
    }

    public CardType getType() {
        return type;
    }

    /**
     * Resolves the effect of this card. The engine has already validated the action and
     * removed the card from the hand before this method is called.
     *
     * @param context    controlled access to the game state, given to the card by the engine
     * @param controller the player who played the card
     * @param target     the chosen target, or {@code null} if the card needs no target
     */
    public abstract void play(GameContext context, Player controller, GameCharacter target);

    /**
     * @param controller the player who is trying to play the card
     * @param target     the proposed target, possibly {@code null}
     * @return {@code true} if {@code target} is legal for this card
     */
    public abstract boolean isValidTarget(Player controller, GameCharacter target);

    /**
     * Describes which targets this card accepts. A user interface can use it to highlight the
     * legal choices before the player clicks.
     *
     * @return the requirement, never {@code null}
     */
    public abstract TargetRequirement getTargetRequirement();

    /**
     * @return a one line description of what the card does, used in the log and in the demo
     */
    public abstract String describe();

    @Override
    public String toString() {
        return name + " (" + manaCost + " mana)";
    }
}
