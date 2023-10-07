package pl.smarthouse.smartmonitoring.utils;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.HashMap;
import pl.smarthouse.smartmonitoring.model.PrimitiveField;

public class PrimitiveFieldFinder {

  public static HashMap<String, PrimitiveField> findPrimitiveFields(Class<?> clazz, Object object) {
    HashMap<String, PrimitiveField> primitiveFields = new HashMap<>();
    Field[] fields = clazz.getDeclaredFields();

    for (Field field : fields) {
      Class<?> fieldType = field.getType();
      String fieldName = field.getName();

      if (field.isAnnotationPresent(org.springframework.data.annotation.Transient.class)) {
        continue;
      }

      if (field.getType().equals(String.class) || field.getType().equals(LocalDateTime.class)) {
        continue;
      }

      if (fieldType.isPrimitive() || fieldType.isEnum()) {
        primitiveFields.put(fieldName, new PrimitiveField(getFieldObject(object, field)));
      } else if (isWrapperType(fieldType)) {
        primitiveFields.put(fieldName, new PrimitiveField(getFieldObject(object, field)));
      } else if (fieldType.isArray() && fieldType.getComponentType().isPrimitive()) {
        primitiveFields.put(fieldName, new PrimitiveField(getFieldObject(object, field)));
      } else if (!fieldType.isPrimitive() && !isWrapperType(fieldType) && !fieldType.isArray()) {
        Object fieldValue = getFieldObject(object, field);
        findPrimitiveFields(fieldValue.getClass(), fieldValue)
            .forEach(
                (key, primitiveField) ->
                    primitiveFields.put(fieldName + "." + key, primitiveField));
      }
    }
    return primitiveFields;
  }

  private static Object getFieldObject(Object object, Field field) {
    try {
      field.setAccessible(true);
      return field.get(object);
    } catch (IllegalAccessException e) {
      throw new RuntimeException("Error accessing field: " + field.getName(), e);
    }
  }

  private static boolean isWrapperType(Class<?> clazz) {
    return clazz.equals(Boolean.class)
        || clazz.equals(Integer.class)
        || clazz.equals(Long.class)
        || clazz.equals(Double.class)
        || clazz.equals(Float.class)
        || clazz.equals(Byte.class)
        || clazz.equals(Character.class)
        || clazz.equals(Short.class);
  }
}
