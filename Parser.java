import java.util.HashMap;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.BreakIterator;
import java.util.Locale;

public class Parser {
	
	String filename;
	private static HashMap<Token, Integer> unigrams = new HashMap<Token, Integer>();
	private static HashMap<Bigram, Integer> bigrams = new HashMap<Bigram, Integer>();

	/*
	 * Create a parser instance for the given file
	 */
	public Parser(String name) {
		filename = name;
	}
	
	/*
	 * Preprocesses the specified type of corpora
	 * "b" = bible, "h" = hotel
	 */
	public void processCorpus(String type) {
		File file = new File(filename);
		String clean = "";
		
		try {
			String content = new String(Files.readAllBytes(Paths.get(file.getAbsolutePath())));	
			
			switch(type) {
			case "b":	// process bible
				clean = content.replaceAll("[0-9]+:[0-9]+", "").replaceAll("<.*>", "");
				break;
			case "h":	// process hotel
				clean = content.replaceAll("[0-9],[0-9],", "");
				break;
			default:
				clean = content;
				break;
			}
		} catch (IOException e) {
			System.out.println("An error occured while reading the file \"" + file.getName() + "\"");
			return;
		}
		
		processChunk(clean.replaceAll("\n", ""));
	}
	
	/*
	 * Process an arbitrary string chunk,
	 * store the resulting unigram and bigram language models in their respective fields
	 */
	public void processChunk(String chunk) {
		BreakIterator iterator = BreakIterator.getSentenceInstance(Locale.US);
		iterator.setText(chunk);
		int start = iterator.first();
		for(int end = iterator.next(); end != BreakIterator.DONE; start = end, end = iterator.next()) {
			String sentence = chunk.substring(start, end).trim();
			//System.out.println(sentence);
			processSentenceUnigrams(sentence);
			processSentenceBigrams(sentence);
		}
	}
	
	/*
	 * Stores unigram language model information from a sentence
	 */
	public void processSentenceUnigrams(String s) {
		// Increment the number of start-of-sentence tokens.
		Token startToken = new Token(null, TokenType.START);
		if (unigrams.get(startToken) == null) unigrams.put(startToken, 1);
		else unigrams.put(startToken, unigrams.get(startToken) + 1); 
		
		// Process and tokenize
		String processed = s.replaceAll("([(),!.?;:])", " $1 ");
		String[] tokens = processed.split("\\s+");
		
		// Increment the value for each token in the map.
		for(int a = 0; a < tokens.length; a++) {
			Token newToken = new Token(tokens[a].trim(), TokenType.WORD);
			if (unigrams.get(newToken) == null) unigrams.put(newToken, 1);
			else unigrams.put(newToken, unigrams.get(newToken) + 1);
		}
		
		// Increment the number of end-of-sentence tokens.
		Token endToken = new Token(null, TokenType.END);
		if (unigrams.get(endToken) == null) unigrams.put(endToken, 1);
		else unigrams.put(endToken, unigrams.get(endToken) + 1); 
	}
	
	/*
	 * Stores bigram language model information from a sentence
	 */
	public void processSentenceBigrams(String s) {
		// Process and tokenize
		String processed = s.replaceAll("([(),!.?;:])", " $1 ");
		String[] tokens = processed.split("\\s+");
		
		// Define start token
		Token prev = new Token(null, TokenType.START);
		
		// Increment the value of each bigram in the map.
		for(int a = 0; a < tokens.length; a++) {
			Token newToken = new Token(tokens[a].trim(), TokenType.WORD);
			Bigram newBigram = new Bigram(prev, newToken);
			if (bigrams.get(newBigram) == null) bigrams.put(newBigram, 1);
			else bigrams.put(newBigram, bigrams.get(newBigram) + 1);
			prev = newToken;
		}
		
		// Increment the number of end-of-sentence bigrams.
		Bigram endBigram = new Bigram(prev, new Token(null, TokenType.END));
		if (bigrams.get(endBigram) == null) bigrams.put(endBigram, 1);
		else bigrams.put(endBigram, bigrams.get(endBigram) + 1);
	}
	
	/*
	 * Displays info about the unigram language model
	 */
	public void unigramDump() {
		System.out.println("Unigram info:");
		System.out.println("Size of unigram HashMap is " + unigrams.size());
		for(Token t: unigrams.keySet()) {
			System.out.println(t.printVal() + ", " + unigrams.get(t));
		}
	}
	
	/*
	 * Displays info about the bigram language model
	 */
	public void bigramDump() {
		System.out.println("Bigram info:");
		System.out.println("Size of bigram HashMap is " + bigrams.size());
		for(Bigram b: bigrams.keySet()) {
			System.out.println(b.printVal() + ", " + bigrams.get(b));
		}
	}
	
	public HashMap<Token, Integer> getUnigrams() {
		return unigrams;
	}
	
	public HashMap<Bigram, Integer> getBigrams() {
		return bigrams;
	}
}
