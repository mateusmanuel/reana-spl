package fdtmc;

public class Transition {

	private String actionName;
	private String probability;
	private State source, target;

	public Transition(State source, State target, String actionName, String probability) {
		this.setSource(source);
		this.setTarget(target);
		this.setActionName(actionName);
		this.setProbability(probability);
	}

	public String getActionName() {
		return actionName;
	}

	public void setActionName(String actionName) {
		this.actionName = actionName;
	}	
	
	public String getProbability() {
		return probability;
	}

	public void setProbability(String probability) {
		this.probability = probability;
	}
	
	public State getSource() {
		return source;
	}
	
	public void setSource(State source) {
		this.source = source;
	}

	public State getTarget() {
		return target;
	}
	
	public void setTarget(State target) {
		this.target = target;
	}

    /**
     * Two transitions are equal if they have equal source and target states.
     * Moreover, their transition probabilities must be equal numbers or
     * be both (not necessarily equal) variable names.
     */
    @Override
    public boolean equals(Object obj) {
        if (obj != null && obj instanceof Transition) {
            Transition other = (Transition) obj;
            return this.getSource().equals(other.getSource())
                    && getTarget().equals(other.getTarget())
                    && areEqualProbabilities(this.getProbability(), other.getProbability());
        }
        return false;
    }

    @Override
    public int hashCode() {
        return this.getSource().hashCode() + this.getTarget().hashCode();
    }

    /**
     * Returns true if {@code p1} and {@code p2} are equal double values.
     * If they contain variable names, even different ones, the result is also true.
     * @param p1
     * @param p2
     * @return
     */
    private boolean areEqualProbabilities(String p1, String p2) {
        double prob1 = 0;
        double prob2 = 0;
        boolean isVariable = false;
        try {
            prob1 = Double.parseDouble(p1);
        } catch (NumberFormatException e) {
            isVariable = true;
        }
        try {
            prob2 = Double.parseDouble(p2);
        } catch (NumberFormatException e) {
            if (isVariable) {
                return true;
            }
        }
        return prob1 == prob2;
    }
}
