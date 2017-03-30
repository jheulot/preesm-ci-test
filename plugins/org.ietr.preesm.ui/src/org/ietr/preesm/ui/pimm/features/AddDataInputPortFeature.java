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
package org.ietr.preesm.ui.pimm.features;

import org.eclipse.graphiti.features.IFeatureProvider;
import org.eclipse.graphiti.mm.algorithms.GraphicsAlgorithm;
import org.eclipse.graphiti.mm.algorithms.Rectangle;
import org.eclipse.graphiti.mm.algorithms.Text;
import org.eclipse.graphiti.mm.algorithms.styles.Orientation;
import org.eclipse.graphiti.services.Graphiti;
import org.eclipse.graphiti.services.IGaService;
import org.eclipse.graphiti.util.ColorConstant;
import org.eclipse.graphiti.util.IColorConstant;
import org.ietr.preesm.experiment.model.pimm.DataInputPort;
import org.ietr.preesm.experiment.model.pimm.ExecutableActor;
import org.ietr.preesm.experiment.model.pimm.PiMMFactory;
import org.ietr.preesm.experiment.model.pimm.Port;

/**
 * Add Feature for {@link DataInputPort}s
 * 
 * @author kdesnos
 * @author jheulot
 * 
 */
public class AddDataInputPortFeature extends AbstractAddActorPortFeature {

	public static final IColorConstant DATA_INPUT_PORT_FOREGROUND = AddActorFeature.ACTOR_FOREGROUND;
	public static final IColorConstant DATA_INPUT_PORT_BACKGROUND = new ColorConstant(
			182, 215, 122);
	public static final PortPosition DATA_INPUT_PORT_POSITION = PortPosition.LEFT;
	public static final String DATA_INPUT_PORT_KIND = "input";

	/**
	 * Default constructor
	 * 
	 * @param fp
	 *            the feature provider
	 */
	public AddDataInputPortFeature(IFeatureProvider fp) {
		super(fp);
	}

	@Override
	public String getName() {
		return "Add Input Port";
	}

	@Override
	public String getDescription() {
		return "Add an input port to the Actor";
	}

	@Override
	public PortPosition getPosition() {
		return DATA_INPUT_PORT_POSITION;
	}

	@Override
	public GraphicsAlgorithm addPortGA(GraphicsAlgorithm containerShape) {
		// Get the GaService
		IGaService gaService = Graphiti.getGaService();
		// Create the port GraphicAlgorithm
		Rectangle rectangle = gaService.createPlainRectangle(containerShape);
		rectangle.setForeground(manageColor(DATA_INPUT_PORT_FOREGROUND));
		rectangle.setBackground(manageColor(DATA_INPUT_PORT_BACKGROUND));
		rectangle.setLineWidth(1);
		int portFontHeight = AbstractAddActorPortFeature.PORT_FONT_HEIGHT;
		gaService.setSize(rectangle, PORT_ANCHOR_GA_SIZE, PORT_ANCHOR_GA_SIZE);
		gaService.setLocation(rectangle, 0, 1 + (portFontHeight - PORT_ANCHOR_GA_SIZE) / 2);
		return rectangle;
	}

	@Override
	public GraphicsAlgorithm addPortLabel(GraphicsAlgorithm containerShape,
			String portName) {
		// Get the GaService
		IGaService gaService = Graphiti.getGaService();

		// Create the text
		final Text text = gaService.createText(containerShape);
		text.setValue(portName);
		text.setFont(getPortFont());
		text.setForeground(manageColor(PORT_TEXT_FOREGROUND));

		// Layout the text
		int portFontHeight = AbstractAddActorPortFeature.PORT_FONT_HEIGHT;
		text.setHorizontalAlignment(Orientation.ALIGNMENT_RIGHT);
		gaService.setHeight(text, portFontHeight);

		return text;
	}

	@Override
	public Port getNewPort(String portName, ExecutableActor actor) {
		DataInputPort newPort = PiMMFactory.eINSTANCE.createDataInputPort();
		newPort.setName(portName);
		actor.getDataInputPorts().add(newPort);
		return newPort;
	}

	@Override
	public String getPortKind() {
		return DATA_INPUT_PORT_KIND;
	}

}
