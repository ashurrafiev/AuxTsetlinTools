package ncl.tsetlin.tools.pkbits;

import ncl.tsetlin.tools.pkbits.MnistData.MnistDataItem;

public class MiniMnistData {

	public static final int IMG_SIZE = 8;
	public static final int CONV_BLOCK_SIZE = 3;
	public static final int CONV_MARGIN_LEFT = 2;
	public static final int CONV_MARGIN_RIGHT = 2;
	public static final int BINARISE_THRESHOLD = 80;
	public static final int[] CONV_LABELS = {0, 1, 2, 3, 4};
	
	public static int convertLabel(int label) {
		for(int i=0; i<CONV_LABELS.length; i++) {
			if(CONV_LABELS[i]==label)
				return i;
		}
		return -1;
	}
	
	public static int countLabels(MnistData data) {
		int count = 0;
		for(MnistDataItem di : data.data) {
			if(convertLabel(di.label)>=0)
				count++;
		}
		return count;
	}
	
	public static MnistData convertMnist(MnistData src, int count, boolean binarise) {
		int targetImgSize = IMG_SIZE*CONV_BLOCK_SIZE+CONV_MARGIN_LEFT+CONV_MARGIN_RIGHT;
		if(targetImgSize!=src.imgSize)
			throw new RuntimeException(String.format("Conversion image size mismatch (%d vs %d)", targetImgSize, src.imgSize));
		int labelCount = countLabels(src);
		if(count>0 && labelCount>count)
			labelCount = count;
		MnistData data = new MnistData(IMG_SIZE);
		
		int dataIndex = 0;
		for(int srcIndex=0; srcIndex<src.count(); srcIndex++) {
			MnistDataItem si = src.data.get(srcIndex);
			int label = convertLabel(si.label);
			if(label>=0) {
				MnistDataItem di = data.add();
				for(int y=0; y<data.imgSize; y++)
					for(int x=0; x<data.imgSize; x++) {
						int sum = 0;
						int max = 0;
						for(int by=0; by<CONV_BLOCK_SIZE; by++)
							for(int bx=0; bx<CONV_BLOCK_SIZE; bx++) {
								int sx = CONV_MARGIN_LEFT + x*CONV_BLOCK_SIZE + bx;
								int sy = CONV_MARGIN_LEFT + y*CONV_BLOCK_SIZE + by;
								int v = si.img[sx][sy];
								sum += v;
								if(v>max) max = v;
							}
						int value = (sum+max*CONV_BLOCK_SIZE) / CONV_BLOCK_SIZE / (CONV_BLOCK_SIZE+1);
						if(binarise)
							value = (value>=BINARISE_THRESHOLD) ? 1 : 0;
						di.img[x][y] = value;
					}
				di.label = label;
				dataIndex++;
				if(count>0 && dataIndex>=count)
					break;
			}
		}
		System.out.printf("Converted %d items\n", data.count());
		
		return data;
	}
	
	public static void main(String[] args) {
		MnistData trainData = convertMnist(MnistData.readTrain(MnistData.ROOT_PATH), 30000, true);
		//MnistToPng.savePng(MnistToPng.createPng(trainData, 0, true), "mini-train.png");
		MnistData.writeAsText(trainData, "mini8-0-4-train.txt");
		
		MnistData testData = convertMnist(MnistData.readTest(MnistData.ROOT_PATH), 5000, true);
		//MnistToPng.savePng(MnistToPng.createPng(testData, 0, true), "mini-test.png");
		MnistData.writeAsText(testData, "mini8-0-4-test.txt");
	}
}
