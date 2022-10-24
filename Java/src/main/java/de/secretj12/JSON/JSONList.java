package de.secretj12.JSON;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Iterator;

public class JSONList extends JSONObject implements Iterable<Object> {
	private ArrayList<Object> map;
	
	public JSONList() {
		map = new ArrayList<>();
	}
	
	protected JSONList(ArrayList<Object> map) {
		this.map = map;
	}

	@Override
	public Iterator<Object> iterator() {
		return map.iterator();
	}
	
	public int getSize() {
		return map.size();
	}
	
	public void add(Object obj) {
		map.add(obj);
	}
	
	public void remove(Object obj) {
		map.remove(obj);
	}
	
	public void remove(int i) {
		map.remove(i);
	}
	
	public void get(int i) {
		map.get(i);
	}
	
	public JSONCollection getCollection(int i) {
		if(i < map.size() && map.get(i) instanceof JSONCollection) return (JSONCollection) map.get(i);
		else return null;
	}
	
	public JSONList getList(int i) {
		if(i < map.size() && map.get(i) instanceof JSONList) return (JSONList) map.get(i);
		else return null;
	}
	
	public String getString(int i) {
		if(i < map.size() && map.get(i) instanceof String) return (String) map.get(i);
		else return null;
	}
	
	public BigInteger getInteger(int i) {
		if(i < map.size() && map.get(i) instanceof BigInteger) return (BigInteger) map.get(i);
		else return null;
	}
	
	public BigDecimal getDecimal(int i) {
		if(i < map.size() && map.get(i) instanceof BigDecimal) return (BigDecimal) map.get(i);
		else return null;
	}
	
	public Object getObject(int i) {
		if(i < map.size()) return map.get(i);
		else return null;
	}
	
	protected String toString(int indented) {
		String tabs = "";
		for(int i = 0; i < indented; i++) {
			tabs += "\t";
		}
		String out = "[";
		
		boolean isFirst = true;
		for(Object obj : map) {
			out += (isFirst?"\n\t":",\n\t") + tabs
					+ (obj instanceof String?"\"" + obj + "\"":
						obj instanceof JSONObject?((JSONObject) obj).toString(indented + 1):obj.toString());
			isFirst = false;
		}
		
		return out + "\n" + tabs + "]";
	}
	
	public String toString() {
		return toString(1);
	}
}
