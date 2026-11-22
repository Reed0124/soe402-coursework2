package soe402.cardgame.model;

/**
 * Small helper that turns a {@link TargetRequirement} into a yes/no answer.
 *
 * <p>This class is intentionally stateless and has no dependencies, which makes it trivial to
 * unit test and safe to call from any card. Ownership is decided by the creature's
 * {@link Creature#getOwner()} reference; a hero is friendly only when it is the caster.</p>
 */
public final class Targets {

    private Targets() {
    }

    /**
     * @param caster the player playing the card
     * @param target the character to test
     * @return {@code true} if {@code target} is controlled by {@code caster}
     */
    public static boolean isFriendly(Player caster, GameCharacter target) {
        if (target instanceof Player hero) {
            return hero == caster;
        }
        if (target instanceof Creature creature) {
            return creature.getOwner() == caster;
        }
        return false;
    }

    /**
     * @param requirement what the card is allowed to target
     * @param caster      the player playing the card
     * @param target      the proposed target, possibly {@code null}
     * @return {@code true} if the target satisfies the requirement
     */
    public static boolean isValid(TargetRequirement requirement, Player caster, GameCharacter target) {
        if (requirement == TargetRequirement.NONE) {
            return true;
        }
        if (target == null) {
            return false;
        }
        return switch (requirement) {
            case NONE -> true;
            case ANY_CHARACTER -> true;
            case ENEMY_CHARACTER -> !isFriendly(caster, target);
            case FRIENDLY_CHARACTER -> isFriendly(caster, target);
            case ANY_CREATURE -> target instanceof Creature;
            case ENEMY_CREATURE -> target instanceof Creature && !isFriendly(caster, target);
            case FRIENDLY_CREATURE -> target instanceof Creature && isFriendly(caster, target);
        };
    }
}
