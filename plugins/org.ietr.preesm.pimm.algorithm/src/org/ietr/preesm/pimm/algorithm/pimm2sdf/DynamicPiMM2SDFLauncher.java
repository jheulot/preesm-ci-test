/*******************************************************************************
 * Copyright or © or Copr. 2015 - 2017 IETR/INSA:
 *
 * Antoine Morvan <antoine.morvan@insa-rennes.fr> (2017)
 * Clément Guy <clement.guy@insa-rennes.fr> (2015)
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
package org.ietr.preesm.pimm.algorithm.pimm2sdf;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.ietr.dftools.algorithm.model.sdf.SDFGraph;
import org.ietr.preesm.core.scenario.ParameterValue;
import org.ietr.preesm.core.scenario.PreesmScenario;
import org.ietr.preesm.experiment.model.pimm.Parameter;
import org.ietr.preesm.experiment.model.pimm.PiGraph;
import org.ietr.preesm.pimm.algorithm.pimm2sdf.visitor.DynamicPiMM2SDFVisitor;

public class DynamicPiMM2SDFLauncher {

	private PreesmScenario scenario;
	private PiGraph graph;

	public DynamicPiMM2SDFLauncher(PreesmScenario scenario, PiGraph graph) {
		this.scenario = scenario;
		this.graph = graph;
	}

	public Set<SDFGraph> launch() {
		Set<SDFGraph> result = new HashSet<SDFGraph>();

		// Get all the available values for all the parameters
		Map<String, List<Integer>> parametersValues = getParametersValues();
		// Get the values for Parameters directly contained by graph (top-level
		// parameters), if any
		Map<String, List<Integer>> outerParametersValues = new HashMap<String, List<Integer>>();
		// The number of time we need to execute, and thus visit graph
		int nbExecutions = scenario.getSimulationManager()
				.getNumberOfTopExecutions();

		for (Parameter param : graph.getParameters()) {
			List<Integer> pValues = parametersValues.get(param.getName());
			if (pValues != null) {
				outerParametersValues.put(param.getName(), pValues);
			}
		}

		// Visitor creating the SDFGraphs
		DynamicPiMM2SDFVisitor visitor;
		PiGraphExecution execution;
		// Values for the parameters for one execution
		Map<String, List<Integer>> currentValues;
		for (int i = 0; i < nbExecutions; i++) {
			// Values for one execution are parametersValues except for
			// top-level Parameters, for which we select only one value for a
			// given execution
			currentValues = parametersValues;
			for (String s : outerParametersValues.keySet()) {
				// Value selection
				List<Integer> availableValues = outerParametersValues.get(s);
				int nbValues = availableValues.size();
				if (nbValues > 0) {
					ArrayList<Integer> value = new ArrayList<Integer>();
					value.add(availableValues.get(i % nbValues));
					currentValues.put(s, new ArrayList<Integer>(value));
				}
			}

			execution = new PiGraphExecution(graph, currentValues, "_" + i, i);
			visitor = new DynamicPiMM2SDFVisitor(execution);
			graph.accept(visitor);

			SDFGraph sdf = visitor.getResult();
			
			result.add(sdf);
		}

		return result;
	}

	private Map<String, List<Integer>> getParametersValues() {
		Map<String, List<Integer>> result = new HashMap<String, List<Integer>>();

		for (ParameterValue paramValue : scenario.getParameterValueManager()
				.getParameterValues()) {
			switch (paramValue.getType()) {
			case ACTOR_DEPENDENT:
				result.put(paramValue.getName(), new ArrayList<Integer>(
						paramValue.getValues()));
				break;
			case INDEPENDENT:
				List<Integer> values = new ArrayList<Integer>();
				int value = Integer.parseInt(paramValue.getValue());
				values.add(value);				
				result.put(paramValue.getName(), values);
				break;
			default:
				break;
			}
		}

		return result;
	}

}
