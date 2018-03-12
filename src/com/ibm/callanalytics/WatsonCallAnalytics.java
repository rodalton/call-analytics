package com.ibm.callanalytics;


import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.gson.JsonObject;
import com.ibm.cloud.objectstorage.ClientConfiguration;
import com.ibm.cloud.objectstorage.auth.AWSCredentials;
import com.ibm.cloud.objectstorage.auth.AWSStaticCredentialsProvider;
import com.ibm.cloud.objectstorage.auth.BasicAWSCredentials;
import com.ibm.cloud.objectstorage.client.builder.AwsClientBuilder.EndpointConfiguration;
import com.ibm.cloud.objectstorage.oauth.BasicIBMOAuthCredentials;
import com.ibm.cloud.objectstorage.services.s3.AmazonS3;
import com.ibm.cloud.objectstorage.services.s3.AmazonS3ClientBuilder;
import com.ibm.cloud.objectstorage.services.s3.model.ListObjectsRequest;
import com.ibm.cloud.objectstorage.services.s3.model.ObjectListing;
import com.ibm.cloud.objectstorage.services.s3.model.S3Object;
import com.ibm.cloud.objectstorage.services.s3.model.S3ObjectInputStream;
import com.ibm.cloud.objectstorage.services.s3.model.S3ObjectSummary;

/**
 * Servlet implementation class CallTranscript
 */
@WebServlet("/call_analytics")
public class WatsonCallAnalytics extends HttpServlet {
	private static final long serialVersionUID = 1L;

	/**
	 * Default constructor. 
	 */
	public WatsonCallAnalytics() {
		// TODO Auto-generated constructor stub
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		try{
			//Passed in from html form 
			String bucketName = request.getParameter("bucket");

			AmazonS3 _s3Client = createClient();

			//File metadata from S3 store 
			String time; 
			String date; 
            SimpleDateFormat dateFormat; 
			
			//Get each file in the bucket & create an inputstream for each file 
			ObjectListing objectListing = _s3Client.listObjects(new ListObjectsRequest().withBucketName(bucketName));

			for (S3ObjectSummary objectSummary : objectListing.getObjectSummaries()) {
				System.out.println(" - " + objectSummary.getKey() + "  " + "(size = " + objectSummary.getSize() + ")");

				//getObject(bucket name, file name)
				S3Object returned = _s3Client.getObject("recordings", objectSummary.getKey());
				S3ObjectInputStream audio = returned.getObjectContent(); 
				dateFormat = new SimpleDateFormat("HH:mm:ss");
	            time =  dateFormat.format(objectSummary.getLastModified());
				dateFormat = new SimpleDateFormat("dd/MM/yyyy");
		        date = dateFormat.format(objectSummary.getLastModified());
		            	            
				analyseCall(audio, time, date);
			}

		}
		catch(Exception e){
			e.printStackTrace();
		}
		
		RequestDispatcher rd=request.getRequestDispatcher("result.jsp");  
        rd.forward(request, response);  	    
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		doGet(request, response);
	}

	public void analyseCall(InputStream audio, String time, String date){		
		CallData callData = new CallData(time, date, 1); 
		int call_id = callData.addCall();
		
		CallTranscript callTranscript = new CallTranscript(call_id, time, date); 
		callTranscript.getTranscript(audio);
	}

	/**
	 * @param bucketName
	 * @param clientNum
	 * @param api_key
	 *            (or access key)
	 * @param service_instance_id
	 *            (or secret key)
	 * @param endpoint_url
	 * @param location
	 * @return AmazonS3
	 */
	public static AmazonS3 createClient(){
		
		String api_key; 
		String service_instance_id;
		String endpoint_url;
		String location;
		
		if (System.getenv("VCAP_SERVICES") != null) {
			JsonObject creds = VCAPHelper.getCloudCredentials("cloud-object-storage");
			if(creds == null){
				System.out.println("No COS service bound to this application");
				return null;
			}
			api_key = creds.get("apikey").getAsString();
			service_instance_id = creds.get("resource_instance_id").getAsString();
			endpoint_url = VCAPHelper.getLocalProperties("resource.properties").getProperty("cos_endpoint_url");
			location = VCAPHelper.getLocalProperties("resource.properties").getProperty("cos_endpoint_location");
		} else {
			System.out.println("Running locally. Looking for credentials in resource.properties");
			api_key = VCAPHelper.getLocalProperties("resource.properties").getProperty("cos_api_key");
			service_instance_id = VCAPHelper.getLocalProperties("resource.properties").getProperty("cos_service_instance_id");
			endpoint_url = VCAPHelper.getLocalProperties("resource.properties").getProperty("cos_endpoint_url");
			location = VCAPHelper.getLocalProperties("resource.properties").getProperty("cos_endpoint_location");
			if(api_key == null || api_key.length()==0){
				System.out.println("Missing COS credentials in resource.properties");
				return null;
			}
		}
	
		AWSCredentials credentials;
		if (endpoint_url.contains("objectstorage.softlayer.net")) {
			credentials = new BasicIBMOAuthCredentials(api_key, service_instance_id);
		} else {
			String access_key = api_key;
			String secret_key = service_instance_id;
			credentials = new BasicAWSCredentials(access_key, secret_key);
		}
		ClientConfiguration clientConfig = new ClientConfiguration().withRequestTimeout(5000);
		clientConfig.setUseTcpKeepAlive(true);

		AmazonS3 s3Client = AmazonS3ClientBuilder.standard().withCredentials(new AWSStaticCredentialsProvider(credentials))
				.withEndpointConfiguration(new EndpointConfiguration(endpoint_url, location)).withPathStyleAccessEnabled(true)
				.withClientConfiguration(clientConfig).build();
		return s3Client;
	}
}
