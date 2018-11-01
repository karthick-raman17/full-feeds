package com.full.feeds.daointerface;

import java.util.Map;

import com.full.feeds.model.User;
import com.google.appengine.api.datastore.Entity;

public interface UserDAOInterface {
	String createUser(Map<String, Object> userData);
	Entity readUser(String email);
	boolean updateUser(String email);
	boolean deleteUser(String email);
	Entity checkNewUser(String email);
	String generateUserID();
	String readAllUsers();
	
}
