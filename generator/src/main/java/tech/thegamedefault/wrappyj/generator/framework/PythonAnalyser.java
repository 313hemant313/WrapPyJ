package tech.thegamedefault.wrappyj.generator.framework;

import java.util.List;
import java.util.Map;
import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.CollectionUtils;
import tech.thegamedefault.wrappyj.generator.utility.ExecPythonUtility;
import tech.thegamedefault.wrappyj.generator.utility.ExecPythonUtility.ExecOption;
import tech.thegamedefault.wrappyj.generator.utility.ExecPythonUtility.ExecOption.InputType;
import tech.thegamedefault.wrappyj.generator.utility.ObjectMapperUtility;

/**
 * Analyzes Python libraries to extract metadata for Java wrapper generation.
 * 
 * <p>This class provides functionality to analyze Python libraries and extract
 * information about their classes, functions, and methods. It uses a Python
 * analyzer script to inspect the library structure and returns metadata that
 * can be used by the Java wrapper generator.</p>
 * 
 * <p>The analysis includes:</p>
 * <ul>
 *   <li>Library functions and their signatures</li>
 *   <li>Classes and their methods</li>
 *   <li>Documentation strings</li>
 *   <li>Module information</li>
 * </ul>
 * 
 * <p>This class is designed to work with the embedded Python runtime managed
 * by {@link PythonRuntimeManager}.</p>
 * 
 * @author WrapPyJ Team
 * @version 1.0
 * @since 1.0
 * @see PythonRuntimeManager
 * @see JavaWrapperGenerator
 */
@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class PythonAnalyser {

  /**
   * Python statement template for analyzing libraries.
   * 
   * <p>This template executes the analyzer script with the specified library name
   * and include filter, then returns the result as JSON.</p>
   */
  private static final String PY_STATEMENT = """
      import traceback
      try:
          from analyzer import analyze_library
          import json
          result = analyze_library("%s", %s)
          json_result = json.dumps(result)
      except Exception as e:
          print("Exception while analyzing:")
          print("Exception:", repr(e))
          print("Traceback:")
          print(traceback.format_exc())
          raise e
      """;

  /**
   * Analyzes a Python library and returns metadata for wrapper generation.
   * 
   * <p>This method executes a Python script that analyzes the specified library
   * and extracts information about its structure, including functions, classes,
   * and their documentation.</p>
   * 
   * <p>The analysis is performed using the embedded Python runtime and the
   * analyzer.py script that was extracted during initialization.</p>
   * 
   * @param libName the name of the Python library to analyze (e.g., "numpy", "pandas")
   * @param includeOnly optional list of specific items to include in the analysis.
   *                    If null or empty, all items are included
   * @return LibraryMetadata containing the analysis results
   * @throws RuntimeException if the analysis fails or returns invalid data
   */
  public static LibraryMetadata analyse(String libName, List<String> includeOnly) {

    String allow = CollectionUtils.isEmpty(includeOnly) ? "None"
        : ObjectMapperUtility.writeValueAsString(includeOnly);
    Map<String, Object> output = ExecPythonUtility.execute(ExecOption.builder()
        .input(PY_STATEMENT.formatted(libName, allow))
        .inputType(InputType.RAW_CMD)
        .outputFields(List.of("json_result"))
        .build());

    log.info("Python analysis result: {}", output);
    return ObjectMapperUtility.readValue((String) output.get("json_result"),
        LibraryMetadata.class);
  }

  /**
   * Metadata about a Python library including its functions and classes.
   * 
   * <p>This class contains the complete analysis results for a Python library,
   * including all discovered functions and classes with their metadata.</p>
   * 
   * @author WrapPyJ Team
   * @version 1.0
   * @since 1.0
   */
  @Data
  public static class LibraryMetadata {

    /** The name of the analyzed library */
    private String library;
    
    /** List of functions found in the library */
    private List<PyFunction> functions;
    
    /** List of classes found in the library */
    private List<PyClass> classes;
  }

  /**
   * Metadata about a Python class.
   * 
   * <p>This class contains information about a Python class including its name,
   * documentation, module, and methods.</p>
   * 
   * @author WrapPyJ Team
   * @version 1.0
   * @since 1.0
   */
  @Data
  public static class PyClass {

    /** The name of the Python class */
    private String name;
    
    /** The documentation string (docstring) of the class */
    private String doc;
    
    /** The module where the class is defined */
    private String module;
    
    /** List of methods belonging to this class */
    private List<PyFunction> methods;
  }

  /**
   * Metadata about a Python function or method.
   * 
   * <p>This class contains information about a Python function including its name,
   * arguments, documentation, and module.</p>
   * 
   * @author WrapPyJ Team
   * @version 1.0
   * @since 1.0
   */
  @Data
  public static class PyFunction {

    /** The name of the Python function or method */
    private String name;
    
    /** List of argument names for the function */
    private List<String> args;
    
    /** Number of optional arguments (arguments with default values) */
    private int optionalCount;
    
    /** The documentation string (docstring) of the function */
    private String doc;
    
    /** The module where the function is defined */
    private String module;
  }

}
