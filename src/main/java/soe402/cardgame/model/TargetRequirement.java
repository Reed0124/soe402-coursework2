package soe402.cardgame.model;

/**
 * Describes which targets are legal for a card or an {@code Effect}.
 *
 * <p>Making the requirement data rather than a hand written {@code if} in every card lets one
 * small, well tested method ({@link Targets#isValid(TargetRequirement, Player, GameCharacter)})
 * decide target validity for the whole system.</p>
 */
public enum TargetRequirement {

    /** The card needs no target at all. */
    NONE,

    /** Any hero or creature, friendly or not. */
    ANY_CHARACTER,

    /** A hero or creature controlled by the opponent. */
    ENEMY_CHARACTER,

    /** A hero or creature controlled by the caster. */
    FRIENDLY_CHARACTER,

    /** Any creature currently on a board. */
    ANY_CREATURE,

    /** A creature controlled by the opponent. */
    ENEMY_CREATURE,

    /** A creature controlled by the caster. */
    FRIENDLY_CREATURE
}
