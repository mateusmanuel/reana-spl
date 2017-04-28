package paramwrapper;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import fdtmc.FDTMC;
import fdtmc.State;
import fdtmc.Transition;



class ParamModel {
	private String stateVariable = "s";

	// TODO Deixar nome do módulo PARAM configurável.
	private String moduleName = "dummyModule";
	
	// TODO Inferir estado inicial a partir da topologia da FDTMC.
	private int initialState = 0;

	private Set<String> parameters;
	private Map<String, Set<Integer>> labels;
	private Map<Integer, Command> commands;

	private int stateRangeStart;
	private int stateRangeEnd;

	public ParamModel(FDTMC fdtmc) {
		if (fdtmc.getVariableName() != null) {
			stateVariable = fdtmc.getVariableName();
		}
		
		initialState = fdtmc.getInitialState().getIndex();
		commands = getCommands(fdtmc);
		labels = getLabels(fdtmc);
		
		stateRangeStart = Collections.min(commands.keySet());
		// PARAM não deixa declarar um intervalo com apenas um número.
		
		stateRangeEnd = Math.max(stateRangeStart + 1,
								 Collections.max(commands.keySet()));
		
		parameters = getParameters(commands.values());
	}
    public int getParametersNumber() {
        return parameters.size();
    }

    public Set<String> getParameters() {
        return parameters;
    }

	public int getStatesNumber() {
	    return stateRangeEnd + 1;
	}

	private Map<String, Set<Integer>> getLabels(FDTMC fdtmc) {
		Map<String, Set<Integer>> labeledStates = new TreeMap<String, Set<Integer>>();
		
		for (State s : fdtmc.getStates()) {
			labeledStates = addState(labeledStates, s);
		}
		
		return labeledStates;
	}

	
	private Map<String, Set<Integer>> addState(Map<String, Set<Integer>> labeledStates, State s){
		String label = s.getLabel();
		
		if (label != null && !label.isEmpty()) {
			if (!labeledStates.containsKey(label)) {
				labeledStates.put(label, new TreeSet<Integer>());
			}
			
			labeledStates.get(label).add(s.getIndex());
		}
		
		return labeledStates;
	}
	
	private Map<Integer, Command> getCommands(FDTMC fdtmc) {
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

	private Set<String> getParameters(Collection<Command> commands) {
		Set<String> tmpParameters = new HashSet<String>();

		Pattern validIdentifier = Pattern.compile("(^|\\d+-)([A-Za-z_][A-Za-z0-9_]*)");
		for (Command command : commands) {
			for (String probability : command.getUpdatesProbabilities()) {
				Matcher m = validIdentifier.matcher(probability);
				while (m.find()) {
					tmpParameters.add(m.group(2));
				}
			}
		}
		return tmpParameters;
	}

	private String initializeModule (String params){
		String module =
				"dtmc\n" +
				"\n" +
				params +
				"\n" +
				"module " + moduleName + "\n" +
				"	" + stateVariable + " : [" + stateRangeStart + ".." + stateRangeEnd + "] init " + initialState + ";" +
				"\n";
		return module;
	}

	private String addCommandsInModule(String module){
		String moduleWithCommands = module;
		
		for (Command command : commands.values()) {
			moduleWithCommands += "	"+command.makeString(stateVariable) + "\n";
		}
		moduleWithCommands += "endmodule\n\n";

		return moduleWithCommands;
	}

	private String addLabelsAndStatesInModule (String module){
		String moduleWithLabelsAndStates = module;
		
		for (Map.Entry<String, Set<Integer>> entry : labels.entrySet()) {
			String label = entry.getKey();
			moduleWithLabelsAndStates += "label \"" + label + "\" = ";

			Set<Integer> states = entry.getValue();
			moduleWithLabelsAndStates = addStatesInModule(moduleWithLabelsAndStates, states);

			moduleWithLabelsAndStates += ";\n";
		}
		
		return moduleWithLabelsAndStates;
	}

	private String addStatesInModule (String module, Set<Integer> states){
		int count = 1;
		
		String moduleWithStates = module;
		
		for (Integer state : states) {
			moduleWithStates += stateVariable + "=" + state;
			if (count < states.size()) {
				moduleWithStates += " | ";
			}
			count++;
		}
		return moduleWithStates;
	}

	private String createParametersString (){
		String params = "";
		
		for (String parameter : parameters) {
			params += "param double " + parameter + ";\n";
		}
		
		return params;
	}

	@Override
	public String toString() {
		String params, initialModule, moduleWithCommands, moduleWithLabelsCommandsandStates;

		params = createParametersString();

		initialModule = initializeModule(params);

		moduleWithCommands = addCommandsInModule(initialModule);

		moduleWithLabelsCommandsandStates = addLabelsAndStatesInModule(moduleWithCommands);

		return moduleWithLabelsCommandsandStates;
	}
}

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