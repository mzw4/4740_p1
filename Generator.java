import java.util.ArrayList;
import java.util.HashMap;

public class Generator {
	
	/*
	 * Generates a random sentence based on a unigram language model.
	 */
	public static String randomUnigramSentence(HashMap<Token,Double> unigrams) {
        //int total = getTotal(unigrams);
        String sentence = "";
        boolean isEnd = false; //boolean indicating whether sentence is done

        //count words of corpus
        while (!isEnd) {
        	Token next = selectUnigramWithProbability(unigrams);
        	if(next == null || next.getType() == TokenType.END) {
        		isEnd = true;
        	} else if(next.getType() == TokenType.START) {
        		continue;
        	} else if(sentence.length() == 0 && next.getWord().length() != 0) { // capitalize first word of the sentence
            	String word = next.getWord();
        		word = word.substring(0,1).toUpperCase()
    					+ word.substring(1,word.length());
        		sentence += word + " ";
        	} else {
        		sentence += next.getWord() + " ";
        	}
        }
        
        return sentence;
    }
	
	/* 
	 * Selects a Token with probability according to its corresponding value 
	 * with roulette style selection
	 */
    public static Token selectUnigramWithProbability(HashMap<Token,Double> model) {
		int sum = 0;
		for(Double count: model.values()) {
			sum += count;
		}
		
		double rand = (Math.random() * sum);
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
	 * Generates a random sentence based on a bigram language model.
	 */
	public static String randomBigramSentence(HashMap<Bigram, Double> bigram_model) {
		
		ArrayList<Bigram> sentence_arr = new ArrayList<>();
		Token prev_word = null;
		
		while(prev_word == null || prev_word.getType() != TokenType.END) {
			// Construct map of bigrams matching the previous word
			HashMap<Bigram, Double> possible_bigrams = new HashMap<>();
			for(Bigram b: bigram_model.keySet()) {
				if(prev_word == null) {
					if(b.getFirst().getType() == TokenType.START) {
						possible_bigrams.put(b, bigram_model.get(b));
					}
				} else if(b.getFirst().equals(prev_word)){
					possible_bigrams.put(b, bigram_model.get(b));
				}
			}
			
			// Select the next word according to bigram probabilities
			Bigram next = selectBigramWithProbability(possible_bigrams);
			if(next == null) {
				break;
			}
			sentence_arr.add(next);
			prev_word = sentence_arr.get(sentence_arr.size() - 1).getSecond();
		}
		
		String sentence = "";
		for(Bigram b: sentence_arr) {
			if(b.getSecond().getType() != TokenType.END && b.getFirst().getType() != TokenType.START) {
				sentence += " ";
			}
			if(b.getFirst().getType() == TokenType.WORD) {
				sentence += b.getFirst().getWord();
			} 

		}
		return sentence.substring(1);
	}
	
	/* 
	 * Selects a Bigram with probability according to its corresponding value 
	 * with roulette style selection
	 * 
	 * The number of occurrences of the preceding word should be the same as the 
	 * sum of all values in the model received
	 */
	private static Bigram selectBigramWithProbability(HashMap<Bigram, Double> model) {
		int sum = 0;
		for(Double count: model.values()) {
			sum += count;
		}
		
		double rand = (Math.random() * sum);
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
		// "The grey fox likes cats.", "The grey fox hates dogs.", "The red fox likes Pokemon."
		// "The grey deer likes cookies.", "The grey fox likes DeadMau5.", "The red deer hates Muppets."
		
		HashMap<Token, Double> unigram_model = new HashMap<>();
		
		unigram_model.put(new Token("The", TokenType.WORD), 6.0);
		unigram_model.put(new Token("grey", TokenType.WORD), 4.0);
		unigram_model.put(new Token("red", TokenType.WORD), 2.0);
		unigram_model.put(new Token("fox", TokenType.WORD), 4.0);
		unigram_model.put(new Token("deer", TokenType.WORD), 2.0);
		unigram_model.put(new Token("likes", TokenType.WORD), 4.0);
		unigram_model.put(new Token("hates", TokenType.WORD), 2.0);
		unigram_model.put(new Token("cats", TokenType.WORD), 1.0);
		unigram_model.put(new Token("dogs", TokenType.WORD), 1.0);
		unigram_model.put(new Token("Pokemon", TokenType.WORD), 1.0);
		unigram_model.put(new Token("cookies", TokenType.WORD), 1.0);
		unigram_model.put(new Token("DeadMau5", TokenType.WORD), 1.0);
		unigram_model.put(new Token("Muppets", TokenType.WORD), 1.0);
		unigram_model.put(new Token(null, TokenType.START), 6.0);
		unigram_model.put(new Token(".", TokenType.WORD), 6.0);
		unigram_model.put(new Token(null, TokenType.END), 6.0);

		HashMap<Bigram, Double> bigram_model = new HashMap<>();
		bigram_model.put(new Bigram(new Token(null, TokenType.START), new Token("The", TokenType.WORD)), 4.0);
		bigram_model.put(new Bigram(new Token("The", TokenType.WORD), new Token("grey", TokenType.WORD)), 4.0);
		bigram_model.put(new Bigram(new Token("The", TokenType.WORD), new Token("red", TokenType.WORD)), 2.0);
		bigram_model.put(new Bigram(new Token("grey", TokenType.WORD), new Token("fox", TokenType.WORD)), 3.0);
		bigram_model.put(new Bigram(new Token("grey", TokenType.WORD), new Token("deer", TokenType.WORD)), 1.0);
		bigram_model.put(new Bigram(new Token("red", TokenType.WORD), new Token("fox", TokenType.WORD)), 1.0);
		bigram_model.put(new Bigram(new Token("red", TokenType.WORD), new Token("deer", TokenType.WORD)), 1.0);
		bigram_model.put(new Bigram(new Token("fox", TokenType.WORD), new Token("likes", TokenType.WORD)), 3.0);
		bigram_model.put(new Bigram(new Token("fox", TokenType.WORD), new Token("hates", TokenType.WORD)), 1.0);
		bigram_model.put(new Bigram(new Token("deer", TokenType.WORD), new Token("likes", TokenType.WORD)), 1.0);
		bigram_model.put(new Bigram(new Token("deer", TokenType.WORD), new Token("hates", TokenType.WORD)), 1.0);
		bigram_model.put(new Bigram(new Token("likes", TokenType.WORD), new Token("cats", TokenType.WORD)), 1.0);
		bigram_model.put(new Bigram(new Token("likes", TokenType.WORD), new Token("Pokemon", TokenType.WORD)), 1.0);
		bigram_model.put(new Bigram(new Token("likes", TokenType.WORD), new Token("cookies", TokenType.WORD)), 1.0);
		bigram_model.put(new Bigram(new Token("likes", TokenType.WORD), new Token("DeadMau5", TokenType.WORD)), 1.0);
		bigram_model.put(new Bigram(new Token("hates", TokenType.WORD), new Token("dogs", TokenType.WORD)), 1.0);
		bigram_model.put(new Bigram(new Token("hates", TokenType.WORD), new Token("Muppets", TokenType.WORD)), 1.0);
		bigram_model.put(new Bigram(new Token("cats", TokenType.WORD), new Token(".", TokenType.WORD)), 1.0);
		bigram_model.put(new Bigram(new Token("Pokemon", TokenType.WORD), new Token(".", TokenType.WORD)), 1.0);
		bigram_model.put(new Bigram(new Token("cookies", TokenType.WORD), new Token(".", TokenType.WORD)), 1.0);
		bigram_model.put(new Bigram(new Token("DeadMau5", TokenType.WORD), new Token(".", TokenType.WORD)), 1.0);
		bigram_model.put(new Bigram(new Token("dogs", TokenType.WORD), new Token(".", TokenType.WORD)), 1.0);
		bigram_model.put(new Bigram(new Token("Muppets", TokenType.WORD), new Token(".", TokenType.WORD)), 1.0);
		bigram_model.put(new Bigram(new Token(".", TokenType.WORD), new Token(null, TokenType.END)), 6.0);

		String sentence = Generator.randomBigramSentence(bigram_model);
		System.out.println(sentence);
		String sentence2 = Generator.randomUnigramSentence(unigram_model);
		System.out.println(sentence2);
	}
}
