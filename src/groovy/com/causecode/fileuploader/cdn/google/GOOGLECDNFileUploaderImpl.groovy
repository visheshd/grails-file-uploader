/*
 * Copyright (c) 2016, CauseCode Technologies Pvt Ltd, India.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or
 * without modification, are not permitted.
 */
package com.causecode.fileuploader.cdn.google

import com.causecode.fileuploader.cdn.CDNFileUploader
import com.google.cloud.storage.Blob
import com.google.cloud.storage.BlobId
import com.google.cloud.storage.BlobInfo
import com.google.cloud.storage.Bucket
import com.google.cloud.storage.BucketInfo
import com.google.cloud.storage.Storage
import com.google.cloud.storage.StorageOptions
import java.util.concurrent.TimeUnit
import javax.activation.MimetypesFileTypeMap

/**
 * This class is used for all the Google Cloud Storage operations.
 *
 * @author Nikhil Sharma
 * @since 2.4.9
 */
class GOOGLECDNFileUploaderImpl extends CDNFileUploader {

    Storage gStorage

    GOOGLECDNFileUploaderImpl() {
        authenticate()
    }

    Blob getBlob(String containerName, String fileName) {
        BlobId blobId = BlobId.of(containerName, fileName);
        return gStorage.get(blobId);
    }

    @Override
    boolean authenticate() {
        gStorage = StorageOptions.defaultInstance().service()

        return true
    }

    @Override
    void close() {
        gStorage = null
    }

    @Override
    boolean containerExists(String name) {
        return false
    }

    @Override
    boolean createContainer(String name) {
        Bucket bucket = gStorage.create(BucketInfo.of(name));

        if (!bucket || !bucket.exists(Bucket.BucketSourceOption.metagenerationMatch())) {
            log.warn "Could not create container $name"

            return false
        }

        log.info "Container with name ${bucket.name()} successfully created."

        return true
    }

    @Override
    void deleteFile(String containerName, String fileName) {
        Blob blob = getBlob(containerName, fileName)

        if (!blob || !blob.exists(Blob.BlobSourceOption.generationMatch())) {
            log.warn "File $fileName does not exist."

            return
        }

        if (!blob.delete(Blob.BlobSourceOption.generationMatch())) {
            log.warn "Could not delete file $fileName from container $containerName"

            return
        }

        log.info "Successfully deleted file $fileName from container $containerName"
    }

    @Override
    String getPermanentURL(String containerName, String fileName) {
        Blob blob = getBlob(containerName, fileName)

        return blob?.mediaLink()
    }

    @Override
    String getTemporaryURL(String containerName, String fileName, long expiration) {
        Blob blob = getBlob(containerName, fileName)

        return blob?.signUrl(expiration, TimeUnit.SECONDS)?.toString()
    }

    @Override
    boolean makeFilePublic(String containerName, String fileName) {
        return false
    }

    @Override
    boolean uploadFile(String containerName, File file, String fileName, boolean makePublic, long maxAge) {
        String contentType = new MimetypesFileTypeMap().getContentType(fileName)

        BlobId blobId = BlobId.of(containerName, fileName)
        BlobInfo blobInfo = BlobInfo.builder(blobId).contentType(contentType).build()

        Blob blob = gStorage.create(blobInfo, file.bytes)

        if (!blob || !blob.exists(Blob.BlobSourceOption.generationMatch())) {
            log.warn "Could not upload file $fileName"

            return false
        }

        log.info "Successfully uploaded file $fileName"

        return true
    }
}
