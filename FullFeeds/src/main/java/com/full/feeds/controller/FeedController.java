package com.full.feeds.controller;

import java.io.IOException;
import java.util.*;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.full.feeds.dao.FeedDAO;
import com.full.feeds.daointerface.FeedDAOInterface;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;

@Controller
@RequestMapping(value = { "/feed" })
public class FeedController {
	
	private final FeedDAOInterface feedDAOObject;	

	public FeedController() {
		feedDAOObject = new FeedDAO();
		  }

	@RequestMapping(value = "/create/{userId}", method = RequestMethod.POST, consumes = "application/json")
	public @ResponseBody String createFeed(@PathVariable("userId") String userId, @RequestBody String feedData)
			throws JsonParseException, JsonMappingException, IOException {
		
		return feedDAOObject.createFeed(userId, feedData);
	}

	@RequestMapping(value = "/read/{userId}", method = RequestMethod.GET, produces = "application/json")
	public @ResponseBody String readFeedsByUser(@PathVariable("userId") String userId)
			throws JsonParseException, JsonMappingException, IOException {
		return feedDAOObject.readUserFeeds(userId);
	}

	@RequestMapping(value = "/read/all", method = RequestMethod.GET, produces = "application/json")
	public @ResponseBody String readAllFeeds(@RequestParam(value = "cursor", required = false) String cursor)
			throws JsonParseException, JsonMappingException, IOException {
		return feedDAOObject.readAllFeeds(cursor);
		
	}
	@RequestMapping(value = "/like", method = RequestMethod.POST, produces = "application/json", consumes="application/json")
	public @ResponseBody String feedLike(@RequestBody String feedData)
	{	
		return feedDAOObject.likeFeed(feedData);
	}
	@RequestMapping(value = "/comment", method = RequestMethod.POST, produces = "application/json", consumes="application/json")
	public @ResponseBody String feedComment(@RequestBody String feedData) {
		return feedDAOObject.commentFeed(feedData);
	}
	@RequestMapping(value = "/update", method = RequestMethod.PUT, produces = "application/json", consumes="application/json")
	public @ResponseBody String updateFeed(@RequestBody String feedData) {
		return feedDAOObject.updateFeed(feedData);
	}
	
}
