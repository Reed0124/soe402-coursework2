package soe402.cardgame.model;

/**
 * An immutable stat change that a {@link Modifier} card attaches to a creature.
 *
 * <p>Kept as a record because it is a pure value: two {@code StatMod}s with the same numbers
 * are interchangeable. The number of turns it lasts is stored separately by the creature
 * that owns it, see {@link AppliedModifier}.</p>
 *
 * @param attackBonus attack added while the modifier is active, may be negative
 * @param healthBonus maximum health added while the modifier is active, may be negative
 * @param label       short text used in the log, for example {@code "+3 attack"}
 */
public record StatMod(int attackBonus, int healthBonus, String label) {

    public StatMod {
        if (attackBonus == 0 && healthBonus == 0) {
            throw new IllegalArgumentException("A stat modifier must change at least one stat");
        }
        if (label == null || label.isBlank()) {
            throw new IllegalArgumentException("A stat modifier must have a label");
        }
    }

    /**
     * A modifier together with the number of turns it is still active for.
     *
     * @param stat           the stat change
     * @param remainingTurns turns left, counted at the end of the controller's turn
     */
    public record AppliedModifier(StatMod stat, int remainingTurns) {

        public AppliedModifier {
            if (remainingTurns < 1) {
                throw new IllegalArgumentException("A modifier must last at least one turn");
            }
        }
    }
}
