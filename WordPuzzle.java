package il.ac.tau.cs.sw1.ex4;

import java.io.File;
import java.util.Arrays;
import java.util.Random;
import java.util.Scanner;

public class WordPuzzle {
	public static final char HIDDEN_CHAR = '_';
	public static final int MAX_VOCABULARY_SIZE = 3000;

	public static String[] scanVocabulary(Scanner scanner) { // Q - 1
		String currentString = "";
		String[] result = new String[MAX_VOCABULARY_SIZE];
		int index = 0;

		while (scanner.hasNext()) { // running on the input

			if (index == 3000) { // need the first 3000 legal words
				break;
			}

			currentString = scanner.next();
			if (currentString.length() < 2) { // only words of length 2 and above are legal
				continue; // goes to next word in input if len < 2
			}

			char[] currentStringAsChars = currentString.toCharArray();
			boolean isLegal = true;

			for (char ch : currentStringAsChars) {
				if (!(((64 < ch) && (ch < 91)) || ((96 < ch) && (ch < 123)))) { // convert word into a char array in
																				// order
					isLegal = false; // to check for non english letters
					break;
				}
			}
			if (isLegal == true) { // in case word is legal -> add to result
				if (isContains(result, currentString.toLowerCase(), index) == false) {
					result[index] = currentString.toLowerCase();
					index++;
				}
			}
		}

		String[] actualResult = Arrays.copyOfRange(result, 0, index); // declaring a fit sized array
		Arrays.sort(actualResult); // lexicographically sorting it

		return actualResult;
	}

	private static boolean isContains(String[] arr, String value, int index) {
		for (int count = 0; count < index; count++) {
			if (arr[count].equals(value)) {
				return true;
			}
		}
		return false;
	}

	public static int countHiddenInPuzzle(char[] puzzle) { // Q - 2
		int count = 0;
		for (char ch : puzzle) {
			if (ch == HIDDEN_CHAR) {
				count++;
			}
		}

		return count;
	}

	public static String getRandomWord(String[] vocabulary, Random generator) { // Q - 3
		return vocabulary[generator.nextInt(vocabulary.length)];
	}

	public static boolean checkLegal(String word, char[] puzzle) { // Q - 4

		int[] wordCount = new int[26];
		int[] puzzleCount = new int[26];
		boolean isOneHidden = false;
		boolean isOneOpen = false;

		for (char ch : word.toCharArray()) {
			wordCount[ch - 97]++;
		}
		for (char ch : puzzle) {
			if (ch != HIDDEN_CHAR) {
				puzzleCount[ch - 97]++;
				isOneOpen = true;
			} else {
				isOneHidden = true;
			}
		}

		if (!isOneHidden || !isOneOpen) {
			return false;
		}

		for (int i = 0; i < 26; i++) {
			if (wordCount[i] == 1) {
				continue;
			}
			if (wordCount[i] > 1 && puzzleCount[i] != 0 && puzzleCount[i] != wordCount[i]) {
				return false;
			}
		}
		return true;
	}

	public static char[] getRandomPuzzleCandidate(String word, double prob, Random generator) {// Q - 5
		char[] result = new char[word.length()];

		for (int i = 0; i < word.length(); i++) {
			if (generator.nextFloat() <= prob) {
				result[i] = HIDDEN_CHAR;
			} else {
				result[i] = word.charAt(i);
			}
		}
		return result;
	}

	public static char[] getRandomPuzzle(String word, double prob, Random generator) { // Q - 6
		char[] riddle = new char[word.length()];

		for (int i = 0; i < 1000; i++) {
			riddle = getRandomPuzzleCandidate(word, prob, generator);
			if (checkLegal(word, riddle)) {
				return riddle;
			}
		}
		throwPuzzleGenerationException();
		return null;
	}

	public static int applyGuess(char guess, String solution, char[] puzzle) { // Q - 7
		int count = 0;
		for (int i = 0; i < solution.length(); i++) {
			if (puzzle[i] == HIDDEN_CHAR) {
				if (solution.charAt(i) == guess) {
					puzzle[i] = guess;
					count++;
				}
			}
		}
		return count;
	}

	public static char[] getHelp(String solution, char[] puzzle) { // Q - 8
		boolean hasFound = false;
		char ch = 'P';

		for (int i = 0; i < solution.length(); i++) {
			if (!hasFound & puzzle[i] == HIDDEN_CHAR) { // checks for first underscore
				ch = solution.charAt(i);
				hasFound = true;
			}
			if (hasFound & puzzle[i] == HIDDEN_CHAR & solution.charAt(i) == ch) {
				puzzle[i] = ch;
			}
		}
		return puzzle;
	}

	public static void main(String[] args) throws Exception { // Q - 9
		if (args.length != 1) { // checking input is a single string
			System.out.println("Invalid input!");
			return;
		}

		File file = new File(args[0]);
		Scanner scanner = new Scanner(file);
		String[] words = scanVocabulary(scanner);

		printReadVocabulary(args[0], words.length);
		printSettingsMessage();
		printEnterHidingProbability();
		Scanner userInput = new Scanner(System.in);
		float prob = userInput.nextFloat();

		int[] nums = new int[words.length];
		for (int i = 0; i < nums.length; i++) {
			nums[i] = i;
		}
		Random generator = new MyRandom(nums,
				new float[] { 0.0f, 0.1f, 0.2f, 0.3f, 0.4f, 0.5f, 0.6f, 0.7f, 0.8f, 0.9f, 1.0f });
		
		char[] puzzle = new char[0];
		String word = "";
		boolean isNo = true;
		while (isNo) {
			word = getRandomWord(words, generator);
			puzzle = getRandomPuzzle(word, prob, generator);
			printPuzzle(puzzle);

			while (true) {
				printReplacePuzzleMessage();
				String msg = userInput.next();

				if (msg.equals("no")) { // id the user said no --> isNo = false and break to continue the game
					isNo = false;
					break;
				} else if (msg.equals("yes")) {
					break;
				}
			}
		}
		// pre-game setup
		printGameStageMessage();
		int counter = countHiddenInPuzzle(puzzle) + 3;
		char inputChar = 't';
		int guessCounter = 0;
		
		while(counter > 0) {
			printPuzzle(puzzle);
			printEnterYourGuessMessage();
			inputChar = userInput.next().charAt(0);
			
			guessCounter = applyGuess(inputChar, word, puzzle);
			if(guessCounter > 0) { // if the user guessed correctly
				if(countHiddenInPuzzle(puzzle) == 0) {
					printWinMessage();
					return;
				}
				else {
					printCorrectGuess(counter);
					counter--;
				}
			}
			else {
				if(inputChar != 'H') {
					counter--;
					printWrongGuess(counter);
					continue;
				}
				else {
					puzzle = getHelp(word, puzzle);
					counter--;
					
					if(countHiddenInPuzzle(puzzle) == 0) {
						printWinMessage();
						return;
					}
				}
			}
		}
		printGameOver();
	}

	/*************************************************************/
	/********************* Don't change this ********************/
	/*************************************************************/

	public static void printReadVocabulary(String vocabularyFileName, int numOfWords) {
		System.out.println("Read " + numOfWords + " words from " + vocabularyFileName);
	}

	public static void throwPuzzleGenerationException() {
		throw new RuntimeException("Failed creating a legal puzzle after 1000 attempts!");
	}

	public static void printSettingsMessage() {
		System.out.println("--- Settings stage ---");
	}

	public static void printEnterHidingProbability() {
		System.out.println("Enter your hiding probability:");
	}

	public static void printPuzzle(char[] puzzle) {
		System.out.println(puzzle);
	}

	public static void printReplacePuzzleMessage() {
		System.out.println("Replace puzzle?");
	}

	public static void printGameStageMessage() {
		System.out.println("--- Game stage ---");
	}

	public static void printEnterYourGuessMessage() {
		System.out.println("Enter your guess:");
	}

	public static void printCorrectGuess(int attemptsNum) {
		System.out.println("Correct Guess, " + attemptsNum + " guesses left");
	}

	public static void printWrongGuess(int attemptsNum) {
		System.out.println("Wrong Guess, " + attemptsNum + " guesses left");
	}

	public static void printWinMessage() {
		System.out.println("Congratulations! You solved the puzzle");
	}

	public static void printGameOver() {
		System.out.println("Game over!");
	}

}
