/**
 *
 */
package com.lazerycode.jmeter.checker;

import static com.lazerycode.jmeter.analyzer.config.Environment.ENVIRONMENT;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.logging.SystemStreamLog;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.xml.sax.SAXException;

import com.lazerycode.jmeter.analyzer.Check;
import com.lazerycode.jmeter.analyzer.CheckResult;
import com.lazerycode.jmeter.analyzer.JMeterResultParserTest;
import com.lazerycode.jmeter.analyzer.RequestGroup;
import com.lazerycode.jmeter.analyzer.config.Environment;
import com.lazerycode.jmeter.analyzer.parser.AggregatedResponses;
import com.lazerycode.jmeter.analyzer.parser.JMeterResultParser;

/**
 *
 */
public class ResultCheckerTest {

    private ResultChecker resultChecker = new ResultChecker();

    @Before
    public void setUp() {
        ENVIRONMENT.clear();
        ENVIRONMENT.setLog(new SystemStreamLog());
        ENVIRONMENT.setSampleNames(new HashSet<String>(Arrays.asList(new String[] { Environment.HTTPSAMPLE_ELEMENT_NAME, Environment.SAMPLE_ELEMENT_NAME })));
        ENVIRONMENT.setCheckResult(getCheckResult(10));
    }

    @Test
    public void testCheckDisablingSuccessJmeter() throws Exception {
        ENVIRONMENT.setCheckResult(null);
        check("JMeterResultParserTest-success.xml", 1);
    }

    @Test
    public void testCheckSuccessJmeter() throws Exception {
        check("JMeterResultParserTest-success.xml", 1);
    }

    @Test(expected = MojoFailureException.class)
    public void testCheckSomeErrorsJmeter() throws Exception {
        check("JMeterResultParserTest-someErrors.xml", 1);
    }

    @Test
    public void testCheckPatternSuccessJmeter() throws Exception {
        setRequestGroupCheckResult(6);
        check("JMeterResultParserTest-patternSuccess.xml", 2);
    }

    @Test(expected = MojoFailureException.class)
    public void testCheckErrorPatternSuccessJmeter() throws Exception {
        setRequestGroupCheckResult(7);
        check("JMeterResultParserTest-patternSuccess.xml", 2);
    }

    private void check(String fileTest, int size) throws IOException, SAXException, MojoFailureException {
        JMeterResultParser a = new JMeterResultParser();
        Map<String, AggregatedResponses> result = a.aggregate(
                new InputStreamReader(JMeterResultParserTest.class.getResourceAsStream(fileTest)));
        Assert.assertEquals("size", size, result.size());
        resultChecker.check(result);
    }

    private void setRequestGroupCheckResult(int thresholdRequestGroup) {
        List<RequestGroup> requestGroups = new ArrayList<RequestGroup>();
        RequestGroup requestGroup1 = new RequestGroup();
        requestGroup1.setName("PATTERN_NAME");
        requestGroup1.setPattern("main");
        requestGroup1.setCheckResult(getCheckResult(thresholdRequestGroup));
        requestGroups.add(requestGroup1);
        ENVIRONMENT.setRequestGroups(requestGroups);
        ENVIRONMENT.setCheckResult(getCheckResult(4));
    }

    private CheckResult getCheckResult(int threshold) {
        CheckResult checkResult = new CheckResult();
        Check throughput = new Check();
        throughput.setThreshold(threshold);
        checkResult.setThroughput(throughput);
        Check errors = new Check();
        errors.setToleranceDirection("EQUALS");
        errors.setThreshold(0);
        checkResult.setErrors(errors);
        return checkResult;
    }

}
