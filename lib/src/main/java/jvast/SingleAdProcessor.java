package jvast;

import jvast.model.VideoAdType;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class SingleAdProcessor {
  private static final Logger LOGGER = LogManager.getLogger(SingleAdProcessor.class);
  private StringBuilder vast;
  private VideoAdType type;

  SingleAdProcessor(StringBuilder vast, VideoAdType type) {
    this.vast = vast;
    this.type = type;
  }

  public void process() {
    //break into multiple Ad elements with prefix and postfix strings

    //process each Ad element, e.g. insert pixels

    //construct the video ad again
  }


}
