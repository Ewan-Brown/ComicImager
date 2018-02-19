package main;

import java.awt.Color;
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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JPanel;
/**
 * @author Ewan
 *The intention of this branch of this project is to take a photo and break it up into a triangle mosaic, 'cut up' fairly evenly. Then each triangle's color is the average of the pixels located inside of it
 * in order to look cool.
 * Hopefully this doesn't just look messy.<p>
 * I made my own Line and Triangle class instead of using Java's for a reason that i've since forgotten. oops.
 */
public class Main extends JPanel implements KeyListener{

	private static final long serialVersionUID = 1L;
	public static BufferedImage rawImage;
	public static BufferedImage newImage;
	public static ExecutorService e = Executors.newFixedThreadPool(4);
	//	public static int[] RGB[][];
	public static FastRGB fastRGB;
	public static int w = 1000;
	public static int h = 1000;
	public static int blockx = 100; // 5 is a good value for benchmark
	public static int blocky = 100; // me too 
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
			//			reIterate();
		}
	}
	public static void reIterate1(int wz, int hz, int wzf, int hzf, Triangle[] tA){
		//		int z = 4;
		//		int s = tA.length;
		//		Future<?>[] futures = new Future<?>[z];
		//		for(int i = 0; i < z;i++){
		//			int startX = (i % 2) * wz/2;
		//			int startY = (i > 1) ? hz/2 : 0;
		//			int endX = startX + wz/2;
		//			int endY = startY + hz/2;
		//			int spaceX = wzf;
		//			int spaceY = hzf;
		//			switch(i){
		//			case 0:
		////				endX++;
		//				break;
		//			case 1:
		//				break;
		//			case 2:
		////				endX++;
		//				break;
		//			case 3:
		//				break;
		//			}
		//			futures[i] = e.submit(new Worker1(tA, startX, endX, startY, endY, spaceX, spaceY, i * s / 4));
		//		}
		//		boolean b = false;
		//		while(!b){
		//			b = true;
		//			for(int i = 0 ;i < z;i++){
		//				if(futures[i] != null && !futures[i].isDone()){
		//					b = false;
		//				}
		//			}
		//		}
		int index = 0;
		for(int x = 0; x < wz - 1;x++){
			for(int y = 0; y < hz - 1;y++){
				int xz = x * wzf;
				int yz = y * hzf;
				int xzE = xz + wzf;
				int yzE = yz + hzf;
				Point[] p = {new Point(xz,yz),new Point(xz,yzE),new Point(xzE,yz)};
				Point[] p2 = {new Point(xzE,yzE),new Point(xz,yzE),new Point(xzE,yz)};

				tA[index] = new Triangle(p);
				index++;
				tA[index] = new Triangle(p2);
				index++;
			}
		}
	}
	public static void reIterate2(Triangle[] tA, Graphics2D g, int wz, int hz, int wzf, int hzf){
		int s = tA.length;
		int z = s / 4;
		Future<?>[] futures = new Future<?>[4];
		for(int i = 0; i < 4;i++){
			futures[i] = e.submit(new Worker2(tA,z*i,z*(i+1),fastRGB));
		}
		boolean b = false;
		while(!b){
			b = true;
			for(int i = 0 ;i < 4;i++){
				if(!futures[i].isDone()){
					b = false;
				}
			}
		}
		for(int i = 0; i < tA.length;i++){
			Triangle t = tA[i];
			try{
				//						t.CalculatePoints();
				//						t.CalculateColor(fastRGB);
				ArrayList<Point> pA = t.fillPoints;
				for(int j = 0; j < pA.size();j++){
					newImage.setRGB(pA.get(j).x, pA.get(j).y, t.RGB); //29.2%
				}
			} catch(java.lang.NullPointerException e){
				System.out.println(i + " " +estimatedLocation(i,hz,wz,wzf,hzf));
			}
		}

	}
	public static Point estimatedLocation(int index, int hz, int wz, int wzf, int hzf){
		int square = index / 2;
		int x = square % wz;
		int y = (int)(Math.floor(square - x) / wz);
		return new Point(x,y);
	}
	public static void reIterate(){
		Graphics2D g = (Graphics2D)newImage.getGraphics();
		newImage = new BufferedImage(w,h,BufferedImage.TYPE_3BYTE_BGR);
		int hzf = blockx;
		int wzf = blocky;
		int hz = (int)((double)h / (double)hzf);
		int wz = (int)((double)w / (double)wzf);
		int estimatedTriangleCount = (hz-1) * (wz-1) * 2;
		Triangle[] tA = new Triangle[estimatedTriangleCount];
		reIterate1(wz, hz, wzf, hzf, tA); 
		reIterate2(tA, g,wz, hz, wzf, hzf);

	}
	public void paint(Graphics gr){
		Graphics2D g2 = (Graphics2D) gr; 
		g2.drawImage(newImage, 0, 0, w, h,null);
		g2.setColor(Color.WHITE);
		g2.drawLine(0, h/2, w, h/2);
		g2.drawLine(w/2, 0, w/2, h);
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
