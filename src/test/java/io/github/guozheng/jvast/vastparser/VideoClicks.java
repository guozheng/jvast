package io.github.guozheng.jvast.vastparser;

import java.util.ArrayList;
import java.util.List;
import io.github.guozheng.jvast.util.StringUtil;

public class VideoClicks {
  private String clickThrough;
  private List<String> clickTracking;
  private List<String> customClick;

  public String getClickThrough() {
    return clickThrough;
  }

  public void setClickThrough(String clickThrough) {
    this.clickThrough = clickThrough;
  }

  /**+
   * .
   * @return List<String></String>
   */

  public List<String> getClickTracking() {
    if (clickTracking == null) {
      clickTracking = new ArrayList<String>();
    }
    return this.clickTracking;
  }

  /**+
   * .
   * @return List<String></String>
   */
  public List<String> getCustomClick() {
    if (customClick == null) {
      customClick = new ArrayList<String>();
    }
    return this.customClick;
  }

  @Override
  public String toString() {
    return "VideoClicks [clickThrough=" + clickThrough
            + ", clickTracking=[" + listToString(clickTracking)
            + "], customClick=[" + listToString(customClick) + "] ]";
  }

  private String listToString(List<String> list) {
    StringBuilder sb = new StringBuilder();

    if (list == null) {
      return StringUtil.EMPTY;
    }
    for (int x = 0; x < list.size(); x++) {
      sb.append(list.get(x).toString());
    }
    return sb.toString();
  }
}
