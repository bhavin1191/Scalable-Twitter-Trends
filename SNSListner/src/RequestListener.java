

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CodingErrorAction;
import java.util.Map;
import java.util.Scanner;

import javax.servlet.ServletRequestEvent;
import javax.servlet.ServletRequestListener;
import javax.servlet.annotation.WebListener;

import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.cloudsearchdomain.AmazonCloudSearchDomainClient;
import com.amazonaws.services.cloudsearchdomain.model.UploadDocumentsRequest;
import com.amazonaws.services.cloudsearchdomain.model.UploadDocumentsResult;
import com.amazonaws.services.sns.AmazonSNSClient;
import com.amazonaws.services.sns.model.ConfirmSubscriptionRequest;
import com.amazonaws.util.IOUtils;
import com.amazonaws.util.json.JSONException;
import com.amazonaws.util.json.JSONObject;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Application Lifecycle Listener implementation class RequestListener
 *
 */
@WebListener 	
public class RequestListener implements ServletRequestListener {
	public static int count = 0;
	public static boolean set = false;
	public static String msgbody="";
	public static String setrequest="";
	public static String token="";
	public static String topicArn="";
	public static String result="";
	public AmazonCloudSearchDomainClient cloudclient;
    public UploadDocumentsRequest upload;
    public UploadDocumentsResult results;
	/**
     * Default constructor. 
     */
    public RequestListener() {
        // TODO Auto-generated constructor stub
    }

	/**
     * @see ServletRequestListener#requestDestroyed(ServletRequestEvent)
     */
    public void requestDestroyed(ServletRequestEvent event)  { 
         // TODO Auto-generated method stub
    	System.out.print("The Request object destroyed at  :"+new java.util.Date());
    }

	/**
     * @see ServletRequestListener#requestInitialized(ServletRequestEvent)
     */
    public void requestInitialized(ServletRequestEvent event)  { 
         // TODO Auto-generated method stub
        count++;
        //System.out.print("Request Object created At:"+ new java.util.Date());
        //System.out.print("The hit count for this web application :"+count);    
        set = true;
        try {
            AmazonSNSClient sns = new AmazonSNSClient(new BasicAWSCredentials("", ""));
			msgbody = IOUtils.toString(event.getServletRequest().getInputStream());

            InputStream stream = new ByteArrayInputStream(msgbody.getBytes());
            Map<String, String> message = new ObjectMapper().readValue(stream, Map.class);
            token = message.get("Token");
			topicArn = message.get("TopicArn");

            // Confirm the subscription
            if (message.get("Type").equals("SubscriptionConfirmation")) 
            {
            	ConfirmSubscriptionRequest confirmReq = new ConfirmSubscriptionRequest()
            			.withTopicArn(topicArn).withToken(token);
            	sns.confirmSubscription(confirmReq);
            }
            
            String msg = message.get("Message");
            if (msg != null) 
            {
                setrequest = msg;
                cloudclient = new AmazonCloudSearchDomainClient(new BasicAWSCredentials("", ""));
                cloudclient.setEndpoint("https://doc-tweet-vlpcowxfvnf6zvxik52vlgdynm.us-east-1.cloudsearch.amazonaws.com");
                upload = new UploadDocumentsRequest();
         		CharsetDecoder utf8Decoder = Charset.forName("UTF-8").newDecoder();
         		utf8Decoder.onMalformedInput(CodingErrorAction.REPLACE);
         		utf8Decoder.onUnmappableCharacter(CodingErrorAction.REPLACE);
         		utf8Decoder.replaceWith("?");
         		ByteBuffer input = ByteBuffer.wrap(setrequest.getBytes());
         		CharBuffer output = utf8Decoder.decode(input);
         		// Char buffer to string
         		String tweets = output.toString();
         		InputStream doc = new ByteArrayInputStream(tweets.getBytes());
                upload.setContentType("application/json");
                Long l = Long.valueOf(tweets.getBytes().length);
                //System.out.println("long value = "+l);
                upload.setContentLength(l);
                upload.setDocuments(doc);
                results = cloudclient.uploadDocuments(upload);
                //System.out.println(results.getStatus());
                result = results.getStatus();
                
                
            }

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	
    	
    }
	
}