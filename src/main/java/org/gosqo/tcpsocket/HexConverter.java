package org.gosqo.tcpsocket;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class HexConverter {
    public static final Charset BASE_CHARSET = StandardCharsets.ISO_8859_1;

    static String bytesToString(byte[] bytes, int length, Charset charset) {
        return new String(bytes, 0, length, charset);
    }

    static String bytesToHexExpression(byte[] bytes, int length) {

        byte[] copy = bytes.length == length
                ? bytes
                : Arrays.copyOf(bytes, length);
        StringBuilder builder = new StringBuilder();

        for (byte b : copy) {
            builder.append("%02X ".formatted(b));
        }

        return builder.substring(0, builder.length() - 1);
    }

    static byte[] hexExpressionToBytes(String hexExp) throws IllegalArgumentException {

        if (hexExp == null || hexExp.isEmpty()) {
            throw new IllegalArgumentException("Input string cannot be null or empty");
        }

        String trimmed = hexExp.trim();
        String[] split = trimmed.contains(" ") ? trimmed.split(" ") : splitEach2(trimmed);
        byte[] bytes = new byte[split.length];

        for (int i = 0; i < split.length; i++) {
            String each = split[i];

            if (each.length() != 2) {
                throw new IllegalArgumentException("cannot pair in 2 digit hex");
            }

            bytes[i] = (byte) Integer.parseInt(each, 16); // NumberFormatException
        }

        return bytes;
    }

    static String[] splitEach2(String s) {
        int length = s.length();

        if (length % 2 != 0) {
            throw new IllegalArgumentException("cannot pair in 2 characters");
        }

        String[] ret = new String[length / 2];

        for (int i = 0; i < length; i += 2) {
            String substring = s.substring(i, i + 2);

            ret[i / 2] = substring;
        }

        return ret;
    }
}
