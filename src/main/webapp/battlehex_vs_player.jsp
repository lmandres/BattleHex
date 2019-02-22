<%@ page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1" %>
<%@ page import="battlehex.BoardHelper" %>
<%@ page import="battlehex.GameManager" %>
<%@ page import="com.google.appengine.api.users.User" %>
<%@ page import="com.google.appengine.api.users.UserService" %>
<%@ page import="com.google.appengine.api.users.UserServiceFactory" %>
<%@ page import="com.google.cloud.datastore.Key" %>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<!-- The HTML 4.01 Transitional DOCTYPE declaration-->
<!-- above set at the top of the file will set     -->
<!-- the browser's rendering engine into           -->
<!-- "Quirks Mode". Replacing this declaration     -->
<!-- with a "Standards Mode" doctype is supported, -->
<!-- but may lead to some differences in layout.   -->

<%
	UserService userService = UserServiceFactory.getUserService();
	User user = userService.getCurrentUser();

	if (user != null) {
		pageContext.setAttribute("user", user, PageContext.SESSION_SCOPE);
	} else {
		response.sendRedirect("index.jsp");
	}

	BoardHelper boardHelper = new BoardHelper();
	GameManager gameManager = new GameManager();

	boardHelper.setImageWidth(320);
	boardHelper.setImageHeight(550);
	boardHelper.setBoardShape(BoardHelper.VERTICAL_BOARD);
	boardHelper.setPlayer(BoardHelper.FIRST_PLAYER);

	if (
		pageContext.getAttribute("gameKey", PageContext.SESSION_SCOPE) == null ||
		pageContext.getAttribute("moveNumber", PageContext.SESSION_SCOPE) == null
	) {
		response.sendRedirect("index.jsp");
	}
	gameManager.setGameKeyUrlSafe((String)pageContext.getAttribute("gameKey", PageContext.SESSION_SCOPE));
	boardHelper.setPlayer(gameManager.getPlayer(user));

	boardHelper.setBoardRows(gameManager.getBoardRows());
	boardHelper.setBoardColumns(gameManager.getBoardColumns());

	String cardSet = "cardstux";
	String [][] ranks = {{"a","2","3","4"},{"5","6","7","8"},{"9","10","j","q"},{"k"}};
	String [] suits = boardHelper.getPlayerSuits().split(",");
%>

<html>
  <head>
    <meta http-equiv="content-type" content="text/html; charset=ISO-8859-1">
    <title>Battle Hex</title>
    <link type="text/css" rel="stylesheet" href="main.css" />
    <link rel="shortcut icon" href="favicon.ico" />
	<script type="text/javascript">
	
		let player = <%= boardHelper.getPlayer() %>;
		let opponent = (player % 2) + 1;

		let svgNS = "http://www.w3.org/2000/svg";
		let svgID = "svgBoard";
		
		let indexValues = ["a", "2", "3", "4", "5", "6", "7", "8", "9", "10", "j", "q", "k"];
		let moveNumber = <%= pageContext.getAttribute("moveNumber", PageContext.SESSION_SCOPE) %>;

		let playerFlipCard = null;
		let playerPlayCard = null;

		let opponentFlipCard = null;
		let opponentPlayCard = null;

		let opponentSuits = "<%= boardHelper.getOpponentSuits() %>".split(",");
		let deleteLine = null;

		let gameBoard = [];
		
		function supportsSVG() {
			return !!document.createElementNS && !!document.createElementNS(svgNS, "svg").createSVGRect;
		}
		
		function getXCoord(row, column) {
<%
	if (boardHelper.getPlayer() == BoardHelper.FIRST_PLAYER) {
%>
			return <%= boardHelper.getStartXCoord() %>+((column-row)*<%= boardHelper.getCellHalfWidth() %>);
<%
	} else if (boardHelper.getPlayer() == BoardHelper.SECOND_PLAYER) {
%>
			return <%= boardHelper.getStartXCoord() %>+(((<%= boardHelper.getBoardColumns()+1 %>-column)-(<%= boardHelper.getBoardRows()+1 %>-row))*<%= boardHelper.getCellHalfWidth() %>);
<%
	}
%>
		}
		
		function getYCoord(row, column) {
<%
	if (boardHelper.getPlayer() == BoardHelper.FIRST_PLAYER) {
%>
			return <%= boardHelper.getStartYCoord() %>+((column-row)*<%= boardHelper.getCellExtendedLength() %>)+((row-1)*<%= 2*boardHelper.getCellExtendedLength() %>);
<%
	} else if (boardHelper.getPlayer() == BoardHelper.SECOND_PLAYER) {
%>
			return <%= boardHelper.getStartYCoord() %>+(((<%= boardHelper.getBoardColumns()+1 %>-column)-(<%= boardHelper.getBoardRows()+1 %>-row))*<%= boardHelper.getCellExtendedLength() %>)+(((<%= boardHelper.getBoardRows() %>-row))*<%= 2*boardHelper.getCellExtendedLength() %>);
<%
	}
%>
		}
		
		function makeMove(card, playerIn) {

			let gameBoardSet = true;

			if (gameBoard.length != indexValues.length+2) {
				gameBoardSet = false;
			} else {
				for (let rowIndex = 0; (rowIndex < indexValues.length+2) || !gameBoardSet; rowIndex++) {
					if (gameBoard[rowIndex].length != indexValues.length+2) {
						gameBoardSet = false;
					}
				}
			}

			if (gameBoardSet) {

				if (playerIn == player) {
			
					if (
						document.getElementById(card).src != "<%= cardSet %>/back.png" &&
						(
							playerFlipCard == null ||
							playerPlayCard != null ||
							(
								playerPlayCard == null &&
								card.substr(0,1).toLowerCase() != playerFlipCard.substr(0,1).toLowerCase()	
							)
						)
					) {

						if (playerFlipCard == null) {
							playerFlipCard = card;
						} else if (playerPlayCard == null) {

							let playerMove = getPlayerRowColumn(playerFlipCard, card);

							if (
								playerMove["row"] &&
								playerMove["column"] &&
								gameBoard[playerMove["row"]][playerMove["column"]] == 0
							) {
								playerPlayCard = card;
							}
			
						} else {

							let playerMove = getPlayerRowColumn(playerFlipCard, playerPlayCard);
							if (playerMove["row"] != null && playerMove["column"] != null) {
								drawHexCell(playerMove["row"], playerMove["column"], "makeCellMove("+playerMove["row"]+","+playerMove["column"]+",player);");
							}

							playerFlipCard = card;
							playerPlayCard = null;
						}

						displayMove();
					}
				
					if (
						playerFlipCard != null &&
						playerPlayCard != null
					) {
						if (document.getElementById("randomizeFlipInput").checked) {

							randomizeMove(player);
				
							document.getElementById("playerFlipCardImg").src = document.getElementById(playerFlipCard).src;
							document.getElementById("playerPlayCardImg").src = document.getElementById(playerPlayCard).src;
						}
						document.getElementById("sendMove").disabled = false;
					} else {
						document.getElementById("sendMove").disabled = true;
					}

				} else {

					if (opponentFlipCard == null) {
						opponentFlipCard = card;
					} else if (opponentPlayCard == null) {
						opponentPlayCard = card;					
					} else {
						opponentFlipCard = card;
						opponentPlayCard = null;
					}
				}
					

				if (opponentPlayCard != null) {
					document.getElementById("opponentPlayCardImg").src = "<%= cardSet %>/back.png";
				}
				if (opponentFlipCard != null) {
					document.getElementById("opponentFlipCardImg").src = "<%= cardSet %>/back.png";
				}
			}
		}
		
		function makeCellMove(row, column, playerIn) {

			if (playerIn == player) {
				if ((playerPlayCard == null) && (playerFlipCard != null)) {
					if (deleteLine.substr(0,1) == "r") {
						makeMove("b"+indexValues[column-1], playerIn);
					} else if (deleteLine.substr(0,1) == "c") {
						makeMove("r"+indexValues[row-1], playerIn);
					}
				}

				makeMove("r"+indexValues[row-1], playerIn);
				makeMove("b"+indexValues[column-1], playerIn);

			} else {

				makeMove("r"+indexValues[row-1], playerIn);
				makeMove("b"+indexValues[column-1], playerIn);

				randomizeMove(playerIn);
			}
		}
		
		function randomizeMove(playerIn) {

			if (playerIn == player) {

				if (playerFlipCard != null && playerPlayCard != null) {
					if (Math.random() < 0.5) {
						let tempCard = playerFlipCard;
						playerFlipCard = playerPlayCard;
						playerPlayCard = tempCard;
					}
				
					document.getElementById("playerFlipCardImg").src = document.getElementById(playerFlipCard).src;
					document.getElementById("playerPlayCardImg").src = document.getElementById(playerPlayCard).src;
				}

			} else {
				if (Math.random() < 0.5) {
					let tempCard = opponentFlipCard;
					opponentFlipCard = opponentPlayCard;
					opponentPlayCard = tempCard;
				}
			}
		}

		function drawBoardPieces() {
			for (let rowIndex = 1; rowIndex <= indexValues.length; rowIndex++) {
				for (let columnIndex = 1; columnIndex <= indexValues.length; columnIndex++) {
					if (gameBoard[rowIndex][columnIndex] == 1) {
						drawHexPiece(rowIndex, columnIndex, "red");
					} else if (gameBoard[rowIndex][columnIndex] == 2) {
						drawHexPiece(rowIndex, columnIndex, "black");
					} else {
						drawHexCell(rowIndex, columnIndex, "makeCellMove("+rowIndex+","+columnIndex+",player);");
					}
				}
			}
		}

		function showMoves() {

			if (
				playerFlipCard != null &&
				playerPlayCard != null &&
				opponentFlipCard != null &&
				opponentPlayCard != null
			) {

				let playerMove = getPlayerRowColumn(playerFlipCard, playerPlayCard);
				let opponentMove = getPlayerRowColumn(opponentFlipCard, opponentPlayCard);

				if (opponentMove["row"] != null && opponentMove["column"] != null) {
					document.getElementById("opponentFlipCardImg").src = "<%= cardSet %>/" + opponentSuits["b,r".split(",").indexOf(opponentFlipCard.substr(0, 1))] + opponentFlipCard.substr(1) + ".png";
					document.getElementById("opponentPlayCardImg").src = "<%= cardSet %>/" + opponentSuits["b,r".split(",").indexOf(opponentPlayCard.substr(0, 1))] + opponentPlayCard.substr(1) + ".png";
				}

				if (
					playerMove["row"] == opponentMove["row"] &&
					playerMove["column"] == opponentMove["column"]
				) {
					tieBreaker();
				} else {

					loadGameBoard();

					playerFlipCard = null;
					playerPlayCard = null;
					opponentFlipCard = null;
					opponentPlayCard = null;

					getServerMoveStatus(moveNumber)
					.then(
						function(data) {
							if (data["status"].toUpperCase() == "READY") {
								document.getElementById("opponentPlayCardImg").src = "<%= cardSet %>/back.png";
								document.getElementById("opponentFlipCardImg").src = "<%= cardSet %>/back.png";
							}
						}
					);
				}

			} else {

				let playerCount = 0;
				let opponentCount = 0;

				for (let rowIndex = 1; rowIndex <= indexValues.length; rowIndex++) {
					for (let columnIndex = 1; columnIndex <= indexValues.length; columnIndex++) {
						if (gameBoard[rowIndex][columnIndex] == player) {
							playerCount++;
						} else if (gameBoard[rowIndex][columnIndex] == opponent) {
							opponentCount++;
						}
					}
				}

				if (
					(playerCount < opponentCount) &&
					playerFlipCard != null &&
					playerPlayCard != null
				) {

					let playerMove = getPlayerRowColumn(playerFlipCard, playerPlayCard);

					if (opponentFlipCard.substr(0, 1) == "r") {
						makeMove("r" + indexValues[playerMove["row"]-1], player);
						makeMove("b" + indexValues[playerMove["column"]-1], player);
					} else {
						makeMove("b" + indexValues[playerMove["column"]-1], player);
						makeMove("r" + indexValues[playerMove["row"]-1], player);
					}

					if (player == 1) {
						gameBoard[playerMove["row"]][playerMove["column"]] = player;
						drawHexPiece(playerMove["row"], playerMove["column"], "red");
					} else {
						gameBoard[playerMove["row"]][playerMove["column"]] = player;
						drawHexPiece(playerMove["row"], playerMove["column"], "black");
					}

					playerFlipCard = null;
					playerPlayCard = null;
					opponentFlipCard = null;
					opponentPlayCard = null;

					getServerMoveStatus(moveNumber)
					.then(
						function(data) {
							if (data["status"].toUpperCase() == "READY") {
								document.getElementById("opponentPlayCardImg").src = "<%= cardSet %>/back.png";
								document.getElementById("opponentFlipCardImg").src = "<%= cardSet %>/back.png";
							}
						}
					);
				}
			}
		}

		function tieBreaker() {

			if (
				playerFlipCard != null &&
				playerPlayCard != null &&
				opponentFlipCard != null &&
				opponentPlayCard != null
			) {

				let playerMove = getPlayerRowColumn(playerFlipCard, playerPlayCard);

				if (playerFlipCard == opponentFlipCard) {

					if (player == 1) {

						alert("Both move to \"" + playerFlipCard + "\", \"" + playerPlayCard + ".\"  Player wins move.");

						let opponentMove = {};

						gameBoard[playerMove["row"]][playerMove["column"]] = player;
						drawHexPiece(playerMove["row"], playerMove["column"], "red");

						requestServerMove()
						.then(
							function(data) {
								moveNumber = data["nextMoveNumber"];
								playerFlipCard = data["playerFlipCard"];
								playerPlayCard = data["playerPlayCard"];
								opponentFlipCard = data["opponentFlipCard"];
								opponentPlayCard = data["opponentPlayCard"];
								loadGameBoard();
							}
						);

						playerFlipCard = null;
						playerPlayCard = null;

						document.getElementById("opponentFlipCardImg").src = "<%= cardSet %>/" + opponentSuits["b,r".split(",").indexOf(opponentFlipCard.substr(0, 1))] + opponentFlipCard.substr(1) + ".png";
						document.getElementById("opponentPlayCardImg").src = "<%= cardSet %>/" + opponentSuits["b,r".split(",").indexOf(opponentPlayCard.substr(0, 1))] + opponentPlayCard.substr(1) + ".png";

					} else {

						alert("Both move to \"" + opponentFlipCard + "\", \"" + opponentPlayCard + ".\"  Opponent wins move.");

						let opponentMove = getPlayerRowColumn(opponentFlipCard, opponentPlayCard);

						playerFlipCard = null;
						playerPlayCard = null;

						gameBoard[opponentMove["row"]][opponentMove["column"]] = opponent;
						drawHexPiece(opponentMove["row"], opponentMove["column"], "red");
					}
					
				} else {

					if (player == 2) {

						alert("Both move to \"" + playerFlipCard + "\", \"" + playerPlayCard + ".\"  Player wins move.");

						let opponentMove = {};

						gameBoard[playerMove["row"]][playerMove["column"]] = player;
						drawHexPiece(playerMove["row"], playerMove["column"], "black");

						requestServerMove()
						.then(
							function(data) {
								moveNumber = data["nextMoveNumber"];
								playerFlipCard = data["playerFlipCard"];
								playerPlayCard = data["playerPlayCard"];
								opponentFlipCard = data["opponentFlipCard"];
								opponentPlayCard = data["opponentPlayCard"];
								loadGameBoard();
							}
						);

						playerFlipCard = null;
						playerPlayCard = null;

						document.getElementById("opponentFlipCardImg").src = "<%= cardSet %>/" + opponentSuits["b,r".split(",").indexOf(opponentFlipCard.substr(0, 1))] + opponentFlipCard.substr(1) + ".png";
						document.getElementById("opponentPlayCardImg").src = "<%= cardSet %>/" + opponentSuits["b,r".split(",").indexOf(opponentPlayCard.substr(0, 1))] + opponentPlayCard.substr(1) + ".png";

					} else {

						alert("Both move to \"" + opponentFlipCard + "\", \"" + opponentPlayCard + ".\"  Opponent wins move.");

						let opponentMove = getPlayerRowColumn(opponentFlipCard, opponentPlayCard);

						playerFlipCard = null;
						playerPlayCard = null;

						gameBoard[opponentMove["row"]][opponentMove["column"]] = opponent;
						drawHexPiece(opponentMove["row"], opponentMove["column"], "black");
					}
				}
			}
		}

		function requestServerMove() {
			return getServerMoveStatus(moveNumber)
			.then(
				function(data) {
					document.getElementById("sendMove").disabled = true;
					if (data["status"] == "READY") {
						return getServerMove(moveNumber);
					} else if (data["status"] == "WON") {

						if (data["player"] == player) {
							alert("Player wins!");
						} else if (data["player"] == opponent) {
							alert("Opponent wins!");
						}

						return getServerMove(moveNumber);
					}
				}
			);
		}

		function sendPlayerMove() {
			if (
				playerFlipCard != null &&
				playerPlayCard != null
			) {
				document.getElementById("sendMove").disabled = true;
				sendBoardMove(playerFlipCard, playerPlayCard, moveNumber);
				requestServerMove()
				.then(
					function(data) {
						moveNumber = data["nextMoveNumber"];
						playerFlipCard = data["playerFlipCard"];
						playerPlayCard = data["playerPlayCard"];
						opponentFlipCard = data["opponentFlipCard"];
						opponentPlayCard = data["opponentPlayCard"];
						showMoves();
					}
				);
			}
		}
		
		function drawHexPiece(row, column, color) {
			drawHexCell(row, column, null);
			drawSVGPiece(row, column, color);
		}
		
		function drawHexCell(row, column, scriptText) {
			drawSVGCell(row, column, scriptText);
		}

		function getPlayerRowColumn(playerFlipCardIn, playerPlayCardIn) {

			let playerMove = {
				"row" : null,
				"column" : null
			}

			if (playerFlipCardIn) {
				if ("br".indexOf(playerFlipCardIn.substr(0,1).toLowerCase()) == 1) {
					playerMove["row"] = indexValues.indexOf(playerFlipCardIn.substr(1))+1;
				} else if ("br".indexOf(playerFlipCardIn.substr(0,1).toLowerCase()) == 0) {
					playerMove["column"] = indexValues.indexOf(playerFlipCardIn.substr(1))+1;
				}
			}

			if (playerPlayCardIn) {
				if ("br".indexOf(playerPlayCardIn.substr(0,1).toLowerCase()) == 1) {
					playerMove["row"] = indexValues.indexOf(playerPlayCardIn.substr(1))+1;
				} else if ("br".indexOf(playerPlayCardIn.substr(0,1).toLowerCase()) == 0) {
					playerMove["column"] = indexValues.indexOf(playerPlayCardIn.substr(1))+1;
				}
			}

			return playerMove;
		}
		
		function displayMove() {

			let playerMove = getPlayerRowColumn(playerFlipCard, playerPlayCard);
			
			if (playerPlayCard == null) {
				
				document.getElementById("playerFlipCardImg").src = document.getElementById(playerFlipCard).src;
				document.getElementById("playerPlayCardImg").src = "<%= cardSet %>/blank.png";
				
				for (let cardIndex = 0; cardIndex < indexValues.length; cardIndex++) {
					document.getElementById(playerFlipCard.substr(0,1).toLowerCase()+indexValues[cardIndex]).src = "<%= cardSet %>/back.png";
				}
				
			} else {
				
				document.getElementById("playerPlayCardImg").src = document.getElementById(playerPlayCard).src;
				
				for (let cardIndex = 0; cardIndex < indexValues.length; cardIndex++) {
					document.getElementById(playerFlipCard.substr(0,1).toLowerCase()+indexValues[cardIndex]).src = "<%= cardSet %>/"+"<%= boardHelper.getPlayerSuits() %>".substr("b,r".indexOf(playerFlipCard.substr(0,1).toLowerCase()),1)+indexValues[cardIndex]+".png";
				}
			}
				
			if (playerFlipCard != null && playerPlayCard == null) {
					
				if (playerMove["row"] != null) {
					deleteLine = "r"+playerMove["row"];
					for (let lineIndex = 1; lineIndex <= indexValues.length; lineIndex++) {
						if (gameBoard[playerMove["row"]][lineIndex] == null) {
							drawHexPiece(playerMove["row"], lineIndex, "cyan");
						}
					}
				} else if (playerMove["column"] != null) {
					deleteLine = "c"+playerMove["column"];
					for (let lineIndex = 1; lineIndex <= indexValues.length; lineIndex++) {
						if (gameBoard[lineIndex][playerMove["column"]] == null) {
							drawHexPiece(lineIndex, playerMove["column"], "cyan");
						}
					}
				}
					
			} else if (playerFlipCard != null && playerPlayCard != null) {
				
				document.getElementById("playerFlipCardImg").src = document.getElementById(playerFlipCard).src;
				document.getElementById("playerPlayCardImg").src = document.getElementById(playerPlayCard).src;

				for (let lineIndex = 1; lineIndex <= indexValues.length; lineIndex++) {
					if (deleteLine.substr(0,1) == "r") {
						if (gameBoard[deleteLine.substr(1,deleteLine.length-1)][lineIndex] == null) {
							drawHexCell(deleteLine.substr(1,deleteLine.length-1), lineIndex, "makeCellMove("+row+","+column+",player);");
						}
					} else if (deleteLine.substr(0,1) == "c") {
						if (gameBoard[lineIndex][deleteLine.substr(1,deleteLine.length-1)] == null) {
							drawHexCell(lineIndex, deleteLine.substr(1,deleteLine.length-1), "makeCellMove("+row+","+column+",player);");
						}
					}
				}

				drawHexPiece(playerMove["row"], playerMove["column"], "cyan");
			}
		}
		
		function svgHexCell(row, column, scriptText) {
		
			let hexObj = document.createElementNS(svgNS, "polygon");
			let pointsStr = "";
			
			let xCoord = getXCoord(row, column);
			let yCoord = getYCoord(row, column);
			
			<%
				for (int pointIndex = 0; pointIndex < 6; pointIndex++) {
					
					if (pointIndex != 0) {
						out.println("\t\t\tpointsStr += \" \"");
					}
					
					out.print("\t\t\tpointsStr += (xCoord+("+String.format("%.15f", boardHelper.getPointXDelta(pointIndex))+"))+\",\"+");
					out.println("(yCoord+("+String.format("%.15f", (-1)*boardHelper.getPointYDelta(pointIndex))+"));");
				}
			%>
		
			hexObj.setAttribute("points", pointsStr);
			if (scriptText) {
				hexObj.setAttribute("onclick", scriptText);
			}
			
			hexObj.style.fill="white";
			hexObj.style.stroke="black";
		
			return hexObj;
		}
		
		function svgHexPiece(row, column, color) {
			
			let pieceObj = document.createElementNS(svgNS, "circle");
			
			pieceObj.setAttribute("cx", getXCoord(row, column));
			pieceObj.setAttribute("cy", getYCoord(row, column));
			pieceObj.setAttribute("r", <%= boardHelper.getPieceRadius() %>);
			pieceObj.setAttribute("stroke", "black");
			pieceObj.setAttribute("fill", color);
			
			return pieceObj;
		}
		
		function drawSVGPiece(row, column, color) {
			let svgObj = document.getElementById(svgID);
			svgObj.appendChild(svgHexPiece(row, column, color));
		}
		
		function drawSVGCell(row, column, scriptText) {
			let svgObj = document.getElementById(svgID);
			svgObj.appendChild(svgHexCell(row, column, scriptText));
		}

		function startGame() {
			loadGameBoard();
		}

		function getJSONResponse(url, method, data) {
			return new Promise(
				function(resolve, reject) {
    					let xhr = new XMLHttpRequest();
					xhr.open(method.toUpperCase(), url, true);
					if (method.toUpperCase() == "POST") {
						xhr.setRequestHeader("Content-type", "application/x-www-form-urlencoded");
					}
					xhr.responseType = 'json';
    					xhr.onload = function() {
      						let status = xhr.status;
      						if (status === 200) {
							resolve(xhr.response);
						} else {
							reject(status);
						}
					};
					if (!data) {
						xhr.send();
					} else {
						xhr.send(urlEncodeObj(data));
					}
				}
			);
		}

		function urlEncodeObj(objIn) {
			let str = [];
			for (let k in objIn) {
				str.push(encodeURIComponent(k) + "=" + encodeURIComponent(objIn[k]));
			}
			return str.join("&");
		}

		function loadGameBoard() {
			getJSONResponse("getboardmoves.jsp", "GET")
			.then(
				function(data) {
					gameBoard = data;
					drawBoardPieces();
				}
			).catch(
				function(err) {
					alert("Request encountered error: " + err);
				}
			);
		}

		function sendBoardMove(flipCardIn, playCardIn, moveNumberIn) {

			let boardMove = {
				"moveNumber": moveNumberIn,
				"flipCard": flipCardIn,
				"playCard": playCardIn
			};

			getJSONResponse("putboardmove.jsp", "POST", boardMove)
			.then(
				function(data) {
					if (data["status"] == "READY") {
						loadGameBoard();
					}
				}
			).catch(
				function(err) {
					alert("Request encountered error: " + err);
				}
			);
		}

		function getServerMoveStatus(moveNumberIn) {
			return getJSONResponse("getservermovestatus.jsp?moveNumber=" + moveNumberIn, "GET");
		}

		function getServerMove(moveNumberIn) {
			return getJSONResponse("getservermove.jsp?moveNumber=" + moveNumberIn, "GET");
		}
	</script>
  </head>

  <body onload="startGame();">
  	<table align="center" style="background-color: #004400;">
  		<tr>
  			<td>
				<svg
					id="svgBoard"
					xmlns="http://www.w3.org/2000/svg"
					xmlns:xlink="http://www.w3.org/1999/xlink"
					version="1.1"
					width="<%= boardHelper.getImageWidth() %>"
					height="<%= boardHelper.getImageHeight() %>"
					style="background: #004400;">
						<polygon
							points="
								<%= boardHelper.getStartXCoord()+boardHelper.getBorderObtuseXCoord() %>,<%= boardHelper.getStartYCoord()+boardHelper.getBorderObtuseYCoord() %>
								<%= boardHelper.getStartXCoord() %>,<%= boardHelper.getStartYCoord()+boardHelper.getBorderAcuteYCoord() %>
								<%= boardHelper.getStartXCoord() %>,<%= boardHelper.getStartYCoord()+boardHelper.getBorderAcuteYCoord()+boardHelper.getAcuteBorderLength() %>
								<%= boardHelper.getStartXCoord()+boardHelper.getBorderObtuseXCoord()+boardHelper.getObtuseBorderLength() %>,<%= boardHelper.getStartYCoord()+boardHelper.getBorderObtuseYCoord() %>"
							style="fill: red; stroke: black;" />
						<polygon
							points="
								<%= boardHelper.getStartXCoord()-boardHelper.getBorderObtuseXCoord() %>,<%= boardHelper.getStartYCoord()+boardHelper.getBorderObtuseYCoord() %>
								<%= boardHelper.getStartXCoord() %>,<%= boardHelper.getStartYCoord() %>
								<%= boardHelper.getStartXCoord() %>,<%= boardHelper.getStartYCoord()-boardHelper.getAcuteBorderLength() %>
								<%= boardHelper.getStartXCoord()-boardHelper.getBorderObtuseXCoord()-boardHelper.getObtuseBorderLength() %>,<%= boardHelper.getStartYCoord()+boardHelper.getBorderObtuseYCoord() %>"
							style="fill: red; stroke: black;" />
						<polygon
							points="
								<%= boardHelper.getStartXCoord()+boardHelper.getBorderObtuseXCoord() %>,<%= boardHelper.getStartYCoord()+boardHelper.getBorderObtuseYCoord() %>
								<%= boardHelper.getStartXCoord() %>,<%= boardHelper.getStartYCoord() %>
								<%= boardHelper.getStartXCoord() %>,<%= boardHelper.getStartYCoord()-boardHelper.getAcuteBorderLength() %>
								<%= boardHelper.getStartXCoord()+boardHelper.getBorderObtuseXCoord()+boardHelper.getObtuseBorderLength() %>,<%= boardHelper.getStartYCoord()+boardHelper.getBorderObtuseYCoord() %>"
							style="fill: black; stroke: black;" />
						<polygon
							points="
								<%= boardHelper.getStartXCoord()-boardHelper.getBorderObtuseXCoord() %>,<%= boardHelper.getStartYCoord()+boardHelper.getBorderObtuseYCoord() %>
								<%= boardHelper.getStartXCoord() %>,<%= boardHelper.getStartYCoord()+boardHelper.getBorderAcuteYCoord() %>
								<%= boardHelper.getStartXCoord() %>,<%= boardHelper.getStartYCoord()+boardHelper.getBorderAcuteYCoord()+boardHelper.getAcuteBorderLength() %>
								<%= boardHelper.getStartXCoord()-boardHelper.getBorderObtuseXCoord()-boardHelper.getObtuseBorderLength() %>,<%= boardHelper.getStartYCoord()+boardHelper.getBorderObtuseYCoord() %>"
							style="fill: black; stroke: black;" />
<%
	for (int i=1; i <= boardHelper.getBoardRows(); i++) {
%>
						<text
							x="<%= boardHelper.getCellXCoord(i, 0) %>"
							y="<%= boardHelper.getCellYCoord(i, 0) %>"
							text-anchor="middle"
							dominant-baseline="middle"
							fill="black"
							style="font-size:9; font-family: verdana; font-weight: bold;">R<%= boardHelper.getBoardLabel(i) %></text>
						<text
							x="<%= boardHelper.getCellXCoord(i, 14) %>"
							y="<%= boardHelper.getCellYCoord(i, 14) %>"
							text-anchor="middle"
							dominant-baseline="middle"
							fill="black"
							style="font-size:9; font-family: verdana; font-weight: bold;">R<%= boardHelper.getBoardLabel(i) %></text>
<%
	}
%>

<%
	for (int i=1; i <= boardHelper.getBoardColumns(); i++) {
%>
						<text
							x="<%= boardHelper.getCellXCoord(0, i) %>"
							y="<%= boardHelper.getCellYCoord(0, i) %>"
							text-anchor="middle"
							dominant-baseline="middle"
							fill="white"
							style="font-size:9; font-family: verdana; font-weight: bold;">B<%= boardHelper.getBoardLabel(i) %></text>
						<text
							x="<%= boardHelper.getCellXCoord(14, i) %>"
							y="<%= boardHelper.getCellYCoord(14, i) %>"
							text-anchor="middle"
							dominant-baseline="middle"
							fill="white"
							style="font-size:9; font-family: verdana; font-weight: bold;">B<%= boardHelper.getBoardLabel(i) %></text>
<%
	}
%>

<%
	for (int i=1; i <= boardHelper.getBoardRows(); i++) {
		for (int j=1; j <= boardHelper.getBoardColumns(); j++) {
%>
						<polygon
							points="
<%
			for (int k=0; k < 6; k++) {
%>
								<%= boardHelper.getPointXCoord(boardHelper.getCellXCoord(i, j), k) %>,<%= boardHelper.getPointYCoord(boardHelper.getCellYCoord(i, j), k) %>
<%
			}
%>
							"
							onclick="makeCellMove(<%= i %>, <%= j %>, player);"
							style="stroke: black; fill: white;"
							/>
<%
		}
	}
%>
				</svg>
  			</td>
  			<td style="text-align: center;">
  			<table>
  				
  				<tr>
  					<td style="text-align: right;">
		  				<% if (boardHelper.getPlayer() == BoardHelper.FIRST_PLAYER) { %>Opposite<% } else { %>Same<% } %>
		  			</td>
		  			<td colspan="2">
						<img id="opponentFlipCardImg" src="<%= cardSet %>/blank.png" width="54" height="72">
						<img id="opponentPlayCardImg" src="<%= cardSet %>/blank.png" width="54" height="72"><br />
  			  		</td>
  			  		<td></td>
  			  	<tr>
  			  		<td style="text-align: right;">	
		  				<% if (boardHelper.getPlayer() == BoardHelper.FIRST_PLAYER) { %>Same<% } else { %>Opposite<% } %>
		  			</td>
		  			<td colspan="2">
						<img id="playerFlipCardImg" src="<%= cardSet %>/blank.png" width="54" height="72">
						<img id="playerPlayCardImg" src="<%= cardSet %>/blank.png" width="54" height="72"><br />
  					</td>
  					<td>
						<a style="color: white;" href="http://www.thegamecrafter.com/games/battle-hex">Play the board game</a><br />
  						<input id="sendMove" type="button" value="Send Move" disabled="disabled" onclick="sendPlayerMove();" /><br />
  						<input id="randomizeFlipInput" type="checkbox" /> Randomize Flip
  					</td>
  					<tr>
		  				<td colspan="2" style="text-align: center;">Black</td>
  						<td colspan="2" style="text-align: center;">Red</td>
  					</tr>
	  				
<%
	for (int i=0; i < ranks.length; i++) {
%>
						<tr>
<%
		for (int j=0; j < suits.length; j++) {
%>
			  				<td colspan="2" style="text-align: center;">
<%

			String suit = suits[j];
			String line = "";

			if (suit.equals("c") || suit.equals("s")) {
				line = "b";
			} else if (suit.equals("d") || suit.equals("h")) {
				line = "r";
			}

			for (int k=0; k < ranks[i].length; k++) {
				String rank = ranks[i][k];
%>
		  				
		  						<img id="<%= line %><%= rank %>" src="<%= cardSet %>/<%= suit %><%= rank %>.png" width="54" height="72" onclick="makeMove('<%= line %><%= rank %>', player);">
<%
			}
%>
							</td>
<%
		}
%>
						</tr>
<%
	}
%>
  				
  			</table>
  			</td>
  		</tr>
  	</table>
    </body>
</html>
