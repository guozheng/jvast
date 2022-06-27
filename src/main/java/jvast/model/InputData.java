package jvast.model;

import com.google.common.collect.Multimap;
import lombok.Builder;
import lombok.Data;

@Builder
@Data
/**
 * Data used for ad processing.
 */
public class InputData {
  // Ad level pixel insertion
  Multimap<PixelElementType, String> pixelMap;
  // creative level TrackingEvent insertion
  Multimap<TrackingEventElementType, String> trackingEventMap;
}
