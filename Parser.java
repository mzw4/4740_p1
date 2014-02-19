
import java.util.Scanner;
import java.util.HashMap;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.regex.*;

public class Parser {
	
	public Parser(String name) {
		filename = name;
	}
	
	String filename;
	static HashMap<Token, Integer> unigrams = new HashMap<Token, Integer>();
	static HashMap<Bigram, Integer> bigrams = new HashMap<Bigram, Integer>();
	

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
			String input = inScanner.nextLine();
			
			//Process a Bible corpus.
			if (input.equals("b")) {
				parser.bibleProcess();
				goodInput = true;
			}
			
			//Process a hotel review corpus.
			else if (input.equals("h")) {
				parser.hotelProcess();
				goodInput = true;
			}
			
			else {
				System.out.println("Unrecognized input. Please try again.");
			}
		}
		System.out.println("Corpus processed!");
		
		parser.unigramDump();
		parser.bigramDump();
		
		System.out.println("To see a random sentence generated based on the unigrams of the given corpus, enter 'u'.");
		System.out.println("To see a random sentence generated based on the bigrams of the given corpus, enter 'b'.");
		System.out.println("To exit, enter 'x'.");
		while(true) {
			String input = inScanner.next();
			
			if (input.equals("u")) {
				//System.out.println(Generator.randomUnigramSentence());
			}
			
			else if(input.equals("b")) {
				System.out.println(Generator.randomBigramSentence(bigrams));
			}
			
			else if(input.equals("x")) {
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
			processSentenceBigrams(nextSentence.replaceAll("<.*>", ""));
		}
		
		bibleProcessor.close();
	}
	
	public void hotelProcess() {
		File file = new File(filename);
		Scanner hotelProcessor;
		try {
			hotelProcessor = new Scanner(file);
		}
		catch(FileNotFoundException e) {
			System.out.println("That file was not found. Sorry :/");
			return;
		}
		
		//testing to see if the file is read
		try {
			String sCurrentLine;
			BufferedReader br = new BufferedReader(new FileReader(file));
			while ((sCurrentLine = br.readLine()) != null) {
				//System.out.println(sCurrentLine);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		//Split into sentences by verse designations (of form [int]:[int])
		hotelProcessor.useDelimiter(Pattern.compile("\n"));
		while(hotelProcessor.hasNext()) {
			String nextSentence = hotelProcessor.next();
			
			//Strip out all of the <> tags before processing.
			processSentenceUnigrams(nextSentence.replaceAll("<.*>", ""));
			processSentenceBigrams(nextSentence.replaceAll("<.*>", ""));
		}
		
		hotelProcessor.close();
		
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
		String processed = s.replaceAll("([,!.?;:])", " \1 ");
		
		//For a moment, presume that this has already been done.
		//Split the string on whitespace.
		String[] tokens = processed.split("\\s+");
		//Increment the value for each token in the HashMap.
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
		String processed = s.replaceAll("([,!.?;:])", " \1 ");
		String[] tokens = processed.split("\\s+");
		Token prev = new Token(null, TokenType.START);
		
		for(int a = 0; a < tokens.length; a++) {
			Token newToken = new Token(tokens[a], TokenType.WORD);
			Bigram newBigram = new Bigram(prev, newToken);
			if (bigrams.get(newBigram) == null) bigrams.put(newBigram, 1);
			else bigrams.put(newBigram, bigrams.get(newBigram) + 1);
			
			prev = newToken;
		}
		
		Bigram endBigram;
		
		if (tokens.length == 0) { 
			endBigram = new Bigram(new Token(null, TokenType.START),
				                   new Token(null, TokenType.END));
		}
		else { 
			endBigram = new Bigram(new Token(tokens[tokens.length - 1], TokenType.WORD),
				                   new Token(null, TokenType.END));
		}
		if (bigrams.get(endBigram) == null) bigrams.put(endBigram, 1);
		else bigrams.put(endBigram, bigrams.get(endBigram) + 1);
		
		//Now we're done.
	}
	
	public void unigramDump() {
		System.out.println("Inside unigramDump");
		System.out.println("Size of unigram HashMap is " + unigrams.size());
		for(Token t: unigrams.keySet()) {
//			System.out.println(t.printVal() + ", " + 
//		                       unigrams.get(t));
		}
	}
	
	public void bigramDump() {
		System.out.println("Inside bigramDump");
		System.out.println("Size of bigram HashMap is " + bigrams.size());
		for(Bigram b: bigrams.keySet()) {
//			System.out.println(b.printVal() + ", " + 
//		                       unigrams.get(b));
		}
	}
}
