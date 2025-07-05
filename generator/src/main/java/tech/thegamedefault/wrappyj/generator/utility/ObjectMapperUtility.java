package tech.thegamedefault.wrappyj.generator.utility;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.io.IOException;
import java.util.List;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Utility class for JSON serialization and deserialization using Jackson.
 * 
 * <p>This class provides a centralized, pre-configured ObjectMapper instance
 * for JSON operations throughout the WrapPyJ framework. It includes common
 * configurations for handling various data types and edge cases.</p>
 * 
 * <p>The ObjectMapper is configured with:</p>
 * <ul>
 *   <li>JavaTimeModule for handling Java 8 time types</li>
 *   <li>Auto-discovery of Jackson modules</li>
 *   <li>Lenient deserialization (ignores unknown properties)</li>
 *   <li>Lenient serialization (allows empty beans)</li>
 * </ul>
 * 
 * <p>This utility is primarily used for:</p>
 * <ul>
 *   <li>Converting Python analysis results to Java objects</li>
 *   <li>Serializing configuration objects</li>
 *   <li>Converting between different object types via JSON</li>
 * </ul>
 * 
 * @author WrapPyJ Team
 * @version 1.0
 * @since 1.0
 */
@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ObjectMapperUtility {

  /** Pre-configured ObjectMapper instance for JSON operations */
  private static final ObjectMapper INSTANCE = new ObjectMapper();

  static {
    INSTANCE.registerModule(new JavaTimeModule());
    INSTANCE.findAndRegisterModules();
    INSTANCE.configure(DeserializationFeature.FAIL_ON_IGNORED_PROPERTIES, false);
    INSTANCE.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    INSTANCE.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
  }

  /**
   * Gets the pre-configured ObjectMapper instance.
   * 
   * <p>Returns the singleton ObjectMapper instance that has been configured
   * with appropriate settings for the WrapPyJ framework.</p>
   * 
   * @return the configured ObjectMapper instance
   */
  public static ObjectMapper getInstance() {
    return INSTANCE;
  }

  /**
   * Converts a list of objects to a list of a different type.
   * 
   * <p>This method converts each element in the input list to the specified
   * target type using Jackson's conversion capabilities.</p>
   * 
   * @param <I> the input type
   * @param <O> the output type
   * @param value the list of objects to convert
   * @param requiredValueType the target class type
   * @return a new list containing the converted objects
   */
  public static <I, O> List<O> convertValue(List<I> value, Class<O> requiredValueType) {
    return value.stream()
        .map(object -> convertValue(object, requiredValueType))
        .toList();
  }

  /**
   * Converts an object to a different type.
   * 
   * <p>This method uses Jackson's conversion capabilities to transform an object
   * from one type to another, typically by serializing to JSON and then
   * deserializing to the target type.</p>
   * 
   * @param <I> the input type
   * @param <O> the output type
   * @param value the object to convert
   * @param requiredValueType the target class type
   * @return the converted object
   */
  public static <I, O> O convertValue(I value, Class<O> requiredValueType) {
    return INSTANCE.convertValue(value, requiredValueType);
  }

  /**
   * Deserializes a JSON string to an object of the specified type.
   * 
   * <p>This method parses a JSON string and creates an object of the specified
   * class. If parsing fails, it logs an error and returns null.</p>
   * 
   * @param <T> the target type
   * @param source the JSON string to parse
   * @param valueType the target class type
   * @return the deserialized object, or null if parsing fails
   */
  public static <T> T readValue(String source, Class<T> valueType) {
    try {
      return INSTANCE.readValue(source, valueType);
    } catch (IOException e) {
      log.error("Unable to convert to object {}", valueType.getName(), e);
      return null;
    }
  }

  /**
   * Deserializes a JSON string to an object using a TypeReference.
   * 
   * <p>This method is useful for deserializing complex types like generic
   * collections or parameterized types. If parsing fails, it logs an error
   * and returns null.</p>
   * 
   * @param <T> the target type
   * @param source the JSON string to parse
   * @param valueTypeRef the TypeReference describing the target type
   * @return the deserialized object, or null if parsing fails
   */
  public static <T> T readValue(String source, TypeReference<T> valueTypeRef) {
    try {
      return INSTANCE.readValue(source, valueTypeRef);
    } catch (IOException e) {
      log.error("Unable to convert to object {}", valueTypeRef.getType(), e);
      return null;
    }
  }

  /**
   * Serializes an object to a JSON string.
   * 
   * <p>This method converts an object to its JSON string representation.
   * If serialization fails, it logs an error and returns null.</p>
   * 
   * @param <T> the type of the object to serialize
   * @param object the object to serialize
   * @return the JSON string representation, or null if serialization fails
   */
  public static <T> String writeValueAsString(T object) {
    try {
      return INSTANCE.writeValueAsString(object);
    } catch (JsonProcessingException e) {
      log.error("Error parsing object to string ", e);
      return null;
    }
  }

  /**
   * Converts an object to a different type via JSON serialization/deserialization.
   * 
   * <p>This method performs a two-step conversion: first serializes the source
   * object to JSON, then deserializes it to the target type. This is useful
   * for converting between objects that don't have direct conversion paths.</p>
   * 
   * @param <T> the target type
   * @param <X> the source type
   * @param source the object to convert
   * @param valueTypeRef the TypeReference describing the target type
   * @return the converted object, or null if conversion fails
   */
  public static <T, X> T convertByString(X source, TypeReference<T> valueTypeRef) {
    try {
      return INSTANCE.readValue(writeValueAsString(source), valueTypeRef);
    } catch (IOException e) {
      log.error("Unable to convert to object {}", valueTypeRef.getType(), e);
      return null;
    }
  }

}

