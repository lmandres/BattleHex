<%@ page language="java" contentType="application/json; charset=UTF-8" pageEncoding="UTF-8" %>
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
		(pageContext.getAttribute("gameKey", PageContext.SESSION_SCOPE) == null) ||
		(request.getParameter("moveNumber") == null) ||
		(request.getParameter("flipCard") == null) ||
		(request.getParameter("playCard") == null)
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
				String[] prevMove = gameManager.getPlayerMove(user, Integer.parseInt(request.getParameter("moveNumber")));
				if (
					prevMove[0].equals("") &&
					prevMove[1].equals("")
				) {
					gameManager.putGameMove(user, request.getParameter("flipCard"), request.getParameter("playCard"));
					%>{"status": "OK"}<%
				} else {
					%>{"status": "ERROR"}<%
				}
			}
		} else {
			%>{"status": "ERROR"}<%
		}
	}
%>