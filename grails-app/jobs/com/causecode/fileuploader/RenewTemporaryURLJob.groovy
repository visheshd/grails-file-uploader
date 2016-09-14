package com.causecode.fileuploader

class RenewTemporaryURLJob {

    static triggers = {
        cron name: "RenewTempURLTrigger", cronExpression: "0 0 2 * * ? *"   // Once every twenty four hours at 2am
    }

    def fileUploaderService
    // def grailsEvents

    def execute() {
        log.debug "Started executing RenewTemporaryURLJob.."
        fileUploaderService.renewTemporaryURL()

        /*
         * Trigger event to notity the installing app for any further app specific processing.
         * 
         * TODO This is not working. Need to investigate grails events.
         */
        // grailsEvents.event("file-uploader", "on-ufile-renewal")

        log.debug "Finished executing RenewTemporaryURLJob."
    }

}