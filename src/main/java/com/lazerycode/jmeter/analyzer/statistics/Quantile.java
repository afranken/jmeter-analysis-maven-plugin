package com.lazerycode.jmeter.analyzer.statistics;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * A Q-Quantile
 * see http://en.wikipedia.org/wiki/Quantile
 *
 * @author Dennis Homann, Arne Franken
 */
public class Quantile {

  private static final int QUANTILE_MAX = 10000;
  
  private List<ValueCount> counts;
  private int q;
  private long totalCount;

  /**
   * Creates a Quantile with the grade/resolution q using counts as values
   *
   * For example:
   * <ul>
   *   <li>Quantile is created with q=100, then one could call #getQuantile with k=99 for the 99th quantile</li>
   *   <li>Quantile is created with q=1000, then one could call #getQuantile with k=990 for the 99th quantile</li>
   *   <li>Quantile is created with q=10000, then one could call #getQuantile with k=9900 for the 99th quantile</li>
   * </ul>
   *
   * @param q the grade/resolution
   * @param counts the values
   */
  protected Quantile(int q, Collection<ValueCount> counts) {

    if (q < 2 || q > QUANTILE_MAX) {
      throw new IllegalArgumentException(q + " is not a valid q value to create q-quantiles");
    }

    // sort the values for internal reasons
    ArrayList<ValueCount> sorted = new ArrayList<ValueCount>(counts);
    Collections.sort(sorted, new ValueCountComparator());
    this.q = q;
    this.counts = sorted;

    for (ValueCount count : sorted) {
      totalCount += count.getCount();
    }
  }

  /**
   * Get the count for the given Quantile population.
   * All
   *
   * @param k the population
   *
   * @return the value matching the population
   */
  public long getQuantile(int k) {

    if (k <= 0 || k > q) {
      throw new IllegalArgumentException("k must be a positive integer less than " + q);
    }

    int i = 0;
    long n = totalCount * k / q;
    long v = 0;

    for (ValueCount count : counts) {
      if (i >= n) {
        return v;
      }
      v = count.getValue();
      i += count.getCount();
    }

    return v;
  }

  /**
   * @return the grade/resolution the Quantile was created with
   */
  public int getGrade() {
    return q;
  }

}
