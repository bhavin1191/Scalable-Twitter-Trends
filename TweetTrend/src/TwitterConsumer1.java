import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.json.JSONArray;
import org.json.JSONObject;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClient;
import com.amazonaws.services.sqs.model.CreateQueueRequest;
import com.amazonaws.services.sqs.model.DeleteMessageBatchRequest;
import com.amazonaws.services.sqs.model.DeleteMessageBatchRequestEntry;
import com.amazonaws.services.sqs.model.Message;
import com.amazonaws.services.sqs.model.ReceiveMessageRequest;
import com.amazonaws.services.sqs.model.SendMessageRequest;

import oauth.signpost.OAuthConsumer;
import oauth.signpost.commonshttp.CommonsHttpOAuthConsumer;


public class TwitterConsumer implements Runnable {

	public static AWSCredentials credentials;
	public long Messages = 0;
    public long Bytes = 0;
    public long Timestamp = 0;

    private static String accessToken = "";
    private static String accessSecret = "";
    private static String consumerKey = "";
    private static String consumerSecret = ""; 

    private String feedUrl;
    private String filePrefix;
    boolean isRunning = true;
    
	public TwitterConsumer(String accessToken, String accessSecret, String consumerKey, String consumerSecret, String url) {
        this.accessToken = accessToken;
        this.accessSecret = accessSecret;
        this.consumerKey = consumerKey;
        this.consumerSecret = consumerSecret;
        feedUrl = url;
    }
	
	
	@Override
	public void run() {
		int count=0;
    	double longi=0,lati = 0;
    	List<String> tokens = new ArrayList<String>(Arrays.asList("starwarsday", "MayTheFourthBeWithYou", "CivilWar", "GameofThrones", "music", "obama", "shopping", "throwback",
 				"followme", "wta"));
        OAuthConsumer consumer = new CommonsHttpOAuthConsumer(consumerKey, consumerSecret);
        consumer.setTokenWithSecret(accessToken, accessSecret);
        //credentials = new ProfileCredentialsProvider("default").getCredentials();
        AmazonSQSClient sqs = new AmazonSQSClient(new BasicAWSCredentials("", "")); //Method 2 of using credentials
		System.out.println("Creating a new SQS queue called MyQueue.\n");
		String myQueueUrl = sqs.listQueues("myqueue").getQueueUrls().get(0);
		// Run loop
        while (isRunning) {
            try {
                HttpGet request = new HttpGet(feedUrl);
                consumer.sign(request);
                HttpClient client = HttpClientBuilder.create().build();
                HttpResponse response = client.execute(request);
                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(response.getEntity().getContent()));
                String patternString = "\\b(" + StringUtils.join(tokens, "|") + ")\\b";
        		Pattern pattern = Pattern.compile(patternString,Pattern.CASE_INSENSITIVE);
        		String category = "";
                while (true) 
                {
                 String line = reader.readLine();
                 //System.out.println(line);
                 String en = "en";
                 JSONObject jsonobj = new JSONObject(line);
                 if(en.equals(jsonobj.get("lang")))
                 { 
                   
                    if( !( jsonobj.get("place").equals(null)	) )
                   {
                    	String cloudformat = "[{\"type\":\"add\","
                    						+"\"id\":\""+jsonobj.getInt("id")+"\","
                    						+"\"fields\":{";
                    						//+ "\"created_at\":\""+jsonobj.getString("created_at")+"\","
                    						
                    	String trimmedtext = jsonobj.getString("text").trim();
                    	trimmedtext = trimmedtext.replace('\"',' ').replace('\n', ' ');
                    	cloudformat = cloudformat + "\"text\":\""+trimmedtext+"\","
                    						+ "\"user_screen_name\":\""+jsonobj.getJSONObject("user").getString("screen_name")+"\",";
                    						
                    	
                    	
                    	if(!(jsonobj.get("place").equals(null)))
                    	{
                    		JSONArray jsonarr = jsonobj.getJSONObject("place").getJSONObject("bounding_box").getJSONArray("coordinates");
                    		JSONArray childarr = jsonarr.getJSONArray(0);
                    		JSONArray othrchild = childarr.getJSONArray(0);
                    		for(int i = 0;i<othrchild.length(); i++)
                    		{
                    			longi = othrchild.getDouble(0);
                    			lati=othrchild.getDouble(1);
                    		}
                    			
                    		cloudformat = cloudformat + "\"latitude\":\""+lati+"\",\"longitude\":\""+longi+"\",";
                    	    //System.out.println(lati+","+longi);	
                    	}
                    		
                    	else
                    	{  
                    		cloudformat = cloudformat + "\"latitude\":\""+0+"\",\"longitude\":\""+0+"\",";
                    	}	
                    	
                    	Matcher matcher = pattern.matcher(trimmedtext);
                    	boolean flag = false;
                    	while (matcher.find()) {
                    		flag = true;
                		    System.out.println(matcher.group());
                		    category = matcher.group();
                		}
                    	if(!category.isEmpty() && flag == true )
                		    cloudformat = cloudformat + "\"category\":\""+category+"\"";
                    	else
                    	{
                    		System.out.println("None");
                    		cloudformat = cloudformat + "\"category\":\"None\"";
                    	}
                    	
                    	cloudformat = cloudformat + "}}]";

                    System.out.println(cloudformat);
                    line=cloudformat;
                    //System.out.println("long value = "+line);
                    //System.out.println(myQueueUrl);

                    	System.out.println("Sending a message to MyQueue.\n");
                    	//InputStream doc = new ByteArrayInputStream(line.getBytes());
                        //Long l = Long.valueOf(line.getBytes().length);
                        //System.out.println("long value = "+l);
                    	sqs.sendMessage(new SendMessageRequest()
                    	    .withQueueUrl(myQueueUrl)
                    	    .withMessageBody(line));
                    
                    
                    count = count + 1; 
                    System.out.println("Write count: "+count);
                    //Thread.sleep(100);
                    }
                }
              }
            }
            catch(Exception e)
            {
            	
            }
        }
	}
	
	public static void main(String[] args) {
        TwitterConsumer t = new TwitterConsumer("accessToken", "accessSecret", "consumerKey", "consumerSecret", "https://stream.twitter.com/1.1/statuses/filter.json?track=starwarsday,MayTheFourthBeWithYou,CivilWar,GameofThrones,music,obama,shopping,throwback,followme,wta");
        t.run();
    }

}
