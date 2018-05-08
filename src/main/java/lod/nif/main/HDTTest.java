package lod.nif.main;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.compress.compressors.CompressorException;
import org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.util.FileManager;
import org.rdfhdt.hdt.exceptions.ParserException;
import org.rdfhdt.hdt.hdt.HDT;
import org.rdfhdt.hdt.hdt.HDTManager;
import org.rdfhdt.hdt.hdt.HDTManagerImpl;
import org.rdfhdt.hdt.options.HDTSpecification;
import org.rdfhdt.hdt.rdf.parsers.JenaModelIterator;
import org.rdfhdt.hdt.enums.RDFNotation;

public class HDTTest  {

	public static void main(String[] args) throws IOException, ParserException, CompressorException {
		// TODO Auto-generated method stub
	//	JenaModelIterator jmi = new JenaModelIterator(Model model);
		List<String> lines = new ArrayList<String>();
		createOneHDT(lines, "hdtoutput.hdt");

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
	
	public static void createOneHDT(List<String> lines, String hdtOutput) throws IOException, ParserException, CompressorException {
		File outputFile = new File(hdtOutput);
		
		File[] files = new File("/Users/lti/Downloads/Simple_nif/segments/00/01/").listFiles();
		HDT hdt = null;
			
			BZip2CompressorInputStream input = null;
			try {
				InputStream pageStructure = FileManager.get().open("/Users/lti/Downloads/Simple_nif/segments/all_parts.ttl.bz2");
				input = new BZip2CompressorInputStream(pageStructure, true);
			} catch (IOException e) {
				e.printStackTrace();
			}
			
//			Model model = ModelFactory.createDefaultModel();
//			model.read(input, null, "NTRIPLES");
			
//			JenaModelIterator jmi = new JenaModelIterator(model);
			OutputStream out = new BufferedOutputStream(new FileOutputStream(outputFile, true));
			hdt = HDTManager.generateHDT("/Users/lti/Downloads/Simple_nif/segments/all_parts.ttl.bz2", "http://nif.dbpedia.org/wiki/simple/", RDFNotation.parse("ntriples"), new HDTSpecification(), null);
			try {
				// Save generated HDT to a file
					hdt.saveToHDT(out, null);
					
			} finally {
				// IMPORTANT: Free resources
				
				out.close();
				hdt.close();
				input.close();
//				model.close();
				
			}
		
		
		
	}
	
	

}
