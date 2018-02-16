package main;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Random;

import javax.swing.JFrame;
import javax.swing.JPanel;

public class Main extends JPanel implements KeyListener{
	public static BufferedImage rawImage;
	public static BufferedImage newImage;
	public static int w = 1000;
	public static int h = 1000;
	public static int block = 50;
	boolean[] keyset = new boolean[256];
	long[] cooldowns = new long[256];
	public static void main(String[] args){
		//		System.setProperty("sun.java2d.opengl","True");
		//		FileDialog fd = new FileDialog((java.awt.Frame) null);
		//		fd.setTitle("Choose an image");
		//		fd.setVisible(true);
		//		File f = new File(fd.getDirectory() + fd.getFile());
		//		if(fd.getDirectory() == null || fd.getFile() == null)
		//			return;
		//		BufferedImage img = null;
		//		try {
		//			img = ImageIO.read(f);
		//			img.getType();
		//		} catch (IOException | NullPointerException e) {}
		//		rawImage = img;
		//		w = img.getWidth();
		//		h = img.getHeight();
		//				newImage = new BufferedImage(w,h,rawImage.getType());
		newImage = new BufferedImage(w,h,BufferedImage.TYPE_3BYTE_BGR);
		JFrame frame = new JFrame();
		Main m = new Main();
		frame.add(m);
		frame.addKeyListener(m);
		frame.setSize(w, h);
		frame.setVisible(true);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		//				newImage = rawImage;
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
		Point[] points = new Point[3];
		Line[] lines = new Line[3];
		//List of all the calculated points that fill this triangle
		ArrayList<Point> fillPoints = new ArrayList<Point>();
		//The line that takes up the biggest x-space (widest)
		Line primeLine;
		//The corner opposite to the prime line
		Point oppositeCorner;
		//Whether the triangle (based off of primeLine) is 'upsidedown' or not
		boolean cornerIsBelow;
		Triangle(Point[] p){
			double maxXSize = 0;
			int prime = 0;
			//Calculate the prime line based off of the dX of each line.
			for(int i = 0; i < 3;i++){
				points[i] = p[i];
				lines[i] = new Line(p[i], p[(i+1) % 3]);
				if(Math.abs(lines[i].dX) > maxXSize){
					Math.abs(maxXSize = Math.abs(lines[i].dX));
					prime = i;
				}
			}
			primeLine = lines[prime];
			//Get the opposite corner
			for(int i = 0; i < 3;i++){
				if(!primeLine.hasPoint(points[i])){
					oppositeCorner = points[i];
				}
			}
			//Y value of primeline at opposite corner's x value
			double yP = primeLine.slope*oppositeCorner.x + primeLine.b;
			//Check if the corner is below the prime line.
			cornerIsBelow = oppositeCorner.y < yP;
			
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
	static int i = 0;
	public static void reIterate(){
		if(i < 3){
			i++;
			Point[] p = {new Point(rand.nextInt(w),rand.nextInt(h)),new Point(rand.nextInt(w),rand.nextInt(h)),new Point(rand.nextInt(w),rand.nextInt(h))};
			Triangle t = new Triangle(p);
			Graphics2D g = (Graphics2D)newImage.getGraphics();
			for(int i = 0; i < t.lines.length;i++){
				t.lines[i].paint(g);
			}
			g.setColor(Color.RED);
			Line pr = t.primeLine;
			pr.paint(g);
			if(t.cornerIsBelow) g.setColor(Color.BLUE);
			g.fillRect(t.oppositeCorner.x, t.oppositeCorner.y, 10, 10);
//			double maxXTop = Math.max(top.p1.getX(), top.p2.getX());
//			double minXTop = Math.min(top.p1.getX(), top.p2.getX());
//			System.out.println(maxXTop + " " + minXTop);
		}
	}
	public void paint(Graphics gr){
		Graphics2D g2 = (Graphics2D) gr; 

		g2.drawImage(createFlipped(newImage), 0, 0, w, h,null);
	}
	private static BufferedImage createFlipped(BufferedImage image)
	{
		AffineTransform at = new AffineTransform();
		at.concatenate(AffineTransform.getScaleInstance(1, -1));
		at.concatenate(AffineTransform.getTranslateInstance(0, -image.getHeight()));
		return createTransformed(image, at);
	}
	private static BufferedImage createTransformed(
			BufferedImage image, AffineTransform at)
	{
		BufferedImage newImage = new BufferedImage(
				image.getWidth(), image.getHeight(),
				BufferedImage.TYPE_INT_ARGB);
		Graphics2D g = newImage.createGraphics();
		g.transform(at);
		g.drawImage(image, 0, 0, null);
		g.dispose();
		return newImage;
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
