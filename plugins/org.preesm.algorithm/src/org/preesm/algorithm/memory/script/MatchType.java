/*
 * Copyright or © or Copr. IETR/INSA - Rennes (2018) :
 *
 * Antoine Morvan [antoine.morvan@insa-rennes.fr] (2018)
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
package org.preesm.algorithm.memory.script;

/**
 * This enumeration represent the type of the current {@link Match}
 */
public enum MatchType {

  /**
   * The {@link Match} links several inputs (or outputs) together. Not allowed anymore
   */
  // INTER_SIBLINGS,
  /**
   * The {@link Match} is internal to an actor and links an input {@link Buffer} to an output {@link Buffer}, <b>or</b>
   * the {@link Match} is external to an actor (i.e. correspond to an edge) and it links an output {@link Buffer} of an
   * actor to the input {@link Buffer} of the next.
   */
  FORWARD,

  /**
   * Opposite of FORWARD
   */
  BACKWARD
}
