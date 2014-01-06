package iristk.parser.cfg;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.OutputStream;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import iristk.util.XmlMarshaller;
import iristk.xml.srgs.Item;
import iristk.xml.srgs.ObjectFactory;
import iristk.xml.srgs.OneOf;
import iristk.xml.srgs.Rule;
import iristk.xml.srgs.Ruleref;
import iristk.xml.srgs.ScopeDatatype;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.namespace.QName;

public class SrgsGrammar implements Grammar {

	private static final String SRGSNS = "http://www.w3.org/2001/06/grammar";
	private XmlMarshaller<JAXBElement<iristk.xml.srgs.Grammar>> srgsMarshaller = new XmlMarshaller<>("iristk.xml.srgs");
	iristk.xml.srgs.Grammar grammar;
	private ObjectFactory srgsFactory = new ObjectFactory();
	
	public SrgsGrammar(URI uri) {
		try {
			grammar = srgsMarshaller.unmarshal(uri.toURL()).getValue();
		} catch (JAXBException e) {
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
	}
	
	public SrgsGrammar(File file) {
		try {
			grammar = srgsMarshaller.unmarshal(file).getValue();
		} catch (JAXBException e) {
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}
	
	public SrgsGrammar(String grammarString) {
		try {
			grammar = srgsMarshaller.unmarshal(grammarString).getValue();
		} catch (JAXBException e) {
			e.printStackTrace();
		}
	}
	
	public SrgsGrammar(Grammar cfg) {
		grammar = new iristk.xml.srgs.Grammar();
		for (Object rule : cfg.getRules()) {
			iristk.xml.srgs.Rule srule = new iristk.xml.srgs.Rule();
			srule.setId(cfg.getRuleId(rule));
			if (cfg.getRuleId(rule).equals(cfg.getRoot()))
				grammar.setRoot(srule);
			grammar.getRule().add(srule);
			if (cfg.isPublic(rule))
				srule.setScope(ScopeDatatype.PUBLIC);
			for (Object match : cfg.getMatches(rule)) {
				srule.getContent().add(createMatch(cfg, match, Rule.class));
			}
		}
	}
	
	private Serializable createMatch(Grammar cfg, Object matcher, Class scope) {
		if (cfg.isGroup(matcher)) {
			Item item = new Item();
			int min = cfg.getMinRepeat(matcher);
			int max = cfg.getMaxRepeat(matcher);
			if (min != 1 || max != 1) {
				item.setRepeat(min + "-" + (max == INFINITY ? "" : max));
			}
			for (Object child : cfg.getMatches(matcher)) {
				item.getContent().add(createMatch(cfg, child, Item.class));
			}
			return new JAXBElement<Item>(new QName(SRGSNS, "item"), Item.class, scope, item);
		} else if (cfg.isOneOf(matcher)) {
			OneOf oneof = new OneOf();
			for (Object match : cfg.getMatches(matcher)) {
				Serializable child = createMatch(cfg, match, Item.class);
				Item item;
				if (child instanceof JAXBElement<?> && ((JAXBElement<?>)child).getValue() instanceof Item) {
					item = (Item)((JAXBElement<?>)child).getValue();
				} else {
					item = new Item();
					item.getContent().add(child);
				}
				oneof.getItem().add(item);
			}
			return new JAXBElement<OneOf>(new QName(SRGSNS, "one-of"), OneOf.class, scope, oneof);
		} else if (cfg.isRuleRef(matcher)) {
			Ruleref ruleref = new Ruleref();
			ruleref.setUri("#" + cfg.getRuleRef(matcher));
			return new JAXBElement<Ruleref>(new QName(SRGSNS, "ruleref"), Ruleref.class, scope, ruleref);
		} else if (cfg.isWord(matcher)) {
			return cfg.getWordString(matcher);
		} else if (cfg.isTag(matcher)) {
			return new JAXBElement<String>(new QName(SRGSNS, "tag"), String.class, scope, cfg.getTagScript(matcher));
		} else {
			System.err.println("Cannot recognizer " + matcher);
			return null;
		}
	}
	
	private List<Object> filter(List<? extends Object> matches) {
		ArrayList<Object> result = new ArrayList<Object>();
		for (Object match : matches) {
			if (match instanceof String) {
				String str = ((String)match).trim();
				for (String word : str.split(" ")) {
					if (word.length() > 0)
						result.add(word);
				}
			} else {
				result.add(match);
			}
		}
		return result;
	}

	@Override
	public List<Object> getRules() {
		return filter(grammar.getRule());
	}

	@Override
	public String getRuleId(Object token) {
		if (token instanceof Rule)
			return ((Rule)token).getId();
		else 
			return null;
	}

	@Override
	public boolean isPublic(Object token) {
		return (token instanceof Rule && ((Rule)token).getScope() == ScopeDatatype.PUBLIC);
	}

	@Override
	public List<Object> getMatches(Object token) {
		if (token instanceof JAXBElement) token = ((JAXBElement)token).getValue();
		if (token instanceof Rule) {
			return filter(((Rule)token).getContent());
		} else if (token instanceof Item) {
			return filter(((Item)token).getContent());
		} else if (token instanceof OneOf) {
			return filter(((OneOf)token).getItem());
		} else {
			return new ArrayList<Object>();
		}
	}

	@Override
	public boolean isOneOf(Object token) {
		if (token instanceof JAXBElement) token = ((JAXBElement)token).getValue();
		return token instanceof OneOf;
	}

	@Override
	public boolean isRuleRef(Object token) {
		if (token instanceof JAXBElement) token = ((JAXBElement)token).getValue();
		return (token instanceof Ruleref && ((Ruleref)token).getUri() != null);
	}

	@Override
	public boolean isGroup(Object token) {
		if (token instanceof JAXBElement) token = ((JAXBElement)token).getValue();
		return token instanceof Item;
	}

	@Override
	public int getMinRepeat(Object token) {
		if (token instanceof JAXBElement) token = ((JAXBElement)token).getValue();
		if (token instanceof Item) {
			String repeat = ((Item)token).getRepeat();
			if (repeat != null) {
				if (repeat.contains("-"))
					return Integer.parseInt(repeat.split("-")[0]);
				else
					return Integer.parseInt(repeat);
			}
		}
		return 1;
	}
	
	@Override
	public int getMaxRepeat(Object token) {
		if (token instanceof JAXBElement) token = ((JAXBElement)token).getValue();
		if (token instanceof Item) {
			String repeat = ((Item)token).getRepeat();
			if (repeat != null) {
				String max;
				if (repeat.contains("-"))
					max = repeat.split("-")[1].trim();
				else
					max = repeat.trim();
				if (max.length() == 0)
					return INFINITY;
				else
					return Integer.parseInt(max);
			}
		}
		return 1;
	}

	@Override
	public String getRuleRef(Object token) {
		if (token instanceof JAXBElement) token = ((JAXBElement)token).getValue();
		if (token instanceof Ruleref)
			return ((Ruleref)token).getUri().substring(1);
		else
			return null;
	}

	@Override
	public boolean isWord(Object token) {
		return token instanceof String;
	}

	@Override
	public boolean isTag(Object token) {
		if (token instanceof JAXBElement) {
			JAXBElement elem = (JAXBElement) token;
			return (elem.getName().getLocalPart().equals("tag"));
		} else return false;
	}
	
	@Override
	public String getWordString(Object word) {
		return word.toString();
	}

	@Override
	public String getTagScript(Object matcher) {
		JAXBElement elem = (JAXBElement) matcher;
		return elem.getValue().toString().trim();
	}

	@Override
	public String getLanguage() {
		return grammar.getLang();
	}

	@Override
	public String getRoot() {
		return ((Rule)grammar.getRoot()).getId();
	}
	
	@Override
	public void marshal(OutputStream out) {
		try {
			srgsMarshaller.marshal(srgsFactory.createGrammar(grammar), out);
		} catch (JAXBException e) {
			e.printStackTrace();
		}
	}
}
