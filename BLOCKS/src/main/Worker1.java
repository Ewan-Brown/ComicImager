package main;

import java.awt.Point;

public class Worker1 implements Runnable{
	Triangle[] t;
	int startX,startY;
	int endX,endY;
	int spaceX, spaceY;
	int index;
	public Worker1(Triangle[] tA,int startX, int endX,int startY, int endY,
			int spaceX,int spaceY, int indexStart){
		this.t = tA;
		this.startX = startX;
		this.endX = endX;
		this.startY = startY;
		this.endY = endY;
		this.spaceX = spaceX;
		this.spaceY = spaceY;
		index = indexStart;
	}
	@Override
	public void run() {
		for(int x = startX; x < endX - 1;x++){
			for(int y = startY; y < endY - 1;y++){
				int xz = x * spaceX;
				int yz = y * spaceY;
				int xzE = xz + spaceX;
				int yzE = yz + spaceY;
				Point[] p = {new Point(xz,yz),new Point(xz,yzE),new Point(xzE,yz)};
				Point[] p2 = {new Point(xzE,yzE),new Point(xz,yzE),new Point(xzE,yz)};
				t[index] = new Triangle(p);
				index++;
				t[index] = new Triangle(p2);
				index++;
			}
		}
	}



}
