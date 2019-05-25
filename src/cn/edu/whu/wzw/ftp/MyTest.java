package cn.edu.whu.wzw.ftp;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Arrays;

public class MyTest {

	Socket ctrlSocket;// 控制用Socket
	public PrintWriter ctrlOutput;// 控制输出用的流
	public BufferedReader ctrlInput;// 控制输入用的流
	final int CTRLPORT = 21;// ftp 的控制用端口
	ServerSocket serverDataSocket;
	final String URL = "127.0.0.1";
	final String user = "wzw";
	final String pwd = "";
	
	// getMsgs方法
	// 启动从控制流收信的线程
	public void getMsgs() {
		try {
			CtrlListen listener = new CtrlListen(ctrlInput);
			Thread listenerthread = new Thread(listener);
			listenerthread.start();
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
	}
	
	// openConnection方法
	// 由地址和端口号构造Socket，形成控制用的流
	public void openConnection(String host) throws IOException, UnknownHostException {
		ctrlSocket = new Socket(host, CTRLPORT);
		ctrlOutput = new PrintWriter(ctrlSocket.getOutputStream());
		ctrlInput = new BufferedReader(new InputStreamReader(ctrlSocket.getInputStream()));
	}
	
	public void doLogin(String loginName, String password) {
		try {
			ctrlOutput.println("USER " + loginName);
			ctrlOutput.flush();
			ctrlOutput.println("PASS " + password);
			ctrlOutput.flush();
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
	}
	
	// dataConnection方法
	// 构造与服务器交换数据用的Socket
	// 再用PORT命令将端口通知服务器
	public Socket dataConnection(String ctrlcmd) {
		String cmd = "PORT "; // PORT存放用PORT命令传递数据的变量
		int i;
		Socket dataSocket = null;// 传送数据用Socket
		try {
			// 得到自己的地址
			//byte[] address = InetAddress.getLocalHost().getAddress();
			byte[] address = {127,0,0,1};
			// 用适当的端口号构造服务器
			serverDataSocket = new ServerSocket(0, 1);
			/*
			System.out.println(Arrays.toString(address));
			System.out.println(InetAddress.getLocalHost().getHostAddress());
			System.out.println(serverDataSocket.getLocalPort());
			*/
			// 准备传送PORT命令用的数据
			for (i = 0; i < 4; ++i)
				cmd = cmd + (address[i] & 0xff) + ",";
			cmd = cmd + (((serverDataSocket.getLocalPort()) / 256) & 0xff) + ","
					+ (serverDataSocket.getLocalPort() & 0xff);
			// 利用控制用的流传送PORT命令
			ctrlOutput.println(cmd);
			ctrlOutput.flush();
			// 向服务器发送处理对象命令(LIST,RETR,及STOR)
			//ctrlOutput.println(ctrlcmd);
			ctrlcmd = new String(ctrlcmd);
			//System.out.println("文件控制指令为：" + ctrlcmd);
			ctrlOutput.println(ctrlcmd);
			ctrlOutput.flush();

			// 接受与服务器的连接

			dataSocket = serverDataSocket.accept();
			serverDataSocket.close();
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
		return dataSocket;
	}
	
	public static void main(String[] args) throws UnknownHostException {		
		MyTest test = new MyTest();
		//开启连接
		try {
			test.openConnection(test.URL);
		} catch (UnknownHostException e) {
			// TODO 自动生成的 catch 块
			e.printStackTrace();
		} catch (IOException e) {
			// TODO 自动生成的 catch 块
			e.printStackTrace();
		}
		//开启信息接收线程
//		test.getMsgs();
		//登陆
		test.doLogin(test.user, test.pwd);
		//发送LIST指令，打印内容
		// 建立数据连接
		Socket dataSocket = test.dataConnection("LIST ");
		// 读取返回LIST数据
		String line;		
		try {
			BufferedReader dataInput = new BufferedReader(new InputStreamReader(dataSocket.getInputStream(), "UTF-8"));
			while ((line = dataInput.readLine()) != null) {
//				System.out.println(line);
				String[] sArray = line.split("\\s+", 0);//如果Windows文件夹名含有空格会出现问题
				System.out.println(Arrays.toString(sArray));
			}
		} catch (UnsupportedEncodingException e) {
			// TODO 自动生成的 catch 块
			e.printStackTrace();
		} catch (IOException e) {
			// TODO 自动生成的 catch 块
			e.printStackTrace();
		} finally {
			try {
				dataSocket.close();
			} catch (IOException e) {
				// TODO 自动生成的 catch 块
				e.printStackTrace();
			}
		}
		
		
		//byte b = (byte) 0b11000000;
		//System.out.println(b & 0xff);
		/*
		String hello = "hello";
		Charset charSet = Charset.defaultCharset();
		System.out.println(charSet.toString());
		byte[] hbytes = hello.getBytes(charSet);
		System.out.println(Arrays.toString(hbytes));
		*/
		

//		try {
//			Charset charSet = Charset.defaultCharset();
//			System.out.println(charSet.toString());
//			String s = "中China";
//			byte[] sb = s.getBytes("gbk");
//			byte[] sb2 = s.getBytes("UTF-8");
//			//String ns = new String(sb,"gbk");
//			System.out.println(Arrays.toString(sb));
//			System.out.println(Arrays.toString(sb2));
//			System.out.println(new String(sb2,"gbk"));
//			//System.out.println(new String(sb,"UTF-8"));
//			//String ns = new String(sb,"gbk");
//			//System.out.println(ns.equals(s));
//		} catch (UnsupportedEncodingException e) {
//			e.printStackTrace();
//		}
	}

}
