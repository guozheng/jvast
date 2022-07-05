package io.github.guozheng.jvast.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
/**
 * Enum representing TrackingEvent element in VAST XML.
 */
public class TrackingEvent {
  private TrackingEventElementType type;
  private String url;
}
