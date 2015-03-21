package com.siondream.engine;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

public class IDGenerator {
	private static int m_next = 1;
	private static HashMap<String, Integer> m_ids = new HashMap<String, Integer>();
	
	public static int getID(String string) {
		// Try to fetch ID
		Integer id = m_ids.get(string);
		
		// If it's the first time we try to get it, generate it
		if (id == null) {
			id = m_next;
			m_ids.put(string, m_next++);
		}

		return id;
	}
	
	public static String getString(int id) {
		Iterator<Entry<String, Integer>> it = m_ids.entrySet().iterator();
		
		while (it.hasNext()) {
			Entry<String, Integer> entry = it.next();
			
			if (entry.getValue() == id) {
				return entry.getKey();
			}
		}
		
		return null;
	}
}
