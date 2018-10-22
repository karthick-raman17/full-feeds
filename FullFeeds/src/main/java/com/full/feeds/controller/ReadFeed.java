package com.full.feeds.controller;

import java.io.IOException;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.full.feeds.dao.FeedDAO;
import com.full.feeds.daointerface.FeedDAOInterface;

@WebServlet(name = "ReadFeed", urlPatterns = "/readfeeds")
public class ReadFeed extends HttpServlet{
	public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
		String userEmail = request.getParameter("email");
		FeedDAOInterface feedObj = new FeedDAO();
		response.getWriter().print(feedObj.readAllFeeds(userEmail).toString());
	}
}

