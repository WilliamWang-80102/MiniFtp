package cn.edu.whu.wzw.ftp;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.LinkedList;

public class MyFtp {
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
	
	public void doLogin(String loginName,String password) {
		try {
			ctrlOutput.println("USER " + loginName);
			ctrlOutput.flush();
			ctrlOutput.println("PASS " + password);
			ctrlOutput.flush();
		} catch (Exception e) {
			e.printStackTrace();
			System.err.println("doLogin异常！");
			System.exit(1);
		}
	}
	
    //默认构造函数
    public MyFtp(String url,String username,String password)
    {
    	try {
			this.openConnection(url);
			this.getMsgs(); // 启动接收线程
			this.doLogin(username, password);
		} catch (UnknownHostException e) {
			// TODO 自动生成的 catch 块
			e.printStackTrace();
		} catch (IOException e) {
			// TODO 自动生成的 catch 块
			e.printStackTrace();
		}
    }


    public void close_connection() {
        try {
            ctrlSocket.close();
        } catch (IOException e) {
            // TODO 自动生成的 catch 块
            System.err.println("close_connection异常！");
            e.printStackTrace();
        }
    }


    //获取所有文件和文件夹的名字

    public MyFtpFile[] getAllFile() {
        LinkedList<MyFtpFile> files = new LinkedList<>();
        //发送LIST指令，打印内容
        // 建立数据连接
        Socket dataSocket = this.dataConnection("LIST ");
        // 读取返回LIST数据
        String line;
        try {
            BufferedReader dataInput = new BufferedReader(new InputStreamReader(dataSocket.getInputStream(), "UTF-8"));
            while ((line = dataInput.readLine()) != null) {
                System.out.println(line);
                String[] sArray = line.split("\\s+", 0);//如果Windows文件夹名含有空格会出现问题
                MyFtpFile file = new MyFtpFile();
                //文件名
                file.setName(sArray[sArray.length - 1]);
                //文件类型
                if (sArray[0].startsWith("d")) {
                    file.setType(true);
                } else {
                    file.setType(false);
                }
                //文件大小
                file.setSize(Long.parseLong(sArray[4]));
                files.add(file);
            }
            dataSocket.close();
        } catch (UnsupportedEncodingException e) {
            // TODO 自动生成的 catch 块
            e.printStackTrace();
        } catch (IOException e) {
            // TODO 自动生成的 catch 块
            e.printStackTrace();
        }
        MyFtpFile[] filesRef = new MyFtpFile[files.size()];
        return files.toArray(filesRef);

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
            byte[] address = {127, 0, 0, 1};
            // 用适当的端口号构造服务器
            serverDataSocket = new ServerSocket(0, 1);
            // 准备传送PORT命令用的数据
            for (i = 0; i < 4; ++i)
                cmd = cmd + (address[i] & 0xff) + ",";
            cmd = cmd + (((serverDataSocket.getLocalPort()) / 256) & 0xff) + ","
                    + (serverDataSocket.getLocalPort() & 0xff);
            // 利用控制用的流传送PORT命令
            ctrlOutput.println(cmd);
            ctrlOutput.flush();
            // 向服务器发送处理对象命令(LIST,RETR,及STOR)
            ctrlcmd = new String(ctrlcmd);
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

    //下载 from_file_name是下载的文件名,to_path是下载到的路径地址
    public void download(boolean from_file_type, String from_file_name, String to_path, long fileSize) throws IOException {
        //如果是文件
        if (!from_file_type) {
            String fileName = from_file_name;
            String filePath = to_path + from_file_name;
            try {
                int n;
                byte[] buff = new byte[1024];
                File localFile = new File(filePath);
                FileOutputStream fileOutputStream;
                Socket socket;
                //检查是否下载过文件
                if (localFile.exists()) {
                    long localSize = localFile.length();
                    //本地文件大小大于远程文件，跳过下载
                    if (localSize >= fileSize) {
                    	return;
                    }
                    //断点续传
                    fileOutputStream = new FileOutputStream(localFile, true);
                    ctrlOutput.println("REST " + localSize);
                } else {
                    fileOutputStream = new FileOutputStream(localFile);
                }
                // 构造传输文件用的数据流
                socket = dataConnection("RETR " + fileName);
                BufferedInputStream bufferedInputStream = new BufferedInputStream(socket.getInputStream());
                // 接收来自服务器的数据，写入本地文件
                while ((n = bufferedInputStream.read(buff)) != -1) {
                	fileOutputStream.write(buff, 0, n);
                }
                //关闭数据套接字    
                socket.close();
                //关闭文件输出流
                fileOutputStream.close();
                return;
            } catch (Exception e) {
                e.printStackTrace();
                System.exit(1);
            }
        }
        
        //对文件夹递归处理
        File dir = new File(to_path + from_file_name);
        if (dir.mkdir()) {
            //更新当前目录
            ctrlOutput.println("CWD " + from_file_name);
            ctrlOutput.flush();
            //获取远程文件夹内的所有文件信息
            MyFtpFile[] files = getAllFile();
            for (MyFtpFile file : files) {
                this.download(file.getType(), file.getName(), dir.getPath() + File.separatorChar, file.getSize());
            }
            //返回上级目录
            ctrlOutput.println("CDUP ");
            ctrlOutput.flush();

        } else {
            System.err.println("mkdir error when download directory！");
            System.exit(1);
        }
    }
	
	public void upload(File file) throws IOException
    {
    	//如果上传的是文件夹
    	if(file.isDirectory()) 
    	{
    		//保存文件夹名
    		String fileName = file.getName();
    		try 
    		{
    			ctrlOutput.println("MKD "+fileName);
        		ctrlOutput.flush();
    		} 
    		catch (Exception e) 
    		{
    			e.printStackTrace();
    			System.exit(1);
    		}	
    		ctrlOutput.println("CWD "+fileName);
    		ctrlOutput.flush();
    		
    		String[] files = file.list(); 
    		for (int i = 0; i < files.length; i++) 
            {    
                File file1 = new File(file.getPath()+"\\"+files[i] );    
                upload(file1);  
            }
    		ctrlOutput.println("CDUP ");
    		ctrlOutput.flush();
    	}
    	else 
    	{
    		try {
    			int n;
    			byte[] buff = new byte[1024];
    			FileInputStream sendfile = null;
    			// 指定文件名
    			try {
    				sendfile = new FileInputStream(file.getPath());
    			} catch (Exception e) {
    				System.out.println("文件不存在");
    				return;
    			}
    			String lonfile = file.getName();
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
}

//读取控制流的CtrlListen 类
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

class MyFtpFile{
	//目录true，文件false
	private boolean type;
	private String name;
	private long size;
	
	public boolean getType() {
		return type;
	}
	public void setType(boolean type) {
		this.type = type;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public long getSize() {
		return size;
	}
	public void setSize(long size) {
		this.size = size;
	}
	
	public boolean isDirectory() {
		return this.getType();
	}
	
	public boolean isFile() {
		return !this.getType();
	}
}

