package tech.thegamedefault.wrappyj.generator.framework;

import static tech.thegamedefault.wrappyj.generator.framework.PythonRuntimeManager.PYTHON_HOME;
import static tech.thegamedefault.wrappyj.generator.framework.PythonRuntimeManager.PY_SCRIPTS;

import java.nio.file.Path;
import java.util.Objects;
import jep.JepConfig;
import jep.SharedInterpreter;
import jep.SubInterpreter;

public class WPJInterpreter extends SubInterpreter {

  private static WPJInterpreter instance;

  public static SharedInterpreter sharedInterpreter;

  public WPJInterpreter(JepConfig jepConfig) {
    super(jepConfig);
  }

  public static JepConfig getJepConfig() {
    JepConfig jepConfig = new JepConfig();

    Path libRoot = PYTHON_HOME.resolve("lib").resolve("python3.10");
    Path sitePackages = libRoot.resolve("site-packages");
    Path distPackages = libRoot.resolve("dist-packages");
    Path localLibRoot = PYTHON_HOME.resolve("local").resolve("lib").resolve("python3.10");
    Path localSitePackages = localLibRoot.resolve("site-packages");
    Path localDistPackages = localLibRoot.resolve("dist-packages");

    jepConfig
        .addIncludePaths(PY_SCRIPTS.toString(), sitePackages.toString(), distPackages.toString(),
            localSitePackages.toString(), localDistPackages.toString())
        .redirectStdErr(System.out)
        .redirectStdout(System.out);

    return jepConfig;
  }

  public static SharedInterpreter getSharedInterpreter() {
    if (Objects.isNull(sharedInterpreter)) {
      sharedInterpreter = new SharedInterpreter();
    }
    return sharedInterpreter;
  }


  public static WPJInterpreter getInterpreter() {
    if (Objects.isNull(instance)) {
      instance = new WPJInterpreter(getJepConfig());
    }
    return instance;
  }

  public static void closeCommonInterpreter() {
    if (Objects.nonNull(instance)) {
      instance.close();
    }
  }

  public static void setConfig(JepConfig jepConfig) {
    SharedInterpreter.setConfig(jepConfig);
  }

  public static void setConfig() {
    setConfig(getJepConfig());
  }

}
