package ncl.tsetlin.tools.pkbits;

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class PkBitsOutputStream {

	public final DataOutputStream out;
	
	private int b;
	private int bits;
	
	public PkBitsOutputStream(String fileName) throws IOException {
		out = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(new File(fileName))));
		b = 0;
		bits = 0;
	}
	
	public void flushByte() throws IOException {
		if(bits>0) {
			out.writeByte(b);
			b = 0;
			bits = 0;
		}
	}
	
	public void writeBit(int v) throws IOException {
		b |= (v<<bits);
		bits++;
		if(bits==8) {
			out.writeByte(b);
			b = 0;
			bits = 0;
		}
	}
	
	public void writeByte(int v) throws IOException {
		flushByte();
		out.writeByte(v);
	}
	
	public void writeIntLE(int v) throws IOException {
		flushByte();
		out.writeByte(v & 0xff);
		out.writeByte((v>>8) & 0xff);
		out.writeByte((v>>16) & 0xff);
		out.writeByte((v>>24) & 0xff);
	}
	
	public void close() throws IOException {
		flushByte();
		out.close();
	}
	
	public static int getReqBytes(int bits) {
		return (bits+7)/8;
	}
}
