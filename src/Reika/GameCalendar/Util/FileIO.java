package Reika.GameCalendar.Util;


import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.URLConnection;
import java.net.UnknownHostException;
import java.util.ArrayList;

public class FileIO {

	public static ArrayList<String> getFileAsLines(String path) throws IOException {
		return getFileAsLines(getReader(path));
	}

	public static ArrayList<String> getFileAsLines(URL url, int timeout) throws IOException {
		BufferedReader r = getReader(url, timeout);
		return r != null ? getFileAsLines(r) : null;
	}

	public static ArrayList<String> getFileAsLines(File f) throws IOException {
		return getFileAsLines(getReader(f));
	}

	public static ArrayList<String> getFileAsLines(InputStream in) throws IOException {
		return getFileAsLines(getReader(in));
	}

	public static ArrayList<String> getFileAsLines(BufferedReader r) throws IOException {
		ArrayList<String> li = new ArrayList();
		String line = "";
		while (line != null) {
			line = r.readLine();
			if (line != null) {
				li.add(line);
			}
		}
		r.close();
		return li;
	}

	public static BufferedReader getReader(File f) {
		try {
			return new BufferedReader(new InputStreamReader(new FileInputStream(f)));
		}
		catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public static BufferedReader getReader(InputStream in) {
		try {
			return new BufferedReader(new InputStreamReader(in));
		}
		catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public static BufferedReader getReader(String path) {
		try {
			return new BufferedReader(new InputStreamReader(new FileInputStream(path)));
		}
		catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public static BufferedReader getReader(URL url, int timeout) {
		try {
			URLConnection c = url.openConnection();
			c.setConnectTimeout(timeout);
			return new BufferedReader(new InputStreamReader(c.getInputStream()));
		}
		catch (UnknownHostException e) { // Server not found
			e.printStackTrace();
		}
		catch (ConnectException e) { // Redirect/tampering
			e.printStackTrace();

		}
		catch (SocketTimeoutException e) { // Slow internet, cannot load a text
			// file...
			e.printStackTrace();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public static void deleteDirectoryWithContents(File folder) {
		File[] files = folder.listFiles();
		if (files != null) { //some JVMs return null for empty dirs
			for (File f : files) {
				if (f.isDirectory()) {
					deleteDirectoryWithContents(f);
				}
				else {
					//System.out.println("Deleting raw file " + f);
					f.delete();
				}
			}
		}
		//System.out.println("Deleting folder " + folder);
		folder.delete();
	}

	public static void writeLinesToFile(String s, ArrayList<String> li) throws IOException {
		writeLinesToFile(new File(s), li);
	}

	public static void writeLinesToFile(File f, ArrayList<String> li) throws IOException {
		writeLinesToFile(new BufferedWriter(new PrintWriter(f)), li);
	}

	public static void writeLinesToFile(BufferedWriter p, ArrayList<String> li) throws IOException {
		String sep = System.getProperty("line.separator");
		for (String s : li) {
			p.write(s + sep);
		}
		p.flush();
		p.close();
	}

}
