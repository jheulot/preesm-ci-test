/**
 * Copyright or © or Copr. IETR/INSA - Rennes (2008 - 2018) :
 *
 * Antoine Morvan <antoine.morvan@insa-rennes.fr> (2017 - 2018)
 * Clément Guy <clement.guy@insa-rennes.fr> (2014)
 * Jonathan Piat <jpiat@laas.fr> (2011)
 * Matthieu Wipliez <matthieu.wipliez@insa-rennes.fr> (2008)
 * Maxime Pelcat <maxime.pelcat@insa-rennes.fr> (2008 - 2015)
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
package org.preesm.model.scenario;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.eclipse.core.runtime.CoreException;
import org.preesm.model.pisdf.AbstractActor;
import org.preesm.model.scenario.serialize.ExcelConstraintsParser;
import org.preesm.model.slam.ComponentInstance;

/**
 * container and manager of Constraint groups. It can load and store constraint groups
 *
 * @author mpelcat
 */
public class ConstraintGroupManager {

  /** List of all constraint groups. */
  private final Map<ComponentInstance, List<AbstractActor>> constraintgroups = new LinkedHashMap<>();

  /** Path to a file containing constraints. */
  private String excelFileURL = "";

  private final PreesmScenario preesmScenario;

  /**
   * Instantiates a new constraint group manager.
   */
  public ConstraintGroupManager(final PreesmScenario preesmScenario) {
    this.preesmScenario = preesmScenario;
  }

  /**
   * Adds the constraint.
   *
   * @param cmpInstance
   *          the op id
   * @param actor
   *          the vertex
   */
  public void addConstraint(final ComponentInstance cmpInstance, final AbstractActor actor) {
    if (!constraintgroups.containsKey(cmpInstance)) {
      constraintgroups.put(cmpInstance, new ArrayList<>());
    }
    constraintgroups.get(cmpInstance).add(actor);
  }

  /**
   * Adding a constraint group on several vertices and one core.
   *
   * @param cmpInstance
   *          the op id
   * @param actors
   *          the vertex set
   */
  public void addConstraints(final ComponentInstance cmpInstance, final Collection<AbstractActor> actors) {
    if (!constraintgroups.containsKey(cmpInstance)) {
      constraintgroups.put(cmpInstance, new ArrayList<>());
    }
    constraintgroups.get(cmpInstance).addAll(actors);
  }

  /**
   * Removes the constraint.
   *
   * @param cmpInstance
   *          the op id
   * @param actor
   *          the vertex
   */
  public void removeConstraint(final ComponentInstance cmpInstance, final AbstractActor actor) {
    if (constraintgroups.containsKey(cmpInstance)) {
      constraintgroups.get(cmpInstance).remove(actor);
    }
  }

  /**
   * Gets the constraint groups.
   *
   * @return the constraint groups
   */
  public Map<ComponentInstance, List<AbstractActor>> getConstraintGroups() {
    return Collections.unmodifiableMap(this.constraintgroups);
  }

  /**
   * Gets the op constraint groups.
   *
   * @param cmpInstance
   *          the op id
   * @return the op constraint groups
   */
  public List<AbstractActor> getOpConstraintGroups(final ComponentInstance cmpInstance) {
    if (!this.constraintgroups.containsKey(cmpInstance)) {
      this.constraintgroups.put(cmpInstance, new ArrayList<>());
    }
    return this.constraintgroups.get(cmpInstance);
  }

  /**
   * Removes the all.
   */
  public void removeAll() {
    this.constraintgroups.clear();
  }

  /*
   * (non-Javadoc)
   *
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder();
    for (final Entry<ComponentInstance, List<AbstractActor>> cg : this.constraintgroups.entrySet()) {
      sb.append(cg.toString());
    }
    return sb.toString();
  }

  /**
   * Gets the excel file URL.
   *
   * @return the excel file URL
   */
  public String getExcelFileURL() {
    return this.excelFileURL;
  }

  /**
   * Sets the excel file URL.
   *
   * @param excelFileURL
   *          the new excel file URL
   */
  public void setExcelFileURL(final String excelFileURL) {
    this.excelFileURL = excelFileURL;
  }

  /**
   * Import constraints.
   *
   * @param currentScenario
   *          the current scenario
   * @throws FileNotFoundException
   *           the file not found exception
   * @throws CoreException
   *           the core exception
   */
  public void importConstraints(final PreesmScenario currentScenario) throws FileNotFoundException, CoreException {
    if (!this.excelFileURL.isEmpty() && (currentScenario != null)) {
      final ExcelConstraintsParser parser = new ExcelConstraintsParser(currentScenario);
      parser.parse(this.excelFileURL);
    }
  }

  /**
   * Update.
   */
  public void update() {
    removeAll();
  }
}
