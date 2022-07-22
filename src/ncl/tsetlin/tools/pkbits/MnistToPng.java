package ncl.tsetlin.tools.pkbits;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import ncl.tsetlin.tools.pkbits.MnistData.MnistDataItem;

public class MnistToPng {

	public static final int GRID_WIDTH = 100;
	public static final int MAX_GRID_HEIGHT = 100;
	
	public static BufferedImage createPng(MnistData data, int start, boolean binarised) {
		int gridHeight = (data.count()+GRID_WIDTH-1) / GRID_WIDTH;
		if(gridHeight>MAX_GRID_HEIGHT)
			gridHeight = MAX_GRID_HEIGHT;
		
		int w = GRID_WIDTH*data.imgSize;
		int h =gridHeight*data.imgSize;
		int pixels[] = new int[w*h*4];
		
		int dataIndex = start;
		for(int gy=0; gy<gridHeight; gy++)
			for(int gx=0; gx<GRID_WIDTH; gx++) {
				MnistDataItem di = dataIndex<data.count() ? data.data.get(dataIndex++) : null;
				for(int y=0; y<data.imgSize; y++)
					for(int x=0; x<data.imgSize; x++) {
						int offs = ((x+gx*data.imgSize) + (y+gy*data.imgSize)*w)*4;
						int c = di==null ? 0 : di.img[x][y];
						if(binarised)
							c = (c!=0) ? 255 : 0;
						Color col = new Color(c, c, c);
						pixels[offs+3] = 255;
						pixels[offs+0] = col.getRed();
						pixels[offs+1] = col.getGreen();
						pixels[offs+2] = col.getBlue();
					}
			}
		
		
		BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
		WritableRaster raster = img.getRaster();
		raster.setPixels(0, 0, w, h, pixels);
		return img;
	}
	
	public static void savePng(BufferedImage img, String fileName) {
		try {
			ImageIO.write(img, "PNG", new File(fileName));
			System.out.println("Saved "+fileName);
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) {
		MnistData testData = MnistData.readTest(MnistData.ROOT_PATH);
		savePng(createPng(testData, 0, false), "test.png");
		MnistData trainData = MnistData.readTrain(MnistData.ROOT_PATH);
		for(int i=0; i<6; i++)
			savePng(createPng(trainData, i*10000, false), String.format("train%d.png", i));
	}

}
