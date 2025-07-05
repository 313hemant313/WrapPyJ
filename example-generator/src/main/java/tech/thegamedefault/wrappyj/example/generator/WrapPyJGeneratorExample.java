package tech.thegamedefault.wrappyj.example.generator;

import java.io.File;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import tech.thegamedefault.wrappyj.generator.WrapPyJApp;
import tech.thegamedefault.wrappyj.generator.WrapPyJGenerator;
import tech.thegamedefault.wrappyj.generator.WrapPyJGenerator.GeneratorRequest;

@Slf4j
public class WrapPyJGeneratorExample {

  private static String OUTPUT_PATH =
      System.getProperty("user.dir") + File.separator + "example-generator" + File.separator + "src"
          + File.separator + "main"
          + File.separator + "java";

  private static final List<GeneratorRequest> EXAMPLE_GENERATOR_REQUESTS = List.of(
      GeneratorRequest.builder()
          .outputPath(OUTPUT_PATH)
          .basePackage("tech.thegamedefault.wrappyj.example.generator.generated")
          .importDependencyName("numpy")
          .library("numpy")
          .build(),
      GeneratorRequest.builder()
          .outputPath(OUTPUT_PATH)
          .basePackage("tech.thegamedefault.wrappyj.example.generator.generated")
          .importDependencyName("pandas")
          .library("pandas")
          .build(),
      GeneratorRequest.builder()
          .outputPath(OUTPUT_PATH)
          .basePackage("tech.thegamedefault.wrappyj.example.generator.generated")
          .importDependencyName("matplotlib")
          .library("matplotlib.pyplot")
          .build(),
      GeneratorRequest.builder()
          .outputPath(OUTPUT_PATH)
          .basePackage("tech.thegamedefault.wrappyj.example.generator.generated")
          .importDependencyName("matplotlib")
          .library("matplotlib")
          .includeOnly(List.of("use"))
          .build());

  public static void main(String[] args) {
    WrapPyJGenerator.generate(EXAMPLE_GENERATOR_REQUESTS);
  }

}
