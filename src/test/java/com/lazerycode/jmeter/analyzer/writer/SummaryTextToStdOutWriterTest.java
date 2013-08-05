package com.lazerycode.jmeter.analyzer.writer;

import org.junit.Before;
import org.junit.Test;

import java.io.*;
import java.io.Writer;

import static com.lazerycode.jmeter.analyzer.config.Environment.ENVIRONMENT;
import static com.lazerycode.jmeter.analyzer.writer.WriterTestHelper.getMockedTestResults;
import static com.lazerycode.jmeter.analyzer.writer.WriterTestHelper.normalizeFileContents;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;

/**
 * Tests for {@link SummaryTextToStdOutWriter}
 */
public class SummaryTextToStdOutWriterTest {

  private final StringWriter out = new StringWriter();
  private SummaryTextToStdOutWriter testling;

  @Before
  public void setUp() throws Exception {
    ENVIRONMENT.initializeFreemarkerConfiguration();

    testling = new LocalSummaryTextToStdOutWriter();
  }

  @Test
  public void testWrite() throws Exception {

    testling.write(getMockedTestResults());

    File expected = new File(getClass().getResource("test.txt").getFile());

    String actual = out.toString();

    assertThat("output does not match: ",
            normalizeFileContents(actual),
            is(equalTo(normalizeFileContents(expected))));

  }

  //====================================================================================================================

  private class LocalSummaryTextToStdOutWriter extends SummaryTextToStdOutWriter {

    @Override
    protected Writer getWriter() throws IOException {
      return out;
    }
  }

}
