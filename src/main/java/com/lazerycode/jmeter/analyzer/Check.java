/**
 *
 */
package com.lazerycode.jmeter.analyzer;

/**
 *
 */
public class Check {

    private double threshold = -1.0;

    private double tolerance = 5.0;

    private ToleranceDirection toleranceDirection = ToleranceDirection.UPPER_LOWER_TOLERANCE;

    private double minValue;

    private double maxValue;

    public double getMinValue() {
        return minValue;
    }

    public double getMaxValue() {
        return maxValue;
    }

    public double getThreshold() {
        return threshold;
    }

    public void setThreshold(double threshold) {
        this.threshold = threshold;
        updateMinMaxValue();
    }

    public double getTolerance() {
        return tolerance;
    }

    public void setTolerance(double tolerance) {
        this.tolerance = tolerance;
        updateMinMaxValue();
    }

    public void setToleranceDirection(String toleranceDirection) {
        this.toleranceDirection = ToleranceDirection.valueOf(toleranceDirection);
        updateMinMaxValue();
    }

    public Boolean valid(double value) {
        if (value < 0) {
            throw new IllegalArgumentException("Value must be positive. " + value);
        }
        // Any verification if threshold is not greater than 0
        if (threshold >= 0) {
            if(ToleranceDirection.EQUALS.equals(toleranceDirection)) {
                return value == threshold;
            } else {
                return value >= minValue && value <= maxValue;
            }
        }
        return null;
    }

    private void updateMinMaxValue() {
        if (ToleranceDirection.UPPER_TOLERANCE.equals(toleranceDirection) || ToleranceDirection.UPPER_LOWER_TOLERANCE.equals(toleranceDirection)) {
            maxValue = threshold + (threshold * tolerance / 100);
        } else if (ToleranceDirection.UPPER.equals(toleranceDirection)) {
            maxValue = Double.MAX_VALUE;
        } else {
            maxValue = threshold;
        }

        if (ToleranceDirection.LOWER_TOLERANCE.equals(toleranceDirection) || ToleranceDirection.UPPER_LOWER_TOLERANCE.equals(toleranceDirection)) {
            minValue = threshold - (threshold * tolerance / 100);
        } else if (ToleranceDirection.LOWER.equals(toleranceDirection)) {
            minValue = 0;
        } else {
            minValue = threshold;
        }
    }

    enum ToleranceDirection {
        UPPER, LOWER, UPPER_TOLERANCE, LOWER_TOLERANCE, UPPER_LOWER_TOLERANCE, EQUALS;
    }

}
