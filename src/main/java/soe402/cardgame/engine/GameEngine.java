package soe402.cardgame.engine;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import soe402.cardgame.event.EventBus;
import soe402.cardgame.event.GameEvent;
import soe402.cardgame.log.GameLog;
import soe402.cardgame.model.Card;
import soe402.cardgame.model.CardType;
import soe402.cardgame.model.Creature;
import soe402.cardgame.model.GameCharacter;
import soe402.cardgame.model.GameContext;
import soe402.cardgame.model.Player;
import soe402.cardgame.model.StatMod;

/**
 * The central controller of the game.
 *
 * <p>The engine owns the turn structure and enforces the rules. It exposes three player
 * actions - {@link #playCard(int, GameCharacter)}, {@link #attack(int, GameCharacter)} and
 * {@link #endTurn()} - and each of them follows the same shape: ask the {@link ActionValidator},
 * mutate the state, publish the resulting {@link GameEvent}s. Because validation happens before
 * any mutation, a rejected action is guaranteed to leave the game untouched.</p>
 *
 * <p>The engine is also the single implementation of {@link GameContext}. Cards therefore get
 * access to exactly the operations they need - damage, healing, drawing, summoning - and never
 * to the turn machinery.</p>
 */
public final class GameEngine implements GameContext {

    private final GameConfig config;
    private final List<Player> players;
    private final EventBus events = new EventBus();
    private final GameLog log;
    private final TurnManager turns = new TurnManager();
    private final ActionValidator validator;

    private Player current;
    private boolean started;
    private boolean finished;
    private Player winner;

    /**
     * Creates an engine with the standard rules and a silent log.
     *
     * @param first  the player who takes the first turn
     * @param second the other player
     */
    public GameEngine(Player first, Player second) {
        this(first, second, GameConfig.standard());
    }

    /**
     * @param first  the player who takes the first turn
     * @param second the other player
     * @param config the rules to use
     */
    public GameEngine(Player first, Player second, GameConfig config) {
        this(first, second, config, new GameLog());
    }

    /**
     * @param first  the player who takes the first turn
     * @param second the other player
     * @param config the rules to use
     * @param log    the log that receives every event
     */
    public GameEngine(Player first, Player second, GameConfig config, GameLog log) {
        Objects.requireNonNull(first, "first player must not be null");
        Objects.requireNonNull(second, "second player must not be null");
        if (first == second) {
            throw new IllegalArgumentException("A game needs two different players");
        }
        this.config = Objects.requireNonNull(config, "config must not be null");
        this.log = Objects.requireNonNull(log, "log must not be null");
        this.players = List.of(first, second);
        this.validator = new ActionValidator(config);
        this.current = first;
        this.events.subscribe(log);
    }

    // ---------------------------------------------------------------- lifecycle

    /**
     * Starts the game and begins the first turn.
     *
     * @throws IllegalStateException if the game has already been started
     */
    public void startGame() {
        if (started) {
            throw new IllegalStateException("The game has already been started");
        }
        started = true;
        current = players.get(0);
        events.publish(new GameEvent.GameStarted(current.getName()));
        beginTurn(current);
    }

    private void beginTurn(Player player) {
        current = player;
        turns.beginTurn();
        player.startTurnMana(config.maxMana());
        for (Creature creature : player.getBoard()) {
            creature.beginTurn();
        }
        events.publish(new GameEvent.TurnStarted(player.getName(), turns.getTurnNumber()));
        log.log(player.getName() + " has " + player.getMana() + "/" + player.getMaxMana() + " mana");
        turns.advance();
        drawCards(player, 1);
        if (!finished) {
            turns.advance();
        }
    }

    // ---------------------------------------------------------------- player actions

    /**
     * Plays a card from the active player's hand.
     *
     * @param handIndex index of the card in hand order
     * @param target    the chosen target, or {@code null} if the card needs none
     * @return the outcome, including a reason when the play is rejected
     */
    public ActionResult playCard(int handIndex, GameCharacter target) {
        ActionResult check = validator.validatePlay(finished, turns.getPhase(), current,
                opponentOf(current), handIndex, target);
        if (check.isDenied()) {
            return check;
        }
        Card card = current.getHand().get(handIndex);
        current.removeFromHand(handIndex);
        current.spendMana(card.getManaCost());
        events.publish(new GameEvent.CardPlayed(current.getName(), card.getName()));
        card.play(this, current, target);
        if (card.getType() != CardType.CREATURE) {
            current.addToDiscard(card);
        }
        return ActionResult.ok();
    }

    /**
     * Attacks with one of the active player's creatures.
     *
     * <p>When a creature is attacked and survives, it strikes back with its own attack value.
     * A creature that dies to the attack does not retaliate, which matches the usual rule for
     * this family of games.</p>
     *
     * @param attackerIndex index of the attacker on the active player's board
     * @param target        the enemy hero or creature to attack
     * @return the outcome, including a reason when the attack is rejected
     */
    public ActionResult attack(int attackerIndex, GameCharacter target) {
        ActionResult check = validator.validateAttack(finished, turns.getPhase(), current,
                opponentOf(current), attackerIndex, target);
        if (check.isDenied()) {
            return check;
        }
        Creature attacker = current.getBoard().get(attackerIndex);
        attacker.markAttacked();
        events.publish(new GameEvent.AttackDeclared(attacker.getDisplayName(), target.getDisplayName()));
        dealDamage(target, attacker.getAttack(), attacker);
        if (target instanceof Creature defender && !defender.isDead()) {
            dealDamage(attacker, defender.getAttack(), defender);
        }
        return ActionResult.ok();
    }

    /**
     * Ends the active player's turn and starts the opponent's turn.
     *
     * @return the outcome, including a reason when the turn cannot be ended
     */
    public ActionResult endTurn() {
        if (finished) {
            return ActionResult.deny("The game is already over");
        }
        if (turns.getPhase() != Phase.MAIN) {
            return ActionResult.deny("The turn can only be ended during the main phase");
        }
        for (Creature creature : current.getBoard()) {
            creature.tickModifiers();
        }
        removeDeadCreatures();
        turns.endTurn();
        events.publish(new GameEvent.TurnEnded(current.getName()));
        beginTurn(opponentOf(current));
        return ActionResult.ok();
    }

    // ---------------------------------------------------------------- GameContext

    @Override
    public void dealDamage(GameCharacter target, int amount, GameCharacter source) {
        if (finished || target == null || amount <= 0 || target.isDead()) {
            return;
        }
        target.takeDamage(amount);
        String sourceName = source == null ? "Fatigue" : source.getDisplayName();
        events.publish(new GameEvent.DamageDealt(sourceName, target.getDisplayName(), amount,
                target.getHealth()));
        if (target instanceof Creature creature && creature.isDead()) {
            destroy(creature);
        }
        checkForWinner();
    }

    @Override
    public void heal(GameCharacter target, int amount) {
        if (finished || target == null || amount <= 0) {
            return;
        }
        int before = target.getHealth();
        target.heal(amount);
        int restored = target.getHealth() - before;
        if (restored > 0) {
            events.publish(new GameEvent.Healed(target.getDisplayName(), restored, target.getHealth()));
        }
    }

    @Override
    public void drawCards(Player player, int amount) {
        for (int i = 0; i < amount && !finished; i++) {
            Optional<Card> drawn = player.drawOne();
            if (drawn.isEmpty()) {
                int fatigue = player.increaseFatigue();
                dealDamage(player, fatigue, null);
                if (finished) {
                    return;
                }
                continue;
            }
            Card card = drawn.get();
            if (player.getHand().size() >= config.handLimit()) {
                player.addToDiscard(card);
                events.publish(new GameEvent.CardBurned(player.getName(), card.getName()));
            } else {
                player.addToHand(card);
                events.publish(new GameEvent.CardDrawn(player.getName(), card.getName()));
            }
        }
    }

    @Override
    public void summon(Player controller, Creature creature) {
        if (finished) {
            return;
        }
        controller.addToBoard(creature);
        log.log(controller.getName() + " summons " + creature.getName()
                + " (" + creature.getAttack() + "/" + creature.getHealth() + ")");
    }

    @Override
    public void attachModifier(Creature target, StatMod statMod, int duration) {
        if (finished) {
            return;
        }
        target.addModifier(statMod, duration);
        events.publish(new GameEvent.ModifierApplied(target.getDisplayName(), statMod.label(), duration));
        if (target.isDead()) {
            destroy(target);
        }
    }

    @Override
    public Player opponentOf(Player player) {
        return players.get(0) == player ? players.get(1) : players.get(0);
    }

    @Override
    public List<Creature> creaturesOwnedBy(Player player) {
        return player.getBoard();
    }

    @Override
    public boolean isFinished() {
        return finished;
    }

    @Override
    public void log(String message) {
        log.log(message);
    }

    /**
     * Removes creatures that died from a stat change rather than from damage.
     *
     * <p>Losing a health buff at the end of a turn can push a creature to zero health without
     * any damage being dealt, so the death check has to run after modifiers are ticked as well
     * as inside {@link #dealDamage}.</p>
     */
    private void removeDeadCreatures() {
        for (Player player : players) {
            for (Creature creature : player.getBoard()) {
                if (creature.isDead()) {
                    destroy(creature);
                }
            }
        }
    }

    private void destroy(Creature creature) {
        Player owner = creature.getOwner();
        if (owner != null && owner.removeFromBoard(creature)) {
            owner.addToDiscard(creature);
            events.publish(new GameEvent.CreatureDied(creature.getDisplayName()));
        }
    }

    private void checkForWinner() {
        if (finished) {
            return;
        }
        Optional<Player> dead = players.stream().filter(Player::isDead).findFirst();
        if (dead.isEmpty()) {
            return;
        }
        finished = true;
        winner = opponentOf(dead.get());
        events.publish(new GameEvent.GameEnded(winner.getName()));
    }

    // ---------------------------------------------------------------- accessors

    public GameConfig getConfig() {
        return config;
    }

    public Player getCurrentPlayer() {
        return current;
    }

    public Player getOpponent() {
        return opponentOf(current);
    }

    public List<Player> getPlayers() {
        return players;
    }

    public Phase getPhase() {
        return turns.getPhase();
    }

    public int getTurnNumber() {
        return turns.getTurnNumber();
    }

    /**
     * @return the winner, or empty while the game is still running
     */
    public Optional<Player> getWinner() {
        return Optional.ofNullable(winner);
    }

    public GameLog getLog() {
        return log;
    }
}
