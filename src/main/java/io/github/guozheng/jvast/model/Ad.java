package io.github.guozheng.jvast.model;

import java.util.List;
import lombok.Builder;
import lombok.Data;
import lombok.ToString;

/**
 * This class represents data from Ad XML element.
 */
@Data
@Builder(toBuilder = true)
@ToString(callSuper = true, includeFieldNames = true)
public class Ad {
  //Ad XML element string value, e.g. <Ad...>...</Ad>
  private StringBuilder content;

  //Ad type that includes template such as VAST, VMAP and version such as 1.0, 2.0, etc.
  private AdTypeVersion type;

  private String adId;

  private List<String> creativeIds;

}
