package robowars.shared.model;
public class MapPoint {
	private int x,y,color;
	public MapPoint(){
		this(-1,-1,-1);
	}
	public MapPoint(int x, int y, int color) {
		this.x=x;
		this.y=y;
		this.color=color;
	}
	public int getColor() {
		return color;
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
		s+="c:"+color;
		s+="]";
		return s;
	}
	public String toOutputString(){
		String s ="[";
		s+=x+"|";
		s+=y+"|";
		s+=color;
		s+="]";
		return s;
	}
}

