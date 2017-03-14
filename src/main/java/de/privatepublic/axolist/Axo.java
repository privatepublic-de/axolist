package de.privatepublic.axolist;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class Axo {
	private List<Attribute> inlets;
	private List<Attribute> outlets;
	private List<Attribute> params;
	private List<Attribute> attribs;
	private List<Attribute> displays;
	private String name, description, category, escapedCategory, author, license;

	// create from object root node in axo file
	public Axo(Node node, String categoryName) {
		this.name = node.getAttributes().getNamedItem("id").getNodeValue();
		Element el = (Element)node;
		this.description = getSValue(el.getElementsByTagName("sDescription").item(0), "(no description)");
		this.author = getSValue(el.getElementsByTagName("author").item(0), "(no author)");
		this.license = getSValue(el.getElementsByTagName("license").item(0));
		this.inlets = getAttributes(el, Attribute.Type.INLET);
		this.outlets = getAttributes(el, Attribute.Type.OUTLET);
		this.params = getAttributes(el, Attribute.Type.PARAM);
		this.attribs = getAttributes(el, Attribute.Type.ATTRIB);
		this.displays = getAttributes(el, Attribute.Type.DISP);
		this.category = categoryName;
		escapedCategory = this.category.replaceAll("[^a-zA-z0-9]", "");
	}
	
	// create from axs subpatch document
	public Axo(Document doc, String objName, String categoryName) {
		inlets = new ArrayList<Attribute>();
		outlets = new ArrayList<Attribute>();
		displays = new ArrayList<Attribute>();
		attribs = new ArrayList<Attribute>();
		params = new ArrayList<Attribute>();
		
		NodeList nodelist = doc.getElementsByTagName("obj");
		if (nodelist!=null && nodelist.getLength()>0) {
			for (int i=0;i<nodelist.getLength();i++) {
				Node objectNode = nodelist.item(i);
				String type = objectNode.getAttributes().getNamedItem("type").getNodeValue();
				String name = objectNode.getAttributes().getNamedItem("name").getNodeValue();
				if (type.startsWith("patch/inlet")) {
					inlets.add(new Attribute(type, name, Attribute.Type.INLET));
				}
				if (type.startsWith("patch/outlet")) {
					outlets.add(new Attribute(type, name, Attribute.Type.OUTLET));
				}
				
				NodeList paramcontainer = ((Element)objectNode).getElementsByTagName("params");
				if (paramcontainer!=null && paramcontainer.getLength()>0) {
					Node paramNode = paramcontainer.item(0);
					NodeList paramlist = paramNode.getChildNodes();
					for (int c=0;c<paramlist.getLength();c++) {
						Node child = paramlist.item(c);
						if (child.hasAttributes() && child.getAttributes().getNamedItem("onParent")!=null && "true".equals(child.getAttributes().getNamedItem("onParent").getNodeValue())) {
							params.add(new Attribute(child.getNodeName(), name, Attribute.Type.PARAM));
						}
					}
				}
			}
		}
		nodelist = doc.getElementsByTagName("patchobj");
		if (nodelist!=null && nodelist.getLength()>0) {
			for (int i=0;i<nodelist.getLength();i++) {
				Node objectNode = nodelist.item(i);
				NodeList paramcontainer = ((Element)objectNode).getElementsByTagName("params");
				if (paramcontainer!=null && paramcontainer.getLength()>0) {
					Node paramNode = paramcontainer.item(0);
					NodeList paramlist = paramNode.getChildNodes();
					for (int c=0;c<paramlist.getLength();c++) {
						Node child = paramlist.item(c);
						if (child.getAttributes()!=null && child.getAttributes().getNamedItem("name")!=null) {
							String name = child.getAttributes().getNamedItem("name").getNodeValue();
							if (child.hasAttributes() && child.getAttributes().getNamedItem("onParent")!=null && "true".equals(child.getAttributes().getNamedItem("onParent").getNodeValue())) {
								params.add(new Attribute(child.getNodeName(), name, Attribute.Type.PARAM));
							}
						}
					}
				}
			}
		}
		
		this.author = getSValue(doc.getElementsByTagName("Author"), "(no author)");
		this.license = getSValue(doc.getElementsByTagName("License"));
		this.name = objName;
		this.description = getSValue(doc.getElementsByTagName("notes"), "(no description)");
		this.category = categoryName;
		escapedCategory = this.category.replaceAll("[^a-zA-z0-9]", "");
	}

	public String getAuthor() {
		return author;
	}

	public String getLicense() {
		return license;
	}
	
	public String getId() {
		return "o"+name.hashCode();
	}

	public List<Attribute> getInlets() {
		return inlets;
	}

	public List<Attribute> getOutlets() {
		return outlets;
	}

	public List<Attribute> getParams() {
		return params;
	}

	public List<Attribute> getAttribs() {
		return attribs;
	}

	public List<Attribute> getDisplays() {
		return displays;
	}

	public String getName() {
		return name;
	}

	public String getDescription() {
		return description;
	}

	public String getCategory() {
		return category;
	}

	public String getEscapedCategory() {
		return escapedCategory;
	}

	
	public boolean getHasInOutOrParams() {
		return inlets.size() + outlets.size() + params.size() + attribs.size() + displays.size() > 0;
	}
	
	
	private String getSValue(Node node) {
		if (node!=null) {
			Node child = node.getFirstChild();
			return child!=null?child.getNodeValue():"";

		}
		return "";
	}
	
	private String getSValue(NodeList nodes) {
		if (nodes!=null && nodes.getLength()>0) {
			return getSValue(nodes.item(0));

		}
		return "";
	}
	
	private String getSValue(Node node, String defaultText) {
		if (node!=null) {
			Node child = node.getFirstChild();
			String result = child!=null?child.getNodeValue():defaultText;
			return "".equals(result)?defaultText:result;

		}
		return defaultText;
	}
	
	private String getSValue(NodeList nodes, String defaultText) {
		if (nodes!=null && nodes.getLength()>0) {
			return getSValue(nodes.item(0), defaultText);

		}
		return defaultText;
	}
	
	private List<Attribute> getAttributes(Element rootElement, Attribute.Type type) {
		NodeList nlist = rootElement.getElementsByTagName(type.getXmlTag());
		List<Attribute> result = new ArrayList<Attribute>();
		if (nlist.getLength()>0) {
			Node node = nlist.item(0);
			NodeList children = node.getChildNodes();
			for (int i=0; i<children.getLength();i++) {
				Node child = children.item(i);
				if (child.hasAttributes()) {
					result.add(new Attribute(type, child));
				}
			}
		}
		return result;
	}
	

	public static class Attribute {

		public static enum Type {
			INLET("inlets"), OUTLET("outlets"), PARAM("params"), ATTRIB("attribs"), DISP("displays");

			private String xmltag;

			Type(String tag) {
				this.xmltag = tag;
			}

			public String getXmlTag() {
				return xmltag;
			}

		};

		private String name, type, description, style;

		public String getName() {
			return name;
		}
		
		public String getType() {
			return type;
		}

		public String getDescription() {
			return description;
		}

		public String getStyle() {
			return style;
		}

		public Attribute(Type at, Node node) {
			name = node.getAttributes().getNamedItem("name").getNodeValue();
			description = node.getAttributes().getNamedItem("description") != null
					? node.getAttributes().getNamedItem("description").getNodeValue() : "";
			type = node.getNodeName();
			style = assembleStyleClass(type);
		}
		
		public Attribute(String typeName, String name, Type at) {
			this.name = name;
			this.description = "";
			type = typeName;
			if (TYPE_MAP.get(typeName)!=null) {
				style = TYPE_MAP.get(typeName)+" " + typeName.replaceAll("\\.", "-");
			}
			else {
				style = assembleStyleClass(typeName);
			}
		}
		
		private String assembleStyleClass(String typeName) {
			String result = "";
			if (typeName.contains("buffer")) {
				result = "rd";
			} else if (typeName.contains("frac")) {
				result = "bl";
			} else if (typeName.contains("bool")) {
				result = "ye";
			} else if (typeName.contains("int")) {
				result = "gr";
			} else if (typeName.contains("char")) {
				result = "pn";
			} else {
				result = "";
			}
			return result + " " + typeName.replaceAll("\\.", "-");
		}
	}

	private static Map<String, String> TYPE_MAP = new HashMap<String, String>();
	static {
		TYPE_MAP.put("patch/inlet a", "rd");
		TYPE_MAP.put("patch/inlet f", "bl");
		TYPE_MAP.put("patch/inlet i", "gr");
		TYPE_MAP.put("patch/inlet b", "ye");
		TYPE_MAP.put("patch/inlet s", "pn");
		TYPE_MAP.put("patch/outlet a", "rd");
		TYPE_MAP.put("patch/outlet f", "bl");
		TYPE_MAP.put("patch/outlet i", "gr");
		TYPE_MAP.put("patch/outlet b", "ye");
		TYPE_MAP.put("patch/outlet s", "pn");
	}
}