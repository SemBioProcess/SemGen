JSim v1.1
// MODEL NUMBER: 0165
// MODEL NAME: Luo-Rudy
// SHORT DESCRIPTION: The Modified Luo-Rudy Dynamic Model of the Mammalian Ventricular Myocyte.


// This is a JSIM.mod adaptation of the C-code detailed below.
// It has been substantially modified, converting many of
// the expressions into ordinary differential equations.
// Rapid equilibria have been transformed into "fast" ODE's
// in order to avoid iterative procedures, complex polynomial
// solutions, etc.
// This adaptation was done by Gary M. Raymond
// December 12, 2001
// Original "C" code from
// http://www.cwru.edu/med/CBRTC/LRdOnline/LRdModel.c
//
// Technique: C-code was basically adapted, block for block.
// C subroutines for currents were converted to ordinary differential
// equations where necessary. Stimulus was replaced by JSIM's
// external function (extern real st(t);). Information pertaining to
// step size was removed and replaced by JSIM's realDomain t.
// Recording of maximum excursions and a beat by beat basis was
// deleted. Double declarations (C construct) were replaced by
// real declarations (JSIM's MML construct which are double precision
// variables.) The C subroutines were converted to inline code
// and placed earlier in the structure. Fast equilibria were
// were replaced by ODE's to avoid solving quadratic and cubic
// equations--the special innovation of the LR model.
//
// ----------------------------------------------------------------------
//
//         ****************************
//         * MODIFICATIONS BEGIN HERE *
//         ****************************
//
// The Luo-Rudy Dynamic (LRd) Model of the Mammalian Ventricular Myocyte 
// Gregory Faber 
// This code requires a C++ compiler 
// Detailed list of equations and model description are provided in 
//
//  Circ Res 1991;68:1501-1526      
//  Circ Res 1994;74:1071-1096      
//  Circ Res 1994;74:1097-1113      
//  Circ Res 1995;77:140-152        
//  Biophys J 1995;68:949-964       
//  Cardiovasc Res 1997;35:256-272  
//  Circulation 1999;99:2466-2474   
//  Cardiovas Res 1999;42:530-542   
//  Nature 1999;400:566-569         
//  Circulation 2000;101:1192-1198  
//  Biophy J 2000;78:2392-2404    

math LRd {
realDomain t; t.min=0.0; t.max = 1000; t.delta=0.1; 


// List of variables and paramaters (this code uses all global variables)   


     // Cell Geometry 
     real l = 0.01;       // Length of the cell (cm)
     real a = 0.0011;     // Radius of the cell (cm)
     real pi = 3.141592;  // Pi
     real vcell;   // Cell volume (uL)
     real ageo;    // Geometric membrane area (cm^2)
     real acap;    // Capacitive membrane area (cm^2)
     real vmyo;    // Myoplasm volume (uL)
     real vmito;   // Mitochondria volume (uL)
     real vsr;     // SR volume (uL)
     real vnsr;    // NSR volume (uL)
     real vjsr;    // JSR volume (uL)
     real vcleft;  // Cleft volume (uL)
     
     // Voltage 
     real v(t);       // Membrane voltage (mV)
     real dvdt(t);    // Change in Voltage / Change in Time (mV/ms)
     real boolean(t); // boolean condition to test for dvdtmax
     
     
     
     // Total Current and Stimulus 
     extern real st(t); // External function for stimulus (uA/cm^2)
     real it(t);           // Total current (uA/cm^2)

     // Terms for Solution of Conductance and Reversal Potential 
     real R = 8314;      // Universal Gas Constant (J/kmol*K)
     real frdy = 96485;  // Faraday's Constant (C/mol)
     real temp = 310;    // Temperature (K)
     real RToF = R*temp/frdy; // RT over F
     real FoRT = 1/RToF;      // FoRT

     // Ion Valences 
     real zna = 1;  // Na valence
     real zk = 1;   // K valence
     real zca = 2;  // Ca valence

     // Ion Concentrations 
     real nai(t);    // Intracellular Na Concentration (mM)
     real nao(t);    // Extracellular Na Concentration (mM)
     real nabm(t);   // Bulk Medium Na Concentration (mM)
     real dnao(t);   // Change in Cleft Na Concentration (mM)
     real ki(t);     // Intracellular K Concentration (mM)
      real ko(t);     // Extracellular K Concentration (mM)
     real kbm(t);    // Bulk Medium K Concentration (mM)
      real dko(t);    // Change in Cleft K Concentration (mM)
     real cai(t);    // Intracellular Ca Concentration (mM)
     real cao(t);    // Extracellular Ca Concentration (mM)
     real cabm(t);   // Bulk Medium Ca Concentration (mM)
     real dcao(t);   // Change in Cleft Ca Concentration (mM)
     real cmdn(t);   // Calmodulin Buffered Ca Concentration (mM)
     real trpn(t);   // Troponin Buffered Ca Concentration (mM)
     real nsr(t);    // NSR Ca Concentration (mM)
     real Cjsr(t);    // JSR Ca Concentration (mM)
     real csqn(t);   // Calsequestrin Buffered Ca Concentration (mM)
     real taudiff = 1000; //Diffusion Constant for Ion Movement from Bulk Medium to Cleft Space

     // Myoplasmic Na Ion Concentration Changes 
     real naiont(t);  // Total Na Ion Flow (uA/uF)
     real dnai(t);    // Change in Intracellular Na Concentration (mM)

     // Myoplasmic K Ion Concentration Changes 
     real kiont(t); // Total K Ion Flow (uA/uF)
     real dki(t);   // Change in Intracellular K Concentration (mM)

     // NSR Ca Ion Concentration Changes 
     real dnsr(t);   // Change in [Ca] in the NSR (mM)
     real iup(t);    // Ca uptake from myo. to NSR (mM/ms)
     real ileak(t);  // Ca leakage from NSR to myo. (mM/ms)
     real kleak;  // Rate constant of Ca leakage from NSR (ms^-1)
     real kmup = 0.00092;    // Half-saturation concentration of iup (mM)
     real iupbar = 0.00875;  // Max. current through iup channel (mM/ms)
     real nsrbar = 15;       // Max. [Ca] in NSR (mM) 
     
     // JSR Ca Ion Concentration Changes 
     real djsr(t);      // Change in [Ca] in the JSR (mM)
     real tauon = 2;    // Time constant of activation of Ca release from JSR (ms)
     real tauoff = 2;   // Time constant of deactivation of Ca release from JSR (ms)
//   real tcicr(t);        // t=0 at time of CICR (ms)
     real irelcicr(t);     // Ca release from JSR to myo. due to CICR (mM/ms)
     real csqnth = 8.75;// Threshold for release of Ca from CSQN due to JSR overload (mM)
     real gmaxrel = 150;// Max. rate constant of Ca release from JSR due to overload (ms^-1)
     real grelbarjsrol; // Rate constant of Ca release from JSR due to overload (ms^-1)
//     real greljsrol(t);    // Rate constant of Ca release from JSR due to CICR (ms^-1)
//     real tjsrol(t);       // t=0 at time of JSR overload (ms)
//     real ireljsrol(t);    // Ca release from JSR to myo. due to JSR overload (mM/ms)
     real csqnbar = 10; // Max. [Ca] buffered in CSQN (mM)
     real kmcsqn = 0.8; // Equilibrium constant of buffering for CSQN (mM)
//   real on(t);           // Time constant of activation of Ca release from JSR (ms)
//   real off(t);          // Time constant of deactivation of Ca release from JSR (ms)
//   real magrel(t);       // Magnitude of Ca release
//   real dcaiont;      // Rate of change of Ca entry
//   real dcaiontnew;   // New rate of change of Ca entry
//   real caiontold;    // Old rate of change of Ca entry

     // Translocation of Ca Ions from NSR to JSR 
     real itr(t);                // Translocation current of Ca ions from NSR to JSR (mM/ms)
     real tautr = 180;  // Time constant of Ca transfer from NSR to JSR (ms)
     
     // Myoplasmic Ca Ion Concentration Changes 
     real caiont(t);  // Total Ca Ion Flow (uA/uF)
     real dcai(t); // Change in myoplasmic Ca concentration (mM)
     real catotal(t); // Total myoplasmic Ca concentration (mM)
//      
     real cmdnbar = 0.050;   // Max. [Ca] buffered in CMDN (mM)
     real trpnbar = 0.070;   // Max. [Ca] buffered in TRPN (mM)
     real kmcmdn = 0.00238;  // Equilibrium constant of buffering for CMDN (mM)
     real kmtrpn = 0.0005;   // Equilibrium constant of buffering for TRPN (mM)

     // Fast Sodium Current (time dependent) 
     real ina(t);    // Fast Na Current (uA/uF)
     real gna;    // Max. Conductance of the Na Channel (mS/uF)
     real ena(t);    // Reversal Potential of Na (mV)
     real am(t);     // Na alpha-m rate constant (ms^-1)
     real bm(t);     // Na beta-m rate constant (ms^-1)
     real ah(t);     // Na alpha-h rate constant (ms^-1)
     real bh(t);     // Na beta-h rate constant (ms^-1)
     real aj(t);     // Na alpha-j rate constant (ms^-1)
     real bj(t);     // Na beta-j rate constant (ms^-1)
     real m(t);      // Na activation
     real h(t);      // Na inactivation
     real j(t);      // Na inactivation

     // Current through L-type Ca Channel 
     real ilca(t);    // Ca current through L-type Ca channel (uA/uF)
     real ilcana(t);  // Na current through L-type Ca channel (uA/uF)
     real ilcak(t) ;  // K current through L-type Ca channel (uA/uF)
     real ilcatot(t); // Total current through the L-type Ca channel (uA/uF)
     real ibarca(t) ;  // Max. Ca current through Ca channel (uA/uF)
     real ibarna(t) ;  // Max. Na current through Ca channel (uA/uF)
     real ibark(t);   // Max. K current through Ca channel (uA/uF)
     real d(t);       // Voltage dependent activation gate
     real dss(t);     // Steady-state value of activation gate d 
     real taud(t);    // Time constant of gate d (ms^-1)
     real f(t);       // Voltage dependent inactivation gate
     real fss(t);     // Steady-state value of inactivation gate f
     real tauf(t);    // Time constant of gate f (ms^-1)
     real fca(t);     // Ca dependent inactivation gate
     real kmca = 0.0006;     // Half-saturation concentration of Ca channel (mM)
     real pca = 0.00054;     // Permiability of membrane to Ca (cm/s)
     real gacai = 1;         // Activity coefficient of Ca
     real gacao = 0.341;     // Activity coefficient of Ca
     real pna = 0.000000675; // Permiability of membrane to Na (cm/s)
     real ganai = 0.75;      // Activity coefficient of Na
     real ganao = 0.75;      // Activity coefficient of Na
     real pk = 0.000000193;  // Permiability of membrane to K (cm/s)
     real gaki = 0.75;       // Activity coefficient of K
     real gako = 0.75;       // Activity coefficient of K

     // Current through T-type Ca Channel 
     real icat(t);    // Ca current through T-type Ca channel (uA/uF)
     real gcat;    // Max. Conductance of the T-type Ca channel (mS/uF)
     real eca(t);     // Reversal Potential of the T-type Ca channel (mV)
     real b(t);       // Voltage dependent activation gate
     real bss(t);     // Steady-state value of activation gate b 
     real taub(t);    // Time constant of gate b (ms^-1)
     real g(t);       // Voltage dependent inactivation gate
     real gss(t);     // Steady-state value of inactivation gate g
     real taug(t);    // Time constant of gate g (ms^-1)

     // Rapidly Activating Potassium Current   
     real ikr(t);   // Rapidly Activating K Current (uA/uF)
     real gkr(t);   // Channel Conductance of Rapidly Activating K Current (mS/uF)
     real ekr(t);   // Reversal Potential of Rapidly Activating K Current (mV)
     real xr(t);    // Rapidly Activating K time-dependent activation
     real xrss(t);  // Steady-state value of inactivation gate xr
     real tauxr(t); // Time constant of gate xr (ms^-1)
     real rkt(t);     // K time-independent inactivation (formerly named r, 
                      // conflict with name R
     
     // Slowly Activating Potassium Current   
     real iks(t);   // Slowly Activating K Current (uA/uF)
     real gks(t);   // Channel Conductance of Slowly Activating K Current (mS/uF)
     real eks(t);   // Reversal Potential of Slowly Activating K Current (mV)
     real xs1(t);    // Slowly Activating K time-dependent activation
     real xs1ss(t);  // Steady-state value of inactivation gate xs1
     real tauxs1(t); // Time constant of gate xs1 (ms^-1)
     real xs2(t);    // Slowly Activating K time-dependent activation
     real xs2ss(t);  // Steady-state value of inactivation gate xs2
     real tauxs2(t); // Time constant of gate xs2 (ms^-1)
     real prnak = 0.01833;  // Na/K Permiability Ratio
     
     // Potassium Current (time-independent)   
     real iki(t);    // Time-independent K current (uA/uF)
     real gki(t);    // Channel Conductance of Time Independent K Current (mS/uF)
     real eki(t);    // Reversal Potential of Time Independent K Current (mV)
     real aki(t);    // K alpha-ki rate constant (ms^-1)
     real bki(t);    // K beta-ki rate constant (ms^-1)
     real kin(t);    // K inactivation

     // Plateau Potassium Current   
     real ikp(t);    // Plateau K current (uA/uF)
     real gkp;    // Channel Conductance of Plateau K Current (mS/uF)
     real ekp(t);    // Reversal Potential of Plateau K Current (mV)
     real kp(t);     // K plateau factor     

     // Na-Activated K Channel   
     real ikna(t);   // Na activated K channel
     real pona(t);   // Open probability dependent on Nai
     real pov(t);    // Open probability dependent on Voltage
     real ekna(t);   // Reversal potential
     real gkna = 0.12848;   // Maximum conductance (mS/uF)
     real nkna = 2.8;       // Hill coefficient for Na dependance
     real kdkna = 66;       // Dissociation constant for Na dependance(mM)
     
     // ATP-Sensitive K Channel   
     real ikatp(t);    // ATP-sensitive K current (uA/uF)
     real ekatp(t);    // K reversal potential (mV)
     real gkbaratp(t); // Conductance of the ATP-sensitive K channel (mS/uF)
     real gkatp;    // Maximum conductance of the ATP-sensitive K channel (mS/uF)
     real patp;     // Percentage availibility of open channels
     real natp = 0.24;          // K dependence of ATP-sensitive K current
     real nicholsarea = 0.00005; // Nichol's areas (cm^2)
     real atpi = 3;             // Intracellular ATP concentraion (mM)
     real hatp = 2;             // Hill coefficient
     real katp = 0.250;         // Half-maximal saturation point of ATP-sensitive K current (mM)
     
     // Ito Transient Outward Current (Dumaine et al. Circ Res 1999;85:803-809)   
     real ito(t);       // Transient outward current
     real gitodv;       // Maximum conductance of Ito
     real ekdv(t);       // Reversal Potential of Ito
     real rvdv(t);      // Time independent voltage dependence of Ito
     real zdv(t);       // Ito activation
     real azdv(t);      // Ito alpha-z rate constant
     real bzdv(t);      // Ito beta-z rate constant
     real tauzdv(t);       // Time constant of z gate
     real zssdv(t);     // Steady-state value of z gate
     real ydv(t);       // Ito inactivation
     real aydv(t);      // Ito alpha-y rate constant
     real bydv(t);      // Ito beta-y rate constant
     real tauydv(t);       // Time constant of y gate
     real yssdv(t);     // Steady-state value of y gate
          
     // Sodium-Calcium Exchanger V-S   
     real inaca(t);               // NaCa exchanger current (uA/uF)
     real c1 = 0.00025;   // Scaling factor for inaca (uA/uF)
     real c2 = 0.0001;   // Half-saturation concentration of NaCa exhanger (mM)
     real gammas = 0.15;  // Position of energy barrier controlling voltage dependance of inaca

     // Sodium-Potassium Pump   
     real inak(t);    // NaK pump current (uA/uF)
     real fnak(t);    // Voltage-dependance parameter of inak
     real sigma(t);   // [Na]o dependance factor of fnak
     real ibarnak = 2.25;   // Max. current through Na-K pump (uA/uF)
     real kmnai = 10;    // Half-saturation concentration of NaK pump (mM)
     real kmko = 1.5;    // Half-saturation concentration of NaK pump (mM)
     
     // Nonspecific Ca-activated Current   
     real insna(t);     // Non-specific Na current (uA/uF)
     real insk(t);      // Non-specific K current (uA/uF)
     real ibarnsna(t);  // Max. Na current through NSCa channel (uA/uF)
     real ibarnsk(t);   // Max. K current through NSCa channel (uA/uF)
     real pnsca = 0.000000175;  // Permiability of channel to Na and K (cm/s)
     real kmnsca = 0.0012;      // Half-saturation concentration of NSCa channel (mM)

     // Sarcolemmal Ca Pump   
     real ipca(t);                 // Sarcolemmal Ca pump current (uA/uF)
     real ibarpca = 1.15; // Max. Ca current through sarcolemmal Ca pump (uA/uF)
     real kmpca = 0.0005; // Half-saturation concentration of sarcolemmal Ca pump (mM)
     
     // Ca Background Current   
     real icab(t);  // Ca background current (uA/uF)
     real gcab;  // Max. conductance of Ca background (mS/uF)
     real ecan(t);  // Nernst potential for Ca (mV)

     // Na Background Current   
     real inab(t);  // Na background current (uA/uF)
     real gnab;  // Max. conductance of Na background (mS/uF)
     real enan(t);  // Nernst potential for Na (mV)

     
     // Cell Geometry 
     vcell = 1000*pi*a*a*l;     //   3.801e-5 uL
     ageo = 2*pi*a*a+2*pi*a*l;  //   7.671e-5 cm^2
     acap = ageo*2;             //   1.534e-4 cm^2
     vmyo = vcell*0.68;
     vmito = vcell*0.26;
     vsr = vcell*0.06;
     vnsr = vcell*0.0552;
     vjsr = vcell*0.0048;
     vcleft = vcell*0.12/0.88;
     
     // Beginning Ion Concentrations 
     nabm = 140;     // Initial Bulk Medium Na (mM)
     kbm = 4.5;      // Initial Bulk Medium K (mM)
     cabm = 1.8;     // Initial Bulk Medium Ca (mM)
when(t=t.min) {
     v = -90;       // Initial Voltage (mv)
     nai = 9;       // Initial Intracellular Na (mM)
     nao = 140;      // Initial Extracellular Na (mM)
     ki = 141.2;       // Initial Intracellular K (mM)
     ko = 4.5;       // Initial Extracellular K (mM)
     cai = 0.00006;  // Initial Intracellular Ca (mM)
     cao = 1.8;      // Initial Extracellular Ca (mM)

     //Initial Gate Conditions 
     m = 0.0008;
     h = 0.993771;
     j = 0.995727;
     d = 3.210618e-06;
     f = 0.999837;
     xs1 = 0.00445683;
     xs2 = 0.00445683;
     xr = 0.000124042;
     b = 0.000970231;
     g = 0.994305;
     zdv = 0.0120892;
     ydv = 0.999978;
        }
     
     // Initial Conditions   
when(t=t.min) {
     grelbarjsrol = 4;
//   tjsrol = 25;
//   tcicr = 25;
     Cjsr = 1.838;
     nsr = 1.838;
     trpn = 0.0143923;
     cmdn = 0.00257849;
     csqn = 6.97978;
}
     boolean = 1;
//---------------------------------------------------------------------------
//  void comp_ina
//       *******************************
//       *  Calculates Fast Na Current *
//       *******************************
//
     gna = 16;						//c
     ena = RToF*ln(nao/nai);		//c
     am = if( v<>-47.13) 0.32*(v+47.13)/(1-exp(-0.1*(v+47.13))) else 3.2;	//c
     bm = 0.08*exp(-v/11);                              //c
     
     ah = if(v < -40) 0.135*exp((80+v)/-6.8) else 0;
     bh = if(v < -40) 3.56*exp(0.079*v)+310000*exp(0.35*v) 
                      else 1/(0.13*(1+exp(-(v+10.66)/11.1)));
//
     aj = if(v < -40) 
       (-127140*exp(0.2444*v)-0.00003474*exp(-0.04391*v))*((v+37.78)/(1+exp(0.311*(v+79.23))))
       else
       0;
     bj = if(v < 40) (0.1212*exp(-0.01052*v))/(1+exp(-0.1378*(v+40.14)))
       else
        (0.3*exp(-0.0000002535*v))/(1+exp(-0.1*(v+32)));
//
        m:t = am*(1-m)-bm*m;
        h:t = ah*(1-h)-bh*h;
        j:t = aj*(1-j)-b*j;
     
     ina = gna*m*m*m*h*j*(v-ena);
//----------------------------------------------------------------------------------------
//  void comp_ical
//       **************************************************
//       *  Calculates Currents through L-Type Ca Channel *
//       **************************************************
//  
     dss = 1/(1+exp(-(v+10)/6.24));
     taud = dss*(1-exp(-(v+10)/6.24))/(0.035*(v+10));

     fss = (1/(1+exp((v+32)/8)))+(0.6/(1+exp((50-v)/20)));
     tauf = 1/(0.0197*exp(-( (0.0337*(v+10))^2) )+0.02);

     d:t = (dss-d)/taud;
     f:t = (fss-f)/tauf;
     
     ibarca = pca*zca*zca*((v*frdy)*FoRT)
              *((gacai*cai*exp((zca*v)*FoRT)-gacao*cao)
              /(exp((zca*v)*FoRT)-1));
     ibarna = pna*zna*zna*((v*frdy)*FoRT)
              *((ganai*nai*exp((zna*v)*FoRT)-ganao*nao)
              /(exp((zna*v)*FoRT)-1));
     ibark = pk*zk*zk*((v*frdy)*FoRT)
             *((gaki*ki*exp((zk*v)*FoRT)-gako*ko)
             /(exp((zk*v)*FoRT)-1));
          
     fca = 1/(1+cai/kmca);
// COMMENT BY GM RAYMOND: in Circ.Res 1994; 74:1071-1096, page 1090
// above equation is given as
// fca = 1/(1+(cai/kmca)^2) Noted Oct 22, 2003. The exponent is
// missing from the C-code for this model.
// 

     
     ilca = d*f*fca*ibarca;
     ilcana = d*f*fca*ibarna;
     ilcak = d*f*fca*ibark;
     
     ilcatot = ilca+ilcana+ilcak;
//-----------------------------------------------------------------------------------------
//  void comp_icat
//       **************************************************
//       *  Calculates Currents through T-Type Ca Channel *
//       **************************************************
//  
     bss = 1/(1+exp(-(v+14)/10.8));
     taub = 3.7+6.1/(1+exp((v+25)/4.5));

     gss = 1/(1+exp((v+60)/5.6));
     taug =if (v<=0) -0.875*v+12 else  12;
     
     b:t = (bss-b)/taub;
     g:t = (gss-g)/taug; 
     gcat = 0.05;
     eca = (R*temp/(2*frdy))*ln(cao/cai);
     
     icat = gcat*b*b*g*(v-eca);
//---------------------------------------------------------------------------
//  void comp_ikr
//       ********************************************
//       *  Calculates Rapidly Activating K Current *
//       ********************************************
     gkr = 0.02614*sqrt(ko/5.4);
     ekr = RToF*ln(ko/ki);
     xrss = 1/(1+exp(-(v+21.5)/7.5));
     tauxr = if( v=-14.2) 85.83033208 else
             if( v=-38.9) 168.8410495 else
              1/(0.00138*(v+14.2)
               /(1-exp(-0.123*(v+14.2)))+0.00061*(v+38.9)
               /(exp(0.145*(v+38.9))-1));
     xr:t = (xrss-xr)/tauxr; 
// variable rkt replaces variable r because of name conflict with R
     rkt = 1/(1+exp((v+9)/22.4));
     ikr = gkr*xr*rkt*(v-ekr);
//---------------------------------------------------------------------------
//  void comp_iks
//       *******************************************
//       *  Calculates Slowly Activating K Current *
//       *******************************************
//
     gks = 0.433*(1  +0.6/(1+    (0.000038/cai)^1.4  ));
     eks = RToF*ln((ko+prnak*nao)/(ki+prnak*nai));
     xs1ss = 1/(1+exp(-(v-1.5)/16.7));
     xs2ss = xs1ss;
     tauxs1 = if (v=-30.0) 417.9462527 else
               1/(0.0000719*(v+30)
               /(1-exp(-0.148*(v+30)))+0.000131*(v+30)
               /(exp(0.0687*(v+30))-1));
     tauxs2 = 4*tauxs1;
               
     xs1:t = (xs1ss-xs1)/tauxs1;
     xs2:t = (xs2ss-xs2)/tauxs2;
     iks = gks*xs1*xs2*(v-eks);
//---------------------------------------------------------------------------
//  void comp_iki
//       ******************************************
//       *  Calculates Time-Independent K Current *
//       ******************************************
//

     gki = 0.75*(sqrt(ko/5.4));
     eki = RToF*ln(ko/ki);

     aki = 1.02/(1+exp(0.2385*(v-eki-59.215)));
     bki = (0.49124*exp(0.08032*(v-eki+5.476))
           +exp(0.06175*(v-eki-594.31)))
           /(1+exp(-0.5143*(v-eki+4.753)));
     kin = aki/(aki+bki);
     iki = gki*kin*(v-eki) ;
/*  winslow formulat below
     aki=0;
     bki=0;
     eki = RToF*ln(ko/ki);
     gki = 0.75*(sqrt(ko/5.4));
     real kmk1 = 13.0; // mmol/L
     kin = 1/(2+exp(1.5*FoRT*(v-eki)));
     iki = gki*kin*( ko/(ko+kmk1) )*(v-eki);
*/      
//---------------------------------------------------------------------------
//  void comp_ikp
//       *********************************
//       *  Calculates Plateau K Current *
//       *********************************
//
     gkp = 0.00552;
     ekp = eki;
     kp = 1/(1+exp((7.488-v)/5.98));     
     ikp = gkp*kp*(v-ekp);
//----------------------------------------------------------------------------
//  void comp_ikna
//       **************************************
//       *  Calculates Na-activated K Current *
//       **************************************
// This current was commented out in the C-code with the annotation
// "curents commented out are only used when modeling pathological
//  conditions"
    real ikna_switch =0; // add ikna_switch, preset to zero to match 
// C-code
//
     ekna =  RToF*ln(ko/ki)    ;
     pona =  0.85/(1+(kdkna/nai)^2.8) ;
     pov =   0.8-0.65/(1+exp((v+125)/15))  ;
     ikna = if(ikna_switch=1) gkna*pona*pov*(v-ekna)        else 0;     
//-----------------------------------------------------------------------------
//  void comp_ikatp
//       ***************************************
//       *  Calculates ATP-Sensitive K Current *
//       ***************************************
// Note: If you wish to use this current in your simulations, there are additional       //
// changes which must be made to the code as detailed in Cardiovasc Res 1997;35:256-272  //
//
// This current was commented out in the C-code with the annotation
// "curents commented out are only used when modeling pathological
//  conditions"
    real ikatp_switch =0; // add ikatp_switch, preset to zero to match 
// C-code
//
     ekatp    =  RToF*ln(ko/ki) ;
     gkatp    =  0.000195/nicholsarea;
     patp     =  1/(1+ (atpi/katp)^hatp ) ;
     gkbaratp =  gkatp*patp*(ko/4)^natp;
     ikatp    = if(ikatp_switch=1) gkbaratp*(v-ekatp)         else 0;     
//------------------------------------------------------------------------------
//  void comp_ito
//       *****************************************
//       *  Calculates Transient Outward Current *
//       *****************************************
// This current was commented out in the C-code with the annotation
// "curents commented out are only used when modeling pathological
//  conditions"
    real ito_switch =0; // add ito_switch, preset to zero to match 
// C-code
//
     gitodv = 0.5 ;
     ekdv   = RToF*ln((ko)/(ki)) ;
     rvdv   = exp(v/100) ;
     
     azdv   = (10*exp((v-40)/25))/(1+exp((v-40)/25)) ;
     bzdv   = (10*exp(-(v+90)/25))/(1+exp(-(v+90)/25)) ;
     tauzdv = 1/(azdv+bzdv) ;
     zssdv  = azdv/(azdv+bzdv) ;
     zdv:t    = (zssdv-zdv)/tauzdv ;

     aydv   =  0.015/(1+exp((v+60)/5)) ;
     bydv   =  (0.1*exp((v+25)/5))/(1+exp((v+25)/5)) ;
     tauydv =  1/(aydv+bydv) ;
     yssdv  =  aydv/(aydv+bydv) ;
     ydv:t  =  (yssdv-ydv)/tauydv ;
     
     ito    = if(ito_switch=1)  gitodv*zdv*zdv*zdv*ydv*rvdv*(v-ekdv) else 0;
//--------------------------------------------------------------------------------
//  void comp_inaca
//       ***************************************
//       *  Calculates Na-Ca Exchanger Current *
//       ***************************************
     inaca = c1*exp((gammas-1)*v*FoRT)
             *((exp(v*FoRT)
             *nai*nai*nai*cao-nao*nao*nao*cai)
             /(1+c2*exp((gammas-1)*v*FoRT)
             *(exp(v*FoRT)*nai*nai*nai*cao+nao*nao*nao*cai)));
//----------------------------------------------------------------------------------
//  void comp_inak
//       *********************************
//       *  Calculates Na-K Pump Current *
//       *********************************
     sigma = (exp(nao/67.3)-1)/7;

     fnak = 1/(1+0.1245*exp((-0.1*v)*FoRT)+0.0365*sigma*exp((-v)*FoRT));

     inak = ibarnak*fnak*(1/(1+(kmnai/nai)^2))*(ko/(ko+kmko));
//---------------------------------------------------------------------------------- 
//  void comp_insca
//       *************************************************
//       *  Calculates Non-Specific ca-Activated Current *
//       *************************************************
// This current was commented out in the C-code with the annotation
// "curents commented out are only used when modeling pathological
//  conditions"
    real insca_switch =0; // add insca_switch, preset to zero to match 
// C-code
//
     ibarnsna = if( v<>0) pnsca*zna*zna*((v*frdy)*FoRT)
                *((ganai*nai*exp((zna*v)*FoRT)-ganao*nao)
                /(exp((zna*v)*FoRT)-1)) 
                else
                zna*frdy*pnsca*ganai*nai - zna*frdy*pnsca*ganao*nao ;

     ibarnsk = if( v<>0) pnsca*zk*zk*((v*frdy)*FoRT)
               *((gaki*ki*exp((zk*v)*FoRT)-gako*ko)
               /(exp((zk*v)*FoRT)-1))
               else
               zk*frdy*pnsca*gaki*ki - zk*frdy*pnsca*gako*ko;

     insna = if(insca_switch=1) ibarnsna/(1+(kmnsca/cai)^3) else 0;
     insk  = if(insca_switch=1) ibarnsk /(1+(kmnsca/cai)^3) else 0;
//---------------------------------------------------------------------------  
//  void comp_ipca
//       *******************************************
//       *  Calculates Sarcolemmal Ca Pump Current *
//       *******************************************
//
     ipca = (ibarpca*cai)/(kmpca+cai);     
//----------------------------------------------------------------------------
//  void comp_icab
//       *************************************
//       *  Calculates Ca Background Current *
//       *************************************
//
     gcab = 0.003016;
     ecan = (RToF/2)*ln(cao/cai);
     icab = gcab*(v-ecan);
//----------------------------------------------------------------------------
//  void comp_inab
//       *************************************
//       *  Calculates Na Background Current *
//       *************************************
//
     gnab = 0.004;
     enan = ena;
     inab = gnab*(v-enan);
//----------------------------------------------------------------------------
//  void comp_it
//       *****************************
//       *  Calculates Total Current *
//       *****************************
//
// Total sum of currents is calculated here, if the time is between stimtime = 0 
//  and stimtime = 0.5, a stimulus is applied 
//
// Comment from Gary Raymond, most of the code in this section was replaced
// by a simpler construct, it=st+naiont+kiont+caiont, and st is the 
// stimulus controlled as an external function.
//
     naiont = ina+inab+ilcana+insna+3*inak+3*inaca;
     kiont = ikr+iks+iki+ikp+ilcak+insk-2*inak+ito+ikna+ikatp;
     caiont = ilca+icab+ipca-2*inaca+icat;
     
//    it = st+naiont+kiont+caiont; // changed by Anamika Sarkar
     it = naiont+kiont+caiont;
//----------------------------------------------------------------------------
//  void conc_nai
//       ***************************************************
//       *  Calculates new myoplasmic Na ion concentration *
//       ***************************************************
//
// The units of dnai is in mM.  Note that naiont should be multiplied by the
// cell capacitance to get the correct units.  Since cell capacitance = 1 uF/cm^2,
// it doesn't explicitly appear in the equation below.
// This holds true for the calculation of dki and dcai.   

     dnai  = (naiont*acap)/(vmyo*zna*frdy);
     nai:t = dnai ;
//----------------------------------------------------------------------------
//  void conc_ki
//       **************************************************
//       *  Calculates new myoplasmic K ion concentration *
//       **************************************************
//
     dki = -((kiont+st)*acap)/(vmyo*zk*frdy);

     ki:t = dki;
//----------------------------------------------------------------------------
//  void conc_nsr
//       ********************************************
//       *  Calculates new NSR Ca ion concentration *
//       ********************************************
//
     kleak = iupbar/nsrbar;
     ileak = kleak*nsr;

     iup = iupbar*cai/(cai+kmup);

     dnsr = (iup-ileak-itr*vjsr/vnsr);
     nsr:t = dnsr;
//----------------------------------------------------------------------------
//  void conc_jsr
//       ********************************************
//       *  Calculates new JSR Ca ion concentration *
//       ********************************************
//
// Comment from Gary Raymond
// This section (routine) of the code could use some serious
// documentation because it is not obvious to me what was being
// done.
// Temporarily left undone.
//
// There are two specialized currents in this section
// irelcicr and ireljsrol. 
// What is needed is a complete analysis of both and their
// interaction with the stimulus being applied.
// Part of the analysis is determining the stimulus being applied
// through an analysis of the C-code.
//
// Analysis: The current irelcicr gets initialized
// 	when v>-35 mV and dcai(t)<dcai(t-dt) and boolean is off
//      at this point boolean gets set on 
//	and tcicr is initialized to zero. This is tied into
//      the stimulus          
//
/*     dcaiontnew = (caiont-caiontold)/dt;

     if(v>-35 && dcaiontnew<dcaiont && boolean==0)
          {boolean = 1;
          tcicr = 0;}
     
     on = 1/(1+exp((-tcicr+4)/.5));
     off = (1-1/(1+exp((-tcicr+4)/.5)));
     magrel = 1/(1+exp(((ilca+icab+ipca-2*inaca+icat)+5)/0.9));
     
     irelcicr = gmaxrel*on*off*magrel*(Cjsr-cai);

     tcicr = tcicr+dt;
*/
// Replace this block with code from wjr3.mod
/*
     real ta(t), tb=0 ;
     tjsrol = t-tb;
     ta = if(csqn>csqnth and tjsrol>50) t else 0;
     greljsrol = grelbarjsrol*(1-exp(-tjsrol/tauon))*exp(-tjsrol/tauoff);
     ireljsrol = if(t>50) greljsrol*(Cjsr-cai) else 0;

     on = 1/(1+exp((-tjsrol+4)/.5));
     off = (1-1/(1+exp((-tjsrol+4)/.5)));
     magrel = 1/(1+exp(((ilca+icab+ipca-2*inaca+icat)+5)/0.9));
     irelcicr = if (t>50) gmaxrel*on*off*magrel*(Cjsr-cai) else 0;
*/
// NEW CODE FROM wrj3.mod
real factor =1;
real gbarSRrel0=2;
real  gbarSRrel = gbarSRrel0*factor; // ms^(-1);
real  kap =  12.15*10^9*factor; // mM^(-4)*ms^(-1);
real  kam = 0.576*factor; // ms^(-1);
real  kbp = 4.05*10^6*factor; // mM^(-3)*ms(-1);
real  zkbm = 1.930*factor; // ms^(-1);
real  kcp = 0.1*factor; // ms^(-1);
real  kcm = 0.0008*factor; // ms^(-1);
real ncoop = 4; //
real mcoop = 3; //
real ssloss=1*factor; // ms^(-1);
real vss = 0.01*vjsr;
real PC1(t), PO1(t), PO2(t), PC2(t), Jrel(t);
when (t=t.min) {
                PC1=0.4994;
                PO1=0.00059;
                PO2=0.00001;
                PC2=0.50;}

        PC1:t  = -kap*cai^4*PC1+kam*PO1;               //(A.72)
        PO1:t  = kap*cai^4*PC1-kam*PO1-kbp*cai^3*PO1
               + zkbm*PO2-kcp*PO1+kcm*PC2;                    //(A.73)
        PO2:t  = kbp*cai^3*PO1-zkbm*PO2;                //(A.74)
        PC2:t  = kcp*PO1-kcm*PC2;                            //(A.75)
        Jrel   = gbarSRrel*(PO1+PO2)*(Cjsr-cai);                //(A.76)
        irelcicr =-Jrel ; // /vjsr;  // note modification here     

// Replacing quadratic solution with two ODE's
      real     acsqn = 1.0e06,
               bcsqn = acsqn*kmcsqn;
      csqn:t=(csqnbar-csqn)*Cjsr*acsqn -csqn*bcsqn;
      djsr = itr-irelcicr; // -ireljsrol ;
      Cjsr:t = djsr -( (csqnbar-csqn)*Cjsr*acsqn -csqn*bcsqn) ;
//--------------------------------------------------------------------------
//  void calc_itr
//       ***************************************************
//       *  Calculates Translocation of Ca from NSR to JSR *
//       ***************************************************
     itr = (nsr-Cjsr)/tautr;
//------------------------------------------------------------------------
 //  void conc_cai
 //       ***************************************************
 //       *  Calculates new myoplasmic Ca ion concentration *
 //       ***************************************************
      dcai = (-1.0)*(((caiont*acap)/(vmyo*zca*frdy))
                +((iup-ileak)*vnsr/vmyo)
                  -(irelcicr*(vjsr/vmyo)))-ssloss*cai;
       //         -(ireljsrol*vjsr/vmyo));
      real     atrpn = 1.0e06,
               btrpn = atrpn*kmtrpn,
               acmdn = 1.0e06,
               bcmdn = acmdn*kmcmdn;
      cmdn:t=(cmdnbar-cmdn)*cai*acmdn -cmdn*bcmdn;
      trpn:t=(trpnbar-trpn)*cai*atrpn -trpn*btrpn;
      cai:t= dcai
             -( (cmdnbar-cmdn)*cai*acmdn -cmdn*bcmdn
               +(trpnbar-trpn)*cai*atrpn -trpn*btrpn );
      catotal = trpn+cmdn+cai;
//-------------------------------------------------------------------------------------
 //  void conc_cleft
 //       ********************************************
 //       *  Calculates new cleft ion concentrations *
 //       ********************************************
 //  
     dnao = ((nabm-nao)/taudiff+naiont*acap/(vcleft*frdy));
     nao:t = dnao;
     dko = ((kbm-ko)/taudiff+kiont*acap/(vcleft*frdy));
     ko:t = dko;
     dcao = ((cabm-cao)/taudiff+caiont*acap/(vcleft*frdy*2));
     cao:t = dcao;
//-------------------------------------------------------------------
// Equation for Voltage
//     
//   v:t = -it;
//   dvdt = -it;
     v:t = st-it;  // changed by Dr. Sarkar
     dvdt = st-it;
     
/*     if(csqn>=csqnth && tjsrol>50)
          {grelbarjsrol = 4;
          tjsrol = 0;
          cout << "Spontaneous Release occured at time " << t << endl;
          }     
     caiontold = caiont;
     dcaiont = dcaiontnew;
*/
}
/*

 DETAILED DESCRIPTION:
 The Luo-Rudy Dynamic (LRd) Model of the Mammalian Ventricular Myocyte. This model describes 
 the mammalian cardiac ventricular action potential in a single cell in guinea pigs. Along 
 with the membrane sodium and potassium currents, the model focuses on processes that regulate 
 intracellular calcium and depend on its concentration. The figure in this section shows all 
 the channels and currents in the modeled single cell.

 The original “C�? code is from http://www.cwru.edu/med/CBRTC/LRdOnline/LRdModel.c. The “C�?code 
 was adapted, Illustrative Diagram block for block,into JSIM's Mathematical Modeling Language (MML). 
 The code was substantially modified, converting many of the expressions into ordinary 
 differential equations. Rapid equilibria have been transformed into “fast�? ODE's in order to 
 avoid iterative procedures, complex polynomial solutions, etc. “C�? subroutines for currents 
 were converted to ordinary differential equations where necessary. Stimulus was replaced by 
 JSIM's external function (extern real st(t);). Information pertaining to step size was removed 
 and replaced by JSim's realDomain t. Recording of maximum excursions on a beat by beat basis 
 was deleted. Double declarations (“C�? construct) were replaced by real declarations 
 (JSIM's MML construct which are double precision variables.) The “C�? subroutines were converted 
 to inline code and placed earlier in the structure. Fast equilibria were were replaced by ODE's 
 to avoid solving quadratic and cubic equations–the special innovation of the LR model. Any 
 errors contained in the code are not the fault of the original authors.
	

 SHORTCOMINGS/GENERAL COMMENTS:
	- Specific inadequacies or next level steps
 
 KEY WORDS: cardiac action potential, ionic currents, sodium, potassium, calcium, transient, 
 pump, ATP, sarcolemmal, cell physiology   

 REFERENCES:
 Luo CH, Rudy Y.;A model of the ventricular cardiac action potential. Depolarization, repolarization, 
 and their interaction, Circ Res. 1991 Jun;68(6):1501-26.

 Luo CH, Rudy Y.; A dynamic model of the cardiac ventricular action potential. II. Afterdepolarizations, 
 triggered activity, and potentiation, Circ Res. 1994 Jun;74(6):1097-113.

 Zeng J, Laurita KR, Rosenbaum DS, Rudy Y; Two components of the delayed rectifier K+ current in 
 ventricular myocytes of the guinea pig type. Theoretical formulation and their role in repolarization.,Circ Res. 1995 Jul;77(1):140-52.

 Zeng J, Rudy Y.; Early afterdepolarizations in cardiac myocytes: mechanism and rate dependence.,
 Biophys J. 1995 Mar;68(3):949-64.

 Shaw RM, Rudy Y.; Electrophysiologic effects of acute myocardial ischemia: a theoretical study 
 of altered cell excitability and action potential duration, Cardiovasc Res. 1997 Aug;35(2):256-72.

 Viswanathan PC, Shaw RM, Rudy Y.; Effects of IKr and IKs heterogeneity on action potential 
 duration and its rate dependence: a simulation study., Circulation. 1999 May 11;99(18):2466-74.

 Viswanathan PC, Rudy Y.; Pause induced early afterdepolarizations in the long QT syndrome: 
 a simulation study.,Cardiovasc Res. 1999 May;42(2):530-42.

 Clancy CE, Rudy Y.; Linking a genetic defect to its cellular phenotype in a cardiac arrhythmia. 
 Nature. 1999 Aug 5;400(6744):566-9.

 Viswanathan PC, Rudy Y.; Cellular arrhythmogenic effects of congenital and acquired long-QT 
 syndrome in the heterogeneous myocardium. Circulation. 2000 Mar 14;101(10):1192-8.

 Faber GM, Rudy Y.; Action potential and contractility changes in [Na(+)](i) overloaded cardiac 
 myocytes: a simulation study. Biophys J. 2000 May;78(5):2392-404.

 Beatriz Trénor, Lucía Romero, José María Ferrero (Jr), Javier Sáiz, Germán Moltó, José Miguel 
 Alonso; Vulnerability to Reentry in a Regionally Ischemic tissue. A simulation Study, (in press) 	

 REVISION HISTORY:
	Original Author : garyr  Date: 29/jun/06
	Revised by: BEJ Date:12Sep11 : Update Comments and formatting
	

 COPYRIGHT AND REQUEST FOR ACKNOWLEDGMENT OF USE:   
  Copyright (C) 1999-2011 University of Washington. From the National Simulation Resource,  
  Director J. B. Bassingthwaighte, Department of Bioengineering, University of Washington, Seattle WA 98195-5061. 
  Academic use is unrestricted. Software may be copied so long as this copyright notice is included.
  
  This software was developed with support from NIH grant HL073598. 
  Please cite this grant in any publication for which this software is used and send an email 
  with the citation and, if possible, a PDF file of the paper to: staff@physiome.org. 



*/
