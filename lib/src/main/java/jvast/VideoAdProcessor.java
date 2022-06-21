package jvast;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import jvast.model.AdTypeVersion;
import jvast.model.InputData;
import jvast.model.PixelElementType;
import jvast.model.TrackingEventElementType;
import jvast.util.VideoAdUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Implements various logic such as inserting pixels or tracking events for video ad response.
 */
public class VideoAdProcessor {
  private static final Logger LOGGER = LogManager.getLogger(VideoAdProcessor.class);

  public static StringBuilder process(StringBuilder videoAdBuilder) {
    final AdTypeVersion adTypeVersion = VideoAdUtil.getVideoAdType(videoAdBuilder);
    if (adTypeVersion.equals(AdTypeVersion.UNKNOWN)) {
      LOGGER.error("Unsupported video ad type");
      return videoAdBuilder;
    }

    //TODO: input data should be generated from configuration
    Multimap<PixelElementType, String> pixelMap = ArrayListMultimap.create();
    pixelMap.put(PixelElementType.Impression, "https://adclick.com/impression");

    Multimap<TrackingEventElementType, String> trackingEventMap = ArrayListMultimap.create();
    trackingEventMap.put(TrackingEventElementType.start, "https://adclick.com/start");

    InputData inputData = InputData.builder()
        .pixelMap(pixelMap)
        .trackingEventMap(trackingEventMap)
        .build();

    if (adTypeVersion.getType().equals("VAST")) {
      VastProcessor.process(videoAdBuilder, adTypeVersion, inputData);
    } else {
      LOGGER.error("Unsupported video ad type or version: {}", adTypeVersion);
    }

    return videoAdBuilder;
  }

  public static String process(String videoAd) {
    return process(new StringBuilder(videoAd)).toString();
  }


}
