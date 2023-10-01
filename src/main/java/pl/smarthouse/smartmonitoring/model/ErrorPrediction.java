package pl.smarthouse.smartmonitoring.model;

import java.time.LocalDateTime;
import java.util.function.Consumer;
import java.util.function.Predicate;
import lombok.*;
import pl.smarthouse.sharedobjects.dao.ModuleDao;

@Getter
@Setter
@EqualsAndHashCode
@RequiredArgsConstructor
public class ErrorPrediction implements Cloneable {
  private boolean enable;
  @NonNull private final Predicate<? extends ModuleDao> predicate;
  @NonNull private final String message;
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
