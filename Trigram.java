
public class Trigram {

	private Token first;
	private Token second;
	private Token third;
	
	public Trigram(Token f, Token s, Token t) {
		first = f;
		second = s;
		third = t;
	}
	
	public Token getFirst() {
		return first;
	}
	
	public Token getSecond() {
		return second;
	}
	
	public Token getThird() {
		return third;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((first == null) ? 0 : first.hashCode());
		result = prime * result + ((second == null) ? 0 : second.hashCode());
		result = prime * result + ((third == null) ? 0 : third.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Trigram other = (Trigram) obj;
		if (first == null) {
			if (other.first != null)
				return false;
		} else if (!first.equals(other.first))
			return false;
		if (second == null) {
			if (other.second != null)
				return false;
		} else if (!second.equals(other.second))
			return false;
		if (third == null) {
			if (other.third != null)
				return false;
		} else if (!third.equals(other.third))
			return false;
		return true;
	}
	
	public String printVal() {
		return "[" + first.printVal() + "; " + second.printVal() + "; " + third.printVal() + "]";
	}
}
