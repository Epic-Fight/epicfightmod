package yesman.epicfight.api.utils.datastruct;

public class ModifiablePair<F, S> {
	public static <F, S> ModifiablePair<F, S> of(F first, S second) {
		return new ModifiablePair<>(first, second);
	}
	
	private F first;
	private S second;
	
	private ModifiablePair(F first, S second) {
		this.first = first;
		this.second = second;
	}
	
	public F getFirst() {
		return this.first;
	}
	
	public S getSecond() {
		return this.second;
	}
	
	public void setFirst(F first) {
		this.first = first;
	}
	
	public void setSecond(S second) {
		this.second = second;
	}
}
