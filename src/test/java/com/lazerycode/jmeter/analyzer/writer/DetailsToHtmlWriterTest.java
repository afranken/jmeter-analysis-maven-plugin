package com.lazerycode.jmeter.analyzer.writer;

import com.google.common.collect.ImmutableMap;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.util.Map;

import static com.lazerycode.jmeter.analyzer.config.Environment.ENVIRONMENT;
import static com.lazerycode.jmeter.analyzer.writer.WriterTestHelper.getMockedTestResults;
import static com.lazerycode.jmeter.analyzer.writer.WriterTestHelper.normalizeFileContents;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;

/**
 * Tests for {@link DetailsToHtmlWriter}
 */
public class DetailsToHtmlWriterTest {

  private final Map<String, StringWriter> writerMap =
          ImmutableMap.of(
                  "warmup-durations-test.html", new StringWriter(),
                  "warmup-sizes-test.html", new StringWriter());
  private DetailsToHtmlWriter testling;

  @Before
  public void setUp() throws Exception {

    ENVIRONMENT.initializeFreemarkerConfiguration();

    testling = new LocalDetailsToHtmlWriter();
    testling.setFileName("test");
  }

  @Test
  public void testWrite() throws Exception {

    testling.write(getMockedTestResults());

    for (Map.Entry<String, StringWriter> entry : writerMap.entrySet()) {

      File expected = new File(getClass().getResource(entry.getKey()).getFile());

      String actual = entry.getValue().toString();

      assertThat("output does not match: ",
              normalizeFileContents(actual),
              is(equalTo(normalizeFileContents(expected))));
    }


  }

  //=====================================

  /**
   * Local test class overrides methods to isolate this test from other classes
   */
  private class LocalDetailsToHtmlWriter extends DetailsToHtmlWriter {

    @Override
    protected File getFile(String name) throws IOException {
      return new File(name);
    }

    @Override
    protected java.io.Writer getWriter(File file) throws IOException {

      String fileName = file.getName();

      return writerMap.get(fileName);
    }
  }

}
