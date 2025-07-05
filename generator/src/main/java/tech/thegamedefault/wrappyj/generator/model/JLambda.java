package tech.thegamedefault.wrappyj.generator.model;

import jep.Interpreter;
import jep.python.PyObject;
import tech.thegamedefault.wrappyj.generator.utility.ExecPythonUtility;

/**
 * Java wrapper for Python lambda functions.
 * 
 * <p>This class provides a bridge between Java string representations of Python
 * lambda expressions and actual Python lambda functions. It allows Java applications
 * to create Python lambda functions from string expressions and use them in Python
 * function calls.</p>
 * 
 * <p>The JLambda class is commonly used in generated wrapper classes to support
 * Python functions that accept lambda functions as parameters, such as pandas
 * apply functions or numpy operations.</p>
 * 
 * <p>Example usage in generated code:</p>
 * <pre>{@code
 * JLambda lambda = new JLambda("lambda x: x * 2");
 * 
 * // In Python: result = df.apply(lambda.py())
 * }</pre>
 * 
 * <p><strong>Security Note:</strong> This class uses {@code eval()} to evaluate
 * lambda expressions, which can be dangerous if used with untrusted input.
 * Only use with trusted lambda expressions.</p>
 * 
 * @author WrapPyJ Team
 * @version 1.0
 * @since 1.0
 * @see JDict
 */
public class JLambda {

  /** The underlying Python lambda function object */
  private final PyObject pyDict;

  /**
   * Creates a Python lambda function from a string expression.
   * 
   * <p>This constructor converts a string representation of a Python lambda
   * expression into an actual Python lambda function using the JEP interpreter.
   * The resulting lambda function can be used as a parameter in Python function calls.</p>
   * 
   * <p><strong>Security Warning:</strong> This method uses {@code eval()} to
   * evaluate the lambda expression. Only use with trusted input to avoid
   * potential security vulnerabilities.</p>
   * 
   * @param lambdaExpr the Python lambda expression as a string (e.g., "lambda x: x * 2")
   */
  public JLambda(String lambdaExpr) {
    Interpreter i = ExecPythonUtility.getSharedInterpreter();
    i.set("lambdaExpr", lambdaExpr);
    i.exec("py_lambda = eval(lambdaExpr)");
    this.pyDict = i.getValue("py_lambda", PyObject.class);
  }

  /**
   * Returns the underlying Python lambda function object.
   * 
   * <p>This method provides access to the PyObject representing the Python
   * lambda function. This object can be used directly in Python function calls
   * that expect lambda functions as parameters.</p>
   * 
   * @return the PyObject representing the Python lambda function
   */
  public PyObject py() {
    return pyDict;
  }

}
