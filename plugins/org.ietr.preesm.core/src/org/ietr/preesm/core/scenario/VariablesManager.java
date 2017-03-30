package org.ietr.preesm.core.scenario;

import org.ietr.dftools.algorithm.model.parameters.Variable;
import org.ietr.dftools.algorithm.model.parameters.VariableSet;
import org.ietr.dftools.algorithm.model.sdf.SDFGraph;
import org.ietr.preesm.core.scenario.serialize.ExcelVariablesParser;

/**
 * Handles graph variables which values are redefined in the scenario
 * 
 * @author mpelcat
 */
public class VariablesManager {

	private VariableSet variables;

	/**
	 * Path to a file containing variables
	 */
	private String excelFileURL = "";

	public VariablesManager() {
		variables = new VariableSet();
	}

	public void setVariable(String name, String value) {

		if (variables.keySet().contains(name)) {
			variables.get(name).setValue(value);
		} else {
			variables.put(name, new Variable(name, value));
		}
	}

	public VariableSet getVariables() {
		return variables;
	}

	public void removeVariable(String varName) {
		variables.remove(varName);
	}

	public String getExcelFileURL() {
		return excelFileURL;
	}

	public void setExcelFileURL(String excelFileURL) {
		this.excelFileURL = excelFileURL;
	}

	public void importVariables(PreesmScenario currentScenario) {
		if (!excelFileURL.isEmpty() && currentScenario != null) {
			ExcelVariablesParser parser = new ExcelVariablesParser(
					currentScenario);
			
			try {
				parser.parse(excelFileURL);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public void updateWith(SDFGraph sdfGraph) {
		getVariables().clear();
		for (String v : sdfGraph.getVariables().keySet()) {
			setVariable(v, sdfGraph.getVariable(v).getValue());
		}
	}

}
