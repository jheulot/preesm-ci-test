/**
 * Copyright or © or Copr. IETR/INSA - Rennes (2008 - 2019) :
 *
 * Antoine Morvan <antoine.morvan@insa-rennes.fr> (2017 - 2019)
 * Clément Guy <clement.guy@insa-rennes.fr> (2014 - 2015)
 * Daniel Madroñal <daniel.madronal@upm.es> (2018)
 * Matthieu Wipliez <matthieu.wipliez@insa-rennes.fr> (2008)
 * Maxime Pelcat <maxime.pelcat@insa-rennes.fr> (2008 - 2012)
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.resource.Resource;
import org.preesm.model.pisdf.AbstractActor;
import org.preesm.model.pisdf.PiGraph;
import org.preesm.model.pisdf.serialize.PiParser;
import org.preesm.model.scenario.papi.PapifyConfigManager;
import org.preesm.model.slam.Design;
import org.preesm.model.slam.SlamPackage;
import org.preesm.model.slam.serialize.IPXACTResourceFactoryImpl;
import org.preesm.model.slam.serialize.SlamParser;
import org.preesm.model.slam.utils.DesignTools;

/**
 * Storing all information of a scenario.
 *
 * @author mpelcat
 */
public class PreesmScenario {

  /** Manager of constraint groups. */
  private ConstraintGroupManager constraintgroupmanager = null;

  /** Manager of relative constraints. */
  private RelativeConstraintManager relativeconstraintmanager = null;

  /** Manager of timings. */
  private TimingManager timingmanager = null;

  /** Manager of simulation parameters. */
  private SimulationManager simulationManager = null;

  /** Manager of code generation parameters. */
  private CodegenManager codegenManager = null;

  /** Manager of parameters values for PiGraphs. */
  private ParameterValueManager parameterValueManager = null;

  /** Path to the algorithm file. */
  private String algorithmURL = "";

  /** Path to the architecture file. */
  private String architectureURL = "";

  /** current architecture properties. */
  private Set<String> operatorIds = null;

  /** The operator definition ids. */
  private Set<String> operatorDefinitionIds = null;

  /** The com node ids. */
  private Set<String> comNodeIds = null;

  /** Path to the scenario file. */
  private String scenarioURL = "";

  /** Manager of PapifyConfig groups. */
  private PapifyConfigManager papifyconfiggroupmanager = null;

  /**
   * Instantiates a new preesm scenario.
   *
   */
  public PreesmScenario() {
    this.constraintgroupmanager = new ConstraintGroupManager();
    this.relativeconstraintmanager = new RelativeConstraintManager();
    this.timingmanager = new TimingManager();
    this.simulationManager = new SimulationManager();
    this.codegenManager = new CodegenManager();
    this.parameterValueManager = new ParameterValueManager();
    this.papifyconfiggroupmanager = new PapifyConfigManager();
  }

  /**
   * Checks if is IBSDF scenario.
   *
   * @return true, if is IBSDF scenario
   */
  public boolean isProperlySet() {
    final boolean hasProperAlgo = this.algorithmURL.endsWith(".pi");
    final boolean hasProperArchi = this.architectureURL.endsWith(".slam");
    return hasProperAlgo && hasProperArchi;
  }

  /**
   * Util method generating a name for a given PreesmSceario from its architecture and algorithm.
   *
   * @return the scenario name
   */
  public String getScenarioName() {
    final IPath algoPath = new Path(this.getAlgorithmURL()).removeFileExtension();
    final String algoName = algoPath.lastSegment();
    final IPath archiPath = new Path(this.getArchitectureURL()).removeFileExtension();
    final String archiName = archiPath.lastSegment();
    return algoName + "_" + archiName;
  }

  /**
   * Gets the actor names.
   *
   * @return the actor names
   */
  public Set<String> getActorNames() {
    return getPiActorNames();
  }

  /**
   * Gets the pi actor names.
   *
   * @return the pi actor names
   */
  private Set<String> getPiActorNames() {
    final Set<String> result = new LinkedHashSet<>();
    final PiGraph graph = PiParser.getPiGraphWithReconnection(this.algorithmURL);
    for (final AbstractActor vertex : graph.getActors()) {
      result.add(vertex.getName());
    }
    return result;
  }

  /**
   * Gets the constraint group manager.
   *
   * @return the constraint group manager
   */
  public ConstraintGroupManager getConstraintGroupManager() {
    return this.constraintgroupmanager;
  }

  /**
   * Gets the relativeconstraint manager.
   *
   * @return the relativeconstraint manager
   */
  public RelativeConstraintManager getRelativeconstraintManager() {
    return this.relativeconstraintmanager;
  }

  /**
   * Gets the timing manager.
   *
   * @return the timing manager
   */
  public TimingManager getTimingManager() {
    return this.timingmanager;
  }

  /**
   * Gets the algorithm URL.
   *
   * @return the algorithm URL
   */
  public String getAlgorithmURL() {
    return this.algorithmURL;
  }

  /**
   * Sets the algorithm URL.
   *
   * @param algorithmURL
   *          the new algorithm URL
   */
  public void setAlgorithmURL(final String algorithmURL) {
    this.algorithmURL = algorithmURL;
  }

  /**
   * Gets the architecture URL.
   *
   * @return the architecture URL
   */
  public String getArchitectureURL() {
    return this.architectureURL;
  }

  /**
   * Sets the architecture URL.
   *
   * @param architectureURL
   *          the new architecture URL
   */
  public void setArchitectureURL(final String architectureURL) {
    this.architectureURL = architectureURL;
  }

  /**
   * Gets the simulation manager.
   *
   * @return the simulation manager
   */
  public SimulationManager getSimulationManager() {
    return this.simulationManager;
  }

  /**
   * Gets the codegen manager.
   *
   * @return the codegen manager
   */
  public CodegenManager getCodegenManager() {
    return this.codegenManager;
  }

  /**
   * Gets the scenario URL.
   *
   * @return the scenario URL
   */
  public String getScenarioURL() {
    return this.scenarioURL;
  }

  /**
   * Gets the PapifyConfig group manager.
   *
   * @return the PapifyConfig group manager
   */
  public PapifyConfigManager getPapifyConfigManager() {
    return this.papifyconfiggroupmanager;
  }

  /**
   * Sets the new PapifyConfig group manager.
   *
   */
  public void setPapifyConfigManager(final PapifyConfigManager manager) {
    this.papifyconfiggroupmanager = manager;
  }

  /**
   * Sets the scenario URL.
   *
   * @param scenarioURL
   *          the new scenario URL
   */
  public void setScenarioURL(final String scenarioURL) {
    this.scenarioURL = scenarioURL;
  }

  /**
   * Gets the operator ids.
   *
   * @return the operator ids
   */
  public Set<String> getOperatorIds() {
    if (this.operatorIds == null) {
      this.operatorIds = new LinkedHashSet<>();
    }
    return this.operatorIds;
  }

  /**
   * Gets the ordered operator ids.
   *
   * @return the ordered operator ids
   */
  public List<String> getOrderedOperatorIds() {
    final List<String> opIdList = new ArrayList<>(getOperatorIds());
    Collections.sort(opIdList, (o1, o2) -> o1.compareTo(o2));

    return opIdList;
  }

  /**
   * Gets the operator definition ids.
   *
   * @return the operator definition ids
   */
  public Set<String> getOperatorDefinitionIds() {
    if (this.operatorDefinitionIds == null) {
      this.operatorDefinitionIds = new LinkedHashSet<>();
    }
    return this.operatorDefinitionIds;
  }

  /**
   * Sets the operator ids.
   *
   * @param operatorIds
   *          the new operator ids
   */
  public void setOperatorIds(final Set<String> operatorIds) {
    this.operatorIds = operatorIds;
  }

  /**
   * Sets the operator definition ids.
   *
   * @param operatorDefinitionIds
   *          the new operator definition ids
   */
  public void setOperatorDefinitionIds(final Set<String> operatorDefinitionIds) {
    this.operatorDefinitionIds = operatorDefinitionIds;
  }

  /**
   * Gets the com node ids.
   *
   * @return the com node ids
   */
  public Set<String> getComNodeIds() {
    if (this.comNodeIds == null) {
      this.comNodeIds = new LinkedHashSet<>();
    }
    return this.comNodeIds;
  }

  /**
   * Sets the com node ids.
   *
   * @param comNodeIds
   *          the new com node ids
   */
  public void setComNodeIds(final Set<String> comNodeIds) {
    this.comNodeIds = comNodeIds;
  }

  /**
   * Gets the parameter value manager.
   *
   * @return the parameter value manager
   */
  public ParameterValueManager getParameterValueManager() {
    return this.parameterValueManager;
  }

  /**
   * Sets the parameter value manager.
   *
   * @param parameterValueManager
   *          the new parameter value manager
   */
  public void setParameterValueManager(final ParameterValueManager parameterValueManager) {
    this.parameterValueManager = parameterValueManager;
  }

  /**
   * From PiScenario.
   *
   * @param algorithmChange
   *          the algorithm change
   * @param architectureChange
   *          the architecture change
   * @throws InvalidModelException
   *           the invalid model exception
   * @throws CoreException
   *           the core exception
   */

  public void update(final boolean algorithmChange, final boolean architectureChange) throws CoreException {
    // If the architecture changes, operator ids, operator defintion ids and
    // com node ids are no more valid (they are extracted from the
    // architecture)
    if (architectureChange && this.architectureURL.endsWith(".slam")) {
      final Map<String, Object> extToFactoryMap = Resource.Factory.Registry.INSTANCE.getExtensionToFactoryMap();
      Object instance = extToFactoryMap.get("slam");
      if (instance == null) {
        instance = new IPXACTResourceFactoryImpl();
        extToFactoryMap.put("slam", instance);
      }

      if (!EPackage.Registry.INSTANCE.containsKey(SlamPackage.eNS_URI)) {
        EPackage.Registry.INSTANCE.put(SlamPackage.eNS_URI, SlamPackage.eINSTANCE);
      }

      // Extract the root object from the resource.
      final Design design = SlamParser.parseSlamDesign(this.architectureURL);

      getOperatorIds().clear();
      getOperatorIds().addAll(DesignTools.getOperatorInstanceIds(design));

      getOperatorDefinitionIds().clear();
      getOperatorDefinitionIds().addAll(DesignTools.getOperatorComponentIds(design));

      getComNodeIds().clear();
      getComNodeIds().addAll(DesignTools.getComNodeInstanceIds(design));

    }
    // If the algorithm changes, parameters or variables are no more valid
    // (they are set in the algorithm)
    if (algorithmChange) {
      this.parameterValueManager.updateWith(PiParser.getPiGraphWithReconnection(this.algorithmURL));
    }
    // If the algorithm or the architecture changes, timings and constraints
    // are no more valid (they depends on both algo and archi)
    if (algorithmChange || architectureChange) {
      this.timingmanager.getTimings().clear();
      this.constraintgroupmanager.update();
      this.papifyconfiggroupmanager.update();
    }
  }

}
