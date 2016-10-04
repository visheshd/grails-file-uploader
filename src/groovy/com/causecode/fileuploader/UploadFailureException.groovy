package com.causecode.fileuploader

class UploadFailureException extends StorageException {

    UploadFailureException(String message, Throwable throwable) {
        super(message, throwable)
    }

    UploadFailureException(String fileName, String containerName, Throwable throwable) {
        super("Could not upload file $fileName to container $containerName", throwable)
    }
}
