package io.github.guozheng.jvast.util;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.experimental.UtilityClass;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@UtilityClass
public class StringUtil {
  private static final Logger LOGGER = LogManager.getLogger(StringUtil.class);
  public static final String EMPTY = "";

  /**
   * Match all strings in the VAST doc using a pattern.
   *
   * @param adXml     {@code String} video ad xml
   * @param pattern   {@link Pattern} regex pattern
   * @return          {@code Set<String>} matched strings
   */
  static Set<String> findNeedlesByPattern(String adXml, Pattern pattern) {
    Set<String> matchedResults = new HashSet<>();
    Matcher matcher = pattern.matcher(adXml);
    while (matcher.find()) {
      String result = matcher.group(1);
      matchedResults.add(result);
    }
    return matchedResults;
  }

  /**
   * Find the first occurrence of an element in between start ane end index.
   * @param docBuilder     {@link StringBuilder} vast builder
   * @param start           {@code int} start index in the vast builder to process
   * @param end             {@code int} end index in the vast builder to process
   * @param needle         {@link String} VAST element to search
   * @param returnEndIndex  {@code boolean} true if return end index (including the length of element),
   *     false if return the begin index (not including the length of element)
   * @return                {@code int} index of the element
   */
  public static int findNextNeedleIndex(StringBuilder docBuilder, int start, int end, String needle, boolean returnEndIndex) {
    final int idx = docBuilder.indexOf(needle, start);
    if (idx != -1 && idx < end) {
      return returnEndIndex ? idx + needle.length() : idx;
    } else {
      return -1;
    }
  }

  /**
   * Find the first matching substring in between {@code left} and {@code right}, starting from {@code fromIdx}.
   *
   * @param left                {@code String} left substring separator
   * @param right               {@code String} right substring separator
   * @param doc                 {@code String} doc string to search
   * @param fromIdx             {@code int} starting index to search
   * @param defaultValue        {@code String} default value if no match is found
   * @return                    {@code String} matching substring
   */
  public static String getSubString(String left, String right, String doc, int fromIdx, String defaultValue) {
    String sub;
    int leftIdx = doc.indexOf(left, fromIdx);
    if (leftIdx == -1) {
      LOGGER.debug("No left string {} found in doc: {}, use default value: {}", left, doc, defaultValue);
      return defaultValue;
    }

    int rightIdx = doc.indexOf(right, leftIdx + left.length());
    if (rightIdx == -1) {
      LOGGER.debug("No right string {} found in doc: {}, use default value: {}", right, doc, defaultValue);
      return defaultValue;
    }

    sub = doc.substring(leftIdx + left.length(), rightIdx);
    return sub;
  }

  /**
   * Find the first matching substring in between {@code left} and {@code right}, starting from {@code fromIdx}.
   *
   * @param left                {@code String} left substring separator
   * @param right               {@code String} right substring separator
   * @param docBuilder          {@code StringBuilder} doc string builder to search
   * @param fromIdx             {@code int} starting index to search
   * @param defaultValue        {@code String} default value if no match is found
   * @return                    {@code String} matching substring
   */
  public static String getSubString(String left, String right, StringBuilder docBuilder, int fromIdx, String defaultValue) {
    String sub;
    int leftIdx = docBuilder.indexOf(left, fromIdx);
    if (leftIdx == -1) {
      LOGGER.debug("No left string {} found in doc: {}, use default value: {}", left, docBuilder, defaultValue);
      return defaultValue;
    }

    int rightIdx = docBuilder.indexOf(right, leftIdx + left.length());
    if (rightIdx == -1) {
      LOGGER.debug("No right string {} found in doc: {}, use default value: {}", right, docBuilder, defaultValue);
      return defaultValue;
    }

    sub = docBuilder.substring(leftIdx + left.length(), rightIdx);
    return sub;
  }

  /**
   * Find all matching substrings in between {@code left} and {@code right}.
   *
   * @param left      {@code String} left substring separator
   * @param right     {@code String} right substring separator
   * @param doc       {@code String} doc string to search
   * @return          {@code List<String>} a list of substrings
   */
  public static List<String> getSubStrings(String left, String right, String doc) {
    List<String> subs = new LinkedList<>();

    int fromIdx = 0;
    while (fromIdx < doc.length()) {
      int leftIdx = doc.indexOf(left, fromIdx);
      if (leftIdx == -1) {
        LOGGER.debug("No more left string {} found", left);
        break;
      }
      int rightIdx = doc.indexOf(right, leftIdx + left.length());
      if (rightIdx == -1) {
        LOGGER.debug("No more right string {} found", right);
        break;
      }
      String sub = doc.substring(leftIdx + left.length(), rightIdx);
      subs.add(sub.trim()); //make sure new lines or whitespaces are trimmed
      fromIdx = rightIdx;
    }

    return subs;
  }

  /**
   * Find all matching substrings in between {@code left} and {@code right}.
   *
   * @param left          {@code String} left substring separator
   * @param right         {@code String} right substring separator
   * @param docBuilder    {@code StringBuilder} doc string builder to search
   * @return              {@code List<String>} a list of substrings
   */
  public static List<String> getSubStrings(String left, String right, StringBuilder docBuilder) {
    List<String> subs = new LinkedList<>();

    int fromIdx = 0;
    while (fromIdx < docBuilder.length()) {
      int leftIdx = docBuilder.indexOf(left, fromIdx);
      if (leftIdx == -1) {
        LOGGER.debug("No more left string {} found", left);
        break;
      }
      int rightIdx = docBuilder.indexOf(right, leftIdx + left.length());
      if (rightIdx == -1) {
        LOGGER.debug("No more right string {} found", right);
        break;
      }
      String sub = docBuilder.substring(leftIdx + left.length(), rightIdx);
      subs.add(sub.trim()); //make sure new lines or whitespaces are trimmed
      fromIdx = rightIdx;
    }

    return subs;
  }


  /**
   * Count occurrence of a substring in a string builder.
   * @param sb          {@link StringBuilder} string builder
   * @param needle      {@link String} sub string to search for
   * @return            {@code int} count of the sub string
   */
  public static int countNeedles(StringBuilder sb, String needle) {
    int count = 0;
    int start = sb.indexOf(needle);
    while (start != -1) {
      count++;
      start = sb.indexOf(needle, start + 1);
    }
    return count;
  }

}
