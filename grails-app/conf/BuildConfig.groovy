import grails.util.Environment

grails.project.class.dir = "target/classes"
grails.project.test.class.dir = "target/test-classes"
grails.project.test.reports.dir  = "target/test-reports"
grails.project.work.dir = "target/work"
grails.project.dependency.resolver = "maven"

if (Environment.current in [Environment.DEVELOPMENT, Environment.TEST]) {
    codenarc.propertiesFile = "grails-app/conf/codenarc.properties"
}

grails.project.dependency.resolution = {

    inherits("global")

    log "warn"
    repositories {
        grailsPlugins()
        grailsCentral()
        mavenCentral()
        mavenLocal()
    }

    dependencies {
        compile ("org.apache.jclouds.provider:cloudfiles-us:1.8.1", "org.apache.jclouds:jclouds-compute:1.8.1",
                "org.apache.jclouds.provider:aws-s3:1.8.1") {
                    excludes "jclouds-core"
                }

        compile("org.apache.jclouds:jclouds-core:1.8.1", "com.google.cloud:google-cloud-storage:0.3.0") {
            if (Environment.current == Environment.PRODUCTION) {
                excludes 'guice', 'http-client'
            }
        }

        compile("commons-fileupload:commons-fileupload:1.3.1")
        compile("com.google.guava:guava:19.0")
    }

    plugins {
        // Make sure to comment while packaging to allow Grails Mongodb plugin to install & hibernate to uninstall in parent app.
        runtime (":hibernate4:5.0.0.RC1") {
            export = false
        }
        build(":tomcat:7.0.52.1", ":release:3.0.1", ":rest-client-builder:2.0.1", ":codenarc:0.22") {
            export = false
        }
    }
}

codenarc.reports = {
    FileUploaderCodenarcHtmlReport('html') {
        outputFile = 'target/codenarc/file-uploader-codenarc-report.html'
        title = 'File Uploader Test Report'
    }
}
