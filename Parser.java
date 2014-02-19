
import java.util.Scanner;
import java.util.Hashtable;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.regex.*;

public class Parser {
	
	public Parser(String name) {
		filename = name;
	}
	
	String filename;
	Hashtable<Token, Integer> unigrams = new Hashtable<Token, Integer>();
	Hashtable<Bigram, Integer> bigrams = new Hashtable<Bigram, Integer>();
	

	/**
	 * @param args - The name of the file to be processed.
	 * Identifies the type of corpus and then processes it. Allows the user to see
	 * random sentences based on the unigrams or bigrams of the corpus.
	 */
	public static void main(String[] args) {
		//Check to see if the correct number of arguments (1) was given.
		if (args.length != 1) {
			System.out.println("Please call this method with one argument, the file to be processed.");
			return;
		}
		
		Parser parser = new Parser(args[0]);
		Scanner inScanner = new Scanner(System.in);
		
		boolean goodInput = false;
		
		//Identify what type of corpus is being processed.
		System.out.println("If this is a Bible corpus, please enter 'b'. If this is a hotel review corpus, please enter 'h'.");
		
		while(!goodInput) {
			String input = inScanner.next();
			
			//Process a Bible corpus.
			if (input == "b") {
				parser.bibleProcess();
				goodInput = true;
			}
			
			//Process a hotel review corpus.
			else if (input == "h") {
				parser.hotelProcess();
				goodInput = true;
			}
			
			else {
				System.out.println("Unrecognized input. Please try again.");
			}
		}
		System.out.println("Corpus processed!");
		
		System.out.println("To see a random sentence generated based on the unigrams of the given corpus, enter 'u'.");
		System.out.println("To see a random sentence generated based on the bigrams of the given corpus, enter 'b'.");
		System.out.println("To exit, enter 'x'.");
		while(true) {
			String input = inScanner.next();
			
			if (input == "u") {
				System.out.println(parser.randomUnigramSentence());
			}
			
			else if(input == "b") {
				System.out.println(parser.randomBigramSentence());
			}
			
			else if(input == "x") {
				System.out.println("Have a good day!");
				break;
			}
			
			else {
				System.out.println("Unrecognized input. Please try again.");
			}
		}
	}

	public void bibleProcess() {
		File file = new File(filename);
		Scanner bibleProcessor;
		try {
			bibleProcessor = new Scanner(file);
		}
		catch(FileNotFoundException e) {
			System.out.println("That file was not found. Sorry :/");
			return;
		}

		//Split into sentences by verse designations (of form [int]:[int])
		bibleProcessor.useDelimiter(Pattern.compile("[0-9]+:[0-9]+"));
		while(bibleProcessor.hasNext()) {
			String nextSentence = bibleProcessor.next();
			
			//Strip out all of the <> tags before processing.
			processSentenceUnigrams(nextSentence.replaceAll("<.*>", ""));
		}
	}
	
	public void hotelProcess() {
		//TODO: implement
	}
	
	public void processSentenceUnigrams(String s) {
		//Increment the number of start-of-sentence tokens.
		if (unigrams.get(new Token(null, TokenType.START)) == null) {
			unigrams.put(new Token(null, TokenType.START), 1);
		}
		else {
			unigrams.put(new Token(null, TokenType.START), 
					     unigrams.get(new Token(null, TokenType.START)) + 1); 
		}
		
		//TODO: Put spaces around punctuation, so that you can split on spaces.
		
		//For a moment, presume that this has already been done.
		//Split the string on whitespace.
		String[] tokens = s.split("\\s+");
		//Increment the value for each token in the hashtable.
		for(int a = 0; a < tokens.length; a++) {
			Token newToken = new Token(tokens[a], TokenType.WORD);
			if (unigrams.get(newToken) == null) unigrams.put(newToken, 1);
			else unigrams.put(newToken, unigrams.get(newToken) + 1);
		}
		
		//Increment the number of end-of-sentence tokens.
		if (unigrams.get(new Token(null, TokenType.END)) == null) {
			unigrams.put(new Token(null, TokenType.END), 1);
		}
		else {
			unigrams.put(new Token(null, TokenType.END), 
					     unigrams.get(new Token(null, TokenType.END)) + 1); 
		}
		
		//Now we're done.
	}
	
	public void processSentenceBigrams(String s) {
		
	}
	
	public String randomUnigramSentence() {
		//TODO: implement
		return null;
	}
	
	public String randomBigramSentence() {
		//TODO: implement
		return null;
	}
}
