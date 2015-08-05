package semsim.model.computational.units;

public class UnitFactor{
	private UnitOfMeasurement baseUnit;
	private double exponent;
	private String prefix;
	private double multiplier;
	
	public UnitFactor(UnitOfMeasurement baseUnit, double exponent, String prefix){
		setBaseUnit(baseUnit);
		setExponent(exponent);
		setPrefix(prefix);
	}

	public UnitFactor(UnitFactor uftocopy) {
		exponent = uftocopy.exponent;
		prefix = new String(uftocopy.prefix);
		multiplier = uftocopy.multiplier;
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
	
	public double getMultiplier(){
		return multiplier;
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
	
	public void setMultiplier(double val){
		this.multiplier = val;
	}
}