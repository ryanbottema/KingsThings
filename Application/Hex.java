package KAT;

import javafx.scene.shape.Polygon;

public class Hex extends Polygon {

	private double heightNeeded, widthNeeded, sideLength;
	
	
	/*
	 * Constructors
	 */
	public Hex(){
		super();
	}
	
	public Hex(double height, boolean flatTop) {
		super();
		double x1, x2, x3, x4, y1, y2, y3, y4;
		if (!flatTop) {
			
			x1 = 0;
			x2 = Math.sqrt(3)/2 * height/2;
			x3 = 2 * x2;
			y1 = 0;
			y2 = height/4;
			y3 = height/2 + y2;
			y4 = height;
			
			heightNeeded = height;
			widthNeeded = y4;
			sideLength = height/2;
			
			this.getPoints().addAll(new Double[]{
					x2, y1,
					x3, y2,
					x3, y3,
					x2, y4,
					x1, y3,
					x1, y2
				});
			
		} else { // flat top == true
			
			sideLength = height/Math.sqrt(3);
			x1 = 0;
			x2 = sideLength/2;
			x3 = x2 + sideLength;
			x4 = x2 + x3;
			y1 = 0;
			y2 = height/2;
			y3 = height;
			
			heightNeeded = height;
			widthNeeded = x4;
			
			this.getPoints().addAll(new Double[]{
					x2, y1,
					x3, y1,
					x4, y2,
					x3, y3,
					x2, y3,
					x1, y2
				});
		}
	}
	
	/*
	 * Gets and Sets
	 */
	public double getHeightNeeded() { return heightNeeded; }
	public double getWidthNeeded() { return widthNeeded; }
	public double getSideLength() {	return sideLength; }

	
}
