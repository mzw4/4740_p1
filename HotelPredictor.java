import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class HotelPredictor {

	public static void main(String[] args) {
		
		Parser trueReviewParser = new Parser(args[0]);
		Parser falseReviewParser = new Parser(args[0]);
		File trainFile = new File(args[0]);
		File testFile = new File(args[1]);

		try{
			String content = new String(Files.readAllBytes(Paths.get(trainFile.getAbsolutePath())));
			String marked = content.replaceAll("1,[0-9],", "AJXXYTRUE ").replaceAll("0,[0-9],", "AJXXYFALSE ");
			
			String[] chunks = marked.split("AJXXY");
			
			//Parse true and false reviews.
			for(int a = 0; a < chunks.length; a++) {
				String[] words = chunks[a].split("\\s+");
				if(words[0].equals("TRUE")) trueReviewParser.processChunk(chunks[a]);
				else if(words[0].equals("FALSE")) falseReviewParser.processChunk(chunks[a]);
				else System.out.println("No reading for a chunk.");
			}
			
			//Smooth the n-grams.
			trueReviewParser.smoothUnigrams();
			trueReviewParser.smoothBigrams();
			falseReviewParser.smoothUnigrams();
			falseReviewParser.smoothBigrams();
			
			
			String testData = new String(Files.readAllBytes(Paths.get(testFile.getAbsolutePath())));
			
			String[] testChunks = testData.split("\\?,.*;\"");
			
			for(int a = 1; a < testChunks.length; a++) {
				if(trueReviewParser.computeBigramPerplexity(testChunks[a]) < falseReviewParser.computeBigramPerplexity(testChunks[a])) {
					System.out.println((a - 1) + ", 1");
				}
				else {
					System.out.println((a - 1) + ", 0");
				}
			}
		}
		catch(FileNotFoundException e) {
			System.out.println("File not found.");
		}
		catch(IOException e) {
			System.out.println("IOException.");
		}
	}

}
