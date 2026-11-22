package soe402.cardgame.engine;

/**
 * The outcome of a player action.
 *
 * <p>The engine never throws for a rule violation. Rejected actions come back as a result with
 * a reason, which the user interface can show to the player and which tests can assert on.
 * Exceptions are reserved for programming errors such as invalid constructor arguments.</p>
 *
 * @param success whether the action was carried out
 * @param reason  why it was rejected, empty when it succeeded
 */
public record ActionResult(boolean success, String reason) {

    public ActionResult {
        if (reason == null) {
            reason = "";
        }
    }

    /**
     * @return a result describing a successfully executed action
     */
    public static ActionResult ok() {
        return new ActionResult(true, "");
    }

    /**
     * @param reason a short, user facing explanation
     * @return a result describing a rejected action
     */
    public static ActionResult deny(String reason) {
        return new ActionResult(false, reason);
    }

    /**
     * @return {@code true} when the action was rejected
     */
    public boolean isDenied() {
        return !success;
    }
}
