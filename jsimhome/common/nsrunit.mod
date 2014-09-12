// Common NSR unit library; last revised Mar 2011
// This file implements SI-based unit-compatibility

unit
	// fundamental units
	kg = fundamental,		// kilograms
	m = fundamental,		// meters
	sec = fundamental prefixable,	// seconds
	amp = fundamental prefixable,	// amperes
	degK = fundamental,		// degrees kelvin
	mol = fundamental prefixable,	// moles
	candela = fundamental prefixable, // candela

	// dimensionless
	steradian = dimensionless prefixable,
	scalar = dimensionless,
	percent = 1/100 scalar,

	// moles
	mole = mol prefixable,
	mmol = 1/1000 mol,
	umol = 1e-6 mol,
	nmol = 1e-9 mol,
	pmol = 1e-12 mol,

	// metric length
	meter = m prefixable,
	metre = m prefixable,
	mm = 1/1000 m,
	cm = 1e-2 m,
	km = 1000 m,
	micron = 1e-6 m,	
	um = micron,
	nm = 1e-9 m,
	angstrom = 1e-10 m prefixable,

	// english length
	inch = 2.54 cm,
	foot = 12 inch,
	yard = 3 foot,
	mile = 5280 foot,

	// mass
	gram = 1/1000 kg prefixable,
	g = gram,
	mg = 1e-3 gram,
	ug = 1e-6 gram,
	amu = 1.660531e-27 kg,
	dalton = amu prefixable,
	da = dalton,

	// time
	second = sec prefixable,
	s = sec,
	min = 60 sec,
	hour = 60 min,
	hr = hour,
	ms = 1e-3 sec,
	msec = 1e-3 sec,
	usec = 1e-6 sec,

	// frequency 
	hz = 1/sec,
	hertz = 1 hz prefixable,

	// volume
	ml = cm^3,
	liter = 1000 ml prefixable,
	litre = liter prefixable,
	L = liter,
	dL = 0.1 liter,
	mL = 1e-3 liter,
	uL = 1e-6 liter,

	// force
	N = kg*m/s^2 prefixable,
	newton = N prefixable,
	dyn = g*cm/s^2,
	dyne = dyn prefixable,

	// pressure
	pa = N/m^2,
	pascal = pa prefixable,
	atm = 101325 pa,
	mmHg = 1/760 atm,
	torr = mmHg prefixable,
	cmH2O = 1/1.3565 mmHg,
	bar = 1e5 pa,

	// energy
	erg = g*cm^2/s^2 prefixable,
	joule = kg*m^2/s^2 prefixable,
	J = joule,
	cal = 4.187 J prefixable,
	calorie = cal prefixable, 
	
	// power
	watt = J/sec prefixable,

	// viscosity
	poise = g/(cm*s) prefixable,
	p = poise,
	cp = 1/100 p,
	P = poise,
	cP = 1/100 P,

	// temperature
	degR = 5/9 degK,		// Rankine scale 
	K = degK,
	kelvin = K prefixable,

	// electric current
	A = amp prefixable,
	mA = 1/1000 amp,
	uA = 1e-6 amp,
	ampere = amp prefixable,

	// electric charge
	coulomb = amp*sec prefixable,
	faraday = 96485.341 coulomb/mol prefixable,

	// elec potential
	volt = watt/amp prefixable,
	mV = 1/1000 volt,
	
	// resistance
	ohm = volt/amp prefixable,
	mho = amp/volt prefixable,
	mmho = 1/1000 mho,

	// conductance
	siemens = amp/volt prefixable,
	mS = 1e-3 siemens,
	uS = 1e-6 siemens,

	// capacitance
	farad = coulomb/volt prefixable,
	uF = 1e-6 farad,

	// magnetic flux
	weber = volt*sec prefixable,
		
	// magnetic flux density
	tesla = weber/m^2 prefixable,

	// inductance
	henry = weber/amp prefixable,

	// magnetic flux density
	gauss = 1e-4 weber/m^2 prefixable,

	// magnetic field
	mu0 = 4e-7*3.14159265358979 henry/m,
	oersted = gauss/mu0 prefixable,

	// magnetomotive force
	gilbert = gauss*cm/mu0 prefixable,

	// concentration
	molar = mol/L prefixable,
	Molar = molar prefixable,
	M     = molar,
	mM    = 1e-3 Molar,	
	uM    = 1e-6 Molar,	
	nM    = 1e-9 Molar,	
	pM    = 1e-12 Molar,

	// catalytic activity
	katal = mole/sec prefixable,

	// luminous power
	lumen = candela*steradian prefixable,

	// illuminance
	lux = lumen/m^2 prefixable,

	// radioactive activity
	becquerel = 1/sec prefixable,

	// absorbed dose
	gray = joule/kg prefixable,
	sievert = gray prefixable
	;

// defined prefixes for prefixable units above
unit prefix yotta  = 1e24;
unit prefix zetta = 1e21;	
unit prefix exa = 1e18;	
unit prefix peta = 1e15; 	
unit prefix tera = 1e12; 	
unit prefix giga = 1e9; 	
unit prefix mega = 1e6; 	
unit prefix kilo = 1000; 	
unit prefix hecto = 100; 	
unit prefix deka = 10; 	
unit prefix deca = 10; 	
unit prefix deci = .1;
unit prefix centi = .01;
unit prefix milli = .001;
unit prefix micro = 1e-6;
unit prefix nano = 1e-9;
unit prefix pico = 1e-12;
unit prefix femto = 1e-15;
unit prefix atto = 1e-18;
unit prefix zepto = 1e-21;
unit prefix yocto = 1e-24;

