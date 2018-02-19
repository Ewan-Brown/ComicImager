package main;

import java.awt.Point;
import java.util.ArrayList;


public class Triangle {
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
	public void CalculateColor(FastRGB fRGB){
		int r = 0;
		int g = 0;
		int b = 0;
		int i = 0;
		for(Point p : fillPoints){
			int RGB = fRGB.getRGB(p.x, p.y);
			if(RGB != -1){
				i++;
				r += (RGB >> 16) & 0x000000FF;
				g += (RGB >> 8) & 0x000000FF;
				b += (RGB) & 0x000000FF;
			}

		}
		r = (int)((float)r / (float)i);
		g = (int)((float)g / (float)i);
		b = (int)((float)b / (float)i);
		this.RGB = r << 16 | g << 8 | b;
	}
}
