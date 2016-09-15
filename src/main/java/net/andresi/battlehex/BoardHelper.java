package battlehex;

import java.lang.Math;

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
	
	public void setPlayer(int playerIn) {
		
		if ((playerIn & 0x0003) > 0) {
			boardShape |= playerIn;
		} else {
			boardShape |= FIRST_PLAYER;
		}
		
		setBoardVars();
		
	}
	
	public int getPlayer() {
		return boardShape & 0x0003;
	}
	
	public void setBoardShape(int bShapeIn) {
		
		if ((bShapeIn & 0x001c) > 0) {
			boardShape |= bShapeIn;
		} else {
			boardShape |= VERTICAL_BOARD;
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
				startXCoord = ((double)((double)imageWidth/2)-(((double)boardColumns-(double)boardRows)*getCellHalfWidth()/2));
				startYCoord = ((double) (((double)imageHeight/2)-(((double)boardColumns-(double)boardRows)*getCellExtendedLength()/2))-(((double)boardRows-1)*getCellExtendedLength()));
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
	
	public double getCellXCoord(int rowIn, int columnIn) {
		
		double returnXCoord = 0;
		
		switch (boardShape) {
		
		case VERTICAL_BOARD+FIRST_PLAYER:
			returnXCoord = getStartXCoord()+((columnIn-rowIn)*getCellHalfWidth());
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
		}
		
		return returnYCoord;
	}
		
	public double getPointXCoord(double cellXCoordIn, int pointIndexIn) {
		
		double returnXCoord = 0;
		
		switch (boardShape) {
		
		case VERTICAL_BOARD+FIRST_PLAYER:
			returnXCoord = cellXCoordIn+(getCellRadius()*Math.cos((pointIndexIn*Math.PI/3)+Math.PI/6));
			break;
		}
		
		return returnXCoord;
	}
	
	public double getPointYCoord(double cellYCoordIn, int pointIndexIn) {
		
		double returnYCoord = 0;
		
		switch (boardShape) {
		
		case VERTICAL_BOARD+FIRST_PLAYER:
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
}