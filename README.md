# SemGen

SemGen is an experimental software tool for automating the modular composition and decomposition of biosimulation models.

SemGen facilitates the construction of complex, integrated models, and the swift extraction of reusable submodels from larger ones. SemGen relies on the semantically-rich [SemSim model description format](http://sbp.bhi.washington.edu/projects/semsim) to help automate these modeling tasks. 

With SemGen, users can:

- Visualize models using D3 force-directed networks,
- Create SemSim versions of existing models and annotate them with rich semantic data,
- Automatically decompose models into interoperable submodels,
- Semi-automatically merge models into more complex systems, and
- Encode models in executable simulation formats.

## Getting Started

These instructions will help you use SemGen to visualize, annotate, extract, and merge models.

### Prerequisites

SemGen is a Java-based program and requires [Java Runtime Environment](https://www.java.com/en/download/manual.jsp) version 1.7 (64-bit) or higher to execute.

To check your Java version, go to a command prompt and enter:

```
java -version
```

### Installing

Simply download the appropriate build for your operating system from the [releases page](https://github.com/SemBioProcess/SemGen/releases).

Windows: Double-click the ```SemGen.exe``` file in the main SemGen directory.

Mac: Open ```SemGen_macosx_4_0_0.dmg```, and drag ```SemGen.app``` to Applications folder. Double-click ```SemGen.app``` to start SemGen.

Linux: Unarchive ```SemGen_linux_4_0_0.tar.gz```. Double-click the "SemGen.jar" file in the main SemGen directory. 

## Running SemGen

Here is a primer on how to use SemGen to load, visualize, annotate, extract, and merge models.

In SemGen, the Project tab will be your main workspace:

![](https://imgur.com/JjJWaqA.png)

- **Search**: Hovering your cursor over the magnifying glass brings up the search bar. You can search for example models, or currently visualized nodes by typing in search terms. The search can be performed over the name, description, or the annotation.
- **Project Actions**: The menu on the left side contains project-level actions. This menu can be collapsed/expanded by clicking the chevron on the left edge.
- **Stage Options**: The menu on the right side contains visualization options, as well as additional information about the selected node. This menu can be collapsed/expanded by clicking the chevron on the right edge.
- **Selection/Navigation**: The buttons in the top right corner toggles click-and-drag between moving the visualization, and selecting multiple nodes. Additionally, the mouse scroll wheel can be used to zoom in/out of the visualization.

### Loading a model

To load a model, click the **Open model** button under **Project Actions** on the lefthand side. This will prompt you to select a model file to load (SemGen currently supports *SemSim*, *CellML*, *SBML*, *JSim* file formats):

![](https://imgur.com/IFvd9nK.png)

Once you select a model, it will be loaded in SemGen and visualized as a model node:

![](https://i.imgur.com/019X3ow.png)

Alternatively, SemGen comes with a library of example models. These can be accessed by using the search bar. Hover over the magnifying glass on the top left and type in terms to search for. Click the model name in the results to load the model:

![](https://i.imgur.com/Sj8kUBb.png)

### Visualizing a model

Once a model is loaded in SemGen, there are several ways to visualize and explore the model.

Select the model you want to visualize by clicking the model node (selected node will have a yellow ring around it). Then click one of the visualizations from the **Project Actions** menu on the lefthand side.

#### Submodels

The submodel visualization shows the hierarchical and/or compartmental organization of the model:

![](https://i.imgur.com/oeapNYN.png)

Each submodel node can be further expanded by double clicking it:

![](https://i.imgur.com/0KQbJIW.png)

#### Dependencies

The dependency visualization shows the mathematical dependency network in the model:

![](https://i.imgur.com/zgJIxSA.png)

Different node types can be hidden or shown in the **Stage Option** menu, which can be useful for visualizing large models:

![](https://i.imgur.com/0DyI37a.png)

#### PhysioMap

[PhysioMap](https://www.ncbi.nlm.nih.gov/pubmed/?term=cook+gennari+physiomap) displays the physiological processes and their participants (sources, sinks, and mediators) based on the semantics of the biological processes and entities:

![](https://i.imgur.com/rpx6hDp.png)

### Annotator

[Click here for a comprehensive Annotator tutorial.](https://sites.google.com/site/semanticsofbiologicalprocesses/projects/semgen/semgenhelp/annotator-tutorial)

With the Annotator tool, you can convert mathematical models into the SemSim format and annotate the model's codewords using concepts from online reference ontologies. Currently the Annotator can convert MML, SBML, and CellML models into the SemSim format. The Semantics of Biological Processes group maintains [a protocol for annotating a model](https://github.com/SemBioProcess/SemGen/wiki/Protocol-for-annotating-a-SemSim-model) which can help guide the annotation process.

To annotate a model, click **Annotate** button under **Project Actions**. This will create a new Annotation tab:

![](https://i.imgur.com/XOCHlNY.png)

#### Composite annotations

Each composite annotation consists of a physical property term connected to a physical entity or physical process term. The physical entity term can itself also be a composite of ontology terms. We recommend using only terms from the Ontology of Physics for Biology (OPB) for the physical property annotation components. For the physical entity annotations we recommend using robust, thorough, and widely accepted online reference ontologies like the Foundational Model of Anatomy (FMA), Chemical Entities of Biological Interest (ChEBI), and Gene Ontology cellular components (GO-cc). For physical processes annotations, we recommend creating custom terms and defining them by identifying their thermodynamic sources, sinks and mediators from the physical entities in the model.

When you edit a composite annotation for a model codeword, the Annotator provides an interface for rapid searching and retrieval of reference ontology concepts via the [BioPortal](http://bioportal.bioontology.org/) web service. 

Example: Suppose you are annotating a beta cell glycolysis model that includes a codeword representing glucose concentration in the cytosol of the cell.

A detailed composite annotation would be:

```
OPB:Chemical concentration <propertyOf> CHEBI:glucose <part_of> FMA:Portion of cytosol <part_of> FMA:Beta cell
```

In this case we use the term Chemical concentration from the OPB for the physical property part of the annotation, and we compose the physical entity part by linking four concepts - one from the OPB, one from ChEBI and two from the FMA. This example illustrates the post-coordinated nature of the SemSim approach to annotation and how it provides high expressivity for annotating model terms.

The above example represents a very detailed composite annotation, however, such detail may not be necessary to disambiguate concepts in a given model. For example, there may not be any other portions of glucose within the model apart from that in the cytosol. In this case, one could use the first three terms in the composite annotation and still disambiguate the model codeword from the rest of the model's contents:

```
OPB:Chemical concentration <propertyOf> CHEBI:glucose
```

Although this annotation approach does not fully capture the biophysical meaning of the model codeword, SemGen is more likely to find semantic overlap between models if they use this shallower annotation style. This is mainly because the SemGen Merger tool currently only recognizes semantic equivalencies; it does not identify semantically similar terms in models that a user wants to integrate. Therefore, if a user wants to integrate our example glycolysis model with a TCA cycle model based on cardiac myocyte metabolism, the shallower approach would likely identify more semantic equivalencies than the more detailed approach.  

Nonetheless, we recommend using the more detailed approach, given that future versions of SemGen will include a "Merging Wizard" that will identify and rank codewords that are semantically similar, not just semantically identical. 

### Extractor

The Extractor tool provides ways to decompose SemSim models into submodels. This decomposition process is useful if you want to "carve out" a smaller portion of a given model in order to remove extraneous model features.

Below is an step-by-step example of an extraction:


1. Load a model and select one or more nodes you would like to extract by left-clicking. Multiple nodes can be selected by control+click (command+click on Mac), or by toggling *selection* in the top right corner.
2. Right-click one of the selected nodes and click *Extract Selected*. In case you want to extract the majority of the model, it may be more convenient to select the nodes you do not wish to save in the extraction, and click *Extract Unselected*. ![](https://i.imgur.com/vEywONu.png)
3. Enter a new name for the extracted nodes, and the newly extracted nodes will appear in SemGen. ![](https://i.imgur.com/Be6RSn8.png) ![](https://i.imgur.com/zSZzVqE.png)
4. Extraction can also be performed on submodel and PhysioMap nodes. ![](https://i.imgur.com/J5nd49p.png) ![](https://i.imgur.com/pLFiGNc.png)


### Merger

The Merger tool helps automate the integration of two SemSim models. The Merger identifies the interface between two models by comparing the biological meaning of the models' codewords as expressed by their composite and singular annotations. If the two models share the same biological concept, the codewords representing this concept are mapped to each other and the user must decide which computational representation of the concept they want to preserve in the integrated model.

Below is an step-by-step example of a merge between a cardiovascular dynamics and a baroreceptor model:

1. Load two models you would like to merge in SemGen. ![](https://i.imgur.com/PA2xoBd.png)
2. Drag-and-drop one of the models on top of the other. SemGen will automatically find semantic overlaps between the two models. ![](https://i.imgur.com/vIS8lr0.png)
3. Click the panel to see more information about the codewords. Click the *Preview* button to visualize what the local dependency network would look like for the merged model using each computational representation. ![](https://i.imgur.com/EHHzpmj.png)
4. Indicate which computational representation of the concept you wish to preserve in the merged model. ![](https://i.imgur.com/hJjUmzj.png)
5. If SemGen did not include all of the merge points, you can manually add mappings by selecting individual codewords from the bottom panels and clicking the *Add manual mapping* button.  ![](https://i.imgur.com/HrfHk1w.png) ![](https://i.imgur.com/Eqrq3Cy.png) Manual mappings can also be added visually by clicking *Visualize* and dragging-and-dropping a node onto another (blue link indicates exact semantic match, and yellow link indicates manual mapping). ![](https://i.imgur.com/nmZiBUH.png)
6. Once you have indicated all of the merge resolution points, click *Resolve Merge Conflicts*. Resolve any duplicate code name or unit conversion conflicts. ![](https://i.imgur.com/FNp2P59.png)
7. When finished, click *Merge* to save the new merged model!


## Authors

**Dr. Maxwell Neal** originally developed the SemGen software as part of his dissertation research.

Currently, Dr. Neal leads a team of developers to further augment, test and evaluate SemGen under an R01 grant from the National Library of Medicine (PIs: **John Gennari** and **Brian Carlson**) that aims to accelerate model-driven research.

Contributors to SemGen development include **Christopher Thompson**, **Graham Kim** and **Ryan James**.

SemGen development is currently supported by a grant from the [National Library of Medicine](http://www.nlm.nih.gov/) and through the [Virtual Physiological Rat](http://www.virtualrat.org/) project. 

## License

SEMGEN

Copyright (c) 2010-2017 Maxwell Neal, University of Washington.

All rights reserved.

Redistribution and use in source and binary forms, with or without modification, 
for COMMERCIAL PURPOSES IS PROHIBITED without prior written permission from the author.

Redistribution and use in source and binary forms, with or without modification, are 
permitted for non-commercial purposes (such as for research, personal use, or educational use), provided that redistribution in any form includes this entire notice in all copies of the software, derivative works, and supporting documentation.

The name of the author or the University of Washington may not be used to endorse or promote software or services derived from this software without prior written permission from the author and/or the University.

THIS SOFTWARE IS PROVIDED "AS IS" AND ANY EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL MAXWELL NEAL, UNIVERSITY OF WASHINGTON, OR ANY CONTRIBUTORS TO THIS SOFTWARE BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED
AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE. 