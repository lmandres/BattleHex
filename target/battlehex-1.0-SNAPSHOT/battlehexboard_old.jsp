<%@ page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<!-- The HTML 4.01 Transitional DOCTYPE declaration-->
<!-- above set at the top of the file will set     -->
<!-- the browser's rendering engine into           -->
<!-- "Quirks Mode". Replacing this declaration     -->
<!-- with a "Standards Mode" doctype is supported, -->
<!-- but may lead to some differences in layout.   -->

<c:set var="responseString" value="Yes"/>

<html>
  <head>
    <meta http-equiv="content-type" content="text/html; charset=ISO-8859-1">
    <title>Hello SVG!</title>
	<script type="text/javascript">
	
		var svgNS = "http://www.w3.org/2000/svg";
	
		var boardRows = 13;
		var boardColumns = 13;
		
		var indexValues = ["A", "2", "3", "4", "5", "6", "7", "8", "9", "10", "J", "Q", "K"];
		
		var startXCoord, startYCoord, cellRadius;
		
		var supportsSVG = function () {
			return !!document.createElementNS && !!document.createElementNS(svgNS, "svg").createSVGRect;
		}
		
		var setCellRadius = function(svgObj) {
			
			cellRadius = svgObj.width.baseVal.value/(((boardColumns+1)*Math.sin(Math.PI/3))+((boardRows-1)*Math.sin(2*Math.PI/3))+(2/Math.sin(Math.PI/3))+(2*Math.cos(Math.PI/6)));
			var cellYRadius = svgObj.height.baseVal.value/(2+(((boardRows-1)+(boardColumns-1)+3)*(1+Math.sin(Math.PI/6)))+(2/Math.sin(Math.PI/6)));
			
			if (cellRadius > cellYRadius) {
				cellRadius = cellYRadius;
			}
		}
		
		var setStartCoords = function(svgObj) {
			
			startXCoord = svgObj.width.baseVal.value/2;
			startYCoord = svgObj.height.baseVal.value/2;
			
			startXCoord -= (boardColumns-boardRows)*cellRadius*Math.cos(Math.PI/6)/2;
			
			startYCoord -= (boardColumns-boardRows)*cellRadius*(1+Math.sin(Math.PI/6))/2;
			startYCoord -= (boardRows-1)*cellRadius*(1+Math.sin(Math.PI/6));
		}
		
		var getXCoord = function(row, column) {
			return startXCoord+(column-row)*cellRadius*Math.cos(Math.PI/6);
		}
		
		var getYCoord = function(row, column) {
			
			var yCoord = startYCoord;

			yCoord += (column-row)*cellRadius*(1+Math.sin(Math.PI/6));
			yCoord += (row-1)*2*cellRadius*(1+Math.sin(Math.PI/6));
			
			return yCoord;
		}
		
		var boardBorder = function(borderSide) {
			
			var yBorderLength = (cellRadius/Math.sin(Math.PI/6))+(2*(cellRadius*(1+Math.sin(Math.PI/6))));
			var xBorderLength = (cellRadius/Math.sin(Math.PI/3))+(2*(cellRadius*(Math.cos(Math.PI/6))));
			
			var borderObj = document.createElementNS(svgNS, "polygon");
			var pointsStr = "";
			
			switch (borderSide) {
			
			case 1:
				
				pointsStr += getXCoord(1,boardColumns) + "," + getYCoord(1,boardColumns) + " ";
				pointsStr += getXCoord(boardRows,boardColumns) + "," + getYCoord(boardRows,boardColumns) + " ";
				pointsStr += getXCoord(boardRows,boardColumns) + "," + (getYCoord(boardRows,boardColumns)+yBorderLength) + " ";
				pointsStr += (getXCoord(1,boardColumns)+xBorderLength) + "," + getYCoord(1,boardColumns);
				
				borderObj.style.fill="red";
				break;
				
			case 2:
				
				pointsStr += getXCoord(boardRows,1) + "," + getYCoord(boardRows,1) + " ";
				pointsStr += getXCoord(1,1) + "," + getYCoord(1,1) + " ";
				pointsStr += getXCoord(1,1) + "," + (getYCoord(1,1)-yBorderLength) + " ";
				pointsStr += (getXCoord(boardRows,1)-xBorderLength) + "," + getYCoord(boardRows,1);
				
				borderObj.style.fill="red";
				break;
				
			case 3:
				
				pointsStr += getXCoord(1,boardColumns) + "," + getYCoord(1,boardColumns) + " ";
				pointsStr += getXCoord(1,1) + "," + getYCoord(1,1) + " ";
				pointsStr += getXCoord(1,1) + "," + (getYCoord(1,1)-yBorderLength) + " ";
				pointsStr += (getXCoord(1,boardColumns)+xBorderLength) + "," + getYCoord(1,boardColumns);
				
				borderObj.style.fill="black";
				break;
				
			case 4:
				
				pointsStr += getXCoord(boardRows,1) + "," + getYCoord(boardRows,1) + " ";
				pointsStr += getXCoord(boardRows,boardColumns) + "," + getYCoord(boardRows,boardColumns) + " ";
				pointsStr += getXCoord(boardRows,boardColumns) + "," + (getYCoord(boardRows,boardColumns)+yBorderLength) + " ";
				pointsStr += (getXCoord(boardRows,1)-xBorderLength) + "," + getYCoord(boardRows,1);
				
				borderObj.style.fill="black";
				break;
			}

			borderObj.style.stroke="black";
			borderObj.setAttribute("points", pointsStr);
			
			return borderObj;
		}
		
		var textLabel = function(x, y, text, fill) {
			
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
		
		var hexCell = function(x, y, rad) {
		
			var hexObj = document.createElementNS(svgNS, "polygon");
			var pointsStr = "";
			
			for (i = 0; i < 6; i++) {
				if (pointsStr != "") {
					pointsStr += " ";
				}
				pointsStr += (x+(rad*Math.cos((i*Math.PI/3)+Math.PI/6))) + "," + (y-(rad*Math.sin((i*Math.PI/3)+Math.PI/6)));
			}
		
			hexObj.setAttribute("points", pointsStr);
			
			hexObj.style.fill="white";
			hexObj.style.stroke="black";
		
			return hexObj;
		}
		
		var drawSVGBoard = function(svgID) {
			
			var svgObj = document.getElementById(svgID);

			setCellRadius(svgObj);
			setStartCoords(svgObj);
			
			for (borderIndex = 1; borderIndex <= 4; borderIndex++) {
				svgObj.appendChild(boardBorder(borderIndex));
			}
			
			for (columnIndex = 1; columnIndex <= boardColumns; columnIndex++) {
				for (rowIndex = 1; rowIndex <= boardRows; rowIndex++) {
					svgObj.appendChild(hexCell(getXCoord(rowIndex, columnIndex), getYCoord(rowIndex, columnIndex), cellRadius));
				}
			}

			for (rowIndex = 1; rowIndex <= boardRows; rowIndex++) {
				svgObj.appendChild(textLabel(getXCoord(rowIndex,boardColumns+1), getYCoord(rowIndex,boardColumns+1), "R"+indexValues[rowIndex-1], "black"));
				svgObj.appendChild(textLabel(getXCoord(rowIndex,0), getYCoord(rowIndex,0), "R"+indexValues[rowIndex-1], "black"));
			}
			
			for (columnIndex = 1; columnIndex <= boardColumns; columnIndex++) {
				svgObj.appendChild(textLabel(getXCoord(boardRows+1,columnIndex), getYCoord(boardRows+1,columnIndex), "B"+indexValues[columnIndex-1], "white"));
				svgObj.appendChild(textLabel(getXCoord(0,columnIndex), getYCoord(0,columnIndex), "B"+indexValues[columnIndex-1], "white"));
			}
			
			document.getElementById("testDiv").innerHTML = Math.sin(Math.PI/6);
			document.getElementById("testDiv").innerHTML += ", " + svgObj.width.baseVal.value;
			document.getElementById("testDiv").innerHTML += ", " + cellRadius;
		}
	</script>
  </head>

  <body onload="drawSVGBoard('svgBoard');">
  	<table>
  		<tr>
  			<td>
				<svg
					id="svgBoard"
					xmlns="http://www.w3.org/2000/svg"
					xmlns:xlink="http://www.w3.org/1999/xlink"
					version="1.1"
					width="320"
					height="550"
					style="background: #004400;"></svg> <!-- Original dimensions 640 x 550 ; #004400 -->
  			</td>
  			<td>
  				<div id="testDiv"></div>
  			</td>
  		</tr>
  		<tr>
  			<td>
				<input type="button" value="Draw Hex" onclick="drawSVGBoard('svgBoard2');">
  			</td>
  			<td>
				<svg
					id="svgBoard2"
					xmlns="http://www.w3.org/2000/svg"
					xmlns:xlink="http://www.w3.org/1999/xlink"
					version="1.1"
					width="260"
					height="330"
					style="background: #444444;"></svg>
  			</td>
  		</tr>
  	</table>
  </body>
</html>
