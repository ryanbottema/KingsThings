package KAT;

public class Coord implements Comparable<Coord> {
	
	private int x, y, z;
	
	public Coord () {
	}
	public Coord (int x, int y, int z) {
		this.x = x;
		this.y = y; 
		this.z = z;
	}

	public Coord (String in) {
		String[] input = in.split(",");
		x = Integer.parseInt(input[0]);
		y = Integer.parseInt(input[1]);
		z = Integer.parseInt(input[2]);
	}
	
	public void setX(int x) { this.x = x; }
	public void setY(int y) { this.y = y; }
	public void setZ(int z) { this.z = z; }
	public void setXYZ(int x, int y, int z) {
		setX(x);
		setY(y);
		setZ(z);
	}
	public int getX() { return x; }
	public int getY() { return y; }
	public int getZ() { return z; }
	
	@Override
	public int compareTo(Coord c) {
		int diffX = this.x - c.getX();
		int diffY = this.y - c.getY();
		int diffZ = this.z - c.getZ();
		int distance = (int)Math.sqrt(Math.pow(diffX,2)+Math.pow(diffY,2)+Math.pow(diffZ,2));
		return distance;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + x;
		result = prime * result + y;
		result = prime * result + z;
		return result;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Coord other = (Coord) obj;
		if (x != other.x)
			return false;
		if (y != other.y)
			return false;
		if (z != other.z)
			return false;
		return true;
	}
	
	@Override
	public String toString() {
		return "(" + x + ", " + y + ", " + z + ")";
	}
	
	public Coord[] getAdjacent() {
		
		int numAdjacent = 0;
		int numThrees = 0;
		if (Math.abs(x) == 3) numThrees++;
		if (Math.abs(y) == 3) numThrees++;
		if (Math.abs(z) == 3) numThrees++;
		if (numThrees == 0) numAdjacent = 6;
		if (numThrees == 1) numAdjacent = 4;
		if (numThrees == 2) numAdjacent = 3;
		
		Coord[] toReturn = new Coord[numAdjacent];
		int i = 0;
		while (i < numAdjacent) {
			if (y != 3 && x != -3) {
				toReturn[i] = new Coord(x-1, y+1, z);
				i++;
			}
			if (y != 3 && z != -3) {
				toReturn[i] = new Coord(x, y+1, z-1);
				i++;
			}
			if (x != 3 && z != -3) {
				toReturn[i] = new Coord(x+1, y, z-1);
				i++;
			}
			if (x != 3 && y != -3) {
				toReturn[i] = new Coord(x+1, y-1, z);
				i++;
			}
			if (z != 3 && y != -3) {
				toReturn[i] = new Coord(x, y-1, z+1);
				i++;
			}
			if (z != 3 && x != -3) {
				toReturn[i] = new Coord(x-1, y, z+1);
				i++;
			}
		}
		return toReturn;
	}
	
	public int getNumAdjacent() {
		int numThrees = 0;
		if (Math.abs(x) == 3) numThrees++;
		if (Math.abs(y) == 3) numThrees++;
		if (Math.abs(z) == 3) numThrees++;
		if (numThrees == 0) return 6;
		if (numThrees == 1) return 4;
		else return 3;
	}
}
