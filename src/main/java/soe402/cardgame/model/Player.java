package soe402.cardgame.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * One of the two players: a hero with health and mana, a deck, a hand, a board and a discard
 * pile.
 *
 * <p>The player owns its zones but does not decide the rules. Drawing, for example, is
 * {@link #drawOne()} plus engine logic, because the hand limit and fatigue damage are game
 * rules rather than data. Mutating methods are public because the engine lives in another
 * package; they are documented as engine facing and are not meant to be called by cards, which
 * only see {@link GameContext}.</p>
 */
public class Player implements GameCharacter {

    private final String name;
    private final int maxHealth;
    private final Deck deck;
    private final List<Card> hand = new ArrayList<>();
    private final List<Creature> board = new ArrayList<>();
    private final List<Card> discardPile = new ArrayList<>();

    private int health;
    private int mana;
    private int maxMana;
    private int fatigueCounter;

    /**
     * @param name the player's name, used in the log
     * @param deck the deck this player draws from
     */
    public Player(String name, Deck deck) {
        this(name, deck, 30);
    }

    /**
     * @param name           the player's name
     * @param deck           the deck this player draws from
     * @param startingHealth starting and maximum health
     */
    public Player(String name, Deck deck, int startingHealth) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("A player must have a non-blank name");
        }
        if (startingHealth < 1) {
            throw new IllegalArgumentException("Starting health must be positive");
        }
        this.name = name;
        this.deck = Objects.requireNonNull(deck, "deck must not be null");
        this.maxHealth = startingHealth;
        this.health = startingHealth;
    }

    // ---------------------------------------------------------------- turn resources

    /**
     * Refills the mana pool at the start of this player's turn. The pool grows by one crystal
     * per turn up to {@code cap}, and is then refilled to that maximum.
     *
     * @param cap the maximum number of mana crystals, normally ten
     */
    public void startTurnMana(int cap) {
        maxMana = Math.min(cap, maxMana + 1);
        mana = maxMana;
    }

    /**
     * @param cost mana cost of the card being played
     * @return {@code true} if this player can pay it
     */
    public boolean canAfford(int cost) {
        return mana >= cost;
    }

    /**
     * @param cost mana cost to pay
     * @throws IllegalStateException if the player does not have enough mana
     */
    public void spendMana(int cost) {
        if (cost > mana) {
            throw new IllegalStateException(
                    name + " cannot pay " + cost + " mana, only " + mana + " available");
        }
        mana -= cost;
    }

    public int getMana() {
        return mana;
    }

    public int getMaxMana() {
        return maxMana;
    }

    // ---------------------------------------------------------------- zones

    /**
     * @return the deck this player draws from
     */
    public Deck getDeck() {
        return deck;
    }

    /**
     * Draws the top card of the deck without applying any rule.
     *
     * @return the drawn card, or empty when the deck is exhausted
     */
    public Optional<Card> drawOne() {
        return deck.draw();
    }

    /**
     * @return an immutable snapshot of the hand, in hand order
     */
    public List<Card> getHand() {
        return List.copyOf(hand);
    }

    /**
     * Engine facing: puts a card into the hand.
     *
     * @param card the card to add
     */
    public void addToHand(Card card) {
        hand.add(Objects.requireNonNull(card));
    }

    /**
     * Engine facing: removes and returns the card at the given hand index.
     *
     * @param index zero based hand index
     * @return the removed card
     * @throws IndexOutOfBoundsException if the index is not a valid hand slot
     */
    public Card removeFromHand(int index) {
        return hand.remove(index);
    }

    /**
     * @return an immutable snapshot of the creatures on this player's board
     */
    public List<Creature> getBoard() {
        return List.copyOf(board);
    }

    /**
     * Engine facing: adds a creature to the board.
     *
     * @param creature the creature to summon
     */
    public void addToBoard(Creature creature) {
        board.add(Objects.requireNonNull(creature));
    }

    /**
     * Engine facing: removes a creature from the board, typically because it died.
     *
     * @param creature the creature to remove
     * @return {@code true} if it was on the board
     */
    public boolean removeFromBoard(Creature creature) {
        return board.remove(creature);
    }

    /**
     * @return an immutable snapshot of the discard pile
     */
    public List<Card> getDiscardPile() {
        return List.copyOf(discardPile);
    }

    /**
     * Engine facing: puts a card into the discard pile.
     *
     * @param card the card to discard
     */
    public void addToDiscard(Card card) {
        discardPile.add(Objects.requireNonNull(card));
    }

    // ---------------------------------------------------------------- fatigue

    /**
     * @return how many times this player has drawn from an empty deck
     */
    public int getFatigueCounter() {
        return fatigueCounter;
    }

    /**
     * Increases the fatigue counter by one and returns the new value, which is also the amount
     * of fatigue damage the next empty draw deals.
     *
     * @return the new fatigue counter
     */
    public int increaseFatigue() {
        return ++fatigueCounter;
    }

    // ---------------------------------------------------------------- hero state

    @Override
    public String getDisplayName() {
        return name;
    }

    public String getName() {
        return name;
    }

    @Override
    public int getHealth() {
        return health;
    }

    @Override
    public int getMaxHealth() {
        return maxHealth;
    }

    @Override
    public void takeDamage(int amount) {
        if (amount < 0) {
            throw new IllegalArgumentException("Damage must not be negative, but was " + amount);
        }
        health -= amount;
    }

    @Override
    public void heal(int amount) {
        if (amount < 0) {
            throw new IllegalArgumentException("Healing must not be negative, but was " + amount);
        }
        health = Math.min(maxHealth, health + amount);
    }

    @Override
    public boolean isDead() {
        return health <= 0;
    }

    @Override
    public String toString() {
        return name + " (" + health + " hp, " + mana + "/" + maxMana + " mana, "
                + hand.size() + " cards in hand)";
    }
}
