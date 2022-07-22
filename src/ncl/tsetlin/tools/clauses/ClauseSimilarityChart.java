package ncl.tsetlin.tools.clauses;

import static ncl.tsetlin.tools.clauses.TMState.numClauses;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Arrays;

import javax.imageio.ImageIO;

public class ClauseSimilarityChart {

	public static final String basePath = "../../ClassParallelTM/bak/res0719/jm/";
	public static final String path = basePath+"st%d.csv";
	public static final String outPath = basePath+"sim%d.png";
	public static final int pxSize = 16;
	public static final float contrast = 2f; 
	
	private static class Clause implements Comparable<Clause> {
		public int index;
		public double[] sim = new double[numClauses/2];
		public double avg;
		
		public Clause(TMState tm, int j, int sign) {
			this.index = j;
			double sum = 0.0;
			for(int j2=0; j2<numClauses/2; j2++) {
				sim[j2] = tm.clauseSimilarity(j*2+sign, j2*2+sign);
				sum += sim[j2];
			}
			this.avg = sum / (numClauses/2);
		}
		
		@Override
		public int compareTo(Clause o) {
			return Double.compare(o.avg, this.avg);
		}
	}
	
	public static void processClass(int cls) {
		TMState tm = TMState.load(String.format(path, cls));
		BufferedImage img = new BufferedImage((numClauses/2)*pxSize*2, (numClauses/2)*pxSize, BufferedImage.TYPE_INT_RGB);
		Graphics2D g2 = img.createGraphics();
		Clause[][] clauses = new Clause[2][numClauses/2];
		for(int sign=0; sign<2; sign++)
			for(int j=0; j<numClauses/2; j++) {
				clauses[sign][j] = new Clause(tm, j, sign);
			}
		Arrays.sort(clauses[0]);
		Arrays.sort(clauses[1]);
		for(int sign=0; sign<2; sign++)
			for(int j1=0; j1<numClauses/2; j1++) {
				Clause clause = clauses[sign][j1];
				for(int j2=0; j2<numClauses/2; j2++) {
					int index = clauses[sign][j2].index;
					float c = (float)clause.sim[index] * contrast;
					if(c>0f) {
						if(c>1f)
							c = 1f;
						g2.setColor(new Color(1f-c, 1f-c, 1f-c));
					}
					else {
						if(c<-1f)
							c = -1f;
						g2.setColor(new Color(1f, 1f+c, 1f+c));
					}
					g2.fillRect((sign*numClauses/2+j1)*pxSize, j2*pxSize, pxSize, pxSize);
				}
			}
		try {
			ImageIO.write(img, "PNG", new File(String.format(outPath, cls)));
			System.out.printf("Done %d\n", cls);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) {
		for(int c=0; c<10; c++)
			processClass(c);
	}

}
