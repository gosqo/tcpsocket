package org.gosqo.tcpsocket;

import org.junit.jupiter.api.Test;

import java.math.BigInteger;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

class HexTest {

    static byte[] hexStringToByteArray(String hex) {
        int l = hex.length();
        byte[] data = new byte[l / 2];

        for (int i = 0; i < l; i += 2) {
            int digit1 = Character.digit(hex.charAt(i), 16);
            int digit2 = Character.digit(hex.charAt(i + 1), 16);
            data[i / 2] = (byte) (
                    (digit1 << 4) + digit2
            );
        }
        return data;
    }

    @Test
    void splitEach2() {
        String obj = "ffeeffeeffeeffee";

        StringBuilder ret = new StringBuilder();

        int length = obj.length();

        if (length % 2 != 0) {
            throw new IllegalArgumentException("cannot pair in 2digit hexadecimal");
        }

        for (int i = 0; i < length; i = i + 2) {
            char each0 = obj.charAt(i);
            char each1 = obj.charAt(i + 1);

            ret.append(each0)
                    .append(each1)
                    .append(" ");
        }

        System.out.println(ret);
    }

    @Test
    void hexIntoString() { // charset: ISO_8859_1
        String s = "fffe";
        byte[] bytes = hexStringToByteArray(s);
        String toPrint = new String(bytes, StandardCharsets.ISO_8859_1);

        System.out.println(toPrint);
    }

    @Test
    void separateEachCharacter() { // show Hex
        String message = "Hello Hex";
        StringBuilder toPrint = new StringBuilder();

        byte[] bytes = message.getBytes(StandardCharsets.UTF_8);

        for (byte b : bytes) {
            String hexFormatted = "%02x ".formatted(b);

            toPrint.append(hexFormatted).append(" ");
        }

        System.out.println(toPrint); // 48  65  6c  6c  6f  20  48  65  78
    }

    @Test
    void singleCharacter() {
        char c = 'a';
        byte b = (byte) (c + 1);

        System.out.println(b); // 98
        System.out.printf("%x%n", b); // 62 (hex)
    }

    @Test
    void HelloHex() {
        String message = "Hello Hexadecimal !";
        byte[] byteMessage = message.getBytes(StandardCharsets.UTF_8);
        BigInteger bigInt = new BigInteger(1, byteMessage);
        String messageIntoHex = "%x".formatted(bigInt);

        System.out.println(messageIntoHex); // 48656c6c6f2048657861646563696d616c2021
    }

    @Test
    void defaultEncoding() {
        System.out.println(Charset.defaultCharset().name());
    }
}
