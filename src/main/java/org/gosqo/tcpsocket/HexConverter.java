package org.gosqo.tcpsocket;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public class HexConverter {
    public static final Charset BASE_CHARSET = StandardCharsets.ISO_8859_1;

    static String bytesToString(byte[] bytes, Charset charset) {
        return new String(bytes, charset);
    }

    static String bytesToHexExpression(byte[] bytes) {
        StringBuilder builder = new StringBuilder();

        for (byte b : bytes) {
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

            bytes[i] = Byte.parseByte(each, 16); // NumberFormatException
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

    static String hexStringToUTF_8Encoded(String hex) throws IllegalArgumentException {
        int length = hex.length();

        if (length % 2 != 0) {
            throw new IllegalArgumentException("cannot pair in 2digit hexadecimal");
        }

        byte[] data = new byte[length / 2];

        for (int i = 0; i < length; i += 2) {
            int digit1 = Character.digit(hex.charAt(i), 16);
            int digit2 = Character.digit(hex.charAt(i + 1), 16);

            if (digit1 < 0 || digit2 < 0) {
                throw new IllegalArgumentException("One or more digits is not in the Hexadecimal range."
                        + "\n\tOtherwise, please check if 'Enter in Hex' is checked.");
            }

            data[i / 2] = (byte) (
                    (digit1 << 4) + digit2
            );
        }

        return new String(data, BASE_CHARSET);
    }

    static String stringToHex(String s) {
        StringBuilder ret = new StringBuilder();

        byte[] bytes = s.getBytes(BASE_CHARSET);

        for (byte b : bytes) {
            String hexFormatted = "%02x ".formatted(b);

            ret.append(hexFormatted);
        }

        return ret.toString();
    }

    static String separateEach2(String s) throws IllegalArgumentException {
        StringBuilder ret = new StringBuilder();

        int length = s.length();

        if (length % 2 != 0) {
            throw new IllegalArgumentException("cannot pair in 2digit hexadecimal");
        }

        for (int i = 0; i < length; i = i + 2) {
            char each0 = s.charAt(i);
            char each1 = s.charAt(i + 1);

            ret.append(each0)
                    .append(each1)
                    .append(" ");
        }

        return ret.toString();
    }
}
