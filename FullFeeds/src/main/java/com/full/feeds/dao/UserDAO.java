package com.full.feeds.dao;

import java.util.UUID;
import com.full.feeds.daointerface.UserDAOInterface;
import com.full.feeds.model.User;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.Filter;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.google.appengine.api.datastore.Query.FilterPredicate;

public class UserDAO implements UserDAOInterface {
	DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
	
	@Override
	public boolean createUser(User user) {
		try {
			Entity newUser = new Entity("User");
			newUser.setProperty("userId",  user.getId());
			newUser.setProperty("email", user.getEmail());
			newUser.setProperty("name",user.getName());
			newUser.setProperty("status", user.getStatus());
			datastore.put(newUser);
		}catch(Exception e) {
			System.out.println("Can't able to create a user");
			e.printStackTrace();
			return false;
		}
		return true;
	}

	@Override
	public User readUser(String email) {
		
		return null;
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
		String usrID = "user_"+UUID.randomUUID().toString();
		return usrID;
	}

}
