package datastructures;


public class Box {
	private Point lowerLeft;
	private Point upperRight;

	public Box(Point lowerLeft, Point upperRight) {
		this.lowerLeft = lowerLeft;
		this.upperRight = upperRight;
	}

	public Box(double x, double y) {
		this.lowerLeft = new Point(x, y);
		this.upperRight = new Point(x, y);
	}

	public Box minimumEnlargement(Box b) {
		Point newLeft = new Point(Math.min(b.getLowerLeft().getX(), getLowerLeft().getX()),
				Math.min(b.getLowerLeft().getY(), getLowerLeft().getY()));
		Point newRight = new Point(Math.max(b.getUpperRight().getX(), getUpperRight().getX()),
				Math.max(b.getUpperRight().getY(), getUpperRight().getY()));
		return new Box(newLeft, newRight);
	}

	public boolean contains(Point n) {
		return n.getX() >= lowerLeft.getX() && n.getY() <= upperRight.getY();
	}

	public boolean contains(Box n) {
		return n.getLowerLeft().getX() >= lowerLeft.getX() && n.getUpperRight().getY() <= upperRight.getY()
				&& n.getLowerLeft().getY() >= lowerLeft.getY() && n.getUpperRight().getX() <= upperRight.getX();
	}

	public boolean intersects(Box n) {
		return !(n.lowerLeft.getX() > upperRight.getX() || n.lowerLeft.getY() > upperRight.getY()
				|| lowerLeft.getY() > n.upperRight.getY() || lowerLeft.getX() > n.upperRight.getX());
	}

	public Point getLowerLeft() {
		return lowerLeft;
	}

	public void setLowerLeft(Point lowerLeft) {
		this.lowerLeft = lowerLeft;
	}

	public Point getUpperRight() {
		return upperRight;
	}

	public void setUpperRight(Point upperRight) {
		this.upperRight = upperRight;
	}

	public double getArea() {
		return (upperRight.getX() - lowerLeft.getX()) * (upperRight.getY() - lowerLeft.getY());
	}

	public String toString() {
		return lowerLeft.toString() + " " + upperRight.toString();
	}

}
