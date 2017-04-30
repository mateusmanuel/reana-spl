package paramwrapper;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import fdtmc.FDTMC;
import fdtmc.State;



class ParamModel {
	private String stateVariable = "s";
	
	private final String REGEX_IDENTIFIER = "(^|\\d+-)([A-Za-z_][A-Za-z0-9_]*)";
	private final String DTMC = "dtmc";
	private final String MODULE = "module";
	private final String PARAM_DOUBLE = "param double";
	private final String ENDMODULE = "endmodule";
	private final String LABEL = "label";
	private final String ENDL = "\n";
	
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
		Command command = new Command(getInitialState());
		
		if (fdtmc.getVariableName() != null) {
			setStateVariable(fdtmc.getVariableName());
		}
		
		setInitialState(fdtmc.getInitialState().getIndex());
		setCommands(command.getCommands(fdtmc));
		setLabels(getLabels(fdtmc));
		
		setStateRangeStart(Collections.min(getCommands().keySet()));
		// PARAM não deixa declarar um intervalo com apenas um número.
		
		setStateRangeEnd(Math.max(getStateRangeStart() + 1,
								 Collections.max(getCommands().keySet())));
		
		setParameters(getParametersByCommands(getCommands().values()));
	}
	
    public int getParametersNumber() {
        return getParameters().size();
    }

	public int getStatesNumber() {
	    return getStateRangeEnd() + 1;
	}
	
	public String getStateVariable() {
		return stateVariable;
	}
	
	private void setStateVariable(String stateVariable) {
		this.stateVariable = stateVariable;
	}

	public Set<String> getParameters(){
		return parameters;
	}
	
	private void setParameters(Set<String> parameters){
		this.parameters = parameters;
	}
	
	public Map<String, Set<Integer>> getLabels(){
		return labels;
	}
	
	private void setLabels(Map<String, Set<Integer>> labels){
		this.labels = labels;
	}
	
	public String getModuleName() {
		return moduleName;
	}
	
	public int getInitialState() {
		return initialState;
	}
	
	private void setInitialState(int initialState) {
		this.initialState = initialState;
	}
	
	public int getStateRangeStart() {
		return stateRangeStart;
	}
	
	public void setStateRangeStart(int stateRangeStart) {
		this.stateRangeStart = stateRangeStart;
	}
	
	public int getStateRangeEnd() {
		return stateRangeEnd;
	}
	
	public void setStateRangeEnd(int stateRangeEnd) {
		this.stateRangeEnd = stateRangeEnd;
	}
	
	public Map<Integer, Command> getCommands() {
		return commands;
	}

	public void setCommands(Map<Integer, Command> commands) {
		this.commands = commands;
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
		Map<String, Set<Integer>> _labeledStates = labeledStates;
		
		if (label != null && !label.isEmpty()) {
			if (!_labeledStates.containsKey(label)) {
				_labeledStates.put(label, new TreeSet<Integer>());
			}
			
			_labeledStates.get(label).add(s.getIndex());
		}
		
		return _labeledStates;
	}
	
	private Set<String> getParametersByCommands(Collection<Command> commands) {
		Set<String> tmpParameters = new HashSet<String>();

		Pattern validIdentifier = Pattern.compile(REGEX_IDENTIFIER);
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
				DTMC + ENDL +
				ENDL +
				params +
				ENDL +
				MODULE + " " + getModuleName() + ENDL +
				"	" + getStateVariable() + " : [" + getStateRangeStart() + ".." + getStateRangeEnd() + "] init " + getInitialState() + ";" +
				ENDL;
		return module;
	}

	private String addCommandsInModule(String module){
		String moduleWithCommands = module;
		
		for (Command command : getCommands().values()) {
			moduleWithCommands += "	"+command.makeString(getStateVariable()) + ENDL;
		}
		moduleWithCommands += ENDMODULE + ENDL + ENDL;

		return moduleWithCommands;
	}

	private String addLabelsAndStatesInModule (String module){
		String moduleWithLabelsAndStates = module;
		
		for (Map.Entry<String, Set<Integer>> entry : getLabels().entrySet()) {
			String label = entry.getKey();
			moduleWithLabelsAndStates += LABEL + " \"" + label + "\" = ";

			Set<Integer> states = entry.getValue();
			moduleWithLabelsAndStates = addStatesInModule(moduleWithLabelsAndStates, states);

			moduleWithLabelsAndStates += ";" + ENDL;
		}
		
		return moduleWithLabelsAndStates;
	}

	private String addStatesInModule (String module, Set<Integer> states){
		int count = 1;
		
		String moduleWithStates = module;
		
		for (Integer state : states) {
			moduleWithStates += getStateVariable() + "=" + state;
			if (count < states.size()) {
				moduleWithStates += " | ";
			}
			count++;
		}
		return moduleWithStates;
	}

	private String createParametersString (){
		String params = "";
		
		for (String parameter : getParameters()) {
			params += PARAM_DOUBLE + " " + parameter + ";" + ENDL;
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