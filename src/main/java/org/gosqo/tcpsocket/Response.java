package org.gosqo.tcpsocket;

public record Response(
        int status
        , String message
) {
}
