package tech.thegamedefault.wrappyj.generator.utility;


import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import jep.Interpreter;
import jep.SharedInterpreter;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import tech.thegamedefault.wrappyj.generator.framework.WPJInterpreter;
import tech.thegamedefault.wrappyj.generator.utility.ExecPythonUtility.ExecOption.InputType;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ExecPythonUtility {

  public static Map<String, Object> execute(ExecOption execOption) {
    if (Objects.requireNonNull(execOption.inputType) == InputType.FILE_PATH) {
      return executeScript(execOption.input);
    } else if (execOption.inputType == InputType.RAW_CMD) {
      return executeStatement(execOption.input,
          execOption.getOutputFields().toArray(new String[0]));
    }
    return Map.of();
  }

  private static Map<String, Object> executeStatement(String pyStatement,
      String... responseFields) {
    Interpreter interpreter = getCommonJPInterpreter();
    interpreter.exec(pyStatement);
    return responseEvaluator(interpreter, responseFields);
  }

  public static WPJInterpreter getCommonJPInterpreter() {
    return WPJInterpreter.getInterpreter();
  }

  public static WPJInterpreter getInterpreter() {
    return new WPJInterpreter(WPJInterpreter.getJepConfig());
  }

  private static Map<String, Object> executeScript(String filePath, String... responseFields) {
    try (Interpreter interpreter = getInterpreter()) {
      String pythonScript = FileUtility.readResourceFile(filePath);
      interpreter.exec(pythonScript);
      return responseEvaluator(interpreter, responseFields);
    }
  }

  private static Map<String, Object> responseEvaluator(Interpreter interpreter,
      String... responseFields) {
    Map<String, Object> result = new HashMap<>();
    for (String responseField : responseFields) {
      result.put(responseField, interpreter.getValue(responseField));
    }
    return result;
  }

  public static SharedInterpreter getSharedInterpreter() {
    return WPJInterpreter.getSharedInterpreter();
  }

  @Data
  @Builder(toBuilder = true)
  public static class ExecOption {

    private String input;
    private InputType inputType;

    private List<String> outputFields;

    public enum InputType {
      FILE_PATH, RAW_CMD
    }

  }


}
