<%@ page language="java" contentType="application/json; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="battlehex.BattleHexAI" %>
<%@ page import="battlehex.BoardHelper" %>
<%@ page import="battlehex.GameManager" %>
<%@ page import="java.util.HashMap" %>
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

		BattleHexAI gameAI = new BattleHexAI();
		GameManager gameManager = new GameManager();

		HashMap<String, Integer> aiCoords = new HashMap<String, Integer>();
		HashMap<String, String> aiMove = new HashMap<String, String>();

		int moveNumber = Integer.parseInt(request.getParameter("moveNumber"));
		pageContext.setAttribute("user", user, PageContext.SESSION_SCOPE);
		gameKey = Key.fromUrlSafe(pageContext.getAttribute("gameKey", PageContext.SESSION_SCOPE).toString());

		gameManager.setGameKey(gameKey);
		gameAI.setMonteCarloIterations(100);

		System.out.println(
			"moveNumber: " + Integer.toString(moveNumber) + "; " +
			Integer.toString(gameManager.getOpponentMoveStatus(user, moveNumber)));

		if (gameManager.getOpponentMoveStatus(user, moveNumber) == 0) {
			aiCoords = gameAI.calculateComputerMove(gameManager.getPlayer(user), gameAI.convertBoardArraysToLists(gameManager.getGameMoves()));
			aiMove = gameManager.getRandomCardsFromCoords(aiCoords.get("row"), aiCoords.get("column"));
			gameManager.putComputerMove(aiMove.get("flipCard"), aiMove.get("playCard"));
		} else if (gameManager.getOpponentMoveStatus(user, moveNumber) == 1) {
			String[] cards = gameManager.getOpponentMove(user, moveNumber);
			System.out.println(
				"cards[0]: " + cards[0] + "; " +
				"cards[1]: " + cards[1] + ";"
			);
		}

		if (gameManager.getOpponentMoveStatus(user, moveNumber) == 0) {
			%>{"status": "WAITING"}<%
		} else if (gameManager.getOpponentMoveStatus(user, moveNumber) == 1) {
			%>{"status": "READY"}<%
		} else {
			%>{"status": "ERROR"}<%
		}
	}
%>