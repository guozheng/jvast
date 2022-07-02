package org.woshiadai.jvast.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.CharacterData;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

public class XMLUtil {
  private static final Logger LOGGER = LogManager.getLogger(XMLUtil.class);

  /**
   * Convert XML document object to string.
   * @param doc XML Document
   * @return String
   */
  public static String xmlDocumentToString(Document doc) {
    String xml = null;
    try {
      TransformerFactory tf = TransformerFactory.newInstance();
      Transformer transformer = tf.newTransformer();
      transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
      transformer.setOutputProperty(OutputKeys.METHOD, "xml");
      transformer.setOutputProperty(OutputKeys.INDENT, "yes");
      transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
      transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");

      StringWriter sw = new StringWriter();
      transformer.transform(new DOMSource(doc), new StreamResult(sw));

      xml = sw.toString();
    } catch (Exception e) {
      LOGGER.warn(e.getMessage());
    }

    return xml;
  }

  /**
   * Convert XML Node to string.
   * @param node XML node
   * @return String
   */
  public static String xmlDocumentToString(Node node) {
    String xml = null;
    try {
      TransformerFactory tf = TransformerFactory.newInstance();
      Transformer transformer = tf.newTransformer();
      transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
      transformer.setOutputProperty(OutputKeys.METHOD, "xml");
      transformer.setOutputProperty(OutputKeys.INDENT, "yes");
      transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
      transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");

      StringWriter sw = new StringWriter();
      transformer.transform(new DOMSource(node), new StreamResult(sw));

      xml = sw.toString();

    } catch (Exception e) {
      LOGGER.warn(e.getMessage());
    }

    return xml;
  }

  /**
   * Convert a string to XML document object.
   * @param doc String
   * @return XML Document
   */
  public static Document stringToDocument(String doc) {
    DocumentBuilder db;
    Document document = null;
    try {
      db = DocumentBuilderFactory.newInstance().newDocumentBuilder();

      InputSource is = new InputSource();
      is.setCharacterStream(new StringReader(doc));

      document = db.parse(is);

    } catch (Exception e) {
      LOGGER.warn(e.getMessage());
    }

    return document;
  }

  /**
   * Convert input stream into string.
   * @param inputStream InputStream
   * @return String
   * @throws IOException Exception
   */
  public static String stringFromStream(InputStream inputStream)
      throws IOException {
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    byte[] buffer = new byte[1024];
    int length = 0;

    while ((length = inputStream.read(buffer)) != -1) {
      baos.write(buffer, 0, length);
    }

    byte[] bytes = baos.toByteArray();

    return new String(bytes, "UTF-8");
  }

  /**
   * Get XML node value.
   * @param node XML node
   * @return String
   */
  public static String getElementValue(Node node) {
    NodeList childNodes = node.getChildNodes();
    Node child;
    String value = null;
    CharacterData cd;

    for (int childIndex = 0; childIndex < childNodes.getLength(); childIndex++) {
      child = childNodes.item(childIndex);
      cd = (CharacterData) child;
      value = cd.getData().trim();

      if (value.length() == 0) {
        // ignore whitespace
        continue;
      }
      return value;
    }

    return value;
  }

  /**
   * Get XML node attribute value.
   * @param node XML Node
   * @param attrName String
   * @return String
   */
  public static String getAttributeValue(Node node, String attrName) {
    String val = StringUtil.EMPTY;
    if (node != null) {
      Node namedItem = node.getAttributes().getNamedItem(attrName);
      if (namedItem != null) {
        val = namedItem.getNodeValue();
      }
    }
    return val;
  }

  /**
   * Write attribute value to given node.
   * @param node  XML Node
   * @param attrName {@code String} Attribute name
   * @param attrValue {@code String} Attribute value
   */
  public static void writeAttributeValue(Node node, String attrName, String attrValue) {
    if (node == null || attrName == null) {
      return;
    }
    Node namedItem = node.getAttributes().getNamedItem(attrName);
    if (namedItem == null) {
      ((Element) node).setAttribute(attrName, attrValue);
    } else {
      namedItem.setNodeValue(attrValue);
    }
  }
}
