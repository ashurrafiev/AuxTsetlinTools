package ncl.tsetlin.tools.genlogger;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Scanner;

public class StringLibrary {

	private HashMap<String, String> map = new HashMap<>(); 
	
	public StringLibrary(InputStream s) throws IOException {
		@SuppressWarnings("resource")
		Scanner in = new Scanner(s);
		while(in.hasNextLine()) {
			String line = in.nextLine();
			if(line.trim().isEmpty())
				continue;
			String key, value;
			if(line.endsWith("$$")) {
				key = line.substring(0, line.length()-2);
				StringBuilder sb = new StringBuilder();
				while(in.hasNextLine()) {
					String str = in.nextLine();
					if(str.equals("$$"))
						break;
					sb.append(str);
					sb.append("\n");
				}
				value = sb.toString();
			}
			else {
				int eq = line.indexOf("=");
				if(eq<0)
					throw new IOException("Expected $$ or =");
				key = line.substring(0, eq);
				value = line.substring(eq+1).trim();
			}
			key = key.replaceFirst("\\[.*\\]", "").trim();
			if(key.matches("[A-Za-z0-9_\\.]+\\\\?")) {
				if(key.endsWith("\\")) {
					value = value.replace("\\n", "\n").replace("\\t", "\t")
							.replace("\\s", " ").replace("\\r", "\r").replace("\\\\", "\\");
					key = key.substring(0, key.length()-1);
				}
				map.put(key, value);
			}
			else
				throw new IOException("Bad key format");
		}
		in.close();
	}
	
	public String get(String key) {
		return map.get(key);
	}
	
	public static StringLibrary load(String uri) {
		try {
			InputStream in = ClassLoader.getSystemResourceAsStream(uri);
			if(in==null)
				in = new FileInputStream(new File(uri));
			return new StringLibrary(in);
		}
		catch(Exception e) {
			e.printStackTrace();
			return null;
		}
	}
}
