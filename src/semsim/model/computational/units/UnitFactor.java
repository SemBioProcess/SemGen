package semsim.model.computational.units;

public class UnitFactor{
	private UnitOfMeasurement baseUnit;
	private double exponent;
	private String prefix;
	
	public UnitFactor(UnitOfMeasurement baseUnit, double exponent, String prefix){
		setBaseUnit(baseUnit);
		setExponent(exponent);
		setPrefix(prefix);
	}

	public UnitOfMeasurement getBaseUnit() {
		return baseUnit;
	}
	
	public double getExponent() {
		return exponent;
	}

	public String getPrefix() {
		return prefix;
	}
	
	public void setBaseUnit(UnitOfMeasurement baseUnit) {
		this.baseUnit = baseUnit;
	}

	public void setExponent(double exponent) {
		this.exponent = exponent;
	}
	
	public void setPrefix(String prefix) {
		this.prefix = prefix;
	}
}