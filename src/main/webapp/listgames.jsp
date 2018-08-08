<%@ page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1" %>
<%@ page import="com.google.appengine.api.users.User" %>
<%@ page import="com.google.appengine.api.users.UserService" %>
<%@ page import="com.google.appengine.api.users.UserServiceFactory" %>
<%@ page import="com.google.cloud.datastore.Datastore" %>
<%@ page import="com.google.cloud.datastore.DatastoreException" %>
<%@ page import="com.google.cloud.datastore.DatastoreOptions" %>
<%@ page import="com.google.cloud.datastore.Query" %>
<%@ page import="com.google.cloud.datastore.QueryResults" %>
<%@ page import="com.google.cloud.datastore.Entity" %>
<%@ page import="com.google.cloud.datastore.StructuredQuery.CompositeFilter" %>
<%@ page import="com.google.cloud.datastore.StructuredQuery.OrderBy" %>
<%@ page import="com.google.cloud.datastore.StructuredQuery.PropertyFilter" %>
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
	if (user == null) {
		response.sendRedirect("index.jsp");
	} else {

		pageContext.setAttribute("user", user);

		Datastore datastore = DatastoreOptions.getDefaultInstance().getService();
		Query<Entity> query1 = Query.newEntityQueryBuilder()
			.setKind("Game")
			.setFilter(PropertyFilter.eq("player1ID", ""))
			.addOrderBy(OrderBy.desc("lastModified"))
			.build()
		;
		Query<Entity> query2 = Query.newEntityQueryBuilder()
			.setKind("Game")
			.setFilter(PropertyFilter.eq("player2ID", ""))
			.addOrderBy(OrderBy.desc("lastModified"))
			.build()
		;

		QueryResults<Entity> games1 = datastore.run(query1);
		QueryResults<Entity> games2 = datastore.run(query2);
%>
  <body>

	<p>Hello, <%= StringEscapeUtils.escapeXml(user.getNickname()) %>! (You can <a href="<%= userService.createLogoutURL(request.getRequestURI()) %>" onclick="signOut();">sign out</a>)</p>

	<ul>
<%
		while (games1.hasNext()) {

			Entity game = games1.next();

			String player1Nickname = "";
			String player2Nickname = "";
			String gameKey = "";

			try {
				player1Nickname = game.getString("player1Nickname");
			} catch (DatastoreException de) {
			}
			try {
				player2Nickname = game.getString("player2Nickname");
			} catch (DatastoreException de) {
			}
			try {
				gameKey = game.getKey().toUrlSafe();
			} catch (DatastoreException de) {
			}
%>
		<li><a href="getgame.jsp?action=2&gameKey=<%= gameKey %>">Player 1: <%= player1Nickname %>; Player 2: <%= player2Nickname %></a></li>
<%
		}

		while (games2.hasNext()) {

			Entity game = games2.next();

			String player1Nickname = "";
			String player2Nickname = "";
			String gameKey = "";

			try {
				player1Nickname = game.getString("player1Nickname");
			} catch (DatastoreException de) {
			}
			try {
				player2Nickname = game.getString("player2Nickname");
			} catch (DatastoreException de) {
			}
			try {
				gameKey = game.getKey().toUrlSafe();
			} catch (DatastoreException de) {
			}
%>
		<li><a href="getgame.jsp?action=2&gameKey=<%= gameKey %>">Player 1: <%= player1Nickname %>; Player 2: <%= player2Nickname %></a></li>
<%
		}
%>
	</ul>
<%
	}
%>
  </body>
</html>
