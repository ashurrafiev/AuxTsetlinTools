package ncl.tsetlin.tools.utils;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Consumer;

public class ParseParams {

	private abstract class ParamDef<T> {
		public Consumer<T> set;
		public String description;
		public ParamDef(Consumer<T> set, String desc) {
			this.set = set;
			this.description = desc;
		}
		public abstract String getType(boolean ord);
		public abstract T parse(String s);
		public int acceptNext(int i, String[] args) {
			if(i>=args.length) {
				System.err.println("Expected value.");
				return -1;
			}
			try {
				set.accept(parse(args[i+1]));
			}
			catch (NumberFormatException e) {
				System.err.println("Bad number format.");
				return -1;
			}
			return i+1;
		}
		public boolean acceptThis(String s) {
			try {
				set.accept(parse(s));
				return true;
			}
			catch (NumberFormatException e) {
				System.err.println("Bad number format.");
				return false;
			}
		}
	}

	private class FlagParamDef extends ParamDef<Boolean> {
		public FlagParamDef(Consumer<Boolean> set, String desc) {
			super(set, desc);
		}
		@Override
		public String getType(boolean ord) {
			return null;
		}
		@Override
		public Boolean parse(String s) {
			return true;
		}
		public int acceptNext(int i, String[] args) {
			set.accept(true);
			return i;
		}
	}

	private class StrParamDef extends ParamDef<String> {
		public StrParamDef(Consumer<String> set, String desc) {
			super(set, desc);
		}
		@Override
		public String getType(boolean ord) {
			return ord ? null : "string";
		}
		@Override
		public String parse(String s) {
			return s;
		}
	}
	
	private class IntParamDef extends ParamDef<Integer> {
		public IntParamDef(Consumer<Integer> set, String desc) {
			super(set, desc);
		}
		@Override
		public String getType(boolean ord) {
			return ord ? null : "int";
		}
		@Override
		public Integer parse(String s) {
			return Integer.parseInt(s);
		}
	}
	
	private class DoubleParamDef extends ParamDef<Double> {
		public DoubleParamDef(Consumer<Double> set, String desc) {
			super(set, desc);
		}
		@Override
		public String getType(boolean ord) {
			return ord ? null : "int";
		}
		@Override
		public Double parse(String s) {
			return Double.parseDouble(s);
		}
	}
	
	public LinkedHashMap<String, ParamDef<?>> options = new LinkedHashMap<>();
	public ArrayList<ParamDef<?>> ords = new ArrayList<>();

	public void addFlagParam(String name, Consumer<Boolean> set, String description) {
		options.put(name, new FlagParamDef(set, description));
	}

	public void addStrParam(String name, Consumer<String> set, String description) {
		options.put(name, new StrParamDef(set, description));
	}

	public void addStrParam(Consumer<String> set, String description) {
		ords.add(new StrParamDef(set, description));
	}

	public void addIntParam(String name, Consumer<Integer> set, String description) {
		options.put(name, new IntParamDef(set, description));
	}

	public void addIntParam(Consumer<Integer> set, String description) {
		ords.add(new IntParamDef(set, description));
	}
	
	public void addDoubleParam(String name, Consumer<Double> set, String description) {
		options.put(name, new DoubleParamDef(set, description));
	}

	public void addDoubleParam(Consumer<Double> set, String description) {
		ords.add(new DoubleParamDef(set, description));
	}

	public void printUsage() {
		if(!ords.isEmpty()) {
			System.out.print("Usage: [options]");
			int c = 1;
			for(ParamDef<?> def : ords) {
				System.out.print(" ");
				String desc = def.description;
				if(desc==null)
					desc = String.format("param%d", c);
				String type = def.getType(true);
				if(type!=null)
					System.out.printf(" <%s:%s>", type, desc);
				else
					System.out.printf(" <%s>", desc);
				c++;
			}
			System.out.println();
		}
		System.out.println("Options:");
		
		int tab = 0;
		for(String name : options.keySet()) {
			int len = name.length()+12;
			if(len>tab)
				tab = len;
		}
		
		for(Map.Entry<String, ParamDef<?>> e : options.entrySet()) {
			String name = e.getKey();
			ParamDef<?> def = e.getValue();
			System.out.print(name);
			int len = name.length();
			String type = def.getType(false);
			if(type!=null) {
				type = String.format(" <%s>", type);
				System.out.print(type);
				len += type.length();
			}
			if(def.description!=null) {
				for(; len<tab; len++)
					System.out.print(" ");
				System.out.printf(": %s", def.description);
			}
			System.out.println();
		}
	}
	
	public boolean parseParams(String[] args) {
		int c = 0;
		for(int i=0; i<args.length; i++) {
			String arg = args[i];
			ParamDef<?> def = options.get(arg);
			if(def!=null) {
				i = def.acceptNext(i, args);
				if(i<0) {
					printUsage();
					return false;
				}
			}
			else if(arg.startsWith("-")) {
				System.err.println("Unknown option: "+arg);
				printUsage();
				return false;
			}
			else {
				if(c>ords.size()) {
					System.err.println("Too many program arguments");
					printUsage();
					return false;
				}
				if(!ords.get(c).acceptThis(arg)) {
					printUsage();
					return false;
				}
				c++;
			}
		}
		return true;
	}
	
}
