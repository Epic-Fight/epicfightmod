package yesman.epicfight.api.utils.math;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public interface ValueModifier {
	public static final Codec<Unified> CODEC =
		RecordCodecBuilder.create(instance -> instance.group(
			Codec.FLOAT.fieldOf("adder").forGetter(Unified::adder),
			Codec.FLOAT.fieldOf("multiplier").forGetter(Unified::multiplier),
			Codec.FLOAT.fieldOf("setter").forGetter(Unified::setter)
		).apply(instance, Unified::new)
	);
	
	public void attach(ResultCalculator calculator);
	
	public static ValueModifier adder(float value) {
		return new Adder(value);
	}
	
	public static ValueModifier multiplier(float value) {
		return new Multiplier(value);
	}
	
	public static ValueModifier setter(float arg) {
		return new Setter(arg);
	}
	
	public static record Adder(float adder) implements ValueModifier {
		@Override
		public void attach(ResultCalculator calculator) {
			calculator.add += this.adder;
		}
	}
	
	public static record Multiplier(float multiplier) implements ValueModifier {
		@Override
		public void attach(ResultCalculator calculator) {
			calculator.multiply *= this.multiplier;
		}
	}
	
	public static record Setter(float setter) implements ValueModifier {
		@Override
		public void attach(ResultCalculator calculator) {
			if (Float.isNaN(calculator.set)) {
				calculator.set = this.setter;
			} else if (!Float.isNaN(this.setter)) {
				calculator.set = Math.min(calculator.set, this.setter);
			}
		}
	}
	
	public static record Unified(float adder, float multiplier, float setter) implements ValueModifier {
		@Override
		public void attach(ResultCalculator calculator) {
			if (Float.isNaN(calculator.set)) {
				calculator.set = this.setter;
			} else if (!Float.isNaN(this.setter)) {
				calculator.set = Math.min(calculator.set, this.setter);
			}
			
			calculator.add += this.adder;
			calculator.multiply *= this.multiplier;
		}
	}
	
	public static ResultCalculator calculator() {
		return new ResultCalculator();
	}
	
	public static class ResultCalculator implements ValueModifier {
		private float set = Float.NaN;
		private float add = 0.0F;
		private float multiply = 1.0F;
		
		public ResultCalculator attach(ValueModifier valueModifier) {
			valueModifier.attach(this);
			return this;
		}
		
		@Override
		public void attach(ResultCalculator calculator) {
			if (Float.isNaN(calculator.set)) {
				calculator.set = this.set;
			} else if (!Float.isNaN(this.set)) {
				calculator.set = Math.min(calculator.set, this.set);
			}
			
			calculator.add += this.add;
			calculator.multiply *= this.multiply;
		}
		
		public ValueModifier toValueModifier() {
			if (Float.isNaN(this.set)) {
				if (Float.compare(this.add, 0.0F) == 0 && Float.compare(this.multiply, 1.0F) != 0) {
					return new Multiplier(this.multiply);
				} else if (Float.compare(this.add, 0.0F) != 0 && Float.compare(this.multiply, 1.0F) == 0) {
					return new Adder(this.add);
				}
			} else if (Float.compare(this.add, 0.0F) == 0 && Float.compare(this.multiply, 1.0F) == 0) {
				return new Setter(this.set);
			}
			
			return new Unified(this.set, this.add, this.multiply);
		}
		
		public void set(float f) {
			this.set = f;
		}
		
		public void add(float f) {
			this.add += add;
		}
		
		public void multiply(float f) {
			this.multiply *= f;
		}
		
		public float getResult(float baseValue) {
			float result = baseValue;
			
			if (!Float.isNaN(this.set)) {
				result = this.set;
			}
			
			result += this.add;
			result *= this.multiply;
			
			return result;
		}
	}
}