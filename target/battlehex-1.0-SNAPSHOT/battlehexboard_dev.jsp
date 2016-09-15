<%@ page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1" %>
<%@ page import="java.lang.Math" %>
<%@ page import="battlehex.BoardHelper" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<!-- The HTML 4.01 Transitional DOCTYPE declaration-->
<!-- above set at the top of the file will set     -->
<!-- the browser's rendering engine into           -->
<!-- "Quirks Mode". Replacing this declaration     -->
<!-- with a "Standards Mode" doctype is supported, -->
<!-- but may lead to some differences in layout.   -->

<%
	BoardHelper boardHelper = new BoardHelper();

	int boardRows = 13;
	int boardColumns = 13;
	int cellWidth = 10;
	
	double imageWidth = Math.round(((double)2*cellWidth*((((double)boardColumns+1)*Math.sin(Math.PI/3))+(((double)boardRows-1)*Math.sin(2*Math.PI/3))+(2/Math.sin(Math.PI/3))+(2*Math.cos(Math.PI/6))))/Math.sqrt(3));
	double imageHeight;
	
	boardHelper.setImageWidth((int)imageWidth);
	boardHelper.setImageHeight(700);
	boardHelper.setBoardRows(13);
	boardHelper.setBoardColumns(13);
	boardHelper.setPlayer(BoardHelper.FIRST_PLAYER);
	boardHelper.setBoardShape(BoardHelper.VERTICAL_BOARD);
	
	imageHeight = Math.round(boardHelper.getCellRadius()*(2+((((double)boardRows-1)+((double)boardColumns-1)+3)*(1+Math.sin(Math.PI/6)))+(2/Math.sin(Math.PI/6))));
	boardHelper.setImageHeight((int)imageHeight);
	
	out.println(imageWidth);
	out.println(imageHeight);
%>

<c:set var="player"><%= boardHelper.getPlayer() %></c:set>
<c:set var="cardSet" value="cardstux" />
  				
<c:if test="${player == 1}">
	<c:set var="suits" value="s,d" />
</c:if>
<c:if test="${player == 2}">
	<c:set var="suits" value="c,h" />
</c:if>

<html>
  <head>
    <meta http-equiv="content-type" content="text/html; charset=ISO-8859-1">
    <title>Hello SVG!</title>
    <script type="text/javascript" src="wz_jsgraphics.js"></script>
	<script type="text/javascript">
	
		var svgNS = "http://www.w3.org/2000/svg";
		var svgID = "svgBoard";
		
		var divID = "divBoard";
		
		var indexValues = ["a", "2", "3", "4", "5", "6", "7", "8", "9", "10", "j", "q", "k"];
		
		var playerFlipCard = null;
		var playerPlayCard = null;
		
		var playerBoardRow = null;
		var playerBoardColumn = null;
		var deleteLine = null;
		
		var supportsSVG = function () {
			return !!document.createElementNS && !!document.createElementNS(svgNS, "svg").createSVGRect;
		}
		
		var getXCoord = function(row, column) {
			return <%= boardHelper.getStartXCoord() %>+((column-row)*<%= boardHelper.getCellHalfWidth() %>);
		}
		
		var getYCoord = function(row, column) {
			return <%= boardHelper.getStartYCoord() %>+((column-row)*<%= boardHelper.getCellExtendedLength() %>)+((row-1)*<%= 2*boardHelper.getCellExtendedLength() %>);
		}
		
		var makeMove = function(card) {
			
			if (
					document.getElementById(card).src != "${cardSet}/back.png" &&
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
					
					playerPlayCard = card;
					document.getElementById("randomizeFlipInput").disabled = false;
					document.getElementById("randomizeFlipInput").checked = false;
					
				} else {
					
					playerFlipCard = card;
					playerPlayCard = null;
					
					drawHexCell(playerBoardRow, playerBoardColumn);
					document.getElementById("randomizeFlipInput").disabled = true;
					document.getElementById("randomizeFlipInput").checked = false;
					
					playerBoardRow = null;
					playerBoardColumn = null;
				}
				
				displayMove();
			}
		}
		
		var makeCellMove = function(row, column) {
			
			if (playerPlayCard == null) {
				if (deleteLine.substr(0,1) == "r") {
					makeMove("b"+indexValues[column-1]);
				} else if (deleteLine.substr(0,1) == "c") {
					makeMove("r"+indexValues[row-1]);
				}
			}
			
			makeMove("r"+indexValues[row-1]);
			makeMove("b"+indexValues[column-1]);
			
			randomizeMove();
		}
		
		var randomizeMove = function() {
			
			if (playerFlipCard != null && playerPlayCard != null) {
				
				if (Math.random() < 0.5) {
					var tempCard = playerFlipCard;
					playerFlipCard = playerPlayCard;
					playerPlayCard = tempCard;
				}
				
				document.getElementById("randomizeFlipInput").disabled = true;
				document.getElementById("randomizeFlipInput").checked = true;
				displayMove();
			}
		}
		
		var drawHexPiece = function(row, column, color) {
			drawSVGPiece(row, column, color);
			drawJSGPiece(row, column, color);
		}
		
		var drawHexCell = function(row, column) {
			drawSVGCell(row, column);
			drawJSGCell(row, column);
		}
		
		var displayMove = function() {
			
			if (playerPlayCard == null) {
				
				document.getElementById("playerFlipCardImg").src = document.getElementById(playerFlipCard).src;
				document.getElementById("playerPlayCardImg").src = "${cardSet}/blank.png";
				
				for (cardIndex = 0; cardIndex < indexValues.length; cardIndex++) {
					
					document.getElementById(playerFlipCard.substr(0,1).toLowerCase()+indexValues[cardIndex]).src = "${cardSet}/back.png";
					
					if ((playerFlipCard.substr(0,1).toLowerCase()+indexValues[cardIndex]) == playerFlipCard.toLowerCase()) {
						if (("b,r".indexOf(playerFlipCard.substr(0,1).toLowerCase())) == 2) {
							playerBoardRow = cardIndex+1;
						} else {
							playerBoardColumn = cardIndex+1;
						}
					}
				}
				
			} else {
				
				document.getElementById("playerPlayCardImg").src = document.getElementById(playerPlayCard).src;
				
				for (cardIndex = 0; cardIndex < indexValues.length; cardIndex++) {
					
					document.getElementById(playerFlipCard.substr(0,1).toLowerCase()+indexValues[cardIndex]).src = "${cardSet}/"+"${suits}".substr("b,r".indexOf(playerFlipCard.substr(0,1).toLowerCase()),1)+indexValues[cardIndex]+".png";
					
					if ((playerPlayCard.substr(0,1).toLowerCase()+indexValues[cardIndex]) == playerPlayCard.toLowerCase()) {
						if (("b,r".indexOf(playerPlayCard.substr(0,1).toLowerCase())) == 2) {
							playerBoardRow = cardIndex+1;
						} else {
							playerBoardColumn = cardIndex+1;
						}
					}
				}
				
				drawHexPiece(playerBoardRow, playerBoardColumn, "cyan");
			}
				
			if (playerFlipCard != null && playerPlayCard == null) {
					
				if (playerBoardRow != null) {
					deleteLine = "r"+playerBoardRow;
					for (lineIndex = 1; lineIndex <= indexValues.length; lineIndex++) {
						drawHexPiece(playerBoardRow, lineIndex, "cyan");
					}
				} else if (playerBoardColumn != null) {
					deleteLine = "c"+playerBoardColumn;
					for (lineIndex = 1; lineIndex <= indexValues.length; lineIndex++) {
						drawHexPiece(lineIndex, playerBoardColumn, "cyan");
					}
				}
					
			} else if (playerFlipCard != null && playerPlayCard != null) {
				
				document.getElementById("playerFlipCardImg").src = document.getElementById(playerFlipCard).src;
				document.getElementById("playerPlayCardImg").src = document.getElementById(playerPlayCard).src;

				for (lineIndex = 1; lineIndex <= indexValues.length; lineIndex++) {
					if (deleteLine.substr(0,1) == "r") {
						drawHexCell(deleteLine.substr(1,deleteLine.length-1), lineIndex);
					} else if (deleteLine.substr(0,1) == "c") {
						drawHexCell(lineIndex,deleteLine.substr(1,deleteLine.length-1));
					}
				}
				
				drawHexPiece(playerBoardRow, playerBoardColumn, "cyan");
			}
		}
		
		var svgTextLabel = function(x, y, text, fill) {
			
			var textObj = document.createElementNS(svgNS, "text");
			var textNode = document.createTextNode(text);
			
			textObj.setAttribute("x", x);
			textObj.setAttribute("y", y);
			textObj.setAttribute("text-anchor", "middle");
			textObj.setAttribute("dominant-baseline", "middle");
			
			textObj.setAttribute("style", "font-size:9; font-family: verdana; font-weight: bold;");
			textObj.style.fill = fill;
			
			textObj.appendChild(textNode);
			
			return textObj;
		}
		
		var svgHexCell = function(row, column) {
		
			var hexObj = document.createElementNS(svgNS, "polygon");
			var pointsStr = "";
			
			var xCoord = getXCoord(row, column);
			var yCoord = getYCoord(row, column);
			
			<%
				for (int pointIndex = 0; pointIndex < 6; pointIndex++) {
					
					if (pointIndex != 0) {
						out.println("pointsStr += \" \"");
					}
					
					out.print("pointsStr += (xCoord+("+String.format("%.15f", boardHelper.getPointXDelta(pointIndex))+"))+\",\"+");
					out.println("(yCoord+("+String.format("%.15f", (-1)*boardHelper.getPointYDelta(pointIndex))+"));");
				}
			%>
		
			hexObj.setAttribute("points", pointsStr);
			hexObj.setAttribute("onclick", "makeCellMove("+row+","+column+");");
			
			hexObj.style.fill="white";
			hexObj.style.stroke="black";
		
			return hexObj;
		}
		
		var svgHexPiece = function(row, column, color) {
			
			var pieceObj = document.createElementNS(svgNS, "circle");
			
			pieceObj.setAttribute("cx", getXCoord(row, column));
			pieceObj.setAttribute("cy", getYCoord(row, column));
			pieceObj.setAttribute("r", <%= boardHelper.getPieceRadius() %>);
			pieceObj.setAttribute("stroke", "black");
			pieceObj.setAttribute("fill", color);
			
			return pieceObj;
		}
		
		var drawSVGBoard = function() {
			
			var svgObj = document.getElementById(svgID);
			
			for (columnIndex = 1; columnIndex <= <%= boardHelper.getBoardColumns() %>; columnIndex++) {
				for (rowIndex = 1; rowIndex <= <%= boardHelper.getBoardRows() %>; rowIndex++) {
					drawSVGCell(rowIndex, columnIndex);
				}
			}

			for (rowIndex = 1; rowIndex <= <%= boardHelper.getBoardRows() %>; rowIndex++) {
				svgObj.appendChild(svgTextLabel(getXCoord(rowIndex, <%= boardHelper.getBoardColumns()+1 %>), getYCoord(rowIndex, <%= boardHelper.getBoardColumns()+1 %>), "R"+indexValues[rowIndex-1].toUpperCase(), "black"));
				svgObj.appendChild(svgTextLabel(getXCoord(rowIndex, 0), getYCoord(rowIndex, 0), "R"+indexValues[rowIndex-1].toUpperCase(), "black"));
			}
			
			for (columnIndex = 1; columnIndex <= <%= boardHelper.getBoardColumns() %>; columnIndex++) {
				svgObj.appendChild(svgTextLabel(getXCoord(<%= boardHelper.getBoardRows()+1 %>, columnIndex), getYCoord(<%= boardHelper.getBoardRows()+1 %>, columnIndex), "B"+indexValues[columnIndex-1].toUpperCase(), "white"));
				svgObj.appendChild(svgTextLabel(getXCoord(0, columnIndex), getYCoord(0, columnIndex), "B"+indexValues[columnIndex-1].toUpperCase(), "white"));
			}
		}
		
		var drawSVGPiece = function(row, column, color) {
			var svgObj = document.getElementById(svgID);
			svgObj.appendChild(svgHexPiece(row, column, color));
		}
		
		var drawSVGCell = function(row, column) {
			var svgObj = document.getElementById(svgID);
			svgObj.appendChild(svgHexCell(row, column));
		}
		
		var jsgHexCellXCoords = function(row, column) {
		
			var xCoordArray = new Array();
			var xCoord = getXCoord(row, column);
			
			<%
				for (int pointIndex = 0; pointIndex < 6; pointIndex++) {
					out.println("xCoordArray["+pointIndex+"] = xCoord+("+String.format("%.15f", boardHelper.getPointXDelta(pointIndex))+");");
				}
			%>
		
			return xCoordArray;
		}
		
		var jsgHexCellYCoords = function(row, column) {
		
			var yCoordArray = new Array();
			var yCoord = getYCoord(row, column);
			
			<%
				for (int pointIndex = 0; pointIndex < 6; pointIndex++) {
					out.println("yCoordArray["+pointIndex+"] = yCoord+("+String.format("%.15f", (-1)*boardHelper.getPointYDelta(pointIndex))+");");
				}
			%>
		
			return yCoordArray;
		}
		
		var drawJSGBoard = function() {
			
			var canvas = document.getElementById(divID);
			var jsg = new jsGraphics(canvas);
			
			var lrBorderXCoords = new Array();
			var lrBorderYCoords = new Array();
			
			var ulBorderXCoords = new Array();
			var ulBorderYCoords = new Array();
			
			var urBorderXCoords = new Array();
			var urBorderYCoords = new Array();
			
			var llBorderXCoords = new Array();
			var llBorderYCoords = new Array();
			
			lrBorderXCoords[0] = <%= boardHelper.getCellXCoord(1, boardHelper.getBoardColumns()) %>;
			lrBorderXCoords[1] = <%= boardHelper.getCellXCoord(boardHelper.getBoardRows(), boardHelper.getBoardColumns()) %>;
			lrBorderXCoords[2] = <%= boardHelper.getCellXCoord(boardHelper.getBoardRows(), boardHelper.getBoardColumns()) %>;
			lrBorderXCoords[3] = <%= boardHelper.getCellXCoord(1, boardHelper.getBoardColumns())+boardHelper.getObtuseBorderLength() %>;
			
			lrBorderYCoords[0] = <%= boardHelper.getCellYCoord(1, boardHelper.getBoardColumns()) %>;
			lrBorderYCoords[1] = <%= boardHelper.getCellYCoord(boardHelper.getBoardRows(), boardHelper.getBoardColumns()) %>;
			lrBorderYCoords[2] = <%= boardHelper.getCellYCoord(boardHelper.getBoardRows(), boardHelper.getBoardColumns())+boardHelper.getAcuteBorderLength() %>;
			lrBorderYCoords[3] = <%= boardHelper.getCellYCoord(1, boardHelper.getBoardColumns()) %>;
			
			jsg.setColor("red");
			jsg.fillPolygon(lrBorderXCoords, lrBorderYCoords);
			jsg.setColor("black")
			jsg.drawPolygon(lrBorderXCoords, lrBorderYCoords);
			
			ulBorderXCoords[0] = <%= boardHelper.getCellXCoord(boardHelper.getBoardRows(), 1) %>;
			ulBorderXCoords[1] = <%= boardHelper.getCellXCoord(1, 1) %>;
			ulBorderXCoords[2] = <%= boardHelper.getCellXCoord(1, 1) %>;
			ulBorderXCoords[3] = <%= boardHelper.getCellXCoord(boardHelper.getBoardRows(), 1)-boardHelper.getObtuseBorderLength() %>;
			
			ulBorderYCoords[0] = <%= boardHelper.getCellYCoord(boardHelper.getBoardRows(), 1) %>;
			ulBorderYCoords[1] = <%= boardHelper.getCellYCoord(1, 1) %>;
			ulBorderYCoords[2] = <%= boardHelper.getCellYCoord(1, 1)-boardHelper.getAcuteBorderLength() %>;
			ulBorderYCoords[3] = <%= boardHelper.getCellYCoord(boardHelper.getBoardRows(), 1) %>;
			
			jsg.setColor("red");
			jsg.fillPolygon(ulBorderXCoords, ulBorderYCoords);
			jsg.setColor("black")
			jsg.drawPolygon(ulBorderXCoords, ulBorderYCoords);
			
			urBorderXCoords[0] = <%= boardHelper.getCellXCoord(1, boardHelper.getBoardColumns()) %>;
			urBorderXCoords[1] = <%= boardHelper.getCellXCoord(1, 1) %>;
			urBorderXCoords[2] = <%= boardHelper.getCellXCoord(1, 1) %>;
			urBorderXCoords[3] = <%= boardHelper.getCellXCoord(1, boardHelper.getBoardColumns())+boardHelper.getObtuseBorderLength() %>;
			
			urBorderYCoords[0] = <%= boardHelper.getCellYCoord(1, boardHelper.getBoardColumns()) %>;
			urBorderYCoords[1] = <%= boardHelper.getCellYCoord(1, 1) %>;
			urBorderYCoords[2] = <%= boardHelper.getCellYCoord(1, 1)-boardHelper.getAcuteBorderLength() %>;
			urBorderYCoords[3] = <%= boardHelper.getCellYCoord(1, boardHelper.getBoardColumns()) %>;
			
			jsg.setColor("black");
			jsg.fillPolygon(urBorderXCoords, urBorderYCoords);
			jsg.setColor("black")
			jsg.drawPolygon(urBorderXCoords, urBorderYCoords);
			
			llBorderXCoords[0] = <%= boardHelper.getCellXCoord(boardHelper.getBoardRows(), 1) %>;
			llBorderXCoords[1] = <%= boardHelper.getCellXCoord(boardHelper.getBoardRows(), boardHelper.getBoardColumns()) %>;
			llBorderXCoords[2] = <%= boardHelper.getCellXCoord(boardHelper.getBoardRows(), boardHelper.getBoardColumns()) %>;
			llBorderXCoords[3] = <%= boardHelper.getCellXCoord(boardHelper.getBoardRows(), 1)-boardHelper.getObtuseBorderLength() %>;
			
			llBorderYCoords[0] = <%= boardHelper.getCellYCoord(boardHelper.getBoardRows(), 1) %>;
			llBorderYCoords[1] = <%= boardHelper.getCellYCoord(boardHelper.getBoardRows(), boardHelper.getBoardColumns()) %>;
			llBorderYCoords[2] = <%= boardHelper.getCellYCoord(boardHelper.getBoardRows(), boardHelper.getBoardColumns())+boardHelper.getAcuteBorderLength() %>;
			llBorderYCoords[3] = <%= boardHelper.getCellYCoord(boardHelper.getBoardRows(), 1) %>;

			jsg.setColor("black");
			jsg.fillPolygon(llBorderXCoords, llBorderYCoords);
			jsg.setColor("black")
			jsg.drawPolygon(llBorderXCoords, llBorderYCoords);
			
			jsg.setColor("white");
			for (columnIndex = 1; columnIndex <= <%= boardHelper.getBoardColumns() %>; columnIndex++) {
				for (rowIndex = 1; rowIndex <= <%= boardHelper.getBoardRows() %>; rowIndex++) {
					jsg.fillPolygon(jsgHexCellXCoords(rowIndex, columnIndex), jsgHexCellYCoords(rowIndex, columnIndex));
				}
			}
			
			jsg.setColor("black");
			for (columnIndex = 1; columnIndex <= <%= boardHelper.getBoardColumns() %>; columnIndex++) {
				for (rowIndex = 1; rowIndex <= <%= boardHelper.getBoardRows() %>; rowIndex++) {
					jsg.drawPolygon(jsgHexCellXCoords(rowIndex, columnIndex), jsgHexCellYCoords(rowIndex, columnIndex));
				}
			}

			jsg.setColor("black");
			jsg.setFont("verdana", "9px", Font.BOLD);
			for (rowIndex = 1; rowIndex <= <%= boardHelper.getBoardRows() %>; rowIndex++) {
				jsg.drawString("R"+indexValues[rowIndex-1].toUpperCase(), getXCoord(rowIndex, <%= boardHelper.getBoardColumns()+1 %>)-7, getYCoord(rowIndex, <%= boardHelper.getBoardColumns()+1 %>)-5);
				jsg.drawString("R"+indexValues[rowIndex-1].toUpperCase(), getXCoord(rowIndex, 0)-10, getYCoord(rowIndex, 0)-5);
			}

			jsg.setColor("white");
			jsg.setFont("verdana", "9px", Font.BOLD);
			for (columnIndex = 1; columnIndex <= <%= boardHelper.getBoardColumns() %>; columnIndex++) {
				jsg.drawString("B"+indexValues[columnIndex-1].toUpperCase(), getXCoord(<%= boardHelper.getBoardRows()+1 %>, columnIndex)-10, getYCoord(<%= boardHelper.getBoardRows()+1 %>, columnIndex)-5);
				jsg.drawString("B"+indexValues[columnIndex-1].toUpperCase(), getXCoord(0, columnIndex)-7, getYCoord(0, columnIndex)-5);
			}
			
			jsg.paint();
		}
		
		var drawJSGCell = function(row, column) {
			
			var canvas = document.getElementById(divID);
			var jsg = new jsGraphics(canvas);
			
			jsg.setColor("white");
			jsg.fillPolygon(jsgHexCellXCoords(row, column), jsgHexCellYCoords(row, column));
			
			jsg.setColor("black");
			jsg.drawPolygon(jsgHexCellXCoords(row, column), jsgHexCellYCoords(row, column));
			
			jsg.paint();
		}
		
		var drawJSGPiece = function(row, column, color) {
			
			var canvas = document.getElementById(divID);
			var jsg = new jsGraphics(canvas);
			
			var xCoord = getXCoord(row, column)-(<%= boardHelper.getPieceRadius() %>);
			var yCoord = getYCoord(row, column)-(<%= boardHelper.getPieceRadius() %>);
			var diameter = <%= boardHelper.getPieceRadius()*2 %>;

			jsg.setColor(color);
			jsg.fillEllipse(xCoord, yCoord, diameter, diameter);
			jsg.setColor("black");
			jsg.drawEllipse(xCoord, yCoord, diameter, diameter);
			
			jsg.paint();
		}
	</script>
  </head>

  <body onload="drawSVGBoard(); drawJSGBoard();">
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
								<%= boardHelper.getCellXCoord(1, boardHelper.getBoardColumns()) %>,<%= boardHelper.getCellYCoord(1, boardHelper.getBoardColumns()) %>
								<%= boardHelper.getCellXCoord(boardHelper.getBoardRows(), boardHelper.getBoardColumns()) %>,<%= boardHelper.getCellYCoord(boardHelper.getBoardRows(), boardHelper.getBoardColumns()) %>
								<%= boardHelper.getCellXCoord(boardHelper.getBoardRows(), boardHelper.getBoardColumns()) %>,<%= boardHelper.getCellYCoord(boardHelper.getBoardRows(), boardHelper.getBoardColumns())+boardHelper.getAcuteBorderLength() %>
								<%= boardHelper.getCellXCoord(1, boardHelper.getBoardColumns())+boardHelper.getObtuseBorderLength() %>,<%= boardHelper.getCellYCoord(1, boardHelper.getBoardColumns()) %>"
							style="fill: red; stroke: black;" />
						<polygon
							points="
								<%= boardHelper.getCellXCoord(boardHelper.getBoardRows(), 1) %>,<%= boardHelper.getCellYCoord(boardHelper.getBoardRows(), 1) %>
								<%= boardHelper.getCellXCoord(1, 1) %>,<%= boardHelper.getCellYCoord(1, 1) %>
								<%= boardHelper.getCellXCoord(1, 1) %>,<%= boardHelper.getCellYCoord(1, 1)-boardHelper.getAcuteBorderLength() %>
								<%= boardHelper.getCellXCoord(boardHelper.getBoardRows(), 1)-boardHelper.getObtuseBorderLength() %>,<%= boardHelper.getCellYCoord(boardHelper.getBoardRows(), 1) %>"
							style="fill: red; stroke: black;" />
						<polygon
							points="
								<%= boardHelper.getCellXCoord(1, boardHelper.getBoardColumns()) %>,<%= boardHelper.getCellYCoord(1, boardHelper.getBoardColumns()) %>
								<%= boardHelper.getCellXCoord(1, 1) %>,<%= boardHelper.getCellYCoord(1, 1) %>
								<%= boardHelper.getCellXCoord(1, 1) %>,<%= boardHelper.getCellYCoord(1, 1)-boardHelper.getAcuteBorderLength() %>
								<%= boardHelper.getCellXCoord(1, boardHelper.getBoardColumns())+boardHelper.getObtuseBorderLength() %>,<%= boardHelper.getCellYCoord(1, boardHelper.getBoardColumns()) %>"
							style="fill: black; stroke: black;" />
						<polygon
							points="
								<%= boardHelper.getCellXCoord(boardHelper.getBoardRows(), 1) %>,<%= boardHelper.getCellYCoord(boardHelper.getBoardRows(), 1) %>
								<%= boardHelper.getCellXCoord(boardHelper.getBoardRows(), boardHelper.getBoardColumns()) %>,<%= boardHelper.getCellYCoord(boardHelper.getBoardRows(), boardHelper.getBoardColumns()) %>
								<%= boardHelper.getCellXCoord(boardHelper.getBoardRows(), boardHelper.getBoardColumns()) %>,<%= boardHelper.getCellYCoord(boardHelper.getBoardRows(), boardHelper.getBoardColumns())+boardHelper.getAcuteBorderLength() %>
								<%= boardHelper.getCellXCoord(boardHelper.getBoardRows(), 1)-boardHelper.getObtuseBorderLength() %>,<%= boardHelper.getCellYCoord(boardHelper.getBoardRows(), 1) %>"
							style="fill: black; stroke: black;" />
				</svg>
  			</td>
  			<td style="text-align: center;">
  			<table>
  				
  				<tr>
  					<td style="text-align: right;">
		  				<c:if test="${player == 1}">Opposite</c:if>
		  				<c:if test="${player == 2}">Same</c:if>
		  			</td>
		  			<td colspan="2">
						<img id="opponentFlipCardImg" src="${cardSet}/blank.png" width="54" height="72">
						<img id="opponentPlayCardImg" src="${cardSet}/blank.png" width="54" height="72"><br />
  			  		</td>
  			  		<td></td>
  			  	<tr>
  			  		<td style="text-align: right;">	
		  				<c:if test="${player == 1}">Same</c:if>
		  				<c:if test="${player == 2}">Opposite</c:if>
		  			</td>
		  			<td colspan="2">
						<img id="playerFlipCardImg" src="${cardSet}/blank.png" width="54" height="72">
						<img id="playerPlayCardImg" src="${cardSet}/blank.png" width="54" height="72"><br />
  					</td>
  					<td>
  						<input type="button" value="Send Move"><br />
  						<input id="randomizeFlipInput" type="checkbox" onclick="randomizeMove();" disabled="disabled"> Randomize Flip
  					</td>
  					<tr>
		  				<td colspan="2" style="text-align: center;">Black</td>
  						<td colspan="2" style="text-align: center;">Red</td>
  					</tr>
  					
	  				<c:forTokens items="a,2,3,4;5,6,7,8;9,10,j,q;k" delims=";" var="ranks">
	  				
	  					<tr>
			  			<c:forTokens items="${suits}" delims="," var="suit">
			  			
			  				<td colspan="2" style="text-align: center;">
	  						<c:forTokens items="${ranks}" delims="," var="rank">
			  				
				  				<c:if test="${suit == 's' || suit == 'c'}">
				  					<c:set var="line" value="b" />
				  				</c:if>
				  				<c:if test="${suit == 'd' || suit == 'h'}">
				  					<c:set var="line" value="r" />
				  				</c:if>
		  				
		  						<img id="${line}${rank}" src="${cardSet}/${suit}${rank}.png" width="54" height="72" onclick="makeMove('${fn:toLowerCase(line)}${fn:toLowerCase(rank)}');">
		  						
		  					</c:forTokens>
		  					 </td>
	  					</c:forTokens>
	  					<tr>
	  				</c:forTokens>
  				
  			</table>
  			</td>
  		</tr>
  		<tr>
  			<td>
  				<div
  					id="divBoard"
  					style="
  						position: relative;
  						width: <%= boardHelper.getImageWidth() %>;
  						height: <%= boardHelper.getImageHeight() %>;
  						background: #004400;">
  				</div>
  			</td>
  			<td>
  				<!-- <input type="button" value="Draw Piece" onclick="drawSVGPiece(4, 9, 'red'); drawJSGPiece(8, 5, 'black');"> -->
  			</td>
  		<tr>
  	</table>
  </body>
</html>
