package io.github.guozheng.jvast.vastparser;

public enum VastDocElements {
  VASTVERSION("3.0"),
  VASTS("VASTS"),
  VASTADTAGURI("VASTAdTagURI"),
  VASTVERSIONATTRIBUTE("version");

  private String value;

  private VastDocElements(String value) {
    this.value = value;

  }

  public String getValue() {
    return value;
  }
}
