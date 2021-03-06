package cn.edu.whu.wzw.ftp;

import java.awt.Component;  
import java.awt.event.ActionEvent;  
import java.awt.event.ActionListener;  
import java.io.File;
import java.io.IOException;
  
import javax.swing.AbstractCellEditor;  
import javax.swing.JButton;  
import javax.swing.JFileChooser;
import javax.swing.JTable;  
import javax.swing.UIManager;  
import javax.swing.table.TableCellEditor;  
import javax.swing.table.TableCellRenderer;  
import javax.swing.table.TableColumnModel;
  
public class ButtonColumn extends AbstractCellEditor implements  
        TableCellRenderer, TableCellEditor, ActionListener {  
    JTable table;  
    JButton renderButton;  
    JButton editButton;  
    String text;  
  
    public ButtonColumn(JTable table, int column) {  
        super();  
        this.table = table;  
        renderButton = new JButton();  
        editButton = new JButton();  
        editButton.setFocusPainted(false);  
        editButton.addActionListener(this);  
  
        TableColumnModel columnModel = table.getColumnModel();  
        columnModel.getColumn(column).setCellRenderer(this);  
        columnModel.getColumn(column).setCellEditor(this);  
    }  
  
    @Override
	public Component getTableCellRendererComponent(JTable table, Object value,  
            boolean isSelected, boolean hasFocus, int row, int column) {  
        if (hasFocus) {  
            renderButton.setForeground(table.getForeground());  
            renderButton.setBackground(UIManager.getColor("Button.background"));  
        } else if (isSelected) {  
            renderButton.setForeground(table.getSelectionForeground());  
            renderButton.setBackground(table.getSelectionBackground());  
        } else {  
            renderButton.setForeground(table.getForeground());  
            renderButton.setBackground(UIManager.getColor("Button.background"));  
        }  
  
        renderButton.setText((value == null) ? " " : value.toString());  
        return renderButton;  
    }  
  
    @Override
	public Component getTableCellEditorComponent(JTable table, Object value,  
            boolean isSelected, int row, int column) {  
        text = (value == null) ? " " : value.toString();  
        editButton.setText(text);  
        return editButton;  
    }  
  
    @Override
	public Object getCellEditorValue() {  
        return text;  
    }  
  
    @Override
	public void actionPerformed(ActionEvent e) {  
        fireEditingStopped();  
        MyFtpFile[]  file1=Frame_Main.getFtp().getAllFile();
        String from_file_name=file1[table.getSelectedRow()].getName();
        boolean from_file_type=file1[table.getSelectedRow()].getType();
        long fileSize=file1[table.getSelectedRow()].getSize();
        int result = 0;  
        String path = null;  
        JFileChooser fileChooser = new JFileChooser();  
        fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        fileChooser.setDialogTitle("另存为:");  
        result = fileChooser.showSaveDialog(null);
        if (JFileChooser.APPROVE_OPTION == result) {  
            path=fileChooser.getSelectedFile().getPath()+"\\"; //加"\\"是为了防止在桌面的时候C:destop最后没有\ 
            System.out.println("path: "+path);
            System.out.println("from_file_name:"+from_file_name);
            try {
                Frame_Main.getFtp().download(from_file_type,from_file_name, path,fileSize);
                System.out.println("下载成功! ");

            } catch (IOException e1) {
                e1.printStackTrace();
            }
            
        } 
    }  

}
