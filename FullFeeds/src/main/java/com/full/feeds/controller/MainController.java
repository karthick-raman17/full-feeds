package com.full.feeds.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Controller
@RequestMapping("/")
public class MainController {

	@RequestMapping(value = "/", method = RequestMethod.GET)
	public ModelAndView displayLandingPage(HttpServletRequest servletRequest, HttpServletResponse servletResponse) {
			System.out.println("redirecting to index.html");
		return new ModelAndView("redirect:/index.html");

	}
}
