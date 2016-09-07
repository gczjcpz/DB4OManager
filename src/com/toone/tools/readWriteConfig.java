package com.toone.tools;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Iterator;

public class readWriteConfig {
	
	private String path;
	
	public void writeCofig(String path){
		try {
			File file = new File("configFile/ListViewConfig.txt");
			
			if (!file.exists()) {
				file.createNewFile();
			}
			FileWriter fw = new FileWriter(file,true);	
			BufferedWriter bw = new BufferedWriter(fw);
			bw.write(path+"\n");
			
			bw.close();
			
		} catch (Exception e) {
			e.printStackTrace();
		}	
	}
	
	public ArrayList<String> readCofig(){
		ArrayList<String> listView = new ArrayList<>();
		try {
			File file = new File("configFile/ListViewConfig.txt");
			if (file.exists()) {
				FileReader fr = new FileReader(file);
				BufferedReader br = new BufferedReader(fr);
				while(br.readLine()!=null){
					listView.add(br.readLine());
				}
				
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return listView;
	}
	
	public static void main(String []args){
		ArrayList<String> readCofig = new readWriteConfig().readCofig();
		for(Iterator<String> iterator = readCofig.iterator();iterator.hasNext();){
			System.out.println(iterator.next());
		}
	}

}
