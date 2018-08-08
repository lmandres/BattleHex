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

		int gameMoves[][];

		GameManager gameManager = new GameManager();

		pageContext.setAttribute("user", user, PageContext.SESSION_SCOPE);
		gameKey = Key.fromUrlSafe(pageContext.getAttribute("gameKey", PageContext.SESSION_SCOPE).toString());

		gameManager.setGameKey(gameKey);
		gameMoves = gameManager.getGameMoves();

		%>[<%
		for (int rowIndex = 0; rowIndex < gameMoves.length; rowIndex++) {
			%><%= (rowIndex != 0 ? "," : "") %>[<%
			for (int columnIndex = 0; columnIndex < gameMoves[rowIndex].length; columnIndex++) {
				%><%= (columnIndex != 0 ? "," : "") %><%= gameMoves[rowIndex][columnIndex] %><%
			}
			%>]<%
		}
		%>]<%
	}
%>