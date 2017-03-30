/*******************************************************************************
 * Copyright or © or Copr. %%LOWERDATE%% - %%UPPERDATE%% IETR/INSA:
 *
 * %%AUTHORS%%
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
package org.ietr.preesm.ui.pimm.features;

import org.eclipse.graphiti.features.IFeatureProvider;
import org.eclipse.graphiti.features.context.ICreateContext;
import org.eclipse.graphiti.features.impl.AbstractCreateFeature;
import org.eclipse.graphiti.mm.pictograms.Diagram;
import org.ietr.preesm.experiment.model.pimm.Expression;
import org.ietr.preesm.experiment.model.pimm.Parameter;
import org.ietr.preesm.experiment.model.pimm.PiGraph;
import org.ietr.preesm.experiment.model.pimm.PiMMFactory;
import org.ietr.preesm.experiment.model.pimm.util.VertexNameValidator;
import org.ietr.preesm.ui.pimm.util.PiMMUtil;

/**
 * Create Feature for {@link Parameter}s
 * 
 * @author kdesnos
 * @author jheulot
 * 
 */
public class CreateParameterFeature extends AbstractCreateFeature {

	private static final String FEATURE_NAME = "Parameter";

	private static final String FEATURE_DESCRIPTION = "Create Parameter";

	protected Boolean hasDoneChanges;

	/**
	 * Default constructor for the {@link CreateParameterFeature}.
	 * 
	 * @param fp
	 *            the feature provider
	 * @param name
	 *            the name of
	 * @param description
	 */
	public CreateParameterFeature(IFeatureProvider fp) {
		super(fp, FEATURE_NAME, FEATURE_DESCRIPTION);
		hasDoneChanges = false;
	}

	@Override
	public boolean canCreate(ICreateContext context) {
		return context.getTargetContainer() instanceof Diagram;
	}

	@Override
	public Object[] create(ICreateContext context) {
		// Retrieve the graph
		PiGraph graph = (PiGraph) getBusinessObjectForPictogramElement(getDiagram());

		// Ask user for Parameter name until a valid name is entered.
		String question = "Enter new parameter name";
		String newParameterName = "ParameterName";

		// TODO create a parameter name validator
		newParameterName = PiMMUtil.askString("Create Parameter", question,
				newParameterName, new VertexNameValidator(graph, null));
		if (newParameterName == null || newParameterName.trim().length() == 0) {
			this.hasDoneChanges = false; // If this is not done, the graph is
											// considered modified.
			return EMPTY;
		}

		// create Parameter
		Parameter newParameter = PiMMFactory.eINSTANCE.createParameter();
		Expression expr = PiMMFactory.eINSTANCE.createExpression();
		newParameter.setExpression(expr);
		newParameter.setName(newParameterName);
		newParameter.setConfigurationInterface(false);
		// newParameter.setLocallyStatic(true);
		newParameter.setGraphPort(null); // No port of the graph corresponds to
											// this parameter

		// Add new parameter to the graph.
		if (graph.getParameters().add(newParameter)) {
			this.hasDoneChanges = true;
		}

		// do the add to the Diagram
		addGraphicalRepresentation(context, newParameter);

		// return newly created business object(s)
		return new Object[] { newParameter };
	}

}
