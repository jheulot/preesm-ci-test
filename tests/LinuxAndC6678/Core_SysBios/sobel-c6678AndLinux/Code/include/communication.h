/*******************************************************************************
 * Copyright or © or Copr. %%LOWERDATE%% - %%UPPERDATE%% IETR/INSA:
 *
 * %%AUTHORS%%
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
#ifndef COMMUNICATION_H
#define COMMUNICATION_H
#include <xdc/std.h>

void communicationInit();

/**
 * Non-blocking function called by the sender to signal that a buffer is ready
 * to be sent.
 *
 * @param[in] coreID
 *        the ID of the receiver core
 */
void sendStart(Uint16 coreID);

/**
 * Blocking function (not for shared_mem communication) called by the sender to
 * signal that communication is completed.
 */
void sendEnd();

/**
 * Non-blocking function called by the receiver begin receiving the
 * data. (not implemented with shared memory communications).
 */
void receiveStart();

/**
 * Blocking function called by the sender to wait for the received data
 * availability.
 *
 * @param[in] coreID
 *        the ID of the sender core
 */
void receiveEnd(Uint16 coreID);

/**
 * Barrier used to synchronize all the 8 cores of the DSP.
 * The communication must be initialized in order to use this method.
 */
void busy_barrier();

#endif
