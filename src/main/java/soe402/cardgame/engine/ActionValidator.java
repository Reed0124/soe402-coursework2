package soe402.cardgame.engine;

import java.util.List;
import java.util.Objects;

import soe402.cardgame.model.Card;
import soe402.cardgame.model.CardType;
import soe402.cardgame.model.Creature;
import soe402.cardgame.model.GameCharacter;
import soe402.cardgame.model.Player;
import soe402.cardgame.model.Targets;

/**
 * Checks whether an action is legal, without changing any state.
 *
 * <p>Pulling validation out of the engine keeps the engine focused on <em>doing</em> things and
 * makes the rules independently testable: every message below can be asserted on directly,
 * without starting a game. A rejected action leaves the game exactly as it was, which is the
 * property the test suite checks most often.</p>
 */
public final class ActionValidator {

    private final GameConfig config;

    /**
     * @param config the rules used for limits such as the board size
     */
    public ActionValidator(GameConfig config) {
        this.config = Objects.requireNonNull(config, "config must not be null");
    }

    /**
     * @param finished  whether the game is already over
     * @param phase     the current phase
     * @param player    the player who wants to play a card
     * @param opponent  the other player in this game
     * @param handIndex index of the card in that player's hand
     * @param target    the chosen target, possibly {@code null}
     * @return {@link ActionResult#ok()} when the card may be played
     */
    public ActionResult validatePlay(boolean finished, Phase phase, Player player, Player opponent,
                                     int handIndex, GameCharacter target) {
        if (finished) {
            return ActionResult.deny("The game is already over");
        }
        if (phase != Phase.MAIN) {
            return ActionResult.deny("Cards can only be played during the main phase");
        }
        List<Card> hand = player.getHand();
        if (handIndex < 0 || handIndex >= hand.size()) {
            return ActionResult.deny("There is no card at hand index " + handIndex);
        }
        Card card = hand.get(handIndex);
        if (!player.canAfford(card.getManaCost())) {
            return ActionResult.deny("Not enough mana: " + card.getName() + " costs "
                    + card.getManaCost() + ", " + player.getMana() + " available");
        }
        if (card.getType() == CardType.CREATURE && player.getBoard().size() >= config.boardLimit()) {
            return ActionResult.deny("The board is full (" + config.boardLimit() + " creatures)");
        }
        if (!belongsToGame(player, opponent, target)) {
            return ActionResult.deny("That target is not part of this game");
        }
        if (!card.isValidTarget(player, target)) {
            return ActionResult.deny("Invalid target for " + card.getName());
        }
        return ActionResult.ok();
    }

    /**
     * @param finished      whether the game is already over
     * @param phase         the current phase
     * @param player        the attacking player
     * @param opponent      the other player in this game
     * @param attackerIndex index of the attacker on that player's board
     * @param target        the character being attacked
     * @return {@link ActionResult#ok()} when the attack may be made
     */
    public ActionResult validateAttack(boolean finished, Phase phase, Player player, Player opponent,
                                       int attackerIndex, GameCharacter target) {
        if (finished) {
            return ActionResult.deny("The game is already over");
        }
        if (phase != Phase.MAIN) {
            return ActionResult.deny("Creatures can only attack during the main phase");
        }
        List<Creature> board = player.getBoard();
        if (attackerIndex < 0 || attackerIndex >= board.size()) {
            return ActionResult.deny("There is no creature at board index " + attackerIndex);
        }
        Creature attacker = board.get(attackerIndex);
        if (attacker.isDead()) {
            return ActionResult.deny(attacker.getDisplayName() + " is dead");
        }
        if (attacker.isFrozen()) {
            return ActionResult.deny(attacker.getDisplayName() + " is frozen and cannot attack");
        }
        if (attacker.hasAttackedThisTurn()) {
            return ActionResult.deny(attacker.getDisplayName() + " has already attacked this turn");
        }
        if (!attacker.canAttack()) {
            return ActionResult.deny(attacker.getDisplayName()
                    + " cannot attack on the turn it was played");
        }
        if (target == null) {
            return ActionResult.deny("Choose a target to attack");
        }
        if (!belongsToGame(player, opponent, target)) {
            return ActionResult.deny("That target is not part of this game");
        }
        if (Targets.isFriendly(player, target)) {
            return ActionResult.deny("You cannot attack your own character");
        }
        return ActionResult.ok();
    }

    /**
     * Checks that a target really takes part in this game.
     *
     * <p>Ownership alone is not enough: a player or creature created elsewhere also has an
     * owner (or is a hero), so reference equality against the two players and membership of
     * their boards is what makes a target legal. Without this check an action could damage an
     * object that belongs to another game, or a creature that has already left the board.</p>
     *
     * @param player   the active player
     * @param opponent the other player in this game
     * @param target   the proposed target, possibly {@code null}
     * @return {@code true} if the target is one of the two heroes or a creature on a board
     */
    private static boolean belongsToGame(Player player, Player opponent, GameCharacter target) {
        if (target == null) {
            return true;
        }
        if (target == player || target == opponent) {
            return true;
        }
        if (target instanceof Creature creature) {
            return player.getBoard().contains(creature) || opponent.getBoard().contains(creature);
        }
        return false;
    }
}
