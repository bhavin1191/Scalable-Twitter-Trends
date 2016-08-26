# Scalable-Twitter-Trends - JAVA and Alchemy API
Shows Sentiments of Tweets based on hastag topics
The project works as follows: 

1. TweetTrend will fetch public tweets based on hashtag and push the tweets to SQS to store this tweets for processing. Alchemy API is used to sense the sentiment of the tweet and is stored for each tweet data
2. SNS Listener as name suggests listen for incoming tweets to SQS and then pushes further to CloudSearch
3. CloudSearch is used by TweetFrontEnd to capture the public tweet along with the sentiments and displays those on Google Map based on location info.
