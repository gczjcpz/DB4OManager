package com.toone.tools;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class readWriteConfig {

	private String path;

	public void writeCofig(String path) {
		try {
			File file = new File("configFile/ListViewConfig.txt");

			if (!file.exists()) {
				file.createNewFile();
			}
			FileWriter fw = new FileWriter(file, true);
			BufferedWriter bw = new BufferedWriter(fw);
			bw.write(path + "\n");

			bw.close();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public List<String> readCofig() {
		List<String> listView = new ArrayList<>();
		File file = new File("configFile/ListViewConfig.txt");
		if (file.exists()) {
			try (FileReader fr = new FileReader(file); BufferedReader br = new BufferedReader(fr)) {
				String line = "";
				while ((line = br.readLine()) != null) {
					listView.add(line);
				}
				return listView;
			} catch (Exception e) {
				e.printStackTrace();
				return Collections.emptyList();
			}
		}
		return Collections.emptyList();
	}

//	public static void main(String[] args) {
//		List<String> readCofig = new readWriteConfig().readCofig();
//		for (Iterator<String> iterator = readCofig.iterator(); iterator.hasNext();) {
//			System.out.println(iterator.next());
//		}
//	}

}
