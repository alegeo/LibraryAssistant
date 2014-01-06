package iristk.flow;

import iristk.util.FileFinder;
import iristk.util.XmlUtils;
import iristk.xml.flow.Param;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.List;

public class TemplateSchemaCompiler {

	private FlowXmlReader flowReader = new FlowXmlReader();
	PrintStream xsd;

	public TemplateSchemaCompiler(File xmlFile) throws FlowCompilerException {
		flowReader.read(xmlFile);
	}

	public static void compile(String flowFileName, String dir) throws FlowCompilerException {
		File fdir = new File(dir);
		File flowFile = null;
		if (new File(flowFileName).exists()) {
			flowFile = new File(flowFileName);
		} else if (new File(fdir, flowFileName).exists()) {
			flowFile = new File(fdir, flowFileName);
		} else {
			String f = FileFinder.findFirst(dir + "/src", flowFileName);
			if (f != null) {
				flowFile = new File(f);
			}
		}
		if (flowFile != null) {
			System.out.println("Compiling flow: " + flowFile.getAbsolutePath());
			TemplateSchemaCompiler fcompiler = new TemplateSchemaCompiler(flowFile);
			File srcFile = new File(flowFile.getAbsolutePath().replaceFirst("\\.[A-Za-z]+$", "\\.xsd"));
			fcompiler.compileToFile(srcFile);
			System.out.println("Compiled to xsd: " + srcFile.getAbsolutePath());
		}
	}

	private void compileToFile(File xsdFile) throws FlowCompilerException {
		try {
			FileOutputStream fstream = new FileOutputStream(xsdFile);
			compileToStream(fstream);
			fstream.close();
			XmlUtils.indentXmlFile(xsdFile);
		} catch (Exception e) {
			throw new FlowCompilerException(e.getMessage());
		}
	}

	private void compileToStream(OutputStream outStream) {
		xsd = new PrintStream(outStream);
		xsd.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
		xsd.println("<schema xmlns=\"http://www.w3.org/2001/XMLSchema\" xmlns:flow=\"iristk.flow\" elementFormDefault=\"qualified\" targetNamespace=\"" + flowReader.getPackage() + "." + flowReader.getName() + "\">");
		xsd.println("<import schemaLocation=\"flow.xsd\" namespace=\"iristk.flow\"/>");
		for (Object template : flowReader.getTemplates()) {
			if (template instanceof iristk.xml.flow.ActionTemplate) {
				iristk.xml.flow.ActionTemplate atemplate = (iristk.xml.flow.ActionTemplate)template;
				xsd.println("<element name=\""+atemplate.getId()+"\">");
				xsd.println("<complexType mixed=\"true\">");
				xsd.println("<sequence>");
				xsd.println("<any processContents=\"lax\" namespace=\"##any\" minOccurs=\"0\" maxOccurs=\"unbounded\"/>");
				xsd.println("</sequence>");
				printParameters(atemplate.getParam());
				xsd.println("</complexType>");
				xsd.println("</element>");
			} else if (template instanceof iristk.xml.flow.CatchTemplate) {
				iristk.xml.flow.CatchTemplate ctemplate = (iristk.xml.flow.CatchTemplate)template;
				xsd.println("<element name=\""+ctemplate.getId()+"\">");
				xsd.println("<complexType>");
				xsd.println("<complexContent>");
				xsd.println("<extension base=\"flow:actionSequenceType\">");
				printParameters(ctemplate.getParam());
				xsd.println("<anyAttribute namespace=\"##other\" />");
				xsd.println("</extension>");
				xsd.println("</complexContent>");
				xsd.println("</complexType>");
				xsd.println("</element>");
			}
		}
		xsd.println("</schema>");
	}

	private void printParameters(List<Param> params) {
		for (Param param : params) {
			String attr = "";
			attr = "<attribute name=\"" + param.getName() + "\"";
			if (param.getDefault() != null) 
				attr += " default=\"" + param.getDefault() + "\"";
			if (param.getAlt().size() > 0) {
				attr += ">";
				attr += "<simpleType>";
				attr += "<restriction base=\"string\">";
				for (String alt : param.getAlt()) {
					attr += "<enumeration value=\"" + alt + "\" />";
				}
				attr += "</restriction>";
				attr += "</simpleType>";
				attr += "</attribute>";
			} else {
				attr += " type=\"string\"/>";
			}
			xsd.println(attr);
		}
	}

}
