package com.full.feeds.controller;

import java.io.IOException;

import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.full.feeds.dao.FeedDAO;
import com.full.feeds.dao.UserDAO;
import com.full.feeds.daointerface.FeedDAOInterface;
import com.full.feeds.daointerface.UserDAOInterface;
import com.full.feeds.model.Feed;
import com.full.feeds.model.User;
import com.google.appengine.api.datastore.Entity;

@WebServlet(name = "NewFeed", urlPatterns = "/newfeed")
public class NewFeed extends HttpServlet {

	@Override
	public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
	
		response.setContentType("application/json");
		response.setCharacterEncoding("UTF-8");
		PrintWriter out = response.getWriter();
		String feedValue = request.getParameter("feedText");
		String currentDate = request.getParameter("currentDate");
		System.out.println(feedValue);

		Map<String, Object> responseString = new HashMap<>();
		responseString.put("isUpdated", false);
		ObjectMapper mapper = new ObjectMapper();
		String jsonResponse = "no feeds";
		
		String userEmail = "karthick.raman@anywhere.co";
		UserDAOInterface userDAOObj = new UserDAO();
		FeedDAOInterface feedDAOObj = new FeedDAO();
		
		
		Feed feedObj = new Feed();
		
		Entity userEntity = userDAOObj.checkNewUser(userEmail);
		if (userEntity == null) {
			String userId = userDAOObj.generateUserID();
			User obj = new User();
			obj.setId(userId);
			obj.setName("Karthick");
			obj.setEmail("karthick.raman@anywhere.co");
			obj.setStatus(true);
			if (userDAOObj.createUser(obj)) {
				feedObj.setId(feedDAOObj.generateFeedId());
				feedObj.setFeedValue(feedValue);
				feedObj.setCreatedTime(currentDate);
				feedObj.setUserId(userId);
				feedObj.setStatus(true);
			}
		} else {
			feedObj.setId(feedDAOObj.generateFeedId());
			feedObj.setFeedValue(feedValue);
			feedObj.setCreatedTime(currentDate);
			feedObj.setStatus(true);
			feedObj.setUserId(userEntity.getProperty("userId").toString());
		}

		if (feedDAOObj.createFeed(feedObj)) {
			responseString.put("isUpdated", true);
			responseString.put("feed", feedObj.getFeedValue());
			responseString.put("createdDate", feedObj.getCreatedTime());
			responseString.put("status", feedObj.getStatus());
			responseString.put("id",feedObj.getId());
			try {
				jsonResponse = mapper.writeValueAsString(responseString);
			} catch (JsonProcessingException e) {
				e.printStackTrace();
			}

			out.print(jsonResponse);
			out.flush();
		} else {
			responseString.put("feed", "no feed");
			responseString.put("status", false);
			try {
				jsonResponse = mapper.writeValueAsString(responseString);
			} catch (JsonProcessingException e) {
				e.printStackTrace();
			}
			out.print(jsonResponse);
		}
	}
}
