package pl.smarthouse.smartmonitoring.utils;

import lombok.experimental.UtilityClass;

@UtilityClass
public class CloneUtils {
  public static <T> T cloneObject(final T obj) {
    try {
      final T clone = (T) obj.getClass().getMethod("clone").invoke(obj);
      return clone;
    } catch (final Exception e) {
      throw new RuntimeException("Error cloning object", e);
    }
  }
}
