/**
 * <copyright>
 * </copyright>
 *

 */
package org.ietr.preesm.editor.iDLLanguage;

import org.eclipse.emf.ecore.EObject;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Data Type</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * <ul>
 *   <li>{@link org.ietr.preesm.editor.iDLLanguage.DataType#getBtype <em>Btype</em>}</li>
 *   <li>{@link org.ietr.preesm.editor.iDLLanguage.DataType#getCtype <em>Ctype</em>}</li>
 *   <li>{@link org.ietr.preesm.editor.iDLLanguage.DataType#getName <em>Name</em>}</li>
 * </ul>
 * </p>
 *
 * @see org.ietr.preesm.editor.iDLLanguage.IDLLanguagePackage#getDataType()
 * @model
 * @generated
 */
public interface DataType extends EObject
{
  /**
   * Returns the value of the '<em><b>Btype</b></em>' attribute.
   * The literals are from the enumeration {@link org.ietr.preesm.editor.iDLLanguage.BaseType}.
   * <!-- begin-user-doc -->
   * <p>
   * If the meaning of the '<em>Btype</em>' attribute isn't clear,
   * there really should be more of a description here...
   * </p>
   * <!-- end-user-doc -->
   * @return the value of the '<em>Btype</em>' attribute.
   * @see org.ietr.preesm.editor.iDLLanguage.BaseType
   * @see #setBtype(BaseType)
   * @see org.ietr.preesm.editor.iDLLanguage.IDLLanguagePackage#getDataType_Btype()
   * @model
   * @generated
   */
  BaseType getBtype();

  /**
   * Sets the value of the '{@link org.ietr.preesm.editor.iDLLanguage.DataType#getBtype <em>Btype</em>}' attribute.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @param value the new value of the '<em>Btype</em>' attribute.
   * @see org.ietr.preesm.editor.iDLLanguage.BaseType
   * @see #getBtype()
   * @generated
   */
  void setBtype(BaseType value);

  /**
   * Returns the value of the '<em><b>Ctype</b></em>' reference.
   * <!-- begin-user-doc -->
   * <p>
   * If the meaning of the '<em>Ctype</em>' reference isn't clear,
   * there really should be more of a description here...
   * </p>
   * <!-- end-user-doc -->
   * @return the value of the '<em>Ctype</em>' reference.
   * @see #setCtype(DataType)
   * @see org.ietr.preesm.editor.iDLLanguage.IDLLanguagePackage#getDataType_Ctype()
   * @model
   * @generated
   */
  DataType getCtype();

  /**
   * Sets the value of the '{@link org.ietr.preesm.editor.iDLLanguage.DataType#getCtype <em>Ctype</em>}' reference.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @param value the new value of the '<em>Ctype</em>' reference.
   * @see #getCtype()
   * @generated
   */
  void setCtype(DataType value);

  /**
   * Returns the value of the '<em><b>Name</b></em>' attribute.
   * <!-- begin-user-doc -->
   * <p>
   * If the meaning of the '<em>Name</em>' attribute isn't clear,
   * there really should be more of a description here...
   * </p>
   * <!-- end-user-doc -->
   * @return the value of the '<em>Name</em>' attribute.
   * @see #setName(String)
   * @see org.ietr.preesm.editor.iDLLanguage.IDLLanguagePackage#getDataType_Name()
   * @model
   * @generated
   */
  String getName();

  /**
   * Sets the value of the '{@link org.ietr.preesm.editor.iDLLanguage.DataType#getName <em>Name</em>}' attribute.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @param value the new value of the '<em>Name</em>' attribute.
   * @see #getName()
   * @generated
   */
  void setName(String value);

} // DataType
