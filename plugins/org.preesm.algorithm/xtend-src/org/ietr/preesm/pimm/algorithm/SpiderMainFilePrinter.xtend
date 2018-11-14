/**
 * Copyright or © or Copr. IETR/INSA - Rennes (2017 - 2018) :
 *
 * Antoine Morvan <antoine.morvan@insa-rennes.fr> (2017 - 2018)
 * Florian Arrestier <florian.arrestier@insa-rennes.fr> (2018)
 * Karol Desnos <karol.desnos@insa-rennes.fr> (2017)
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
package org.ietr.preesm.pimm.algorithm

import java.util.Date
import org.preesm.model.pisdf.Actor
import org.preesm.model.pisdf.CHeaderRefinement
import org.preesm.model.pisdf.ConfigInputPort
import org.preesm.model.pisdf.Parameter
import org.preesm.model.pisdf.PiGraph

class SpiderMainFilePrinter {

	var refinementDone = newArrayList
	var initRefinementDone = newArrayList

	def String print(PiGraph pg, int nbCores, boolean usingPapify) '''
	/**
	 * @file main.cpp
	 * @generated by «SpiderMainFilePrinter»
	 * @date «new Date»
	 *
	 */

	#include <spider.h>

	#include <stdio.h>
	#include <stdlib.h>
	#include <stdexcept>

	/* Include your files here */
«printRefinementRec(pg)»

	#include "../generated/«pg.name».h"

	#define SH_MEM_SIZE 0x04000000

	#define NB_LRT «nbCores»

	#define NB_ITERATION 10000

	#define STACK_IS_DYNAMIC (1)

	#if STACK_IS_DYNAMIC == (1)

	static char* archiStack;
	static char* lrtStack;
	static char* pisdfStack;
	static char* srdagStack;
	static char* transfoStack;

	#else

	#define ARCHI_SIZE 		128	*1024	*NB_LRT
	#define LRT_SIZE 		16	*1024	*NB_LRT
	#define PISDF_SIZE 		4	*1024*1024
	#define SRDAG_SIZE 		8	*1024*1024 *100
	#define TRANSFO_SIZE 	1	*1024*1024 *20

	static char archiStack[ARCHI_SIZE];
	static char lrtStack[LRT_SIZE];
	static char pisdfStack[PISDF_SIZE];
	static char srdagStack[SRDAG_SIZE];
	static char transfoStack[TRANSFO_SIZE];

	#endif

	int stopThreads;

	int main(int argc, char* argv[]){
		SpiderConfig cfg;
		ExecutionStat stat;

		// Setting memory info
		cfg.memAllocType = MEMALLOC_DUMMY;
		cfg.memAllocStart = (void*)0;
		cfg.memAllocSize = SH_MEM_SIZE;


		// Setting scheduler
		cfg.schedulerType = SCHEDULER_LIST;


		// Declaring stacks
		cfg.archiStack.name = "ArchiStack";
		cfg.archiStack.start = archiStack;

		cfg.lrtStack.name = "LrtStack";
		cfg.lrtStack.start = lrtStack;

		cfg.pisdfStack.name = "PiSDFStack";
		cfg.pisdfStack.start = pisdfStack;

		cfg.srdagStack.name = "SrdagStack";
		cfg.srdagStack.start = srdagStack;

		cfg.transfoStack.name = "TransfoStack";
		cfg.transfoStack.start = transfoStack;


	#if STACK_IS_DYNAMIC == 1

		cfg.archiStack.type = STACK_DYNAMIC;

		cfg.lrtStack.type = STACK_DYNAMIC;

		cfg.pisdfStack.type = STACK_DYNAMIC;

		cfg.srdagStack.type = STACK_DYNAMIC;

		cfg.transfoStack.type = STACK_DYNAMIC;

	#else
		cfg.archiStack.type = STACK_STATIC;
		cfg.archiStack.size = ARCHI_SIZE;

		cfg.lrtStack.type = STACK_STATIC;
		cfg.lrtStack.size = LRT_SIZE;

		cfg.pisdfStack.type = STACK_STATIC;
		cfg.pisdfStack.size = PISDF_SIZE;

		cfg.srdagStack.type = STACK_STATIC;
		cfg.srdagStack.size = SRDAG_SIZE;

		cfg.transfoStack.type = STACK_STATIC;
		cfg.transfoStack.size = TRANSFO_SIZE;
	#endif

		// Setting desired number of LRT/thread
		cfg.platform.nLrt = NB_LRT;

		// Setting size of shared mem
		cfg.platform.shMemSize = SH_MEM_SIZE;

		// Setting graph PiSDF graph
		cfg.platform.fcts = «pg.name»_fcts;
		cfg.platform.nLrtFcts = N_FCT_«pg.name.toUpperCase»;

		// Initialize the architecture information
		if (init_archi_infos(&cfg.platform) < 0) {
			fprintf(stderr, "ERROR: failed to initialize the architecture.\n");
			return -1;
		}

		cfg.verbose = false;
		cfg.traceEnabled = false;
		cfg.useGraphOptim = true;

		«IF usingPapify»
			// Papify initialization
			cfg.usePapify = true;
			cfg.papifyJobInfo = get_«pg.name»_papifyConfigs();
		«ELSE»
			// Papify instrumentation will not be used
			cfg.usePapify = false;
		«ENDIF»

		try {
			// Spider initialisation
			Spider::init(cfg);

«printInitCallRec(pg)»
			// PiSDF graph construction
			init_«pg.name»();

			// Reserving memory for persistent delays
			Spider::initReservedMemory();

			printf("Start\n");

			// Main loop, exception handling can be removed to increase performance
			for(int i=0; i<NB_ITERATION && !stopThreads; i++){
				// Compute the SR-DAG, scheduling and executing the main graph
				Spider::iterate();

				// Printing Gantt
				if (cfg.traceEnabled) {
					Spider::printGantt("gantt.pgantt", "gantt_tex.dat", &stat);
					printf("Total execution time: %lf ms\n",  (stat.execTime + stat.schedTime) / 1000000.);
					printf("Application execution time: %lf ms\n",  stat.execTime / 1000000.);
					printf("SPIDER overhead time: %lf ms\n",  stat.schedTime / 1000000.);
				}
			}

			printf("finished\n");

			// PiSDF graph destruction
			free_«pg.name»();

			Spider::clean();

			«IF usingPapify»
					 // Freeing PapifyConfigs
					free_«pg.name»_papifyConfigs(cfg.papifyJobInfo);
			«ENDIF»

			// Actor finalisation here if needed

			// Free the information linked to the architecture
			free_archi_infos(&cfg.platform);

		} catch(std::exception &e) {
			printf("Exception : %s\n", e.what());
		}

		return 0;
	}


	'''

	def static String printInitCall(Actor actor) '''
	  «val proto = (actor.refinement as CHeaderRefinement).getInitPrototype»
	  «proto.name»(«FOR param : proto.parameters SEPARATOR ", "»«
	   ((actor.lookupPort(param.name) as ConfigInputPort).incomingDependency.setter as Parameter).valueExpression.evaluate.toString»«ENDFOR»);
	'''

	def CharSequence printInitCallRec(PiGraph g) '''
		«IF !g.actorsWithRefinement.isEmpty()»
		«"\t\t// Actor initializations of graph " + g.vertexPath»
			«FOR actor : g.actorsWithRefinement»
				«IF actor.refinement instanceof CHeaderRefinement && (actor.refinement as CHeaderRefinement).getInitPrototype !== null»
					«IF !this.initRefinementDone.contains(printInitCall(actor))»
«"\t\t"+ printInitCall(actor)»
						«{this.initRefinementDone.add(printInitCall(actor)) ""}»
			        «ENDIF»
				«ENDIF»
			«ENDFOR»
  		«ENDIF»

		«FOR cg : g.childrenGraphs»
			«printInitCallRec(cg)»
		«ENDFOR»
	'''

	def CharSequence printRefinementRec(PiGraph g) '''
		«FOR actor : g.actorsWithRefinement»
		  «IF actor.refinement instanceof CHeaderRefinement && (actor.refinement as CHeaderRefinement).getInitPrototype !== null»
		  	«IF !this.refinementDone.contains(actor.refinement.fileName)»
		  	«"#include <" + actor.refinement.getFileName + ">"»
		  	«{this.refinementDone.add(actor.refinement.fileName) ""}»
		  	«ENDIF»
		  «ENDIF»
		«ENDFOR»
		«FOR cg : g.childrenGraphs»
		«printRefinementRec(cg)»
		«ENDFOR»
	'''

}
