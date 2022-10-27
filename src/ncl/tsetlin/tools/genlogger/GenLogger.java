package ncl.tsetlin.tools.genlogger;

import static ncl.tsetlin.tools.utils.XmlReader.*;

import java.io.File;
import java.io.PrintStream;
import java.util.ArrayList;

import org.w3c.dom.Element;

import ncl.tsetlin.tools.utils.ParseParams;
import ncl.tsetlin.tools.utils.StringLibrary;

public class GenLogger {

	public static final StringLibrary str = StringLibrary.load("ncl/tsetlin/tools/genlogger/GenLogger.str");
	public static String defsName = "TsetlinLoggerDefs.h";
	public static String loggerName = "TsetlinLogger.h";
	
	public ArrayList<String> defIncludes = new ArrayList<>();
	public ArrayList<String> includes = new ArrayList<>();
	public ArrayList<Log> logs = new ArrayList<>();

	private void printIncludes(PrintStream out, ArrayList<String> includes) {
		for(String file : includes)
			out.printf(str.get("include"), file);
		out.println();
	}
	
	public void genDefs(PrintStream out) {
		out.print(str.get("defs.begin"));
		printIncludes(out, defIncludes);
		
		for(Log log : logs)
			out.printf(str.get("defs.path"), log.getPathConst(), log.path);
		out.println();
		
		for(Log log : logs)
			log.printDefs(out);
		out.println();

		for(Log log : logs)
			out.printf(str.get("log.enable.defs"), log.enable);
		out.printf(str.get("log.enable.defs"), "LOG_APPEND");
		out.println();

		ArrayList<StatusLog.Event> counters = StatusLog.collectCounters(logs);
		if(!counters.isEmpty()) {
			out.printf(str.get("log.counter.defs"),
					StatusLog.getCounterDefs(counters),
					StatusLog.getCounterResets(counters));
			out.println();
		}
		
		out.print(str.get("end"));
	}

	public void genLogger(PrintStream out) {
		out.print(str.get("main.begin"));
		printIncludes(out, includes);
		
		for(Log log : logs)
			log.printLocalDefs(out);
		out.println();
		
		for(Log log : logs) {
			log.printSeparator(out);
			log.printTypedefs(out);
			log.printStartLog(out);
			log.printLog(out);
			log.printFinishLog(out);
		}
		
		out.print(str.get("end"));
	}

	public void write(String path) {
		try {
			genDefs(new PrintStream(new File(path, defsName)));
			genLogger(new PrintStream(new File(path, loggerName)));
		}
		catch (Exception e) {
			System.err.println("Cannot write logger code to "+path);
			e.printStackTrace();
		}
	}
	
	private static void getIncludes(Element parent, ArrayList<String> includes) {
		if(parent==null)
			return;
		for(Element e : elements(parent, "include")) {
			String file = attr(e, "file", null);
			String std = attr(e, "std", null);
			if((file==null) ^ (std==null)) {
				if(file!=null)
					includes.add(str.format("inc.file", file));
				else
					includes.add(str.format("inc.std", std));
			}
			else
				throw new RuntimeException("include: file or std attribute required, but not both.");
		}
	}
	
	public static GenLogger loadConfig(String path) {
		try {
			Element root = load(path);
			if(root==null)
				throw new RuntimeException("Cannot read file.");
			GenLogger gen = new GenLogger();
			
			getIncludes(element(root, "defs"), gen.defIncludes);
			getIncludes(root, gen.includes);
			
			for(Element log : elements(root, Log.tags)) {
				gen.logs.add(Log.load(log));
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
	
	private static String inPath = null;
	private static String outPath = ".";
	
	public static void main(String[] args) {
		ParseParams p = new ParseParams();
		p.addStrParam(x -> inPath = x, "input");
		p.addStrParam("-o", x -> outPath = x, "output path");
		if(!p.parseParams(args))
			System.exit(1);
		if(inPath==null) {
			System.err.println("Input XML is not specified.");
			System.err.flush();
			p.printUsage();
			System.exit(1);
		}
		
		loadConfig(inPath).write(outPath);
		System.out.println("Done");
	}

}
