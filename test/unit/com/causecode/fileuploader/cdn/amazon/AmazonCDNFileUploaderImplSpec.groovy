package com.causecode.fileuploader.cdn.amazon

import com.causecode.fileuploader.UploadFailureException
import grails.test.mixin.TestMixin
import grails.test.mixin.support.GrailsUnitTestMixin
import org.jclouds.aws.s3.AWSS3Client
import org.jclouds.blobstore.KeyNotFoundException
import org.jclouds.s3.domain.S3Object
import org.jclouds.s3.options.CopyObjectOptions
import org.jclouds.s3.options.PutObjectOptions
import spock.lang.Specification
/**
 * See the API for {@link grails.test.mixin.support.GrailsUnitTestMixin} for usage instructions
 */
@TestMixin(GrailsUnitTestMixin)
class AmazonCDNFileUploaderImplSpec extends Specification {

    AmazonCDNFileUploaderImpl amazonCDNFileUploaderImpl = new AmazonCDNFileUploaderImpl()
    def setup() {
        AWSS3Client.metaClass.putObject = { String s, S3Object s3Object, PutObjectOptions... putObjectOptionses ->
            throw new Exception("Test exception")
        }
        AWSS3Client.metaClass.copyObject = { String s, String s1, String s2, String s3, CopyObjectOptions... copyObjectOptionses ->
            throw new KeyNotFoundException("dummyContainer", "dummyKey","Test exception")
        }
    }

    def cleanup() {
    }

    void "test uploadFile for UploadFailureException"() {
        given: "A file instance"
        File file = new File('test.txt')
        file.createNewFile()
        file << 'This is a test document.'

        when: "uploadFile is called"
        amazonCDNFileUploaderImpl.uploadFile("dummyContainer", file, "test", false, 3600l)

        then: "it should throw UploadFailureException"
        UploadFailureException e = thrown()
    }

    void "test updatePreviousFileMetaData for handling KeyNotFoundException"() {
        given: "A file instance"
        File file = new File('test.txt')
        file.createNewFile()
        file << 'This is a test document.'

        when: "uploadFile is called"
        amazonCDNFileUploaderImpl.updatePreviousFileMetaData("dummyContainer", "test", false, 3600l)

        then: "it should handle KeyNotFoundException"
    }
}
