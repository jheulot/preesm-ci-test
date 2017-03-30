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
#ifndef UTILS_H
#define UTILS_H

#include <xdc/std.h>
/**
 * Sets the MPAX register for the indexed segment.</br>
 * The segment is configured to be readable and writable by users and
 * supervisors
 *
 * @param[in] index
 * The index of the segment used for this translation.
 * @param[in] bAddr
 * The virtual address used to access memory
 * @param[in] rAddr
 * The real address accessed when using the virtual address
 * @param[in] segSize
 * Code for the size of the translated address range.
 * @param[in] cacheable
 * Whether the segment should be cacheable or not
 *
 * @see sprugw0c for more information on the parameters
 */
extern Bool isEnded[1];

void set_MPAX(int index, Uint32 bAddr, Uint32 rAddr, Uint8 segSize, Bool cacheable);

int endExecution();

#endif
