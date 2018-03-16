package lod.nif.main;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.compress.compressors.CompressorException;
import org.apache.commons.compress.compressors.CompressorInputStream;
import org.apache.commons.compress.compressors.CompressorStreamFactory;

public class Tool {
	
	/*
	 * args[0] bz2 file
	 * args[1] output file name
	 */

	public static void main(String[] args) throws CompressorException, IOException, DecoderException{
		Tool tool  = new Tool();
		Comparator com = new Comparator();
		if(args == null){
			com.showParameters();
		}
		String bz2File = args[0];
		String outputFile = args[1];
		BufferedReader br = tool.getBufferedReaderForCompressedFile(bz2File);
		Set<String> setLines = tool.extractUniqLines(br);
		tool.writeResults(outputFile, setLines);
		
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
	
	public Set<String> extractUniqLines(BufferedReader br) throws IOException, DecoderException {
		Set<String> setLines = new HashSet<String>();
		Set<String> codes = new HashSet<String>();
		String line = "";
		while ((line = br.readLine()) != null) {
			if(!line.startsWith("<"))
				continue;
			String splitLine = line.split("=")[0];
			if(splitLine.contains("%")){
//				System.out.println(splitLine);
				int markPos = splitLine.indexOf("%");
				String code = splitLine.substring(markPos,markPos+3);
				codes.add(code);
				if(splitLine.contains("%3F"))
					splitLine = splitLine.replaceAll("%3F", "?");
				else if(splitLine.contains("%22"))
					splitLine = splitLine.replaceAll("%22", "\"");
				else if(splitLine.contains("%60")){
					splitLine = splitLine.replaceAll("%60", "`");
				}
			}
			setLines.add(splitLine);

		}
		for(String code : codes){
			System.out.println(code);
		}
		return setLines;
	}
	
	public void writeResults(String output, Set<String> setLines){
		try(PrintWriter pw = new PrintWriter(new OutputStreamWriter(new FileOutputStream(output), StandardCharsets.US_ASCII))){
			for(String line : setLines){
				pw.write(line + "\n");
			}
			pw.close();
		}catch(IOException e){
			
		}
	}
	
	public void showParameters(){
		System.out.println("arg0 = BZ2 file \n arg1 = output file \n example: nif_context_simple.ttl.bz2 context_list.txt");
	}
}
