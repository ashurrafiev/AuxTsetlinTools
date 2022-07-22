package ncl.tsetlin.tools.pkbits;

import java.util.Random;

import ncl.tsetlin.tools.pkbits.MnistData.MnistDataItem;

public class ClassFilter {

	public static final int BINARISE_THRESHOLD = 80;
	
	public static MnistData[] filterClasses(MnistData src, int classes, Random random) {
		MnistData[] data = new MnistData[classes];
		for(int i=0; i<classes; i++)
			data[i] = new MnistData(src.imgSize);
		
		for(MnistDataItem si : src.data) {
			MnistDataItem di = data[si.label].add();
			di.img = si.img;
			di.label = 1;
			
			int negLabel = si.label;
			while(negLabel==si.label)
				negLabel = random.nextInt(classes);
			di = data[negLabel].add();
			di.img = si.img;
			di.label = 0;
		}
		
		return data;
	}
	
	public static void main(String[] args) {
		MnistData[] train = filterClasses(MnistData.readTrain(MnistData.ROOT_PATH), 10, new Random());
		for(int i=0; i<train.length; i++)
			MnistData.writeAsPkBits(train[i], String.format("mnist-train-cls%d.bin", i), BINARISE_THRESHOLD);
		
		MnistData.writeAsPkBits(MnistData.readTest(MnistData.ROOT_PATH), "mnist-test.bin", BINARISE_THRESHOLD);
	}

}
