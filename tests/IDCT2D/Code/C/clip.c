/*******************************************************************************
 * Copyright or © or Copr. 2009 - 2017 IETR/INSA:
 *
 * Antoine Morvan <antoine.morvan@insa-rennes.fr> (2017)
 * Jonathan Piat <jpiat@laas.fr> (2009)
 * Maxime Pelcat <Maxime.Pelcat@insa-rennes.fr> (2010)
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
/* Generated by CIL v. 1.3.6 */
/* print_CIL_Input is false */

void clip(int * I, int * sign , int *O ) ;
void RightShift(int * x , int * Out ) ;


void RightShift(int * x , int * Out ) 
{ int i ;

  {
  i = 0;
  while (i < 64) {
    Out[i] = x[i] >> 13;
    i ++;
  }
}
}

void clip(int * I, int * sign , int *O ) 
{ int min ;
  int y [64] ;
  int i = 0;
  int _if_5 ;
  RightShift(I, y);

  {
  if (*sign) {
    _if_5 = -255;
  } else {
    _if_5 = 0;
  }
  min = _if_5;
  while (i < 64) {
	  if (y[i] > 255) {
		O[i] = 255;
	  } else {
		if (y[i] < min) {
		  O[i] = min;
		} else {
		  O[i] = y[i];
		}
	  }
	  i ++ ;
  }
}
}
