import java.util.HashMap;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.BreakIterator;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Iterator;

public class Parser {
	

	private String filename;
	private static final int GOOD_TURING_K = 6;
	
	private static HashMap<Token, Double> unigrams = new HashMap<Token, Double>();
	private static HashMap<Bigram, Double> bigrams = new HashMap<Bigram, Double>();
	private static HashMap<Trigram, Double> trigrams = new HashMap<Trigram, Double>();
	private static HashMap<Token, Double> gtunigrams = new HashMap<Token, Double>();
	private static HashMap<Bigram, Double> gtbigrams = new HashMap<Bigram, Double>();
	private static HashMap<Trigram, Double> gttrigrams = new HashMap<Trigram, Double>();

	private double unseen_bigram_count = 0;
	
	/*
	 * Create a parser instance for the given file
	 */
	public Parser(String name) {
		filename = name;
	}
	
	public void setFile(String name) {
		filename = name;
	}
	
	/*
	 * Preprocesses the specified type of corpora
	 * "b" = bible, "h" = hotel
	 */
	public void processCorpus(String type, boolean perplexity) {
		File file = new File(filename);
		String clean = "";
		
		try {
			String content = new String(Files.readAllBytes(Paths.get(file.getAbsolutePath())));	
			
			switch(type) {
			case "b":	// process bible
				clean = content.replaceAll("[0-9]+:[0-9]+", "").replaceAll("<.*>", "");
				break;
			case "h":	// process hotel
				clean = content.replaceAll("[0-9?],[0-9?],", "");
				break;
			default:
				clean = content;
				break;
			}
		} catch (IOException e) {
			System.out.println("An error occured while reading the file \"" + file.getName() + "\"");
			return;
		}
		
		if(perplexity) {
			computePerplexity(clean.replaceAll("\n", ""));
		} else {
			processChunk(clean.replaceAll("\n", ""));
		}
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
		if (unigrams.get(startToken) == null) unigrams.put(startToken, 1.0);
		else unigrams.put(startToken, unigrams.get(startToken) + 1); 
		
		// Process and tokenize
		String processed = s.replaceAll("([(),!.?;:])", " $1 ");
		String[] tokens = processed.split("\\s+");
		
		// Increment the value for each token in the map.
		for(int a = 0; a < tokens.length; a++) {
			Token newToken = new Token(tokens[a].trim(), TokenType.WORD);
			if (unigrams.get(newToken) == null) unigrams.put(newToken, 1.0);
			else unigrams.put(newToken, unigrams.get(newToken) + 1);
		}
		
		// Increment the number of end-of-sentence tokens.
		Token endToken = new Token(null, TokenType.END);
		if (unigrams.get(endToken) == null) unigrams.put(endToken, 1.0);
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
			if (bigrams.get(newBigram) == null) bigrams.put(newBigram, 1.0);
			else bigrams.put(newBigram, bigrams.get(newBigram) + 1);
			prev = newToken;
		}
		
		// Increment the number of end-of-sentence bigrams.
		Bigram endBigram = new Bigram(prev, new Token(null, TokenType.END));
		if (bigrams.get(endBigram) == null) bigrams.put(endBigram, 1.0);
		else bigrams.put(endBigram, bigrams.get(endBigram) + 1);
	}
	
	public void processSentenceTrigrams(String s) {
		//Process and tokenize
		String processed = s.replaceAll("([(),!.?;:])", " $1 ");
		String [] tokens = processed.split("\\s+");
		
		//Define start token
		Token prev = new Token(null, TokenType.START);
		
		//Add each trigram to the map.
		for(int a = 0; a < (tokens.length - 1); a++) {
			Token newToken = new Token(tokens[a].trim(), TokenType.WORD);
			Token successorToken = new Token(tokens[a+1].trim(), TokenType.WORD);
			Trigram newTrigram = new Trigram(prev, newToken, successorToken);
			if (trigrams.get(newTrigram) == null) trigrams.put(newTrigram, 1.0);
			else trigrams.put(newTrigram, trigrams.get(newTrigram) + 1);
			prev = newToken;
		}
		
		//Handle the final trigram.
		Trigram endTrigram = new Trigram(prev, new Token(tokens[tokens.length - 1].trim(), TokenType.WORD),
				                         new Token(null, TokenType.END));
		if (trigrams.get(endTrigram) == null) trigrams.put(endTrigram, 1.0);
		else trigrams.put(endTrigram, trigrams.get(endTrigram) + 1.0);
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
	
	public void trigramDump() {
		System.out.println("Trigram info:");
		for(Trigram t : trigrams.keySet()) {
			System.out.println(t.printVal() + ", " + trigrams.get(t));
		}
		System.out.println("Size of trigram HashMap is " + trigrams.size());
	}
	
	public HashMap<Token, Double> getUnigrams() {
		return unigrams;
	}
	
	public HashMap<Token, Double> getGTUnigrams() {
		return gtunigrams;
	}
	
	public HashMap<Bigram, Double> getBigrams() {
		return bigrams;
	}
	
	public HashMap<Bigram, Double> getGTBigrams() {
		return gtbigrams;
	}
	
	public HashMap<Trigram, Double> getTrigrams() {
		return trigrams;
	}
	
	public HashMap<Trigram, Double> getGTTrigrams() {
		return gttrigrams;
	}
	
	public void smoothUnigrams() {
		unigrams.put(new Token(null, TokenType.UNK), 0.0);
		
		// Get counts for n-grams that appear c times
		int[] counts = new int[GOOD_TURING_K + 1];
		for(double d: unigrams.values()) {
			if (d >= 0 && d <= GOOD_TURING_K) {
				counts[(int) d]++;
			}
		}
		
		//TODO: Simple Good-Turing - smooth N_c counts to replace zeroes
		double[] c_stars = new double[GOOD_TURING_K];
		
		// Calculate new c_star values
		for(int i = 0; i < GOOD_TURING_K; i++) {
			c_stars[i] = (i+1) * ((double)counts[i+1]/counts[i]);
		}
		
		// Iterate over the bigrams and replace the values with the c_star values.
		for(Token t: unigrams.keySet()) {
			double unsmoothedCount = unigrams.get(t);
			if (unsmoothedCount < GOOD_TURING_K) gtunigrams.put(t, c_stars[(int) unsmoothedCount]);
			else gtunigrams.put(t, unsmoothedCount);
		}
	}
	
	public void smoothBigrams() {
		// Get counts for n-grams that appear c times
		int[] counts = new int[GOOD_TURING_K + 1];
		
		// Account for unseen bigrams
		counts[0] += Math.pow(unigrams.size(), 2) - bigrams.size();
		
		// Account for unknown words
		bigrams.put(new Bigram(new Token(null, TokenType.WORD), new Token(null, TokenType.UNK)), 0.0);
		bigrams.put(new Bigram(new Token(null, TokenType.UNK), new Token(null, TokenType.UNK)), 0.0);
		bigrams.put(new Bigram(new Token(null, TokenType.UNK), new Token(null, TokenType.WORD)), 0.0);
		
		for(double d: bigrams.values()) {
			if (d >= 0 && d <= GOOD_TURING_K) {
				counts[(int) d]++;
			}
		}
		
		//TODO: Simple Good-Turing - smooth N_c counts to replace zeroes
		double[] c_stars = new double[GOOD_TURING_K];
		
		// Calculate new c_star values
		for(int i = 0; i < GOOD_TURING_K; i++) {
			c_stars[i] = (i+1) * ((double)counts[i+1]/counts[i]);
		}
		// Record count for zero probability bigrams
		unseen_bigram_count = c_stars[0];
		
		// Iterate over the bigrams and replace the values with the c_star values.
		for(Bigram b: bigrams.keySet()) {
			double unsmoothedCount = bigrams.get(b);
			
//			if(b.getSecond().getWord() != null && b.getSecond().getWord().equals("IsTruthFul")) {
//				System.out.println("FUCK" + c_stars[(int) unsmoothedCount]);
//			}
//			if(b.getFirst().getWord() != null && b.getSecond().getWord() != null &&
//					b.getFirst().getWord().equals(",") && b.getSecond().getWord().equals("review")) {
//				System.out.println("added review");
//			}
			if (unsmoothedCount < GOOD_TURING_K) gtbigrams.put(b, c_stars[(int) unsmoothedCount]);
			else gtbigrams.put(b, unsmoothedCount);
		}
	}
	
	public void smoothBigrams2() {
		Iterator<Double> iterator = bigrams.values().iterator();
		int[] counts = new int[GOOD_TURING_K + 2];
		
		//initialize values in counts to 0
		for(int a = 0; a <= GOOD_TURING_K; a++) {
			counts[a] = 0;
		}
		
		while(iterator.hasNext()) {
			double val = iterator.next();
			if (val >= 0 && val <= GOOD_TURING_K + 1) {
				counts[(int) val] = (counts[(int) val] + 1);
			}
		}
		
		//TODO: Simple Good-Turing - smooth N_c counts to replace zeroes
		
		double[] c_stars = new double[GOOD_TURING_K + 1];
		//initalize c_stars[0] to be N_1
		c_stars[0] = (double) counts[1];
		
		for(int a = 1; a <= GOOD_TURING_K; a++) {
			//use the Katz 1987 formula (page 103 of the book) to calculate c_star given the value k
			double c = (double) a;
			
			double katz_numerator = ((c+1) * ((double) counts[a+1])/((double) counts[a])) - 
									(c * (((double) (GOOD_TURING_K + 1) * counts[a+1]) / counts[a]));
			double katz_denominator = (double) (1 - (((double) (GOOD_TURING_K + 1) * counts[a+1]) / (double) counts[a]));
			
			c_stars[a] = katz_numerator / katz_denominator;
		}
		
		//Now that we have the values for c_star, iterate over the unigrams and replace the values with the c_star values.
		
		Iterator<Bigram> token_iterator = bigrams.keySet().iterator();
		
		while(token_iterator.hasNext()) {
			Bigram nextVal = token_iterator.next();
			double unsmoothedCount = bigrams.get(nextVal);
			
			if (unsmoothedCount <= GOOD_TURING_K) gtbigrams.put(nextVal, c_stars[(int) unsmoothedCount]);
			else gtbigrams.put(nextVal, (double) unsmoothedCount);
		}
	}
	
	public void smoothTrigrams() {
		Iterator<Double> iterator = trigrams.values().iterator();
		int[] counts = new int[GOOD_TURING_K + 2];
		
		//initialize values in counts to 0
		for(int a = 0; a <= GOOD_TURING_K; a++) {
			counts[a] = 0;
		}
		
		while(iterator.hasNext()) {
			double val = iterator.next();
			if (val >= 0 && val <= GOOD_TURING_K + 1) {
				counts[(int) val] = (counts[(int) val] + 1);
			}
		}
		
		//TODO: Simple Good-Turing - smooth N_c counts to replace zeroes
		
		double[] c_stars = new double[GOOD_TURING_K + 1];
		//initalize c_stars[0] to be N_1
		c_stars[0] = (double) counts[1];
		
		for(int a = 1; a <= GOOD_TURING_K; a++) {
			//use the Katz 1987 formula (page 103 of the book) to calculate c_star given the value k
			double c = (double) a;
			
			double katz_numerator = ((c+1) * ((double) counts[a+1])/((double) counts[a])) - 
									(c * (((double) (GOOD_TURING_K + 1) * counts[a+1]) / counts[a]));
			double katz_denominator = (double) (1 - (((double) (GOOD_TURING_K + 1) * counts[a+1]) / (double) counts[a]));
			
			c_stars[a] = katz_numerator / katz_denominator;
		}
		
		//Now that we have the values for c_star, iterate over the unigrams and replace the values with the c_star values.
		
		Iterator<Trigram> token_iterator = trigrams.keySet().iterator();
		
		while(token_iterator.hasNext()) {
			Trigram nextVal = token_iterator.next();
			double unsmoothedCount = trigrams.get(nextVal);
			
			if (unsmoothedCount <= GOOD_TURING_K) gttrigrams.put(nextVal, c_stars[(int) unsmoothedCount]);
			else gttrigrams.put(nextVal, (double) unsmoothedCount);
		}
	}
	
	public void computePerplexity(String chunk) {	
		// Parse the test corpus into a list of tokens, including sentence boundaries
		ArrayList<Token> tokens = new ArrayList<>();
		
		BreakIterator iterator = BreakIterator.getSentenceInstance(Locale.US);
		iterator.setText(chunk);
		int start = iterator.first();
		for(int end = iterator.next(); end != BreakIterator.DONE; start = end, end = iterator.next()) {
			tokens.add(new Token(null, TokenType.START));
			
			// Process and tokenize each sentence
			String sentence = chunk.substring(start, end).trim();
			
			System.out.println(sentence);
			String processed = sentence.replaceAll("([(),!.?;:])", " $1 ");
			String[] sentence_tokens = processed.split("\\s+");
			for(String s: sentence_tokens) {
				Token t = new Token(s, TokenType.WORD);
				
				// if the word is not in our unigram map, add it to the list as an unknown word
				if(unigrams.containsKey(t)) {
					tokens.add(t);
				} else {
					tokens.add(new Token(s, TokenType.UNK));
				}
			}
			tokens.add(new Token(null, TokenType.END));
		}
		
		// Calculate perplexities
		double pp = 0;
		Token prev_word = null;
		int token_count = 0;
		
		for(Token t: tokens) {
			if(prev_word == null) {
				int uni_sum = 0;
				for(Double u: gtunigrams.values()) {
					uni_sum += u;
				}
				// If word is unknown, use probability of unknown word
				if(t.getType() == TokenType.UNK) {
					pp += Math.log10(gtunigrams.get(new Token(null, TokenType.UNK))/uni_sum);
				} else {
					pp += Math.log10(gtunigrams.get(t)/uni_sum);
				}
			} else {
				System.out.println("-----------------");
				System.out.println("Prevword: " + prev_word.getWord());
				System.out.println("Word: " + t.getWord());
				
				double count = 0;
				if(t.getType() == TokenType.UNK && prev_word.getType() == TokenType.UNK) {
					count = gtbigrams.get(new Bigram(new Token(null, TokenType.UNK), new Token(null, TokenType.UNK)));
				} else if(t.getType() == TokenType.UNK) {
					count = gtbigrams.get(new Bigram(new Token(null, TokenType.WORD), new Token(null, TokenType.UNK)));
				} else if(prev_word.getType() == TokenType.UNK) {
					count = gtbigrams.get(new Bigram(new Token(null, TokenType.UNK), new Token(null, TokenType.WORD)));
				} else {
					if(gtbigrams.get(new Bigram(prev_word, t)) == null) {
						count = unseen_bigram_count;
					} else {
						System.out.println("Bigram count: " +gtbigrams.get(new Bigram(prev_word, t)));
						count = gtbigrams.get(new Bigram(prev_word, t));
					}
				}
				
				double prob = 0;
				if(prev_word.getType() == TokenType.UNK) {
					prob = count/gtunigrams.get(new Token(null, TokenType.UNK));
				} else {
					prob = count/gtunigrams.get(prev_word);
				}

				System.out.println("Count: " +count);
				System.out.println("Prob: " +prob);
				pp += Math.log10(1/(prob));
			}
			System.out.println("PP: " +pp);
			prev_word = t;
			token_count++;
		}
		
		System.out.println(token_count);
		System.out.println("Perplexity of test corpus " + filename + ": "
		+ Math.pow(10, pp/token_count));
	}
}
