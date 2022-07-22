package ncl.tsetlin.tools.pkbits;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;

public class MnistData {

	public static final String ROOT_PATH = "../../../poets/tsetlin";
	
	public static final int MNIST_IMG_SIZE = 28;
	
	public static final String FILENAME_IMAGES_TRAIN = "train-images.idx3-ubyte";
	public static final String FILENAME_LABELS_TRAIN = "train-labels.idx1-ubyte";
	public static final int COUNT_TRAIN = 60000;
	public static final String FILENAME_IMAGES_TEST = "t10k-images.idx3-ubyte";
	public static final String FILENAME_LABELS_TEST = "t10k-labels.idx1-ubyte";
	public static final int COUNT_TEST = 10000;
	
	public class MnistDataItem {
		public int[][] img = new int[imgSize][imgSize];
		public int label;
	}
	
	public final int imgSize;
	public final ArrayList<MnistDataItem> data;
	
	public MnistData(int imgSize) {
		this.imgSize = imgSize;
		this.data = new ArrayList<>();
	}
	
	public MnistDataItem add() {
		MnistDataItem di = new MnistDataItem();
		data.add(di);
		return di;
	}
	
	public int count() {
		return data.size();
	}
	
	public int countLabel(int label) {
		int count = 0;
		for(MnistDataItem di : data) {
			if(di.label==label)
				count++;
		}
		return count;
	}
	
	public void printLabelCounts(int[] labels) {
		for(int label : labels) {
			System.out.printf("%d : %d\n", label, countLabel(label));
		}
	}

	public void printLabelCounts(int labelRange) {
		int[] labels = new int[labelRange];
		for(int i=0; i<labels.length; i++)
			labels[i] = i;
		printLabelCounts(labels);
	}

	public static MnistData read(int count, File fileImages, File fileLabels) {
		try {
			DataInputStream in;
			in = new DataInputStream(new BufferedInputStream(new FileInputStream(fileImages), 65536));
			in.readInt();
			in.readInt();
			in.readInt();
			in.readInt();
			
			MnistData data = new MnistData(MNIST_IMG_SIZE);
			for(int i=0; i<count; i++) {
				MnistDataItem di = data.add();
				for(int y=0; y<data.imgSize; y++)
					for(int x=0; x<data.imgSize; x++) {
						int b = in.readByte() & 0xff;
						di.img[x][y] = b;
					}
			}
			in.close();

			in = new DataInputStream(new BufferedInputStream(new FileInputStream(fileLabels), 65536));
			in.readInt();
			in.readInt();
			for(int i=0; i<count; i++) {
				int b = in.readByte() & 0xff;
				data.data.get(i).label = b;
			}
			in.close();

			return data;
		}
		catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public static MnistData readTrain(String rootPath) {
		return read(COUNT_TRAIN, new File(rootPath, FILENAME_IMAGES_TRAIN), new File(rootPath, FILENAME_LABELS_TRAIN));
	}

	public static MnistData readTest(String rootPath) {
		return read(COUNT_TEST, new File(rootPath, FILENAME_IMAGES_TEST), new File(rootPath, FILENAME_LABELS_TEST));
	}
	
	public static void writeAsText(MnistData data, String fileName) {
		try {
			PrintWriter out = new PrintWriter(new File(fileName));
			for(MnistDataItem di : data.data) {
				for(int y=0; y<data.imgSize; y++)
					for(int x=0; x<data.imgSize; x++) {
						out.printf("%d ", di.img[x][y]);
					}
				out.printf("%d\n", di.label);
			}
			out.close();
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void writeAsPkBits(MnistData data, String fileName, int binariseThreshold) {
		try {
			PkBitsOutputStream out = new PkBitsOutputStream(fileName);
			int bits = data.imgSize*data.imgSize;
			out.writeIntLE(bits);
			out.writeIntLE(PkBitsOutputStream.getReqBytes(bits));
			out.writeIntLE(data.count());
			for(MnistDataItem di : data.data) {
				for(int y=0; y<data.imgSize; y++)
					for(int x=0; x<data.imgSize; x++) {
						out.writeBit((di.img[x][y]>=binariseThreshold) ? 1 : 0);
					}
				out.writeByte(di.label);
			}
			out.close();
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}

}
