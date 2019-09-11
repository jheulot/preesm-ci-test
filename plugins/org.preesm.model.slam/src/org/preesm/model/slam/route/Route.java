/**
 * Copyright or © or Copr. IETR/INSA - Rennes (2008 - 2019) :
 *
 * Antoine Morvan [antoine.morvan@insa-rennes.fr] (2017 - 2019)
 * Clément Guy [clement.guy@insa-rennes.fr] (2014)
 * Matthieu Wipliez [matthieu.wipliez@insa-rennes.fr] (2008)
 * Maxime Pelcat [maxime.pelcat@insa-rennes.fr] (2008 - 2012)
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
package org.preesm.model.slam.route;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import org.preesm.model.slam.ComponentInstance;

/**
 * A route contains several Route Steps. It links operators. To operators directly connected have a route with one route
 * step.
 *
 * @author mpelcat
 */
public class Route {

  /** ID used to reference the element in a property bean. */
  public static final String PROPERTY_BEAN_NAME = "route";

  /** The Constant averageTransfer. */
  public static final int AVG_TRANSFER = 1000;

  private final List<AbstractRouteStep> routeSteps = new ArrayList<>();

  /**
   * Instantiates a new route.
   *
   * @param step
   *          the step
   */
  public Route(final AbstractRouteStep step) {
    super();
    routeSteps.add(step);
  }

  /**
   * Instantiates a new route.
   *
   * @param r1
   *          the r 1
   * @param r2
   *          the r 2
   */
  public Route(final Route r1, final Route r2) {
    super();
    for (final AbstractRouteStep step : r1.routeSteps) {
      routeSteps.add(step);
    }
    for (final AbstractRouteStep step : r2.routeSteps) {
      routeSteps.add(step);
    }
  }

  /**
   * Instantiates a new route.
   */
  public Route() {
    super();
  }

  /**
   * Returns true if each operator in the route appears only once.
   *
   * @return true, if is single appearance
   */
  public boolean isSingleAppearance() {
    boolean isIt = true;
    final Set<ComponentInstance> opSet = new LinkedHashSet<>();
    // Iterating the route and testing number of occurences in sender
    for (final AbstractRouteStep step : routeSteps) {
      if (opSet.contains(step.getSender())) {
        isIt = false;
      }
      opSet.add(step.getSender());
    }

    // Testing last step receiver
    if (opSet.contains(routeSteps.get(routeSteps.size() - 1).getReceiver())) {
      isIt = false;
    }
    return isIt;
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder();
    for (final AbstractRouteStep step : routeSteps) {
      sb.append(step + " ");
    }
    return sb.toString();
  }

  public List<AbstractRouteStep> getRouteSteps() {
    return routeSteps;
  }
}
