<%@ page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1" %>
<%@ page import="com.google.appengine.api.users.User" %>
<%@ page import="com.google.appengine.api.users.UserService" %>
<%@ page import="com.google.appengine.api.users.UserServiceFactory" %>
<%@ page import="org.apache.commons.lang3.StringEscapeUtils" %>

<html>
  <head>
    <meta http-equiv="content-type" content="text/html; charset=ISO-8859-1">
    <title>Battle Hex</title>
    <link type="text/css" rel="stylesheet" href="main.css"/>
    <link rel="shortcut icon" href="/favicon.ico">
    <link rel="manifest" href="/manifest.json">
  </head>

<%
	UserService userService = UserServiceFactory.getUserService();
	User user = userService.getCurrentUser();
	if (user != null) {
		pageContext.setAttribute("user", user);
%>
  <body>

	<p>Hello, <%= StringEscapeUtils.escapeXml(user.getNickname()) %>! (You can <a href="<%= userService.createLogoutURL(request.getRequestURI()) %>" onclick="signOut();">sign out</a>)</p>

	<ul>
		<li>
			Start a game
				(<a id="newGameLink" href="getgame.jsp?action=1&player=1">as first player</a> /
				<a id="newGameLink" href="getgame.jsp?action=1&player=2">as second player</a>).
		</li>
		<li><a href="listgames.jsp">Join a game.</a></li>
		<li>Resume a game.</li>
		<li>Watch a game.</li>
	</ul>
<%
	} else {
%>
  <body>

	<p>Hello! <a href="<%= userService.createLoginURL(request.getRequestURI()) %>">Sign in</a> to play.</p>

	<ul>
		<li>Watch a game.</li>
	</ul>
<%
	}
%>
  </body>
</html>
