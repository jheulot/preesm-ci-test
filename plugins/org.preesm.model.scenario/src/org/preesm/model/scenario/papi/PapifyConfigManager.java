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

import java.util.LinkedHashSet;
import java.util.Set;
import org.preesm.model.scenario.PreesmScenario;
import org.preesm.model.slam.component.Component;

/**
 * container and manager of PapifyConfig groups. It can load and store PapifyConfig groups
 *
 * @author dmadronal
 */
public class PapifyConfigManager {

  private PapiEventInfo papiData = null;

  /** List of all PapifyConfig groups for Actors. */
  private final Set<PapifyConfigActor> papifyConfigGroupsActors;
  /** List of all PapifyConfig groups for PEs. */
  private final Set<PapifyConfigPE>    papifyConfigGroupsPEs;

  /** Path to a file containing constraints. */
  private String xmlFileURL = "";

  private final PreesmScenario preesmScenario;

  /**
   * Instantiates a new PapifyConfig manager.
   */
  public PapifyConfigManager(final PreesmScenario preesmScenario) {
    this.preesmScenario = preesmScenario;
    this.papifyConfigGroupsActors = new LinkedHashSet<>();
    this.papifyConfigGroupsPEs = new LinkedHashSet<>();
  }

  /**
   * Adds PAPI data.
   *
   * @param data
   *          the data
   */
  public void addPapifyData(final PapiEventInfo data) {

    this.papiData = data;
  }

  /**
   * Get PAPI data.
   *
   */
  public PapiEventInfo getPapifyData() {

    return this.papiData;
  }

  /**
   * Adds a PapifyConfigActor group.
   *
   * @param pg
   *          the pg
   */
  public void addPapifyConfigActorGroup(final PapifyConfigActor pg) {

    if (!this.papifyConfigGroupsActors.contains(pg)) {
      this.papifyConfigGroupsActors.add(pg);
    }
  }

  /**
   * Adds a PapifyConfigPE group.
   *
   * @param pg
   *          the pg
   */
  public void addPapifyConfigPEGroup(final PapifyConfigPE pg) {

    if (!this.papifyConfigGroupsPEs.contains(pg)) {
      this.papifyConfigGroupsPEs.add(pg);
    }
  }

  /**
   * Adding a component to the core.
   *
   * @param opId
   *          the op id
   * @param component
   *          the PAPI component
   */
  public void addComponent(final Component opId, final PapiComponent component) {

    final PapifyConfigPE pgSet = getCorePapifyConfigGroupPE(opId);

    if (pgSet == null) {
      final PapifyConfigPE pg = new PapifyConfigPE(opId);
      pg.addPAPIComponent(component);
      this.papifyConfigGroupsPEs.add(pg);
    } else {
      pgSet.addPAPIComponent(component);
    }

  }

  /**
   * Removes the component from the PapifyConfig for the core.
   *
   * @param opId
   *          the op id
   * @param component
   *          the PAPI component
   */
  public void removeComponent(final Component opId, final PapiComponent component) {
    final PapifyConfigPE pgSet = getCorePapifyConfigGroupPE(opId);

    if (pgSet != null) {
      pgSet.removePAPIComponent(component);
    }
  }

  /**
   * Adds an event to the PapifyConfig for the core.
   *
   * @param opId
   *          the op id
   * @param event
   *          the PAPI event
   */
  public void addEvent(final String opId, final String component, final PapiEvent event) {

    final PapifyConfigActor pgSet = getCorePapifyConfigGroupActor(opId);

    pgSet.addPAPIEvent(component, event);
  }

  /**
   * Removes an event from the PapifyConfig for the actor.
   *
   * @param opId
   *          the op id
   * @param event
   *          the PAPI event
   */
  public void removeEvent(final String opId, final String component, final PapiEvent event) {
    final PapifyConfigActor pgSet = getCorePapifyConfigGroupActor(opId);

    if (pgSet != null) {
      pgSet.removePAPIEvent(component, event);
    }
  }

  /**
   * Gets the PapifyConfigActors groups.
   *
   * @return the PapifyConfigActors groups
   */
  public Set<PapifyConfigActor> getPapifyConfigGroupsActors() {

    return this.papifyConfigGroupsActors;
  }

  /**
   * Gets the PapifyConfigPE groups.
   *
   * @return the PapifyConfigPE groups
   */
  public Set<PapifyConfigPE> getPapifyConfigGroupsPEs() {

    return this.papifyConfigGroupsPEs;
  }

  /**
   * Gets the op PapifyConfigActor group.
   *
   * @param opPath
   *          the op path
   * @return the op PapifyConfigActor groups
   */
  public PapifyConfigActor getCorePapifyConfigGroupActor(final String opPath) {
    PapifyConfigActor papifyConfigGroup = null;

    for (final PapifyConfigActor pg : this.papifyConfigGroupsActors) {
      if (pg.isActorPath(opPath)) {
        papifyConfigGroup = pg;
      }
    }

    return papifyConfigGroup;
  }

  /**
   * Gets the op PapifyConfigPE group.
   *
   * @param opId
   *          the op id
   * @return the op PapifyConfigActor groups
   */
  public PapifyConfigPE getCorePapifyConfigGroupPE(final Component opId) {
    PapifyConfigPE papifyConfigGroup = null;

    for (final PapifyConfigPE pg : this.papifyConfigGroupsPEs) {
      if (pg.ispeType(opId)) {
        papifyConfigGroup = pg;
      }
    }

    return papifyConfigGroup;
  }

  /**
   * Removes the all.
   */
  public void removeAllCores() {

    this.papifyConfigGroupsActors.clear();
  }

  /**
   * Removes the all.
   */
  public void removeAllPEs() {

    this.papifyConfigGroupsPEs.clear();
  }

  /**
   * Gets the xml file URL.
   *
   * @return the xml file URL
   */
  public String getXmlFileURL() {
    return this.xmlFileURL;
  }

  /**
   * Sets the xml file URL.
   *
   * @param xmlFileURL
   *          the new xml file URL
   */
  public void setExcelFileURL(final String xmlFileURL) {
    this.xmlFileURL = xmlFileURL;
  }

  /*
   * (non-Javadoc)
   *
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    final StringBuilder s = new StringBuilder();

    for (final PapifyConfigActor pg : this.papifyConfigGroupsActors) {
      s.append(pg.toString());
    }
    for (final PapifyConfigPE pg : this.papifyConfigGroupsPEs) {
      s.append(pg.toString());
    }

    return s.toString();
  }

  /**
   * Update.
   */
  public void update() {
    removeAllCores();
    removeAllPEs();
  }
}
