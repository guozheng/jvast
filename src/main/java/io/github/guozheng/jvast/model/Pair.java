package io.github.guozheng.jvast.model;

import java.util.Objects;
import lombok.Getter;


public class Pair<A, B> {
  @Getter
  public final A first;
  @Getter
  public final B second;

  public Pair(A first, B second) {
    this.first = first;
    this.second = second;
  }

  public static <A, B> Pair<A, B> of(A first, B second) {
    return new Pair(first, second);
  }

  public String toString() {
    return "Pair[" + this.first + "," + this.second + "]";
  }

  /**
   * equality check.
   *
   * @param other other object
   * @return whether two vars equal or not
   */
  public boolean equals(Object other) {
    return other instanceof Pair
        && Objects.equals(this.first, ((Pair) other).first)
        && Objects.equals(this.second, ((Pair) other).second);
  }

  /**
   * Hash code of the object.
   *
   * @return int representation of the object
   */
  public int hashCode() {
    return this.first == null
        ? (this.second == null ? 0 : this.second.hashCode() + 1)
        : (this.second
            == null ? this.first.hashCode() + 2 : this.first.hashCode() * 17 + this.second.hashCode());
  }
}
