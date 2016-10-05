package com.causecode.fileuploader.cdn.google

import com.causecode.fileuploader.GoogleStorageException
import com.causecode.fileuploader.UploadFailureException
import com.google.cloud.storage.*
import grails.test.mixin.TestMixin
import grails.test.mixin.support.GrailsUnitTestMixin
import spock.lang.Specification

/**
 * See the API for {@link grails.test.mixin.support.GrailsUnitTestMixin} for usage instructions
 */
@TestMixin(GrailsUnitTestMixin)
class GoogleCDNFileUploaderImplSpec extends Specification {

    GoogleCDNFileUploaderImpl googleCDNFileUploaderImpl = new GoogleCDNFileUploaderImpl()

    def setup() {
        GoogleCDNFileUploaderImpl.metaClass.getBlob = { String containerName, String fileName ->
            return new Blob(googleCDNFileUploaderImpl.gStorage,
                    new BlobInfo.BuilderImpl(new BlobId("dummyContainer", "testFile", 2l)))
        }

        Storage.metaClass.create = { BucketInfo var1, Storage.BucketTargetOption... var2 ->
            throw new StorageException(1, "Test exception")
        }
    }

    def cleanup() {
    }

    void "test deleteFile for GoogleStorageException"() {
        given: "mocked delete method for Blob class"
        Blob.metaClass.delete = { Blob.BlobSourceOption... options ->
            return false
        }

        when: "deleteFile is called"
        googleCDNFileUploaderImpl.deleteFile("dummyContainer", "testFile")

        then: "it should throw an exception"
        GoogleStorageException e = thrown()
    }

    void "test uploadFile for UploadFailureException"() {
        given: "A file instance"
        File file = new File('test.txt')
        file.createNewFile()
        file << 'This is a test document.'

        BlobId.metaClass.of = { String containerName, String fileName ->
            return new BlobId("dummyContainer", "test", 2l)
        }

        when: "uploadFile is called"
        googleCDNFileUploaderImpl.uploadFile("dummyContainer", file, "test", false, 3600l)

        then: "it should throw UploadFailureException"
        UploadFailureException e = thrown()
    }

    void "test createContainer for GoogleStorageException"() {
        given: "mocked delete method for Blob class"

        when: "deleteFile is called"
        googleCDNFileUploaderImpl.createContainer("dummyContainer")

        then: "it should throw an exception"
        GoogleStorageException e = thrown()
    }
}