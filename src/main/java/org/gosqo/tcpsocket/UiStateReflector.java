package org.gosqo.tcpsocket;

public class UiStateReflector {

    static String addCrLf(boolean enterHex, String s) {
        if (enterHex) {
            String trimmed = s.trim();
            if (trimmed.contains(" ")) {
                return trimmed + " 0d 0a";
            }
            return trimmed + "0d0a";
        }
        return s + "\r\n";
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
