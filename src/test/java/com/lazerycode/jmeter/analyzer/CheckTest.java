/**
 *
 */
package com.lazerycode.jmeter.analyzer;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Test check class
 */
public class CheckTest {

    private Check check;

    @Before
    public void init() {
        check = new Check();
    }

    @Test
    public void testCheckDefault() {
        Assert.assertEquals(-1, check.getThreshold(), 0);
        Assert.assertEquals(5, check.getTolerance(), 0);
        Assert.assertNull(check.valid(20));
    }

    @Test
    public void testCheckUpper() {
        check.setThreshold(2000);
        check.setToleranceDirection(Check.ToleranceDirection.UPPER.toString());
        Assert.assertEquals(2000, check.getMinValue(), 0);
        Assert.assertEquals(Double.MAX_VALUE, check.getMaxValue(), 0);
        Assert.assertTrue(check.valid(2000));
        Assert.assertTrue(check.valid(2001));
        Assert.assertTrue(check.valid(2100));
        Assert.assertTrue(check.valid(2101));
        Assert.assertTrue(check.valid(Double.MAX_VALUE));
        Assert.assertFalse(check.valid(1999));
        Assert.assertFalse(check.valid(1900));
        Assert.assertFalse(check.valid(1899));
        Assert.assertFalse(check.valid(0));
    }

    @Test
    public void testCheckLower() {
        check.setThreshold(2000);
        check.setToleranceDirection(Check.ToleranceDirection.LOWER.toString());
        Assert.assertEquals(0, check.getMinValue(), 0);
        Assert.assertEquals(2000, check.getMaxValue(), 0);
        Assert.assertTrue(check.valid(2000));
        Assert.assertTrue(check.valid(1999));
        Assert.assertTrue(check.valid(1900));
        Assert.assertTrue(check.valid(1899));
        Assert.assertTrue(check.valid(0));
        Assert.assertFalse(check.valid(2001));
        Assert.assertFalse(check.valid(2100));
        Assert.assertFalse(check.valid(2101));
        Assert.assertFalse(check.valid(Double.MAX_VALUE));
    }

    @Test
    public void testCheckUpperTolerance() {
        check.setThreshold(2000);
        check.setToleranceDirection(Check.ToleranceDirection.UPPER_TOLERANCE.toString());
        Assert.assertEquals(2000, check.getMinValue(), 0);
        Assert.assertEquals(2100, check.getMaxValue(), 0);
        Assert.assertTrue(check.valid(2000));
        Assert.assertTrue(check.valid(2001));
        Assert.assertTrue(check.valid(2100));
        Assert.assertFalse(check.valid(2101));
        Assert.assertFalse(check.valid(Double.MAX_VALUE));
        Assert.assertFalse(check.valid(1999));
        Assert.assertFalse(check.valid(1900));
        Assert.assertFalse(check.valid(1899));
        Assert.assertFalse(check.valid(0));
    }

    @Test
    public void testCheckLowerTolerance() {
        check.setThreshold(2000);
        check.setToleranceDirection(Check.ToleranceDirection.LOWER_TOLERANCE.toString());
        Assert.assertEquals(1900, check.getMinValue(), 0);
        Assert.assertEquals(2000, check.getMaxValue(), 0);
        Assert.assertTrue(check.valid(2000));
        Assert.assertTrue(check.valid(1999));
        Assert.assertTrue(check.valid(1900));
        Assert.assertFalse(check.valid(2001));
        Assert.assertFalse(check.valid(2100));
        Assert.assertFalse(check.valid(2101));
        Assert.assertFalse(check.valid(Double.MAX_VALUE));
        Assert.assertFalse(check.valid(1899));
        Assert.assertFalse(check.valid(0));
    }

    @Test
    public void testCheckUpperLowerTolerance() {
        check.setThreshold(2000);
        check.setToleranceDirection(Check.ToleranceDirection.UPPER_LOWER_TOLERANCE.toString());
        Assert.assertEquals(1900, check.getMinValue(), 0);
        Assert.assertEquals(2100, check.getMaxValue(), 0);
        Assert.assertTrue(check.valid(2000));
        Assert.assertTrue(check.valid(1999));
        Assert.assertTrue(check.valid(1900));
        Assert.assertTrue(check.valid(2001));
        Assert.assertTrue(check.valid(2100));
        Assert.assertFalse(check.valid(2101));
        Assert.assertFalse(check.valid(Double.MAX_VALUE));
        Assert.assertFalse(check.valid(1899));
        Assert.assertFalse(check.valid(0));
    }

    @Test
    public void testCheckUpperLowerToleranceWithNewTolerance() {
        check.setThreshold(2000);
        check.setTolerance(10);
        check.setToleranceDirection(Check.ToleranceDirection.UPPER_LOWER_TOLERANCE.toString());
        Assert.assertEquals(1800, check.getMinValue(), 0);
        Assert.assertEquals(2200, check.getMaxValue(), 0);
        Assert.assertTrue(check.valid(2000));
        Assert.assertTrue(check.valid(1999));
        Assert.assertTrue(check.valid(1900));
        Assert.assertTrue(check.valid(2001));
        Assert.assertTrue(check.valid(2100));
        Assert.assertTrue(check.valid(2101));
        Assert.assertFalse(check.valid(Double.MAX_VALUE));
        Assert.assertTrue(check.valid(1899));
        Assert.assertFalse(check.valid(0));
    }

    @Test
    public void testCheckEquals() {
        check.setThreshold(2000);
        check.setToleranceDirection(Check.ToleranceDirection.EQUALS.toString());
        Assert.assertEquals(2000, check.getMinValue(), 0);
        Assert.assertEquals(2000, check.getMaxValue(), 0);
        Assert.assertTrue(check.valid(2000));
        Assert.assertFalse(check.valid(1999));
        Assert.assertFalse(check.valid(1900));
        Assert.assertFalse(check.valid(2001));
        Assert.assertFalse(check.valid(2100));
        Assert.assertFalse(check.valid(2101));
        Assert.assertFalse(check.valid(Double.MAX_VALUE));
        Assert.assertFalse(check.valid(1899));
        Assert.assertFalse(check.valid(0));
    }

    @Test(expected=IllegalArgumentException.class)
    public void testCheckWrongValue() {
        check.setThreshold(2000);
        check.valid(-1);
    }

}
