

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

import org.apache.commons.io.FileUtils;

public class SplitMerge {

	public static void selectfeatures(String infolderpath,
			ArrayList<String> feats, String outfolderpath) throws IOException {
		final File folder = new File(infolderpath);
		String filename = "";
		for (final File fileEntry : folder.listFiles()) {

			filename = fileEntry.getName();

			if (filename.contains(".DS_Store")) {
				continue;
			}

			BufferedReader bufferedReader = new BufferedReader(new FileReader(
					fileEntry));
			BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(
					new FileOutputStream(outfolderpath + "/" + filename),
					"utf-8"));
			String newline = "";
			String line = "";
			String[] linearr = null;
			while ((line = bufferedReader.readLine()) != null) {

				if (line.equals(""))
					continue;

				newline = "";

				if (!line.contains("\t")) {
					linearr = line.split(" ");
				} else {
					linearr = line.split("\t")[1].split(" ");
				}

				for (String word : linearr) {
					for (String feat : feats) {
						if (word.contains(feat)) {
							newline += word + " ";

						}
					}
				}
				// System.out.println(filename+line);
				newline = newline.substring(0, newline.length() - 1);

				if (line.contains("\t")) {
					writer.write(line.split("\t")[0] + "\t" + newline + "\n");
				} else {
					writer.write(newline + "\n");
				}
			}
			writer.close();
			bufferedReader.close();

		}

	}

	public static void randomfilemerge(String infolderpath, String outfilepath,
			boolean addname) throws IOException {
		final File folder = new File(infolderpath);
		String filename = "";
		ArrayList<String> all = new ArrayList<String>();
		BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(
				new FileOutputStream(outfilepath), "utf-8"));

		for (final File fileEntry : folder.listFiles()) {

			filename = fileEntry.getName();
			if (filename.contains(".DS_Store")) {
				continue;
			}

			BufferedReader bufferedReader = new BufferedReader(new FileReader(
					fileEntry));

			String line = "";

			while ((line = bufferedReader.readLine()) != null) {

				if (line.equals(""))
					continue;

				if (addname) {
					all.add(filename + "\t" + line);
				} else {
					all.add(line);
				}

			}
			bufferedReader.close();

		}

		// randomize the lines now
		Collections.shuffle(all, new Random(4));

		for (String temp : all) {
			writer.write(temp + "\n");
		}
		writer.close();

	}

	public static void randomfilesplitbypercent(String infilepath,
			int splitaperc, int splitbperc) throws IOException {

		ArrayList<String> all = new ArrayList<String>();
		BufferedReader bufferedReader = new BufferedReader(new FileReader(
				infilepath));

		BufferedWriter writera = new BufferedWriter(new OutputStreamWriter(
				new FileOutputStream(infilepath + "_a"), "utf-8"));

		BufferedWriter writerb = new BufferedWriter(new OutputStreamWriter(
				new FileOutputStream(infilepath + "_b"), "utf-8"));

		String line = "";

		while ((line = bufferedReader.readLine()) != null) {

			all.add(line);

		}
		bufferedReader.close();

		int total = all.size();

		int splita = (splitaperc * total) / 100;
		int splitb = (splitbperc * total) / 100;

		// System.out.println(total+" "+splita+" "+splitb);

		// randomize the lines now
		Collections.shuffle(all, new Random(4));

		for (int i = 0; i < splita; i++) {
			writera.write(all.get(i) + "\n");
		}

		for (int i = splita; i < splita + splitb; i++) {
			writerb.write(all.get(i) + "\n");
		}

		writera.close();
		writerb.close();

	}

	public static void randomfilesplit(String infilepath, int splita, int splitb)
			throws IOException {
		ArrayList<String> all = new ArrayList<String>();
		BufferedReader bufferedReader = new BufferedReader(new FileReader(
				infilepath));

		BufferedWriter writera = new BufferedWriter(new OutputStreamWriter(
				new FileOutputStream(infilepath + "_a"), "utf-8"));

		BufferedWriter writerb = new BufferedWriter(new OutputStreamWriter(
				new FileOutputStream(infilepath + "_b"), "utf-8"));

		String line = "";

		while ((line = bufferedReader.readLine()) != null) {

			all.add(line);

		}
		bufferedReader.close();

		// randomize the lines now
		Collections.shuffle(all, new Random(4));

		for (int i = 0; i < splita; i++) {
			writera.write(all.get(i) + "\n");
		}

		for (int i = splita; i < splita + splitb; i++) {
			writerb.write(all.get(i) + "\n");
		}

		writera.close();
		writerb.close();

	}

	//copying files to in folder to merge them into a junk set
	public static void test() throws IOException {
		
		if (!new File("/Users/asitangmishra/Desktop/JPL/files/ccakes/in").exists()) {
			new File("/Users/asitangmishra/Desktop/JPL/files/ccakes/in").mkdir();
		}
		
		FileUtils.copyFile(new File(
				"/Users/asitangmishra/Desktop/JPL/files/ccakes/disease.junk"), new File(
				"/Users/asitangmishra/Desktop/JPL/files/ccakes/in/disease.junk"));
		FileUtils.copyFile(new File(
				"/Users/asitangmishra/Desktop/JPL/files/ccakes/protein.junk"), new File(
				"/Users/asitangmishra/Desktop/JPL/files/ccakes/in/protein.junk"));
		
		
		//merge the files in the in folder to create a junk set
		String infolderpath = "/Users/asitangmishra/Desktop/JPL/files/ccakes/in";
		String outfilepath = "/Users/asitangmishra/Desktop/JPL/files/ccakes/junk.train";
		randomfilemerge(infolderpath, outfilepath, false);

		//splitting files into training and test sets for validation
		int splita = 100;
		int splitb = 2000;

		String infilepath = "/Users/asitangmishra/Desktop/JPL/files/ccakes/disease.train";
		randomfilesplit(infilepath, splita, splitb);
		

		infilepath = "/Users/asitangmishra/Desktop/JPL/files/ccakes/protein.train";
		randomfilesplit(infilepath, splita, splitb);
		
		infilepath = "/Users/asitangmishra/Desktop/JPL/files/ccakes/junk.train";
		randomfilesplit(infilepath, splita, splitb);
		
		if (new File("/Users/asitangmishra/Desktop/JPL/files/ccakes/test").exists()) {
			// do cleaning
			FileUtils.deleteDirectory(new File(
					"/Users/asitangmishra/Desktop/JPL/files/ccakes/test"));
			FileUtils.deleteDirectory(new File(
					"/Users/asitangmishra/Desktop/JPL/files/ccakes/out"));
			
		}

		// create new diectories
		new File("/Users/asitangmishra/Desktop/JPL/files/ccakes/test").mkdir();
		new File("/Users/asitangmishra/Desktop/JPL/files/ccakes/out").mkdir();

		//copy the test files into test folder to merge them
		FileUtils.copyFile(new File(
				"/Users/asitangmishra/Desktop/JPL/files/ccakes/junk.train_a"), new File(
				"/Users/asitangmishra/Desktop/JPL/files/ccakes/test/4"));
		FileUtils.copyFile(new File(
				"/Users/asitangmishra/Desktop/JPL/files/ccakes/disease.train_a"),
				new File("/Users/asitangmishra/Desktop/JPL/files/ccakes/test/1"));
		FileUtils.copyFile(new File(
				"/Users/asitangmishra/Desktop/JPL/files/ccakes/protein.train_a"),
				new File("/Users/asitangmishra/Desktop/JPL/files/ccakes/test/3"));
	
		//merge the test files and put it into out folder
		infolderpath = "/Users/asitangmishra/Desktop/JPL/files/ccakes/test";
		outfilepath = "/Users/asitangmishra/Desktop/JPL/files/ccakes/out/test";
		randomfilemerge(infolderpath, outfilepath, true);

		//copy the training files to test folder (overwriting the test files)
		FileUtils.copyFile(new File(
				"/Users/asitangmishra/Desktop/JPL/files/ccakes/junk.train_b"), new File(
				"/Users/asitangmishra/Desktop/JPL/files/ccakes/test/4"));
		FileUtils.copyFile(new File(
				"/Users/asitangmishra/Desktop/JPL/files/ccakes/disease.train_b"),
				new File("/Users/asitangmishra/Desktop/JPL/files/ccakes/test/1"));
		FileUtils.copyFile(new File(
				"/Users/asitangmishra/Desktop/JPL/files/ccakes/protein.train_b"),
				new File("/Users/asitangmishra/Desktop/JPL/files/ccakes/test/3"));
		
		// selecting features: take all files from test folder, keep selected features then put them in the out folder
		// ------------
		infolderpath = "/Users/asitangmishra/Desktop/JPL/files/ccakes/test";
		String outfolderpath = "/Users/asitangmishra/Desktop/JPL/files/ccakes/out";
		ArrayList<String> feats = new ArrayList<String>();
		feats.add("oword");
		feats.add("odtag");
		feats.add("ogtag");
		feats.add("odreltag");
		feats.add("ogreltag");
		feats.add("orelword");
		feats.add("otagreltag");
		feats.add("otagrelword");

		feats.add("itagreltag");
		feats.add("itagrelword");
		feats.add("irelword");
		feats.add("idreltag");
		feats.add("igreltag");
		feats.add("iword");
		selectfeatures(infolderpath, feats, outfolderpath);

	}

	
	public static void main(String[] args) throws IOException {

		test();

	}

}
