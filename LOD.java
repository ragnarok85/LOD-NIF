package lod.nif.main;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;

import javax.xml.bind.DatatypeConverter;

import org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream;
import org.apache.commons.compress.compressors.bzip2.BZip2CompressorOutputStream;
import org.apache.commons.io.FileUtils;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.util.FileManager;
import org.rdfhdt.hdt.exceptions.ParserException;

public class LOD {
	/*
	 * input - main path of NIF files
	 */
	private Model model; 
	public LOD(){
		this.model = ModelFactory.createDefaultModel();
	}
	
	public LOD(String pathNIF){
		
	}
	
	public static void main(String[] args) throws NoSuchAlgorithmException, IOException{
		LOD lod = new LOD();
		
		//System.out.println("a7910a971aeaca3109653161499e6e6e".substring(4));
		String outputPath = lod.createOutputPath("Kopar_Road", "");
		System.out.println(outputPath);
		lod.parseFile("/Users/lti/Downloads/Simple_nif/tmp.bz2","/Users/lti/Downloads/Simple_nif/tmp/April");
		
	}
	
	public String lodFile(String article, String pathArticle, String output) throws NoSuchAlgorithmException, IOException{
//		System.out.println("pathArticle = "+pathArticle);
		String outputPath = createOutputPath(article, output);
//		parseFile(outputPath+".ttl.bz2", pathArticle);
		parseFileGZ(outputPath+".ttl.bz2", pathArticle);
		return outputPath+".ttl.bz2";
	}
	
	public void hdtFile(String baseURI, String pathArticle, String hdtOutput) throws NoSuchAlgorithmException, IOException, ParserException{
		parseFileHDT(hdtOutput, pathArticle, baseURI);
	}		
	
	public String createOutputPath(String name, String output) throws NoSuchAlgorithmException{
		String md5Name = md5(name);
		String path = createDirectoryStructure(md5Name, output);
		String fileName = extractFileName(md5Name);
		
//		System.out.println(md5Name + "\n" + path + "\n" + fileName + "\n");
		return  path + "/" + fileName;
	}
	
	
	public String md5(String name) throws NoSuchAlgorithmException {
		String md5 = "";
//		String base = new String(("<http://nif.dbpedia.org/wiki/en/"+name).getBytes(),StandardCharsets.UTF_8);
		String base = new String(name.getBytes(),StandardCharsets.UTF_8);
		byte[] fileName = base.getBytes();
		MessageDigest md = MessageDigest.getInstance("MD5");
		md5 = DatatypeConverter.printHexBinary(md.digest(fileName)).toLowerCase();
		return md5;
	}
	
	public String createDirectoryStructure(String md5Name, String output){
		String path = output + "/" + md5Name.substring(0,2) + "/" + md5Name.substring(2,4);
		File newPath = new File(path);
		newPath.mkdirs();
		return path;
	}
	
	public String extractFileName(String md5Name){
		return md5Name.substring(4);
	}
	
	public void parseFileGZ(String outputPath, String filePath) throws IOException{
		List<String> lines = FileUtils.readLines(new File(filePath));
		OutputStream os = Files.newOutputStream(Paths.get(outputPath));
		BufferedOutputStream bos = new BufferedOutputStream(os);
		BZip2CompressorOutputStream outputStream = new BZip2CompressorOutputStream(bos);
		PrintWriter pw = new PrintWriter(outputStream, true);
		for(String line : lines){
			pw.write(line + "\n");
		}
		pw.close();
	}
	
	public void parseFile(String outputPath, String filePath) throws IOException{
		Model model = ModelFactory.createDefaultModel();
		
		//BZip2CompressorInputStream inputStream = createBz2Reader(filePath);
		OutputStream os = Files.newOutputStream(Paths.get(outputPath));
		BufferedOutputStream bos = new BufferedOutputStream(os);
		BZip2CompressorOutputStream outputStream = new BZip2CompressorOutputStream(bos);
		PrintWriter pw = new PrintWriter(outputStream, true);
		File path = new File(filePath);
		
		if(path.exists() && path.isFile()){
			model.read(filePath,"NTRIPLES");
			
			model.setNsPrefix("nif", "http://persistence.uni-leipzig.org/nlp2rdf/ontologies/nif-core#");
			model.setNsPrefix("rdf", "http://www.w3.org/1999/02/22-rdf-syntax-ns#");
			model.setNsPrefix("skos", "http://www.w3.org/2004/02/skos/core#");
			model.setNsPrefix("itsrdf", "http://www.w3.org/2005/11/its/rdf#");
			model.setNsPrefix("xsd", "http://www.w3.org/2001/XMLSchema#");
			model.setNsPrefix("rdfs", "http://www.w3.org/2000/01/rdf-schema#");
			
			model.write(pw, "TURTLE");
			outputStream.close();
			model.close();
		}else{
//			System.out.println("Path (" + path.getAbsolutePath() + ") does not exist ");
		}
		
//		File file = new File(filePath);
//		file.delete();
	}
	
	public void parseFileHDT(String hdtOutput, String filePath, String baseURI) throws IOException, ParserException{
		Model model = ModelFactory.createDefaultModel();
		
		//BZip2CompressorInputStream inputStream = createBz2Reader(filePath);
		File path = new File(filePath);
		
		if(path.exists() && path.isFile()){
			model.read(filePath,"NTRIPLES");
			HDTTest.createHDT(model, hdtOutput, baseURI);
			model.close();
		}else{
		}
	}
	
	public void loadIntoModel(String hdtOutput, String filePath){
		Model readModel = ModelFactory.createDefaultModel();
		File path = new File(filePath);
		
		if(path.exists() && path.isFile()){
			readModel.read(filePath,"NTRIPLES");
			model.add(readModel);
			readModel.close();
		}else{
		}
	}
	
	public BZip2CompressorInputStream createBz2Reader(String source) {
		BZip2CompressorInputStream pageStruct = null;
		try{
			if(!new File(source).exists()) {
				return null;
			}
			InputStream pageStructure = FileManager.get().open(source);
	    	pageStruct = new BZip2CompressorInputStream(pageStructure);
		}catch(IOException e) {
			e.printStackTrace();
		}
		return pageStruct;
	}
	

}
