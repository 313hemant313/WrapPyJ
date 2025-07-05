package tech.thegamedefault.wrappyj.generator;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import lombok.Builder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import tech.thegamedefault.wrappyj.generator.framework.WPJInterpreter;
import tech.thegamedefault.wrappyj.generator.framework.JavaWrapperGenerator;
import tech.thegamedefault.wrappyj.generator.framework.PythonAnalyser;
import tech.thegamedefault.wrappyj.generator.framework.PythonRuntimeManager;

/**
 * Main generator class for creating Java wrappers around Python libraries.
 *
 * <p>This class orchestrates the entire process of generating Java wrapper classes
 * for Python libraries. It coordinates the initialization of the Python runtime, analysis of Python
 * libraries, and generation of Java wrapper code.</p>
 *
 * <p>The generation process involves:</p>
 * <ol>
 *   <li>Initializing the Python runtime environment</li>
 *   <li>Analyzing the target Python library to extract metadata</li>
 *   <li>Generating Java wrapper classes using the extracted metadata</li>
 *   <li>Cleaning up resources after generation</li>
 * </ol>
 *
 * <p>This class is the primary entry point for the WrapPyJ framework and should
 * be used to generate wrappers for any Python library that needs Java integration.</p>
 *
 * @author WrapPyJ Team
 * @version 1.0
 * @see PythonRuntimeManager
 * @see PythonAnalyser
 * @see JavaWrapperGenerator
 * @since 1.0
 */
@Slf4j
public class WrapPyJGenerator {

  /**
   * Generates Java wrapper classes for the specified Python libraries.
   *
   * <p>This method processes a list of generation requests, each specifying a Python
   * library to wrap. For each request, it:</p>
   * <ol>
   *   <li>Initializes the Python runtime with required dependencies</li>
   *   <li>Analyzes the Python library to extract metadata</li>
   *   <li>Generates Java wrapper classes based on the analysis</li>
   *   <li>Writes the generated classes to the specified output directory</li>
   * </ol>
   *
   * <p>The method handles multiple libraries in a single call, ensuring that all
   * required Python packages are installed before analysis begins.</p>
   *
   * @param requests list of generation requests, each specifying a library to wrap
   * @throws RuntimeException if any step of the generation process fails
   */
  public static void generate(List<GeneratorRequest> requests) {
    try {
      WrapPyJApp.init(requests.stream().map(GeneratorRequest::getImportDependencyName).toList());
      generateWrapper(requests);
    } catch (Exception e) {
      log.error("Exception occurred while generating wrapper: {}", e.getMessage(), e);
      throw new RuntimeException(e);
    }
  }

  /**
   * Processes individual generation requests and generates wrapper classes.
   *
   * <p>This method iterates through each generation request, analyzing the specified
   * Python library and generating corresponding Java wrapper classes. It uses the Python analyzer
   * to extract library metadata and the Java wrapper generator to create the actual Java
   * classes.</p>
   *
   * <p>After processing all requests, it closes the shared Python interpreter to
   * clean up resources.</p>
   *
   * @param requests list of generation requests to process
   */
  private static void generateWrapper(List<GeneratorRequest> requests) {

    List<Boolean> result = new ArrayList<>();
    requests.forEach(request -> {
      log.info("Generating Python Wrapper for request: {}", request);
      var libraryMetadata = PythonAnalyser.analyse(request.getLibrary(), request.getIncludeOnly());
      result.add(JavaWrapperGenerator.generateWrapper(libraryMetadata, request.getOutputPath(),
          request.getBasePackage()));
    });
    WPJInterpreter.closeCommonInterpreter();
    for (boolean b : result) {
      if (!b) {
        throw new RuntimeException("Python Wrapper generation failed");
      }
    }
  }

  /**
   * Configuration for a single Python library wrapper generation request.
   *
   * <p>This class contains all the parameters needed to generate Java wrapper
   * classes for a specific Python library. It uses the builder pattern to allow flexible
   * configuration of generation parameters.</p>
   *
   * <p>Default values are provided for common parameters like output path and
   * base package, but can be overridden as needed.</p>
   *
   * @author WrapPyJ Team
   * @version 1.0
   * @since 1.0
   */
  @Data
  @Builder
  public static class GeneratorRequest {

    /**
     * Output directory for generated Java classes.
     *
     * <p>Default value points to the standard Maven source directory structure:
     * {@code src/main/java} in the current working directory.</p>
     */
    @Builder.Default
    private String outputPath =
        System.getProperty("user.dir") + File.separator + "src" + File.separator + "main"
            + File.separator + "java";

    /**
     * Base package name for generated Java classes.
     *
     * <p>Default value is {@code tech.thegamedefault.wrappyj.generator.generated}.
     * Generated classes will be placed in subpackages based on the Python library name.</p>
     */
    @Builder.Default
    private String basePackage = "tech.thegamedefault.wrappyj.generator.generated";

    /**
     * Python package name for dependency installation.
     *
     * <p>This is the name of the Python package that should be installed via pip
     * before analyzing the library. For example, if analyzing "matplotlib.pyplot", this would
     * typically be "matplotlib".</p>
     */
    private String importDependencyName;

    /**
     * Name of the Python library to analyze and wrap.
     *
     * <p>This is the full module path of the Python library to analyze.
     * Examples include "numpy", "pandas", "matplotlib.pyplot", etc.</p>
     */
    private String library;

    /**
     * Optional list of specific items to include in the analysis.
     *
     * <p>If provided, only the specified functions, classes, or modules will be
     * included in the generated wrapper. If null or empty, all discovered items will be
     * included.</p>
     */
    private List<String> includeOnly;
  }

}
