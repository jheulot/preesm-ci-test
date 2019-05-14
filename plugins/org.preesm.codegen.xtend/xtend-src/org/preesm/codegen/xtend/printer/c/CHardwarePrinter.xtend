/**
 * Copyright or © or Copr. IETR/INSA - Rennes (2013 - 2018) :
 *
 * Antoine Morvan <antoine.morvan@insa-rennes.fr> (2017 - 2018)
 * Clément Guy <clement.guy@insa-rennes.fr> (2015)
 * Daniel Madroñal <daniel.madronal@upm.es> (2018)
 * Florian Arrestier <florian.arrestier@insa-rennes.fr> (2018)
 * Julien Hascoet <jhascoet@kalray.eu> (2016)
 * Karol Desnos <karol.desnos@insa-rennes.fr> (2013 - 2018)
 * Maxime Pelcat <maxime.pelcat@insa-rennes.fr> (2013 - 2016)
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
import org.preesm.codegen.model.Communication
import org.preesm.codegen.model.Constant
import org.preesm.codegen.model.ConstantString
import org.preesm.codegen.model.CoreBlock
import org.preesm.codegen.model.Delimiter
import org.preesm.codegen.model.Direction
import org.preesm.codegen.model.FifoCall
import org.preesm.codegen.model.FifoOperation
import org.preesm.codegen.model.FiniteLoopBlock
import org.preesm.codegen.model.FreeDataTransferBuffer
import org.preesm.codegen.model.FpgaLoadAction
import org.preesm.codegen.model.FunctionCall
import org.preesm.codegen.model.GlobalBufferDeclaration
import org.preesm.codegen.model.IntVar
import org.preesm.codegen.model.LoopBlock
import org.preesm.codegen.model.NullBuffer
import org.preesm.codegen.model.OutputDataTransfer
import org.preesm.codegen.model.PapifyAction
import org.preesm.codegen.model.DataTransferAction
import org.preesm.codegen.model.RegisterSetUpAction
import org.preesm.codegen.model.SharedMemoryCommunication
import org.preesm.codegen.model.SpecialCall
import org.preesm.codegen.model.SubBuffer
import org.preesm.codegen.model.Variable
import org.preesm.codegen.printer.DefaultPrinter
import org.preesm.codegen.xtend.CodegenPlugin
import org.preesm.commons.exceptions.PreesmException
import org.preesm.commons.exceptions.PreesmRuntimeException
import org.preesm.commons.files.URLResolver
import org.preesm.model.pisdf.util.CHeaderUsedLocator
import org.preesm.commons.logger.PreesmLogger
import java.util.logging.Level
import java.lang.reflect.Parameter
import org.preesm.codegen.model.DistributedMemoryCommunication
import org.preesm.codegen.model.PapifyFunctionCall
import org.eclipse.emf.common.util.EList
import org.preesm.codegen.model.CodeElt

/**
 * This printer is currently used to print C code only for GPP processors
 * supporting pthreads and shared memory communication.
 *
 * @author kdesnos
 * @author mpelcat
 */
class CHardwarePrinter extends DefaultPrinter {

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
	
	/**
	 * Variable that store the number or iteration in hardware. Using it, it will be possible to 
	 * compress all the function calls in just one.
	 */
	protected var factorNumber = 0;
	protected var functionCallNumber = 0;
	protected var dataTransferCallNumber = 0;
	protected var dataOutputTransferCallNumber = 0;
	
//	int getFactorNumber(){
//		return this.factorNunber;
//	}

	override printCoreBlockHeader(CoreBlock block) '''
			/**
			 * @file «block.name».c
			 * @generated by «this.class.simpleName»
			 * @date «new Date»
			 *
			 * Code generated for processing element «block.name» (ID=«block.coreID»).
			 */

			#include "preesm_gen.h"
			#include "hardware.h"
			#include "hardware_accelerator_setup.h"

	'''

	override printDefinitionsHeader(List<Variable> list) '''
	«IF !list.empty»
		// Core Global Definitions

	«ENDIF»
	'''

	override printBufferDefinition(Buffer buffer) '''
	«buffer.type» «buffer.name»[«buffer.size»]; // «buffer.comment» size:= «buffer.size»*«buffer.type» ([LEO] printBufferDefinition)
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
	extern int stopThreads;

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
		// Initialize Hardware infrastructure
		hardware_init();
		
		«IF !callBlock.codeElts.empty»// Initialisation(s)«"\n\n"»«ENDIF»
	'''

	override printCoreLoopBlockHeader(LoopBlock block2) '''

		«"\t"»// Begin the execution loop
#ifdef LOOP_SIZE // Case of a finite loop
			int index;
			for(index=0;index<LOOP_SIZE;index++){
#else // Default case of an infinite loop
			while(1){
#endif
				pthread_barrier_wait(&iter_barrier);
				if(stopThreads){
					pthread_exit(NULL);
				}«"\n\n"»
	'''

	override printCoreLoopBlockFooter(LoopBlock block2) '''
		}
		// Release kernel instance
		hardware_kernel_release("matmul");
		
		// Clean Hardware setup
		hardware_exit();
		
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
			for(«block2.iter.name»=0;«block2.iter.name»<«block2.nbIter»;«block2.iter.name»++){

	'''

	override printFiniteLoopBlockFooter(FiniteLoopBlock block2) '''
		}
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
	    val findAllCHeaderFileNamesUsed = CHeaderUsedLocator.findAllCHeaderFileNamesUsed(getEngine.algo.referencePiMMGraph)
	    context.put("USER_INCLUDES", findAllCHeaderFileNamesUsed.map["#include \""+ it +"\""].join("\n"));
		if(this.usingPapify == 1){
	    	context.put("CONSTANTS", "\n#ifdef _PREESM_MONITOR_INIT\n#include \"eventLib.h\"\n#endif");
		}
	    
	    // 3- init template reader
	    val String templateLocalURL = "templates/c/preesm_gen.h";
	    val URL mainTemplate = URLResolver.findFirstInBundleList(templateLocalURL, CodegenPlugin.BUNDLE_ID);
	    var InputStreamReader reader = null;
	    try {
	      reader = new InputStreamReader(mainTemplate.openStream());
	    } catch (IOException e) {
	      throw new PreesmRuntimeException("Could not locate main template [" + templateLocalURL + "].", e);
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
		val String stdFilesFolder = "/stdfiles/hardware/"
		val files = Arrays.asList(#[
						"hardware.c",
						"hardware.h",
						"hardware_hw.c",
						"hardware_hw.h",
						"hardware_rcfg.c",
						"hardware_rcfg.h",
						"hardware_dbg.h",
						"dmaproxy.h",
						"dump.c",
						"dump.h",
						"fifo.c",
						"fifo.h",
						"communication.c",
						"communication.h"
					]);
		files.forEach[it | try {
			result.put(it, URLResolver.readURLInBundleList(stdFilesFolder + it, CodegenPlugin.BUNDLE_ID))
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
		 * a new version for Hardware
		 * @generated by «this.class.simpleName»
		 * @date «new Date»
		 *
		 */
		// no monitoring by default
		#define _GNU_SOURCE
		#ifdef _WIN32
		#include <windows.h>
		#else
		#include <unistd.h>
		#endif

		#ifdef __APPLE__
		#include "TargetConditionals.h"
		#endif

		#include <pthread.h>
		#include <stdio.h>

		#define _PREESM_NBTHREADS_ «engine.codeBlocks.size»
		#define _PREESM_MAIN_THREAD_ «mainOperatorId»

		// application dependent includes
		#include "preesm_gen.h"

		// Declare computation thread functions
		«FOR coreBlock : engine.codeBlocks»
		void *computationThread_Core«(coreBlock as CoreBlock).coreID»(void *arg);
		«ENDFOR»

		pthread_barrier_t iter_barrier;
		int stopThreads;


		unsigned int launch(unsigned int core_id, pthread_t * thread, void *(*start_routine) (void *)) {

		#ifdef _WIN32
			SYSTEM_INFO sysinfo;
			GetSystemInfo(&sysinfo);
			unsigned int numCPU = sysinfo.dwNumberOfProcessors;
		#else
			unsigned int numCPU = sysconf(_SC_NPROCESSORS_ONLN);
		#endif

			// init pthread attributes
			pthread_attr_t attr;
			pthread_attr_init(&attr);

			// check CPU id is valid
			if (core_id >= numCPU) {
				// leave attribute uninitialized
				printf("** Warning: thread %d will not be set with specific core affinity \n   due to the lack of available dedicated cores.\n",core_id);
			} else {
		#ifdef __APPLE__
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

			// create thread
			pthread_create(thread, &attr, start_routine, NULL);
			return 0;
		}


		int main(void) {
			#ifdef _PREESM_MONITOR_INIT
			mkdir("papify-output", 0777);
			event_init_multiplex();
			#endif
			// Declaring thread pointers
			pthread_t coreThreads[_PREESM_NBTHREADS_];
			void *(*coreThreadComputations[_PREESM_NBTHREADS_])(void *) = {
				«FOR coreBlock : engine.codeBlocks»&computationThread_Core«(coreBlock as CoreBlock).coreID»«if(engine.codeBlocks.last == coreBlock) {""} else {", "}»«ENDFOR»
			};

		#ifdef VERBOSE
			printf("Launched main\n");
		#endif

			// Creating a synchronization barrier
			stopThreads = 0;
			pthread_barrier_init(&iter_barrier, NULL, _PREESM_NBTHREADS_);

			communicationInit();

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
			#ifdef _PREESM_MONITOR_INIT
			event_destroy();
			#endif

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

	override printDistributedMemoryCommunication(DistributedMemoryCommunication communication) ''''''

	override printFunctionCall(FunctionCall functionCall) '''
	hardware_kernel_execute("«functionCall.name»",gsize_TO_BE_CHANGED«IF (functionCall.factorNumber > 0)» * «functionCall.factorNumber»«ENDIF», lsize_TO_BE_CHANGED); // executing hardware kernel
	hardware_kernel_wait("«functionCall.name»");
	'''
	
	override printPapifyFunctionCall(PapifyFunctionCall papifyFunctionCall) '''
	«IF papifyFunctionCall.opening == true»
		#ifdef _PREESM_MONITOR_INIT
	«ENDIF»
	«papifyFunctionCall.name»(«FOR param : papifyFunctionCall.parameters SEPARATOR ','»«param.doSwitch»«ENDFOR»); // «papifyFunctionCall.actorName»
	«IF papifyFunctionCall.closing == true»
		#endif
	«ENDIF»
	'''

	override printConstant(Constant constant) '''«constant.value»«IF !constant.name.nullOrEmpty»/*«constant.name»*/«ENDIF»'''

	override printConstantString(ConstantString constant) '''"«constant.value»"'''

	override printPapifyActionDefinition(PapifyAction action) '''
	«IF action.opening == true»
		#ifdef _PREESM_MONITOR_INIT
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

	override printIntVar(IntVar intVar) '''«intVar.name»'''

	override printIntVarDeclaration(IntVar intVar) '''
	extern int «intVar.name»;
	'''

	override printIntVarDefinition(IntVar intVar) '''
	int «intVar.name»;
	'''

	override printDataTansfer(DataTransferAction action) '''
	// Hardware³ data transfer token into Global Buffer
	«var count = 0»
	«FOR buffer : action.buffers»
	«IF (action.parameterDirections.get(count).toString == 'INPUT')»
		memcpy((void *) global_hardware_«count» + («buffer.size» * «this.dataTransferCallNumber» * sizeof(a3data_t)), (void *)«buffer.name», «buffer.size»*sizeof(a3data_t)); // input «count++»
	«ELSE»
	    // output «count++»
	«ENDIF»
	«ENDFOR»
	//«this.dataTransferCallNumber++»
	'''
	
	override printOutputDataTransfer(OutputDataTransfer action) '''
	// Hardware³ data transfer token output
	«var count = 0»
	«FOR buffer : action.buffers»
	«IF (action.parameterDirections.get(count).toString == 'INPUT')»
		// input «count++»
	«ELSE»
		memcpy((void *)«buffer.name», (void *) global_hardware_«count» + («buffer.size» * «this.dataOutputTransferCallNumber» * sizeof(a3data_t)), «buffer.size»*sizeof(a3data_t)); // output «count++»
	«ENDIF»
	«ENDFOR»
	//«this.dataOutputTransferCallNumber++»
	'''
	
	override printRegisterSetUp(RegisterSetUpAction action) '''
	«var count = 0»
	«FOR param : action.parameters»
		for (int i = 0; i < MAX_NACCS; i++) {
			wcfg_temp[i] = «param.doSwitch»;
		}
		hardware_kernel_wcfg("«action.name»", A3_ACCELERATOR_REG_«(count++).toString()», wcfg_temp);
	«ENDFOR»
	'''
	
	override printFpgaLoad(FpgaLoadAction action) '''
			
		// Create kernel instance
		hardware_kernel_create("«action.name»", SIZE_MEM_HW, N_MEMORY_BANKS, N_REGISTERS);
		
		a3data_t wcfg_temp[MAX_NACCS];
		
		for (int i = 0; i < MAX_NACCS; i++) {
			hardware_load("«action.name»", i, 0, 0, 1);
		}
	'''
	
	override printFreeDataTransferBuffer(FreeDataTransferBuffer action) ''''''
	
	override printGlobalBufferDeclaration(GlobalBufferDeclaration action) '''
	// Hardware³ global data buffer declaration
	«var count = 0»
	«FOR buffer : action.buffers»
		a3data_t *global_hardware_«count» = NULL;
		global_hardware_«count» = hardware_alloc(«buffer.size»«IF (this.factorNumber > 0)» * «this.factorNumber»«ENDIF» * sizeof *«buffer.name», "«action.name»", "«buffer.doSwitch»",  «action.parameterDirections.get(count++)»);
	«ENDFOR»
	'''
		
	def void compactPapifyUsage(Collection<Block> allBlocks){
		for (cluster : allBlocks){
			if (cluster instanceof CoreBlock) {
				var EList<Variable> definitions = cluster.definitions;
				var EList<CodeElt> loopBlockElts = cluster.loopBlock.codeElts;
				var EList<CodeElt> initBlockElts = cluster.initBlock.codeElts;
				var int iterator = 0;
				var boolean closed = false;
				/*
				 * Only one #ifdef _PREESM_MONITORING_INIT in the definition code 
				 */
				if(!definitions.isEmpty){
					for(iterator = 0; iterator < definitions.size; iterator++){
						if(definitions.get(iterator) instanceof PapifyAction && this.usingPapify == 0){
							this.usingPapify = 1;
							(definitions.get(iterator) as PapifyAction).opening = true;
						}
					}
					for(iterator = definitions.size-1; iterator >= 0; iterator--){
						if(definitions.get(iterator) instanceof PapifyAction && closed == false){
							closed = true;
							(definitions.get(iterator) as PapifyAction).closing = true;
						}
					}
				} 
				/*
				 * Minimizing the number of #ifdef _PREESM_MONITORING_INIT in the loop
				 */
				if(!loopBlockElts.isEmpty){
					if(loopBlockElts.get(0) instanceof PapifyFunctionCall){
						(loopBlockElts.get(0) as PapifyFunctionCall).opening = true;
						if(!(loopBlockElts.get(1) instanceof PapifyAction)){
							(loopBlockElts.get(0) as PapifyFunctionCall).closing = true;							
						}
					}
					for(iterator = 1; iterator < loopBlockElts.size-1; iterator++){
						if(loopBlockElts.get(iterator) instanceof PapifyFunctionCall && !(loopBlockElts.get(iterator - 1) instanceof PapifyFunctionCall)){
							(loopBlockElts.get(iterator) as PapifyFunctionCall).opening = true;
						}
						if(loopBlockElts.get(iterator) instanceof PapifyFunctionCall && !(loopBlockElts.get(iterator + 1) instanceof PapifyFunctionCall)){
							(loopBlockElts.get(iterator) as PapifyFunctionCall).closing = true;
						}
					}
					if(loopBlockElts.get(loopBlockElts.size-1) instanceof PapifyFunctionCall){
						(loopBlockElts.get(loopBlockElts.size-1) as PapifyFunctionCall).closing = true;
					}
				} 
				/*
				 * Minimizing the number of #ifdef _PREESM_MONITORING_INIT in the init
				 */
				if(!initBlockElts.isEmpty){
					if(initBlockElts.get(0) instanceof PapifyFunctionCall){
						(initBlockElts.get(0) as PapifyFunctionCall).opening = true;
						if(!(initBlockElts.get(1) instanceof PapifyAction)){
							(initBlockElts.get(0) as PapifyFunctionCall).closing = true;							
						}
					}
					for(iterator = 1; iterator < initBlockElts.size-1; iterator++){
						if(initBlockElts.get(iterator) instanceof PapifyFunctionCall && !(initBlockElts.get(iterator - 1) instanceof PapifyFunctionCall)){
							(initBlockElts.get(iterator) as PapifyFunctionCall).opening = true;
						}
						if(initBlockElts.get(iterator) instanceof PapifyFunctionCall && !(initBlockElts.get(iterator + 1) instanceof PapifyFunctionCall)){
							(initBlockElts.get(iterator) as PapifyFunctionCall).closing = true;
						}
					}
					if(initBlockElts.get(initBlockElts.size-1) instanceof PapifyFunctionCall){
						(initBlockElts.get(initBlockElts.size-1) as PapifyFunctionCall).closing = true;
					}
				} 
			}
		}	
	}
	override preProcessing(List<Block> printerBlocks, Collection<Block> allBlocks) {
		PreesmLogger.getLogger().info("[LEO] preProcessing for Hardware³");
		//var functionCallNumber = 0;
		var DataTransferActionNumber = 0;
		var FreeDataTransferBufferNumber = 0;
		var RegisterSetUpNumber = 0;
		//var firstRegisterSetUp = 0;
		var firstFunctionCallIndex = 0;
		var lastFunctionCallIndex = 0;
		var currentFunctionPosition = 0;
		//var firstDataTransferIndex = 0;
		//var firstFreeDataIndex = 0;
		for (Block block : printerBlocks) {
			
			/*
			 * to delete all the FunctionCallImpl and to keep just the fist one where the input data buffer
			 * is the set of all data buffers together.
			 * Keep in mind that this is just the first integration that needs to be modified.
			 * 
			 */
			
			var coreLoop = (block as CoreBlock).loopBlock
			var i = 0;
			
			// This Loop just locate where the function are and how many they are.
			while (i < coreLoop.codeElts.size) {
				// Retrieve the function ID
				val elt = coreLoop.codeElts.get(i)
				if (elt.getClass().getSimpleName().equals("FunctionCallImpl") && this.functionCallNumber == 0) {
					this.functionCallNumber++;
					firstFunctionCallIndex = i;
					lastFunctionCallIndex = i;
				}
				else if (elt.getClass().getSimpleName().equals("FunctionCallImpl") && this.functionCallNumber > 0) {
					this.functionCallNumber++;
					lastFunctionCallIndex = i;
					//coreLoop.codeElts.remove(i);
				}
				i++;
			}
			
			// This loop adds the new information on the on the class to be printed and substitute the old with the new one
			// Note that the function to keep is the last one!
			if (this.functionCallNumber > 0) {
				currentFunctionPosition = lastFunctionCallIndex;
				var functionCallImplOld = (block as CoreBlock).loopBlock.codeElts.get(lastFunctionCallIndex);
				// checking that the function to be changed is the right one
				 if (!functionCallImplOld.class.getSimpleName().equals("FunctionCallImpl")){
				 	PreesmLogger.getLogger().log(Level.SEVERE, "Hardware³ Codegen ERROR in the preProcessing function. The functionCall to be modified was NOT found");
				 } else {
				 	// create a new function identical to the Old one
					var functionCallImplNew = (functionCallImplOld as FunctionCall);
					// set the new value in the new version of the element of the list
					functionCallImplNew.factorNumber = this.functionCallNumber;
					// replace the old element with the new one
					(block as CoreBlock).loopBlock.codeElts.set(lastFunctionCallIndex,functionCallImplNew);	
				 } 
				PreesmLogger.getLogger().info("[LEO] number of FunctionCallImpl " + this.functionCallNumber);
			}
			
			// this loop is to delete all the functions but not the last one (the new one!)
			i = coreLoop.codeElts.size-1;
			var flagFirstFunctionFound = 0;
			while (i > 0) {
				// Retrieve the function ID
				val elt = coreLoop.codeElts.get(i)
				if (elt.getClass().getSimpleName().equals("FunctionCallImpl") && flagFirstFunctionFound == 0) {
					flagFirstFunctionFound++;
					
				}
				else if (elt.getClass().getSimpleName().equals("FunctionCallImpl") && flagFirstFunctionFound > 0) {
					coreLoop.codeElts.remove(i);
					currentFunctionPosition--;
				}
				i--;
			}
			
			// this loop is for the data transfer. A new DataTrasfer (a replica) is added AFTER the FunctionCall,
			// one for every buffer used!
			
			i=0;
			var positionOfNewDataTransfer = currentFunctionPosition;
			while (i < coreLoop.codeElts.size) {
				// Retrieve the function ID
				val elt = coreLoop.codeElts.get(i)
				if (elt.getClass().getSimpleName().equals("DataTransferActionImpl") && DataTransferActionNumber == 0) {
					DataTransferActionNumber++;
					positionOfNewDataTransfer++;
				}
				else if (elt.getClass().getSimpleName().equals("DataTransferActionImpl") && DataTransferActionNumber > 0) {
					DataTransferActionNumber++;
					positionOfNewDataTransfer++;
					//coreLoop.codeElts.remove(i);
				}
				i++;
			}
			//the last loop is for the Free data transfer (that actually does nothing)
			i=0;
			while (i < coreLoop.codeElts.size) {
				// Retrieve the function ID
				val elt = coreLoop.codeElts.get(i)
				if (elt.getClass().getSimpleName().equals("FreeDataTransferBufferImpl") && FreeDataTransferBufferNumber == 0) {
					FreeDataTransferBufferNumber++;
					//firstFreeDataIndex = i;
				}
				else if (elt.getClass().getSimpleName().equals("FreeDataTransferBufferImpl") && FreeDataTransferBufferNumber > 0) {
					FreeDataTransferBufferNumber++;
					//coreLoop.codeElts.remove(i);
				}
				i++;
			}
			
			// this loop is for the OutputDataTransfer. All the OUTPUT DataTransfer will be deleted 
			// and inserted after the function execution
			
			i=0;
			var positionOfNewOutputDataTransfer = currentFunctionPosition;
			var OutputDataTransferActionNumber = 0;
			val cloneCoreLoop = coreLoop.codeElts.clone
			while (i < coreLoop.codeElts.size) {
				// Retrieve the function ID
				val elt = coreLoop.codeElts.get(i)
				if (elt.getClass().getSimpleName().equals("OutputDataTransferImpl") && OutputDataTransferActionNumber == 0) {
					OutputDataTransferActionNumber++;
					currentFunctionPosition--
					coreLoop.codeElts.remove(i);
				}
				else if (elt.getClass().getSimpleName().equals("OutputDataTransferImpl") && OutputDataTransferActionNumber > 0) {
					OutputDataTransferActionNumber++;
					currentFunctionPosition--
					coreLoop.codeElts.remove(i);
				}
				i++;
			}
			// the last one is deleted after the function execution and does not count
			currentFunctionPosition++
			
			// all the OutputDataTransfer should be inserted again but AFTER the function call
			var countOutputDataTransferInserted = 0
			i=0
			while (i < cloneCoreLoop.size){
				// Retrieve the function ID
				val elt = cloneCoreLoop.get(i)
				if (elt.getClass().getSimpleName().equals("OutputDataTransferImpl")){
					countOutputDataTransferInserted++
					coreLoop.codeElts.add(currentFunctionPosition+countOutputDataTransferInserted,elt)
				}
				i++
			}
			PreesmLogger.getLogger().info("[LEO] number of OutputDataTransfer inserted is " + countOutputDataTransferInserted);
			
			
			
			
			
			
			
			// it is enough to set up the register just once at the beginning.
			i=0;
			RegisterSetUpNumber = 0;
			while (i < coreLoop.codeElts.size) {
				// Retrieve the function ID
				val elt = coreLoop.codeElts.get(i)
				if (elt.getClass().getSimpleName().equals("RegisterSetUpActionImpl") && RegisterSetUpNumber == 0) {
					RegisterSetUpNumber++;
					//firstRegisterSetUp = i;
				}
				else if (elt.getClass().getSimpleName().equals("RegisterSetUpActionImpl") && RegisterSetUpNumber > 0) {
					RegisterSetUpNumber++;
					coreLoop.codeElts.remove(i);
				}
				i++;
			}

			// storing the functionCallNumber that may be used by other printers
			if (this.functionCallNumber == DataTransferActionNumber && this.functionCallNumber == FreeDataTransferBufferNumber){
				this.factorNumber = this.functionCallNumber;
			} else {
				PreesmLogger.getLogger().log(Level.SEVERE, "Hardware³ Codegen ERROR in the preProcessing function. Different number of function calls and data transfers were detected");
			}
			
		}
		
		 
		/*
		 * Preprocessing for Papify
		 */
		compactPapifyUsage(allBlocks);
	}


}



