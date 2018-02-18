package main;

import java.awt.FileDialog;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JPanel;
/**
 * @author Ewan
 *The intention of this branch of this project is to take a photo and break it up into a triangle mosaic, 'cut up' fairly evenly. Then each triangle's color is the average of the pixels located inside of it
 * in order to look cool.
 * Hopefully this doesn't just look messy.
 * I made my own Line and Triangle class instead of using Java's just for fun, i'm not a <i>complete</i> idiot.
 */
public class Main extends JPanel implements KeyListener{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public static BufferedImage rawImage;
	public static BufferedImage newImage;
	public static FastRGB fastRGB;
	public static int w = 1000;
	public static int h = 1000;
	public static int blockx = 5; // 5 is a good value for benchmark
	public static int blocky = 5; // me too 
	boolean[] keyset = new boolean[256];
	long[] cooldowns = new long[256];
	static Random rand = new Random();
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
		JFrame frame = new JFrame();
		Main m = new Main();
		frame.add(m);
		frame.addKeyListener(m);
		frame.setSize(w, h);
		frame.setVisible(true);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		fastRGB = new FastRGB(rawImage);
		newImage = new BufferedImage(w,h,BufferedImage.TYPE_3BYTE_BGR);
		reIterate();
		while(true){
			try {
				Thread.sleep(10);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			m.updateKeys();
			m.repaint();
			reIterate();
		}
	}
	public static void reIterate1(int wz, int hz, int wzf, int hzf, ArrayList<Triangle> tA){
		for(int x = 0; x < wz - 1;x++){
			for(int y = 0; y < hz - 1;y++){
				
				//~1%
				int xz = x * wzf;
				int yz = y * hzf;
				int xzE = xz + wzf;
				int yzE = yz + hzf;
				Point[] p = {new Point(xz,yz),new Point(xz,yzE),new Point(xzE,yz)};
				Point[] p2 = {new Point(xzE,yzE),new Point(xz,yzE),new Point(xzE,yz)};
				
				//33.3%
				reIterate1A(tA, p); 
				reIterate1A(tA, p2);
			}
		}
	}
	public static void reIterate1A(ArrayList<Triangle> tA, Point[] p){
		tA.add(new Triangle(p));
	}
	public static void reIterate2(ArrayList<Triangle> tA, Graphics2D g){
		for(int i = 0; i < tA.size();i++){
			tA.get(i).CalculatePoints();
			tA.get(i).CalculateColor(rawImage);
			ArrayList<Point> pA = tA.get(i).fillPoints;
			for(int j = 0; j < pA.size();j++){
				g.fillRect(pA.get(j).x, pA.get(j).y, 1, 1);
				newImage.setRGB(pA.get(j).x, pA.get(j).y, tA.get(i).RGB);
			}
		}
	}
	public static void reIterate(){				//99.8%
		ArrayList<Triangle> tA = new ArrayList<Triangle>();
		Graphics2D g = (Graphics2D)newImage.getGraphics();
		newImage = new BufferedImage(w,h,BufferedImage.TYPE_3BYTE_BGR);
		int hzf = blockx;
		int wzf = blocky;
		int hz = (int)((double)h / (double)hzf);
		int wz = (int)((double)w / (double)wzf);
		
		reIterate1(wz, hz, wzf, hzf, tA);   	//65 %
		reIterate2(tA, g);				    	//34 %

	}
	public void paint(Graphics gr){
		Graphics2D g2 = (Graphics2D) gr; 
		g2.drawImage(newImage, 0, 0, w, h,null);
	}

	@Override
	public void keyTyped(KeyEvent e) {}
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
			ticks = 0;
		}
		for(int i = 0; i < cooldowns.length;i++){
			cooldowns[i]--;
		}
		if(keyset[KeyEvent.VK_LEFT] && cooldowns[KeyEvent.VK_LEFT] < 0){
			reIterate();
			cooldowns[KeyEvent.VK_LEFT] = 5L;
			blockx -= 1;
			if(blockx < 1){
				blockx = 1;
			}
		}
		if(keyset[KeyEvent.VK_RIGHT] && cooldowns[KeyEvent.VK_RIGHT] < 0){
			reIterate();
			cooldowns[KeyEvent.VK_RIGHT] = 5L;
			blockx += 1;
			if (blockx > w){
				blockx = w;
			}
		}
		if(keyset[KeyEvent.VK_DOWN] && cooldowns[KeyEvent.VK_DOWN] < 0){
			reIterate();
			cooldowns[KeyEvent.VK_DOWN] = 5L;
			blocky -= 1;
			if(blocky < 3){
				blocky = 3;
			}
		}
		if(keyset[KeyEvent.VK_UP] && cooldowns[KeyEvent.VK_UP] < 0){
			reIterate();
			cooldowns[KeyEvent.VK_UP] = 5L;
			blocky += 1;
			if (blocky > h){
				blocky = h;
			}
		}
	}
}
