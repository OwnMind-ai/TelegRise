package org.telegrise.telegrise.application;

import lombok.extern.slf4j.Slf4j;
import org.telegrise.telegrise.TelegRiseApplication;

import java.util.concurrent.*;
import java.util.function.Supplier;

/**
 * A basic extension of ThreadPoolExecutor that is meant to be used in
 * {@link TelegRiseApplication#setExecutorService(Supplier)} method and ensures proper error-handling.
 * <p>
 * This class has been used in older versions (<0.10) of the framework before the migration to Java 21 and adoption of <code>Virtual Threads</code>.
 *
 * @since 0.1
 */
@Slf4j
public class DefaultThreadPoolExecutor extends ThreadPoolExecutor {
    public DefaultThreadPoolExecutor(int coreSize, int maxSize) {
        super(coreSize, maxSize, 0L, TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<>());
    }

    @Override
    protected void afterExecute(Runnable r, Throwable t) {
        super.afterExecute(r, t);
        if (t == null && r instanceof Future<?>) {
            try {
                Future<?> future = (Future<?>) r;
                if (future.isDone()) {
                    future.get();
                }
            } catch (CancellationException ce) {
                t = ce;
            } catch (ExecutionException ee) {
                t = ee.getCause();
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
            }
        }
        if (t != null) {
            log.error(t.toString(), t);
        }
    }
}
