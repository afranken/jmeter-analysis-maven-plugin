package com.lazerycode.jmeter.analyzer.statistics;

import java.util.*;

/**
 * Collects samples (as a sequence of values of type "long") and provides the following values:
 *
 * <ul>
 *   <li>min: Minimum value</li>
 *   <li>max: Maximum value</li>
 *   <li>average: Average value</li>
 *   <li>standard deviation</li>
 *   <li>samples per second</li>
 *   <li>quantiles</li>
 * </ul>
 *
 * @author Dennis Homann, Arne Franken, Peter Kaul
 */
public class Samples {

  private static final float SECOND = 1000f;

  // number or error samples
  private long errors = 0;

  // number of success samples
  private long success = 0;

  // collected samples
  private List<Long> samples = new ArrayList<Long>();
  // timestamps corresponding to samples
  private List<Long> timestamps = new ArrayList<Long>();

  // minimum sample timestamp
  private long minTimestamp = Long.MAX_VALUE;
  // maximum sample timestamp
  private long maxTimestamp = Long.MIN_VALUE;

  // minimum sample value
  private long min = Long.MAX_VALUE;
  // maximum sample value
  private long max = Long.MIN_VALUE;
  private volatile boolean finished = false;

  // current number of samples which are aggregated into a single sample
  private int compression = 1;

  // maximum number of samples to store
  private final int maxSamplesCount;

  private List<Long> samplesBuffer = new ArrayList<Long>();
  private List<Long> timestampsBuffer = new ArrayList<Long>();


  private double total = 0;
  // sum of all values each powered by 2
  private double totalPowered2 = 0;
  private long standardDeviation;

  // The value histogram
  private Map<Long, ValueCount> histogram;


  // ----------------------

  /**
   * Creates a new instance where a limited number of samples is stored internally.
   * If more samples are added, existing samples will be compressed internally.
   *
   * @param maxSamples The maximum number of samples. 0=store no samples
   * @param histogram If set to true the a value histogram should be counted as well.
   */
  public Samples(int maxSamples, boolean histogram) {
    this.maxSamplesCount = maxSamples;
    if( histogram ) {
      this.histogram = new HashMap<Long, ValueCount>();
    }
  }

  /**
   * Adds an "error" sample. An error sample isn't used for statistics values such as average, ...
   *
   * @param timestamp The timestamp of the sample. It's assumed that a timestamp is greater or equal than the previous one
   */
  public void addError(long timestamp) {

    assertNotFinished();
    errors++;

    setTimestamp(timestamp);

  }

  /**
   * Adds a "success" sample
   *
   * @param timestamp The timestamp of the sample. It's assumed that a timestamp is greater or equal than the previous one
   * @param value The sample value, e.g. the response duration or response bytes
   *
   * @see #addError(long)
   */
  public void addSample(long timestamp, long value) {

    assertNotFinished();

    //  handle counters/statistics
    success++;

    total += value;
    totalPowered2 += Math.pow(value, 2);

    //set min / max value
    if( value > max ) {
      max = value;
    }
    if( value < min ) {
      min = value;
    }

    //set min / max timestamp
    setTimestamp(timestamp);

    //collect the value
    if( histogram != null ) {

      ValueCount count = histogram.get(value);
      if( count == null ) {
        //there was no other request with the same response value, collect
        count = new ValueCount(value);
        histogram.put(value, count);
      }

      //there already was another request with the same response value, increment
      count.increment();
    }

    // store sample
    add(timestamp, value);
  }

  /**
   * Marks collecting samples as "finished"
   */
  public void finish() {

    finished = true;

    // flush buffer by adding remaining items
    if( samplesBuffer.size() > 0 ) {

      addAggregated(samplesBuffer, timestampsBuffer);
      samplesBuffer.clear();
      timestampsBuffer.clear();
    }

    // adjust some statistics

    // Standard Deviation: http://en.wikipedia.org/wiki/Standard_deviation#Rapid_calculation_methods
    double totalPowered0 = success;
    double totalPowered1 = total;
    standardDeviation = (long) (Math.sqrt(totalPowered0 * totalPowered2 - Math.pow(totalPowered1, 2)) /  totalPowered0);


    // protect collected data against modification
    samples = Collections.unmodifiableList(samples);
    timestamps = Collections.unmodifiableList(timestamps);
  }

  /**
   * @return Determines whether a relevant number of samples have been provided so that statistics can be computed
   */
  public boolean hasSamples() {
    return getSuccessCount() > 0;
  }


  public List<Long> getSamples() {
    assertFinished();
    return samples;
  }

  /**
   * @return The sample's timestamp.
   */
  public List<Long> getTimestamps() {
    assertFinished();
    return timestamps;
  }


  /**
   * @return Total number of stored samples
   * @see #getSuccessCount()
   */
  public long getStoredSamplesCount() {
    assertFinished();
    return samples.size();
  }

  /**
   * @return Total number of successful samples
   */
  public long getSuccessCount() {
    assertFinished();
    return success;
  }

  /**
   * @return Total number of samples having an error
   */
  public long getErrorsCount() {
    assertFinished();
    return errors;
  }

  public long getMin() {
    assertFinished();
    if( !hasSamples() ) {
      throw new IllegalStateException("No samples");
    }
    return min;
  }

  public long getMax() {
    assertFinished();
    if( !hasSamples() ) {
      throw new IllegalStateException("No samples");
    }
    return max;
  }

  /**
   * @return The average for all samples
   */
  public long getAverage() {
    assertFinished();
    long count = getSuccessCount();
    if( count == 0 ) {
      throw new IllegalStateException("No samples");
    }
    return (long) total / count;
  }

  public long getTotal() {
    assertFinished();
    return (long)total;
  }

  public long getStandardDeviation() {
    return standardDeviation;
  }

  public long getMaxTimestamp() {
    assertFinished();
    return maxTimestamp;
  }

  public long getMinTimestamp() {
    assertFinished();
    return minTimestamp;
  }

  /**
   * @return Number of successful samples per second
   */
  public long getSuccessPerSecond() {

    assertFinished();
    long duration = getDuration();
    if( duration == 0 ) {
      return 0; // shouldn't happen
    }

    return getSuccessCount() / duration;
  }

  /**
   * @return The duration in s
   */
  public long getDuration() {
    assertFinished();
    return Math.round((getMaxTimestamp()-getMinTimestamp()) / SECOND);
  }

  /**
   * Returns a Quantile with the grade/resolution q using counts as values
   *
   * @param q the grade
   *
   * @return the q-quantile.
   */
  public Quantile getQuantiles(int q) {
    assertFinished();

    if( histogram == null ) {
      throw new IllegalStateException("No histogram available");
    }

    return new Quantile(q, histogram.values());
  }

  //====================================================================================================================



  private void assertNotFinished() {
    if( finished ) {
      throw new IllegalStateException("Already finished");
    }
  }

  private void assertFinished() {
    if( !finished ) {
      throw new IllegalStateException("Not finished");
    }
  }

  /**
   * set min / max timestamp
   * @param timestamp the timestamp
   */
  private void setTimestamp(long timestamp) {

    if( timestamp < minTimestamp ) {
      minTimestamp = timestamp;
    }
    if( timestamp > maxTimestamp ) {
      maxTimestamp = timestamp;
    }

  }


  /**
   * Collect timestamp and value
   */
  private void add(long timestamp, long value) {

    if( maxSamplesCount == 0 ) {
      return;
    }

    // Disabling the compression if maxSamplesCount < 0
    if (maxSamplesCount > 0) {
      // check whether the maximum of samples is reached and reduce number of samples if necessary
      if( samples.size() >= maxSamplesCount ) {

        // compress
        halve();
        compression *= 2;
      }
    }

    // add current sample
    if( compression == 1 ) {

      // store samples
      samples.add(value);
      timestamps.add(timestamp);
    }
    else {

      // buffer samples for aggregation
      samplesBuffer.add(value);
      timestampsBuffer.add(timestamp);
      if( samplesBuffer.size() >= compression ) {

        // we have collected enough items
        addAggregated(samplesBuffer, timestampsBuffer);
        samplesBuffer.clear();
        timestampsBuffer.clear();
      }
    }
  }

  /**
   * Aggregates samples and timestamps and add them as a single item to samples/timestamp
   *
   * @param samplesBuffer  samples to be aggregated. will be cleared
   * @param timestampsBuffer timestamps to be aggregated
   */
  private void addAggregated(List<Long> samplesBuffer, List<Long> timestampsBuffer) {

    long firstTimestamp = timestampsBuffer.get(0);
    long lastTimestamp = timestampsBuffer.get(timestampsBuffer.size()-1);
    long aggregatedTimestamp = firstTimestamp + (lastTimestamp-firstTimestamp) / 2;


    long aggregatedSample = 0;
    for( long sample : samplesBuffer ) {
      aggregatedSample += sample;
    }
    aggregatedSample = aggregatedSample / samplesBuffer.size();

    samples.add(aggregatedSample);
    timestamps.add(aggregatedTimestamp);
  }

  /**
   * Cuts a list of samples in half by aggregating pairs a samples
   */
  private void halve() {

    List<Long> newSamples = new ArrayList<Long>();
    List<Long> newTimestamps = new ArrayList<Long>();

    Iterator<Long> si = samples.iterator();
    Iterator<Long> ti = timestamps.iterator();

    while( si.hasNext() ) {

      long sample = si.next();
      long timestamp = ti.next();


      if( !si.hasNext() ) {

        // there is no second sample. thus, don't aggregate this last element
        newSamples.add(sample);
        newTimestamps.add(timestamp);
      }
      else {

        long secondTimestamp = ti.next();
        long secondSample = si.next();

        long aggregatedSample = (sample+secondSample) / 2;
        long aggregatedTimestamp = (timestamp+(secondTimestamp-timestamp) / 2);

        newSamples.add(aggregatedSample);
        newTimestamps.add(aggregatedTimestamp);
      }
    }


    samples = newSamples;
    timestamps = newTimestamps;
  }

}
