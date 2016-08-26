import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.json.JSONException;
import org.json.JSONObject;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClient;
import com.amazonaws.services.sqs.model.ReceiveMessageRequest;
import com.amazonaws.services.sqs.model.DeleteMessageBatchRequest;
import com.amazonaws.services.sqs.model.DeleteMessageBatchRequestEntry;
import com.amazonaws.services.sqs.model.Message;

public class Consumer_Worker 
{
	private static AWSCredentials credentials;

	public static AmazonSQS init()
	{
		credentials = new ProfileCredentialsProvider("default").getCredentials();
		AmazonSQS sqs = new AmazonSQSClient(credentials);
		System.out.println("===========================================");
		System.out.println("Getting Started with Amazon SQS");
		System.out.println("===========================================\n");
		return sqs;
		
	}
	
	private static void processMsgForAlchemy(AmazonSQS sqs) throws InterruptedException 
	{
		String myQueueUrl = sqs.listQueues("myqueue").getQueueUrls().get(0);
		int count = 0;
		int maxmsg = 10;
		while (true) 
		{
			ReceiveMessageRequest receiveMessageRequest = new ReceiveMessageRequest(myQueueUrl).withMaxNumberOfMessages(maxmsg);
			List<Message> messages = sqs.receiveMessage(receiveMessageRequest).getMessages();
			if(!messages.isEmpty())
			{
				ExecutorService executor = Executors.newFixedThreadPool(10);
				for (Message msg : messages) 
				{
					try {
						//System.out.println(msg.getBody());
						String body = msg.getBody().replace('[', ' ').trim();
						body = body.replace(']', ' ').trim();
						JSONObject tweet = new JSONObject(body);
						Runnable worker = new Worker(credentials, tweet);
						executor.execute(worker);
					} catch (JSONException e) {
						// TODO Auto-generated catch block
						//e.printStackTrace();
					}
				}
				try 
				{
					System.out.println("Thread sleeping");
					Thread.sleep(2000);
//					System.out.println("Attempting to shut down worker now!!");
					executor.shutdown();
				}
				finally 
				{
					if (!executor.isTerminated()) {
						executor.shutdownNow();
				}
					
				}
				List<DeleteMessageBatchRequestEntry> deletemsg = new ArrayList<DeleteMessageBatchRequestEntry>();
				for (Message m : messages) {
					deletemsg.add(new DeleteMessageBatchRequestEntry(m.getMessageId(), m.getReceiptHandle()));
				}
				DeleteMessageBatchRequest deleteMessageBatchRequest = new DeleteMessageBatchRequest(myQueueUrl, deletemsg);
				sqs.deleteMessageBatch(deleteMessageBatchRequest);
				System.out.println("Message Deleted!");
			}
			else
			{
				try {
					System.out.println("Thread sleeping for new messages");
					Thread.sleep(30000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			
		}
	}
	public static void main(String[] args) throws InterruptedException {
		// TODO Auto-generated method stub'
		AmazonSQS sqs = init();
		processMsgForAlchemy(sqs);
	}

}
