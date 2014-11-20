/**
 *
 */
package com.lazerycode.jmeter.checker;

import static com.lazerycode.jmeter.analyzer.config.Environment.ENVIRONMENT;

import java.util.Map;

import org.apache.maven.plugin.MojoFailureException;

import com.lazerycode.jmeter.analyzer.Check;
import com.lazerycode.jmeter.analyzer.CheckResult;
import com.lazerycode.jmeter.analyzer.RequestGroup;
import com.lazerycode.jmeter.analyzer.parser.AggregatedResponses;

/**
 *
 */
public class ResultChecker {

    public void check(Map<String, AggregatedResponses> jmeterResults) throws MojoFailureException {
        boolean check = true;

        for (String key : jmeterResults.keySet()) {
            AggregatedResponses aggregatedResponses = jmeterResults.get(key);
            CheckResult checkResult = getCheckResult(key);

            // Check throughput
            check &= checkValue(checkResult.getThroughput(),
                    aggregatedResponses.getDuration().getSuccessPerSecond(),
                    key, "throughput");

            // Check errors
            double percentErrors = (((double) aggregatedResponses.getDuration().getErrorsCount()) /
                    (aggregatedResponses.getDuration().getErrorsCount() + aggregatedResponses.getDuration().getSuccessCount())) * 100;
            check &= checkValue(checkResult.getErrors(), percentErrors,
                    key, "errors");
        }

        if (!check) {
            throw new MojoFailureException("Check is incorrect.");
        }
    }

    private CheckResult getCheckResult(String key) {
        CheckResult result = null;

        // Check by request group
        if (null != ENVIRONMENT.getRequestGroups()) {
            for (RequestGroup requestGroup : ENVIRONMENT.getRequestGroups()) {
                if (key.equals(requestGroup.getName())) {
                    result = requestGroup.getCheckResult();
                    break;
                }
            }
        }

        // Check by default
        if (null == result) {
            result = ENVIRONMENT.getCheckResult();
        }

        return result;
    }

    private boolean checkValue(Check check, double value, String key, String valueDescription) {
        Boolean valid = check.valid(value);
        if (null == valid) {
            valid = true;
            ENVIRONMENT.getLog().info(new StringBuilder(key)
                    .append(" : Check ").append(valueDescription)
                    .append(" disabling : ").append(value).toString());
        } else if (!valid) {
            ENVIRONMENT.getLog().error(new StringBuilder(key)
                    .append(" : Check ").append(valueDescription)
                    .append(" is incorrect : ").append(value)
                    .append(" (minValue : ").append(check.getMinValue())
                    .append(", maxValue : ").append(check.getMaxValue())
                    .append(")").toString());
        } else {
            ENVIRONMENT.getLog().info(new StringBuilder(key)
                    .append(" : Check ").append(valueDescription)
                    .append(" is correct : ").append(value)
                    .append(" (minValue : ").append(check.getMinValue())
                    .append(", maxValue : ").append(check.getMaxValue())
                    .append(")").toString());
        }
        return valid;
    }

}
