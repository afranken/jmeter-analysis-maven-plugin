/**
 *
 */
package com.lazerycode.jmeter.analyzer;

/**
 *
 */
public class CheckResult {

    // Default check is disabling
    private Check throughput = new Check();

    // Default check is disabling
    private Check errors = new Check();

    public Check getThroughput() {
        return throughput;
    }

    public void setThroughput(Check pThroughput) {
        throughput = pThroughput;
    }

    public Check getErrors() {
        return errors;
    }

    public void setErrors(Check pErrors) {
        errors = pErrors;
    }

}
