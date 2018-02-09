/**
 * Copyright or © or Copr. IETR/INSA - Rennes (2017) :
 *
 * Hamza Deroui <hamza.deroui@insa-rennes.fr> (2017)
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
package org.ietr.preesm.throughput.tools.helpers;

/**
 * stopwatch class for time measurements used in benchmarks
 * 
 * @author hderoui
 *
 */
public class Stopwatch {
  private double  startTime;
  private double  accumulatedTime;
  private String  label;
  private boolean isRunning;
  private String  format;
  private double  timeConverterFactor;

  /**
   * a class constructor with a name for the stopwatch
   * 
   * @param label
   *          a name for the stopwatch
   */
  public Stopwatch(String label) {
    this.label = label;
    this.accumulatedTime = 0;
    this.setTimeSconds();
  }

  /**
   * Default constructor
   */
  public Stopwatch() {
    this.label = "stopwatch";
    this.accumulatedTime = 0;
    this.setTimeSconds();
  }

  /**
   * reset the stopwatch
   */
  public void reset() {
    this.accumulatedTime = 0;
    this.isRunning = false;
  }

  /**
   * start the stopwatch
   */
  public void start() {
    this.reset();
    this.isRunning = true;
    this.startTime = this.currentTime();

  }

  /**
   * stop the stopwatch
   */
  public void stop() {
    if (this.isRunning) {
      this.accumulatedTime += this.currentTime() - startTime;
      this.isRunning = false;
    }
  }

  /**
   * pause the stopwatch
   */
  public void pause() {
    if (this.isRunning) {
      this.accumulatedTime += this.currentTime() - startTime;
    }
  }

  /**
   * resume the stopwatch
   */
  public void resume() {
    this.startTime = this.currentTime();
  }

  /**
   * get the current time of the system
   * 
   * @return current time
   */
  private double currentTime() {
    return (System.currentTimeMillis() * this.timeConverterFactor);
  }

  /**
   * get the value of the stopwacth
   * 
   * @return time value of the stopwatch
   */
  public double value() {
    return this.accumulatedTime;
  }

  /**
   * get the current value of the stopwatch
   * 
   * @return time
   */
  public double watch() {
    if (this.isRunning) {
      return (this.currentTime() - startTime) + this.accumulatedTime;
    } else {
      return 0;
    }
  }

  /**
   * get the name of the stopwatch
   * 
   * @return label
   */
  public String getLabel() {
    return this.label;
  }

  /**
   * set the milliseconds as the time unit of the stopwatch
   */
  public void setTimeMillis() {
    this.timeConverterFactor = 1;
    this.format = "Millis";
  }

  /**
   * set the seconds as the time unit of the stopwatch
   */
  public void setTimeSconds() {
    this.timeConverterFactor = (double) 1 / (1000);
    this.format = "Seconds";
  }

  /**
   * set the minutes as the time unit of the stopwatch
   */
  public void setTimeMinutes() {
    this.timeConverterFactor = (double) 1 / (1000 * 60);
    this.format = "Minutes";
  }

  /**
   * get the stopwatch time unit
   * 
   * @return times unit
   */
  public String getFormat() {
    return this.format;
  }

  @Override
  public String toString() {
    return this.accumulatedTime + " " + this.format;
  }

}
