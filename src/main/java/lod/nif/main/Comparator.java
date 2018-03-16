package lod.nif.main;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;

public class Comparator {
	/*
	 * args[0] - list 1
	 * args[1] - list 2
	 * args[2] - output file
	 */
	public static void main(String[] args) throws IOException{
		Comparator com = new Comparator();
		if(args == null){
			com.showParameters();
		}
		String input1 = args[0];
		String input2 = args[1];
		String output = args[2];
		
		
		List<String> list1 = com.readLinesFromFile(input1);
		List<String> list2 = com.readLinesFromFile(input2);
		List<String> notInList = null;
		
		System.out.println("number of articles in list 1 = " + list1.size());
		System.out.println("number of articles in list 2 = " + list2.size());
		
		notInList = com.compareList(list1, list2);
		
		System.out.println("The number of different lines is: " + notInList.size());
		com.writeFile(output,notInList);
	}
	
	public List<String> readLinesFromFile(String inputFile) throws IOException{
		List<String> lines = new ArrayList<String>();
		
		File input = new File(inputFile);
		lines = FileUtils.readLines(input, "UTF-8");
		
		return lines;
	}
	
	public List<String> compareList(List<String> list1, List<String> list2){
		List<String> notInList = new ArrayList<String>();
		for(String line : list1){
			if(!list2.contains(line))
				notInList.add(line);
				
		}
		return notInList;
	}
	
	public void writeFile(String outputFile, List<String> notInList) throws IOException{
		File output = new File(outputFile);
		CharSequence[] cs = notInList.toArray(new CharSequence[notInList.size()]);
		for(String c : notInList){
			FileUtils.write(output, c  +"\n", true);
		}
		
	}
	
	public void showParameters(){
		System.out.println("arg0 = List 1 \n arg1 = List 2 \n arg2 = output file \n example: page_list.txt context_list.txt notInPage-Context.txt");
	}

}
