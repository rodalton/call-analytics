package com.ibm.callanalytics;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

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
import com.ibm.cloud.objectstorage.services.s3.model.Bucket;

/**
 * Servlet implementation class BucketList
 */
@WebServlet("/home")
public class BucketList extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public BucketList() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		try{
			AmazonS3 _s3Client = createClient();
			List<Bucket> bucketList = _s3Client.listBuckets();
			
			request.setAttribute("bucketList", bucketList);
	        request.getRequestDispatcher("/WEB-INF/index.jsp").forward(request, response);

		}
		catch(Exception e){
			e.printStackTrace();
		}

		// TODO Auto-generated method stub
		response.getWriter().append("Served at: ").append(request.getContextPath());
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
		String location="us";
		
		if (System.getenv("VCAP_SERVICES") != null) {
			JsonObject creds = VCAPHelper.getCloudCredentials("cloud-object-storage");
			if(creds == null){
				System.out.println("No COS service bound to this application");
				return null;
			}
			api_key = creds.get("apikey").getAsString();
			service_instance_id = creds.get("resource_instance_id").getAsString();
			endpoint_url = creds.get("endpoints").getAsString();
		} else {
			System.out.println("Running locally. Looking for credentials in resource.properties");
			api_key = VCAPHelper.getLocalProperties("resource.properties").getProperty("cos_api_key");
			service_instance_id = VCAPHelper.getLocalProperties("resource.properties").getProperty("cos_service_instance_id");
			endpoint_url = VCAPHelper.getLocalProperties("resource.properties").getProperty("cos_endpoint_url");
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
