package com.lazerycode.jmeter.analyzer.writer;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.lazerycode.jmeter.analyzer.parser.AggregatedResponses;
import com.lazerycode.jmeter.analyzer.parser.StatusCodes;
import com.lazerycode.jmeter.analyzer.statistics.Quantile;
import com.lazerycode.jmeter.analyzer.statistics.Samples;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Abstract base for {@link Writer} tests
 */
public final class WriterTestHelper {

  private static final SimpleDateFormat LOCAL_DATE_FORMAT = new SimpleDateFormat("yyyyMMdd'T'HHmmssZ", Locale.getDefault());

  public static Map<String, AggregatedResponses> getMockedTestResults() throws Exception {
    String key = "warmup";

    //---- mock aggregatedResponse and related objects ----

    //statuscodes
    StatusCodes statusCodes = mock(StatusCodes.class);
    when(statusCodes.getCodes()).thenReturn(ImmutableMap.of(200, 36049L, 403, 9L, 404, 10L));

    //quantile
    Quantile quantile = mock(Quantile.class);
    when(quantile.getQuantile(100)).thenReturn(0L);
    when(quantile.getQuantile(200)).thenReturn(0L);
    when(quantile.getQuantile(300)).thenReturn(0L);
    when(quantile.getQuantile(400)).thenReturn(0L);
    when(quantile.getQuantile(500)).thenReturn(1L);
    when(quantile.getQuantile(600)).thenReturn(1L);
    when(quantile.getQuantile(700)).thenReturn(1L);
    when(quantile.getQuantile(800)).thenReturn(1L);
    when(quantile.getQuantile(900)).thenReturn(1L);
    when(quantile.getQuantile(990)).thenReturn(6L);
    when(quantile.getQuantile(999)).thenReturn(19L);
    when(quantile.getQuantile(1000)).thenReturn(1352L);

    //duration
    Samples duration = mock(Samples.class);
    when(duration.getDuration()).thenReturn(30L);
    when(duration.getSuccessCount()).thenReturn(36049L);
    when(duration.getSuccessPerSecond()).thenReturn(1201L);

    when(duration.getMin()).thenReturn(0L);
    when(duration.getAverage()).thenReturn(0L);
    when(duration.getMax()).thenReturn(1352L);
    when(duration.getStandardDeviation()).thenReturn(7L);

    when(duration.getQuantiles(1000)).thenReturn(quantile);

    List<Long> samples = new ArrayList<Long>();
    samples.add(12L);
    samples.add(15L);
    samples.add(231L);
    samples.add(231L);
    samples.add(2312L);
    when(duration.getSamples()).thenReturn(samples);

    List<Long> timestamps = new ArrayList<Long>();
    timestamps.add(1324043709785L);
    timestamps.add(1324043709786L);
    timestamps.add(1324043709787L);
    timestamps.add(1324043709788L);
    timestamps.add(1324043709789L);
    when(duration.getTimestamps()).thenReturn(timestamps);

    //size
    Samples size = mock(Samples.class);
    when(size.getTotal()).thenReturn(750210890L);
    when(size.getMin()).thenReturn(20480L);
    when(size.getAverage()).thenReturn(20810L);
    when(size.getMax()).thenReturn(53890L);
    when(size.getStandardDeviation()).thenReturn(3308L);


    //duration
    Samples activeThreads = mock(Samples.class);
    when(activeThreads.getDuration()).thenReturn(30L);
    when(activeThreads.getSuccessCount()).thenReturn(36049L);
    when(activeThreads.getSuccessPerSecond()).thenReturn(1201L);

    when(activeThreads.getMin()).thenReturn(0L);
    when(activeThreads.getAverage()).thenReturn(0L);
    when(activeThreads.getMax()).thenReturn(1352L);
    when(activeThreads.getStandardDeviation()).thenReturn(7L);

    when(activeThreads.getQuantiles(1000)).thenReturn(quantile);

    //aggregatedResponse
    AggregatedResponses value = mock(AggregatedResponses.class);
    when(value.getStartDate()).thenReturn(new Date(1324043709785L));
    when(value.getEndDate()).thenReturn(new Date(1324043739766L));

    when(value.getStatusCodes()).thenReturn(statusCodes);
    when(value.getSize()).thenReturn(size);
    when(value.getSizeByUri()).thenReturn(ImmutableMap.of(key, size));
    when(value.getDuration()).thenReturn(duration);
    when(value.getActiveThreads()).thenReturn(activeThreads);
    when(value.getDurationByUri()).thenReturn(ImmutableMap.of(key, duration));
    when(value.getUriByStatusCode()).thenReturn(ImmutableMap.<Integer, Set<String>>of(200, ImmutableSet.of("http://success.com", "http://anotherSuccess.com"), 403, ImmutableSet.<String>of("http://forbidden.com"), 404, ImmutableSet.<String>of("http://notFound.com","http://againNotFound.com")));

    //--- add aggregatedResults as testresult
    return ImmutableMap.of(key, value);
  }

  /**
   * Strip line ends from String contents of a file so that contents can be compared on different platforms.
   *
   * @return normalized String
   * @throws IOException
   */
  public static String normalizeFileContents(File file) throws IOException, ParseException {

    String content = FileUtils.readFileToString(file, "UTF-8");

    return normalizeFileContents(content);
  }

  /**
   * Strip line ends from String contents so that contents can be compared on different platforms.
   *
   * @return normalized String
   * @throws java.io.IOException
   */
  public static String normalizeFileContents(String content) throws IOException, ParseException {

    //replace line endings
    content = content.replaceAll("(\\r\\n|\\r|\\n)", "");

    //replace date with date converted to the local timezone
    Pattern datePattern = Pattern.compile("\\d\\d\\d\\d\\d\\d\\d\\dT\\d\\d\\d\\d\\d\\d\\+\\d\\d\\d\\d");
    Matcher matcher = datePattern.matcher(content);
    StringBuffer sb = new StringBuffer();
    while (matcher.find()) {
      matcher.appendReplacement(sb, toLocal(matcher.group()));
    }
    matcher.appendTail(sb);

    return sb.toString();
  }

  public static String toLocal(String dateString) throws ParseException {
    return toLocal(parseDate(dateString));
  }

  //====================================================================================================================

  private static Date parseDate(String dateString) throws ParseException {
    return LOCAL_DATE_FORMAT.parse(dateString);
  }

  private static String toLocal(Date date) {
    return LOCAL_DATE_FORMAT.format(date);
  }

}
