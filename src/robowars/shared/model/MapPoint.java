package robowars.shared.model;

public class MapPoint {
	private int x,y,r,g,b;
	public MapPoint(){
		this(-1,-1,-1,-1,-1);
	}
	public MapPoint(int x, int y, int r, int g, int b) {
		this.x=x;
		this.y=y;
		this.r=r;
		this.g=g;
		this.b=b;
	}

	public int getR() {
		return r;
	}

	public int getB() {
		return b;
	}

	public int getG() {
		return g;
	}

	public int getX() {
		return x;
	}

	public int getY() {
		return y;
	}
	public String toString(){
		String s="[";
		s+="x:"+x+"|";
		s+="y:"+y+"|";
		s+="r:"+r+"|";
		s+="g:"+g+"|";
		s+="b:"+b;
		s+="]";
		return s;
	}


}

