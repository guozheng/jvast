package jvast.vastparser;

import jvast.util.HttpGetUtil;
import jvast.util.XMLUtil;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.ArrayList;
import java.util.List;

public class VmapModel {
  private static final String ADBREAKXPATH = "/AdBreak";
  private transient Document vmapDocument;

  public VmapModel(Document xmlDoc) {
    this.vmapDocument = xmlDoc;
  }

  public Document getVmapDocument() {
    return vmapDocument;
  }

  /**+
   * .
   * @return List
   */
  public List<VastModel> getVastModels() {
    List<VastModel> models = new ArrayList<>(5);

    NodeList adbreakNodes = vmapDocument.getElementsByTagName("vmap:AdBreak");
    int length = adbreakNodes.getLength();
    for (int i = 0; i < length; i++) {
      Node node = adbreakNodes.item(i);

      String timeOffset = XMLUtil.getAttributeValue(node, "timeOffset");
      //String breakId = XmlTools.getAttributeValue(node, "breakId");

      NodeList childs = node.getChildNodes();
      for (int j = 0; j < childs.getLength(); j++) {
        Node item = childs.item(j);
        if (item.getNodeName().equalsIgnoreCase("vmap:AdSource")) {
          String vastUrl = item.getTextContent().trim();
          String vastXml = HttpGetUtil.get(vastUrl);
          VastModel vastModel = new VastModel(XMLUtil.stringToDocument(vastXml),timeOffset);
          models.add(vastModel);
        }
      }
    }
    return models;
  }
}
