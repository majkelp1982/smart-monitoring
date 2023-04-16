package pl.smarthouse.smartmonitoring.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public class PrimitiveField implements Cloneable {
  private final Object field;

  @Override
  public Object clone() throws CloneNotSupportedException {
    return super.clone();
  }
}
