package com.lazerycode.jmeter.analyzer.parser;

import com.lazerycode.jmeter.analyzer.RequestGroup;
import com.lazerycode.jmeter.analyzer.statistics.Samples;
import org.apache.maven.plugin.logging.Log;
import org.springframework.util.AntPathMatcher;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.IOException;
import java.io.Reader;
import java.util.*;

import static com.lazerycode.jmeter.analyzer.config.Environment.ENVIRONMENT;
import static com.lazerycode.jmeter.analyzer.parser.StatusCodes.HTTPCODE_CONNECTIONERROR;
import static com.lazerycode.jmeter.analyzer.parser.StatusCodes.HTTPCODE_ERROR;

/**
 * Parses a JMeter xml result and provides {@link AggregatedResponses aggregated results}
 *
 * @author Dennis Homann, Arne Franken, Peter Kaul
 */
public class JMeterResultParser {

  /**
   * number of parsed items after which a log message is written
   */
  private static final int LOGMESSAGE_ITEMS = 10000;

  /**
   * Parses a JMeter Result XML file and provides a {@link AggregatedResponses} for every {@link Parser#getKey key}
   *
   * @param reader the JMeter xml file
   *
   * @return The AggregatedResponses for every thread group
   *
   * @throws IOException If reading fails
   * @throws SAXException  If parsing fails
   */
  public Map<String, AggregatedResponses> aggregate(Reader reader) throws IOException, SAXException {

    SAXParser saxParser;
    try {

      saxParser = SAXParserFactory.newInstance().newSAXParser();
    }
    catch (ParserConfigurationException e) {

      throw new IllegalStateException("Parser could not be created ", e);
    }

    Parser parser = new Parser();
    saxParser.parse(new InputSource(reader), parser);

    return parser.getResults();
  }



  // ==================

  /**
   * @return the current log
   */
  private static Log getLog() {
    return ENVIRONMENT.getLog();
  }

  /**
   * Parser does the heavy lifting.
   */
  private static class Parser extends DefaultHandler {

    private final AntPathMatcher matcher = new AntPathMatcher();

    private final int maxSamples;
    private final List<RequestGroup> pathPatterns;
    private final boolean sizeByUris;
    private final boolean durationByUris;

    private long parsedCount = 0;

    private Map<String, AggregatedResponses> results = new LinkedHashMap<String, AggregatedResponses>();
    private Set<String> nodeNames;

    /**
     * Constructor.
     * Fields configured from Environment
     */
    public Parser() {
      this(ENVIRONMENT.getMaxSamples(),
           ENVIRONMENT.getRequestGroups(),
           ENVIRONMENT.isGenerateDetails(),
           ENVIRONMENT.isGenerateDetails(),
           ENVIRONMENT.getSampleNames());
    }

    /**
     * Constructor.
     *
     * @param maxSamples The maximum number of samples that be stored internally for every metric
     * @param pathPatterns A number of ANT patterns. If set then the resulting {@link AggregatedResponses} will be
     *        grouped by uris matching these patterns. If not set then the threadgroup is used
     * @param sizeByUris true, if the response size shall be counted for each uri separately
     * @param durationByUris true, if the response duration shall be counted for each uri separately
     * @param nodeNames Set of node names to process
     */
    public Parser(int maxSamples, List<RequestGroup> pathPatterns, boolean sizeByUris, boolean durationByUris, Set<String> nodeNames) {
      this.maxSamples = maxSamples;
      this.pathPatterns = pathPatterns;
      this.sizeByUris = sizeByUris;
      this.durationByUris = durationByUris;
      this.nodeNames = nodeNames;
    }

    /**
     * @return a mapping from identifier to aggregatedResult
     */
    public Map<String, AggregatedResponses> getResults() {
      return results;
    }

    @Override
    public void startElement(String u, String localName, String qName, Attributes attributes) throws SAXException {

      if( nodeNames.contains(localName) || nodeNames.contains(qName) ) {

        String uri = attributes.getValue("lb");
        String timestampString = attributes.getValue("ts");
        long timestamp = Long.parseLong(timestampString);

        boolean success = Boolean.valueOf(attributes.getValue("s"));

        String key = getKey(attributes);

        // --- create / provide result container
        AggregatedResponses resultContainer = getResult(key);


        // --- parse bytes
        long bytes = parseLong(attributes, "by");

        // --- parse duration
        long duration = parseLong(attributes, "t");

        // --- parse active thread for all groups
        long activeThreads = parseLong(attributes, "na");

        // --- parse responseCode
        int responseCode = getResponseCode(attributes);

        // ==== add data to the resultContainer
        addData(resultContainer, uri, timestamp, bytes, duration, activeThreads, responseCode, success);


        parsedCount++;

        // write a log message every 10000 entries
        if( parsedCount % LOGMESSAGE_ITEMS == 0 ) {
          getLog().info("Parsed "+parsedCount+" entries ...");
        }
      }

      super.startElement(u, localName, qName, attributes);
    }

    @Override
    public void endDocument() throws SAXException {
      super.endDocument();
      //finish collection of responses/samples
      for( AggregatedResponses responses : results.values() ) {
        responses.finish();
      }
      getLog().info("Finished Parsing "+parsedCount+" entries.");
    }

    //==================================================================================================================

    /**
     * Add data from httpSample to {@link AggregatedResponses the resultContainer}
     *
     * @param resultContainer container to add data to
     * @param uri uri identifying the resultContainer
     * @param timestamp httpSample timestamp
     * @param bytes httpSample bytes
     * @param duration httpSample duration
     * @param responseCode httpSample responseCode
     * @param success httpSample success
     */
    private void addData(AggregatedResponses resultContainer, String uri,
                         long timestamp, long bytes, long duration, long activeThreads, int responseCode, boolean success) {


      StatusCodes statusCodes = resultContainer.getStatusCodes();
      statusCodes.increment(responseCode);

      Map<Integer, Set<String>> uriByStatusCodeMapping = resultContainer.getUriByStatusCode();
      add(uriByStatusCodeMapping, responseCode, uri);

      Samples activeThreadResult = resultContainer.getActiveThreads();
      activeThreadResult.addSample(timestamp + duration, activeThreads);

      // -- register data
      if( !success || bytes == -1 || duration == -1 ||
              responseCode >= HTTPCODE_ERROR || responseCode == HTTPCODE_CONNECTIONERROR ) {

        // httpSample is not okay
        // 4xx (client error) or 5xx (server error)
        Samples requestResult = resultContainer.getDuration();
        requestResult.addError(timestamp);
        Samples bytesResult = resultContainer.getSize();
        bytesResult.addError(timestamp);
      }
      else {

        // httpSample is okay
        Samples bytesResult = resultContainer.getSize();
        bytesResult.addSample(timestamp, bytes);
        Samples requestResult = resultContainer.getDuration();
        requestResult.addSample(timestamp, duration);

        Map<String, Samples> sizeByUriMapping = resultContainer.getSizeByUri();
        Map<String, Samples> durationByUriMapping = resultContainer.getDurationByUri();

        add(sizeByUriMapping, uri, timestamp, bytes);
        add(durationByUriMapping, uri, timestamp, duration);
      }

      //set start and end time
      if( resultContainer.getStart() == 0 ) {
        resultContainer.setStart(timestamp);
      }
      resultContainer.setEnd(timestamp);

    }

    /**
     * Create / provide {@link AggregatedResponses result container}
     *
     * @param key identifier
     *
     * @return the aggregated response matching the key
     */
    private AggregatedResponses getResult(String key) {

      AggregatedResponses resultContainer = results.get(key);
      if(resultContainer == null) {

        //initialize new AggregatedResponses
        resultContainer = new AggregatedResponses();
        resultContainer.setActiveThreads(new Samples(maxSamples, true));
        resultContainer.setDuration(new Samples(maxSamples, true));
        resultContainer.setSize(new Samples(maxSamples, false));
        resultContainer.setStatusCodes(new StatusCodes());
        resultContainer.setUriByStatusCode(new HashMap<Integer, Set<String>>());
        if( sizeByUris ) {
          resultContainer.setSizeByUri(new HashMap<String, Samples>());
        }
        if( durationByUris ) {
          resultContainer.setDurationByUri(new HashMap<String, Samples>());
        }

        results.put(key,resultContainer);
      }

      return resultContainer;
    }

    /**
     * Get the reponse code from the {@link Attributes}
     * Response code in <httpSample> element may not be an Integer, this is a safeguard against that.
     *
     * @param atts attributes to extract the response code from
     * @return a valid response code
     */
    private int getResponseCode(Attributes atts) {

      int responseCode;
      String responseCodeString = atts.getValue("rc");
      try {

        responseCode = Integer.valueOf(responseCodeString);
      }
      catch (Exception e) {
        getLog().warn("Error parsing response code '"+responseCodeString+"'");
        responseCode = HTTPCODE_CONNECTIONERROR;
      }

      return responseCode;
    }

    /**
     * Returns a key for the attributes.
     * If a path pattern is configured, either a matching path or "default" will be returned.
     * If no path pattern is configured, the name of the threadgroup will be used.
     *
     * @param attributes attributes to extract the key from
     * @return the key
     */
    private String getKey(Attributes attributes) {

      String key = null;
      if( pathPatterns != null && !pathPatterns.isEmpty()) {

        // try to find a pattern key
        String uri = attributes.getValue("lb");
        for( RequestGroup requestGroup : pathPatterns ) {

          if( matcher.match(requestGroup.getPattern(), uri) ) {
            // found a pattern
            key = requestGroup.getName();
            break;
          }
        }

        if( key == null ) {
          // no pattern found. use default
          key = "default";
        }
      }
      else {

        // use threadgroup name as a key
        key = attributes.getValue("tn");

        //key is now "threadgroupname int-int"
        int threadGroupSeparator = key.indexOf(' ');
        if( threadGroupSeparator > -1) {
          // cut off trailing threadno
          key = key.substring(0, threadGroupSeparator);
        }
      }

      return key;
    }

    /**
     * Add #timestamp and matching #value to (new) Samples object matching the given #uri to given Map #uriSamples
     *
     * @param uriSamples map to add the Samples to
     * @param uri the uri identifying the Samples object
     * @param timestamp the timestamp
     * @param value the value
     */
    private void add(Map<String, Samples> uriSamples, String uri, long timestamp, long value) {

      if( uriSamples != null ) {

        Samples samples = uriSamples.get(uri);

        if( samples == null ) {
          // no Sample was previously stored for the uri.
          samples = new Samples(0, false); // 0 = don't collect samples. This is important, otherwise a OOM may occur if the result set is big

          uriSamples.put(uri, samples);
        }

        samples.addSample(timestamp, value);
      }
    }

    private void  add(Map<Integer, Set<String>> uriByStatusCode, Integer code, String uri){
      if(uriByStatusCode != null){

        Set<String> uriSet = uriByStatusCode.get(code);

        if(uriSet == null){
          uriSet = new HashSet<String>();
          uriByStatusCode.put(code, uriSet);
        }

        uriSet.add(uri);
      }
    }

    private long parseLong(Attributes attributes, String qName) {
      long result = -1;
      String valueString = attributes.getValue(qName);
      if (null != valueString) {
        try {
          result = Long.parseLong(valueString);
        } catch (Exception e) {
          getLog().warn("Error parsing bytes: '" + valueString + "'");
        }
      }
      return result;
    }

  }

}
