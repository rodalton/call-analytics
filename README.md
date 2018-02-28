# Watson Call Analytics 
This Java web-app is designed to; 
- access .wav files (call recordings) hosted in an IBM Cloud Object Storage bucket
- transcribe these call recording with Watson Speech to Text 
- analyse the transribed call for tone with Watson Tone Analyzer
- determine important keywords & entities referenced in the call using Watson Natural Language Understanding
- persist call analytics data to a DB2 data store hosted on IBM Cloud 

Building from source 
Once you check out the code from GitHub, you can build it using Maven:
mvn clean install

Services to create on IBM Cloud before deployment; 
- IBM Cloud Object Storage
- Speech to Text 
- Tone Analyzer
- Natural Language Classifier 
- Db2 Warehouse on Cloud 
