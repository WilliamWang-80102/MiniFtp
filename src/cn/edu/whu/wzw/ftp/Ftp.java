package cn.edu.whu.wzw.ftp;

import java.net.*;
import java.io.*;
import java.util.*;
/*
 * 基于filezilla的ftp服务器实现的ftp客户端
 */
public class Ftp {
	Socket ctrlSocket;// 控制用Socket
	public PrintWriter ctrlOutput;// 控制输出用的流
	public BufferedReader ctrlInput;// 控制输入用的流
	final int CTRLPORT = 21;// ftp 的控制用端口
	ServerSocket serverDataSocket;

	// openConnection方法
	// 由地址和端口号构造Socket，形成控制用的流
	public void openConnection(String host) throws IOException, UnknownHostException {
		ctrlSocket = new Socket(host, CTRLPORT);
		ctrlOutput = new PrintWriter(ctrlSocket.getOutputStream());
		ctrlInput = new BufferedReader(new InputStreamReader(ctrlSocket.getInputStream()));
	}

	// closeConnection方法
	// 关闭控制用的Socket
	public void closeConnection() throws IOException {
		ctrlSocket.close();
	}

	// showMenu方法
	// 输出ftp的命令菜单
	public void showMenu() {
		System.out.println(">Command?");
		System.out.print(" 1:ls");
		System.out.print(" 2:cd");
		System.out.print(" 3:get");
		System.out.print(" 4:put");
		System.out.print(" 5:ascii");
		System.out.print(" 6:binary");
		System.out.println(" 7:quit");
	}

	// getCommand方法
	// 读取用户指定的命令序号
	public String getCommand() {
		String buf = "";
		BufferedReader lineread = new BufferedReader(new InputStreamReader(System.in));
		while (buf.length() != 1) {
			try {
				buf = lineread.readLine();
			} catch (Exception e) {
				e.printStackTrace();
				System.exit(1);
			}
		}
		return (buf);
	}

	public void doLogin() {
		String loginName = "";
		String password = "";
		BufferedReader lineread = new BufferedReader(new InputStreamReader(System.in));
		try {
			System.out.println("请输入用户名");
			loginName = lineread.readLine();

			ctrlOutput.println("USER " + loginName);
			ctrlOutput.flush();
			System.out.println("请输入口令");
			password = lineread.readLine();
			ctrlOutput.println("PASS " + password);
			ctrlOutput.flush();
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
	}

	public void doQuit() {
		try {
			ctrlOutput.println("QUIT ");
			ctrlOutput.flush();
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
	}

	public void doCd() {
		String dirName = "";
		BufferedReader lineread = new BufferedReader(new InputStreamReader(System.in));
		try {
			System.out.println("请输入目录名");
			dirName = lineread.readLine();
			String cmd = new String(("CWD " + dirName).getBytes("gbk"),"gbk");
			System.out.println(cmd);
			ctrlOutput.println(cmd);// CWD命令
			ctrlOutput.flush();
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
	}

	// doLs方法
	// 取得目录信息
	public void doLs() {
		try {
			//int n;
			//char[] buff = new char[1024];
			int c;
			// 建立数据连接
			Socket dataSocket = dataConnection("NLST ");

			// 准备读取数据用的流
			//BufferedInputStream dataInput = new BufferedInputStream(dataSocket.getInputStream());
			InputStreamReader dataInput = new InputStreamReader(dataSocket.getInputStream(),"UTF-8");
			// 读取目录信息
			
			//while ((n = dataInput.read(buff)) > 0) {
			//	System.out.write(buff, 0, n);
			//}
			while ((c = dataInput.read()) > 0) {
				System.out.print((char)c);
			}
			dataSocket.close();
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

	// doAscii方法
	// 设置文本传输模式
	public void doAscii() {
		try {
			ctrlOutput.println("TYPE A");// A模式
			ctrlOutput.flush();
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
	}

	// doBinary方法
	// 设置二进制传输模式
	public void doBinary() {
		try {
			ctrlOutput.println("TYPE I");// I模式
			ctrlOutput.flush();
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
	}

	// doGet方法
	// 取得服务器上的文件
	public void doGet() {
		String fileName = "";
		String loafile = "";
		BufferedReader lineread = new BufferedReader(new InputStreamReader(System.in));
		try {
			int n;
			byte[] buff = new byte[1024];
			// 指定服务器上的文件名
			System.out.println("远程文件名");
			fileName = lineread.readLine();
			System.out.println("输入的远程文件名：" + fileName);
			// 在客户端上准备接收用的文件
			System.out.println("本地文件");
			loafile = lineread.readLine();
			File local = new File(loafile);
			FileOutputStream outfile = new FileOutputStream(local);
			// 构造传输文件用的数据流
			Socket dataSocket = dataConnection("RETR " + fileName);
			BufferedInputStream dataInput = new BufferedInputStream(dataSocket.getInputStream());
			// 接收来自服务器的数据，写入本地文件
			while ((n = dataInput.read(buff)) > 0) {
				outfile.write(buff, 0, n);
			}
			dataSocket.close();
			outfile.close();
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
	}

	// doPut方法
	// 向服务器发送文件
	public void doPut() {
		String fileName = "";
		BufferedReader lineread = new BufferedReader(new InputStreamReader(System.in));

		try {
			int n;
			byte[] buff = new byte[1024];
			FileInputStream sendfile = null;
			// 指定文件名
			System.out.println("本地文件");
			fileName = lineread.readLine();

			// 准备读出客户端上的文件
			// BufferedInputStream dataInput = new BufferedInputStream(new
			// FileInputStream(fileName));
			try {
				sendfile = new FileInputStream(fileName);
			} catch (Exception e) {
				System.out.println("文件不存在");
				return;
			}
			System.out.println("远程文件");
			String lonfile = lineread.readLine();
			// 准备发送数据的流
			Socket dataSocket = dataConnection("STOR " + lonfile);
			OutputStream outstr = dataSocket.getOutputStream();
			while ((n = sendfile.read(buff)) > 0) {
				outstr.write(buff, 0, n);
			}

			dataSocket.close();
			sendfile.close();
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
	}

	// execCommand方法
	// 执行与各命令相应的处理
	public boolean execCommand(String command) {
		boolean cont = true;
		switch (Integer.parseInt(command)) {
		case 1: // 显示服务器目录信息
			doLs();
			break;
		case 2: // 切换服务器的工作目录
			doCd();
			break;
		case 3: // 取得服务器上的文件
			doGet();
			break;
		case 4: // 向服务器发送文件
			doPut();
			break;
		case 5: // 文件传输模式
			doAscii();
			break;
		case 6: // 二进制传输模式
			doBinary();
			break;
		case 7: // 处理结束
			doQuit();
			cont = false;
			break;
		default: // 其他输入
			System.out.println("请选择一个序号");
		}
		return (cont);
	}

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

	// main_proc方法
	// 输出ftp的命令菜单，调用各种处理方法
	public void main_proc() throws IOException {
		boolean cont = true;
		try {
			doLogin();
			while (cont) {
				showMenu();
				cont = execCommand(getCommand());
			}
		} catch (Exception e) {
			System.err.print(e);
			System.exit(1);
		}
	}

	// main方法
	// 建立TCP连接，开始处理
	public static void main(String[] arg) {
		try {
			Ftp f = null;
			f = new Ftp();
			f.openConnection("127.0.0.1"); // 控制连接建立,设置为自己的IP.
			f.getMsgs(); // 启动接收线程
			f.main_proc(); // ftp 处理
			f.closeConnection(); // 关闭连接
			System.exit(0); // 结束程序
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
	}
}

// 读取控制流的CtrlListen 类
class CtrlListen implements Runnable {
	BufferedReader ctrlInput = null;

	public CtrlListen(BufferedReader in) {
		ctrlInput = in;
	}

	public void run() {
		while (true) {
			try {
				// 按行读入并输出到标准输出上
				System.out.println(ctrlInput.readLine());
			} catch (Exception e) {
				System.exit(1);
			}
		}
	}
}
