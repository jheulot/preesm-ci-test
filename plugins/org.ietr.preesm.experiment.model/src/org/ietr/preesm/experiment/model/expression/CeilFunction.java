/*******************************************************************************
 * Copyright or © or Copr. 2012 - 2017 IETR/INSA:
 *
 * Antoine Morvan <antoine.morvan@insa-rennes.fr> (2017)
 * Maxime Pelcat <Maxime.Pelcat@insa-rennes.fr> (2012)
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

package org.ietr.preesm.experiment.model.expression;

import java.util.Stack;
import org.nfunk.jep.ParseException;
import org.nfunk.jep.function.PostfixMathCommand;

// TODO: Auto-generated Javadoc
/**
 * The Class CeilFunction.
 */
public class CeilFunction extends PostfixMathCommand {

  /**
   * Instantiates a new ceil function.
   */
  public CeilFunction() {
    this.numberOfParameters = -1;
  }

  /**
   * Ceil.
   *
   * @param d1
   *          the d 1
   * @return the double
   */
  public Double ceil(final Number d1) {
    return Math.ceil(d1.doubleValue());
  }

  /**
   * Adds two numbers together. The parameters can be of type Number, Complex, or String. If a certain combination of types is not supported, a ParseException
   * is thrown.
   *
   * @param param1
   *          The first parameter to be added.
   * @return The sum of param1 and param2, or concatenation of the two if they are Strings.
   * @throws ParseException
   *           the parse exception
   */
  public Object ceil(final Object param1) throws ParseException {
    if (param1 instanceof Number) {
      return ceil((Number) param1);
    } else if ((param1 instanceof String)) {
      return "%" + (String) param1;
    }

    throw new ParseException("Invalid parameter type");
  }

  /**
   * Calculates the result of applying the "+" operator to the arguments from the stack and pushes it back on the stack.
   *
   * @param stack
   *          the stack
   * @throws ParseException
   *           the parse exception
   */
  @SuppressWarnings({ "rawtypes", "unchecked" })
  @Override
  public void run(final Stack stack) throws ParseException {
    checkStack(stack);// check the stack

    Object ceil;
    Object param1;
    // get the parameter from the stack
    param1 = stack.pop();

    // add it to the sum (order is important for String arguments)
    ceil = ceil(param1);

    stack.push(ceil);

    return;
  }

}
