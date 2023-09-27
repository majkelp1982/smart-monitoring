package pl.smarthouse.smartmonitoring.model;

import java.time.LocalDateTime;
import java.util.function.Consumer;
import java.util.function.Predicate;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import pl.smarthouse.sharedobjects.dao.ModuleDao;

@Getter
@Setter
@RequiredArgsConstructor
@EqualsAndHashCode
public class ErrorPrediction implements Cloneable {
  private boolean enable;
  private final Predicate<ModuleDao> predicate;
  private final String message;
  private LocalDateTime beginTimestamp;
  private LocalDateTime endTimestamp;
  private boolean active;
  private boolean pendingAcknowledge;
  private final Consumer<Boolean> stateChangedListener;

  @Override
  public Object clone() throws CloneNotSupportedException {
    return super.clone();
  }
}
