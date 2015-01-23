#!/usr/bin/python

# Python script to loop through CellML files and export custom units and standard CellML units as tab delimited .txt file
# CellML 1.1 "parent" files are not considered as they do not have standard units explicit within the file

import string
import os
import glob
import re
import xml.etree.ElementTree as et

# Local CellML file directory
path_cellml_dir = "/Users/graham_kim/Documents/eclipse_workspace/SemGen/examples/CellML/"

result_file = open("/Users/graham_kim/Desktop/standardCellMLUnits_result.txt", 'w')

unique_files = []

def standardUnits(category):
    # Counters for CellML 1.0 and CellML 1.1 files
    cellml10_counter = 0
    cellml11_counter = 0

    path = path_cellml_dir + category

    for file_dir in glob.glob(os.path.join(path, '*.cellml')):
        filename = file_dir.replace(path + '/', '').replace(".cellml", '')

        if filename in unique_files:
            pass
        else:
            unique_files.append(filename)
            print filename

            tree = et.parse(file_dir)
            root = tree.getroot()

            cellml_10 = "http://www.cellml.org/cellml/1.0#"
            cellml_11 = "http://www.cellml.org/cellml/1.1#"

            cellml_ver = root.tag

            if cellml_10 in cellml_ver:
                cellml10_counter += 1
                for units in root.findall('{http://www.cellml.org/cellml/1.0#}units'):
                    unit_name = units.get('name')
                    result_file.write(category)
                    result_file.write('\t' + filename)
                    result_file.write('\t' + unit_name)
                    for std_unit in units.findall('{http://www.cellml.org/cellml/1.0#}unit'):
                        std_unit_name = std_unit.get('units')
                        result_file.write('\t' + std_unit_name)
                        std_unit_exp = std_unit.get('exponent')
                        if std_unit_exp is not None:
                            result_file.write('^' + std_unit_exp)
                    result_file.write('\n')

            elif cellml_11 in cellml_ver:
                cellml11_counter += 1

    #result_file.write("CellML 1.0: " + str(cellml10_counter))
    #result_file.write("CellML 1.1: " + str(cellml11_counter))


###
model_categories = ["calcium_dynamics", "cardiovascular_circulation", "cell_cycle", "cell_migration", "circadian_rhythms", "electrophysiology", "endocrine", "excitation-contraction_coupling", "gene_regulation", "hepatology", "immunology", "ion_transport", "mechanical_constitutive_laws", "metabolism", "myofilament_mechanics", "neurobiology", "ph_regulation", "PKPD", "signal_transduction", "synthetic_biology"]
for category in model_categories:
    standardUnits(category)

result_file.close()
