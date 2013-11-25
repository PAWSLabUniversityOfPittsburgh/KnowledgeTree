package edu.pitt.sis.paws.kt2;

import edu.pitt.sis.paws.core.Item2;

public class ItemType extends Item2
{
	final static long serialVersionUID = 2L;
	
	public static final int ITEMTYPE_TYPE_INTERNAL = 1;
	public static final int ITEMTYPE_TYPE_RSS = 2;
	public static final int ITEMTYPE_TYPE_OTHER = 3;
	
	protected String description;
	protected int type;
	protected String icon;
	protected String class_name;
	protected String resource_url_suffix;
	
	public ItemType(int _id, String _title, String _uri, String _description, int _type, 
			String _icon, String _class_name, String _resource_url_suffix)
	{
		super(_id, _title, _uri);
		
		description = _description;
		type = _type;
		icon = "<img border='0' src='" + _icon + "'/>";
		class_name = _class_name;
		resource_url_suffix = _resource_url_suffix;
	}
	
	public String getIcon(){ return icon; }

	public int getType(){ return type; }

	public String getDescription(){ return description; }
	
	public String getClassName(){ return class_name; }

	public String getResourceURLSuffix(){ return resource_url_suffix; }
	
	public String toString()
	{
		return this.getTitle() + ((description != null && description.length() >0 )?" (" + description + ")":"");
	}

}
