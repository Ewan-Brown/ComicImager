package main;

import java.awt.Color;
import java.awt.FileDialog;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.geom.AffineTransform;
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
	public static int w = 1000;
	public static int h = 1000;
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
		newImage = new BufferedImage(w,h,BufferedImage.TYPE_3BYTE_BGR);
		JFrame frame = new JFrame();
		Main m = new Main();
		frame.add(m);
		frame.addKeyListener(m);
		frame.setSize(w, h);
		frame.setVisible(true);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
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
	public static class Triangle{
		int RGB = 0;
		Point[] points = new Point[3];
		Line[] lines = new Line[3];
		//List of all the calculated points that fill this triangle
		ArrayList<Point> fillPoints = new ArrayList<Point>();
		//The line that takes up the biggest x-space (widest)
		Line primeLine;
		//The point opposite to the prime line
		Point oppositePoint;//"Corner"
		Point startCorner;
		Point endCorner;
		Line leftLine;
		Line rightLine;
		//Whether the triangle (based off of primeLine) is 'upsidedown' or not
		boolean cornerIsBelow;
		Triangle(Point[] p){
			double maxXSize = 0;
			double minX = Double.MAX_VALUE;
			double maxX = Double.MIN_VALUE;
			int minPointI = 0;
			int maxPointI = 0;
			int primeI = 0;
			//Calculate the prime line based off of the dX of each line.
			for(int i = 0; i < 3;i++){
				points[i] = p[i];
				lines[i] = new Line(p[i], p[(i+1) % 3]);
				if(Math.abs(lines[i].dX) > maxXSize){
					Math.abs(maxXSize = Math.abs(lines[i].dX));
					primeI = i;
				}
				if(points[i].x < minX){
					minX = points[i].x;
					minPointI = i;
				}
				if(points[i].x > maxX){
					maxX = points[i].x;
					maxPointI = i;
				}
			}
			startCorner = points[minPointI];
			endCorner = points[maxPointI];
			primeLine = lines[primeI];
			//Get the opposite corner and left/right lines
			for(int i = 0; i < 3;i++){
				if(!primeLine.hasPoint(points[i])){
					oppositePoint = points[i];
				}
				if(lines[i].hasPoint(startCorner) && lines[i] != primeLine){
					leftLine = lines[i];
				}
				if(lines[i].hasPoint(endCorner) && lines[i] != primeLine){
					rightLine = lines[i];	
				}
			}
			//Y value of primeline at opposite corner's x value
			double yP = primeLine.slope*oppositePoint.x + primeLine.b;
			//Check if the corner is below the prime line.
			cornerIsBelow = oppositePoint.y < yP;

		}
		public void CalculatePoints(){
			fillPoints.clear();
			int x = (int)startCorner.getX();
			int y = (int)startCorner.getY();
			for(;x < oppositePoint.x;x++){
				boolean flag = false;
				y = (int) (primeLine.slope * x + primeLine.b);
				while(!flag){
					fillPoints.add(new Point(x,y));
					if(cornerIsBelow){
						y--;
					}
					else{
						y++;
					}
					flag = y > (leftLine.slope * x) + leftLine.b;
					if(cornerIsBelow) flag = !flag;
				}
			}
			for(;x < endCorner.x;x++){
				boolean flag = false;
				y = (int) (primeLine.slope * x + primeLine.b);
				while(!flag){
					fillPoints.add(new Point(x,y));
					if(cornerIsBelow){
						y--;
					}
					else{
						y++;
					}
					flag = y > (rightLine.slope * x) + rightLine.b;
					if(cornerIsBelow) flag = !flag;
				}
			}
		}
		public void CalculateColor(BufferedImage bi){
			int r = 0;
			int g = 0;
			int b = 0;
			for(Point p : fillPoints){
				int RGB = bi.getRGB(p.x, p.y);
				r += (RGB >> 16) & 0x000000FF;
				g += (RGB >> 8) & 0x000000FF;
				b += (RGB) & 0x000000FF;
			}
			r = (int)((float)r / (float)fillPoints.size());
			g = (int)((float)g / (float)fillPoints.size());
			b = (int)((float)b / (float)fillPoints.size());
			this.RGB = r << 16 | g << 8 | b;
		}
	}
	public static class Line{
		Point p1;
		Point p2;
		double slope = 0;
		double b = 0;
		double dX,dY;
		Line(Point p1, Point p2){
			this.p1 = p1;
			this.p2 = p2;
			dX = p2.getX() - p1.getX();
			dY = p2.getY() - p1.getY();
			slope = dY / dX;
			b = p1.getY() - slope*p1.getX();
			bresenhamsPoints = getBresenhamsPoints();
			for(int i = 1; i < bresenhamsPoints.size();i++){
				if(bresenhamsPoints.get(i).equals(bresenhamsPoints.get(i-1))){
					bresenhamsPoints.remove(i);
				}
			}
		}
		Line(int x1, int y1, int x2, int y2){
			this(new Point(x1,y1),new Point(x2,y2));
		}
		public void paint(Graphics g){
			g.drawLine(p1.x, p1.y, p2.x, p2.y);
		}
		public boolean hasPoint(Point points){
			return p1.equals(points) || p2.equals(points);
		}
		ArrayList<Point> bresenhamsPoints = new ArrayList<Point>();
		public ArrayList<Point> getBresenhamsPoints(){
			ArrayList<Point> p = new ArrayList<Point>();
			p.add(new Point((int)p1.getX(),(int)p1.getY()));
			int d = 0;
			int x1 = (int)p1.getX();
			int x2 = (int)p2.getX();
			int y1 = (int)p1.getY();
			int y2 = (int)p2.getY();
			int dx = Math.abs(x2 - x1);
			int dy = Math.abs(y2 - y1);

			int dx2 = 2 * dx; // slope scaling factors to
			int dy2 = 2 * dy; // avoid floating point

			int ix = x1 < x2 ? 1 : -1; // increment direction
			int iy = y1 < y2 ? 1 : -1;

			int x = x1;
			int y = y1;
			if (dx >= dy) {
				while (true) {
					p.add(new Point(x,y));
					if (x == x2)
						break;
					x += ix;
					d += dy2;
					if (d > dx) {
						y += iy;
						d -= dx2;
					}

				}
			} else {
				while (true) {
					p.add(new Point(x,y));
					if (y == y2)
						break;
					y += iy;
					d += dx2;
					if (d > dy) {
						x += ix;
						d -= dy2;
					}

				}
			}
			return p;
		}
	}
	static Random rand = new Random();
	public static void reIterate(){
		ArrayList<Triangle> tA = new ArrayList<Triangle>();
		newImage = new BufferedImage(w,h,BufferedImage.TYPE_3BYTE_BGR);
		Graphics2D g = (Graphics2D)newImage.getGraphics();
		double ratio = (double)h / (double)w;
		int hzf = block;
		int wzf = block;
		int hz = (int)((double)h / (double)hzf);
		int wz = (int)((double)w / (double)wzf);
		for(int x = 0; x < wz - 2;x++){
			for(int y = 0; y < hz - 2;y++){
				int xz = x * wzf;
				int yz = y * hzf;
				int xzE = xz + wzf;
				int yzE = yz + hzf;
				Point[] p = {new Point(xz,yz),new Point(xz,yzE),new Point(xzE,yz)};
				Point[] p2 = {new Point(xzE,yzE),new Point(xz,yzE),new Point(xzE,yz)};
				tA.add(new Triangle(p));
				tA.add(new Triangle(p2));
			}
		}
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
			block -= 1;
			if(block < 3){
				block = 3;
			}
		}
		if(keyset[KeyEvent.VK_RIGHT] && cooldowns[KeyEvent.VK_RIGHT] < 0){
			reIterate();
			cooldowns[KeyEvent.VK_RIGHT] = 5L;
			block += 1;
			if (block > Math.min(w, h)){
				block = Math.min(w, h);
			}
		}
	}
}
