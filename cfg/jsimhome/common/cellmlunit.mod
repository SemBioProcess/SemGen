JSim v1.1

// CellML defined units

// prefixes
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

unit
	// fundamental units
	kilogram = fundamental prefixable,	
	meter = fundamental prefixable,	
	second = fundamental prefixable, 
	ampere = fundamental prefixable, 
	kelvin = fundamental prefixable,
	mole = fundamental prefixable,
	candela = fundamental prefixable, 

	// dimensionless
	steradian = dimensionless prefixable,

	// metric length
	metre = meter prefixable,

	// mass
	gram = 1e-3 kilogram prefixable,
	dalton = 1.6605387e-24 gram prefixable,

	// frequency 
	hertz = 1/second prefixable,

	// volume
	liter = 1 decimeter^3 prefixable,
	litre = liter prefixable,

	// force
	newton = kilogram*meter/second^2 prefixable,
	dyne = gram*centimeter/second^2 prefixable,

	// pressure
	pascal = newton/meter^2 prefixable,
	torr = 133.32239 pascal prefixable,

	// energy
	erg = gram*centimeter^2/second^2 prefixable,
	joule = kilogram*meter^2/second^2 prefixable,
	calorie = 4.1868 joule prefixable, 
	
	// power
	watt = joule/second prefixable,

	// viscosity
	poise = gram/(centimeter*second) prefixable,

	// electric charge
	coulomb = ampere*second prefixable,
	faraday = 96485.341 coulomb prefixable,

	// elec potential
	volt = watt/ampere prefixable,
	
	// resistance
	ohm = volt/ampere prefixable,
	mho = ampere/volt prefixable,

	// conductance
	siemens = ampere/volt prefixable,

	// capacitance
	farad = coulomb/volt prefixable,

	// magnetic flux
	weber = volt*second prefixable,
		
	// magnetic flux density
	tesla = weber/meter^2 prefixable,

	// inductance
	henry = weber/ampere prefixable,

	// magnetic flux density
	gauss = 1e-4 weber/meter^2 prefixable,

	// magnetic field
	oersted = 79.577472 ampere/meter prefixable,

	// magnetomotive force
	gilbert = oersted*centimeter prefixable,

	// concentration
	molar = mole/liter prefixable,

	// catalytic activity
	katal = mole/second prefixable,

	// luminous power
	lumen = candela*steradian prefixable,

	// illuminance
	lux = lumen/meter^2 prefixable,

	// radioactive activity
	becquerel = 1/second prefixable,

	// absorbed dose
	gray = joule/kilogram prefixable,
	sievert = gray prefixable
	;

