package com.RoboWars.opengl.mesh;

public class Cube extends Mesh {
	private final int NUMBER_OF_VERTICES = 8;
	private final int NUMBER_OF_DIMENSIONS = 3;

	private final float width, height, depth;
	private final float vertices[];
	private final short indices[];

	public Cube(float size) {
		this(size, size, size);
	}

	public Cube(float width, float height, float depth) {
		this.width = width;
		this.height = height;
		this.depth = depth;

		width /= 2;
		height /= 2;
		depth /= 2;

		vertices = new float[] { -width, -height, -depth, // 0
				width, -height, -depth, // 1
				width, height, -depth, // 2
				-width, height, -depth, // 3
				-width, -height, depth, // 4
				width, -height, depth, // 5
				width, height, depth, // 6
				-width, height, depth, // 7
		};

		indices = new short[] { 0, 4, 5, 0, 5, 1, 1, 5, 6, 1, 6, 2, 2, 6, 7, 2,
				7, 3, 3, 7, 4, 3, 4, 0, 4, 7, 6, 4, 6, 5, 3, 0, 1, 3, 1, 2, };

		setIndices(indices);
		setVertices(vertices);
	}

	public float getWidth() {
		return width;
	}

	public float getHeight() {
		return height;
	}

	public float getDepth() {
		return depth;
	}

	public void move(float x, float y, float z) {
		for (int i = 0; i < NUMBER_OF_VERTICES * NUMBER_OF_DIMENSIONS; i++) {
			vertices[i] += x;
			vertices[i + 1] += y;
			vertices[i + 2] += z;

			i += 3;
		}
	}

	@Override
	public void setColor(float red, float green, float blue, float alpha) {
		super.setColor(red, green, blue, alpha);
	}
}
