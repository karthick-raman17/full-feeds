package com.full.feeds.controller;

import java.io.IOException;


import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.full.feeds.dao.FeedDAO;
import com.full.feeds.daointerface.FeedDAOInterface;
import com.full.feeds.model.Feed;

@WebServlet(name = "UpdateFeed", urlPatterns = "/updatefeed")
public class UpdateFeed extends HttpServlet {

	@Override
	public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
		
		String feedId = request.getParameter("id");
		String updatedValue = request.getParameter("updatedValue");
		System.out.println("ID : "+feedId +" Value : "+updatedValue);
		FeedDAOInterface feedDAOObj = new FeedDAO();
		Feed feedObj = new Feed();
		feedObj.setFeedValue(updatedValue);
		feedObj.setId(feedId);
		response.getWriter().print(feedDAOObj.updateFeed(feedObj));
	}
	

}