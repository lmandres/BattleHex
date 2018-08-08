<%@ page language="java" contentType="application/json; charset=ISO-8859-1" pageEncoding="ISO-8859-1" %>
<%@ page import="battlehex.BoardHelper" %>
<%@ page import="battlehex.GameManager" %>
<%@ page import="com.google.appengine.api.users.User" %>
<%@ page import="com.google.appengine.api.users.UserService" %>
<%@ page import="com.google.appengine.api.users.UserServiceFactory" %>
<%@ page import="com.google.cloud.datastore.Key" %>
<%
	UserService userService = UserServiceFactory.getUserService();
	User user = userService.getCurrentUser();

	Key gameKey = null;

	if (
		(user == null) ||
		(pageContext.getAttribute("gameKey", PageContext.SESSION_SCOPE) == null)
	) {
		response.sendRedirect("index.jsp");
	} else {

		GameManager gameManager = new GameManager();

		pageContext.setAttribute("user", user, PageContext.SESSION_SCOPE);
		gameKey = Key.fromUrlSafe(pageContext.getAttribute("gameKey", PageContext.SESSION_SCOPE).toString());

		gameManager.setGameKey(gameKey);

		if (
			request.getParameter("flipCard") != null &&
			request.getParameter("playCard") != null
		) {
			int coords[] = gameManager.getMoveRowColumn(request.getParameter("flipCard"), request.getParameter("playCard"));
			if (coords[0] == 0 || coords[1] == 0) {
				%>{"status": "ERROR"}<%
			} else {
				%>{"status": "OK"}<%
			}
		} else {
			%>{"status": "ERROR"}<%
		}
	}
%>