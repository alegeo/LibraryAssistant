package iristk.flow;

import iristk.util.FileFinder;
import iristk.util.Replacer;
import iristk.util.XmlUtils;
import iristk.xml.flow.Block;
import iristk.xml.flow.OnEvent;
import iristk.xml.flow.Choice;
import iristk.xml.flow.Expr;
import iristk.xml.flow.Item;
import iristk.xml.flow.OnEntry;
import iristk.xml.flow.OnExit;
import iristk.xml.flow.Param;
import iristk.xml.flow.Var;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;
import javax.xml.bind.JAXBElement;
import javax.xml.namespace.QName;

import org.w3c.dom.Attr;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import java.lang.String;
import java.lang.Object;

public class FlowCompiler {

	private static final String stringPattern = "\".*?\"";
	protected String classPackage;
	protected String className;
	protected CodeStream code;
	private boolean useUniqueNames = false;
	private static int uniqueNameSuffix = 0;
	private FlowXmlReader flowReader = new FlowXmlReader();

	public FlowCompiler(File xmlFile) throws FlowCompilerException {
		flowReader.read(xmlFile);
	}

	public FlowCompiler(iristk.xml.flow.Flow xmlFlow) throws FlowCompilerException {
		flowReader.read(xmlFlow);
	}

	public void compileToFile(File srcFile) throws FlowCompilerException {
		try {
			FileOutputStream fstream = new FileOutputStream(srcFile);
			compileToStream(fstream);
			fstream.close();
		} catch (FileNotFoundException e) {
			throw new FlowCompilerException(e.getMessage());
		} catch (IOException e) {
			throw new FlowCompilerException(e.getMessage());
		}
	}

	public void compileToStream(OutputStream out) throws FlowCompilerException {
		if (useUniqueNames)
			uniqueNameSuffix++;

		code = new CodeStream(out);

		className = getLocalFlowName();

		code.println("package " + flowReader.getPackage() + ";");
		code.println();

		printImports();
		for (String imp : flowReader.getImportedClasses()) {
			code.println("import " + imp + ";");
		}
		code.println();
		code.println("public class " + className + " extends " + flowReader.getExtends() + " {");
		code.println();
		printVariables(flowReader.getVariables());
		code.println();
		printVariableAccessors(flowReader.getVariables());
		code.println();
		code.println("public " + className + "() {");
		code.println("}");
		code.println();

		if (flowReader.getInitial() != null) {
			code.println("@Override");
			code.println("protected State getInitialState() {return new " + getFlowName() + "." + flowReader.getInitial() + "();}");
			code.println();
		}

		printTemplates(flowReader.getTemplates());

		printStates(flowReader.getStates());

		code.println();
		code.println("}");
	} 
	
	private boolean stateExists(String name) {
		for (iristk.xml.flow.State state: flowReader.getStates()) {
			if (state.getId().equals(name))
				return true;
		}
		return false;
	}

	private void printTemplates(List<Object> templates) throws FlowCompilerException {
		for (Object template : templates) {
			if (template instanceof iristk.xml.flow.ActionTemplate) {
				iristk.xml.flow.ActionTemplate atemplate = (iristk.xml.flow.ActionTemplate)template;
				code.println("public" + (atemplate.isStatic() ? " static" : "") + " ActionTemplate " + atemplate.getId() + " = new ActionTemplate() {");
				code.println();
				code.println("@Override");
				code.println("public boolean execute(FlowRunner flowRunner, Record parameters) {");
				printDeclParameters(atemplate.getParam(), "");
				printSetParameters(atemplate.getParam());
				code.println("boolean result = false;");
				code.println("EXECUTION: {");
				printActions(atemplate.getAction());
				code.println("result = true;");	
				code.println("}");
				code.println("return result;");
				code.println("}");
				code.println("};");
			} else if (template instanceof iristk.xml.flow.CatchTemplate) {
				iristk.xml.flow.CatchTemplate ctemplate = (iristk.xml.flow.CatchTemplate)template;
				code.println("public" + (ctemplate.isStatic() ? " static" : "") + " CatchTemplate " + ctemplate.getId() + " = new CatchTemplate() {");
				code.println();
				code.println("@Override");
				code.println("public boolean test(Event event, Record parameters) {");
				printDeclParameters(ctemplate.getParam(), "");
				printSetParameters(ctemplate.getParam());
				if (ctemplate.getEvent() != null)
					code.println("if (event.triggers(\"" + ctemplate.getEvent() + "\")) {");
				if (ctemplate.getCond() != null)
					code.println("return ("+ formatCondExpr(ctemplate.getCond()) + ");");
				else
					code.println("return true;");
				code.println("}");
				code.println("else return false;");
				code.println("}");
				code.println("};");
			}
			code.println();
		}
	}

	private void printStates(List<iristk.xml.flow.State> states) throws FlowCompilerException {
		for (iristk.xml.flow.State state : states) {
			String ext = "State";
			if (state.getExtends() != null) {
				ext = state.getExtends();
			}
			String modifiers = state.getScope() + " " + (state.isStatic() ? "static " : "");
			code.println();
			code.println(modifiers + "class " + state.getId() + " extends " + ext + " {");
			code.println();
			if (state.getVar() != null) {
				printVariables(state.getVar());
				code.println();
			}
			if (state.getParam() != null) {
				printParameters(state.getParam());
			}
			printTriggers(state.getTrigger(), true);
			printOnExit(state.getTrigger());
			code.println();
			code.println("}");
			code.println();
		}
	}

	protected void printImports() {
		code.println("import iristk.system.Event;");
		code.println("import iristk.flow.*;");
		code.println("import iristk.util.Record;");
		code.println("import static iristk.util.Converters.*;");
	}

	private void printVariables(List<Var> vars) throws FlowCompilerException {
		for (Var var : vars) {
			code.println("public " + variable(var));
		}
	}

	private String variable(Var var) throws FlowCompilerException {
		String line = var.getType() + " " + var.getName();
		if (var.getValue() != null) {
			line += " = " + formatAttrExpr(var.getValue()) + ";";
			if (var.getContent() != null && var.getContent().size() > 0) {
				line += "{\n" + formatExec(XmlUtils.nodesToString(var.getContent())) + "\n}";
			}
		} else if (var.getContent() != null && var.getContent().size() > 0) {
			line += " = " +  concatFun(processString(var.getContent())).replaceAll("\n", "").replaceAll("\\s+", " ");
		} else {
			line += ";";
		}
		return line;
	}
	
	private void printVariableAccessors(List<Var> vars) {
		code.println("public Object getVariable(String name) {");
		for (Var var : vars) {
			code.println("if (name.equals(\""+var.getName()+"\")) return "+var.getName()+";");
		}
		code.println("return null;");
		code.println("}");
		code.println();
		code.println("public void setVariable(String name, Object value) {");
		for (Var var : vars) {
			code.println("if (name.equals(\""+var.getName()+"\")) "+var.getName()+" = " + convertType(var.getType(), "value") + ";");
		}
		code.println("}");
	}

	private void printParameters(List<Param> params) throws FlowCompilerException {
		printDeclParameters(params, "public");
		code.println();
		code.println("@Override");
		code.println("public void setParameters(Record parameters) {");
		code.println("super.setParameters(parameters);");
		printSetParameters(params);
		code.println("}");
		code.println();
	}

	private void printSetParameters(List<Param> params) {
		for (Param param : params) {
			code.println("if (parameters.has(\"" + param.getName() + "\")) {");
			code.println(param.getName() + " = " + convertType(param.getType(), "parameters.get(\"" + param.getName() + "\")") + ";");
			code.println("}");
		}
	}

	private void printDeclParameters(List<Param> params, String modifiers) throws FlowCompilerException {
		for (Param param : params) {
			String var = modifiers + " " + param.getType() + " " + param.getName() + " = ";
			if (param.getDefault() != null)
				var += formatAttrExpr(param.getDefault());
			else {
				var += "null";
			}
			var += ";";
			code.println(var.trim());
		}
	}

	private String convertType(String type, String value) {
		if (type.equals("String")) {
			return "makeString(" + value + ")";
		} else if (type.equals("Record")) {
			return "makeRecord(" + value + ")";
		} else if (type.equals("Boolean")) {
			return "makeBool(" + value + ")";
		} else if (type.equals("Integer")) {
			return "makeInt(" + value + ")";
		} else if (type.equals("Float")) {
			return "makeFloat(" + value + ")";
		} else {
			return "(" + type + ") " + value;
		}
	}

	private void printOnExit(List<?> eventHandlers) throws FlowCompilerException {
		OnExit onExit = null;
		for (Object eventHandler : eventHandlers) {
			if (eventHandler instanceof OnExit) {
				onExit = (OnExit) eventHandler;
			}
		}
		code.println("@Override");
		code.println("public boolean onExit() {");
		if (onExit != null) {
			code.println("int rand;");
			code.println("boolean chosen;");
			code.println("boolean matching;");
			code.println("Event sendEvent;");
			printExecution(onExit.getAction());
		}
		code.println("return super.onExit();");
		code.println("}");
	}

	private void printTriggers(List<?> triggers, boolean implicitHandlers) throws FlowCompilerException {
		code.println("@Override");
		code.println("public boolean onFlowEvent(Event event) {");
		/*
		code.println("int rand;");
		code.println("boolean chosen;");
		code.println("boolean matching;");
		code.println("Event sendEvent;");
		 */
		for (Object trigger : triggers) {
			if (trigger instanceof OnEntry) {
				OnEntry onEntry = (OnEntry) trigger;
				//code.println("if (event instanceof EntryEvent) {System.out.println(((EntryEvent)event).getStateClass() + \" \" + this.getClass());System.out.println(caller);}");
				code.println("if (event instanceof EntryEvent) {");
				//TODO: should really check if it is the right state (or a descendant) and not just the same class 
				//code.println("if (((EntryEvent)event).getStateClass().isAssignableFrom(this.getClass())) {");
				printExecution(onEntry.getAction());
				//code.println("} else if (callerHandlers(event)) {");
				//code.println("return true;");
				//code.println("}");
				code.println("}");
			} else if (trigger instanceof OnEvent) {
				OnEvent onEventElem = (OnEvent) trigger;
				if (onEventElem.getName() != null)
					code.println("if (event.triggers(\"" + onEventElem.getName() + "\")) {");
				if (onEventElem.getCond() != null)
					code.println("if ("+ formatCondExpr(onEventElem.getCond()) + ") {");
				printExecution(onEventElem.getAction());
				for (int i = 0; i < onEventElem.getOtherAttributes().keySet().size(); i++) 
					code.println("}");
				if (onEventElem.getCond() != null)
					code.println("}");
				if (onEventElem.getName() != null)
					code.println("}");
			} else if (trigger instanceof OnExit) {
			} else if (trigger instanceof Element) {
				// Catch Template
				Element elem = (Element)trigger;
				String catchParams = varname("catchParams");
				code.println("Record " + catchParams + " = new Record();");
				printSetTemplParameters(catchParams, elem.getAttributes(), null);
				code.println("if (" + getTemplateClass(elem) + "." + elem.getLocalName() + ".test(event, " + catchParams + ")) {");
				printExecution(flowReader.unmarshal(elem.getChildNodes()));
				code.println("}");
			}
		}
		if (implicitHandlers) {
			code.println("if (super.onFlowEvent(event)) return true;");
			code.println("if (callerHandlers(event)) return true;");
		}
		code.println("return false;");
		code.println("}");
	}

	private String ucFirst(String text) {
		return text.substring(0, 1).toUpperCase() + text.substring(1); 
	}

	private String listToString(List<?> list) {
		StringBuilder result = new StringBuilder();
		for (int i = 0; i < list.size(); i++) {
			if (i > 0) result.append(", ");
			result.append(list.get(i));
		}
		return result.toString();
	}

	private List<String> processString(List<?> children) throws FlowCompilerException {
		List<String> result = new ArrayList<String>();
		for (Object child : children) {
			if (child instanceof String) {
				String str = child.toString().trim();
				if (str.length() > 0)
					result.add("\"" + str + "\"");
			} else if (child instanceof Expr) {
				Expr expr = (Expr) child;
				result.add(formatExpr(expr.getValue()));
			} else if (child instanceof Item) {
				Item item = (Item) child;
				if (item.getRef() != null) 
					item = (Item) item.getRef();
				if (item.getProb() != null || item.getCond() != null)
					result.add("item(makeBool(" + item.getCond() + "), " + item.getProb() + ", " + concatFun(processString(item.getContent())) + ")");
				else
					result.addAll(processString(item.getContent()));
			} else if (child instanceof Choice) {
				Choice choice = (Choice) child;
				List<String> choices = new ArrayList<String>();
				for (Item item : choice.getItem()) {
					if (item.getRef() != null) 
						item = (Item) item.getRef();
					if (item.getCond() != null)
						choices.add("item(makeBool(" + item.getCond() + "), null, " + concatFun(processString(item.getContent())) + ")");
					else
						choices.add(concatFun(processString(item.getContent())));	
				}
				result.add("choice(" + child.hashCode() + ", " + listToString(choices)  + ")");
			} else if (child instanceof Element) {
				result.add(concatFun(processElement(child)));
			} else if (child instanceof Text) {
				String str = ((Text)child).getNodeValue();
				if (str.length() > 0)
					result.add("\"" + str + "\"");
			}
		}
		return result;
	}

	private String concatFun(List<?> nodes) {
		if (nodes.size() == 0)
			return "\"\"";
		else if (nodes.size() == 1)
			return nodes.get(0).toString();
		else
			return "concat(" + listToString(nodes) + ")";
	}

	private List<String> processElement(Object node) throws FlowCompilerException {
		ArrayList<String> result = new ArrayList<String>();
		if (node instanceof String) {
			String str = ((String)node).trim();
			if (str.length() > 0)
				result.add("\"" + str + "\"");
		} else if (node instanceof Text) {
			String str = ((Text) node).getTextContent().trim();
			if (str.length() > 0)
				result.add("\"" + str + "\"");
		} else if (node instanceof Element) {
			try {
				Object o = flowReader.unmarshal((Element)node);
				List<Object> objects = new ArrayList<Object>();
				objects.add(o);
				result.addAll(processString(objects));
			} catch (FlowCompilerException e) {
				String estring = "";
				Element en = (Element)node;
				estring += "\"<" + en.getLocalName();
				for (int j = 0; j < en.getAttributes().getLength(); j++) {
					Attr attr = (Attr) en.getAttributes().item(j);
					if (!(attr.getNamespaceURI() != null && attr.getNamespaceURI().equals("http://www.w3.org/2000/xmlns/")) && !attr.getLocalName().equals("xmlns") && !attr.getLocalName().equals("xsi")) {
						estring += " " + attr.getLocalName() + "=\\\"" + attr.getValue() + "\\\"";
					}
				}
				if (en.getChildNodes().getLength() == 0) {
					estring += "/>\"";
					result.add(estring);
				} else {
					estring += ">\"";
					result.add(estring);
					for (int i = 0; i < en.getChildNodes().getLength(); i++) {
						result.addAll(processElement(en.getChildNodes().item(i))); 
					}
					result.add("\"</" + en.getLocalName() + ">\"");
				}
			}
		}
		return result;
	}

	private String newState(String state) {
		if (state.contains(":")) {
			return state.substring(0, state.indexOf(":")) + ".new "+ state.substring(state.indexOf(":") + 1) + "()";
		} else {
			return "new " + state + "()";
		}
	}
	
	private int varnameCount = 0;
	private String varname(String prefix) {
		return prefix + (varnameCount++);
	}

	protected void printAction(Object action) throws FlowCompilerException {
		if (action instanceof JAXBElement<?>) 
			action = ((JAXBElement<?>)action).getValue();
		if (action instanceof iristk.xml.flow.Goto) {
			iristk.xml.flow.Goto gotoAction = (iristk.xml.flow.Goto) action;
			if (!stateExists(gotoAction.getState()))
				throw new FlowCompilerException("State " + gotoAction.getState() + " does not exist", gotoAction.sourceLocation().getLineNumber());
			String gotoParams = varname("gotoParams");
			code.println("Record " + gotoParams + " = new Record();");
			printSetParameters(gotoParams, gotoAction.getParams(), gotoAction.getOtherAttributes(), gotoAction.getContent());
			code.println("flowRunner.gotoState(" + newState(gotoAction.getState()) + ", " + gotoParams + ", this);");
			code.println("break EXECUTION;");
		} else if (action instanceof iristk.xml.flow.Call) {
			iristk.xml.flow.Call callAction = (iristk.xml.flow.Call) action;
			String callParams = varname("callParams"); 
			code.println("Record " + callParams + " = new Record();");
			printSetParameters(callParams, callAction.getParams(), callAction.getOtherAttributes(), callAction.getContent());
			code.println("if (!flowRunner.callState(" + newState(callAction.getState()) + ", " + callParams + ")) break EXECUTION;");
			/*
			String returnEvent = varname("returnEvent");
			code.println("Event " + returnEvent + " = flowRunner.callState(" + newState(callAction.getState()) + ", " + callParams + ");");
			code.println("if (" + returnEvent + " == null) break EXECUTION;");
			code.println("if (!" + returnEvent + ".getName().equals(\"null\")) {");
			code.println("flowRunner.processFlowEvent(" + returnEvent + ");");
			code.println("if (flowRunner.abortExecution()) break EXECUTION;");
			code.println("}");
			*/
		} else if (action instanceof iristk.xml.flow.Return) {
			iristk.xml.flow.Return returnAction = (iristk.xml.flow.Return)action;
			if (returnAction.getEvent() != null || returnAction.getCopy() != null) {
				String returnEvent = varname("returnEvent");
				printInitEvent(returnEvent, returnAction.getCopy(), returnAction.getEvent());
				printSetParameters(returnEvent, returnAction.getCopy(), returnAction.getOtherAttributes(), returnAction.getContent());
				code.println("flowRunner.raiseEvent(" + returnEvent + ");");
			}
			code.println("returnFromState(this);");
			code.println("break EXECUTION;");
		} else if (action instanceof iristk.xml.flow.Reentry) {
			code.println("flowRunner.raiseEvent(new EntryEvent());");
			code.println("returnToState(this);");
		} else if (action instanceof iristk.xml.flow.Block) {
			iristk.xml.flow.Block blockAction = (iristk.xml.flow.Block) action;
			code.println("{");
			printActions(blockAction.getAction());
			code.println("}");
		} else if (action instanceof iristk.xml.flow.Wait) {
			iristk.xml.flow.Wait waitAction = (iristk.xml.flow.Wait) action;
			code.println("actionWait(" + waitAction.getMsec() + ");");
		} else if (action instanceof iristk.xml.flow.Random) {
			iristk.xml.flow.Random randomAction = (iristk.xml.flow.Random) action;
			int tot = 0;
			for (Object child : randomAction.getAction()) {
				int inc = 1;
				if (child instanceof JAXBElement)
					child = ((JAXBElement)child).getValue();
				if (child instanceof Block && ((Block)child).getWeight() != null)
					inc = ((Block)child).getWeight();
				tot += inc;
			}
			int n = 0;
			int lastn = 0;
			String chosenVar = varname("chosen");
			String matchingVar = varname("matching");
			code.println("boolean " + chosenVar + " = false;");
			code.println("boolean " + matchingVar + " = true;");
			code.println("while (!"+chosenVar+" && "+matchingVar+") {");
			String randVar = varname("rand");
			String model = "iristk.util.RandomList.RandomModel." + randomAction.getModel().toUpperCase();
			code.println("int " + randVar + " = random(" + randomAction.hashCode() + ", " + tot + ", " + model + ");");
			code.println(matchingVar + " = false;");
			for (Object child : randomAction.getAction()) {
				if (n > 0) 
					code.println("}");
				int inc = 1;
				String cond = "true";
				if (child instanceof JAXBElement)
					child = ((JAXBElement)child).getValue();
				if (child instanceof Block) {
					Block block = (Block) child;
					if (block.getWeight() != null)
						inc = block.getWeight();
					if (block.getCond() != null) 
						cond = formatCondExpr(block.getCond());
				}
				n += inc;
				code.println("if (" + cond + ") {");
				code.println(matchingVar + " = true;");
				code.println("if (" + randVar + " >= " + lastn + " && " + randVar + " < " + n + ") {");
				code.println(chosenVar + " = true;");
				printAction(child);
				code.println("}");
				lastn = n;
			}
			code.println("}");
			code.println("}");
		} else if (action instanceof iristk.xml.flow.Raise) {
			iristk.xml.flow.Raise raiseAction = (iristk.xml.flow.Raise) action;
			String raiseEvent = varname("raiseEvent");
			printInitEvent(raiseEvent, raiseAction.getCopy(), raiseAction.getEvent());
			printSetParameters(raiseEvent, raiseAction.getCopy(), raiseAction.getOtherAttributes(), raiseAction.getContent());
			if (raiseAction.getDelay() != null) 
				code.println("flowRunner.raiseEvent(" + raiseEvent + ", " + formatAttrExpr(raiseAction.getDelay()) + ");");
			else
				code.println("flowRunner.raiseEvent(" + raiseEvent + ");");
		} else if (action instanceof iristk.xml.flow.Send) {
			iristk.xml.flow.Send sendAction = (iristk.xml.flow.Send) action;
			String sendEvent = varname("sendEvent");
			printInitEvent(sendEvent, sendAction.getCopy(), sendAction.getEvent());
			printSetParameters(sendEvent, sendAction.getCopy(), sendAction.getOtherAttributes(), sendAction.getContent());
			if (sendAction.getDelay() != null) 
				code.println("flowRunner.sendEvent(" + sendEvent + ", " + formatAttrExpr(sendAction.getDelay()) + ");");
			else
				code.println("flowRunner.sendEvent(" + sendEvent + ");");
			if (sendAction.getBindId() != null) {
				code.println(sendAction.getBindId() + " = " + sendEvent + ".getId();");
			}
		} else if (action instanceof iristk.xml.flow.If) {
			iristk.xml.flow.If ifAction = (iristk.xml.flow.If) action;
			code.println("if (" + formatCondExpr(ifAction.getCond()) + ") {");
			printActions(ifAction.getAction());
			code.println("}");
		} else if (action instanceof iristk.xml.flow.Else) {
			code.println("} else {");
		} else if (action instanceof iristk.xml.flow.Elseif) {
			iristk.xml.flow.Elseif eifAction = (iristk.xml.flow.Elseif) action;
			code.println("} else if (" + formatCondExpr(eifAction.getCond()) + ") {");
		} else if (action instanceof iristk.xml.flow.Propagate) {
			code.println("propagate = true;");
			code.println("break EXECUTION;");
		} else if (action instanceof Var) {
			code.println(variable((Var) action));
		} else if (action instanceof iristk.xml.flow.Exec) {
			code.println(formatExec(((iristk.xml.flow.Exec)action).getValue().trim()));
		} else if (action instanceof iristk.xml.flow.Log) {
			code.println("log(" + concatFun(processString(((iristk.xml.flow.Log)action).getContent())).replaceAll("\n", "").replaceAll("\\s+", " ") + ");");
		} else if (action instanceof Element) {
			// Action Template
			Element elem = (Element)action;
			String actionParams = varname("actionParams");
			code.println("Record " + actionParams + " = new Record();");
			printSetTemplParameters(actionParams, elem.getAttributes(), elem.getChildNodes());
			code.println("if (!" + getTemplateClass(elem) + "." + elem.getLocalName() + ".execute(flowRunner, " + actionParams + ")) break EXECUTION;");
		}
	}
	
	private void printInitEvent(String varName, String copy, String eventName) throws FlowCompilerException {
		if (eventName != null)
			code.println("Event " + varName + " = new Event(\"" + eventName + "\");");
		else if (copy != null)
			code.println("Event " + varName + " = new Event(" + copy + ".getName());");
		else throw new FlowCompilerException("Must have either copy or event parameter set when sending event");
	}

	private String getTemplateClass(Element elem) {
		if (elem.getNodeName().contains(":")) {
			String prefix = elem.getNodeName().split(":")[0];
			for (Var var : flowReader.getVariables()) {
				if (var.getName().equals(prefix)) {
					return var.getName();
				}
				/*
				String type = var.getType();
				for (String imp : flowReader.getImportedClasses()) {
					if (imp.endsWith(type) && ) {
						type = imp;
						break;
					}
				}
				if (elem.equals(type)) {
					return var.getName();
				} 
				*/
			}
		}
		return elem.getNamespaceURI();
	}

	private void printSetTemplParameters(String paramName, NamedNodeMap attributes, NodeList childNodes) throws FlowCompilerException {
		for (int i = 0; i < attributes.getLength(); i++) {
			Node attr = attributes.item(i);
			if (attr.getNamespaceURI() != null && attr.getNamespaceURI().equals("http://www.w3.org/2000/xmlns/")) continue;
			String value = attr.getNodeValue();
			String name = attr.getLocalName();
			value = formatAttrExpr(value);
			code.println(paramName + ".put(\"" + name + "\", " + value + ");");
		}
		if (childNodes != null && childNodes.getLength() > 0) {
			List<Object> textChildren = new ArrayList<Object>();
			HashMap<String,List<String>> paramList = new HashMap<String,List<String>>();
			for (int i = 0; i < childNodes.getLength(); i++) {
				Node child = childNodes.item(i);
				if (child instanceof Element) {
					Element elem = (Element) child;
					if (elem.getNamespaceURI().equals("iristk.flow.param")) {
						String key = elem.getLocalName();
						if (!paramList.containsKey(key))
							paramList.put(key, new ArrayList<String>());
						List<Object> paramChildren = new ArrayList<Object>();
						for (int j = 0; j < elem.getChildNodes().getLength(); j++) {
							paramChildren.add(elem.getChildNodes().item(j));
						}
						String text = concatFun(processString(paramChildren)).replaceAll("\n", "").replaceAll("\\s+", " ");
						paramList.get(key).add(text);
					} else {
						textChildren.add(child);
					}
				} else if (child instanceof Text && ((Text)child).getNodeValue().trim().length() != 0) {
					textChildren.add(child);
				}
			}
			if (textChildren.size() > 0) {
				if (!paramList.containsKey("text"))
					paramList.put("text", new ArrayList<String>());
				paramList.get("text").add(concatFun(processString(textChildren)).replaceAll("\n", "").replaceAll("\\s+", " "));
			}
			for (String key : paramList.keySet()) {
				if (paramList.get(key).size() > 1) {
					code.println(paramName + ".put(\"" + key + "\", java.util.Arrays.asList(" + listToString(paramList.get(key)) + "));");
				} else {
					code.println(paramName + ".put(\"" + key + "\", " + paramList.get(key).get(0) + ");");
				}
			}
		}
	}

	private void printSetParameters(String paramName, String params, Map<QName, String> otherAttributes, List<Object> content) throws FlowCompilerException {
		if (params != null)
			code.println(paramName + ".putAll(" + params + ");");
		if (content.size() > 0) {
			String text = concatFun(processString(content)).replaceAll("\n", "").replaceAll("\\s+", " ");
			code.println(paramName + ".put(\"text\", " + text + ");");
		}
		for (QName qname : otherAttributes.keySet()) {
			if (qname.getNamespaceURI().equals("iristk.flow.param")) {
				String value = otherAttributes.get(qname);
				String name = qname.getLocalPart();
				code.println(paramName + ".put(\"" + name + "\", " + formatAttrExpr(value) + ");");
			}
		}
	}

	private void printExecution(List<Object> list) throws FlowCompilerException {
		code.println("boolean propagate = false;");
		code.println("EXECUTION: {");
		printActions(list);
		code.println("}");
		code.println("if (!propagate) return true;");
	}

	private void printActions(List<Object> list) throws FlowCompilerException {
		for (Object action : list) {
			printAction(action);
		}
	}

	private static String replaceIgnoreStrings(String expr, String pattern, final String repl) {
		return new Replacer(pattern, stringPattern) {	
			@Override
			protected String replace(Matcher matcher) {
				return repl;
			}
		}.replaceAll(expr);
	}

	private String formatCondExpr(String cond) throws FlowCompilerException {
		if (cond == null)
			return null;
		cond = formatAttrExpr(cond);
		cond = replaceIgnoreStrings(cond, " and ", " && ");
		cond = replaceIgnoreStrings(cond, " or ", " || ");
		return "makeBool(" + cond + ")";
	}

	private String formatAttrExpr(String expr) throws FlowCompilerException {
		if (expr == null)
			return null;
		// ' => " (if not preceded by \)
		expr = expr.replaceAll("(?<!\\\\)'", "\"");
		// \' => ' 
		expr = expr.replaceAll("\\\\'", "'");
		// \ => \\ (if not followed by ") 
		expr = expr.replaceAll("\\\\(?!\")", "\\\\\\\\");
		expr = formatExpr(expr);
		return expr;
	}

	private String formatExec(String exec) throws FlowCompilerException {
		exec = formatExpr(exec);
		if (!exec.endsWith(";"))
			return exec + ";";
		else
			return exec;
	}
	
	public static String formatRecordExpr(String expr) throws FlowCompilerException {
		try {
			return new Replacer("(\\?)?:([A-Za-z0-9_\\:\\.\\(\\)\\*]*)( *=(?!=)[^;]*)?", stringPattern) {
				@Override
				protected String replace(Matcher matcher) {
					String getStr = matcher.group(2);
					String[] split = Replacer.paraSplit(getStr);
					getStr = split[0];
					String rest = split[1];
					String call = null;
					if (getStr.endsWith("(") && getStr.contains(":")) {
						int i = getStr.lastIndexOf(":");
						call = getStr.substring(i + 1, getStr.length() - 1);
						getStr = getStr.substring(0, i);
					} else if (getStr.endsWith("(")) {
						throw new RuntimeException("Cannot parse: " + matcher.group());
					}
					int para = 0;
					String getExpr = "";
					String getPart = "";
					getStr += ":";
					for (int i = 0; i < getStr.length(); i++) {
						String c = getStr.substring(i, i + 1); 
						if (c.equals("(")) {
							getPart += c;
							para++;
						} else if (c.equals(")")) {
							getPart += c;
							para--;
						} else if (c.equals(":") && para == 0) {
							if (getPart.startsWith("("))
								getPart = getPart.substring(1, getPart.length() - 1);
							else 
								getPart = "\"" + getPart + "\"";
							if (getExpr.length() == 0)
								getExpr = getPart;
							else
								getExpr += " + \":\" + " + getPart;
							getPart = "";
						} else {
							getPart += c;
						}
					}
					getExpr = "\"\" + " + getExpr;
					getExpr = getExpr.replaceAll("\" \\+ \"", "");
					if (matcher.group(3) != null && rest.length() == 0) {
						String put = matcher.group(3).trim().substring(1).trim();
						return ".put(" + getExpr + ", " + put + ")";
					} else if (call != null) {
						return ".call(" + getExpr + ", \"" + call + "\", " + rest;
					} else if (matcher.group(1) != null) {
						return ".has(" + getExpr + ")" + rest;
					} else {
						return ".get(" + getExpr + ")" + rest;
					}
				} 
			}.replaceIter(expr);
		} catch (RuntimeException e) {
			throw new FlowCompilerException(e.getMessage());
		}
	}

	/*
	public static String formatRecordExpr(String expr) throws FlowCompilerException {
		try {
		
			int pos = expr.indexOf(":");
			while (pos > -1 && pos < expr.length() - 1) {
				if (!expr.substring(pos + 1, pos + 2).equals(" ")) {
					for (int i = pos + 1; i < expr.length(); i++) {
						String c = expr.substring(i, i+1);
						if (c.equals("(")) {
							
						} else if (c.equals(")")) {
							
						} else if (c.matches("[A-Za-z0-9_]+")) {
						
						} else if () {
							
						}
					}
				}
				pos = expr.indexOf(":", pos);
			}
		
			return new Replacer("([A-Za-z0-9_]+)\\:([A-Za-z0-9_\\:\\.\\(\\)]*)( *=(?!=)[^;]*)?", stringPattern) {
				@Override
				protected String replace(Matcher matcher) {
					String recordName = matcher.group(1);
					String getStr = matcher.group(2);
					String[] split = Replacer.paraSplit(getStr);
					getStr = split[0];
					String rest = split[1];
					String call = null;
					if (getStr.endsWith("(") && getStr.contains(":")) {
						int i = getStr.lastIndexOf(":");
						call = getStr.substring(i + 1, getStr.length() - 1);
						getStr = getStr.substring(0, i);
					} else if (getStr.endsWith("(")) {
						throw new RuntimeException("Cannot parse: " + matcher.group());
					}
					//if (!(getStr.contains(".(") || getStr.startsWith("(")) && getStr.contains("(")) {
					//	return recordName + "." + formatRecordExpr(getStr + rest);
					//} 
					int para = 0;
					String getExpr = "";
					String getPart = "";
					getStr += ":";
					for (int i = 0; i < getStr.length(); i++) {
						String c = getStr.substring(i, i + 1); 
						if (c.equals("(")) {
							getPart += c;
							para++;
						} else if (c.equals(")")) {
							getPart += c;
							para--;
						} else if (c.equals(":") && para == 0) {
							if (getPart.startsWith("("))
								getPart = getPart.substring(1, getPart.length() - 1);
							else 
								getPart = "\"" + getPart + "\"";
							if (getExpr.length() == 0)
								getExpr = getPart;
							else
								getExpr += " + \":\" + " + getPart;
							getPart = "";
						} else {
							getPart += c;
						}
					}
					getExpr = "\"\" + " + getExpr;
					getExpr = getExpr.replaceAll("\" \\+ \"", "");
					if (matcher.group(3) != null && rest.length() == 0) {
						String put = matcher.group(3).trim().substring(1).trim();
						return recordName + ".put(" + getExpr + ", " + put + ")";
					} else if (call != null) {
						return recordName + ".call(" + getExpr + ", \"" + call + "\", " + rest;
					} else {
						return recordName + ".get(" + getExpr + ")" + rest;
					}
				} 
			}.replaceIter(expr);
		} catch (RuntimeException e) {
			throw new FlowCompilerException(e.getMessage());
		}
	}
	*/

	public static String formatExpr(String expr) throws FlowCompilerException {
		expr = formatRecordExpr(expr);
		expr = new Replacer(" *:=([^;]*)", stringPattern) {
			@Override
			protected String replace(Matcher matcher) {
				return ".putAll(" + matcher.group(1).trim() + ")";
			} 
		}.replaceIter(expr);
		expr = formatEqExpr(expr);
		expr = expr.replaceAll("\\bbool\\(", "makeBool(");
		expr = expr.replaceAll("\\brecord\\(", "makeRecord(");
		expr = expr.replaceAll("\\bfloat\\(", "makeFloat(");
		expr = expr.replaceAll("\\bint\\(", "makeInt(");
		expr = expr.replaceAll("\\bstring\\(", "makeString(");
		return expr;
	}

	private static int findExpr(String expr, int pos, int dir) {
		int para = 0;
		boolean inQuote = false;
		boolean hasChar = false;
		int i = pos;
		for (; i > 0 && i < expr.length(); i += dir) {
			String c = expr.substring(i, i+1);
			if (c.equals("(")) {
				para += dir;
			} else if (c.equals(")")) {
				para -= dir;
			} else if (c.equals("\"")) {
				inQuote = !inQuote;
			} else if (c.equals(" ") && para <= 0 && !inQuote && hasChar) {
				return i;
			} 
			if (para < 0) {
				return i - dir;
			}
			if (!c.equals(" ")) hasChar = true;
		}
		return i;
	}

	public static String formatEqExpr(String expr) {
		return expr;
	}

	public static void compile(String flowFileName, String dir, boolean binary) throws FlowCompilerException {
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
			FlowCompiler fcompiler = new FlowCompiler(flowFile);
			File srcFile = new File(flowFile.getAbsolutePath().replaceFirst("\\.[A-Za-z]+$", "\\.java"));
			fcompiler.compileToFile(srcFile);
			System.out.println("Compiled to source code: " + srcFile.getAbsolutePath());
			if (binary) {
				JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
				if (compiler == null) {
					throw new RuntimeException("Could not find Java Compiler");
				}
				String binDir = flowFile.getAbsolutePath().replaceFirst("([\\\\/])src[\\\\/].*", "$1") + "bin";
				if (new File(binDir).exists()) {
					if (compiler.run(null, null, null, srcFile.getPath(), "-d", binDir) == 0) {
						System.out.println("Compiled to byte code folder: " + binDir);
					}
				} else {
					throw new RuntimeException("Directory " + binDir + " does not exist");
				}
			}
		}
	}

	public static void main(String[] args)  {
		try {
			boolean compileToBinary = false;
			boolean compileToXSD = false;
			String file = null;
			for (String arg : args) {
				if (arg.equals("-b")) 
					compileToBinary = true;
				else if (arg.equals("-x")) 
					compileToXSD = true;
				else
					file = arg;
			}
			if (file != null) {
				compile(file, System.getProperty("user.dir"), compileToBinary);
				if (compileToXSD)
					TemplateSchemaCompiler.compile(file, System.getProperty("user.dir"));
			} else {
				System.out.println("Compiles flow XML to Java source.\n");
				System.out.println("Usage:");
				System.out.println("iristk cflow [OPTIONS] XML\n");
				System.out.println("Options:");
				System.out.println("-b  Also compile Java source to binary");
				System.out.println("-x  Also compile templates to XSD Schema");
			}
		} catch (FlowCompilerException e) {
			System.err.println("Error on line " + e.getLineNumber() + ": " + e.getMessage());
		}
	}

	public void useUniqueNames(boolean b) {
		this.useUniqueNames   = b;
	}

	public String getFlowName() {
		return flowReader.getPackage() + "." + getLocalFlowName();
	}

	public String getLocalFlowName() {
		if (useUniqueNames)
			return flowReader.getName() + uniqueNameSuffix;
		else
			return flowReader.getName();
	}

}
