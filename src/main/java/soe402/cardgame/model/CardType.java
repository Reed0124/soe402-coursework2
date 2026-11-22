package soe402.cardgame.model;

/**
 * The category a {@link Card} belongs to.
 *
 * <p>The type drives how the engine moves a card after it has been played:
 * creatures stay on the board, spells and modifiers are placed in the discard pile.</p>
 */
public enum CardType {

    /** A card that is summoned onto the board and can attack. */
    CREATURE("Creature"),

    /** A card with a one-off effect that is resolved immediately. */
    SPELL("Spell"),

    /** A card that attaches a timed stat change to a creature. */
    MODIFIER("Modifier");

    private final String label;

    CardType(String label) {
        this.label = label;
    }

    /**
     * @return a human readable name of the type, used by the game log and the demo UI
     */
    public String getLabel() {
        return label;
    }
}
