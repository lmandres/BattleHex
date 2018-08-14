package battlehex;

import java.lang.Math;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public class BoardHelper {
	
	public static final int FIRST_PLAYER = 1;
	public static final int SECOND_PLAYER = 2;

	public static final int VERTICAL_BOARD = 4;
	public static final int HORIZONTAL_BOARD = 8;
	public static final int DIAMOND_BOARD = 16;
	
	private int imageWidth, imageHeight, boardRows, boardColumns, boardShape;	
	private double startXCoord, startYCoord, cellRadius;
	
	public BoardHelper() {
		
		imageWidth = 0;
		imageHeight = 0;
		
		boardRows = 13;
		boardColumns = 13;
		
		cellRadius = 0;
		
		startXCoord = 0;
		startYCoord = 0;
		
		setBoardShape(VERTICAL_BOARD+FIRST_PLAYER);
	}
	
	interface BoardTraverse {
        	public boolean move(ArrayList<ArrayList<Object>> gameBoardIn, int currentRow, int currentColumn, int player);
		public HashMap<String, Integer> increment(int currentRow, int currentColumn);
    	}

    	private static BoardTraverse[] boardTraversions = new BoardTraverse[] {
		new BoardTraverse() {
			public boolean move(ArrayList<ArrayList<Object>> gameBoardIn, int currentRow, int currentColumn, int player) {
				if ((currentColumn < gameBoardIn.get(currentRow).size()-1)) {
					if (Arrays.asList(3, player).indexOf(gameBoardIn.get(currentRow).get(currentColumn+1)) >= 0) {
						return true;
					}
				}
				return false;
			}
			public HashMap<String, Integer> increment(int currentRow, int currentColumn) {

				HashMap<String, Integer> incrementOut = new HashMap<String, Integer>();

				incrementOut.put("row", currentRow);
				incrementOut.put("column", currentColumn+1);

				return incrementOut;
			}
		},
		new BoardTraverse() {
			public boolean move(ArrayList<ArrayList<Object>> gameBoardIn, int currentRow, int currentColumn, int player) {
				if ((currentRow < gameBoardIn.size()-1)) {
					if (Arrays.asList(3, player).indexOf(gameBoardIn.get(currentRow+1).get(currentColumn)) >= 0) {
						return true;
					}
				}
				return false;
			}
			public HashMap<String, Integer> increment(int currentRow, int currentColumn) {

				HashMap<String, Integer> incrementOut = new HashMap<String, Integer>();

				incrementOut.put("row", currentRow+1);
				incrementOut.put("column", currentColumn);

				return incrementOut;
			}
		},
		new BoardTraverse() {
			public boolean move(ArrayList<ArrayList<Object>> gameBoardIn, int currentRow, int currentColumn, int player) {
				if ((currentRow < gameBoardIn.size()-1) && (currentColumn > 0)) {
					if (Arrays.asList(3, player).indexOf(gameBoardIn.get(currentRow+1).get(currentColumn-1)) >= 0) {
						return true;
					}
				}
				return false;
			}
			public HashMap<String, Integer> increment(int currentRow, int currentColumn) {

				HashMap<String, Integer> incrementOut = new HashMap<String, Integer>();

				incrementOut.put("row", currentRow+1);
				incrementOut.put("column", currentColumn-1);

				return incrementOut;
			}
		},
		new BoardTraverse() {
			public boolean move(ArrayList<ArrayList<Object>> gameBoardIn, int currentRow, int currentColumn, int player) {
				if ((currentColumn > 0)) {
					if (Arrays.asList(3, player).indexOf(gameBoardIn.get(currentRow).get(currentColumn-1)) >= 0) {
						return true;
					}
				}
				return false;
			}
			public HashMap<String, Integer> increment(int currentRow, int currentColumn) {

				HashMap<String, Integer> incrementOut = new HashMap<String, Integer>();

				incrementOut.put("row", currentRow);
				incrementOut.put("column", currentColumn-1);

				return incrementOut;
			}
		},
		new BoardTraverse() {
			public boolean move(ArrayList<ArrayList<Object>> gameBoardIn, int currentRow, int currentColumn, int player) {
				if ((currentRow > 0)) {
					if (Arrays.asList(3, player).indexOf(gameBoardIn.get(currentRow-1).get(currentColumn)) >= 0) {
						return true;
					}
				}
				return false;
			}
			public HashMap<String, Integer> increment(int currentRow, int currentColumn) {

				HashMap<String, Integer> incrementOut = new HashMap<String, Integer>();

				incrementOut.put("row", currentRow-1);
				incrementOut.put("column", currentColumn);

				return incrementOut;
			}
		},
		new BoardTraverse() {
			public boolean move(ArrayList<ArrayList<Object>> gameBoardIn, int currentRow, int currentColumn, int player) {
				if ((currentRow > 0) && (currentColumn < gameBoardIn.get(currentRow).size()-1)) {
					if (Arrays.asList(3, player).indexOf(gameBoardIn.get(currentRow-1).get(currentColumn+1)) >= 0) {
						return true;
					}
				}
				return false;
			}
			public HashMap<String, Integer> increment(int currentRow, int currentColumn) {

				HashMap<String, Integer> incrementOut = new HashMap<String, Integer>();

				incrementOut.put("row", currentRow-1);
				incrementOut.put("column", currentColumn+1);

				return incrementOut;
			}
		}
	};

    	public static boolean evaluateWin(int playerIn, int[][] gameBoardIn) {
		return evaluateWin(playerIn, convertBoardArraysToObjLists(gameBoardIn));
	}

    	public static boolean evaluateWin(int playerIn, ArrayList<ArrayList<Object>> gameBoardIn) {

		int startIndex = 0;
		int currentRow = 0;
		int currentColumn = 0;

		if (playerIn == 1) {
			while (true) {
				if (
					!boardTraversions[startIndex].move(gameBoardIn, currentRow, currentColumn, playerIn) &&
					boardTraversions[((startIndex+1) % 6)].move(gameBoardIn, currentRow, currentColumn, playerIn)
				) {
					startIndex = ((startIndex+1) % 6);
					HashMap<String, Integer> coordMap = boardTraversions[startIndex].increment(currentRow, currentColumn);
					currentRow = (Integer)coordMap.get("row");
					currentColumn = (Integer)coordMap.get("column");
					startIndex = ((startIndex+4) % 6);
				} else if (
					boardTraversions[startIndex].move(gameBoardIn, currentRow, currentColumn, playerIn) &&
					!boardTraversions[((startIndex+1) % 6)].move(gameBoardIn, currentRow, currentColumn, playerIn)
				) {
					startIndex = ((startIndex+5) % 6);
				} else if (
					boardTraversions[startIndex].move(gameBoardIn, currentRow, currentColumn, playerIn) &&
					boardTraversions[((startIndex+1) % 6)].move(gameBoardIn, currentRow, currentColumn, playerIn)
				) {
					startIndex = ((startIndex+5) % 6);
				} else if (
					!boardTraversions[startIndex].move(gameBoardIn, currentRow, currentColumn, playerIn) &&
					!boardTraversions[((startIndex+1) % 6)].move(gameBoardIn, currentRow, currentColumn, playerIn)
				) {
					startIndex = ((startIndex+1) % 6);
				} else {
					startIndex = ((startIndex+1) % 6);
				}
				if ((Integer)gameBoardIn.get(currentRow).get(currentColumn) == 3) {
					break;
				}
			}
		} else if (playerIn == 2) {
			while (true) {
				if (
					boardTraversions[startIndex].move(gameBoardIn, currentRow, currentColumn, playerIn) &&
					!boardTraversions[((startIndex+1) % 6)].move(gameBoardIn, currentRow, currentColumn, playerIn)
				) {
					HashMap<String, Integer> coordMap = boardTraversions[startIndex].increment(currentRow, currentColumn);
					currentRow = (Integer)coordMap.get("row");
					currentColumn = (Integer)coordMap.get("column");
				} else if (
					!boardTraversions[startIndex].move(gameBoardIn, currentRow, currentColumn, playerIn) &&
					boardTraversions[((startIndex+1) % 6)].move(gameBoardIn, currentRow, currentColumn, playerIn)
				) {
					startIndex = ((startIndex+1) % 6);
				} else if (
					boardTraversions[startIndex].move(gameBoardIn, currentRow, currentColumn, playerIn) &&
					boardTraversions[((startIndex+1) % 6)].move(gameBoardIn, currentRow, currentColumn, playerIn)
				) {
					startIndex = ((startIndex+1) % 6);
				} else if (
					!boardTraversions[startIndex].move(gameBoardIn, currentRow, currentColumn, playerIn) &&
					!boardTraversions[((startIndex+1) % 6)].move(gameBoardIn, currentRow, currentColumn, playerIn)
				) {
					startIndex = ((startIndex+5) % 6);
				} else {
					startIndex = ((startIndex+5) % 6);
				}
				if ((Integer)gameBoardIn.get(currentRow).get(currentColumn) == 3) {
					break;
				}
			}
		}

		if ((playerIn == 1) && (currentColumn >= gameBoardIn.get(currentRow).size()-1)) {
			return true;
		} else if ((playerIn == 2) && (currentRow >= gameBoardIn.size()-1)) {
			return true;
		}

		return false;
	}

	private static ArrayList<ArrayList<Object>> convertBoardArraysToObjLists(int[][] gameBoardIn) {

		ArrayList<ArrayList<Object>> gameBoardOut = new ArrayList<ArrayList<Object>>();

		for (int rowIndex = 0; rowIndex < gameBoardIn.length; rowIndex++) {
			ArrayList<Object> intObjList = new ArrayList<Object>();
			for (int columnIndex = 0; columnIndex < gameBoardIn[rowIndex].length; columnIndex++) {
				intObjList.add(gameBoardIn[rowIndex][columnIndex]);
			}
			gameBoardOut.add(intObjList);
		}

		return gameBoardOut;
	}

	public static ArrayList<ArrayList<Integer>> convertBoardArraysToLists(int[][] gameBoardIn) {

		ArrayList<ArrayList<Integer>> gameBoardOut = new ArrayList<ArrayList<Integer>>();

		for (int rowIndex = 0; rowIndex < gameBoardIn.length; rowIndex++) {
			ArrayList<Integer> intObjList = new ArrayList<Integer>();
			for (int columnIndex = 0; columnIndex < gameBoardIn[rowIndex].length; columnIndex++) {
				intObjList.add(gameBoardIn[rowIndex][columnIndex]);
			}
			gameBoardOut.add(intObjList);
		}

		return gameBoardOut;
	}

	public static int[][] convertBoardListsToArrays(ArrayList<ArrayList<Integer>> gameBoardIn) {

		int[][] gameBoardOut = new int[gameBoardIn.size()][];

		for (int rowIndex = 0; rowIndex < gameBoardIn.size(); rowIndex++) {
			gameBoardOut[rowIndex] = new int[gameBoardIn.get(rowIndex).size()];
			for (int columnIndex = 0; columnIndex < gameBoardIn.get(rowIndex).size(); columnIndex++) {
				gameBoardOut[rowIndex][columnIndex] = gameBoardIn.get(rowIndex).get(columnIndex);
			}
		}

		return gameBoardOut;
	}
	
	public void setPlayer(int playerIn) {
		
		if ((playerIn & 0x0003) > 0) {
			boardShape = (boardShape & 0xfffc) | (playerIn & 0x0003);
		} else {
			boardShape = (boardShape & 0xfffc) | (FIRST_PLAYER & 0x0003);
		}
		
		setBoardVars();
		
	}
	
	public int getPlayer() {
		return boardShape & 0x0003;
	}
	
	public int getOpponent() {

		int returnPlayer = FIRST_PLAYER;

		if ((boardShape & 0x0003) == FIRST_PLAYER) {
			returnPlayer = SECOND_PLAYER;
		}

		return returnPlayer;
	}
	
	public void setBoardShape(int bShapeIn) {
		
		if ((bShapeIn & 0x001c) > 0) {
			boardShape = (boardShape & 0xffe3) | (bShapeIn & 0x001c);
		} else {
			boardShape = (boardShape & 0xffe3) | (VERTICAL_BOARD & 0x001c);
		}
		
		setBoardVars();
	}
	
	public int getBoardShape() {
		return boardShape & 0x001c;
	}

	public void setImageWidth(int widthIn) {
		
		if (widthIn > 0) {
			imageWidth = widthIn;
		}
		
		setBoardVars();
	}
	
	public int getImageWidth() {
		return imageWidth;
	}
	
	public void setImageHeight(int heightIn) {
		
		if (heightIn > 0) {
			imageHeight = heightIn;
		}
		
		setBoardVars();
	}
	
	public int getImageHeight() {
		return imageHeight;
	}
	
	public void setBoardRows(int rowsIn) {
		
		if (rowsIn > 0) {
			boardRows = rowsIn;
		}
		
		setBoardVars();
	}
	
	public int getBoardRows() {
		return boardRows;
	}
	
	public void setBoardColumns(int columnsIn) {
		
		if (columnsIn > 0) {
			boardColumns = columnsIn;
		}
		
		setBoardVars();
	}
	
	public int getBoardColumns() {
		return boardColumns;
	}
	
	private void setCellRadius() {
		
		if (imageWidth > 0 && imageHeight > 0 && boardRows > 0 && boardColumns > 0) {
			
			double cellYRadius = 0;
			
			switch (boardShape) {
			
			case VERTICAL_BOARD+FIRST_PLAYER:
			case VERTICAL_BOARD+SECOND_PLAYER:
				cellYRadius = ((double)imageHeight/(2+((((double)boardRows-1)+((double)boardColumns-1)+3)*(1+Math.sin(Math.PI/6)))+(2/Math.sin(Math.PI/6))));
				cellRadius = ((double)imageWidth/((((double)boardColumns+1)*Math.sin(Math.PI/3))+(((double)boardRows-1)*Math.sin(2*Math.PI/3))+(2/Math.sin(Math.PI/3))+(2*Math.cos(Math.PI/6))));
				break;
			}
			
			if (cellRadius > cellYRadius  && cellYRadius > 0) {
				cellRadius = cellYRadius;
			}
		}
	}
	
	private void setBoardVars() {
		setCellRadius();
		setStartCoords();
	}
	
	public double getCellRadius() {
		return cellRadius;
	}
	
	public double getPieceRadius() {
		return cellRadius*0.7;
	}
	
	private void setStartCoords() {
		
		if (imageWidth > 0 && imageHeight > 0 && boardRows > 0 && boardColumns > 0 && cellRadius > 0) {
			
			switch (boardShape) {
			
			case VERTICAL_BOARD+FIRST_PLAYER:
			case VERTICAL_BOARD+SECOND_PLAYER:
				startXCoord = ((double)((double)imageWidth/2)-(((double)boardColumns-(double)boardRows)*getCellHalfWidth()/2));
				startYCoord = ((double)(((double)imageHeight/2)-(((double)boardColumns-(double)boardRows)*getCellExtendedLength()/2))-(((double)boardRows-1)*getCellExtendedLength()));
				break;
			}
		}
	}
	
	public double getStartXCoord() {
		return startXCoord;
	}
	
	public double getStartYCoord() {
		return startYCoord;
	}
	
	public double getBorderObtuseXCoord() {
		
		double returnXCoord = 0;
		
		switch (boardShape) {
		
		case VERTICAL_BOARD+FIRST_PLAYER:
		case VERTICAL_BOARD+SECOND_PLAYER:
			returnXCoord = ((getBoardColumns()-1)*getCellHalfWidth());
			break;
		}
		
		return returnXCoord;
	}
	
	public double getBorderObtuseYCoord() {
		
		double returnYCoord = 0;
		
		switch (boardShape) {
		
		case VERTICAL_BOARD+FIRST_PLAYER:
		case VERTICAL_BOARD+SECOND_PLAYER:
			returnYCoord = ((getBoardColumns()-1)*getCellExtendedLength());
			break;
		}
		
		return returnYCoord;
	}
	
	public double getBorderAcuteXCoord() {
		
		double returnXCoord = 0;
		
		switch (boardShape) {
		
		case VERTICAL_BOARD+FIRST_PLAYER:
		case VERTICAL_BOARD+SECOND_PLAYER:
			returnXCoord = 0;
			break;
		}
		
		return returnXCoord;
	}
	
	public double getBorderAcuteYCoord() {
		
		double returnYCoord = 0;
		
		switch (boardShape) {
		
		case VERTICAL_BOARD+FIRST_PLAYER:
		case VERTICAL_BOARD+SECOND_PLAYER:
			returnYCoord = ((getBoardRows()-1)*(2*getCellExtendedLength()));
			break;
		}
		
		return returnYCoord;
	}
	
	public double getCellXCoord(int rowIn, int columnIn) {
		
		double returnXCoord = 0;
		
		switch (boardShape) {
		
		case VERTICAL_BOARD+FIRST_PLAYER:
			returnXCoord = getStartXCoord()+((columnIn-rowIn)*getCellHalfWidth());
			break;
		case VERTICAL_BOARD+SECOND_PLAYER:
			returnXCoord = getStartXCoord()+(((getBoardColumns()-columnIn+1)-(getBoardRows()-rowIn+1))*getCellHalfWidth());
			break;
		}
		
		return returnXCoord;
	}
	
	public double getCellYCoord(int rowIn, int columnIn) {
		
		double returnYCoord = 0;
		
		switch (boardShape) {
		
		case VERTICAL_BOARD+FIRST_PLAYER:
			returnYCoord = getStartYCoord()+((columnIn-rowIn)*getCellExtendedLength())+((rowIn-1)*(2*getCellExtendedLength()));
			break;
		case VERTICAL_BOARD+SECOND_PLAYER:
			returnYCoord = getStartYCoord()+(((getBoardColumns()-columnIn+1)-(getBoardRows()-rowIn+1))*getCellExtendedLength())+(((getBoardRows()-rowIn))*(2*getCellExtendedLength()));
			break;
		}
		
		return returnYCoord;
	}
		
	public double getPointXCoord(double cellXCoordIn, int pointIndexIn) {
		
		double returnXCoord = 0;
		
		switch (boardShape) {
		
		case VERTICAL_BOARD+FIRST_PLAYER:
		case VERTICAL_BOARD+SECOND_PLAYER:
			returnXCoord = cellXCoordIn+(getCellRadius()*Math.cos((pointIndexIn*Math.PI/3)+Math.PI/6));
			break;
		}
		
		return returnXCoord;
	}
	
	public double getPointYCoord(double cellYCoordIn, int pointIndexIn) {
		
		double returnYCoord = 0;
		
		switch (boardShape) {
		
		case VERTICAL_BOARD+FIRST_PLAYER:
		case VERTICAL_BOARD+SECOND_PLAYER:
			returnYCoord = cellYCoordIn+(getCellRadius()*Math.sin((pointIndexIn*Math.PI/3)+Math.PI/6));
			break;
		}
		
		return returnYCoord;
	}
	
	public double getPointXDelta(int pointIndexIn) {
		return getPointXCoord(0, pointIndexIn);
	}
	
	public double getPointYDelta(int pointIndexIn) {
		return getPointYCoord(0, pointIndexIn);
	}
	
	public double getCellHalfWidth() {
		return cellRadius*Math.cos(Math.PI/6);
	}
	
	public double getCellExtendedLength() {
		return cellRadius*(1+Math.sin(Math.PI/6));
	}
	
	public double getAcuteBorderLength() {
		return (cellRadius/Math.sin(Math.PI/6))+(2*(cellRadius*(1+Math.sin(Math.PI/6))));
	}
	
	public double getObtuseBorderLength() {
		return (cellRadius/Math.sin(Math.PI/3))+(2*(cellRadius*(Math.cos(Math.PI/6))));
	}

	public String getBoardLabel(int intRowColumnIn) {
		String[] indexValues = {"A", "2", "3", "4", "5", "6", "7", "8", "9", "10", "J", "Q", "K"};
		return indexValues[intRowColumnIn-1];
	}

	public String getPlayerSuits() {

		String returnSuits = "";

		if (getPlayer() == FIRST_PLAYER) {
			returnSuits = "s,d";
		} else if (getPlayer() == SECOND_PLAYER) {
			returnSuits = "c,h";
		}

		return returnSuits;
	}

	public String getOpponentSuits() {

		String returnSuits = "";

		if (getPlayer() == SECOND_PLAYER) {
			returnSuits = "s,d";
		} else if (getPlayer() == FIRST_PLAYER) {
			returnSuits = "c,h";
		}

		return returnSuits;
	}
}