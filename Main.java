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
		
		Parser parser = new Parser(args[0]);
		Scanner inScanner = new Scanner(System.in);
				
		// Identify what type of corpus is being processed.
		System.out.println("If this is a Bible corpus, please enter 'b'.");
		System.out.println("If this is a hotel review corpus, please enter 'h'.");
		System.out.println("For any arbitrary text file that requires no preprocessing, enter any other character");
		String input = inScanner.nextLine();
		parser.processCorpus(input);
		System.out.println("Corpus processed!");
		System.out.println("Size of unigram HashMap is " + parser.getUnigrams().size());
		System.out.println("Size of bigram HashMap is " + parser.getBigrams().size());

		// Display parser dump
		//parser.unigramDump();
		//parser.bigramDump();
		
		// Generate random sentences
		System.out.println("To see a random sentence generated based on the unigrams of the given corpus, enter 'u'.");
		System.out.println("To see a random sentence generated based on the bigrams of the given corpus, enter 'b'.");
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