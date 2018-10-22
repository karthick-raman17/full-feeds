package com.full.feeds.dao;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.full.feeds.daointerface.FeedDAOInterface;
import com.full.feeds.model.Feed;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.Filter;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.google.appengine.api.datastore.Query.FilterPredicate;
import com.google.appengine.api.datastore.Query.SortDirection;

public class FeedDAO implements FeedDAOInterface {
	DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
	@Override
	public boolean createFeed(Feed feedObj) {
		try {
			
		Entity feed = new Entity("Feed");
		
		feed.setProperty("userKey",feedObj.getUserId());
		feed.setProperty("feedId",feedObj.getId());
		feed.setProperty("feedValue",feedObj.getFeedValue());
		feed.setProperty("createTime", feedObj.getCreatedTime());
		feed.setProperty("status", feedObj.getStatus());
		feed.setProperty("isEdited",false);
		feed.setProperty("createDate", new Date());
		feed.setProperty("updatedDate", new Date());
		
		
		datastore.put(feed);
		}catch(Exception e) {
			System.out.println("Can't able to create a feed");
			e.printStackTrace();
			return false;
		}
		return true;
	}

	
	public String readAllFeeds(String userEmail) {
		Filter userFilter = new FilterPredicate("email", FilterOperator.EQUAL, userEmail);
		Query userquery = new Query("User").setFilter(userFilter);
		Entity userEntity = datastore.prepare(userquery).asSingleEntity();
	
		Filter feedFilter = new FilterPredicate("userKey", FilterOperator.EQUAL,userEntity.getProperty("userId"));
		Query feedsQuery = new Query("Feed").setFilter(feedFilter).addSort("updatedDate", SortDirection.DESCENDING);
		List<Entity> entityList = datastore.prepare(feedsQuery).asList(FetchOptions.Builder.withDefaults());
		ArrayList<Object> allList = new ArrayList<>();
		Iterator<Entity> it = entityList.iterator();
		while(it.hasNext()) {	
			Entity temp = it.next();
			Map<String,Object> responseString = new HashMap<>();
			if((boolean) temp.getProperty("status")) {
			responseString.put("id", temp.getProperty("feedId"));
			responseString.put("createdDate", temp.getProperty("createTime"));
			responseString.put("feed",temp.getProperty("feedValue"));
			responseString.put("isEdited", temp.getProperty("isEdited"));
			allList.add(responseString);
			}
		}
		if(allList.isEmpty()) {
			Map<String,Object> responseString = new HashMap<>();
			responseString.put("status",false);
			responseString.put("message","no feeds");
			allList.add(responseString);
		}	
		return convertObjectToString(allList);
	}


	@Override
	public String updateFeed(Feed feedModel) {
		Filter feedFilter = new FilterPredicate("feedId", FilterOperator.EQUAL,feedModel.getId());
		Query feedQuery = new Query("Feed").setFilter(feedFilter);
		Entity feed = datastore.prepare(feedQuery).asSingleEntity();
		
		System.out.println(feedModel.getFeedValue());
		feed.setProperty("feedValue", feedModel.getFeedValue());
		feed.setProperty("isEdited", true);
		feed.setProperty("updatedDate",new Date());
		datastore.put(feed);
		
		Map<String,Object> responseObject = new HashMap<>();
		
		responseObject.put("message", "Feed Edited successfully");
		
		return convertObjectToString(responseObject);
	}

	@Override
	public boolean deleteFeed(String feedId) {
		try {
		Filter feedFilter = new FilterPredicate("feedId", FilterOperator.EQUAL,feedId);
		Query feedQuery = new Query("Feed").setFilter(feedFilter);
		Entity feed = datastore.prepare(feedQuery).asSingleEntity();
		datastore.delete(feed.getKey());
		return true;
		}catch(Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	public String generateFeedId() {
		String feedId = "feed_"+UUID.randomUUID().toString();
		return feedId;
	}

	public String convertObjectToString(Object obj) {
		String jsonResponse = "no feeds";
		ObjectMapper mapper = new ObjectMapper();
		try {
			jsonResponse = mapper.writeValueAsString(obj);
		} catch (JsonProcessingException e) {
			e.printStackTrace();
			return  "Something went wrong while converting...";
		}
		return jsonResponse;
	}

	
}
