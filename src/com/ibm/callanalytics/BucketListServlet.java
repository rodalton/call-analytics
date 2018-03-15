package com.ibm.callanalytics;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

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
public class BucketListServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public BucketListServlet() {
        super();
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		try{
			AmazonS3 cos = createClient();
			List<Bucket> bucketList = cos.listBuckets();
			
			request.setAttribute("bucketList", bucketList);
	        request.getRequestDispatcher("/WEB-INF/index.jsp").forward(request, response);
		}
		catch(Exception e){
			e.printStackTrace(response.getWriter());
		}
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		doGet(request, response);
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
