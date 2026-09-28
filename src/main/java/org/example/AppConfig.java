package org.example;
//Singleton design pattern
public class AppConfig {
    private static final AppConfig INSTANCE = new AppConfig();
    private final double baseDeliveryFee;
    private final double extraKmRate;
    private final double serviceFeeRate;
    private final int threadPoolSize;

    private AppConfig() {
        this.baseDeliveryFee = 15.0;
        this.extraKmRate = 3.0;
        this.serviceFeeRate = 0.10;
        this.threadPoolSize = 10;
    }

    public static AppConfig getInstance() {
        return INSTANCE;
    }
    public double getBaseDeliveryFee() { return baseDeliveryFee; }
    public double getExtraKmRate() { return extraKmRate; }
    public double getServiceFeeRate() { return serviceFeeRate; }
    public int getThreadPoolSize() { return threadPoolSize; }
}
