package edu.pitt.sis.paws.kt2;

import edu.pitt.sis.paws.core.Item;;

public class AnnotationItem extends Item
{
	static final long serialVersionUID = 2L;
	
	private String annotation;
	private String format;
	private boolean popup = false; 
	
	public AnnotationItem()
	{
		super();
		annotation = "";
		format = "";
		popup = false; 
	}
	
	public AnnotationItem(String _title, String _annotation)
	{
		super(0, _title);
		annotation = (_annotation==null) ? "" : _annotation;
		format = "";
		popup = false; 
	}
	
	public AnnotationItem(String _title, String _annotation, String _format)
	{
		super(0, _title);
		annotation = (_annotation==null) ? "" : _annotation;
		format = (_format==null) ? "" : _format;
		popup = false; 
	}
	
	public String getAnnotation() { return annotation; }
	
	public void setAnnotation(String _annotation) { annotation = _annotation; }

	public String getFormat() { return format; }
	
	public void setFormat(String _format) { format = _format; }

	public boolean getPopUp() { return popup; }
	
	public void setgetPopUp(boolean _popup) { popup = _popup; }

}
