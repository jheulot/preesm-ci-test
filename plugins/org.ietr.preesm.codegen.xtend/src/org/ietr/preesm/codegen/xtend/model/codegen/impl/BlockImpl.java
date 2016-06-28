/**
 * Copyright or © or Copr. IETR/INSA: Maxime Pelcat, Jean-François Nezan,
 * Karol Desnos, Julien Heulot
 * 
 * [mpelcat,jnezan,kdesnos,jheulot]@insa-rennes.fr
 * 
 * This software is a computer program whose purpose is to prototype
 * parallel applications.
 * 
 * This software is governed by the CeCILL-C license under French law and
 * abiding by the rules of distribution of free software.  You can  use, 
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
 */
package org.ietr.preesm.codegen.xtend.model.codegen.impl;

import java.util.Collection;
import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.common.notify.NotificationChain;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.InternalEObject;
import org.eclipse.emf.ecore.impl.ENotificationImpl;
import org.eclipse.emf.ecore.impl.EObjectImpl;
import org.eclipse.emf.ecore.util.EObjectContainmentEList;
import org.eclipse.emf.ecore.util.EObjectContainmentWithInverseEList;
import org.eclipse.emf.ecore.util.EObjectWithInverseResolvingEList;
import org.eclipse.emf.ecore.util.InternalEList;
import org.ietr.preesm.codegen.xtend.model.codegen.Block;
import org.ietr.preesm.codegen.xtend.model.codegen.CodeElt;
import org.ietr.preesm.codegen.xtend.model.codegen.CodegenPackage;
import org.ietr.preesm.codegen.xtend.model.codegen.Variable;

/**
 * <!-- begin-user-doc --> An implementation of the model object '
 * <em><b>Block</b></em>'. <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * </p>
 * <ul>
 *   <li>{@link org.ietr.preesm.codegen.xtend.model.codegen.impl.BlockImpl#getCodeElts <em>Code Elts</em>}</li>
 *   <li>{@link org.ietr.preesm.codegen.xtend.model.codegen.impl.BlockImpl#getDeclarations <em>Declarations</em>}</li>
 *   <li>{@link org.ietr.preesm.codegen.xtend.model.codegen.impl.BlockImpl#getName <em>Name</em>}</li>
 *   <li>{@link org.ietr.preesm.codegen.xtend.model.codegen.impl.BlockImpl#getDefinitions <em>Definitions</em>}</li>
 * </ul>
 *
 * @generated
 */
public class BlockImpl extends EObjectImpl implements Block {
	/**
	 * The cached value of the '{@link #getCodeElts() <em>Code Elts</em>}' containment reference list.
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @see #getCodeElts()
	 * @generated
	 * @ordered
	 */
	protected EList<CodeElt> codeElts;
	/**
	 * The cached value of the '{@link #getDeclarations() <em>Declarations</em>}' reference list.
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @see #getDeclarations()
	 * @generated
	 * @ordered
	 */
	protected EList<Variable> declarations;
	/**
	 * The default value of the '{@link #getName() <em>Name</em>}' attribute.
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @see #getName()
	 * @generated
	 * @ordered
	 */
	protected static final String NAME_EDEFAULT = null;
	/**
	 * The cached value of the '{@link #getName() <em>Name</em>}' attribute.
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @see #getName()
	 * @generated
	 * @ordered
	 */
	protected String name = NAME_EDEFAULT;
	/**
	 * The cached value of the '{@link #getDefinitions() <em>Definitions</em>}' containment reference list.
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @see #getDefinitions()
	 * @generated
	 * @ordered
	 */
	protected EList<Variable> definitions;

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	protected BlockImpl() {
		super();
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	protected EClass eStaticClass() {
		return CodegenPackage.Literals.BLOCK;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@SuppressWarnings("unchecked")
	@Override
	public EList<CodeElt> getCodeElts() {
		if (codeElts == null) {
			codeElts = new EObjectContainmentEList<CodeElt>(CodeElt.class, this, CodegenPackage.BLOCK__CODE_ELTS);
		}
		return codeElts;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@SuppressWarnings("unchecked")
	@Override
	public EList<Variable> getDeclarations() {
		if (declarations == null) {
			declarations = new EObjectWithInverseResolvingEList.ManyInverse<Variable>(Variable.class, this, CodegenPackage.BLOCK__DECLARATIONS, CodegenPackage.VARIABLE__USERS);
		}
		return declarations;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public String getName() {
		return name;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setName(String newName) {
		String oldName = name;
		name = newName;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, CodegenPackage.BLOCK__NAME, oldName, name));
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@SuppressWarnings("unchecked")
	@Override
	public EList<Variable> getDefinitions() {
		if (definitions == null) {
			definitions = new EObjectContainmentWithInverseEList<Variable>(Variable.class, this, CodegenPackage.BLOCK__DEFINITIONS, CodegenPackage.VARIABLE__CREATOR);
		}
		return definitions;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@SuppressWarnings("unchecked")
	@Override
	public NotificationChain eInverseAdd(InternalEObject otherEnd, int featureID, NotificationChain msgs) {
		switch (featureID) {
			case CodegenPackage.BLOCK__DECLARATIONS:
				return ((InternalEList<InternalEObject>)(InternalEList<?>)getDeclarations()).basicAdd(otherEnd, msgs);
			case CodegenPackage.BLOCK__DEFINITIONS:
				return ((InternalEList<InternalEObject>)(InternalEList<?>)getDefinitions()).basicAdd(otherEnd, msgs);
		}
		return super.eInverseAdd(otherEnd, featureID, msgs);
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public NotificationChain eInverseRemove(InternalEObject otherEnd, int featureID, NotificationChain msgs) {
		switch (featureID) {
			case CodegenPackage.BLOCK__CODE_ELTS:
				return ((InternalEList<?>)getCodeElts()).basicRemove(otherEnd, msgs);
			case CodegenPackage.BLOCK__DECLARATIONS:
				return ((InternalEList<?>)getDeclarations()).basicRemove(otherEnd, msgs);
			case CodegenPackage.BLOCK__DEFINITIONS:
				return ((InternalEList<?>)getDefinitions()).basicRemove(otherEnd, msgs);
		}
		return super.eInverseRemove(otherEnd, featureID, msgs);
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public Object eGet(int featureID, boolean resolve, boolean coreType) {
		switch (featureID) {
			case CodegenPackage.BLOCK__CODE_ELTS:
				return getCodeElts();
			case CodegenPackage.BLOCK__DECLARATIONS:
				return getDeclarations();
			case CodegenPackage.BLOCK__NAME:
				return getName();
			case CodegenPackage.BLOCK__DEFINITIONS:
				return getDefinitions();
		}
		return super.eGet(featureID, resolve, coreType);
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@SuppressWarnings("unchecked")
	@Override
	public void eSet(int featureID, Object newValue) {
		switch (featureID) {
			case CodegenPackage.BLOCK__NAME:
				setName((String)newValue);
				return;
			case CodegenPackage.BLOCK__DEFINITIONS:
				getDefinitions().clear();
				getDefinitions().addAll((Collection<? extends Variable>)newValue);
				return;
		}
		super.eSet(featureID, newValue);
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void eUnset(int featureID) {
		switch (featureID) {
			case CodegenPackage.BLOCK__NAME:
				setName(NAME_EDEFAULT);
				return;
			case CodegenPackage.BLOCK__DEFINITIONS:
				getDefinitions().clear();
				return;
		}
		super.eUnset(featureID);
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public boolean eIsSet(int featureID) {
		switch (featureID) {
			case CodegenPackage.BLOCK__CODE_ELTS:
				return codeElts != null && !codeElts.isEmpty();
			case CodegenPackage.BLOCK__DECLARATIONS:
				return declarations != null && !declarations.isEmpty();
			case CodegenPackage.BLOCK__NAME:
				return NAME_EDEFAULT == null ? name != null : !NAME_EDEFAULT.equals(name);
			case CodegenPackage.BLOCK__DEFINITIONS:
				return definitions != null && !definitions.isEmpty();
		}
		return super.eIsSet(featureID);
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public String toString() {
		if (eIsProxy()) return super.toString();

		StringBuffer result = new StringBuffer(super.toString());
		result.append(" (name: ");
		result.append(name);
		result.append(')');
		return result.toString();
	}

} // BlockImpl
