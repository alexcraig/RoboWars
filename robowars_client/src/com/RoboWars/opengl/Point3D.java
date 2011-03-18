package com.RoboWars.opengl;

public class Point3D {
	private float x, y, z;

	public Point3D() {
		this(0, 0, 0);
	}

	public Point3D(float x, float y) {
		this(x, y, 0);
	}

	public Point3D(float x, float y, float z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}

	public void setX(float x) {
		this.x = x;
	}

	public float getX() {
		return x;
	}

	public void setY(float y) {
		this.y = y;
	}

	public float getY() {
		return y;
	}

	public void setZ(float z) {
		this.z = z;
	}

	public float getZ() {
		return z;
	}
}
