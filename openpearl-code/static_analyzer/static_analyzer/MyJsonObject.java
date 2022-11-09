package static_analyzer;

import java.util.HashMap;
import java.util.Map;

/**
 * simple json parser which supports reading of 
 * <ul>
 * <li> string
 * <li> int
 * <li> boolean
 * <li> stringArray
 * </ul>
 * 
 */
public class MyJsonObject {
	private Map<String,Object> items;

	public MyJsonObject(String text) {
		items = new HashMap<>();
		// parse text into section until next ','
		// but ignore comma in '[...]' regions

		int startIndex = 0;
		text = text.substring(1, text.length()-1); // remove {}
		//System.out.println(text);
		
		while (startIndex < text.length()) {
			
			
			String key = text.substring(startIndex,text.indexOf(':', startIndex));
			startIndex += key.length()+1;
			String value="";
			if (text.charAt(startIndex)=='"') {
				// read until closing double quote
				for (int i=startIndex+1; i<text.length(); i++) {
					if (text.charAt(i) !='"') {
						value += text.charAt(i);
					} else {
						if (i>0 && text.charAt(i-1) == '\\') {
					
						value = value.substring(0, value.length()-1);
						value += '"';
					} else {
						int xx=0;
						break;
					}
					}
				}
				value = '"'+value+'"';
			} else if (text.charAt(startIndex)=='[') {
				// array start - read until ]
				// simple solution should be ok, since the content of the array do not contail a ] character
				int indexClosingBrace= text.indexOf(']', startIndex);
				value = text.substring(startIndex, indexClosingBrace+1);
				
			} else {
				int nextCommaPosition = text.indexOf(',', startIndex);
				if (nextCommaPosition > 0) {
					value = text.substring(startIndex,nextCommaPosition);
				} else {
					value = text.substring(startIndex);
				}
			}
			key = key.substring(1, key.length()-1); // remove delimiting quotes

			startIndex += value.length()+1;



				
				if (value.charAt(0) == '"') {
					// is string
					value = value.substring(1,value.length()-1);
					items.put(key, value);

				} else if (value.equals("true") ) {
					items.put(key, true);
				} else if (value.equals("false") ) {
					items.put(key, false);
				} else if (value.charAt(0) =='[') {
					String elements[] = value.substring(1,value.length()-1).split(",");
					for (int i=0; i<elements.length; i++) {
						elements[i] = elements[i].substring(1,elements[i].length()-1);
					}
					items.put(key, elements);
					
				} else {	
					// must be int value
					int intval = Integer.parseInt(value);
					items.put(key, intval);
				}
		}


	}

	public int getInt(String key, int def) {
		//String item = jsonText.indexOf(key);
		if (items.containsKey(key)) {
			return (int)items.get(key);
		} 
		return def;
	}
	public String getString(String key, String def) {
		if (items.containsKey(key)) {
			return (String)items.get(key);
		} 
		return def;
	}

	public String[] getStringArray(String key) {
		if (items.containsKey(key)) {
			return (String[])items.get(key);
		} 
		return null;
	}

	public boolean getBoolean(String key,boolean def) {
		if (items.containsKey(key)) {
			return (boolean)items.get(key);
		} 
		return def;
	}

}
