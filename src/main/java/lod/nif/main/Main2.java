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
import org.apache.commons.io.FileUtils;
import org.apache.jena.atlas.lib.ListUtils;
import org.apache.log4j.Logger;
import org.rdfhdt.hdt.exceptions.ParserException;

public class Main2 {
	static final Logger logger = Logger.getLogger(Main2.class);

	/*
	 * args[0] - path of the BZ2 file
	 * args[1] - output folder
	 * args[2] - outputReports
	 * args[3] - report name - the same as the BZ2
	 * args[4] - lang
	 * 
	 */

	String replaceUri = "";
	String last = "";
	boolean replacement = true;

	public static void main(String... args)
			throws CompressorException, IOException, NoSuchAlgorithmException, DecoderException, ParserException {
		Main2 main = new Main2();
		long initialTime = System.currentTimeMillis();
		String pathBZ2 = args[0];
		String outputFolder = args[1];
		String outputReports = args[2];
		String reportName = args[3];
		String lang = args[4];


		// System.out.println("Creating BufferedReader from BZ2 files");
		BufferedReader br = main.getBufferedReaderForCompressedFile(pathBZ2);

		// System.out.println("Loading the list of articles from context, page and
		// link");
		String reportData = "";

		logger.info("Extracting triples");
		 main.createTempFiles(br, outputFolder, lang);
		
		long endTime = System.currentTimeMillis() - initialTime;
		String timeElapsed = String.format("TOTAL TIME = %d min, %d sec", TimeUnit.MILLISECONDS.toMinutes(endTime),
				TimeUnit.MILLISECONDS.toSeconds(endTime)
						- TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(endTime)));
		System.out.println(timeElapsed);
		reportData += "\nTIME ELAPSED\t" + timeElapsed + "\n";
		main.writeReport(outputReports, reportName+"-timeElapsed.tsv", reportData);
	}
	
//	public static void main(String[] args) throws IOException{
//		System.out.println("Manuel_%22Flaco%22_Ibáñez");
//		String fileName = "Manuel_%22Flaco%22_Ibáñez";
//		File output =  new File(fileName);
//		FileUtils.write(output, "Manuel_%22Flaco%22_Ibáñez");
//	}

	public BufferedReader getBufferedReaderForCompressedFile(String fileIn)
			throws FileNotFoundException, CompressorException {
		FileInputStream fin = new FileInputStream(fileIn);
		BufferedInputStream bis = new BufferedInputStream(fin);
		CompressorInputStream input = new CompressorStreamFactory().createCompressorInputStream(bis);
		BufferedReader br2 = new BufferedReader(new InputStreamReader(input));
		return br2;
	}

	int brCounter = 0;

	public void createTempFiles(BufferedReader br, String outputFolder, String lang)
			throws IOException, DecoderException, NoSuchAlgorithmException, ParserException {
		boolean fin = false;
		LOD lod = new LOD();
		int counter = 0;
		File index = new File(outputFolder+"/index.txt");
		while (!fin) {
			String article = "";
			List<String> lines = new ArrayList<String>();
			fin = extractArticleLines(br, lines);
			if (lines.size() > 0) {
				counter += lines.size();
				logger.info(counter);
				article = generateName(lines.get(0), lang);
				String iName = lod.lodFile(article, outputFolder, lines);
//				lod.hdtFile(lines, outputFolder+"/hdtVersion.hdt");
				FileUtils.write(index, article + "\t" + iName + "\n", true);
			} else {
				logger.info("\tarticle = " + article + " num lines = " + lines.size());
				logger.info("\tlast = " + last);
			}
		}
	}

	public String listToString(List<String> list){
		String all = "";
		for(String l : list){
			all += l + "\n";
		}
		return all;
	}
	
	public boolean extractArticleLines(BufferedReader br, List<String> lines)
			throws IOException, DecoderException {
		String line = "";
		String article;
		boolean fin = false;
		if (last.length() == 0 && (line = br.readLine()) != null) {
			article = line.split("\\?dbpv")[0];

		} else {
			line = last;
			article = last.split("\\?dbpv")[0];
			
		}
		
		while (true) {
			if(line == null) {
				fin = true;
				break;
			}
			if (!line.startsWith("<")) {
				line = br.readLine();
				continue;
			}
			if (line.contains(article + "?dbpv")) {// dbpv=2016-10
				lines.add(line);
			} else {
				last = line;
				break;
			}
			if ((line = br.readLine()) == null) {
				fin = true;
				break;
			}
		}
		return fin;
	}


	public String generateName(String uri, String lang) {
		uri = uri.split("\\?dbpv")[0];
		String uriLang = "";
		try {
			uriLang = uri.split("/"+lang+"/")[1];
		}catch(Exception e) {
			logger.fatal("error caused by = " + uri + " --- " + uriLang);
		}
		
		return uriLang.replace("/", "%20");
	}
	

	public void printLines(List<String> lines) {
		for (String line : lines) {
			System.out.println(line);
		}
	}

	public void writeReport(String reportDirectory, String reportName, String reportData) {
		String output = reportDirectory + "/" + reportName;
		try (PrintWriter pw = new PrintWriter(
				new OutputStreamWriter(new FileOutputStream(output, true), StandardCharsets.UTF_8))) {
			pw.write(reportData);
			pw.close();
		} catch (IOException e) {

		}
	}

	int counter = 0;

	public void writeTempFile(String output, List<String> lines, String lang) {
		File file = new File(output);
		if (!file.exists()) {
			logger.info(counter + "-" + output);
			counter++;
		}

		try (PrintWriter pw = new PrintWriter(
				new OutputStreamWriter(new FileOutputStream(output+".nt", true), StandardCharsets.UTF_8))) {
			for (String line : lines) {
				pw.write(line + "\n");
			}
			pw.close();
		} catch (IOException e) {

		}
	}

	public void writeTempFileLine(String output, String lang) {
		String outputName = generateName(last, lang);
		String o = output + "/" + outputName+ ".nt";
		try (PrintWriter pw = new PrintWriter(
				new OutputStreamWriter(new FileOutputStream(o, true), StandardCharsets.UTF_8))) {
			pw.write(last + "\n");
			last = "";
			pw.close();
		} catch (IOException e) {

		}
	}

}
