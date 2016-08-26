<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
	
<html>
	<head>
	<style>
#header {
    background-color:gray;
    color:white;
    text-align:center;
    padding:5px;
}
#nav {
    line-height:30px;
    background-color:#00000;
    height:300px;
    width:100px;
    float:left;
    padding:5px;	      
}
#section {
    width:350px;
    float:left;
    padding:10px;	 	 
}
 
}
</style>
		<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
		<title>TweetTrends</title>
		<script src="http://ajax.googleapis.com/ajax/libs/angularjs/1.4.8/angular.min.js"></script>
		
	</head>
	<body>
	<div id='cssmenu'>
		<form id = "tweetform" method="GET" action="tweetmap1.jsp">
			<div id="header">
			<h2 align="center"><u>Trending</u></h2>
			</div>
			<div id="nav"></div>
			<div id=section>
			Please Select the search parameter<br/>
 			<select id="keyword" name="filterQuery">
			<option value="starwarsday" selected="selected">StarWarsDay</option>
			<option value="maythefourthBewithyou">MayTheFourthBeWithYou</option>
			<option value="CivilWar">CivilWar</option>
			<option value="gameofthrones">GameofThrones</option>
			<option value="obama">SpiderMan</option>
			<option value="shopping">Shopping</option>
			<option value="wta">WTA</option>
			<option value="throwback">throwback</option>
			<option value="followme">followme</option>
			<option value="music">music</option>
		   </select>
		   
    	<input type="submit" value="Search"></input>
    	</div>
    		</form>
	
    	
	<script src="http://code.jquery.com/jquery-1.10.2.min.js"></script>
  
 <script type="text/javascript">

 var form = $('#tweetform');
 form.submit(function () {

 $.ajax({
 type: form.attr('method'),
 url: form.attr('action'),
 data: form.serialize(),
 success: function (data) {
 var result=data;
 $('#tag').html(result);

 }
 });

 return false;
 }); 
 </script>
   <div id=tag></div> 
	</div>
	</body>
</html>