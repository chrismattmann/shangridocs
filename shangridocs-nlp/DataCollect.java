
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import org.jsoup.Connection.Response;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;

import edu.stanford.nlp.ling.HasWord;
import edu.stanford.nlp.ling.IndexedWord;
import edu.stanford.nlp.ling.TaggedWord;
import edu.stanford.nlp.parser.nndep.DependencyParser;
import edu.stanford.nlp.process.DocumentPreprocessor;
import edu.stanford.nlp.tagger.maxent.MaxentTagger;
import edu.stanford.nlp.trees.GrammaticalStructure;
import edu.stanford.nlp.trees.TypedDependency;

public class DataCollect {

	public static void updatemap(
			HashMap<IndexedWord, ArrayList<IndexedWord>> map,
			TypedDependency rel) {

		if (map.containsKey(rel.gov())) {
			map.get(rel.gov()).add(rel.dep());
		} else {
			ArrayList<IndexedWord> temp = new ArrayList<IndexedWord>();
			temp.add(rel.dep());
			map.put(rel.gov(), temp);
		}
	}

	/**
	 * Iterates through the typed dependencies. Aggregates all the deps that are
	 * under the same gov. Can be queried through the gov.
	 * 
	 * @param tagged
	 * @return
	 */

	public static HashMap<IndexedWord, ArrayList<IndexedWord>> createInvertedRelationsMap(
			ArrayList<TypedDependency> tagged) {

		HashMap<IndexedWord, ArrayList<IndexedWord>> invertedmap = new HashMap<IndexedWord, ArrayList<IndexedWord>>();
		for (TypedDependency rel : tagged) {
			updatemap(invertedmap, rel);
		}
		return invertedmap;
	}

	/**
	 * This method makes clusters of words to create noun phrases
	 * 
	 * @param op
	 *            : The list of kind of pos tags you want to put in the island
	 * @param td
	 * @param gs
	 * @return
	 */
	public static ArrayList<ArrayList<TypedDependency>> makeIslands(
			ArrayList<String> op, ArrayList<TypedDependency> td,
			GrammaticalStructure gs) {
		boolean lastflag = false;
		IndexedWord lastword = null;
		TypedDependency lastdepend = null;
		ArrayList<TypedDependency> island = null;
		ArrayList<ArrayList<TypedDependency>> master = new ArrayList<ArrayList<TypedDependency>>();
		master.add(null);
		int previndex = 0;

		for (TypedDependency t : td) {
			if (t.dep().index() == previndex) // do not process a dep with
				// multiple incoming edges (or will create a loop)
				continue;

			// if the word contains relevant tags: put into islands
			if (op.contains(t.dep().tag().toString())) {
				if (!lastflag) { // last words was not a relevant tag: so start
									// a new island
					island = new ArrayList<TypedDependency>();
					// special case: check before making a new island: if the
					// previous word was an adjective modifier
					if (lastword != null
							&& (lastword.tag().equals("VBN") || lastword.tag()
									.equals("ADJ"))
							&& (gs.getGrammaticalRelation(t.dep(), lastword)
									.toString().equals("amod"))) {
						// add the previous word
						island.add(lastdepend);
						master.add(lastword.index(), island); // Note: each word
																// index contain
																// now the
																// location of
																// the island it
																// belongs to
					}
					// now add the current word
					island.add(t);
					master.add(t.dep().index(), island);
					lastflag = true;
					lastword = t.dep();
					lastdepend = t;
				}

				else { // last words was a relevant tag: add the current word to
						// the last island
					island.add(t);
					master.add(t.dep().index(), island);
					lastword = t.dep();
					lastdepend = t;
					lastflag = true;

				}

			}
			// if the word contains irrelevant tags
			else {
				lastflag = false;
				lastword = t.dep();
				master.add(t.dep().index(), null); // adding null to that word
													// index position, as it has
													// no island to belong to
				lastdepend = t;
			}

			previndex = t.dep().index();

		}

		return master;
	}

	/**
	 * Gives the position of small in big if big contains small. Returns -1 if it does not.
	 * @param big
	 * @param small
	 * @return
	 */
	public static int findEntityPosition(List<HasWord> big, List<HasWord> small) {

		if (big == null || small == null || big.size() < small.size()) {
			return -1;
		}

		String s = "";
		String b = "";
		String c = "";

		for (HasWord ts : small) {
			s += "<DELIM>" + ts.toString() + "<ELIM>";
		}

		for (HasWord tb : big) {
			b += "<DELIM>" + tb.toString() + "<ELIM>";
		}

		int index = b.indexOf(s);
		
		if (index != -1) {
			c = b.substring(0, index);
			return c.split("<DELIM>").length - 1;

		}

		return -1;
	}

	public static HashMap<ArrayList<TypedDependency>, String> extractallfeatures(
			List<HasWord> sentence, MaxentTagger tagger, DependencyParser parser) {

		List<TaggedWord> tagged = tagger.tagSentence(sentence);
		GrammaticalStructure gs = parser.predict(tagged);
		ArrayList<TypedDependency> td = (ArrayList<TypedDependency>) gs
				.typedDependenciesCollapsedTree();
		HashMap<IndexedWord, ArrayList<IndexedWord>> invertedmap = createInvertedRelationsMap(td);

		ArrayList<String> ops = new ArrayList<String>();
		ops.add("NN");
		ops.add("JJ");
		ops.add("NNS");
		ops.add("NNP");

		ArrayList<ArrayList<TypedDependency>> islands = makeIslands(ops, td, gs);
		HashSet<ArrayList<TypedDependency>> uniqueislands = new HashSet<ArrayList<TypedDependency>>();

		HashMap<ArrayList<TypedDependency>, String> trainlines = new HashMap<ArrayList<TypedDependency>, String>();

		String trainline = "";

		for (ArrayList<TypedDependency> island : islands) {
			if (island != null) {
				uniqueislands.add(island);
			}
		}

		for (ArrayList<TypedDependency> uniqueisland : uniqueislands) {

			trainline = "";
			ArrayList<String> features = new ArrayList<String>();
			featurize(false, gs, features, invertedmap, uniqueisland, islands);

			for (String feature : features) {
				trainline += feature + " ";
			}
			if (!trainline.equals("")) {
				trainline = trainline.substring(0, trainline.length() - 1);
			}

			trainlines.put(uniqueisland, trainline);

		}

		return trainlines;

	}

	public static void featurize(boolean intra, GrammaticalStructure gs,
			ArrayList<String> features,
			HashMap<IndexedWord, ArrayList<IndexedWord>> invertedmap,
			ArrayList<TypedDependency> island,
			ArrayList<ArrayList<TypedDependency>> islands) {

		String dtag = "";
		String rel = "";
		String gword = "";
		String gtag = "";
		//String word = "";

		for (TypedDependency t : island) {

			if (t != null) {

				if (intra
						|| !islands.get(t.dep().index()).equals(
								islands.get(t.gov().index()))) { // if both the
																	// dep and
																	// gov have
																	// the same
																	// island
																	// then dont
																	// take the
																	// relation
																	// in
																	// consideration

					dtag = t.dep().tag().toString();
					rel = t.reln().toString();

					if (t.gov().word() != null) {
						gword = t.gov().word().toString();
						gtag = t.gov().tag().toString();

					} else {
						gword = "R00T";
						gtag = "R00T";

					}
					features.add("dword" + t.dep().word().toString());
					features.add("oword" + gword);
					features.add("odtag" + dtag);
					features.add("ogtag" + gtag);

					features.add("odreltag" + rel + dtag);
					features.add("ogreltag" + rel + gtag);
					features.add("orelword" + rel + gword);

					features.add("otagreltag" + dtag + rel + gtag); // from me
																	// (t.dep)
					// to gov
					features.add("otagrelword" + dtag + rel + gword);

				}

				if (invertedmap.containsKey(t.dep())) {
					for (IndexedWord deps : invertedmap.get(t.dep())) {
						if (intra
								|| !islands.get(t.dep().index()).equals(
										islands.get(deps.index()))) { // if the
																		// current
																		// dep
																		// is a
																		// gov
																		// to
																		// others
																		// (deps)
																		// and
																		// they
																		// belong
																		// to
																		// the
																		// same
																		// island:
																		// dont
																		// consider
																		// it

							features.add("itagreltag"
									+ dtag
									+ gs.getGrammaticalRelation(t.dep(), deps)
											.toString() + deps.tag().toString());// from
							// me
							// (is
							// represented
							// by
							// a
							// t.dep)
							// to
							// dep
							features.add("itagrelword"
									+ dtag
									+ gs.getGrammaticalRelation(t.dep(), deps)
											.toString()
									+ deps.word().toString());
							features.add("irelword"
									+ gs.getGrammaticalRelation(t.dep(), deps)
											.toString()
									+ deps.word().toString());
							features.add("idreltag"
									+ gs.getGrammaticalRelation(t.dep(), deps)
											.toString() + deps.tag().toString());
							features.add("igreltag"
									+ gs.getGrammaticalRelation(t.dep(), deps)
											.toString() + dtag);
							features.add("iword" + deps.word().toString());

						}
					}
				}

			}

		}

	}

	public static ArrayList<String> extractweedfeatures(String entity,
			List<HasWord> sentence, MaxentTagger tagger, DependencyParser parser) {
		DocumentPreprocessor tokenizer = new DocumentPreprocessor(
				new StringReader(entity));
		List<HasWord> convEntity = null;

		for (List<HasWord> ent : tokenizer) {
			convEntity = ent;
			break;

		}

		int entitypos = findEntityPosition(sentence, convEntity);
		int entitylength = convEntity.size();

		List<TaggedWord> tagged = tagger.tagSentence(sentence);
		GrammaticalStructure gs = parser.predict(tagged);
		ArrayList<TypedDependency> td = (ArrayList<TypedDependency>) gs
				.typedDependenciesCollapsedTree();
		HashMap<IndexedWord, ArrayList<IndexedWord>> invertedmap = createInvertedRelationsMap(td);

		ArrayList<String> ops = new ArrayList<String>();
		ops.add("NN");
		ops.add("JJ");
		ops.add("NNS");
		ops.add("NNP");

		ArrayList<ArrayList<TypedDependency>> islands = makeIslands(ops, td, gs);
		HashSet<ArrayList<TypedDependency>> uniqueislands = new HashSet<ArrayList<TypedDependency>>();

		int[] weedIlands = new int[islands.size()];

		// remove the islands that have even one of their words coming in the
		// entity range

		// removing the entity words: marking them as 1

		for (int i = entitypos + 1; i <= entitypos + entitylength; i++) {
			weedIlands[i] = 1;
		}

		for (ArrayList<TypedDependency> island : islands) {
			if (island != null && weedIlands[islands.indexOf(island)] != 1) {
				uniqueislands.add(island);
			}
		}

		ArrayList<String> trainlines = new ArrayList<String>();
		String trainline = "";

		for (ArrayList<TypedDependency> uniqueisland : uniqueislands) {

			trainline = "";
			ArrayList<String> features = new ArrayList<String>();
			featurize(false, gs, features, invertedmap, uniqueisland, islands);

			for (String feature : features) {
				trainline += feature + " ";
			}
			if (!trainline.equals("")) {
				trainline = trainline.substring(0, trainline.length() - 1);
			}

			trainlines.add(trainline);
		}

		return trainlines;

	}

	public static ArrayList<String> extractfeatures(String entity,
			List<HasWord> sentence, MaxentTagger tagger, DependencyParser parser) {
		// System.out.println("HI"+entity);
		DocumentPreprocessor tokenizer = new DocumentPreprocessor(
				new StringReader(entity));
		List<HasWord> convEntity = null;

		for (List<HasWord> ent : tokenizer) {
			convEntity = ent;
			break;

		}

		int entitypos = findEntityPosition(sentence, convEntity);
		// System.out.println(sentence);
		// System.out.println(entitypos);
		ArrayList<String> features = new ArrayList<String>();
		if (entitypos != -1) {

			int entitylength = convEntity.size();
			// System.out.println(entitylength);

			List<TaggedWord> tagged = tagger.tagSentence(sentence);
			GrammaticalStructure gs = parser.predict(tagged);
			ArrayList<TypedDependency> td = (ArrayList<TypedDependency>) gs
					.typedDependenciesCollapsedTree();
			HashMap<IndexedWord, ArrayList<IndexedWord>> invertedmap = createInvertedRelationsMap(td);
			// extract features and make a training set by concatenation them

			// create the island ds: each dep index points to the island that it
			// belongs to: here only the entity is an island rest are null
			ArrayList<ArrayList<TypedDependency>> islands = new ArrayList<ArrayList<TypedDependency>>();
			ArrayList<TypedDependency> uniqueisland = new ArrayList<TypedDependency>();
			islands.add(null);
			for (int i = 1; i <= td.size(); i++) {

				if (i >= entitypos + 1 && i <= entitypos + entitylength) {
					uniqueisland.add(td.get(i - 1));
					islands.add(i, uniqueisland);
				} else {

					islands.add(i, null);
				}
			}
			try {
				featurize(false, gs, features, invertedmap, uniqueisland,
						islands);
			} catch (Exception e) {
				System.out.println("ERROR WHILE FEATURIZING:" + sentence);
			}

		}
		return features;

	}

	private static String recreateSentence(List<HasWord> sentence) {
		StringBuilder result = new StringBuilder();
		if (!sentence.isEmpty()) {
			HasWord word = sentence.get(0);
			String s = word.word();
			result.append(s);
			for (int i = 1, sz = sentence.size(); i < sz; i++) {
				word = sentence.get(i);
				s = word.word();
				result.append(" ").append(s);
			}
		}
		return result.toString();
	}

	public static HashMap<String, ArrayList<List<HasWord>>> fetch(
			ArrayList<String> urllist) throws IOException {
		Writer writer = new BufferedWriter(
				new OutputStreamWriter(
						new FileOutputStream(
								"/Users/asitangmishra/Desktop/JPL/files/entities-file"),
						"utf-8"));
		HashMap<String, ArrayList<List<HasWord>>> entityDesc = new HashMap<String, ArrayList<List<HasWord>>>();
		ArrayList<List<HasWord>> desc;
		Response response;
		Document html_content;

		String text = "";
		String tempsent = "";

		for (String url : urllist) {
			response = Jsoup.connect(url).followRedirects(true).execute();
			html_content = response.parse();

			text = html_content.getElementById("mw-content-text").text();
			text = text.toLowerCase();

			DocumentPreprocessor tokenizer = new DocumentPreprocessor(
					new StringReader(text));
			desc = new ArrayList<List<HasWord>>();

			for (List<HasWord> sentence : tokenizer) {

				tempsent = recreateSentence(sentence);

				if (tempsent.contains(html_content
						.getElementById("firstHeading").text().toLowerCase())) {
					writer.write(html_content.getElementById("firstHeading")
							.text().toLowerCase()
							+ "\t" + tempsent + "\n");
					desc.add(sentence);

				}

			}

			entityDesc.put(html_content.getElementById("firstHeading").text()
					.toLowerCase(), desc);
			// break;
		}
		writer.close();
		return entityDesc;

	}

	public static ArrayList<String> linksExtractor1(String url)
			throws InterruptedException, UnsupportedEncodingException {
		WebDriver driver = new FirefoxDriver();

		ArrayList<String> urllist = new ArrayList<String>();
		String link = "";
		driver.get(url);
		boolean next = true;

		Thread.sleep(3000);

		while (next) {

			List<WebElement> atags = driver.findElement(By.id("mw-pages"))
					.findElement(By.xpath(".//div[@class='mw-category']"))
					.findElements(By.xpath(".//a"));
			for (WebElement atag : atags) {
				link = atag.getAttribute("href").toString();
				if (!link.contains("redlink=1")) {// //system.out.println(link);
					urllist.add(URLDecoder.decode(atag.getAttribute("href")
							.toString(), "UTF-8"));
				}

			}

			// click next
			if (driver.findElements(By.xpath("id('mw-pages')/a")).size() == 4) {
				driver.findElement(By.xpath("id('mw-pages')/a[2]")).click();
				Thread.sleep(3000);
			} else if ((driver.findElements(By.xpath("id('mw-pages')/a"))
					.size() == 2)
					&& (driver.findElement(By.xpath("id('mw-pages')/a[1]"))
							.getText().equals("next page"))) {
				driver.findElement(By.xpath("id('mw-pages')/a[1]")).click();
				Thread.sleep(3000);
			} else {
				next = false;
			}
		}
		driver.quit();

		return urllist;

	}

	public static ArrayList<String> linksExtractor2(String url)
			throws InterruptedException, UnsupportedEncodingException {

		WebDriver driver = new FirefoxDriver();
		ArrayList<String> urllist = new ArrayList<String>();
		String link = "";
		driver.get(url);

		Thread.sleep(3000);

		List<WebElement> uls = driver.findElement(By.id("mw-content-text"))
				.findElements(By.xpath("./ul"));

		for (WebElement ul : uls) {
			List<WebElement> atags = ul.findElements(By.xpath(".//a"));

			for (WebElement atag : atags) {
				link = atag.getAttribute("href").toString();
				if (!link.contains("redlink=1")) {
					urllist.add(URLDecoder.decode(atag.getAttribute("href")
							.toString(), "UTF-8"));
				}

			}

		}

		driver.quit();

		return urllist;

	}

	public static void createTrainingData(String folderpath,String classname,

			ArrayList<String> wikiurllist) throws IOException {

		HashMap<String, ArrayList<List<HasWord>>> entityDesc = fetch(wikiurllist);

		String modelPath = DependencyParser.DEFAULT_MODEL;
		String taggerPath = "edu/stanford/nlp/models/pos-tagger/english-left3words/english-left3words-distsim.tagger";
		MaxentTagger tagger = new MaxentTagger(taggerPath);
		DependencyParser parser = DependencyParser.loadFromModelFile(modelPath);

		Writer writer = new BufferedWriter(
				new OutputStreamWriter(
						new FileOutputStream(
								folderpath+"/"+classname+".train"),
						"utf-8"));
		Writer writer2 = new BufferedWriter(
				new OutputStreamWriter(
						new FileOutputStream(
								folderpath+"/"+classname+".junk"),
						"utf-8"));

		ArrayList<String> features;
		String trainline = "";
		for (String key : entityDesc.keySet()) {

			for (List<HasWord> sentence : entityDesc.get(key)) {
				if (entityDesc.get(key).indexOf(sentence) == 0
						|| entityDesc.get(key).indexOf(sentence) == 1) {
					continue; // do not consider the first two sentence from
								// each page of wikipedia, cause it is a useless
								// description line.
				}
				trainline = "";
				features = extractfeatures(key, sentence, tagger, parser);
				for (String feature : features) {
					trainline += feature + " ";
				}
				if (!trainline.equals("")) {
					trainline = trainline.substring(0, trainline.length() - 1);
					writer.write(trainline + "\n");
				}

				ArrayList<String> testlines = extractweedfeatures(key,
						sentence, tagger, parser);

				for (String testline : testlines) {
					writer2.write(testline + "\n");
				}
			}

		}

		writer.close();
		writer2.close();

	}
public static void createTestData(String infilepath, String outfilepath) throws IOException{
		
		String fulltext = "";

		String line = null;

		FileReader fileReader = new FileReader(infilepath);

		BufferedReader bufferedReader = new BufferedReader(fileReader);

		Writer writer = new BufferedWriter(
				new OutputStreamWriter(
						new FileOutputStream(
								outfilepath),
						"utf-8"));

		while ((line = bufferedReader.readLine()) != null) {
			fulltext +=" "+ line;
		}

		bufferedReader.close();

		String modelPath = DependencyParser.DEFAULT_MODEL;
		String taggerPath = "edu/stanford/nlp/models/pos-tagger/english-left3words/english-left3words-distsim.tagger";
		MaxentTagger tagger = new MaxentTagger(taggerPath);
		DependencyParser parser = DependencyParser.loadFromModelFile(modelPath);
		DocumentPreprocessor tokenizer = new DocumentPreprocessor(
				new StringReader(fulltext));

		HashMap<ArrayList<TypedDependency>, String> features;
		for (List<HasWord> sentence : tokenizer) {

			features = DataCollect.extractallfeatures(sentence, tagger, parser);
			
			for (ArrayList<TypedDependency> island : features.keySet()) {

				String word = "";
				for (TypedDependency temp : island) {
					if (temp != null) {

						word += " " + temp.dep().word().toString();
					}
				}
				writer.write(word + "\t" + features.get(island) + "\n");
			}

		}

		writer.close();
	
	}
	
	public static void main(String[] args) throws InterruptedException,
			IOException {

		
		String folder="/Users/asitangmishra/Desktop/JPL/files/ccakes";
		
		
		String url = "https://en.wikipedia.org/wiki/List_of_diseases_%28A%29"; //disease
		ArrayList<String> urllist = linksExtractor1(url);
		createTrainingData(folder,"disease", urllist);
		
		url = "https://en.wikipedia.org/wiki/Category:Proteins"; //protein
		urllist = linksExtractor2(url);
		createTrainingData(folder,"protein", urllist);
		
//		url = "https://en.wikipedia.org/w/index.php?title=Category:Enzymes&pageuntil=Oxygenase#mw-pages"; //enzyme
//		urllist = linksExtractor2(url);
//		createTrainingData(folder,"enzyme", urllist);

	
	}

}
