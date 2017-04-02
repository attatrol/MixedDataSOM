package com.github.attatrol.som;

public class TestResult {

    private final int testIndex;

    private final int height;

    private final int width;

    private final double beta;

    private final String sifName;

    private double startAvgError;

    private double endAvgError;

    private double maxAvgError;

    private double minAvgError;

    private double purity;

    private double visualQuality;

    private int deadNeuronsCount;

    /**
     * Default ctor.
     * @param testIndex
     * @param height
     * @param width
     * @param beta
     * @param sifName
     */
    public TestResult(int testIndex, int height, int width, double beta, String sifName) {
        this.testIndex = testIndex;
        this.beta = beta;
        this.height = height;
        this.width = width;
        this.sifName = sifName;
    }

    public int getTestIndex() {
        return testIndex;
    }

    public int getHeight() {
        return height;
    }

    public int getWidth() {
        return width;
    }

    public double getBeta() {
        return beta;
    }

    public String getSifName() {
        return sifName;
    }

    public double getStartAvgError() {
        return startAvgError;
    }

    public void setStartAvgError(double startAvgError) {
        this.startAvgError = startAvgError;
    }

    public double getEndAvgError() {
        return endAvgError;
    }

    public void setEndAvgError(double endAvgError) {
        this.endAvgError = endAvgError;
    }

    public double getMaxAvgError() {
        return maxAvgError;
    }

    public void setMaxAvgError(double maxAvgError) {
        this.maxAvgError = maxAvgError;
    }

    public double getMinAvgError() {
        return minAvgError;
    }

    public void setMinAvgError(double minAvgError) {
        this.minAvgError = minAvgError;
    }

    public double getPurity() {
        return purity;
    }

    public void setPurity(double purity) {
        this.purity = purity;
    }

    public double getVisualQuality() {
        return visualQuality;
    }

    public void setVisualQuality(double visualQuality) {
        this.visualQuality = visualQuality;
    }

    public int getDeadNeuronsCount() {
        return deadNeuronsCount;
    }

    public void setDeadNeuronsCount(int deadNeuronsCount) {
        this.deadNeuronsCount = deadNeuronsCount;
    }

    @Override
    public String toString() {
        return String.format("| %5d | %5f| %5d| %5d| %5d| %5f| %5f| %5f| %5f| %5f| %5f| %5d|\n",
                testIndex, beta, height * width, height, width,
                startAvgError, endAvgError, minAvgError,
                maxAvgError, purity, visualQuality, deadNeuronsCount);
    }

    public static String getHeader() {
        return("----------------------------------------------------------------------------------------------------------\n"
                + "|   #   |   beta  |Square|   H  |   W  |  start  |   end   |   min   |   max   |   pur   |   vis   | dead#|\n"
                + "-----------------------------------------------------------------------------------------------------------\n");
    }

}
