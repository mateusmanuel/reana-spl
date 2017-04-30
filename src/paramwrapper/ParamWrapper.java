/**
 *
 */
package paramwrapper;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import fdtmc.FDTMC;

import java.util.Set;

/**
 * Façade to a PARAM executable.
 *
 * @author Thiago
 *
 */
public class ParamWrapper implements ParametricModelChecker {
    private static final Logger LOGGER = Logger.getLogger(ParamWrapper.class.getName());

	private String paramPath;
	private IModelCollector modelCollector;
	private boolean usePrism = false;

    public ParamWrapper(String paramPath) {
        this(paramPath, new NoopModelCollector());
    }

    public ParamWrapper(String paramPath, IModelCollector modelCollector) {
        this.paramPath = paramPath;
        this.usePrism = paramPath.contains("prism");
        this.modelCollector = modelCollector;
    }
    
	public String getParamPath() {
		return paramPath;
	}

	public void setParamPath(String paramPath) {
		this.paramPath = paramPath;
	}

	public IModelCollector getModelCollector() {
		return modelCollector;
	}

	public void setModelCollector(IModelCollector modelCollector) {
		this.modelCollector = modelCollector;
	}

	public boolean isUsePrism() {
		return usePrism;
	}

	public void setUsePrism(boolean usePrism) {
		this.usePrism = usePrism;
	}
	
	public static Logger getLogger() {
		return LOGGER;
	}

	public String fdtmcToParam(FDTMC fdtmc) {
		ParamModel model = new ParamModel(fdtmc);
		getModelCollector().collectModel(model.getParametersNumber(), model.getStatesNumber());
		return model.toString();
	}

	@Override
	public String getReliability(FDTMC fdtmc) {
	    ParamModel model = new ParamModel(fdtmc);
        getModelCollector().collectModel(model.getParametersNumber(), model.getStatesNumber());
		String modelString = model.toString();

		if (isUsePrism()) {
		    modelString = modelString.replace("param", "const");
		}
		String reliabilityProperty = "P=? [ F \"success\" ]";

		return evaluate(modelString, reliabilityProperty, model);
	}

	private File writeModelFileWithExtensionParam (String modelString) throws IOException{
		File modelFile = File.createTempFile("model", "param");
		
		FileWriter modelWriter = new FileWriter(modelFile);
		modelWriter.write(modelString);
		
		modelWriter.flush();
		modelWriter.close();
		
		return modelFile;
	}
	
	private File writePropertyFileWithExtesionProp (String property) throws IOException{ 					 
		File propertyFile = File.createTempFile("property", "prop");
	
		FileWriter propertyWriter = new FileWriter(propertyFile);
		propertyWriter.write(property);
		
		propertyWriter.flush();
		propertyWriter.close();
		
		return propertyFile;
	}
 
	private String writeFormula(String modelString, File modelFile, File propertyFile, File resultsFile, ParamModel model) throws IOException{
		String formula;
		
		final boolean containConst = modelString.contains("const");
		
		if (isUsePrism()){
			if(!containConst){
				formula = invokeModelChecker(modelFile.getAbsolutePath(),
		                                 propertyFile.getAbsolutePath(),
		                                 resultsFile.getAbsolutePath());
			}else{
			    formula = invokeParametricPRISM(model,
                        				 modelFile.getAbsolutePath(),
                        				 propertyFile.getAbsolutePath(),
                        				 resultsFile.getAbsolutePath());
			}
		}else {
		    formula = invokeParametricModelChecker(modelFile.getAbsolutePath(),
		                                  		   propertyFile.getAbsolutePath(),
		                                           resultsFile.getAbsolutePath());
		}
		
		return formula;
	}

	private String evaluate(String modelString, String property, ParamModel model) {
		try {
		    getLogger().finer(modelString);

		    File modelFile = writeModelFileWithExtensionParam(modelString);

			File propertyFile = writePropertyFileWithExtesionProp(property);
					
			File resultsFile = File.createTempFile("result", null);

			long startTime = System.nanoTime();
			
			String formula = writeFormula(modelString, modelFile, propertyFile, resultsFile, model);
		
			long elapsedTime = System.nanoTime() - startTime;
            
			getModelCollector().collectModelCheckingTime(elapsedTime);
			
            return formula.trim().replaceAll("\\s+", "");
            
		} catch (IOException e) {
			getLogger().log(Level.SEVERE, e.toString(), e);
		}
		return "";
	}
	

	private String invokeParametricModelChecker(String modelPath,
												String propertyPath,
												String resultsPath) throws IOException {
		String commandLine = getParamPath() + " "
							 + modelPath + " "
							 + propertyPath + " "
							 + "--result-file " + resultsPath;
		return invokeAndGetResult(commandLine, resultsPath + ".out");
	}

	public String initializeCommandLine(String modelPath, String propertyPath, String resultsPath) {
		return getParamPath() + " "
                + modelPath + " "
                + propertyPath + " "
                + "-exportresults " + resultsPath;
	}

	public String addParametersToCommandLine(String commandLine, Set<String> parameters) {
		return commandLine + " -param " + String.join(",", parameters);
	}

	public String generateExpressionFromRawResult(String rawResult) {
        int openBracket = rawResult.indexOf("{");
        int closeBracket = rawResult.indexOf("}");

		return rawResult.substring(openBracket + 1, closeBracket);
	}

	public String parseExpression(String expression) {
		return expression.trim().replace('|', '/');
	}

    private String invokeParametricPRISM(ParamModel model, String modelPath, String propertyPath, String resultsPath) throws IOException {
        String commandLine = initializeCommandLine(modelPath, propertyPath, resultsPath);

        commandLine = addParametersToCommandLine(commandLine, model.getParameters());

        String rawResult = invokeAndGetResult(commandLine, resultsPath);

        String expression = generateExpressionFromRawResult(rawResult);

        return parseExpression(expression);
    }

	private String invokeModelChecker(String modelPath, String propertyPath, String resultsPath) throws IOException {

		String commandLine = initializeCommandLine(modelPath, propertyPath, resultsPath);

		return invokeAndGetResult(commandLine, resultsPath);
	}

	private String invokeAndGetResult(String commandLine, String resultsPath) throws IOException {
	    getLogger().fine(commandLine);
	    
		Process program = Runtime.getRuntime().exec(commandLine);
		int exitCode = 0;
		
		try {
			exitCode = program.waitFor();
		} catch (InterruptedException e) {
			getLogger().severe("Exit code: " + exitCode);
			getLogger().log(Level.SEVERE, e.toString(), e);
		}
		
		List<String> lines = Files.readAllLines(Paths.get(resultsPath), Charset.forName("UTF-8"));
		lines.removeIf(String::isEmpty);
		
		// Formula
		return getLastLine(lines);
	}
	
	private String getLastLine(List<String> lines){
		return lines.get(lines.size()-1);
	}

}
