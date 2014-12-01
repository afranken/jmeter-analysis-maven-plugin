package com.lazerycode.jmeter.analyzer.util;

import com.lazerycode.jmeter.analyzer.parser.AggregatedResponses;
import org.codehaus.plexus.util.StringUtils;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.Properties;

import static com.lazerycode.jmeter.analyzer.config.Environment.ENVIRONMENT;
import static com.lazerycode.jmeter.analyzer.config.Environment.ISO8601_FORMAT;

/**
 * Collection of file related convenience methods.
 */
public class FileUtil {

  private static final String TOKEN_FROM = "_FROM_";
  private static final String TOKEN_TO = "_TO_";
  private static final int READ_BUFFER = 1024;

  /**
   * URLEncode given String with UTF-8
   *
   * @param name string to encode
   * @return encoded String
   * @throws java.io.UnsupportedEncodingException
   *
   * @see java.net.URLEncoder#encode(String, String)
   */
  public static String urlEncode(String name) throws UnsupportedEncodingException {
    return URLEncoder.encode(name, "UTF-8");
  }

  /**
   * Create and return file of given name in given directory
   *
   * @param dir directory to create the file in
   * @param name file name
   * @param resultDataFileRelativePath optional relative path below dir, if {@link com.lazerycode.jmeter.analyzer.AnalyzeMojo#preserveDirectories} is true
   *
   * @return the initialized file, with created directories.
   */
  public static File initializeFile(File dir, String name, String resultDataFileRelativePath) throws IOException {

    File directory = dir;

    //add relative path to output directory if configured to do so
    if (ENVIRONMENT.isPreserveDirectories() &&
            StringUtils.isNotEmpty(resultDataFileRelativePath)) {
      String filename = dir.getAbsolutePath()
              + File.separator
              + resultDataFileRelativePath;
      directory = new File(filename);
    }

    File result = new File(directory, name);

    if (!result.getParentFile().mkdirs() && !result.getParentFile().exists()) {
      throw new IOException("Cannot create directories: " + result.getParentFile().getAbsolutePath());
    }

    if (result.exists() && !result.delete()) {
      throw new IOException("Failed to delete file: " + result.getAbsolutePath());
    }

    if (!result.createNewFile()) {
      throw new IOException("Failed to create file: " + result.getAbsolutePath());
    }

    return result;
  }


  /**
   * Reads in a set of remote resources
   *
   * @param remoteResources The resources as mapping URL to file name
   * @param targetDir       The dir to store the resources
   * @throws IOException If reading fails
   */
  public static void readResources(Properties remoteResources, File targetDir, String resultDataFileRelativePath,
                                   Collection<AggregatedResponses> testResults, String fromuntilDateFormat) throws IOException {

    long[] startEnd = getStartEnd(testResults);
    long start = startEnd[0];
    long end = startEnd[1];

    SimpleDateFormat dateFormat = new SimpleDateFormat(fromuntilDateFormat);
    String fromString = urlEncode(dateFormat.format(new Date(start)));
    String endString = urlEncode(dateFormat.format(new Date(end)));

    for (String url : remoteResources.stringPropertyNames()) {

      String fullUrl = url.replace(TOKEN_FROM, fromString);
      fullUrl = fullUrl.replace(TOKEN_TO, endString);

      String fileName = remoteResources.getProperty(url);

      File file = initializeFile(targetDir, fileName, resultDataFileRelativePath);

      read(fullUrl, file);
    }
  }

  //====================================================================================================================

  /**
   * Get start/end timestamp from a list of results
   *
   * @param testResults The results
   * @return an array containing 2 elements: [startTimeStamp, endTimeStamp]
   */
  private static long[] getStartEnd(Collection<AggregatedResponses> testResults) {

    // compute min max
    long from = Long.MAX_VALUE;
    long to = 0;
    for (AggregatedResponses entry : testResults) {

      if (from > entry.getStart()) {
        from = entry.getStart();
      }
      if (to < entry.getEnd()) {
        to = entry.getEnd();
      }
    }

    return new long[]{from, to};
  }


  /**
   * Reads a remote resource and stores it as a file
   *
   * @param url    URL to read from
   * @param target target file to write the URL contents to
   * @throws java.io.IOException if the URL cannot be read or the file cannot be written
   */
  private static void read(String url, File target) throws IOException {
    try {
      URLConnection connection = new URL(url).openConnection();

      InputStream in = connection.getInputStream();
      try {
        OutputStream out = new FileOutputStream(target);
        try {
          byte[] buffer = new byte[READ_BUFFER];
          int len;
          while ((len = in.read(buffer)) > -1) {
            out.write(buffer, 0, len);
          }
        }
        finally {
          out.close();
        }
      }
      finally {
        in.close();
      }
    }
    catch (IOException e) {
      throw new IOException("Error writing " + url + " to " + target, e);
    }
  }


}
