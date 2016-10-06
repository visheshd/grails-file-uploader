# File-Uploader Plugin

#### Causecode Technologies Pvt. Ltd.

## Access Protect Controller & Actions

```
'/fileUploader/show': ['ROLE_USER'], (According to application needs)
'/fileUploader/download': ['ROLE_USER'], (According to application needs)
```

## Uploading files to CDN

To upload files to CDN (Supports both Google and Amazon) one must have some configuration like given below:

```
import com.causecode.fileuploader.CDNProvider

grails.tempDirectory = "./temp-files"     // Required to store files temporarily. Must not ends with "/"

fileuploader {

    AmazonKey = "somekey"	// For amazon S3
    AmazonSecret = "somesecret"
    defaultContainer = "anyConatainer"  // Container to move local files to cloud

    degreeApplication {			// Non CDN files, will be stored in local directory.
        maxSize = 1000 * 1024 //256 kbytes
        allowedExtensions = ["xls"]
        path = "./web-app/degree-applications"
        storageTypes = ""
    }
    userAvatar {
        maxSize = 1024 * 1024 * 2 //256 kbytes
        allowedExtensions = ["jpg","jpeg","gif","png"]
        storageTypes = "CDN"
        container = "anyContainerName"
        provider = CDNProvider.GOOGLE
        expirationPeriod = Time.Day * 30 // Time in seconds
    }
    logo {
        maxSize = 1024 * 1024 * 2 //256 kbytes
        allowedExtensions = ["jpg","jpeg","gif","png"]
        storageTypes = "CDN"
        container = "anyContainerName"
        provider = CDNProvider.AMAZON
        expirationPeriod = 60 * 60 * 24 * 2 // Two hours
    }
}
```

1. To enable CDN uploading to any group just set **storageType** to **CDN** & provide a container name.

2. By default path URL retrieved from Amazon S3 service is temporary URL, which will be valid for 30 days bydefault. Which
can be overwritten for group level configuration by setting **expirationPeriod**. This period must be of long type in seconds.

3. For Google Cloud Authentication, you will have to add the key (JSON file downloaded from the Cloud Console) to the server
and add an environment variable called `GOOGLE_APPLICATION_CREDENTIALS` which points to the file. In bashrc file, add:

```
# Google Default Credentials
export GOOGLE_APPLICATION_CREDENTIALS='/path/to/key.json'
```
Note: Leave the file name as downloaded from the Google Cloud console.

## For Grails 2.x,
  
1. To release plugin locally run grails clean && grails compile && grails prod maven-install.
  
2. To release to causecode maven repo, refer https://bitbucket.org/causecode/knowledge/src/f975c72b1ffc67dc5ec5f7bf0807ef6188f6c262/5.grails/plugins/deploy-plugin-to-maven-repository.md

