/*
 * Copyright (c) 2011, CauseCode Technologies Pvt Ltd, India.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or
 * without modification, are not permitted.
 */

import com.causecode.fileuploader.CDNProvider
import com.causecode.fileuploader.util.Time

fileuploader {
    AmazonKey = "RANDOM_KEY"
    AmazonSecret = "RANDOM_SECRET"
    user {
        maxSize = 1024 * 1024 * 2 // 2 MB
        allowedExtensions = ["jpg","jpeg","gif","png"]
        storageTypes = "CDN"
        container = "causecode-1"
        provider = CDNProvider.AMAZON
        makePublic = true
    }
    image {
        maxSize = 1024 * 1024 * 2 // 2 MB
        allowedExtensions = ["jpg","jpeg","gif","png"]
        storageTypes = "CDN"
        container = "causecode-1"
        provider = CDNProvider.AMAZON
        expirationPeriod = Time.DAY * 365
    }
    profile {
        maxSize = 1024 * 1024 * 2 // 2 MB
        allowedExtensions = ["jpg","jpeg","gif","png"]
        storageTypes = "CDN"
        container = "causecode-1"
        provider = CDNProvider.AMAZON
        expirationPeriod = Time.DAY * 365
    }
}

environments {
    test {
        fileuploader {
            testGoogle {
                maxSize = 1024 * 1024 * 2 // 2 MB
                allowedExtensions = ["jpg","jpeg","gif","png","txt"]
                storageTypes = "CDN"
                container = "causecode"
                provider = CDNProvider.GOOGLE
                expirationPeriod = Time.DAY * 365
            }
            testAmazon {
                maxSize = 1024 * 1024 * 2 // 2 MB
                allowedExtensions = ["jpg","jpeg","gif","png","txt"]
                storageTypes = "CDN"
                container = "causecode"
                provider = CDNProvider.AMAZON
                expirationPeriod = Time.DAY * 365
            }
        }
    }
}
