package net.eagtek.eagl.tools;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.nio.file.Files;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JSpinner;
import javax.swing.JCheckBox;
import javax.swing.SpinnerNumberModel;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

public class FontGeneratorWindow extends JDialog {
	
	private static final long serialVersionUID = -7628524683861118239L;
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
	/**
	 * Create the dialog.
	 */
	public FontGeneratorWindow(JFrame parent, File selectedFile) {
		super(parent);
		setResizable(false);
		setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		setTitle("font generator");
		setBounds(100, 100, 257, 218);
		getContentPane().setLayout(new BorderLayout());
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		contentPanel.setLayout(null);
		
		JLabel lblNewLabel = new JLabel("Font height, in pixels:");
		lblNewLabel.setBounds(22, 21, 128, 14);
		contentPanel.add(lblNewLabel);
		
		JSpinner spinner = new JSpinner();
		spinner.setModel(new SpinnerNumberModel(new Integer(12), new Integer(1), null, new Integer(1)));
		spinner.setBounds(134, 18, 49, 20);
		contentPanel.add(spinner);
		
		JCheckBox chckbxNewCheckBox = new JCheckBox("Bold");
		chckbxNewCheckBox.setBounds(22, 42, 55, 23);
		contentPanel.add(chckbxNewCheckBox);
		
		JCheckBox chckbxNewCheckBox_1 = new JCheckBox("Italic");
		chckbxNewCheckBox_1.setBounds(79, 42, 62, 23);
		contentPanel.add(chckbxNewCheckBox_1);
		
		JCheckBox chckbxNewCheckBox_2 = new JCheckBox("Render Unicode Pages");
		chckbxNewCheckBox_2.setBounds(22, 77, 161, 23);
		contentPanel.add(chckbxNewCheckBox_2);
		{
			JPanel buttonPane = new JPanel();
			buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
			getContentPane().add(buttonPane, BorderLayout.SOUTH);
			{
				JButton okButton = new JButton("OK");
				okButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						try {
							JFileChooser j = new JFileChooser();
							j.setCurrentDirectory(currentDir);
							int r = j.showSaveDialog(FontGeneratorWindow.this);
							if(r == JFileChooser.APPROVE_OPTION) {
								currentDir = j.getSelectedFile().getParentFile();
								FontFileGenerator.generateFontFile(selectedFile, j.getSelectedFile(), ((Integer)spinner.getValue()).intValue(), chckbxNewCheckBox.isSelected(), chckbxNewCheckBox_1.isSelected(), chckbxNewCheckBox_2.isSelected());
								FontGeneratorWindow.this.dispose();
							}
						}catch(Throwable t) {
							t.printStackTrace();
							JOptionPane.showMessageDialog(FontGeneratorWindow.this, t.toString(), "error", JOptionPane.ERROR_MESSAGE);
						}
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
						FontGeneratorWindow.this.dispose();
					}
				});
				cancelButton.setActionCommand("Cancel");
				buttonPane.add(cancelButton);
			}
		}
	}
}
