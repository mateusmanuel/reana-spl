package paramwrapper;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.junit.Assert;

import java.util.Map.Entry;

import fdtmc.FDTMC;
import fdtmc.State;
import fdtmc.Transition;

class Command {
	private int initialState;
	private List<String> updatesProbabilities;
	private List<Integer> updatesActions;
	

	public Command(int initialState) {
		this.initialState = initialState;
        this.updatesProbabilities = new LinkedList<String>();
        this.updatesActions = new LinkedList<Integer>();
	}
	
	public int getInitialState() {
		return initialState;
	}

	public void setInitialState(int initialState) {
		this.initialState = initialState;
	}
	
	public Collection<String> getUpdatesProbabilities() {
		return updatesProbabilities;
	}
	
	public List<Integer> getUpdatesActions() {
		return updatesActions;
	}
	
	@SuppressWarnings("rawtypes")
	public void initializeUpdateActions(List<Integer> actions) {
		if(!updatesActions.isEmpty()){
			Iterator iterator = actions.iterator();
			while (iterator.hasNext()) {
				addUpdatesActions((Integer)iterator.next());
			}
		}
	}
	
	public void addUpdatesActions(Integer action) {
		updatesActions.add(action);
	}
	public void removeCourse (Integer action) {
		updatesActions.remove(action);
	}
	
	@SuppressWarnings("rawtypes")
	public void initializeUpdatesProbabilities(List<String> probabilities) {
		if(!updatesProbabilities.isEmpty()){
			Iterator iterator = probabilities.iterator();
			while (iterator.hasNext()) {
				addUpdatesProbabilities((String)iterator.next());
			}
		}
	}
	
	public void addUpdatesProbabilities(String probability) {
		updatesProbabilities.add(probability);
	}
	public void removeProbabilities (String probability) {
		updatesProbabilities.remove(probability);
	}
	
	public Map<Integer, Command> getCommands(FDTMC fdtmc) {
		Map<Integer, Command> tmpCommands = new TreeMap<Integer, Command>();
		
		for (Entry<State, List<Transition>> entry : fdtmc.getTransitions().entrySet()) {
		    int initState = entry.getKey().getIndex();
			Command command = createCommand(initState, entry.getValue());
			
			tmpCommands.put(initState, command);
		}
		
		return tmpCommands;
	}
	
	private Command createCommand(int initState, List<Transition> transitions){
		Command command = new Command(initState);
		
		if (transitions != null) {
		    for (Transition transition : transitions) {
		        command.addUpdate(transition.getProbability(),
		                          transition.getTarget().getIndex());
		    }
		} else {
		    // Workaround: manually adding self-loops in case no
		    // transition was specified for a given state.
		    command.addUpdate("1", initState);
		}
		
		return command;
	}

	public void addUpdate(String probability, int update) {
		getUpdatesProbabilities().add(probability);
		getUpdatesActions().add(update);
	}

	public String initializeCommand(String stateVariable) {
		return "[] " + stateVariable + "=" + getInitialState() + " -> ";
	}

	public boolean needsPlus(int index) {
		return index != 0;
	}

	public String addProbabilitiesAndActionsToCommand(String command, String stateVariable) {
		String newCommand = command;
		Collection<String> updatesProbabilities = getUpdatesProbabilities();

		for (int i = 0; i < updatesProbabilities.size(); i++) {
		    if (needsPlus(i)) {
		        newCommand += " + ";
		    }

			newCommand += "(" + ((List<String>) updatesProbabilities).get(i) + ") : (" + stateVariable + "'=" + updatesActions.get(i) + ")";
		}

		return newCommand + ";";
	}

	public String makeString(String stateVariable) {
		String command = initializeCommand(stateVariable);

		command = addProbabilitiesAndActionsToCommand(command, stateVariable);

		return command;
	}
}