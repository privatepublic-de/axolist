package de.privatepublic.axolist;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class AxoPreferences {
	
	private static final String PREF_FILENAME = "axoloti.prefs";
	
	private String version;
	private String factoryDir;
	private String communityDir;
	private String date;

	public AxoPreferences(String axodir) throws Exception {
		SimpleDateFormat formatter = new SimpleDateFormat("MMM d, yyyy");
		date = formatter.format(new Date());
		File axoPref = new File(axodir, PREF_FILENAME);
		if (axoPref.exists()) {
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			Document doc = dBuilder.parse(axoPref);

			Element prefs = doc.getDocumentElement();
			this.version = prefs.getAttribute("appVersion");
			NodeList libs = doc.getElementsByTagName("gitlib");
			if (libs!=null && libs.getLength()>0) {
				for (int i=0;i<libs.getLength();i++) {
					Element e = (Element)libs.item(i);
					String libname = e.getElementsByTagName("Id").item(0).getFirstChild().getNodeValue();
					String location = e.getElementsByTagName("LocalLocation").item(0).getFirstChild().getNodeValue();
					if ("factory".equals(libname)) {
						this.factoryDir = location+"objects/";
					}
					if ("community".equals(libname)) {
						this.communityDir = location+"objects/";
					}
				}
			}				
		}
		else {
			throw new IllegalStateException("Could not read Axoloti preferences in "+axodir);
		}
	}
	
	public String getVersion() {
		return version;
	}

	public String getFactoryDir() {
		return factoryDir;
	}

	public String getCommunityDir() {
		return communityDir;
	}

	public String getDate() {
		return date;
	}
	
	@Override
	public String toString() {
		return "Version "+version+", factory-dir: "+factoryDir+", community-dir: "+communityDir+", date: "+date;
	}
}