/**
 * Copyright or © or Copr. IETR/INSA - Rennes (2008 - 2018) :
 *
 * Antoine Morvan <antoine.morvan@insa-rennes.fr> (2017 - 2018)
 * Clément Guy <clement.guy@insa-rennes.fr> (2014)
 * Florian Arrestier <florian.arrestier@insa-rennes.fr> (2018)
 * Jonathan Piat <jpiat@laas.fr> (2009 - 2011)
 * Julien Heulot <julien.heulot@insa-rennes.fr> (2015)
 * Matthieu Wipliez <matthieu.wipliez@insa-rennes.fr> (2008)
 * Maxime Pelcat <maxime.pelcat@insa-rennes.fr> (2008 - 2013)
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

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.preesm.model.scenario.serialize.CsvTimingParser;
import org.preesm.model.scenario.serialize.ExcelTimingParser;
import org.preesm.model.slam.component.Component;

/**
 * Manager of the graphs timings.
 *
 * @author mpelcat
 */
public class TimingManager {

  /** List of all timings. */
  private final List<Timing> timings;

  /** Path to a file containing timings. */
  private String excelFileURL = "";

  /** Storing setup time and speed of memcpy for each type of operator. */
  private final Map<Component, MemCopySpeed> memcpySpeeds;

  /** Default value for a memcpy setup time. */
  private static final long DEFAULTMEMCPYSETUPTIME = 1;

  /** Default value for a memcpy speed. */
  private static final float DEFAULTMEMCPYTIMEPERUNIT = 1.0f;

  private final PreesmScenario preesmScenario;

  /**
   * Instantiates a new timing manager.
   */
  public TimingManager(final PreesmScenario preesmScenario) {
    this.preesmScenario = preesmScenario;
    this.timings = new ArrayList<>();
    this.memcpySpeeds = new LinkedHashMap<>();
  }

  /**
   * Adds the timing.
   *
   * @param newt
   *          the newt
   * @return the timing
   */
  public Timing addTiming(final Timing newt) {

    for (final Timing timing : this.timings) {
      if (timing.equals(newt)) {
        timing.setTime(newt.getTime());
        return timing;
      }
    }

    this.timings.add(newt);
    return newt;
  }

  /**
   * Adds the timing.
   *
   * @param dagVertexId
   *          the dag vertex id
   * @param operatorDefinitionId
   *          the operator definition id
   * @return the timing
   */
  public Timing addTiming(final String dagVertexId, final Component operatorDefinitionId) {

    final Timing newt = new Timing(operatorDefinitionId, dagVertexId);
    for (final Timing timing : this.timings) {
      if (timing.equals(newt)) {
        return timing;
      }
    }

    this.timings.add(newt);
    return newt;
  }

  /**
   * Sets the timing.
   *
   * @param dagVertexId
   *          the dag vertex id
   * @param operatorDefinitionId
   *          the operator definition id
   * @param time
   *          the time
   */
  public void setTiming(final String dagVertexId, final Component operatorDefinitionId, final long time) {
    addTiming(dagVertexId, operatorDefinitionId).setTime(time);
  }

  /**
   * Sets the timing.
   *
   * @param dagVertexId
   *          the dag vertex id
   * @param operatorDefinitionId
   *          the operator definition id
   * @param value
   *          the value
   */
  public void setTiming(final String dagVertexId, final Component operatorDefinitionId, final String value) {
    addTiming(dagVertexId, operatorDefinitionId).setStringValue(value);
  }

  /**
   * Looks for a timing entered in scenario editor. If there is none, returns a default value
   *
   * @param dagVertexId
   *          the dag vertex id
   * @param operatorDefinitionId
   *          the operator definition id
   * @return the timing or default
   */
  public Timing getTimingOrDefault(final String dagVertexId, final Component operatorDefinitionId) {
    Timing val = null;

    for (final Timing timing : this.timings) {
      if (timing.getVertexId().equals(dagVertexId) && timing.getOperatorDefinitionId().equals(operatorDefinitionId)) {
        val = timing;
      }
    }

    if (val == null) {
      val = new Timing(operatorDefinitionId, dagVertexId);
    }

    return val;
  }

  /**
   * Gets the timings.
   *
   * @return the timings
   */
  public List<Timing> getTimings() {

    return this.timings;
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
   * Import timings.
   *
   * @param currentScenario
   *          the current scenario
   */
  public void importTimings(final PreesmScenario currentScenario) {
    if (!this.excelFileURL.isEmpty() && (currentScenario != null)) {
      final ExcelTimingParser excelParser = new ExcelTimingParser(currentScenario);
      final CsvTimingParser csvParser = new CsvTimingParser(currentScenario);

      try {
        final String[] fileExt = this.excelFileURL.split("\\.");
        switch (fileExt[fileExt.length - 1]) {
          case "xls":
            excelParser.parse(this.excelFileURL, currentScenario.getOperatorDefinitions());
            break;
          case "csv":
            csvParser.parse(this.excelFileURL, currentScenario.getOperatorDefinitions());
            break;
          default:
        }
      } catch (final Exception e) {
        e.printStackTrace();
      }
    }
  }

  /**
   * For a type of operator, sets a memcopy setup time and speed.
   *
   * @param speed
   *          the speed
   */
  public void putMemcpySpeed(final MemCopySpeed speed) {
    this.memcpySpeeds.put(speed.getOperatorDef(), speed);
  }

  /**
   * For a type of operator, gets a memcopy setup time.
   *
   * @param operatorDef
   *          the operator def
   * @return the memcpy setup time
   */
  public long getMemcpySetupTime(final Component operatorDef) {
    return this.memcpySpeeds.get(operatorDef).getSetupTime();
  }

  /**
   * For a type of operator, gets the INVERSED memcopy speed (time per memory unit.
   *
   * @param operatorDef
   *          the operator def
   * @return the memcpy time per unit
   */
  public double getMemcpyTimePerUnit(final Component operatorDef) {
    return this.memcpySpeeds.get(operatorDef).getTimePerUnit();
  }

  /**
   * Gets the memcpy speeds.
   *
   * @return the memcpy speeds
   */
  public Map<Component, MemCopySpeed> getMemcpySpeeds() {
    return this.memcpySpeeds;
  }

  /**
   * Checks for mem cpy speed.
   *
   * @param operatorDef
   *          the operator def
   * @return true, if successful
   */
  public boolean hasMemCpySpeed(final Component operatorDef) {
    return this.memcpySpeeds.keySet().contains(operatorDef);
  }

  /**
   * Sets the default mem cpy speed.
   *
   * @param operatorDef
   *          the new default mem cpy speed
   */
  public void setDefaultMemCpySpeed(final Component operatorDef) {
    putMemcpySpeed(
        new MemCopySpeed(operatorDef, TimingManager.DEFAULTMEMCPYSETUPTIME, TimingManager.DEFAULTMEMCPYTIMEPERUNIT));
  }
}
