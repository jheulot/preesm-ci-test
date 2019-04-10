/**
 * Copyright or © or Copr. IETR/INSA - Rennes (2018 - 2019) :
 *
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
package org.preesm.workflow.elements;

import org.preesm.commons.doc.annotations.Port;
import org.preesm.commons.doc.annotations.PreesmTask;
import org.preesm.workflow.implement.AbstractScenarioImplementation;

/**
 * This class provides a scenario workflow node.
 *
 * @author mpelcat
 */
public class ScenarioNode extends AbstractWorkflowNode<AbstractScenarioImplementation> {

  /**
   * The identifier of this scenario node. It is needed to retrieve the implementation of this node
   */
  private String scenarioId = null;

  /**
   * Instantiates a new scenario node.
   *
   * @param scenarioId
   *          the scenario id
   */
  public ScenarioNode(final String scenarioId) {
    super();
    this.scenarioId = scenarioId;
  }

  /**
   * Gets the scenario id.
   *
   * @return the scenario id
   */
  @Override
  public String getName() {
    return "Scenario Node";
  }

  @Override
  public String getID() {
    return this.scenarioId;
  }

  /**
   * Gets the scenario.
   *
   * @return the scenario
   */
  public AbstractScenarioImplementation getScenario() {
    return (AbstractScenarioImplementation) this.implementation;
  }

  /*
   * (non-Javadoc)
   *
   * @see org.ietr.dftools.workflow.elements.AbstractWorkflowNode#isScenarioNode()
   */
  @Override
  public boolean isScenarioNode() {
    return true;
  }

  /*
   * (non-Javadoc)
   *
   * @see org.ietr.dftools.workflow.elements.AbstractWorkflowNode#isTaskNode()
   */
  @Override
  public boolean isTaskNode() {
    return false;
  }

  /**
   * Initializes the outputs types of the scenario using information from the plugin extension.
   *
   * @param scenario
   *          the scenario
   * @param element
   *          the element
   * @return True if the prototype was correctly set.
   */
  @Override
  protected boolean initPrototype(final AbstractScenarioImplementation scenario) {
    final PreesmTask annotation = scenario.getClass().getAnnotation(PreesmTask.class);
    for (final Port output : annotation.outputs()) {
      scenario.addOutput(output.name(), output.type().getCanonicalName());
    }
    return true;
  }

}
