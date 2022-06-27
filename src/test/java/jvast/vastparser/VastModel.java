package jvast.vastparser;

import jvast.util.XMLUtil;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;


public class VastModel {
  private static final Logger LOGGER = LogManager.getLogger(VastModel.class);

  private transient Document vastsDocument;
  private String pickedMediaFileURL = null;

  // Tracking xpath expressions
  private static final String INLINE_LINEAR_TRCKING_XPATH =
      "/VASTS/VAST/Ad/InLine/Creatives/Creative/Linear/TrackingEvents/Tracking";
  private static final String INLINE_NONLINEAR_TRACKING_XPATH =
      "/VASTS/VAST/Ad/InLine/Creatives/Creative/NonLinearAds/TrackingEvents/Tracking";
  private static final String INLINE_COMPANION_TRACKING_XPATH =
      "/VASTS/VAST/Ad/InLine/Creatives/Creative/CompanionAds/Companion/TrackingEvents/Tracking";
  private static final String WRAPPER_LINEAR_TRACKING_XPATH =
      "/VASTS/VAST/Ad/Wrapper/Creatives/Creative/Linear/TrackingEvents/Tracking";
  private static final String WRAPPER_NONLINEAR_TRACKING_XPATH =
      "/VASTS/VAST/Ad/Wrapper/Creatives/Creative/NonLinearAds/TrackingEvents/Tracking";
  private static final String WRAPPER_COMPANION_TRACKING_XPATH =
      "/VASTS/VAST/Ad/Wrapper/Creatives/Creative/CompanionAds/Companion/TrackingEvents/Tracking";

  private static final String COMBINEDTRACKINGXPATH = INLINE_LINEAR_TRCKING_XPATH + "|"
      + INLINE_NONLINEAR_TRACKING_XPATH + "|"
      + INLINE_COMPANION_TRACKING_XPATH + "|"
      + WRAPPER_LINEAR_TRACKING_XPATH + "|"
      + WRAPPER_NONLINEAR_TRACKING_XPATH + "|"
      + WRAPPER_COMPANION_TRACKING_XPATH;

  // Mediafile xpath expression
  private static final String MEDIAFILEXPATH = "//MediaFile";

  // Duration xpath expression
  private static final String DURATIONXPATH = "//Duration";

  // Videoclicks xpath expression
  private static final String VIDEOCLICKSXPATH = "//VideoClicks";

  // Videoclicks xpath expression
  private static final String IMPRESSIONXPATH = "//Impression";

  // Error url  xpath expression
  private static final String ERRORURLXPATH = "//Error";

  private static final String LINEARXPATH = "//Linear";

  String timeOffset = "00:00:00";

  public VastModel(Document vasts) {
    this.vastsDocument = vasts;
  }

  public VastModel(Document vasts, String timeOffset) {
    this.vastsDocument = vasts;
    setTimeOffset(timeOffset);
  }

  /**+
   *.
   * @param timeOffset String
   */
  public void setTimeOffset(String timeOffset) {
    if (timeOffset.equalsIgnoreCase("start")) {
      this.timeOffset = "00:00:00";
    } else if (timeOffset.equalsIgnoreCase("end")) {
      this.timeOffset = "00:45:00";
    } else {
      this.timeOffset = timeOffset;
    }
  }

  public String getTimeOffset() {
    return timeOffset;
  }

  public Document getVastsDocument() {
    return vastsDocument;
  }

  /**
   * Get TrackingEvents and their pixel urls.
   * @return HashMap
   */
  public HashMap<TrackingEventsType, List<String>> getTrackingPixels() {
    List<String> tracking;
    HashMap<TrackingEventsType, List<String>> trackings = new HashMap<TrackingEventsType, List<String>>();

    XPath xpath = XPathFactory.newInstance().newXPath();

    try {
      NodeList nodes = (NodeList) xpath.evaluate(COMBINEDTRACKINGXPATH, vastsDocument, XPathConstants.NODESET);
      Node node;
      String trackingURL;
      String eventName;
      TrackingEventsType key = null;

      if (nodes != null) {
        for (int i = 0; i < nodes.getLength(); i++) {
          node = nodes.item(i);
          NamedNodeMap attributes = node.getAttributes();

          eventName = (attributes.getNamedItem("event")).getNodeValue();
          try {
            key = TrackingEventsType.valueOf(eventName);
          } catch (IllegalArgumentException e) {
            LOGGER.debug("Event is not valid, skipping it: {}", eventName);
            continue;
          }
          trackingURL = XMLUtil.getElementValue(node);

          if (trackings.containsKey(key)) {
            tracking = trackings.get(key);
            tracking.add(trackingURL);
          } else {
            tracking = new ArrayList<String>();
            tracking.add(trackingURL);
            trackings.put(key, tracking);

          }
        }
      }
    } catch (Exception e) {
      LOGGER.error(e.getMessage(), e);
      return null;
    }

    return trackings;
  }

  /**+
   * .
   * @return List
   */
  public List<VastMediaFile> getMediaFiles() {
    ArrayList<VastMediaFile> mediaFiles = new ArrayList<VastMediaFile>();
    XPath xpath = XPathFactory.newInstance().newXPath();

    try {
      NodeList nodes = (NodeList) xpath.evaluate(MEDIAFILEXPATH,
              vastsDocument, XPathConstants.NODESET);
      Node node;
      VastMediaFile mediaFile;
      String mediaURL;
      Node attributeNode;

      if (nodes != null) {
        for (int i = 0; i < nodes.getLength(); i++) {
          mediaFile = new VastMediaFile();
          node = nodes.item(i);
          NamedNodeMap attributes = node.getAttributes();

          attributeNode = attributes.getNamedItem("apiFramework");
          mediaFile.setApiFramework((attributeNode == null) ? null
                  : attributeNode.getNodeValue());

          attributeNode = attributes.getNamedItem("bitrate");
          mediaFile.setBitrate((attributeNode == null) ? null
                  : new BigInteger(attributeNode.getNodeValue()));

          attributeNode = attributes.getNamedItem("delivery");
          mediaFile.setDelivery((attributeNode == null) ? null
                  : attributeNode.getNodeValue());

          attributeNode = attributes.getNamedItem("height");
          mediaFile.setHeight((attributeNode == null) ? null
                  : new BigInteger(attributeNode.getNodeValue()));

          attributeNode = attributes.getNamedItem("id");
          mediaFile.setId((attributeNode == null) ? null
                  : attributeNode.getNodeValue());

          attributeNode = attributes
                  .getNamedItem("maintainAspectRatio");
          mediaFile
                  .setMaintainAspectRatio((attributeNode == null) ? null
                          : Boolean.valueOf(attributeNode
                          .getNodeValue()));

          attributeNode = attributes.getNamedItem("scalable");
          mediaFile.setScalable((attributeNode == null) ? null
                  : Boolean.valueOf(attributeNode.getNodeValue()));

          attributeNode = attributes.getNamedItem("type");
          mediaFile.setType((attributeNode == null) ? null
                  : attributeNode.getNodeValue());

          attributeNode = attributes.getNamedItem("width");
          mediaFile.setWidth((attributeNode == null) ? null
                  : new BigInteger(attributeNode.getNodeValue()));

          mediaURL = XMLUtil.getElementValue(node);
          mediaFile.setValue(mediaURL);

          mediaFiles.add(mediaFile);
        }
      }

    } catch (Exception e) {
      LOGGER.error(e.getMessage(), e);
      return null;
    }

    return mediaFiles;
  }

  /**+
   * .
   * @return String
   */
  public String getDuration() {
    String duration = null;
    XPath xpath = XPathFactory.newInstance().newXPath();
    try {
      NodeList nodes = (NodeList) xpath.evaluate(DURATIONXPATH,vastsDocument, XPathConstants.NODESET);
      Node node;
      if (nodes != null) {
        for (int i = 0; i < nodes.getLength(); i++) {
          node = nodes.item(i);
          duration = XMLUtil.getElementValue(node);
        }
      }
    } catch (Exception e) {
      LOGGER.error(e.getMessage(), e);
      return null;
    }

    return duration;
  }

  /**+
   * .
   * @return VideoClicks
   */
  public VideoClicks getVideoClicks() {
    VideoClicks videoClicks = new VideoClicks();

    XPath xpath = XPathFactory.newInstance().newXPath();

    try {
      NodeList nodes = (NodeList) xpath.evaluate(VIDEOCLICKSXPATH, vastsDocument, XPathConstants.NODESET);
      Node node;

      if (nodes != null) {
        for (int i = 0; i < nodes.getLength(); i++) {
          node = nodes.item(i);

          NodeList childNodes = node.getChildNodes();

          Node child;
          String value = null;

          for (int childIndex = 0; childIndex < childNodes
                  .getLength(); childIndex++) {

            child = childNodes.item(childIndex);
            String nodeName = child.getNodeName();

            if (nodeName.equalsIgnoreCase("ClickTracking")) {
              value = XMLUtil.getElementValue(child);
              videoClicks.getClickTracking().add(value);

            } else if (nodeName.equalsIgnoreCase("ClickThrough")) {
              value = XMLUtil.getElementValue(child);
              videoClicks.setClickThrough(value);

            } else if (nodeName.equalsIgnoreCase("CustomClick")) {
              value = XMLUtil.getElementValue(child);
              videoClicks.getCustomClick().add(value);
            }
          }
        }
      }

    } catch (Exception e) {
      LOGGER.error(e.getMessage(), e);
      return null;
    }

    return videoClicks;
  }

  private List<String>  getListFromXPath(String inputXpath) {
    ArrayList<String> list = new ArrayList<String>();
    XPath xpath = XPathFactory.newInstance().newXPath();
    try {
      NodeList nodes = (NodeList) xpath.evaluate(inputXpath,
              vastsDocument, XPathConstants.NODESET);
      Node node;
      if (nodes != null) {
        for (int i = 0; i < nodes.getLength(); i++) {
          node = nodes.item(i);
          list.add(XMLUtil.getElementValue(node));
        }
      }

    } catch (Exception e) {
      LOGGER.error(e.getMessage(), e);
      return null;
    }

    return list;
  }

  /**
   * Get Ad level Impression pixel urls.
   * @return      {@code List<String>} immpression pixels
   */
  public List<String> getImpressionPixels() {
    List<String> list = getListFromXPath(IMPRESSIONXPATH);
    return list;
  }

  /**
   * Get Ad level Error pixel urls.
   * @return      {@code List<String>} error pixels
   */
  public List<String> getErrorPixels() {
    List<String> list = getListFromXPath(ERRORURLXPATH);
    return list;
  }

  /**+
   * .
   * @return String
   */
  public String getSkipOffset() {
    String skipoffset = "00:00:05";
    XPath xpath = XPathFactory.newInstance().newXPath();
    try {
      NodeList nodes = (NodeList) xpath.evaluate(LINEARXPATH,vastsDocument, XPathConstants.NODESET);
      Node node;
      if (nodes != null) {
        for (int i = 0; i < nodes.getLength(); i++) {
          node = nodes.item(i);
          skipoffset = XMLUtil.getAttributeValue(node, "skipoffset");
        }
      }
    } catch (Exception e) {
      LOGGER.error(e.getMessage(), e);
      return null;
    }

    return skipoffset;
  }

  private void writeObject(ObjectOutputStream oos) throws IOException {
    LOGGER.debug("writeObject: about to write");
    oos.defaultWriteObject();

    String data = XMLUtil.xmlDocumentToString(vastsDocument);
    // oos.writeChars();
    oos.writeObject(data);
    LOGGER.debug("done writing");

  }

  private void readObject(ObjectInputStream ois)
          throws ClassNotFoundException, IOException {
    LOGGER.debug("readObject: about to read");
    ois.defaultReadObject();

    String vastString = (String) ois.readObject();
    LOGGER.debug("vastString data is: {}", vastString);

    vastsDocument = XMLUtil.stringToDocument(vastString);

    LOGGER.debug("done reading");
  }

  public String getPickedMediaFileURL() {
    return pickedMediaFileURL;
  }

  public void setPickedMediaFileURL(String pickedMediaFileURL) {
    this.pickedMediaFileURL = pickedMediaFileURL;
  }
}
