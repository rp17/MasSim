package masSim.taems;

import java.util.Dictionary;
import java.util.HashMap;
import java.util.Map;

public class SpecialAttributes {
	private Map<String,String> spec_attributes_Strings;
	private Map<String,Integer> spec_attributes_Ints;
	
	public SpecialAttributes()
	{
		spec_attributes_Strings = new HashMap<String,String>();	
	}
	
	public int getInt(String key)
	{
		return spec_attributes_Ints.get(key);
	}
	
	public void setInt(String key, Integer value)
	{
		spec_attributes_Ints.put(key, value);
	}
	
	public String getString(String key)
	{
		return spec_attributes_Strings.get(key);
	}
	
	public void setString(String key, String value)
	{
		spec_attributes_Strings.put(key, value);
	}

	public boolean containsKey(String string) {
		if (spec_attributes_Strings.containsKey(string)) return true;
		return spec_attributes_Ints.containsKey(string);
	}
	
}
