package ncl.tsetlin.tools.genlogger;

import java.io.PrintStream;
import java.util.ArrayList;

import org.w3c.dom.Element;

import static ncl.tsetlin.tools.genlogger.GenLogger.str;
import static ncl.tsetlin.tools.utils.XmlReader.*;

public class StatusLog extends Log {

	public abstract class Probe {
		public String id;
		public String group;
		public String type;
		
		public String getLogFormat() {
			switch(type) {
				case "int":
					return "%d";
				case "long":
					return "%ld";
				case "float":
					return "%.3f";
				case "double":
					return "%.3lf";
				default:
					throw new RuntimeException("Unknown log type: "+type);
			}
		}
		
		public void printCache(PrintStream out) {
		}
		
		public void printResetCache(PrintStream out) {
		}
		
		public void printHeader(PrintStream out) {
			if(group!=null)
				out.printf(str.get("probe.header.group"), group, id);
			else
				out.printf(str.get("probe.header"), id);
		}
		
		public void printLog(PrintStream out) {
			if(group!=null)
				out.printf(str.get("probe.log.group"), group);
			out.printf(str.get("probe.log"), getLogFormat(), getCalcCode());
		}
		
		public abstract String getCalcCode();
	}
	
	public class Var extends Probe {
		public String cache = null;
		public String calc = null;
		@Override
		public void printCache(PrintStream out) {
			if(cache!=null) {
				if(group!=null)
					out.printf(str.get("var.cache.group"), type, cache, group);
				else
					out.printf(str.get("var.cache"), type, cache);
			}
		}
		@Override
		public void printResetCache(PrintStream out) {
			if(cache!=null) {
				if(group!=null)
					out.printf(str.get("var.cache.reset.group"), group, cache);
				else
					out.printf(str.get("var.cache.reset"), cache);
			}
		}
		@Override
		public String getCalcCode() {
			if(cache!=null)
				return str.format(group!=null ? "var.code.cache.group" : "var.code.cache", cache);
			else if(calc!=null)
				return calc;
			else
				throw new RuntimeException("var: cache or calculation code required.");
		}
	}
	
	public class Event extends Probe {
		public String counter;
		public String reset;
		public String avg;
		@Override
		public String getCalcCode() {
			if(avg==null)
				return str.format("event.code", counter);
			else
				return str.format("event.code.avg", counter, avg);
		}
		@Override
		public void printLog(PrintStream out) {
			super.printLog(out);
			out.printf(str.get("event.log"), counter, reset);
		}
	}
	
	public ArrayList<Probe> probes = new ArrayList<>();

	@Override
	protected void printStructMembers(PrintStream out) {
		for(Probe pr : probes)
			pr.printCache(out);
	}
	
	@Override
	protected void printHeaders(PrintStream out) {
		for(Probe pr : probes)
			pr.printHeader(out);
	}
	
	@Override
	protected void printCacheReset(PrintStream out) {
		for(Probe pr : probes)
			pr.printResetCache(out);
	}
	
	@Override
	protected void printLogBody(PrintStream out) {
		for(Probe pr : probes)
			pr.printLog(out);
	}
	
	@Override
	public void loadLog(Element log) {
		super.loadLog(log);
		// TODO load access
		for(Element p : elements(log, "var", "event")) {
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
			if(var) {
				Var v = (Var) pr;
				v.cache = attr(p, "cache", null);
				if(v.cache==null)
					v.calc = trimTextContent(p);
				pr.group = attr(p, "group", null);
			}
			else {
				Event e = (Event) pr;
				e.counter = attr(p, "counter", pr.id);
				e.reset = attr(p, "reset", "0");
				e.avg = attr(p, "avg", null);
				if(e.avg!=null)
					pr.type = "double";
			}
			pr.type = attr(p, "type", pr.type);
			probes.add(pr);
		}
	}
	
	public static String getCounterDefs(ArrayList<Event> counters) {
		StringBuilder sb = new StringBuilder();
		for(Event c : counters)
			sb.append(str.format("counter.fmt", c.counter));
		return sb.toString();
	}

	public static String getCounterResets(ArrayList<Event> counters) {
		StringBuilder sb = new StringBuilder();
		for(Event c : counters)
			sb.append(str.format("counter.reset.fmt", c.counter, c.reset));
		return sb.toString();
	}

	public static ArrayList<Event> collectCounters(ArrayList<Log> logs) {
		ArrayList<Event> counters = new ArrayList<>();
		for(Log log : logs) {
			if(log instanceof StatusLog) {
				StatusLog status = (StatusLog) log;
				for(Probe p : status.probes) {
					if(p instanceof Event)
						counters.add((Event) p);
				}
			}
		}
		return counters;
	}

}
