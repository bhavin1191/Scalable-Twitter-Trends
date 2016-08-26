import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CodingErrorAction;

import org.json.JSONObject;
import com.alchemyapi.api.AlchemyAPI;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.services.cloudsearchdomain.AmazonCloudSearchDomainClient;
import com.amazonaws.services.cloudsearchdomain.model.UploadDocumentsRequest;
import com.amazonaws.services.cloudsearchdomain.model.UploadDocumentsResult;
import com.amazonaws.services.sns.AmazonSNSClient;
import com.amazonaws.services.sns.model.PublishRequest;
import com.amazonaws.services.sns.model.PublishResult;

import org.w3c.dom.Document;


public class Worker implements Runnable {
	private JSONObject tweet;
	private final String alchemyapikey = "";
	private AmazonSNSClient snsClient;
	private AWSCredentials credentials = null;
	public AmazonCloudSearchDomainClient cloudclient;
    public UploadDocumentsRequest upload;
    public UploadDocumentsResult results;
    

	public Worker(AWSCredentials credentials, JSONObject tweet) {
		this.tweet = tweet;
		this.credentials = credentials;
	}
	
	@Override
	public void run() {
		// TODO Auto-generated method stub
		String sentiment = getSentimentDetails();
		cloudclient = new AmazonCloudSearchDomainClient(credentials); // Another Way of using crednentionals
		//cloudclient.setEndpoint("https://doc-tweet-vlpcowxfvnf6zvxik52vlgdynm.us-east-1.cloudsearch.amazonaws.com");
		//	System.out.println(sentiment);
		if (sentiment != null) {
			String manipulate = tweet.toString();
			manipulate = manipulate.replace('}', ' ').trim();
			String tweetjson = "["+manipulate+",\"sentiment\":\""+sentiment+"\"}}]";

			publishToSNS(tweetjson);
			
		}
	}

	private void publishToSNS(String tweetjson) {
		// TODO Auto-generated method stub
		this.snsClient = new AmazonSNSClient(credentials);
		String topicArn = "SNS ARN";
		PublishRequest publishRequest = new PublishRequest(topicArn, tweetjson);
		PublishResult publishResult = snsClient.publish(publishRequest);
		System.out.println("MessageId - " + publishResult.getMessageId());
	}

	private void addToCloudSearch(String tweets) throws CharacterCodingException {
		upload = new UploadDocumentsRequest();
		CharsetDecoder utf8Decoder = Charset.forName("UTF-8").newDecoder();
		utf8Decoder.onMalformedInput(CodingErrorAction.REPLACE);
		utf8Decoder.onUnmappableCharacter(CodingErrorAction.REPLACE);
		utf8Decoder.replaceWith("?");
		ByteBuffer input = ByteBuffer.wrap(tweets.getBytes());
		CharBuffer output = utf8Decoder.decode(input);
		// Char buffer to string
		tweets = output.toString();

		InputStream doc = new ByteArrayInputStream(tweets.getBytes());
        upload.setContentType("application/json");
        Long l = Long.valueOf(tweets.getBytes().length);
        //System.out.println("long value = "+l);
        upload.setContentLength(l);
        upload.setDocuments(doc);
        results = cloudclient.uploadDocuments(upload);
        System.out.println(results.getStatus());
	}

	private void addSentimentToTweet(String sentiment) {
		// TODO Auto-generated method stub
		
	}

	public String getSentimentDetails() {
		AlchemyAPI alchemyObj = AlchemyAPI.GetInstanceFromString(alchemyapikey);
		Document doc;
		try 
		{
			//System.out.println(tweet.toString());
			doc = alchemyObj.TextGetTextSentiment(tweet.getJSONObject("fields").getString("text"));
			String sentiment = doc.getElementsByTagName("type").item(0).getTextContent();
			return sentiment;
		}
		catch (Exception e) 
		{
			//e.printStackTrace();
			return null;
		}
	}
}
