<%@page import="java.io.*" %>
<%@page import="java.util.*" %>
<%@page import="java.net.*" %>
<%@page import="org.json.*" %>
<%@page import="com.amazonaws.services.cloudsearchdomain.*"%>
<%@page import="com.amazonaws.services.cloudsearchdomain.model.*"%>
<%@page import="com.amazonaws.auth.BasicAWSCredentials"%>

<!DOCTYPE html>
<html>
  <head>
    <meta charset="utf-8">
    <meta name="viewport" content="initial-scale=1.0, user-scalable=no" />
    <style>
    #map {
        margin: 1%;
        padding: 5;
        height: 80%;
      }

    </style>
    <script type="text/javascript" src="https://maps.googleapis.com/maps/api/js?libraries=visualization&v=3.exp&sensor=false&callback=initialize&key=<key value>">
    </script>
    

  </head>
  <body>
      <%
      URL cloudsearchdomain;
      URLConnection search;
      String line = null;
      String[] lat = null;
      String[] lon = null;
      String[] user = null;
      String[] text = null;
      String[] sentiment = null;
      BufferedReader in = null;
      AmazonCloudSearchDomainClient cloudclient = new AmazonCloudSearchDomainClient(new BasicAWSCredentials("", ""));
      
      
		try {
			cloudsearchdomain = new URL("https://search-tweet-vlpcowxfvnf6zvxik52vlgdynm.us-east-1.cloudsearch.amazonaws.com/2013-01-01/search?q="+request.getParameter("filterQuery")+"&size=1000");
			
			search = null;
			search = cloudsearchdomain.openConnection();
			in = new BufferedReader(
                    new InputStreamReader(
                    search.getInputStream()));
		} 
		catch (MalformedURLException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
				  
        try {
			while ((line = in.readLine()) != null) 
			{
				String jsonString = line;
				JSONArray documnt = null;
				JSONObject jsonResult = null;
				JSONObject jsonChild = null;
				try {
					jsonResult = new JSONObject(jsonString);
					jsonChild = jsonResult.getJSONObject("hits");
					documnt = jsonChild.getJSONArray("hit");
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				if(documnt != null) {
				     lat = new String[documnt.length()];
				     lon = new String[documnt.length()];
				     user = new String[documnt.length()];
				     text = new String[documnt.length()];
				     sentiment = new String[documnt.length()];
				     
				    for(int i = 0 ; i < documnt.length() ; i++) {
				    	JSONObject pull = null;
				    	JSONObject queryresult = null;
						try {
							pull = (JSONObject)documnt.get(i);
							queryresult = pull.getJSONObject("fields");
						} catch (JSONException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}
				    	try {
				    		
							lat[i] = queryresult.getString("latitude");
							lon[i] = queryresult.getString("longitude");
							user[i] = queryresult.getString("user_screen_name");
							text[i] = queryresult.getString("text");
							text[i].replace('\'', ' ');
							sentiment[i] = queryresult.getString("sentiment");
							
						} catch (JSONException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
				        
				    }
				}
			
			}
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
        try {
			in.close();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	   request.setAttribute("latitude",lat);
	   request.setAttribute("username",user);
       request.setAttribute("longitude",lon);
       request.setAttribute("text",text);
       request.setAttribute("sentiment",sentiment);

 %>
 	<div id="counter"> </div>
    <div id="positive-counter"> </div>
    <div id="negative-counter"> </div>
    <div id="neutral-counter"> </div>
    <div id="map"></div>
    <script>
        var lat = new Array();
        var lon = new Array();
        var username = new Array();
        var tweet = new Array();
        var sentiment = new Array();
        var marker, i;
        var infowindow;
        var map
        function initialize() {
        map = new google.maps.Map(document.getElementById('map'), {
            zoom: 4,
            center: new google.maps.LatLng(37.4419, -122.1419),
            mapTypeId: google.maps.MapTypeId.TERRAIN
          });
        
		infowindow = new google.maps.InfoWindow();
        
   <%
      String[] latitude = (String[])request.getAttribute("latitude");
   	  String[] longitude = (String[])request.getAttribute("longitude");
   	  String[] user1 = (String[])request.getAttribute("username");
   	  String[] tweet = (String[])request.getAttribute("text");
   	  String[] senti = (String[])request.getAttribute("sentiment");
   	  
      for(int count=0; count <user1.length; count++) {
   %>
         lat[<%= count %>]='<%= latitude[count] %>';//dont miss the single quotes here
         lon[<%= count %>]='<%= longitude[count] %>';//dont miss the single quotes here
         username[<%= count %>]='<%= user1[count] %>';
         tweet[<%= count %>]="<%= tweet[count] %>";
         sentiment[<%= count %>]="<%= senti[count] %>";
   <% } %>

try
{
    var pos =0;
    var neg =0;
    var neu =0;
    for (j = 0; j < lon.length; j++) 
    {   
    	var icon = "http://maps.google.com/mapfiles/ms/icons/blue-dot.png";
		if(sentiment[j] == "positive") 
		{
		icon = "http://maps.google.com/mapfiles/ms/icons/green-dot.png";
		pos += 1;
		}
		else if (sentiment[j] == "negative") 
		{
		icon = "http://maps.google.com/mapfiles/ms/icons/red-dot.png";
		neg += 1;
		}
		else {
		neu += 1;
		}
		
        var marker = new google.maps.Marker({
        position: new google.maps.LatLng(lat[j], lon[j]),
        map: map,
        icon: icon,
        animation: google.maps.Animation.DROP
      });
     
      google.maps.event.addListener(marker, 'click', (function(marker, j) {
        return function() {
          infowindow.setContent("USERNAME: "+username[j]+"<br>"+" TWEET: "+tweet[j]+"<br>"+"SENTIMENT: "+sentiment[j]);
          infowindow.open(map, marker);
        }
      })(marker, j));
      
    }
    $('#positive-counter').text("Positive Tweets: " + pos);
	$('#negative-counter').text("Negative Tweets: " + neg);
	$('#neutral-counter').text("Neutral Tweets: " + neu);
}
catch(err) {
    document.getElementById("header").innerHTML = err.message;
}
        }    
	</script>
  </body>
</html>
