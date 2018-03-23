# Call Analytics with Watson
Our Call Analytics web-app is designed to access call recordings hosted on IBM Cloud Object Storage, transcribe these calls, then provide call insights using Watson.

Services provided by our Call Analytics web-app;
- access .wav files (call recordings) hosted in an [IBM Cloud Object Storage](https://www.ibm.com/cloud/object-storage) bucket
- transcribe these call recordings with [Watson Speech to Text](https://www.ibm.com/watson/services/speech-to-text/)
- analyse the transcribed call for tone with [Watson Tone Analyzer](https://www.ibm.com/watson/services/tone-analyzer/)
- determine important keywords & entities referenced in the call using [Watson Natural Language Understanding](https://www.ibm.com/watson/services/natural-language-understanding/)
- persist call analytics data to a Db2 data store using [Db2 Warehouse on IBM Cloud](https://www.ibm.com/cloud/db2-warehouse-on-cloud)

### High Level Architecture
![alt text](https://ibm.box.com/shared/static/lgzjxw7wdy2aaz2l621csdvf5u4kgtc5.jpg "High level architecture")

## Build & deploy to IBM Cloud
Use the following steps to build our Call Analytics web-app from source and deploy to IBM Cloud.

---
**Note:** The following software should be installed and available for use before proceeding: 
- Git, see install instructions here: https://git-scm.com/book/en/v2/Getting-Started-Installing-Git
- Maven, see install instructions here: https://maven.apache.org/install.html
- IBM Cloud CLI, see install instructions here: https://console.bluemix.net/docs/cli/reference/bluemix_cli/download_cli.html#download_install

---

### 1. Create the required IBM Cloud services
1.1 Create an instance of each of the following services on IBM Cloud. From the IBM Cloud Catalog create: 
- Speech to Text
- Tone Analyzer
- Natural Language Understanding
- Db2 Warehouse on Cloud
- IBM Cloud Object Storage
	- Note: also create an IBM COS bucket to store call recordings (.wav files) and note the associated endpoint URL

### 2. Build from source
2.1 Clone this GitHub repository to your local machine. Open a terminal window and issue the following command: 
```
git clone https://github.com/rodalton/call-analytics.git
```

2.2 Open the `resource.properties` file on your local machine and update the IBM Cloud Object Storage endpoint details (if needed) to align with the location of your IBM COS instance. These endpoint details are used by our web-app when reading files from the IBM COS bucket created in Step 1 above. 

2.3 Next we'll build the WAR file that we'll later push to IBM Cloud. From a terminal window, issue the following command: 
```
mvn clean install
```
### 3. Create the db schema
Use the SQL from the `db2.ddl` file to build a db schema. 

3.1 Open `db2.ddl` with a text editor and copy the content. Next, open the Db2 Warehouse on Cloud service created earlier, open the console and use the Run SQL option to build the schema. 

See Db2 Warehouse on IBM Cloud below for more information on our database structure.

### 4. Create an IBM COS alias
An IBM COS service instance is global on IBM Cloud. Cloud Foundry apps require a regional alias for an IBM COS service before a CF app can bind to the service. 

4.1 In order to bind our IBM COS service to our web-app, we'll first create a regional alias for our IBM COS service.

From a terminal window, issue the following command
```
bx resource service-alias-create ALIAS_NAME --instance-name NAME
``` 

Replace `ALIAS_NAME` with your preferred alias name (this can be the same as the current service name). 
Replace `NAME` in the command above with the name of the IBM COS instance created in step 1 above.
 
### 5. Update the manifest.yml file
The `manifest.yml` cloned earlier contains deployment values for our application. 
 
5.1 Open the `manifest.yml` file in a text editor and update with the values specific to your environment. In particular, update the `manifest.yml` with the names of the IBM Cloud services created above. For IBM COS, use the ALIAS_NAME provided for the service in Step 4.1 above. 

Change the application name and other values as required then save the `manifest.yml` file. 

### 6. Push the web-app to IBM Cloud
Using the IBM Cloud CLI, push the web-app to IBM Cloud. 

6.1 From a terminal window or command prompt, configure the CLI to use the correct IBM Cloud API endpoint, login to IBM Cloud then push the app.

The set of commands you'll use are as follows: 
```
bx api
bx login
bx app push
```
### 7. Open the web-app in a browser and run

7.1 Open the web-app deployed to IBM Cloud in a browser with the following URL `APP URL/home`

7.2 Select your IBM COS bucket, then click on Run

Once our web-app has processed all .wav files stored on IBM COS, view the data gathered in our relational data store using Db2 Warehouse on Cloud. 

---
## IBM Cloud Services
![alt text](https://ibm.box.com/shared/static/jwfhwkvs87vbw53peq6j78p98uc17xzk.png "Sequence Diagram")

### IBM Cloud Object Storage
IBM Cloud Object Storage is used to host our call recordings. Our web-app uses the [IBM Cloud Object Storage Java SDK](https://github.com/IBM/ibm-cos-sdk-java) to access a bucket that hosts our calls.

Call recordings are expected in .wav format.

IBM Cloud Object Storage makes both regional and cross region endpoints available for connecting applications to IBM COS. The region endpoint is defined in the web-apps resource.properties file. Update the entry in this file before deploying the web-app to IBM Cloud.

### Speech to Text
Watson Speech to Text (STT) is used to transcribe our call recordings. Our web-app uses the [Java SDK for Watson](https://github.com/watson-developer-cloud/java-sdk) when calling the STT service. We call the STT WebSockets endpoint passing our call recording to the API as an InputStream. [Speaker labels](https://console.bluemix.net/docs/services/speech-to-text/output.html#output) are applied to the response of the STT service to allow our web-app determine the utterance per speaker on the call.  

### Tone Analyzer
Watson Tone Analyzer is used to determine the tone of each utterance on the call. As above, the STT service provides us with the utterance of each speaker, we use the [Customer Engagemement](https://console.bluemix.net/docs/services/tone-analyzer/using-tone-chat.html#using-the-customer-engagement-endpoint) of the Tone Analyzer service to provide a tone per utterance on the call.  

### Natural Language Understanding
Watson Natural Language Understanding (NLU) is used to identify keywords and entities referenced in the call recording. Only keywords/entities with a confidence score of greater than 0.7 are persisted in the database.

### Db2 Warehouse on IBM Cloud
Db2 Warehouse on Cloud is used to store the raw insights gathered from call recordings. Our db schema consists of just 4 tables and is structured as follows:

![alt text](https://ibm.box.com/shared/static/besjmwa5p5ixou2q247g51cwetyaol39.png "DB Schema")

The CALLS table stores; 
- the last modified time of a call recording  
- last modified date 
- call duration in seconds 

The UTTERANCES table stores; 
- each speaker utterance from the call transcript
- the tone per utterance 

The KEYWORD table stores; 
- keywords from the call transcript
- only keywords with a confidence score greater than 0.5 are stored 

The ENTITIES table stores; 
- entities from the call transcript
- only entities with a confidence score greater than 0.5 are stored  
