/*******************************************************************************
 * Copyright or © or Copr. 2012 - 2017 IETR/INSA:
 *
 * Antoine Morvan <antoine.morvan@insa-rennes.fr> (2017)
 * Clément Guy <clement.guy@insa-rennes.fr> (2014 - 2015)
 * Julien Heulot <julien.heulot@insa-rennes.fr> (2013)
 * Karol Desnos <karol.desnos@insa-rennes.fr> (2012 - 2013)
 * Maxime Pelcat <Maxime.Pelcat@insa-rennes.fr> (2013)
 * Romina Racca <romina.racca@gmail.com> (2013)
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
package org.ietr.preesm.experiment.model.pimm.util;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;

import org.eclipse.emf.ecore.util.Switch;

import org.ietr.preesm.experiment.model.pimm.*;

/**
 * <!-- begin-user-doc -->
 * The <b>Switch</b> for the model's inheritance hierarchy.
 * It supports the call {@link #doSwitch(EObject) doSwitch(object)}
 * to invoke the <code>caseXXX</code> method for each class of the model,
 * starting with the actual class of the object
 * and proceeding up the inheritance hierarchy
 * until a non-null result is returned,
 * which is the result of the switch.
 * <!-- end-user-doc -->
 * @see org.ietr.preesm.experiment.model.pimm.PiMMPackage
 * @generated
 */
public class PiMMSwitch<T> extends Switch<T> {
	/**
	 * The cached model package
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected static PiMMPackage modelPackage;

	/**
	 * Creates an instance of the switch.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public PiMMSwitch() {
		if (modelPackage == null) {
			modelPackage = PiMMPackage.eINSTANCE;
		}
	}

	/**
	 * Checks whether this is a switch for the given package.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @parameter ePackage the package in question.
	 * @return whether this is a switch for the given package.
	 * @generated
	 */
	@Override
	protected boolean isSwitchFor(EPackage ePackage) {
		return ePackage == modelPackage;
	}

	/**
	 * Calls <code>caseXXX</code> for each class of the model until one returns a non null result; it yields that result.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the first non-null result returned by a <code>caseXXX</code> call.
	 * @generated
	 */
	@Override
	protected T doSwitch(int classifierID, EObject theEObject) {
		switch (classifierID) {
			case PiMMPackage.PARAMETERIZABLE: {
				Parameterizable parameterizable = (Parameterizable)theEObject;
				T result = caseParameterizable(parameterizable);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case PiMMPackage.ABSTRACT_VERTEX: {
				AbstractVertex abstractVertex = (AbstractVertex)theEObject;
				T result = caseAbstractVertex(abstractVertex);
				if (result == null) result = caseParameterizable(abstractVertex);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case PiMMPackage.ABSTRACT_ACTOR: {
				AbstractActor abstractActor = (AbstractActor)theEObject;
				T result = caseAbstractActor(abstractActor);
				if (result == null) result = caseAbstractVertex(abstractActor);
				if (result == null) result = caseParameterizable(abstractActor);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case PiMMPackage.PI_GRAPH: {
				PiGraph piGraph = (PiGraph)theEObject;
				T result = casePiGraph(piGraph);
				if (result == null) result = caseAbstractActor(piGraph);
				if (result == null) result = caseAbstractVertex(piGraph);
				if (result == null) result = caseParameterizable(piGraph);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case PiMMPackage.ACTOR: {
				Actor actor = (Actor)theEObject;
				T result = caseActor(actor);
				if (result == null) result = caseExecutableActor(actor);
				if (result == null) result = caseAbstractActor(actor);
				if (result == null) result = caseAbstractVertex(actor);
				if (result == null) result = caseParameterizable(actor);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case PiMMPackage.PORT: {
				Port port = (Port)theEObject;
				T result = casePort(port);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case PiMMPackage.DATA_INPUT_PORT: {
				DataInputPort dataInputPort = (DataInputPort)theEObject;
				T result = caseDataInputPort(dataInputPort);
				if (result == null) result = caseDataPort(dataInputPort);
				if (result == null) result = casePort(dataInputPort);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case PiMMPackage.DATA_OUTPUT_PORT: {
				DataOutputPort dataOutputPort = (DataOutputPort)theEObject;
				T result = caseDataOutputPort(dataOutputPort);
				if (result == null) result = caseDataPort(dataOutputPort);
				if (result == null) result = casePort(dataOutputPort);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case PiMMPackage.CONFIG_INPUT_PORT: {
				ConfigInputPort configInputPort = (ConfigInputPort)theEObject;
				T result = caseConfigInputPort(configInputPort);
				if (result == null) result = casePort(configInputPort);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case PiMMPackage.CONFIG_OUTPUT_PORT: {
				ConfigOutputPort configOutputPort = (ConfigOutputPort)theEObject;
				T result = caseConfigOutputPort(configOutputPort);
				if (result == null) result = caseDataOutputPort(configOutputPort);
				if (result == null) result = caseISetter(configOutputPort);
				if (result == null) result = caseDataPort(configOutputPort);
				if (result == null) result = casePort(configOutputPort);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case PiMMPackage.FIFO: {
				Fifo fifo = (Fifo)theEObject;
				T result = caseFifo(fifo);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case PiMMPackage.INTERFACE_ACTOR: {
				InterfaceActor interfaceActor = (InterfaceActor)theEObject;
				T result = caseInterfaceActor(interfaceActor);
				if (result == null) result = caseAbstractActor(interfaceActor);
				if (result == null) result = caseAbstractVertex(interfaceActor);
				if (result == null) result = caseParameterizable(interfaceActor);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case PiMMPackage.DATA_INPUT_INTERFACE: {
				DataInputInterface dataInputInterface = (DataInputInterface)theEObject;
				T result = caseDataInputInterface(dataInputInterface);
				if (result == null) result = caseInterfaceActor(dataInputInterface);
				if (result == null) result = caseAbstractActor(dataInputInterface);
				if (result == null) result = caseAbstractVertex(dataInputInterface);
				if (result == null) result = caseParameterizable(dataInputInterface);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case PiMMPackage.DATA_OUTPUT_INTERFACE: {
				DataOutputInterface dataOutputInterface = (DataOutputInterface)theEObject;
				T result = caseDataOutputInterface(dataOutputInterface);
				if (result == null) result = caseInterfaceActor(dataOutputInterface);
				if (result == null) result = caseAbstractActor(dataOutputInterface);
				if (result == null) result = caseAbstractVertex(dataOutputInterface);
				if (result == null) result = caseParameterizable(dataOutputInterface);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case PiMMPackage.CONFIG_INPUT_INTERFACE: {
				ConfigInputInterface configInputInterface = (ConfigInputInterface)theEObject;
				T result = caseConfigInputInterface(configInputInterface);
				if (result == null) result = caseParameter(configInputInterface);
				if (result == null) result = caseAbstractVertex(configInputInterface);
				if (result == null) result = caseISetter(configInputInterface);
				if (result == null) result = caseParameterizable(configInputInterface);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case PiMMPackage.CONFIG_OUTPUT_INTERFACE: {
				ConfigOutputInterface configOutputInterface = (ConfigOutputInterface)theEObject;
				T result = caseConfigOutputInterface(configOutputInterface);
				if (result == null) result = caseInterfaceActor(configOutputInterface);
				if (result == null) result = caseAbstractActor(configOutputInterface);
				if (result == null) result = caseAbstractVertex(configOutputInterface);
				if (result == null) result = caseParameterizable(configOutputInterface);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case PiMMPackage.REFINEMENT: {
				Refinement refinement = (Refinement)theEObject;
				T result = caseRefinement(refinement);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case PiMMPackage.PARAMETER: {
				Parameter parameter = (Parameter)theEObject;
				T result = caseParameter(parameter);
				if (result == null) result = caseAbstractVertex(parameter);
				if (result == null) result = caseISetter(parameter);
				if (result == null) result = caseParameterizable(parameter);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case PiMMPackage.DEPENDENCY: {
				Dependency dependency = (Dependency)theEObject;
				T result = caseDependency(dependency);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case PiMMPackage.ISETTER: {
				ISetter iSetter = (ISetter)theEObject;
				T result = caseISetter(iSetter);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case PiMMPackage.DELAY: {
				Delay delay = (Delay)theEObject;
				T result = caseDelay(delay);
				if (result == null) result = caseParameterizable(delay);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case PiMMPackage.EXPRESSION: {
				Expression expression = (Expression)theEObject;
				T result = caseExpression(expression);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case PiMMPackage.HREFINEMENT: {
				HRefinement hRefinement = (HRefinement)theEObject;
				T result = caseHRefinement(hRefinement);
				if (result == null) result = caseRefinement(hRefinement);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case PiMMPackage.FUNCTION_PROTOTYPE: {
				FunctionPrototype functionPrototype = (FunctionPrototype)theEObject;
				T result = caseFunctionPrototype(functionPrototype);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case PiMMPackage.FUNCTION_PARAMETER: {
				FunctionParameter functionParameter = (FunctionParameter)theEObject;
				T result = caseFunctionParameter(functionParameter);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case PiMMPackage.DATA_PORT: {
				DataPort dataPort = (DataPort)theEObject;
				T result = caseDataPort(dataPort);
				if (result == null) result = casePort(dataPort);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case PiMMPackage.BROADCAST_ACTOR: {
				BroadcastActor broadcastActor = (BroadcastActor)theEObject;
				T result = caseBroadcastActor(broadcastActor);
				if (result == null) result = caseExecutableActor(broadcastActor);
				if (result == null) result = caseAbstractActor(broadcastActor);
				if (result == null) result = caseAbstractVertex(broadcastActor);
				if (result == null) result = caseParameterizable(broadcastActor);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case PiMMPackage.JOIN_ACTOR: {
				JoinActor joinActor = (JoinActor)theEObject;
				T result = caseJoinActor(joinActor);
				if (result == null) result = caseExecutableActor(joinActor);
				if (result == null) result = caseAbstractActor(joinActor);
				if (result == null) result = caseAbstractVertex(joinActor);
				if (result == null) result = caseParameterizable(joinActor);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case PiMMPackage.FORK_ACTOR: {
				ForkActor forkActor = (ForkActor)theEObject;
				T result = caseForkActor(forkActor);
				if (result == null) result = caseExecutableActor(forkActor);
				if (result == null) result = caseAbstractActor(forkActor);
				if (result == null) result = caseAbstractVertex(forkActor);
				if (result == null) result = caseParameterizable(forkActor);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case PiMMPackage.ROUND_BUFFER_ACTOR: {
				RoundBufferActor roundBufferActor = (RoundBufferActor)theEObject;
				T result = caseRoundBufferActor(roundBufferActor);
				if (result == null) result = caseExecutableActor(roundBufferActor);
				if (result == null) result = caseAbstractActor(roundBufferActor);
				if (result == null) result = caseAbstractVertex(roundBufferActor);
				if (result == null) result = caseParameterizable(roundBufferActor);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case PiMMPackage.EXECUTABLE_ACTOR: {
				ExecutableActor executableActor = (ExecutableActor)theEObject;
				T result = caseExecutableActor(executableActor);
				if (result == null) result = caseAbstractActor(executableActor);
				if (result == null) result = caseAbstractVertex(executableActor);
				if (result == null) result = caseParameterizable(executableActor);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			default: return defaultCase(theEObject);
		}
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Parameterizable</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Parameterizable</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T caseParameterizable(Parameterizable object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Abstract Vertex</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Abstract Vertex</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T caseAbstractVertex(AbstractVertex object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Abstract Actor</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Abstract Actor</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T caseAbstractActor(AbstractActor object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Pi Graph</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Pi Graph</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T casePiGraph(PiGraph object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Actor</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Actor</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T caseActor(Actor object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Port</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Port</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T casePort(Port object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Data Input Port</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Data Input Port</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T caseDataInputPort(DataInputPort object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Data Output Port</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Data Output Port</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T caseDataOutputPort(DataOutputPort object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Config Input Port</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Config Input Port</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T caseConfigInputPort(ConfigInputPort object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Config Output Port</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Config Output Port</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T caseConfigOutputPort(ConfigOutputPort object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Fifo</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Fifo</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T caseFifo(Fifo object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Interface Actor</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Interface Actor</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T caseInterfaceActor(InterfaceActor object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Data Input Interface</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Data Input Interface</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T caseDataInputInterface(DataInputInterface object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Data Output Interface</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Data Output Interface</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T caseDataOutputInterface(DataOutputInterface object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Config Output Interface</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Config Output Interface</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T caseConfigOutputInterface(ConfigOutputInterface object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Refinement</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Refinement</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T caseRefinement(Refinement object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Parameter</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Parameter</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T caseParameter(Parameter object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Dependency</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Dependency</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T caseDependency(Dependency object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>ISetter</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>ISetter</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T caseISetter(ISetter object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Delay</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Delay</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T caseDelay(Delay object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Expression</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Expression</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T caseExpression(Expression object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>HRefinement</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>HRefinement</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T caseHRefinement(HRefinement object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Function Prototype</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Function Prototype</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T caseFunctionPrototype(FunctionPrototype object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Function Parameter</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Function Parameter</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T caseFunctionParameter(FunctionParameter object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Data Port</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Data Port</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T caseDataPort(DataPort object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Broadcast Actor</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Broadcast Actor</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T caseBroadcastActor(BroadcastActor object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Join Actor</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Join Actor</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T caseJoinActor(JoinActor object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Fork Actor</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Fork Actor</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T caseForkActor(ForkActor object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Round Buffer Actor</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Round Buffer Actor</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T caseRoundBufferActor(RoundBufferActor object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Executable Actor</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Executable Actor</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T caseExecutableActor(ExecutableActor object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Config Input Interface</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Config Input Interface</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T caseConfigInputInterface(ConfigInputInterface object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>EObject</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch, but this is the last case anyway.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>EObject</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject)
	 * @generated
	 */
	@Override
	public T defaultCase(EObject object) {
		return null;
	}

} //PiMMSwitch
