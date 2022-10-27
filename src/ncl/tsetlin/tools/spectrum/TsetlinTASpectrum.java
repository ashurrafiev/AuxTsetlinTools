package ncl.tsetlin.tools.spectrum;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;

import javax.imageio.ImageIO;

import ncl.tsetlin.tools.utils.ParseParams;

public class TsetlinTASpectrum {

	public static final String dataPathFmt = "/c%d-spectrum.csv";
	public static final String imgPathFmt = "/c%d-spectrum.png";
	
	public static String basePath = null;
	
	public static int classes = 10;
	public static int epochSize = 60;
	public static int minState = -99;
	public static int maxState = 100;
	
	public static int pointw = 4;
	public static int pointh = 2;
	public static double normalise = 5000.0; // 1000.0;
	
	public static int xgrid = 10;
	public static int ygrid = 10;
	
	public static int marginLeft = 50;
	public static int marginRight = 20;
	public static int marginTop = 20;
	public static int marginBottom = 40;
	
	public static String fontName = "Tahoma";
	public static int fontSize = 13;
	public static int titleFontSize = 16;

	private static String timeLabel(int x) {
		return String.format("%d:%dk", (x-1)/epochSize, (x-1)%epochSize);
	}
	
	private static float align(float span, int align) {
		return span * (float)align / 2f;
	}
	
	private static void drawString(Graphics2D graph, String str, float x, float y, int halign, int valign) {
		FontMetrics fm = graph.getFontMetrics();
		float w = fm.stringWidth(str);
		float h = fm.getAscent() - fm.getDescent();
		float tx = x - align(w, halign);
		float ty = y + h - align(h, valign);
		graph.drawString(str, tx, ty);
	}
	
	public static void main(String[] args) {
		ParseParams p = new ParseParams();
		p.addStrParam(x -> basePath = x, "csv path");
		p.addIntParam("-classes", x -> classes = x, "number of classes");
		p.addIntParam("-epochsize", x -> epochSize = x, "number of csv lines per epoch");
		p.addIntParam("-minstate", x -> minState = x, "lowest TA state");
		p.addIntParam("-maxstate", x -> maxState = x, "highest TA state");
		p.addIntParam("-pointw", x -> pointw = x, "spectrum point width");
		p.addIntParam("-pointh", x -> pointh = x, "spectrum point height");
		p.addDoubleParam("-brightness", x -> normalise = x, "spectrum brightness factor");
		p.addIntParam("-xgrid", x -> xgrid = x, "horizontal grid");
		p.addIntParam("-ygrid", x -> ygrid = x, "vertical grid");
		p.addIntParam("-margin-left", x -> marginLeft = x, "left margin");
		p.addIntParam("-margin-right", x -> marginRight = x, "right margin");
		p.addIntParam("-margin-top", x -> marginTop = x, "top margin");
		p.addIntParam("-margin-bottom", x -> marginBottom = x, "bottom margin");
		p.addStrParam("-font", x -> fontName = x, "font name");
		p.addIntParam("-font-size", x -> fontSize = x, "font size");
		p.addIntParam("-font-title", x -> titleFontSize = x, "font size (title)");
		p.addFlagParam("-help", x -> {p.printUsage(); System.exit(1);}, null);
		if(!p.parseParams(args))
			System.exit(1);
		if(basePath==null) {
			System.err.println("Path to CSV files is not specified.");
			System.err.flush();
			p.printUsage();
			System.exit(1);
		}
		
		int ycount = maxState-minState+1; 
		for(int cls=0; cls<classes; cls++) {
			String dataPath = String.format(basePath+dataPathFmt, cls);
			
			ArrayList<String> lines = new ArrayList<>();
			try {
				Scanner in = new Scanner(new File(dataPath));
				in.nextLine();
				while(in.hasNextLine()) {
					String line = in.nextLine();
					if(!line.isEmpty())
						lines.add(line);
				}
			}
			catch (IOException e) {
				e.printStackTrace();
			}
			int xcount = lines.size();
			if(xcount==0) {
				System.err.printf("No data for class %d\n", cls);
				return;
			}
			
			BufferedImage img = new BufferedImage(pointw*xcount+marginLeft+marginRight, pointh*ycount+marginTop+marginBottom, BufferedImage.TYPE_INT_RGB);
			Graphics2D g2 = img.createGraphics();
			g2.setColor(Color.WHITE);
			g2.fillRect(0, 0, img.getWidth(), img.getHeight());
			g2.translate(marginLeft, marginTop);
			
			int x = 0;
			for(String line : lines) {
				if(x>0) {
					String[] data = line.split("\\s+");
					for(int y=0; y<ycount; y++) {
						int v = Integer.parseInt(data[y+1]);
						float c = 1.0f - (float)Math.min(v/normalise, 1.0);
						g2.setColor(new Color(c, c, c));
						g2.fillRect(x*pointw, (ycount-y-1)*pointh, pointw, pointh);
					}
				}
				x++;
			}

			int y;
			g2.setColor(new Color(0x22000000, true));
			g2.setStroke(new BasicStroke(1));
			for(x=1; x<=xcount; x+=xgrid)
				g2.drawLine(x*pointw, 0, x*pointw, pointh*ycount);
			for(y=0; y<=ygrid; y++)
				g2.drawLine(pointw, y*pointh*ycount/ygrid, pointw*xcount, y*pointh*ycount/ygrid);
			y = (ycount+minState-1)*pointh;
			g2.setColor(Color.WHITE);
			g2.drawLine(0, y, pointw*xcount, y);
			g2.setColor(Color.BLACK);
			g2.setStroke(new BasicStroke(1, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, new float[]{10, 10}, 0));
			g2.drawLine(pointw, y, pointw*xcount, y);
			
			g2.setColor(Color.BLACK);
			Font font = new Font(fontName, Font.PLAIN, fontSize);
			g2.setFont(font);
			g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);
			for(x=1; x<=xcount; x+=xgrid)
				drawString(g2, timeLabel(x), x*pointw, pointh*ycount+5, 1, 0);
			drawString(g2, "time (epochs:inputs)", xcount*pointw/2, pointh*ycount+20, 1, 0);
			for(y=0; y<=ygrid; y++) {
				String s;
				if(y==ygrid/2)
					s = "flip";
				else if(y<ygrid/2)
					s = String.format("inc %d", maxState-y*2*maxState/ygrid);
				else
					s = String.format("exc %d", (y-ygrid/2)*2*maxState/ygrid);
				drawString(g2, s, 0, y*pointh*ycount/ygrid, 2, 1);
			}
			
			font = font.deriveFont(Font.BOLD, titleFontSize);
			g2.setFont(font);
			drawString(g2, String.format("Class '%d'", cls), xcount*pointw/2, -5, 1, 2);

			try {
				String imgPath = String.format(basePath+imgPathFmt, cls);
				ImageIO.write(img, "PNG", new File(imgPath));
				System.out.printf("Saved %s\n", imgPath);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

}
