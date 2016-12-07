
/*
 * File: Yahtzee.java
 * ------------------
 * This program let's you play the Yahtzee game.
 * by Michael Francoeur
 */

import java.util.*;
import acm.io.*;
import acm.program.*;
import acm.util.*;

public class Yahtzee extends GraphicsProgram implements YahtzeeConstants {

	public static void main(String[] args) {
		new Yahtzee().start(args);
	}

	public void run() {
		IODialog dialog = getDialog();
		nPlayers = dialog.readInt("Enter number of players");
		while (nPlayers > 4) {
			nPlayers = dialog.readInt("Sorry, the max number of players is 4. Enter number of players");
		}

		/* creates an multidimensional array the size of nPlayers x 17 */
		scoresArray = new int[nPlayers][N_CATEGORIES];

		/*
		 * creates a multidimensional array the size of nPlayers x N_CATEGORIES
		 * to keep track of which categories a player has selected. A player
		 * cannot select the same category multiple times
		 */
		filledCategoriesArray = new int[nPlayers][N_CATEGORIES];

		/* creates an array of player names */
		playerNames = new String[nPlayers];
		for (int i = 1; i <= nPlayers; i++) {
			playerNames[i - 1] = dialog.readLine("Enter name for player " + i);
		}

		display = new YahtzeeDisplay(getGCanvas(), playerNames);

		/* sets the game into motion */
		playGame();
	}

	private void playGame() {
		/* creates an array of dice, the size of N_DICE */
		dice = new int[N_DICE];
		index = new ArrayList<Integer>();

		for (int round = 0; round < 13; round++) {
			// iterates through 13 rounds

			for (int player = 0; player < playerNames.length; player++) {
				// gives each player one turn per round

				currentPlayerName = playerNames[player];
				currentPlayerIndex = player;
				currentPlayer = player + 1;

				for (int turn = 0; turn < 3; turn++) {
					// each turn consists of three rolls
					if (turn == 0) {
						displayMessage(turn);
						display.waitForPlayerToClickRoll(player + 1);
						rollDie(); // method to generate random ints for all die
						display.displayDice(dice);
					}
					if (turn > 0) {
						displayMessage(turn);
						display.waitForPlayerToSelectDice();
						checkForSelectedDice();
						rollSelectedDie();
						display.displayDice(dice);
					}
				}
				selectCategory();

			}
		}
		/* compute final scores and bonuses */
		tallyUpperScores();
		tallyLowerScores();
		awardUpperBonus();
		tallyTotalScores();
		determineWinner();
	}

	private void selectCategory() {
		displayMessage(3);
		int category;

		/*
		 * checks to see if category has been selected in prior turn, if it has
		 * prompts player to select a different category
		 */
		while (true) {
			category = display.waitForPlayerToSelectCategory();

			if (isCategoryOpen(category)) {
				break;
			} else {
				display.printMessage("You've selected this category before, dummy! Try a different category.");
			}
		}
		/* checks if the roll is valid for specified category */
		if (isCategoryValid(category)) {
			score = setScore(category); // calculates the score
		} else {
			score = 0; // score if invalid category is selected
		}

		/* display category score */
		display.updateScorecard(category, currentPlayer, score);

		/* display total score */
		display.updateScorecard(TOTAL, currentPlayer, scoresArray[currentPlayerIndex][TOTAL - 1]);
	}

	private boolean isCategoryValid(int category) {
		boolean result = false;
		switch (category) {
		case ONES:
		case TWOS:
		case THREES:
		case FOURS:
		case FIVES:
		case SIXES:
			result = true;
			break;
		case THREE_OF_A_KIND:
			result = isRollRepeatedXTimes(category, 3);
			break;
		case FOUR_OF_A_KIND:
			result = isRollRepeatedXTimes(category, 4);
			break;
		case CHANCE:
			result = true;
			break;
		case FULL_HOUSE:
			result = isFullHouse(category);
			break;
		case SMALL_STRAIGHT:
			result = isStraight(category, 4);
			break;
		case LARGE_STRAIGHT:
			result = isStraight(category, 5);
			break;
		case YAHTZEE:
			result = isRollRepeatedXTimes(category, 5);
			break;
		default:
			break;
		}
		filledCategoriesArray[currentPlayerIndex][category - 1] = 1;
		return result;
	}

	/* methods to check if conditions for scoring categories are met */

	private boolean isRollRepeatedXTimes(int category, int x) {
		HashMap<Integer, Integer> duplicates = new HashMap<Integer, Integer>();

		for (int i = 0; i < dice.length; i++) {
			int roll = dice[i];

			if (duplicates.containsKey(roll)) {
				duplicates.put(roll, duplicates.get(roll) + 1);
			} else {
				duplicates.put(roll, 1);
			}
		}

		for (int k : duplicates.keySet()) {
			if (duplicates.get(k) >= x) {
				return true;
			}
		}
		return false;
	}

	private boolean isFullHouse(int category) {
		HashMap<Integer, Integer> duplicates = new HashMap<Integer, Integer>();

		for (int i = 0; i < dice.length; i++) {
			int roll = dice[i];

			if (duplicates.containsKey(roll)) {
				duplicates.put(roll, duplicates.get(roll) + 1);
			} else {
				duplicates.put(roll, 1);
			}
		}

		boolean isTwoOfAKind = false;
		boolean isThreeOfAKind = false;

		for (int k : duplicates.keySet()) {
			if (duplicates.get(k) == 3) {
				isThreeOfAKind = true;
			} else if (duplicates.get(k) == 2) {
				isTwoOfAKind = true;
			}
		}
		if (isTwoOfAKind && isThreeOfAKind) {
			return true;
		} else {
			return false;
		}
	}

	private boolean isStraight(int category, int straightLength) {

		int count = 1;
		sortArray(dice); // sort array from smallest to largest integer

		for (int i = 1; i < dice.length; i++) {
			if (dice[i] - dice[i - 1] == 1) {
				count++;
			}
		}

		if (count == straightLength) {
			return true;
		} else {
			return false;
		}
	}

	// Sorts array from smallest to largest integer
	private void sortArray(int[] array) {

		for (int x1 = 0; x1 < array.length; x1++) {
			int indexOfSmallest = x1;
			int smallest = array[x1];

			for (int x2 = x1 + 1; x2 < array.length; x2++) {

				if (array[x2] < array[indexOfSmallest]) {
					smallest = array[x2];
					indexOfSmallest = x2;
				}
			}
			array[indexOfSmallest] = array[x1];
			array[x1] = smallest;

		}
	}

	// Returns true if category has not been scored for current player
	private boolean isCategoryOpen(int category) {
		if (filledCategoriesArray[currentPlayerIndex][category - 1] == 0) {
			return true;
		} else {
			return false;
		}
	}

	// Sums the upper scores
	private void tallyUpperScores() {
		int upperScore;
		for (int i = 0; i < nPlayers; i++) {
			upperScore = 0;
			for (int j = 0; j < UPPER_SCORE - 1; j++) {
				upperScore += scoresArray[i][j];
			}
			scoresArray[i][UPPER_SCORE - 1] = upperScore;
		}
	}

	// Sums the lower scores
	private void tallyLowerScores() {
		int lowerScore;
		for (int i = 0; i < nPlayers; i++) {
			lowerScore = 0;
			for (int j = THREE_OF_A_KIND - 1; j < LOWER_SCORE - 1; j++) {
				lowerScore += scoresArray[i][j];
			}
			scoresArray[i][LOWER_SCORE - 1] = lowerScore;
		}
	}

	// Awards upper bonus if criteria is met
	private void awardUpperBonus() {
		for (int i = 0; i < nPlayers; i++) {
			if (scoresArray[i][UPPER_SCORE - 1] >= 63) {
				scoresArray[i][UPPER_BONUS - 1] = 35;
			}
		}
	}

	// Sums lower, upper, and bonus scores
	private void tallyTotalScores() {
		int totalPlayerScore;
		for (int i = 0; i < nPlayers; i++) {
			totalPlayerScore = 0;
			totalPlayerScore += scoresArray[i][UPPER_SCORE - 1] + scoresArray[i][UPPER_BONUS - 1]
					+ scoresArray[i][LOWER_SCORE - 1];
			scoresArray[i][TOTAL - 1] = totalPlayerScore;
		}
	}

	// Declares a winner, the player with the highest total score
	private void determineWinner() {
		winningScore = 0;
		for (int i = 0; i < nPlayers; i++) {
			if (scoresArray[i][TOTAL - 1] > winningScore) {
				winningScore = scoresArray[i][TOTAL - 1];
				winningPlayerIndex = i;
			}
		}
		winningPlayer = playerNames[winningPlayerIndex];
		displayMessage(4);
	}

	private void rollDie() {
		// sets random integer (between 1 and 6) for each die
		for (int i = 0; i < N_DICE; i++) {
			dice[i] = rgen.nextInt(1, 6);
		}
	}

	private void rollSelectedDie() {
		// selects new random integer (between 1 and 6) for selected die only
		for (Integer i : index) {
			int j = i.intValue();
			dice[j] = rgen.nextInt(1, 6);
		}
	}

	// Clears index of selected die and adds the dice that have been selected
	// for upcoming roll
	private void checkForSelectedDice() {
		index.clear();
		for (int i = 0; i < N_DICE; i++) {
			if (display.isDieSelected(i)) {
				index.add(i);
			}
		}
	}

	// Fills in the score for the selected category with the correct number of
	// points
	private int setScore(int category) {
		int result = 0;

		switch (category) {
		case ONES:
		case TWOS:
		case THREES:
		case FOURS:
		case FIVES:
		case SIXES:
			for (int i = 0; i < dice.length; i++) {
				if (dice[i] == category) {
					result++;
				}
			}
			break;
		case UPPER_SCORE:
			break;
		case UPPER_BONUS:
			break;
		case THREE_OF_A_KIND:
		case FOUR_OF_A_KIND:
		case CHANCE:
			for (int i = 0; i < dice.length; i++) {
				result += dice[i];
			}
			break;
		case FULL_HOUSE:
			result = 25;
			break;
		case SMALL_STRAIGHT:
			result = 30;
			break;
		case LARGE_STRAIGHT:
			result = 40;
			break;
		case YAHTZEE:
			result = 50;
			break;
		case LOWER_SCORE:
			break;
		case TOTAL:
			break;
		default:
			break;
		}
		scoresArray[currentPlayerIndex][category - 1] = result;
		scoresArray[currentPlayerIndex][TOTAL - 1] += result;

		return result;
	}

	// Displays the correct prompt on the bottom of the game canvas
	private void displayMessage(int x) {

		switch (x) {
		case 0:
			display.printMessage(currentPlayerName + "'s turn! Click \"Roll Dice\" button to roll the dice.");
			break;
		case 1:
		case 2:
			display.printMessage("Select the dice you wish to re-roll and click \"Roll Again\"");
			break;
		case 3:
			display.printMessage("Select a category for this roll");
			break;
		case 4:
			display.printMessage(
					"Congratulations " + winningPlayer + ", you're the winner with a total of " + winningScore + "!");
			break;
		}

	}

	/* Private instance variables */
	private int nPlayers; // number of players in the game
	private String[] playerNames; // Array of strings holding player names
	private YahtzeeDisplay display; // Instance of YahtzeeDisplay class
	private RandomGenerator rgen = new RandomGenerator(); // instance of a
															// random number
															// geneartor
	private int[] dice; // Array of integers to represent rolls of the dice
	private ArrayList<Integer> index; // ArrayList of Integers to represent
										// which die were selected
	private String currentPlayerName; // keeps track of whose turn it is
	private int currentPlayer; // keeps track of player index + 1
	private int currentPlayerIndex; // current player index
	private int score; // keeps track of current turn score
	private int[][] scoresArray;
	private int[][] filledCategoriesArray;
	private int winningPlayerIndex; // index of winning player
	private String winningPlayer; // name of the winning player
	private int winningScore; // score of winning player

}
