package net.gcolin.di.atinject.event;

import java.nio.charset.StandardCharsets;

public class Test {

	public static void main(String[] args) {
		char[] c = new char[6];
		c[0] = 0xCE;
		c[0] = 0xBC;
		c[0] = 'm';
		c[0] = ' ';
		c[0] = 0xC3;
		c[0] = 0xA0;
		//System.out.println(new String(c));
		byte[] b = new byte[3];
		b[0] =  (byte) 0xCE;
		b[1] = (byte) 0xBC;
		b[2] = 'm';
		System.out.println(new String(b, StandardCharsets.UTF_8));

	}

}
