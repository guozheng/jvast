package jvast.util;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class FileUtil {
  private static Logger LOGGER = LogManager.getLogger(FileUtil.class);

  /**
   * Helper method to read file content.
   *
   * @param filePath    {@code String} file path
   * @return            {@code String} file content
   */
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
