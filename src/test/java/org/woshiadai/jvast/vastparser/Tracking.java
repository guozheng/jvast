package org.woshiadai.jvast.vastparser;

public class Tracking {
  private String value;
  private TrackingEventsType event;

  public String getValue() {
    return value;
  }

  public void setValue(String value) {
    this.value = value;
  }

  public TrackingEventsType getEvent() {
    return event;
  }

  public void setEvent(TrackingEventsType event) {
    this.event = event;
  }

  @Override
  public String toString() {
    return "Tracking [event=" + event + ", value=" + value + "]";
  }
}
