/**
 * Copyright or © or Copr. IETR/INSA - Rennes (2008 - 2019) :
 *
 * Alexandre Honorat <alexandre.honorat@insa-rennes.fr> (2019)
 * Matthieu Wipliez <matthieu.wipliez@insa-rennes.fr> (2008)
 * Maxime Pelcat <maxime.pelcat@insa-rennes.fr> (2008 - 2012)
 * Pengcheng Mu <pengcheng.mu@insa-rennes.fr> (2008)
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
package org.preesm.algorithm.mapper.abc;

import org.preesm.algorithm.mapper.abc.taskscheduling.TaskSchedType;

/**
 * Types of simulator to be used in parameters.
 *
 * @author mpelcat
 */
public enum AbcType {

  /** Available Abc types. */
  InfiniteHomogeneous("InfiniteHomogeneous"),
  /** The Constant LooselyTimed. */
  LooselyTimed("LooselyTimed"),
  /** The Constant ApproximatelyTimed. */
  ApproximatelyTimed("ApproximatelyTimed"),
  /** The Constant AccuratelyTimed. */
  AccuratelyTimed("AccuratelyTimed"),
  /** The Constant CommConten. */
  CommConten("CommConten"),
  /** The Constant DynamicQueuing. */
  DynamicQueuing("DynamicQueuing");

  /** Name of the current type. */
  private String name = null;

  /**
   * True if the tasks are switched while mapping using algorithms that do further tests than the mapping/scheduling
   * chosen algorithm.
   */
  private TaskSchedType taskSchedType = null;

  /**
   * Instantiates a new abc type.
   *
   * @param name
   *          the name
   */
  AbcType(final String name) {
    this.name = name;
    this.taskSchedType = TaskSchedType.SIMPLE;
  }

  /*
   * (non-Javadoc)
   *
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {

    return this.name;
  }

  /**
   * From string.
   *
   * @param type
   *          the type
   * @return the abc type
   */
  public static AbcType fromString(final String type) {

    if (InfiniteHomogeneous.name.equalsIgnoreCase(type)) {
      return InfiniteHomogeneous;
    } else if (LooselyTimed.name.equalsIgnoreCase(type)) {
      return LooselyTimed;
    } else if (ApproximatelyTimed.name.equalsIgnoreCase(type)) {
      return ApproximatelyTimed;
    } else if (AccuratelyTimed.name.equalsIgnoreCase(type)) {
      return AccuratelyTimed;
    } else if (CommConten.name.equalsIgnoreCase(type)) {
      return CommConten;
    } else if (DynamicQueuing.name.equalsIgnoreCase(type)) {
      return DynamicQueuing;
    }

    return null;
  }

  /**
   * Gets the task sched type.
   *
   * @return the task sched type
   */
  public TaskSchedType getTaskSchedType() {
    return this.taskSchedType;
  }

  /**
   * Sets the task sched type.
   *
   * @param taskSchedType
   *          the task sched type
   * @return the abc type
   */
  public AbcType setTaskSchedType(final TaskSchedType taskSchedType) {
    if (taskSchedType != null) {
      this.taskSchedType = taskSchedType;
    } else {
      this.taskSchedType = TaskSchedType.SIMPLE;
    }

    return this;
  }
}
