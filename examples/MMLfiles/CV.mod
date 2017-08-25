// TITLE: 


// SUBTITLE:
  


// CODE LANGUAGE:  MML


// CODE:

JSim v1.1
	import nsrunit;
	unit conversion on;    // Double slashes indicate a comment
	
	math MyModel {
	
realDomain t sec; t.min=0; t.max=600; t.delta=0.01;


// PARAMETERS:

real PRint = 0.12 sec;
real HR = 77 1/min;

// varying elastance model for ventricles

real EmaxLV = 5.91908558 mmHg/ml;   // Maximum elastance of left ventricle
real EminLV = 0.0922898032 mmHg/ml;   // Minimum elastance of left ventricle
real EmaxRV = 0.45938612 mmHg/ml;   // Maximum elastance of right ventricle
real EminRV = 0.0342539388 mmHg/ml;   // Minimum elastance of right ventricle

real VrestLVs = 23.6993603412 ml;   // Peak-systolic rest volume of left ventricle
real VrestLVd = 71.816243458 ml;   // Diastolic rest volume of left ventricle
real VrestRVs = 53.4983766234 ml;   // Peak-systolic rest volume of right ventricle
real VrestRVd = 102.8814935065 ml;   // Diastolic rest volume of right ventricle

// varying elastance for atria


real EmaxLA = 0.5056326299 mmHg/ml;   // Maximum elastance of left atrium
real EminLA = 0.404506104 mmHg/ml;   // Minimum elastance of left atrium
real EmaxRA = 0.3218841186 mmHg/ml;   // Maximum elastance of right atrium
real EminRA = 0.2575072949 mmHg/ml;   // Minimum elastance of right atrium

real VrestLAs = 73.9735665827 ml;     // Peak-systolic rest volume of left atrium
real VrestLAd = 73.9735665827 ml;     // Diastolic rest volume of left atrium
real VrestRAs = 75.9202393875 ml;     // Peak-systolic rest volume of right atrium
real VrestRAd = 75.9202393875 ml;     // Diastolic rest volume of right atrium

// systemic
real Rartcap = 0.6923076923 mmHg*sec/ml;   // Resistance of systemic arteries and capillaries
real RSysVeins = 0.1384615385 mmHg*sec/ml;      // Resistance of systemic veins
real RAorticValve = 0.0046153846 mmHg*sec/ml;   // Resistance of aortic valve
real RMitralValve = 0.0046153846 mmHg*sec/ml;   // Resistance of mitral valve

real CSysArtCaps = 8.6167741935 ml/mmHg;       // Compliance of systemic arteries and capillaries
real CSysVeins = 70.00951584 ml/mmHg;       // Compliance of systemic veins

real VrestSysArtCaps = 311.64 ml;  // Rest-volume of systemic arteries and capillaries
real VrestSysVeins = 1829.03 ml;    // Rest-volume of systemic veins


// pulmonic 
real RPulArtCaps = 0.0775384615 mmHg*sec/ml;   // Resistance of pulmonary arteries and capillaries
real RPulVeins = 0.0092307692 mmHg*sec/ml;   // Resistance of pulmonary veins
real RPulValve = 0.0046153846 mmHg*sec/ml;   // Resistance of pulmonary valve
real RTricuspidValve = 0.0046153846 mmHg*sec/ml;   // Resistance of tricuspid valve

real CPulArtCaps = 13.25 ml/mmHg;       // Compliance of pulmonary arteries and capillaries
real CPulVeins = 20.405 ml/mmHg;       // Compliance of pulmonary veins

real VrestPulArtCaps = 74.2 ml;   // Rest-volume of pulmonary arteries and capillaries
real VrestPulVeins = 169.07 ml;     // Rest-volume of pulmonary veins


// VARIABLES

real ActFuncVentricles(t) dimensionless;   // Activation function for ventricular elastance
real ActFuncAtria(t) dimensionless;   // Activation function for atrial elastance

real ELV(t) mmHg/ml;        // Elastance of left ventricle
real VrestLV(t) ml;         // Rest volume of left ventricle 
real VLV(t) ml;             // Volume of left ventricle
realState EDVLV(t) ml;      // End-diastolic volume of left ventricle

real ERV(t) mmHg/ml;        // Elastance of right ventricle
real VrestRV(t) ml;         // Rest volume of right ventricle 
real VRV(t) ml;             // Volume of right ventricle
realState EDVRV(t) ml;      // End-diastolic volume of right ventricle

// Variables for systemic circulation
real Paorta(t)  mmHg;       // Transmural pressure in aorta
real PSysVeins(t)  mmHg;     // Transmural pressure in systemic veins
real PLV(t)  mmHg;           // Transmural pressure in left ventricle
real PLA(t)  mmHg;           // Transmural pressure in left atrium

real VrestLA(t) ml;         // Rest volume of left atrium
real VLA(t) ml;             // Volume of left atrium
real ELA(t) mmHg/ml;      // Elastance of left atrium

real VSysArtCaps(t) ml;   // Volume of systemic arteries and capillaries
real VSysVeins(t) ml;     // Volume of systemic veins

real FSysArtCaps(t) ml/sec; // Flow through systemic arteries and capillaries
real FSysVeins(t) ml/sec;   // Flow through systemic veins
real FAorticValve(t) ml/sec;   // Flow through aortic valve
real FMitralValve(t) ml/sec; // Flow through mitral valve
real CardiacOutput(t) L/min;     // Cardiac output

// Variables for pulmonary circulation 
real PPulArtCaps(t) mmHg;   // Transmural pressure in pulmonary arteries and capillaries
real PPulVeins(t) mmHg;     // Transmural pressure in pulmonary veins
real PRV(t) mmHg;           // Transmural pressure in right ventricle
real PRA(t) mmHg;           // Transmural pressure in right atrium

real VrestRA(t) ml;         // Rest volume of right atium
real VRA(t) ml;             // Volume of right atrium
real ERA(t) mmHg/ml;        // Elastance of right atrium

real VPulArtCaps(t) ml;     // Volume of pulmonary arteries and capillaries
real VPulVeins(t) ml;       // Volume of pulmonary veins

real FPulVeins(t) ml/sec;    // Flow through pulmonary veins
real FPulArtCaps(t) ml/sec;  // Flow through pulmonary arteries and capillaries
real FPulValve(t) ml/sec;    // Flow through pulmonary valve
real FTricuspidValve(t) ml/sec;  // Flow through tricuspid valve

real Vtotal(t) ml;		// Total blood volume


// EQUATIONS:

ActFuncAtria = if (sin(2*PI*t*HR) >= 0 ) sin(2*PI*t*HR)   
               else 0;                                  // Custom activation fxn

ActFuncVentricles = if (sin(2*PI*(t-PRint)*HR) >= 0) sin(2*PI*(t-PRint)*HR)
                    else 0;                             // Custom activation fxn

// Left & right ventricle
ELV = (EmaxLV-EminLV)*ActFuncVentricles + EminLV;       // Custom fxn
VrestLV = (1-ActFuncVentricles)*(VrestLVd-VrestLVs) + VrestLVs;  // Custom fxn
PLV = ELV*(VLV-VrestLV);    // Law of elastance

ERV = (EmaxRV-EminRV)*ActFuncVentricles + EminRV;  // Custom fxn
VrestRV = (1-ActFuncVentricles)*(VrestRVd-VrestRVs) + VrestRVs;  // Custom fxn
PRV = ERV*(VRV-VrestRV);    // Law of elastance

// Left atrium
ELA = ActFuncAtria*(EmaxLA-EminLA) + EminLA;  // Custom fxn
VrestLA = (1-ActFuncAtria)*(VrestLAd-VrestLAs) + VrestLAs;  // Custom fxn
PLA = ELA*(VLA-VrestLA);  // Law of elastance

// Right atrium
ERA = ActFuncAtria*(EmaxRA-EminRA) + EminRA;  // Custom fxn
VrestRA = (1-ActFuncAtria)*(VrestRAd-VrestRAs) + VrestRAs;  // Custom fxn
PRA = ERA*(VRA-VrestRA);  // Law of elastance

FTricuspidValve = if (PRA>PRV) (PRA-PRV)/RTricuspidValve         // Ohm's Law (conditional)
      else 0;

FAorticValve = if (PLV>Paorta) (PLV-Paorta)/RAorticValve    // Ohm's Law (conditional)
      else 0;                            

FMitralValve = if (PLA>PLV) (PLA-PLV)/RMitralValve   // Ohm's Law (conditional)
      else 0;

CardiacOutput:t = (FAorticValve-CardiacOutput)/(15sec);  // Low-pass filter


// Equations for systemic circulation
Paorta = ((VSysArtCaps-VrestSysArtCaps)/CSysArtCaps);  // Law of elastance

FSysArtCaps = (Paorta-PSysVeins)/Rartcap;  // Ohm's Law

PSysVeins = ((VSysVeins-VrestSysVeins)/CSysVeins);  // Law of elastance

FSysVeins = (PSysVeins-PRA)/RSysVeins;   // Ohm's Law
    

// Equations for pulmonary circulation
PPulArtCaps = ((VPulArtCaps-VrestPulArtCaps)/CPulArtCaps);  // Law of elastance

PPulVeins = ((VPulVeins-VrestPulVeins)/CPulVeins);  // Law of elastance

FPulValve = if (PRV>PPulArtCaps) (PRV-PPulArtCaps)/RPulValve  // Ohm's Law (conditional)
      else 0;                            // One-way valve

FPulVeins = (PPulVeins-PLA)/RPulVeins;    // Ohm's Law

FPulArtCaps = (PPulArtCaps-PPulVeins)/RPulArtCaps;   // Ohm's Law

Vtotal = VLV+VSysArtCaps+VSysVeins+VRA+VRV+VPulArtCaps+VPulVeins+VLA;  // Summation of volumes


// ODEs
VLV:t = FMitralValve-FAorticValve; // Kirchoff Current Law
VSysArtCaps:t = FAorticValve-FSysArtCaps; // Kirchoff Current Law
VSysVeins:t = FSysArtCaps-FSysVeins; // Kirchoff Current Law
VRV:t = FTricuspidValve-FPulValve; // Kirchoff Current Law
VPulArtCaps:t = FPulValve-FPulArtCaps; // Kirchoff Current Law
VPulVeins:t = FPulArtCaps-FPulVeins; // Kirchoff Current Law
VLA:t = FPulVeins-FMitralValve; // Kirchoff Current Law
VRA:t = FSysVeins-FTricuspidValve; // Kirchoff Current Law


when (t=t.min) {                       //Initial conditions
     VSysArtCaps = 1113;
     VSysVeins = 3153.5;
     VLV = 125.9934095755;
     VPulArtCaps = 265;
     VPulVeins = 291.5;
     VRV = 175.8658008658;
     VLA = 87.5703947794;
     VRA = 87.5703947794;
     CardiacOutput = 6.5;
     EDVLV = VLV(t.min);
       EDVRV = VRV(t.min);
     }

event(sin(2*PI*(t-PRint)*HR) >= 0 and sin(2*PI*(t-PRint-t.delta)*HR) < 0) {
EDVLV = VLV;
EDVRV = VRV;
}

}
