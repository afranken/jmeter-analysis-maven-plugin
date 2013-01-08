package com.lazerycode.jmeter.analyzer.statistics;

/**
 * Manages the count for a long value.
 *
 * @author Peter Kaul
 */
class ValueCount {
  private long value;
  private long count;

  public ValueCount(final long value) {
    this.value = value;
  }

  public void increment() {
    count++;
  }

  public long getCount() {
    return count;
  }

  public long getValue() {
    return value;
  }
}
