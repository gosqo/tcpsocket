package org.gosqo.tcpsocket;

public class UiStateReflector {

    static String addCrLf(boolean addCr, boolean addLf, boolean enterHex, String s) {

        if (addCr && addLf) {
            String temp = addCr(enterHex, s);
            return addLf(enterHex, temp);
        }

        if (addCr) {
            return addCr(enterHex, s);
        }

        if (addLf) {
            return addLf(enterHex, s);
        }

        return s;
    }

    static String addCr(boolean enterHex, String s) {
        String trimmed = s.trim();

        if (enterHex && trimmed.contains(" ")) {
            return trimmed + " 0d";
        }

        if (enterHex) {
            return trimmed + "0d";
        }

        return s + "\r";
    }

    static String addLf(boolean enterHex, String s) {
        String trimmed = s.trim();

        if (enterHex && trimmed.contains(" ")) {
            return trimmed + " 0a";
        }

        if (enterHex) {
            return trimmed + "0a";
        }

        return s + "\n";
    }

    static byte[] getBytes(boolean enterHex, String s) {
        if (enterHex) {
            return HexConverter.hexExpressionToBytes(s);
        }
        return s.getBytes(HexConverter.BASE_CHARSET);
    }

    static String decideHowToShow(boolean showHex, byte[] bytes, int length) {

        if (showHex) {
            return HexConverter.bytesToHexExpression(bytes, length);
        }

        // temp code: if CR, LF added, - to be method for feature
        String converted = HexConverter.bytesToString(bytes, length, HexConverter.BASE_CHARSET);
        String crReplaced = converted.replaceAll("\\r", " \\\\r");
        String lfReplaced = crReplaced.replaceAll("\\n", " \\\\n");

        return lfReplaced;
    }
}
