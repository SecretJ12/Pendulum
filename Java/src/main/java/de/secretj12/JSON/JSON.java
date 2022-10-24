package de.secretj12.JSON;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;

public class JSON {
	private static int line;
	private static int character;
	
	private JSON() {}
	
	public static JSONObject parse(File f) throws ParseException, IOException {
		BufferedReader reader = new BufferedReader(new FileReader(f));
		line = 1;
		character = 0;
		
		String s;
		int newChar;
		boolean hasRoot = false;
		Object rootObject = null;
		while((newChar = reader.read()) != -1) {
			character++;
			s = Character.toString((char) newChar);
			if(s.equals(" ") || s.equals("\n") || s.equals("\r") || s.equals("\t")) {
				continue;
			}
			else if(s.equals("{") || s.equals("[")) {
				if(!hasRoot) {
					rootObject = parseValue(s, reader);
					hasRoot = true;
				} else
					throw new ParseException("File has more than one root", line, character);
			} else if(!hasRoot) throw new ParseException("File has to start with \"{\" or \"[\"", line, character);
			else  throw new ParseException("File has more than one root", line, character);
		}
		reader.close();
		return rootObject instanceof JSONCollection?(JSONCollection) rootObject:rootObject instanceof JSONList?(JSONList) rootObject:null;
	}
	
	public static void save(File f, JSONObject rootObject) throws IOException {
		if(!f.createNewFile()) {
			f.delete();
			f.createNewFile();
		}
		
		BufferedWriter writer = new BufferedWriter(new FileWriter(f));
		
		writer.write(rootObject.toString());
		writer.close();
	}
	
	private static JSONCollection parseCollection(BufferedReader reader) throws ParseException, IOException {
		HashMap<String, Object> map = new HashMap<>();
		
		String s;
		int newChar;
		byte progress = 0; //0 Nothing, 1 hasName, 2 hasColon, 3 hasValue
		String name = null;
		while((newChar = reader.read()) != -1) {
			character++;
			s = Character.toString((char) newChar);
			if(s.equals(" ")) continue;
			else if(s.equals("\r")) continue;
			else if(s.equals("\t")) {
				continue;
			} else if(s.equals("\n")) {
				line++;
				character = 0;
				continue;
			} else if(s.equals("\"")) {
				if(progress == 0) {
					name = parseString(reader);
					progress = 1;
				} else if(progress == 2) {
					map.put(name, parseValue(s, reader));
					progress = 3;
				} else throw new ParseException("Wrong placed value or object", line, character);
			} else if(s.equals(":")) {
				if(progress == 1) progress = 2;
				else new ParseException("Wrong placed colon", line, character);
			} else if(s.equals(",")) {
				if(progress == 3) progress = 0;
				else throw new ParseException("Wrong placed comma", line, character);
			} else if(s.equals("}")) {
				return new JSONCollection(map);
			} else {
				if(progress == 0) throw new ParseException("Name has to start with \"", line, character);
				else if(progress == 1) throw new ParseException("Missing colon", line, character);
				else if(progress == 2) {
					map.put(name, parseValue(s, reader));
					progress = 3;
				} else if(progress == 3) throw new ParseException("Missing comma", line, character);
			}
		}
		throw new ParseException("File not closed", line, character);
	}
	
	private static JSONList parseList(BufferedReader reader) throws ParseException, IOException {
		ArrayList<Object> list = new ArrayList<>();
		String s;
		int newChar;
		byte progress = 0; //0 Nothing, 1 hasValue
		while((newChar = reader.read()) != -1) {
			character++;
			s = Character.toString((char) newChar);
			if(s.equals(" ")) continue;
			else if(s.equals("\t")) continue;
			else if(s.equals("\r")) continue;
			else if(s.equals("\n")) {
				line++;
				character = 0;
				continue;
			} else if(s.equals(",")) {
				if(progress == 1) progress = 0;
				else throw new ParseException("Wrong placed comma", line, character);
			} else if(s.equals("]")) {
				return new JSONList(list);
			} else {
				if(progress == 0) {
					list.add(parseValue(s, reader));
					progress = 1;
				} else if(progress == 1) throw new ParseException("Missing comma", line, character);
			}
		}
		throw new ParseException("File not closed", line, character);
	}
	
	private static Object parseValue(String valueStart, BufferedReader reader) throws ParseException, IOException {
		if(valueStart.equals("\"")) return parseString(reader);
		else if(Character.isDigit(valueStart.charAt(0)) || valueStart.equals("-")) return parseNumber(valueStart, reader);
		else if(valueStart.equals("{")) return parseCollection(reader);
		else if(valueStart.equals("[")) return parseList(reader);
		throw new ParseException("Invalid value", line, character);
	}
	
	private static String parseString(BufferedReader reader) throws ParseException, IOException {
		String s;
		int newChar;
		String name = "";
		while((newChar = reader.read()) != -1) {
			character++;
			s = Character.toString((char) newChar);
			
			if(s.equals("\n")) {
				line++;
				character = 0;
				return name;
			} else if(s.equals("\r")) continue;
			else if(s.equals("\"")) return name;
			else {
				name += s;
			}
		}
		throw new ParseException("Name has to end with \"", line, character);
	}
	
	private static Number parseNumber(String firstDigit, BufferedReader reader) throws IOException, ParseException {
		String s;
		int newChar;
		reader.mark(10);
		while((newChar = reader.read()) != -1) {
			character++;
			s = Character.toString((char) newChar);
			
			if(Character.isDigit(s.charAt(0)) || s.equals(".")) firstDigit += s;
			else {
				reader.reset();
				break;
			}
			reader.mark(10);
		}
		
		try {
			if(firstDigit.contains(".")) return new BigDecimal(firstDigit);
			else return new BigInteger(firstDigit);
		} catch (NumberFormatException e) {
			throw new ParseException("Invalid number: " + firstDigit, line, character);
		}
	}
}
