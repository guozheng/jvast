package jvast;

import static jvast.VideoAdProcessor.process;
import static jvast.VideoAdProcessor.readFile;
import static jvast.util.VideoAdUtil.VAST_VERSION_2_0;
import static jvast.util.VideoAdUtil.VAST_VERSION_3_0;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import java.util.HashMap;
import java.util.List;
import jvast.model.InputData;
import jvast.model.PixelElementType;
import jvast.model.TrackingEventElementType;
import jvast.util.FileUtil;
import jvast.util.VideoAdUtil;
import jvast.vastparser.TrackingEventsType;
import jvast.vastparser.VastModel;
import jvast.vastparser.VastParser;
import jvast.vastparser.VastParserErrorCode;
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

  private void validateVastFromFile(String path, boolean schemaValidation) {
    String vast = FileUtil.readFile(path);
    if (Strings.isBlank(vast)) {
      LOGGER.error("empty VAST from path: {}", path);
      fail("empty VAST from path: " + path);
    }

    String vastVersion = VideoAdUtil.getVastVersion(vast);

    VastParser parser = null;

    if (vastVersion.equals(VAST_VERSION_2_0)) {
      parser = schemaValidation ? v2ParserWithValidation : v2ParserWithoutValidation;
    } else if (vastVersion.equals(VAST_VERSION_3_0)) {
      parser = schemaValidation ? v3ParserWithValidation : v3ParserWithoutValidation;
    } else {
      LOGGER.error("Unknown VAST version: {}", vastVersion);
      fail("Unknown VAST version: " + vastVersion);
    }

    LOGGER.debug(
        "======== vastVersion: {}, schemaValidation: {}, verifying VAST from file: {} ========",
        vastVersion, schemaValidation, path);

    int code = parser.process(vast);
    LOGGER.debug("VAST parser error code: {}, {} for vast: \n{}", code,
        VastParserErrorCode.getByVal(code), vast);
    assertEquals(
        "VAST parser should not give any error! Getting error: " + VastParserErrorCode.getByVal(
            code),
        VastParserErrorCode.ERROR_NONE.getValue(), code);
  }

  private void validateVastFromFile(String path) {
    validateVastFromFile(path, true);
  }

  @Test
  public void testInlineVastV2() {
    final String filePath = "src/test/resources/pixel/vast_inline.xml";
    //validate input file
    validateVastFromFile(filePath);

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
    LOGGER.debug("Video ad after processing: {}", videoAd);

    //verify inserted pixels and tracking events
    int code = v2ParserWithoutValidation.process(videoAd);
    assertEquals("VAST parser error code (0 means no error): " + VastParserErrorCode.getByVal(code) + "!!! ",
        VastParserErrorCode.ERROR_NONE.getValue(), code);
    VastModel model = v2ParserWithoutValidation.getModel();
    assertNotNull("VAST model should not be null", model);

    VastModel vastModel = v2ParserWithoutValidation.getModel();
    List<String> impressionPixels = vastModel.getImpressionPixels();
    assertTrue(impressionPixels.contains(impressionPixel));

    HashMap<TrackingEventsType, List<String>> mappings = vastModel.getTrackingPixels();
    List<String> startTrackingEvents = mappings.get(TrackingEventsType.start);
    assertTrue(startTrackingEvents.contains(startTrackingEvent));
  }

}
