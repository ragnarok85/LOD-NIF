package lod.nif.main;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import org.apache.jena.rdf.model.Model;
import org.rdfhdt.hdt.exceptions.ParserException;
import org.rdfhdt.hdt.hdt.HDT;
import org.rdfhdt.hdt.hdt.HDTManager;
import org.rdfhdt.hdt.options.HDTSpecification;
import org.rdfhdt.hdt.rdf.parsers.JenaModelIterator;

public class HDTTest {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
	//	JenaModelIterator jmi = new JenaModelIterator(Model model);

	}
	
	public static void createHDT(Model model, String hdtOutput, String baseURI) throws IOException, ParserException{
		JenaModelIterator jmi = new JenaModelIterator(model);
		OutputStream out = new BufferedOutputStream(new FileOutputStream(hdtOutput, true));
		HDT hdt =  HDTManager.generateHDT(jmi, baseURI, new HDTSpecification(), null);

		try {
			// Save generated HDT to a file
			hdt.saveToHDT(out, null);
		} finally {
			// IMPORTANT: Free resources
			out.close();
		}
	}

}
