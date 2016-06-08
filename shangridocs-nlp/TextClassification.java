
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

import com.datumbox.framework.applications.nlp.TextClassifier;
import com.datumbox.framework.common.Configuration;
import com.datumbox.framework.common.utilities.RandomGenerator;
import com.datumbox.framework.core.machinelearning.classification.MaximumEntropy;
import com.datumbox.framework.core.machinelearning.common.interfaces.ValidationMetrics;
import com.datumbox.framework.core.machinelearning.featureselection.categorical.ChisquareSelect;
import com.datumbox.framework.core.utilities.text.extractors.NgramsExtractor;



/**
 * Modified from: 
 * Text Classification example (@author Vasilis Vryniotis <bbriniotis@datumbox.com>)
 * 
 * Added f-score calculation
 * 
 * @author Asitang Mishra <asitang@jpl.nasa.gov>
 */
public class TextClassification {
	public static void IncrementHashMap(HashMap<String,Integer>dict,String key,Integer by){
		if(!key.equals("")){
		if(dict.containsKey(key))
		dict.put(key,dict.get(key)+by);
			else
				dict.put(key, by);
		}
	}
	
	   

    public static void main(String[] args) throws URISyntaxException, IOException {        
    	
    	 Writer writer = new BufferedWriter(
    			   new OutputStreamWriter(
    			   new FileOutputStream(
    			   "/Users/asitangmishra/Desktop/JPL/files/ccakes/out/results"),
    			   "utf-8"));
    	
        
        //Initialization
        //--------------
        RandomGenerator.setGlobalSeed(42L); //optionally set a specific seed for all Random objects
        Configuration conf = Configuration.getConfiguration(); //default configuration based on properties file
        
        
        //Reading Data
        //------------
        Map<Object, URI> datasets = new HashMap<>(); //The examples of each category are stored on the same file, one example per row.
        datasets.put("1", new File("/Users/asitangmishra/Desktop/JPL/files/ccakes/out/1").toURI());
        datasets.put("3", new File("/Users/asitangmishra/Desktop/JPL/files/ccakes/out/3").toURI());
       datasets.put("4", new File("/Users/asitangmishra/Desktop/JPL/files/ccakes/out/4").toURI());
        
        
        
        //Setup Training Parameters
        //-------------------------
        TextClassifier.TrainingParameters trainingParameters = new TextClassifier.TrainingParameters();
        
        //Classifier configuration
        trainingParameters.setModelerClass(MaximumEntropy.class);
        trainingParameters.setModelerTrainingParameters(new MaximumEntropy.TrainingParameters());
        
        //Set data transfomation configuration
        trainingParameters.setDataTransformerClass(null);
        trainingParameters.setDataTransformerTrainingParameters(null);
        
        //Set feature selection configuration
        trainingParameters.setFeatureSelectorClass(ChisquareSelect.class);
        trainingParameters.setFeatureSelectorTrainingParameters(new ChisquareSelect.TrainingParameters());
        
        //Set text extraction configuration
        trainingParameters.setTextExtractorClass(NgramsExtractor.class);
        trainingParameters.setTextExtractorParameters(new NgramsExtractor.Parameters());
        
        
        
        //Fit the classifier
        //------------------
        TextClassifier classifier = new TextClassifier("ccakes-analysis", conf);
        classifier.fit(datasets, trainingParameters);
        
        
        
        //Use the classifier
        //------------------
        
        
        //Get validation metrics on the training set
        ValidationMetrics vm = classifier.validate(datasets);
        classifier.setValidationMetrics(vm); //store them in the model for future reference
        
        HashMap<String,Integer>correct=new HashMap<String,Integer>();
    	HashMap<String,Integer>recall=new HashMap<String,Integer>();
    	HashMap<String,Integer>precision=new HashMap<String,Integer>();
    	
    	
        
        FileReader fileReader = new FileReader("/Users/asitangmishra/Desktop/JPL/files/ccakes/out/test");

        
		BufferedReader bufferedReader = new BufferedReader(fileReader);
		String line="";
		String classified="";
		while (( line = bufferedReader.readLine()) != null) {
			 String target=line.split("\t")[0];	
		 		
			
				classified=classifier.predict(line.split("\t")[1]).getYPredicted().toString();
				
				if(!classified.equals("4")){
				writer.write(target+"\t"+classified+"\n");
				}
				
				if((target).equals(classified)){
					
					
					IncrementHashMap(correct, target, 1);
					
				}
				IncrementHashMap(recall, target, 1);
				IncrementHashMap(precision, classified, 1);
				
				
		      
			
			
	        
		}
		writer.close();

		bufferedReader.close();
	String result="";
	double avgprecision=0.0;
	double avgrecall=0.0;
		
		    System.out.println("RECALL:");
		   
		    for(String temp:recall.keySet()){
		    	
		    	
		    	
		    	if(correct.containsKey(temp)){
		    		result=temp+" "+correct.get(temp)+" OUT OF ";
		    		System.out.println(result+recall.get(temp)+": "+(double)(correct.get(temp))/(double)(recall.get(temp)));
		    		avgrecall+=((double)(correct.get(temp))/(double)(recall.get(temp)));
		    	}
		    	else{
		    		result=temp+" 0 OUT OF ";
		    		System.out.println(""+recall.get(temp)+": 0.0");
		    		avgrecall+=0.0;	
		    	}
		    	
		    }
		    
		    System.out.println("avg: "+(avgrecall/recall.keySet().size()));
		    
		    
		    
		    
		    System.out.println("\n");
		    System.out.println("PRECISION:");
		    
for(String temp:precision.keySet()){
		    	
		    	
		    	
		    	if(correct.containsKey(temp)){
		    		result=temp+" "+correct.get(temp)+" OUT OF ";
		    		System.out.println(result+precision.get(temp)+": "+(double)(correct.get(temp))/(double)(precision.get(temp)));
		    		avgprecision+=((double)(correct.get(temp))/(double)(precision.get(temp)));
		    		
		    	}
		    	else{
		    		result=temp+" 0 OUT OF ";
		    		System.out.println(result+precision.get(temp)+": 0.0");
		    		avgprecision+=0.0;

		    	}
		    	
		    }
System.out.println("avg: "+(avgprecision/precision.keySet().size()));
        
        //Delete the classifier. This removes all files.
        classifier.delete();
    }
    
}
