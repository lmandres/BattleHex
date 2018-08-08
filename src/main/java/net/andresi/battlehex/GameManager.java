package battlehex;

import com.google.appengine.api.users.User;
import com.google.cloud.Timestamp;
import com.google.cloud.datastore.Datastore;
import com.google.cloud.datastore.DatastoreOptions;
import com.google.cloud.datastore.Entity;
import com.google.cloud.datastore.Key;
import com.google.cloud.datastore.KeyFactory;
import com.google.cloud.datastore.Query;
import com.google.cloud.datastore.QueryResults;
import com.google.cloud.datastore.StructuredQuery.OrderBy;
import com.google.cloud.datastore.StructuredQuery.PropertyFilter;

import java.util.Arrays;
import java.util.List;

public class GameManager {

	Key gameKey = null;

	int player = 0;

	List<String> indexValues = Arrays.asList("a", "2", "3", "4", "5", "6", "7", "8", "9", "10", "j", "q", "k");

	public static final Datastore datastore = DatastoreOptions.getDefaultInstance().getService();
	
	public void setPlayer(int playerIn) {
		player = playerIn;
	}
	
	public int getPlayer() {
		return player;
	}

	public Key getNewGame(User user, int playerIn, int boardRowsIn, int boardColumnsIn) {

		String player1Nickname = "";
		String player1ID = "";
		String player2Nickname = "";
		String player2ID = "";

		KeyFactory keyFactory = datastore.newKeyFactory().setKind("Game");
		Key key = keyFactory.newKey("gameKey");

		if (playerIn == BoardHelper.FIRST_PLAYER) {
			player1Nickname = user.getNickname();
			player1ID = user.getUserId();
		} else if (playerIn == BoardHelper.SECOND_PLAYER) {
			player2Nickname = user.getNickname();
			player2ID = user.getUserId();
		}

		Entity game = Entity.newBuilder(key)
			.set("player1Nickname", player1Nickname)
			.set("player2Nickname", player2Nickname)
			.set("player1ID", player1ID)
			.set("player2ID", player2ID)
			.set("boardRows", boardRowsIn)
			.set("boardColumns", boardColumnsIn)
			.set("lastModified", Timestamp.now())
			.build()
		;
		game = datastore.put(game);
		gameKey = game.getKey();

		return gameKey;
	}

	public Key joinExistingGame(String keyIn) {
		return joinExistingGame("(Computer)", "", keyIn);
	}

	public Key joinExistingGame(User user, String keyIn) {
		return joinExistingGame(user.getNickname(), user.getUserId(), keyIn);
	}

	public Key joinExistingGame(String playerNickname, String playerID, String keyIn) {

		Entity game = null;
		Entity gameOut = null;

		gameKey = Key.fromUrlSafe(keyIn);
		game = datastore.get(gameKey);

		if (
			game.getString("player1ID").equals("") &&
			game.getString("player1Nickname").equals("")
		) {
			gameOut = Entity.newBuilder(game)
				.set("player1Nickname", playerNickname)
				.set("player1ID", playerID)
				.set("lastModified", Timestamp.now())
				.build()
			;
		} else if (
			game.getString("player2ID").equals("") &&
			game.getString("player2Nickname").equals("")
		) {
			gameOut = Entity.newBuilder(game)
				.set("player2Nickname", playerNickname)
				.set("player2ID", playerID)
				.set("lastModified", Timestamp.now())
				.build()
			;
		}

		datastore.update(gameOut);
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

	public int[][] getGameMoves() {

		Entity gameMove = null;
		int gameMovesOut[][];

		gameMovesOut = new int[getBoardRows()+2][getBoardColumns()+2];

		for (int rowIndex = 0; rowIndex < gameMovesOut.length; rowIndex++) {
			for (int columnIndex = 0; columnIndex < gameMovesOut[rowIndex].length; columnIndex++) {
				if (
					(rowIndex == 0 && columnIndex == 0) ||
					(rowIndex == gameMovesOut.length-1 && columnIndex == gameMovesOut[rowIndex].length-1) ||
					(rowIndex == 0 && columnIndex == gameMovesOut[rowIndex].length-1) ||
					(rowIndex == gameMovesOut.length-1 && columnIndex == 0)
				) {
					gameMovesOut[rowIndex][columnIndex] = 3;
				} else if (rowIndex == 0 || rowIndex == gameMovesOut.length-1) {
					gameMovesOut[rowIndex][columnIndex] = 1;
				} else if (columnIndex == 0 || columnIndex == gameMovesOut[rowIndex].length-1) {
					gameMovesOut[rowIndex][columnIndex] = 2;
				} else {
					gameMovesOut[rowIndex][columnIndex] = 0;
				}
			}
		}

		Query<Entity> query = Query.newEntityQueryBuilder()
    			.setKind("GameMove")
    			.setFilter(PropertyFilter.hasAncestor(gameKey))
			.setOrderBy(OrderBy.asc("moveNumber"))
    			.build()
		;
		QueryResults<Entity> gameMoves = datastore.run(query);

		while (gameMoves.hasNext()) {

			String player1FlipCard, player1PlayCard, player2FlipCard, player2PlayCard;
			int player1Coords[], player2Coords[];

			gameMove = gameMoves.next();

			player1FlipCard = gameMove.getString("player1FlipCard");
			player1PlayCard = gameMove.getString("player1PlayCard");
			player2FlipCard = gameMove.getString("player2FlipCard");
			player2PlayCard = gameMove.getString("player2PlayCard");

			player1Coords = getMoveRowColumn(player1FlipCard, player1PlayCard);
			player2Coords = getMoveRowColumn(player2FlipCard, player2PlayCard);

			if (
				(player1Coords[0] == player2Coords[0]) &&
				(player1Coords[1] == player2Coords[1])
			) {
				if (player1FlipCard.equals(player2FlipCard)) {
					gameMovesOut[player1Coords[0]][player1Coords[1]] = 1;
				} else {
					gameMovesOut[player2Coords[0]][player2Coords[1]] = 2;
				}
			} else {
				gameMovesOut[player1Coords[0]][player1Coords[1]] = 1;
				gameMovesOut[player2Coords[0]][player2Coords[1]] = 2;
			}	
		}

		return gameMovesOut;
	}

	public int getBoardRows() {

		Entity game = null;
		int boardRowsOut = 0;

		game = datastore.get(gameKey);
		boardRowsOut = (int)game.getLong("boardRows");

		return boardRowsOut;
	}

	public int getBoardColumns() {

		Entity game = null;
		int boardColumnsOut = 0;

		game = datastore.get(gameKey);
		boardColumnsOut = (int)game.getLong("boardColumns");

		return boardColumnsOut;
	}

	public int[] getMoveRowColumn(String flipCardIn, String playCardIn) {

		int playerMove[] = {0, 0};

		if (flipCardIn != null) {
			if ("br".indexOf(flipCardIn.substring(0,1).toLowerCase()) == 1) {
				playerMove[0] = indexValues.indexOf(flipCardIn.substring(1))+1;
			} else if ("br".indexOf(flipCardIn.substring(0,1).toLowerCase()) == 0) {
				playerMove[1] = indexValues.indexOf(flipCardIn.substring(1))+1;
			}
		}

		if (playCardIn != null) {
			if ("br".indexOf(playCardIn.substring(0,1).toLowerCase()) == 1) {
				playerMove[0] = indexValues.indexOf(playCardIn.substring(1))+1;
			} else if ("br".indexOf(playCardIn.substring(0,1).toLowerCase()) == 0){
				playerMove[1] = indexValues.indexOf(playCardIn.substring(1))+1;
			}
		}

		return playerMove;
	}

	public void putGameMove(User user, String flipCardIn, String playCardIn) {
		putGameMove(user.getNickname(), user.getUserId(), flipCardIn, playCardIn);
	}

	public void putGameMove(String userNickname, String userID, String flipCardIn, String playCardIn) {

		Entity game = null;
		Entity gameMove = null;
		String player = "";

		KeyFactory keyFactory = datastore.newKeyFactory().setKind("GameMove");
		Key key = keyFactory.newKey("gameMoveKey");

		game = datastore.get(gameKey);
		if (
			userNickname.equals(game.getString("player1Nickname")) &&
			userID.equals(game.getString("player1ID"))
		) {
			player = "player1";
		} else if (
			userNickname.equals(game.getString("player2Nickname")) &&
			userID.equals(game.getString("player2ID"))
		) {
			player = "player2";
		}
		
		gameMove = Entity.newBuilder(key)
			.set(player + "FlipCard", flipCardIn)
			.set(player + "PlayCard", playCardIn)
			.set("lastModified", Timestamp.now())
			.build()
		;
		datastore.put(gameMove);
	}
}