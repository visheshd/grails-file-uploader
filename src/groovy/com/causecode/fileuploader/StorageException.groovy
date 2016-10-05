package com.causecode.fileuploader

/**
 * Created by causecode on 4/10/16.
 */
class StorageException extends Exception {

    StorageException(String msg) {
        super(msg)
    }

    StorageException(String message, Throwable throwable) {
        super(message, throwable)
    }
}
