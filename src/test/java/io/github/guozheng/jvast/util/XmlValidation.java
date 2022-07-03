package io.github.guozheng.jvast.util;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.apache.xerces.jaxp.validation.XMLSchemaFactory;

public class XmlValidation {
  private static final Logger LOGGER = LogManager.getLogger(XmlValidation.class);

  /**
   * Validate XML content against XML schema.
   * @param schemaStream InputStream
   * @param xml String
   * @return boolean
   */
  public static boolean validate(InputStream schemaStream, String xml) {
    SchemaFactory factory = new XMLSchemaFactory();
    Source schemaSource = new StreamSource(schemaStream);
    Source xmlSource = new StreamSource(new ByteArrayInputStream(xml.getBytes()));
    Schema schema;
    try {
      schema = factory.newSchema(schemaSource);
      Validator validator = schema.newValidator();
      validator.validate(xmlSource);
    } catch (Exception e) {
      LOGGER.warn(e.getMessage(), e);
      return false;
    }

    return true;
  }
}
