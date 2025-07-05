package tech.thegamedefault.wrappyj.generator;

import java.util.List;
import jep.JepConfig;
import lombok.extern.slf4j.Slf4j;
import tech.thegamedefault.wrappyj.generator.framework.WPJInterpreter;
import tech.thegamedefault.wrappyj.generator.framework.PythonRuntimeManager;

/**
 * Main application class for the WrapPyJ framework.
 * 
 * <p>This class serves as the entry point for initializing the WrapPyJ framework
 * and setting up the Python runtime environment. It provides methods to configure
 * the JEP interpreter and initialize the Python runtime with required libraries.</p>
 * 
 * <p>The WrapPyJApp class is responsible for:</p>
 * <ul>
 *   <li>Initializing the Python runtime environment</li>
 *   <li>Configuring JEP interpreter settings</li>
 *   <li>Setting up required Python libraries</li>
 *   <li>Providing a clean initialization interface</li>
 * </ul>
 * 
 * <p>This class should be used to initialize the framework before using any
 * Python functionality or generating Java wrappers.</p>
 * 
 * @author WrapPyJ Team
 * @version 1.0
 * @since 1.0
 * @see PythonRuntimeManager
 * @see WPJInterpreter
 * @see WrapPyJGenerator
 */
@Slf4j
public class WrapPyJApp {

  /**
   * Initializes the WrapPyJ framework with default JEP configuration.
   * 
   * <p>This method initializes the Python runtime environment and sets up
   * the JEP interpreter with default configuration settings. It should be
   * called once before using any Python functionality in the application.</p>
   * 
   * <p>The initialization process includes:</p>
   * <ul>
   *   <li>Setting up the Python runtime environment</li>
   *   <li>Installing required Python libraries</li>
   *   <li>Configuring JEP with default settings</li>
   * </ul>
   * 
   * @param requiredLibraries list of Python library names to install and make available
   */
  public static void init(List<String> requiredLibraries) {
    init(requiredLibraries, WPJInterpreter.getJepConfig());
  }

  /**
   * Initializes the WrapPyJ framework with custom JEP configuration.
   * 
   * <p>This method initializes the Python runtime environment and sets up
   * the JEP interpreter with custom configuration settings. This allows
   * fine-grained control over the JEP interpreter behavior.</p>
   * 
   * <p>The initialization process includes:</p>
   * <ul>
   *   <li>Setting up the Python runtime environment</li>
   *   <li>Installing required Python libraries</li>
   *   <li>Configuring JEP with the provided settings</li>
   * </ul>
   * 
   * @param requiredLibraries list of Python library names to install and make available
   * @param jepConfig custom JEP configuration to use for the interpreter
   */
  public static void init(List<String> requiredLibraries, JepConfig jepConfig) {
    WPJInterpreter.setConfig(jepConfig);
    PythonRuntimeManager.initialize(requiredLibraries);
  }

  /**
   * Main entry point for the WrapPyJ application.
   * 
   * <p>This method serves as the main entry point when running WrapPyJ as a
   * standalone application. Currently, this method is empty and serves as a
   * placeholder for future command-line interface functionality.</p>
   * 
   * @param args command-line arguments (currently unused)
   */
  public static void main(String[] args) {

  }

}
