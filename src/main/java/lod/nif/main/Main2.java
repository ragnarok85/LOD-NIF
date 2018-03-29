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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.compress.compressors.CompressorException;
import org.apache.commons.compress.compressors.CompressorInputStream;
import org.apache.commons.compress.compressors.CompressorStreamFactory;
import org.apache.log4j.Logger;

public class Main2 {
	static final Logger logger = Logger.getLogger(Main.class);

	/*
	 * args[0-2] - Context, page and links (in that order) lists. 
	 * args[3] - list of articles not found in Links 
	 * args[4-6] - Context, page and links (in that order) bz2 files. 
	 * args[6] - temporal output folder 
	 * args[7] - output temporal files 
	 * args[8] - output lod files 
	 * args[9] - output reports
	 * args[10] - original URI e.g. <http://dbpedia.org/resource/
	 */

	String replaceUri = "";
	String last = "";
	boolean replacement = false;
	public static void main(String... args) throws CompressorException,
			IOException, NoSuchAlgorithmException, DecoderException {
		Main2 main = new Main2();
		long initialTime = System.currentTimeMillis();
		String pathBZ2 = args[4];
		String outputFolder = args[8];
		String outputReports = args[9];

		String originalUri = args[10];

		try {
			if (args[11] != null) {
				main.replaceUri = args[11];
				main.replacement = true;
			}
		} catch (ArrayIndexOutOfBoundsException e) {

		}

		// System.out.println("Creating BufferedReader from BZ2 files");
		BufferedReader br = main
				.getBufferedReaderForCompressedFile(pathBZ2);

		// System.out.println("Loading the list of articles from context, page and link");
		String reportData = "";
		String articlesData = "";


			logger.info("Extracting triples");
			main.createTempFiles(br, outputFolder, originalUri);


		long endTime = System.currentTimeMillis() - initialTime;
		String timeElapsed = String.format(
				"TOTAL TIME = %d min, %d sec",
				TimeUnit.MILLISECONDS.toMinutes(endTime),
				TimeUnit.MILLISECONDS.toSeconds(endTime)
						- TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS
								.toMinutes(endTime)));
		System.out.println(timeElapsed);
		reportData += "\nTIME ELAPSED\t" + timeElapsed + "\n";
		main.writeReport(outputReports, "ListProcessedArticles.tsv",
				articlesData);
		main.writeReport(outputReports, "reports.tsv", reportData);
		main.extractArticlesProcessed(outputReports);
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
	int brCounter = 0;

	public void createTempFiles(BufferedReader br, String outputFolder,
			String originalUri) throws IOException, DecoderException {
		boolean fin = false;
		while(!fin){
			String article = "";
			List<String> lines = new ArrayList<String>();
//			logger.info("\tlast = " + last);
			fin = extractArticleLines(br, lines, originalUri);
			if (lines.size() > 0) {
				setArticles.add(article);
				//String outputName = generateName(lines.get(0), originalUri);
				String outputName = generateName(lines.get(0), replaceUri);
				writeTempFile(outputFolder + "/" + outputName, lines);
				writeTempFileLine(outputFolder, originalUri);
			}else {
				logger.info("\tarticle = " + article + " num lines = " + lines.size());
				logger.info("\tlast = " + last);
			}
		}
	}

	public boolean extractArticleLines(BufferedReader br, List<String> lines,
			String originalUri) throws IOException, DecoderException {
		String line = "";
		String article;
		boolean fin = false;
		if (last.length() == 0 && (line = br.readLine()) != null) {
			article = cleanLine(line.split("\\?dbpv")[0]);
		} else {
			article = cleanLine(last.split("\\?dbpv")[0]);
		}
		
		while (true) {
			if((line = br.readLine()) == null)
				fin = true;
			if (!line.startsWith("<")) {
				continue;
			}
			line = cleanLine(line);

			if (line.contains(article + "?dbpv")) {// dbpv=2016-10
				if(replacement){
					line = line.replace(originalUri, replaceUri);
				}
				lines.add(line);
			} else {
				last = line;
				break;
			}
		}
		
		return fin;
	}
	
	public String cleanLine(String line) throws IOException, DecoderException {
		if (line.contains("%")) {
			if (line.contains("%3F"))
				line = line.replaceAll("%3F", "?");
//			else if (line.contains("%22"))
//				line = line.replaceAll("%22", "\"");
//			else if (line.contains("%60")) {
//				line = line.replaceAll("%60", "`");
//			}
		}
		return line;
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

	int counter = 0;
	public void writeTempFile(String output, List<String> lines) {
		File file = new File(output);
		if (!file.exists()) {
			logger.info(counter + "-" + output);
			counter++;
		}

		try (PrintWriter pw = new PrintWriter(new OutputStreamWriter(
				new FileOutputStream(output, true), StandardCharsets.UTF_8))) {
			for (String line : lines) {
				pw.write(line + "\n");
			}
			pw.close();
		} catch (IOException e) {

		}
	}

	public void writeTempFileLine(String output, String originalUri) {
		String outputName = generateName(last, originalUri);
		String o = output + "/" + outputName;
		try (PrintWriter pw = new PrintWriter(new OutputStreamWriter(
				new FileOutputStream(o, true), StandardCharsets.UTF_8))) {
			String lineToWrite = last.replace(originalUri, replaceUri);
			pw.write(lineToWrite + "\n");
			last = "";
			pw.close();
		} catch (IOException e) {

		}
	}

}
