package robowars.shared.model;

public class MapPoint {
	private int color;
	private float x,y;
	public MapPoint(){
		this(-1,-1,-1);
	}
	public MapPoint(float x, float y, int color) {
		this.x=x;
		this.y=y;
		this.color=color;
	}
	public int getColor() {
		return color;
	}

	public float getX() {
		return x;
	}

	public float getY() {
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

