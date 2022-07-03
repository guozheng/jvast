package io.github.guozheng.jvast.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TrackingEvent {
  private TrackingEventElementType type;
  private String url;
}
