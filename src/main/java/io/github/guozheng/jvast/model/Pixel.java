package io.github.guozheng.jvast.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
/**
 * The class representing a pixel.
 */
public class Pixel {
  private PixelElementType type;
  private String url;
}
