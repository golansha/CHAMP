package sat.common;

public class Pair<L, R> {
    public L left;
    public R right;
    
    public Pair(final L left, final R right) {
        this.left = left;
        this.right = right;
    }
    
    public L getLeft() {
        return left;
    }

    public void setLeft(final L left) {
        this.left = left;
    }

    public R getRight() {
        return right;
    }

    public void setRight(final R right) {
        this.right = right;
    }

	@Override
	public String toString() {
		return "[" + left + ", " + right + "]";
	}
    
    
}
