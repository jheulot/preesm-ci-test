/**
 * Copyright or © or Copr. IETR/INSA - Rennes (2011 - 2019) :
 *
 * Antoine Morvan [antoine.morvan@insa-rennes.fr] (2017 - 2019)
 * Clément Guy [clement.guy@insa-rennes.fr] (2014)
 * Maxime Pelcat [maxime.pelcat@insa-rennes.fr] (2011)
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

import java.util.logging.Level;
import org.preesm.commons.PreesmPlugin;
import org.preesm.commons.logger.PreesmLogger;
import org.preesm.workflow.implement.AbstractWorkflowNodeImplementation;

/**
 * This class provides methods to manipulate workflow nodes.
 *
 * @author mpelcat
 *
 * @param <T>
 *          the top class of the implementation
 */
public abstract class AbstractWorkflowNode<T extends AbstractWorkflowNodeImplementation> {

  /** Implementation of this node. */
  protected T implementation = null;

  /**
   * Gets the implementation.
   *
   * @return the implementation
   */
  public final AbstractWorkflowNodeImplementation getImplementation() {
    return this.implementation;
  }

  public void init(final T implem) {
    implem.setWorkflowNode(this);
    initPrototype(implem);
  }

  protected abstract boolean initPrototype(final T implem);

  /**
   * Checks if is scenario node.
   *
   * @return True if this node is a scenario node, false otherwise.
   */
  public abstract boolean isScenarioNode();

  /**
   * Checks if is task node.
   *
   * @return True if this node is a transformation node, false otherwise.
   */
  public abstract boolean isTaskNode();

  public abstract String getID();

  public abstract String getName();

  /**
   *
   */
  public boolean getExtensionInformation() {
    try {
      final Class<?> task = PreesmPlugin.getInstance().getTask(this.getID());
      if (task != null) {
        @SuppressWarnings("unchecked")
        final T obj = (T) task.newInstance();
        this.implementation = obj;

        // Initializes the prototype of the scenario
        init(this.implementation);
        return true;
      }

      return false;
    } catch (final InstantiationException | IllegalAccessException e) {
      PreesmLogger.getLogger().log(Level.SEVERE,
          "Failed to load '" + getID() + "' (" + getName() + ") node from workflow", e);
      return false;
    }
  }

}
