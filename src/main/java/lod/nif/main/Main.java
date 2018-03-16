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

public class Main {

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

	//<http://dbpedia.org/resource/
	//<http://simple.dbpedia.org/resource/
	String lastContextLine = "";
	String lastPageLine = "";
	String lastLinksLine = "";
	String last = "";
	int counter = 0;
	Map<String, Report> mapArticleCounter = new HashMap<String, Report>();

	List<String> notInLinksList;
	private String replaceUri = "";
	private boolean replacement = false;
	public static void main(String... args) throws CompressorException,
			IOException, NoSuchAlgorithmException, DecoderException {
		// BasicConfigurator.configure(); // Logger was not working
		Main main = new Main();
		long initialTime = System.currentTimeMillis();
		File pathContextList = new File(args[0]);
		File pathPageList = new File(args[1]);
		File pathLinksList = new File(args[2]);
		File pathNotInLinksList = new File(args[3]);
		String pathContextBZ2 = args[4];
		String pathPageBZ2 = args[5];
		String pathLinksBZ2 = args[6];
		String outputFolder = args[7];
		String outputLod = args[8];
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
		BufferedReader brContext = main
				.getBufferedReaderForCompressedFile(pathContextBZ2);
		BufferedReader brPage = main
				.getBufferedReaderForCompressedFile(pathPageBZ2);
		BufferedReader brLinks = main
				.getBufferedReaderForCompressedFile(pathLinksBZ2);

		// System.out.println("Loading the list of articles from context, page and link");
		List<String> contextList = main.readListFromFile(pathContextList);
		List<String> pageList = main.readListFromFile(pathPageList);
		List<String> linksList = main.readListFromFile(pathLinksList);
		main.notInLinksList = main.readListFromFile(pathNotInLinksList);
		String reportData = "";
		String articlesData = "";
		int numArticlesToProcess = 1000;
		int contextListSize = contextList.size();

		logger.info("Replacement");
		for (int i = numArticlesToProcess, j = 0; i < contextList.size();) {
			logger.info("begin = " + j + "-- End = " + i + " of -- "
					+ contextListSize);

			logger.info("Extracting triples from page");
			main.createTempFiles(j, i, pageList, brPage, outputFolder, "page",
					originalUri);

			logger.info("Extracting triples from context");
			main.createTempFiles(j, i, contextList, brContext, outputFolder,
					"context", originalUri);

			logger.info("Extracting triples from link");
			main.createTempFiles(j, i, linksList, brLinks, outputFolder,
					"link", originalUri);

			articlesData = main.printMapArticles();

			LOD lod = new LOD();
			List<String> removeList = new ArrayList<String>();
			for (Map.Entry<String, Report> entry : main.mapArticleCounter
					.entrySet()) {
				Report report = entry.getValue();
				if (report.getTimesProcessed() == 3) {
					logger.info("outputName = " + report.getOutputName());
					report.setOutputBz2(lod.lodFile(report.getArticle(),
							outputFolder + "/" + report.getOutputName(),
							outputLod));
					removeList.add(entry.getKey());
					reportData += report.toString() + "\n";
				}
//				if (main.notInLinksList.contains(report.getArticle())) {
//					if (report.getTimesProcessed() == 2) {
//						report.setOutputBz2(lod.lodFile(report.getArticle(),
//								outputFolder + "/" + report.getOutputName(),
//								outputLod));
//						removeList.add(entry.getKey());
//						reportData += report.toString() + "\n";
//					}
//				} else if (report.getTimesProcessed() == 3) {
//					logger.info("outputName = " + report.getOutputName());
//					report.setOutputBz2(lod.lodFile(report.getArticle(),
//							outputFolder + "/" + report.getOutputName(),
//							outputLod));
//					removeList.add(entry.getKey());
//					reportData += report.toString() + "\n";
//				}
			}

			for (String r : removeList) {
				File file = new File(outputFolder + "/"
						+ main.mapArticleCounter.get(r).getOutputName());
				file.delete();
				main.mapArticleCounter.remove(r);
			}
			// System.out.println("Remaining files: ");
			// for(Map.Entry<String, Report> entry :
			// main.mapArticleCounter.entrySet()){
			// System.out.println(entry.getKey());
			// }

			j = i;

			if (i == contextList.size() - 1)
				break;
			else
				i += numArticlesToProcess;
			if (i > contextList.size()) {
				i = contextList.size() - 1;
			}
		}

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

	public void createTempFiles(int begin, int end, List<String> list,
			BufferedReader br, String outputFolder, String sender,
			String originalUri) throws IOException, DecoderException {
		if (end > list.size())
			end = list.size();
		logger.info("Sender = " + sender);
		for (int i = begin; i < end; i++) {
			String article = "";
			List<String> lines = new ArrayList<String>();
			article = extractArticleLines(br, lines, originalUri);
			setArticles.add(article);
			if (lines.size() > 0) {
				setArticles.add(sender + "999999" + article);
				String outputName = generateName(lines.get(0), originalUri);
				writeTempFile(outputFolder + "/" + outputName, lines);
				writeTempFileLine(outputFolder, sender, originalUri);
				String indexArticle = sender + " - " + i;
				String numTriples = sender + " - " + lines.size();
				if (mapArticleCounter.containsKey(article)) {
					mapArticleCounter.get(article).update(indexArticle,
							numTriples);
				} else {
					Report report = new Report(article, outputName,
							notInLinksList.contains(article), indexArticle,
							numTriples);
					mapArticleCounter.put(article, report);
				}
			}
		}
	}

	public String extractArticleLines(BufferedReader br, List<String> lines,
			String originalUri) throws IOException, DecoderException {
		String line = "";
		String article;
		if (last.length() == 0 && (line = br.readLine()) != null) {
			article = cleanLine(line.split("\\?dbpv")[0]);
		} else {
			article = cleanLine(last.split("\\?dbpv")[0]);
		}
		while ((line = br.readLine()) != null) {

			if (!line.startsWith("<http://")) {
				continue;
			}
			line = cleanLine(line);
			if(replacement){
				line = line.replace(originalUri, replaceUri);
			}

			if (line.contains(article + "?dbpv")) {// dbpv=2016-10
				lines.add(line);
			} else {
				last = line;
				break;
			}
		}
		return article;
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

	public String generateName(String uri, String originalUri) {
		if(replacement)
			originalUri = replaceUri;
		uri = uri.split("\\?")[0];
		// uri = uri.replace("<http://simple.dbpedia.org/resource/", "");
		uri = uri.replace(originalUri, "");
		uri = uri.split("\\?")[0].replace("/", "_____");
		return uri;
	}

	public String printMapArticles() {
		String data = "";
		for (Map.Entry<String, Report> entry : mapArticleCounter.entrySet()) {
			// System.out.println(counter++ + "--"
			// +entry.getValue().getTimesProcessed() + " ----- " +
			// entry.getValue().isInLink() + " ----- " +entry.getKey());
			data += entry.getValue().getTimesProcessed() + "\t"
					+ entry.getValue().isInLink() + "\t" + entry.getKey()
					+ "\n";
		}
		return data;
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

	public void writeTempFileLine(String output, String sender,
			String originalUri) {
		if(replacement)
			originalUri = replaceUri;
		String outputName = generateName(last, originalUri);
		// String o = output+"/"+sender + "/"+outputName;
		String o = output + "/" + outputName;
		try (PrintWriter pw = new PrintWriter(new OutputStreamWriter(
				new FileOutputStream(o, true), StandardCharsets.UTF_8))) {
			pw.write(last + "\n");
			last = "";
			pw.close();
		} catch (IOException e) {

		}
	}

}
