package tech.thegamedefault.wrappyj.example.generator;

import jakarta.annotation.PostConstruct;
import java.nio.file.Paths;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import tech.thegamedefault.wrappyj.example.generator.generated.pandas.JDataFrame;
import tech.thegamedefault.wrappyj.example.generator.generated.pandas.JPandas;
import tech.thegamedefault.wrappyj.generator.model.JDict;
import tech.thegamedefault.wrappyj.generator.model.JLambda;

@Slf4j
@Component
public class ThePandas {

  public static final String CSV_PATH = Paths.get("src/main/resources/test-data/test.csv")
      .toAbsolutePath().toString();

  @PostConstruct
  public void init() {
    testBasic();
    testMissingValuesAndTypeConversion();
    testFilterByAge();
    testAddDerivedColumn();
    testSelectColumns();
  }

  private void testBasic() {

    JDataFrame df = new JDataFrame(JPandas.read_csv(CSV_PATH)); // pandas.read_csv()

    Object head = df.head(); // pandas.DataFrame.head()
    log.info(">>> HEAD: {}", head);

    df.info(); // pandas.DataFrame.info()

    Object desc = df.describe();            // pandas.DataFrame.describe()
    log.info(">>> DESCRIBE: {}", desc);

    Object gb = new JDataFrame(df.groupby("active")) // pandas.DataFrame.groupby()
        .sum();  // then .sum()
    log.info(">>> GROUPBY SUM (by 'active'): {}", gb);
  }

  public void testMissingValuesAndTypeConversion() {
    JDataFrame df = new JDataFrame(JPandas.read_csv(CSV_PATH));
    JDataFrame cleaned = new JDataFrame(df.fillna(0));

    JDict mapDf = new JDict(Map.of("age", "int", "active", "bool"));

    Object converted = cleaned.astype(mapDf.py());
    log.info(">>> AFTER TYPE CONVERSION: {}", converted);
  }

  public void testFilterByAge() {
    JDataFrame df = new JDataFrame(JPandas.read_csv(CSV_PATH));
    Object filtered = df.queryWithKwargs("age > 30", new JDict(Map.of("engine", "python")));
    log.info(">>> FILTERED AGE > 30:\n{}", filtered);
  }

  public void testAddDerivedColumn() {
    JDataFrame df = new JDataFrame(JPandas.read_csv(CSV_PATH));
    JDataFrame withCat = new JDataFrame(df.assignWithKwargs(new JDict(Map.of("age_category",
        new JLambda(
            "lambda d: d['age'].apply(lambda a: 'Senior' if a >= 35 else 'Junior')").py()))));
    log.info("=== dataframe with derived column ===\n{}", withCat.head());
  }

  public void testSelectColumns() {
    JDataFrame df = new JDataFrame(JPandas.read_csv(CSV_PATH));
    Object selected = df.get(new String[]{"id", "active"});
    log.info(">>> SELECTED COLUMNS:\n{}", selected);
  }

}
