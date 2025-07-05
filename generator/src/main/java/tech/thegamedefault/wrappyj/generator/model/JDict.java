package tech.thegamedefault.wrappyj.generator.model;

import java.util.Map;
import jep.Interpreter;
import jep.python.PyObject;
import tech.thegamedefault.wrappyj.generator.utility.ExecPythonUtility;

/**
 * Java wrapper for Python dictionaries.
 * 
 * <p>This class provides a bridge between Java Map objects and Python dictionaries.
 * It allows Java applications to create Python dictionaries from Java Map objects
 * and pass them to Python functions that expect keyword arguments.</p>
 * 
 * <p>The JDict class is commonly used in generated wrapper classes to support
 * Python functions that accept keyword arguments (kwargs). It converts Java Map
 * objects to Python dictionaries that can be unpacked using the {@code **} operator.</p>
 * 
 * <p>Example usage in generated code:</p>
 * <pre>{@code
 * Map<String, Object> kwargs = new HashMap<>();
 * kwargs.put("axis", 0);
 * kwargs.put("skipna", true);
 * JDict pyKwargs = new JDict(kwargs);
 * 
 * // In Python: result = function(**py_kwargs.py())
 * }</pre>
 * 
 * @author WrapPyJ Team
 * @version 1.0
 * @since 1.0
 * @see JLambda
 */
public class JDict {

  /** The underlying Python dictionary object */
  private final PyObject pyDict;

  /**
   * Creates a Python dictionary from a Java Map.
   * 
   * <p>This constructor converts a Java Map object into a Python dictionary
   * using the JEP interpreter. The resulting Python dictionary can be used
   * as keyword arguments when calling Python functions.</p>
   * 
   * @param javaMap the Java Map to convert to a Python dictionary
   */
  public JDict(Map<?, ?> javaMap) {
    Interpreter i = ExecPythonUtility.getSharedInterpreter();
    i.set("javaMap", javaMap);
    i.exec("py_dict = dict(javaMap)");
    this.pyDict = i.getValue("py_dict", PyObject.class);
  }

  /**
   * Returns the underlying Python dictionary object.
   * 
   * <p>This method provides access to the PyObject representing the Python
   * dictionary. This object can be used directly in Python function calls
   * or converted to other Python types as needed.</p>
   * 
   * @return the PyObject representing the Python dictionary
   */
  public PyObject py() {
    return pyDict;
  }

}
