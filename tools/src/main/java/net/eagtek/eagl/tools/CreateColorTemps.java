package net.eagtek.eagl.tools;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.List;

import org.apache.commons.io.FileUtils;

public class CreateColorTemps {

	public static void main(String[] args) throws IOException {
		List<String> lines = FileUtils.readLines(new File("colorTemperature.txt"), Charset.forName("UTF8"));
		FileOutputStream fos = new FileOutputStream(new File("temperatures.lut"));
		for(String s : lines) {
			String[] s2 = s.split("\s+");
			if(s2[3].equals("10deg")) {
				System.out.println(s2[10] + " " + s2[11] + " " + s2[12]);
				fos.write((byte)Integer.parseInt(s2[10]));
				fos.write((byte)Integer.parseInt(s2[11]));
				fos.write((byte)Integer.parseInt(s2[12]));
			}
		}
	}

}
