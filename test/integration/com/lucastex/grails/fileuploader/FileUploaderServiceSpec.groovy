package com.lucastex.grails.fileuploader


import grails.test.spock.IntegrationSpec

import java.nio.file.FileSystems
import java.nio.file.Path
import org.apache.commons.fileupload.FileItem
import org.apache.commons.fileupload.disk.DiskFileItemFactory
import org.apache.commons.io.IOUtils
import org.codehaus.groovy.grails.commons.DefaultGrailsApplication
import org.springframework.web.multipart.commons.CommonsMultipartFile
import spock.lang.*

class FileUploaderServiceSpec extends IntegrationSpec {

    FileUploaderService fileUploaderService
    def grailsApplication = new DefaultGrailsApplication()

    String group
    UFile ufileInstance
    Map logoConfig

    private void setupConfig() {
        logoConfig = [
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
        configException.message == "No config defined for group [${group}]. Please define one in your Config file."

        cleanup: "Deleting test file for other test cases."
        testFile?.delete()
    }

    void "test saveFile method, when a file with invalid extension is passed."() {

        given: "Applying FileUploader configuration and creating file with unauthorized extension"
        setupConfig()
        File testFile = getTestFile("test-logo.tif")
        String exceptionMessage = fileUploaderService.messageSource.getMessage("fileupload.upload.unauthorizedExtension",
            ["tif", logoConfig.allowedExtensions] as Object[], null)

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

        def maxSizeInKb = ((int) (logoConfig.maxSize)) / 1024
        String exceptionMessage = fileUploaderService.messageSource.getMessage("fileupload.upload.fileBiggerThanAllowed",
            [maxSizeInKb] as Object[], null)

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

    void "test saveFile method, with custom file name parameter passed."() {

        given: "Applying FileUploader configuration."
        setupConfig()
        File testFile = getTestFile()

        when: "File with all validations passed."
        ufileInstance = fileUploaderService.saveFile(group, testFile, "custom-test-logo")

        then: "Method should create UFile instance and custom file name should be applied."
        ufileInstance
        ufileInstance.id != null
        ufileInstance.name == "custom-test-logo"
        ufileInstance.extension == "png"
        ufileInstance.type == UFileType.LOCAL
        ufileInstance.fileGroup == group
        ufileInstance.provider == null
        ufileInstance.path =~ logoConfig.path // Check setupConfig for details

        cleanup: "Deleting test file for other test cases."
        testFile?.delete()
    }

    void "test saveFile method, with custom file name string contains extention parameter passed."() {

        given: "Applying FileUploader configuration."
        setupConfig()
        File testFile = getTestFile()

        when: "File with all validations passed."
        ufileInstance = fileUploaderService.saveFile(group, testFile, "custom-test-logo.png")

        then: "Method should create UFile instance and custom file name with removed extention should be applied."
        ufileInstance
        ufileInstance.id != null
        ufileInstance.name == "custom-test-logo"
        ufileInstance.extension == "png"
        ufileInstance.type == UFileType.LOCAL
        ufileInstance.fileGroup == group
        ufileInstance.provider == null
        ufileInstance.path =~ logoConfig.path // Check setupConfig for details

        cleanup: "Deleting test file for other test cases."
        testFile?.delete()
    }

    void "test saveFile method, with file of Commons Multipart File type."() {

        given: "Applying FileUploader configuration and creating CommonsMultipart test file."
        setupConfig()
        DiskFileItemFactory factory = new DiskFileItemFactory()
        FileItem fileItem = factory.createItem( "file", "multipart/form-data", false, "logo.png" )
        IOUtils.copy(new FileInputStream(getTestFile()), fileItem.getOutputStream())
        CommonsMultipartFile testFile = new CommonsMultipartFile(fileItem)


        when: "CommonsMultipart file with custom name parameter passed."
        ufileInstance = fileUploaderService.saveFile(group, testFile, "custom-test-logo")

        then: "Method should create UFile instance and custom file name with removed extention should be applied."
        ufileInstance
        ufileInstance.id != null
        ufileInstance.name == "custom-test-logo"
        ufileInstance.extension == "png"
        ufileInstance.type == UFileType.LOCAL
        ufileInstance.fileGroup == group
        ufileInstance.provider == null
        ufileInstance.path =~ logoConfig.path // Check setupConfig for details
    }
    void "test saveFile method, with CommonsMultipart File and custom file name parameter contains extention passed."() {

        given: "Applying FileUploader configuration and creating CommonsMultipart test file."
        setupConfig()
        DiskFileItemFactory factory = new DiskFileItemFactory()
        FileItem fileItem = factory.createItem( "file", "multipart/form-data", false, "logo.png" )
        IOUtils.copy(new FileInputStream(getTestFile()), fileItem.getOutputStream())
        CommonsMultipartFile testFile = new CommonsMultipartFile(fileItem)


        when: "CommonsMultipart file with custom name parameter contains extention passed."
        ufileInstance = fileUploaderService.saveFile(group, testFile, "custom-test-logo.png")

        then: "Method should create UFile instance and custom file name with removed extention should be applied."
        ufileInstance
        ufileInstance.id != null
        ufileInstance.name == "custom-test-logo"
        ufileInstance.extension == "png"
        ufileInstance.type == UFileType.LOCAL
        ufileInstance.fileGroup == group
        ufileInstance.provider == null
        ufileInstance.path =~ logoConfig.path // Check setupConfig for details
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
        clonedUfileInstance.name == "test-logo"
        clonedUfileInstance.extension == "png"
        clonedUfileInstance.type == UFileType.LOCAL
        clonedUfileInstance.fileGroup == group
        clonedUfileInstance.provider == null
        ufileInstance.path =~ logoConfig.path // Check setupConfig for details

        cleanup: "Deleting test file for other test cases."
        testFile?.delete()
    }

}
