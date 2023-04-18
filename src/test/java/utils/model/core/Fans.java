package utils.model.core;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class Fans {
  private final Fan inlet;
  private final Fan outlet;
}
