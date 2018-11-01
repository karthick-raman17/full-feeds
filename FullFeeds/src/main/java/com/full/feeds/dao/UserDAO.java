package com.full.feeds.dao;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import java.util.UUID;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.full.feeds.daointerface.UserDAOInterface;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.FetchOptions.Builder;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.Filter;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.google.appengine.api.datastore.Query.FilterPredicate;
import com.google.appengine.api.datastore.Query.SortDirection;

public class UserDAO implements UserDAOInterface {
	DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

	@Override
	public String createUser(Map<String, Object> userData) {
		String userId = null;
		try {
			userId = generateUserID();
			Entity newUser = new Entity("User");
			newUser.setProperty("userId", userId);
			newUser.setProperty("email", userData.get("email").toString());
			newUser.setProperty("name", userData.get("name").toString());
			newUser.setProperty("status", "Active");
			datastore.put(newUser);
		} catch (Exception e) {
			System.out.println("Can't able to create a user");
			e.printStackTrace();
			return null;
		}
		return userId;
	}

	@Override
	public Entity readUser(String email) {
		Filter userFilter = new FilterPredicate("email", FilterOperator.EQUAL, email);
		Query query = new Query("User").setFilter(userFilter);
		PreparedQuery pq = datastore.prepare(query);
		Entity checkUser = pq.asSingleEntity();
		return checkUser;
	}

	@Override
	public boolean updateUser(String email) {

		return false;
	}

	@Override
	public boolean deleteUser(String email) {

		return false;
	}

	@Override
	public Entity checkNewUser(String email) {
		Filter filter = new FilterPredicate("email", FilterOperator.EQUAL, email);
		Query query = new Query("User").setFilter(filter);
		PreparedQuery pq = datastore.prepare(query);
		Entity checkUser = pq.asSingleEntity();
		return checkUser;
	}

	public String generateUserID() {
		String usrID = "user-" + UUID.randomUUID().toString();
		return usrID;
	}

	@Override
	public String readAllUsers() {
		Query query = new Query("User").addSort("name", SortDirection.ASCENDING);
		PreparedQuery pq = datastore.prepare(query);
		List<Entity> checkUser = pq.asList(Builder.withDefaults());

		ArrayList<Object> allUsers = new ArrayList<>();
		Iterator<Entity> userItr = checkUser.iterator();
		while (userItr.hasNext()) {
			Entity temp = userItr.next();
			Map<String, Object> responseString = new HashMap<>();
			if (temp.getProperty("status").equals("Active")) {
				responseString.put("id", temp.getProperty("userId"));
				responseString.put("name", temp.getProperty("name"));
				responseString.put("email", temp.getProperty("email"));
				allUsers.add(responseString);
			}
		}
		if (allUsers.isEmpty()) {
			Map<String, Object> response = new HashMap<>();
			response.put("status", false);
			response.put("message", "no feeds");
			return convertObjectToString(response);
		} else {
			Map<String, Object> response = new HashMap<>();
			response.put("status", true);
			response.put("data", allUsers);
			return convertObjectToString(response);
		}

	}

	private String convertObjectToString(Object obj) {
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

}
