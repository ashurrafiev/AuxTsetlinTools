package ncl.tsetlin.tools.spectrum;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
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
	public static final int pointw = 5;
	public static final int pointh = 2;
	public static final double normalise = 1000.0;
	
	public static void main(String[] args) {
		Data data = Data.read(new File(dataPath));
		int xcount = data.count();
		for(int cls=0; cls<classes; cls++) {
			BufferedImage img = new BufferedImage(pointw*xcount, pointh*ycount, BufferedImage.TYPE_INT_RGB);
			Graphics2D g2 = img.createGraphics();
			int x = 0;
			for(Row row : data.rows()) {
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
				x++;
			}
			int y = (ycount+minState-1)*pointh;
			g2.setColor(Color.WHITE);
			g2.drawLine(0, y, pointw*xcount, y);
			g2.setColor(Color.BLACK);
			g2.setStroke(new BasicStroke(1, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, new float[]{10, 10}, 0));
			g2.drawLine(0, y, pointw*xcount, y);
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
