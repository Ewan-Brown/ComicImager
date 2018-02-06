package main;

import java.awt.FileDialog;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JPanel;

public class Main extends JPanel implements KeyListener{

	public static BufferedImage rawImage;
	public static BufferedImage newImage;
	public static int w = 0;
	public static int h = 0;
	public static int block = 50;
	boolean[] keyset = new boolean[256];
	long[] cooldowns = new long[256];
	public static void main(String[] args){
		System.setProperty("sun.java2d.opengl","True");
		FileDialog fd = new FileDialog((java.awt.Frame) null);
		fd.setTitle("Choose an image");
		fd.setVisible(true);
		File f = new File(fd.getDirectory() + fd.getFile());
		if(fd.getDirectory() == null || fd.getFile() == null)
			return;
		BufferedImage img = null;
		try {
			img = ImageIO.read(f);
			img.getType();
		} catch (IOException | NullPointerException e) {}
		rawImage = img;
		w = img.getWidth();
		h = img.getHeight();
		newImage = new BufferedImage(w,h,rawImage.getType());
		JFrame frame = new JFrame();
		Main m = new Main();
		frame.add(m);
		frame.addKeyListener(m);
		frame.setSize(w, h);
		frame.setVisible(true);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		newImage = rawImage;
		reIterate();
		while(true){
			try {
				Thread.sleep(10);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			m.updateKeys();
			m.repaint();
		}
	}
	public static void reIterate(){
		newImage = new BufferedImage(w,h,rawImage.getType());
		for(int x = 0; x < w;x++){
			for(int y = 0; y < h;y++){
				int RGB = rawImage.getRGB(x, y);
				int r = (RGB >> 16) & 0x0ff;
				int g = (RGB >> 8) & 0x0ff;
				int b = (RGB) & 0x0ff;
				int zr = (int)Math.floor((double)r / block) * block;
				int zg = (int)Math.floor((double)g / block) * block;
				int zb = (int)Math.floor((double)b / block) * block;

				newImage.setRGB(x, y, (zr&0x0ff)<<16|((zg&0x0ff)<<8)|(zb&0x0ff));
			}
		}
	}
	public void paint(Graphics gr){
		Graphics2D g2 = (Graphics2D) gr; 
		g2.drawImage(newImage, 0, 0, w, h,null);
	}
	@Override
	public void keyTyped(KeyEvent e) {

	}
	@Override
	public void keyPressed(KeyEvent e) {
		keyset[e.getKeyCode()] = true;
	}
	@Override
	public void keyReleased(KeyEvent e) {
		keyset[e.getKeyCode()] = false;
	}
	public int ticks = 0;
	public void updateKeys(){
		ticks++;
		if(ticks > 7){
			reIterate();
			ticks = 0;
		}
		for(int i = 0; i < cooldowns.length;i++){
			cooldowns[i]--;
		}
		if(keyset[KeyEvent.VK_LEFT] && cooldowns[KeyEvent.VK_LEFT] < 0){
			cooldowns[KeyEvent.VK_LEFT] = 5L;
			block -= 1;
			if(block < 0){
				block = 0;
			}
		}
		if(keyset[KeyEvent.VK_RIGHT] && cooldowns[KeyEvent.VK_RIGHT] < 0){
			cooldowns[KeyEvent.VK_RIGHT] = 5L;
			block += 1;
			if (block > 255){
				block = 255;
			}
		}
	}
}
