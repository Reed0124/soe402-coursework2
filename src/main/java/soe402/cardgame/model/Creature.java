package soe402.cardgame.model;

import java.util.ArrayList;
import java.util.List;

/**
 * A creature card. Once played it stays on the board and can attack.
 *
 * <p>This class is where the "definition versus instance" split becomes concrete. The card's
 * name and mana cost are immutable (they come from {@link Card}), while its combat state -
 * accumulated damage, temporary stat changes, whether it has attacked - is mutable and
 * belongs to this particular object on this particular board.</p>
 *
 * <p>The creature keeps an accumulated {@code damage} counter instead of a mutable health
 * field. That makes healing and buffs easy to express: current health is always
 * {@code base + bonus - damage}, and a creature that gains maximum health keeps the damage it
 * has already taken.</p>
 */
public class Creature extends Card implements GameCharacter {

    private final int baseAttack;
    private final int baseHealth;
    private final List<StatMod.AppliedModifier> modifiers = new ArrayList<>();

    private Player owner;
    private int damage;
    private int frozenTurns;
    private boolean attackedThisTurn;
    private boolean canAttack;

    /**
     * @param name     display name of the creature
     * @param manaCost mana cost, zero or more
     * @param attack   base attack value, zero or more
     * @param health   base health value, one or more
     * @throws IllegalArgumentException if the health is not positive
     */
    public Creature(String name, int manaCost, int attack, int health) {
        super(name, manaCost, CardType.CREATURE);
        if (attack < 0) {
            throw new IllegalArgumentException("Attack must not be negative, but was " + attack);
        }
        if (health < 1) {
            throw new IllegalArgumentException("Health must be positive, but was " + health);
        }
        this.baseAttack = attack;
        this.baseHealth = health;
    }

    @Override
    public void play(GameContext context, Player controller, GameCharacter target) {
        this.owner = controller;
        this.attackedThisTurn = false;
        this.canAttack = false;
        context.summon(controller, this);
    }

    @Override
    public boolean isValidTarget(Player controller, GameCharacter target) {
        return true;
    }

    @Override
    public TargetRequirement getTargetRequirement() {
        return TargetRequirement.NONE;
    }

    @Override
    public String describe() {
        return baseAttack + "/" + baseHealth + " creature, " + getManaCost() + " mana";
    }

    /**
     * Called by the engine at the start of its controller's turn: clears the attack flag and
     * either removes one turn of freeze or allows the creature to attack.
     */
    public void beginTurn() {
        attackedThisTurn = false;
        if (frozenTurns > 0) {
            frozenTurns--;
            canAttack = false;
        } else {
            canAttack = true;
        }
    }

    /**
     * Prevents the creature from attacking for the given number of turns.
     *
     * @param turns number of turns, at least one
     */
    public void freeze(int turns) {
        if (turns < 1) {
            throw new IllegalArgumentException("Freeze duration must be at least one turn");
        }
        frozenTurns = Math.max(frozenTurns, turns);
        canAttack = false;
    }

    /**
     * @return {@code true} if the creature may attack right now
     */
    public boolean canAttack() {
        return canAttack && !attackedThisTurn && frozenTurns == 0 && !isDead();
    }

    /**
     * Marks the creature as having attacked this turn, so it cannot attack twice.
     */
    public void markAttacked() {
        attackedThisTurn = true;
    }

    /**
     * Applies a timed stat change.
     *
     * @param statMod  the change to apply
     * @param duration number of turns it lasts
     */
    public void addModifier(StatMod statMod, int duration) {
        modifiers.add(new StatMod.AppliedModifier(statMod, duration));
    }

    /**
     * Counts down every active modifier by one turn and drops the expired ones. Called by the
     * engine at the end of the controller's turn.
     */
    public void tickModifiers() {
        List<StatMod.AppliedModifier> remaining = new ArrayList<>();
        for (StatMod.AppliedModifier applied : modifiers) {
            if (applied.remainingTurns() > 1) {
                remaining.add(new StatMod.AppliedModifier(applied.stat(), applied.remainingTurns() - 1));
            }
        }
        modifiers.clear();
        modifiers.addAll(remaining);
    }

    /**
     * @return the attack bonus currently granted by modifiers
     */
    public int getAttackBonus() {
        return modifiers.stream().mapToInt(applied -> applied.stat().attackBonus()).sum();
    }

    /**
     * @return the health bonus currently granted by modifiers
     */
    public int getHealthBonus() {
        return modifiers.stream().mapToInt(applied -> applied.stat().healthBonus()).sum();
    }

    /**
     * @return the current attack value, including modifiers and never below zero
     */
    public int getAttack() {
        return Math.max(0, baseAttack + getAttackBonus());
    }

    @Override
    public int getHealth() {
        return baseHealth + getHealthBonus() - damage;
    }

    @Override
    public int getMaxHealth() {
        return baseHealth + getHealthBonus();
    }

    @Override
    public void takeDamage(int amount) {
        if (amount < 0) {
            throw new IllegalArgumentException("Damage must not be negative, but was " + amount);
        }
        damage += amount;
    }

    @Override
    public void heal(int amount) {
        if (amount < 0) {
            throw new IllegalArgumentException("Healing must not be negative, but was " + amount);
        }
        damage = Math.max(0, damage - amount);
    }

    @Override
    public boolean isDead() {
        return getHealth() <= 0;
    }

    public int getBaseAttack() {
        return baseAttack;
    }

    public int getBaseHealth() {
        return baseHealth;
    }

    public boolean isFrozen() {
        return frozenTurns > 0;
    }

    public boolean hasAttackedThisTurn() {
        return attackedThisTurn;
    }

    public Player getOwner() {
        return owner;
    }

    /**
     * @return an immutable snapshot of the modifiers currently attached to this creature
     */
    public List<StatMod.AppliedModifier> getModifiers() {
        return List.copyOf(modifiers);
    }

    @Override
    public String getDisplayName() {
        return getName();
    }

    @Override
    public String toString() {
        return getName() + " (" + getAttack() + "/" + getHealth() + ")";
    }
}
