package org.woshiadai.jvast.vastparser;

public enum VastParserErrorCode {
  ERROR_NONE(0, "ERROR_NONE"),
  ERROR_NO_NETWORK(1, "ERROR_NO_NETWORK"),
  ERROR_XML_OPEN_OR_READ(2, "ERROR_XML_OPEN_OR_READ"),
  ERROR_XML_PARSE(3, "ERROR_XML_PARSE"),
  ERROR_SCHEMA_VALIDATION(4, "ERROR_SCHEMA_VALIDATION"),
  ERROR_EXCEEDED_WRAPPER_LIMIT(5, "ERROR_EXCEEDED_WRAPPER_LIMIT"),
  ERROR_UNKNOWN(6, "ERROR_UNKNOWN");

  private final int val;
  private final String message;

  VastParserErrorCode(int val, String message) {
    this.val = val;
    this.message = message;
  }

  public int getValue() {
    return val;
  }

  public String getMessage() {
    return message;
  }

  /**
   * Given a error code value, return the error code enum value.
   *
   * @param val   {@code int} integer error code
   * @return      {@link VastParserErrorCode} error code enum value
   */
  public static VastParserErrorCode getByVal(final int val) {
    for (VastParserErrorCode code : VastParserErrorCode.values()) {
      if (code.val == val) {
        return code;
      }
    }
    return ERROR_UNKNOWN;
  }
}
