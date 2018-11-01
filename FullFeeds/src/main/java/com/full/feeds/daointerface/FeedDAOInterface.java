package com.full.feeds.daointerface;


public interface FeedDAOInterface {
	String createFeed(String userId,String feedData);
	String readUserFeeds(String userEmail);
	String readAllFeeds(String cursor);
	String updateFeed(String feedData);
	boolean deleteFeed(String userEmail);
	String generateFeedId();
	String likeFeed(String data);
	String commentFeed(String feedData);
}
