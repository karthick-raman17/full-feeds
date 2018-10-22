package com.full.feeds.daointerface;

import com.full.feeds.model.User;
import com.google.appengine.api.datastore.Entity;

public interface UserDAOInterface {
	boolean createUser(User userObj);
	User readUser(String email);
	boolean updateUser(String email);
	boolean deleteUser(String email);
	Entity checkNewUser(String email);
	String generateUserID();
	
}
