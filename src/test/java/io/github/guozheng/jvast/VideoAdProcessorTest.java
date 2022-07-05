package io.github.guozheng.jvast;

import static io.github.guozheng.jvast.VideoAdProcessor.process;
import static io.github.guozheng.jvast.util.FileUtil.readFile;
import static io.github.guozheng.jvast.util.VideoAdUtil.VAST_VERSION_2_0;
import static io.github.guozheng.jvast.util.VideoAdUtil.VAST_VERSION_3_0;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import java.util.HashMap;
import java.util.List;
import io.github.guozheng.jvast.model.InputData;
import io.github.guozheng.jvast.model.PixelElementType;
import io.github.guozheng.jvast.model.TrackingEventElementType;
import io.github.guozheng.jvast.util.FileUtil;
import io.github.guozheng.jvast.util.VideoAdUtil;
import io.github.guozheng.jvast.vastparser.TrackingEventsType;
import io.github.guozheng.jvast.vastparser.VastModel;
import io.github.guozheng.jvast.vastparser.VastParser;
import io.github.guozheng.jvast.vastparser.VastParserErrorCode;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.util.Strings;
import org.junit.Test;

//@RunWith(JMockit.class)
public class VideoAdProcessorTest {
  private static final Logger LOGGER = LogManager.getLogger(VideoAdProcessorTest.class);

  // various parsers, v2, v3, with and without schema validation
  private static VastParser v2ParserWithoutValidation = new VastParser(VastParser.VAST_2_0_1_SCHEMA_JVAST, false);
  private static VastParser v2ParserWithValidation = new VastParser(VastParser.VAST_2_0_1_SCHEMA_JVAST, true);
  private static VastParser v3ParserWithoutValidation = new VastParser(VastParser.VAST_3_0_SCHEMA_JVAST, false);
  private static VastParser v3ParserWithValidation = new VastParser(VastParser.VAST_3_0_SCHEMA_JVAST, true);

  private VastParser validateVastFromFile(String path, boolean schemaValidation) {
    String vast = readFile(path);
    if (Strings.isBlank(vast)) {
      LOGGER.error("empty VAST from path: {}", path);
      fail("empty VAST from path: " + path);
    }

    String vastVersion = VideoAdUtil.getVastVersion(vast);
    LOGGER.info("VAST version: {}", vastVersion);

    VastParser parser = null;

    if (vastVersion.equals(VAST_VERSION_2_0)) {
      parser = schemaValidation ? v2ParserWithValidation : v2ParserWithoutValidation;
    } else if (vastVersion.equals(VAST_VERSION_3_0)) {
      parser = schemaValidation ? v3ParserWithValidation : v3ParserWithoutValidation;
    } else {
      LOGGER.error("Unknown VAST version: {}", vastVersion);
      fail("Unknown VAST version: " + vastVersion);
    }

    LOGGER.info("======== vastVersion: {}, schemaValidation: {}, verifying VAST from file: {} ========",
        vastVersion, schemaValidation, path);

    int code = parser.process(vast);
    LOGGER.info("VAST parser error code: {}, {} for vast: \n{}", code,
        VastParserErrorCode.getByVal(code), vast);
    assertEquals(
        "VAST parser should not give any error! Getting error: " + VastParserErrorCode.getByVal(
            code),
        VastParserErrorCode.ERROR_NONE.getValue(), code);

    return parser;
  }

  private VastParser validateVastFromFile(String path) {
    return validateVastFromFile(path, true);
  }

  @Test
  public void testVastPixelInsertion() {
    final String[] filePaths = {
        "src/test/resources/pixel/vast_2.0_inline_companion_ads.xml",
        "src/test/resources/pixel/vast_2.0_inline_nonlinear.xml",
        "src/test/resources/pixel/vast_2.0_inline_spotx.xml",
        "src/test/resources/pixel/vast_2.0_wrapper.xml",
        "src/test/resources/pixel/vast_3.0_inline_companion_ads.xml",
        "src/test/resources/pixel/vast_3.0_inline_dfp.xml",
        "src/test/resources/pixel/vast_3.0_inline_nonlinear.xml",
        "src/test/resources/pixel/vast_3.0_pods_dfp.xml",
        "src/test/resources/pixel/vast_3.0_wrapper_dfp.xml"
    };

    for (String filePath : filePaths) {
      LOGGER.info("testing pixel insertion for VAST: {}", filePath);

      //validate input file
      VastParser parser = validateVastFromFile(filePath);
      LOGGER.info("original VAST is valid");

      //insert pixels
      String videoAd = readFile(filePath);
      LOGGER.debug("Video ad before processing: {}", videoAd);

      Multimap<PixelElementType, String> pixelMap = ArrayListMultimap.create();
      final String impressionPixel = "https://adclick.com/impression";
      pixelMap.put(PixelElementType.Impression, impressionPixel);

      Multimap<TrackingEventElementType, String> trackingEventMap = ArrayListMultimap.create();
      final String startTrackingEvent = "https://adclick.com/start";
      trackingEventMap.put(TrackingEventElementType.start, startTrackingEvent);

      InputData inputData = InputData.builder()
          .pixelMap(pixelMap)
          .trackingEventMap(trackingEventMap)
          .build();

      videoAd = process(videoAd, inputData);
      LOGGER.info("Video ad after processing: {}", videoAd);

      //verify inserted pixels and tracking events
      int code = parser.process(videoAd);
      assertEquals(
          "VAST parser error code (0 means no error): " + VastParserErrorCode.getByVal(code)
              + "!!! ",
          VastParserErrorCode.ERROR_NONE.getValue(), code);
      VastModel model = parser.getModel();
      assertNotNull("VAST model should not be null", model);

      List<String> impressionPixels = model.getImpressionPixels();
      assertTrue(impressionPixels.contains(impressionPixel));

      HashMap<TrackingEventsType, List<String>> mappings = model.getTrackingPixels();
      List<String> startTrackingEvents = mappings.get(TrackingEventsType.start);
      assertTrue(startTrackingEvents.contains(startTrackingEvent));

      LOGGER.info("pixel insertion passed for VAST: {}", filePath);
    }
  }

}
