package com.lazerycode.jmeter.analyzer.statistics;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Unit test for {@link ValueCount}.
 */
public class ValueCountTest {

  @Test
  public void testValueCount() {
    ValueCount vc = new ValueCount(11);
    assertEquals(11L, vc.getValue());
    assertEquals(0, vc.getCount());

    vc.increment();
    assertEquals(1, vc.getCount());
  }

}
