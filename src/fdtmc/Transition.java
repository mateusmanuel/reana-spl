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

	private boolean notNullTransition(Object obj) {
		return obj != null && obj instanceof Transition;
	}

	private boolean areEqualSources(State source) {
		return this.getSource().equals(source);
	}

	private boolean areEqualTargets(State target) {
		return this.getTarget().equals(target);
	}

	public boolean areEqualTransitions(State source, State target, String probability) {
		return areEqualSources(source)
				&& areEqualTargets(target)
				&& areEqualProbabilities(probability);
	}

    /**
     * Two transitions are equal if they have equal source and target states.
     * Moreover, their transition probabilities must be equal numbers or
     * be both (not necessarily equal) variable names.
     */
    @Override
    public boolean equals(Object obj) {
        if (notNullTransition(obj)) {
            Transition other = (Transition) obj;
            return areEqualTransitions(other.getSource(), other.getTarget(), other.getProbability());
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
    private boolean areEqualProbabilities(String p2) {
    	String p1 = this.getProbability();

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
