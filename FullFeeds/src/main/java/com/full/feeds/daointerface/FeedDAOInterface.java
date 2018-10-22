package com.full.feeds.daointerface;

import com.full.feeds.model.Feed;

public interface FeedDAOInterface {
	boolean createFeed(Feed feed);
	String readAllFeeds(String userEmail);
	String updateFeed(Feed feed);
	boolean deleteFeed(String userEmail);
	String generateFeedId();
}
