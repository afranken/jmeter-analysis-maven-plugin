package com.lazerycode.jmeter.analyzer.statistics;

import junit.framework.TestCase;
import java.util.Arrays;

/**
 * Tests {@link Samples}
 */
public class SamplesTest extends TestCase {


  public void testAggregate() {

    Samples samples = new Samples(4, false);

    samples.addSample(10, 2);
    samples.addSample(20, 4);
    samples.addSample(30, 6);
    samples.addSample(40, 8);

    // first compression will take place here
    // result is: (15, 3) and (35, 7) with compression 2

    // these samples will be aggregated to (55, 11)
    samples.addSample(50, 10);
    samples.addSample(60, 12);


    // these samples will be aggregated to (75, 15)
    samples.addSample(70, 14);
    samples.addSample(80, 16);

    samples.finish();


    assertEquals("samples", Arrays.asList(3L, 7L, 11L, 15L), samples.getSamples());
    assertEquals("timestamps", Arrays.asList(15L, 35L, 55L, 75L), samples.getTimestamps());
  }

  public void testAggregate2() {

    Samples samples = new Samples(2, false);

    samples.addSample(10, 2);
    samples.addSample(20, 4);
    samples.addSample(30, 6);
    samples.addSample(40, 8);

    // first compression has taken place
    // result is: (15, 3) and (35, 7). compression=2

    samples.addSample(50, 10);

    // second compression has taken place
    // result is: (25, 5) and a pending element. compression=4

    samples.addSample(60, 12);
    samples.addSample(100, 200);


    samples.finish();


    assertEquals("samples", Arrays.asList(5L, 222/3L), samples.getSamples());
    assertEquals("timestamps", Arrays.asList(25L, 75L), samples.getTimestamps());
  }

    
    

    public void testSmallHistogram() {
        Samples samples = createWithSamples(new int[]{1, 3, 4, 1, 1, 2, 2, 4, 5, 6});

        Quantile quantiles = samples.getQuantiles(10);
        assertQuantile(quantiles, 1, 1);
        assertQuantile(quantiles, 2, 1);
        assertQuantile(quantiles, 3, 1);
        assertQuantile(quantiles, 4, 2);
        assertQuantile(quantiles, 5, 2);
        assertQuantile(quantiles, 6, 3);
        assertQuantile(quantiles, 7, 4);
        assertQuantile(quantiles, 8, 4);
        assertQuantile(quantiles, 9, 5);
        assertQuantile(quantiles, 10, 6);
    }

    public void testNoSamples() {

        Samples samples = createWithSamples(new int[] {});        
        Quantile quantiles = samples.getQuantiles(10);
        for (int i = 1; i <= 10; i++) {
            assertQuantile(quantiles, i, 0);
        }
    }


    public void test99Point9Quantile() {
        
        Samples testling = new Samples(10000, true);
        
        // add 10.000 samples, of which 0.5% have value 25, all other ones are 0
        // the 99% quantile should be 0, the 99.9% quantile should be 25
        final int total = 10000;
        final int highValue = 50;

        for (int i = 0; i < total-highValue; i++) {
            testling.addSample(0, 0);
        }
        for (int i = 0; i < highValue; i++) {
            testling.addSample(0, 25);
        }
        testling.finish();

        Quantile quantiles = testling.getQuantiles(1000);
        // the 99% quantile
        assertQuantile(quantiles, 990, 0);
        // the 99.9% quantile
        assertQuantile(quantiles, 999, 25);
    }

    private void assertQuantile(Quantile quantiles, int k, int expectedValue) {
        assertEquals(String.format("k-th %d-quantile (k = %d) is expected to be %d", quantiles.getGrade(), k, expectedValue),
                expectedValue, quantiles.getQuantile(k));
    }

    private Samples createWithSamples(int[] samples) {
        Samples result = new Samples(10000, true);
        for (int sample : samples) {
            result.addSample(0, (long) sample);
        }
        result.finish();
        
        return result;
    }    

}
