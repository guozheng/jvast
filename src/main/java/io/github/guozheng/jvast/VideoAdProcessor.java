package io.github.guozheng.jvast;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import io.github.guozheng.jvast.model.AdTypeVersion;
import io.github.guozheng.jvast.model.InputData;
import io.github.guozheng.jvast.model.PixelElementType;
import io.github.guozheng.jvast.model.TrackingEventElementType;
import io.github.guozheng.jvast.util.VideoAdUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Implements various logic such as inserting pixels or tracking events for video ad response.
 */
public class VideoAdProcessor {
  private static final Logger LOGGER = LogManager.getLogger(VideoAdProcessor.class);

  /**
   * Process video ad from a {@link StringBuilder} input.
   *
   * @param videoAdBuilder    {@link StringBuilder} video ad input from string builder
   * @param inputData         {@link InputData} input data for processing
   * @return                  {@link StringBuilder} video ad output
   */
  public static StringBuilder process(StringBuilder videoAdBuilder, InputData inputData) {
    final AdTypeVersion adTypeVersion = VideoAdUtil.getVideoAdType(videoAdBuilder);
    if (adTypeVersion.equals(AdTypeVersion.UNKNOWN)) {
      LOGGER.error("Unsupported video ad type");
      return videoAdBuilder; // no change
    }

    //based on video ad type and version, call a matching ad processor
    //TODO: support VAST 4.x
    //TODO: add a VMAP processor
    if (adTypeVersion.getType().equals("VAST")) {
      return VastProcessor.process(videoAdBuilder, adTypeVersion, inputData);
    } else {
      LOGGER.error("Unsupported video ad type or version: {}", adTypeVersion);
      return videoAdBuilder; // no change
    }

  }

  /**
   * Process video ad from a {@link String} input.
   *
   * @param videoAd         {@link String} video ad input from string
   * @param inputData       {@link InputData} input data for processing
   * @return                {@link String} video ad output
   */
  public static String process(String videoAd, InputData inputData) {
    return process(new StringBuilder(videoAd), inputData).toString();
  }

}
