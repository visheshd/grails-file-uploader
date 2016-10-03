package com.causecode.fileuploader

class GoogleCDNException extends Exception {

    GoogleCDNException(String message) {
        super(message)
    }

    GoogleCDNException(String message, Throwable throwable) {
        super(message, throwable)
    }
}
