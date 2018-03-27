/**
 * Copyright or © or Copr. IETR/INSA - Rennes (2017 - 2018) :
 *
 * Antoine Morvan <antoine.morvan@insa-rennes.fr> (2017 - 2018)
 *
 * This software is a computer program whose purpose is to help prototyping
 * parallel applications using dataflow formalism.
 *
 * This software is governed by the CeCILL  license under French law and
 * abiding by the rules of distribution of free software.  You can  use,
 * modify and/ or redistribute the software under the terms of the CeCILL
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
 * knowledge of the CeCILL license and that you accept its terms.
 */
package org.ietr.preesm.experiment.model.factory;

import org.eclipse.core.runtime.Path;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.ietr.preesm.experiment.model.pimm.CHeaderRefinement;
import org.ietr.preesm.experiment.model.pimm.ConfigInputInterface;
import org.ietr.preesm.experiment.model.pimm.ConfigInputPort;
import org.ietr.preesm.experiment.model.pimm.ConfigOutputInterface;
import org.ietr.preesm.experiment.model.pimm.ConfigOutputPort;
import org.ietr.preesm.experiment.model.pimm.DataInputInterface;
import org.ietr.preesm.experiment.model.pimm.DataInputPort;
import org.ietr.preesm.experiment.model.pimm.DataOutputInterface;
import org.ietr.preesm.experiment.model.pimm.DataOutputPort;
import org.ietr.preesm.experiment.model.pimm.Delay;
import org.ietr.preesm.experiment.model.pimm.Dependency;
import org.ietr.preesm.experiment.model.pimm.Direction;
import org.ietr.preesm.experiment.model.pimm.Expression;
import org.ietr.preesm.experiment.model.pimm.Fifo;
import org.ietr.preesm.experiment.model.pimm.FunctionPrototype;
import org.ietr.preesm.experiment.model.pimm.ISetter;
import org.ietr.preesm.experiment.model.pimm.Parameter;
import org.ietr.preesm.experiment.model.pimm.PiGraph;
import org.ietr.preesm.experiment.model.pimm.adapter.GraphInterfaceObserver;
import org.ietr.preesm.experiment.model.pimm.impl.PiMMFactoryImpl;

/**
 *
 * @author anmorvan
 *
 */
public final class PiMMUserFactory extends PiMMFactoryImpl {

  public static final PiMMUserFactory instance = new PiMMUserFactory();

  private static final EcoreUtil.Copier copier = new EcoreUtil.Copier(false);

  private PiMMUserFactory() {

  }

  /**
   * Copy an existing PiMM node
   */
  public final <T extends EObject> T copy(final T vertex) {
    @SuppressWarnings("unchecked")
    final T copy = (T) PiMMUserFactory.copier.copy(vertex);
    return copy;
  }

  /**
   *
   */
  public Dependency createDependency(final ISetter setter, final ConfigInputPort target) {
    final Dependency dep = createDependency();
    dep.setGetter(target);
    dep.setSetter(setter);
    return dep;
  }

  /**
   *
   */
  public Fifo createFifo(final DataOutputPort sourcePortCopy, final DataInputPort targetPortCopy, final String type) {
    final Fifo res = createFifo();
    res.setSourcePort(sourcePortCopy);
    res.setTargetPort(targetPortCopy);
    res.setType(type);
    return res;
  }

  @Override
  public ConfigInputInterface createConfigInputInterface() {
    final ConfigInputInterface res = super.createConfigInputInterface();
    final Expression createExpression = createExpression();
    res.setExpression(createExpression);
    return res;
  }

  @Override
  public Parameter createParameter() {
    final Parameter createParameter = super.createParameter();
    final Expression createExpression = createExpression();
    createParameter.setExpression(createExpression);
    return createParameter;
  }

  @Override
  public DataInputPort createDataInputPort() {
    final DataInputPort res = super.createDataInputPort();
    res.setExpression(createExpression());
    return res;
  }

  @Override
  public DataOutputPort createDataOutputPort() {
    final DataOutputPort res = super.createDataOutputPort();
    res.setExpression(createExpression());
    return res;
  }

  @Override
  public ConfigOutputPort createConfigOutputPort() {
    final ConfigOutputPort res = super.createConfigOutputPort();
    res.setExpression(createExpression());
    return res;
  }

  @Override
  public Delay createDelay() {
    final Delay res = super.createDelay();
    // Create ports here and force their name
    final DataOutputPort getter = PiMMUserFactory.instance.createDataOutputPort();
    final DataInputPort setter = PiMMUserFactory.instance.createDataInputPort();
    res.getDataInputPorts().add(setter);
    res.getDataOutputPorts().add(getter);
    res.getDataInputPort().setName("set");
    res.getDataOutputPort().setName("get");

    // Set default expression
    res.setExpression(createExpression());

    // Creates the default refinement
    final CHeaderRefinement hrefinement = PiMMUserFactory.instance.createCHeaderRefinement();
    hrefinement.setLoopPrototype(null);
    final FunctionPrototype proto = PiMMUserFactory.instance.createFunctionPrototype();
    proto.setName("defaultDelayInit");
    proto.getParameters().add(PiMMUserFactory.instance.createFunctionParameter());
    proto.getParameters().add(PiMMUserFactory.instance.createFunctionParameter());
    proto.getParameters().get(0).setName("size");
    proto.getParameters().get(0).setType("int");
    proto.getParameters().get(0).setDirection(Direction.IN);
    proto.getParameters().get(1).setName("fifo");
    proto.getParameters().get(1).setType("void*");
    proto.getParameters().get(1).setDirection(Direction.OUT);
    hrefinement.setInitPrototype(proto);
    hrefinement.setFilePath(new Path("include/memory.h"));
    res.setRefinement(hrefinement);
    return res;
  }

  @Override
  public PiGraph createPiGraph() {
    final PiGraph res = super.createPiGraph();
    res.eAdapters().add(new GraphInterfaceObserver());
    return res;
  }

  @Override
  public DataInputInterface createDataInputInterface() {
    final DataInputInterface res = super.createDataInputInterface();
    final DataOutputPort port = PiMMUserFactory.instance.createDataOutputPort();
    res.getDataOutputPorts().add(port);
    return res;
  }

  @Override
  public DataOutputInterface createDataOutputInterface() {
    final DataOutputInterface res = super.createDataOutputInterface();
    final DataInputPort port = PiMMUserFactory.instance.createDataInputPort();
    res.getDataInputPorts().add(port);
    return res;
  }

  @Override
  public ConfigOutputInterface createConfigOutputInterface() {
    final ConfigOutputInterface res = super.createConfigOutputInterface();
    final DataInputPort port = PiMMUserFactory.instance.createDataInputPort();
    res.getDataInputPorts().add(port);
    return res;
  }
}
