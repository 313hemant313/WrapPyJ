package tech.thegamedefault.wrappyj.example.generator;

import jakarta.annotation.PostConstruct;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import tech.thegamedefault.wrappyj.example.generator.generated.numpy.JNumpy;

@Slf4j
@Component
public class TheNumpy {

  @PostConstruct
  public void init() {
    testNumpy();
  }

  private void testNumpy() {
    Object result = JNumpy.sum(List.of(1, 2, 3, 4, 5));

    long actual = ((long[]) result)[0];  // extract first element
    log.info("[long[]] Numpy result: {}", actual);
    assert actual == 15 : "Test failed. Expected 15 but got: " + result;
  }

}
