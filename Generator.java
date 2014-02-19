import java.util.ArrayList;
import java.util.HashMap;


public class Generator {
	
	/*
	 * Generates a random sentence of specified length based on a bigram language model.
	 * Intermediate punctuation is selected according to the model just as any other token.
	 */
	public static String randomBigramSentence(HashMap<Token, Integer> unigram_model,
			HashMap<Bigram, Integer> bigram_model, int length) {
		ArrayList<Token> sentence_arr = new ArrayList<>();
		int cur_length = 0;
		
		while(cur_length < length) {
			Token toAdd = null;
			// Pick the first word. If there is no possible word, stop.
			if(cur_length == 0) {
				toAdd = selectWithProbability(unigram_model);
			} else {
				// Construct map of bigrams matching the previous word
				Token prev_word = sentence_arr.get(sentence_arr.size() - 1);
				HashMap<Bigram, Integer> possible_bigrams = new HashMap<>();
				for(Bigram b: bigram_model.keySet()) {
					if(b.getFirst().equals(prev_word)) {
						possible_bigrams.put(b, bigram_model.get(b));
					}
				}
				
				// Select the next word according to bigram probabilities
				Bigram next = selectBigramWithProbability(possible_bigrams);
				if(next == null) {
					break;
				}
				toAdd = next.getSecond();
			}
			
			if(toAdd != null) {
				sentence_arr.add(toAdd);
			} else {
				break;
			}
			cur_length++;
		}
		
		String sentence = "";
		for(Token t: sentence_arr) {
			sentence += t.getWord() + " ";
		}
		return sentence + ".";
	}
	
	/* 
	 * Selects a key with probability according to its corresponding value 
	 * with round robin probability selection
	 */
	private static Token selectWithProbability(HashMap<Token, Integer> model) {
		int sum = 0;
		for(Integer count: model.values()) {
			sum += count;
		}
		
		int rand = (int) (Math.random() * sum);
		int cur = 0;
		for(Token t: model.keySet()) {
			cur += model.get(t);
			if(cur > rand) {
				return t;
			}
		}
		return null;
	}
	
	/* 
	 * Selects a key with probability according to its corresponding value 
	 * with round robin probability selection
	 * 
	 * The number of occurrences of the previous word should be the same as the 
	 * sum of all values in the map of possible bigrams
	 */
	private static Bigram selectBigramWithProbability(HashMap<Bigram, Integer> model) {
		int sum = 0;
		for(Integer count: model.values()) {
			sum += count;
		}
		
		int rand = (int) (Math.random() * sum);
		int cur = 0;
		for(Bigram b: model.keySet()) {
			cur += model.get(b);
			if(cur > rand) {
				return b;
			}
		}
		return null;
	}
	
	public static void main(String[] args) {
		// Test models
		// "The grey fox likes cats", "The grey fox hates dogs", "The red fox likes Pokemon"
		// "The grey deer likes cookies", "The grey fox likes DeadMau5", "The red deer hates Muppets"
		
		HashMap<Token, Integer> unigram_model = new HashMap<>();
		unigram_model.put(new Token("The", TokenType.WORD), 6);
		unigram_model.put(new Token("grey", TokenType.WORD), 4);
		unigram_model.put(new Token("red", TokenType.WORD), 2);
		unigram_model.put(new Token("fox", TokenType.WORD), 4);
		unigram_model.put(new Token("deer", TokenType.WORD), 2);
		unigram_model.put(new Token("likes", TokenType.WORD), 4);
		unigram_model.put(new Token("hates", TokenType.WORD), 2);
		unigram_model.put(new Token("cats", TokenType.WORD), 1);
		unigram_model.put(new Token("dogs", TokenType.WORD), 1);
		unigram_model.put(new Token("Pokemon", TokenType.WORD), 1);
		unigram_model.put(new Token("cookies", TokenType.WORD), 1);
		unigram_model.put(new Token("DeadMau5", TokenType.WORD), 1);
		unigram_model.put(new Token("Muppets", TokenType.WORD), 1);

		HashMap<Bigram, Integer> bigram_model = new HashMap<>();
		bigram_model.put(new Bigram(new Token("The", TokenType.WORD), new Token("grey", TokenType.WORD)), 4);
		bigram_model.put(new Bigram(new Token("The", TokenType.WORD), new Token("red", TokenType.WORD)), 2);
		bigram_model.put(new Bigram(new Token("grey", TokenType.WORD), new Token("fox", TokenType.WORD)), 3);
		bigram_model.put(new Bigram(new Token("grey", TokenType.WORD), new Token("deer", TokenType.WORD)), 1);
		bigram_model.put(new Bigram(new Token("red", TokenType.WORD), new Token("fox", TokenType.WORD)), 1);
		bigram_model.put(new Bigram(new Token("red", TokenType.WORD), new Token("deer", TokenType.WORD)), 1);
		bigram_model.put(new Bigram(new Token("fox", TokenType.WORD), new Token("likes", TokenType.WORD)), 3);
		bigram_model.put(new Bigram(new Token("fox", TokenType.WORD), new Token("hates", TokenType.WORD)), 1);
		bigram_model.put(new Bigram(new Token("deer", TokenType.WORD), new Token("likes", TokenType.WORD)), 1);
		bigram_model.put(new Bigram(new Token("deer", TokenType.WORD), new Token("hates", TokenType.WORD)), 1);
		bigram_model.put(new Bigram(new Token("likes", TokenType.WORD), new Token("cats", TokenType.WORD)), 4);
		bigram_model.put(new Bigram(new Token("likes", TokenType.WORD), new Token("Pokemon", TokenType.WORD)), 4);
		bigram_model.put(new Bigram(new Token("likes", TokenType.WORD), new Token("cookies", TokenType.WORD)), 4);
		bigram_model.put(new Bigram(new Token("likes", TokenType.WORD), new Token("DeadMau5", TokenType.WORD)), 4);
		bigram_model.put(new Bigram(new Token("hates", TokenType.WORD), new Token("dogs", TokenType.WORD)), 4);
		bigram_model.put(new Bigram(new Token("hates", TokenType.WORD), new Token("Muppets", TokenType.WORD)), 4);

		
		String sentence = Generator.randomBigramSentence(unigram_model, bigram_model, 5);
		System.out.println(sentence);
	}
}
