/**
 * Copyright or © or Copr. IETR/INSA - Rennes (2018) :
 *
 * Antoine Morvan <antoine.morvan@insa-rennes.fr> (2018)
 * Daniel Madroñal <daniel.madronal@upm.es> (2018)
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
package org.preesm.model.scenario.papi;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import org.preesm.model.pisdf.AbstractActor;

/**
 * A PapifyConfig stores the monitoring configuration of each core instance.
 *
 * @author dmadronal
 */
public class PapifyConfigActor {

  /** The actor instance path. */
  private AbstractActor actor;

  /** The set of eventSets that are going to be monitored. */
  private Map<String, Set<PapiEvent>> actorEventMap;

  /**
   * Instantiates a new PapifyConfig group.
   */
  public PapifyConfigActor() {
    this.actorEventMap = new LinkedHashMap<>();
  }

  /**
   * Instantiates a new PapifyConfig group.
   */
  public PapifyConfigActor(final AbstractActor actorPath) {
    this.actor = actorPath;
    this.actorEventMap = new LinkedHashMap<>();
  }

  /**
   * Adds the actorPath.
   *
   * @param actor
   *          the actor instance
   */
  public void setActor(final AbstractActor actor) {
    this.actor = actor;

  }

  /**
   * Adding an event
   *
   * @param event
   *          the PAPI event
   */
  public void addPAPIEvent(final String component, final PapiEvent event) {
    Set<PapiEvent> eventSetAux = null;
    if (component != null && !component.equals("") && event != null) {
      eventSetAux = this.actorEventMap.get(component);
      if (eventSetAux != null) {
        if (!eventSetAux.contains(event)) {
          eventSetAux.add(event);
        }
      } else {
        eventSetAux = new LinkedHashSet<>();
        eventSetAux.add(event);
        this.actorEventMap.put(component, eventSetAux);
      }
    }
  }

  /**
   * Adding an eventSet
   *
   * @param events
   *          the PAPI events
   */
  public void addPAPIEventSet(final String component, final Set<PapiEvent> events) {
    Set<PapiEvent> eventSetAux = null;
    if (component != null && !component.equals("") && !events.isEmpty()) {
      eventSetAux = this.actorEventMap.get(component);
      if (eventSetAux != null) {
        for (final PapiEvent eventAux : events) {
          if (!eventSetAux.contains(eventAux)) {
            eventSetAux.add(eventAux);
          }
        }
      } else {
        eventSetAux = new LinkedHashSet<>();
        for (final PapiEvent eventAux : events) {
          eventSetAux.add(eventAux);
        }
        this.actorEventMap.put(component, eventSetAux);
      }
    }
  }

  /**
   * Removes an event.
   *
   * @param event
   *          the PAPI event
   */
  public void removePAPIEvent(final String component, final PapiEvent event) {
    Set<PapiEvent> eventSetAux = null;
    if (component != null && !component.equals("") && event != null) {
      eventSetAux = this.actorEventMap.get(component);
      if (eventSetAux != null) {
        if (eventSetAux.contains(event)) {
          eventSetAux.remove(event);
        }
      }
    }
  }

  /**
   * Gets the actor path.
   *
   * @return the actor path
   */
  public AbstractActor getActor() {
    return this.actor;
  }

  /**
   * Gets the PAPI events.
   *
   * @return the PAPI events
   */
  public Map<String, Set<PapiEvent>> getPAPIEvents() {
    return this.actorEventMap;
  }

  /**
   * Checks for Actor path.
   *
   * @param actor
   *          the actor path
   * @return true, if successful
   */
  public boolean isActor(final AbstractActor actor) {
    return actor.equals(this.actor);
  }

  /**
   * Checks for PAPI events.
   *
   * @param event
   *          the PAPI event
   * @return true, if successful
   */
  public boolean hasPapiEvent(String component, final PapiEvent event) {
    Set<PapiEvent> eventSetAux = null;
    if (component != null && !component.equals("") && event != null) {
      eventSetAux = this.actorEventMap.get(component);
      if (eventSetAux != null) {
        if (eventSetAux.contains(event)) {
          return true;
        }
      }
    }
    return false;
  }

  @Override
  public boolean equals(final Object comparer) {

    boolean decision = false;

    if (comparer instanceof PapifyConfigActor) {
      final PapifyConfigActor tester = (PapifyConfigActor) comparer;
      if (this.actor.equals(tester.getActor())) {
        decision = true;
      }
    }
    return decision;
  }

  @Override
  public int hashCode() {
    return this.actor.hashCode();
  }

  /*
   * (non-Javadoc)
   *
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    String s = "<Printing actorPath> \n";
    s += this.actor.getVertexPath();
    s += "\n<Printing components and Events> \n";
    s += this.actorEventMap.toString();
    s += "<end printing>\n";

    return s;
  }
}
