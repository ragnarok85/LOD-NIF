package lod.nif.main;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.compress.compressors.CompressorException;
import org.apache.commons.compress.compressors.CompressorInputStream;
import org.apache.commons.compress.compressors.CompressorStreamFactory;
import org.apache.log4j.Logger;
import org.rdfhdt.hdt.exceptions.ParserException;

public class Main3 {

	static final Logger logger = Logger.getLogger(Main.class);

	/*
	 * args[0] - Context, page and links (in that order) bz2 files. 
	 * args[1] - temporal output folder 
	 * args[2] - original URI e.g. <http://dbpedia.org/resource/
	 * args[3] - replace URI e.g. <http://simple.dbpedia.org/resource/
	 * 
	 */

	//<http://dbpedia.org/resource/
	//<http://simple.dbpedia.org/resource/
	private String replaceUri = "";
	public static void main(String... args) throws CompressorException,
			IOException, NoSuchAlgorithmException, DecoderException, ParserException {
		// BasicConfigurator.configure(); // Logger was not working
		Main3 main = new Main3();
		long initialTime = System.currentTimeMillis();
		String pathBZ2 = args[0];
		String outputFileName = args[1];

		String originalUri = args[2];
		main.replaceUri = args[3];

		BufferedReader bz2 = main
				.getBufferedReaderForCompressedFile(pathBZ2);

		logger.info("Starting Replacement process");
		main.createTempFiles(bz2, outputFileName,	originalUri);

//		LOD lod = new LOD();
//		StringReader sr = new StringReader(ListUtils.str(list.toArray()));
		
		//lod.justWrite(outputFileName, list);

		long endTime = System.currentTimeMillis() - initialTime;
		String timeElapsed = String.format(
				"TOTAL TIME = %d min, %d sec",
				TimeUnit.MILLISECONDS.toMinutes(endTime),
				TimeUnit.MILLISECONDS.toSeconds(endTime)
						- TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS
								.toMinutes(endTime)));
		logger.info(timeElapsed);
	}

	public List<String> readListFromFile(File pathList) {
		List<String> list = new ArrayList<String>();
		try (BufferedReader br = new BufferedReader(new InputStreamReader(
				new FileInputStream(pathList), StandardCharsets.UTF_8))) {
			String line;
			while ((line = br.readLine()) != null) {
				list.add(line);
			}
			br.close();
		} catch (IOException e) {

		}
		return list;
	}

	public BufferedReader getBufferedReaderForCompressedFile(String fileIn)
			throws FileNotFoundException, CompressorException {
		FileInputStream fin = new FileInputStream(fileIn);
		BufferedInputStream bis = new BufferedInputStream(fin);
		CompressorInputStream input = new CompressorStreamFactory()
				.createCompressorInputStream(bis);
		BufferedReader br2 = new BufferedReader(new InputStreamReader(input));
		return br2;
	}

	Set<String> setArticles = new HashSet<String>();
	
	public void createTempFiles(BufferedReader br, String outputFolder,
			String originalUri) throws IOException,
			DecoderException {


		extractArticleLines(br, outputFolder, originalUri);
		
	}

	public void extractArticleLines(BufferedReader br,
			String outputFileName,String originalUri) throws IOException, DecoderException {
		String line = "";
		int counter = 0;
		int limit = 0;
		LOD lod = new LOD();
		List<String> lines = new ArrayList<String>();
		while ((line = br.readLine())!= null) {
			long initialTime = System.currentTimeMillis();
			if (!line.startsWith("<")) {
				continue;
			}

			line = line.replace(originalUri, replaceUri);
			lines.add(line);
			counter++;
			limit++;
			if(limit == 4500000){
				logger.info("number of lines processed: " +counter);
				limit = 0;
				lod.justWrite(outputFileName, lines);
				lines.clear();
				long endTime = System.currentTimeMillis() - initialTime;
				String timeElapsed = String.format(
						"TOTAL TIME = %d min, %d sec",
						TimeUnit.MILLISECONDS.toMinutes(endTime),
						TimeUnit.MILLISECONDS.toSeconds(endTime)
								- TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS
										.toMinutes(endTime)));
				logger.info(timeElapsed);
			}
		}
		if(lines.size() > 0){
			logger.info("number of lines processed: " +counter);
			lod.justWrite(outputFileName, lines);
			lines.clear();
		}
	}
	

	public void extractArticlesProcessed(String reportDirectory) {
		String link = "";
		String context = "";
		String page = "";
		for (String a : setArticles) {
			String[] splitA = a.split("999999");
			if (splitA[0].equalsIgnoreCase("context")) {
				context += splitA[1] + "\n";
			} else if (splitA[0].equalsIgnoreCase("page")) {
				page += splitA[1] + "\n";
			} else if (splitA[0].equalsIgnoreCase("link")) {
				link += splitA[1] + "\n";
			}
		}

		writeReport(reportDirectory, "link", link);
		writeReport(reportDirectory, "page", page);
		writeReport(reportDirectory, "context", context);
	}

	public String generateName(String uri, String newUri) {
//		if(replacement)
//			originalUri = replaceUri;
		uri = uri.split("\\?")[0];
		// uri = uri.replace("<http://simple.dbpedia.org/resource/", "");
		uri = uri.replace(newUri, "");
		uri = uri.split("\\?")[0].replace("/", "_____");
		return uri;
	}

	
	public void printLines(List<String> lines) {
		for(String line : lines) {
			System.out.println(line);
		}
	}

	public void writeReport(String reportDirectory, String reportName,
			String reportData) {
		String output = reportDirectory + "/" + reportName;
		try (PrintWriter pw = new PrintWriter(new OutputStreamWriter(
				new FileOutputStream(output), StandardCharsets.UTF_8))) {
			pw.write(reportData);
			pw.close();
		} catch (IOException e) {

		}
	}

}
