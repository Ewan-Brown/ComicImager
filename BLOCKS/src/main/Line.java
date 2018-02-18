package main;

import java.awt.Graphics;
import java.awt.Point;

public class Line {
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
}
