package p.lodz.pl.adi.utils;


import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;

public class ExecutorUtil {

    public static final int MESSAGE_LIMIT = 10;
    public static final int PROCESSOR_MULTIPLIER = 4;
    public static final double NEED_MULTIPLIER = 1;

    private final int processors = Runtime.getRuntime().availableProcessors();
    private final int executorSlots = processors * PROCESSOR_MULTIPLIER;
    private final ThreadPoolExecutor executors = (ThreadPoolExecutor) Executors.newFixedThreadPool(executorSlots);

    public int getProcessors() {
        return processors;
    }

    public int getExecutorSlots() {
        return executorSlots;
    }

    public ThreadPoolExecutor getExecutors() {
        return executors;
    }

    public long getCompletedTaskCount() {
        return executors.getCompletedTaskCount();
    }

    public int getActiveCount() {
        return executors.getActiveCount();
    }

    public int needTasks() {
        int need = (int) (executorSlots * NEED_MULTIPLIER - executors.getActiveCount());
        return Math.min(MESSAGE_LIMIT, need);
    }

    public Future<?> submit(Runnable task) {
        return executors.submit(task);
    }
}
