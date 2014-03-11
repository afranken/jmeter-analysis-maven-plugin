package com.lazerycode.jmeter.analyzer.writer;

import com.google.common.collect.ImmutableMap;
import com.lazerycode.jmeter.analyzer.ConfigurationCharts;

import org.junit.Before;
import org.junit.Test;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.MemoryCacheImageInputStream;

import java.awt.image.RenderedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Map;

import static com.lazerycode.jmeter.analyzer.writer.WriterTestHelper.getMockedTestResults;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * Tests for {@link ChartWriter}
 */
public class ChartWriterTest {


  private final Map<String,ByteArrayOutputStream> streamMap  =
             ImmutableMap.of(
                     "warmup-throughput-test.png", new ByteArrayOutputStream(),
                     "warmup-response_times-test.png", new ByteArrayOutputStream(),
                     "warmup-durations-test.png", new ByteArrayOutputStream(),
                     "warmup-sizes-test.png", new ByteArrayOutputStream());

  private ChartWriter testling;

  @Before
  public void setUp() throws Exception {
    testling = new LocalChartWriter(800, 600);
    testling.setFileName("test");
  }

  @Test
  public void testWrite() throws Exception {

    testling.write(getMockedTestResults());

    //TODO: find a way to robustly compare two images...

//    for(Map.Entry<String,ByteArrayOutputStream> entry : streamMap.entrySet()) {
//
//      File expected = new File(getClass().getResource(entry.getKey()).getFile());
//
//      compareStreams("image/png", FileUtils.readFileToByteArray(expected), entry.getValue().toByteArray());
//    }

  }


  //====================================================================================================================

  private void compareStreams(String contentType, byte[] expected, byte[] actual) throws Exception {
    if (!Arrays.equals(actual, expected)) {
      IIOImage expectedImg = readImage(contentType, expected);
      IIOImage actualImg = readImage(contentType, actual);
      int[] expectedPixel = getPixels(expectedImg.getRenderedImage());
      int[] actualPixel = getPixels(actualImg.getRenderedImage());
      assertSimilar(expectedPixel, actualPixel, "result image differs: ");
    }
  }

  private int[] getPixels(RenderedImage img) {
    int w = img.getWidth();
    int h = img.getHeight();
    return img.getData().getPixels(0, 0, w, h, (int[]) null);
  }

  private IIOImage readImage(String contentType, byte[] bytes) throws Exception {
    ImageReader reader = findImageReader(contentType);
    reader.setInput(new MemoryCacheImageInputStream(new ByteArrayInputStream(bytes)));
    return reader.readAll(0, reader.getDefaultReadParam());
  }

  private ImageReader findImageReader(String contentType) {
    Iterator<ImageReader> readers = ImageIO.getImageReadersByMIMEType(contentType);
    ImageReader reader = null;
    if (readers != null && readers.hasNext()) {
      reader = readers.next();
    }
    if (reader == null) {
      throw new IllegalArgumentException(String.format("Media type '%s' not supported", contentType));
    }
    return reader;
  }

  private void assertSimilar(int[] expectedPixel, int[] actualPixel, String msg) {
    assertEquals("different number of pixels", expectedPixel.length, actualPixel.length);
    // just simple tolerance: accept anything not more than EPS units away
    int diffCount = 0;
    int EPS = 1;
    for (int i = 0; i < expectedPixel.length; i++) {
      if (Math.abs(expectedPixel[i] - actualPixel[i]) > EPS) {
        diffCount++;
      }
    }
    // Allow 1% of the pixels to differ.
    if (diffCount > expectedPixel.length / 100) {
      fail(msg +diffCount);
    }
  }

  //====================================================================================================================
  //====================================================================================================================

  private class LocalChartWriter extends ChartWriter {

    /**
     * @param pImageWidth
     * @param pImageHeight
     */
    public LocalChartWriter(int pImageWidth, int pImageHeight) {
        super();
    }

    protected OutputStream getOut(File file) throws FileNotFoundException {
      return streamMap.get(file.getName());
    }

    protected File getFile(String name) throws IOException {
      return new File(name);
    }
  }
}
