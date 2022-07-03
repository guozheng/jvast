package io.github.guozheng.jvast.vastparser;

import io.github.guozheng.jvast.util.XMLUtil;
import io.github.guozheng.jvast.util.XmlValidation;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

public class VastParser {

  private static final Logger LOGGER = LogManager.getLogger(VastParser.class);
  private static final int MAX_VAST_LEVELS = 5;

  /**
   * schema files obtained from IAB: https://github.com/InteractiveAdvertisingBureau/vast
   * note that IAB schemas are more strict for validation,
   * for example: IAB schema only allows max of one Error pixel, Error pixel must appear before
   * Impression pixel, etc.
   * in real world, we've seen VAST with more than one Error pixels, or mixed ordering for Error
   * and Impression pixels, etc.
   * so, we are using slightly different jvast schema files that are less restrictive here.
   */
  public static final String VAST_3_0_SCHEMA_JVAST = "schema/vast3_draft_jvast.xsd";
  public static final String VAST_2_0_1_SCHEMA_JVAST = "schema/vast_2.0.1_jvast.xsd";

  private VastModel vastModel;
  private StringBuilder mergedVastDocs = new StringBuilder(500);

  private String vastSchema;
  private boolean isValidationOn;

  /**
   * Custom constructor to select VAST schema for validation and enable/disable validation.
   * @param vastSchema            {@code String} relative path to the VAST schema, see the schema directory
   * @param isValidationOn        {@code boolean} true to turn on schema validation, false to turn off
   */
  public VastParser(String vastSchema, boolean isValidationOn) {
    this.vastSchema = vastSchema;
    this.isValidationOn = isValidationOn;
  }

  /**
   * Default to use VAST 3.0 schema for validation.
   */
  public VastParser() {
    this.vastSchema = VAST_3_0_SCHEMA_JVAST;
    this.isValidationOn = true;
  }

  public VastModel getModel() {
    return vastModel;
  }

  /**+
   * .
   * @param xmlData String
   * @return int
   */
  public int process(String xmlData) {
    vastModel = null;
    InputStream is;

    is = new ByteArrayInputStream(xmlData.getBytes(StandardCharsets.UTF_8));
    int error = processUri(is, 0);
    try {
      is.close();
    } catch (IOException e) {
      LOGGER.error(e.getMessage());
      return VastParserErrorCode.ERROR_XML_OPEN_OR_READ.getValue();
    }

    if (error != VastParserErrorCode.ERROR_NONE.getValue()) {
      return error;
    }

    Document mainDoc = wrapMergedVastDocWithVasts();
    vastModel = new VastModel(mainDoc);
    if (mainDoc == null) {
      return VastParserErrorCode.ERROR_XML_PARSE.getValue();
    }

    return VastParserErrorCode.ERROR_NONE.getValue();
  }

  private Document wrapMergedVastDocWithVasts() {
    mergedVastDocs.insert(0,"<VASTS>");
    mergedVastDocs.append("</VASTS>");

    String merged = mergedVastDocs.toString();

    Document doc = XMLUtil.stringToDocument(merged);
    return doc;
  }

  private int processUri(InputStream is, int depth) {
    if (depth >= MAX_VAST_LEVELS) {
      String message = "VAST wrapping exceeded max limit of "
              + MAX_VAST_LEVELS + ".";
      LOGGER.error(message);
      return VastParserErrorCode.ERROR_EXCEEDED_WRAPPER_LIMIT.getValue();
    }

    Document doc = createDoc(is);
    if (doc == null) {
      return VastParserErrorCode.ERROR_XML_PARSE.getValue();
    }

    if (isValidationOn) {
      if (!validateAgainstSchema(doc)) {
        return VastParserErrorCode.ERROR_SCHEMA_VALIDATION.getValue();
      }
    }

    merge(doc);

    // check to see if this is a VAST wrapper ad
    NodeList uriToNextDoc = doc
            .getElementsByTagName(VastDocElements.VASTADTAGURI.getValue());
    if (uriToNextDoc == null || uriToNextDoc.getLength() == 0) {
      // This isn't a wrapper ad, so we're done.
      return VastParserErrorCode.ERROR_NONE.getValue();
    } else {
      // This is a wrapper ad, so move on to the wrapped ad and process
      // it.
      LOGGER.debug("Doc is a wrapper. ");
      Node node = uriToNextDoc.item(0);
      String nextUri = XMLUtil.getElementValue(node);
      LOGGER.debug("Follow wrapper URL: {} to fetch next VAST, depth: {}", nextUri, depth);
      InputStream nextInputStream = null;
      try {
        URL nextUrl = new URL(nextUri);
        nextInputStream = nextUrl.openStream();
      } catch (Exception e) {
        LOGGER.error(e.getMessage(), e);
        return VastParserErrorCode.ERROR_XML_OPEN_OR_READ.getValue();
      }
      int error = processUri(nextInputStream, depth + 1);
      try {
        nextInputStream.close();
      } catch (IOException e) {
        LOGGER.error(e.getMessage());
        return VastParserErrorCode.ERROR_XML_PARSE.getValue();
      }
      return error;
    }
  }

  private Document createDoc(InputStream is) {
    try {
      Document doc = DocumentBuilderFactory.newInstance()
              .newDocumentBuilder().parse(is);
      LOGGER.trace("Doc created from input stream");

      doc.getDocumentElement().normalize();
      if (LOGGER.isTraceEnabled()) {
        LOGGER.trace("Doc normalized: {}", printXml(doc));
      }

      return doc;
    } catch (Exception e) {
      LOGGER.error(e.getMessage(), e);
      return null;
    }
  }

  private void merge(Document newDoc) {
    NodeList nodeList = newDoc.getElementsByTagName("VAST");
    Node newDocElement = nodeList.item(0);
    String doc = XMLUtil.xmlDocumentToString(newDocElement);
    mergedVastDocs.append(doc);
  }

  private boolean validateAgainstSchema(Document doc) {
    InputStream stream = getClass().getClassLoader().getResourceAsStream(vastSchema);
    String xml = XMLUtil.xmlDocumentToString(doc);
    boolean isValid = XmlValidation.validate(stream, xml);
    try {
      stream.close();
    } catch (IOException e) {
      LOGGER.error(e.getMessage());
      return false;
    }
    return isValid;
  }

  private static String printXml(Document doc) {
    try {
      DOMSource domSource = new DOMSource(doc);
      StringWriter writer = new StringWriter();
      StreamResult result = new StreamResult(writer);
      Transformer transformer = TransformerFactory.newInstance().newTransformer();
      transformer.setOutputProperty(OutputKeys.INDENT, "yes");
      transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
      transformer.transform(domSource, result);
      return writer.toString();
    } catch (TransformerConfigurationException e) {
      LOGGER.error("Transformer config error", e);
    } catch (TransformerException e) {
      LOGGER.error("Transformer error", e);
    }
    return null;
  }
}
