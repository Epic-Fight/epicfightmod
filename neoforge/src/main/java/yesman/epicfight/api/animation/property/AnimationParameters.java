package yesman.epicfight.api.animation.property;

public record AnimationParameters<A, B, C, D, E, F, G, H, I, J> (
	A first,
	B second,
	C third,
	D fourth,
	E fifth,
	F sixth,
	G seventh,
	H eighth,
	I ninth,
	J tenth
) {
	public static <A> AnimationParameters<A, Void, Void, Void, Void, Void, Void, Void, Void, Void> of(A first) {
		return new AnimationParameters<> (first, (Void)null, (Void)null, (Void)null, (Void)null, (Void)null, (Void)null, (Void)null, (Void)null, (Void)null);
	}
	
	public static <A, B> AnimationParameters<A, B, Void, Void, Void, Void, Void, Void, Void, Void> of(A first, B second) {
		return new AnimationParameters<> (first, second, (Void)null, (Void)null, (Void)null, (Void)null, (Void)null, (Void)null, (Void)null, (Void)null);
	}
	
	public static <A, B, C> AnimationParameters<A, B, C, Void, Void, Void, Void, Void, Void, Void> of(A first, B second, C third) {
		return new AnimationParameters<> (first, second, third, (Void)null, (Void)null, (Void)null, (Void)null, (Void)null, (Void)null, (Void)null);
	}
	
	public static <A, B, C, D> AnimationParameters<A, B, C, D, Void, Void, Void, Void, Void, Void> of(A first, B second, C third, D fourth) {
		return new AnimationParameters<> (first, second, third, fourth, (Void)null, (Void)null, (Void)null, (Void)null, (Void)null, (Void)null);
	}
	
	public static <A, B, C, D, E> AnimationParameters<A, B, C, D, E, Void, Void, Void, Void, Void> of(A first, B second, C third, D fourth, E fifth) {
		return new AnimationParameters<> (first, second, third, fourth, fifth, (Void)null, (Void)null, (Void)null, (Void)null, (Void)null);
	}
	
	public static <A, B, C, D, E, F> AnimationParameters<A, B, C, D, E, F, Void, Void, Void, Void> of(A first, B second, C third, D fourth, E fifth, F sixth) {
		return new AnimationParameters<> (first, second, third, fourth, fifth, sixth, (Void)null, (Void)null, (Void)null, (Void)null);
	}
	
	public static <A, B, C, D, E, F, G> AnimationParameters<A, B, C, D, E, F, G, Void, Void, Void> of(A first, B second, C third, D fourth, E fifth, F sixth, G seventh) {
		return new AnimationParameters<> (first, second, third, fourth, fifth, sixth, seventh, (Void)null, (Void)null, (Void)null);
	}
	
	public static <A, B, C, D, E, F, G, H> AnimationParameters<A, B, C, D, E, F, G, H, Void, Void> of(A first, B second, C third, D fourth, E fifth, F sixth, G seventh, H eighth) {
		return new AnimationParameters<> (first, second, third, fourth, fifth, sixth, seventh, eighth, (Void)null, (Void)null);
	}
	
	public static <A, B, C, D, E, F, G, H, I> AnimationParameters<A, B, C, D, E, F, G, H, I, Void> of(A first, B second, C third, D fourth, E fifth, F sixth, G seventh, H eighth, I ninth) {
		return new AnimationParameters<> (first, second, third, fourth, fifth, sixth, seventh, eighth, ninth, (Void)null);
	}
	
	public static <A, B, C, D, E, F, G, H, I, J> AnimationParameters<A, B, C, D, E, F, G, H, I, J> of(A first, B second, C third, D fourth, E fifth, F sixth, G seventh, H eighth, I ninth, J tenth) {
		return new AnimationParameters<> (first, second, third, fourth, fifth, sixth, seventh, eighth, ninth, tenth);
	}
	
	public static <A, B, C, D, E, F, G, H, I, J, N> AnimationParameters<?, ?, ?, ?, ?, ?, ?, ?, ?, ?> addParameter(AnimationParameters<A, B, C, D, E, F, G, H, I, J> parameters, N newParam) {
		if (parameters.first() == null) {
			return new AnimationParameters<N, Void, Void, Void, Void, Void, Void, Void, Void, Void> (newParam, null, null, null, null, null, null, null, null, null);
		} else if (parameters.second() == null) {
			return new AnimationParameters<A, N, Void, Void, Void, Void, Void, Void, Void, Void> (parameters.first(), newParam, null, null, null, null, null, null, null, null);
		} else if (parameters.third() == null) {
			return new AnimationParameters<A, B, N, Void, Void, Void, Void, Void, Void, Void> (parameters.first(), parameters.second(), newParam, null, null, null, null, null, null, null);
		} else if (parameters.fourth() == null) {
			return new AnimationParameters<A, B, C, N, Void, Void, Void, Void, Void, Void> (parameters.first(), parameters.second(), parameters.third(), newParam, null, null, null, null, null, null);
		} else if (parameters.fifth() == null) {
			return new AnimationParameters<A, B, C, D, N, Void, Void, Void, Void, Void> (parameters.first(), parameters.second(), parameters.third(), parameters.fourth(), newParam, null, null, null, null, null);
		} else if (parameters.sixth() == null) {
			return new AnimationParameters<A, B, C, D, E, N, Void, Void, Void, Void> (parameters.first(), parameters.second(), parameters.third(), parameters.fourth(), parameters.fifth(), newParam, null, null, null, null);
		} else if (parameters.seventh() == null) {
			return new AnimationParameters<A, B, C, D, E, F, N, Void, Void, Void> (parameters.first(), parameters.second(), parameters.third(), parameters.fourth(), parameters.fifth(), parameters.sixth(), newParam, null, null, null);
		} else if (parameters.eighth() == null) {
			return new AnimationParameters<A, B, C, D, E, F, G, N, Void, Void> (parameters.first(), parameters.second(), parameters.third(), parameters.fourth(), parameters.fifth(), parameters.sixth(), parameters.seventh(), newParam, null, null);
		} else if (parameters.ninth() == null) {
			return new AnimationParameters<A, B, C, D, E, F, G, H, N, Void> (parameters.first(), parameters.second(), parameters.third(), parameters.fourth(), parameters.fifth(), parameters.sixth(), parameters.seventh(), parameters.eighth(), newParam, null);
		} else if (parameters.tenth() == null) {
			return new AnimationParameters<A, B, C, D, E, F, G, H, I, N> (parameters.first(), parameters.second(), parameters.third(), parameters.fourth(), parameters.fifth(), parameters.sixth(), parameters.seventh(), parameters.eighth(), parameters.ninth(), newParam);
		}
		
		throw new UnsupportedOperationException("Parameters are full!");
	}
}
