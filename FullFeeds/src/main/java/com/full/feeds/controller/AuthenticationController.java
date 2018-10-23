package com.full.feeds.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.full.feeds.dao.UserDAO;
import org.springframework.http.*;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.stereotype.Controller;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.*;

@Controller
@RequestMapping("/auth")
public class AuthenticationController extends HttpServlet {

	@RequestMapping(value = "/googleauthcallback", method = RequestMethod.GET)
	public @ResponseBody String authenticateWithGoogle(@RequestParam("code") String authCode,
			@RequestParam(value = "prompt", required = false) String prompt,
			@RequestParam(value = "state", required = false) String stateTokenFromGoogle, HttpServletRequest request,
			HttpServletResponse response) {
		
			UserDAO userDAO = new UserDAO();
			String responseString = "";
			HttpSession session = request.getSession(false);

			// user already logged in
			if (session != null && session.getAttribute("user") != null) {
				try {
					System.out.println("\n details in the session..\n");
					response.sendRedirect("/");
					return "session already available";
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
	

			ObjectMapper resp = new ObjectMapper();
			HashMap<String, String> responseMap = new HashMap<>();
			// String response = "";

			System.out.println("\nState returned from google: " + stateTokenFromGoogle + "\n");

			String stateSetByUs = session.getAttribute("google_state") != null
					? session.getAttribute("google_state").toString()
					: "";

			System.out.println("\nState applied from our issue reporter: " + stateSetByUs + "\n");

			Map<String, Object> googleResponseAsMap = new HashMap<>();

			// validating the state tokens here
			if (stateTokenFromGoogle.equals(stateSetByUs)) {

				// clear the session attribute
				session.removeAttribute("google_state");
				// hack code to find the host
				String uri = request.getRequestURI();
				String base_url = request.getRequestURL().toString();
				base_url = base_url.replaceFirst(uri, "");

				googleResponseAsMap = exchangeAuthCodeForAccessTokenWithGoogle(authCode, base_url + "/auth/googleauthcallback");
				Map<String, Object> user_details = getUserDetails_jwt(googleResponseAsMap.get("id_token").toString());

				// if not an existing user(a new user) ->
				// redirect to sign up page with pre-filled data from google such as user's
				// name, email etc.
				// setting google's id token to the session

				if (userDAO.readUser(user_details.get("email").toString().toLowerCase()) == null) {

					// if user fails to signup in 10 mins, the cookie will expire
					System.out.println("\n" + user_details.get("email").toString() + " is a new user!" + "\n");

					// set the user details as jwt
					// this user jwt will expire in 10 minutes regardless of whether the user signed
					// in or not
					Cookie user_cookie_temporary = new Cookie("user_details",
							googleResponseAsMap.get("id_token").toString());
					user_cookie_temporary.setMaxAge(300);
					user_cookie_temporary.setDomain("localhost");
					user_cookie_temporary.setPath("/");
					response.addCookie(user_cookie_temporary);

					session.setAttribute("google_signup_token", user_details);

					try {
						response.sendRedirect("/");
					} catch (IOException e1) {
						e1.printStackTrace();
					}
					return "new user";
				}

				System.out.println("\n" + user_details.get("email").toString() + " is an existing user!" + "\n");

				if (!googleResponseAsMap.isEmpty()) {
					responseMap.put("ok", "success");
					responseMap.put("refreshToken", googleResponseAsMap.get("refresh_token").toString());
					responseMap.put("accessToken", googleResponseAsMap.get("access_token").toString());
					responseMap.put("expires in", googleResponseAsMap.get("expires_in").toString());
					responseMap.put("id_token", googleResponseAsMap.get("id_token").toString());
					responseMap.put("user_email", user_details.get("email").toString());
					responseMap.put("email_verified", user_details.get("email_verified").toString());
					responseMap.put("user_picture", user_details.get("picture").toString());

					// set user details to user's session ID
					request.getSession().setAttribute("user", user_details);

					// set user details in a cookie
					Cookie user_details_cookie = new Cookie("user_jwt", responseMap.get("id_token"));
					user_details_cookie.setDomain("localhost");
					user_details_cookie.setPath("/");
					response.addCookie(user_details_cookie);

					try {
						response.sendRedirect("/landingpage");
					} catch (IOException e1) {
						e1.printStackTrace();
						System.out.println("landingpage.html doesn't exist");
					}
				}
			}

			else {

				responseMap.put("ok", "failed to authenticate");
				responseMap.put("reason", "Invalid state Token");
			}
			try {
				responseString = resp.writeValueAsString(responseMap);
			} catch (JsonProcessingException e) {
				e.printStackTrace();
			}

			return responseString;
	}

	// String urlParameters = "code=" + code
	// +
	// "&client_id=1038305202126-nurp4kb4jdlsp27ep81u77bqpt7astg1.apps.googleusercontent.com"
	// + "&client_secret=RNudvJGko75fcmBXwBkkV6ix" +
	// "&redirect_uri=http://localhost:8080/googleauthcallback"
	// + "&grant_type=authorization_code";
	//
	// URL url = new URL("https://accounts.google.com/o/oauth2/token");
	// URLConnection urlConn = url.openConnection();
	// urlConn.setDoOutput(true);
	// OutputStreamWriter writer = new
	// OutputStreamWriter(urlConn.getOutputStream());
	// writer.write(urlParameters);
	// writer.flush();
	//
	// String line, outputString = "";
	// BufferedReader reader = new BufferedReader(new
	// InputStreamReader(urlConn.getInputStream()));
	// while ((line = reader.readLine()) != null) {
	// outputString += line;
	// }
	// String jsonResponse = "";
	// // Access Token
	// ObjectMapper mapper = new ObjectMapper();
	// try {
	// jsonResponse = mapper.writeValueAsString(outputString);
	// } catch (JsonProcessingException e) {
	// e.printStackTrace();
	// }
	//
	// System.out.println("JSON " + jsonResponse);
	// }catch(
	//
	// Exception e)
	// {
	// e.printStackTrace();
	// }
	// url = new URL("https://www.googleapis.com/oauth2/v1/userinfo?access_token=" +
	// access_token);
	// urlConn = url.openConnection();
	// outputString = "";
	// reader = new BufferedReader(new InputStreamReader(urlConn.getInputStream()));
	// while ((line = reader.readLine()) != null) {
	// outputString += line;
	// }
	//
	// System.out.println("redirecting to index.html");
	//
	// }

	private Map<String, Object> exchangeAuthCodeForAccessTokenWithGoogle(String authorization_code,
			String redirect_uri) {

		// state must have been verified on the previous end point itself
		// use the authorization code and the redirect uri to post request to google
		// server
		// get the token response and return it to the callee
		Map<String, Object> googleResponse = new HashMap<>();

		if (!authorization_code.isEmpty() && !redirect_uri.isEmpty()) {

			RestTemplate restTemplate = new RestTemplate();
			restTemplate.getMessageConverters().add(0, new StringHttpMessageConverter(Charset.forName("UTF-8")));

			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

			MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
			params.add("code", authorization_code);
			params.add("client_id", Credentials.CLIENT_ID);
			params.add("client_secret", Credentials.CLIENT_SECRET);
			params.add("grant_type", "authorization_code");
			params.add("redirect_uri", redirect_uri);

			System.out.print("\nAuthcode: " + authorization_code + "\n");

			HttpEntity<MultiValueMap<String, String>> requestHeader = new HttpEntity<MultiValueMap<String, String>>(
					params, headers);

			String authResponseJson = "auth response error";

			try {
				ResponseEntity<String> authResponse_entity = restTemplate.exchange(
						"https://www.googleapis.com/oauth2/v4/token", HttpMethod.POST, requestHeader, String.class);

				if (authResponse_entity.getStatusCode().is2xxSuccessful()) {
					authResponseJson = authResponse_entity.getBody();
				} else {
					authResponseJson = authResponse_entity.getBody();
				}
			} catch (HttpClientErrorException e) {
				e.printStackTrace();
				System.out.println("Invalid post requst to google");
				googleResponse.put("error message", "Client exception");
			}
			try {
				googleResponse = new ObjectMapper().readValue(authResponseJson,
						new TypeReference<Map<String, Object>>() {
						});
			} catch (IOException e1) {
				e1.printStackTrace();
				System.out.println("Error while trying to parse Google's response as json");
				googleResponse.put("error message", "json exception");
			}

		}
		System.out.println("Google Response " + googleResponse);
		return googleResponse;
	}

	private Map<String, Object> getUserDetails_jwt(String jsonWebToken) {

		String payload = jsonWebToken.split("\\.")[1];
		String user_details_json = new String(Base64.getDecoder().decode(payload));
		Map<String, Object> user_details = null;
		try {
			user_details = new ObjectMapper().readValue(user_details_json, new TypeReference<Map<String, Object>>() {
			});
		} catch (IOException e1) {
			e1.printStackTrace();
			System.out.println("json exception");
		}
		System.out.println("User Details " + user_details);
		return user_details;
	}
}
