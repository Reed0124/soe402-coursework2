package soe402.cardgame.model;

/**
 * The behaviour of a spell, expressed as a replaceable object rather than as a subclass of
 * {@link Spell}.
 *
 * <p>This is the Strategy pattern, and it is the single most important design decision in the
 * project. Adding a new spell means writing one {@code Effect} class and registering one card;
 * the engine, the validator and the log stay untouched, which is the open/closed principle in
 * practice. It also avoids the subclass explosion that "one class per card" would cause once
 * the card pool grows.</p>
 *
 * <p>Implementations live in {@code soe402.cardgame.effect}.</p>
 */
public interface Effect {

    /**
     * @return which targets this effect accepts
     */
    TargetRequirement getTargetRequirement();

    /**
     * Applies the effect. The engine has already validated the target.
     *
     * @param context the game context used to mutate the game
     * @param caster  the player who controls the effect
     * @param target  the chosen target, or {@code null} when none is required
     */
    void resolve(GameContext context, Player caster, GameCharacter target);

    /**
     * @return a short description such as {@code "deals 6 damage"}
     */
    String describe();

    /**
     * @param caster the player who is playing the spell
     * @param target the proposed target
     * @return {@code true} if the target is acceptable for this effect
     */
    default boolean isValidTarget(Player caster, GameCharacter target) {
        return Targets.isValid(getTargetRequirement(), caster, target);
    }
}
