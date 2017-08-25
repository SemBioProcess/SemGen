JSim v1.1
import nsrunit;
unit conversion on;

// This model was originally coded by
// Dan Beard (DB) and based on Lu et al. (2001).  An aortic
// pressure waveform from Stergiopulos et al. (1999) is used as input
// to the baroreceptor pathway.  There is no circulatory model present,
// and the aortic waveform remains unchanged by the resulting heart rate,
// ventricular contractility and arterial resistance values.


math baro{
realDomain t sec; t.min=0; t.max=12.0; t.delta=0.01;


//
// I. Baroreceptor Firing Rate, Nbr.
 
// References: The governing equations for Nbr is adopted from Spickler JW, Kezdi P, and Geller E. 
//  Transfer characteristics of the carotid sinus pressure control system. In: Baroreceptors and
//  Hypertension, edited by Kezdi P. Pergamon, Dayton, OH, 1665, pp. 31-40.
//


// Forcing pressures (Paop)
extern real Paop(t) mmHg;
real PaopFOL(t) mmHg;
real tauFOL = 0.001 sec;

// Heart rate control parameters
real h1 = 35 min^(-1);         // Heart rate control offset parameter
real h2 = 140 min^(-1);        // Heart rate control scaling parameter
real h3 = 40 min^(-1);         // Heart rate control scaling parameter
real h4 = 32 min^(-1);         // Heart rate control scaling parameter
real h5 = 10 min^(-1);         // Heart rate control scaling parameter
real h6 = 20 min^(-1);         // Heart rate control scaling parameter

real HR(t) min^(-1);     // Dynamic heart rate

// Contractility functions
real aF_con(t) dimensionless;  // Dynamic contractility scaling function
real bF_con(t) dimensionless;  // Contractility variable to alter Lu et al. (2001)
                               // activation function
real amin = -2 dimensionless;  // Contractility control offset 
real bmin = 0.7 dimensionless; // Contractility control offset
real Ka = 5 dimensionless;     // Contractility control scaling factor
real Kb = 0.5 dimensionless;   // Contractility control scaling factor

// Vasomotor tone
real Rsa(t) mmHg*sec*ml^(-1);
real Vsa = 225 ml;
real Kr = 0.04       mmHg*sec*ml^(-1);    // Pressure scaling constant for systemic
real Vsa_max = 250   ml;                  // Maximal luminal volume of systemic arteries
                                         // arterial resistance

// Baro parameters
real a = 0.001		sec;			// Baroreceptor time constant parameter
  // NOTE: I made up the value for 'a' without any reference (DB).
real a1 = 0.036		sec;			// Baroreceptor time constant parameter
real a2 = 0.0018	sec;			// Baroreceptor time constant parameter
real K = 1.0		sec^(-1)*mmHg^(-1);	// Baroreceptor gain

// Variables
real Nbr(t)		sec^(-1);		// Baro firing rate (impulses per sec)
real Nbr_t(t)
		sec^(-2);		// Time derivative of baro firing rate

// Initial values:
when(t=t.min) {
Nbr  = 175;
Nbr_t = 0;
PaopFOL = 100;
}

PaopFOL:t = (Paop-PaopFOL)/tauFOL;

// Governing equation:
Nbr:t = Nbr_t;
a2*a*(Nbr_t:t) + (a2+a)*Nbr_t + Nbr = K*(Paop + a1*(PaopFOL:t));



//
// IV. Central nervous system. CNS filters the Nbr signal and outputs the following:
//	1. Nhrv,Fhrv - vagal pathway firing rate
//	2. Nhrs,Fhrs - sympathetic pathway conroling heart rate firing rate
//	3. Ncon,Fcon - sympathetic pathway conroling contractility
//	4. Nvaso,Fvaso - controlling vasomotor tone
//
// The general form of the governing equations is:
//   T_x*(N_x:t) + N_x = K_x*Nbr(t-L_x), and
//   F_x = a_x + b_x / (exp(tau_x*(N_x-No_x)) + 1.0),
// where the subscript "x" denotes the pathway name.
//
//
// IVa. HRV pathway
//
// Parameters:
real K_hrv = 0.8		dimensionless;	
real T_hrv = 1.8		sec;
real L_hrv = 0.2		sec;
real a_hrv = 0		        dimensionless;
real b_hrv = 1.0		dimensionless;
real tau_hrv = -0.04		sec;
real No_hrv = 110		sec^(-1);

// Varaibles:
real N_hrv(t)		sec^(-1);
real F_hrv(t)		dimensionless;

// Initial Values:
when(t=t.min) {
N_hrv = 120;
}

// Governing Equations:
N_hrv:t = if (t>L_hrv) (-N_hrv + (K_hrv*Nbr(t-L_hrv)))/T_hrv
	else 0; 
F_hrv = a_hrv + (b_hrv / (exp(tau_hrv*(N_hrv-No_hrv)) + 1.0)); 

// IVb. HRS pathway
//
// Parameters:
real K_hrs = 1.0		dimensionless;	
real T_hrs = 10.0		sec;
real L_hrs = 3.0		sec;
real a_hrs = 0.3		dimensionless;
real b_hrs = 0.7		dimensionless;
real tau_hrs = 0.09		sec;
real No_hrs = 100		sec^(-1);

// Varaibles:
real N_hrs(t)		sec^(-1);
real F_hrs(t)		dimensionless;

// Initial Values:
when(t=t.min) {
N_hrs = 120;
}

// Governing Equations:
N_hrs:t = if (t>L_hrs) (-N_hrs + (K_hrs*Nbr(t-L_hrs)))/T_hrs
	else 0;  
F_hrs = a_hrs + (b_hrs / (exp(tau_hrs*(N_hrs-No_hrs)) + 1.0));

// IVc. CON pathway
//
// Parameters:
real K_con = 1.0		dimensionless;	
real T_con = 10.0		sec;
real L_con = 3.0		sec;
real a_con = 0.3		dimensionless;
real b_con = 0.7		dimensionless;
real tau_con = 0.04		sec;
real No_con = 110		sec^(-1);

// Varaibles:
real N_con(t)				sec^(-1);
real F_con(t)		dimensionless;

// Initial Values:
when(t=t.min) {
N_con = 125;
}

// Governing Equations:
N_con:t = if (t>L_con) (-N_con + (K_con*Nbr(t-L_con)))/T_con
	else 0;  
F_con = a_con + (b_con / (exp(tau_con*(N_con-No_con)) + 1.0)); 

// IVd. VASO pathway

// Parameters:
real K_vaso = 1.0		dimensionless;	
real T_vaso = 6.0		sec;
real L_vaso = 3.0		sec;
real a_vaso = 0.3		dimensionless;
real b_vaso = 0.7		dimensionless;
real tau_vaso = 0.04		sec;
real No_vaso = 110		sec^(-1);

// Varaibles:
real N_vaso(t)			sec^(-1);
real F_vaso(t)			dimensionless;

// Initial Values:
when(t=t.min) {
N_vaso = 130;
}

// Governing Equations:
N_vaso:t = if (t>L_vaso) (-N_vaso + (K_vaso*Nbr(t-L_vaso)))/T_vaso
	else 0; 
F_vaso = a_vaso + (b_vaso / (exp(tau_vaso*(N_vaso-No_vaso)) + 1.0)); 

// Efferent pathways

// Heart rate control
HR = (h1 + (h2*F_hrs)-(h3*F_hrs^2)-(h4*F_hrv)+
(h5*F_hrv^2)-(h6*F_hrv*F_hrs));       // Lu et al. Eq.  (8)

// Contractility control
aF_con = amin + (Ka*F_con);
bF_con = bmin + (Kb*F_con);

// Vasomotor tone
Rsa = (Kr*exp(4*F_vaso)) + (Kr*(Vsa_max/Vsa)^2);// Sys. arteries: Lu et al. Eq.(16)

}

// REFERENCES:
// Lu K, Clark JW, Ghorbel FH, Ware DL, Bidani A.
// A human cardiopulmonary system model applied to the analysis
// of the Valsalva maneuver.  Am J Physiol Heart Circ Physiol.
// 281: H2661-H2679, 2001. 

// Stergiopulos N, Westerhof BE, Westerhof N. Total arterial
// inertance as the fourth element of the windkessel model.
// Am J Physiol 276: H81-H88, 1999.
