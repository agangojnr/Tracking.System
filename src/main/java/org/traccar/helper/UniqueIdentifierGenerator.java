package org.traccar.helper;

import jakarta.inject.Singleton;
import java.util.Random;

@Singleton
public class UniqueIdentifierGenerator {

    private static final String CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final String PREFIX = "RV";
    private static final int LENGTH = 10;

    private final Random random = new Random();

    public String generate() {
        StringBuilder sb = new StringBuilder(PREFIX);

        for (int i = 0; i < LENGTH - PREFIX.length(); i++) {
            sb.append(CHARS.charAt(random.nextInt(CHARS.length())));
        }

        return sb.toString();
    }
}