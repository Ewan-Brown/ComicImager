package main;

public class Worker2 implements Runnable{
	Triangle[] t;
	int start;
	int end;
	FastRGB fRGB;
	public Worker2(Triangle[] t,int start, int end, FastRGB fRGB){
		this.t = t;
		this.start = start;
		this.end = end;
		this.fRGB = fRGB;
	}
	
	@Override
	public void run() {
		for(int i = start; i < end;i++){
			t[i].CalculatePoints();
			t[i].CalculateColor(fRGB);
		}
	}

	
	
}
