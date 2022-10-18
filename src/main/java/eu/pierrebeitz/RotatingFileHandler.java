package eu.pierrebeitz;

import java.io.IOException;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.logging.FileHandler;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.LogRecord;

public class RotatingFileHandler extends Handler {
    private final Duration duration;
    private final Path basePattern;
    private final long limit;
    private final int count;
    private final boolean append;

    private Instant initInstant;
    private FileHandler rotatingHandler;

    private final Object monitor = new Object();

    // initial instant will be prepended to the basePattern filename
    public RotatingFileHandler(Path basePattern, long limit, int count, boolean append, Duration duration) throws IOException {
        this.basePattern = basePattern;
        this.limit = limit;
        this.count = count;
        this.append = append;
        this.duration = duration;
        rotate();
    }

    @Override
    public void publish(LogRecord record) {
        // to avoid synchronizing the whole method
        if (shouldRotate()) {
            synchronized (monitor) {
                if (shouldRotate()) {
                    try {
                        rotate();
                    } catch (IOException e) {
                        // this needs some polishing, we basically closed the old FileHandler and don't have a new one
                        // to publish to, what should we do?
                        throw new RuntimeException(e);
                    }
                }
            }
        }
        rotatingHandler.publish(record);
    }

    @Override
    public synchronized void close() throws SecurityException {
        rotatingHandler.close();
    }

    @Override
    public synchronized void flush() {
        rotatingHandler.flush();
    }

    @Override
    public boolean isLoggable(LogRecord record) {
        return rotatingHandler.isLoggable(record);
    }

    @Override
    public synchronized void setFormatter(Formatter newFormatter) throws SecurityException {
        rotatingHandler.setFormatter(newFormatter);
    }

    private boolean shouldRotate() {
        // this is simplistic, the filehandler might exists without any file written to it, in which case
        // recreating it is a loss of time
        return Instant.now().isAfter(initInstant.plus(duration));
    }

    private void rotate() throws IOException {
        if (rotatingHandler != null) {
            rotatingHandler.close();
        }

        initInstant = Instant.now();
        rotatingHandler = new FileHandler(computePattern(), limit, count, append);
    }

    private String computePattern() {
        var computedFileName = String.format("%s-%s", basePattern.getFileName(), initInstant.toString().replaceAll(":", "_"));
        return basePattern.getParent().resolve(computedFileName).toString();
    }
}
