package jvast.data;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Pixel {
  private PixelElementType type;
  private String url;
}
