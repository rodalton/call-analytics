# Watson Call Analytics 
This Java web-app is designed to run on IBM Cloud and leverages services from Watson to analyse call centre recordings. 

Services provided by this web-app; 
- access .wav files (call recordings) hosted in an [IBM Cloud Object Storage](https://www.ibm.com/cloud/object-storage) bucket
- transcribe these call recordings with [Watson Speech to Text](https://www.ibm.com/watson/services/speech-to-text/) 
- analyse the transcribed call for tone with [Watson Tone Analyzer](https://www.ibm.com/watson/services/tone-analyzer/)
- determine important keywords & entities referenced in the call using [Watson Natural Language Understanding](https://www.ibm.com/watson/services/natural-language-understanding/)
- persist call analytics data to a DB2 data store hosted on IBM Cloud 

![alt text](https://ibm.box.com/shared/static/lgzjxw7wdy2aaz2l621csdvf5u4kgtc5.jpg "High level architecture")

## Building from source 
Once you check out the code from GitHub, you can build it using Maven:
``` 
mvn clean install
```

## Services to create on IBM Cloud before deployment; 
- IBM Cloud Object Storage
- Speech to Text 
- Tone Analyzer
- Natural Language Classifier 
- Db2 Warehouse on Cloud 
