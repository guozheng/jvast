package jvast.model;

/**
 * Video ad type that includes template and specification version.
 */
public enum AdTypeVersion {
  UNKNOWN("UNKNOWN", "0.0"),
  VAST_2_0("VAST", "2.0"),
  VAST_3_0("VAST", "3.0"),
  VAST_4_0("VAST", "4.0"),
  VAST_4_1("VAST", "4.1"),
  VMAP_1_0("VMAP", "1.0");

  private String type;
  private String version;

  AdTypeVersion(String type, String version) {
    this.type = type;
    this.version = version;
  }

  public String getType() {
    return type;
  }

  public String getVersion() {
    return version;
  }

  public static AdTypeVersion fromTypeAndVersion(String type, String version) {
    if (type.equalsIgnoreCase("VAST")) {
      switch (version) {
        case "2.0":
          return VAST_2_0;
        case "3.0":
          return VAST_3_0;
        case "4.0":
          return VAST_4_0;
        case "4.1":
          return VAST_4_1;
        default:
          return VAST_2_0;
      }
    } else if (type.equalsIgnoreCase("VMAP")) {
      switch (version) {
        case "1.0":
          return VMAP_1_0;
        default:
          return VMAP_1_0;
      }
    } else {
      return UNKNOWN;
    }
  }

}
