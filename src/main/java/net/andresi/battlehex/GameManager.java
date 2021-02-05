package battlehex;

import battlehex.BoardHelper;

import com.google.appengine.api.users.User;
import com.google.cloud.datastore.Datastore;
import com.google.cloud.datastore.DatastoreException;
import com.google.cloud.datastore.DatastoreOptions;
import com.google.cloud.datastore.Entity;
import com.google.cloud.datastore.FullEntity;
import com.google.cloud.datastore.IncompleteKey;
import com.google.cloud.datastore.Key;
import com.google.cloud.datastore.KeyFactory;
import com.google.cloud.datastore.PathElement;
import com.google.cloud.datastore.Query;
import com.google.cloud.datastore.QueryResults;
import com.google.cloud.datastore.StructuredQuery.CompositeFilter;
import com.google.cloud.datastore.StructuredQuery.OrderBy;
import com.google.cloud.datastore.StructuredQuery.PropertyFilter;
import com.google.cloud.datastore.Transaction;
import com.google.cloud.Timestamp;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

public class GameManager {

	Key gameKey = null;

	List<String> indexValues = Arrays.asList("a", "2", "3", "4", "5", "6", "7", "8", "9", "10", "j", "q", "k");

	public static final Datastore datastore = DatastoreOptions.getDefaultInstance().getService();

	public Key getNewGame(User user, int playerIn, int boardRowsIn, int boardColumnsIn) {

		String player1Nickname = "";
		String player1ID = "";
		String player2Nickname = "";
		String player2ID = "";

		Transaction transaction = datastore.newTransaction();

		if (playerIn == 1) {
			player1Nickname = user.getNickname();
			player1ID = user.getUserId();
		} else if (playerIn == 2) {
			player2Nickname = user.getNickname();
			player2ID = user.getUserId();
		}

		try {
			KeyFactory keyFactory = datastore.newKeyFactory().setKind("Game");
			IncompleteKey key = keyFactory.newKey();

			FullEntity game = FullEntity.newBuilder(key)
				.set("player1Nickname", player1Nickname)
				.set("player2Nickname", player2Nickname)
				.set("player1ID", player1ID)
				.set("player2ID", player2ID)
				.set("boardRows", boardRowsIn)
				.set("boardColumns", boardColumnsIn)
				.set("lastModified", Timestamp.now())
				.build()
			;
			gameKey = transaction.add(game).getKey();
			transaction.commit();

		} finally {
			if (transaction.isActive()) {
				//transaction.rollback();
			}
		}

		return gameKey;
	}

	public Key joinExistingGame(String keyIn) {
		return joinExistingGame("(Computer)", "", keyIn);
	}

	public Key joinExistingGame(User user, String keyIn) {
		return joinExistingGame(user.getNickname(), user.getUserId(), keyIn);
	}

	public Key joinExistingGame(String playerNickname, String playerID, String keyIn) {

		Entity gameOut = null;

		Transaction transaction = datastore.newTransaction();
		gameKey = Key.fromUrlSafe(keyIn);

		try {
			Entity game = null;
			game = transaction.get(gameKey);

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

			if (gameOut != null) {
				transaction.update(gameOut);
				transaction.commit();
				gameKey = gameOut.getKey();
			}

		} finally {
			if (transaction.isActive()) {
				//transaction.rollback();
			}
		}

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
					gameMovesOut[rowIndex][columnIndex] = 2;
				} else if (columnIndex == 0 || columnIndex == gameMovesOut[rowIndex].length-1) {
					gameMovesOut[rowIndex][columnIndex] = 1;
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

			String player1FlipCard = null;
			String player1PlayCard = null;
			String player2FlipCard = null;
			String player2PlayCard = null;

			int player1Coords[], player2Coords[];

			gameMove = gameMoves.next();

			try {
				player1FlipCard = gameMove.getString("player1FlipCard");
				player1PlayCard = gameMove.getString("player1PlayCard");
				player2FlipCard = gameMove.getString("player2FlipCard");
				player2PlayCard = gameMove.getString("player2PlayCard");
			} catch (DatastoreException de) {
			}

			player1Coords = getMoveRowColumn(player1FlipCard, player1PlayCard);
			player2Coords = getMoveRowColumn(player2FlipCard, player2PlayCard);

			if (
				!(player1FlipCard == null) &&
				!(player1PlayCard == null) &&
				!(player2FlipCard == null) &&
				!(player2PlayCard == null)
			) {

				if (
					(player1Coords[0] == player2Coords[0]) &&
					(player1Coords[1] == player2Coords[1]) &&
					(player1Coords[0] != 0) &&
					(player1Coords[1] != 0)
				) {
					if (player1FlipCard.equals(player2FlipCard)) {
						gameMovesOut[player1Coords[0]][player1Coords[1]] = 1;
					} else {
						gameMovesOut[player2Coords[0]][player2Coords[1]] = 2;
					}
				} else {
					if (
						(player1Coords[0] != 0) &&
						(player1Coords[1] != 0)
					) {
						gameMovesOut[player1Coords[0]][player1Coords[1]] = 1;
					}
					if (
						(player2Coords[0] != 0) &&
						(player2Coords[1] != 0)
					) {
						gameMovesOut[player2Coords[0]][player2Coords[1]] = 2;
					}
				}	
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

	public HashMap<String, String> getRandomCardsFromCoords(int rowIn, int columnIn) {

		Random rand = new Random();
		int flipIndex = rand.nextInt(2);

		HashMap<String, String> moveOut = new HashMap<String, String>();

		String cards[] = {
			"r" + indexValues.get(rowIn-1),
			"b" + indexValues.get(columnIn-1)
		};

		moveOut.put("flipCard", cards[flipIndex]);
		moveOut.put("playCard", cards[((flipIndex + 1) % 2)]);

		return moveOut;	
	}

	public void putComputerMove(String flipCardIn, String playCardIn) {
		putGameMove("(Computer)", "", flipCardIn, playCardIn);
	}

	public void putGameMove(User user, String flipCardIn, String playCardIn) {
		putGameMove(user.getNickname(), user.getUserId(), flipCardIn, playCardIn);
	}

	public void putGameMove(String userNickname, String userID, String flipCardIn, String playCardIn) {

		Key gameMoveKey = null;
		String player = "player" + Integer.toString(getPlayer(userNickname, userID));

		Transaction transaction = datastore.newTransaction();

		Query<Entity> query = Query.newEntityQueryBuilder()
    			.setKind("GameMove")
    			.setFilter(PropertyFilter.hasAncestor(gameKey))
			.setOrderBy(OrderBy.desc("moveNumber"))
			.setLimit(1)
    			.build()
		;

		try {
			Entity gameMove = null;

			QueryResults<Entity> gameMoves = null;

			String flipCard = "";
			String playCard = "";

			gameMoves = transaction.run(query);
			while (gameMoves.hasNext()) {
				gameMove = gameMoves.next();
			}

			try {
				flipCard = gameMove.getString(player + "FlipCard");
				playCard = gameMove.getString(player + "PlayCard");
			} catch (DatastoreException de) {
			}

			if (
				flipCard.equals("") &&
				playCard.equals("")
			) {
				gameMove = Entity.newBuilder(gameMove)
					.set(player + "FlipCard", flipCardIn)
					.set(player + "PlayCard", playCardIn)
					.set("lastModified", Timestamp.now())
					.build()
				;

				transaction.update(gameMove);
				transaction.commit();
			}

			gameMoveKey = gameMove.getKey();

		} finally {
			if (transaction.isActive()) {
				//transaction.rollback();
			}
		}
	}

	public int createGameMove(int moveNumber) {

		FullEntity gameMove = null;
		Transaction transaction = datastore.newTransaction();

		Query<Entity> query = Query.newEntityQueryBuilder()
    			.setKind("GameMove")
    			.setFilter(PropertyFilter.hasAncestor(gameKey))
			.setOrderBy(OrderBy.desc("moveNumber"))
			.setLimit(1)
    			.build()
		;

		try {
			String player1FlipCard = null;
			String player1PlayCard = null;
			String player2FlipCard = null;
			String player2PlayCard = null;

			KeyFactory keyFactory = datastore.newKeyFactory()
				.addAncestor(PathElement.of("Game", gameKey.getId()))
				.setKind("GameMove")
			;
			IncompleteKey key = keyFactory.newKey();

			QueryResults<Entity> gameMoves = transaction.run(query);
			if (gameMoves.hasNext()) {
				gameMove = gameMoves.next();
			}

			try {
				player1FlipCard = gameMove.getString("player1FlipCard");
				player1PlayCard = gameMove.getString("player1PlayCard");
				player2FlipCard = gameMove.getString("player2FlipCard");
				player2PlayCard = gameMove.getString("player2PlayCard");
			} catch (DatastoreException de) {
			} catch (NullPointerException npe) {
			}

			if (gameMove == null) {
				gameMove = FullEntity.newBuilder(key)
					.set("moveNumber", moveNumber)
					.set("lastModified", Timestamp.now())
					.build()
				;
				gameMove = transaction.add(gameMove);
				transaction.commit();
			} else if (
				player1FlipCard != null &&
				player1PlayCard != null &&
				player2FlipCard != null &&
				player2PlayCard != null
			) {
				moveNumber = (int)gameMove.getLong("moveNumber") + 1;

				if (
					player1FlipCard.equals(player2FlipCard) &&
					player1PlayCard.equals(player2PlayCard)
				) {
					gameMove = FullEntity.newBuilder(key)
						.set("moveNumber", moveNumber)
						.set("player1FlipCard", "(null)")
						.set("player1PlayCard", "(null)")
						.set("lastModified", Timestamp.now())
						.build()
					;
				} else if (
					player1FlipCard.equals(player2PlayCard) &&
					player1PlayCard.equals(player2FlipCard)
				) {
					gameMove = FullEntity.newBuilder(key)
						.set("moveNumber", moveNumber)
						.set("player2FlipCard", "(null)")
						.set("player2PlayCard", "(null)")
						.set("lastModified", Timestamp.now())
						.build()
					;
				} else {
					gameMove = FullEntity.newBuilder(key)
						.set("moveNumber", moveNumber)
						.set("lastModified", Timestamp.now())
						.build()
					;
				}

				gameMove = transaction.add(gameMove);
				transaction.commit();
			}

		} finally {
			if (transaction.isActive()) {
				//transaction.rollback();
			}
		}
	
		return (int)gameMove.getLong("moveNumber");
	}

	public String[] getOpponentMove(User user, int moveNumberIn) {
		return getOpponentMove(user.getNickname(), user.getUserId(), moveNumberIn);
	}

	public String[] getOpponentMove(String userNickname, String userID, int moveNumberIn) {
		String player = "player" + Integer.toString(getOpponent(userNickname, userID));
		return getPlayerMove(player, moveNumberIn);
	}

	public String[] getPlayerMove(User user, int moveNumberIn) {
		return getPlayerMove(user.getNickname(), user.getUserId(), moveNumberIn);
	}

	public String[] getPlayerMove(String userNickname, String userID, int moveNumberIn) {
		String player = "player" + Integer.toString(getPlayer(userNickname, userID));
		return getPlayerMove(player, moveNumberIn);
	}

	public String[] getPlayerMove(String playerIn, int moveNumberIn) {

		QueryResults<Entity> gameMoves = null;

		Entity game = null;
		Entity gameMove = null;
		String opponent = null;
		String gameMoveOut[] = {"", ""};

		Query<Entity> query = Query.newEntityQueryBuilder()
    			.setKind("GameMove")
			.setFilter(
				CompositeFilter.and(
					PropertyFilter.hasAncestor(gameKey),
					PropertyFilter.eq("moveNumber", moveNumberIn)
				)
			).setLimit(1)
    			.build()
		;

		gameMoves = datastore.run(query);
		while (gameMoves.hasNext()) {
			gameMove = gameMoves.next();
		}

		try {
			gameMoveOut[0] = gameMove.getString(playerIn + "FlipCard");
			gameMoveOut[1] = gameMove.getString(playerIn + "PlayCard");
		} catch (DatastoreException de) {
		}

		return gameMoveOut;
	}

	public int getPlayerMoveStatus(User user, int moveNumberIn) {
		String[] gameMoveOut = getPlayerMove(user, moveNumberIn);
		if (
			!gameMoveOut[0].equals("") &&
			!gameMoveOut[1].equals("")
		) {
			return 1;
		} else {
			return 0;
		}
	}

	public int getOpponentMoveStatus(User user, int moveNumberIn) {
		String[] gameMoveOut = getOpponentMove(user, moveNumberIn);
		if (
			!gameMoveOut[0].equals("") &&
			!gameMoveOut[1].equals("")
		) {
			return 1;
		} else {
			return 0;
		}
	}

	public int getPlayer(User user) {
		return getPlayer(user.getNickname(), user.getUserId());
	}

	public int getPlayer(String userNickname, String userID) {

		Entity game = datastore.get(gameKey);
		int player = 0;

		if (
			userNickname.equals(game.getString("player1Nickname")) &&
			userID.equals(game.getString("player1ID"))
		) {
			player = 1;
		} else if (
			userNickname.equals(game.getString("player2Nickname")) &&
			userID.equals(game.getString("player2ID"))
		) {
			player = 2;
		}

		return player;
	}

	public int getOpponent(User user) {
		return getOpponent(user.getNickname(), user.getUserId());
	}

	public int getOpponent(String userNickname, String userID) {
		return (getPlayer(userNickname, userID) % 2) + 1;
	}
}