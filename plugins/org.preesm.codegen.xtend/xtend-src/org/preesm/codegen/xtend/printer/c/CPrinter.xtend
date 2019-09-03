/**
 * Copyright or © or Copr. IETR/INSA - Rennes (2013 - 2019) :
 *
 * Alexandre Honorat [alexandre.honorat@insa-rennes.fr] (2019)
 * Antoine Morvan [antoine.morvan@insa-rennes.fr] (2017 - 2019)
 * Clément Guy [clement.guy@insa-rennes.fr] (2015)
 * Daniel Madroñal [daniel.madronal@upm.es] (2018 - 2019)
 * Florian Arrestier [florian.arrestier@insa-rennes.fr] (2018)
 * dylangageot [gageot.dylan@gmail.com] (2019)
 * Julien Hascoet [jhascoet@kalray.eu] (2016)
 * Karol Desnos [karol.desnos@insa-rennes.fr] (2013 - 2018)
 * Leonardo Suriano [leonardo.suriano@upm.es] (2019)
 * Maxime Pelcat [maxime.pelcat@insa-rennes.fr] (2013 - 2016)
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
package org.preesm.codegen.xtend.printer.c

import java.io.IOException
import java.io.InputStreamReader
import java.io.StringWriter
import java.net.URL
import java.util.ArrayList
import java.util.Arrays
import java.util.Collection
import java.util.Date
import java.util.List
import org.apache.velocity.VelocityContext
import org.apache.velocity.app.VelocityEngine
import org.preesm.codegen.model.Block
import org.preesm.codegen.model.Buffer
import org.preesm.codegen.model.BufferIterator
import org.preesm.codegen.model.CallBlock
import org.preesm.codegen.model.CodeElt
import org.preesm.codegen.model.Communication
import org.preesm.codegen.model.Constant
import org.preesm.codegen.model.ConstantString
import org.preesm.codegen.model.CoreBlock
import org.preesm.codegen.model.DataTransferAction
import org.preesm.codegen.model.Delimiter
import org.preesm.codegen.model.Direction
import org.preesm.codegen.model.FifoCall
import org.preesm.codegen.model.FifoOperation
import org.preesm.codegen.model.FiniteLoopBlock
import org.preesm.codegen.model.FunctionCall
import org.preesm.codegen.model.IntVar
import org.preesm.codegen.model.LoopBlock
import org.preesm.codegen.model.NullBuffer
import org.preesm.codegen.model.PapifyAction
import org.preesm.codegen.model.PapifyFunctionCall
import org.preesm.codegen.model.RegisterSetUpAction
import org.preesm.codegen.model.SharedMemoryCommunication
import org.preesm.codegen.model.SpecialCall
import org.preesm.codegen.model.SubBuffer
import org.preesm.codegen.model.Variable
import org.preesm.codegen.printer.DefaultPrinter
import org.preesm.commons.exceptions.PreesmRuntimeException
import org.preesm.commons.files.PreesmResourcesHelper
import org.preesm.model.pisdf.util.CHeaderUsedLocator
import org.preesm.codegen.model.IteratedBuffer
import org.preesm.codegen.model.ClusterBlock
import org.preesm.codegen.model.SectionBlock

/**
 * This printer is currently used to print C code only for GPP processors
 * supporting pthreads and shared memory communication.
 *
 * @author kdesnos
 * @author mpelcat
 */
class CPrinter extends DefaultPrinter {

	/*
	 * Variable to check if we are using PAPIFY or not --> Will be updated during preprocessing
	 */
	int usingPapify = 0;
	/**
	 * Set to true if a main file should be generated. Set at object creation in constructor.
	 */
	final boolean generateMainFile;

	def boolean generateMainFile() {
		return this.generateMainFile;
	}

	new() {
		// generate a main file by default
		this(true);
	}

	new(boolean generateMainFile) {
		this.generateMainFile = generateMainFile;
	}

	/**
	 * Temporary global var to ignore the automatic suppression of memcpy
	 * whose target and destination are identical.
	 */
	protected var boolean IGNORE_USELESS_MEMCPY = true

	override printCoreBlockHeader(CoreBlock block) '''
			/**
			 * @file «block.name».c
			 * @generated by «this.class.simpleName»
			 * @date «new Date»
			 *
			 * Code generated for processing element «block.name» (ID=«block.coreID»).
			 */

			#include "preesm_gen.h"

	'''

	override printDefinitionsHeader(List<Variable> list) '''
	«IF !list.empty»
		// Core Global Definitions

	«ENDIF»
	'''

	override printBufferDefinition(Buffer buffer) '''
	«buffer.type» «buffer.name»[«buffer.size»]; // «buffer.comment» size:= «buffer.size»*«buffer.type»
	'''

	override printSubBufferDefinition(SubBuffer buffer) '''
	«buffer.type» *const «buffer.name» = («buffer.type»*) («var offset = 0L»«
	{offset = buffer.offset
	 var b = buffer.container;
	 while(b instanceof SubBuffer){
	 	offset = offset + b.offset
	  	b = b.container
	  }
	 b}.name»+«offset»);  // «buffer.comment» size:= «buffer.size»*«buffer.type»
	'''

	override printDefinitionsFooter(List<Variable> list) '''
	«IF !list.empty»

	«ENDIF»
	'''

	override printDeclarationsHeader(List<Variable> list) '''
	// Core Global Declaration
	extern pthread_barrier_t iter_barrier;
	extern int preesmStopThreads;

	'''

	override printBufferDeclaration(Buffer buffer) '''
	extern «printBufferDefinition(buffer)»
	'''

	override printSubBufferDeclaration(SubBuffer buffer) '''
	extern «buffer.type» *const «buffer.name»;  // «buffer.comment» size:= «buffer.size»*«buffer.type» defined in «buffer.creator.name»
	'''

	override printDeclarationsFooter(List<Variable> list) '''
	«IF !list.empty»

	«ENDIF»
	'''

	override printCoreInitBlockHeader(CallBlock callBlock) '''
	void *computationThread_Core«(callBlock.eContainer as CoreBlock).coreID»(void *arg){
		if (arg != NULL) {
			printf("Warning: expecting NULL arguments\n");
		}
		«IF !callBlock.codeElts.empty»// Initialisation(s)«"\n\n"»«ENDIF»
	'''

	override printCoreLoopBlockHeader(LoopBlock block2) '''

	// Begin the execution loop«"\n\t"»
	pthread_barrier_wait(&iter_barrier);
#ifdef PREESM_LOOP_SIZE // Case of a finite loop
	int index;
	for(index=0;index<PREESM_LOOP_SIZE || preesmStopThreads;index++){
#else // Default case of an infinite loop
	while(!preesmStopThreads){
#endif
		// loop body«"\n\n"»
		'''

	override printCoreLoopBlockFooter(LoopBlock block2) '''
			// loop footer
			pthread_barrier_wait(&iter_barrier);
		}
		return NULL;
	}

	«IF block2.codeElts.empty»
	// This call may inform the compiler that the main loop of the thread does not call any function.
	void emptyLoop_«(block2.eContainer as CoreBlock).name»(){

	}
	«ENDIF»
	'''

	//#pragma omp parallel for private(«block2.iter.name»)
	override printFiniteLoopBlockHeader(FiniteLoopBlock block2) '''
		// Begin the for loop
		{
			int «block2.iter.name»;
			«IF block2.parallel.equals(true)»
			#pragma omp parallel for private(«block2.iter.name»)
			«ENDIF»
			for(«block2.iter.name»=0;«block2.iter.name»<«block2.nbIter»;«block2.iter.name»++) {
				
				'''

	override printFiniteLoopBlockFooter(FiniteLoopBlock block2) '''
			}
		}
	'''

	override printClusterBlockHeader(ClusterBlock block) '''
		// Cluster: «block.name»
		// Schedule: «block.schedule»
		«IF block.parallel.equals(true)»
		#pragma omp parallel sections
		«ENDIF»
		{
			
			'''

	override printClusterBlockFooter(ClusterBlock block) '''
		}
	'''

	override printSectionBlockHeader(SectionBlock block) '''
		#pragma omp section
		{
			
			'''

	override printSectionBlockFooter(SectionBlock block) '''
		}
	'''

	override String printFifoCall(FifoCall fifoCall) {
		var result = "fifo" + fifoCall.operation.toString.toLowerCase.toFirstUpper + "("

		if (fifoCall.operation != FifoOperation::INIT) {
			var buffer = fifoCall.parameters.head as Buffer
			result = result + '''«buffer.doSwitch», '''
		}

		result = result +
			'''«fifoCall.headBuffer.name», «fifoCall.headBuffer.size»*sizeof(«fifoCall.headBuffer.type»), '''
		return result = result + '''«IF fifoCall.bodyBuffer !== null»«fifoCall.bodyBuffer.name», «fifoCall.bodyBuffer.size»*sizeof(«fifoCall.
			bodyBuffer.type»)«ELSE»NULL, 0«ENDIF»);
			'''
	}

	override printFork(SpecialCall call) '''
	// Fork «call.name»«var input = call.inputBuffers.head»«var index = 0L»
	{
		«FOR output : call.outputBuffers»
			«printMemcpy(output,0,input,index,output.size,output.type)»«{index=(output.size+index); ""}»
		«ENDFOR»
	}
	'''

	override printBroadcast(SpecialCall call) '''
	// Broadcast «call.name»«var input = call.inputBuffers.head»«var index = 0L»
	{
	«FOR output : call.outputBuffers»«var outputIdx = 0L»
		«// TODO: Change how this loop iterates (nbIter is used in a comment only ...)
		FOR nbIter : 0..(output.size/input.size+1) as int/*Worst case is output.size exec of the loop */»
			«IF outputIdx < output.size /* Execute loop core until all output for current buffer are produced */»
				«val value = Math::min(output.size-outputIdx,input.size-index)»// memcpy #«nbIter»
				«printMemcpy(output,outputIdx,input,index,value,output.type)»«
				{index=(index+value)%input.size;outputIdx=(outputIdx+value); ""}»
			«ENDIF»
		«ENDFOR»
	«ENDFOR»
	}
	'''



	override printRoundBuffer(SpecialCall call) '''
	// RoundBuffer «call.name»«var output = call.outputBuffers.head»«var index = 0L»«var inputIdx = 0L»
	«/*Compute a list of useful memcpy (the one writing the outputed value) */
	var copiedInBuffers = {var totalSize = call.inputBuffers.fold(0L)[res, buf | res+buf.size]
		 var lastInputs = new ArrayList
		 inputIdx = totalSize
		 var i = call.inputBuffers.size	- 1
		 while(totalSize-inputIdx < output.size){
		 	inputIdx = inputIdx - call.inputBuffers.get(i).size
		 	lastInputs.add(0,call.inputBuffers.get(i))
			if (i < 0) {
				throw new PreesmRuntimeException("Invalid RoundBuffer sizes: output size is greater than cumulative input size.")
			}
		 	i=i-1
		 }
		 inputIdx = inputIdx %  output.size
		 lastInputs
		 }»
	{
		«FOR input : copiedInBuffers»
			«// TODO: Change how this loop iterates (nbIter is used in a comment only ...)
			FOR nbIter : 0..(input.size/output.size+1) as int/*Worst number the loop exec */»
				«IF inputIdx < input.size /* Execute loop core until all input for current buffer are produced */»
					«val value = Math::min(input.size-inputIdx,output.size-index)»// memcpy #«nbIter»
					«printMemcpy(output,index,input,inputIdx,value,input.type)»«{
						index=(index+value)%output.size;
						inputIdx=(inputIdx+value); ""
					}»
				«ENDIF»
			«ENDFOR»
		«ENDFOR»
	}
	'''

	override printJoin(SpecialCall call) '''
	// Join «call.name»«var output = call.outputBuffers.head»«var index = 0L»
	{
		«FOR input : call.inputBuffers»
			«printMemcpy(output,index,input,0,input.size,input.type)»«{index=(input.size+index); ""}»
		«ENDFOR»
	}
	'''

	/**
	 * Print a memcpy call in the generated code. Unless
	 * {@link #IGNORE_USELESS_MEMCPY} is set to <code>true</code>, this method
	 * checks if the destination and the source of the memcpy are superimposed.
	 * In such case, the memcpy is useless and nothing is printed.
	 *
	 * @param output
	 *            the destination {@link Buffer}
	 * @param outOffset
	 *            the offset in the destination {@link Buffer}
	 * @param input
	 *            the source {@link Buffer}
	 * @param inOffset
	 *            the offset in the source {@link Buffer}
	 * @param size
	 *            the amount of memory to copy
	 * @param type
	 *            the type of objects copied
	 * @return a {@link CharSequence} containing the memcpy call (if any)
	 */
	def String printMemcpy(Buffer output, long outOffset, Buffer input, long inOffset, long size, String type) {

		// Retrieve the container buffer of the input and output as well
		// as their offset in this buffer
		var totalOffsetOut = outOffset*output.typeSize
		var bOutput = output
		while (bOutput instanceof SubBuffer) {
			totalOffsetOut = totalOffsetOut + bOutput.offset
			bOutput = bOutput.container
		}

		var totalOffsetIn = inOffset*input.typeSize
		var bInput = input
		while (bInput instanceof SubBuffer) {
			totalOffsetIn = totalOffsetIn + bInput.offset
			bInput = bInput.container
		}

		// If the Buffer and offsets are identical, or one buffer is null
		// there is nothing to print
		if((IGNORE_USELESS_MEMCPY && bInput == bOutput && totalOffsetIn == totalOffsetOut) ||
			output instanceof NullBuffer || input instanceof NullBuffer){
			return ''''''
		} else {
			return '''memcpy(«output.doSwitch»+«outOffset», «input.doSwitch»+«inOffset», «size»*sizeof(«type»));'''
		}
	}

	override printNullBuffer(NullBuffer Buffer) {
		return printBuffer(Buffer)
	}

	override caseCommunication(Communication communication) {

		if(communication.nodes.forall[type == "SHARED_MEM"]) {
			return super.caseCommunication(communication)
		} else {
			throw new PreesmRuntimeException("Communication "+ communication.name +
				 " has at least one unsupported communication node"+
				 " for the " + this.class.name + " printer")
		}
	}

	def CharSequence generatePreesmHeader() {
	    // 0- without the following class loader initialization, I get the following exception when running as Eclipse
	    // plugin:
	    // org.apache.velocity.exception.VelocityException: The specified class for ResourceManager
	    // (org.apache.velocity.runtime.resource.ResourceManagerImpl) does not implement
	    // org.apache.velocity.runtime.resource.ResourceManager; Velocity is not initialized correctly.
	    val ClassLoader oldContextClassLoader = Thread.currentThread().getContextClassLoader();
	    Thread.currentThread().setContextClassLoader(CPrinter.classLoader);

	    // 1- init engine
	    val VelocityEngine engine = new VelocityEngine();
	    engine.init();

	    // 2- init context
	    val VelocityContext context = new VelocityContext();
	    val findAllCHeaderFileNamesUsed = CHeaderUsedLocator.findAllCHeaderFileNamesUsed(getEngine.algo)
	    context.put("USER_INCLUDES", findAllCHeaderFileNamesUsed.map["#include \""+ it +"\""].join("\n"));

		var String constants = "#define NB_DESIGN_ELTS "+getEngine.archi.componentInstances.size+"\n#define NB_CORES "+getEngine.codeBlocks.size;
		if(this.usingPapify == 1){
			constants = constants.concat("\n\n#ifdef _PREESM_PAPIFY_MONITOR\n#include \"eventLib.h\"\n#endif");
		}
		if(this.apolloEnabled){
			constants = constants.concat("\n\n#ifdef PREESM_APOLLO_ENABLED\n#include \"apolloAPI.h\"\n#endif");
		}
	    context.put("CONSTANTS", constants);

	    // 3- init template reader
	    val String templateLocalPath = "templates/c/preesm_gen.h";
	    val URL mainTemplate = PreesmResourcesHelper.instance.resolve(templateLocalPath, this.class);
	    var InputStreamReader reader = null;
	    try {
	      reader = new InputStreamReader(mainTemplate.openStream());
	    } catch (IOException e) {
	      throw new PreesmRuntimeException("Could not locate main template [" + templateLocalPath + "].", e);
	    }

	    // 4- init output writer
	    val StringWriter writer = new StringWriter();

	    engine.evaluate(context, writer, "org.apache.velocity", reader);

	    // 99- set back default class loader
	    Thread.currentThread().setContextClassLoader(oldContextClassLoader);

	    return writer.getBuffer().toString();
	}

	override generateStandardLibFiles() {
		val result = super.generateStandardLibFiles();
		val String stdFilesFolder = "/stdfiles/c/"
		val files = Arrays.asList(#[
						"communication.c",
						"communication.h",
						"dump.c",
						"dump.h",
						"fifo.c",
						"fifo.h",
						"mac_barrier.c",
						"mac_barrier.h"
					]);
		files.forEach[it | try {
			result.put(it, PreesmResourcesHelper.instance.read(stdFilesFolder + it, this.class))
		} catch (IOException exc) {
			throw new PreesmRuntimeException("Could not generated content for " + it, exc)
		}]
		result.put("preesm_gen.h",generatePreesmHeader())
		return result
	}

	override createSecondaryFiles(List<Block> printerBlocks, Collection<Block> allBlocks) {
		val result = super.createSecondaryFiles(printerBlocks, allBlocks);
		if (generateMainFile()) {
			result.put("main.c", printMain(printerBlocks))
		}
		return result
	}

	def String printMain(List<Block> printerBlocks) '''
		/**
		 * @file main.c
		 * @generated by «this.class.simpleName»
		 * @date «new Date»
		 *
		 */
		// no monitoring by default

		#define _PREESM_NBTHREADS_ «engine.codeBlocks.size»
		#define _PREESM_MAIN_THREAD_ «mainOperatorId»

		// application dependent includes
		#include "preesm_gen.h"

		// Declare computation thread functions
		«FOR coreBlock : engine.codeBlocks»
		void *computationThread_Core«(coreBlock as CoreBlock).coreID»(void *arg);
		«ENDFOR»

		pthread_barrier_t iter_barrier;
		int preesmStopThreads;


		unsigned int launch(unsigned int core_id, pthread_t * thread, void *(*start_routine) (void *)) {

			// init pthread attributes
			pthread_attr_t attr;
			pthread_attr_init(&attr);

		#ifndef PREESM_NO_AFFINITY
		#ifdef _WIN32
			SYSTEM_INFO sysinfo;
			GetSystemInfo(&sysinfo);
			unsigned int numCPU = sysinfo.dwNumberOfProcessors;
		#else
			unsigned int numCPU = sysconf(_SC_NPROCESSORS_ONLN);
		#endif

			// check CPU id is valid
			if (core_id >= numCPU) {
				// leave attribute uninitialized
				printf("** Warning: thread %d will not be set with specific core affinity \n   due to the lack of available dedicated cores.\n",core_id);
			} else {
		#if defined __APPLE__ || defined _WIN32
				// NOT SUPPORTED
		#else
				// init cpuset struct
				cpu_set_t cpuset;
				CPU_ZERO(&cpuset);
				CPU_SET(core_id, &cpuset);

				// set pthread affinity
				pthread_attr_setaffinity_np(&attr, sizeof(cpuset), &cpuset);
		#endif
			}
		#endif

			// create thread
			pthread_create(thread, &attr, start_routine, NULL);
			return 0;
		}


		int main(void) {
			// Set affinity of main thread to proper core ID
		#ifndef PREESM_NO_AFFINITY
		#if defined __APPLE__ || defined _WIN32
			// NOT SUPPORTED
		#else
			cpu_set_t cpuset;
			CPU_ZERO(&cpuset);
			CPU_SET(_PREESM_MAIN_THREAD_, &cpuset);
			«IF this.apolloEnabled»
			#ifdef PREESM_APOLLO_ENABLED
			«ENDIF»
			sched_setaffinity(getpid(),  sizeof(cpuset), &cpuset);
			«IF this.apolloEnabled»
			#endif
			«ENDIF»
			
		#endif
		#endif

			«IF this.usingPapify == 1»
				#ifdef _PREESM_PAPIFY_MONITOR
				mkdir("papify-output", 0777);
				event_init_multiplex();
				#endif
			«ENDIF»
			// Declaring thread pointers
			pthread_t coreThreads[_PREESM_NBTHREADS_];
			void *(*coreThreadComputations[_PREESM_NBTHREADS_])(void *) = {
				«FOR coreBlock : engine.codeBlocks»&computationThread_Core«(coreBlock as CoreBlock).coreID»«if(engine.codeBlocks.last == coreBlock) {""} else {", "}»«ENDFOR»
			};

		#ifdef PREESM_VERBOSE
			printf("Launched main\n");
		#endif

			// Creating a synchronization barrier
			preesmStopThreads = 0;
			pthread_barrier_init(&iter_barrier, NULL, _PREESM_NBTHREADS_);

			communicationInit();
			
			«IF this.apolloEnabled»
			#ifdef PREESM_APOLLO_ENABLED
			initApolloForDataflow();
			#endif
			«ENDIF»

			// Creating threads
			for (int i = 0; i < _PREESM_NBTHREADS_; i++) {
				if (i != _PREESM_MAIN_THREAD_) {
					if(launch(i,&coreThreads[i],coreThreadComputations[i])) {
						printf("Error: could not launch thread %d\n",i);
						return 1;
					}
				}
			}

			// run main operator code in this thread
			coreThreadComputations[_PREESM_MAIN_THREAD_](NULL);

			// Waiting for thread terminations
			for (int i = 0; i < _PREESM_NBTHREADS_; i++) {
				if (i != _PREESM_MAIN_THREAD_) {
					pthread_join(coreThreads[i], NULL);
				}
			}
			«IF this.usingPapify == 1»
				#ifdef _PREESM_PAPIFY_MONITOR
				event_destroy();
				#endif
			«ENDIF»

			return 0;
		}

	'''


	override printSharedMemoryCommunication(SharedMemoryCommunication communication) '''
	«/*Since everything is already in shared memory, communications are simple synchronizations here*/
	IF (communication.comment !== null && !communication.comment.empty)
	»«IF (communication.comment.contains("\n"))
	»/* «ELSE»// «ENDIF»«communication.comment»
	«IF (communication.comment.contains("\n"))» */
	«ENDIF»«ENDIF»«IF communication.isRedundant»//«ENDIF»«communication.direction.toString.toLowerCase»«communication.delimiter.toString.toLowerCase.toFirstUpper»(«IF (communication.
		direction == Direction::SEND && communication.delimiter == Delimiter::START) ||
		(communication.direction == Direction::RECEIVE && communication.delimiter == Delimiter::END)»«communication.sendStart.coreContainer.coreID», «communication.receiveStart.coreContainer.coreID»«ENDIF»); // «communication.sendStart.coreContainer.name» > «communication.receiveStart.coreContainer.name»: «communication.
		data.doSwitch»
	'''

	override printFunctionCall(FunctionCall functionCall) '''
	«IF this.apolloEnabled»
	#ifdef PREESM_APOLLO_ENABLED
	apolloAddActorConfig(NULL, 0, pthread_self(), "«functionCall.actorName»");
	#endif
	«ENDIF»
	«functionCall.name»(«FOR param : functionCall.parameters SEPARATOR ','»«param.doSwitch»«ENDFOR»); // «functionCall.actorName»
	'''

	override printPapifyFunctionCall(PapifyFunctionCall papifyFunctionCall) '''
	«IF papifyFunctionCall.opening == true»
		#ifdef _PREESM_PAPIFY_MONITOR
	«ENDIF»
	«printFunctionCall(papifyFunctionCall)»
	«IF papifyFunctionCall.closing == true»
		#endif
	«ENDIF»
	'''

	override printConstant(Constant constant) '''«constant.value»«IF !constant.name.nullOrEmpty»/*«constant.name»*/«ENDIF»'''

	override printConstantString(ConstantString constant) '''"«constant.value»"'''

	override printPapifyActionDefinition(PapifyAction action) '''
	«IF action.opening == true»
		#ifdef _PREESM_PAPIFY_MONITOR
	«ENDIF»
	«action.type» «action.name»; // «action.comment»
	«IF action.closing == true»
		#endif
	«ENDIF»
	'''
	override printPapifyActionParam(PapifyAction action) '''&«action.name»'''

	override printBuffer(Buffer buffer) '''«buffer.name»'''

	override printSubBuffer(SubBuffer buffer) {return printBuffer(buffer)}

	override printBufferIterator(BufferIterator bufferIterator) '''«bufferIterator.name» + «printIntVar(bufferIterator.iter)» * «bufferIterator.iterSize»'''

	override printBufferIteratorDeclaration(BufferIterator bufferIterator) ''''''

	override printBufferIteratorDefinition(BufferIterator bufferIterator) ''''''

	override printIteratedBuffer(IteratedBuffer iteratedBuffer) '''«doSwitch(iteratedBuffer.buffer)» + «printIntVar(iteratedBuffer.iter)» * «iteratedBuffer.size»'''

	override printIntVar(IntVar intVar) '''«intVar.name»'''

	override printIntVarDeclaration(IntVar intVar) '''
	extern int «intVar.name»;
	'''

	override printIntVarDefinition(IntVar intVar) '''
	int «intVar.name»;
	'''

	override printDataTansfer(DataTransferAction action) ''''''

	override printRegisterSetUp(RegisterSetUpAction action) ''''''

	override preProcessing(List<Block> printerBlocks, Collection<Block> allBlocks){
		super.preProcessing(printerBlocks, allBlocks);
		for (cluster : allBlocks){
			if (cluster instanceof CoreBlock) {
				for(CodeElt codeElt : cluster.loopBlock.codeElts){
					if(codeElt instanceof PapifyFunctionCall){
						this.usingPapify = 1;
					}
				}
			}
		}
	}

}
