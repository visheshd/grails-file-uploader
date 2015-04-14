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
            allowedExtensions: ["jpg", "jpeg", "png"],
            path: "./web-app/user-content/images/logo/",
            container: "altruhelp_test"
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
        /*fileUploaderService = new FileUploaderService()
        fileUploaderService.grailsApplication = grailsApplication
        fileUploaderService.messageSource = getI18n()*/
        group = "logo" // FileUploader Plugin Configuration group
    }

    def cleanup() {
    }

    void "test saveFile method, when empty file parameter passed."() {
        given:
        def file = null

        when: "Empty file parameter passed"
        ufileInstance = fileUploaderService.saveFile(group, file)

        then: "Method should return null value"
        ufileInstance == null
        UFile.list() == []
    }

    void "test saveFile method , when file group configuration does not exists."() {
        given:
        File testFile = getTestFile()

        when: "service method is called for a file with which dosen't belong's to any group configuration"
        ufileInstance = fileUploaderService.saveFile(group, testFile)

        then: "Method should throw exception for missing file uploader plugin configuration."
        FileUploaderServiceException configException = thrown()
        configException.message == "No config defined for group [${group}]. " +
            "Please define one in your Config file."

        cleanup: "Deleting test file for other test cases."
        testFile?.delete()
    }

    void "test saveFile method, when a file with invalid extension is passed."() {
        given: "Applying FileUploader configuration and creating file with unauthorized extension"
        setupConfig()
        File testFile = getTestFile("test-logo.tif")
        String exceptionMessage = "The file you sent has an unauthorized extension (tif)." +
            " Allowed extensions for this upload are " + 
            "${grailsApplication.config.fileuploader.logo.allowedExtensions}"

        when: "service method is called for a file with invalid extension"
        ufileInstance = fileUploaderService.saveFile(group, testFile)

        then: "Method should throw exception for invalid file extention."
        FileUploaderServiceException unauthorizedException = thrown()
        unauthorizedException?.message == exceptionMessage

        cleanup: "Deleting test file for other test cases."
        testFile?.delete()
    }

    void "test saveFile method, when file with maximum size passed."() {
        given: "Applying FileUploader configuration and updating file size configuration."
        setupConfig()
        grailsApplication.config.fileuploader.logo.maxSize = 1024 // 1kb
        File testFile = getTestFile()
        String exceptionMessage = "Sent file is bigger than allowed. Max file size is 1 kb"

        when: "File with maximum size passed"
        ufileInstance = fileUploaderService.saveFile(group, testFile)

        then: "Method should throw exception for Max file size."
        FileUploaderServiceException maxFileSizeException= thrown()
        maxFileSizeException?.message == exceptionMessage

        cleanup: "Deleting test file for other test cases."
        testFile?.delete()
    }

    void "test saveFile method, with file which pass all validations."() {
        given: "Applying FileUploader configuration."
        setupConfig()
        File testFile = getTestFile()

        when: "File with all validations passed."
        ufileInstance = fileUploaderService.saveFile(group, testFile)

        then: "Method should create UFile instance and file should be moved to configured location locally"
        ufileInstance
        ufileInstance.id != null
        ufileInstance.name == "test-logo"
        ufileInstance.extension == "png"
        ufileInstance.type == UFileType.LOCAL
        ufileInstance.fileGroup == group
        ufileInstance.provider == null
        ufileInstance.path =~ "/web-app/user-content/images/logo/"

        cleanup: "Deleting test file for other test cases."
        testFile?.delete()
    }

    void "test cloneFile method, when ufileInstance parameter not passed."() {

        when: "When empty uFile parameter passed"
        ufileInstance = fileUploaderService.cloneFile(group, null)

        then: "Method should return null value"
        ufileInstance == null
    }

    void "test cloneFile method for valid UFile instance"() {
        given:
        setupConfig()
        File testFile = getTestFile()
        ufileInstance = fileUploaderService.saveFile(group, testFile)

        when: "UFileInstance parameter passed"
        UFile clonedUfileInstance = fileUploaderService.cloneFile(group, ufileInstance)

        then: "Method should return cloned file"
        clonedUfileInstance
        clonedUfileInstance.id != null
        clonedUfileInstance.name == "test-logo.png"
        clonedUfileInstance.extension == "png"
        clonedUfileInstance.type == UFileType.LOCAL
        clonedUfileInstance.fileGroup == group
        clonedUfileInstance.provider == null
        ufileInstance.path =~ "/web-app/user-content/images/logo/"

        cleanup: "Deleting test file for other test cases."
        testFile?.delete()
    }

}
