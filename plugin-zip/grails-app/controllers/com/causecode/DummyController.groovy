package com.causecode
class DummyController {
    def fileUploaderService
    def index() {
        fileUploaderService.renewTemporaryURL(true)
    }
}
