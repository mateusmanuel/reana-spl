package fdtmc;

/**
 * Represents an abstracted away FDTMC fragment.
 *
 * @author thiago
 */
public class Interface {
    private String abstractedId;
    private State initial;
    private State success;
    private State error;
    private Transition successTransition;
    private Transition errorTransition;

    public Interface(String abstractedId, State initial, State success, State error, Transition successTransition, Transition errorTransition) {
        this.setAbstractedId(abstractedId);
        this.setInitial(initial);
        this.setSuccess(success);
        this.setError(error);
        this.setSuccessTransition(successTransition);
        this.setErrorTransition(errorTransition);
    }

    public State getInitial() {
        return initial;
    }

    public void setInitial(State initial) {
    	this.initial = initial;
    }

    public State getSuccess() {
        return success;
    }

    public void setSuccess(State success) {
    	this.success = success;
    }

    public State getError() {
        return error;
    }

    public void setError(State error) {
    	this.error = error;
    }

    public Transition getSuccessTransition() {
        return successTransition;
    }

    public void setSuccessTransition(Transition successTransition) {
    	this.successTransition = successTransition;
    }

    public Transition getErrorTransition() {
        return errorTransition;
    }

    public void setErrorTransition(Transition errorTransition) {
    	this.errorTransition = errorTransition;
    }

    public String getAbstractedId() {
        return abstractedId;
    }

    public void setAbstractedId(String abstractedId) {
    	this.abstractedId = abstractedId;
    }

    private boolean notNullInterface(Object obj) {
    	return obj != null && obj instanceof Interface;
    }

    private boolean areEqualInterfaces(Interface other) {
    	return this.getInitial().equals(other.getInitial())
                && this.getSuccess().equals(other.getSuccess())
                && this.getError().equals(other.getError())
                && this.getSuccessTransition().equals(other.getSuccessTransition())
                && this.getErrorTransition().equals(other.getErrorTransition());
    }

    /**
     * Interfaces are compared for equality disregarding the abstracted id.
     */
    @Override
    public boolean equals(Object obj) {
        if (notNullInterface(obj)) {
            Interface other = (Interface) obj;

            return areEqualInterfaces(other);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return this.getInitial().hashCode()
                + this.getSuccess().hashCode()
                + this.getError().hashCode()
                + this.getSuccessTransition().hashCode()
                + this.getErrorTransition().hashCode();
    }

}
