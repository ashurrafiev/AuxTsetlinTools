package ncl.tsetlin.tools.genlogger;

import static ncl.tsetlin.tools.genlogger.GenLogger.str;
import static ncl.tsetlin.tools.genlogger.XmlReader.*;

import java.io.PrintStream;

import org.w3c.dom.Element;

public abstract class Log {

	public static final String[] tags = {"status", "spectrum"};
	
	public static enum Scope { global, cls }

	public String name = null;
	public String path = null;
	public Scope scope = Scope.global;
	public String enable = null;
	
	public String logParams = null;
	
	public String getPathConst() {
		return String.format(str.get("log.pathconst"), name.toUpperCase());
	}
	
	public void printDefs(PrintStream out) {
	}

	public void printLocalDefs(PrintStream out) {
	}

	public void printSeparator(PrintStream out) {
		out.printf(str.get("log.sep"), name);
		out.println();
	}

	protected void printStructMembers(PrintStream out) {
	}

	protected void printStruct(PrintStream out) {
		out.printf(str.get(scope==Scope.cls ? "log.struct.begin.cls" : "log.struct.begin"), name);
		printStructMembers(out);
		out.print(str.get("log.struct.end"));
		out.println();
	}

	public void printTypedefs(PrintStream out) {
		printStruct(out);
	}
	
	protected abstract void printHeaders(PrintStream out);

	protected void printCacheReset(PrintStream out) {
	}

	public void printStartLog(PrintStream out) {
		out.printf(str.get(scope==Scope.cls ? "log.start.func.cls" : "log.start.func"), name);
		if(enable!=null)
			out.printf(str.get("log.enablecheck"), enable);
		out.printf(str.get(scope==Scope.cls ? "log.start.begin.cls" : "log.start.begin"), getPathConst());
		printHeaders(out);
		out.print(str.get("log.start.flush"));
		printCacheReset(out);
		out.print(str.get("log.start.end"));
		out.println();
	}

	protected abstract void printLogBody(PrintStream out);

	public void printLog(PrintStream out) {
		out.printf(str.get("log.log.func"), name, logParams==null ? "" : (", "+logParams));
		if(enable!=null)
			out.printf(str.get("log.enablecheck"), enable);
		out.printf(str.get("log.log.begin"));
		printLogBody(out);
		out.print(str.get("log.log.end"));
		out.println();
	}

	protected void printCleanup(PrintStream out) {
	}

	public void printFinishLog(PrintStream out) {
		out.printf(str.get("log.finish.func"), name);
		if(enable!=null)
			out.printf(str.get("log.enablecheck"), enable);
		printCleanup(out);
		out.print(str.get("log.finish"));
		out.println();
	}
	
	protected void loadLogParams(Element params) {
		if(params!=null) {
			String code = params.getTextContent().trim();
			if(!code.isEmpty())
				logParams = code;
		}
	}
	
	public void loadLog(Element log) {
		name = attr(log, "name", name);
		if(name==null)
			throw new RuntimeException("Log name required.");
		path = attr(log, "path", path);
		if(name==null)
			throw new RuntimeException("Log path required.");
		scope = toScope(attr(log, "scope", null), scope);
		enable = attr(log, "enable", enable);
		loadLogParams(element(log, "params"));
	}

	public static Scope toScope(String s, Scope def) {
		if(s==null)
			return def;
		switch(s) {
			case "global":
				return Scope.global;
			case "class":
				return Scope.cls;
			default:
				return def;
		}
	}
	
	public static Log load(Element e) {
		Log log = null;
		switch(e.getTagName()) {
			case "status":
				log = new StatusLog();
				break;
			case "spectrum":
				log = new SpectrumLog();
				break;
		}
		log.loadLog(e);
		return log;
	}
	
}
