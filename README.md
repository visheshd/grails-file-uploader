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
import com.causecode.fileuploader.util.Time

grails.tempDirectory = "./temp-files"     // Required to store files temporarily. Must not ends with "/"

fileuploader {

    storageProvider {
        amazon {
            AmazonKey = "somekey"	// For amazon S3
            AmazonSecret = "somesecret"
        }
        google {
            authFile = '/path/to/key.json'

            // This is must for both cases, i.e reading file using the path in 'auth' or reading hard coded credentials from here itself.
            project_id = '<project_id_provided_in_json_key_file>'

            // Other required values from JSON key file.
            private_key_id = '<private_key_id_provided_in_json_key_file>'
            private_key = '<private_key_provided_in_json_key_file>'
            client_email = '<client_email_provided_in_json_key_file>'
            client_id = '<client_id_provided_in_json_key_file>'

            // Optional, this defaults to 'service_account'.
            type = ''
        }
    }

    groups {
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


## Publish plugin

### Local maven
- Run following commands to publish plugin to local maven repository.
```
grails clean
grails compile
grails maven-install
```

Note: Make sure you are using Grails version 2.5.0 and Java version 1.7

## Nexus repository
- Run the following commands to publish the plugin to nexus.
 ```
 grails clean
 grails compile
 grails publish-plugin
 ```

 ### Credentials
 Create a `settings.groovy` file in `~/.grails/` directory if not present and add the following config:
 ```
 // use username and password of nexus repo
 grails.project.repos.ccRepo.username = "foo"
 grails.project.repos.ccRepo.password = "bar"

 grails.project.dependency.authentication = {
   credentials {
     id = "ccRepo"
     username = "foo"
     password = "bar"
   }
 }
 ```
