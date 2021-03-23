package net.eagtek.eagl.tools;

import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.nio.file.Files;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.JCheckBox;
import javax.swing.JOptionPane;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

public class ModelConverterWindow extends JDialog {
	
	private static final long serialVersionUID = 1L;
	
	private final JPanel contentPanel = new JPanel();
	
	private static File currentDir;
	
	static {
		try {
			currentDir = new File(ToolsMain.class.getProtectionDomain().getCodeSource().getLocation().toURI());
		} catch (URISyntaxException e) {
			e.printStackTrace();
			currentDir = null;
		}
	}
	
	private File file;

	/**
	 * Create the dialog.
	 * @param file 
	 */
	public ModelConverterWindow(Frame c, File file) {
		super(c);
		this.file = file;
		setResizable(false);
		setTitle("model converter");
		setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		setBounds(100, 100, 321, 168);
		getContentPane().setLayout(new BorderLayout());
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		contentPanel.setLayout(null);
		
		JCheckBox chckbxNewCheckBox = new JCheckBox("export normals");
		chckbxNewCheckBox.setSelected(true);
		chckbxNewCheckBox.setBounds(165, 17, 142, 23);
		contentPanel.add(chckbxNewCheckBox);
		
		JCheckBox chckbxExportTexcoords = new JCheckBox("export texcoords");
		chckbxExportTexcoords.setSelected(true);
		chckbxExportTexcoords.setBounds(21, 17, 142, 23);
		contentPanel.add(chckbxExportTexcoords);
		
		JCheckBox chckbxNewCheckBox_1 = new JCheckBox("generate index buffer");
		chckbxNewCheckBox_1.setSelected(true);
		chckbxNewCheckBox_1.setBounds(21, 43, 142, 23);
		contentPanel.add(chckbxNewCheckBox_1);
		
		JCheckBox chckbxNewCheckBox_2 = new JCheckBox("compress data");
		chckbxNewCheckBox_2.setSelected(true);
		chckbxNewCheckBox_2.setBounds(21, 69, 142, 23);
		contentPanel.add(chckbxNewCheckBox_2);
		
		JCheckBox chckbxNewCheckBox_3 = new JCheckBox("32bit indexes");
		chckbxNewCheckBox_3.setBounds(165, 43, 140, 23);
		contentPanel.add(chckbxNewCheckBox_3);
		{
			JPanel buttonPane = new JPanel();
			buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
			getContentPane().add(buttonPane, BorderLayout.SOUTH);
			{
				JButton okButton = new JButton("Save");
				okButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						EventQueue.invokeLater(new Runnable() {
							public void run() {
								try {
									JFileChooser j = new JFileChooser();
									j.setCurrentDirectory(currentDir);
									int r = j.showSaveDialog(ModelConverterWindow.this);
									if(r == JFileChooser.APPROVE_OPTION) {
										currentDir = j.getSelectedFile().getParentFile();
										OutputStream fos = new FileOutputStream(j.getSelectedFile());
										try {
											OBJConverter.convertModel(new String(Files.readAllBytes(ModelConverterWindow.this.file.toPath()), Charset.forName("UTF8")), chckbxNewCheckBox_1.isSelected(),
													chckbxExportTexcoords.isSelected(), chckbxNewCheckBox.isSelected(), chckbxNewCheckBox_2.isSelected(), chckbxNewCheckBox_3.isSelected(), fos);
										}catch(Throwable t) {
											JOptionPane.showMessageDialog(ModelConverterWindow.this, t.toString(), "error", JOptionPane.ERROR_MESSAGE);
										}
										fos.close();
										ModelConverterWindow.this.dispose();
									}
								}catch(Throwable t) {
									JOptionPane.showMessageDialog(ModelConverterWindow.this, t.toString(), "error", JOptionPane.ERROR_MESSAGE);
								}
							}
						});
					}
				});
				okButton.setActionCommand("OK");
				buttonPane.add(okButton);
				getRootPane().setDefaultButton(okButton);
			}
			{
				JButton cancelButton = new JButton("Cancel");
				cancelButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						ModelConverterWindow.this.dispose();
					}
				});
				cancelButton.setActionCommand("Cancel");
				buttonPane.add(cancelButton);
			}
		}
	}
}
