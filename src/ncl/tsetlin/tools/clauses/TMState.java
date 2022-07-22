package ncl.tsetlin.tools.clauses;

import java.io.File;
import java.util.Scanner;

public class TMState {

	public static final int numClauses = 100;
	public static final int numLiterals = 28*28*2; 
	
	public int[][] ta = new int[numClauses][numLiterals];
	
	public static int voteSign(int j) {
		return (j&1)!=0 ? -1 : 1;
	}

	public static boolean polarity(int k) {
		return (k&1)!=0;
	}

	public static boolean literalValue(boolean[] input, int k) {
		return input[k/2] ^ polarity(k);
	}

	public static boolean includeLiteral(int state) {
		return state>0;
	}
	
	public boolean clauseOutput(int j, boolean input[], boolean eval) {
		boolean output = true;
		boolean inc = false;
		for(int k=0; output && k<numLiterals; k++) {
			if(includeLiteral(ta[j][k])) {
				output &= literalValue(input, k);
				inc = true;
			}
		}
		if(eval && !inc)
			return output = false;
		return output;
	}
	
	public int incSign(int j, int k) {
		if(includeLiteral(ta[j][k]))
			return polarity(k) ?  -1 : 1;
		else
			return 0;
	}
	
	public double clauseSimilarity(int j1, int j2) {
		int r = 0;
		int countInc = 0;
		for(int k=0; k<numLiterals; k++) {
			int inc1 = incSign(j1, k);
			int inc2 = incSign(j2, k);
			if(inc1!=0 || inc2!=0) {
				r += inc1*inc2;
				countInc++;
			}
		}
		if(countInc==0)
			return 0.0;
		else
			return (double)r / (double)countInc;
	}
	
	public static TMState load(String path) {
		try {
			Scanner in = new Scanner(new File(path));
			in.nextInt();
			in.nextInt();
			in.nextInt();
			TMState tm = new TMState();
			for(int j=0; j<numClauses; j++)
				for(int k=0; k<numLiterals; k++)
					tm.ta[j][k] = in.nextInt();
			in.close();
			return tm;
		}
		catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
			return null;
		}
	}
	
}
