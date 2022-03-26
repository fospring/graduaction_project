package com.example.sroffice;

public class Code {
	
	public static String unicodeToString(String unicode) { 
		StringBuffer string = new StringBuffer(); 
		String[] hex = unicode.split("\\\\u"); 
		for (int i = 1; i < hex.length; i++) { 
			int data = Integer.parseInt(hex[i], 16); 
			string.append((char) data); 
			} 
		System.out.println(string.toString());
		return string.toString(); 
	}
}
