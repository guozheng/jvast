package jvast;

import static jvast.util.StringUtil.findNextNeedleIndex;
import static jvast.util.VideoAdUtil.VAST_COMPANION_ELEMENT_END;
import static jvast.util.VideoAdUtil.VAST_COMPANION_ELEMENT_WITH_ATTRS;
import static jvast.util.VideoAdUtil.VAST_CREATIVES;
import static jvast.util.VideoAdUtil.VAST_CREATIVE_EXTENSIONS_END;
import static jvast.util.VideoAdUtil.VAST_DURATION_ELEMENT_END;
import static jvast.util.VideoAdUtil.VAST_ERROR_END;
import static jvast.util.VideoAdUtil.VAST_HTML_RESOURCE_END;
import static jvast.util.VideoAdUtil.VAST_IFRAME_RESOURCE_END;
import static jvast.util.VideoAdUtil.VAST_IMPRESSION;
import static jvast.util.VideoAdUtil.VAST_LINEAR_ELEMENT_END;
import static jvast.util.VideoAdUtil.VAST_LINEAR_ELEMENT_WITHOUT_ATTRS;
import static jvast.util.VideoAdUtil.VAST_LINEAR_ELEMENT_WITH_ATTRS;
import static jvast.util.VideoAdUtil.VAST_NON_LINEAR;
import static jvast.util.VideoAdUtil.VAST_NON_LINEAR_ADS_END;
import static jvast.util.VideoAdUtil.VAST_NON_LINEAR_ADS_WITHOUT_ATTRS;
import static jvast.util.VideoAdUtil.VAST_NON_LINEAR_ADS_WITH_ATTRS;
import static jvast.util.VideoAdUtil.VAST_STATIC_RESOURCE_END;
import static jvast.util.VideoAdUtil.VAST_TRACKING_EVENT;
import static jvast.util.VideoAdUtil.VAST_TRACKING_EVENTS;
import static jvast.util.VideoAdUtil.VAST_TRACKING_EVENTS_END;
import static jvast.util.VideoAdUtil.VAST_VIDEO_CLICKS_ELEMENT;
import static jvast.util.VideoAdUtil.buildPixelElements;
import static jvast.util.VideoAdUtil.buildTrackingEventElements;
import static jvast.util.VideoAdUtil.isInline;
import static jvast.util.VideoAdUtil.isWrapper;

import com.google.common.collect.Multimap;
import java.util.HashSet;
import java.util.Set;
import jvast.model.Ad;
import jvast.model.Pair;
import jvast.model.PixelElementType;
import jvast.model.TrackingEventElementType;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.util.Strings;

public class VastSingleAdProcessor {
  private static Set<Pair<String, String>> TRACKING_EVENT_PARENTS = new HashSet<>(4);

  static {
    // opening parent elements, e.g. Linear, NonLinear, Companion
    TRACKING_EVENT_PARENTS.add(new Pair(VAST_LINEAR_ELEMENT_WITH_ATTRS, VAST_LINEAR_ELEMENT_END));
    TRACKING_EVENT_PARENTS.add(new Pair(VAST_LINEAR_ELEMENT_WITHOUT_ATTRS, VAST_LINEAR_ELEMENT_END));

    TRACKING_EVENT_PARENTS.add(new Pair(VAST_NON_LINEAR_ADS_WITH_ATTRS, VAST_NON_LINEAR_ADS_END));
    TRACKING_EVENT_PARENTS.add(new Pair(VAST_NON_LINEAR_ADS_WITHOUT_ATTRS, VAST_NON_LINEAR_ADS_END));

    TRACKING_EVENT_PARENTS.add(new Pair(VAST_COMPANION_ELEMENT_WITH_ATTRS, VAST_COMPANION_ELEMENT_END));
  }

  private static final Logger LOGGER = LogManager.getLogger(VastSingleAdProcessor.class);

  /**
   * Update ad data, e.g. insert pixels.
   * //TODO: move all the other vast processing logic here
   * @param ad               {@link jvast.model.Ad} ad data
   * @param pixelMap         {@code Multimap<PixelElementType, String>} a multimap of pixels to insert
   * @param trackingEventMap {@code Multimap<TrackingEventElementType, String>} a multimap of tracking events to insert
   * @return                 {@link jvast.model.Ad} updated ad data
   */
  public static Ad processSingleAd(Ad ad,
      Multimap<PixelElementType, String> pixelMap,
      Multimap<TrackingEventElementType, String> trackingEventMap) {
    StringBuilder adXml = ad.getContent();
    LOGGER.trace("Single ad before processing: {}", adXml);

    if (Strings.isEmpty(adXml)) {
      LOGGER.debug("Null or empty ad content from ad, skip ad processing");
      return ad; // do nothing
    }

    boolean isWrapperVast = isWrapper(adXml);
    boolean isInLineVast = isInline(adXml);
    if (!isWrapperVast && !isInLineVast) {
      LOGGER.debug("Invalid ad content from AdData, not Wrapper or InLine vast, skip ad processing");
      return ad; // do nothing
    }

    // insert pixels and tracking events
    insertPixelsAndTrackingEvents(adXml, isInLineVast, pixelMap, trackingEventMap);

    ad.setContent(adXml); // update adData
    LOGGER.trace("Single ad after processing: {}", adXml);

    return ad;
  }


  private static StringBuilder insertPixelsAndTrackingEvents(StringBuilder vastBuilder,
      boolean isInLineVast,
      Multimap<PixelElementType, String> pixelMap,
      Multimap<TrackingEventElementType, String> trackingEventMap) {

    ///////////////// insert <Ad> level pixels, e.g. Impression, Error ////////////////
    LOGGER.debug("Start inserting Ad level pixels into {} VAST...", isInLineVast ? "InLine" : "Wrapper");
    int idx = -1;
    // according to VAST 3.0 XML schema, <Error> must appear before <Impression>
    // although in real world, this is not enforced, e.g. we've seen VAST from demand source having
    // mixed order for Error and Impression
    idx = vastBuilder.lastIndexOf(VAST_ERROR_END);
    if (idx != -1) {
      // if <Error> pixels exist already, insert after the last <Error> pixel
      vastBuilder.insert(idx + VAST_ERROR_END.length(), buildPixelElements(pixelMap));
    } else {
      // if no existing <Error> pixels, insert before the first <Impression> pixel
      idx = vastBuilder.indexOf(VAST_IMPRESSION);
      if (idx != -1) {
        // search for existing <Impression element
        // note <Impression> element might have id attribute, so we use open element tag
        // if exists, then insert ad level v2 pixels before the first <Impression> element
        vastBuilder.insert(idx, buildPixelElements(pixelMap));
      } else {
        // if there is no existing <Impression> element, insert v2 pixels before <Creatives> element
        // according to schema, <Creatives> element is required, and it does not have any attribute
        idx = vastBuilder.indexOf(VAST_CREATIVES);
        vastBuilder.insert(idx, buildPixelElements(pixelMap));
      }
    }
    LOGGER.debug("==== Inserted Ad level pixels: {}", pixelMap);


    ///////////////// insert creative level TrackingEvent pixels, e.g. start, complete ////////////
    // different cases: Linear, NonLinear, Companion
    if (isInLineVast) {
      // InLine VAST
      LOGGER.debug("Start inserting Creative level Tracking Events for InLine VAST...");
      insertTrackingInLineVastLinearCreative(vastBuilder, idx, trackingEventMap);
      insertTrackingNonLinearCreative(vastBuilder, idx, trackingEventMap);
      insertTrackingCompanionCreative(vastBuilder, idx, trackingEventMap);
      LOGGER.debug("==== Inserted Creative level Tracking Events for InLine VAST: {}",
          trackingEventMap);
    } else {
      // Wrapper VAST
      LOGGER.debug("Start inserting Creative level Tracking Events for Wrapper VAST...");
      insertTrackingWrapperVastLinearCreative(vastBuilder, idx, trackingEventMap);
      insertTrackingNonLinearCreative(vastBuilder, idx, trackingEventMap);
      insertTrackingCompanionCreative(vastBuilder, idx, trackingEventMap);
      LOGGER.debug("==== Inserted Creative level Tracking Events for Wrapper VAST: {}",
          trackingEventMap);
    }

    LOGGER.debug("==== pixel insertion DONE ====");

    return vastBuilder;
  }



  /**
   * Insert Tracking pixels in each Linear creatives inside an InLine VAST.
   * @param vastBuilder         {@link StringBuilder} vast builder
   * @param idx                 {@code int} start index in the vast builder to process
   * @param trackingEventMap    {@code Multimap<TrackingEventElementType, String>} pixels to insert
   */
  private static void insertTrackingInLineVastLinearCreative(StringBuilder vastBuilder, int idx,
      Multimap<TrackingEventElementType, String> trackingEventMap) {
    LOGGER.debug("Inserting Tracking pixels for Linear Creatives in InLine VAST...");
    int start = idx;
    int count = 0;

    // there could be multiple Linear creatives
    while (start != -1) {
      LOGGER.trace("Start looking for Linear element #{} inside InLine VAST to insert pixels, start={}",
          count + 1, start);
      int pixelInsertionIdx = start;
      StringBuilder pixelsStringBuilder = null;

      // look for the next close element </Linear>
      final int linearEnd = vastBuilder.indexOf(VAST_LINEAR_ELEMENT_END, start);
      if (linearEnd == -1) {
        LOGGER.trace("Either no Linear creative or all the Linear creatives have been processed already");
        break;
      } else {
        LOGGER.trace("Found {} at index {}", VAST_LINEAR_ELEMENT_END, linearEnd);
      }

      // Linear element could be either <Linear> without attributes or <Linear ...> with attributes
      // we need to find out which case it is, check <Linear> first since it is more common
      int linearStart = vastBuilder.indexOf(VAST_LINEAR_ELEMENT_WITHOUT_ATTRS, start);
      if (linearStart == -1 || linearStart > linearEnd) {
        // cannot find <Linear> or we crossed end index to the next Linear creative
        // try <Linear with attributes as open element, this must exist since we have a close element
        linearStart = vastBuilder.indexOf(VAST_LINEAR_ELEMENT_WITH_ATTRS, start);
        if (linearStart == -1 || linearStart > linearEnd) {
          // this should not happen since we already found a close element </Linear>
          LOGGER.error("No open Linear element found to match the close Linear element, invalid InLine VAST?");
          break;
        } else {
          // move start index pass open element including the attributes and closing >
          linearStart = vastBuilder.indexOf(">", linearStart) + 1;
          LOGGER.trace("Linear element has attributes, start index: {}, end index: {}",
              linearStart, linearEnd);
        }
      } else {
        // Linear element has not attributes, move start index pass open element to the end
        linearStart += VAST_LINEAR_ELEMENT_WITHOUT_ATTRS.length();
        LOGGER.trace("Linear element has no attributes, start index: {}, end index: {}",
            linearStart, linearEnd);
      }

      // check if <TrackingEvents> exists already
      final int trackingEventsStart = vastBuilder.indexOf(VAST_TRACKING_EVENTS, linearStart);
      if (trackingEventsStart == -1 || trackingEventsStart > linearEnd) {
        // current Linear creative does not have TrackingEvents, insert <TrackingEvents> and
        // pixels right after </Duration>, note </Duration> is a required element
        final int durationEnd = vastBuilder.indexOf(VAST_DURATION_ELEMENT_END, linearStart);
        pixelInsertionIdx = durationEnd + VAST_DURATION_ELEMENT_END.length();
        pixelsStringBuilder = buildTrackingEventElements(trackingEventMap)
            .insert(0, VAST_TRACKING_EVENTS)
            .append(VAST_TRACKING_EVENTS_END);
        LOGGER.trace("Linear creative does not have TrackingEvents, insert {} together with pixels after </Duration>",
            VAST_TRACKING_EVENT);
      } else {
        // current Linear creative already has TrackingEvents, insert right after TrackingEvents open element
        pixelInsertionIdx = trackingEventsStart + VAST_TRACKING_EVENTS.length();
        pixelsStringBuilder = buildTrackingEventElements(trackingEventMap);
        LOGGER.trace("Linear creative has {} already, just insert pixels in it",
            VAST_TRACKING_EVENTS);
      }

      // insert pixels
      vastBuilder.insert(pixelInsertionIdx, pixelsStringBuilder);

      // update the global start index to look for the next Linear element
      start = vastBuilder.indexOf(VAST_LINEAR_ELEMENT_END, pixelInsertionIdx) + VAST_LINEAR_ELEMENT_END.length();
      count++;
    }

    if (count > 0) {
      LOGGER.debug("Total Linear creative elements inside InLine VAST Ad processed: {}", count);
    } else {
      LOGGER.debug("No Linear creative found in InLine VAST");
    }
  }

  /**
   * Insert Tracking pixels in each Linear creatives inside a Wrapper VAST.
   * @param vastBuilder   {@link StringBuilder} vast builder
   * @param idx           {@code int} start index in the vast builder to process
   * @param trackingEventMap    {@code Multimap<TrackingEventElementType, String>} pixels to insert
   */
  private static void insertTrackingWrapperVastLinearCreative(StringBuilder vastBuilder, int idx,
      Multimap<TrackingEventElementType, String> trackingEventMap) {
    LOGGER.debug("Inserting Tracking pixels for Linear Creatives in Wrapper VAST...");
    int start = idx;
    int count = 0;

    // there could be multiple Linear creatives
    while (start != -1) {
      LOGGER.trace("Start looking for Linear element #{} inside Wrapper VAST to insert pixels, start={}",
          count + 1, start);
      int pixelInsertionIdx = start;
      StringBuilder pixelsStringBuilder = null;

      // look for the next close element </Linear>
      final int linearEnd = vastBuilder.indexOf(VAST_LINEAR_ELEMENT_END, start);
      if (linearEnd == -1) {
        LOGGER.trace("Either no Linear creative or all the Linear creatives have been processed already");
        break;
      } else {
        LOGGER.trace("Found {} at index {}", VAST_LINEAR_ELEMENT_END, linearEnd);
      }

      // <Linear> element in Wrapper VAST does not have attribute, search for <Linear> as open element
      //TODO: if in the future, Wrapper VAST Linear element could have attributes, then we need to
      //do the same check as in insertTrackingInLineVastLinearCreative
      int linearStart = vastBuilder.indexOf(VAST_LINEAR_ELEMENT_WITHOUT_ATTRS, start);
      if (linearStart == -1 || linearStart > linearEnd) {
        // this should not happen since we already found a close element </Linear>
        LOGGER.error("No open Linear element found to match the close Linear element, invalid Wrapper VAST?");
        break;
      } else {
        // move currentStart index pass open element
        linearStart += VAST_LINEAR_ELEMENT_WITHOUT_ATTRS.length();
        LOGGER.trace("Linear element has no attributes, start index: {}, end index: {}",
            linearStart, linearEnd);
      }

      // check if <TrackingEvents> exists already
      final int trackingEventsStart = vastBuilder.indexOf(VAST_TRACKING_EVENTS, linearStart);
      if (trackingEventsStart == -1 || trackingEventsStart > linearEnd) {
        //TODO: create and insert an empty TrackingEvents element?

        // current Linear creative does not have TrackingEvents
        // insert before the optional <VideoClicks> element if it exists
        final int videoClicksStart = vastBuilder.indexOf(VAST_VIDEO_CLICKS_ELEMENT, linearStart);
        if (videoClicksStart == -1 || videoClicksStart > linearEnd) {
          // <VideoClicks> does not exist, insert before </Linear> element
          pixelInsertionIdx = linearEnd;
          pixelsStringBuilder = buildTrackingEventElements(trackingEventMap)
              .insert(0, VAST_TRACKING_EVENTS)
              .append(VAST_TRACKING_EVENTS_END);
          LOGGER.trace("Linear creative does not have TrackingEvents, {} not exist, "
                  + "insert {} together with pixels before {}",
              VAST_VIDEO_CLICKS_ELEMENT, VAST_TRACKING_EVENTS, VAST_LINEAR_ELEMENT_END);

        } else {
          // <VideoClicks> exist, insert before it
          pixelInsertionIdx = videoClicksStart;
          pixelsStringBuilder = buildTrackingEventElements(trackingEventMap)
              .insert(0, VAST_TRACKING_EVENTS)
              .append(VAST_TRACKING_EVENTS_END);
          LOGGER.trace("Linear creative does not have TrackingEvents, but found {}, "
                  + "insert {} together with pixels before {}",
              VAST_VIDEO_CLICKS_ELEMENT, VAST_TRACKING_EVENTS, VAST_VIDEO_CLICKS_ELEMENT);
        }

      } else {
        // current Linear creative already has TrackingEvents, insert right after TrackingEvents open element
        pixelInsertionIdx = trackingEventsStart + VAST_TRACKING_EVENTS.length();
        pixelsStringBuilder = buildTrackingEventElements(trackingEventMap);
        LOGGER.trace("Linear creative has {}, just insert pixels in it",
            VAST_TRACKING_EVENTS);
      }

      // insert pixels
      vastBuilder.insert(pixelInsertionIdx, pixelsStringBuilder);

      // update the global start index to look for the next Linear element
      start = vastBuilder.indexOf(VAST_LINEAR_ELEMENT_END, pixelInsertionIdx) + VAST_LINEAR_ELEMENT_END.length();
      count++;
    }

    if (count > 0) {
      LOGGER.debug("Total Linear Creative elements inside Wrapper VAST Ad processed: {}", count);
    } else {
      LOGGER.debug("No Linear Creative found in Wrapper VAST");
    }
  }

  /**
   * Insert Tracking pixels in each NonLinear creatives inside an InLine or a Wrapper VAST.
   * @param vastBuilder           {@link StringBuilder} vast builder
   * @param idx                   {@code int} start index in the vast builder to process
   * @param trackingEventMap    {@code Multimap<TrackingEventElementType, String>} pixels to insert
   */
  private static void insertTrackingNonLinearCreative(StringBuilder vastBuilder, int idx,
      Multimap<TrackingEventElementType, String> trackingEventMap) {
    LOGGER.debug("Inserting Tracking pixels for NonLinearAds creatives...");
    int start = idx;
    int count = 0;

    // there might be multiple NonLinear creatives
    while (start != -1) {
      LOGGER.trace("Start looking for NonLinearAds element #{} to insert pixels, start={}",
          count + 1, start);
      int pixelInsertionIdx = start;
      StringBuilder pixelsStringBuilder = null;

      // look for the next close element </NonLinearAds>
      final int nonLinearAdsEnd = vastBuilder.indexOf(VAST_NON_LINEAR_ADS_END, start);
      if (nonLinearAdsEnd == -1) {
        LOGGER.trace("Either no NonLinearAds creative or all the NonLinearAds creatives have been processed already");
        break;
      } else {
        LOGGER.trace("Found {} at index {}", VAST_NON_LINEAR_ADS_END, nonLinearAdsEnd);
      }

      // <NonLinearAds> does not have attributes, we can simply search for <NonLinearAds> as open element
      int nonLinearAdsStart = vastBuilder.indexOf(VAST_NON_LINEAR_ADS_WITHOUT_ATTRS, start);
      if (nonLinearAdsStart == -1 || nonLinearAdsStart > nonLinearAdsEnd) {
        // this should not happen since we already found a close element </Linear>
        LOGGER.error("No open NonLinearAds element found to match the close Linear element, invalid Wrapper VAST?");
        break;
      } else {
        // move start index pass open element
        nonLinearAdsStart += VAST_NON_LINEAR_ADS_WITHOUT_ATTRS.length();
        LOGGER.trace("Found {}, start index: {}, end index: {}",
            VAST_NON_LINEAR_ADS_WITHOUT_ATTRS, nonLinearAdsStart, nonLinearAdsEnd);
      }

      // check if <TrackingEvents> exists already
      final int trackingEventsStart = vastBuilder.indexOf(VAST_TRACKING_EVENTS, nonLinearAdsStart);
      if (trackingEventsStart == -1 || trackingEventsStart > nonLinearAdsEnd) {
        // current NonLinearAds creative does not have TrackingEvents
        // insert before the required <NonLinear> element
        final int nonLinearStart = vastBuilder.indexOf(VAST_NON_LINEAR, nonLinearAdsStart);
        pixelInsertionIdx = nonLinearStart;
        pixelsStringBuilder = buildTrackingEventElements(trackingEventMap)
            .insert(0, VAST_TRACKING_EVENTS)
            .append(VAST_TRACKING_EVENTS_END);
        LOGGER.trace("NonLinearAds does not have TrackingEvents, insert {} together with pixels before <NonLinear>",
            VAST_TRACKING_EVENTS);
      } else {
        // current NonLinearAds creative already has TrackingEvents, insert right after TrackingEvents open element
        pixelInsertionIdx = trackingEventsStart + VAST_TRACKING_EVENTS.length();
        pixelsStringBuilder = buildTrackingEventElements(trackingEventMap);
        LOGGER.trace("NonLinearAds creative has {} already, just insert pixels in it",
            VAST_TRACKING_EVENTS);
      }

      // insert pixels
      vastBuilder.insert(pixelInsertionIdx, pixelsStringBuilder);

      // update the global start index to look for the next NonLinearAds element
      start = vastBuilder.indexOf(VAST_NON_LINEAR_ADS_END, pixelInsertionIdx) + VAST_NON_LINEAR_ADS_END.length();
      count++;
    }

    if (count > 0) {
      LOGGER.debug("Total NonLinearAds elements inside InLine VAST Ad processed: {}", count);
    } else {
      LOGGER.debug("No NonLinearAds creatives found in the VAST");
    }
  }

  /**
   * Insert Tracking pixels in each Companion ad inside an InLine or a Wrapper VAST.
   * @param vastBuilder         {@link StringBuilder} vast builder
   * @param idx                 {@code int} start index in the vast builder to process
   * @param trackingEventMap    {@code Multimap<TrackingEventElementType, String>} pixels to insert
   */
  public static void insertTrackingCompanionCreative(StringBuilder vastBuilder, int idx,
      Multimap<TrackingEventElementType, String> trackingEventMap) {
    LOGGER.debug("Inserting Tracking pixels for Companion ads in the VAST...");
    int start = idx;
    int count = 0;

    // there could be multiple Companion ads
    while (start != -1) {
      // look for the next close element </Companion>
      LOGGER.trace("Start looking for Companion element #{} to insert pixels, start={}",
          count + 1, start);
      int pixelInsertionIdx = start;
      StringBuilder pixelsStringBuilder = null;

      final int companionEnd = vastBuilder.indexOf(VAST_COMPANION_ELEMENT_END, start);
      if (companionEnd == -1) {
        LOGGER.trace("Either no Companion ad in VAST or all the Companion ads have been processed already");
        break;
      } else {
        LOGGER.trace("Found {} at index {}", VAST_COMPANION_ELEMENT_END, companionEnd);
      }

      // <Companion> element has required attributes of width and height, so we can search for
      // "<Companion " as open element
      int companionStart = vastBuilder.indexOf(VAST_COMPANION_ELEMENT_WITH_ATTRS, start);
      LOGGER.trace("Found {} at index {}", VAST_COMPANION_ELEMENT_WITH_ATTRS, companionStart);
      if (companionStart == -1 || companionStart > companionEnd) {
        // this should not happen since we have close element </Companion> already
        LOGGER.error("No open Companion element found to match the close Companion element, "
                + "invalid VAST, companionStart: {}, companionEnd: {}",
            companionStart, companionEnd);
        break;
      } else {
        // move start index pass open element, find the next > to account for attributes
        companionStart = vastBuilder.indexOf(">", companionStart) + 1;
        LOGGER.trace("Updated element {} index to {} to include its attributes",
            VAST_COMPANION_ELEMENT_WITH_ATTRS, companionStart);
      }

      // check if <TrackingEvents> exists already
      final int trackingEventsStart = vastBuilder.indexOf(VAST_TRACKING_EVENTS, companionStart);
      if (trackingEventsStart == -1 || trackingEventsStart > companionEnd) {
        // current Companion element does not have TrackingEvents
        // first search for one of the required close elements:
        // </StaticResource>, </IFrameResource>, </HTMLResource>
        int requiredElementEnd = findNextNeedleIndex(vastBuilder, companionStart, companionEnd,
            VAST_STATIC_RESOURCE_END, true);
        if (requiredElementEnd == -1) {
          requiredElementEnd = findNextNeedleIndex(vastBuilder, companionStart, companionEnd,
              VAST_IFRAME_RESOURCE_END, true);
          if (requiredElementEnd == -1) {
            requiredElementEnd = findNextNeedleIndex(vastBuilder, companionStart, companionEnd,
                VAST_HTML_RESOURCE_END, true);
            if (requiredElementEnd == -1) {
              // should not happen that all three elements are missing
              LOGGER.error("One of <StaticResource>, <IFrameResource> or <HTMLResource> elements "
                  + "must exist in Companion ad, but not, invalid VAST?");
              break;
            } else {
              LOGGER.trace("Found a required element: {} at index {}, searching for {} from the index",
                  VAST_HTML_RESOURCE_END, requiredElementEnd, VAST_CREATIVE_EXTENSIONS_END);
            }
          } else {
            LOGGER.trace("Found a required element: {} at index {}, searching for {} from the index",
                VAST_IFRAME_RESOURCE_END, requiredElementEnd, VAST_CREATIVE_EXTENSIONS_END);
          }
        } else {
          LOGGER.trace("Found a required element: {} at index {} , searching for {} from the index",
              VAST_STATIC_RESOURCE_END, requiredElementEnd, VAST_CREATIVE_EXTENSIONS_END);
        }

        // then search for an optional </CreativeExtensions> element, if found then insert after that
        // if </CreativeExtensions> is not found, then insert after the required element mentioned above
        final int creativeExtensionsEnd = findNextNeedleIndex(vastBuilder, requiredElementEnd,
            companionEnd, VAST_CREATIVE_EXTENSIONS_END, true);
        if (creativeExtensionsEnd == -1) {
          pixelInsertionIdx = requiredElementEnd;
          pixelsStringBuilder = buildTrackingEventElements(trackingEventMap)
              .insert(0, VAST_TRACKING_EVENTS)
              .append(VAST_TRACKING_EVENTS_END);
          LOGGER.trace("Companion ad does not have TrackingEvents, the optional {} is not found, "
                  + "insert {} together with pixels after {}, {} or {}",
              VAST_CREATIVE_EXTENSIONS_END, VAST_TRACKING_EVENTS, VAST_STATIC_RESOURCE_END,
              VAST_IFRAME_RESOURCE_END, VAST_HTML_RESOURCE_END);
        } else {
          pixelInsertionIdx = creativeExtensionsEnd;
          pixelsStringBuilder = buildTrackingEventElements(trackingEventMap)
              .insert(0, VAST_TRACKING_EVENTS)
              .append(VAST_TRACKING_EVENTS_END);
          LOGGER.trace("Companion ad does not have TrackingEvents, found the optional {}, "
                  + "insert {} together with pixels after {}, {} or {}",
              VAST_CREATIVE_EXTENSIONS_END, VAST_TRACKING_EVENTS, VAST_STATIC_RESOURCE_END,
              VAST_IFRAME_RESOURCE_END, VAST_HTML_RESOURCE_END);
        }

      } else {
        // current Companion element has TrackingEvents already
        pixelInsertionIdx = trackingEventsStart + VAST_TRACKING_EVENTS.length();
        pixelsStringBuilder = buildTrackingEventElements(trackingEventMap);
        LOGGER.trace("Companion ad has {}, just insert pixels in it", VAST_TRACKING_EVENTS);
      }

      // insert pixels
      vastBuilder.insert(pixelInsertionIdx, pixelsStringBuilder);

      // update the global start index to look for the next Companion
      start = vastBuilder.indexOf(VAST_COMPANION_ELEMENT_END, pixelInsertionIdx)
          + VAST_COMPANION_ELEMENT_END.length();
      count++;
    }

    if (count > 0) {
      LOGGER.debug("Total Companion ads processed: {}", count);
    } else {
      LOGGER.debug("No Companion ads found in the VAST");
    }
  }
}
