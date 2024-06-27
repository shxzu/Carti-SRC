package dev.tenacity.utils.misc;
import org.apache.commons.lang3.RandomUtils;

public class TimeUtils {
    private static long lastMS = 0L;
    private long resetMS = 0L;

    public static long randomDelay(final int double1, final int double2) {
        return RandomUtils.nextInt();
    }

    public static long getCurrentMS() {
        return System.nanoTime() / 1000000L;
    }

    public static long getCurrentTime()
    {
        return (long) (System.nanoTime() / 1000000.0D);
    }

    public boolean isDelayComplete(long delay)
    {
        return System.currentTimeMillis() - lastMS >= delay;
    }

    public boolean hasDelayRun(double d)
    {
        return System.currentTimeMillis() >= this.resetMS + d;
    }

    public static boolean hasTimePassedMS(long LastMS, long MS) {
        return (getCurrentMS() >= LastMS + MS);
    }

    public static boolean hasTimePassedMS(long MS) {
        return (getCurrentMS() >= lastMS + MS);
    }

    public void resetAndAdd(long reset)
    {
        this.resetMS = (getCurrentTime() + reset);
    }

    public boolean hasReached(long milliseconds) {
        return getCurrentMS() - lastMS >= milliseconds;
    }

    public static void reset() {
        lastMS = getCurrentMS();
    }

    public void setLastMS() {
        lastMS = System.currentTimeMillis();
    }

	public static long randomDelay(Double value, Double value2) {
		return RandomUtils.nextInt();
	}

}
