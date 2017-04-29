package paramwrapper;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

class Command {
	private int initialState;
	private List<String> updatesProbabilities;
	private List<Integer> updatesActions;

	public Command(int initialState) {
		this.initialState = initialState;
        this.updatesProbabilities = new LinkedList<String>();
        this.updatesActions = new LinkedList<Integer>();
	}

	public void addUpdate(String probability, int update) {
		updatesProbabilities.add(probability);
		updatesActions.add(update);
	}

	public Collection<String> getUpdatesProbabilities() {
		return updatesProbabilities;
	}

	public String initializeCommand(String stateVariable) {
		return "[] " + stateVariable + "=" + initialState + " -> ";
	}

	public boolean needsPlus(int index) {
		return index != 0;
	}

	public String addProbabilitiesAndActionsToCommand(String command, String stateVariable) {
		for (int i = 0; i < updatesProbabilities.size(); i++) {
		    if (needsPlus(i)) {
		        command += " + ";
		    }

			command += "(" + updatesProbabilities.get(i) + ") : (" + stateVariable + "'=" + updatesActions.get(i) + ")";
		}

		return command + ";";
	}

	public String makeString(String stateVariable) {
		String command = initializeCommand(stateVariable);

		command = addProbabilitiesAndActionsToCommand(command, stateVariable);

		return command;
	}
}