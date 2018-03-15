package com.ibm.callanalytics;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Map;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;

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
import com.ibm.cloud.objectstorage.services.s3.model.S3ObjectSummary;

/**
 * Servlet implementation class CallTranscript
 */
@WebServlet("/call_analytics")
public class CallAnalyticsServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;


	public CallAnalyticsServlet() {

	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		try{
			//Passed in from html form 
			String bucketName = request.getParameter("bucket");
			System.out.println("CallAnalyticsServlet: Reading files from IBM COS bucket: " + bucketName);
			
			AmazonS3 cos = createClient();

			//File metadata
			String time; 
			String date; 
			int duration = 0; 
			
            SimpleDateFormat dateFormat; 
			
			//Get each file in the bucket & create an input stream for each file 
			ObjectListing objectListing = cos.listObjects(new ListObjectsRequest().withBucketName(bucketName));

			//No point going any further if there's no IBM COS files 
			if(objectListing.getObjectSummaries().size() == 0){
				System.out.println("CallAnalyticsServlet: No files returned from IBM COS");
			}
			else{ 
				for (S3ObjectSummary objectSummary : objectListing.getObjectSummaries()) {
					System.out.println("CallAnalyticsServlet: Reading " + objectSummary.getKey() + " from IBM COS");
					
					//getObject(bucket name, file name)
					S3Object returned = cos.getObject(bucketName, objectSummary.getKey());
					
					//Get last modified time 
					dateFormat = new SimpleDateFormat("HH:mm:ss");
		            time =  dateFormat.format(objectSummary.getLastModified());
		            
		            //Get last modified date 
					dateFormat = new SimpleDateFormat("dd/MM/yyyy");
			        date = dateFormat.format(objectSummary.getLastModified());
			        
			        //Drain the inputstream into a byte[]
			        //https://stackoverflow.com/questions/7805266/how-can-i-reopen-a-closed-inputstream-when-i-need-to-use-it-2-times
			        ByteArrayOutputStream baos = new ByteArrayOutputStream();
			        byte[] buf = new byte[1024];
			        int n = 0;
			        while ((n = returned.getObjectContent().read(buf)) >= 0)
			            baos.write(buf, 0, n);
			        byte[] content = baos.toByteArray();
			        		        
			        //Get audio file duration in seconds
			        try { 
			        	AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(new BufferedInputStream(new ByteArrayInputStream(content)));
			        	AudioFormat format = audioInputStream.getFormat();
			        	long frames = audioInputStream.getFrameLength();
			        	double durationInSeconds = (frames+0.0) / format.getFrameRate();
			        	duration = (int)Math.round(durationInSeconds);
			        	audioInputStream.close();
			        }
			        catch(Exception e) {
			        	System.out.println("CallAnalyticsServlet: Issue getting audio file length");
			        	e.printStackTrace();
			        }     
			        
					analyseCall(new ByteArrayInputStream(content), time, date, duration);
				}
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
	
	public void analyseCall(InputStream audio, String time, String date, int duration){		
		CallData callData = new CallData(time, date, duration); 
		int call_id = callData.addCall();
		
		CallTranscript callTranscript = new CallTranscript(call_id); 
		callTranscript.getTranscript(audio);
	}

	public static AmazonS3 createClient(){
		//Get credentials & connection info from VCAPHelper
		Map<String, String> cosCreds = VCAPHelper.getCOSCreds();
		String api_key = cosCreds.get("apikey").toString();
		String service_instance_id = cosCreds.get("resource_instance_id").toString();
		String endpoint_url = cosCreds.get("cos_endpoint_url").toString();
		String location = cosCreds.get("cos_endpoint_location").toString();
	
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
