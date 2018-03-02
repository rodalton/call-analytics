# Watson Call Analytics
This Java web-app is designed to run on IBM Cloud and leverages services from Watson to analyse call centre recordings.

Services provided by this web-app;
- access .wav files (call recordings) hosted in an [IBM Cloud Object Storage](https://www.ibm.com/cloud/object-storage) bucket
- transcribe these call recordings with [Watson Speech to Text](https://www.ibm.com/watson/services/speech-to-text/)
- analyse the transcribed call for tone with [Watson Tone Analyzer](https://www.ibm.com/watson/services/tone-analyzer/)
- determine important keywords & entities referenced in the call using [Watson Natural Language Understanding](https://www.ibm.com/watson/services/natural-language-understanding/)
- persist call analytics data to a DB2 data store hosted on IBM Cloud

### High Level Architecture
![alt text](https://ibm.box.com/shared/static/lgzjxw7wdy2aaz2l621csdvf5u4kgtc5.jpg "High level architecture")

## Build & deploy to IBM Cloud
Use the following steps to build from source and deploy to IBM Cloud.

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

If needed, open the `resource.properties` file and update the IBM Cloud Object Storage endpoint details to align with the location of your IBM COS instance.

Build the WAR file to deploy to IBM Cloud
```
mvn clean install
```
### 3. Create the db schema
Use the SQL from the `db2.ddl` file to build a db schema. Open the Db2 Warehouse on Cloud service created earlier, open the console and use the Run SQL option to build the schema.

### 4. Create an IBM COS bucket
This web-app expects .wav files to be hosted in an IBM COS bucket. If required, create a new bucket to host your call recordings.

### 5. Update the manifest.yml file
Open the `manifest.yml` file and update. Include the names of the IBM Cloud services created above. Change the application name and other values as required.

### 6. Push the web-app to IBM Cloud
Using the IBM Cloud CLI, push the web-app to IBM Cloud
```
bx api
bx login
bx cf app push
```
### 7. Open the web-app in a browser and run
Open the web-app in a browser with the following URL `APP URL/home`
Select your IBM COS bucket, then click on Run
