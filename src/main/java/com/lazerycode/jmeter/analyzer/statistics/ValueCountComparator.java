package com.lazerycode.jmeter.analyzer.statistics;

import java.io.Serializable;
import java.util.Comparator;

/**
 * Compares {@link ValueCount} by value
 *
 * @author Peter Kaul
 */
class ValueCountComparator implements Comparator<ValueCount>, Serializable {

  private static final long serialVersionUID = -3567044398536300166L;

  @Override
  public int compare(ValueCount vc1, ValueCount vc2) {
    long v1 = vc1.getValue();
    long v2 = vc2.getValue();

    return v1 < v2 ? -1 : (v1 == v2 ? 0 : 1);
  }
}
