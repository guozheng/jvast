package jvast.data;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TrackingEvent {
  private TrackingEventElementType type;
  private String url;
}
