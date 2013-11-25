/* Disclaimer:
 * 	Java code contained in this file is created as part of educational
 *    research and development. It is intended to be used by researchers of
 *    University of Pittsburgh, School of Information Sciences ONLY.
 *    You assume full responsibility and risk of lossed resulting from compiling
 *    and running this code.
 */
 
/**
 * @author Michael V. Yudelson
 */

package edu.pitt.sis.paws.kt2;


public class HTMLUtilities
{
	public static StringBuffer javaScriptBrowserDetector()
	{
		StringBuffer result = new StringBuffer();
		
		result.append("\n");
		result.append("<script type='text/javascript'>\n");
		result.append("<!--\n");
		result.append("var BrowserDetect = {\n");
		result.append("	init: function () {\n");
		result.append("		this.browser = this.searchString(this.dataBrowser) || 'An unknown browser';\n");
		result.append("		this.version = this.searchVersion(navigator.userAgent)\n");
		result.append("			|| this.searchVersion(navigator.appVersion)\n");
		result.append("			|| 'an unknown version';\n");
		result.append("		this.OS = this.searchString(this.dataOS) || 'an unknown OS';\n");
		result.append("	},\n");
		result.append("	searchString: function (data) {\n");
		result.append("		for (var i=0;i<data.length;i++)	{\n");
		result.append("			var dataString = data[i].string;\n");
		result.append("			var dataProp = data[i].prop;\n");
		result.append("			this.versionSearchString = data[i].versionSearch || data[i].identity;\n");
		result.append("			if (dataString) {\n");
		result.append("				if (dataString.indexOf(data[i].subString) != -1)\n");
		result.append("					return data[i].identity;\n");
		result.append("			}\n");
		result.append("			else if (dataProp)\n");
		result.append("				return data[i].identity;\n");
		result.append("		}\n");
		result.append("	},\n");
		result.append("	searchVersion: function (dataString) {\n");
		result.append("		var index = dataString.indexOf(this.versionSearchString);\n");
		result.append("		if (index == -1) return;\n");
		result.append("		return parseFloat(dataString.substring(index+this.versionSearchString.length+1));\n");
		result.append("	},\n");
		result.append("	dataBrowser: [\n");
		result.append("		{ 	string: navigator.userAgent,\n");
		result.append("			subString: 'OmniWeb',\n");
		result.append("			versionSearch: 'OmniWeb/',\n");
		result.append("			identity: 'OmniWeb'\n");
		result.append("		},\n");
		result.append("		{\n");
		result.append("			string: navigator.vendor,\n");
		result.append("			subString: 'Apple',\n");
		result.append("			identity: 'Safari'\n");
		result.append("		},\n");
		result.append("		{\n");
		result.append("			prop: window.opera,\n");
		result.append("			identity: 'Opera'\n");
		result.append("		},\n");
		result.append("		{\n");
		result.append("			string: navigator.vendor,\n");
		result.append("			subString: 'iCab',\n");
		result.append("			identity: 'iCab'\n");
		result.append("		},\n");
		result.append("		{\n");
		result.append("			string: navigator.vendor,\n");
		result.append("			subString: 'KDE',\n");
		result.append("			identity: 'Konqueror'\n");
		result.append("		},\n");
		result.append("		{\n");
		result.append("			string: navigator.userAgent,\n");
		result.append("			subString: 'Firefox',\n");
		result.append("			identity: 'Firefox'\n");
		result.append("		},\n");
		result.append("		{\n");
		result.append("			string: navigator.vendor,\n");
		result.append("			subString: 'Camino',\n");
		result.append("			identity: 'Camino'\n");
		result.append("		},\n");
		result.append("		{		// for newer Netscapes (6+)\n");
		result.append("			string: navigator.userAgent,\n");
		result.append("			subString: 'Netscape',\n");
		result.append("			identity: 'Netscape'\n");
		result.append("		},\n");
		result.append("		{\n");
		result.append("			string: navigator.userAgent,\n");
		result.append("			subString: 'MSIE',\n");
		result.append("			identity: 'Explorer',\n");
		result.append("			versionSearch: 'MSIE'\n");
		result.append("		},\n");
		result.append("		{\n");
		result.append("			string: navigator.userAgent,\n");
		result.append("			subString: 'Gecko',\n");
		result.append("			identity: 'Mozilla',\n");
		result.append("			versionSearch: 'rv'\n");
		result.append("		},\n");
		result.append("		{ 		// for older Netscapes (4-)\n");
		result.append("			string: navigator.userAgent,\n");
		result.append("			subString: 'Mozilla',\n");
		result.append("			identity: 'Netscape',\n");
		result.append("			versionSearch: 'Mozilla'\n");
		result.append("		}\n");
		result.append("	],\n");
		result.append("	dataOS : [\n");
		result.append("		{\n");
		result.append("			string: navigator.platform,\n");
		result.append("			subString: 'Win',\n");
		result.append("			identity: 'Windows'\n");
		result.append("		},\n");
		result.append("		{\n");
		result.append("			string: navigator.platform,\n");
		result.append("			subString: 'Mac',\n");
		result.append("			identity: 'Mac'\n");
		result.append("		},\n");
		result.append("		{\n");
		result.append("			string: navigator.platform,\n");
		result.append("			subString: 'Linux',\n");
		result.append("			identity: 'Linux'\n");
		result.append("		}\n");
		result.append("	]\n");
		result.append("\n");
		result.append("};\n");
		result.append("BrowserDetect.init();\n");
		result.append("\n");
		result.append("//		 -->\n");
		result.append("</script>\n");
		return result;
	}
	
	/**
	 * Replaces single quote ' with reverse single quote ` for HTML safety
	 * Also replaces Word's single closing quote Õ
	 * @return
	 */
	public static String replaceSingleQuote(String _str)
	{
		String result = _str.replace("\u0027", "\u0060");
		result = result.replace("\u2019", "\u0060");
		
//		try
//		{
//			// Convert from Unicode to UTF-8
//			String string = result;
//			byte[] utf8 = string.getBytes("UTF-8");
//			
//			// Convert from UTF-8 to Unicode
//			string = new String(utf8, "UTF-8");
//			System.out.println("new encoding: " + string + " found at " + string.indexOf('\u2019'));
//		}
//		catch (UnsupportedEncodingException e) {}
	    
		return result;
//		return _str.replace("'", "`").replace('\u2019', '`');
	}
}
