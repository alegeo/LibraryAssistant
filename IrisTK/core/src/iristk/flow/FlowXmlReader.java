package iristk.flow;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import iristk.util.XmlMarshaller;
import iristk.xml.flow.Flow;
import iristk.xml.flow.Include;
import iristk.xml.flow.State;
import iristk.xml.flow.Flow.Import;
import iristk.xml.flow.Var;

import javax.xml.bind.JAXBException;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.Location;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

public class FlowXmlReader {

	private Flow flow;
	private List<String> importedClasses;
	private List<State> states;
	private List<Var> vars;
	private XmlMarshaller<Object> flowMarshaller = new XmlMarshaller<Object>("iristk.xml.flow");
	private List<Object> xmlTemplates;
	
	public FlowXmlReader() {
		flowMarshaller.storeLocations();
	}
	
	public Location getLocation(Object object) {
		return flowMarshaller.getLocation(object);
	}
	
	public void read(File xmlfile) throws FlowCompilerException {
		try {
			flowMarshaller.validate(xmlfile);
			read((Flow) flowMarshaller.unmarshal(xmlfile));
		} catch (JAXBException e) {
			if (e.getMessage() != null) {
				throw new FlowCompilerException(e.getMessage());
			} else if (e.getLinkedException() != null) {
				if (e.getLinkedException() instanceof SAXParseException) {
					SAXParseException spe = (SAXParseException)e.getLinkedException();
					throw new FlowCompilerException(spe.getMessage(), spe.getLineNumber());
				} else if (e.getLinkedException().getMessage() != null) {
					throw new FlowCompilerException(e.getLinkedException().getMessage());
				} else {
					throw new FlowCompilerException("Unknown XML parsing exception");
				}
			} else {
				throw new FlowCompilerException("Unknown XML parsing exception");
			}
		} catch (FileNotFoundException e) {
			throw new FlowCompilerException("Could not find file " + xmlfile.getAbsolutePath());
		} catch (ParserConfigurationException e) {
			throw new FlowCompilerException(e.getMessage());
		} catch (SAXParseException e) {
			throw new FlowCompilerException(e.getMessage(), e.getLineNumber());
		} catch (SAXException e) {
			throw new FlowCompilerException(e.getMessage());
		} catch (IOException e) {
			throw new FlowCompilerException(e.getMessage());
		}
	}
	
	public void read(Flow xmlflow) throws FlowCompilerException {
		flow = xmlflow;
		importedClasses = new ArrayList<String>();
		states = new ArrayList<State>();
		xmlTemplates = new ArrayList<Object>();
		for (Object o : flow.getStatesAndTemplates()) {
			addStateOrTemplate(o);
		}
		
		vars = new ArrayList<Var>();
		vars.addAll(flow.getVar());
		
		for (Import imp : flow.getImport()) {
			importedClasses.add(imp.getClazz());
		}
		
		for (Include include : flow.getInclude()) {
			include(null, include.getHref());
		}
	}
	
	private void addStateOrTemplate(Object o) {
		if (o instanceof iristk.xml.flow.CatchTemplate) {
			xmlTemplates.add(o);
		} else if (o instanceof iristk.xml.flow.ActionTemplate) {
			xmlTemplates.add(o);
		} else if (o instanceof iristk.xml.flow.State) {
			states.add((State) o);
		}
	}

	private void include(File context, String href) throws FlowCompilerException {
		File file;
		if (context == null)
			file = new File(href);
		else
			file = new File(context.getParent(), href);
		Flow flow;
		try {
			flow = (Flow) flowMarshaller.unmarshal(file);
		} catch (JAXBException e) {
			throw new FlowCompilerException(e.getMessage());
		} catch (FileNotFoundException e) {
			throw new FlowCompilerException(e.getMessage());
		}
		for (Include inc : flow.getInclude()) {
			include(file, inc.getHref());
		}
		for (Object o : flow.getStatesAndTemplates()) {
			addStateOrTemplate(o);
		}
		vars.addAll(flow.getVar());
		for (Import imp : flow.getImport()) {
			if (!importedClasses.contains(imp.getClazz())) 
				importedClasses.add(imp.getClazz());
		}
	}

	public String getPackage() {
		return flow.getPackage();
	}

	public List<String> getImportedClasses() {
		return importedClasses;
	}

	public String getExtends() {
		return flow.getExtends();
	}

	public List<Var> getVariables() {
		return vars;
	}

	public String getInitial() {
		return flow.getInitial();
	}

	public List<Object> getTemplates() {
		return xmlTemplates;
	}

	public List<State> getStates() {
		return states;
	}

	public List<Object> unmarshal(NodeList childNodes) throws FlowCompilerException {
		try {
			return flowMarshaller.unmarshal(childNodes);
		} catch (JAXBException e) {
			throw new FlowCompilerException(e.getMessage());
		}
	}

	public Object unmarshal(Element node) throws FlowCompilerException {
		try {
			return flowMarshaller.unmarshal(node);
		} catch (JAXBException e) {
			throw new FlowCompilerException(e.getMessage());
		}
	}
	
	public String getName() {
		return flow.getName();
	}
	
}
