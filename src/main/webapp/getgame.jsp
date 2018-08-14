<%@ page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1" %>
<%@ page import="battlehex.BoardHelper" %>
<%@ page import="battlehex.GameManager" %>
<%@ page import="com.google.appengine.api.users.User" %>
<%@ page import="com.google.appengine.api.users.UserService" %>
<%@ page import="com.google.appengine.api.users.UserServiceFactory" %>
<%@ page import="com.google.cloud.datastore.Key" %>
<%
	UserService userService = UserServiceFactory.getUserService();
	User user = userService.getCurrentUser();

	GameManager gameManager = new GameManager();

	Key gameKey = null;

	if (user == null) {
		response.sendRedirect("index.jsp");
	} else {

		pageContext.setAttribute("user", user, PageContext.SESSION_SCOPE);

		if (request.getParameter("action") != null) {

			if (request.getParameter("action").equals("1")) {
				// New Game
				int player = BoardHelper.FIRST_PLAYER;
				if (request.getParameter("player") != null) {
					if (request.getParameter("player").equals("1")) {
						player = BoardHelper.FIRST_PLAYER;
					} else if (request.getParameter("player").equals("2")) {
						player = BoardHelper.SECOND_PLAYER;
					}
				}

				gameKey = gameManager.getNewGame(user, player, 13, 13);
				pageContext.setAttribute("player", player, PageContext.SESSION_SCOPE);
				pageContext.setAttribute("gameKey", gameKey.toUrlSafe(), PageContext.SESSION_SCOPE);
				pageContext.setAttribute("moveNumber", gameManager.createGameMove(1), PageContext.SESSION_SCOPE);

			} else if (request.getParameter("action").equals("2")) {
				// Join Game
				if (request.getParameter("gameKey") != null) {
					gameKey = gameManager.joinExistingGame(user, request.getParameter("gameKey").toString());
					if (gameKey != null) {
						pageContext.setAttribute("gameKey", gameKey.toUrlSafe(), PageContext.SESSION_SCOPE);
						pageContext.setAttribute("moveNumber", gameManager.createGameMove(1), PageContext.SESSION_SCOPE);
					}
				}
			} else if (request.getParameter("action").equals("3")) {
				// Play against AI.
				int player = BoardHelper.FIRST_PLAYER;
				if (request.getParameter("player") != null) {
					if (request.getParameter("player").equals("1")) {
						player = BoardHelper.FIRST_PLAYER;
					} else if (request.getParameter("player").equals("2")) {
						player = BoardHelper.SECOND_PLAYER;
					}
				}

				gameKey = gameManager.getNewGame(user, player, 13, 13);
				gameKey = gameManager.joinExistingGame(gameKey.toUrlSafe());

				pageContext.setAttribute("gameKey", gameKey.toUrlSafe(), PageContext.SESSION_SCOPE);
				pageContext.setAttribute("moveNumber", gameManager.createGameMove(1), PageContext.SESSION_SCOPE);
			}
		}

		response.sendRedirect("battlehex_vs_player.jsp");
	}
%>