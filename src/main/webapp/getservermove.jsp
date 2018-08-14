<%@ page language="java" contentType="application/json; charset=UTF-8" pageEncoding="UTF-8" %>
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
		(request.getParameter("moveNumber") == null)
	) {
		response.sendRedirect("index.jsp");
	} else {

		GameManager gameManager = new GameManager();
		int moveNumber = Integer.parseInt(request.getParameter("moveNumber"));

		pageContext.setAttribute("user", user, PageContext.SESSION_SCOPE);
		gameKey = Key.fromUrlSafe(pageContext.getAttribute("gameKey", PageContext.SESSION_SCOPE).toString());

		gameManager.setGameKey(gameKey);

		if (gameManager.getOpponentMoveStatus(user, moveNumber) == 0) {
			%>{"status": "WAITING", "nextMoveNumber": <%= moveNumber %>}<%
		} else if (
			gameManager.getPlayerMoveStatus(user, moveNumber) == 1 &&
			gameManager.getOpponentMoveStatus(user, moveNumber) == 1
		) {
			String[] playerMove = gameManager.getPlayerMove(user, moveNumber);
			String[] opponentMove = gameManager.getOpponentMove(user, moveNumber);
%>
			{
				"nextMoveNumber": "<%= gameManager.createGameMove(moveNumber) %>",
				"playerFlipCard": "<%= playerMove[0] %>",
				"playerPlayCard": "<%= playerMove[1] %>",
				"opponentFlipCard": "<%= opponentMove[0] %>",
				"opponentPlayCard": "<%= opponentMove[1] %>"
			}
<%
		} else {
			%>{"status": "ERROR", "nextMoveNumber": <%= moveNumber %>}<%
		}
	}
%>