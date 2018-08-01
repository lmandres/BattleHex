package battlehex;

import java.lang.Math;

import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;

import org.apache.commons.lang3.ArrayUtils;

import com.google.api.client.json.JsonParser;
import com.google.api.client.json.JsonToken;
import com.google.api.client.json.gson.GsonFactory;

import com.google.appengine.api.users.User;
import com.google.cloud.Timestamp;
import com.google.cloud.datastore.Datastore;
import com.google.cloud.datastore.DatastoreOptions;
import com.google.cloud.datastore.Entity;
import com.google.cloud.datastore.Entity.Builder;
import com.google.cloud.datastore.Key;
import com.google.cloud.datastore.Query;
import com.google.cloud.datastore.QueryResults;
import com.google.cloud.datastore.StructuredQuery.PropertyFilter;

public class BoardHelper {
	
	public static final int FIRST_PLAYER = 1;
	public static final int SECOND_PLAYER = 2;

	public static final int VERTICAL_BOARD = 4;
	public static final int HORIZONTAL_BOARD = 8;
	public static final int DIAMOND_BOARD = 16;
	
	private int imageWidth, imageHeight, boardRows, boardColumns, boardShape;	
	private double startXCoord, startYCoord, cellRadius;

	private Key gameKey;
	private String appNotifierKey;

	static final String AUTH_KEY = "";
	User user = null;
	
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

        private JsonParser postJSONToURL(String url, String jsonContent) {

		DataOutputStream outputstream = null;
		GsonFactory gsonFactory = new GsonFactory();
		JsonParser jsonParser = null;

		URL urlObj = null;
		HttpURLConnection conn = null;

		try {
			urlObj = new URL(url);
		} catch (MalformedURLException mue) {
		}

		try {
			conn = (HttpURLConnection)urlObj.openConnection();
		} catch (IOException ioe) {
		}

		try {
			conn.setRequestMethod("POST");
		} catch (ProtocolException pe) {
		}

		conn.setRequestProperty("Content-Type", "application/json");
		conn.setRequestProperty("Authorization", "key=" + AUTH_KEY);

		try {
			outputstream = (DataOutputStream)conn.getOutputStream();
			outputstream.writeBytes(jsonContent);
			outputstream.close();
		} catch (IOException ioe) {
		}

		try {
			jsonParser = gsonFactory.createJsonParser(conn.getInputStream());
		} catch (IOException ioe) {
		}

		return jsonParser;
	}
	public void notifyPlayers() {

		Datastore datastore = DatastoreOptions.getDefaultInstance().getService();

		Entity game = null;

		Query<Entity> query = Query.newEntityQueryBuilder()
			.setKind("Game")
			.setFilter(PropertyFilter.eq("gameKey", gameKey))
			.build()
		;
		QueryResults<Entity> games = datastore.run(query);

		while (games.hasNext()) {

			game = games.next();

			if (game.getKey() != null) {

				String url = "https://fcm.googleapis.com/fcm/send";
				String content = "";

				content += "{";
				content += "\"notification\": ";
				content += "{";
				content += "\"notification_key_name\": \"" + appNotifierKey + "\",";
				content += "\"registration_ids\": []";
				content += "}, ";
				content += "\"to\": \"" +  game.getString("notificationKey") + "\",";
				content += "}";

				postJSONToURL(url, content);
			}
		}
	}

	public void addGameListener(String fbTokenIn) {

		Datastore datastore = DatastoreOptions.getDefaultInstance().getService();

		Entity game = null;

		Query<Entity> query = Query.newEntityQueryBuilder()
			.setKind("Game")
			.setFilter(PropertyFilter.eq("gameKey", gameKey))
			.build()
		;
		QueryResults<Entity> games = datastore.run(query);

		while (games.hasNext()) {

			game = games.next();

			if (game.getKey() != null) {

				String url = "https://android.googleapis.com/gcm/notification";
				String content = "";

				content += "{";
				content += "\"operation\": \"add\",";
				content += "\"notification_key_name\": \"" + appNotifierKey + "\",";
				content += "\"notification_key\": \"" + game.getString("notificationKey") + "\",";
				content += "\"registration_ids\": [\"" + fbTokenIn + "\"]";
				content += "}";

				postJSONToURL(url, content);
			}
		}
	}

	public Key getNewGame(User user, int playerIn) throws IOException {

		Datastore datastore = DatastoreOptions.getDefaultInstance().getService();
		JsonParser jsonParser = null;
		JsonToken jsonToken = null;

		String url = "https://android.googleapis.com/gcm/notification";
		String content;

		String kind = "Game";
		String name = "gameKey";

		String gameListenerKey = "";

		String player1Nickname = "";
		String player1ID = "";
		String player2Nickname = "";
		String player2ID = "";
	
		gameKey = datastore.newKeyFactory().setKind(kind).newKey(name);

		if (playerIn == BoardHelper.FIRST_PLAYER) {
			player1Nickname = user.getNickname();
			player1ID = user.getUserId();
		} else if (playerIn == BoardHelper.SECOND_PLAYER) {
			player2Nickname = user.getNickname();
			player2ID = user.getUserId();
		}

		appNotifierKey = "app_battle-hex_game_" + gameKey.toUrlSafe();

		content = "";
		content += "{";
		content += "\"operation\": \"create\",";
		content += "\"notification_key_name\": \"" + appNotifierKey + "\",";
		content += "\"registration_ids\": []";
		content += "}";

		jsonParser = postJSONToURL(url, content);

		while (jsonParser.nextToken() != null) {
			jsonToken = jsonParser.getCurrentToken();
			if (jsonToken == JsonToken.FIELD_NAME) {
				if (jsonParser.getText() == "notification_key") {
					jsonToken = jsonParser.nextToken();
					gameListenerKey = jsonParser.getText();
					break;
				}
			}
		}

		Entity game = Entity.newBuilder(gameKey)
			.set("player1Nickname", player1Nickname)
			.set("player2Nickname", player2Nickname)
			.set("player1ID", player1ID)
			.set("player2ID", player2ID)
			.set("notificationKey", gameListenerKey)
			.set("lastModified", Timestamp.now())
			.build()
		;
		datastore.put(game);

		return gameKey;
	}

	public Key joinExistingGame(User user, String keyIn) {

		String player1Nickname = "";
		String player1ID = "";
		String player2Nickname = "";
		String player2ID = "";

		Entity game = null;

		Datastore datastore = DatastoreOptions.getDefaultInstance().getService();

		Query<Entity> query = Query.newEntityQueryBuilder()
			.setKind("Game")
			.setFilter(PropertyFilter.eq("gameKey", Key.fromUrlSafe(keyIn)))
			.build()
		;
		QueryResults<Entity> games = datastore.run(query);

		while (games.hasNext()) {

			game = games.next();

			Entity gameOut = null;

			if (game.getString("player1ID") == "") {
				gameOut = Entity.newBuilder(game.getKey("gameKey"))
					.set("player1Nickname", user.getNickname())
					.set("player1ID", user.getUserId())
					.set("lastModified", Timestamp.now())
					.build()
				;
			} else {
				gameOut = Entity.newBuilder(game.getKey("gameKey"))
					.set("player2Nickname", user.getNickname())
					.set("player2ID", user.getUserId())
					.set("lastModified", Timestamp.now())
					.build()
				;
			}

			datastore.update(gameOut);
		}
		gameKey = game.getKey("gameKey");

		return gameKey;
	}

	public void setGameKey(Key gameKeyIn) {
		gameKey = gameKeyIn;
	}

	public void setGameKeyUrlSafe(String gameKeyIn) {
		gameKey = Key.fromUrlSafe(gameKeyIn);
	}

	public Key getGameKey() {
		return gameKey;
	}
}
