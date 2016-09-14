package com.causecode.fileuploader

import grails.test.mixin.TestFor
import grails.test.mixin.TestMixin
import grails.test.mixin.support.GrailsUnitTestMixin
import spock.lang.Specification

@TestFor(FileUploaderTagLib)
@TestMixin(GrailsUnitTestMixin)
class FileUploaderTagLibSpec extends Specification {

    void testPrettySizeBytes() {
        expect:
        applyTemplate('<fileuploader:prettysize size="850" />') == '850b'
    }

    void testPrettySize1KByte() {
        expect:
        applyTemplate('<fileuploader:prettysize size="1000" />') == '1kb'
    }

    void testPrettySize16KBytes() {
        expect:
        applyTemplate('<fileuploader:prettysize size="16000" />') == '16kb'
    }

    void testPrettySize18MBytes() {
        expect:
        applyTemplate('<fileuploader:prettysize size="18432000" />') == '18mb'
    }

    void testPrettySize2_5GBytes() {
        expect:
        applyTemplate('<fileuploader:prettysize size="2621440000" />') == '2.5gb'
    }
}