package com.full.feeds.controller;

import java.io.IOException;


import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.full.feeds.dao.FeedDAO;
import com.full.feeds.daointerface.FeedDAOInterface;
@WebServlet(name = "DeleteFeed", urlPatterns = "/deletefeed")
public class DeleteFeed extends HttpServlet {

	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
		
		String feedId = request.getParameter("id");
		System.out.println("ID : "+feedId );
		FeedDAOInterface feedDAOObj = new FeedDAO();
		if(feedDAOObj.deleteFeed(feedId)) {
			response.getWriter().print("feed delete successfully");
		}
		else {
			response.getWriter().print("something went wrong while deleting the feed...");
		}
	}
}
