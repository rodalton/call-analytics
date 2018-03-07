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

### 1. Create the required IBM Cloud services
Create an instance of each of these services on IBM Cloud;
- IBM Cloud Object Storage
- Speech to Text
- Tone Analyzer
- Natural Language Classifier
- Db2 Warehouse on Cloud

### 2. Build from source
Clone this GitHub repository to your local machine
```
git clone https://github.com/rodalton/call-analytics.git
```

If needed, open the `resource.properties` file and update the IBM Cloud Object Storage endpoint details to align with the location of your IBM COS instance. See IBM Cloud Object Storage below to understand how this property is used.

Build the WAR file to deploy to IBM Cloud
```
mvn clean install
```
### 3. Create the db schema
Use the SQL from the `db2.ddl` file to build a db schema. Open the Db2 Warehouse on Cloud service created earlier, open the console and use the Run SQL option to build the schema. See Db2 Warehouse on IBM Cloud below for more information on our database structure.

### 4. Create an IBM COS bucket
Our web-app expects .wav files to be hosted in an IBM COS bucket. If required, create a new bucket to host your call recordings.

### 5. Update the manifest.yml file
Open the `manifest.yml` file and update. Include the names of the IBM Cloud services created above. Change the application name and other values as required.

### 6. Push the web-app to IBM Cloud
Using the IBM Cloud CLI, push the web-app to IBM Cloud. From a terminal window or command prompt, configure the CLI to use the correct API endpoint, login to IBM Cloud then push the app.
```
bx api
bx login
bx cf app push
```
### 7. Open the web-app in a browser and run
Open the web-app in a browser with the following URL `APP URL/home`
Select your IBM COS bucket, then click on Run



## IBM Cloud Services & our Call Analytics web-app
![alt text](https://ibm.box.com/shared/static/jwfhwkvs87vbw53peq6j78p98uc17xzk.png "Sequence Diagram")

#### IBM Cloud Object Storage
IBM Cloud Object Storage is used to host our call recordings. Our web-app uses the [IBM Cloud Object Storage Java SDK](https://github.com/IBM/ibm-cos-sdk-java) to access a bucket that hosts our calls.

Call recordings are expected in .wav format.

IBM Cloud Object Storage makes both regional and cross region endpoints available for connecting applications to IBM COS. The region endpoint is defined in the web-apps resource.properties file. Update the entry in this file before deploying the web-app to IBM Cloud.

#### Speech to Text
Watson Speech to Text (STT) is used to transcribe our call recordings. Our web-app uses the [Java SDK for Watson](https://github.com/watson-developer-cloud/java-sdk) when calling the STT service. We call the STT WebSockets endpoint passing our call recording to the API as an InputStream. [Speaker labels](https://console.bluemix.net/docs/services/speech-to-text/output.html#output) are applied to the response of the STT service to allow our web-app determine the utterance per speaker on the call.  

#### Tone Analyzer
Watson Tone Analyzer is used to determine the tone of each utterance on the call. As above, the STT service provides us with the utterance of each speaker, we use the [Customer Engagemement](https://console.bluemix.net/docs/services/tone-analyzer/using-tone-chat.html#using-the-customer-engagement-endpoint) of the Tone Analyzer service to provide a tone per utterance on the call.  

#### Natural Language Understanding
Watson Natural Language Understanding (NLU) is used to identify keywords and entities referenced in the call recording. Only keywords/entities with a confidence score of greater than 0.7 are persisted in the database.

#### Db2 Warehouse on IBM Cloud
Db2 Warehouse on Cloud is used to store the raw insights pulled from call recordings. Our db schema consists of just 4 tables and is structured as follows:

![alt text](https://ibm.box.com/shared/static/besjmwa5p5ixou2q247g51cwetyaol39.png "DB Schema")
