package pl.smarthouse.smartmonitoring.service;

import java.lang.reflect.Field;
import java.util.ArrayList;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.junit.jupiter.api.Test;
import pl.smarthouse.sharedobjects.dao.ModuleDao;
import utils.TestModuleDao;

class CompareProcessorTest {

  @Test
  void findingFields() {
    ModuleDao moduleDao = TestModuleDao.builder().moduleName("testModule").build();
    ArrayList<CompareField> primitiveFields = getPrimitiveFields(moduleDao);
    System.out.println(primitiveFields);
  }

  @AllArgsConstructor
  @Data
  public class CompareField {
    String name;
    Object field;
  }

  public ArrayList<CompareField> getPrimitiveFields(Object obj) {
    ArrayList<CompareField> results = new ArrayList<>();
    StringBuilder prefixName = new StringBuilder();
    for (Field field : obj.getClass().getDeclaredFields()) {
      field.setAccessible(true);

      try {
        Object fieldValue = field.get(obj);

        if (isPrimitive(field.getType())) {
          results.add(new CompareField(prefixName + field.getName(), fieldValue));
        } else if (fieldValue != null) {
          prefixName.append(field.getName());
          prefixName.append(".");
          results.addAll(getPrimitiveFields(fieldValue));
        }
      } catch (IllegalAccessException e) {
        // handle exception
      }
    }

    return results;
  }

  private boolean isPrimitive(Class<?> clazz) {
    return clazz.isPrimitive()
        || clazz == String.class
        || clazz == Integer.class
        || clazz == Boolean.class;
  }
}
