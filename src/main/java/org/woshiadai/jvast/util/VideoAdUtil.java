package org.woshiadai.jvast.util;

import com.google.common.base.Strings;
import com.google.common.collect.Multimap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import org.woshiadai.jvast.model.Ad;
import org.woshiadai.jvast.model.AdTypeVersion;
import org.woshiadai.jvast.model.Pair;
import org.woshiadai.jvast.model.PixelElementType;
import org.woshiadai.jvast.model.TrackingEventElementType;
import lombok.experimental.UtilityClass;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@UtilityClass
public class VideoAdUtil {
  private static final Logger LOGGER = LogManager.getLogger(VideoAdUtil.class);

  public static final String VAST_VERSION_2_0 = "2.0";
  public static final String VAST_VERSION_3_0 = "3.0";

  public static final String EMPTY_VAST_V2 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
      + "<VAST xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:noNamespaceSchemaLocation=\"vast.xsd\" version=\"2.0\"/>";
  public static final String EMPTY_VAST_V2_2 = "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\n"
      + "<VAST xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:noNamespaceSchemaLocation=\"vast.xsd\" version=\"2.0\"/>";
  public static final String EMPTY_VAST_V2_3 = "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\n"
      + "<VAST version=\"2.0\"></VAST>";

  public static final String EMPTY_VAST_V3 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
      + "<VAST xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:noNamespaceSchemaLocation=\"vast.xsd\" version=\"3.0\"/>";
  public static final String EMPTY_VAST_V3_2 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
      + "<VAST xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:noNamespaceSchemaLocation=\"vast.xsd\" version=\"3.0\"/>\n";

  // regex
  // extract VASTAdTagURI from a VAST Wrapper doc
  public static final Pattern VAST_AD_TAG_URI_PATTERN =
      Pattern.compile("<VASTAdTagURI>.*<!\\[CDATA\\[(.*?)\\]\\]>.*</VASTAdTagURI>", Pattern.DOTALL);
  // extract ad id, assuming ad ids are integers
  public static final Pattern VAST_AD_ID_PATTERN =
      Pattern.compile("<Ad[^>]* id=\"(\\d*)\"[^>]*>");
  public static final Pattern VAST_AD_SEQUENCE_PATTERN =
      Pattern.compile("<Ad[^>]* sequence=\"(\\d*)\"[^>]*>");
  // extract creative ids
  public static final Pattern VAST_CREATIVE_ID_PATTERN =
      Pattern.compile("<Creative[^>]* id=\"(\\d*)\"[^>]*>");

  public static final String EMPTY_WRAPPER_REDIRECT_URL = StringUtil.EMPTY;

  // ad response types
  public static final String TYPE_INLINE_VAST = "INLINE_VAST";
  public static final String TYPE_WRAPPER_VAST = "WRAPPER_VAST";
  public static final String TYPE_V2_EMPTY_VAST = "V2_EMPTY_VAST";
  public static final String TYPE_V3_EMPTY_VAST = "V3_EMPTY_VAST";
  public static final String TYPE_VMAP = "VMAP";
  public static final String TYPE_OPENRTB_JSON = "OPENRTB_JSON";
  public static final String TYPE_UNKNOWN = "UNKNOWN";

  public static final long DEFAULT_AD_ID = -1L;
  public static final long DEFAULT_CREATIVE_ID = -1L;
  public static final String DEFAULT_AD_ID_STR = "-1";
  public static final String DEFAULT_CREATIVE_ID_STR = "-1";
  public static final char ELEMENT_END = '>';


  //prefix and postfix to construct VAST doc
  public static final String VAST_V2_PREFIX = "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\n"
      + "<VAST version=\"2.0\">\n";
  public static final String VAST_V3_PREFIX = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
      + "<VAST version=\"3.0\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:noNamespaceSchemaLocation=\"vast.xsd\">\n";
  public static final String VAST_V4_0_PREFIX = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
      + "<VAST version=\"4.0\" xmlns:xs=\"http://www.w3.org/2001/XMLSchema\" xmlns=\"http://www.iab.com/VAST\">";
  public static final String VAST_V4_1_PREFIX = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
      + "<VAST version=\"4.1\" xmlns:xs=\"http://www.w3.org/2001/XMLSchema\" xmlns=\"http://www.iab.com/VAST\">";
  public static final String VAST_V4_2_PREFIX = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
      + "<VAST version=\"4.2\" xmlns:xs=\"http://www.w3.org/2001/XMLSchema\" xmlns=\"http://www.iab.com/VAST\">";

  public static final String VAST_DEFAULT_PREFIX = VAST_V3_PREFIX;
  public static final String VAST_POSTFIX = "\n</VAST>";

  private static final String AD_OPENING_TAG = "<Ad";
  private static final String AD_OPEN_TAG_END = ">";
  private static final String AD_CLOSING_TAG = "</Ad>";
  private static final String AD_ID_OPEN_STR = "id=\"";
  private static final String AD_ID_CLOSE_STR = "\"";

  private static final String CREATIVES_OPEN_TAG = "<Creatives";
  private static final String CREATIVE_OPEN_TAG_START = "<Creative";
  private static final String CREATIVE_OPEN_TAG_END = ">";
  private static final String CREATIVE_ID_OPEN_STR = "id=\"";
  private static final String CREATIVE_ID_CLOSE_STR = "\"";
  public static final String CREATIVE_CLOSE_TAG = "</Creative>";

  public static final int INVALID_DURATION_SEC = 0;
  public static final String UNKNOWN_CREATIVE_ID = "unknown";

  private static final String AD_ELEMENT = "//Ad";
  private static final String SEQUENCE = "sequence";

  private static final String HB_PRICE_MACRO = "hb_pr=%24%7BAUCTION_PRICE%7D";

  public static final String VAST_WRAPPER_ELEMENT = "<Wrapper>";
  public static final String VAST_INLINE_ELEMENT = "<InLine>";

  public static final String OPEN_VAST_AD_TAG_URI = "<VASTAdTagURI>";
  public static final String CLOSE_VAST_AD_TAG_URI = "</VASTAdTagURI>";

  public static final String VAST_ERROR_END = "</Error>";
  // note there is no > because Impression might have attributes
  public static final String VAST_IMPRESSION = "<Impression";
  public static final String VAST_IMPRESSION_END = "</Impression>";
  public static final String VAST_CREATIVES = "<Creatives";

  public static final String VAST_TRACKING_EVENT = "<TrackingEvent>";
  public static final String VAST_TRACKING_EVENTS = "<TrackingEvents>";
  public static final String VAST_TRACKING_EVENTS_END = "</TrackingEvents>";

  // note there is no closing > because there might be attributes
  public static final String VAST_LINEAR_ELEMENT_WITH_ATTRS = "<Linear ";
  public static final String VAST_LINEAR_ELEMENT_WITHOUT_ATTRS = "<Linear>";
  public static final String VAST_LINEAR_ELEMENT_END = "</Linear>";
  public static final String VAST_DURATION_ELEMENT_END = "</Duration>";
  public static final String VAST_VIDEO_CLICKS_ELEMENT = "<VideoClicks>";

  // note there is no closing > because there might be attributes
  public static final String VAST_NON_LINEAR_ADS_WITH_ATTRS = "<NonLinearAds ";
  public static final String VAST_NON_LINEAR_ADS_WITHOUT_ATTRS = "<NonLinearAds>";
  public static final String VAST_NON_LINEAR_ADS_END = "</NonLinearAds>";
  // note there is no closing > because it has required attributes
  public static final String VAST_NON_LINEAR = "<NonLinear ";

  // note there is no closing > because there might be attributes
  // Companion element always has some required attributes
  public static final String VAST_COMPANION_ELEMENT_WITH_ATTRS = "<Companion ";
  public static final String VAST_COMPANION_ELEMENT_END = "</Companion>";
  public static final String VAST_STATIC_RESOURCE_END = "</StaticResource>";
  public static final String VAST_IFRAME_RESOURCE_END = "</IFrameResource>";
  public static final String VAST_HTML_RESOURCE_END = "</HTMLResource>";
  public static final String VAST_CREATIVE_EXTENSIONS_END = "</CreativeExtensions>";

  // v2 pixel generation
  public static final String V2_PIXEL_PATH_PREFIX = "/pixel/v2?";
  public static final String V2_PIXEL_EV_TYPE = "evType=";
  public static final String V2_PIXEL_ERROR_MACRO = "&err_code=[ERRORCODE]";

  // default versions
  public static final String DEFAULT_VAST_VERSION = "2.0";
  public static final String DEFAULT_VMAP_VERSION = "1.0";

  // v1 pixel prefix
  public static final String V1_PIXEL_PREFIX = "https://pixel.com/pixel/v1?";
  // v2 pixel prefix
  public static final String V2_PIXEL_PREFIX = "https://pixel.com/pixel/v2?";

  public static Pair<String, Integer> CREATIVE_ID_NOT_FOUND = new Pair<>(UNKNOWN_CREATIVE_ID, -1);

  /**
   * Check if a vast doc is an InLine vast or not.
   *
   * @param adXml     {@code String} ad xml
   * @return          {@code boolean} true if ad xml is an InLine vast, false otherwise
   */
  public static boolean isInline(StringBuilder adXml) {
    return adXml.indexOf(VAST_INLINE_ELEMENT) != -1;
  }

  /**
   * Check if a vast doc is a Wrapper vast or not.
   *
   * @param adXml     {@code String} ad xml
   * @return          {@code boolean} true if ad xml is a Wrapper vast, false otherwise
   */
  public static boolean isWrapper(StringBuilder adXml) {
    return adXml.indexOf(VAST_WRAPPER_ELEMENT) != -1;
  }


  /**
   * Check if a doc is a vmap doc.
   *
   * @param doc   {@code String} doc fetched from ad server
   * @return      {@code boolean} true if doc is a vmap doc, false otherwise
   */
  public static boolean isVmap(String doc) {
    return doc.contains("vmap:VMAP");
  }

  /**
   * Check if a doc is a json doc.
   *
   * @param doc   {@code String} doc fetched from ad server
   * @return      {@code boolean} true if doc is a json doc, false otherwise
   */
  public static boolean isJson(String doc) {
    //TODO: there are other better ways...
    return doc.charAt(0) == '{' && doc.charAt(doc.length() - 1) == '}';
  }

  /**
   * Check if a vast doc is empty vast or not.
   *
   * @param vastDoc   {@code String} vast doc
   * @return          {@code boolean} true if vast doc is an empty vast, false otherwise
   */
  public static boolean isEmptyVast(String vastDoc) {
    return isEmptyVastV3(vastDoc) || isEmptyVastV2(vastDoc);
  }


  /**
   * Check if a vast doc is v2 empty vast.
   *
   * @param vastDoc   {@code String} vast doc
   * @return          {@code boolean} true if it is a v3 empty vast, false otherwise.
   */
  public static boolean isEmptyVastV2(String vastDoc) {
    return vastDoc.equals(EMPTY_VAST_V2) || vastDoc.equals(EMPTY_VAST_V2_2) || vastDoc.equals(EMPTY_VAST_V2_3);
  }

  /**
   * Check if a vast doc is v3 empty vast.
   *
   * @param vastDoc   {@code String} vast doc
   * @return          {@code boolean} true if it is a v3 empty vast, false otherwise.
   */
  public static boolean isEmptyVastV3(String vastDoc) {
    return vastDoc.equals(EMPTY_VAST_V3) || vastDoc.equals(EMPTY_VAST_V3_2);
  }


  /**
   * Get VAST version.
   *
   * @param doc   {@code String} vast doc content
   * @return      {@code String} vast version
   */
  public static String getVastVersion(String doc) {
    String vastTag = StringUtil.getSubString("<VAST", ">", doc, 0, DEFAULT_VAST_VERSION);
    return StringUtil.getSubString("version=\"", "\"", vastTag, 0, DEFAULT_VAST_VERSION);
  }

  /**
   * Get VAST version.
   *
   * @param docBuilder   {@code String} vast doc content string builder
   * @return             {@code String} vast version
   */
  public static String getVastVersion(StringBuilder docBuilder) {
    String vastTag = StringUtil.getSubString("<VAST", ">", docBuilder, 0, DEFAULT_VAST_VERSION);
    return StringUtil.getSubString("version=\"", "\"", vastTag, 0, DEFAULT_VAST_VERSION);
  }

  /**
   * Get video ad type and version.
   *
   * @param adDoc   {@code StringBuilder} ad XML element string builder
   * @return        {@link AdTypeVersion} ad type and version
   */
  public static AdTypeVersion getVideoAdType(StringBuilder adDoc) {
    LOGGER.debug("video ad: {}", adDoc);
    String tag = StringUtil.getSubString("<VAST", ">", adDoc, 0, StringUtil.EMPTY);

    if (!tag.equals(StringUtil.EMPTY)) {
      //VAST ad
      String version = StringUtil.getSubString("version=\"", "\"", tag, 0, DEFAULT_VAST_VERSION);
      LOGGER.debug("type: VAST, version: {}", version);
      return AdTypeVersion.fromTypeAndVersion("VAST", version);
    } else {
      //NOT VAST ad, try VMAP
      tag = StringUtil.getSubString("<vmap:VMAP", ">", adDoc, 0, StringUtil.EMPTY);
      if (!tag.equals(StringUtil.EMPTY)) {
        String version = StringUtil.getSubString("version=\"", "\"", tag, 0, DEFAULT_VMAP_VERSION);
        LOGGER.debug("type: VMAP, version: {}", version);
        return AdTypeVersion.fromTypeAndVersion("VMAP", version);
      } else {
        LOGGER.debug("video ad is neither a VAST nor VMAP");
        return AdTypeVersion.UNKNOWN;
      }
    }
  }


  /**
   * Get the first ad id in the ad response doc using index.
   *
   * @param vastDocBuilder    {@code StringBuilder} vast doc string builder
   * @return                  {@code String} ad id
   */
  public static String getAdId(StringBuilder vastDocBuilder) {
    final int adOpenIndex = vastDocBuilder.indexOf(AD_OPENING_TAG);
    if (adOpenIndex == -1) {
      LOGGER.debug("ad response does not contain <Ad> element, use default ad id: {}", DEFAULT_AD_ID_STR);
      return DEFAULT_AD_ID_STR;
    }
    final int adCloseIndex = vastDocBuilder.indexOf(AD_OPEN_TAG_END, adOpenIndex); // find the element closing

    final int idOpenIndex = vastDocBuilder.indexOf(AD_ID_OPEN_STR, adOpenIndex + AD_OPENING_TAG.length());

    if (idOpenIndex > adCloseIndex || idOpenIndex == -1) {
      //<Ad> element has no id attribute, or the entire Ad xml string has no id="..."
      LOGGER.debug("ad response does not have id attribute, use default ad id: {}", DEFAULT_AD_ID_STR);
      return DEFAULT_AD_ID_STR;
    }

    final int closeIndex = vastDocBuilder.indexOf(AD_ID_CLOSE_STR, idOpenIndex + AD_ID_OPEN_STR.length());
    return vastDocBuilder.substring(idOpenIndex + AD_ID_OPEN_STR.length(), closeIndex);
  }


  /**
   * Find all creative ids from the ad response doc using index.
   * Note that it is not guaranteed that Creative element would contain an id attribute, in that case we use
   * the default 'unknown' as creative id.
   *
   * @param vastDocBuilder      {@link StringBuilder} vast doc string builder
   * @return                    {@code List<Long>} set of creative ids
   */
  public static List<String> getCreativeIds(StringBuilder vastDocBuilder) {
    // try to find <Creatives> element
    int creativesOpenIndex = vastDocBuilder.indexOf(CREATIVES_OPEN_TAG);
    if (creativesOpenIndex == -1) {
      LOGGER.debug("VAST doc does not contain any creative id, i.e. no element found for: {}",
          CREATIVES_OPEN_TAG);
      return Collections.EMPTY_LIST;
    }

    List<String> creativeIds = new LinkedList<>();
    int start = creativesOpenIndex + CREATIVES_OPEN_TAG.length();

    // loop through each <Creative> element inside <Creatives>
    while (start < vastDocBuilder.length()) {
      int creativeOpenIndex = vastDocBuilder.indexOf(CREATIVE_OPEN_TAG_START, start);
      if (creativeOpenIndex == -1) {
        LOGGER.trace("no more {}, start={}", CREATIVE_OPEN_TAG_START, start);
        break;
      }
      int creativeCloseIndex = vastDocBuilder.indexOf(CREATIVE_OPEN_TAG_END, creativeOpenIndex);
      if (creativeCloseIndex == -1) {
        LOGGER.debug("cannot find more closing tag > for {} element, start={}, creativeOpenIndex={}",
            CREATIVE_OPEN_TAG_START, start, creativeOpenIndex);
        break;
      }
      String creativeTag = vastDocBuilder.substring(creativeOpenIndex, creativeCloseIndex);
      int idOpenIndex = creativeTag.indexOf(CREATIVE_ID_OPEN_STR);
      String creativeIdStr = null;
      if (idOpenIndex == -1) {
        // if the current creative element does not contain id attribute, try next creative element
        LOGGER.debug("<Creative> element does not contain creative id, set to default: {}", UNKNOWN_CREATIVE_ID);
        creativeIdStr = UNKNOWN_CREATIVE_ID;
      } else {
        int idCloseIndex = creativeTag.indexOf(CREATIVE_ID_CLOSE_STR,
            idOpenIndex + CREATIVE_ID_OPEN_STR.length());
        creativeIdStr = creativeTag.substring(idOpenIndex + CREATIVE_ID_OPEN_STR.length(), idCloseIndex);
        LOGGER.debug("Found creative id from doc: {}", creativeIdStr);
      }
      creativeIds.add(creativeIdStr);
      start = creativeCloseIndex;
    }

    return creativeIds;
  }

  /**
   * Get ad durations from the ad response.
   * For wrapper vast, duration is provided in wrapper_info vast extension.
   * For inline vast, durations are provided in the embedded creatives.
   *
   * @param doc       {@code StringBuilder} ad response or substring, e.g. Ad element of ad response.
   * @return          {@code List<Integer>} ad durations
   */
  public static List<Integer> getAdDuration(StringBuilder doc) {
    List<Integer> durationSecs = new LinkedList<>();

    if (isWrapper(doc) || isInline(doc)) {
      // wrapper or inline vast, logic is the same: look for <Duration>hh:mm:ss</Duration> blocks
      // wrapper has only one <Duration> element, inline could have multiple
      List<String> subs = StringUtil.getSubStrings("<Duration>", "</Duration>", doc);
      for (String sub : subs) {
        LOGGER.debug("Got <Duration> element value: {}, converting it to seconds", sub);
        int duration = durationToSecond(sub);
        durationSecs.add(duration);
      }

    } else {
      LOGGER.debug("Cannot find valid duration for ad response, unsupported type");
      durationSecs.add(INVALID_DURATION_SEC);
    }

    return durationSecs;
  }


  /**
   * Parse duration string in the format of HH:mm:ss into seconds.
   *
   * @param duration      {@code String} duration string
   * @return              {@code int} duration in seconds
   */
  public static int durationToSecond(String duration) {
    int durationSec = INVALID_DURATION_SEC;
    String[] items = duration.split(":");
    if (items.length != 3) {
      LOGGER.debug("Invalid Duration value: {}", duration);
      return INVALID_DURATION_SEC;
    } else {
      try {
        int hour = Integer.parseInt(items[0]);
        durationSec += 3600 * hour;
      } catch (NumberFormatException ex) {
        LOGGER.debug("Invalid hour value: {}, set to duration to {}: {}", items[0], INVALID_DURATION_SEC, ex);
        return INVALID_DURATION_SEC;
      }

      try {
        int minute = Integer.parseInt(items[1]);
        durationSec += 60 * minute;
      } catch (NumberFormatException ex) {
        LOGGER.debug("Invalid minute value: {}, set to duration to {}: {}", items[1], INVALID_DURATION_SEC, ex);
        return INVALID_DURATION_SEC;
      }

      try {
        int second = Integer.parseInt(items[2]);
        durationSec += second;
      } catch (NumberFormatException ex) {
        LOGGER.debug("Invalid second value: {}, set to duration to {}: {}", items[2], INVALID_DURATION_SEC, ex);
        return INVALID_DURATION_SEC;
      }
    }

    return durationSec;
  }

  /**
   * Parse a vast doc into a list of {@link Ad}.
   *
   * @param vastDoc           {@code String} vast doc from the given demand source
   * @return                  {@code List<Ad>} a list of {@link Ad}
   */
  public static List<Ad> splitVastDoc(String vastDoc) {
    return splitVastDoc(new StringBuilder(vastDoc));
  }

  public static List<Ad> splitVastDoc(StringBuilder sb) {
    List<Ad> ads = new LinkedList<>();

    int startIndex = sb.indexOf(AD_OPENING_TAG);

    StringBuilder adElement; // each Ad xml element including open and close XML tags
    String adId;
    List<String> creativeIds;

    while (startIndex < sb.length() && startIndex != -1) {
      int adClosingIndex = sb.indexOf(AD_CLOSING_TAG, startIndex);
      if (adClosingIndex == -1) {
        LOGGER.debug("vast doc has no more ad element, done parsing vast doc");
        break;
      }

      // process each <Ad> element, convert each <Ad> element into one AdData object
      adElement = new StringBuilder(sb.substring(startIndex, adClosingIndex + AD_CLOSING_TAG.length()));
      // update start index to get the next Ad element
      startIndex = sb.indexOf(AD_OPENING_TAG, adClosingIndex + AD_CLOSING_TAG.length());

      adId = getAdId(adElement);
      creativeIds = getCreativeIds(adElement);

      Ad ad = Ad.builder()
          .content(adElement)
          .adId(adId)
          .creativeIds(creativeIds)
          .build();

      LOGGER.trace("parsed an ad item in response: {}", ad);
      ads.add(ad);
    }

    return ads;
  }


  /**
   * Get a matching vast prefix given the version.
   *
   * @param version       {@code String} version string, e.g. 2.0. 3.0, etc.
   * @return              {@code String} vast prefix matching the version string
   */
  public static String getVastPrefix(String version) {
    String vastPrefix;
    switch (version) {
      case "2.0":
        vastPrefix = VAST_V2_PREFIX;
        break;
      case "3.0":
        vastPrefix = VAST_V3_PREFIX;
        break;
      case "4.0":
        vastPrefix = VAST_V4_0_PREFIX;
        break;
      case "4.1":
        vastPrefix = VAST_V4_1_PREFIX;
        break;
      case "4.2":
        vastPrefix = VAST_V4_2_PREFIX;
        break;
      default:
        vastPrefix = VAST_DEFAULT_PREFIX;
    }
    return vastPrefix;
  }

  /**
   * Given a set of {@link PixelElementType}, generate a {@link StringBuilder} that contains XML content
   * for these pixels. The generated XML elements are for Ad level pixel types only, e.g. Impression,
   * Error elements.
   * @param pixelMap          {@code Set<PixelElementType>} Ad XML element level pixel types
   * @return                  {@link StringBuilder} string builder for the generated pixel XML content
   */
  public static StringBuilder buildPixelElements(Multimap<PixelElementType, String> pixelMap) {
    StringBuilder sb = new StringBuilder(System.lineSeparator());

    if (pixelMap == null || pixelMap.isEmpty()) {
      LOGGER.debug("Input pixel map is null or empty, skip building Ad level pixel");
      return sb;
    }

    // insert Error pixel first based on VAST spec
    if (pixelMap.containsKey(PixelElementType.Error)) {
      appendPixelElementsOfType(sb, PixelElementType.Error, pixelMap.get(PixelElementType.Error));
    }

    // insert other pixels of the pixel element type, e.g. Impression
    for (PixelElementType pixelType: pixelMap.keys()) {
      if (pixelType != PixelElementType.Error) {
        appendPixelElementsOfType(sb, pixelType, pixelMap.get(pixelType));
      }
    }

    if (LOGGER.isDebugEnabled()) {
      LOGGER.debug("Built Ad level pixels: {}", sb.toString());
    }
    return sb;
  }

  /**
   * Generate XML element for one Ad level pixel type, Impression or Error element.
   * For example: <pre>{@code <Impression><![CDATA[pixel_url]]></Impression>}</pre>
   * @param sb                  {@link StringBuilder} xml content string builder
   * @param pixelType           {@link PixelElementType} pixel type
   * @param pixelUrls           {@link Collection<String>} pixel urls
   * @return                    {@link StringBuilder} string builder for pixel element
   */
  public static StringBuilder appendPixelElementsOfType(StringBuilder sb, PixelElementType pixelType,
      Collection<String> pixelUrls) {
    if (pixelType == null || pixelType == PixelElementType.unknown) {
      LOGGER.debug("Input pixel type is null or unknown, cannot build pixel for pixel type: {}",
          pixelType);
      return sb;
    }

    if (pixelUrls == null || pixelUrls.isEmpty()) {
      LOGGER.debug("Null or empty pixel urls, cannot build pixel for pixel type: {}",
          pixelType);
      return sb;
    }

    for (String pixelUrl : pixelUrls) {
      // generate and append pixel url based on template pixel url
      appendPixelElementOfType(sb, pixelType, pixelUrl);
    }

    LOGGER.trace("Built pixels for Ad level pixel type: {} using template pixel url: {}",
        pixelType, pixelUrls);

    return sb;
  }

  /**
   * Generate v2 pixel url and append to the {@link StringBuilder} object.
   * @param sb                  {@link StringBuilder} string builder object
   * @param pixelType           {@link PixelElementType} pixel type
   * @param pixelUrl            {@link String} pixel url
   * @return                    {@link StringBuilder} string builder with the generated pixel
   */
  private static StringBuilder appendPixelElementOfType(StringBuilder sb, PixelElementType pixelType,
      String pixelUrl) {
    // prefix for the pixel xml element
    sb.append("<")
        .append(pixelType.name())
        .append("><![CDATA[");

    // pixel url
    sb.append(pixelUrl);
    // extra error reporting macro for Error pixel
    if (pixelType == PixelElementType.Error) {
      sb.append(V2_PIXEL_ERROR_MACRO);
    }

    // postfix for the pixel xml element
    sb.append("]]></")
        .append(pixelType.name())
        .append(">")
        .append(System.lineSeparator());

    return sb;
  }

  /**
   * Get pixels matching the given url prefix.
   *
   * @param vast          {@link String} vast doc
   * @param prefix        {@link String} pixel url prefix, e.g. https://ramv.tv/pixel/v1
   * @return              {@code List<String>} a list of matching pixels
   */
  public static List<String> getPixels(String vast, String prefix) {
    List<String> pixels = new ArrayList<>();
    if (Strings.isNullOrEmpty(vast.trim())) {
      return pixels;
    }
    int start = vast.indexOf(prefix);
    final String endStr = "]]>";
    while (start != -1) {
      // a pixel url is inside <![CDATA[pixel_url]]>
      int end = vast.indexOf(endStr, start + prefix.length());
      pixels.add(vast.substring(start, end));
      start = vast.indexOf(prefix, end + endStr.length());
    }
    return pixels;
  }

  /**
   * Build tracking event elements.
   *
   * @param trackingEventMap    {@code Map<PixelTrackingEvent, String>} tracking event type and its pixel url
   * @return                    {@link StringBuilder} string builder for tracking events
   */
  public static StringBuilder buildTrackingEventElements(Multimap<TrackingEventElementType, String> trackingEventMap) {
    StringBuilder sb = new StringBuilder(System.lineSeparator());
    for (Map.Entry<TrackingEventElementType, String> entry : trackingEventMap.entries()) {
      appendTrackingElement(sb, entry.getKey(), entry.getValue());
    }
    return sb;
  }


  /**
   * Append one tracking event element.
   * @param sb          {@link StringBuilder} string builder for TrackingElement pixel
   * @param type        {@link TrackingEventElementType} pixel type
   * @param pixelUrl    {@link String} pixel url
   */
  private static StringBuilder appendTrackingElement(StringBuilder sb, TrackingEventElementType type, String pixelUrl) {
    sb.append("<Tracking event=\"")
        .append(type)
        .append("\"><![CDATA[")
        .append(pixelUrl)
        .append("]]></Tracking>")
        .append(System.lineSeparator());
    LOGGER.debug("Built TrackingElement: type={}, pixelUrl={}", type, pixelUrl);
    return sb;
  }

}
