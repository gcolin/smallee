package net.gcolin.rest.client;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import net.gcolin.common.io.ByteArrayOutputStream;
import net.gcolin.common.io.Io;

public class InputStreamOutputStream extends ByteArrayOutputStream {

	private InputStream in;
	
	public InputStreamOutputStream(InputStream in) {
		this.in = in;
	}

	@Override
	public void writeTo(OutputStream out) throws IOException {
		Io.copy(in, out);
	}
	
	@Override
	public boolean isEmpty() {
		return false;
	}

}
