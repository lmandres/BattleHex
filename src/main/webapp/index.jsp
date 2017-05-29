<%@ page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1" %>
<%@ page import="com.google.appengine.api.users.User" %>
<%@ page import="com.google.appengine.api.users.UserService" %>
<%@ page import="com.google.appengine.api.users.UserServiceFactory" %>
<%@ page import="org.apache.commons.lang3.StringEscapeUtils" %>

<html>
  <head>
    <meta http-equiv="content-type" content="text/html; charset=ISO-8859-1">
    <title>Battle Hex</title>
    <link type="text/css" rel="stylesheet" href="/stylesheets/main.css"/>
    <link rel="shortcut icon" href="/favicon.ico">
  </head>
  <script type="text/javascript">

    var user = null;

    getUserID = function() {

      let provider = new firebase.auth.GoogleAuthProvider();

      firebase.auth().getRedirectResult().then(function(result) {

        if (result.user == null) {
          firebase.auth().signInWithRedirect(provider);
        }

        // This gives you a Google Access Token. You can use it to access the Google API.
        let token = result.credential.accessToken;
        user = result.user;

      }).catch(function(error) {
        console.log("error: ", error);
      });
    }

    createNewGame = function() {

      let newGameData = {
        "player1UID" : user.uid,
        "player2UID" : null,
        "startDate" : new Date.getTime()
      };

      // Get a key for a new Game.
      let newGameKey = firebase.database().ref().child("games").push().key;

      // Write the new post's data simultaneously in the posts list and the user's post list.
      let updates = {};
      updates['/games/' + newGameKey] = newGameData;
      updates['/user-games/' + user.uid + '/' + newGameKey] = newGameData;

      let updatePromise = firebase.database().ref().update(updates);
      updatePromise.then(function() {
        window.location.assign("/battlehexboard.jsp?gameKey=" + newGameKey);
      });
    }

    joinExistingGame = function() {
      let existingGames = firebase.database().ref().child("games").orderByChild("player2UID").equalTo(null).on("value", function(snapshot) {
        let snapshotVals = snapshot.val();
        Object.keys(snapshotVals).forEach(function(snapshotKey) {
          console.log("key: ", snapshotKey, " val: ", snapshotVals[snapshotKey]);
        });
      });
    }

    signOut = function() {
      firebase.auth().signOut().then(function() {
        console.log("Sign-out successful.");
      }).catch(function(error) {
        console.log("An error happened: ", error);
      });
      return true;
    }
  </script>

<%
	UserService userService = UserServiceFactory.getUserService();
	User user = userService.getCurrentUser();
	if (user != null) {
		pageContext.setAttribute("user", user);
%>
  <body onload="getUserID();">

	<p>Hello, <%= StringEscapeUtils.escapeXml(user.getNickname()) %>! (You can <a href="<%= userService.createLogoutURL(request.getRequestURI()) %>" onclick="signOut();">sign out</a>)</p>

	<ul>
		<li><a id="newGameLink" href="#" onclick="createNewGame();">Start a game.</a></li>
		<li><a href="#" onclick="joinExistingGame();">Join a game.</a></li>
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
  <script src="https://www.gstatic.com/firebasejs/4.0.0/firebase.js"></script>
  <script type="text/javascript">
    // Initialize Firebase
    var config = {
      apiKey: "AIzaSyBpfTxIJdBS82-t5NnThgL1LlfbfEK1dVE",
      authDomain: "battle-hex.firebaseapp.com",
      databaseURL: "https://battle-hex.firebaseio.com",
      projectId: "battle-hex",
      storageBucket: "battle-hex.appspot.com",
      messagingSenderId: "384944290573"
    };
    firebase.initializeApp(config);
  </script>
</html>