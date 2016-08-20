package de.privatepublic.axolist;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.io.FileUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class AXOList {

	public static enum LibraryType {
		FACTORY("Factory Library"), COMMUNITY("Community Library");

		private String title;
		LibraryType(String title) {
			this.title = title;
		}
		public String getTitle() {
			return title;
		}
	}

	public static final String DEFAULT_OUTPUT_PATH = "./axolist/";
	public static final String HTML_RESOURCE_INPUT_PATH = "/res.zip";
	public static final String HTML_RESOURCE_OUTPUT_PATH = "res/";
	public static final String HTML_RESOURCE_STYLE_SHEET_NAME = "styles.css";
	public static final String OUTPUT_FILENAME_FACTORY = "factory-objectlist.html";
	public static final String OUTPUT_FILENAME_COMMUNITY = "community-objectlist.html";
	

	public static void main(String[] args) {
		SimpleLog.info("- - -", "  Axoloti Object List Generator  ", "- - - -");
		SimpleLog.info("- - -", "08/2016 by peter@privatepublic.de", "- - - -");
		SimpleLog.info();
		String axolotiHomePath = null;
		String outputPath = DEFAULT_OUTPUT_PATH;
		if (args.length==0) {
			SimpleLog.error("Called with no arguments. Please call this program with the path to your Axoloti home directory containing the axoloti.prefs file. Optional second parameter is the path to the desired output folder. Per default output is to: "+DEFAULT_OUTPUT_PATH);
			System.exit(-1);
		}
		else {
			axolotiHomePath = args[0];
			if (args.length>1) {
				outputPath = args[1];
				if (!outputPath.endsWith("/")) {
					outputPath += "/";
				}
			}
		}
		AxoPreferences pref = null;
		try {
			pref = new AxoPreferences(axolotiHomePath);
		} catch (Exception e) {
			SimpleLog.error("Could not read axoloti.prefs in",axolotiHomePath);
			System.exit(-1);
		}
		SimpleLog.info("Found Axoloti preferences:", pref);
		SimpleLog.info("Rendering HTML output to:", outputPath);
		new File(outputPath).mkdirs();
		List<Axo> objects = readObjects(LibraryType.FACTORY, pref);
		renderHTMLToFile(outputPath, LibraryType.FACTORY, objects, pref);
		objects = readObjects(LibraryType.COMMUNITY, pref);
		renderHTMLToFile(outputPath, LibraryType.COMMUNITY, objects, pref);
		unpackHTMLResources(outputPath + HTML_RESOURCE_OUTPUT_PATH);
		SimpleLog.info("Done.");
	}


	private static List<Axo> readObjects(LibraryType type, AxoPreferences pref) {
		List<Axo> axoList = new ArrayList<Axo>();
		String path;
		switch (type) {
		case COMMUNITY:
			path = pref.getCommunityDir();
			break;
		default:
			path = pref.getFactoryDir();
			break;
		}
		File axoDir = new File(path);
		if (!axoDir.exists()) {
			SimpleLog.error("Objects folder not found:", axoDir);
			return axoList;
		}
		SimpleLog.info("Reading Axoloti objects in", axoDir);
		List<File> fileList = (List<File>) FileUtils.listFiles(axoDir, new String[] {"axo","axs"}, true);
		Collections.sort(fileList, new Comparator<File>() {
			@Override
			public int compare(File f1, File f2) {
				File p0 = f1.getParentFile();
				File p1 = f2.getParentFile();
				int parentComp = p0.getPath().compareTo(p1.getPath());
				if (parentComp!=0) {
					return parentComp;
				}
				return f1.getPath().compareTo(f2.getPath());
			}
		});
		for (File o:fileList) {
			String fpath = o.toString();
			String categoryName = fpath.substring(path.length(), fpath.lastIndexOf(File.separatorChar)).replace("\\", "/");
			File axoFile = new File(fpath);
			try {
				DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
				DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
				Document doc = dBuilder.parse(axoFile);
				if (fpath.endsWith("axo")) {
					NodeList nl = doc.getElementsByTagName("obj.normal");
					if (nl!=null && nl.getLength()>0) {
						for (int i=0;i<nl.getLength();i++) {
							Node objectNode = nl.item(i);
							Axo axoObject = new Axo(objectNode, categoryName); 
							axoList.add(axoObject);
						}
					}
				}
				else if (fpath.endsWith("axs")) {
					String name = axoFile.getName();
					name = name.substring(0, name.length()-4);
					Axo axoObject = new Axo(doc, name, categoryName);
					axoList.add(axoObject);
				}
			} catch (ParserConfigurationException | SAXException | IOException e) {
				SimpleLog.info("Could not parse object:", axoFile, ", cause:", e);
			}
		}
		SimpleLog.info("Found", axoList.size(), "Axoloti objects");
		return axoList;
	}


	private static void renderHTMLToFile(String outfilepath, LibraryType type, List<Axo> axoList, AxoPreferences pref) {
		switch (type) {
		case FACTORY:
			outfilepath += OUTPUT_FILENAME_FACTORY;
			break;
		case COMMUNITY:
			outfilepath += OUTPUT_FILENAME_COMMUNITY;
			break;
		}

		try {
			SimpleLog.info("Rendering HTML...");
			String html = RenderHTML.render(axoList, pref, type);
			try {
				File outFile = new File(outfilepath);
				SimpleLog.info("Writing object list HTML to:", outFile);
				FileUtils.writeStringToFile(outFile, html, "utf-8");
			} catch (IOException e) {
				SimpleLog.error("Error writing file:", e);
			}
		} catch (Exception e) {
			SimpleLog.error("Error rendering HTML:", e);
		}
	}

	private static void unpackHTMLResources(String targetPath) {
		SimpleLog.info("Extracting web resources...");
		byte[] buffer = new byte[1024];
		try {
			ZipInputStream zis = new ZipInputStream(AXOList.class.getResourceAsStream(HTML_RESOURCE_INPUT_PATH));
			ZipEntry ze = zis.getNextEntry();
			while(ze!=null){
				String fileName = ze.getName();
				File newFile = new File(targetPath + fileName);
				new File(newFile.getParent()).mkdirs();
				FileOutputStream fos = new FileOutputStream(newFile);
				int len;
				while ((len = zis.read(buffer)) > 0) {
					fos.write(buffer, 0, len);
				}

				fos.close();
				ze = zis.getNextEntry();
			}
			zis.closeEntry();
			zis.close();
			FileUtils.copyInputStreamToFile(AXOList.class.getResourceAsStream("/"+HTML_RESOURCE_STYLE_SHEET_NAME), new File(targetPath+HTML_RESOURCE_STYLE_SHEET_NAME));
			
		} catch (IOException e) {
			SimpleLog.error("Error:", e);
		}

	}
}
