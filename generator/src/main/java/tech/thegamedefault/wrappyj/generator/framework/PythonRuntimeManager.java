package tech.thegamedefault.wrappyj.generator.framework;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import jep.MainInterpreter;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

/**
 * Manages the Python runtime environment for the WrapPyJ framework.
 *
 * <p>This class is responsible for:</p>
 * <ul>
 *   <li>Setting up an embedded Python runtime environment</li>
 *   <li>Extracting Python binaries and scripts from resources</li>
 *   <li>Installing required Python packages</li>
 *   <li>Configuring JEP (Java Embedded Python) library paths</li>
 *   <li>Verifying the Python environment is ready for use</li>
 * </ul>
 *
 * <p>The Python runtime is installed in the user's home directory under {@code .jep-runtime}
 * and includes Python 3.10 with necessary packages like JEP.</p>
 *
 * <p>This class uses a singleton pattern and should be initialized once before using
 * any Python functionality in the application.</p>
 *
 * @author WrapPyJ Team
 * @version 1.0
 * @since 1.0
 */
@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class PythonRuntimeManager {

  /**
   * The Python version to use for the embedded runtime
   */
  private static final String PYTHON_VERSION = "3.10";

  /**
   * Set of required Python packages that will be installed
   */
  private static final Set<String> REQUIRED_PACKAGES = new HashSet<>(List.of("jep"));

  /**
   * Base installation directory for the Python runtime
   */
  private static final Path INSTALL_DIR = Paths.get(System.getProperty("user.home"),
      ".jep-runtime");

  /**
   * Python home directory within the installation
   */
  public static final Path PYTHON_HOME = INSTALL_DIR.resolve("python");

  /**
   * Python scripts directory
   */
  public static final Path PY_SCRIPTS = PYTHON_HOME.resolve("scripts");

  /**
   * Name of the Python analyzer script
   */
  private static final String PY_ANALYZER_SCRIPT_NAME = "analyzer.py";

  /**
   * Initializes the Python runtime environment.
   *
   * <p>This method performs the following steps:</p>
   * <ol>
   *   <li>Sets up the required directories</li>
   *   <li>Extracts the embedded Python runtime from resources</li>
   *   <li>Extracts Python scripts (like analyzer.py)</li>
   *   <li>Configures JEP system properties</li>
   *   <li>Installs required Python packages</li>
   *   <li>Verifies the Python environment is ready</li>
   *   <li>Sets up JEP library paths</li>
   * </ol>
   *
   * <p>This method should be called once before using any Python functionality.
   * Subsequent calls will reuse the existing installation.</p>
   *
   * @param requiredPackages Additional Python packages to install beyond the default ones
   * @throws RuntimeException if initialization fails
   */
  @SneakyThrows
  public static void initialize(List<String> requiredPackages) {
    log.info("Initializing Python Runtime Manager");
    REQUIRED_PACKAGES.addAll(requiredPackages);
    setupDirectories();
    extractEmbeddedPython();
    extractPythonScripts();

    System.setProperty("jep.pythonHome", PYTHON_HOME.toAbsolutePath().toString());
    System.setProperty("jep.sharedmodules", "true");

    installPythonPackages();
    verifyPythonReady();
    setupJep();
  }

  /**
   * Sets up JEP library paths by locating the libjep shared library.
   *
   * <p>This method searches for the libjep library in the Python site-packages
   * directory and configures the JEP MainInterpreter to use it.</p>
   *
   * @throws IllegalStateException if libjep cannot be located
   */
  @SneakyThrows
  private static void setupJep() {
    String cmd = """
        import site, os, glob
        matches = glob.glob(os.path.join(site.getsitepackages()[0], 'jep/libjep.*'))
        if not matches:
            raise RuntimeError("libjep not found in site-packages.")
        print(matches[0])
        """;

    Process process = new ProcessBuilder(getPythonExecutable(), "-c", cmd).start();
    BufferedReader in = new BufferedReader(new InputStreamReader(process.getInputStream()));
    String jepLibPath = in.readLine();

    if (process.waitFor() != 0 || jepLibPath == null || jepLibPath.isEmpty()) {
      throw new IllegalStateException("Failed to locate libjep via embedded Python.");
    }

    log.info("Using libjep at: {}", jepLibPath);
    MainInterpreter.setJepLibraryPath(jepLibPath);
  }

  /**
   * Creates the necessary directories for the Python runtime.
   *
   * @throws IOException if directory creation fails
   */
  private static void setupDirectories() throws IOException {
    Files.createDirectories(PYTHON_HOME);
  }

  /**
   * Extracts the embedded Python runtime from resources.
   *
   * <p>This method extracts a platform-specific Python runtime archive
   * from the classpath resources and unzips it to the Python home directory. The extraction is
   * skipped if a marker file indicates the runtime is already installed.</p>
   *
   * @throws IOException           if extraction fails
   * @throws FileNotFoundException if the Python archive is not found in resources
   */
  private static void extractEmbeddedPython() throws IOException {
    Path marker = PYTHON_HOME.resolve("installed.marker");
    if (Files.exists(marker)) {
      log.info("Marker already exists at: {}", marker.toAbsolutePath());
      return;
    }

    String platform = getPlatformResourcePrefix();
    log.info("Extracting embedded python from: {}", platform);
    String archiveName = "python-" + PYTHON_VERSION + ".zip";
    String resourcePath = "/python/" + platform + "/" + archiveName;

    try (InputStream in = PythonRuntimeManager.class.getResourceAsStream(resourcePath)) {
      if (in == null) {
        throw new FileNotFoundException("Embedded Python archive not found: " + resourcePath);
      }
      unzip(in, PYTHON_HOME);
    }

    Files.createFile(marker);
  }

  /**
   * Extracts Python scripts from resources to the scripts directory.
   *
   * <p>This method copies Python scripts like analyzer.py from the classpath
   * resources to the Python scripts directory for execution.</p>
   *
   * @throws IOException           if extraction fails
   * @throws FileNotFoundException if the analyzer script is not found
   */
  private static void extractPythonScripts() throws IOException {
    Path scriptTargetDir = PY_SCRIPTS;
    Files.createDirectories(scriptTargetDir);
    File file = new File("src/main/resources/python/scripts/" + PY_ANALYZER_SCRIPT_NAME);
    System.out.println("Exists? " + file.exists());
    try (InputStream scriptStream = PythonRuntimeManager.class.getResourceAsStream(
        "/python/scripts/" + PY_ANALYZER_SCRIPT_NAME)) {
      if (scriptStream == null) {
        throw new FileNotFoundException("Python analyzer script not found in resources.");
      }
      Files.copy(scriptStream, scriptTargetDir.resolve(PY_ANALYZER_SCRIPT_NAME),
          StandardCopyOption.REPLACE_EXISTING);
    }
  }

  /**
   * Installs required Python packages using pip.
   *
   * <p>This method runs pip install for each required package in the
   * embedded Python environment.</p>
   *
   * @throws IOException          if process execution fails
   * @throws InterruptedException if the installation process is interrupted
   * @throws RuntimeException     if package installation fails
   */
  private static void installPythonPackages() throws IOException, InterruptedException {
    log.info("Installing python packages");
    for (String pkg : REQUIRED_PACKAGES) {
      Process process = new ProcessBuilder(getPythonExecutable(), "-m", "pip", "install", pkg)
          .inheritIO()
          .start();
      if (process.waitFor() != 0) {
        throw new RuntimeException("Failed to install package: " + pkg);
      }
    }
  }

  /**
   * Verifies that the Python environment is ready for use.
   *
   * <p>This method runs a simple Python command to ensure the environment
   * is working correctly.</p>
   *
   * @throws IOException           if process execution fails
   * @throws InterruptedException  if the verification process is interrupted
   * @throws IllegalStateException if Python environment verification fails
   */
  private static void verifyPythonReady() throws IOException, InterruptedException {
    Process process = new ProcessBuilder(getPythonExecutable(), "-c", "import math").inheritIO()
        .start();
    if (process.waitFor() != 0) {
      throw new IllegalStateException("Embedded Python environment failed.");
    }
    log.info("Python environment is ready");
  }

  /**
   * Gets the path to the Python executable.
   *
   * <p>Returns the appropriate Python executable path based on the operating system
   * and makes it executable if needed.</p>
   *
   * @return the path to the Python executable as a string
   */
  private static String getPythonExecutable() {
    String bin = isWindows() ? "python.exe" : "bin/python3";
    Path pythonPath = PYTHON_HOME.resolve(bin).toAbsolutePath();
    makeExecutable(pythonPath);
    return pythonPath.toString();
  }

  /**
   * Unzips an input stream to a target directory.
   *
   * <p>This method extracts all files from a ZIP input stream to the specified
   * target directory, creating directories as needed.</p>
   *
   * @param in        the input stream containing the ZIP data
   * @param targetDir the target directory to extract to
   * @throws IOException if extraction fails
   */
  private static void unzip(InputStream in, Path targetDir) throws IOException {
    try (ZipInputStream zipIn = new ZipInputStream(in)) {
      ZipEntry entry;
      while ((entry = zipIn.getNextEntry()) != null) {
        Path filePath = targetDir.resolve(entry.getName());
        if (entry.isDirectory()) {
          Files.createDirectories(filePath);
        } else {
          Files.createDirectories(filePath.getParent());
          Files.copy(zipIn, filePath, StandardCopyOption.REPLACE_EXISTING);
        }
        zipIn.closeEntry();
      }
    }
  }

  /**
   * Checks if the current operating system is Windows.
   *
   * @return true if running on Windows, false otherwise
   */
  private static boolean isWindows() {
    return System.getProperty("os.name").toLowerCase().contains("win");
  }

  /**
   * Gets the platform-specific resource prefix for Python runtime archives.
   *
   * <p>This method determines the correct directory name for platform-specific
   * Python runtime resources based on the operating system and architecture.</p>
   *
   * <p>The returned values correspond to the directory structure in resources:
   * <ul>
   *   <li>Windows: "win32"</li>
   *   <li>macOS ARM64: "macos-arm64"</li>
   *   <li>macOS x86_64: "macos-x86_64"</li>
   *   <li>Linux x86_64: "linux-x86_64"</li>
   *   <li>Linux other: "linux-x86"</li>
   * </ul></p>
   *
   * @return the platform-specific resource prefix
   */
  private static String getPlatformResourcePrefix() {
    String os = System.getProperty("os.name").toLowerCase();
    String arch = System.getProperty("os.arch").toLowerCase();
    boolean is64 = "64".equals(System.getProperty("sun.arch.data.model"));

    if (os.contains("win")) {                        // Windows: always x86
      return "win32";
    }
    if (os.contains("mac")) {                       // macOS
      return (arch.contains("arm") || arch.contains("aarch"))
          ? "macos-arm64"
          : "macos-x86_64";
    }
    if (os.contains("linux")) {                     // Linux
      boolean isArm = arch.contains("arm") || arch.contains("aarch");
      if (isArm && is64) {
        return "linux-arm64";
      }
      if (is64) {
        return "linux-x86_64";
      }
      return "linux-x86";
    }
    return "unknown";
  }


  /**
   * Makes a file executable.
   *
   * <p>This method sets the executable permission on the specified file.
   * This is particularly important for Python executables on Unix-like systems.</p>
   *
   * @param pythonExecutable the path to the file to make executable
   * @throws RuntimeException if setting executable permission fails
   */
  private static void makeExecutable(Path pythonExecutable) {
    if (!Files.exists(pythonExecutable)) {
      throw new IllegalStateException("Python executable not found at " + pythonExecutable);
    }
    File pyFile = pythonExecutable.toFile();
    if (!pyFile.canExecute()) {
      boolean success = pyFile.setExecutable(true);
      if (!success) {
        throw new RuntimeException("Failed to make Python executable: " + pythonExecutable);
      }
    }
  }
}
