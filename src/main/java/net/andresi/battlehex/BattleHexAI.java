package battlehex;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Random;

import org.apache.commons.math3.stat.StatUtils;

public class BattleHexAI {

	int monteCarloIterations = 0;
	
	interface BoardTraverse {
        	public boolean move(ArrayList<ArrayList<Object>> gameBoardIn, int currentRow, int currentColumn, int player);
		public HashMap<String, Integer> increment(int currentRow, int currentColumn);
    	}

    	private BoardTraverse[] boardTraversions = new BoardTraverse[] {
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

	public void setMonteCarloIterations(int iterationsIn) {
		monteCarloIterations = iterationsIn;
	}

	public int getMonteCarloIterations() {
		return monteCarloIterations;
	}

	public HashMap<String, Integer> calculateComputerMove(int playerIn, ArrayList<ArrayList<Integer>> gameBoardIn) {

		double maxWeight = 0.0;
		HashMap<String, Integer> moveOut = new HashMap<String, Integer>();

		ArrayList<ArrayList<HashMap<String, Object>>> moveLists = new ArrayList<ArrayList<HashMap<String, Object>>>();
		moveLists.add(normalizeAndSort(calculateMonteCarloMoves(playerIn, gameBoardIn)));

		for (int index = 0; index < moveLists.size(); index++) {

			int currentRow = (Integer)moveLists.get(index).get(0).get("row");
			int currentColumn = (Integer)moveLists.get(index).get(0).get("column");
			double currentWeight = testMoveUsingMonteCarlo(playerIn, gameBoardIn, currentRow, currentColumn);

			if (currentWeight > maxWeight) {
				moveOut.put("row", currentRow);
				moveOut.put("column", currentColumn);
			}
		}

		return moveOut;
	}

	public double testMoveUsingMonteCarlo(int playerIn, ArrayList<ArrayList<Integer>> gameBoardIn, int rowIn, int columnIn) {

		ArrayList<HashMap<String, Object>> emptyCoords = getEmptyCoords(gameBoardIn);

		int opponent = (playerIn % 2) + 1;
		int iterationsIndex = monteCarloIterations;
		double winCount = 0.0;

		while (iterationsIndex > 0) {

			ArrayList<ArrayList<Object>> randomBoard = randomizeBoardMoves(gameBoardIn, emptyCoords);

			int test1 = 0;
			int test2 = 0;

			int playerMove = (Integer)randomBoard.get(rowIn).get(columnIn);

			if (evaluateWin(playerIn, randomBoard)) {
				test1 = playerIn;
			} else if (evaluateWin(opponent, randomBoard)) {
				test1 = opponent;
			}

			playerMove = (playerMove % 2) + 1;
			randomBoard.get(rowIn).set(columnIn, playerMove);

			if (evaluateWin(playerIn, randomBoard)) {
				test2 = playerIn;
			} else if (evaluateWin(opponent, randomBoard)) {
				test2 = opponent;
			}

			if (test1 != test2) {
				winCount += 1;
			}

			iterationsIndex--;
		}

		return winCount / (double)monteCarloIterations;
	}

	public ArrayList<HashMap<String, Object>> calculateMonteCarloMoves(int playerIn, ArrayList<ArrayList<Integer>> gameBoardIn) {

		ArrayList<HashMap<String, Object>> emptyCoords = getEmptyCoords(gameBoardIn);

		int opponent = (playerIn % 2) + 1;

		int iterationsIndex = monteCarloIterations;

		while (iterationsIndex > 0) {

			ArrayList<ArrayList<Object>> randomBoard = randomizeBoardMoves(gameBoardIn, emptyCoords);

			for (int coordIndex = 0; coordIndex < emptyCoords.size(); coordIndex++) {

				int test1 = 0;
				int test2 = 0;

				int playerMove = (Integer)randomBoard.get(
					(Integer)emptyCoords.get(coordIndex).get("row")
				).get(
					(Integer)emptyCoords.get(coordIndex).get("column")
				);

				if (evaluateWin(playerIn, randomBoard)) {
					test1 = playerIn;
				} else if (evaluateWin(opponent, randomBoard)) {
					test1 = opponent;
				}

				playerMove = (playerMove % 2) + 1;
				randomBoard.get(
					(Integer)emptyCoords.get(coordIndex).get("row")
				).set(
					(Integer)emptyCoords.get(coordIndex).get("column"),
					playerMove
				);

				if (evaluateWin(playerIn, randomBoard)) {
					test2 = playerIn;
				} else if (evaluateWin(opponent, randomBoard)) {
					test2 = opponent;
				}

				if (test1 != test2) {
					double weight = (Double)emptyCoords.get(coordIndex).get("weight");
					weight += 1;
					emptyCoords.get(coordIndex).put("weight", weight);
				}
			}

			iterationsIndex--;
		}

		return emptyCoords;	
	}

	public ArrayList<HashMap<String, Object>> normalizeAndSort(ArrayList<HashMap<String, Object>> valuesMapList) {

		Comparator<HashMap<String, Object>> mapComparator = new Comparator<HashMap<String, Object>>() {
			public int compare(HashMap<String, Object> m1, HashMap<String, Object> m2) {
				return ((Double)m2.get("normalweight")).compareTo(((Double)m1.get("normalweight")));
			}
		};

		ArrayList<HashMap<String, Object>> normalizedList = valuesMapList;
		double[] weights = new double[normalizedList.size()];

		for (int index = 0; index < normalizedList.size(); index++) {
			weights[index] = (Double)normalizedList.get(index).get("weight");
		}
		weights = StatUtils.normalize(weights);
		for (int index = 0; index < normalizedList.size(); index++) {
			normalizedList.get(index).put("normalweight", weights[index]);
		}

		Collections.sort(normalizedList, mapComparator);

		return normalizedList;
	}

	private ArrayList<HashMap<String, Object>> getEmptyCoords(ArrayList<ArrayList<Integer>> gameBoardIn) {

		ArrayList<HashMap<String, Object>> emptyCoords = new ArrayList<HashMap<String, Object>>();

		for (int rowIndex = 1; rowIndex <= gameBoardIn.size()-1; rowIndex++) {
			for (int columnIndex = 1; columnIndex <= gameBoardIn.get(rowIndex).size()-1; columnIndex++) {
				if (gameBoardIn.get(rowIndex).get(columnIndex) == 0) {
					HashMap<String, Object> emptyCoord = new HashMap<String, Object>();
					emptyCoord.put("row", rowIndex);
					emptyCoord.put("column", columnIndex);
					emptyCoord.put("weight", (Double)0.0);
					emptyCoords.add(emptyCoord);
				}
			}
		}

		return emptyCoords;
	}

	private ArrayList<ArrayList<Object>> randomizeBoardMoves(ArrayList<ArrayList<Integer>> gameBoardIn, ArrayList<HashMap<String, Object>> emptyCoordsIn) {

		Random rand = new Random();

		ArrayList<ArrayList<Object>> randomBoard = new ArrayList<ArrayList<Object>>();
		Integer randomPlayer = rand.nextInt(2) + 1;
		ArrayList<HashMap<String, Object>> populateCoords = new ArrayList<HashMap<String, Object>>();

	 	for (int copyIndex1 = 0; copyIndex1 < gameBoardIn.size(); copyIndex1++) {

			ArrayList<Object> boardRow = new ArrayList<Object>();

			for (int copyIndex2 = 0; copyIndex2 < gameBoardIn.get(copyIndex1).size(); copyIndex2++) {
				boardRow.add(gameBoardIn.get(copyIndex1).get(copyIndex2));
			}

			randomBoard.add(boardRow);
	 	}
	
		for (int copyIndex = 0; copyIndex < emptyCoordsIn.size(); copyIndex++) {

			HashMap<String, Object> emptyCoord = new HashMap<String, Object>();
			int row = (Integer)emptyCoordsIn.get(copyIndex).get("row");
			int column = (Integer)emptyCoordsIn.get(copyIndex).get("column");

			emptyCoord.put("row", row);
			emptyCoord.put("column", column);
			populateCoords.add(emptyCoord);
		}

		for (int moveIndex = populateCoords.size(); moveIndex > 0; moveIndex--) {

			int randomMoveIndex = rand.nextInt(moveIndex);

			randomBoard.get(
				(Integer)populateCoords.get(randomMoveIndex).get("row")
			).set(
				(Integer)populateCoords.get(randomMoveIndex).get("column"),
				randomPlayer
			);

			populateCoords.remove(randomMoveIndex);
			randomPlayer = (randomPlayer % 2) + 1;
		}

		return randomBoard;
	}

	public ArrayList<ArrayList<Integer>> convertBoardArraysToLists(int[][] gameBoardIn) {

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

	public int[][] convertBoardListsToArrays(ArrayList<ArrayList<Integer>> gameBoardIn) {

		int[][] gameBoardOut = new int[gameBoardIn.size()][];

		for (int rowIndex = 0; rowIndex < gameBoardIn.size(); rowIndex++) {
			gameBoardOut[rowIndex] = new int[gameBoardIn.get(rowIndex).size()];
			for (int columnIndex = 0; columnIndex < gameBoardIn.get(rowIndex).size(); columnIndex++) {
				gameBoardOut[rowIndex][columnIndex] = gameBoardIn.get(rowIndex).get(columnIndex);
			}
		}

		return gameBoardOut;
	}

    	public boolean evaluateWin(int playerIn, ArrayList<ArrayList<Object>> gameBoardIn) {

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


/*
		function calculateComputerMove(playerIn, gameBoardIn) {

			let maxProbability = 0;
			let maxIndex = 2;
			let returnCoords = {};

			let computerMoveSets = [
				normalizeMoves(calculateMonteCarloMoves(playerIn, gameBoardIn))[0],
				normalizeMoves(calculateVoltageMoves(playerIn, gameBoardIn))[0],
				normalizeMoves(calculateSemiBestMoves(playerIn, gameBoardIn))[0]
			];

			for (let setIndex = 0; setIndex < computerMoveSets.length; setIndex++) {
				if (computerMoveSets[setIndex]) {
					let currentProbability = testUsingMonteCarlo(playerIn, gameBoardIn, computerMoveSets[setIndex]["row"], computerMoveSets[setIndex]["column"]);
					if (currentProbability > maxProbability) {
						maxProbability = currentProbability;
						maxIndex = setIndex;
					}
				}
			}

			console.log(maxIndex, computerMoveSets[maxIndex]["row"], computerMoveSets[maxIndex]["column"], maxProbability);

			returnCoords = {
				"row": computerMoveSets[maxIndex]["row"],
				"column": computerMoveSets[maxIndex]["column"]
			};

			return returnCoords;
		}

		function normalizeMoves(computerMovesIn) {

			let normalizedMoves = [];
			let weights = [];

			for (let moveIndex = 0; moveIndex < computerMovesIn.length; moveIndex++) {
				weights.push(computerMovesIn[moveIndex]["weight"]);
			}

			for (let moveIndex = 0; moveIndex < computerMovesIn.length; moveIndex++) {
				let moveDict = {
					"row": computerMovesIn[moveIndex]["row"],
					"column": computerMovesIn[moveIndex]["column"],
					"zscore": ((computerMovesIn[moveIndex]["weight"] - math.mean(weights)) / math.std(weights))
				}
				normalizedMoves.push(moveDict);
			}

			normalizedMoves = normalizedMoves.sort(
				function(a, b) {
					return b["zscore"] -  a["zscore"];
				}
			);

			return normalizedMoves;
		}

		function calculateSemiBestMoves(playerIn, gameBoardIn) {

			let computerMoves = [];

			let voltageBoardVectors = [null, calculateVoltages(1, gameBoardIn), calculateVoltages(2, gameBoardIn)];

			if (voltageBoardVectors[1].length && voltageBoardVectors[2].length) {
				computerMoves = getSemiBestMoves(playerIn, gameBoardIn, calculateVoltageDeltas(voltageBoardVectors[1]), calculateVoltageDeltas(voltageBoardVectors[2]));
			}

			return computerMoves;
		}

		function testUsingMonteCarlo(playerIn, gameBoardIn, rowIn, columnIn) {

			let copyBoard = [];
			let calcIndex1 = randomMoveLimit;

			let winCount = 0;

			let opponent = (playerIn % 2) + 1;

	 		for (let copyIndex = 0; copyIndex < gameBoardIn.length; copyIndex++) {
	 			copyBoard.push(Object.assign([], gameBoardIn[copyIndex]));
	 		}

			copyBoard[rowIn][columnIn] = playerIn;
			let emptyCoords = getEmptyCoords(copyBoard);

			while (calcIndex1 > 0) {

				let randomBoard = randomizeBoardMoves(copyBoard, emptyCoords, playerIn);

				let test1 = null;
				let test2 = null;

				if (evaluateWin(playerIn, randomBoard)) {
					test1 = playerIn;
				} else if (evaluateWin(opponent, randomBoard)) {
					test1 = opponent;
				}

				randomBoard[rowIn][columnIn] = opponent;

				if (evaluateWin(playerIn, randomBoard)) {
					test2 = playerIn;
				} else if (evaluateWin(opponent, randomBoard)) {
					test2 = opponent;
				}

				if (test1 != test2) {
					winCount++;
				}

				calcIndex1--;
			}
			
			return (winCount / randomMoveLimit);
		}

		function calculateMonteCarloMoves(playerIn, gameBoardIn) {

			let computerMoves = [];

			let emptyCoords = getEmptyCoords(gameBoardIn);
			let coordCounts = [];

			let opponent = (playerIn % 2) + 1;

			for (let initIndex = 0; initIndex < emptyCoords.length; initIndex++) {
				coordCounts.push(0);
			}

			let calcIndex1 = randomMoveLimit;

			while (calcIndex1 > 0) {

				let randomBoard = randomizeBoardMoves(gameBoardIn, emptyCoords);

				for (let calcIndex2 = 0; calcIndex2 < emptyCoords.length; calcIndex2++) {

					let test1 = null;
					let test2 = null;

					if (evaluateWin(playerIn, randomBoard)) {
						test1 = playerIn;
					} else if (evaluateWin(opponent, randomBoard)) {
						test1 = opponent;
					}

					randomBoard[emptyCoords[calcIndex2][0]][emptyCoords[calcIndex2][1]] = (randomBoard[emptyCoords[calcIndex2][0]][emptyCoords[calcIndex2][1]] % 2) + 1;

					if (evaluateWin(playerIn, randomBoard)) {
						test2 = playerIn;
					} else if (evaluateWin(opponent, randomBoard)) {
						test2 = opponent;
					}

					if (test1 != test2) {
						coordCounts[calcIndex2]++;
					}
				}

				calcIndex1--;
			}

			for (let checkIndex = 0; checkIndex < emptyCoords.length; checkIndex++) {
				let computerMove = {
					"row": emptyCoords[checkIndex][0],
					"column": emptyCoords[checkIndex][1],
					"weight": coordCounts[checkIndex]
				}
				computerMoves.push(computerMove);
			}

			return computerMoves;
		}

		function getEmptyCoords(gameBoardIn) {

			let emptyCoords = [];

			for (let rowIndex = 1; rowIndex <= indexValues.length; rowIndex++) {
				for (let columnIndex = 1; columnIndex <= indexValues.length; columnIndex++) {
					if (gameBoardIn[rowIndex][columnIndex] == null) {
						emptyCoords.push([rowIndex, columnIndex]);
					}
				}
			}

			return emptyCoords;
		}

	 	function randomizeBoardMoves(gameBoardIn, emptyCoordsIn, playerIn) {

	 		let randomBoard = [];
			let randomPlayer = (Math.random() < 0.5, 1, 2);
			let populateCoords = [];

			let opponent = (playerIn % 2) + 1;

			if (playerIn) {
				randomPlayer = opponent;
			}

	 		for (let copyIndex = 0; copyIndex < gameBoardIn.length; copyIndex++) {
	 			randomBoard.push(Object.assign([], gameBoardIn[copyIndex]));
	 		}

			populateCoords = Object.assign([], emptyCoordsIn);
			for (let moveIndex = populateCoords.length; moveIndex > 0; moveIndex--) {
				let moveChoice = Math.floor(Math.random() * moveIndex);
				randomBoard[populateCoords[moveChoice][0]][populateCoords[moveChoice][1]] = randomPlayer;
				populateCoords.splice(moveChoice, 1);
				randomPlayer = (randomPlayer % 2) + 1;
			}

			return randomBoard;
		}

		function calculateVoltageMoves(playerIn, gameBoardIn) {

			let computerMoves = [];

			let voltageBoardVectors = [null, calculateVoltages(1, gameBoardIn), calculateVoltages(2, gameBoardIn)];

			if (voltageBoardVectors[1].length && voltageBoardVectors[2].length) {
				computerMoves = evaluateMoves(playerIn, gameBoardIn, calculateVoltageDeltas(voltageBoardVectors[1]), calculateVoltageDeltas(voltageBoardVectors[2]));
			}

			return computerMoves;
		}

		function resetVoltageSolutions(gameBoardIn) {

			let voltageBoardSolutions = [];

			for (let rowIndex = 0; rowIndex < gameBoardIn.length; rowIndex++) {
				for (let columnIndex = 0; columnIndex < gameBoardIn[rowIndex].length; columnIndex++) {
					voltageBoardSolutions.push(0);
				}
			}

			return voltageBoardSolutions;
		}

		function resetVoltageBoards(solutionsLengthIn) {

			let voltageBoard = [];

			for (let rowIndex = 0; rowIndex < solutionsLengthIn; rowIndex++) {
				voltageBoard.push([]);
				for (let colIndex = 0; colIndex <  solutionsLengthIn; colIndex++) {
					voltageBoard[rowIndex].push(0);
				}
			}

			return voltageBoard;
		}

		function calculateVoltages(playerIn, gameBoardIn) {

			let returnVoltages = [];

			let voltageBoardSolutions = resetVoltageSolutions(gameBoardIn);
			let voltageBoards = resetVoltageBoards(voltageBoardSolutions.length);

			for (let rowIndex = 0; rowIndex < gameBoardIn.length; rowIndex++) {
				for (let columnIndex = 0; columnIndex < gameBoardIn[rowIndex].length; columnIndex++) {

					if (rowIndex == 0 && columnIndex == 0) {
						continue;
					} else if (rowIndex == indexValues.length+1 && columnIndex == indexValues.length+1) {
						continue;
					} else if (rowIndex == 0 && playerIn == 2) {
						voltageBoards[voltageBoardIndexes[rowIndex][columnIndex]][voltageBoardIndexes[rowIndex][columnIndex]] = 1;
						voltageBoardSolutions[voltageBoardIndexes[rowIndex][columnIndex]] = testVoltage;
					} else if (columnIndex == 0 && playerIn == 1) {
						voltageBoards[voltageBoardIndexes[rowIndex][columnIndex]][voltageBoardIndexes[rowIndex][columnIndex]] = 1;
						voltageBoardSolutions[voltageBoardIndexes[rowIndex][columnIndex]] = testVoltage;
					} else if (rowIndex == 0 && playerIn == 1) {
						voltageBoards[voltageBoardIndexes[rowIndex][columnIndex]][voltageBoardIndexes[rowIndex][columnIndex]] = 1;
						voltageBoardSolutions[voltageBoardIndexes[rowIndex][columnIndex]] = 0;
					} else if (columnIndex == 0 && playerIn == 2) {
						voltageBoards[voltageBoardIndexes[rowIndex][columnIndex]][voltageBoardIndexes[rowIndex][columnIndex]] = 1;
						voltageBoardSolutions[voltageBoardIndexes[rowIndex][columnIndex]] = 0;
					} else if (rowIndex == gameBoardIn.length-1 || columnIndex == gameBoardIn[rowIndex].length-1) {
						voltageBoards[playerIn][voltageBoardIndexes[rowIndex][columnIndex]][voltageBoardIndexes[rowIndex][columnIndex]] = 1;
						voltageBoardSolutions[voltageBoardIndexes[rowIndex][columnIndex]] = 0;
					} else {

						let connCount = 0;
						let sameCount = 0;
						let signCount = -1;

						if (getVoltageRelationship(rowIndex, columnIndex, rowIndex-1, columnIndex, gameBoardIn, playerIn) == 2) {
							sameCount++;
						}
						if (getVoltageRelationship(rowIndex, columnIndex, rowIndex-1, columnIndex+1, gameBoardIn, playerIn) == 2) {
							sameCount++;
						}
						if (getVoltageRelationship(rowIndex, columnIndex, rowIndex, columnIndex-1, gameBoardIn, playerIn) == 2) {
							sameCount++;
						}
						if (getVoltageRelationship(rowIndex, columnIndex, rowIndex, columnIndex+1, gameBoardIn, playerIn) == 2) {
							sameCount++;
						}
						if (getVoltageRelationship(rowIndex, columnIndex, rowIndex+1, columnIndex-1, gameBoardIn, playerIn) == 2) {
							sameCount++;
						}
						if (getVoltageRelationship(rowIndex, columnIndex, rowIndex+1, columnIndex, gameBoardIn, playerIn) == 2) {
							sameCount++;
						}

						if (getVoltageRelationship(rowIndex, columnIndex, rowIndex-1, columnIndex, gameBoardIn, playerIn) == 2) {
							voltageBoards[voltageBoardIndexes[rowIndex][columnIndex]][voltageBoardIndexes[rowIndex-1][columnIndex]] = -2/sameCount;
						}
						if (getVoltageRelationship(rowIndex, columnIndex, rowIndex-1, columnIndex+1, gameBoardIn, playerIn) == 2) {
							voltageBoards[voltageBoardIndexes[rowIndex][columnIndex]][voltageBoardIndexes[rowIndex-1][columnIndex+1]] -2/sameCount;
						}
						if (getVoltageRelationship(rowIndex, columnIndex, rowIndex, columnIndex-1, gameBoardIn, playerIn) == 2) {
							voltageBoards[voltageBoardIndexes[rowIndex][columnIndex]][voltageBoardIndexes[rowIndex][columnIndex-1]] = -2/sameCount;
						}
						if (getVoltageRelationship(rowIndex, columnIndex, rowIndex, columnIndex+1, gameBoardIn, playerIn) == 2) {
							voltageBoards[voltageBoardIndexes[rowIndex][columnIndex]][voltageBoardIndexes[rowIndex][columnIndex+1]] = -2/sameCount;
						}
						if (getVoltageRelationship(rowIndex, columnIndex, rowIndex+1, columnIndex-1, gameBoardIn, playerIn) == 2) {
							voltageBoards[voltageBoardIndexes[rowIndex][columnIndex]][voltageBoardIndexes[rowIndex+1][columnIndex-1]] = -2/sameCount;
						}
						if (getVoltageRelationship(rowIndex, columnIndex, rowIndex+1, columnIndex, gameBoardIn, playerIn) == 2) {
							voltageBoards[voltageBoardIndexes[rowIndex][columnIndex]][voltageBoardIndexes[rowIndex+1][columnIndex]] = -2/sameCount;
						}

						if (sameCount) {
							signCount = 1;
						}

						if ([1, 3].indexOf(getVoltageRelationship(rowIndex, columnIndex, rowIndex-1, columnIndex, gameBoardIn, playerIn)) > -1) {
							voltageBoards[voltageBoardIndexes[rowIndex][columnIndex]][voltageBoardIndexes[rowIndex-1][columnIndex]] = signCount;
							connCount++;
						}
						if ([1, 3].indexOf(getVoltageRelationship(rowIndex, columnIndex, rowIndex-1, columnIndex+1, gameBoardIn, playerIn)) > -1) {
							voltageBoards[voltageBoardIndexes[rowIndex][columnIndex]][voltageBoardIndexes[rowIndex-1][columnIndex+1]] = signCount;
							connCount++;
						}
						if ([1, 3].indexOf(getVoltageRelationship(rowIndex, columnIndex, rowIndex, columnIndex-1, gameBoardIn, playerIn)) > -1) {
							voltageBoards[voltageBoardIndexes[rowIndex][columnIndex]][voltageBoardIndexes[rowIndex][columnIndex-1]] = signCount;
							connCount++;
						}
						if ([1, 3].indexOf(getVoltageRelationship(rowIndex, columnIndex, rowIndex, columnIndex+1, gameBoardIn, playerIn)) > -1) {
							voltageBoards[voltageBoardIndexes[rowIndex][columnIndex]][voltageBoardIndexes[rowIndex][columnIndex+1]] = signCount;
							connCount++;
						}
						if ([1, 3].indexOf(getVoltageRelationship(rowIndex, columnIndex, rowIndex+1, columnIndex-1, gameBoardIn, playerIn)) > -1) {
							voltageBoards[voltageBoardIndexes[rowIndex][columnIndex]][voltageBoardIndexes[rowIndex+1][columnIndex-1]] = signCount;
							connCount++;
						}
						if ([1, 3].indexOf(getVoltageRelationship(rowIndex, columnIndex, rowIndex+1, columnIndex, gameBoardIn, playerIn)) > -1) {
							voltageBoards[voltageBoardIndexes[rowIndex][columnIndex]][voltageBoardIndexes[rowIndex+1][columnIndex]] = signCount;
							connCount++;
						}

						voltageBoards[voltageBoardIndexes[rowIndex][columnIndex]][voltageBoardIndexes[rowIndex][columnIndex]] = connCount;
						voltageBoardSolutions[voltageBoardIndexes[rowIndex][columnIndex]] = 0;
					}
				}
			}

			try {
				let sparseVoltageMatrix = numeric.ccsSparse(voltageBoards);
				returnVoltages = numeric.ccsLUPSolve(numeric.ccsLUP(sparseVoltageMatrix), voltageBoardSolutions);
			} catch (err) {
				console.log(err);
			}
			
			return returnVoltages;
		}

		function getVoltageRelationship(refRow, refCol, calcRow, calcCol, gameBoardIn, playerIn) {

			if (gameBoardIn[refRow][refCol] == null && gameBoardIn[calcRow][calcCol] == playerIn) {
				return 1;
			} else if (gameBoardIn[refRow][refCol] == playerIn && gameBoardIn[calcRow][calcCol] == null) {
				return 1;
			} else if (gameBoardIn[refRow][refCol] == null && gameBoardIn[calcRow][calcCol] == 0) {
				return 1;
			} else if (gameBoardIn[refRow][refCol] == 0 && gameBoardIn[calcRow][calcCol] == null) {
				return 1;
			} else if (gameBoardIn[refRow][refCol] == playerIn && gameBoardIn[calcRow][calcCol] == playerIn) {
				return 2;
			} else if (gameBoardIn[refRow][refCol] == 0 && gameBoardIn[calcRow][calcCol] == playerIn) {
				return 2;
			} else if (gameBoardIn[refRow][refCol] == playerIn && gameBoardIn[calcRow][calcCol] == 0) {
				return 2;
			} else if (gameBoardIn[refRow][refCol] == null && gameBoardIn[calcRow][calcCol] == null) {
				return 3;
			}

			return 0;
		}

		function calculateVoltageDeltas(voltageBoardVectors) {

			let deltasOut = [];

			for (let rowIndex = 0; rowIndex < voltageBoardVectors.length; rowIndex++) {
				deltasOut.push([]);
				for (let columnIndex = 0; columnIndex < voltageBoardVectors.length; columnIndex++) {
					deltasOut[rowIndex].push(0);
				}
			}

			for (let rowIndex = 1; rowIndex <= indexValues.length; rowIndex++) {
				for (let columnIndex = 1; columnIndex <= indexValues.length; columnIndex++) {
					deltasOut[voltageBoardIndexes[rowIndex][columnIndex]][voltageBoardIndexes[rowIndex-1][columnIndex]] = voltageBoardVectors[voltageBoardIndexes[rowIndex][columnIndex]] - voltageBoardVectors[voltageBoardIndexes[rowIndex-1][columnIndex]];
					deltasOut[voltageBoardIndexes[rowIndex][columnIndex]][voltageBoardIndexes[rowIndex-1][columnIndex+1]] = voltageBoardVectors[voltageBoardIndexes[rowIndex][columnIndex]] - voltageBoardVectors[voltageBoardIndexes[rowIndex-1][columnIndex+1]];
					deltasOut[voltageBoardIndexes[rowIndex][columnIndex]][voltageBoardIndexes[rowIndex][columnIndex-1]] = voltageBoardVectors[voltageBoardIndexes[rowIndex][columnIndex]] - voltageBoardVectors[voltageBoardIndexes[rowIndex][columnIndex-1]];
					deltasOut[voltageBoardIndexes[rowIndex][columnIndex]][voltageBoardIndexes[rowIndex][columnIndex+1]] = voltageBoardVectors[voltageBoardIndexes[rowIndex][columnIndex]] - voltageBoardVectors[voltageBoardIndexes[rowIndex][columnIndex+1]];
					deltasOut[voltageBoardIndexes[rowIndex][columnIndex]][voltageBoardIndexes[rowIndex+1][columnIndex-1]] = voltageBoardVectors[voltageBoardIndexes[rowIndex][columnIndex]] - voltageBoardVectors[voltageBoardIndexes[rowIndex+1][columnIndex-1]];
					deltasOut[voltageBoardIndexes[rowIndex][columnIndex]][voltageBoardIndexes[rowIndex+1][columnIndex]] = voltageBoardVectors[voltageBoardIndexes[rowIndex][columnIndex]] - voltageBoardVectors[voltageBoardIndexes[rowIndex+1][columnIndex]];
				}
			}

			return deltasOut;
		}

		function evaluateMoveDeltas(rowIndexIn, columnIndexIn, playerIn, deltasIn) {

			let moveEval = 0;

			if ([1, 3].indexOf(getVoltageRelationship(rowIndexIn, columnIndexIn, rowIndexIn-1, columnIndexIn, gameBoard, playerIn)) > -1) {
				moveEval += deltasIn[voltageBoardIndexes[rowIndexIn][columnIndexIn]][voltageBoardIndexes[rowIndexIn-1][columnIndexIn]];
			} else if ([1, 3].indexOf(getVoltageRelationship(rowIndexIn, columnIndexIn, rowIndexIn-1, columnIndexIn+1, gameBoard, playerIn)) > -1) {
				moveEval += deltasIn[voltageBoardIndexes[rowIndexIn][columnIndexIn]][voltageBoardIndexes[rowIndexIn-1][columnIndexIn+1]];
			} else if ([1, 3].indexOf(getVoltageRelationship(rowIndexIn, columnIndexIn, rowIndexIn, columnIndexIn-1, gameBoard, playerIn)) > -1) {
				moveEval += deltasIn[voltageBoardIndexes[rowIndexIn][columnIndexIn]][voltageBoardIndexes[rowIndexIn][columnIndexIn-1]];
			} else if ([1, 3].indexOf(getVoltageRelationship(rowIndexIn, columnIndexIn, rowIndexIn, columnIndexIn+1, gameBoard, playerIn)) > -1) {
				moveEval += deltasIn[voltageBoardIndexes[rowIndexIn][columnIndexIn]][voltageBoardIndexes[rowIndexIn][columnIndexIn+1]];
			} else if ([1, 3].indexOf(getVoltageRelationship(rowIndexIn, columnIndexIn, rowIndexIn+1, columnIndexIn-1, gameBoard, playerIn)) > -1) {
				moveEval += deltasIn[voltageBoardIndexes[rowIndexIn][columnIndexIn]][voltageBoardIndexes[rowIndexIn+1][columnIndexIn-1]];
			} else if ([1, 3].indexOf(getVoltageRelationship(rowIndexIn, columnIndexIn, rowIndexIn+1, columnIndexIn, gameBoard, playerIn)) > -1) {
				moveEval += deltasIn[voltageBoardIndexes[rowIndexIn][columnIndexIn]][voltageBoardIndexes[rowIndexIn+1][columnIndexIn]];
			}

			return moveEval;
		}

		function moveBordersOpponent(rowIndexIn, columnIndexIn, gameStateIn, playerIn) {

			let moveEval = false;
			let opponent = (playerIn % 2) + 1;

			if (
				0 >= rowIndexIn-1 ||
				gameStateIn.length <= rowIndexIn+1 ||
				0 >= columnIndexIn-1 ||
				gameStateIn.length <= columnIndexIn+1
			) {
				moveEval = false;
			} else if (!getVoltageRelationship(rowIndexIn, columnIndexIn, rowIndexIn-1, columnIndexIn, gameStateIn, playerIn) && getVoltageRelationship(rowIndexIn, columnIndexIn, rowIndexIn -1, columnIndexIn, gameStateIn, opponent)) {
				moveEval = true;
			} else if (!getVoltageRelationship(rowIndexIn, columnIndexIn, rowIndexIn-1, columnIndexIn+1, gameStateIn, playerIn) && getVoltageRelationship(rowIndexIn, columnIndexIn, rowIndexIn-1, columnIndexIn+1, gameStateIn, opponent)) {
				moveEval = true;
			} else if (!getVoltageRelationship(rowIndexIn, columnIndexIn, rowIndexIn, columnIndexIn-1, gameStateIn, playerIn) && getVoltageRelationship(rowIndexIn, columnIndexIn, rowIndexIn, columnIndexIn-1, gameStateIn, opponent)) {
				moveEval = true;
			} else if (!getVoltageRelationship(rowIndexIn, columnIndexIn, rowIndexIn, columnIndexIn+1, gameStateIn, playerIn) && getVoltageRelationship(rowIndexIn, columnIndexIn, rowIndexIn, columnIndexIn+1, gameStateIn, opponent)) {
				moveEval = true;
			} else if (!getVoltageRelationship(rowIndexIn, columnIndexIn, rowIndexIn+1, columnIndexIn-1, gameStateIn, playerIn) && getVoltageRelationship(rowIndexIn, columnIndexIn, rowIndexIn+1, columnIndexIn-1, gameStateIn, opponent)) {
				moveEval = true;
			} else if (!getVoltageRelationship(rowIndexIn, columnIndexIn, rowIndexIn+1, columnIndexIn, gameStateIn, playerIn) && getVoltageRelationship(rowIndexIn, columnIndexIn, rowIndexIn+1, columnIndexIn, gameStateIn, opponent)) {
				moveEval = true;
			}

			return moveEval;
		}

		function moveFitsOpponentDiamond(rowIndexIn, columnIndexIn, gameStateIn, playerIn) {

			let moveEval = false;
			let opponent = (playerIn % 2) + 1;

			if (
				0 >= rowIndexIn-2 ||
				gameStateIn.length <= rowIndexIn+2 ||
				0 >= columnIndexIn-2 ||
				gameStateIn.length <= columnIndexIn+2
			) {
				moveEval = false;
			} else if (
				getVoltageRelationship(rowIndexIn, columnIndexIn, rowIndexIn+1, columnIndexIn+1, gameStateIn, opponent) == 1 &&
				getVoltageRelationship(rowIndexIn, columnIndexIn, rowIndexIn+1, columnIndexIn, gameStateIn, opponent) == 1 &&
				getVoltageRelationship(rowIndexIn, columnIndexIn, rowIndexIn, columnIndexIn+1, gameStateIn, opponent) == 1
			) {
				moveEval = true;
			} else if (
				getVoltageRelationship(rowIndexIn, columnIndexIn, rowIndexIn-1, columnIndexIn-1, gameStateIn, opponent) == 1 &&
				getVoltageRelationship(rowIndexIn, columnIndexIn, rowIndexIn-1, columnIndexIn, gameStateIn, opponent) == 1 &&
				getVoltageRelationship(rowIndexIn, columnIndexIn, rowIndexIn, columnIndexIn-1, gameStateIn, opponent) == 1
			) {
				moveEval = true;
			} else if (
				getVoltageRelationship(rowIndexIn, columnIndexIn, rowIndexIn-1, columnIndexIn+2, gameStateIn, opponent) == 1 &&
				getVoltageRelationship(rowIndexIn, columnIndexIn, rowIndexIn, columnIndexIn+1, gameStateIn, opponent) == 1 &&
				getVoltageRelationship(rowIndexIn, columnIndexIn, rowIndexIn-1, columnIndexIn+1, gameStateIn, opponent) == 1
			) {
				moveEval = true;
			} else if (
				getVoltageRelationship(rowIndexIn, columnIndexIn, rowIndexIn+1, columnIndexIn-2, gameStateIn, opponent) == 1 &&
				getVoltageRelationship(rowIndexIn, columnIndexIn, rowIndexIn, columnIndexIn-1, gameStateIn, opponent) == 1 &&
				getVoltageRelationship(rowIndexIn, columnIndexIn, rowIndexIn+1, columnIndexIn-1, gameStateIn, opponent) == 1
			) {
				moveEval = true;
			} else if (
				getVoltageRelationship(rowIndexIn, columnIndexIn, rowIndexIn+2, columnIndexIn-1, gameStateIn, opponent) == 1 &&
				getVoltageRelationship(rowIndexIn, columnIndexIn, rowIndexIn+1, columnIndexIn, gameStateIn, opponent) == 1 &&
				getVoltageRelationship(rowIndexIn, columnIndexIn, rowIndexIn+1, columnIndexIn-1, gameStateIn, opponent) == 1
			) {
				moveEval = true;
			} else if (
				getVoltageRelationship(rowIndexIn, columnIndexIn, rowIndexIn-2, columnIndexIn+1, gameStateIn, opponent) == 1 &&
				getVoltageRelationship(rowIndexIn, columnIndexIn, rowIndexIn-1, columnIndexIn, gameStateIn, opponent) == 1 &&
				getVoltageRelationship(rowIndexIn, columnIndexIn, rowIndexIn-1, columnIndexIn+1, gameStateIn, opponent) == 1
			) {
				moveEval = true;
			}

			return moveEval;
		}

		function moveFitsEstablishedDiamond(rowIndexIn, columnIndexIn, gameStateIn, playerIn) {

			let moveEval = [];

			if (
				0 >= rowIndexIn-2 ||
				gameStateIn.length <= rowIndexIn+2 ||
				0 >= columnIndexIn-2 ||
				gameStateIn.length <= columnIndexIn+2
			) {
				moveEval = [];
			} else if (
				getVoltageRelationship(rowIndexIn, columnIndexIn, rowIndexIn+1, columnIndexIn+1, gameStateIn, playerIn) == 2 &&
				getVoltageRelationship(rowIndexIn, columnIndexIn, rowIndexIn+1, columnIndexIn, gameStateIn, playerIn) == 1 &&
				getVoltageRelationship(rowIndexIn, columnIndexIn, rowIndexIn, columnIndexIn+1, gameStateIn, playerIn) == 1
			) {
				moveEval.push({"row" : rowIndexIn+1, "column" : columnIndexIn});
				moveEval.push({"row" : rowIndexIn, "column" : columnIndexIn+1});
			} else if (
				getVoltageRelationship(rowIndexIn, columnIndexIn, rowIndexIn-1, columnIndexIn-1, gameStateIn, playerIn) == 2 &&
				getVoltageRelationship(rowIndexIn, columnIndexIn, rowIndexIn-1, columnIndexIn, gameStateIn, playerIn) == 1 &&
				getVoltageRelationship(rowIndexIn, columnIndexIn, rowIndexIn, columnIndexIn-1, gameStateIn, playerIn) == 1) {
				moveEval.push({"row" : rowIndexIn-1, "column" : columnIndexIn});
				moveEval.push({"row" : rowIndexIn, "column" : columnIndexIn-1});
			} else if (
				getVoltageRelationship(rowIndexIn, columnIndexIn, rowIndexIn-1, columnIndexIn+2, gameStateIn, playerIn) == 2 &&
				getVoltageRelationship(rowIndexIn, columnIndexIn, rowIndexIn, columnIndexIn+1, gameStateIn, playerIn) == 1 &&
				getVoltageRelationship(rowIndexIn, columnIndexIn, rowIndexIn-1, columnIndexIn+1, gameStateIn, playerIn) == 1
			) {
				moveEval.push({"row" : rowIndexIn, "column" : columnIndexIn+1});
				moveEval.push({"row" : rowIndexIn-1, "column" : columnIndexIn+1});
			} else if (
				getVoltageRelationship(rowIndexIn, columnIndexIn, rowIndexIn+1, columnIndexIn-2, gameStateIn, playerIn) == 2 &&
				getVoltageRelationship(rowIndexIn, columnIndexIn, rowIndexIn, columnIndexIn-1, gameStateIn, playerIn) == 1 &&
				getVoltageRelationship(rowIndexIn, columnIndexIn, rowIndexIn+1, columnIndexIn-1, gameStateIn, playerIn) == 1
			) {
				moveEval.push({"row" : rowIndexIn, "column" : columnIndexIn-1});
				moveEval.push({"row" : rowIndexIn+1, "column" : columnIndexIn-1});
			} else if (
				getVoltageRelationship(rowIndexIn, columnIndexIn, rowIndexIn+2, columnIndexIn-1, gameStateIn, playerIn) == 2 &&
				getVoltageRelationship(rowIndexIn, columnIndexIn, rowIndexIn+1, columnIndexIn, gameStateIn, playerIn) == 1 &&
				getVoltageRelationship(rowIndexIn, columnIndexIn, rowIndexIn+1, columnIndexIn-1, gameStateIn, playerIn) == 1
			) {
				moveEval.push({"row" : rowIndexIn+1, "column" : columnIndexIn});
				moveEval.push({"row" : rowIndexIn+1, "column" : columnIndexIn-1});
			} else if (
				getVoltageRelationship(rowIndexIn, columnIndexIn, rowIndexIn-2, columnIndexIn+1, gameStateIn, playerIn) == 2 &&
				getVoltageRelationship(rowIndexIn, columnIndexIn, rowIndexIn-1, columnIndexIn, gameStateIn, playerIn) == 1 &&
				getVoltageRelationship(rowIndexIn, columnIndexIn, rowIndexIn-1, columnIndexIn+1, gameStateIn, playerIn) == 1
			) {
				moveEval.push({"row" : rowIndexIn-1, "column" : columnIndexIn});
				moveEval.push({"row" : rowIndexIn-1, "column" : columnIndexIn+1});
			}

			return moveEval;
		}

		function moveAdvantageousOpponent(rowIndexIn, columnIndexIn, gameStateIn, playerIn) {

			let moveEval = false;

			if (moveFitsOpponentDiamond(rowIndexIn, columnIndexIn, gameStateIn, playerIn)) {
				moveEval = true;
			} else if (moveBordersOpponent(rowIndexIn, columnIndexIn, gameStateIn, playerIn)) {
				moveEval = true;
			}

			return moveEval;
		}

		function getDefensiveMoves(playerIn, gameStateIn, playerFactorsIn, opponentFactorsIn) {

			let opponent = (playerIn % 2) + 1;
			
			let playerMovesReturn = [];

			if (!playerMovesReturn.length) {
				for (let rowIndex = 0; rowIndex < gameStateIn.length; rowIndex++) {
					for (let columnIndex = 0; columnIndex < gameStateIn[rowIndex].length; columnIndex++) {
						if (gameStateIn[rowIndex][columnIndex] == opponent) {
							let diamonds = moveFitsEstablishedDiamond(rowIndex, columnIndex, gameStateIn, opponent);
							if (diamonds.length) {
								for (let moveIndex = 0; moveIndex < diamonds.length; moveIndex+=2) {
									if (gameStateIn[diamonds[moveIndex]["row"]][diamonds[moveIndex]["column"]] == null && gameStateIn[diamonds[moveIndex+1]["row"]][diamonds[moveIndex+1]["column"]] == null) {
										playerMovesReturn.push({
											"playerFactor" : playerFactorsIn[diamonds[moveIndex]["row"]][diamonds[moveIndex]["column"]],
											"opponentFactor" : opponentFactorsIn[diamonds[moveIndex]["row"]][diamonds[moveIndex]["column"]],
											"normPlayerFactor" : null,
											"normOpponentFactor" : null,
											"weight" : null,
											"row" : diamonds[moveIndex]["row"]+1,
											"column" : diamonds[moveIndex]["column"]+1
										});
										playerMovesReturn.push({
											"playerFactor" : playerFactorsIn[diamonds[moveIndex+1]["row"]][diamonds[moveIndex+1]["column"]],
											"opponentFactor" : opponentFactorsIn[diamonds[moveIndex+1]["row"]][diamonds[moveIndex+1]["column"]],
											"normPlayerFactor" : null,
											"normOpponentFactor" : null,
											"weight" : null,
											"row" : diamonds[moveIndex+1]["row"]+1,
											"column" : diamonds[moveIndex+1]["column"]+1
										});
									}
								}
							}
						}
					}
				}
			}

			for (let rowIndex = 0; rowIndex < gameStateIn.length; rowIndex++) {
				for (let columnIndex = 0; columnIndex < gameStateIn[rowIndex].length; columnIndex++) {
					if (gameStateIn[rowIndex][columnIndex] == null) {
						if (moveAdvantageousOpponent(rowIndex, columnIndex, gameStateIn, playerIn)) {
							playerMovesReturn.push({
								"playerFactor" : playerFactorsIn[rowIndex][columnIndex],
								"opponentFactor" : opponentFactorsIn[rowIndex][columnIndex],
								"normPlayerFactor" : null,
								"normOpponentFactor" : null,
								"weight" : null,
								"row" : rowIndex+1,
								"column" : columnIndex+1
							});
						}
					}
				}
			}

			return playerMovesReturn;
		}

		function setupMovesReturn(moveReturnIn) {

			let moveReturnOut = moveReturnIn;

			let playerMoveCount = 0;
			let playerMovesSum = 0;
			let playerMaxFactor = 0;

			let opponentMoveCount = 0;
			let opponentMovesSum = 0;
			let opponentMaxFactor = 0;

			let ratioNormCount = 0;
			let ratioNormsSum = 0;

			for (let moveIndex = 0; moveIndex < moveReturnOut.length; moveIndex++) {
				if (moveReturnOut[moveIndex]["playerFactor"] != null) {
					playerMoveCount++;
					playerMovesSum += moveReturnOut[moveIndex]["playerFactor"];
					playerMaxFactor = (playerMaxFactor < moveReturnOut[moveIndex]["playerFactor"] ? moveReturnOut[moveIndex]["playerFactor"] : playerMaxFactor);
				}
				if (moveReturnOut[moveIndex]["opponentFactor"] != null) {
					opponentMoveCount++;
					opponentMovesSum += moveReturnOut[moveIndex]["opponentFactor"];
					opponentMaxFactor = (opponentMaxFactor < moveReturnOut[moveIndex]["opponentFactor"] ? moveReturnOut[moveIndex]["opponentFactor"] : opponentMaxFactor);
				}
			}

			for (let moveIndex = 0; moveIndex < moveReturnOut.length; moveIndex++) {

				if (moveReturnOut[moveIndex]["playerFactor"] != null && moveReturnOut[moveIndex]["opponentFactor"] != null) {
					moveReturnOut[moveIndex]["normPlayerFactor"] = moveReturnOut[moveIndex]["playerFactor"]*playerMoveCount/playerMovesSum;
					moveReturnOut[moveIndex]["normOpponentFactor"] = moveReturnOut[moveIndex]["opponentFactor"]*opponentMoveCount/opponentMovesSum;
					moveReturnOut[moveIndex]["weight"] = moveReturnOut[moveIndex]["normPlayerFactor"]/moveReturnOut[moveIndex]["normOpponentFactor"];
					ratioNormCount++;
					ratioNormsSum += moveReturnOut[moveIndex]["weight"];
				}

				if (moveReturnOut[moveIndex]["playerFactor"] == null) {
					moveReturnOut[moveIndex]["playerFactor"] = playerMaxFactor;
				}
				if (moveReturnOut[moveIndex]["opponentFactor"] == null) {
					moveReturnOut[moveIndex]["opponentFactor"] = opponentMaxFactor;
				}
				if (moveReturnOut[moveIndex]["weight"] == null) {
					moveReturnOut[moveIndex]["weight"] = 0;
				}
			}

			moveReturnOut.sort(
				function(a, b) {
					return a["weight"] - b["weight"];
				}
			);

			return moveReturnOut;
		}

		function evaluateMoves(playerIn, gameBoardIn, playerDeltas, opponentDeltas) {

			let opponent = (playerIn % 2) + 1;

			let gameState = [];

			let playerFactors = [];
			let opponentFactors = [];

			let playerMoves = [];
			let playerMoveReturn = null;
			let playerMovesOut = [];

			let playerEvals = [];
			let opponentEvals = [];

			for (let rowIndex = 1; rowIndex <= indexValues.length; rowIndex++) {
				playerFactors.push([]);
				opponentFactors.push([]);
				gameState.push([]);
				for (let columnIndex = 1; columnIndex <= indexValues.length; columnIndex++) {
					playerFactors[rowIndex-1].push(null);
					opponentFactors[rowIndex-1].push(null);
					gameState[rowIndex-1].push(null);
					if (gameBoardIn[rowIndex][columnIndex] == null) {

						playerEvals.push(evaluateMoveDeltas(rowIndex, columnIndex, playerIn, playerDeltas));
						opponentEvals.push(evaluateMoveDeltas(rowIndex, columnIndex, opponent, opponentDeltas));

						playerEvals[playerEvals.length-1] *= playerEvals[playerEvals.length-1];
						playerEvals[playerEvals.length-1] /= 6;
						playerEvals[playerEvals.length-1] = Math.sqrt(playerEvals[playerEvals.length-1]);

						opponentEvals[opponentEvals.length-1] *= opponentEvals[opponentEvals.length-1];
						opponentEvals[opponentEvals.length-1] /= 6;
						opponentEvals[opponentEvals.length-1] = Math.sqrt(opponentEvals[opponentEvals.length-1]);

						playerFactors[rowIndex-1][columnIndex-1] = (playerEvals[playerEvals.length-1] ? opponentEvals[opponentEvals.length-1]/playerEvals[playerEvals.length-1] : null);
						opponentFactors[rowIndex-1][columnIndex-1] = (opponentEvals[opponentEvals.length-1] ? playerEvals[playerEvals.length-1]/opponentEvals[opponentEvals.length-1] : null);

						playerMoves.push({
							"playerFactor" : playerFactors[rowIndex-1][columnIndex-1],
							"opponentFactor" : opponentFactors[rowIndex-1][columnIndex-1],
							"normPlayerFactor" : null,
							"normOpponentFactor" : null,
							"weight" : null,
							"row" : rowIndex,
							"column" : columnIndex
						});

					} else {
						gameState[rowIndex-1][columnIndex-1] = gameBoardIn[rowIndex][columnIndex];
					}
				}
			}

			playerMovesOut = setupMovesReturn(playerMoves);

			return playerMovesOut;
		}

		function getSemiBestMoves(playerIn, gameBoardIn, playerDeltas, opponentDeltas) {

			let opponent = (playerIn % 2) + 1;

			let gameState = [];

			let playerFactors = [];
			let opponentFactors = [];

			let playerMoves = [];
			let playerMoveReturn = null;
			let playerMovesOut = [];

			let playerEvals = [];
			let opponentEvals = [];

			for (let rowIndex = 1; rowIndex <= indexValues.length; rowIndex++) {
				playerFactors.push([]);
				opponentFactors.push([]);
				gameState.push([]);
				for (let columnIndex = 1; columnIndex <= indexValues.length; columnIndex++) {
					playerFactors[rowIndex-1].push(null);
					opponentFactors[rowIndex-1].push(null);
					gameState[rowIndex-1].push(null);
					if (gameBoardIn[rowIndex][columnIndex] == null) {

						playerEvals.push(evaluateMoveDeltas(rowIndex, columnIndex, playerIn, playerDeltas));
						opponentEvals.push(evaluateMoveDeltas(rowIndex, columnIndex, opponent, opponentDeltas));

						playerEvals[playerEvals.length-1] *= playerEvals[playerEvals.length-1];
						playerEvals[playerEvals.length-1] /= 6;
						playerEvals[playerEvals.length-1] = Math.sqrt(playerEvals[playerEvals.length-1]);

						opponentEvals[opponentEvals.length-1] *= opponentEvals[opponentEvals.length-1];
						opponentEvals[opponentEvals.length-1] /= 6;
						opponentEvals[opponentEvals.length-1] = Math.sqrt(opponentEvals[opponentEvals.length-1]);

						playerFactors[rowIndex-1][columnIndex-1] = (playerEvals[playerEvals.length-1] ? opponentEvals[opponentEvals.length-1]/playerEvals[playerEvals.length-1] : null);
						opponentFactors[rowIndex-1][columnIndex-1] = (opponentEvals[opponentEvals.length-1] ? playerEvals[playerEvals.length-1]/opponentEvals[opponentEvals.length-1] : null);

					} else {
						gameState[rowIndex-1][columnIndex-1] = gameBoardIn[rowIndex][columnIndex];
					}
				}
			}

			defensiveMoves = setupMovesReturn(getDefensiveMoves(playerIn, gameState, playerFactors, opponentFactors));

			return defensiveMoves;
		}
*/

}