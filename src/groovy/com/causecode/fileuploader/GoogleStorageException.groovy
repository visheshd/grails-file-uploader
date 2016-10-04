package com.causecode.fileuploader

class GoogleStorageException extends Exception {

    GoogleStorageException(String message) {
        super(message)
    }

    GoogleStorageException(String message, Throwable throwable) {
        super(message, throwable)
    }
}
