package lod.nif.main;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.compress.compressors.CompressorException;
import org.apache.commons.compress.compressors.CompressorInputStream;
import org.apache.commons.compress.compressors.CompressorStreamFactory;
import org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream;
import org.apache.commons.compress.compressors.bzip2.BZip2CompressorOutputStream;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.util.FileManager;
import org.rdfhdt.hdt.enums.RDFNotation;
import org.rdfhdt.hdt.exceptions.ParserException;
import org.rdfhdt.hdt.hdt.HDT;
import org.rdfhdt.hdt.hdt.HDTManager;
import org.rdfhdt.hdt.options.HDTSpecification;
import org.rdfhdt.hdt.rdf.parsers.JenaModelIterator;

public class HDTTest  {

	public static void main(String[] args) throws IOException, ParserException, CompressorException {
		// TODO Auto-generated method stub
	//	JenaModelIterator jmi = new JenaModelIterator(Model model);
//		List<String> lines = new ArrayList<String>();
//		createOneHDT(lines, "hdtoutput.hdt");
		
		HDTTest main  = new HDTTest();
//		FileInputStream fin = new FileInputStream("sample.ttl.bz2");
//		BufferedInputStream bis = new BufferedInputStream(fin);
//		CompressorInputStream input = new CompressorStreamFactory().createCompressorInputStream(bis);
//		BufferedReader br2 = new BufferedReader(new InputStreamReader(input));
//		
//		String line = "";
//		List<String> lines = new ArrayList<String>();
//		while((line = br2.readLine()) != null){
//			System.out.println(line);
//			lines.add(line);
//		}
//		main.parseFileGZ("sample2.ttl.bz2", lines);
//		main.createOneHDT("/home/noe/nif_en.hdt");
		main.createOneHDT(args[0], args[1]);
	}
	
	public void parseFileGZ(String outputPath, List<String> lines) throws IOException{
		//OutputStream os = Files.newOutputStream(Paths.get(outputPath));
		OutputStream os = new FileOutputStream(outputPath,true);
		BufferedOutputStream bos = new BufferedOutputStream(os);
		BZip2CompressorOutputStream outputStream = new BZip2CompressorOutputStream(bos);
//		PrintWriter pw = new PrintWriter(outputStream);
		PrintWriter pw = new PrintWriter(new OutputStreamWriter(outputStream,Charset.forName("UTF-8")));
		for(String line : lines){
			pw.write(line + "\n");
		}
		pw.close();
	}
	
	public static void createHDT(Model model, String hdtOutput, String baseURI) throws IOException, ParserException{
		JenaModelIterator jmi = new JenaModelIterator(model);
		OutputStream out = new BufferedOutputStream(new FileOutputStream(hdtOutput, true));
		HDT hdt = HDTManager.generateHDT(jmi, baseURI, new HDTSpecification(), null);
		hdt.getHeader();
		try {
			// Save generated HDT to a file
			hdt.saveToHDT(out, null);
		} finally {
			// IMPORTANT: Free resources
//			out.close();
//			hdt.close();
		}
	}
	
	public static void createOneHDT(String input, String hdtOutput) throws IOException, ParserException, CompressorException {
		File outputFile = new File(hdtOutput);
//		
//		File[] files = new File("/Users/lti/Downloads/Simple_nif/segments/00/01/").listFiles();
		HDT hdt = null;
//			
//			BZip2CompressorInputStream input = null;
//			try {
//				InputStream pageStructure = FileManager.get().open("/Users/lti/Downloads/Simple_nif/segments/all_parts.ttl.bz2");
//				input = new BZip2CompressorInputStream(pageStructure, true);
//			} catch (IOException e) {
//				e.printStackTrace();
//			}
			
//			Model model = ModelFactory.createDefaultModel();
//			model.read(input, null, "NTRIPLES");
			
//			JenaModelIterator jmi = new JenaModelIterator(model);
			OutputStream out = new BufferedOutputStream(new FileOutputStream(outputFile, true));
//			hdt = HDTManager.generateHDT("/Users/lti/Downloads/Simple_nif/segments/all_parts.ttl.bz2", "http://nif.dbpedia.org/wiki/simple/", RDFNotation.parse("ntriples"), new HDTSpecification(), null);
			//"/home/noe/nif_en.ttl.bz2"
			hdt = HDTManager.generateHDT(input, "http://nif.dbpedia.org/wiki/en/", RDFNotation.parse("ntriples"), new HDTSpecification(), null);
			try {
				// Save generated HDT to a file
					hdt.saveToHDT(out, null);
					
			} finally {
				// IMPORTANT: Free resources
				
				out.close();
				hdt.close();
//				input.close();
//				model.close();
				
			}
		
		
		
	}
	
	

}
