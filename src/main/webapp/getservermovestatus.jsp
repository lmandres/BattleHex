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
		gameAI.setMonteCarloIterations(1000);

		if (
			!(
				BoardHelper.evaluateWin(1, gameManager.getGameMoves()) ||
				BoardHelper.evaluateWin(2, gameManager.getGameMoves())
			) && gameManager.getOpponentMoveStatus(user, moveNumber) == 0
		) {
			aiCoords = gameAI.calculateComputerMove(gameManager.getPlayer(user), BoardHelper.convertBoardArraysToLists(gameManager.getGameMoves()));
			aiMove = gameManager.getRandomCardsFromCoords(aiCoords.get("row"), aiCoords.get("column"));
			gameManager.putComputerMove(aiMove.get("flipCard"), aiMove.get("playCard"));
		}

		if (
			!(
				BoardHelper.evaluateWin(1, gameManager.getGameMoves()) ||
				BoardHelper.evaluateWin(2, gameManager.getGameMoves())
			) && gameManager.getOpponentMoveStatus(user, moveNumber) == 0
		) {
			%>{"status": "WAITING"}<%
		} else if (BoardHelper.evaluateWin(1, gameManager.getGameMoves())) {
			%>{"status": "WON", "player": 1}<%
		} else if (BoardHelper.evaluateWin(2, gameManager.getGameMoves())) {
			%>{"status": "WON", "player": 2}<%
		} else if (gameManager.getOpponentMoveStatus(user, moveNumber) == 1) {
			%>{"status": "READY"}<%
		} else {
			%>{"status": "ERROR"}<%
		}
	}
%>