/**
 * Copyright or © or Copr. IETR/INSA - Rennes (2017 - 2019) :
 *
 * Antoine Morvan <antoine.morvan@insa-rennes.fr> (2017 - 2018)
 * Florian Arrestier <florian.arrestier@insa-rennes.fr> (2018 - 2019)
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
package org.preesm.codegen.xtend.spider

import java.util.Date
import org.preesm.model.pisdf.Actor
import org.preesm.model.pisdf.CHeaderRefinement
import org.preesm.model.pisdf.ConfigInputPort
import org.preesm.model.pisdf.Parameter
import org.preesm.model.pisdf.PiGraph
import org.preesm.codegen.xtend.spider.utils.SpiderConfig
import java.util.List

class SpiderMainFilePrinter {

	var refinementDone = newArrayList
	var initRefinementDone = newArrayList

	def String print(PiGraph pg, List<String> coreTypeNameList, SpiderConfig spiderConfig) '''
	/**
	 * @file main.cpp
	 * @generated by «SpiderMainFilePrinter»
	 * @date «new Date»
	 *
	 */

	/* Spider library include */
	#include <spider.h>

	#include <stdio.h>
	#include <stdlib.h>
	#include <stdexcept>
	#include <string.h>

	/* Include your files here */
	«printRefinementRec(pg)»

	#include "../generated/«pg.name».h"

	#define NB_ITERATION 10000

	«IF spiderConfig.isStackDynamic»
		#define STACK_IS_DYNAMIC (1)
	«ELSE»
		#define STACK_IS_DYNAMIC (0)
	«ENDIF»

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

		/* == Setting memory info == */
		cfg.memAllocType = «spiderConfig.memoryAllocType»;
		cfg.memAllocStart = 0;
		cfg.memAllocSize = SH_MEM_SIZE;

		/* == Setting scheduler == */
		cfg.schedulerType = «spiderConfig.schedulerType»;

		/* == Declaring stacks == */
		SpiderStackConfig stackConfig;
		stackConfig.archiStack.name = "ArchiStack";
		stackConfig.archiStack.start = archiStack;
		stackConfig.lrtStack.name = "LrtStack";
		stackConfig.lrtStack.start = lrtStack;
		stackConfig.pisdfStack.name = "PiSDFStack";
		stackConfig.pisdfStack.start = pisdfStack;
		stackConfig.srdagStack.name = "SrdagStack";
		stackConfig.srdagStack.start = srdagStack;
		stackConfig.transfoStack.name = "TransfoStack";
		stackConfig.transfoStack.start = transfoStack;


	#if STACK_IS_DYNAMIC == 1
		stackConfig.archiStack.type = StackType::DYNAMIC;
		stackConfig.lrtStack.type = StackType::DYNAMIC;
		stackConfig.pisdfStack.type = StackType::DYNAMIC;
		stackConfig.srdagStack.type = StackType::DYNAMIC;
		stackConfig.transfoStack.type = StackType::DYNAMIC;
	#else
		stackConfig.archiStack.type = StackType::STATIC;
		stackConfig.archiStack.size = ARCHI_SIZE;
		stackConfig.lrtStack.type = StackType::STATIC;
		stackConfig.lrtStack.size = LRT_SIZE;
		stackConfig.pisdfStack.type = StackType::STATIC;
		stackConfig.pisdfStack.size = PISDF_SIZE;
		stackConfig.srdagStack.type = StackType::STATIC;
		stackConfig.srdagStack.size = SRDAG_SIZE;
		stackConfig.transfoStack.type = StackType::STATIC;
		stackConfig.transfoStack.size = TRANSFO_SIZE;
	#endif

		/* == Setting graph PiSDF graph == */
		cfg.fcts = «pg.name»_fcts;
		cfg.nLrtFcts = N_FCT_«pg.name.toUpperCase»;

		/* == Verbosity of spider output == */
		cfg.verbose = «spiderConfig.useOfVerbose»;
		/* == Enables output trace of spider == */
		cfg.traceEnabled = «spiderConfig.useOfTrace»;
		/* == Enables graph optimizations (may impact performance) == */
		cfg.useGraphOptim = «spiderConfig.useOfGraphOptims»;
		
		/* == Papify instrumentation == */
		«IF spiderConfig.useOfPapify»
			cfg.usePapify = true;
			cfg.papifyJobInfo = get_«pg.name»_papifyConfigs();
		«ELSE»
			cfg.usePapify = false;
		«ENDIF»

		try {
			/* == Spider stacks init == */
			Spider::initStacks(stackConfig);
			
			/* == Spider Archi init == */
			initArchi();
			
			/* == Spider initialisation == */
			Spider::init(cfg, stackConfig);

«printInitCallRec(pg)»
			/* == PiSDF graph construction == */
			init_«pg.name»();

			/* == Reserving memory for persistent delays == */
			Spider::initReservedMemory();

			fprintf(stderr, "INFO: Application execution -- START\n");

			/* == Main loop, exception handling can be removed to increase performance == */
			for(int i=0; i<NB_ITERATION && !stopThreads; i++){
				/* == Compute the SR-DAG, scheduling and executing the main graph == */
				Spider::iterate();

				/* == Printing Gantt == */
				if (cfg.traceEnabled) {
					Spider::printGantt("gantt.pgantt", "gantt_tex.dat", &stat);
					fprintf(stderr, "INFO: Total execution time:       %lf ms\n",  (stat.execTime + stat.schedTime) / 1000000.);
					fprintf(stderr, "INFO: Application execution time: %lf ms\n",  stat.execTime / 1000000.);
					fprintf(stderr, "INFO: SPIDER overhead time:       %lf ms\n",  stat.schedTime / 1000000.);
				}
			}

			fprintf(stderr, "INFO: Application execution -- FINISHED\n");

			/* == Cleaning everything spider related == */
			Spider::clean();
			«IF spiderConfig.useOfPapify»
					 // Freeing PapifyConfigs
					free_«pg.name»_papifyConfigs(cfg.papifyJobInfo);
			«ENDIF»

			/* == Free memory buffer(s) of the Archi == */
			freeArchi();

		} catch(std::exception &e) {
			fprintf(stderr, "%s\n", e.what());
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
			«FOR actor : g.actorsWithRefinement»
				«IF actor.refinement instanceof CHeaderRefinement && (actor.refinement as CHeaderRefinement).getInitPrototype !== null»
					«val refinement = printInitCall(actor)»
					«IF !this.initRefinementDone.contains(refinement)»
						«"\t\t/* == Actor initializations of vertex " + actor.vertexPath + " == */"»
						«"\t\t"+ refinement + "\n"»
						«{this.initRefinementDone.add(refinement) ""}»
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
			  	«"#include \"" + actor.refinement.getFileName + "\""»
			  	«{this.refinementDone.add(actor.refinement.fileName) ""}»
		  	«ENDIF»
		  «ENDIF»
		«ENDFOR»
		«FOR cg : g.childrenGraphs»
			«printRefinementRec(cg)»
		«ENDFOR»
	'''
}
