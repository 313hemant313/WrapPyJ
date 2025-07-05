package tech.thegamedefault.wrappyj.generator.framework;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Modifier;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import tech.thegamedefault.wrappyj.generator.framework.PythonAnalyser.LibraryMetadata;
import tech.thegamedefault.wrappyj.generator.framework.PythonAnalyser.PyClass;
import tech.thegamedefault.wrappyj.generator.framework.PythonAnalyser.PyFunction;
import tech.thegamedefault.wrappyj.generator.model.JDict;

/**
 * Generates Java wrapper classes for Python libraries using JavaPoet.
 *
 * <p>This class is responsible for creating Java source code that wraps Python
 * libraries, making them accessible from Java applications. It uses the JavaPoet library to
 * generate clean, well-formatted Java code.</p>
 *
 * <p>The generator creates two types of wrapper classes:</p>
 * <ul>
 *   <li><strong>Module-level wrappers:</strong> Static classes that wrap module-level
 *       functions (e.g., {@code JPandas} for pandas functions)</li>
 *   <li><strong>Class-level wrappers:</strong> Instance classes that wrap Python
 *       classes and their methods (e.g., {@code JDataFrame} for pandas.DataFrame)</li>
 * </ul>
 *
 * <p>The generated classes use JEP (Java Embedded Python) to interact with the
 * underlying Python objects and provide a Java-friendly interface to Python
 * functionality.</p>
 *
 * <p>Key features of the generated code:</p>
 * <ul>
 *   <li>Method overloading for optional parameters</li>
 *   <li>Proper Java naming conventions</li>
 *   <li>Comprehensive JavaDoc documentation</li>
 *   <li>Support for keyword arguments via JDict</li>
 *   <li>Automatic filtering of internal Python symbols</li>
 * </ul>
 *
 * @author WrapPyJ Team
 * @version 1.0
 * @see PythonAnalyser
 * @see LibraryMetadata
 * @since 1.0
 */
@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class JavaWrapperGenerator {

  /**
   * Generates Java wrapper classes for a Python library.
   *
   * <p>This method creates a complete set of Java wrapper classes based on the
   * analysis metadata of a Python library. The generation process includes:</p>
   * <ol>
   *   <li>Creating a module-level wrapper class for static functions</li>
   *   <li>Filtering and deduplicating Python classes</li>
   *   <li>Creating class-level wrapper classes for Python classes</li>
   *   <li>Generating overloaded methods for different parameter counts</li>
   *   <li>Writing all generated classes to the specified output directory</li>
   * </ol>
   *
   * <p>The generated classes are organized in packages based on the Python library
   * name, with dots replaced by underscores to create valid Java package names.</p>
   *
   * @param metadata    the analysis metadata containing library structure information
   * @param outputDir   the directory where generated Java files will be written
   * @param basePackage the base package name for generated classes
   * @throws RuntimeException if code generation or file writing fails
   */
  @SneakyThrows
  public static boolean generateWrapper(LibraryMetadata metadata, String outputDir,
      String basePackage) {

    if (Objects.isNull(metadata) || Objects.isNull(metadata.getLibrary())) {
      log.error("Invalid library metadata, metadata or metadata.library is null");
      return false;
    }

    Path out = Paths.get(outputDir);

    String libFqn = metadata.getLibrary();          //  e.g.  "matplotlib.pyplot"
    String libShort = libFqn.contains(".")
        ? libFqn.substring(libFqn.lastIndexOf('.') + 1) // "pyplot"
        : libFqn;
    String pkgBase = basePackage + "." + libFqn.replace('.', '_');

    // 1) Module-level wrapper (e.g. JPandas)
    String moduleClassName = "J" + capitalize(libShort);
    TypeSpec.Builder moduleBuilder = TypeSpec.classBuilder(moduleClassName)
        .addModifiers(Modifier.PUBLIC)
        .addJavadoc("Wrapper for Python module: $L\n", metadata.getLibrary());

    // imports() helper
    moduleBuilder.addMethod(MethodSpec.methodBuilder("imports")
        .addModifiers(Modifier.PROTECTED, Modifier.STATIC)
        .addJavadoc("Return Python import statement.\n")
        .addCode("return \"import %s;\";".formatted(metadata.getLibrary()))
        .returns(String.class)
        .build());

    // static functions
    for (PyFunction fn : metadata.getFunctions()) {
      for (MethodSpec m : generateOverloadedStaticMethods(metadata, fn)) {
        moduleBuilder.addMethod(m);
      }
    }

    // write module class
    JavaFile.builder(pkgBase, moduleBuilder.build())
        .addFileComment("Generated by JavaWrapperGenerator")
        .build()
        .writeTo(out);

    // 2) Filter and dedupe classes (prefer core.frame.DataFrame)
    Map<String, PyClass> bySimpleName = new LinkedHashMap<>();
    for (PyClass pyClass : metadata.getClasses()) {
      String simple = pyClass.getName();
      String mod = pyClass.getModule();
      // skip internals
      if (mod.contains(".interchange.") || mod.contains("._libs.") || mod.contains(".testing")) {
        continue;
      }
      // prefer core.frame modules when duplicate names
      if (!bySimpleName.containsKey(simple)
          || (mod.equals(metadata.getLibrary() + ".core.frame")
          && !bySimpleName.get(simple).getModule().equals(metadata.getLibrary() + ".core.frame"))) {
        bySimpleName.put(simple, pyClass);
      }
    }
    List<PyClass> classesToWrap = new ArrayList<>(bySimpleName.values());

    // 3) Class-level wrappers
    for (PyClass pyClass : classesToWrap) {
      String base = sanitizePythonName(pyClass.getName());
      String className = "J" + base;
      TypeSpec.Builder classBuilder = TypeSpec.classBuilder(className)
          .addModifiers(Modifier.PUBLIC)
          .addJavadoc("Wrapper for Python class: $L (module: $L)\n",
              pyClass.getName(), pyClass.getModule())
          .addField(Object.class, "pyObj", Modifier.PRIVATE)
          .addMethod(MethodSpec.constructorBuilder()
              .addModifiers(Modifier.PUBLIC)
              .addParameter(Object.class, "pyObj")
              .addStatement("this.pyObj = pyObj")
              .build())
          .addMethod(MethodSpec.methodBuilder("py")
              .addModifiers(Modifier.PUBLIC)
              .returns(Object.class)
              .addStatement("return this.pyObj")
              .build())
          .addMethod(MethodSpec.methodBuilder("imports")
              .addModifiers(Modifier.PROTECTED)
              .addJavadoc("Return Python import statement.\n")
              .addCode("return \"import %s;\";".formatted(metadata.getLibrary()))
              .returns(String.class)
              .build());

      // instance methods (skip __init__)
      for (PyFunction fn : pyClass.getMethods()) {
        if ("__init__".equals(fn.getName()) || !isValidJavaIdentifier(fn.getName())) {
          log.debug("skip unusable symbol {}", fn.getName());
          continue;
        }
        for (MethodSpec m : generateOverloadedInstanceMethods(
            pyClass.getModule(), pyClass.getName(), fn)) {
          classBuilder.addMethod(m);
        }
      }

      // write class file
      JavaFile.builder(pkgBase, classBuilder.build())
          .addFileComment("Generated for Python class: " + pyClass.getName())
          .build()
          .writeTo(out);
    }
    return true;
  }

  /**
   * Generates a static method variant with a specific number of arguments.
   *
   * <p>This method creates a Java method that wraps a Python function with a
   * specific number of positional arguments. The generated method uses JEP to invoke the Python
   * function and handles module imports as needed.</p>
   *
   * @param metadata the library metadata containing function information
   * @param fn       the Python function to wrap
   * @param argCount the number of arguments for this method variant
   * @return a MethodSpec representing the generated Java method
   */
  private static MethodSpec generateStaticMethodVariant(
      LibraryMetadata metadata, PyFunction fn, int argCount) {

    String name = safeMethodName(sanitizePythonName(fn.getName()));
    MethodSpec.Builder b = MethodSpec.methodBuilder(name)
        .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
        .addJavadoc("$L\n", sanitizeDoc(fn.getDoc()))
        .returns(Object.class);

    for (int i = 0; i < argCount; i++) {
      b.addParameter(Object.class, "arg" + i);
    }

    String mod = fn.getModule();          // e.g. "matplotlib.pyplot"
    String funcName = fn.getName();            // e.g. "plot"
    String alias = "_" + sanitizePythonName(mod.replace('.', '_')); // "_matplotlib_pyplot"

    boolean needsAlias = mod.contains(".");  // any dotted module
    String callTarget = needsAlias ? alias + "." + funcName
        : mod + "." + funcName; // old path for top-level fns

    ClassName interp = ClassName.get("jep", "Interpreter");
    ClassName util = ClassName.get(
        "tech.thegamedefault.wrappyj.generator.utility", "ExecPythonUtility");

    b.addCode("$T i = $T.getSharedInterpreter();\n", interp, util);
    b.addCode("i.exec(imports());\n");                       // existing helper

    if (needsAlias) {
      b.addCode("i.exec($S);\n",
          "import importlib; " + alias + " = importlib.import_module('" + mod + "')");
    }

    String argsList = java.util.stream.IntStream.range(0, argCount)
        .mapToObj(i -> "arg" + i)
        .collect(java.util.stream.Collectors.joining(", "));

    b.addCode(argCount > 0
            ? "return i.invoke($S, " + argsList + ");\n"
            : "return i.invoke($S);\n",
        callTarget);

    return b.build();
  }

  /**
   * Generates overloaded static methods for a Python function.
   *
   * <p>This method creates multiple Java method variants for a single Python
   * function, allowing different numbers of arguments to be passed. This provides a more Java-like
   * interface by supporting method overloading.</p>
   *
   * @param metadata the library metadata containing function information
   * @param fn       the Python function to wrap
   * @return a list of MethodSpec objects representing the overloaded methods
   */
  private static List<MethodSpec> generateOverloadedStaticMethods(
      LibraryMetadata metadata, PyFunction fn) {

    int total = fn.getArgs().size();
    List<MethodSpec> list = new ArrayList<>();
    // 0..total
    for (int i = 0; i <= total; i++) {
      list.add(generateStaticMethodVariant(metadata, fn, i));
    }
    return list;
  }

  // --- instance methods ---

  /**
   * Generates overloaded instance methods for a Python class method.
   *
   * <p>This method creates multiple Java method variants for a single Python
   * class method, allowing different numbers of arguments to be passed. It skips the
   * {@code __init__} constructor method as it's handled by the class constructor.</p>
   *
   * @param module the Python module containing the class
   * @param cls    the Python class name
   * @param fn     the Python function/method to wrap
   * @return a list of MethodSpec objects representing the overloaded methods
   */
  private static List<MethodSpec> generateOverloadedInstanceMethods(
      String module, String cls, PyFunction fn) {

    if ("__init__".equals(fn.getName())) {
      return List.of();
    }

    int totalWithSelf = fn.getArgs().size();
    int total = Math.max(0, totalWithSelf - 1);       // drop `self`
    int optional = fn.getOptionalCount();
    int required = Math.max(0, total - optional);

    List<MethodSpec> list = new ArrayList<>();

    // 1) positional-only overloads
    for (int n = required; n <= total; n++) {
      list.add(generateInstanceVariant(module, cls, fn, n, false));
    }

    // 2) **kwargs overload if any optional args
    if (optional > 0) {
      list.add(generateInstanceVariant(module, cls, fn, required, true));
    }

    return list;
  }

  private static MethodSpec generateInstanceVariant(
      String module, String cls, PyFunction fn,
      int userArgCount, boolean useKwargs) {

    String methodName = safeMethodName(sanitizePythonName(fn.getName()))
        + (useKwargs ? "WithKwargs" : "");

    MethodSpec.Builder b = MethodSpec.methodBuilder(methodName)
        .addModifiers(Modifier.PUBLIC)
        .addJavadoc("$L\n", sanitizeDoc(fn.getDoc()))
        .returns(Object.class);

    // declare positional params
    for (int i = 0; i < userArgCount; i++) {
      b.addParameter(Object.class, "arg" + i);
    }
    // if kwargs, accept a JDict
    if (useKwargs) {
      b.addParameter(JDict.class, "kwargs");
    }

    ClassName interp = ClassName.get("jep", "Interpreter");
    ClassName util = ClassName.get(
        "tech.thegamedefault.wrappyj.generator.utility", "ExecPythonUtility");

    b.addCode("$T i = $T.getSharedInterpreter();\n", interp, util);
    b.addCode("i.exec(imports());\n");
    b.addCode("i.set($S, pyObj);\n", "self");

    // set positional args
    for (int i = 0; i < userArgCount; i++) {
      b.addCode("i.set($S, $L);\n", "arg" + i, "arg" + i);
    }

    if (useKwargs) {
      // convert JDict → py dict
      b.addCode("i.set($S, kwargs.py());\n", "py_kwargs");
      // build and exec `result = self.fn(*posArgs, **py_kwargs)`
      StringBuilder expr = new StringBuilder("result = self.")
          .append(fn.getName()).append("(");
      for (int i = 0; i < userArgCount; i++) {
        if (i > 0) {
          expr.append(", ");
        }
        expr.append("arg").append(i);
      }
      if (userArgCount > 0) {
        expr.append(", ");
      }
      expr.append("**py_kwargs)");
      b.addCode("i.exec($S);\n", expr.toString());
      b.addCode("return i.getValue($S, $T.class);\n",
          "result", ClassName.get("jep.python", "PyObject"));
    } else {
      // pure positional
      StringBuilder argsList = new StringBuilder();
      for (int i = 0; i < userArgCount; i++) {
        if (i > 0) {
          argsList.append(", ");
        }
        argsList.append("arg").append(i);
      }
      b.addCode(userArgCount > 0
              ? "return i.invoke($S, " + argsList + ");\n"
              : "return i.invoke($S);\n",
          "self." + fn.getName());
    }

    return b.build();
  }

  private static String capitalize(String s) {
    return (s == null || s.isEmpty())
        ? s
        : s.substring(0, 1).toUpperCase() + s.substring(1);
  }

  private static final Set<String> RESERVED = Set.of(
      "equals", "hashCode", "toString", "getClass", "notify",
      "notifyAll", "wait", "finalize", "void");

  private static String safeMethodName(String name) {
    return RESERVED.contains(name) ? name + "_py" : name;
  }

  private static String sanitizePythonName(String name) {
    return name.replaceAll("[^a-zA-Z0-9_]", "_");
  }

  private static String sanitizeDoc(String doc) {
    if (doc == null) {
      return "";
    }
    if (doc.length() > 200) {
      doc = doc.substring(0, 200) + "...";
    }
    return doc
        .replace("\\", "\\\\\\\\")
        .replace("$", "\\$")
        .replace("\r", "")
        .replace("\n", "\n * ")
        .replace("*/", "*&#47;")
        .replace("\r\n", "\n");
  }

  private static boolean isValidJavaIdentifier(String s) {
    if (s == null || s.isEmpty()) {
      return false;
    }
    if (s.equals("_")) {
      return false;          // reserved since Java 9
    }
    if (Character.isJavaIdentifierStart(s.charAt(0))) {
      for (int i = 1; i < s.length(); i++) {
        if (!Character.isJavaIdentifierPart(s.charAt(i))) {
          return false;
        }
      }
      // also ban Java keywords
      return !SourceVersion.isKeyword(s);
    }
    return false;
  }

}
