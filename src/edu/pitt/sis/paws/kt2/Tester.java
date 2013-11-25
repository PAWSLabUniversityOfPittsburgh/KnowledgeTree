package edu.pitt.sis.paws.kt2;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.fileupload.ParameterParser;
//import org.lwjgl.Sys;


public class Tester
{
	public static void main(String[] args)
	{
		// Test Timing
		System.out.println("ns " + System.nanoTime());
		System.out.println("ms " + System.currentTimeMillis());
		
//		System.out.println("tick " + Sys.getTime());

		
		
//		// test parameter parser 
//		String value="one=http://www.pitt.edu;two=http://www.pitt.edu/sdf/user/dfg";
//		ParameterParser pp = new ParameterParser();
//		Map mp = pp.parse(value, ';');
//		for(Iterator iter = mp.keySet().iterator();iter.hasNext();)
//		{
//			String key = (String) iter.next();
//			System.out.println( key + " = " + mp.get(key));
//		}
//		System.out.println("has 'one'? " + mp.containsKey("one"));
//		
//		double x = .345623413524352345;
//		System.out.println("x=" + x);
//		NumberFormat formatter = new DecimalFormat("###");
//
//		System.out.println("formatted x=" + formatter.format(x * 100));
		
//		System.out.println("============================= SHALL WE");
////		Item2Vector<User> list = new Item2Vector<User>();
//		Item2Vector<Paper> list = new Item2Vector<Paper>();
//		Calendar start = null;
//		Calendar finish = null;
//		long diff_mills;
//		for(int i=1000; i>0; i--)
//		{
////			list.add( new User(i, "Item2 number " + i));
//			list.add( new Paper(i, "Item2 number " + i, 2007, "Bib", "Au",
//				"http://", null));
//		}
//
//		System.out.println("------ First element");
//		System.out.println(list.get(0));
//
//		System.out.println("------ Linear search test");
////		User found = null;
//		Paper found = null;
//
//		start = new GregorianCalendar();
//		for(int i=0; i<1000; i++)
//		{
//			found = list.findByIdO(400);
////			found = list.findByTitleO("Item2 number 400");
//		}
//		finish = new GregorianCalendar();
//		diff_mills = finish.getTimeInMillis() - start.getTimeInMillis();
//		System.out.println("\tFound " + found);
//		System.out.println("\tmillisec passed " + diff_mills);
//
//		System.out.println("------ Binary search test T");
//		Comparator iic = new Item2IdComparator();
//		start = new GregorianCalendar();
//		try
//		{
//			for(int i=0; i<1000; i++)
//			{
//				found = list.findById(400);
////				found = list.findByTitle("Item2 number 400");
//			}
//		}
//		catch(Exception e){e.printStackTrace(System.out);}
//		finish = new GregorianCalendar();
//		diff_mills = finish.getTimeInMillis() - start.getTimeInMillis();
//		System.out.println("\tFound " + found);
//		System.out.println("\tmill isec passed " + diff_mills);
//		=============================================================
		
//		A a = new A();
//		B b = new B();
//		a.x();
//		b.x();
//		=============================================================
		
//		Properties prop = new Properties();
//		prop.setProperty("id","123");
//		prop.setProperty("weighted","no");
//		prop.setProperty("ordered","yes");
//		String s =  "";
//		ByteArrayOutputStream o = new ByteArrayOutputStream();
		
//		StringProperties stringprops = new StringProperties();
//		stringprops.setProperty("id","123");
//		stringprops.setProperty("weighted","no");
//		stringprops.setProperty("ordered","yes");
//		
		
//		System.out.printf("");
//
//		try
//		{
//
////		prop.store(o,"");
////
////		String s_prop = o.toString(); Fri Mar 17 13:09:49 EST 2006
//
////		String s_prop = "ordered=yes\nweighted=no\nid=123";
//		String s_prop = "ordered=yes;weighted=no;extid=123;";
//
//		System.out.println(s_prop);
//		
//		System.out.println("---");
//		StringProperties stringprops2 = new StringProperties();
//		stringprops2.loadFrom(s_prop);
////		String s = stringprops2.toString();
//		System.out.println(stringprops2.toString().replace("{","").replace("}","").replace(", ",";"));
//		System.out.println("===");
//
//		}
//		catch(Exception e){ e.printStackTrace(System.out);}

		

//		CourseModel courseModel = new CourseModel("http://www.sis.pitt.edu/~paws/ont/peterb.rdf");
//		for (String t : courseModel.getTopicList())
//		{
//			System.out.println(courseModel.getTopicTitle(t));
//			for (String lo : courseModel.getLoListByTopic(t))
//			{
//				System.out.println(courseModel.getLoTitle(lo) + " - " + courseModel.getLoType(lo) + " " + lo);
//			}
//		}
//		System.out.println("--------------------------------------------------------");
//		for (String lo : courseModel.getLoList())
//			System.out.println(lo);

	}
}

