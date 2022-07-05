package io.github.guozheng.jvast.model;

import java.util.Objects;
import lombok.Getter;


/**
 * A util class representing a pair of objects.
 * @param <A>
 * @param <B>
 */
public class Pair<A, B> {
  @Getter
  public final A first; // first object
  @Getter
  public final B second; // second object

  public Pair(A first, B second) {
    this.first = first;
    this.second = second;
  }

  /**
   * Factory method to create a pair given two objects
   * @param first       {@link A} first object
   * @param second      {@link B} second object
   * @return            {@link Pair} result pair object
   * @param <A>
   * @param <B>
   */
  public static <A, B> Pair<A, B> of(A first, B second) {
    return new Pair(first, second);
  }

  /**
   * Convert the pair object into string form.
   * @return          {@link String} string form of the pair object.
   */
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
