import java.util.Scanner;

public class Main {
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
		
		Parser parser = new Parser("./HotelReviews/reviews.train");
		//Parser parser = new Parser(args[0]);
		Scanner inScanner = new Scanner(System.in);
				
		String corpus_type = "";
		// Identify what type of corpus is being processed.
		System.out.println("If this is a Bible corpus, please enter 'b'.");
		System.out.println("If this is a hotel review corpus, please enter 'h'.");
		System.out.println("For any arbitrary text file that requires no preprocessing, enter any other character");
		String input = inScanner.nextLine();
		corpus_type = input;
		parser.processCorpus(corpus_type, false);
		System.out.println("Corpus processed!");
		System.out.println("Size of unigram HashMap is " + parser.getUnigrams().size());
		System.out.println("Size of bigram HashMap is " + parser.getBigrams().size());
		
		//Testing smoothing
		parser.smoothUnigrams();
		System.out.println("Smoothed unigrams.");
		parser.smoothBigrams();
		System.out.println("Smoothed bigrams.");
		parser.smoothTrigrams();
		System.out.println("Smoothed trigrams.");
		

		// Display parser dump
		//parser.unigramDump();
		//parser.bigramDump();
		
		// Generate random sentences
		System.out.println("To see a random sentence generated based on the unigrams of the given corpus, enter 'u'.");
		System.out.println("To see a random sentence generated based on the bigrams of the given corpus, enter 'b'.");
		System.out.println("To calculate perplexity on a test corpus, enter 'p'.");
		System.out.println("To exit, enter 'x'.");
		
		boolean running = true;
		while(running) {
			input = inScanner.next();
			
			switch(input) {
			case "u":
				System.out.println(Generator.randomUnigramSentence(parser.getUnigrams()));
				break;
			case "b":
				System.out.println(Generator.randomBigramSentence(parser.getBigrams()));
				break;
			case "p":
				System.out.println("Please enter the path to the desired test corpus");
				//input = inScanner.next();
				parser.setFile("./HotelReviews/reviews.test");
				//parser.setFile("./bible_corpus/kjbible.test");
				parser.processCorpus(corpus_type, true);;
				break;
			case "x":
				System.out.println("Have a good day!");
				running = false;
				break;
			default: 
				System.out.println("Unrecognized input. Please try again.");
				break;
			}
		}
		inScanner.close();
	}
}
