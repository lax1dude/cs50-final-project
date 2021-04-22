package net.eagtek.eagl.tools;

import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.ImageIcon;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import java.awt.Font;
import java.awt.event.ActionListener;
import java.io.File;
import java.net.URISyntaxException;
import java.awt.event.ActionEvent;

public class ToolsMain extends JFrame {
	
	private static final long serialVersionUID = 1L;
	
	private JPanel contentPane;

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
	 * Launch the application.
	 */
	public static void main(String[] args) throws ClassNotFoundException, InstantiationException, IllegalAccessException, UnsupportedLookAndFeelException {
		
		UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					ToolsMain frame = new ToolsMain();
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
		
	}

	/**
	 * Create the frame.
	 */
	public ToolsMain() {
		setAlwaysOnTop(true);
		setResizable(false);
		setTitle("eagl engine utils");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 382, 321);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);
		
		JButton btnNewButton = new JButton("Model Converter");
		btnNewButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				EventQueue.invokeLater(new Runnable() {
					public void run() {
						try {
							JFileChooser j = new JFileChooser();
							j.setCurrentDirectory(currentDir);
							int r = j.showOpenDialog(ToolsMain.this);
							if(r == JFileChooser.APPROVE_OPTION) {
								currentDir = j.getSelectedFile().getParentFile();
								ModelConverterWindow win = new ModelConverterWindow(ToolsMain.this, j.getSelectedFile());
								win.setVisible(true);
							}
						}catch(Throwable t) {
							JOptionPane.showMessageDialog(ToolsMain.this, t.toString(), "error", JOptionPane.ERROR_MESSAGE);
						}
					}
				});
			}
		});
		btnNewButton.setFont(new Font("Dialog", Font.BOLD, 17));
		btnNewButton.setHorizontalAlignment(SwingConstants.LEADING);
		btnNewButton.setIconTextGap(20);
		btnNewButton.setIcon(new ImageIcon(ToolsMain.class.getResource("/icons/modelconverter.png")));
		btnNewButton.setBounds(16, 11, 350, 82);
		contentPane.add(btnNewButton);
		
		JButton btnFontGenerator = new JButton("TrueType Font Converter");
		btnFontGenerator.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				EventQueue.invokeLater(new Runnable() {
					public void run() {
						try {
							JFileChooser j = new JFileChooser();
							j.setCurrentDirectory(currentDir);
							int r = j.showOpenDialog(ToolsMain.this);
							if(r == JFileChooser.APPROVE_OPTION) {
								currentDir = j.getSelectedFile().getParentFile();
								FontGeneratorWindow win = new FontGeneratorWindow(ToolsMain.this, j.getSelectedFile());
								win.setVisible(true);
							}
						}catch(Throwable t) {
							JOptionPane.showMessageDialog(ToolsMain.this, t.toString(), "error", JOptionPane.ERROR_MESSAGE);
						}
						
					}
				});
			}
		});
		btnFontGenerator.setIcon(new ImageIcon(ToolsMain.class.getResource("/icons/fontgenerator.png")));
		btnFontGenerator.setIconTextGap(20);
		btnFontGenerator.setHorizontalAlignment(SwingConstants.LEADING);
		btnFontGenerator.setFont(new Font("Dialog", Font.BOLD, 17));
		btnFontGenerator.setBounds(16, 104, 350, 82);
		contentPane.add(btnFontGenerator);
		
		JButton btnMaterialGenerator = new JButton("Material Generator");
		btnMaterialGenerator.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				EventQueue.invokeLater(new Runnable() {
					public void run() {
						MaterialConverterWindow win = new MaterialConverterWindow(ToolsMain.this);
						win.setVisible(true);
					}
				});
			}
		});
		btnMaterialGenerator.setIcon(new ImageIcon(ToolsMain.class.getResource("/icons/materialgen.png")));
		btnMaterialGenerator.setIconTextGap(20);
		btnMaterialGenerator.setHorizontalAlignment(SwingConstants.LEADING);
		btnMaterialGenerator.setFont(new Font("Dialog", Font.BOLD, 17));
		btnMaterialGenerator.setBounds(16, 197, 350, 82);
		contentPane.add(btnMaterialGenerator);
	}
}
