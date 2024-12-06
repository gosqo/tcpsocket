package org.gosqo.tcpsocket;

import java.nio.charset.StandardCharsets;

public class HexConverter {

    static String hexStringTo8859Encoded(String hex) throws IllegalArgumentException {
        int length = hex.length();

        if (length % 2 != 0) {
            throw new IllegalArgumentException("cannot pair in 2digit hexadecimal");
        }

        byte[] data = new byte[length / 2];

        for (int i = 0; i < length; i += 2) {
            int digit1 = Character.digit(hex.charAt(i), 16);
            int digit2 = Character.digit(hex.charAt(i + 1), 16);

            if (digit1 < 0 || digit2 < 0) {
                throw new IllegalArgumentException("one or more digits is not in the Hexadecimal range");
            }

            data[i / 2] = (byte) (
                    (digit1 << 4) + digit2
            );
        }

        return new String(data, StandardCharsets.ISO_8859_1);
    }

    static String stringToHex(String s) {
        StringBuilder ret = new StringBuilder();

        byte[] bytes = s.getBytes(StandardCharsets.UTF_8);

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
