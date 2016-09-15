package com.causecode.fileuploader

class UploadFailureException extends Exception {

    UploadFailureException(String message) {
        super(message)
    }

    UploadFailureException(String fileName, String containerName) {
        super("Could not upload file $fileName to container $containerName")
    }
}
