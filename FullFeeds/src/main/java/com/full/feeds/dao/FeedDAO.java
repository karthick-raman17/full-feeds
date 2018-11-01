package com.full.feeds.dao;

import java.io.IOException;
import java.util.ArrayList;

import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.full.feeds.daointerface.FeedDAOInterface;
import com.full.feeds.model.Feed;
import com.google.appengine.api.datastore.Cursor;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.FetchOptions.Builder;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.Filter;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.google.appengine.api.datastore.Query.FilterPredicate;
import com.google.appengine.api.datastore.Query.SortDirection;
import com.google.appengine.api.datastore.QueryResultList;

public class FeedDAO implements FeedDAOInterface {
	private final DatastoreService datastore;

	public FeedDAO() {
		datastore = DatastoreServiceFactory.getDatastoreService();
	}

	@Override
	public String createFeed(String userId, String feedData) {

		String feedId = "";
		Map<String, Object> response = new HashMap<>();

		response.put("isCreated", false);
		response.put("status", "something went wrong when creating a feed");
		List<String> userlist = new ArrayList<String>();
		userlist.add(null);

		try {
			Map<String, Object> feedPayload = parseJSONFromString(feedData);
			Entity feed = new Entity("Feed");
			feedId = generateFeedId();
			feed.setProperty("userKey", userId);
			feed.setProperty("feedId", feedId);
			feed.setProperty("Text", feedPayload.get("Text"));
			feed.setProperty("createdDate", feedPayload.get("createdDate"));
			feed.setProperty("status", "Active");
			feed.setProperty("isEdited", false);
			feed.setProperty("updatedDate", feedPayload.get("updatedDate"));
			feed.setProperty("likedUsers", userlist);

			datastore.put(feed);
			// response.put("isNew", true);
			response.put("id", feedId);
			response.put("status", true);
			response.put("Text", feedPayload.get("Text"));
			response.put("createdDate", feedPayload.get("createdDate"));

		} catch (Exception e) {
			System.out.println("Can't able to create a feed");
			e.printStackTrace();
			return convertObjectToString(response);
		}
		return convertObjectToString(response);
	}

	public String readUserFeeds(String userId) {

		Entity userEntity = retrieveUserFromDB(userId);

		Filter feedFilter = new FilterPredicate("userKey", FilterOperator.EQUAL, userEntity.getProperty("userId"));
		Query feedsQuery = new Query("Feed").setFilter(feedFilter).addSort("updatedDate", SortDirection.DESCENDING);
		List<Entity> entityList = datastore.prepare(feedsQuery).asList(FetchOptions.Builder.withDefaults());
		ArrayList<Object> allList = new ArrayList<>();
		Iterator<Entity> it = entityList.iterator();
		while (it.hasNext()) {
			Entity temp = it.next();
			Map<String, Object> responseString = new HashMap<>();
			if (temp.getProperty("status").equals("Active")) {
				responseString.put("id", temp.getProperty("feedId"));
				responseString.put("createdDate", temp.getProperty("createdDate"));
				
				responseString.put("updatedDate", temp.getProperty("updatedDate"));
				responseString.put("text", temp.getProperty("Text"));
				responseString.put("isEdited", temp.getProperty("isEdited"));
				responseString.put("likedUsers", temp.getProperty("likedUsers"));
				List<Entity> et = retrieveCommentsFromDB(temp.getProperty("feedId").toString());
				
				if (et != null) {
					ArrayList<Object> allCommentList = new ArrayList<>();
					Iterator<Entity> commentItr = et.iterator();
					while (commentItr.hasNext()) {
						Entity commentTemp = commentItr.next();
						Map<String, Object> comments = new HashMap<>();
						comments.put("commentBy",retrieveUserFromDB(commentTemp.getProperty("userId").toString()).getProperty("name"));
						comments.put("createdDate", commentTemp.getProperty("createdDate"));
						comments.put("commentText", commentTemp.getProperty("commentText"));
						allCommentList.add(comments);
					}
				responseString.put("comments", allCommentList);
				}
				allList.add(responseString);
			}
		}
		if (allList.isEmpty()) {
			Map<String, Object> responseString = new HashMap<>();
			responseString.put("status", false);
			responseString.put("message", "no feeds for this user");
			return convertObjectToString(responseString);
		}
		else {
			Map<String, Object> responseString = new HashMap<>();
			responseString.put("status", true);
			responseString.put("data",allList);
		return convertObjectToString(responseString);
		}
		
	}

	@Override
	public String commentFeed(String feedData) {
		Map<String, Object> feedPayload = parseJSONFromString(feedData);

		Entity comment = new Entity("Comment");
		comment.setProperty("feedId", feedPayload.get("feedId").toString());
		comment.setProperty("userId", feedPayload.get("userId").toString());
		comment.setProperty("createdDate", feedPayload.get("createdDate").toString());
		comment.setProperty("commentText", feedPayload.get("commentText").toString());
		datastore.put(comment);
		Filter commentFilter = new FilterPredicate("feedId", FilterOperator.EQUAL,
				feedPayload.get("feedId").toString());
		Query commentQuery = new Query("Comment").setFilter(commentFilter);
		List<Entity> commentList = datastore.prepare(commentQuery).asList(Builder.withDefaults());

		System.out.println(commentList);

		ArrayList<HashMap<String, Object>> responseList = new ArrayList<>();

		for (int i = 0; i < commentList.size(); i++) {

			HashMap<String, Object> responseComment = new HashMap<>();

			Entity userEntity = retrieveUserFromDB(commentList.get(i).getProperty("userId").toString());
			responseComment.put("createdDate", commentList.get(i).getProperty("createdDate"));
			responseComment.put("commentBy", userEntity.getProperty("name"));
			responseComment.put("commentText", commentList.get(i).getProperty("commentText"));

			responseList.add(responseComment);
		}
		HashMap<String, Object> responseString = new HashMap<String, Object>();
		if (!responseList.isEmpty()) {
			responseString.put("status", true);
			responseString.put("data", responseList);

		} else {
			responseString.put("status", false);
			responseString.put("message", "something went wrong..");
		}
		return convertObjectToString(responseString);
	}

	@Override
	public String updateFeed(String feedData) {
		
		Map<String, Object> feedPayload = parseJSONFromString(feedData);
		Entity feed = retrieveFeedFromDB(feedPayload.get("feedId").toString());

		System.out.println(feedPayload);
		
		feed.setProperty("Text",feedPayload.get("text"));
		feed.setProperty("isEdited", true);
		feed.setProperty("updatedDate",feedPayload.get("updatedDate"));
		datastore.put(feed);

		Map<String, Object> responseObject = new HashMap<>();

		responseObject.put("text", feed.getProperty("Text"));
		responseObject.put("status", true);
		responseObject.put("isEdited", feed.getProperty("isEdited"));

		return convertObjectToString(responseObject);
	}

	@Override
	public boolean deleteFeed(String feedId) {
		try {
			Entity feed = retrieveFeedFromDB(feedId);
			datastore.delete(feed.getKey());
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	@Override
	public String readAllFeeds(String cursor) {
		final int PAGE_SIZE = 5;
		FetchOptions fetchOptions = FetchOptions.Builder.withLimit(PAGE_SIZE);

		String startCursor = cursor;
		if (startCursor != null) {
			fetchOptions.startCursor(Cursor.fromWebSafeString(startCursor));
		}

		Query q = new Query("Feed").addSort("updatedDate", SortDirection.DESCENDING);
		PreparedQuery pq = datastore.prepare(q);

		QueryResultList<Entity> results;

		try {
			results = pq.asQueryResultList(fetchOptions);
		} catch (IllegalArgumentException e) {
			// IllegalArgumentException happens when an invalid cursor is used.
			// A user could have manually entered a bad cursor in the URL or there
			// may have been an internal implementation detail change in App Engine.
			// Redirect to the page without the cursor parameter to show something
			// rather than an error.
			// resp.sendRedirect("/people");
			return null;
		}

		ArrayList<Object> allFeedsList = new ArrayList<>();
		Iterator<Entity> it = results.iterator();
		while (it.hasNext()) {
			Entity temp = it.next();
			Map<String, Object> responseString = new HashMap<>();
			if (temp.getProperty("status").equals("Active")) {
				responseString.put("id", temp.getProperty("feedId"));
				responseString.put("createdDate", temp.getProperty("createdDate"));
				responseString.put("updatedDate", temp.getProperty("updatedDate"));
				responseString.put("text", temp.getProperty("Text"));
				responseString.put("isEdited", temp.getProperty("isEdited"));
				responseString.put("likedUsers", temp.getProperty("likedUsers"));
				responseString.put("userId", temp.getProperty("userKey"));
				List<Entity> et = retrieveCommentsFromDB(temp.getProperty("feedId").toString());
				
				if (et != null) {
					ArrayList<Object> allCommentList = new ArrayList<>();
					Iterator<Entity> commentItr = et.iterator();
					while (commentItr.hasNext()) {
						Entity commentTemp = commentItr.next();
						Map<String, Object> comments = new HashMap<>();
						comments.put("commentBy",retrieveUserFromDB(commentTemp.getProperty("userId").toString()).getProperty("name"));
						comments.put("createdDate", commentTemp.getProperty("createdDate"));
						comments.put("commentText", commentTemp.getProperty("commentText"));
						allCommentList.add(comments);
					}
					responseString.put("comments", allCommentList);
				}
				allFeedsList.add(responseString);
			}
		}

		String cursorString = results.getCursor().toWebSafeString();
		HashMap<String, Object> finalResults = new HashMap<>();
		if (allFeedsList.isEmpty()) {
			finalResults.put("status", false);
			finalResults.put("message", "no more feeds");
			return convertObjectToString(finalResults);
		} else {
			finalResults.put("status", true);
			finalResults.put("data", allFeedsList);
			finalResults.put("cursor", cursorString);
			return convertObjectToString(finalResults);
		}
	}

	public String likeFeed(String likedata) {
		Map<String, Object> feedPayload = parseJSONFromString(likedata);

		Entity feedEntity = retrieveFeedFromDB(feedPayload.get("feedId").toString());
		System.out.println(feedPayload.get("feedId").toString());

		ArrayList<String> userList = (ArrayList<String>) feedEntity.getProperty("likedUsers");
		System.out.println(userList);
		String userId = feedPayload.get("userId").toString();
		Map<String, Object> jsonResponse = new HashMap<>();

		try {
			for (int i = 0; i < userList.size(); i++) {
				if (userList.get(i) == null) {
					System.out.println("first like");
					System.out.println(userList);
					userList.clear();
					userList.add(userId);
					feedEntity.setProperty("likedUsers", userList);
					jsonResponse.put("status", "ok");
					jsonResponse.put("data", userList);
					datastore.put(feedEntity);
					return convertObjectToString(jsonResponse);
				} else if (userList.get(i).equals(userId)) {
					// unlike
					userList.remove(userId);
					if (userList.isEmpty()) {
						userList.add(null);
					}
					System.out.println("user unliked");
					System.out.println(userList);
					feedEntity.setProperty("likedUsers", userList);
					jsonResponse.put("status", "ok");
					jsonResponse.put("data", userList);
					datastore.put(feedEntity);
					return convertObjectToString(jsonResponse);
				}

			}

			// like
			userList.add(userId);
			System.out.println("user liked");
			System.out.println(userList);
			feedEntity.setProperty("likedUsers", userList);
			jsonResponse.put("status", "ok");
			jsonResponse.put("data", userList);
			datastore.put(feedEntity);
			return convertObjectToString(jsonResponse);

		} catch (Exception e) {
			e.printStackTrace();
			jsonResponse.put("status", "failed");
			return convertObjectToString(jsonResponse);
		}
	}

	private Entity retrieveFeedFromDB(String feedId) {
		Filter feedFilter = new FilterPredicate("feedId", FilterOperator.EQUAL, feedId);
		Query feedQuery = new Query("Feed").setFilter(feedFilter);
		Entity feed = datastore.prepare(feedQuery).asSingleEntity();
		return feed;
	}

	private List<Entity> retrieveCommentsFromDB(String feedId) {
		Filter feedFilter = new FilterPredicate("feedId", FilterOperator.EQUAL, feedId);
		Query commentQuery = new Query("Comment").setFilter(feedFilter).addSort("createdDate", SortDirection.ASCENDING);
		PreparedQuery pq = datastore.prepare(commentQuery);
		List<Entity> et = pq.asList(Builder.withDefaults());
		return et;
	}

	private Entity retrieveUserFromDB(String userId) {
		Filter userFilter = new FilterPredicate("userId", FilterOperator.EQUAL, userId);
		Query userquery = new Query("User").setFilter(userFilter);
		Entity userEntity = datastore.prepare(userquery).asSingleEntity();
		return userEntity;
	}

	public String convertObjectToString(Object obj) {
		String jsonResponse = "no feeds";
		ObjectMapper mapper = new ObjectMapper();
		try {
			jsonResponse = mapper.writeValueAsString(obj);
		} catch (JsonProcessingException e) {
			e.printStackTrace();
			return "Something went wrong while converting...";
		}
		return jsonResponse;
	}

	private Map<String, Object> parseJSONFromString(String data) {
		ObjectMapper mapper = new ObjectMapper();
		Map<String, Object> payload = new HashMap<String, Object>();
		try {
			payload = mapper.readValue(data, new TypeReference<Map<String, Object>>() {
			});
		} catch (IOException e) {

			e.printStackTrace();
		}
		return payload;
	}

	public String generateFeedId() {
		String feedId = "feed-" + UUID.randomUUID().toString();
		return feedId;
	}

}
