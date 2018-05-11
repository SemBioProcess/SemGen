package semsim.model.computational.units;

/**
 * When specifying the physical units for a model variable, unit factors 
 * are used to indicate the more fundamental units that make up a
 * physical unit.
 * @author mneal
 *
 */
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
	
	public UnitFactor(UnitOfMeasurement baseUnit, double exponent, String prefix, double multiplier){
		setBaseUnit(baseUnit);
		setExponent(exponent);
		setPrefix(prefix);
		setMultiplier(multiplier);
	}

	/**
	 * Copy constructor
	 * @param uftocopy The UnitFactor to copy
	 */
	public UnitFactor(UnitFactor uftocopy) {
		baseUnit = uftocopy.baseUnit;
		exponent = uftocopy.exponent;
		if (uftocopy.prefix!=null) {
			prefix = new String(uftocopy.prefix);
		}
		multiplier = uftocopy.multiplier;
	}
	
	/** @return The base unit for the unit factor. i.e. the UnitFactor's units. */
	public UnitOfMeasurement getBaseUnit() {
		return baseUnit;
	}
	
	/** @return The power to which the base unit is raised */
	public double getExponent() {
		return exponent;
	}

	/** @return The unit prefix on the UnitFactor's base unit (e.g. "milli","kilo", etc.) */
	public String getPrefix() {
		return prefix;
	}
	
	/** @return The scaling factor applied to the UnitFactor's base unit */
	public double getMultiplier(){
		return multiplier;
	}
	
	/**
	 * @param baseUnit The base unit for this UnitFactor
	 */
	public void setBaseUnit(UnitOfMeasurement baseUnit) {
		this.baseUnit = baseUnit;
	}

	/**
	 * @param exponent The power to which the base unit in this UnitFactor should be raised
	 */
	public void setExponent(double exponent) {
		this.exponent = exponent;
	}
	
	/**
	 * @param prefix The unit prefix to apply to this UnitFactor's base unit
	 */
	public void setPrefix(String prefix) {
		this.prefix = prefix;
	}
	
	/**
	 * @param val The scaling factor to apply to this UnitFactor's base unit 
	 */
	public void setMultiplier(double val){
		this.multiplier = val;
	}
}
