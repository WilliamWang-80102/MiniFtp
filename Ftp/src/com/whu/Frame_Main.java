package com.whu;
import java.awt.EventQueue;
import javax.swing.JFrame;
import javax.swing.JTable;
import javax.swing.JFileChooser;
import javax.swing.JScrollPane;
import javax.swing.JButton;
import javax.swing.JLabel;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.Color;
import java.awt.Font;
import javax.swing.UIManager;
import javax.swing.table.DefaultTableModel;
import javax.swing.border.LineBorder;
import javax.swing.filechooser.FileSystemView;
import org.apache.commons.net.ftp.FTPFile;
import java.io.File;
import java.io.IOException;

public class Frame_Main implements ActionListener
{ 
    //初始化参数--------------------------------
    static FTPFile[] file;
    static String FTP="10.132.87.137";
    static String username="WHU-SXT";
    static String password="18371235768";
    //初始化参数--------------------------------
    
    
    private JFrame frame;
    private JTable table;
    static Ftp_by_apache ftp;
    public static Ftp_by_apache getFtp() 
    {
        return ftp;
    }
    
    
    //程序入口
    public static void main(String[] args) 
    {
         ftp=new Ftp_by_apache(FTP,username,password);
         file=ftp.getAllFile();
        
        EventQueue.invokeLater(new Runnable() 
        {
            public void run() 
            {
                try 
                {
                    Frame_Main window = new Frame_Main();
                    window.frame.setVisible(true);
                } 
                catch (Exception e) 
                {
                    e.printStackTrace();
                }
            }
        }
        );   
    }

    
    //图形用户界面
    public Frame_Main() 
    {
        initialize();
    }

    //界面具体实现
    private void initialize() 
    {
        frame = new JFrame();
       // frame.setIconImage(Toolkit.getDefaultToolkit().getImage(Frame_Main.class.getResource("/com/sun/java/swing/plaf/windows/icons/UpFolder.gif")));
        frame.setTitle("FTP");
        frame.setBounds(100, 100, 470, 534);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.getContentPane().setLayout(null);
        
        //上传按钮--------------------------------------------------
        JButton upload = new JButton("上传");
        upload.setFont(new Font("宋体", Font.PLAIN, 12));
        upload.setBackground(UIManager.getColor("Button.highlight"));
        upload.addActionListener(new ActionListener() 
        {
            public void actionPerformed(ActionEvent arg0) 
            {
                //上传点击按钮触发------------------------------------
                System.out.println("上传！！！！！");
                int result = 0;  
                @SuppressWarnings("unused")
				File file = null;  
                String path = null;  
                JFileChooser fileChooser = new JFileChooser();  
                FileSystemView fsv = FileSystemView.getFileSystemView(); 
                System.out.println(fsv.getHomeDirectory());                //得到桌面路径  
                fileChooser.setCurrentDirectory(fsv.getHomeDirectory());  
                fileChooser.setDialogTitle("请选择要上传的文件...");  
                fileChooser.setApproveButtonText("确定");  
                fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);  
                result = fileChooser.showOpenDialog(null);  
                if (JFileChooser.APPROVE_OPTION == result)
                {  
                    path=fileChooser.getSelectedFile().getPath();  
                    System.out.println("path: "+path);
                    try 
                    {
                        //下载
                        ftp.upload(path);
                    } 
                    catch (IOException e1) 
                    {
                        e1.printStackTrace();
                    }
                    finally
                    {    
                        ftp.close_connection();
                    }
                } 
                //上传点击按钮触发------------------------------------
            }
        });
        upload.setBounds(195, 15, 82, 23);
        frame.getContentPane().add(upload);
        //上传按钮--------------------------------------------------
        
        
        
        //刷新按钮--------------------------------------------------
        JButton refresh = new JButton("刷新");
        //匿名内部类
        refresh.addActionListener(new ActionListener() 
        {
            public void actionPerformed(ActionEvent arg0) 
            {
            }
        }
        );
        refresh.setFont(new Font("宋体", Font.PLAIN, 12));
        refresh.setBackground(UIManager.getColor("Button.highlight"));
        refresh.setBounds(312, 15, 82, 23);
        //添加刷新按钮
        frame.getContentPane().add(refresh);
        //刷新按钮--------------------------------------------------
        
        
        
        //显示基本信息(FTP和username)-------------------------------
        JLabel lblNewLabel = new JLabel("FTP地址");
        lblNewLabel.setBounds(32, 10, 54, 15);
        frame.getContentPane().add(lblNewLabel);
        
        JLabel lblNewLabel_1 = new JLabel("用户名");
        lblNewLabel_1.setBounds(32, 35, 54, 15);
        frame.getContentPane().add(lblNewLabel_1);
        
        JLabel address = new JLabel(FTP);
        address.setBounds(110, 10, 75, 15);
        frame.getContentPane().add(address);
        
        JLabel name = new JLabel(username);
        name.setBounds(110, 35, 82, 15);
        frame.getContentPane().add(name);
        //显示基本信息-----------------------------------------------
        
        
        //表格数据
        String[][] data1=new String[file.length][4];
         for(int row=0;row<file.length;row++)
         {
             
                 data1[row][0]=file[row].getName();
                 if(file[row].isDirectory())
                    {
                     	data1[row][1]="文件夹";
                    }
                    else if(file[row].isFile())
                    {
                        String[] geshi=file[row].getName().split("\\.");
                        data1[row][1]=geshi[1];    
                    }
                 data1[row][2]=file[row].getSize()+"";
                 data1[row][3]="下载";
         } 
        //表格列名
        String[] columnNames = {"文件名称", "文件类型", "文件字节", "文件下载"}; 
        //表格模型
        DefaultTableModel model = new DefaultTableModel();
        model.setDataVector(data1, columnNames);
        
        
        //加滚动条--------------------------------------------------------
        JScrollPane scrollPane = new JScrollPane();
        scrollPane.setBounds(32, 73, 362, 384);
        frame.getContentPane().add(scrollPane);
        //加滚动条--------------------------------------------------------
        
          
     
        //根据表格模型建表
        table = new JTable(model);
        scrollPane.setViewportView(table);
        table.setColumnSelectionAllowed(true);
        table.setCellSelectionEnabled(true);
        table.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        table.setBorder(new LineBorder(new Color(0, 0, 0)));
        table.setToolTipText("可以上传下载文件");
        //表格最后一列按钮------------------------------------------------    
        @SuppressWarnings("unused")
		ButtonColumn buttonsColumn = new ButtonColumn(table, 3);
    }

    @Override
    public void actionPerformed(ActionEvent arg0) 
    {
  
    }
}