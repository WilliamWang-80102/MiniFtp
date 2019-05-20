package cn.edu.whu.wzw.ftp;

import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.Arrays;

public class MyTest {

	public static void main(String[] args) {
		//byte b = (byte) 0b11000000;
		//System.out.println(b & 0xff);
		/*
		String hello = "hello";
		Charset charSet = Charset.defaultCharset();
		System.out.println(charSet.toString());
		byte[] hbytes = hello.getBytes(charSet);
		System.out.println(Arrays.toString(hbytes));
		*/
		

		try {
			Charset charSet = Charset.defaultCharset();
			System.out.println(charSet.toString());
			String s = "中China";
			byte[] sb = s.getBytes("gbk");
			byte[] sb2 = s.getBytes("UTF-8");
			//String ns = new String(sb,"gbk");
			System.out.println(Arrays.toString(sb));
			System.out.println(Arrays.toString(sb2));
			System.out.println(new String(sb2,"gbk"));
			//System.out.println(new String(sb,"UTF-8"));
			//String ns = new String(sb,"gbk");
			//System.out.println(ns.equals(s));
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}

		
	}

}
