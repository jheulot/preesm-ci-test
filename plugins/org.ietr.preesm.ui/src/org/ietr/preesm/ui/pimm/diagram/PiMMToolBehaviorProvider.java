/*******************************************************************************
 * Copyright or © or Copr. IETR/INSA: Maxime Pelcat, Jean-François Nezan,
 * Karol Desnos, Julien Heulot
 * 
 * [mpelcat,jnezan,kdesnos,jheulot]@insa-rennes.fr
 * 
 * This software is a computer program whose purpose is to prototype
 * parallel applications.
 * 
 * This software is governed by the CeCILL-C license under French law and
 * abiding by the rules of distribution of free software.  You can  use, 
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
 ******************************************************************************/
package org.ietr.preesm.ui.pimm.diagram;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.graphiti.dt.IDiagramTypeProvider;
import org.eclipse.graphiti.features.IFeatureProvider;
import org.eclipse.graphiti.features.context.IDoubleClickContext;
import org.eclipse.graphiti.features.context.impl.CustomContext;
import org.eclipse.graphiti.features.custom.ICustomFeature;
import org.eclipse.graphiti.mm.algorithms.GraphicsAlgorithm;
import org.eclipse.graphiti.mm.pictograms.Anchor;
import org.eclipse.graphiti.mm.pictograms.ContainerShape;
import org.eclipse.graphiti.mm.pictograms.Diagram;
import org.eclipse.graphiti.mm.pictograms.PictogramElement;
import org.eclipse.graphiti.services.Graphiti;
import org.eclipse.graphiti.tb.DefaultToolBehaviorProvider;
import org.eclipse.graphiti.tb.IDecorator;
import org.ietr.preesm.experiment.model.pimm.AbstractVertex;
import org.ietr.preesm.experiment.model.pimm.Actor;
import org.ietr.preesm.experiment.model.pimm.Delay;
import org.ietr.preesm.experiment.model.pimm.ExecutableActor;
import org.ietr.preesm.experiment.model.pimm.Parameter;
import org.ietr.preesm.experiment.model.pimm.PiGraph;
import org.ietr.preesm.experiment.model.pimm.Port;
import org.ietr.preesm.ui.pimm.decorators.ActorDecorators;
import org.ietr.preesm.ui.pimm.decorators.DelayDecorators;
import org.ietr.preesm.ui.pimm.decorators.ParameterDecorators;
import org.ietr.preesm.ui.pimm.decorators.PiMMDecoratorAdapter;
import org.ietr.preesm.ui.pimm.decorators.PortDecorators;
import org.ietr.preesm.ui.pimm.features.MoveDownActorPortFeature;
import org.ietr.preesm.ui.pimm.features.MoveUpActorPortFeature;
import org.ietr.preesm.ui.pimm.features.OpenRefinementFeature;
import org.ietr.preesm.ui.pimm.features.RenameAbstractVertexFeature;
import org.ietr.preesm.ui.pimm.features.RenameActorPortFeature;
import org.ietr.preesm.ui.pimm.layout.AutoLayoutFeature;

/**
 * {@link IToolBehaviorProvider} for the {@link Diagram} with type
 * {@link PiMMDiagramTypeProvider}.
 * 
 * @author kdesnos
 * @author jheulot
 * 
 */
public class PiMMToolBehaviorProvider extends DefaultToolBehaviorProvider {

	/**
	 * Store the message to display when a ga is under the mouse.
	 */
	protected Map<GraphicsAlgorithm, String> toolTips;

	protected PiMMDecoratorAdapter decoratorAdapter;

	/**
	 * The default constructor of {@link PiMMToolBehaviorProvider}.
	 * 
	 * @param diagramTypeProvider
	 *            the {@link DiagramTypeWizardPage}
	 */
	public PiMMToolBehaviorProvider(IDiagramTypeProvider diagramTypeProvider) {
		super(diagramTypeProvider);
		toolTips = new HashMap<GraphicsAlgorithm, String>();
		decoratorAdapter = new PiMMDecoratorAdapter(diagramTypeProvider);
	}

	@Override
	public IDecorator[] getDecorators(PictogramElement pe) {

		IFeatureProvider featureProvider = getFeatureProvider();
		Diagram diagram = (Diagram) pe.eResource().getContents().get(0);
		Object bo = featureProvider.getBusinessObjectForPictogramElement(pe);
		PiGraph piGraph = (PiGraph) featureProvider.getBusinessObjectForPictogramElement(diagram);
		if (!piGraph.eAdapters().contains(decoratorAdapter)) {
			piGraph.eAdapters().add(decoratorAdapter);
		}

		if (decoratorAdapter.getPesAndDecorators().containsKey(pe)) {
			System.out.println("GotIt");
			return decoratorAdapter.getPesAndDecorators().get(pe);
		} else {
			System.out.println("Again");
			IDecorator[] result = null;
			if (bo instanceof ExecutableActor) {
				// Add decorators for each ports of the actor
				List<IDecorator> decorators = new ArrayList<IDecorator>();
				for (Anchor a : ((ContainerShape) pe).getAnchors()) {
					for (Object pbo : a.getLink().getBusinessObjects()) {
						if (pbo instanceof Port) {
							for (IDecorator d : PortDecorators.getDecorators((Port) pbo, a)) {
								decorators.add(d);
							}
						}
					}
				}

				if (bo instanceof Actor) {
					// Add decorators to the actor itself
					for (IDecorator d : ActorDecorators.getDecorators((Actor) bo, pe)) {
						decorators.add(d);
					}
				}

				result = new IDecorator[decorators.size()];
				decorators.toArray(result);
				decoratorAdapter.getPesAndDecorators().put(pe, result);
			}

			if (bo instanceof Parameter && !((Parameter) bo).isConfigurationInterface()) {

				result = ParameterDecorators.getDecorators((Parameter) bo, pe);
				decoratorAdapter.getPesAndDecorators().put(pe, result);
			}

			if (bo instanceof Delay) {
				result = DelayDecorators.getDecorators((Delay) bo, pe);
				decoratorAdapter.getPesAndDecorators().put(pe, result);
			}

			if (result == null) {
				result = super.getDecorators(pe);
			}
			
			decoratorAdapter.getPesAndDecorators().put(pe, result);
			return result;
		}
	}

	@Override
	public ICustomFeature getDoubleClickFeature(IDoubleClickContext context) {
		ICustomFeature customFeature = new OpenRefinementFeature(getFeatureProvider());

		// canExecute() tests especially if the context contains a Actor with a
		// valid refinement
		if (customFeature.canExecute(context)) {
			return customFeature;
		}

		return super.getDoubleClickFeature(context);
	}

	@Override
	public String getToolTip(GraphicsAlgorithm ga) {
		return toolTips.get(ga);
	}

	/**
	 * Set the tooltip message for a given {@link GraphicsAlgorithm}
	 * 
	 * @param ga
	 *            the {@link GraphicsAlgorithm}
	 * @param toolTip
	 *            the tooltip message to display
	 */
	public void setToolTip(GraphicsAlgorithm ga, String toolTip) {
		toolTips.put(ga, toolTip);
	}

	@Override
	public ICustomFeature getCommandFeature(CustomContext context, String hint) {
		PictogramElement[] pes = context.getPictogramElements();
		if (pes.length > 0) {
			switch (hint) {
			case MoveUpActorPortFeature.HINT:
				return new MoveUpActorPortFeature(getFeatureProvider());
			case MoveDownActorPortFeature.HINT:
				return new MoveDownActorPortFeature(getFeatureProvider());
			case AutoLayoutFeature.HINT:
				return new AutoLayoutFeature(getFeatureProvider());
			case RenameActorPortFeature.HINT:
				Object obj = Graphiti.getLinkService().getBusinessObjectForLinkedPictogramElement(pes[0]);
				if (obj instanceof Port) {
					return new RenameActorPortFeature(getFeatureProvider());
				}
				if (obj instanceof AbstractVertex) {
					return new RenameAbstractVertexFeature(getFeatureProvider());
				}
			}
		}
		return super.getCommandFeature(context, hint);
	}

}
