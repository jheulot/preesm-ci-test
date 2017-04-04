/*******************************************************************************
 * Copyright or © or Copr. 2014 - 2017 IETR/INSA:
 *
 * Antoine Morvan <antoine.morvan@insa-rennes.fr> (2017)
 * Clément Guy <clement.guy@insa-rennes.fr> (2014 - 2015)
 * Karol Desnos <karol.desnos@insa-rennes.fr> (2015)
 *
 * This software is a computer program whose purpose is to prototype
 * parallel applications.
 *
 * This software is governed by the CeCILL-C license under French law and
 * abiding by the rules of distribution of free software.  You can  use
 * modify and/ or redistribute the software under the terms of the CeCILL-C
 * license as circulated by CEA, CNRS and INRIA at the following URL
 * "http://www.cecill.info".
 *
 * As a counterpart to the access to the source code and  rights to copy,
 * modify and redistribute granted by the license, users are provided only
 * with a limited warranty  and the software's author,  the holder of the
 * economic rights,  and the successive licensors  have only  limited
 * liability.
 *
 * In this respect, the user's attention is drawn to the risks associated
 * with loading,  using,  modifying and/or developing or reproducing the
 * software by the user in light of its specific status of free software,
 * that may mean  that it is complicated to manipulate,  and  that  also
 * therefore means  that it is reserved for developers  and  experienced
 * professionals having in-depth computer knowledge. Users are therefore
 * encouraged to load and test the software's suitability as regards their
 * requirements in conditions enabling the security of their systems and/or
 * data to be ensured and,  more generally, to use and operate it in the
 * same conditions as regards security.
 *
 * The fact that you are presently reading this means that you have had
 * knowledge of the CeCILL-C license and that you accept its terms.
 *******************************************************************************/
package org.ietr.preesm.core.workflow;

import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Path;
import org.ietr.dftools.algorithm.importer.InvalidModelException;
import org.ietr.dftools.algorithm.model.parameters.VariableSet;
import org.ietr.dftools.algorithm.model.sdf.SDFGraph;
import org.ietr.dftools.algorithm.model.sdf.transformations.SpecialActorPortsIndexer;
import org.ietr.dftools.architecture.slam.Design;
import org.ietr.dftools.workflow.WorkflowException;
import org.ietr.dftools.workflow.implement.AbstractScenarioImplementation;
import org.ietr.preesm.core.scenario.PreesmScenario;
import org.ietr.preesm.core.scenario.serialize.ScenarioParser;
import org.ietr.preesm.experiment.model.pimm.PiGraph;

/**
 * Implementing the new DFTools scenario node behavior for Preesm. This version
 * generates an architecture with the S-LAM2 meta-model type and an algorithm
 * with the IBSDF type
 * 
 * @author mpelcat
 * @author kdesnos
 * 
 */
public class AlgorithmAndArchitectureScenarioNode extends
		AbstractScenarioImplementation {

	/**
	 * The scenario node in Preesm outputs three elements: Algorithm (SDF or
	 * PiMM), architecture and scenario
	 */
	@Override
	public Map<String, Object> extractData(String path)
			throws WorkflowException {

		Map<String, Object> outputs = new HashMap<String, Object>();

		// Retrieving the scenario from the given path
		ScenarioParser scenarioParser = new ScenarioParser();

		Path relativePath = new Path(path);
		IFile file = ResourcesPlugin.getWorkspace().getRoot()
				.getFile(relativePath);

		PreesmScenario scenario;
		// Retrieving the algorithm
		SDFGraph sdfAlgorithm = null;
		PiGraph piAlgorithm = null;

		try {
			scenario = scenarioParser.parseXmlFile(file);
			String url = scenario.getAlgorithmURL();
			if (scenario.isIBSDFScenario()) {
				// Parse the graph
				sdfAlgorithm = ScenarioParser.getSDFGraph(url);
				// Add indexes to all its special actor ports
				SpecialActorPortsIndexer.addIndexes(sdfAlgorithm);
				SpecialActorPortsIndexer.sortIndexedPorts(sdfAlgorithm);
			}
			else if (scenario.isPISDFScenario())
				piAlgorithm = ScenarioParser.getPiGraph(url);
		} catch (FileNotFoundException | InvalidModelException | CoreException e) {
			throw new WorkflowException(e.getMessage());
		}

		// Apply to the algorithm the values of variables
		// defined in the scenario
		if (scenario.isIBSDFScenario()) {
			VariableSet variablesGraph = sdfAlgorithm.getVariables();
			VariableSet variablesScenario = scenario.getVariablesManager()
					.getVariables();

			for (String variableName : variablesScenario.keySet()) {
				String newValue = variablesScenario.get(variableName)
						.getValue();
				variablesGraph.getVariable(variableName).setValue(newValue);
			}
		}
		// Retrieving the architecture
		Design slamDesign = ScenarioParser.parseSlamDesign(scenario
				.getArchitectureURL());

		outputs.put(KEY_SCENARIO, scenario);
		outputs.put(KEY_SDF_GRAPH, sdfAlgorithm);
		outputs.put(KEY_PI_GRAPH, piAlgorithm);
		outputs.put(KEY_ARCHITECTURE, slamDesign);
		return outputs;
	}

	@Override
	public String monitorMessage() {
		return "Scenario, algorithm and architecture parsing.";
	}

}
