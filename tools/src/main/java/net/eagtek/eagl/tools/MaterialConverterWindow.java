package net.eagtek.eagl.tools;

import java.awt.BorderLayout;
import java.awt.FlowLayout;

import javax.imageio.ImageIO;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.JLabel;
import java.awt.Color;
import javax.swing.UIManager;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import java.awt.Insets;
import java.awt.RenderingHints;

import org.lwjgl.system.MemoryUtil;
import org.lwjgl.util.lz4.LZ4;

import java.awt.Font;
import java.awt.Graphics2D;

import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.JMenuBar;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;

import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.awt.event.ActionEvent;
import javax.swing.JComboBox;
import javax.swing.DefaultComboBoxModel;

public class MaterialConverterWindow extends JDialog {
	
	private static final long serialVersionUID = -8080920006412761389L;
	
	private JTextField textField;
	private JTextField textField_1;
	private JTextField textField_2;
	private JTextField textField_3;
	private JTextField textField_4;
	private JTextField textField_5;
	private JTextField textField_6;
	private JTextField textField_8;
	private JRadioButton rdbtnNewRadioButton;
	private JRadioButton rdbtnConstantValue;
	private JPanel panel_2;
	private JButton btnNewButton;
	private JButton btnPickColor;
	private JRadioButton rdbtnNewRadioButton_1;
	private JRadioButton rdbtnNoNormalMap;
	private JButton btnNewButton_1;
	private JRadioButton rdbtnNewRadioButton_2;
	private JRadioButton rdbtnConstantValue_1;
	private JSpinner spinner;
	private JButton btnNewButton_2;
	private JRadioButton rdbtnNewRadioButton_2_1_1_2;
	private JRadioButton rdbtnConstantValue_1_1_1_2;
	private JSpinner spinner_1_1_2;
	private JButton btnNewButton_2_1_1_2;
	private JRadioButton rdbtnNewRadioButton_2_1;
	private JButton btnNewButton_2_1;
	private JRadioButton rdbtnConstantValue_1_1;
	private JSpinner spinner_1;
	private JRadioButton rdbtnNewRadioButton_2_1_1;
	private JButton btnNewButton_2_1_1;
	private JRadioButton rdbtnConstantValue_1_1_1;
	private JSpinner spinner_1_1;
	private JRadioButton rdbtnNewRadioButton_2_1_1_1;
	private JButton btnNewButton_2_1_1_1;
	private JRadioButton rdbtnConstantValue_1_1_1_1;
	private JSpinner spinner_1_1_1;
	private JRadioButton rdbtnNewRadioButton_1_1;
	private JRadioButton rdbtnNoDisplacementMap;
	private JButton btnNewButton_1_1;

	private static File currentDir = new File(".");
	private static File currentCC0Dir = new File(".");
	private JComboBox comboBox;
	
	private static BufferedImage scale(BufferedImage img, int w, int h) {
		BufferedImage p = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
		Graphics2D g2d = p.createGraphics();
		g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION,RenderingHints.VALUE_INTERPOLATION_BILINEAR);
		g2d.setRenderingHint(RenderingHints.KEY_RENDERING,RenderingHints.VALUE_RENDER_QUALITY);
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
		g2d.drawImage(img, 0, 0, w, h, 0, 0, img.getWidth(), img.getHeight(), null);
		g2d.dispose();
		return p;
	}

	/**
	 * Create the dialog.
	 */
	public MaterialConverterWindow(ToolsMain toolsMain) {
		super(toolsMain);
		setResizable(false);
		setTitle("material converter");
		setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		setBounds(100, 100, 845, 542);
		getContentPane().setLayout(new BorderLayout());
		{
			JPanel buttonPane = new JPanel();
			buttonPane.setBackground(UIManager.getColor("Button.light"));
			buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
			getContentPane().add(buttonPane, BorderLayout.SOUTH);
			{
				JButton okButton = new JButton("Export");
				okButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						EventQueue.invokeLater(new Runnable() {
							public void run() {
								try {
									JFileChooser jk = new JFileChooser();
									jk.setCurrentDirectory(currentDir);
									int r = jk.showSaveDialog(MaterialConverterWindow.this);
									if(r == JFileChooser.APPROVE_OPTION) {
										currentDir = jk.getSelectedFile().getParentFile();
										int wh = Integer.parseInt(comboBox.getSelectedItem().toString());
										
										BufferedImage diffuse, normal, roughness, clearcoat, metalness, specular, emission, displacement;
										diffuse = emission = normal = displacement = metalness = roughness = specular = clearcoat = null;
			
										if(rdbtnNewRadioButton.isSelected()) {
											diffuse = scale(ImageIO.read(new File(textField.getText())), wh, wh);
										}
										if(rdbtnNewRadioButton_1.isSelected()) {
											normal = scale(ImageIO.read(new File(textField_1.getText())), wh, wh);
										}
										if(rdbtnNewRadioButton_2.isSelected()) {
											roughness = scale(ImageIO.read(new File(textField_2.getText())), wh, wh);
										}
										if(rdbtnNewRadioButton_2_1_1_2.isSelected()) {
											clearcoat = scale(ImageIO.read(new File(textField_6.getText())), wh, wh);
										}
										if(rdbtnNewRadioButton_2_1.isSelected()) {
											metalness = scale(ImageIO.read(new File(textField_3.getText())), wh, wh);
										}
										if(rdbtnNewRadioButton_2_1_1.isSelected()) {
											specular = scale(ImageIO.read(new File(textField_4.getText())), wh, wh);
										}
										if(rdbtnNewRadioButton_2_1_1_1.isSelected()) {
											emission = scale(ImageIO.read(new File(textField_5.getText())), wh, wh);
										}
										if(rdbtnNewRadioButton_1_1.isSelected()) {
											displacement = scale(ImageIO.read(new File(textField_8.getText())), wh, wh);
										}
										
										ByteBuffer pageA = MemoryUtil.memAlloc(wh * wh * 4);
										ByteBuffer pageB = MemoryUtil.memAlloc(wh * wh * 4);
										ByteBuffer pageC = MemoryUtil.memAlloc(wh * wh * 4);
										
										for(int i = 0; i < wh*wh; ++i) {
											
											int diffuseR;
											int diffuseG;
											int diffuseB;
											if(diffuse == null) {
												Color c = panel_2.getBackground();
												diffuseR = c.getRed();
												diffuseG = c.getGreen();
												diffuseB = c.getBlue();
											}else {
												int j = diffuse.getRGB(i % wh, i / wh);
												diffuseR = (byte)(j >> 16);
												diffuseG = (byte)(j >> 8);
												diffuseB = (byte)(j);
											}
											
											int emissionA;
											if(emission == null) {
												emissionA = ((Integer)spinner_1_1_1.getValue()).intValue();
											}else {
												int j = emission.getRGB(i % wh, i / wh);
												emissionA = (((j >> 16) & 255) + ((j >> 8) & 255) + (j & 255)) / 3;
											}
											
											int normalR;
											int normalG;
											int normalB;
											if(normal == null) {
												normalR = 128;
												normalG = 128;
												normalB = 255;
											}else {
												int j = normal.getRGB(i % wh, i / wh);
												normalR = (byte)(j >> 16);
												normalG = (byte)(j >> 8);
												normalB = (byte)(j);
											}
											
											int displacementA;
											if(displacement == null) {
												displacementA = 0;
											}else {
												int j = displacement.getRGB(i % wh, i / wh);
												displacementA = (((j >> 16) & 255) + ((j >> 8) & 255) + (j & 255)) / 3;
											}
											
											int metalnessR;
											if(metalness == null) {
												metalnessR = ((Integer)spinner_1.getValue()).intValue();
											}else {
												int j = metalness.getRGB(i % wh, i / wh);
												metalnessR = (((j >> 16) & 255) + ((j >> 8) & 255) + (j & 255)) / 3;
											}
											
											int roughnessG;
											if(roughness == null) {
												roughnessG = ((Integer)spinner.getValue()).intValue();
											}else {
												int j = roughness.getRGB(i % wh, i / wh);
												roughnessG = (((j >> 16) & 255) + ((j >> 8) & 255) + (j & 255)) / 3;
											}
											
											int specularB;
											if(specular == null) {
												specularB = ((Integer)spinner_1_1.getValue()).intValue();
											}else {
												int j = specular.getRGB(i % wh, i / wh);
												specularB = (((j >> 16) & 255) + ((j >> 8) & 255) + (j & 255)) / 3;
											}
											
											int clearcoatA;
											if(clearcoat == null) {
												clearcoatA = ((Integer)spinner_1_1_2.getValue()).intValue();
											}else {
												int j = clearcoat.getRGB(i % wh, i / wh);
												clearcoatA = (((j >> 16) & 255) + ((j >> 8) & 255) + (j & 255)) / 3;
											}
			
											pageA.put((byte)diffuseR);
											pageA.put((byte)diffuseG);
											pageA.put((byte)diffuseB);
											pageA.put((byte)emissionA);
											
											pageB.put((byte)normalR);
											pageB.put((byte)normalG);
											pageB.put((byte)normalB);
											pageB.put((byte)displacementA);
											
											pageC.put((byte)metalnessR);
											pageC.put((byte)roughnessG);
											pageC.put((byte)specularB);
											pageC.put((byte)clearcoatA);
											
										}
										
										pageA.flip();
										pageB.flip();
										pageC.flip();
										
										OutputStream o = new FileOutputStream(jk.getSelectedFile());
										MaterialConverterHelper.mipmapAndCompress(wh, pageA, pageB, pageC, o);
										o.close();

										MemoryUtil.memFree(pageA);
										MemoryUtil.memFree(pageB);
										MemoryUtil.memFree(pageC);
										
										JOptionPane.showMessageDialog(MaterialConverterWindow.this, "export complete");
									}
								}catch(Throwable t) {
									t.printStackTrace();
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
				JButton cancelButton = new JButton("Close");
				cancelButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						MaterialConverterWindow.this.dispose();
					}
				});
				cancelButton.setActionCommand("Cancel");
				buttonPane.add(cancelButton);
			}
		}
		
		JPanel panel = new JPanel();
		panel.setBackground(UIManager.getColor("Button.light"));
		getContentPane().add(panel, BorderLayout.CENTER);
		panel.setLayout(null);
		
		JPanel panel_1 = new JPanel();
		panel_1.setBounds(10, 11, 394, 91);
		panel.add(panel_1);
		panel_1.setLayout(null);
		
		ButtonGroup bga = new ButtonGroup();
		
		rdbtnNewRadioButton = new JRadioButton("read image file ");
		rdbtnNewRadioButton.setBounds(10, 33, 109, 23);
		panel_1.add(rdbtnNewRadioButton);
		bga.add(rdbtnNewRadioButton);
		
		JLabel lblNewLabel = new JLabel("Diffuse");
		lblNewLabel.setFont(new Font("Dialog", lblNewLabel.getFont().getStyle(), 17));
		lblNewLabel.setBounds(10, 9, 133, 20);
		panel_1.add(lblNewLabel);
		
		rdbtnConstantValue = new JRadioButton("constant value");
		rdbtnConstantValue.setSelected(true);
		rdbtnConstantValue.setBounds(10, 59, 109, 23);
		panel_1.add(rdbtnConstantValue);
		bga.add(rdbtnConstantValue);
		
		textField = new JTextField();
		textField.setMargin(new Insets(0, 2, 0, 2));
		textField.setBounds(123, 34, 167, 20);
		panel_1.add(textField);
		textField.setColumns(10);
		
		btnNewButton = new JButton("Browse...");
		btnNewButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JFileChooser j = new JFileChooser();
				j.setCurrentDirectory(currentDir);
				int r = j.showOpenDialog(MaterialConverterWindow.this);
				if(r == JFileChooser.APPROVE_OPTION) {
					currentDir = j.getSelectedFile().getParentFile();
					textField.setText(j.getSelectedFile().toString());
				}
					
			}
		});
		btnNewButton.setMargin(new Insets(0, 0, 0, 0));
		btnNewButton.setBounds(291, 33, 89, 22);
		panel_1.add(btnNewButton);
		
		panel_2 = new JPanel();
		panel_2.setBackground(new Color(255, 255, 255));
		panel_2.setBorder(UIManager.getBorder("TextField.border"));
		panel_2.setBounds(123, 59, 115, 20);
		panel_1.add(panel_2);
		
		btnPickColor = new JButton("Pick New Color");
		btnPickColor.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				panel_2.setBackground(JColorChooser.showDialog(MaterialConverterWindow.this, "choose diffuse color", new Color(255, 255, 255)));
			}
		});
		btnPickColor.setMargin(new Insets(0, 0, 0, 0));
		btnPickColor.setBounds(240, 58, 140, 22);
		panel_1.add(btnPickColor);
		
		JPanel panel_1_1 = new JPanel();
		panel_1_1.setLayout(null);
		panel_1_1.setBounds(10, 113, 394, 91);
		panel.add(panel_1_1);
		
		ButtonGroup bgb = new ButtonGroup();
		rdbtnNewRadioButton_1 = new JRadioButton("read image file ");
		rdbtnNewRadioButton_1.setBounds(10, 33, 109, 23);
		panel_1_1.add(rdbtnNewRadioButton_1);
		bgb.add(rdbtnNewRadioButton_1);
		
		rdbtnNoNormalMap = new JRadioButton("no normal map");
		rdbtnNoNormalMap.setSelected(true);
		rdbtnNoNormalMap.setBounds(10, 59, 109, 23);
		panel_1_1.add(rdbtnNoNormalMap);
		bgb.add(rdbtnNoNormalMap);
		
		textField_1 = new JTextField();
		textField_1.setMargin(new Insets(0, 2, 0, 2));
		textField_1.setColumns(10);
		textField_1.setBounds(123, 34, 167, 20);
		panel_1_1.add(textField_1);
		
		btnNewButton_1 = new JButton("Browse...");
		btnNewButton_1.setMargin(new Insets(0, 0, 0, 0));
		btnNewButton_1.setBounds(291, 33, 89, 22);
		btnNewButton_1.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JFileChooser j = new JFileChooser();
				j.setCurrentDirectory(currentDir);
				int r = j.showOpenDialog(MaterialConverterWindow.this);
				if(r == JFileChooser.APPROVE_OPTION) {
					currentDir = j.getSelectedFile().getParentFile();
					textField_1.setText(j.getSelectedFile().toString());
				}
					
			}
		});
		panel_1_1.add(btnNewButton_1);
		
		JLabel lblNormal = new JLabel("Normal");
		lblNormal.setFont(new Font("Dialog", Font.PLAIN, 17));
		lblNormal.setBounds(10, 9, 133, 20);
		panel_1_1.add(lblNormal);
		
		JPanel panel_1_2 = new JPanel();
		panel_1_2.setLayout(null);
		panel_1_2.setBounds(10, 215, 394, 91);
		panel.add(panel_1_2);
		
		ButtonGroup bgc = new ButtonGroup();
		
		rdbtnNewRadioButton_2 = new JRadioButton("read image file ");
		rdbtnNewRadioButton_2.setBounds(10, 33, 109, 23);
		panel_1_2.add(rdbtnNewRadioButton_2);
		bgc.add(rdbtnNewRadioButton_2);
		
		JLabel lblRoughness = new JLabel("Roughness");
		lblRoughness.setFont(new Font("Dialog", Font.PLAIN, 17));
		lblRoughness.setBounds(10, 9, 133, 20);
		panel_1_2.add(lblRoughness);
		
		rdbtnConstantValue_1 = new JRadioButton("constant value");
		rdbtnConstantValue_1.setSelected(true);
		rdbtnConstantValue_1.setBounds(10, 59, 109, 23);
		panel_1_2.add(rdbtnConstantValue_1);
		bgc.add(rdbtnConstantValue_1);
		
		textField_2 = new JTextField();
		textField_2.setMargin(new Insets(0, 2, 0, 2));
		textField_2.setColumns(10);
		textField_2.setBounds(123, 34, 167, 20);
		panel_1_2.add(textField_2);
		
		btnNewButton_2 = new JButton("Browse...");
		btnNewButton_2.setMargin(new Insets(0, 0, 0, 0));
		btnNewButton_2.setBounds(291, 33, 89, 22);
		btnNewButton_2.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JFileChooser j = new JFileChooser();
				j.setCurrentDirectory(currentDir);
				int r = j.showOpenDialog(MaterialConverterWindow.this);
				if(r == JFileChooser.APPROVE_OPTION) {
					currentDir = j.getSelectedFile().getParentFile();
					textField_2.setText(j.getSelectedFile().toString());
				}
					
			}
		});
		panel_1_2.add(btnNewButton_2);
		
		spinner = new JSpinner();
		spinner.setModel(new SpinnerNumberModel(127, 0, 255, 10));
		spinner.setBounds(123, 60, 60, 20);
		panel_1_2.add(spinner);
		
		JPanel panel_1_2_1 = new JPanel();
		panel_1_2_1.setLayout(null);
		panel_1_2_1.setBounds(414, 11, 394, 91);
		panel.add(panel_1_2_1);
		
		ButtonGroup bgd = new ButtonGroup();
		rdbtnNewRadioButton_2_1 = new JRadioButton("read image file ");
		rdbtnNewRadioButton_2_1.setBounds(10, 33, 109, 23);
		panel_1_2_1.add(rdbtnNewRadioButton_2_1);
		bgd.add(rdbtnNewRadioButton_2_1);
		
		JLabel lblMetalness = new JLabel("Metalness");
		lblMetalness.setFont(new Font("Dialog", Font.PLAIN, 17));
		lblMetalness.setBounds(10, 9, 133, 20);
		panel_1_2_1.add(lblMetalness);
		
		rdbtnConstantValue_1_1 = new JRadioButton("constant value");
		rdbtnConstantValue_1_1.setSelected(true);
		rdbtnConstantValue_1_1.setBounds(10, 59, 109, 23);
		panel_1_2_1.add(rdbtnConstantValue_1_1);
		bgd.add(rdbtnConstantValue_1_1);
		textField_3 = new JTextField();
		textField_3.setMargin(new Insets(0, 2, 0, 2));
		textField_3.setColumns(10);
		textField_3.setBounds(123, 34, 167, 20);
		panel_1_2_1.add(textField_3);
		
		btnNewButton_2_1 = new JButton("Browse...");
		btnNewButton_2_1.setMargin(new Insets(0, 0, 0, 0));
		btnNewButton_2_1.setBounds(291, 33, 89, 22);
		btnNewButton_2_1.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JFileChooser j = new JFileChooser();
				j.setCurrentDirectory(currentDir);
				int r = j.showOpenDialog(MaterialConverterWindow.this);
				if(r == JFileChooser.APPROVE_OPTION) {
					currentDir = j.getSelectedFile().getParentFile();
					textField_3.setText(j.getSelectedFile().toString());
				}
					
			}
		});
		panel_1_2_1.add(btnNewButton_2_1);
		
		spinner_1 = new JSpinner();
		spinner_1.setModel(new SpinnerNumberModel(0, 0, 255, 10));
		spinner_1.setBounds(123, 60, 60, 20);
		panel_1_2_1.add(spinner_1);
		
		JPanel panel_1_2_1_1 = new JPanel();
		panel_1_2_1_1.setLayout(null);
		panel_1_2_1_1.setBounds(414, 113, 394, 91);
		panel.add(panel_1_2_1_1);
		
		ButtonGroup bge = new ButtonGroup();
		rdbtnNewRadioButton_2_1_1 = new JRadioButton("read image file ");
		rdbtnNewRadioButton_2_1_1.setBounds(10, 33, 109, 23);
		panel_1_2_1_1.add(rdbtnNewRadioButton_2_1_1);
		bge.add(rdbtnNewRadioButton_2_1_1);
		
		JLabel lblSpecularIntensity = new JLabel("Specular Intensity");
		lblSpecularIntensity.setFont(new Font("Dialog", Font.PLAIN, 17));
		lblSpecularIntensity.setBounds(10, 9, 143, 20);
		panel_1_2_1_1.add(lblSpecularIntensity);
		
		rdbtnConstantValue_1_1_1 = new JRadioButton("constant value");
		rdbtnConstantValue_1_1_1.setSelected(true);
		rdbtnConstantValue_1_1_1.setBounds(10, 59, 109, 23);
		panel_1_2_1_1.add(rdbtnConstantValue_1_1_1);
		bge.add(rdbtnConstantValue_1_1_1);
		
		textField_4 = new JTextField();
		textField_4.setMargin(new Insets(0, 2, 0, 2));
		textField_4.setColumns(10);
		textField_4.setBounds(123, 34, 167, 20);
		panel_1_2_1_1.add(textField_4);
		
		btnNewButton_2_1_1 = new JButton("Browse...");
		btnNewButton_2_1_1.setMargin(new Insets(0, 0, 0, 0));
		btnNewButton_2_1_1.setBounds(291, 33, 89, 22);
		btnNewButton_2_1_1.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JFileChooser j = new JFileChooser();
				j.setCurrentDirectory(currentDir);
				int r = j.showOpenDialog(MaterialConverterWindow.this);
				if(r == JFileChooser.APPROVE_OPTION) {
					currentDir = j.getSelectedFile().getParentFile();
					textField_4.setText(j.getSelectedFile().toString());
				}
					
			}
		});
		panel_1_2_1_1.add(btnNewButton_2_1_1);
		
		spinner_1_1 = new JSpinner();
		spinner_1_1.setModel(new SpinnerNumberModel(64, 0, 255, 10));
		spinner_1_1.setBounds(123, 60, 60, 20);
		panel_1_2_1_1.add(spinner_1_1);
		
		JPanel panel_1_2_1_1_1 = new JPanel();
		panel_1_2_1_1_1.setLayout(null);
		panel_1_2_1_1_1.setBounds(414, 215, 394, 91);
		panel.add(panel_1_2_1_1_1);
		
		ButtonGroup bgf = new ButtonGroup();
		rdbtnNewRadioButton_2_1_1_1 = new JRadioButton("read image file ");
		rdbtnNewRadioButton_2_1_1_1.setBounds(10, 33, 109, 23);
		panel_1_2_1_1_1.add(rdbtnNewRadioButton_2_1_1_1);
		bgf.add(rdbtnNewRadioButton_2_1_1_1);
		
		JLabel lblEmission = new JLabel("Emission");
		lblEmission.setFont(new Font("Dialog", Font.PLAIN, 17));
		lblEmission.setBounds(10, 9, 143, 20);
		panel_1_2_1_1_1.add(lblEmission);
		
		rdbtnConstantValue_1_1_1_1 = new JRadioButton("constant value");
		rdbtnConstantValue_1_1_1_1.setSelected(true);
		rdbtnConstantValue_1_1_1_1.setBounds(10, 59, 109, 23);
		panel_1_2_1_1_1.add(rdbtnConstantValue_1_1_1_1);
		bgf.add(rdbtnConstantValue_1_1_1_1);
		
		textField_5 = new JTextField();
		textField_5.setMargin(new Insets(0, 2, 0, 2));
		textField_5.setColumns(10);
		textField_5.setBounds(123, 34, 167, 20);
		panel_1_2_1_1_1.add(textField_5);
		
		btnNewButton_2_1_1_1 = new JButton("Browse...");
		btnNewButton_2_1_1_1.setMargin(new Insets(0, 0, 0, 0));
		btnNewButton_2_1_1_1.setBounds(291, 33, 89, 22);
		btnNewButton_2_1_1_1.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JFileChooser j = new JFileChooser();
				j.setCurrentDirectory(currentDir);
				int r = j.showOpenDialog(MaterialConverterWindow.this);
				if(r == JFileChooser.APPROVE_OPTION) {
					currentDir = j.getSelectedFile().getParentFile();
					textField_5.setText(j.getSelectedFile().toString());
				}
					
			}
		});
		panel_1_2_1_1_1.add(btnNewButton_2_1_1_1);
		
		spinner_1_1_1 = new JSpinner();
		spinner_1_1_1.setModel(new SpinnerNumberModel(0, 0, 255, 10));
		spinner_1_1_1.setBounds(123, 60, 60, 20);
		panel_1_2_1_1_1.add(spinner_1_1_1);
		
		JPanel panel_1_2_1_1_2 = new JPanel();
		panel_1_2_1_1_2.setLayout(null);
		panel_1_2_1_1_2.setBounds(10, 317, 394, 91);
		panel.add(panel_1_2_1_1_2);
		
		ButtonGroup bgg = new ButtonGroup();
		rdbtnNewRadioButton_2_1_1_2 = new JRadioButton("read image file ");
		rdbtnNewRadioButton_2_1_1_2.setBounds(10, 33, 109, 23);
		panel_1_2_1_1_2.add(rdbtnNewRadioButton_2_1_1_2);
		bgg.add(rdbtnNewRadioButton_2_1_1_2);
		
		JLabel lblClearcoatValue = new JLabel("Clearcoat Value");
		lblClearcoatValue.setFont(new Font("Dialog", Font.PLAIN, 17));
		lblClearcoatValue.setBounds(10, 9, 143, 20);
		panel_1_2_1_1_2.add(lblClearcoatValue);
		
		rdbtnConstantValue_1_1_1_2 = new JRadioButton("constant value");
		rdbtnConstantValue_1_1_1_2.setSelected(true);
		rdbtnConstantValue_1_1_1_2.setBounds(10, 59, 109, 23);
		panel_1_2_1_1_2.add(rdbtnConstantValue_1_1_1_2);
		bgg.add(rdbtnConstantValue_1_1_1_2);
		textField_6 = new JTextField();
		textField_6.setMargin(new Insets(0, 2, 0, 2));
		textField_6.setColumns(10);
		textField_6.setBounds(123, 34, 167, 20);
		panel_1_2_1_1_2.add(textField_6);
		
		btnNewButton_2_1_1_2 = new JButton("Browse...");
		btnNewButton_2_1_1_2.setMargin(new Insets(0, 0, 0, 0));
		btnNewButton_2_1_1_2.setBounds(291, 33, 89, 22);
		btnNewButton_2_1_1_2.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JFileChooser j = new JFileChooser();
				j.setCurrentDirectory(currentDir);
				int r = j.showOpenDialog(MaterialConverterWindow.this);
				if(r == JFileChooser.APPROVE_OPTION) {
					currentDir = j.getSelectedFile().getParentFile();
					textField_6.setText(j.getSelectedFile().toString());
				}
					
			}
		});
		panel_1_2_1_1_2.add(btnNewButton_2_1_1_2);
		
		spinner_1_1_2 = new JSpinner();
		spinner_1_1_2.setModel(new SpinnerNumberModel(0, 0, 255, 10));
		spinner_1_1_2.setBounds(123, 60, 60, 20);
		panel_1_2_1_1_2.add(spinner_1_1_2);
		
		JPanel panel_1_1_1 = new JPanel();
		panel_1_1_1.setLayout(null);
		panel_1_1_1.setBounds(414, 317, 394, 91);
		panel.add(panel_1_1_1);
		
		ButtonGroup bgh = new ButtonGroup();
		rdbtnNewRadioButton_1_1 = new JRadioButton("read image file ");
		rdbtnNewRadioButton_1_1.setBounds(10, 33, 109, 23);
		panel_1_1_1.add(rdbtnNewRadioButton_1_1);
		bgh.add(rdbtnNewRadioButton_1_1);
		
		rdbtnNoDisplacementMap = new JRadioButton("no displacement map");
		rdbtnNoDisplacementMap.setSelected(true);
		rdbtnNoDisplacementMap.setBounds(10, 59, 144, 23);
		panel_1_1_1.add(rdbtnNoDisplacementMap);
		bgh.add(rdbtnNoDisplacementMap);
		
		textField_8 = new JTextField();
		textField_8.setMargin(new Insets(0, 2, 0, 2));
		textField_8.setColumns(10);
		textField_8.setBounds(123, 34, 167, 20);
		panel_1_1_1.add(textField_8);
		
		btnNewButton_1_1 = new JButton("Browse...");
		btnNewButton_1_1.setMargin(new Insets(0, 0, 0, 0));
		btnNewButton_1_1.setBounds(291, 33, 89, 22);
		btnNewButton_1_1.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JFileChooser j = new JFileChooser();
				j.setCurrentDirectory(currentDir);
				int r = j.showOpenDialog(MaterialConverterWindow.this);
				if(r == JFileChooser.APPROVE_OPTION) {
					currentDir = j.getSelectedFile().getParentFile();
					textField_8.setText(j.getSelectedFile().toString());
				}
					
			}
		});
		panel_1_1_1.add(btnNewButton_1_1);
		
		JLabel lblDisplacementMap = new JLabel("Displacement Map");
		lblDisplacementMap.setFont(new Font("Dialog", Font.PLAIN, 17));
		lblDisplacementMap.setBounds(10, 9, 167, 20);
		panel_1_1_1.add(lblDisplacementMap);
		
		JPanel panel_3 = new JPanel();
		panel_3.setBounds(10, 419, 798, 30);
		panel.add(panel_3);
		panel_3.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));
		
		JLabel lblNewLabel_1 = new JLabel("Width/Height:");
		panel_3.add(lblNewLabel_1);
		
		comboBox = new JComboBox();
		comboBox.setModel(new DefaultComboBoxModel(new String[] {"256", "512", "1024", "2048", "4096"}));
		comboBox.setPreferredSize(new Dimension(60, 22));
		panel_3.add(comboBox);
		
		JMenuBar menuBar = new JMenuBar();
		setJMenuBar(menuBar);
		
		JMenu mnNewMenu = new JMenu("File");
		menuBar.add(mnNewMenu);
		
		JMenuItem mntmNewMenuItem = new JMenuItem("load CC0 material");
		mntmNewMenuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JFileChooser j = new JFileChooser();
				j.setCurrentDirectory(currentCC0Dir);
				j.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
				j.setDialogTitle("Select extracted CC0 material directory");
				int r = j.showOpenDialog(MaterialConverterWindow.this);
				if(r == JFileChooser.APPROVE_OPTION) {
					currentCC0Dir = j.getSelectedFile().getParentFile();
					rdbtnConstantValue.setSelected(true);
					rdbtnNoNormalMap.setSelected(true);
					rdbtnConstantValue_1.setSelected(true);
					rdbtnConstantValue_1_1_1_2.setSelected(true);
					rdbtnConstantValue_1_1.setSelected(true);
					rdbtnConstantValue_1_1_1.setSelected(true);
					rdbtnConstantValue_1_1_1_1.setSelected(true);
					rdbtnNoDisplacementMap.setSelected(true);
					comboBox.setSelectedIndex(1);
					File[] filesInDir = j.getSelectedFile().listFiles();
					for(File f : filesInDir) {
						String name = f.getName();
						String nameLower = name.toLowerCase();
						if(nameLower.contains("color")) {
							textField.setText(f.toString());
							rdbtnNewRadioButton.setSelected(true);
						}else if(nameLower.contains("metalness")) {
							textField_3.setText(f.toString());
							rdbtnNewRadioButton_2_1.setSelected(true);
						}else if(nameLower.contains("normal")) {
							textField_1.setText(f.toString());
							rdbtnNewRadioButton_1.setSelected(true);
						}else if(nameLower.contains("roughness")) {
							textField_2.setText(f.toString());
							rdbtnNewRadioButton_2.setSelected(true);
						}else if(nameLower.contains("displacement")) {
							textField_8.setText(f.toString());
							rdbtnNewRadioButton_1_1.setSelected(true);
						}else if(nameLower.contains("emission")) {
							textField_5.setText(f.toString());
							rdbtnNewRadioButton_2_1_1_1.setSelected(true);
						}
					}
				}
			}
		});
		mnNewMenu.add(mntmNewMenuItem);
	}
}
