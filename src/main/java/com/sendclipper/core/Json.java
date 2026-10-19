package com.sendclipper.core;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.io.IOException;

/**
 * The SDK's one JSON configuration. Internal: not part of the public API.
 *
 * <p>Models bind through their fields, so their getters are plain reads; fields the API adds are
 * ignored, an enum value the SDK does not know reads as its {@code UNKNOWN} constant, and unset
 * (null) request fields are left out of the body.
 */
public final class Json {
  /** The configured mapper. */
  public static final ObjectMapper MAPPER =
      new ObjectMapper()
          .registerModule(new JavaTimeModule())
          .setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.NONE)
          .setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY)
          .setSerializationInclusion(JsonInclude.Include.NON_NULL)
          .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
          .configure(DeserializationFeature.READ_UNKNOWN_ENUM_VALUES_USING_DEFAULT_VALUE, true)
          .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
          .configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);

  private Json() {}

  /**
   * Encodes a value. HTML is never escaped: an email body travels as written.
   *
   * @param value the value
   * @return its JSON bytes
   */
  public static byte[] write(Object value) {
    try {
      return MAPPER.writeValueAsBytes(value);
    } catch (JsonProcessingException e) {
      throw new IllegalArgumentException("SendClipper: the request could not be encoded", e);
    }
  }

  /**
   * Encodes a value as a string.
   *
   * @param value the value
   * @return its JSON text
   */
  public static String writeString(Object value) {
    try {
      return MAPPER.writeValueAsString(value);
    } catch (JsonProcessingException e) {
      throw new IllegalArgumentException("SendClipper: the value could not be encoded", e);
    }
  }

  /**
   * Parses bytes into a tree, or null when they are not JSON.
   *
   * @param body the bytes
   * @return the tree, or null
   */
  public static JsonNode tree(byte[] body) {
    try {
      return MAPPER.readTree(body);
    } catch (IOException e) {
      return null;
    }
  }

  /**
   * Re-reads a value as another type (a tree into a model, for instance).
   *
   * @param <T> the type
   * @param value the value
   * @param type its class
   * @return the converted value
   */
  public static <T> T convert(Object value, Class<T> type) {
    return MAPPER.convertValue(value, type);
  }
}
