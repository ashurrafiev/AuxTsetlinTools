package ncl.tsetlin.tools.genlogger;

import static ncl.tsetlin.tools.genlogger.GenLogger.str;
import static ncl.tsetlin.tools.utils.XmlReader.*;

import java.io.PrintStream;

import org.w3c.dom.Element;

public class SpectrumLog extends Log {

	public String minState, maxState;
	public String clauses = "CLAUSES";
	public String literals = "LITERALS";
	public String taStateCode = null;

	public SpectrumLog() {
		name = "TAStates";
		scope = Scope.cls;
	}
	
	@Override
	public void printDefs(PrintStream out) {
		out.printf(str.get("spectrum.defs"), minState, maxState);
	}

	@Override
	public void printLocalDefs(PrintStream out) {
		out.print(str.get("spectrum.localdefs"));
	}
	
	@Override
	protected void printHeaders(PrintStream out) {
		out.print(str.get("spectrum.headers"));
	}

	@Override
	protected void printLogBody(PrintStream out) {
		out.printf(str.get("spectrum.log.body"), clauses, literals, taStateCode);
	}

	@Override
	public void loadLog(Element log) {
		super.loadLog(log);
		minState = attr(log, "minstate", null);
		maxState = attr(log, "maxstate", null);
		if(minState==null || maxState==null)
			throw new RuntimeException("Min/max state definition required.");

		clauses = attr(log, "clauses", clauses);
		literals = attr(log, "literals", literals);

		taStateCode = trimTextContent(element(log, "tastate"));
		if(taStateCode==null)
			throw new RuntimeException("<tastate> missing.");
	}
	
}
