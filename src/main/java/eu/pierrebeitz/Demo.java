package eu.pierrebeitz;

import java.io.IOException;
import java.nio.file.Files;
import java.time.Duration;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Demo {

    private static final Logger LOGGER = Logger.getLogger("Demo");

    public static void main(String[] args) throws IOException, InterruptedException {
        var logggingDir = Files.createTempDirectory("demo-rotation");
        var handler = new RotatingFileHandler(logggingDir.resolve("rotation.log"), 0, 1, true, Duration.ofSeconds(30));
        LOGGER.addHandler(handler);
        System.out.println(String.format("Files will be logged to %s every 1s or so, rotation will happen every 30s", logggingDir));

        while (true) {
            LOGGER.log(Level.INFO, generateString());
            Thread.sleep(1000);
        }
    }

    private static String generateString() {
        var a = 97;
        var z = 122;
        return new Random().ints(a, z + 1)
                .limit(50)
                .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
                .toString();
    }
}
