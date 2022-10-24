package de.secretj12.JSON;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.Set;

public class JSONCollection extends JSONObject {
	private HashMap<String, Object> collection;
	
	public JSONCollection() {
		collection = new HashMap<>();
	}

	protected JSONCollection(HashMap<String, Object> collection) {
		this.collection = collection;
	}
	
	public void add(String name, Object obj) {
		collection.put(name, obj);
	}
	
	public void remove(String name) {
		collection.remove(name);
	}
	
	public JSONCollection getCollection(String name) {
		if(collection.containsKey(name) && collection.get(name) instanceof JSONCollection) return (JSONCollection) collection.get(name);
		else {
			JSONCollection col = new JSONCollection();
			add(name, col);
			return col;
		}
	}
	
	public Set<String> getNames() {
		return collection.keySet();
	}
	
	public JSONList getList(String name) {
		if(collection.containsKey(name) && collection.get(name) instanceof JSONList) return (JSONList) collection.get(name);
		else {
			JSONList list = new JSONList();
			add(name, list);
			return list;
		}
	}
	
	public String getString(String name) {
		if(collection.containsKey(name) && collection.get(name) instanceof String) return (String) collection.get(name);
		else return null;
	}
	
	public BigInteger getInteger(String name) {
		if(collection.containsKey(name) && collection.get(name) instanceof BigInteger) return (BigInteger) collection.get(name);
		else return null;
	}
	
	public BigDecimal getDecimal(String name) {
		if(collection.containsKey(name) && collection.get(name) instanceof BigDecimal) return (BigDecimal) collection.get(name);
		else return null;
	}
	
	public Object getObject(String name) {
		if(collection.containsKey(name)) return collection.get(name);
		else return null;
	}
	
	protected String toString(int indented) {
		String tabs = "";
		for(int i = 0; i < indented; i++) {
			tabs += "\t";
		}
		String out = "{";
		
		boolean isFirst = true;
		for(String name : collection.keySet()) {
			out += (isFirst?"\n\t":",\n\t") + tabs + "\"" + name + "\": "
					+ (collection.get(name) instanceof String?"\"" + collection.get(name) + "\"":
							collection.get(name) instanceof JSONObject?((JSONObject) collection.get(name)).toString(indented + 1):collection.get(name).toString());
			isFirst = false;
		}
		
		return out + "\n" + tabs + "}";
	}
	
	public String toString() {
		return toString(0);
	}
}
