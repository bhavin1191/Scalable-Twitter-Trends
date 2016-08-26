

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Scanner;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;

import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.sns.AmazonSNSClient;
import com.amazonaws.services.sns.model.SubscribeRequest;

/**
 * Servlet implementation class Requester
 */
@WebServlet("/Requester")
public class Requester extends HttpServlet {
	private static final long serialVersionUID = 1L;
	String topicArn = "<SNS ARN>";
	public static boolean flagsub = false;
    /**
     * @see HttpServlet#HttpServlet()
     */
    public Requester() {
        super();
        // TODO Auto-generated constructor stub
    }

    public void init(ServletConfig config) throws ServletException
    {
      super.init(config);
    }
	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		doPost(request, response);
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		
		PrintWriter out = response.getWriter();
        out.println("This is target Servlet");
        out.println("the no of hits for this application is "+RequestListener.count);
        try {
        	AmazonSNSClient sns = new AmazonSNSClient(new BasicAWSCredentials("", "//"));
        	if(!flagsub)
        	{
        		flagsub = true;
        		SubscribeRequest subRequest = new SubscribeRequest(topicArn, "http", "http://tweet1.us-east-1.elasticbeanstalk.com/Requester");
        		sns.subscribe(subRequest);
        		out.println("SubscribeRequest - " + sns.getCachedResponseMetadata(subRequest));
        		out.println("Check your email and confirm subscription.");
        	}
        
        
        //out.println(RequestListener.set);
        //out.println(RequestListener.msgbody);
        
        out.println(RequestListener.setrequest);
        //logger.info(RequestListener.setrequest);
        String messagetype = request.getHeader("x-amz-sns-message-type");
        out.println(messagetype);
		//If message doesn't have the message type header, don't process it.
		if (messagetype == null)
			return;
		out.println(RequestListener.result);

        }
        catch(Exception e)
        {
        	out.print(e.getLocalizedMessage());
        }
	}

}
