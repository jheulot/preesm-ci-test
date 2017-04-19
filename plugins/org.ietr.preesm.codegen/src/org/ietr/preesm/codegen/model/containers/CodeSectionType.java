/*******************************************************************************
 * Copyright or © or Copr. 2012 - 2017 IETR/INSA:
 *
 * Antoine Morvan <antoine.morvan@insa-rennes.fr> (2017)
 * Maxime Pelcat <Maxime.Pelcat@insa-rennes.fr> (2012)
 *
 * This software is a computer program whose purpose is to prototype
 * parallel applications.
 *
 * This software is governed by the CeCILL-C license under French law and
 * abiding by the rules of distribution of free software.  You can  use
 * modify and/ or redistribute the software under the terms of the CeCILL-C
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
 * knowledge of the CeCILL-C license and that you accept its terms.
 *******************************************************************************/

package org.ietr.preesm.codegen.model.containers;

// TODO: Auto-generated Javadoc
/**
 * Defining a type of section in a thread.
 *
 * @author mpelcat
 */
public class CodeSectionType {

  /**
   * The Enum MajorType.
   */
  public enum MajorType {

    /** The fifoinit. */
    FIFOINIT,
    /** The cominit. */
    COMINIT,
    /** The init. */
    INIT,
    /** The loop. */
    LOOP
  }

  /** Main code block identification. */
  private final MajorType major;

  /** sub code block identification. */
  private int minor = -1;

  /**
   * creating a section type with major and minor.
   *
   * @param major
   *          the major
   * @param minor
   *          the minor
   */
  public CodeSectionType(final MajorType major, final int minor) {
    super();
    this.major = major;
    this.minor = minor;
  }

  /**
   * creating a section type with only major.
   *
   * @param major
   *          the major
   */
  public CodeSectionType(final MajorType major) {
    super();
    this.major = major;
    this.minor = -1;
  }

  /**
   * Gets the major.
   *
   * @return the major
   */
  public MajorType getMajor() {
    return this.major;
  }

  /**
   * Gets the minor.
   *
   * @return the minor
   */
  public int getMinor() {
    return this.minor;
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    if (this.major.equals(MajorType.LOOP)) {
      return "LOOP";
    } else if (this.major.equals(MajorType.COMINIT)) {
      return "COMINIT";
    } else if (this.major.equals(MajorType.INIT)) {
      return "(INIT," + this.minor + ")";
    } else if (this.major.equals(MajorType.FIFOINIT)) {
      return "FIFOINIT";
    }
    return "";
  }
}
