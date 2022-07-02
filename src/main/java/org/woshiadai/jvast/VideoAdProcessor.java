package org.woshiadai.jvast;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import org.woshiadai.jvast.model.AdTypeVersion;
import org.woshiadai.jvast.model.InputData;
import org.woshiadai.jvast.model.PixelElementType;
import org.woshiadai.jvast.model.TrackingEventElementType;
import org.woshiadai.jvast.util.VideoAdUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Implements various logic such as inserting pixels or tracking events for video ad response.
 */
public class VideoAdProcessor {
  private static final Logger LOGGER = LogManager.getLogger(VideoAdProcessor.class);

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

  public static String process(String videoAd, InputData inputData) {
    return process(new StringBuilder(videoAd), inputData).toString();
  }

  public static void main(String[] args) {
    String videoAd = readFile("src/test/resources/pixel/vast_inline.xml");
    LOGGER.info("Video ad before processing: {}", videoAd);

    Multimap<PixelElementType, String> pixelMap = ArrayListMultimap.create();
    pixelMap.put(PixelElementType.Impression, "https://adclick.com/impression");

    Multimap<TrackingEventElementType, String> trackingEventMap = ArrayListMultimap.create();
    trackingEventMap.put(TrackingEventElementType.start, "https://adclick.com/start");

    InputData inputData = InputData.builder()
        .pixelMap(pixelMap)
        .trackingEventMap(trackingEventMap)
        .build();

    videoAd = process(videoAd, inputData);
    LOGGER.info("Video ad after processing: {}", videoAd);
  }

  public static String readFile(String filePath) {
    StringBuilder sb = new StringBuilder();
    try (BufferedReader br = new BufferedReader(
        new InputStreamReader(
            new FileInputStream(filePath), StandardCharsets.UTF_8))) {
      String line;
      while ((line = br.readLine()) != null) {
        sb.append(line).append(System.lineSeparator());
      }
    } catch (FileNotFoundException e) {
      LOGGER.error("File not found: {}", filePath);
    } catch (IOException e) {
      LOGGER.error("Error reading from file: {}", filePath, e);
    } finally {
      String fileContent = sb.toString().trim();
      return fileContent;
    }
  }

}
