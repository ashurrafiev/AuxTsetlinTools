package ncl.tsetlin.tools.genlogger;

import static ncl.tsetlin.tools.genlogger.XmlReader.*;

import java.io.PrintStream;
import java.util.ArrayList;

import org.w3c.dom.Element;

public class GenLogger {

	public static final StringLibrary str = StringLibrary.load("ncl/tsetlin/tools/genlogger/GenLogger.str");
	
	public abstract static class Probe {
		public String id;
		public boolean cls;
		public String type;
		
		public String getLogFormat() {
			switch(type) {
				case "int": return "%d";
				case "float": return "%.3f";
				default:
					throw new RuntimeException("Unknown log type: "+type);
			}
		}
		
		public void printCache(PrintStream out) {
		}
		
		public void printResetCache(PrintStream out) {
		}
		
		public void printHeader(PrintStream out) {
			out.printf(str.get(cls ? "probe.header.cls" : "probe.header"), id);
		}
		
		public void printLog(PrintStream out) {
			if(cls) out.print("\t");
			out.printf(str.get("probe.log"), getLogFormat(), getCalcCode());
		}
		
		public abstract String getCalcCode();
	}
	
	public static class Var extends Probe {
		public String cache = null;
		public String calc = null;
		@Override
		public void printCache(PrintStream out) {
			if(cache!=null)
				out.printf(str.get(cls ? "var.cache.cls" : "var.cache"), type, cache);
		}
		@Override
		public void printResetCache(PrintStream out) {
			if(cache!=null)
				out.printf(str.get(cls ? "status.resetCache.code.cls" : "status.resetCache.code"), getCalcCode());
		}
		@Override
		public String getCalcCode() {
			if(cache!=null)
				return String.format(str.get(cls ? "var.code.cache.cls" : "var.code.cache"), cache);
			else if(calc!=null)
				return calc;
			else
				throw new RuntimeException("var: cache or calculation code required.");
		}
	}
	
	public static class Event extends Probe {
		public String counter;
		public String reset;
		public String avg;
		@Override
		public String getCalcCode() {
			if(avg==null)
				return String.format(str.get("event.code"), counter);
			else
				return String.format(str.get("event.code.avg"), counter, avg);
		}
		@Override
		public void printLog(PrintStream out) {
			super.printLog(out);
			out.printf(str.get("event.log"), counter, reset);
		}
	}
	
	public ArrayList<String> includes = new ArrayList<>();
	public boolean joinClasses = true;
	
	public String statesPath, statesEnable;
	public String statusPath, statusEnable;
	public String minState, maxState, stateCode;
	
	public ArrayList<Probe> probes = new ArrayList<>();
	
	// TODO tm types, get tm from mctm, tm member access
	
	private boolean hasClassProbes() {
		for(Probe pr : probes) {
			if(pr.cls) return true;
		}
		return false;
	}
	
	public void generate(PrintStream out) {
		out.print(str.get("main.start"));
		for(String file : includes)
			out.printf(str.get("main.include"), file);
		out.printf(str.get("main.defs"), statesPath, statusPath, minState, maxState);
		
		// states
		out.print(str.get("states.struct"));

		out.printf(str.get("startLog.start"), "startLogTAStates", "LogTAStates", "TASTATES_PATH", statesEnable);
		out.print(str.get("states.startLog.body"));
		out.print(str.get("startLog.finish"));
		
		out.printf(str.get("states.log"), stateCode);
		
		out.printf(str.get("finishLog"), "finishLogTAStates", "LogTAStates", statesEnable);
		
		// status
		out.print(str.get("status.struct.start"));
		for(Probe pr : probes)
			pr.printCache(out);
		out.print(str.get("status.struct.finish"));
		
		boolean clsProbes = hasClassProbes();
		
		out.printf(str.get("startLog.start"), "startLogStatus", "LogStatus", "STATUS_PATH", statusEnable);
		for(Probe pr : probes) {
			if(!pr.cls)
				pr.printHeader(out);
		}
		if(clsProbes) {
			out.print(str.get("status.startLog.classes.0"));
			for(Probe pr : probes) {
				if(pr.cls)
					pr.printHeader(out);
			}
			out.print(str.get("status.startLog.classes.1"));
		}
		out.print(str.get("startLog.finish.0"));
		for(Probe pr : probes) {
			if(!pr.cls)
				pr.printResetCache(out);
		}
		if(clsProbes) {
			out.print(str.get("status.resetCache.classes.0"));
			for(Probe pr : probes) {
				if(pr.cls)
					pr.printResetCache(out);
			}
			out.print(str.get("status.resetCache.classes.1"));
		}
		out.print(str.get("startLog.finish.1"));
		
		// log
		out.print(str.get("status.log.start"));
		for(Probe pr : probes) {
			if(!pr.cls)
				pr.printLog(out);
		}
		if(clsProbes) {
			out.print(str.get("status.log.classes.0"));
			for(Probe pr : probes) {
				if(pr.cls)
					pr.printLog(out);
			}
			out.print(str.get("status.log.classes.1"));
		}
		out.print(str.get("status.log.finish"));
		
		out.printf(str.get("finishLog"), "finishLogStatus", "LogStatus", statusEnable);
		
		out.println("#endif");
	}
	
	public void write(String path) {
		try {
			generate(new PrintStream(path));
		}
		catch (Exception e) {
			System.err.println("Cannot write logger code "+path);
			e.printStackTrace();
		}
	}
	
	public static GenLogger loadConfig(String path) {
		try {
			Element root = load(path);
			if(root==null)
				throw new RuntimeException("Cannot read file.");
			GenLogger gen = new GenLogger();
			
			for(Element e : elements(root, "include")) {
				String file = attr(e, "file", null);
				if(file==null)
					throw new RuntimeException("include: file attribute required.");
				gen.includes.add(file);
			}
			
			Element states = element(root, "states");
			gen.statesPath = attr(states, "path", "tm-spectrum.csv");
			gen.statesEnable = attr(states, "enable", "LOG_TASTATES");
			gen.minState = attr(states, "min", "0");
			gen.maxState = attr(states, "max", "NUM_STATES-1");
			gen.stateCode = states.getTextContent().trim();
			
			Element status = element(root, "status");
			gen.joinClasses = attrBool(status, "joinClasses", true);
			gen.statusPath = attr(status, "path", "tm-status.csv");
			gen.statusEnable = attr(status, "enable", "LOG_STATUS");
			
			for(Element grp : elements(status, "tm", "class")) {
				boolean cls = grp.getTagName().equals("class");
				for(Element p : elements(grp, "var", "event")) {
					Probe pr;
					boolean var;
					if(p.getTagName().equals("var")) {
						pr = new Var();
						pr.type = "float";
						var = true;
					}
					else {
						pr = new Event();
						pr.type = "int";
						var = false;
					}
					pr.id = attr(p, "id", null);
					if(pr.id==null)
						throw new RuntimeException("Probe id required.");
					pr.cls = cls;
					if(var) {
						Var v = (Var) pr;
						v.cache = attr(p, "cache", null);
						if(v.cache==null)
							v.calc = p.getTextContent().trim();
					}
					else {
						Event e = (Event) pr;
						e.counter = attr(p, "counter", pr.id);
						e.reset = attr(p, "reset", "0");
						e.avg = attr(p, "avg", null);
						if(e.avg!=null)
							pr.type = "float";
					}
					pr.type = attr(p, "type", pr.type);
					gen.probes.add(pr);
				}
			}

			return gen;
		}
		catch(Exception e) {
			System.err.println("Cannot load logger config "+path);
			e.printStackTrace();
			System.exit(1);
			return null;
		}
	}
	
	public static void main(String[] args) {
		loadConfig("logger.xml")
			//.generate(System.out);
			.write("out/TsetlinLogger.h");
		System.out.println("Done");
	}

}
