package de.privatepublic.axolist;

import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import de.privatepublic.axolist.AXOList.LibraryType;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateExceptionHandler;

public class RenderHTML {

	private static Configuration freemarker;
	
	static {
		freemarker = new Configuration(Configuration.VERSION_2_3_25);
		freemarker.setDefaultEncoding("UTF-8");
		freemarker.setTemplateExceptionHandler(TemplateExceptionHandler.HTML_DEBUG_HANDLER);
		freemarker.setClassForTemplateLoading(RenderHTML.class, "/");
	}
	
	
	public static String render(List<Axo> list, AxoPreferences pref, LibraryType type) throws Exception {
		// collect categories (assuming that list is ordered by category/path)
		Set<String> cats = new HashSet<String>();
		List<Map<String,String>> catList = new ArrayList<Map<String,String>>();
		for (Axo axo:list) {
			if (!cats.contains(axo.getEscapedCategory())) {
				cats.add(axo.getEscapedCategory());
				Map<String,String> entry = new HashMap<String,String>();
				entry.put("name", axo.getCategory());
				entry.put("id", axo.getEscapedCategory());
				catList.add(entry);
			}
		}

		Map<String, Object> data = new HashMap<String, Object>();
		data.put("catlist", catList);
		data.put("list", list);
		data.put("objectCount", list.size());
		data.put("preferences", pref);
		data.put("typeTitle", type.getTitle());
		data.put("styleClass", type.name());

		Template template = freemarker.getTemplate("objectlisttemplate.html");
		Writer out = new StringWriter();
		template.process(data, out);
		return out.toString();
		
	}
	
	
}
