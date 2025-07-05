package tech.thegamedefault.wrappyj.example.generator;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import tech.thegamedefault.wrappyj.example.generator.generated.matplotlib.JMatplotlib;
import tech.thegamedefault.wrappyj.example.generator.generated.matplotlib_pyplot.JPyplot;
import tech.thegamedefault.wrappyj.example.generator.generated.numpy.JNumpy;

@Slf4j
@Component
public class TheMatplotlibPyplot {

  @PostConstruct
  public void init() {

    // For headless
    JMatplotlib.use("Agg");

    testPlot();
  }

  public void testPlot() {
    Object xs = JNumpy.linspace(0, 6.28, 100);
    Object ys = JNumpy.sin(xs);

    JPyplot.plot(xs, ys);
    JPyplot.title("Sine wave (from Java)");
    JPyplot.savefig("./src/main/resources/generated/sine.png");
  }

}
