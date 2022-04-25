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

import javax.imageio.ImageIO;

import com.xrbpowered.jdiagram.data.Data;
import com.xrbpowered.jdiagram.data.Data.Row;

public class TsetlinTASpectrum {

	public static final String dataPath = "data/tm-spectrum.csv"; 
	public static final int classes = 10;
	public static final int minState = -99;
	public static final int maxState = 100;
	public static final int ycount = maxState-minState+1;
	
	public static final String imgPathFmt = "data/tm-spectrum-c%d.png";
	public static final int pointw = 4;
	public static final int pointh = 2;
	public static final double normalise = 1500.0; // 1000.0;
	
	public static final int marginLeft = 50;
	public static final int marginRight = 20;
	public static final int marginTop = 20;
	public static final int marginBottom = 40;
	public static final int xgrid = 10;
	public static final int ygrid = 10;
	
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
		Data data = Data.read(new File(dataPath));
		int xcount = data.count();
		for(int cls=0; cls<classes; cls++) {
			BufferedImage img = new BufferedImage(pointw*xcount+marginLeft+marginRight, pointh*ycount+marginTop+marginBottom, BufferedImage.TYPE_INT_RGB);
			Graphics2D g2 = img.createGraphics();
			g2.setColor(Color.WHITE);
			g2.fillRect(0, 0, img.getWidth(), img.getHeight());
			g2.translate(marginLeft, marginTop);
			
			int x = 0;
			for(Row row : data.rows()) {
				if(x>0) {
					for(int y=0; y<ycount; y++) {
						String hdr = String.format("c%d%+d", cls, y+minState);
						Double v = row.getNum(hdr);
						if(v==null) {
							System.out.printf("[%s][%d] is null\n", hdr, x);
							v = 0.0;
						}
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
			for(x=1; x<=data.count(); x+=xgrid)
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
			Font font = new Font("Tahoma", Font.PLAIN, 13);
			g2.setFont(font);
			g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);
			for(x=1; x<=data.count(); x+=xgrid)
				drawString(g2, String.format("%d:%dk", (x-1)/60, (x-1)%60), x*pointw, pointh*ycount+5, 1, 0);
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
			
			font = font.deriveFont(Font.BOLD, 16);
			g2.setFont(font);
			drawString(g2, String.format("Class '%d'", cls), xcount*pointw/2, -5, 1, 2);

			try {
				String imgPath = String.format(imgPathFmt, cls);
				ImageIO.write(img, "PNG", new File(imgPath));
				System.out.printf("Saved %s\n", imgPath);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

}
