package com.lucastex.grails.fileuploader


import org.springframework.context.MessageSource
import org.springframework.context.support.ResourceBundleMessageSource
import java.nio.file.FileSystems
import java.nio.file.Path
import org.codehaus.groovy.grails.commons.DefaultGrailsApplication
import spock.lang.*
import grails.test.spock.IntegrationSpec
import com.lucastex.grails.fileuploader.FileUploaderService
import com.lucastex.grails.fileuploader.UFileType

class FileUploaderServiceSpec extends IntegrationSpec {

    FileUploaderService fileUploaderService
    def grailsApplication = new DefaultGrailsApplication()

    String group
    UFile ufileInstance

    MessageSource getI18n() {
        URL url = new File('grails-app/i18n').toURI().toURL()
        MessageSource messageSource = new ResourceBundleMessageSource()
        messageSource.bundleClassLoader = new URLClassLoader(url)
        messageSource.basename = 'messages'
        return messageSource
    }

    private void setupConfig() {
        Map logoConfig = [
            maxSize: 1024 * 1024 * 10,
            allowedExtensions: ["jpg","jpeg"],
            path: "./web-app/user-content/images/logo/",
            container: "altruhelp_p"
        ]
        fileUploaderService.grailsApplication.config.fileuploader = [storageTypes: "", logo: logoConfig]
    }

    private File getTestFile(String fileName = "") {
        // A destination temporary path to copy our test file, since after ufile upload the file gets deleted
        fileName = fileName ?: "test-logo.png"
        Path destination = FileSystems.getDefault().getPath(fileName)
        Path source = FileSystems.getDefault().getPath("test", "integration", "test-files", "logo.png")

        // Copying file to another location so that original file does not get deleted from repo.
        File testFile
        try {
            java.nio.file.Files.copy(source, destination)
        } catch (Exception e) {
            log.warn "Exception occured while copying test file", e
        }
        testFile = new File("./${fileName}")

        return testFile
    }

    def setup() {
        fileUploaderService = new FileUploaderService()
        fileUploaderService.grailsApplication = grailsApplication
        fileUploaderService.messageSource = getI18n()
        group = "logo" // FileUploader Plugin Configuration group
    }

    def cleanup() {
    }

    void "Test: saveFile() when empty file parameter passed."() {
        given:
        def file = null

        when:
        ufileInstance = fileUploaderService.saveFile(group, file)

        then: "Method should return null value"
        assert ufileInstance == null
    }

    void "Test: saveFile() method should throw exception for missing fileuploader configuration."() {
        given:
        FileUploaderServiceException configException = null
        File testFile = getTestFile()

        when:
        try {
            ufileInstance = fileUploaderService.saveFile(group, testFile)
        } catch (Exception e) {
            configException = e
        }

        then: "Method should throw exception for missing file uploader plugin configuration."
        assert configException.message == "No config defined for group [${group}]. " +
            "Please define one in your Config file."

        cleanup: "Deleting test file for other test cases."
        testFile?.delete()
    }

    void "Test: saveFile() method should throw exception for invalid file extention."() {
        given: "Applying FileUploader configuration and creating file with unauthorized extension"
        setupConfig()
        FileUploaderServiceException unauthorizedExtension = null
        File testFile = getTestFile("test-logo.tif")

        when:
        try {
            ufileInstance = fileUploaderService.saveFile(group, testFile)
        } catch (Exception e) {
            unauthorizedExtension = e
        }

        then: "Method should throw exception for invalid file extention."
        String exceptionMessage = "The file you sent has an unauthorized extension (tif)." +
            " Allowed extensions for this upload are " + 
            "${grailsApplication.config.fileuploader.logo.allowedExtensions}"

        assert unauthorizedExtension?.message == exceptionMessage

        cleanup: "Deleting test file for other test cases."
        testFile?.delete()
    }

    void "Test: saveFile() method should throw exception for Max file size."() {
        given: "Applying FileUploader configuration and updating file size configuration."
        setupConfig()
        grailsApplication.config.fileuploader.logo.maxSize = 1024 // 1kb
        FileUploaderServiceException maxFileSizeExtension = null
        File testFile = getTestFile()

        when:
        try {
            ufileInstance = fileUploaderService.saveFile(group, testFile)
        } catch (Exception e) {
            maxFileSizeExtension = e
        }

        then: "Method should throw exception for Max file size."
        String exceptionMessage = "Sent file is bigger than allowed. Max file size is 1 kb"
        assert maxFileSizeExtension?.message == exceptionMessage

        cleanup: "Deleting test file for other test cases."
        testFile?.delete()
    }

    void "Test: saveFile() method should save file successfully."() {
        given: "Applying FileUploader configuration."
        setupConfig()
        FileUploaderServiceException exception = null
        File testFile = getTestFile()

        when:
        try {
            ufileInstance = fileUploaderService.saveFile(group, testFile)
        } catch (Exception e) {
            exception = e
        }

        then: "Method should create UFile Instance successfully."
        assert exception == null
        assert ufileInstance
        assert ufileInstance.id != null
        assert ufileInstance.name == "test-logo"
        assert ufileInstance.extension == "png"
        assert ufileInstance.type == UFileType.LOCAL
        assert ufileInstance.fileGroup == group
        assert ufileInstance.provider == null

        cleanup: "Deleting test file for other test cases."
        testFile?.delete()
    }

    void "Test: cloneFile() when ufileInstance parameter not passed."() {
        given:
        def file = null

        when: "When empty uFile parameter passed"
        ufileInstance = fileUploaderService.cloneFile(group, file)

        then: "Method should return null value"
        assert ufileInstance == null
    }

    void "Test: cloneFile() when ufileInstance parameter passed."() {
        given:
        setupConfig()
        FileUploaderServiceException exception = null
        File testFile = getTestFile()

        try {
            ufileInstance = fileUploaderService.saveFile(group, testFile)
        } catch (Exception e) {
            exception = e
        }

        when:
        UFile clonedUfileInstance = fileUploaderService.cloneFile(group, ufileInstance)

        then: "Method should return cloned file"
        assert clonedUfileInstance != null
        assert clonedUfileInstance
        assert clonedUfileInstance.id != null
        assert clonedUfileInstance.name == "test-logo"
        assert clonedUfileInstance.extension == "png"
        assert clonedUfileInstance.type == UFileType.LOCAL
        assert clonedUfileInstance.fileGroup == group
        assert clonedUfileInstance.provider == null

        cleanup: "Deleting test file for other test cases."
        testFile?.delete()
    }

}
