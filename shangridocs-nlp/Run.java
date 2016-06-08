

import java.io.IOException;
import java.net.URISyntaxException;

public class Run {
	
	
	public static void main(String[] args) throws IOException, URISyntaxException {
		
		SplitMerge.main(args);
		
		DataCollect.createTestData("/Users/asitangmishra/Desktop/JPL/files/ccakes/testfile.txt", "/Users/asitangmishra/Desktop/JPL/files/ccakes/out/test");
			
		TextClassification.main(args);
	
	}
}
