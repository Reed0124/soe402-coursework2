package soe402.cardgame.engine;

/**
 * Tunable rules of the game.
 *
 * <p>Every number from the specification lives here instead of being scattered as a magic
 * constant, which makes the rules easy to read, easy to change and easy to test.</p>
 *
 * @param startingHealth health each hero starts with
 * @param handLimit      maximum number of cards in hand
 * @param boardLimit     maximum number of creatures on a board
 * @param maxMana        mana ceiling
 * @param deckSize       number of cards in a constructed deck
 */
public record GameConfig(int startingHealth, int handLimit, int boardLimit, int maxMana, int deckSize) {

    public GameConfig {
        if (startingHealth < 1) {
            throw new IllegalArgumentException("Starting health must be positive");
        }
        if (handLimit < 1) {
            throw new IllegalArgumentException("Hand limit must be positive");
        }
        if (boardLimit < 1) {
            throw new IllegalArgumentException("Board limit must be positive");
        }
        if (maxMana < 1) {
            throw new IllegalArgumentException("Maximum mana must be positive");
        }
        if (deckSize < 1) {
            throw new IllegalArgumentException("Deck size must be positive");
        }
    }

    /**
     * @return the rules described in the coursework specification
     */
    public static GameConfig standard() {
        return new GameConfig(30, 7, 7, 10, 20);
    }
}
