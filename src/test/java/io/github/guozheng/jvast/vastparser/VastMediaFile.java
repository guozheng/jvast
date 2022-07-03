package io.github.guozheng.jvast.vastparser;

import java.math.BigInteger;

public class VastMediaFile {
  private String value;
  private String id;
  private String delivery;
  private String type;
  private BigInteger bitrate;
  private BigInteger minBitrate;
  private BigInteger maxBitrate;
  private BigInteger width;
  private BigInteger height;
  private Boolean scalable;
  private Boolean maintainAspectRatio;
  private String apiFramework;
  private String codec;

  public String getValue() {
    return value;
  }

  public void setValue(String value) {
    this.value = value;
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getDelivery() {
    return delivery;
  }

  public void setDelivery(String delivery) {
    this.delivery = delivery;
  }

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public BigInteger getBitrate() {
    return bitrate;
  }

  public void setBitrate(BigInteger bitrate) {
    this.bitrate = bitrate;
  }

  public BigInteger getMinBitrate() {
    return minBitrate;
  }

  public void setMinBitrate(BigInteger minBitrate) {
    this.minBitrate = minBitrate;
  }

  public BigInteger getMaxBitrate() {
    return maxBitrate;
  }

  public void setMaxBitrate(BigInteger maxBitrate) {
    this.maxBitrate = maxBitrate;
  }

  public BigInteger getWidth() {
    return width;
  }

  public void setWidth(BigInteger width) {
    this.width = width;
  }

  public BigInteger getHeight() {
    return height;
  }

  public void setHeight(BigInteger height) {
    this.height = height;
  }

  public Boolean getScalable() {
    return scalable;
  }

  public void setScalable(Boolean scalable) {
    this.scalable = scalable;
  }

  public Boolean getMaintainAspectRatio() {
    return maintainAspectRatio;
  }

  public void setMaintainAspectRatio(Boolean maintainAspectRatio) {
    this.maintainAspectRatio = maintainAspectRatio;
  }

  public String getApiFramework() {
    return apiFramework;
  }

  public void setApiFramework(String apiFramework) {
    this.apiFramework = apiFramework;
  }

  public String getCodec() {
    return codec;
  }

  public void setCodec(String codec) {
    this.codec = codec;
  }

  @Override
  public String toString() {
    return new StringBuilder()
      .append("MediaFile [value=")
      .append(value)
      .append(", id=")
      .append(id)
      .append(", delivery=")
      .append(delivery)
      .append(", type=")
      .append(type)
      .append(", bitrate=")
      .append(bitrate)
      .append(", minBitrate=")
      .append(minBitrate)
      .append(", maxBitrate=")
      .append(maxBitrate)
      .append(", width=")
      .append(width)
      .append(", height=")
      .append(height)
      .append(", scalable=")
      .append(scalable)
      .append(", maintainAspectRatio=")
      .append(maintainAspectRatio)
      .append(", apiFramework=")
      .append(apiFramework)
      .append(", codec=")
      .append(codec)
      .append("]").toString();
  }
}
