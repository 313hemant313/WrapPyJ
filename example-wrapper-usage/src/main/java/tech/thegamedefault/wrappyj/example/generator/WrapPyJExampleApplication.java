package tech.thegamedefault.wrappyj.example.generator;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import tech.thegamedefault.wrappyj.generator.WrapPyJApp;
import tech.thegamedefault.wrappyj.generator.framework.WPJInterpreter;

@Slf4j
@SpringBootApplication
@RequiredArgsConstructor
public class WrapPyJExampleApplication {

  public static void main(String[] args) {
    SpringApplication.run(WrapPyJExampleApplication.class, args);
  }

  @PostConstruct
  public void init() {
    WrapPyJApp.init(List.of("pandas", "numpy", "matplotlib"));
  }

  @PreDestroy
  public static void shutdown() {
    WPJInterpreter.getSharedInterpreter().close();
  }

}
