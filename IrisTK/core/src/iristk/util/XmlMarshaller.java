package iristk.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.Unmarshaller.Listener;
import javax.xml.stream.Location;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.util.StreamReaderDelegate;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

public class XmlMarshaller<T> extends XmlUtils {
	
	private JAXBContext jc;
	public Unmarshaller unmarshaller;
	public Marshaller marshaller;
	private Map<Object, Location> locations = new HashMap<Object, Location>();
	//private XMLStreamReader xsr = null;	
	
	public XmlMarshaller(String namespace) {
		super();
		try {
			jc = JAXBContext.newInstance(namespace);
			unmarshaller = jc.createUnmarshaller();
			marshaller = jc.createMarshaller();
		} catch (JAXBException e) {
			e.printStackTrace();
		} 
	}

	public void storeLocations() {
		unmarshaller.setListener(new LocationListener());
	}
	
	private class LocationListener extends Listener {

        @Override
        public void beforeUnmarshal(Object target, Object parent) {
        	/*
        	if (xsr != null) {
        		locations.put(target, xsr.getLocation());
        	}
        	*/
        }
        
	}

    public Location getLocation(Object o) {
        return locations.get(o);
    }
	
	public T unmarshal(String xml) throws JAXBException {
		synchronized (unmarshaller) {
			return (T) unmarshaller.unmarshal(new ByteArrayInputStream(xml.getBytes()));
		}
	}
	
	public T unmarshal(byte[] bytes) throws JAXBException {
		synchronized (unmarshaller) {
			return (T) unmarshaller.unmarshal(new ByteArrayInputStream(bytes));
		}
	}
	
	private class MyXmlReader extends StreamReaderDelegate {
		
		public MyXmlReader(XMLStreamReader reader) {
			super(reader);
		}
		
	}
	
	public T unmarshal(URL url) throws JAXBException, FileNotFoundException {
		synchronized (unmarshaller) {
			return (T) unmarshaller.unmarshal(url);
		}
	}
	
	public T unmarshal(File file) throws JAXBException, FileNotFoundException {
		synchronized (unmarshaller) {
			return (T) unmarshaller.unmarshal(new FileInputStream(file));
			/*
			try {
				XMLInputFactory xif = XMLInputFactory.newFactory();
		        FileInputStream xml = new FileInputStream(file);
		        xsr = new MyXmlReader(xif.createXMLStreamReader(xml));
				T result = (T) unmarshaller.unmarshal(xsr);
				xsr = null;
				return result;
			} catch (FileNotFoundException e) {
				e.printStackTrace();
				return null;
			} catch (XMLStreamException e) {
				e.printStackTrace();
				return null;
			}
			*/
			/*
			try {
				Document doc = PositionalXMLReader.readXML(new FileInputStream(file));
				return (T) unmarshal(doc.getDocumentElement());
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} catch (SAXException e) {
				e.printStackTrace();
			} 
			return null;
			*/
		}
	}
	
	public T unmarshal(Node node) throws JAXBException {
		synchronized (unmarshaller) {
			return (T) unmarshaller.unmarshal(node);
		}
	}
	
	public Document marshalToDOM(T data) throws JAXBException {
		synchronized (marshaller) {
			Document doc = builder.newDocument();
			marshaller.marshal(data, doc);
			return doc;
		}
	}
	
	public void marshal(T data, File file) throws JAXBException {
		try {
			FileOutputStream fout = new FileOutputStream(file);
			synchronized (marshaller) {
				marshaller.marshal(data, fout);
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}
	
	public String marshal(T data) throws JAXBException {
		return marshal(data, false);
	}
	
	public String marshal(T data, boolean xmlDecl) throws JAXBException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		synchronized (marshaller) {
			marshaller.marshal(data, baos);
		}
		String result = baos.toString();
		// Remove the XML declaration
		if (!xmlDecl) {
			result = result.replaceFirst("<.*?>", "");
		}
		result = result.replaceAll("\\s+", " ");
		result = result.replaceAll("> <", "><");
		return result;
	}
	

	public void marshal(T data, OutputStream out) throws JAXBException {
		marshaller.marshal(data, out);
	}
	
	public List<Object> unmarshal(NodeList childNodes) throws JAXBException {
		List<Object> result = new ArrayList<Object>();
		for (int i = 0; i < childNodes.getLength(); i++) {
			Node child = childNodes.item(i);
			if (child instanceof Text) {
				result.add(((Text)child).getNodeValue());
			} else if (!child.getNamespaceURI().equals("iristk.flow")) {
				result.add(child);
			} else {
				result.add(unmarshal(childNodes.item(i)));
			}
		}
		return result;
	}

}
