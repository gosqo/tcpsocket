package org.gosqo.tcpsocket;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Arrays;
import java.util.stream.Stream;

public class HexConverterTest {

    @ParameterizedTest
    @ValueSource(strings = {
            "68656c6c6f"
            , "6g656c6c6f"
            , "68 65 6c 6c 6f"
            , "68 65 6c 6c 6f"
            , "68 65 6c 6c 6f 0a"
            , "68 65 6c 6c 6f 0d 0a"
    })
    void hexExpressionToBytes(String param) {
        byte[] bytes = HexConverter.hexExpressionToBytes(param);

        System.out.println(Arrays.toString(bytes));
    }

    @ParameterizedTest
    @MethodSource("provideByteArrays")
    void bytesToHexExpression(byte[] param) {
        String hexExp = HexConverter.bytesToHexExpression(param, param.length);

        System.out.println(hexExp);
    }

    @ParameterizedTest
    @MethodSource("provideByteArrays")
    void bytesToString(byte[] param) {
        String s = HexConverter.bytesToString(param, param.length, HexConverter.BASE_CHARSET);

        System.out.println(s);
    }

    private static Stream<byte[]> provideByteArrays() {
        return Stream.of(
                new byte[]{(byte) 0x68, (byte) 0x65, (byte) 0x6c, (byte) 0x6c, (byte) 0x6f}
                , new byte[]{(byte) 0x68, (byte) 0x65, (byte) 0x6c, (byte) 0x6c, (byte) 0x6f, (byte) 0x0d}
                , new byte[]{(byte) 0x68, (byte) 0x65, (byte) 0x6c, (byte) 0x6c, (byte) 0x6f, (byte) 0x0d, (byte) 0x0a}
        );
    }
}
