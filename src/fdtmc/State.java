package fdtmc;

public class State {

	private String variableName;
	private int index;
	private String label;

	public void setVariableName(String variableName) {
		this.variableName = variableName;
	}

	public void setIndex(int index) {
		this.index = index;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public int getIndex() {
		return this.index;
	}

	public String getVariableName() {
		return this.variableName;
	}

	public String getLabel() {
		return this.label;
	}

    /**
     * A state is equal to another one if they have equal indices.
     * Labels are only considered when comparing FDTMCs as a whole.
     */
    @Override
    public boolean equals(Object obj) {
        if (obj != null && obj instanceof State) {
            State other = (State) obj;
            return this.getIndex() == other.getIndex();
        }
        return false;
    }

    @Override
    public int hashCode() {
        return this.getIndex() + 1;
    }

}
