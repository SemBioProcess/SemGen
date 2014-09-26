/**
 * KEGGBindingStub.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package semgen.webservices.KEGG;

import org.apache.axis.soap.SOAPConstants;
import org.apache.axis.utils.JavaUtils;

<<<<<<< HEAD
import java.util.Enumeration;
import java.util.Vector;

import javax.xml.namespace.QName;

import org.apache.axis.encoding.ser.ArraySerializerFactory;
import org.apache.axis.client.Service;
import org.apache.axis.encoding.ser.ArrayDeserializerFactory;

public class KEGGBindingStub extends org.apache.axis.client.Stub implements KEGGPortType {
    private Vector<Class<?>> cachedSerClasses = new Vector<Class<?>>();
    private Vector<QName> cachedSerQNames = new Vector<QName>();
    private Vector cachedSerFactories = new Vector();
    private Vector cachedDeserFactories = new Vector();
=======
public class KEGGBindingStub extends org.apache.axis.client.Stub implements KEGGPortType {
    private java.util.Vector cachedSerClasses = new java.util.Vector();
    private java.util.Vector cachedSerQNames = new java.util.Vector();
    private java.util.Vector cachedSerFactories = new java.util.Vector();
    private java.util.Vector cachedDeserFactories = new java.util.Vector();
>>>>>>> 2eb394907b98577f1b916408cf22a2de6952b22d

    static org.apache.axis.description.OperationDesc [] _operations;

    static {
        _operations = new org.apache.axis.description.OperationDesc[72];
        _initOperationDesc1();
        _initOperationDesc2();
        _initOperationDesc3();
        _initOperationDesc4();
        _initOperationDesc5();
        _initOperationDesc6();
        _initOperationDesc7();
        _initOperationDesc8();
    }

    private static void _initOperationDesc1(){
        org.apache.axis.description.OperationDesc oper;
        org.apache.axis.description.ParameterDesc param;
        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("list_databases");
<<<<<<< HEAD
        oper.setReturnType(new QName("SOAP/KEGG", "ArrayOfDefinition"));
        oper.setReturnClass(Definition[].class);
        oper.setReturnQName(new QName("", "return"));
=======
        oper.setReturnType(new javax.xml.namespace.QName("SOAP/KEGG", "ArrayOfDefinition"));
        oper.setReturnClass(Definition[].class);
        oper.setReturnQName(new javax.xml.namespace.QName("", "return"));
>>>>>>> 2eb394907b98577f1b916408cf22a2de6952b22d
        oper.setStyle(org.apache.axis.constants.Style.RPC);
        oper.setUse(org.apache.axis.constants.Use.ENCODED);
        _operations[0] = oper;

        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("list_organisms");
<<<<<<< HEAD
        oper.setReturnType(new QName("SOAP/KEGG", "ArrayOfDefinition"));
        oper.setReturnClass(Definition[].class);
        oper.setReturnQName(new QName("", "return"));
=======
        oper.setReturnType(new javax.xml.namespace.QName("SOAP/KEGG", "ArrayOfDefinition"));
        oper.setReturnClass(Definition[].class);
        oper.setReturnQName(new javax.xml.namespace.QName("", "return"));
>>>>>>> 2eb394907b98577f1b916408cf22a2de6952b22d
        oper.setStyle(org.apache.axis.constants.Style.RPC);
        oper.setUse(org.apache.axis.constants.Use.ENCODED);
        _operations[1] = oper;

        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("list_pathways");
<<<<<<< HEAD
        param = new org.apache.axis.description.ParameterDesc(new QName("", "org"), org.apache.axis.description.ParameterDesc.IN, new QName("http://www.w3.org/2001/XMLSchema", "string"), String.class, false, false);
        oper.addParameter(param);
        oper.setReturnType(new QName("SOAP/KEGG", "ArrayOfDefinition"));
        oper.setReturnClass(Definition[].class);
        oper.setReturnQName(new QName("", "return"));
=======
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "org"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"), java.lang.String.class, false, false);
        oper.addParameter(param);
        oper.setReturnType(new javax.xml.namespace.QName("SOAP/KEGG", "ArrayOfDefinition"));
        oper.setReturnClass(Definition[].class);
        oper.setReturnQName(new javax.xml.namespace.QName("", "return"));
>>>>>>> 2eb394907b98577f1b916408cf22a2de6952b22d
        oper.setStyle(org.apache.axis.constants.Style.RPC);
        oper.setUse(org.apache.axis.constants.Use.ENCODED);
        _operations[2] = oper;

        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("list_ko_classes");
<<<<<<< HEAD
        param = new org.apache.axis.description.ParameterDesc(new QName("", "class_id"), org.apache.axis.description.ParameterDesc.IN, new QName("http://www.w3.org/2001/XMLSchema", "string"), String.class, false, false);
        oper.addParameter(param);
        oper.setReturnType(new QName("SOAP/KEGG", "ArrayOfDefinition"));
        oper.setReturnClass(Definition[].class);
        oper.setReturnQName(new QName("", "return"));
=======
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "class_id"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"), java.lang.String.class, false, false);
        oper.addParameter(param);
        oper.setReturnType(new javax.xml.namespace.QName("SOAP/KEGG", "ArrayOfDefinition"));
        oper.setReturnClass(Definition[].class);
        oper.setReturnQName(new javax.xml.namespace.QName("", "return"));
>>>>>>> 2eb394907b98577f1b916408cf22a2de6952b22d
        oper.setStyle(org.apache.axis.constants.Style.RPC);
        oper.setUse(org.apache.axis.constants.Use.ENCODED);
        _operations[3] = oper;

        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("binfo");
<<<<<<< HEAD
        param = new org.apache.axis.description.ParameterDesc(new QName("", "db"), org.apache.axis.description.ParameterDesc.IN, new QName("http://www.w3.org/2001/XMLSchema", "string"), String.class, false, false);
        oper.addParameter(param);
        oper.setReturnType(new QName("http://www.w3.org/2001/XMLSchema", "string"));
        oper.setReturnClass(String.class);
        oper.setReturnQName(new QName("", "return"));
=======
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "db"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"), java.lang.String.class, false, false);
        oper.addParameter(param);
        oper.setReturnType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        oper.setReturnClass(java.lang.String.class);
        oper.setReturnQName(new javax.xml.namespace.QName("", "return"));
>>>>>>> 2eb394907b98577f1b916408cf22a2de6952b22d
        oper.setStyle(org.apache.axis.constants.Style.RPC);
        oper.setUse(org.apache.axis.constants.Use.ENCODED);
        _operations[4] = oper;

        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("bget");
<<<<<<< HEAD
        param = new org.apache.axis.description.ParameterDesc(new QName("", "string"), org.apache.axis.description.ParameterDesc.IN, new QName("http://www.w3.org/2001/XMLSchema", "string"), String.class, false, false);
        oper.addParameter(param);
        oper.setReturnType(new QName("http://www.w3.org/2001/XMLSchema", "string"));
        oper.setReturnClass(String.class);
        oper.setReturnQName(new QName("", "return"));
=======
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "string"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"), java.lang.String.class, false, false);
        oper.addParameter(param);
        oper.setReturnType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        oper.setReturnClass(java.lang.String.class);
        oper.setReturnQName(new javax.xml.namespace.QName("", "return"));
>>>>>>> 2eb394907b98577f1b916408cf22a2de6952b22d
        oper.setStyle(org.apache.axis.constants.Style.RPC);
        oper.setUse(org.apache.axis.constants.Use.ENCODED);
        _operations[5] = oper;

        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("bfind");
<<<<<<< HEAD
        param = new org.apache.axis.description.ParameterDesc(new QName("", "string"), org.apache.axis.description.ParameterDesc.IN, new QName("http://www.w3.org/2001/XMLSchema", "string"), String.class, false, false);
        oper.addParameter(param);
        oper.setReturnType(new QName("http://www.w3.org/2001/XMLSchema", "string"));
        oper.setReturnClass(String.class);
        oper.setReturnQName(new QName("", "return"));
=======
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "string"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"), java.lang.String.class, false, false);
        oper.addParameter(param);
        oper.setReturnType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        oper.setReturnClass(java.lang.String.class);
        oper.setReturnQName(new javax.xml.namespace.QName("", "return"));
>>>>>>> 2eb394907b98577f1b916408cf22a2de6952b22d
        oper.setStyle(org.apache.axis.constants.Style.RPC);
        oper.setUse(org.apache.axis.constants.Use.ENCODED);
        _operations[6] = oper;

        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("btit");
<<<<<<< HEAD
        param = new org.apache.axis.description.ParameterDesc(new QName("", "string"), org.apache.axis.description.ParameterDesc.IN, new QName("http://www.w3.org/2001/XMLSchema", "string"), String.class, false, false);
        oper.addParameter(param);
        oper.setReturnType(new QName("http://www.w3.org/2001/XMLSchema", "string"));
        oper.setReturnClass(String.class);
        oper.setReturnQName(new QName("", "return"));
=======
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "string"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"), java.lang.String.class, false, false);
        oper.addParameter(param);
        oper.setReturnType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        oper.setReturnClass(java.lang.String.class);
        oper.setReturnQName(new javax.xml.namespace.QName("", "return"));
>>>>>>> 2eb394907b98577f1b916408cf22a2de6952b22d
        oper.setStyle(org.apache.axis.constants.Style.RPC);
        oper.setUse(org.apache.axis.constants.Use.ENCODED);
        _operations[7] = oper;

        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("bconv");
<<<<<<< HEAD
        param = new org.apache.axis.description.ParameterDesc(new QName("", "string"), org.apache.axis.description.ParameterDesc.IN, new QName("http://www.w3.org/2001/XMLSchema", "string"), String.class, false, false);
        oper.addParameter(param);
        oper.setReturnType(new QName("http://www.w3.org/2001/XMLSchema", "string"));
        oper.setReturnClass(String.class);
        oper.setReturnQName(new QName("", "return"));
=======
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "string"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"), java.lang.String.class, false, false);
        oper.addParameter(param);
        oper.setReturnType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        oper.setReturnClass(java.lang.String.class);
        oper.setReturnQName(new javax.xml.namespace.QName("", "return"));
>>>>>>> 2eb394907b98577f1b916408cf22a2de6952b22d
        oper.setStyle(org.apache.axis.constants.Style.RPC);
        oper.setUse(org.apache.axis.constants.Use.ENCODED);
        _operations[8] = oper;

        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("get_linkdb_by_entry");
<<<<<<< HEAD
        param = new org.apache.axis.description.ParameterDesc(new QName("", "entry_id"), org.apache.axis.description.ParameterDesc.IN, new QName("http://www.w3.org/2001/XMLSchema", "string"), String.class, false, false);
        oper.addParameter(param);
        param = new org.apache.axis.description.ParameterDesc(new QName("", "db"), org.apache.axis.description.ParameterDesc.IN, new QName("http://www.w3.org/2001/XMLSchema", "string"), String.class, false, false);
        oper.addParameter(param);
        param = new org.apache.axis.description.ParameterDesc(new QName("", "offset"), org.apache.axis.description.ParameterDesc.IN, new QName("http://www.w3.org/2001/XMLSchema", "int"), int.class, false, false);
        oper.addParameter(param);
        param = new org.apache.axis.description.ParameterDesc(new QName("", "limit"), org.apache.axis.description.ParameterDesc.IN, new QName("http://www.w3.org/2001/XMLSchema", "int"), int.class, false, false);
        oper.addParameter(param);
        oper.setReturnType(new QName("SOAP/KEGG", "ArrayOfLinkDBRelation"));
        oper.setReturnClass(LinkDBRelation[].class);
        oper.setReturnQName(new QName("", "return"));
=======
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "entry_id"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"), java.lang.String.class, false, false);
        oper.addParameter(param);
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "db"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"), java.lang.String.class, false, false);
        oper.addParameter(param);
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "offset"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "int"), int.class, false, false);
        oper.addParameter(param);
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "limit"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "int"), int.class, false, false);
        oper.addParameter(param);
        oper.setReturnType(new javax.xml.namespace.QName("SOAP/KEGG", "ArrayOfLinkDBRelation"));
        oper.setReturnClass(LinkDBRelation[].class);
        oper.setReturnQName(new javax.xml.namespace.QName("", "return"));
>>>>>>> 2eb394907b98577f1b916408cf22a2de6952b22d
        oper.setStyle(org.apache.axis.constants.Style.RPC);
        oper.setUse(org.apache.axis.constants.Use.ENCODED);
        _operations[9] = oper;

    }

    private static void _initOperationDesc2(){
        org.apache.axis.description.OperationDesc oper;
        org.apache.axis.description.ParameterDesc param;
        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("get_best_neighbors_by_gene");
<<<<<<< HEAD
        param = new org.apache.axis.description.ParameterDesc(new QName("", "genes_id"), org.apache.axis.description.ParameterDesc.IN, new QName("http://www.w3.org/2001/XMLSchema", "string"), String.class, false, false);
        oper.addParameter(param);
        param = new org.apache.axis.description.ParameterDesc(new QName("", "offset"), org.apache.axis.description.ParameterDesc.IN, new QName("http://www.w3.org/2001/XMLSchema", "int"), int.class, false, false);
        oper.addParameter(param);
        param = new org.apache.axis.description.ParameterDesc(new QName("", "limit"), org.apache.axis.description.ParameterDesc.IN, new QName("http://www.w3.org/2001/XMLSchema", "int"), int.class, false, false);
        oper.addParameter(param);
        oper.setReturnType(new QName("SOAP/KEGG", "ArrayOfSSDBRelation"));
        oper.setReturnClass(SSDBRelation[].class);
        oper.setReturnQName(new QName("", "return"));
=======
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "genes_id"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"), java.lang.String.class, false, false);
        oper.addParameter(param);
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "offset"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "int"), int.class, false, false);
        oper.addParameter(param);
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "limit"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "int"), int.class, false, false);
        oper.addParameter(param);
        oper.setReturnType(new javax.xml.namespace.QName("SOAP/KEGG", "ArrayOfSSDBRelation"));
        oper.setReturnClass(SSDBRelation[].class);
        oper.setReturnQName(new javax.xml.namespace.QName("", "return"));
>>>>>>> 2eb394907b98577f1b916408cf22a2de6952b22d
        oper.setStyle(org.apache.axis.constants.Style.RPC);
        oper.setUse(org.apache.axis.constants.Use.ENCODED);
        _operations[10] = oper;

        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("get_best_best_neighbors_by_gene");
<<<<<<< HEAD
        param = new org.apache.axis.description.ParameterDesc(new QName("", "genes_id"), org.apache.axis.description.ParameterDesc.IN, new QName("http://www.w3.org/2001/XMLSchema", "string"), String.class, false, false);
        oper.addParameter(param);
        param = new org.apache.axis.description.ParameterDesc(new QName("", "offset"), org.apache.axis.description.ParameterDesc.IN, new QName("http://www.w3.org/2001/XMLSchema", "int"), int.class, false, false);
        oper.addParameter(param);
        param = new org.apache.axis.description.ParameterDesc(new QName("", "limit"), org.apache.axis.description.ParameterDesc.IN, new QName("http://www.w3.org/2001/XMLSchema", "int"), int.class, false, false);
        oper.addParameter(param);
        oper.setReturnType(new QName("SOAP/KEGG", "ArrayOfSSDBRelation"));
        oper.setReturnClass(SSDBRelation[].class);
        oper.setReturnQName(new QName("", "return"));
=======
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "genes_id"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"), java.lang.String.class, false, false);
        oper.addParameter(param);
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "offset"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "int"), int.class, false, false);
        oper.addParameter(param);
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "limit"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "int"), int.class, false, false);
        oper.addParameter(param);
        oper.setReturnType(new javax.xml.namespace.QName("SOAP/KEGG", "ArrayOfSSDBRelation"));
        oper.setReturnClass(SSDBRelation[].class);
        oper.setReturnQName(new javax.xml.namespace.QName("", "return"));
>>>>>>> 2eb394907b98577f1b916408cf22a2de6952b22d
        oper.setStyle(org.apache.axis.constants.Style.RPC);
        oper.setUse(org.apache.axis.constants.Use.ENCODED);
        _operations[11] = oper;

        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("get_reverse_best_neighbors_by_gene");
<<<<<<< HEAD
        param = new org.apache.axis.description.ParameterDesc(new QName("", "genes_id"), org.apache.axis.description.ParameterDesc.IN, new QName("http://www.w3.org/2001/XMLSchema", "string"), String.class, false, false);
        oper.addParameter(param);
        param = new org.apache.axis.description.ParameterDesc(new QName("", "offset"), org.apache.axis.description.ParameterDesc.IN, new QName("http://www.w3.org/2001/XMLSchema", "int"), int.class, false, false);
        oper.addParameter(param);
        param = new org.apache.axis.description.ParameterDesc(new QName("", "limit"), org.apache.axis.description.ParameterDesc.IN, new QName("http://www.w3.org/2001/XMLSchema", "int"), int.class, false, false);
        oper.addParameter(param);
        oper.setReturnType(new QName("SOAP/KEGG", "ArrayOfSSDBRelation"));
        oper.setReturnClass(SSDBRelation[].class);
        oper.setReturnQName(new QName("", "return"));
=======
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "genes_id"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"), java.lang.String.class, false, false);
        oper.addParameter(param);
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "offset"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "int"), int.class, false, false);
        oper.addParameter(param);
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "limit"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "int"), int.class, false, false);
        oper.addParameter(param);
        oper.setReturnType(new javax.xml.namespace.QName("SOAP/KEGG", "ArrayOfSSDBRelation"));
        oper.setReturnClass(SSDBRelation[].class);
        oper.setReturnQName(new javax.xml.namespace.QName("", "return"));
>>>>>>> 2eb394907b98577f1b916408cf22a2de6952b22d
        oper.setStyle(org.apache.axis.constants.Style.RPC);
        oper.setUse(org.apache.axis.constants.Use.ENCODED);
        _operations[12] = oper;

        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("get_paralogs_by_gene");
<<<<<<< HEAD
        param = new org.apache.axis.description.ParameterDesc(new QName("", "genes_id"), org.apache.axis.description.ParameterDesc.IN, new QName("http://www.w3.org/2001/XMLSchema", "string"), String.class, false, false);
        oper.addParameter(param);
        param = new org.apache.axis.description.ParameterDesc(new QName("", "offset"), org.apache.axis.description.ParameterDesc.IN, new QName("http://www.w3.org/2001/XMLSchema", "int"), int.class, false, false);
        oper.addParameter(param);
        param = new org.apache.axis.description.ParameterDesc(new QName("", "limit"), org.apache.axis.description.ParameterDesc.IN, new QName("http://www.w3.org/2001/XMLSchema", "int"), int.class, false, false);
        oper.addParameter(param);
        oper.setReturnType(new QName("SOAP/KEGG", "ArrayOfSSDBRelation"));
        oper.setReturnClass(SSDBRelation[].class);
        oper.setReturnQName(new QName("", "return"));
=======
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "genes_id"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"), java.lang.String.class, false, false);
        oper.addParameter(param);
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "offset"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "int"), int.class, false, false);
        oper.addParameter(param);
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "limit"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "int"), int.class, false, false);
        oper.addParameter(param);
        oper.setReturnType(new javax.xml.namespace.QName("SOAP/KEGG", "ArrayOfSSDBRelation"));
        oper.setReturnClass(SSDBRelation[].class);
        oper.setReturnQName(new javax.xml.namespace.QName("", "return"));
>>>>>>> 2eb394907b98577f1b916408cf22a2de6952b22d
        oper.setStyle(org.apache.axis.constants.Style.RPC);
        oper.setUse(org.apache.axis.constants.Use.ENCODED);
        _operations[13] = oper;

        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("get_motifs_by_gene");
<<<<<<< HEAD
        param = new org.apache.axis.description.ParameterDesc(new QName("", "genes_id"), org.apache.axis.description.ParameterDesc.IN, new QName("http://www.w3.org/2001/XMLSchema", "string"), String.class, false, false);
        oper.addParameter(param);
        param = new org.apache.axis.description.ParameterDesc(new QName("", "db"), org.apache.axis.description.ParameterDesc.IN, new QName("http://www.w3.org/2001/XMLSchema", "string"), String.class, false, false);
        oper.addParameter(param);
        oper.setReturnType(new QName("SOAP/KEGG", "ArrayOfMotifResult"));
        oper.setReturnClass(MotifResult[].class);
        oper.setReturnQName(new QName("", "return"));
=======
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "genes_id"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"), java.lang.String.class, false, false);
        oper.addParameter(param);
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "db"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"), java.lang.String.class, false, false);
        oper.addParameter(param);
        oper.setReturnType(new javax.xml.namespace.QName("SOAP/KEGG", "ArrayOfMotifResult"));
        oper.setReturnClass(MotifResult[].class);
        oper.setReturnQName(new javax.xml.namespace.QName("", "return"));
>>>>>>> 2eb394907b98577f1b916408cf22a2de6952b22d
        oper.setStyle(org.apache.axis.constants.Style.RPC);
        oper.setUse(org.apache.axis.constants.Use.ENCODED);
        _operations[14] = oper;

        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("get_genes_by_motifs");
<<<<<<< HEAD
        param = new org.apache.axis.description.ParameterDesc(new QName("", "motif_id_list"), org.apache.axis.description.ParameterDesc.IN, new QName("SOAP/KEGG", "ArrayOfstring"), String[].class, false, false);
        oper.addParameter(param);
        param = new org.apache.axis.description.ParameterDesc(new QName("", "offset"), org.apache.axis.description.ParameterDesc.IN, new QName("http://www.w3.org/2001/XMLSchema", "int"), int.class, false, false);
        oper.addParameter(param);
        param = new org.apache.axis.description.ParameterDesc(new QName("", "limit"), org.apache.axis.description.ParameterDesc.IN, new QName("http://www.w3.org/2001/XMLSchema", "int"), int.class, false, false);
        oper.addParameter(param);
        oper.setReturnType(new QName("SOAP/KEGG", "ArrayOfDefinition"));
        oper.setReturnClass(Definition[].class);
        oper.setReturnQName(new QName("", "return"));
=======
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "motif_id_list"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("SOAP/KEGG", "ArrayOfstring"), java.lang.String[].class, false, false);
        oper.addParameter(param);
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "offset"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "int"), int.class, false, false);
        oper.addParameter(param);
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "limit"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "int"), int.class, false, false);
        oper.addParameter(param);
        oper.setReturnType(new javax.xml.namespace.QName("SOAP/KEGG", "ArrayOfDefinition"));
        oper.setReturnClass(Definition[].class);
        oper.setReturnQName(new javax.xml.namespace.QName("", "return"));
>>>>>>> 2eb394907b98577f1b916408cf22a2de6952b22d
        oper.setStyle(org.apache.axis.constants.Style.RPC);
        oper.setUse(org.apache.axis.constants.Use.ENCODED);
        _operations[15] = oper;

        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("get_ko_by_gene");
<<<<<<< HEAD
        param = new org.apache.axis.description.ParameterDesc(new QName("", "genes_id"), org.apache.axis.description.ParameterDesc.IN, new QName("http://www.w3.org/2001/XMLSchema", "string"), String.class, false, false);
        oper.addParameter(param);
        oper.setReturnType(new QName("SOAP/KEGG", "ArrayOfstring"));
        oper.setReturnClass(String[].class);
        oper.setReturnQName(new QName("", "return"));
=======
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "genes_id"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"), java.lang.String.class, false, false);
        oper.addParameter(param);
        oper.setReturnType(new javax.xml.namespace.QName("SOAP/KEGG", "ArrayOfstring"));
        oper.setReturnClass(java.lang.String[].class);
        oper.setReturnQName(new javax.xml.namespace.QName("", "return"));
>>>>>>> 2eb394907b98577f1b916408cf22a2de6952b22d
        oper.setStyle(org.apache.axis.constants.Style.RPC);
        oper.setUse(org.apache.axis.constants.Use.ENCODED);
        _operations[16] = oper;

        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("get_ko_by_ko_class");
<<<<<<< HEAD
        param = new org.apache.axis.description.ParameterDesc(new QName("", "class_id"), org.apache.axis.description.ParameterDesc.IN, new QName("http://www.w3.org/2001/XMLSchema", "string"), String.class, false, false);
        oper.addParameter(param);
        oper.setReturnType(new QName("SOAP/KEGG", "ArrayOfDefinition"));
        oper.setReturnClass(Definition[].class);
        oper.setReturnQName(new QName("", "return"));
=======
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "class_id"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"), java.lang.String.class, false, false);
        oper.addParameter(param);
        oper.setReturnType(new javax.xml.namespace.QName("SOAP/KEGG", "ArrayOfDefinition"));
        oper.setReturnClass(Definition[].class);
        oper.setReturnQName(new javax.xml.namespace.QName("", "return"));
>>>>>>> 2eb394907b98577f1b916408cf22a2de6952b22d
        oper.setStyle(org.apache.axis.constants.Style.RPC);
        oper.setUse(org.apache.axis.constants.Use.ENCODED);
        _operations[17] = oper;

        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("get_genes_by_ko");
<<<<<<< HEAD
        param = new org.apache.axis.description.ParameterDesc(new QName("", "ko_id"), org.apache.axis.description.ParameterDesc.IN, new QName("http://www.w3.org/2001/XMLSchema", "string"), String.class, false, false);
        oper.addParameter(param);
        param = new org.apache.axis.description.ParameterDesc(new QName("", "org"), org.apache.axis.description.ParameterDesc.IN, new QName("http://www.w3.org/2001/XMLSchema", "string"), String.class, false, false);
        oper.addParameter(param);
        oper.setReturnType(new QName("SOAP/KEGG", "ArrayOfDefinition"));
        oper.setReturnClass(Definition[].class);
        oper.setReturnQName(new QName("", "return"));
=======
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "ko_id"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"), java.lang.String.class, false, false);
        oper.addParameter(param);
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "org"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"), java.lang.String.class, false, false);
        oper.addParameter(param);
        oper.setReturnType(new javax.xml.namespace.QName("SOAP/KEGG", "ArrayOfDefinition"));
        oper.setReturnClass(Definition[].class);
        oper.setReturnQName(new javax.xml.namespace.QName("", "return"));
>>>>>>> 2eb394907b98577f1b916408cf22a2de6952b22d
        oper.setStyle(org.apache.axis.constants.Style.RPC);
        oper.setUse(org.apache.axis.constants.Use.ENCODED);
        _operations[18] = oper;

        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("get_genes_by_ko_class");
<<<<<<< HEAD
        param = new org.apache.axis.description.ParameterDesc(new QName("", "class_id"), org.apache.axis.description.ParameterDesc.IN, new QName("http://www.w3.org/2001/XMLSchema", "string"), String.class, false, false);
        oper.addParameter(param);
        param = new org.apache.axis.description.ParameterDesc(new QName("", "org"), org.apache.axis.description.ParameterDesc.IN, new QName("http://www.w3.org/2001/XMLSchema", "string"), String.class, false, false);
        oper.addParameter(param);
        param = new org.apache.axis.description.ParameterDesc(new QName("", "offset"), org.apache.axis.description.ParameterDesc.IN, new QName("http://www.w3.org/2001/XMLSchema", "int"), int.class, false, false);
        oper.addParameter(param);
        param = new org.apache.axis.description.ParameterDesc(new QName("", "limit"), org.apache.axis.description.ParameterDesc.IN, new QName("http://www.w3.org/2001/XMLSchema", "int"), int.class, false, false);
        oper.addParameter(param);
        oper.setReturnType(new QName("SOAP/KEGG", "ArrayOfDefinition"));
        oper.setReturnClass(Definition[].class);
        oper.setReturnQName(new QName("", "return"));
=======
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "class_id"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"), java.lang.String.class, false, false);
        oper.addParameter(param);
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "org"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"), java.lang.String.class, false, false);
        oper.addParameter(param);
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "offset"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "int"), int.class, false, false);
        oper.addParameter(param);
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "limit"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "int"), int.class, false, false);
        oper.addParameter(param);
        oper.setReturnType(new javax.xml.namespace.QName("SOAP/KEGG", "ArrayOfDefinition"));
        oper.setReturnClass(Definition[].class);
        oper.setReturnQName(new javax.xml.namespace.QName("", "return"));
>>>>>>> 2eb394907b98577f1b916408cf22a2de6952b22d
        oper.setStyle(org.apache.axis.constants.Style.RPC);
        oper.setUse(org.apache.axis.constants.Use.ENCODED);
        _operations[19] = oper;

    }

    private static void _initOperationDesc3(){
        org.apache.axis.description.OperationDesc oper;
        org.apache.axis.description.ParameterDesc param;
        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("get_elements_by_pathway");
<<<<<<< HEAD
        param = new org.apache.axis.description.ParameterDesc(new QName("", "pathway_id"), org.apache.axis.description.ParameterDesc.IN, new QName("http://www.w3.org/2001/XMLSchema", "string"), String.class, false, false);
        oper.addParameter(param);
        oper.setReturnType(new QName("SOAP/KEGG", "ArrayOfPathwayElement"));
        oper.setReturnClass(PathwayElement[].class);
        oper.setReturnQName(new QName("", "return"));
=======
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "pathway_id"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"), java.lang.String.class, false, false);
        oper.addParameter(param);
        oper.setReturnType(new javax.xml.namespace.QName("SOAP/KEGG", "ArrayOfPathwayElement"));
        oper.setReturnClass(PathwayElement[].class);
        oper.setReturnQName(new javax.xml.namespace.QName("", "return"));
>>>>>>> 2eb394907b98577f1b916408cf22a2de6952b22d
        oper.setStyle(org.apache.axis.constants.Style.RPC);
        oper.setUse(org.apache.axis.constants.Use.ENCODED);
        _operations[20] = oper;

        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("get_element_relations_by_pathway");
<<<<<<< HEAD
        param = new org.apache.axis.description.ParameterDesc(new QName("", "pathway_id"), org.apache.axis.description.ParameterDesc.IN, new QName("http://www.w3.org/2001/XMLSchema", "string"), String.class, false, false);
        oper.addParameter(param);
        oper.setReturnType(new QName("SOAP/KEGG", "ArrayOfPathwayElementRelation"));
        oper.setReturnClass(PathwayElementRelation[].class);
        oper.setReturnQName(new QName("", "return"));
=======
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "pathway_id"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"), java.lang.String.class, false, false);
        oper.addParameter(param);
        oper.setReturnType(new javax.xml.namespace.QName("SOAP/KEGG", "ArrayOfPathwayElementRelation"));
        oper.setReturnClass(PathwayElementRelation[].class);
        oper.setReturnQName(new javax.xml.namespace.QName("", "return"));
>>>>>>> 2eb394907b98577f1b916408cf22a2de6952b22d
        oper.setStyle(org.apache.axis.constants.Style.RPC);
        oper.setUse(org.apache.axis.constants.Use.ENCODED);
        _operations[21] = oper;

        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("color_pathway_by_elements");
<<<<<<< HEAD
        param = new org.apache.axis.description.ParameterDesc(new QName("", "pathway_id"), org.apache.axis.description.ParameterDesc.IN, new QName("http://www.w3.org/2001/XMLSchema", "string"), String.class, false, false);
        oper.addParameter(param);
        param = new org.apache.axis.description.ParameterDesc(new QName("", "element_list"), org.apache.axis.description.ParameterDesc.IN, new QName("SOAP/KEGG", "ArrayOfint"), int[].class, false, false);
        oper.addParameter(param);
        param = new org.apache.axis.description.ParameterDesc(new QName("", "fg_color_list"), org.apache.axis.description.ParameterDesc.IN, new QName("SOAP/KEGG", "ArrayOfstring"), String[].class, false, false);
        oper.addParameter(param);
        param = new org.apache.axis.description.ParameterDesc(new QName("", "bg_color_list"), org.apache.axis.description.ParameterDesc.IN, new QName("SOAP/KEGG", "ArrayOfstring"), String[].class, false, false);
        oper.addParameter(param);
        oper.setReturnType(new QName("http://www.w3.org/2001/XMLSchema", "string"));
        oper.setReturnClass(String.class);
        oper.setReturnQName(new QName("", "return"));
=======
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "pathway_id"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"), java.lang.String.class, false, false);
        oper.addParameter(param);
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "element_list"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("SOAP/KEGG", "ArrayOfint"), int[].class, false, false);
        oper.addParameter(param);
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "fg_color_list"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("SOAP/KEGG", "ArrayOfstring"), java.lang.String[].class, false, false);
        oper.addParameter(param);
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "bg_color_list"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("SOAP/KEGG", "ArrayOfstring"), java.lang.String[].class, false, false);
        oper.addParameter(param);
        oper.setReturnType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        oper.setReturnClass(java.lang.String.class);
        oper.setReturnQName(new javax.xml.namespace.QName("", "return"));
>>>>>>> 2eb394907b98577f1b916408cf22a2de6952b22d
        oper.setStyle(org.apache.axis.constants.Style.RPC);
        oper.setUse(org.apache.axis.constants.Use.ENCODED);
        _operations[22] = oper;

        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("get_html_of_colored_pathway_by_elements");
<<<<<<< HEAD
        param = new org.apache.axis.description.ParameterDesc(new QName("", "pathway_id"), org.apache.axis.description.ParameterDesc.IN, new QName("http://www.w3.org/2001/XMLSchema", "string"), String.class, false, false);
        oper.addParameter(param);
        param = new org.apache.axis.description.ParameterDesc(new QName("", "element_list"), org.apache.axis.description.ParameterDesc.IN, new QName("SOAP/KEGG", "ArrayOfint"), int[].class, false, false);
        oper.addParameter(param);
        param = new org.apache.axis.description.ParameterDesc(new QName("", "fg_color_list"), org.apache.axis.description.ParameterDesc.IN, new QName("SOAP/KEGG", "ArrayOfstring"), String[].class, false, false);
        oper.addParameter(param);
        param = new org.apache.axis.description.ParameterDesc(new QName("", "bg_color_list"), org.apache.axis.description.ParameterDesc.IN, new QName("SOAP/KEGG", "ArrayOfstring"), String[].class, false, false);
        oper.addParameter(param);
        oper.setReturnType(new QName("http://www.w3.org/2001/XMLSchema", "string"));
        oper.setReturnClass(String.class);
        oper.setReturnQName(new QName("", "return"));
=======
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "pathway_id"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"), java.lang.String.class, false, false);
        oper.addParameter(param);
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "element_list"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("SOAP/KEGG", "ArrayOfint"), int[].class, false, false);
        oper.addParameter(param);
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "fg_color_list"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("SOAP/KEGG", "ArrayOfstring"), java.lang.String[].class, false, false);
        oper.addParameter(param);
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "bg_color_list"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("SOAP/KEGG", "ArrayOfstring"), java.lang.String[].class, false, false);
        oper.addParameter(param);
        oper.setReturnType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        oper.setReturnClass(java.lang.String.class);
        oper.setReturnQName(new javax.xml.namespace.QName("", "return"));
>>>>>>> 2eb394907b98577f1b916408cf22a2de6952b22d
        oper.setStyle(org.apache.axis.constants.Style.RPC);
        oper.setUse(org.apache.axis.constants.Use.ENCODED);
        _operations[23] = oper;

        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("mark_pathway_by_objects");
<<<<<<< HEAD
        param = new org.apache.axis.description.ParameterDesc(new QName("", "pathway_id"), org.apache.axis.description.ParameterDesc.IN, new QName("http://www.w3.org/2001/XMLSchema", "string"), String.class, false, false);
        oper.addParameter(param);
        param = new org.apache.axis.description.ParameterDesc(new QName("", "object_id_list"), org.apache.axis.description.ParameterDesc.IN, new QName("SOAP/KEGG", "ArrayOfstring"), String[].class, false, false);
        oper.addParameter(param);
        oper.setReturnType(new QName("http://www.w3.org/2001/XMLSchema", "string"));
        oper.setReturnClass(String.class);
        oper.setReturnQName(new QName("", "return"));
=======
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "pathway_id"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"), java.lang.String.class, false, false);
        oper.addParameter(param);
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "object_id_list"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("SOAP/KEGG", "ArrayOfstring"), java.lang.String[].class, false, false);
        oper.addParameter(param);
        oper.setReturnType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        oper.setReturnClass(java.lang.String.class);
        oper.setReturnQName(new javax.xml.namespace.QName("", "return"));
>>>>>>> 2eb394907b98577f1b916408cf22a2de6952b22d
        oper.setStyle(org.apache.axis.constants.Style.RPC);
        oper.setUse(org.apache.axis.constants.Use.ENCODED);
        _operations[24] = oper;

        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("color_pathway_by_objects");
<<<<<<< HEAD
        param = new org.apache.axis.description.ParameterDesc(new QName("", "pathway_id"), org.apache.axis.description.ParameterDesc.IN, new QName("http://www.w3.org/2001/XMLSchema", "string"), String.class, false, false);
        oper.addParameter(param);
        param = new org.apache.axis.description.ParameterDesc(new QName("", "object_id_list"), org.apache.axis.description.ParameterDesc.IN, new QName("SOAP/KEGG", "ArrayOfstring"), String[].class, false, false);
        oper.addParameter(param);
        param = new org.apache.axis.description.ParameterDesc(new QName("", "fg_color_list"), org.apache.axis.description.ParameterDesc.IN, new QName("SOAP/KEGG", "ArrayOfstring"), String[].class, false, false);
        oper.addParameter(param);
        param = new org.apache.axis.description.ParameterDesc(new QName("", "bg_color_list"), org.apache.axis.description.ParameterDesc.IN, new QName("SOAP/KEGG", "ArrayOfstring"), String[].class, false, false);
        oper.addParameter(param);
        oper.setReturnType(new QName("http://www.w3.org/2001/XMLSchema", "string"));
        oper.setReturnClass(String.class);
        oper.setReturnQName(new QName("", "return"));
=======
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "pathway_id"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"), java.lang.String.class, false, false);
        oper.addParameter(param);
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "object_id_list"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("SOAP/KEGG", "ArrayOfstring"), java.lang.String[].class, false, false);
        oper.addParameter(param);
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "fg_color_list"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("SOAP/KEGG", "ArrayOfstring"), java.lang.String[].class, false, false);
        oper.addParameter(param);
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "bg_color_list"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("SOAP/KEGG", "ArrayOfstring"), java.lang.String[].class, false, false);
        oper.addParameter(param);
        oper.setReturnType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        oper.setReturnClass(java.lang.String.class);
        oper.setReturnQName(new javax.xml.namespace.QName("", "return"));
>>>>>>> 2eb394907b98577f1b916408cf22a2de6952b22d
        oper.setStyle(org.apache.axis.constants.Style.RPC);
        oper.setUse(org.apache.axis.constants.Use.ENCODED);
        _operations[25] = oper;

        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("get_html_of_marked_pathway_by_objects");
<<<<<<< HEAD
        param = new org.apache.axis.description.ParameterDesc(new QName("", "pathway_id"), org.apache.axis.description.ParameterDesc.IN, new QName("http://www.w3.org/2001/XMLSchema", "string"), String.class, false, false);
        oper.addParameter(param);
        param = new org.apache.axis.description.ParameterDesc(new QName("", "object_id_list"), org.apache.axis.description.ParameterDesc.IN, new QName("SOAP/KEGG", "ArrayOfstring"), String[].class, false, false);
        oper.addParameter(param);
        oper.setReturnType(new QName("http://www.w3.org/2001/XMLSchema", "string"));
        oper.setReturnClass(String.class);
        oper.setReturnQName(new QName("", "return"));
=======
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "pathway_id"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"), java.lang.String.class, false, false);
        oper.addParameter(param);
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "object_id_list"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("SOAP/KEGG", "ArrayOfstring"), java.lang.String[].class, false, false);
        oper.addParameter(param);
        oper.setReturnType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        oper.setReturnClass(java.lang.String.class);
        oper.setReturnQName(new javax.xml.namespace.QName("", "return"));
>>>>>>> 2eb394907b98577f1b916408cf22a2de6952b22d
        oper.setStyle(org.apache.axis.constants.Style.RPC);
        oper.setUse(org.apache.axis.constants.Use.ENCODED);
        _operations[26] = oper;

        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("get_html_of_colored_pathway_by_objects");
<<<<<<< HEAD
        param = new org.apache.axis.description.ParameterDesc(new QName("", "pathway_id"), org.apache.axis.description.ParameterDesc.IN, new QName("http://www.w3.org/2001/XMLSchema", "string"), String.class, false, false);
        oper.addParameter(param);
        param = new org.apache.axis.description.ParameterDesc(new QName("", "object_id_list"), org.apache.axis.description.ParameterDesc.IN, new QName("SOAP/KEGG", "ArrayOfstring"), String[].class, false, false);
        oper.addParameter(param);
        param = new org.apache.axis.description.ParameterDesc(new QName("", "fg_color_list"), org.apache.axis.description.ParameterDesc.IN, new QName("SOAP/KEGG", "ArrayOfstring"), String[].class, false, false);
        oper.addParameter(param);
        param = new org.apache.axis.description.ParameterDesc(new QName("", "bg_color_list"), org.apache.axis.description.ParameterDesc.IN, new QName("SOAP/KEGG", "ArrayOfstring"), String[].class, false, false);
        oper.addParameter(param);
        oper.setReturnType(new QName("http://www.w3.org/2001/XMLSchema", "string"));
        oper.setReturnClass(String.class);
        oper.setReturnQName(new QName("", "return"));
=======
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "pathway_id"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"), java.lang.String.class, false, false);
        oper.addParameter(param);
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "object_id_list"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("SOAP/KEGG", "ArrayOfstring"), java.lang.String[].class, false, false);
        oper.addParameter(param);
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "fg_color_list"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("SOAP/KEGG", "ArrayOfstring"), java.lang.String[].class, false, false);
        oper.addParameter(param);
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "bg_color_list"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("SOAP/KEGG", "ArrayOfstring"), java.lang.String[].class, false, false);
        oper.addParameter(param);
        oper.setReturnType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        oper.setReturnClass(java.lang.String.class);
        oper.setReturnQName(new javax.xml.namespace.QName("", "return"));
>>>>>>> 2eb394907b98577f1b916408cf22a2de6952b22d
        oper.setStyle(org.apache.axis.constants.Style.RPC);
        oper.setUse(org.apache.axis.constants.Use.ENCODED);
        _operations[27] = oper;

        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("get_genes_by_pathway");
<<<<<<< HEAD
        param = new org.apache.axis.description.ParameterDesc(new QName("", "pathway_id"), org.apache.axis.description.ParameterDesc.IN, new QName("http://www.w3.org/2001/XMLSchema", "string"), String.class, false, false);
        oper.addParameter(param);
        oper.setReturnType(new QName("SOAP/KEGG", "ArrayOfstring"));
        oper.setReturnClass(String[].class);
        oper.setReturnQName(new QName("", "return"));
=======
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "pathway_id"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"), java.lang.String.class, false, false);
        oper.addParameter(param);
        oper.setReturnType(new javax.xml.namespace.QName("SOAP/KEGG", "ArrayOfstring"));
        oper.setReturnClass(java.lang.String[].class);
        oper.setReturnQName(new javax.xml.namespace.QName("", "return"));
>>>>>>> 2eb394907b98577f1b916408cf22a2de6952b22d
        oper.setStyle(org.apache.axis.constants.Style.RPC);
        oper.setUse(org.apache.axis.constants.Use.ENCODED);
        _operations[28] = oper;

        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("get_enzymes_by_pathway");
<<<<<<< HEAD
        param = new org.apache.axis.description.ParameterDesc(new QName("", "pathway_id"), org.apache.axis.description.ParameterDesc.IN, new QName("http://www.w3.org/2001/XMLSchema", "string"), String.class, false, false);
        oper.addParameter(param);
        oper.setReturnType(new QName("SOAP/KEGG", "ArrayOfstring"));
        oper.setReturnClass(String[].class);
        oper.setReturnQName(new QName("", "return"));
=======
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "pathway_id"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"), java.lang.String.class, false, false);
        oper.addParameter(param);
        oper.setReturnType(new javax.xml.namespace.QName("SOAP/KEGG", "ArrayOfstring"));
        oper.setReturnClass(java.lang.String[].class);
        oper.setReturnQName(new javax.xml.namespace.QName("", "return"));
>>>>>>> 2eb394907b98577f1b916408cf22a2de6952b22d
        oper.setStyle(org.apache.axis.constants.Style.RPC);
        oper.setUse(org.apache.axis.constants.Use.ENCODED);
        _operations[29] = oper;

    }

    private static void _initOperationDesc4(){
        org.apache.axis.description.OperationDesc oper;
        org.apache.axis.description.ParameterDesc param;
        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("get_reactions_by_pathway");
<<<<<<< HEAD
        param = new org.apache.axis.description.ParameterDesc(new QName("", "pathway_id"), org.apache.axis.description.ParameterDesc.IN, new QName("http://www.w3.org/2001/XMLSchema", "string"), String.class, false, false);
        oper.addParameter(param);
        oper.setReturnType(new QName("SOAP/KEGG", "ArrayOfstring"));
        oper.setReturnClass(String[].class);
        oper.setReturnQName(new QName("", "return"));
=======
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "pathway_id"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"), java.lang.String.class, false, false);
        oper.addParameter(param);
        oper.setReturnType(new javax.xml.namespace.QName("SOAP/KEGG", "ArrayOfstring"));
        oper.setReturnClass(java.lang.String[].class);
        oper.setReturnQName(new javax.xml.namespace.QName("", "return"));
>>>>>>> 2eb394907b98577f1b916408cf22a2de6952b22d
        oper.setStyle(org.apache.axis.constants.Style.RPC);
        oper.setUse(org.apache.axis.constants.Use.ENCODED);
        _operations[30] = oper;

        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("get_pathways_by_genes");
<<<<<<< HEAD
        param = new org.apache.axis.description.ParameterDesc(new QName("", "genes_id_list"), org.apache.axis.description.ParameterDesc.IN, new QName("SOAP/KEGG", "ArrayOfstring"), String[].class, false, false);
        oper.addParameter(param);
        oper.setReturnType(new QName("SOAP/KEGG", "ArrayOfstring"));
        oper.setReturnClass(String[].class);
        oper.setReturnQName(new QName("", "return"));
=======
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "genes_id_list"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("SOAP/KEGG", "ArrayOfstring"), java.lang.String[].class, false, false);
        oper.addParameter(param);
        oper.setReturnType(new javax.xml.namespace.QName("SOAP/KEGG", "ArrayOfstring"));
        oper.setReturnClass(java.lang.String[].class);
        oper.setReturnQName(new javax.xml.namespace.QName("", "return"));
>>>>>>> 2eb394907b98577f1b916408cf22a2de6952b22d
        oper.setStyle(org.apache.axis.constants.Style.RPC);
        oper.setUse(org.apache.axis.constants.Use.ENCODED);
        _operations[31] = oper;

        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("get_pathways_by_enzymes");
<<<<<<< HEAD
        param = new org.apache.axis.description.ParameterDesc(new QName("", "enzyme_id_list"), org.apache.axis.description.ParameterDesc.IN, new QName("SOAP/KEGG", "ArrayOfstring"), String[].class, false, false);
        oper.addParameter(param);
        oper.setReturnType(new QName("SOAP/KEGG", "ArrayOfstring"));
        oper.setReturnClass(String[].class);
        oper.setReturnQName(new QName("", "return"));
=======
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "enzyme_id_list"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("SOAP/KEGG", "ArrayOfstring"), java.lang.String[].class, false, false);
        oper.addParameter(param);
        oper.setReturnType(new javax.xml.namespace.QName("SOAP/KEGG", "ArrayOfstring"));
        oper.setReturnClass(java.lang.String[].class);
        oper.setReturnQName(new javax.xml.namespace.QName("", "return"));
>>>>>>> 2eb394907b98577f1b916408cf22a2de6952b22d
        oper.setStyle(org.apache.axis.constants.Style.RPC);
        oper.setUse(org.apache.axis.constants.Use.ENCODED);
        _operations[32] = oper;

        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("get_pathways_by_reactions");
<<<<<<< HEAD
        param = new org.apache.axis.description.ParameterDesc(new QName("", "reaction_id_list"), org.apache.axis.description.ParameterDesc.IN, new QName("SOAP/KEGG", "ArrayOfstring"), String[].class, false, false);
        oper.addParameter(param);
        oper.setReturnType(new QName("SOAP/KEGG", "ArrayOfstring"));
        oper.setReturnClass(String[].class);
        oper.setReturnQName(new QName("", "return"));
=======
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "reaction_id_list"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("SOAP/KEGG", "ArrayOfstring"), java.lang.String[].class, false, false);
        oper.addParameter(param);
        oper.setReturnType(new javax.xml.namespace.QName("SOAP/KEGG", "ArrayOfstring"));
        oper.setReturnClass(java.lang.String[].class);
        oper.setReturnQName(new javax.xml.namespace.QName("", "return"));
>>>>>>> 2eb394907b98577f1b916408cf22a2de6952b22d
        oper.setStyle(org.apache.axis.constants.Style.RPC);
        oper.setUse(org.apache.axis.constants.Use.ENCODED);
        _operations[33] = oper;

        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("get_linked_pathways");
<<<<<<< HEAD
        param = new org.apache.axis.description.ParameterDesc(new QName("", "pathway_id"), org.apache.axis.description.ParameterDesc.IN, new QName("http://www.w3.org/2001/XMLSchema", "string"), String.class, false, false);
        oper.addParameter(param);
        oper.setReturnType(new QName("SOAP/KEGG", "ArrayOfstring"));
        oper.setReturnClass(String[].class);
        oper.setReturnQName(new QName("", "return"));
=======
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "pathway_id"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"), java.lang.String.class, false, false);
        oper.addParameter(param);
        oper.setReturnType(new javax.xml.namespace.QName("SOAP/KEGG", "ArrayOfstring"));
        oper.setReturnClass(java.lang.String[].class);
        oper.setReturnQName(new javax.xml.namespace.QName("", "return"));
>>>>>>> 2eb394907b98577f1b916408cf22a2de6952b22d
        oper.setStyle(org.apache.axis.constants.Style.RPC);
        oper.setUse(org.apache.axis.constants.Use.ENCODED);
        _operations[34] = oper;

        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("get_genes_by_enzyme");
<<<<<<< HEAD
        param = new org.apache.axis.description.ParameterDesc(new QName("", "enzyme_id"), org.apache.axis.description.ParameterDesc.IN, new QName("http://www.w3.org/2001/XMLSchema", "string"), String.class, false, false);
        oper.addParameter(param);
        param = new org.apache.axis.description.ParameterDesc(new QName("", "org"), org.apache.axis.description.ParameterDesc.IN, new QName("http://www.w3.org/2001/XMLSchema", "string"), String.class, false, false);
        oper.addParameter(param);
        oper.setReturnType(new QName("SOAP/KEGG", "ArrayOfstring"));
        oper.setReturnClass(String[].class);
        oper.setReturnQName(new QName("", "return"));
=======
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "enzyme_id"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"), java.lang.String.class, false, false);
        oper.addParameter(param);
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "org"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"), java.lang.String.class, false, false);
        oper.addParameter(param);
        oper.setReturnType(new javax.xml.namespace.QName("SOAP/KEGG", "ArrayOfstring"));
        oper.setReturnClass(java.lang.String[].class);
        oper.setReturnQName(new javax.xml.namespace.QName("", "return"));
>>>>>>> 2eb394907b98577f1b916408cf22a2de6952b22d
        oper.setStyle(org.apache.axis.constants.Style.RPC);
        oper.setUse(org.apache.axis.constants.Use.ENCODED);
        _operations[35] = oper;

        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("get_enzymes_by_gene");
<<<<<<< HEAD
        param = new org.apache.axis.description.ParameterDesc(new QName("", "genes_id"), org.apache.axis.description.ParameterDesc.IN, new QName("http://www.w3.org/2001/XMLSchema", "string"), String.class, false, false);
        oper.addParameter(param);
        oper.setReturnType(new QName("SOAP/KEGG", "ArrayOfstring"));
        oper.setReturnClass(String[].class);
        oper.setReturnQName(new QName("", "return"));
=======
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "genes_id"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"), java.lang.String.class, false, false);
        oper.addParameter(param);
        oper.setReturnType(new javax.xml.namespace.QName("SOAP/KEGG", "ArrayOfstring"));
        oper.setReturnClass(java.lang.String[].class);
        oper.setReturnQName(new javax.xml.namespace.QName("", "return"));
>>>>>>> 2eb394907b98577f1b916408cf22a2de6952b22d
        oper.setStyle(org.apache.axis.constants.Style.RPC);
        oper.setUse(org.apache.axis.constants.Use.ENCODED);
        _operations[36] = oper;

        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("get_enzymes_by_reaction");
<<<<<<< HEAD
        param = new org.apache.axis.description.ParameterDesc(new QName("", "reaction_id"), org.apache.axis.description.ParameterDesc.IN, new QName("http://www.w3.org/2001/XMLSchema", "string"), String.class, false, false);
        oper.addParameter(param);
        oper.setReturnType(new QName("SOAP/KEGG", "ArrayOfstring"));
        oper.setReturnClass(String[].class);
        oper.setReturnQName(new QName("", "return"));
=======
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "reaction_id"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"), java.lang.String.class, false, false);
        oper.addParameter(param);
        oper.setReturnType(new javax.xml.namespace.QName("SOAP/KEGG", "ArrayOfstring"));
        oper.setReturnClass(java.lang.String[].class);
        oper.setReturnQName(new javax.xml.namespace.QName("", "return"));
>>>>>>> 2eb394907b98577f1b916408cf22a2de6952b22d
        oper.setStyle(org.apache.axis.constants.Style.RPC);
        oper.setUse(org.apache.axis.constants.Use.ENCODED);
        _operations[37] = oper;

        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("get_reactions_by_enzyme");
<<<<<<< HEAD
        param = new org.apache.axis.description.ParameterDesc(new QName("", "enzyme_id"), org.apache.axis.description.ParameterDesc.IN, new QName("http://www.w3.org/2001/XMLSchema", "string"), String.class, false, false);
        oper.addParameter(param);
        oper.setReturnType(new QName("SOAP/KEGG", "ArrayOfstring"));
        oper.setReturnClass(String[].class);
        oper.setReturnQName(new QName("", "return"));
=======
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "enzyme_id"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"), java.lang.String.class, false, false);
        oper.addParameter(param);
        oper.setReturnType(new javax.xml.namespace.QName("SOAP/KEGG", "ArrayOfstring"));
        oper.setReturnClass(java.lang.String[].class);
        oper.setReturnQName(new javax.xml.namespace.QName("", "return"));
>>>>>>> 2eb394907b98577f1b916408cf22a2de6952b22d
        oper.setStyle(org.apache.axis.constants.Style.RPC);
        oper.setUse(org.apache.axis.constants.Use.ENCODED);
        _operations[38] = oper;

        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("get_genes_by_organism");
<<<<<<< HEAD
        param = new org.apache.axis.description.ParameterDesc(new QName("", "org"), org.apache.axis.description.ParameterDesc.IN, new QName("http://www.w3.org/2001/XMLSchema", "string"), String.class, false, false);
        oper.addParameter(param);
        param = new org.apache.axis.description.ParameterDesc(new QName("", "offset"), org.apache.axis.description.ParameterDesc.IN, new QName("http://www.w3.org/2001/XMLSchema", "int"), int.class, false, false);
        oper.addParameter(param);
        param = new org.apache.axis.description.ParameterDesc(new QName("", "limit"), org.apache.axis.description.ParameterDesc.IN, new QName("http://www.w3.org/2001/XMLSchema", "int"), int.class, false, false);
        oper.addParameter(param);
        oper.setReturnType(new QName("SOAP/KEGG", "ArrayOfstring"));
        oper.setReturnClass(String[].class);
        oper.setReturnQName(new QName("", "return"));
=======
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "org"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"), java.lang.String.class, false, false);
        oper.addParameter(param);
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "offset"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "int"), int.class, false, false);
        oper.addParameter(param);
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "limit"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "int"), int.class, false, false);
        oper.addParameter(param);
        oper.setReturnType(new javax.xml.namespace.QName("SOAP/KEGG", "ArrayOfstring"));
        oper.setReturnClass(java.lang.String[].class);
        oper.setReturnQName(new javax.xml.namespace.QName("", "return"));
>>>>>>> 2eb394907b98577f1b916408cf22a2de6952b22d
        oper.setStyle(org.apache.axis.constants.Style.RPC);
        oper.setUse(org.apache.axis.constants.Use.ENCODED);
        _operations[39] = oper;

    }

    private static void _initOperationDesc5(){
        org.apache.axis.description.OperationDesc oper;
        org.apache.axis.description.ParameterDesc param;
        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("get_number_of_genes_by_organism");
<<<<<<< HEAD
        param = new org.apache.axis.description.ParameterDesc(new QName("", "abbr"), org.apache.axis.description.ParameterDesc.IN, new QName("http://www.w3.org/2001/XMLSchema", "string"), String.class, false, false);
        oper.addParameter(param);
        oper.setReturnType(new QName("http://www.w3.org/2001/XMLSchema", "int"));
        oper.setReturnClass(int.class);
        oper.setReturnQName(new QName("", "return"));
=======
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "abbr"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"), java.lang.String.class, false, false);
        oper.addParameter(param);
        oper.setReturnType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "int"));
        oper.setReturnClass(int.class);
        oper.setReturnQName(new javax.xml.namespace.QName("", "return"));
>>>>>>> 2eb394907b98577f1b916408cf22a2de6952b22d
        oper.setStyle(org.apache.axis.constants.Style.RPC);
        oper.setUse(org.apache.axis.constants.Use.ENCODED);
        _operations[40] = oper;

        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("get_reactions_by_glycan");
<<<<<<< HEAD
        param = new org.apache.axis.description.ParameterDesc(new QName("", "glycan_id"), org.apache.axis.description.ParameterDesc.IN, new QName("http://www.w3.org/2001/XMLSchema", "string"), String.class, false, false);
        oper.addParameter(param);
        oper.setReturnType(new QName("SOAP/KEGG", "ArrayOfstring"));
        oper.setReturnClass(String[].class);
        oper.setReturnQName(new QName("", "return"));
=======
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "glycan_id"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"), java.lang.String.class, false, false);
        oper.addParameter(param);
        oper.setReturnType(new javax.xml.namespace.QName("SOAP/KEGG", "ArrayOfstring"));
        oper.setReturnClass(java.lang.String[].class);
        oper.setReturnQName(new javax.xml.namespace.QName("", "return"));
>>>>>>> 2eb394907b98577f1b916408cf22a2de6952b22d
        oper.setStyle(org.apache.axis.constants.Style.RPC);
        oper.setUse(org.apache.axis.constants.Use.ENCODED);
        _operations[41] = oper;

        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("get_reactions_by_compound");
<<<<<<< HEAD
        param = new org.apache.axis.description.ParameterDesc(new QName("", "compound_id"), org.apache.axis.description.ParameterDesc.IN, new QName("http://www.w3.org/2001/XMLSchema", "string"), String.class, false, false);
        oper.addParameter(param);
        oper.setReturnType(new QName("SOAP/KEGG", "ArrayOfstring"));
        oper.setReturnClass(String[].class);
        oper.setReturnQName(new QName("", "return"));
=======
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "compound_id"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"), java.lang.String.class, false, false);
        oper.addParameter(param);
        oper.setReturnType(new javax.xml.namespace.QName("SOAP/KEGG", "ArrayOfstring"));
        oper.setReturnClass(java.lang.String[].class);
        oper.setReturnQName(new javax.xml.namespace.QName("", "return"));
>>>>>>> 2eb394907b98577f1b916408cf22a2de6952b22d
        oper.setStyle(org.apache.axis.constants.Style.RPC);
        oper.setUse(org.apache.axis.constants.Use.ENCODED);
        _operations[42] = oper;

        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("get_enzymes_by_glycan");
<<<<<<< HEAD
        param = new org.apache.axis.description.ParameterDesc(new QName("", "glycan_id"), org.apache.axis.description.ParameterDesc.IN, new QName("http://www.w3.org/2001/XMLSchema", "string"), String.class, false, false);
        oper.addParameter(param);
        oper.setReturnType(new QName("SOAP/KEGG", "ArrayOfstring"));
        oper.setReturnClass(String[].class);
        oper.setReturnQName(new QName("", "return"));
=======
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "glycan_id"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"), java.lang.String.class, false, false);
        oper.addParameter(param);
        oper.setReturnType(new javax.xml.namespace.QName("SOAP/KEGG", "ArrayOfstring"));
        oper.setReturnClass(java.lang.String[].class);
        oper.setReturnQName(new javax.xml.namespace.QName("", "return"));
>>>>>>> 2eb394907b98577f1b916408cf22a2de6952b22d
        oper.setStyle(org.apache.axis.constants.Style.RPC);
        oper.setUse(org.apache.axis.constants.Use.ENCODED);
        _operations[43] = oper;

        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("get_enzymes_by_compound");
<<<<<<< HEAD
        param = new org.apache.axis.description.ParameterDesc(new QName("", "compound_id"), org.apache.axis.description.ParameterDesc.IN, new QName("http://www.w3.org/2001/XMLSchema", "string"), String.class, false, false);
        oper.addParameter(param);
        oper.setReturnType(new QName("SOAP/KEGG", "ArrayOfstring"));
        oper.setReturnClass(String[].class);
        oper.setReturnQName(new QName("", "return"));
=======
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "compound_id"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"), java.lang.String.class, false, false);
        oper.addParameter(param);
        oper.setReturnType(new javax.xml.namespace.QName("SOAP/KEGG", "ArrayOfstring"));
        oper.setReturnClass(java.lang.String[].class);
        oper.setReturnQName(new javax.xml.namespace.QName("", "return"));
>>>>>>> 2eb394907b98577f1b916408cf22a2de6952b22d
        oper.setStyle(org.apache.axis.constants.Style.RPC);
        oper.setUse(org.apache.axis.constants.Use.ENCODED);
        _operations[44] = oper;

        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("get_pathways_by_compounds");
<<<<<<< HEAD
        param = new org.apache.axis.description.ParameterDesc(new QName("", "compound_id_list"), org.apache.axis.description.ParameterDesc.IN, new QName("SOAP/KEGG", "ArrayOfstring"), String[].class, false, false);
        oper.addParameter(param);
        oper.setReturnType(new QName("SOAP/KEGG", "ArrayOfstring"));
        oper.setReturnClass(String[].class);
        oper.setReturnQName(new QName("", "return"));
=======
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "compound_id_list"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("SOAP/KEGG", "ArrayOfstring"), java.lang.String[].class, false, false);
        oper.addParameter(param);
        oper.setReturnType(new javax.xml.namespace.QName("SOAP/KEGG", "ArrayOfstring"));
        oper.setReturnClass(java.lang.String[].class);
        oper.setReturnQName(new javax.xml.namespace.QName("", "return"));
>>>>>>> 2eb394907b98577f1b916408cf22a2de6952b22d
        oper.setStyle(org.apache.axis.constants.Style.RPC);
        oper.setUse(org.apache.axis.constants.Use.ENCODED);
        _operations[45] = oper;

        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("get_pathways_by_glycans");
<<<<<<< HEAD
        param = new org.apache.axis.description.ParameterDesc(new QName("", "glycan_id_list"), org.apache.axis.description.ParameterDesc.IN, new QName("SOAP/KEGG", "ArrayOfstring"), String[].class, false, false);
        oper.addParameter(param);
        oper.setReturnType(new QName("SOAP/KEGG", "ArrayOfstring"));
        oper.setReturnClass(String[].class);
        oper.setReturnQName(new QName("", "return"));
=======
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "glycan_id_list"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("SOAP/KEGG", "ArrayOfstring"), java.lang.String[].class, false, false);
        oper.addParameter(param);
        oper.setReturnType(new javax.xml.namespace.QName("SOAP/KEGG", "ArrayOfstring"));
        oper.setReturnClass(java.lang.String[].class);
        oper.setReturnQName(new javax.xml.namespace.QName("", "return"));
>>>>>>> 2eb394907b98577f1b916408cf22a2de6952b22d
        oper.setStyle(org.apache.axis.constants.Style.RPC);
        oper.setUse(org.apache.axis.constants.Use.ENCODED);
        _operations[46] = oper;

        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("get_compounds_by_pathway");
<<<<<<< HEAD
        param = new org.apache.axis.description.ParameterDesc(new QName("", "pathway_id"), org.apache.axis.description.ParameterDesc.IN, new QName("http://www.w3.org/2001/XMLSchema", "string"), String.class, false, false);
        oper.addParameter(param);
        oper.setReturnType(new QName("SOAP/KEGG", "ArrayOfstring"));
        oper.setReturnClass(String[].class);
        oper.setReturnQName(new QName("", "return"));
=======
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "pathway_id"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"), java.lang.String.class, false, false);
        oper.addParameter(param);
        oper.setReturnType(new javax.xml.namespace.QName("SOAP/KEGG", "ArrayOfstring"));
        oper.setReturnClass(java.lang.String[].class);
        oper.setReturnQName(new javax.xml.namespace.QName("", "return"));
>>>>>>> 2eb394907b98577f1b916408cf22a2de6952b22d
        oper.setStyle(org.apache.axis.constants.Style.RPC);
        oper.setUse(org.apache.axis.constants.Use.ENCODED);
        _operations[47] = oper;

        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("get_glycans_by_pathway");
<<<<<<< HEAD
        param = new org.apache.axis.description.ParameterDesc(new QName("", "pathway_id"), org.apache.axis.description.ParameterDesc.IN, new QName("http://www.w3.org/2001/XMLSchema", "string"), String.class, false, false);
        oper.addParameter(param);
        oper.setReturnType(new QName("SOAP/KEGG", "ArrayOfstring"));
        oper.setReturnClass(String[].class);
        oper.setReturnQName(new QName("", "return"));
=======
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "pathway_id"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"), java.lang.String.class, false, false);
        oper.addParameter(param);
        oper.setReturnType(new javax.xml.namespace.QName("SOAP/KEGG", "ArrayOfstring"));
        oper.setReturnClass(java.lang.String[].class);
        oper.setReturnQName(new javax.xml.namespace.QName("", "return"));
>>>>>>> 2eb394907b98577f1b916408cf22a2de6952b22d
        oper.setStyle(org.apache.axis.constants.Style.RPC);
        oper.setUse(org.apache.axis.constants.Use.ENCODED);
        _operations[48] = oper;

        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("get_compounds_by_reaction");
<<<<<<< HEAD
        param = new org.apache.axis.description.ParameterDesc(new QName("", "reaction_id"), org.apache.axis.description.ParameterDesc.IN, new QName("http://www.w3.org/2001/XMLSchema", "string"), String.class, false, false);
        oper.addParameter(param);
        oper.setReturnType(new QName("SOAP/KEGG", "ArrayOfstring"));
        oper.setReturnClass(String[].class);
        oper.setReturnQName(new QName("", "return"));
=======
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "reaction_id"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"), java.lang.String.class, false, false);
        oper.addParameter(param);
        oper.setReturnType(new javax.xml.namespace.QName("SOAP/KEGG", "ArrayOfstring"));
        oper.setReturnClass(java.lang.String[].class);
        oper.setReturnQName(new javax.xml.namespace.QName("", "return"));
>>>>>>> 2eb394907b98577f1b916408cf22a2de6952b22d
        oper.setStyle(org.apache.axis.constants.Style.RPC);
        oper.setUse(org.apache.axis.constants.Use.ENCODED);
        _operations[49] = oper;

    }

    private static void _initOperationDesc6(){
        org.apache.axis.description.OperationDesc oper;
        org.apache.axis.description.ParameterDesc param;
        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("get_glycans_by_reaction");
<<<<<<< HEAD
        param = new org.apache.axis.description.ParameterDesc(new QName("", "reaction_id"), org.apache.axis.description.ParameterDesc.IN, new QName("http://www.w3.org/2001/XMLSchema", "string"), String.class, false, false);
        oper.addParameter(param);
        oper.setReturnType(new QName("SOAP/KEGG", "ArrayOfstring"));
        oper.setReturnClass(String[].class);
        oper.setReturnQName(new QName("", "return"));
=======
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "reaction_id"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"), java.lang.String.class, false, false);
        oper.addParameter(param);
        oper.setReturnType(new javax.xml.namespace.QName("SOAP/KEGG", "ArrayOfstring"));
        oper.setReturnClass(java.lang.String[].class);
        oper.setReturnQName(new javax.xml.namespace.QName("", "return"));
>>>>>>> 2eb394907b98577f1b916408cf22a2de6952b22d
        oper.setStyle(org.apache.axis.constants.Style.RPC);
        oper.setUse(org.apache.axis.constants.Use.ENCODED);
        _operations[50] = oper;

        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("get_compounds_by_enzyme");
<<<<<<< HEAD
        param = new org.apache.axis.description.ParameterDesc(new QName("", "enzyme_id"), org.apache.axis.description.ParameterDesc.IN, new QName("http://www.w3.org/2001/XMLSchema", "string"), String.class, false, false);
        oper.addParameter(param);
        oper.setReturnType(new QName("SOAP/KEGG", "ArrayOfstring"));
        oper.setReturnClass(String[].class);
        oper.setReturnQName(new QName("", "return"));
=======
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "enzyme_id"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"), java.lang.String.class, false, false);
        oper.addParameter(param);
        oper.setReturnType(new javax.xml.namespace.QName("SOAP/KEGG", "ArrayOfstring"));
        oper.setReturnClass(java.lang.String[].class);
        oper.setReturnQName(new javax.xml.namespace.QName("", "return"));
>>>>>>> 2eb394907b98577f1b916408cf22a2de6952b22d
        oper.setStyle(org.apache.axis.constants.Style.RPC);
        oper.setUse(org.apache.axis.constants.Use.ENCODED);
        _operations[51] = oper;

        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("get_glycans_by_enzyme");
<<<<<<< HEAD
        param = new org.apache.axis.description.ParameterDesc(new QName("", "enzyme_id"), org.apache.axis.description.ParameterDesc.IN, new QName("http://www.w3.org/2001/XMLSchema", "string"), String.class, false, false);
        oper.addParameter(param);
        oper.setReturnType(new QName("SOAP/KEGG", "ArrayOfstring"));
        oper.setReturnClass(String[].class);
        oper.setReturnQName(new QName("", "return"));
=======
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "enzyme_id"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"), java.lang.String.class, false, false);
        oper.addParameter(param);
        oper.setReturnType(new javax.xml.namespace.QName("SOAP/KEGG", "ArrayOfstring"));
        oper.setReturnClass(java.lang.String[].class);
        oper.setReturnQName(new javax.xml.namespace.QName("", "return"));
>>>>>>> 2eb394907b98577f1b916408cf22a2de6952b22d
        oper.setStyle(org.apache.axis.constants.Style.RPC);
        oper.setUse(org.apache.axis.constants.Use.ENCODED);
        _operations[52] = oper;

        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("convert_mol_to_kcf");
<<<<<<< HEAD
        param = new org.apache.axis.description.ParameterDesc(new QName("", "mol_text"), org.apache.axis.description.ParameterDesc.IN, new QName("http://www.w3.org/2001/XMLSchema", "string"), String.class, false, false);
        oper.addParameter(param);
        oper.setReturnType(new QName("http://www.w3.org/2001/XMLSchema", "string"));
        oper.setReturnClass(String.class);
        oper.setReturnQName(new QName("", "return"));
=======
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "mol_text"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"), java.lang.String.class, false, false);
        oper.addParameter(param);
        oper.setReturnType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        oper.setReturnClass(java.lang.String.class);
        oper.setReturnQName(new javax.xml.namespace.QName("", "return"));
>>>>>>> 2eb394907b98577f1b916408cf22a2de6952b22d
        oper.setStyle(org.apache.axis.constants.Style.RPC);
        oper.setUse(org.apache.axis.constants.Use.ENCODED);
        _operations[53] = oper;

        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("get_kos_by_pathway");
<<<<<<< HEAD
        param = new org.apache.axis.description.ParameterDesc(new QName("", "pathway_id"), org.apache.axis.description.ParameterDesc.IN, new QName("http://www.w3.org/2001/XMLSchema", "string"), String.class, false, false);
        oper.addParameter(param);
        oper.setReturnType(new QName("SOAP/KEGG", "ArrayOfstring"));
        oper.setReturnClass(String[].class);
        oper.setReturnQName(new QName("", "return"));
=======
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "pathway_id"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"), java.lang.String.class, false, false);
        oper.addParameter(param);
        oper.setReturnType(new javax.xml.namespace.QName("SOAP/KEGG", "ArrayOfstring"));
        oper.setReturnClass(java.lang.String[].class);
        oper.setReturnQName(new javax.xml.namespace.QName("", "return"));
>>>>>>> 2eb394907b98577f1b916408cf22a2de6952b22d
        oper.setStyle(org.apache.axis.constants.Style.RPC);
        oper.setUse(org.apache.axis.constants.Use.ENCODED);
        _operations[54] = oper;

        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("get_pathways_by_kos");
<<<<<<< HEAD
        param = new org.apache.axis.description.ParameterDesc(new QName("", "ko_id_list"), org.apache.axis.description.ParameterDesc.IN, new QName("SOAP/KEGG", "ArrayOfstring"), String[].class, false, false);
        oper.addParameter(param);
        param = new org.apache.axis.description.ParameterDesc(new QName("", "org"), org.apache.axis.description.ParameterDesc.IN, new QName("http://www.w3.org/2001/XMLSchema", "string"), String.class, false, false);
        oper.addParameter(param);
        oper.setReturnType(new QName("SOAP/KEGG", "ArrayOfstring"));
        oper.setReturnClass(String[].class);
        oper.setReturnQName(new QName("", "return"));
=======
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "ko_id_list"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("SOAP/KEGG", "ArrayOfstring"), java.lang.String[].class, false, false);
        oper.addParameter(param);
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "org"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"), java.lang.String.class, false, false);
        oper.addParameter(param);
        oper.setReturnType(new javax.xml.namespace.QName("SOAP/KEGG", "ArrayOfstring"));
        oper.setReturnClass(java.lang.String[].class);
        oper.setReturnQName(new javax.xml.namespace.QName("", "return"));
>>>>>>> 2eb394907b98577f1b916408cf22a2de6952b22d
        oper.setStyle(org.apache.axis.constants.Style.RPC);
        oper.setUse(org.apache.axis.constants.Use.ENCODED);
        _operations[55] = oper;

        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("search_compounds_by_name");
<<<<<<< HEAD
        param = new org.apache.axis.description.ParameterDesc(new QName("", "name"), org.apache.axis.description.ParameterDesc.IN, new QName("http://www.w3.org/2001/XMLSchema", "string"), String.class, false, false);
        oper.addParameter(param);
        oper.setReturnType(new QName("SOAP/KEGG", "ArrayOfstring"));
        oper.setReturnClass(String[].class);
        oper.setReturnQName(new QName("", "return"));
=======
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "name"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"), java.lang.String.class, false, false);
        oper.addParameter(param);
        oper.setReturnType(new javax.xml.namespace.QName("SOAP/KEGG", "ArrayOfstring"));
        oper.setReturnClass(java.lang.String[].class);
        oper.setReturnQName(new javax.xml.namespace.QName("", "return"));
>>>>>>> 2eb394907b98577f1b916408cf22a2de6952b22d
        oper.setStyle(org.apache.axis.constants.Style.RPC);
        oper.setUse(org.apache.axis.constants.Use.ENCODED);
        _operations[56] = oper;

        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("search_glycans_by_name");
<<<<<<< HEAD
        param = new org.apache.axis.description.ParameterDesc(new QName("", "name"), org.apache.axis.description.ParameterDesc.IN, new QName("http://www.w3.org/2001/XMLSchema", "string"), String.class, false, false);
        oper.addParameter(param);
        oper.setReturnType(new QName("SOAP/KEGG", "ArrayOfstring"));
        oper.setReturnClass(String[].class);
        oper.setReturnQName(new QName("", "return"));
=======
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "name"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"), java.lang.String.class, false, false);
        oper.addParameter(param);
        oper.setReturnType(new javax.xml.namespace.QName("SOAP/KEGG", "ArrayOfstring"));
        oper.setReturnClass(java.lang.String[].class);
        oper.setReturnQName(new javax.xml.namespace.QName("", "return"));
>>>>>>> 2eb394907b98577f1b916408cf22a2de6952b22d
        oper.setStyle(org.apache.axis.constants.Style.RPC);
        oper.setUse(org.apache.axis.constants.Use.ENCODED);
        _operations[57] = oper;

        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("search_compounds_by_composition");
<<<<<<< HEAD
        param = new org.apache.axis.description.ParameterDesc(new QName("", "composition"), org.apache.axis.description.ParameterDesc.IN, new QName("http://www.w3.org/2001/XMLSchema", "string"), String.class, false, false);
        oper.addParameter(param);
        oper.setReturnType(new QName("SOAP/KEGG", "ArrayOfstring"));
        oper.setReturnClass(String[].class);
        oper.setReturnQName(new QName("", "return"));
=======
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "composition"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"), java.lang.String.class, false, false);
        oper.addParameter(param);
        oper.setReturnType(new javax.xml.namespace.QName("SOAP/KEGG", "ArrayOfstring"));
        oper.setReturnClass(java.lang.String[].class);
        oper.setReturnQName(new javax.xml.namespace.QName("", "return"));
>>>>>>> 2eb394907b98577f1b916408cf22a2de6952b22d
        oper.setStyle(org.apache.axis.constants.Style.RPC);
        oper.setUse(org.apache.axis.constants.Use.ENCODED);
        _operations[58] = oper;

        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("search_compounds_by_mass");
<<<<<<< HEAD
        param = new org.apache.axis.description.ParameterDesc(new QName("", "mass"), org.apache.axis.description.ParameterDesc.IN, new QName("http://www.w3.org/2001/XMLSchema", "float"), float.class, false, false);
        oper.addParameter(param);
        param = new org.apache.axis.description.ParameterDesc(new QName("", "range"), org.apache.axis.description.ParameterDesc.IN, new QName("http://www.w3.org/2001/XMLSchema", "float"), float.class, false, false);
        oper.addParameter(param);
        oper.setReturnType(new QName("SOAP/KEGG", "ArrayOfstring"));
        oper.setReturnClass(String[].class);
        oper.setReturnQName(new QName("", "return"));
=======
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "mass"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "float"), float.class, false, false);
        oper.addParameter(param);
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "range"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "float"), float.class, false, false);
        oper.addParameter(param);
        oper.setReturnType(new javax.xml.namespace.QName("SOAP/KEGG", "ArrayOfstring"));
        oper.setReturnClass(java.lang.String[].class);
        oper.setReturnQName(new javax.xml.namespace.QName("", "return"));
>>>>>>> 2eb394907b98577f1b916408cf22a2de6952b22d
        oper.setStyle(org.apache.axis.constants.Style.RPC);
        oper.setUse(org.apache.axis.constants.Use.ENCODED);
        _operations[59] = oper;

    }

    private static void _initOperationDesc7(){
        org.apache.axis.description.OperationDesc oper;
        org.apache.axis.description.ParameterDesc param;
        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("search_glycans_by_mass");
<<<<<<< HEAD
        param = new org.apache.axis.description.ParameterDesc(new QName("", "mass"), org.apache.axis.description.ParameterDesc.IN, new QName("http://www.w3.org/2001/XMLSchema", "float"), float.class, false, false);
        oper.addParameter(param);
        param = new org.apache.axis.description.ParameterDesc(new QName("", "range"), org.apache.axis.description.ParameterDesc.IN, new QName("http://www.w3.org/2001/XMLSchema", "float"), float.class, false, false);
        oper.addParameter(param);
        oper.setReturnType(new QName("SOAP/KEGG", "ArrayOfstring"));
        oper.setReturnClass(String[].class);
        oper.setReturnQName(new QName("", "return"));
=======
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "mass"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "float"), float.class, false, false);
        oper.addParameter(param);
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "range"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "float"), float.class, false, false);
        oper.addParameter(param);
        oper.setReturnType(new javax.xml.namespace.QName("SOAP/KEGG", "ArrayOfstring"));
        oper.setReturnClass(java.lang.String[].class);
        oper.setReturnQName(new javax.xml.namespace.QName("", "return"));
>>>>>>> 2eb394907b98577f1b916408cf22a2de6952b22d
        oper.setStyle(org.apache.axis.constants.Style.RPC);
        oper.setUse(org.apache.axis.constants.Use.ENCODED);
        _operations[60] = oper;

        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("search_glycans_by_composition");
<<<<<<< HEAD
        param = new org.apache.axis.description.ParameterDesc(new QName("", "composition"), org.apache.axis.description.ParameterDesc.IN, new QName("http://www.w3.org/2001/XMLSchema", "string"), String.class, false, false);
        oper.addParameter(param);
        oper.setReturnType(new QName("SOAP/KEGG", "ArrayOfstring"));
        oper.setReturnClass(String[].class);
        oper.setReturnQName(new QName("", "return"));
=======
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "composition"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"), java.lang.String.class, false, false);
        oper.addParameter(param);
        oper.setReturnType(new javax.xml.namespace.QName("SOAP/KEGG", "ArrayOfstring"));
        oper.setReturnClass(java.lang.String[].class);
        oper.setReturnQName(new javax.xml.namespace.QName("", "return"));
>>>>>>> 2eb394907b98577f1b916408cf22a2de6952b22d
        oper.setStyle(org.apache.axis.constants.Style.RPC);
        oper.setUse(org.apache.axis.constants.Use.ENCODED);
        _operations[61] = oper;

        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("search_compounds_by_subcomp");
<<<<<<< HEAD
        param = new org.apache.axis.description.ParameterDesc(new QName("", "mol"), org.apache.axis.description.ParameterDesc.IN, new QName("http://www.w3.org/2001/XMLSchema", "string"), String.class, false, false);
        oper.addParameter(param);
        param = new org.apache.axis.description.ParameterDesc(new QName("", "offset"), org.apache.axis.description.ParameterDesc.IN, new QName("http://www.w3.org/2001/XMLSchema", "int"), int.class, false, false);
        oper.addParameter(param);
        param = new org.apache.axis.description.ParameterDesc(new QName("", "limit"), org.apache.axis.description.ParameterDesc.IN, new QName("http://www.w3.org/2001/XMLSchema", "int"), int.class, false, false);
        oper.addParameter(param);
        oper.setReturnType(new QName("SOAP/KEGG", "ArrayOfStructureAlignment"));
        oper.setReturnClass(StructureAlignment[].class);
        oper.setReturnQName(new QName("", "return"));
=======
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "mol"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"), java.lang.String.class, false, false);
        oper.addParameter(param);
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "offset"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "int"), int.class, false, false);
        oper.addParameter(param);
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "limit"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "int"), int.class, false, false);
        oper.addParameter(param);
        oper.setReturnType(new javax.xml.namespace.QName("SOAP/KEGG", "ArrayOfStructureAlignment"));
        oper.setReturnClass(StructureAlignment[].class);
        oper.setReturnQName(new javax.xml.namespace.QName("", "return"));
>>>>>>> 2eb394907b98577f1b916408cf22a2de6952b22d
        oper.setStyle(org.apache.axis.constants.Style.RPC);
        oper.setUse(org.apache.axis.constants.Use.ENCODED);
        _operations[62] = oper;

        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("search_glycans_by_kcam");
<<<<<<< HEAD
        param = new org.apache.axis.description.ParameterDesc(new QName("", "kcf"), org.apache.axis.description.ParameterDesc.IN, new QName("http://www.w3.org/2001/XMLSchema", "string"), String.class, false, false);
        oper.addParameter(param);
        param = new org.apache.axis.description.ParameterDesc(new QName("", "program"), org.apache.axis.description.ParameterDesc.IN, new QName("http://www.w3.org/2001/XMLSchema", "string"), String.class, false, false);
        oper.addParameter(param);
        param = new org.apache.axis.description.ParameterDesc(new QName("", "option"), org.apache.axis.description.ParameterDesc.IN, new QName("http://www.w3.org/2001/XMLSchema", "string"), String.class, false, false);
        oper.addParameter(param);
        param = new org.apache.axis.description.ParameterDesc(new QName("", "offset"), org.apache.axis.description.ParameterDesc.IN, new QName("http://www.w3.org/2001/XMLSchema", "int"), int.class, false, false);
        oper.addParameter(param);
        param = new org.apache.axis.description.ParameterDesc(new QName("", "limit"), org.apache.axis.description.ParameterDesc.IN, new QName("http://www.w3.org/2001/XMLSchema", "int"), int.class, false, false);
        oper.addParameter(param);
        oper.setReturnType(new QName("SOAP/KEGG", "ArrayOfStructureAlignment"));
        oper.setReturnClass(StructureAlignment[].class);
        oper.setReturnQName(new QName("", "return"));
=======
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "kcf"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"), java.lang.String.class, false, false);
        oper.addParameter(param);
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "program"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"), java.lang.String.class, false, false);
        oper.addParameter(param);
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "option"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"), java.lang.String.class, false, false);
        oper.addParameter(param);
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "offset"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "int"), int.class, false, false);
        oper.addParameter(param);
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "limit"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "int"), int.class, false, false);
        oper.addParameter(param);
        oper.setReturnType(new javax.xml.namespace.QName("SOAP/KEGG", "ArrayOfStructureAlignment"));
        oper.setReturnClass(StructureAlignment[].class);
        oper.setReturnQName(new javax.xml.namespace.QName("", "return"));
>>>>>>> 2eb394907b98577f1b916408cf22a2de6952b22d
        oper.setStyle(org.apache.axis.constants.Style.RPC);
        oper.setUse(org.apache.axis.constants.Use.ENCODED);
        _operations[63] = oper;

        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("get_linkdb_between_databases");
<<<<<<< HEAD
        param = new org.apache.axis.description.ParameterDesc(new QName("", "from_db"), org.apache.axis.description.ParameterDesc.IN, new QName("http://www.w3.org/2001/XMLSchema", "string"), String.class, false, false);
        oper.addParameter(param);
        param = new org.apache.axis.description.ParameterDesc(new QName("", "to_db"), org.apache.axis.description.ParameterDesc.IN, new QName("http://www.w3.org/2001/XMLSchema", "string"), String.class, false, false);
        oper.addParameter(param);
        param = new org.apache.axis.description.ParameterDesc(new QName("", "offset"), org.apache.axis.description.ParameterDesc.IN, new QName("http://www.w3.org/2001/XMLSchema", "int"), int.class, false, false);
        oper.addParameter(param);
        param = new org.apache.axis.description.ParameterDesc(new QName("", "limit"), org.apache.axis.description.ParameterDesc.IN, new QName("http://www.w3.org/2001/XMLSchema", "int"), int.class, false, false);
        oper.addParameter(param);
        oper.setReturnType(new QName("SOAP/KEGG", "ArrayOfLinkDBRelation"));
        oper.setReturnClass(LinkDBRelation[].class);
        oper.setReturnQName(new QName("", "return"));
=======
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "from_db"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"), java.lang.String.class, false, false);
        oper.addParameter(param);
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "to_db"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"), java.lang.String.class, false, false);
        oper.addParameter(param);
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "offset"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "int"), int.class, false, false);
        oper.addParameter(param);
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "limit"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "int"), int.class, false, false);
        oper.addParameter(param);
        oper.setReturnType(new javax.xml.namespace.QName("SOAP/KEGG", "ArrayOfLinkDBRelation"));
        oper.setReturnClass(LinkDBRelation[].class);
        oper.setReturnQName(new javax.xml.namespace.QName("", "return"));
>>>>>>> 2eb394907b98577f1b916408cf22a2de6952b22d
        oper.setStyle(org.apache.axis.constants.Style.RPC);
        oper.setUse(org.apache.axis.constants.Use.ENCODED);
        _operations[64] = oper;

        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("search_drugs_by_name");
<<<<<<< HEAD
        param = new org.apache.axis.description.ParameterDesc(new QName("", "name"), org.apache.axis.description.ParameterDesc.IN, new QName("http://www.w3.org/2001/XMLSchema", "string"), String.class, false, false);
        oper.addParameter(param);
        oper.setReturnType(new QName("SOAP/KEGG", "ArrayOfstring"));
        oper.setReturnClass(String[].class);
        oper.setReturnQName(new QName("", "return"));
=======
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "name"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"), java.lang.String.class, false, false);
        oper.addParameter(param);
        oper.setReturnType(new javax.xml.namespace.QName("SOAP/KEGG", "ArrayOfstring"));
        oper.setReturnClass(java.lang.String[].class);
        oper.setReturnQName(new javax.xml.namespace.QName("", "return"));
>>>>>>> 2eb394907b98577f1b916408cf22a2de6952b22d
        oper.setStyle(org.apache.axis.constants.Style.RPC);
        oper.setUse(org.apache.axis.constants.Use.ENCODED);
        _operations[65] = oper;

        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("search_drugs_by_composition");
<<<<<<< HEAD
        param = new org.apache.axis.description.ParameterDesc(new QName("", "composition"), org.apache.axis.description.ParameterDesc.IN, new QName("http://www.w3.org/2001/XMLSchema", "string"), String.class, false, false);
        oper.addParameter(param);
        oper.setReturnType(new QName("SOAP/KEGG", "ArrayOfstring"));
        oper.setReturnClass(String[].class);
        oper.setReturnQName(new QName("", "return"));
=======
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "composition"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"), java.lang.String.class, false, false);
        oper.addParameter(param);
        oper.setReturnType(new javax.xml.namespace.QName("SOAP/KEGG", "ArrayOfstring"));
        oper.setReturnClass(java.lang.String[].class);
        oper.setReturnQName(new javax.xml.namespace.QName("", "return"));
>>>>>>> 2eb394907b98577f1b916408cf22a2de6952b22d
        oper.setStyle(org.apache.axis.constants.Style.RPC);
        oper.setUse(org.apache.axis.constants.Use.ENCODED);
        _operations[66] = oper;

        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("search_drugs_by_mass");
<<<<<<< HEAD
        param = new org.apache.axis.description.ParameterDesc(new QName("", "mass"), org.apache.axis.description.ParameterDesc.IN, new QName("http://www.w3.org/2001/XMLSchema", "float"), float.class, false, false);
        oper.addParameter(param);
        param = new org.apache.axis.description.ParameterDesc(new QName("", "range"), org.apache.axis.description.ParameterDesc.IN, new QName("http://www.w3.org/2001/XMLSchema", "float"), float.class, false, false);
        oper.addParameter(param);
        oper.setReturnType(new QName("SOAP/KEGG", "ArrayOfstring"));
        oper.setReturnClass(String[].class);
        oper.setReturnQName(new QName("", "return"));
=======
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "mass"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "float"), float.class, false, false);
        oper.addParameter(param);
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "range"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "float"), float.class, false, false);
        oper.addParameter(param);
        oper.setReturnType(new javax.xml.namespace.QName("SOAP/KEGG", "ArrayOfstring"));
        oper.setReturnClass(java.lang.String[].class);
        oper.setReturnQName(new javax.xml.namespace.QName("", "return"));
>>>>>>> 2eb394907b98577f1b916408cf22a2de6952b22d
        oper.setStyle(org.apache.axis.constants.Style.RPC);
        oper.setUse(org.apache.axis.constants.Use.ENCODED);
        _operations[67] = oper;

        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("search_drugs_by_subcomp");
<<<<<<< HEAD
        param = new org.apache.axis.description.ParameterDesc(new QName("", "mol"), org.apache.axis.description.ParameterDesc.IN, new QName("http://www.w3.org/2001/XMLSchema", "string"), String.class, false, false);
        oper.addParameter(param);
        param = new org.apache.axis.description.ParameterDesc(new QName("", "offset"), org.apache.axis.description.ParameterDesc.IN, new QName("http://www.w3.org/2001/XMLSchema", "int"), int.class, false, false);
        oper.addParameter(param);
        param = new org.apache.axis.description.ParameterDesc(new QName("", "limit"), org.apache.axis.description.ParameterDesc.IN, new QName("http://www.w3.org/2001/XMLSchema", "int"), int.class, false, false);
        oper.addParameter(param);
        oper.setReturnType(new QName("SOAP/KEGG", "ArrayOfStructureAlignment"));
        oper.setReturnClass(StructureAlignment[].class);
        oper.setReturnQName(new QName("", "return"));
=======
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "mol"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"), java.lang.String.class, false, false);
        oper.addParameter(param);
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "offset"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "int"), int.class, false, false);
        oper.addParameter(param);
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "limit"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "int"), int.class, false, false);
        oper.addParameter(param);
        oper.setReturnType(new javax.xml.namespace.QName("SOAP/KEGG", "ArrayOfStructureAlignment"));
        oper.setReturnClass(StructureAlignment[].class);
        oper.setReturnQName(new javax.xml.namespace.QName("", "return"));
>>>>>>> 2eb394907b98577f1b916408cf22a2de6952b22d
        oper.setStyle(org.apache.axis.constants.Style.RPC);
        oper.setUse(org.apache.axis.constants.Use.ENCODED);
        _operations[68] = oper;

        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("get_references_by_pathway");
<<<<<<< HEAD
        param = new org.apache.axis.description.ParameterDesc(new QName("", "pathway_id"), org.apache.axis.description.ParameterDesc.IN, new QName("http://www.w3.org/2001/XMLSchema", "string"), String.class, false, false);
        oper.addParameter(param);
        oper.setReturnType(new QName("SOAP/KEGG", "ArrayOfint"));
        oper.setReturnClass(int[].class);
        oper.setReturnQName(new QName("", "return"));
=======
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "pathway_id"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"), java.lang.String.class, false, false);
        oper.addParameter(param);
        oper.setReturnType(new javax.xml.namespace.QName("SOAP/KEGG", "ArrayOfint"));
        oper.setReturnClass(int[].class);
        oper.setReturnQName(new javax.xml.namespace.QName("", "return"));
>>>>>>> 2eb394907b98577f1b916408cf22a2de6952b22d
        oper.setStyle(org.apache.axis.constants.Style.RPC);
        oper.setUse(org.apache.axis.constants.Use.ENCODED);
        _operations[69] = oper;

    }

    private static void _initOperationDesc8(){
        org.apache.axis.description.OperationDesc oper;
        org.apache.axis.description.ParameterDesc param;
        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("get_drugs_by_pathway");
<<<<<<< HEAD
        param = new org.apache.axis.description.ParameterDesc(new QName("", "pathway_id"), org.apache.axis.description.ParameterDesc.IN, new QName("http://www.w3.org/2001/XMLSchema", "string"), String.class, false, false);
        oper.addParameter(param);
        oper.setReturnType(new QName("SOAP/KEGG", "ArrayOfstring"));
        oper.setReturnClass(String[].class);
        oper.setReturnQName(new QName("", "return"));
=======
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "pathway_id"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"), java.lang.String.class, false, false);
        oper.addParameter(param);
        oper.setReturnType(new javax.xml.namespace.QName("SOAP/KEGG", "ArrayOfstring"));
        oper.setReturnClass(java.lang.String[].class);
        oper.setReturnQName(new javax.xml.namespace.QName("", "return"));
>>>>>>> 2eb394907b98577f1b916408cf22a2de6952b22d
        oper.setStyle(org.apache.axis.constants.Style.RPC);
        oper.setUse(org.apache.axis.constants.Use.ENCODED);
        _operations[70] = oper;

        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("get_pathways_by_drugs");
<<<<<<< HEAD
        param = new org.apache.axis.description.ParameterDesc(new QName("", "drug_id_list"), org.apache.axis.description.ParameterDesc.IN, new QName("SOAP/KEGG", "ArrayOfstring"), String[].class, false, false);
        oper.addParameter(param);
        oper.setReturnType(new QName("SOAP/KEGG", "ArrayOfstring"));
        oper.setReturnClass(String[].class);
        oper.setReturnQName(new QName("", "return"));
=======
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "drug_id_list"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("SOAP/KEGG", "ArrayOfstring"), java.lang.String[].class, false, false);
        oper.addParameter(param);
        oper.setReturnType(new javax.xml.namespace.QName("SOAP/KEGG", "ArrayOfstring"));
        oper.setReturnClass(java.lang.String[].class);
        oper.setReturnQName(new javax.xml.namespace.QName("", "return"));
>>>>>>> 2eb394907b98577f1b916408cf22a2de6952b22d
        oper.setStyle(org.apache.axis.constants.Style.RPC);
        oper.setUse(org.apache.axis.constants.Use.ENCODED);
        _operations[71] = oper;

    }

    public KEGGBindingStub() throws org.apache.axis.AxisFault {
         this(null);
    }

    public KEGGBindingStub(java.net.URL endpointURL, javax.xml.rpc.Service service) throws org.apache.axis.AxisFault {
         this(service);
         super.cachedEndpoint = endpointURL;
    }

    public KEGGBindingStub(javax.xml.rpc.Service service) throws org.apache.axis.AxisFault {
        if (service == null) {
<<<<<<< HEAD
            super.service = new Service();
=======
            super.service = new org.apache.axis.client.Service();
>>>>>>> 2eb394907b98577f1b916408cf22a2de6952b22d
        } else {
            super.service = service;
        }
        ((org.apache.axis.client.Service)super.service).setTypeMappingVersion("1.2");
<<<<<<< HEAD
            Class<?> cls;
            QName qName;
            QName qName2;
            Class beansf = org.apache.axis.encoding.ser.BeanSerializerFactory.class;
            Class beandf = org.apache.axis.encoding.ser.BeanDeserializerFactory.class;
            Class enumsf = org.apache.axis.encoding.ser.EnumSerializerFactory.class;
            Class enumdf = org.apache.axis.encoding.ser.EnumDeserializerFactory.class;
            Class arraysf = ArraySerializerFactory.class;
            Class arraydf = ArrayDeserializerFactory.class;
            Class simplesf = org.apache.axis.encoding.ser.SimpleSerializerFactory.class;
            Class simpledf = org.apache.axis.encoding.ser.SimpleDeserializerFactory.class;
            Class simplelistsf = org.apache.axis.encoding.ser.SimpleListSerializerFactory.class;
            Class simplelistdf = org.apache.axis.encoding.ser.SimpleListDeserializerFactory.class;
            qName = new QName("SOAP/KEGG", "ArrayOfDefinition");
            cachedSerQNames.add(qName);
            cls = Definition[].class;
            cachedSerClasses.add(cls);
            qName = new QName("SOAP/KEGG", "Definition");
            qName2 = null;
            cachedSerFactories.add(new ArraySerializerFactory(qName, qName2));
            cachedDeserFactories.add(new ArrayDeserializerFactory());

            qName = new QName("SOAP/KEGG", "ArrayOfint");
            cachedSerQNames.add(qName);
            cls = int[].class;
            cachedSerClasses.add(cls);
            qName = new QName("http://www.w3.org/2001/XMLSchema", "int");
            qName2 = null;
            cachedSerFactories.add(new ArraySerializerFactory(qName, qName2));
            cachedDeserFactories.add(new ArrayDeserializerFactory());

            qName = new QName("SOAP/KEGG", "ArrayOfLinkDBRelation");
            cachedSerQNames.add(qName);
            cls = LinkDBRelation[].class;
            cachedSerClasses.add(cls);
            qName = new QName("SOAP/KEGG", "LinkDBRelation");
            qName2 = null;
            cachedSerFactories.add(new ArraySerializerFactory(qName, qName2));
            cachedDeserFactories.add(new ArrayDeserializerFactory());

            qName = new QName("SOAP/KEGG", "ArrayOfMotifResult");
            cachedSerQNames.add(qName);
            cls = MotifResult[].class;
            cachedSerClasses.add(cls);
            qName = new QName("SOAP/KEGG", "MotifResult");
            qName2 = null;
            cachedSerFactories.add(new ArraySerializerFactory(qName, qName2));
            cachedDeserFactories.add(new ArrayDeserializerFactory());

            qName = new QName("SOAP/KEGG", "ArrayOfPathwayElement");
            cachedSerQNames.add(qName);
            cls = PathwayElement[].class;
            cachedSerClasses.add(cls);
            qName = new QName("SOAP/KEGG", "PathwayElement");
            qName2 = null;
            cachedSerFactories.add(new ArraySerializerFactory(qName, qName2));
            cachedDeserFactories.add(new ArrayDeserializerFactory());

            qName = new QName("SOAP/KEGG", "ArrayOfPathwayElementRelation");
            cachedSerQNames.add(qName);
            cls = PathwayElementRelation[].class;
            cachedSerClasses.add(cls);
            qName = new QName("SOAP/KEGG", "PathwayElementRelation");
            qName2 = null;
            cachedSerFactories.add(new ArraySerializerFactory(qName, qName2));
            cachedDeserFactories.add(new ArrayDeserializerFactory());

            qName = new QName("SOAP/KEGG", "ArrayOfSSDBRelation");
            cachedSerQNames.add(qName);
            cls = SSDBRelation[].class;
            cachedSerClasses.add(cls);
            qName = new QName("SOAP/KEGG", "SSDBRelation");
            qName2 = null;
            cachedSerFactories.add(new ArraySerializerFactory(qName, qName2));
            cachedDeserFactories.add(new ArrayDeserializerFactory());

            qName = new QName("SOAP/KEGG", "ArrayOfstring");
            cachedSerQNames.add(qName);
            cls = String[].class;
            cachedSerClasses.add(cls);
            qName = new QName("http://www.w3.org/2001/XMLSchema", "string");
            qName2 = null;
            cachedSerFactories.add(new ArraySerializerFactory(qName, qName2));
            cachedDeserFactories.add(new ArrayDeserializerFactory());

            qName = new QName("SOAP/KEGG", "ArrayOfStructureAlignment");
            cachedSerQNames.add(qName);
            cls = StructureAlignment[].class;
            cachedSerClasses.add(cls);
            qName = new QName("SOAP/KEGG", "StructureAlignment");
            qName2 = null;
            cachedSerFactories.add(new ArraySerializerFactory(qName, qName2));
            cachedDeserFactories.add(new ArrayDeserializerFactory());

            qName = new QName("SOAP/KEGG", "ArrayOfSubtype");
            cachedSerQNames.add(qName);
            cls = Subtype[].class;
            cachedSerClasses.add(cls);
            qName = new QName("SOAP/KEGG", "Subtype");
            qName2 = null;
            cachedSerFactories.add(new ArraySerializerFactory(qName, qName2));
            cachedDeserFactories.add(new ArrayDeserializerFactory());

            qName = new QName("SOAP/KEGG", "Definition");
=======
            java.lang.Class cls;
            javax.xml.namespace.QName qName;
            javax.xml.namespace.QName qName2;
            java.lang.Class beansf = org.apache.axis.encoding.ser.BeanSerializerFactory.class;
            java.lang.Class beandf = org.apache.axis.encoding.ser.BeanDeserializerFactory.class;
            java.lang.Class enumsf = org.apache.axis.encoding.ser.EnumSerializerFactory.class;
            java.lang.Class enumdf = org.apache.axis.encoding.ser.EnumDeserializerFactory.class;
            java.lang.Class arraysf = org.apache.axis.encoding.ser.ArraySerializerFactory.class;
            java.lang.Class arraydf = org.apache.axis.encoding.ser.ArrayDeserializerFactory.class;
            java.lang.Class simplesf = org.apache.axis.encoding.ser.SimpleSerializerFactory.class;
            java.lang.Class simpledf = org.apache.axis.encoding.ser.SimpleDeserializerFactory.class;
            java.lang.Class simplelistsf = org.apache.axis.encoding.ser.SimpleListSerializerFactory.class;
            java.lang.Class simplelistdf = org.apache.axis.encoding.ser.SimpleListDeserializerFactory.class;
            qName = new javax.xml.namespace.QName("SOAP/KEGG", "ArrayOfDefinition");
            cachedSerQNames.add(qName);
            cls = Definition[].class;
            cachedSerClasses.add(cls);
            qName = new javax.xml.namespace.QName("SOAP/KEGG", "Definition");
            qName2 = null;
            cachedSerFactories.add(new org.apache.axis.encoding.ser.ArraySerializerFactory(qName, qName2));
            cachedDeserFactories.add(new org.apache.axis.encoding.ser.ArrayDeserializerFactory());

            qName = new javax.xml.namespace.QName("SOAP/KEGG", "ArrayOfint");
            cachedSerQNames.add(qName);
            cls = int[].class;
            cachedSerClasses.add(cls);
            qName = new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "int");
            qName2 = null;
            cachedSerFactories.add(new org.apache.axis.encoding.ser.ArraySerializerFactory(qName, qName2));
            cachedDeserFactories.add(new org.apache.axis.encoding.ser.ArrayDeserializerFactory());

            qName = new javax.xml.namespace.QName("SOAP/KEGG", "ArrayOfLinkDBRelation");
            cachedSerQNames.add(qName);
            cls = LinkDBRelation[].class;
            cachedSerClasses.add(cls);
            qName = new javax.xml.namespace.QName("SOAP/KEGG", "LinkDBRelation");
            qName2 = null;
            cachedSerFactories.add(new org.apache.axis.encoding.ser.ArraySerializerFactory(qName, qName2));
            cachedDeserFactories.add(new org.apache.axis.encoding.ser.ArrayDeserializerFactory());

            qName = new javax.xml.namespace.QName("SOAP/KEGG", "ArrayOfMotifResult");
            cachedSerQNames.add(qName);
            cls = MotifResult[].class;
            cachedSerClasses.add(cls);
            qName = new javax.xml.namespace.QName("SOAP/KEGG", "MotifResult");
            qName2 = null;
            cachedSerFactories.add(new org.apache.axis.encoding.ser.ArraySerializerFactory(qName, qName2));
            cachedDeserFactories.add(new org.apache.axis.encoding.ser.ArrayDeserializerFactory());

            qName = new javax.xml.namespace.QName("SOAP/KEGG", "ArrayOfPathwayElement");
            cachedSerQNames.add(qName);
            cls = PathwayElement[].class;
            cachedSerClasses.add(cls);
            qName = new javax.xml.namespace.QName("SOAP/KEGG", "PathwayElement");
            qName2 = null;
            cachedSerFactories.add(new org.apache.axis.encoding.ser.ArraySerializerFactory(qName, qName2));
            cachedDeserFactories.add(new org.apache.axis.encoding.ser.ArrayDeserializerFactory());

            qName = new javax.xml.namespace.QName("SOAP/KEGG", "ArrayOfPathwayElementRelation");
            cachedSerQNames.add(qName);
            cls = PathwayElementRelation[].class;
            cachedSerClasses.add(cls);
            qName = new javax.xml.namespace.QName("SOAP/KEGG", "PathwayElementRelation");
            qName2 = null;
            cachedSerFactories.add(new org.apache.axis.encoding.ser.ArraySerializerFactory(qName, qName2));
            cachedDeserFactories.add(new org.apache.axis.encoding.ser.ArrayDeserializerFactory());

            qName = new javax.xml.namespace.QName("SOAP/KEGG", "ArrayOfSSDBRelation");
            cachedSerQNames.add(qName);
            cls = SSDBRelation[].class;
            cachedSerClasses.add(cls);
            qName = new javax.xml.namespace.QName("SOAP/KEGG", "SSDBRelation");
            qName2 = null;
            cachedSerFactories.add(new org.apache.axis.encoding.ser.ArraySerializerFactory(qName, qName2));
            cachedDeserFactories.add(new org.apache.axis.encoding.ser.ArrayDeserializerFactory());

            qName = new javax.xml.namespace.QName("SOAP/KEGG", "ArrayOfstring");
            cachedSerQNames.add(qName);
            cls = java.lang.String[].class;
            cachedSerClasses.add(cls);
            qName = new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string");
            qName2 = null;
            cachedSerFactories.add(new org.apache.axis.encoding.ser.ArraySerializerFactory(qName, qName2));
            cachedDeserFactories.add(new org.apache.axis.encoding.ser.ArrayDeserializerFactory());

            qName = new javax.xml.namespace.QName("SOAP/KEGG", "ArrayOfStructureAlignment");
            cachedSerQNames.add(qName);
            cls = StructureAlignment[].class;
            cachedSerClasses.add(cls);
            qName = new javax.xml.namespace.QName("SOAP/KEGG", "StructureAlignment");
            qName2 = null;
            cachedSerFactories.add(new org.apache.axis.encoding.ser.ArraySerializerFactory(qName, qName2));
            cachedDeserFactories.add(new org.apache.axis.encoding.ser.ArrayDeserializerFactory());

            qName = new javax.xml.namespace.QName("SOAP/KEGG", "ArrayOfSubtype");
            cachedSerQNames.add(qName);
            cls = Subtype[].class;
            cachedSerClasses.add(cls);
            qName = new javax.xml.namespace.QName("SOAP/KEGG", "Subtype");
            qName2 = null;
            cachedSerFactories.add(new org.apache.axis.encoding.ser.ArraySerializerFactory(qName, qName2));
            cachedDeserFactories.add(new org.apache.axis.encoding.ser.ArrayDeserializerFactory());

            qName = new javax.xml.namespace.QName("SOAP/KEGG", "Definition");
>>>>>>> 2eb394907b98577f1b916408cf22a2de6952b22d
            cachedSerQNames.add(qName);
            cls = Definition.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

<<<<<<< HEAD
            qName = new QName("SOAP/KEGG", "LinkDBRelation");
=======
            qName = new javax.xml.namespace.QName("SOAP/KEGG", "LinkDBRelation");
>>>>>>> 2eb394907b98577f1b916408cf22a2de6952b22d
            cachedSerQNames.add(qName);
            cls = LinkDBRelation.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

<<<<<<< HEAD
            qName = new QName("SOAP/KEGG", "MotifResult");
=======
            qName = new javax.xml.namespace.QName("SOAP/KEGG", "MotifResult");
>>>>>>> 2eb394907b98577f1b916408cf22a2de6952b22d
            cachedSerQNames.add(qName);
            cls = MotifResult.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

<<<<<<< HEAD
            qName = new QName("SOAP/KEGG", "PathwayElement");
=======
            qName = new javax.xml.namespace.QName("SOAP/KEGG", "PathwayElement");
>>>>>>> 2eb394907b98577f1b916408cf22a2de6952b22d
            cachedSerQNames.add(qName);
            cls = PathwayElement.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

<<<<<<< HEAD
            qName = new QName("SOAP/KEGG", "PathwayElementRelation");
=======
            qName = new javax.xml.namespace.QName("SOAP/KEGG", "PathwayElementRelation");
>>>>>>> 2eb394907b98577f1b916408cf22a2de6952b22d
            cachedSerQNames.add(qName);
            cls = PathwayElementRelation.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

<<<<<<< HEAD
            qName = new QName("SOAP/KEGG", "SSDBRelation");
=======
            qName = new javax.xml.namespace.QName("SOAP/KEGG", "SSDBRelation");
>>>>>>> 2eb394907b98577f1b916408cf22a2de6952b22d
            cachedSerQNames.add(qName);
            cls = SSDBRelation.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

<<<<<<< HEAD
            qName = new QName("SOAP/KEGG", "StructureAlignment");
=======
            qName = new javax.xml.namespace.QName("SOAP/KEGG", "StructureAlignment");
>>>>>>> 2eb394907b98577f1b916408cf22a2de6952b22d
            cachedSerQNames.add(qName);
            cls = StructureAlignment.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

<<<<<<< HEAD
            qName = new QName("SOAP/KEGG", "Subtype");
=======
            qName = new javax.xml.namespace.QName("SOAP/KEGG", "Subtype");
>>>>>>> 2eb394907b98577f1b916408cf22a2de6952b22d
            cachedSerQNames.add(qName);
            cls = Subtype.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

    }

    protected org.apache.axis.client.Call createCall() throws java.rmi.RemoteException {
        try {
            org.apache.axis.client.Call _call = super._createCall();
            if (super.maintainSessionSet) {
                _call.setMaintainSession(super.maintainSession);
            }
            if (super.cachedUsername != null) {
                _call.setUsername(super.cachedUsername);
            }
            if (super.cachedPassword != null) {
                _call.setPassword(super.cachedPassword);
            }
            if (super.cachedEndpoint != null) {
                _call.setTargetEndpointAddress(super.cachedEndpoint);
            }
            if (super.cachedTimeout != null) {
                _call.setTimeout(super.cachedTimeout);
            }
            if (super.cachedPortName != null) {
                _call.setPortName(super.cachedPortName);
            }
<<<<<<< HEAD
            Enumeration<Object> keys = super.cachedProperties.keys();
            while (keys.hasMoreElements()) {
                String key = (String) keys.nextElement();
=======
            java.util.Enumeration keys = super.cachedProperties.keys();
            while (keys.hasMoreElements()) {
                java.lang.String key = (java.lang.String) keys.nextElement();
>>>>>>> 2eb394907b98577f1b916408cf22a2de6952b22d
                _call.setProperty(key, super.cachedProperties.get(key));
            }
            // All the type mapping information is registered
            // when the first call is made.
            // The type mapping information is actually registered in
            // the TypeMappingRegistry of the service, which
            // is the reason why registration is only needed for the first call.
            synchronized (this) {
                if (firstCall()) {
                    // must set encoding style before registering serializers
                    _call.setSOAPVersion(SOAPConstants.SOAP11_CONSTANTS);
                    _call.setEncodingStyle(org.apache.axis.Constants.URI_SOAP11_ENC);
                    for (int i = 0; i < cachedSerFactories.size(); ++i) {
<<<<<<< HEAD
                        Class cls = (Class) cachedSerClasses.get(i);
                        QName qName =
                                (QName) cachedSerQNames.get(i);
                        java.lang.Object x = cachedSerFactories.get(i);
                        if (x instanceof Class) {
                            Class sf = (Class)
                                 cachedSerFactories.get(i);
                            Class df = (Class)
=======
                        java.lang.Class cls = (java.lang.Class) cachedSerClasses.get(i);
                        javax.xml.namespace.QName qName =
                                (javax.xml.namespace.QName) cachedSerQNames.get(i);
                        java.lang.Object x = cachedSerFactories.get(i);
                        if (x instanceof Class) {
                            java.lang.Class sf = (java.lang.Class)
                                 cachedSerFactories.get(i);
                            java.lang.Class df = (java.lang.Class)
>>>>>>> 2eb394907b98577f1b916408cf22a2de6952b22d
                                 cachedDeserFactories.get(i);
                            _call.registerTypeMapping(cls, qName, sf, df, false);
                        }
                        else if (x instanceof javax.xml.rpc.encoding.SerializerFactory) {
                            org.apache.axis.encoding.SerializerFactory sf = (org.apache.axis.encoding.SerializerFactory)
                                 cachedSerFactories.get(i);
                            org.apache.axis.encoding.DeserializerFactory df = (org.apache.axis.encoding.DeserializerFactory)
                                 cachedDeserFactories.get(i);
                            _call.registerTypeMapping(cls, qName, sf, df, false);
                        }
                    }
                }
            }
            return _call;
        }
        catch (java.lang.Throwable _t) {
            throw new org.apache.axis.AxisFault("Failure trying to get the Call object", _t);
        }
    }

    public Definition[] list_databases() throws java.rmi.RemoteException {
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[0]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("SOAP/KEGG#list_databases");
        _call.setSOAPVersion(SOAPConstants.SOAP11_CONSTANTS);
<<<<<<< HEAD
        _call.setOperationName(new QName("SOAP/KEGG", "list_databases"));
=======
        _call.setOperationName(new javax.xml.namespace.QName("SOAP/KEGG", "list_databases"));
>>>>>>> 2eb394907b98577f1b916408cf22a2de6952b22d

        setRequestHeaders(_call);
        setAttachments(_call);
 try {        java.lang.Object _resp = _call.invoke(new java.lang.Object[] {});

        if (_resp instanceof java.rmi.RemoteException) {
            throw (java.rmi.RemoteException)_resp;
        }
        else {
            extractAttachments(_call);
            try {
                return (Definition[]) _resp;
            } catch (java.lang.Exception _exception) {
                return (Definition[]) JavaUtils.convert(_resp, Definition[].class);
            }
        }
  } catch (org.apache.axis.AxisFault axisFaultException) {
  throw axisFaultException;
}
    }

    public Definition[] list_organisms() throws java.rmi.RemoteException {
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[1]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("SOAP/KEGG#list_organisms");
        _call.setSOAPVersion(SOAPConstants.SOAP11_CONSTANTS);
<<<<<<< HEAD
        _call.setOperationName(new QName("SOAP/KEGG", "list_organisms"));
=======
        _call.setOperationName(new javax.xml.namespace.QName("SOAP/KEGG", "list_organisms"));
>>>>>>> 2eb394907b98577f1b916408cf22a2de6952b22d

        setRequestHeaders(_call);
        setAttachments(_call);
 try {        java.lang.Object _resp = _call.invoke(new java.lang.Object[] {});

        if (_resp instanceof java.rmi.RemoteException) {
            throw (java.rmi.RemoteException)_resp;
        }
        else {
            extractAttachments(_call);
            try {
                return (Definition[]) _resp;
            } catch (java.lang.Exception _exception) {
                return (Definition[]) JavaUtils.convert(_resp, Definition[].class);
            }
        }
  } catch (org.apache.axis.AxisFault axisFaultException) {
  throw axisFaultException;
}
    }

<<<<<<< HEAD
    public Definition[] list_pathways(String org) throws java.rmi.RemoteException {
=======
    public Definition[] list_pathways(java.lang.String org) throws java.rmi.RemoteException {
>>>>>>> 2eb394907b98577f1b916408cf22a2de6952b22d
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[2]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("SOAP/KEGG#list_pathways");
        _call.setSOAPVersion(SOAPConstants.SOAP11_CONSTANTS);
<<<<<<< HEAD
        _call.setOperationName(new QName("SOAP/KEGG", "list_pathways"));
=======
        _call.setOperationName(new javax.xml.namespace.QName("SOAP/KEGG", "list_pathways"));
>>>>>>> 2eb394907b98577f1b916408cf22a2de6952b22d

        setRequestHeaders(_call);
        setAttachments(_call);
 try {        java.lang.Object _resp = _call.invoke(new java.lang.Object[] {org});

        if (_resp instanceof java.rmi.RemoteException) {
            throw (java.rmi.RemoteException)_resp;
        }
        else {
            extractAttachments(_call);
            try {
                return (Definition[]) _resp;
            } catch (java.lang.Exception _exception) {
                return (Definition[]) JavaUtils.convert(_resp, Definition[].class);
            }
        }
  } catch (org.apache.axis.AxisFault axisFaultException) {
  throw axisFaultException;
}
    }

<<<<<<< HEAD
    public Definition[] list_ko_classes(String class_id) throws java.rmi.RemoteException {
=======
    public Definition[] list_ko_classes(java.lang.String class_id) throws java.rmi.RemoteException {
>>>>>>> 2eb394907b98577f1b916408cf22a2de6952b22d
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[3]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("SOAP/KEGG#list_ko_classes");
        _call.setSOAPVersion(SOAPConstants.SOAP11_CONSTANTS);
<<<<<<< HEAD
        _call.setOperationName(new QName("SOAP/KEGG", "list_ko_classes"));
=======
        _call.setOperationName(new javax.xml.namespace.QName("SOAP/KEGG", "list_ko_classes"));
>>>>>>> 2eb394907b98577f1b916408cf22a2de6952b22d

        setRequestHeaders(_call);
        setAttachments(_call);
 try {        java.lang.Object _resp = _call.invoke(new java.lang.Object[] {class_id});

        if (_resp instanceof java.rmi.RemoteException) {
            throw (java.rmi.RemoteException)_resp;
        }
        else {
            extractAttachments(_call);
            try {
                return (Definition[]) _resp;
            } catch (java.lang.Exception _exception) {
                return (Definition[]) JavaUtils.convert(_resp, Definition[].class);
            }
        }
  } catch (org.apache.axis.AxisFault axisFaultException) {
  throw axisFaultException;
}
    }

<<<<<<< HEAD
    public String binfo(String db) throws java.rmi.RemoteException {
=======
    public java.lang.String binfo(java.lang.String db) throws java.rmi.RemoteException {
>>>>>>> 2eb394907b98577f1b916408cf22a2de6952b22d
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[4]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("SOAP/KEGG#binfo");
        _call.setSOAPVersion(SOAPConstants.SOAP11_CONSTANTS);
<<<<<<< HEAD
        _call.setOperationName(new QName("SOAP/KEGG", "binfo"));
=======
        _call.setOperationName(new javax.xml.namespace.QName("SOAP/KEGG", "binfo"));
>>>>>>> 2eb394907b98577f1b916408cf22a2de6952b22d

        setRequestHeaders(_call);
        setAttachments(_call);
 try {        java.lang.Object _resp = _call.invoke(new java.lang.Object[] {db});

        if (_resp instanceof java.rmi.RemoteException) {
            throw (java.rmi.RemoteException)_resp;
        }
        else {
            extractAttachments(_call);
            try {
<<<<<<< HEAD
                return (String) _resp;
            } catch (java.lang.Exception _exception) {
                return (String) JavaUtils.convert(_resp, String.class);
=======
                return (java.lang.String) _resp;
            } catch (java.lang.Exception _exception) {
                return (java.lang.String) JavaUtils.convert(_resp, java.lang.String.class);
>>>>>>> 2eb394907b98577f1b916408cf22a2de6952b22d
            }
        }
  } catch (org.apache.axis.AxisFault axisFaultException) {
  throw axisFaultException;
}
    }

<<<<<<< HEAD
    public String bget(String string) throws java.rmi.RemoteException {
=======
    public java.lang.String bget(java.lang.String string) throws java.rmi.RemoteException {
>>>>>>> 2eb394907b98577f1b916408cf22a2de6952b22d
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[5]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("SOAP/KEGG#bget");
        _call.setSOAPVersion(SOAPConstants.SOAP11_CONSTANTS);
<<<<<<< HEAD
        _call.setOperationName(new QName("SOAP/KEGG", "bget"));
=======
        _call.setOperationName(new javax.xml.namespace.QName("SOAP/KEGG", "bget"));
>>>>>>> 2eb394907b98577f1b916408cf22a2de6952b22d

        setRequestHeaders(_call);
        setAttachments(_call);
 try {        java.lang.Object _resp = _call.invoke(new java.lang.Object[] {string});

        if (_resp instanceof java.rmi.RemoteException) {
            throw (java.rmi.RemoteException)_resp;
        }
        else {
            extractAttachments(_call);
            try {
<<<<<<< HEAD
                return (String) _resp;
            } catch (java.lang.Exception _exception) {
                return (String) JavaUtils.convert(_resp, String.class);
=======
                return (java.lang.String) _resp;
            } catch (java.lang.Exception _exception) {
                return (java.lang.String) JavaUtils.convert(_resp, java.lang.String.class);
>>>>>>> 2eb394907b98577f1b916408cf22a2de6952b22d
            }
        }
  } catch (org.apache.axis.AxisFault axisFaultException) {
  throw axisFaultException;
}
    }

<<<<<<< HEAD
    public String bfind(String string) throws java.rmi.RemoteException {
=======
    public java.lang.String bfind(java.lang.String string) throws java.rmi.RemoteException {
>>>>>>> 2eb394907b98577f1b916408cf22a2de6952b22d
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[6]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("SOAP/KEGG#bfind");
        _call.setSOAPVersion(SOAPConstants.SOAP11_CONSTANTS);
<<<<<<< HEAD
        _call.setOperationName(new QName("SOAP/KEGG", "bfind"));
=======
        _call.setOperationName(new javax.xml.namespace.QName("SOAP/KEGG", "bfind"));
>>>>>>> 2eb394907b98577f1b916408cf22a2de6952b22d

        setRequestHeaders(_call);
        setAttachments(_call);
 try {        java.lang.Object _resp = _call.invoke(new java.lang.Object[] {string});

        if (_resp instanceof java.rmi.RemoteException) {
            throw (java.rmi.RemoteException)_resp;
        }
        else {
            extractAttachments(_call);
            try {
<<<<<<< HEAD
                return (String) _resp;
            } catch (java.lang.Exception _exception) {
                return (String) JavaUtils.convert(_resp, String.class);
=======
                return (java.lang.String) _resp;
            } catch (java.lang.Exception _exception) {
                return (java.lang.String) JavaUtils.convert(_resp, java.lang.String.class);
>>>>>>> 2eb394907b98577f1b916408cf22a2de6952b22d
            }
        }
  } catch (org.apache.axis.AxisFault axisFaultException) {
  throw axisFaultException;
}
    }

<<<<<<< HEAD
    public String btit(String string) throws java.rmi.RemoteException {
=======
    public java.lang.String btit(java.lang.String string) throws java.rmi.RemoteException {
>>>>>>> 2eb394907b98577f1b916408cf22a2de6952b22d
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[7]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("SOAP/KEGG#btit");
        _call.setSOAPVersion(SOAPConstants.SOAP11_CONSTANTS);
<<<<<<< HEAD
        _call.setOperationName(new QName("SOAP/KEGG", "btit"));
=======
        _call.setOperationName(new javax.xml.namespace.QName("SOAP/KEGG", "btit"));
>>>>>>> 2eb394907b98577f1b916408cf22a2de6952b22d

        setRequestHeaders(_call);
        setAttachments(_call);
 try {        java.lang.Object _resp = _call.invoke(new java.lang.Object[] {string});

        if (_resp instanceof java.rmi.RemoteException) {
            throw (java.rmi.RemoteException)_resp;
        }
        else {
            extractAttachments(_call);
            try {
<<<<<<< HEAD
                return (String) _resp;
            } catch (java.lang.Exception _exception) {
                return (String) JavaUtils.convert(_resp, String.class);
=======
                return (java.lang.String) _resp;
            } catch (java.lang.Exception _exception) {
                return (java.lang.String) JavaUtils.convert(_resp, java.lang.String.class);
>>>>>>> 2eb394907b98577f1b916408cf22a2de6952b22d
            }
        }
  } catch (org.apache.axis.AxisFault axisFaultException) {
  throw axisFaultException;
}
    }

<<<<<<< HEAD
    public String bconv(String string) throws java.rmi.RemoteException {
=======
    public java.lang.String bconv(java.lang.String string) throws java.rmi.RemoteException {
>>>>>>> 2eb394907b98577f1b916408cf22a2de6952b22d
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[8]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("SOAP/KEGG#bconv");
        _call.setSOAPVersion(SOAPConstants.SOAP11_CONSTANTS);
<<<<<<< HEAD
        _call.setOperationName(new QName("SOAP/KEGG", "bconv"));
=======
        _call.setOperationName(new javax.xml.namespace.QName("SOAP/KEGG", "bconv"));
>>>>>>> 2eb394907b98577f1b916408cf22a2de6952b22d

        setRequestHeaders(_call);
        setAttachments(_call);
 try {        java.lang.Object _resp = _call.invoke(new java.lang.Object[] {string});

        if (_resp instanceof java.rmi.RemoteException) {
            throw (java.rmi.RemoteException)_resp;
        }
        else {
            extractAttachments(_call);
            try {
<<<<<<< HEAD
                return (String) _resp;
            } catch (java.lang.Exception _exception) {
                return (String) JavaUtils.convert(_resp, String.class);
=======
                return (java.lang.String) _resp;
            } catch (java.lang.Exception _exception) {
                return (java.lang.String) JavaUtils.convert(_resp, java.lang.String.class);
>>>>>>> 2eb394907b98577f1b916408cf22a2de6952b22d
            }
        }
  } catch (org.apache.axis.AxisFault axisFaultException) {
  throw axisFaultException;
}
    }

<<<<<<< HEAD
    public LinkDBRelation[] get_linkdb_by_entry(String entry_id, String db, int offset, int limit) throws java.rmi.RemoteException {
=======
    public LinkDBRelation[] get_linkdb_by_entry(java.lang.String entry_id, java.lang.String db, int offset, int limit) throws java.rmi.RemoteException {
>>>>>>> 2eb394907b98577f1b916408cf22a2de6952b22d
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[9]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("SOAP/KEGG#get_linkdb_by_entry");
        _call.setSOAPVersion(SOAPConstants.SOAP11_CONSTANTS);
<<<<<<< HEAD
        _call.setOperationName(new QName("SOAP/KEGG", "get_linkdb_by_entry"));
=======
        _call.setOperationName(new javax.xml.namespace.QName("SOAP/KEGG", "get_linkdb_by_entry"));
>>>>>>> 2eb394907b98577f1b916408cf22a2de6952b22d

        setRequestHeaders(_call);
        setAttachments(_call);
 try {        java.lang.Object _resp = _call.invoke(new java.lang.Object[] {entry_id, db, new java.lang.Integer(offset), new java.lang.Integer(limit)});

        if (_resp instanceof java.rmi.RemoteException) {
            throw (java.rmi.RemoteException)_resp;
        }
        else {
            extractAttachments(_call);
            try {
                return (LinkDBRelation[]) _resp;
            } catch (java.lang.Exception _exception) {
                return (LinkDBRelation[]) JavaUtils.convert(_resp, LinkDBRelation[].class);
            }
        }
  } catch (org.apache.axis.AxisFault axisFaultException) {
  throw axisFaultException;
}
    }

<<<<<<< HEAD
    public SSDBRelation[] get_best_neighbors_by_gene(String genes_id, int offset, int limit) throws java.rmi.RemoteException {
=======
    public SSDBRelation[] get_best_neighbors_by_gene(java.lang.String genes_id, int offset, int limit) throws java.rmi.RemoteException {
>>>>>>> 2eb394907b98577f1b916408cf22a2de6952b22d
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[10]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("SOAP/KEGG#get_best_neighbors_by_gene");
        _call.setSOAPVersion(SOAPConstants.SOAP11_CONSTANTS);
<<<<<<< HEAD
        _call.setOperationName(new QName("SOAP/KEGG", "get_best_neighbors_by_gene"));
=======
        _call.setOperationName(new javax.xml.namespace.QName("SOAP/KEGG", "get_best_neighbors_by_gene"));
>>>>>>> 2eb394907b98577f1b916408cf22a2de6952b22d

        setRequestHeaders(_call);
        setAttachments(_call);
 try {        java.lang.Object _resp = _call.invoke(new java.lang.Object[] {genes_id, new java.lang.Integer(offset), new java.lang.Integer(limit)});

        if (_resp instanceof java.rmi.RemoteException) {
            throw (java.rmi.RemoteException)_resp;
        }
        else {
            extractAttachments(_call);
            try {
                return (SSDBRelation[]) _resp;
            } catch (java.lang.Exception _exception) {
                return (SSDBRelation[]) JavaUtils.convert(_resp, SSDBRelation[].class);
            }
        }
  } catch (org.apache.axis.AxisFault axisFaultException) {
  throw axisFaultException;
}
    }

<<<<<<< HEAD
    public SSDBRelation[] get_best_best_neighbors_by_gene(String genes_id, int offset, int limit) throws java.rmi.RemoteException {
=======
    public SSDBRelation[] get_best_best_neighbors_by_gene(java.lang.String genes_id, int offset, int limit) throws java.rmi.RemoteException {
>>>>>>> 2eb394907b98577f1b916408cf22a2de6952b22d
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[11]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("SOAP/KEGG#get_best_best_neighbors_by_gene");
        _call.setSOAPVersion(SOAPConstants.SOAP11_CONSTANTS);
<<<<<<< HEAD
        _call.setOperationName(new QName("SOAP/KEGG", "get_best_best_neighbors_by_gene"));
=======
        _call.setOperationName(new javax.xml.namespace.QName("SOAP/KEGG", "get_best_best_neighbors_by_gene"));
>>>>>>> 2eb394907b98577f1b916408cf22a2de6952b22d

        setRequestHeaders(_call);
        setAttachments(_call);
 try {        java.lang.Object _resp = _call.invoke(new java.lang.Object[] {genes_id, new java.lang.Integer(offset), new java.lang.Integer(limit)});

        if (_resp instanceof java.rmi.RemoteException) {
            throw (java.rmi.RemoteException)_resp;
        }
        else {
            extractAttachments(_call);
            try {
                return (SSDBRelation[]) _resp;
            } catch (java.lang.Exception _exception) {
                return (SSDBRelation[]) JavaUtils.convert(_resp, SSDBRelation[].class);
            }
        }
  } catch (org.apache.axis.AxisFault axisFaultException) {
  throw axisFaultException;
}
    }

<<<<<<< HEAD
    public SSDBRelation[] get_reverse_best_neighbors_by_gene(String genes_id, int offset, int limit) throws java.rmi.RemoteException {
=======
    public SSDBRelation[] get_reverse_best_neighbors_by_gene(java.lang.String genes_id, int offset, int limit) throws java.rmi.RemoteException {
>>>>>>> 2eb394907b98577f1b916408cf22a2de6952b22d
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[12]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("SOAP/KEGG#get_reverse_best_neighbors_by_gene");
        _call.setSOAPVersion(SOAPConstants.SOAP11_CONSTANTS);
<<<<<<< HEAD
        _call.setOperationName(new QName("SOAP/KEGG", "get_reverse_best_neighbors_by_gene"));
=======
        _call.setOperationName(new javax.xml.namespace.QName("SOAP/KEGG", "get_reverse_best_neighbors_by_gene"));
>>>>>>> 2eb394907b98577f1b916408cf22a2de6952b22d

        setRequestHeaders(_call);
        setAttachments(_call);
 try {        java.lang.Object _resp = _call.invoke(new java.lang.Object[] {genes_id, new java.lang.Integer(offset), new java.lang.Integer(limit)});

        if (_resp instanceof java.rmi.RemoteException) {
            throw (java.rmi.RemoteException)_resp;
        }
        else {
            extractAttachments(_call);
            try {
                return (SSDBRelation[]) _resp;
            } catch (java.lang.Exception _exception) {
                return (SSDBRelation[]) JavaUtils.convert(_resp, SSDBRelation[].class);
            }
        }
  } catch (org.apache.axis.AxisFault axisFaultException) {
  throw axisFaultException;
}
    }

<<<<<<< HEAD
    public SSDBRelation[] get_paralogs_by_gene(String genes_id, int offset, int limit) throws java.rmi.RemoteException {
=======
    public SSDBRelation[] get_paralogs_by_gene(java.lang.String genes_id, int offset, int limit) throws java.rmi.RemoteException {
>>>>>>> 2eb394907b98577f1b916408cf22a2de6952b22d
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[13]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("SOAP/KEGG#get_paralogs_by_gene");
        _call.setSOAPVersion(SOAPConstants.SOAP11_CONSTANTS);
<<<<<<< HEAD
        _call.setOperationName(new QName("SOAP/KEGG", "get_paralogs_by_gene"));
=======
        _call.setOperationName(new javax.xml.namespace.QName("SOAP/KEGG", "get_paralogs_by_gene"));
>>>>>>> 2eb394907b98577f1b916408cf22a2de6952b22d

        setRequestHeaders(_call);
        setAttachments(_call);
 try {        java.lang.Object _resp = _call.invoke(new java.lang.Object[] {genes_id, new java.lang.Integer(offset), new java.lang.Integer(limit)});

        if (_resp instanceof java.rmi.RemoteException) {
            throw (java.rmi.RemoteException)_resp;
        }
        else {
            extractAttachments(_call);
            try {
                return (SSDBRelation[]) _resp;
            } catch (java.lang.Exception _exception) {
                return (SSDBRelation[]) JavaUtils.convert(_resp, SSDBRelation[].class);
            }
        }
  } catch (org.apache.axis.AxisFault axisFaultException) {
  throw axisFaultException;
}
    }

<<<<<<< HEAD
    public MotifResult[] get_motifs_by_gene(String genes_id, String db) throws java.rmi.RemoteException {
=======
    public MotifResult[] get_motifs_by_gene(java.lang.String genes_id, java.lang.String db) throws java.rmi.RemoteException {
>>>>>>> 2eb394907b98577f1b916408cf22a2de6952b22d
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[14]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("SOAP/KEGG#get_motifs_by_gene");
        _call.setSOAPVersion(SOAPConstants.SOAP11_CONSTANTS);
<<<<<<< HEAD
        _call.setOperationName(new QName("SOAP/KEGG", "get_motifs_by_gene"));
=======
        _call.setOperationName(new javax.xml.namespace.QName("SOAP/KEGG", "get_motifs_by_gene"));
>>>>>>> 2eb394907b98577f1b916408cf22a2de6952b22d

        setRequestHeaders(_call);
        setAttachments(_call);
 try {        java.lang.Object _resp = _call.invoke(new java.lang.Object[] {genes_id, db});

        if (_resp instanceof java.rmi.RemoteException) {
            throw (java.rmi.RemoteException)_resp;
        }
        else {
            extractAttachments(_call);
            try {
                return (MotifResult[]) _resp;
            } catch (java.lang.Exception _exception) {
                return (MotifResult[]) JavaUtils.convert(_resp, MotifResult[].class);
            }
        }
  } catch (org.apache.axis.AxisFault axisFaultException) {
  throw axisFaultException;
}
    }

<<<<<<< HEAD
    public Definition[] get_genes_by_motifs(String[] motif_id_list, int offset, int limit) throws java.rmi.RemoteException {
=======
    public Definition[] get_genes_by_motifs(java.lang.String[] motif_id_list, int offset, int limit) throws java.rmi.RemoteException {
>>>>>>> 2eb394907b98577f1b916408cf22a2de6952b22d
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[15]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("SOAP/KEGG#get_genes_by_motifs");
        _call.setSOAPVersion(SOAPConstants.SOAP11_CONSTANTS);
<<<<<<< HEAD
        _call.setOperationName(new QName("SOAP/KEGG", "get_genes_by_motifs"));
=======
        _call.setOperationName(new javax.xml.namespace.QName("SOAP/KEGG", "get_genes_by_motifs"));
>>>>>>> 2eb394907b98577f1b916408cf22a2de6952b22d

        setRequestHeaders(_call);
        setAttachments(_call);
 try {        java.lang.Object _resp = _call.invoke(new java.lang.Object[] {motif_id_list, new java.lang.Integer(offset), new java.lang.Integer(limit)});

        if (_resp instanceof java.rmi.RemoteException) {
            throw (java.rmi.RemoteException)_resp;
        }
        else {
            extractAttachments(_call);
            try {
                return (Definition[]) _resp;
            } catch (java.lang.Exception _exception) {
                return (Definition[]) JavaUtils.convert(_resp, Definition[].class);
            }
        }
  } catch (org.apache.axis.AxisFault axisFaultException) {
  throw axisFaultException;
}
    }

<<<<<<< HEAD
    public String[] get_ko_by_gene(String genes_id) throws java.rmi.RemoteException {
=======
    public java.lang.String[] get_ko_by_gene(java.lang.String genes_id) throws java.rmi.RemoteException {
>>>>>>> 2eb394907b98577f1b916408cf22a2de6952b22d
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[16]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("SOAP/KEGG#get_ko_by_gene");
        _call.setSOAPVersion(SOAPConstants.SOAP11_CONSTANTS);
<<<<<<< HEAD
        _call.setOperationName(new QName("SOAP/KEGG", "get_ko_by_gene"));
=======
        _call.setOperationName(new javax.xml.namespace.QName("SOAP/KEGG", "get_ko_by_gene"));
>>>>>>> 2eb394907b98577f1b916408cf22a2de6952b22d

        setRequestHeaders(_call);
        setAttachments(_call);
 try {        java.lang.Object _resp = _call.invoke(new java.lang.Object[] {genes_id});

        if (_resp instanceof java.rmi.RemoteException) {
            throw (java.rmi.RemoteException)_resp;
        }
        else {
            extractAttachments(_call);
            try {
<<<<<<< HEAD
                return (String[]) _resp;
            } catch (java.lang.Exception _exception) {
                return (String[]) JavaUtils.convert(_resp, String[].class);
=======
                return (java.lang.String[]) _resp;
            } catch (java.lang.Exception _exception) {
                return (java.lang.String[]) JavaUtils.convert(_resp, java.lang.String[].class);
>>>>>>> 2eb394907b98577f1b916408cf22a2de6952b22d
            }
        }
  } catch (org.apache.axis.AxisFault axisFaultException) {
  throw axisFaultException;
}
    }

<<<<<<< HEAD
    public Definition[] get_ko_by_ko_class(String class_id) throws java.rmi.RemoteException {
=======
    public Definition[] get_ko_by_ko_class(java.lang.String class_id) throws java.rmi.RemoteException {
>>>>>>> 2eb394907b98577f1b916408cf22a2de6952b22d
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[17]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("SOAP/KEGG#get_ko_by_ko_class");
        _call.setSOAPVersion(SOAPConstants.SOAP11_CONSTANTS);
<<<<<<< HEAD
        _call.setOperationName(new QName("SOAP/KEGG", "get_ko_by_ko_class"));
=======
        _call.setOperationName(new javax.xml.namespace.QName("SOAP/KEGG", "get_ko_by_ko_class"));
>>>>>>> 2eb394907b98577f1b916408cf22a2de6952b22d

        setRequestHeaders(_call);
        setAttachments(_call);
 try {        java.lang.Object _resp = _call.invoke(new java.lang.Object[] {class_id});

        if (_resp instanceof java.rmi.RemoteException) {
            throw (java.rmi.RemoteException)_resp;
        }
        else {
            extractAttachments(_call);
            try {
                return (Definition[]) _resp;
            } catch (java.lang.Exception _exception) {
                return (Definition[]) JavaUtils.convert(_resp, Definition[].class);
            }
        }
  } catch (org.apache.axis.AxisFault axisFaultException) {
  throw axisFaultException;
}
    }

<<<<<<< HEAD
    public Definition[] get_genes_by_ko(String ko_id, String org) throws java.rmi.RemoteException {
=======
    public Definition[] get_genes_by_ko(java.lang.String ko_id, java.lang.String org) throws java.rmi.RemoteException {
>>>>>>> 2eb394907b98577f1b916408cf22a2de6952b22d
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[18]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("SOAP/KEGG#get_genes_by_ko");
        _call.setSOAPVersion(SOAPConstants.SOAP11_CONSTANTS);
<<<<<<< HEAD
        _call.setOperationName(new QName("SOAP/KEGG", "get_genes_by_ko"));
=======
        _call.setOperationName(new javax.xml.namespace.QName("SOAP/KEGG", "get_genes_by_ko"));
>>>>>>> 2eb394907b98577f1b916408cf22a2de6952b22d

        setRequestHeaders(_call);
        setAttachments(_call);
 try {        java.lang.Object _resp = _call.invoke(new java.lang.Object[] {ko_id, org});

        if (_resp instanceof java.rmi.RemoteException) {
            throw (java.rmi.RemoteException)_resp;
        }
        else {
            extractAttachments(_call);
            try {
                return (Definition[]) _resp;
            } catch (java.lang.Exception _exception) {
                return (Definition[]) JavaUtils.convert(_resp, Definition[].class);
            }
        }
  } catch (org.apache.axis.AxisFault axisFaultException) {
  throw axisFaultException;
}
    }

<<<<<<< HEAD
    public Definition[] get_genes_by_ko_class(String class_id, String org, int offset, int limit) throws java.rmi.RemoteException {
=======
    public Definition[] get_genes_by_ko_class(java.lang.String class_id, java.lang.String org, int offset, int limit) throws java.rmi.RemoteException {
>>>>>>> 2eb394907b98577f1b916408cf22a2de6952b22d
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[19]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("SOAP/KEGG#get_genes_by_ko_class");
        _call.setSOAPVersion(SOAPConstants.SOAP11_CONSTANTS);
<<<<<<< HEAD
        _call.setOperationName(new QName("SOAP/KEGG", "get_genes_by_ko_class"));
=======
        _call.setOperationName(new javax.xml.namespace.QName("SOAP/KEGG", "get_genes_by_ko_class"));
>>>>>>> 2eb394907b98577f1b916408cf22a2de6952b22d

        setRequestHeaders(_call);
        setAttachments(_call);
 try {        java.lang.Object _resp = _call.invoke(new java.lang.Object[] {class_id, org, new java.lang.Integer(offset), new java.lang.Integer(limit)});

        if (_resp instanceof java.rmi.RemoteException) {
            throw (java.rmi.RemoteException)_resp;
        }
        else {
            extractAttachments(_call);
            try {
                return (Definition[]) _resp;
            } catch (java.lang.Exception _exception) {
                return (Definition[]) JavaUtils.convert(_resp, Definition[].class);
            }
        }
  } catch (org.apache.axis.AxisFault axisFaultException) {
  throw axisFaultException;
}
    }

<<<<<<< HEAD
    public PathwayElement[] get_elements_by_pathway(String pathway_id) throws java.rmi.RemoteException {
=======
    public PathwayElement[] get_elements_by_pathway(java.lang.String pathway_id) throws java.rmi.RemoteException {
>>>>>>> 2eb394907b98577f1b916408cf22a2de6952b22d
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[20]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("SOAP/KEGG#get_elements_by_pathway");
        _call.setSOAPVersion(SOAPConstants.SOAP11_CONSTANTS);
<<<<<<< HEAD
        _call.setOperationName(new QName("SOAP/KEGG", "get_elements_by_pathway"));
=======
        _call.setOperationName(new javax.xml.namespace.QName("SOAP/KEGG", "get_elements_by_pathway"));
>>>>>>> 2eb394907b98577f1b916408cf22a2de6952b22d

        setRequestHeaders(_call);
        setAttachments(_call);
 try {        java.lang.Object _resp = _call.invoke(new java.lang.Object[] {pathway_id});

        if (_resp instanceof java.rmi.RemoteException) {
            throw (java.rmi.RemoteException)_resp;
        }
        else {
            extractAttachments(_call);
            try {
                return (PathwayElement[]) _resp;
            } catch (java.lang.Exception _exception) {
                return (PathwayElement[]) JavaUtils.convert(_resp, PathwayElement[].class);
            }
        }
  } catch (org.apache.axis.AxisFault axisFaultException) {
  throw axisFaultException;
}
    }

<<<<<<< HEAD
    public PathwayElementRelation[] get_element_relations_by_pathway(String pathway_id) throws java.rmi.RemoteException {
=======
    public PathwayElementRelation[] get_element_relations_by_pathway(java.lang.String pathway_id) throws java.rmi.RemoteException {
>>>>>>> 2eb394907b98577f1b916408cf22a2de6952b22d
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[21]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("SOAP/KEGG#get_element_relations_by_pathway");
        _call.setSOAPVersion(SOAPConstants.SOAP11_CONSTANTS);
<<<<<<< HEAD
        _call.setOperationName(new QName("SOAP/KEGG", "get_element_relations_by_pathway"));
=======
        _call.setOperationName(new javax.xml.namespace.QName("SOAP/KEGG", "get_element_relations_by_pathway"));
>>>>>>> 2eb394907b98577f1b916408cf22a2de6952b22d

        setRequestHeaders(_call);
        setAttachments(_call);
 try {        java.lang.Object _resp = _call.invoke(new java.lang.Object[] {pathway_id});

        if (_resp instanceof java.rmi.RemoteException) {
            throw (java.rmi.RemoteException)_resp;
        }
        else {
            extractAttachments(_call);
            try {
                return (PathwayElementRelation[]) _resp;
            } catch (java.lang.Exception _exception) {
                return (PathwayElementRelation[]) JavaUtils.convert(_resp, PathwayElementRelation[].class);
            }
        }
  } catch (org.apache.axis.AxisFault axisFaultException) {
  throw axisFaultException;
}
    }

<<<<<<< HEAD
    public String color_pathway_by_elements(String pathway_id, int[] element_list, String[] fg_color_list, String[] bg_color_list) throws java.rmi.RemoteException {
=======
    public java.lang.String color_pathway_by_elements(java.lang.String pathway_id, int[] element_list, java.lang.String[] fg_color_list, java.lang.String[] bg_color_list) throws java.rmi.RemoteException {
>>>>>>> 2eb394907b98577f1b916408cf22a2de6952b22d
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[22]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("SOAP/KEGG#color_pathway_by_elements");
        _call.setSOAPVersion(SOAPConstants.SOAP11_CONSTANTS);
<<<<<<< HEAD
        _call.setOperationName(new QName("SOAP/KEGG", "color_pathway_by_elements"));
=======
        _call.setOperationName(new javax.xml.namespace.QName("SOAP/KEGG", "color_pathway_by_elements"));
>>>>>>> 2eb394907b98577f1b916408cf22a2de6952b22d

        setRequestHeaders(_call);
        setAttachments(_call);
 try {        java.lang.Object _resp = _call.invoke(new java.lang.Object[] {pathway_id, element_list, fg_color_list, bg_color_list});

        if (_resp instanceof java.rmi.RemoteException) {
            throw (java.rmi.RemoteException)_resp;
        }
        else {
            extractAttachments(_call);
            try {
<<<<<<< HEAD
                return (String) _resp;
            } catch (java.lang.Exception _exception) {
                return (String) JavaUtils.convert(_resp, String.class);
=======
                return (java.lang.String) _resp;
            } catch (java.lang.Exception _exception) {
                return (java.lang.String) JavaUtils.convert(_resp, java.lang.String.class);
>>>>>>> 2eb394907b98577f1b916408cf22a2de6952b22d
            }
        }
  } catch (org.apache.axis.AxisFault axisFaultException) {
  throw axisFaultException;
}
    }

<<<<<<< HEAD
    public String get_html_of_colored_pathway_by_elements(String pathway_id, int[] element_list, String[] fg_color_list, String[] bg_color_list) throws java.rmi.RemoteException {
=======
    public java.lang.String get_html_of_colored_pathway_by_elements(java.lang.String pathway_id, int[] element_list, java.lang.String[] fg_color_list, java.lang.String[] bg_color_list) throws java.rmi.RemoteException {
>>>>>>> 2eb394907b98577f1b916408cf22a2de6952b22d
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[23]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("SOAP/KEGG#get_html_of_colored_pathway_by_elements");
        _call.setSOAPVersion(SOAPConstants.SOAP11_CONSTANTS);
<<<<<<< HEAD
        _call.setOperationName(new QName("SOAP/KEGG", "get_html_of_colored_pathway_by_elements"));
=======
        _call.setOperationName(new javax.xml.namespace.QName("SOAP/KEGG", "get_html_of_colored_pathway_by_elements"));
>>>>>>> 2eb394907b98577f1b916408cf22a2de6952b22d

        setRequestHeaders(_call);
        setAttachments(_call);
 try {        java.lang.Object _resp = _call.invoke(new java.lang.Object[] {pathway_id, element_list, fg_color_list, bg_color_list});

        if (_resp instanceof java.rmi.RemoteException) {
            throw (java.rmi.RemoteException)_resp;
        }
        else {
            extractAttachments(_call);
            try {
<<<<<<< HEAD
                return (String) _resp;
            } catch (java.lang.Exception _exception) {
                return (String) JavaUtils.convert(_resp, String.class);
=======
                return (java.lang.String) _resp;
            } catch (java.lang.Exception _exception) {
                return (java.lang.String) JavaUtils.convert(_resp, java.lang.String.class);
>>>>>>> 2eb394907b98577f1b916408cf22a2de6952b22d
            }
        }
  } catch (org.apache.axis.AxisFault axisFaultException) {
  throw axisFaultException;
}
    }

<<<<<<< HEAD
    public String mark_pathway_by_objects(String pathway_id, String[] object_id_list) throws java.rmi.RemoteException {
=======
    public java.lang.String mark_pathway_by_objects(java.lang.String pathway_id, java.lang.String[] object_id_list) throws java.rmi.RemoteException {
>>>>>>> 2eb394907b98577f1b916408cf22a2de6952b22d
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[24]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("SOAP/KEGG#mark_pathway_by_objects");
        _call.setSOAPVersion(SOAPConstants.SOAP11_CONSTANTS);
<<<<<<< HEAD
        _call.setOperationName(new QName("SOAP/KEGG", "mark_pathway_by_objects"));
=======
        _call.setOperationName(new javax.xml.namespace.QName("SOAP/KEGG", "mark_pathway_by_objects"));
>>>>>>> 2eb394907b98577f1b916408cf22a2de6952b22d

        setRequestHeaders(_call);
        setAttachments(_call);
 try {        java.lang.Object _resp = _call.invoke(new java.lang.Object[] {pathway_id, object_id_list});

        if (_resp instanceof java.rmi.RemoteException) {
            throw (java.rmi.RemoteException)_resp;
        }
        else {
            extractAttachments(_call);
            try {
<<<<<<< HEAD
                return (String) _resp;
            } catch (java.lang.Exception _exception) {
                return (String) JavaUtils.convert(_resp, String.class);
=======
                return (java.lang.String) _resp;
            } catch (java.lang.Exception _exception) {
                return (java.lang.String) JavaUtils.convert(_resp, java.lang.String.class);
>>>>>>> 2eb394907b98577f1b916408cf22a2de6952b22d
            }
        }
  } catch (org.apache.axis.AxisFault axisFaultException) {
  throw axisFaultException;
}
    }

<<<<<<< HEAD
    public String color_pathway_by_objects(String pathway_id, String[] object_id_list, String[] fg_color_list, String[] bg_color_list) throws java.rmi.RemoteException {
=======
    public java.lang.String color_pathway_by_objects(java.lang.String pathway_id, java.lang.String[] object_id_list, java.lang.String[] fg_color_list, java.lang.String[] bg_color_list) throws java.rmi.RemoteException {
>>>>>>> 2eb394907b98577f1b916408cf22a2de6952b22d
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[25]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("SOAP/KEGG#color_pathway_by_objects");
        _call.setSOAPVersion(SOAPConstants.SOAP11_CONSTANTS);
<<<<<<< HEAD
        _call.setOperationName(new QName("SOAP/KEGG", "color_pathway_by_objects"));
=======
        _call.setOperationName(new javax.xml.namespace.QName("SOAP/KEGG", "color_pathway_by_objects"));
>>>>>>> 2eb394907b98577f1b916408cf22a2de6952b22d

        setRequestHeaders(_call);
        setAttachments(_call);
 try {        java.lang.Object _resp = _call.invoke(new java.lang.Object[] {pathway_id, object_id_list, fg_color_list, bg_color_list});

        if (_resp instanceof java.rmi.RemoteException) {
            throw (java.rmi.RemoteException)_resp;
        }
        else {
            extractAttachments(_call);
            try {
<<<<<<< HEAD
                return (String) _resp;
            } catch (java.lang.Exception _exception) {
                return (String) JavaUtils.convert(_resp, String.class);
=======
                return (java.lang.String) _resp;
            } catch (java.lang.Exception _exception) {
                return (java.lang.String) JavaUtils.convert(_resp, java.lang.String.class);
>>>>>>> 2eb394907b98577f1b916408cf22a2de6952b22d
            }
        }
  } catch (org.apache.axis.AxisFault axisFaultException) {
  throw axisFaultException;
}
    }

<<<<<<< HEAD
    public String get_html_of_marked_pathway_by_objects(String pathway_id, String[] object_id_list) throws java.rmi.RemoteException {
=======
    public java.lang.String get_html_of_marked_pathway_by_objects(java.lang.String pathway_id, java.lang.String[] object_id_list) throws java.rmi.RemoteException {
>>>>>>> 2eb394907b98577f1b916408cf22a2de6952b22d
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[26]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("SOAP/KEGG#get_html_of_marked_pathway_by_objects");
        _call.setSOAPVersion(SOAPConstants.SOAP11_CONSTANTS);
<<<<<<< HEAD
        _call.setOperationName(new QName("SOAP/KEGG", "get_html_of_marked_pathway_by_objects"));
=======
        _call.setOperationName(new javax.xml.namespace.QName("SOAP/KEGG", "get_html_of_marked_pathway_by_objects"));
>>>>>>> 2eb394907b98577f1b916408cf22a2de6952b22d

        setRequestHeaders(_call);
        setAttachments(_call);
 try {        java.lang.Object _resp = _call.invoke(new java.lang.Object[] {pathway_id, object_id_list});

        if (_resp instanceof java.rmi.RemoteException) {
            throw (java.rmi.RemoteException)_resp;
        }
        else {
            extractAttachments(_call);
            try {
<<<<<<< HEAD
                return (String) _resp;
            } catch (java.lang.Exception _exception) {
                return (String) JavaUtils.convert(_resp, String.class);
=======
                return (java.lang.String) _resp;
            } catch (java.lang.Exception _exception) {
                return (java.lang.String) JavaUtils.convert(_resp, java.lang.String.class);
>>>>>>> 2eb394907b98577f1b916408cf22a2de6952b22d
            }
        }
  } catch (org.apache.axis.AxisFault axisFaultException) {
  throw axisFaultException;
}
    }

<<<<<<< HEAD
    public String get_html_of_colored_pathway_by_objects(String pathway_id, String[] object_id_list, String[] fg_color_list, String[] bg_color_list) throws java.rmi.RemoteException {
=======
    public java.lang.String get_html_of_colored_pathway_by_objects(java.lang.String pathway_id, java.lang.String[] object_id_list, java.lang.String[] fg_color_list, java.lang.String[] bg_color_list) throws java.rmi.RemoteException {
>>>>>>> 2eb394907b98577f1b916408cf22a2de6952b22d
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[27]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("SOAP/KEGG#get_html_of_colored_pathway_by_objects");
        _call.setSOAPVersion(SOAPConstants.SOAP11_CONSTANTS);
<<<<<<< HEAD
        _call.setOperationName(new QName("SOAP/KEGG", "get_html_of_colored_pathway_by_objects"));
=======
        _call.setOperationName(new javax.xml.namespace.QName("SOAP/KEGG", "get_html_of_colored_pathway_by_objects"));
>>>>>>> 2eb394907b98577f1b916408cf22a2de6952b22d

        setRequestHeaders(_call);
        setAttachments(_call);
 try {        java.lang.Object _resp = _call.invoke(new java.lang.Object[] {pathway_id, object_id_list, fg_color_list, bg_color_list});

        if (_resp instanceof java.rmi.RemoteException) {
            throw (java.rmi.RemoteException)_resp;
        }
        else {
            extractAttachments(_call);
            try {
<<<<<<< HEAD
                return (String) _resp;
            } catch (java.lang.Exception _exception) {
                return (String) JavaUtils.convert(_resp, String.class);
=======
                return (java.lang.String) _resp;
            } catch (java.lang.Exception _exception) {
                return (java.lang.String) JavaUtils.convert(_resp, java.lang.String.class);
>>>>>>> 2eb394907b98577f1b916408cf22a2de6952b22d
            }
        }
  } catch (org.apache.axis.AxisFault axisFaultException) {
  throw axisFaultException;
}
    }

<<<<<<< HEAD
    public String[] get_genes_by_pathway(String pathway_id) throws java.rmi.RemoteException {
=======
    public java.lang.String[] get_genes_by_pathway(java.lang.String pathway_id) throws java.rmi.RemoteException {
>>>>>>> 2eb394907b98577f1b916408cf22a2de6952b22d
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[28]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("SOAP/KEGG#get_genes_by_pathway");
        _call.setSOAPVersion(SOAPConstants.SOAP11_CONSTANTS);
<<<<<<< HEAD
        _call.setOperationName(new QName("SOAP/KEGG", "get_genes_by_pathway"));
=======
        _call.setOperationName(new javax.xml.namespace.QName("SOAP/KEGG", "get_genes_by_pathway"));
>>>>>>> 2eb394907b98577f1b916408cf22a2de6952b22d

        setRequestHeaders(_call);
        setAttachments(_call);
 try {        java.lang.Object _resp = _call.invoke(new java.lang.Object[] {pathway_id});

        if (_resp instanceof java.rmi.RemoteException) {
            throw (java.rmi.RemoteException)_resp;
        }
        else {
            extractAttachments(_call);
            try {
<<<<<<< HEAD
                return (String[]) _resp;
            } catch (java.lang.Exception _exception) {
                return (String[]) JavaUtils.convert(_resp, String[].class);
=======
                return (java.lang.String[]) _resp;
            } catch (java.lang.Exception _exception) {
                return (java.lang.String[]) JavaUtils.convert(_resp, java.lang.String[].class);
>>>>>>> 2eb394907b98577f1b916408cf22a2de6952b22d
            }
        }
  } catch (org.apache.axis.AxisFault axisFaultException) {
  throw axisFaultException;
}
    }

<<<<<<< HEAD
    public String[] get_enzymes_by_pathway(String pathway_id) throws java.rmi.RemoteException {
=======
    public java.lang.String[] get_enzymes_by_pathway(java.lang.String pathway_id) throws java.rmi.RemoteException {
>>>>>>> 2eb394907b98577f1b916408cf22a2de6952b22d
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[29]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("SOAP/KEGG#get_enzymes_by_pathway");
        _call.setSOAPVersion(SOAPConstants.SOAP11_CONSTANTS);
<<<<<<< HEAD
        _call.setOperationName(new QName("SOAP/KEGG", "get_enzymes_by_pathway"));
=======
        _call.setOperationName(new javax.xml.namespace.QName("SOAP/KEGG", "get_enzymes_by_pathway"));
>>>>>>> 2eb394907b98577f1b916408cf22a2de6952b22d

        setRequestHeaders(_call);
        setAttachments(_call);
 try {        java.lang.Object _resp = _call.invoke(new java.lang.Object[] {pathway_id});

        if (_resp instanceof java.rmi.RemoteException) {
            throw (java.rmi.RemoteException)_resp;
        }
        else {
            extractAttachments(_call);
            try {
<<<<<<< HEAD
                return (String[]) _resp;
            } catch (java.lang.Exception _exception) {
                return (String[]) JavaUtils.convert(_resp, String[].class);
=======
                return (java.lang.String[]) _resp;
            } catch (java.lang.Exception _exception) {
                return (java.lang.String[]) JavaUtils.convert(_resp, java.lang.String[].class);
>>>>>>> 2eb394907b98577f1b916408cf22a2de6952b22d
            }
        }
  } catch (org.apache.axis.AxisFault axisFaultException) {
  throw axisFaultException;
}
    }

<<<<<<< HEAD
    public String[] get_reactions_by_pathway(String pathway_id) throws java.rmi.RemoteException {
=======
    public java.lang.String[] get_reactions_by_pathway(java.lang.String pathway_id) throws java.rmi.RemoteException {
>>>>>>> 2eb394907b98577f1b916408cf22a2de6952b22d
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[30]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("SOAP/KEGG#get_reactions_by_pathway");
        _call.setSOAPVersion(SOAPConstants.SOAP11_CONSTANTS);
<<<<<<< HEAD
        _call.setOperationName(new QName("SOAP/KEGG", "get_reactions_by_pathway"));
=======
        _call.setOperationName(new javax.xml.namespace.QName("SOAP/KEGG", "get_reactions_by_pathway"));
>>>>>>> 2eb394907b98577f1b916408cf22a2de6952b22d

        setRequestHeaders(_call);
        setAttachments(_call);
 try {        java.lang.Object _resp = _call.invoke(new java.lang.Object[] {pathway_id});

        if (_resp instanceof java.rmi.RemoteException) {
            throw (java.rmi.RemoteException)_resp;
        }
        else {
            extractAttachments(_call);
            try {
<<<<<<< HEAD
                return (String[]) _resp;
            } catch (java.lang.Exception _exception) {
                return (String[]) JavaUtils.convert(_resp, String[].class);
=======
                return (java.lang.String[]) _resp;
            } catch (java.lang.Exception _exception) {
                return (java.lang.String[]) JavaUtils.convert(_resp, java.lang.String[].class);
>>>>>>> 2eb394907b98577f1b916408cf22a2de6952b22d
            }
        }
  } catch (org.apache.axis.AxisFault axisFaultException) {
  throw axisFaultException;
}
    }

<<<<<<< HEAD
    public String[] get_pathways_by_genes(String[] genes_id_list) throws java.rmi.RemoteException {
=======
    public java.lang.String[] get_pathways_by_genes(java.lang.String[] genes_id_list) throws java.rmi.RemoteException {
>>>>>>> 2eb394907b98577f1b916408cf22a2de6952b22d
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[31]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("SOAP/KEGG#get_pathways_by_genes");
        _call.setSOAPVersion(SOAPConstants.SOAP11_CONSTANTS);
<<<<<<< HEAD
        _call.setOperationName(new QName("SOAP/KEGG", "get_pathways_by_genes"));
=======
        _call.setOperationName(new javax.xml.namespace.QName("SOAP/KEGG", "get_pathways_by_genes"));
>>>>>>> 2eb394907b98577f1b916408cf22a2de6952b22d

        setRequestHeaders(_call);
        setAttachments(_call);
 try {        java.lang.Object _resp = _call.invoke(new java.lang.Object[] {genes_id_list});

        if (_resp instanceof java.rmi.RemoteException) {
            throw (java.rmi.RemoteException)_resp;
        }
        else {
            extractAttachments(_call);
            try {
<<<<<<< HEAD
                return (String[]) _resp;
            } catch (java.lang.Exception _exception) {
                return (String[]) JavaUtils.convert(_resp, String[].class);
=======
                return (java.lang.String[]) _resp;
            } catch (java.lang.Exception _exception) {
                return (java.lang.String[]) JavaUtils.convert(_resp, java.lang.String[].class);
>>>>>>> 2eb394907b98577f1b916408cf22a2de6952b22d
            }
        }
  } catch (org.apache.axis.AxisFault axisFaultException) {
  throw axisFaultException;
}
    }

<<<<<<< HEAD
    public String[] get_pathways_by_enzymes(String[] enzyme_id_list) throws java.rmi.RemoteException {
=======
    public java.lang.String[] get_pathways_by_enzymes(java.lang.String[] enzyme_id_list) throws java.rmi.RemoteException {
>>>>>>> 2eb394907b98577f1b916408cf22a2de6952b22d
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[32]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("SOAP/KEGG#get_pathways_by_enzymes");
        _call.setSOAPVersion(SOAPConstants.SOAP11_CONSTANTS);
<<<<<<< HEAD
        _call.setOperationName(new QName("SOAP/KEGG", "get_pathways_by_enzymes"));
=======
        _call.setOperationName(new javax.xml.namespace.QName("SOAP/KEGG", "get_pathways_by_enzymes"));
>>>>>>> 2eb394907b98577f1b916408cf22a2de6952b22d

        setRequestHeaders(_call);
        setAttachments(_call);
 try {        java.lang.Object _resp = _call.invoke(new java.lang.Object[] {enzyme_id_list});

        if (_resp instanceof java.rmi.RemoteException) {
            throw (java.rmi.RemoteException)_resp;
        }
        else {
            extractAttachments(_call);
            try {
<<<<<<< HEAD
                return (String[]) _resp;
            } catch (java.lang.Exception _exception) {
                return (String[]) JavaUtils.convert(_resp, String[].class);
=======
                return (java.lang.String[]) _resp;
            } catch (java.lang.Exception _exception) {
                return (java.lang.String[]) JavaUtils.convert(_resp, java.lang.String[].class);
>>>>>>> 2eb394907b98577f1b916408cf22a2de6952b22d
            }
        }
  } catch (org.apache.axis.AxisFault axisFaultException) {
  throw axisFaultException;
}
    }

<<<<<<< HEAD
    public String[] get_pathways_by_reactions(String[] reaction_id_list) throws java.rmi.RemoteException {
=======
    public java.lang.String[] get_pathways_by_reactions(java.lang.String[] reaction_id_list) throws java.rmi.RemoteException {
>>>>>>> 2eb394907b98577f1b916408cf22a2de6952b22d
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[33]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("SOAP/KEGG#get_pathways_by_reactions");
        _call.setSOAPVersion(SOAPConstants.SOAP11_CONSTANTS);
<<<<<<< HEAD
        _call.setOperationName(new QName("SOAP/KEGG", "get_pathways_by_reactions"));
=======
        _call.setOperationName(new javax.xml.namespace.QName("SOAP/KEGG", "get_pathways_by_reactions"));
>>>>>>> 2eb394907b98577f1b916408cf22a2de6952b22d

        setRequestHeaders(_call);
        setAttachments(_call);
 try {        java.lang.Object _resp = _call.invoke(new java.lang.Object[] {reaction_id_list});

        if (_resp instanceof java.rmi.RemoteException) {
            throw (java.rmi.RemoteException)_resp;
        }
        else {
            extractAttachments(_call);
            try {
<<<<<<< HEAD
                return (String[]) _resp;
            } catch (java.lang.Exception _exception) {
                return (String[]) JavaUtils.convert(_resp, String[].class);
=======
                return (java.lang.String[]) _resp;
            } catch (java.lang.Exception _exception) {
                return (java.lang.String[]) JavaUtils.convert(_resp, java.lang.String[].class);
>>>>>>> 2eb394907b98577f1b916408cf22a2de6952b22d
            }
        }
  } catch (org.apache.axis.AxisFault axisFaultException) {
  throw axisFaultException;
}
    }

<<<<<<< HEAD
    public String[] get_linked_pathways(String pathway_id) throws java.rmi.RemoteException {
=======
    public java.lang.String[] get_linked_pathways(java.lang.String pathway_id) throws java.rmi.RemoteException {
>>>>>>> 2eb394907b98577f1b916408cf22a2de6952b22d
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[34]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("SOAP/KEGG#get_linked_pathways");
        _call.setSOAPVersion(SOAPConstants.SOAP11_CONSTANTS);
<<<<<<< HEAD
        _call.setOperationName(new QName("SOAP/KEGG", "get_linked_pathways"));
=======
        _call.setOperationName(new javax.xml.namespace.QName("SOAP/KEGG", "get_linked_pathways"));
>>>>>>> 2eb394907b98577f1b916408cf22a2de6952b22d

        setRequestHeaders(_call);
        setAttachments(_call);
 try {        java.lang.Object _resp = _call.invoke(new java.lang.Object[] {pathway_id});

        if (_resp instanceof java.rmi.RemoteException) {
            throw (java.rmi.RemoteException)_resp;
        }
        else {
            extractAttachments(_call);
            try {
<<<<<<< HEAD
                return (String[]) _resp;
            } catch (java.lang.Exception _exception) {
                return (String[]) JavaUtils.convert(_resp, String[].class);
=======
                return (java.lang.String[]) _resp;
            } catch (java.lang.Exception _exception) {
                return (java.lang.String[]) JavaUtils.convert(_resp, java.lang.String[].class);
>>>>>>> 2eb394907b98577f1b916408cf22a2de6952b22d
            }
        }
  } catch (org.apache.axis.AxisFault axisFaultException) {
  throw axisFaultException;
}
    }

<<<<<<< HEAD
    public String[] get_genes_by_enzyme(String enzyme_id, String org) throws java.rmi.RemoteException {
=======
    public java.lang.String[] get_genes_by_enzyme(java.lang.String enzyme_id, java.lang.String org) throws java.rmi.RemoteException {
>>>>>>> 2eb394907b98577f1b916408cf22a2de6952b22d
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[35]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("SOAP/KEGG#get_genes_by_enzyme");
        _call.setSOAPVersion(SOAPConstants.SOAP11_CONSTANTS);
<<<<<<< HEAD
        _call.setOperationName(new QName("SOAP/KEGG", "get_genes_by_enzyme"));
=======
        _call.setOperationName(new javax.xml.namespace.QName("SOAP/KEGG", "get_genes_by_enzyme"));
>>>>>>> 2eb394907b98577f1b916408cf22a2de6952b22d

        setRequestHeaders(_call);
        setAttachments(_call);
 try {        java.lang.Object _resp = _call.invoke(new java.lang.Object[] {enzyme_id, org});

        if (_resp instanceof java.rmi.RemoteException) {
            throw (java.rmi.RemoteException)_resp;
        }
        else {
            extractAttachments(_call);
            try {
<<<<<<< HEAD
                return (String[]) _resp;
            } catch (java.lang.Exception _exception) {
                return (String[]) JavaUtils.convert(_resp, String[].class);
=======
                return (java.lang.String[]) _resp;
            } catch (java.lang.Exception _exception) {
                return (java.lang.String[]) JavaUtils.convert(_resp, java.lang.String[].class);
>>>>>>> 2eb394907b98577f1b916408cf22a2de6952b22d
            }
        }
  } catch (org.apache.axis.AxisFault axisFaultException) {
  throw axisFaultException;
}
    }

<<<<<<< HEAD
    public String[] get_enzymes_by_gene(String genes_id) throws java.rmi.RemoteException {
=======
    public java.lang.String[] get_enzymes_by_gene(java.lang.String genes_id) throws java.rmi.RemoteException {
>>>>>>> 2eb394907b98577f1b916408cf22a2de6952b22d
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[36]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("SOAP/KEGG#get_enzymes_by_gene");
        _call.setSOAPVersion(SOAPConstants.SOAP11_CONSTANTS);
<<<<<<< HEAD
        _call.setOperationName(new QName("SOAP/KEGG", "get_enzymes_by_gene"));
=======
        _call.setOperationName(new javax.xml.namespace.QName("SOAP/KEGG", "get_enzymes_by_gene"));
>>>>>>> 2eb394907b98577f1b916408cf22a2de6952b22d

        setRequestHeaders(_call);
        setAttachments(_call);
 try {        java.lang.Object _resp = _call.invoke(new java.lang.Object[] {genes_id});

        if (_resp instanceof java.rmi.RemoteException) {
            throw (java.rmi.RemoteException)_resp;
        }
        else {
            extractAttachments(_call);
            try {
<<<<<<< HEAD
                return (String[]) _resp;
            } catch (java.lang.Exception _exception) {
                return (String[]) JavaUtils.convert(_resp, String[].class);
=======
                return (java.lang.String[]) _resp;
            } catch (java.lang.Exception _exception) {
                return (java.lang.String[]) JavaUtils.convert(_resp, java.lang.String[].class);
>>>>>>> 2eb394907b98577f1b916408cf22a2de6952b22d
            }
        }
  } catch (org.apache.axis.AxisFault axisFaultException) {
  throw axisFaultException;
}
    }

<<<<<<< HEAD
    public String[] get_enzymes_by_reaction(String reaction_id) throws java.rmi.RemoteException {
=======
    public java.lang.String[] get_enzymes_by_reaction(java.lang.String reaction_id) throws java.rmi.RemoteException {
>>>>>>> 2eb394907b98577f1b916408cf22a2de6952b22d
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[37]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("SOAP/KEGG#get_enzymes_by_reaction");
        _call.setSOAPVersion(SOAPConstants.SOAP11_CONSTANTS);
<<<<<<< HEAD
        _call.setOperationName(new QName("SOAP/KEGG", "get_enzymes_by_reaction"));
=======
        _call.setOperationName(new javax.xml.namespace.QName("SOAP/KEGG", "get_enzymes_by_reaction"));
>>>>>>> 2eb394907b98577f1b916408cf22a2de6952b22d

        setRequestHeaders(_call);
        setAttachments(_call);
 try {        java.lang.Object _resp = _call.invoke(new java.lang.Object[] {reaction_id});

        if (_resp instanceof java.rmi.RemoteException) {
            throw (java.rmi.RemoteException)_resp;
        }
        else {
            extractAttachments(_call);
            try {
<<<<<<< HEAD
                return (String[]) _resp;
            } catch (java.lang.Exception _exception) {
                return (String[]) JavaUtils.convert(_resp, String[].class);
=======
                return (java.lang.String[]) _resp;
            } catch (java.lang.Exception _exception) {
                return (java.lang.String[]) JavaUtils.convert(_resp, java.lang.String[].class);
>>>>>>> 2eb394907b98577f1b916408cf22a2de6952b22d
            }
        }
  } catch (org.apache.axis.AxisFault axisFaultException) {
  throw axisFaultException;
}
    }

<<<<<<< HEAD
    public String[] get_reactions_by_enzyme(String enzyme_id) throws java.rmi.RemoteException {
=======
    public java.lang.String[] get_reactions_by_enzyme(java.lang.String enzyme_id) throws java.rmi.RemoteException {
>>>>>>> 2eb394907b98577f1b916408cf22a2de6952b22d
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[38]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("SOAP/KEGG#get_reactions_by_enzyme");
        _call.setSOAPVersion(SOAPConstants.SOAP11_CONSTANTS);
<<<<<<< HEAD
        _call.setOperationName(new QName("SOAP/KEGG", "get_reactions_by_enzyme"));
=======
        _call.setOperationName(new javax.xml.namespace.QName("SOAP/KEGG", "get_reactions_by_enzyme"));
>>>>>>> 2eb394907b98577f1b916408cf22a2de6952b22d

        setRequestHeaders(_call);
        setAttachments(_call);
 try {        java.lang.Object _resp = _call.invoke(new java.lang.Object[] {enzyme_id});

        if (_resp instanceof java.rmi.RemoteException) {
            throw (java.rmi.RemoteException)_resp;
        }
        else {
            extractAttachments(_call);
            try {
<<<<<<< HEAD
                return (String[]) _resp;
            } catch (java.lang.Exception _exception) {
                return (String[]) JavaUtils.convert(_resp, String[].class);
=======
                return (java.lang.String[]) _resp;
            } catch (java.lang.Exception _exception) {
                return (java.lang.String[]) JavaUtils.convert(_resp, java.lang.String[].class);
>>>>>>> 2eb394907b98577f1b916408cf22a2de6952b22d
            }
        }
  } catch (org.apache.axis.AxisFault axisFaultException) {
  throw axisFaultException;
}
    }

<<<<<<< HEAD
    public String[] get_genes_by_organism(String org, int offset, int limit) throws java.rmi.RemoteException {
=======
    public java.lang.String[] get_genes_by_organism(java.lang.String org, int offset, int limit) throws java.rmi.RemoteException {
>>>>>>> 2eb394907b98577f1b916408cf22a2de6952b22d
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[39]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("SOAP/KEGG#get_genes_by_organism");
        _call.setSOAPVersion(SOAPConstants.SOAP11_CONSTANTS);
<<<<<<< HEAD
        _call.setOperationName(new QName("SOAP/KEGG", "get_genes_by_organism"));
=======
        _call.setOperationName(new javax.xml.namespace.QName("SOAP/KEGG", "get_genes_by_organism"));
>>>>>>> 2eb394907b98577f1b916408cf22a2de6952b22d

        setRequestHeaders(_call);
        setAttachments(_call);
 try {        java.lang.Object _resp = _call.invoke(new java.lang.Object[] {org, new java.lang.Integer(offset), new java.lang.Integer(limit)});

        if (_resp instanceof java.rmi.RemoteException) {
            throw (java.rmi.RemoteException)_resp;
        }
        else {
            extractAttachments(_call);
            try {
<<<<<<< HEAD
                return (String[]) _resp;
            } catch (java.lang.Exception _exception) {
                return (String[]) JavaUtils.convert(_resp, String[].class);
=======
                return (java.lang.String[]) _resp;
            } catch (java.lang.Exception _exception) {
                return (java.lang.String[]) JavaUtils.convert(_resp, java.lang.String[].class);
>>>>>>> 2eb394907b98577f1b916408cf22a2de6952b22d
            }
        }
  } catch (org.apache.axis.AxisFault axisFaultException) {
  throw axisFaultException;
}
    }

<<<<<<< HEAD
    public int get_number_of_genes_by_organism(String abbr) throws java.rmi.RemoteException {
=======
    public int get_number_of_genes_by_organism(java.lang.String abbr) throws java.rmi.RemoteException {
>>>>>>> 2eb394907b98577f1b916408cf22a2de6952b22d
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[40]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("SOAP/KEGG#get_number_of_genes_by_organism");
        _call.setSOAPVersion(SOAPConstants.SOAP11_CONSTANTS);
<<<<<<< HEAD
        _call.setOperationName(new QName("SOAP/KEGG", "get_number_of_genes_by_organism"));
=======
        _call.setOperationName(new javax.xml.namespace.QName("SOAP/KEGG", "get_number_of_genes_by_organism"));
>>>>>>> 2eb394907b98577f1b916408cf22a2de6952b22d

        setRequestHeaders(_call);
        setAttachments(_call);
 try {        java.lang.Object _resp = _call.invoke(new java.lang.Object[] {abbr});

        if (_resp instanceof java.rmi.RemoteException) {
            throw (java.rmi.RemoteException)_resp;
        }
        else {
            extractAttachments(_call);
            try {
                return ((java.lang.Integer) _resp).intValue();
            } catch (java.lang.Exception _exception) {
                return ((java.lang.Integer) JavaUtils.convert(_resp, int.class)).intValue();
            }
        }
  } catch (org.apache.axis.AxisFault axisFaultException) {
  throw axisFaultException;
}
    }

<<<<<<< HEAD
    public String[] get_reactions_by_glycan(String glycan_id) throws java.rmi.RemoteException {
=======
    public java.lang.String[] get_reactions_by_glycan(java.lang.String glycan_id) throws java.rmi.RemoteException {
>>>>>>> 2eb394907b98577f1b916408cf22a2de6952b22d
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[41]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("SOAP/KEGG#get_reactions_by_glycan");
        _call.setSOAPVersion(SOAPConstants.SOAP11_CONSTANTS);
<<<<<<< HEAD
        _call.setOperationName(new QName("SOAP/KEGG", "get_reactions_by_glycan"));
=======
        _call.setOperationName(new javax.xml.namespace.QName("SOAP/KEGG", "get_reactions_by_glycan"));
>>>>>>> 2eb394907b98577f1b916408cf22a2de6952b22d

        setRequestHeaders(_call);
        setAttachments(_call);
 try {        java.lang.Object _resp = _call.invoke(new java.lang.Object[] {glycan_id});

        if (_resp instanceof java.rmi.RemoteException) {
            throw (java.rmi.RemoteException)_resp;
        }
        else {
            extractAttachments(_call);
            try {
<<<<<<< HEAD
                return (String[]) _resp;
            } catch (java.lang.Exception _exception) {
                return (String[]) JavaUtils.convert(_resp, String[].class);
=======
                return (java.lang.String[]) _resp;
            } catch (java.lang.Exception _exception) {
                return (java.lang.String[]) JavaUtils.convert(_resp, java.lang.String[].class);
>>>>>>> 2eb394907b98577f1b916408cf22a2de6952b22d
            }
        }
  } catch (org.apache.axis.AxisFault axisFaultException) {
  throw axisFaultException;
}
    }

<<<<<<< HEAD
    public String[] get_reactions_by_compound(String compound_id) throws java.rmi.RemoteException {
=======
    public java.lang.String[] get_reactions_by_compound(java.lang.String compound_id) throws java.rmi.RemoteException {
>>>>>>> 2eb394907b98577f1b916408cf22a2de6952b22d
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[42]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("SOAP/KEGG#get_reactions_by_compound");
        _call.setSOAPVersion(SOAPConstants.SOAP11_CONSTANTS);
<<<<<<< HEAD
        _call.setOperationName(new QName("SOAP/KEGG", "get_reactions_by_compound"));
=======
        _call.setOperationName(new javax.xml.namespace.QName("SOAP/KEGG", "get_reactions_by_compound"));
>>>>>>> 2eb394907b98577f1b916408cf22a2de6952b22d

        setRequestHeaders(_call);
        setAttachments(_call);
 try {        java.lang.Object _resp = _call.invoke(new java.lang.Object[] {compound_id});

        if (_resp instanceof java.rmi.RemoteException) {
            throw (java.rmi.RemoteException)_resp;
        }
        else {
            extractAttachments(_call);
            try {
<<<<<<< HEAD
                return (String[]) _resp;
            } catch (java.lang.Exception _exception) {
                return (String[]) JavaUtils.convert(_resp, String[].class);
=======
                return (java.lang.String[]) _resp;
            } catch (java.lang.Exception _exception) {
                return (java.lang.String[]) JavaUtils.convert(_resp, java.lang.String[].class);
>>>>>>> 2eb394907b98577f1b916408cf22a2de6952b22d
            }
        }
  } catch (org.apache.axis.AxisFault axisFaultException) {
  throw axisFaultException;
}
    }

<<<<<<< HEAD
    public String[] get_enzymes_by_glycan(String glycan_id) throws java.rmi.RemoteException {
=======
    public java.lang.String[] get_enzymes_by_glycan(java.lang.String glycan_id) throws java.rmi.RemoteException {
>>>>>>> 2eb394907b98577f1b916408cf22a2de6952b22d
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[43]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("SOAP/KEGG#get_enzymes_by_glycan");
        _call.setSOAPVersion(SOAPConstants.SOAP11_CONSTANTS);
<<<<<<< HEAD
        _call.setOperationName(new QName("SOAP/KEGG", "get_enzymes_by_glycan"));
=======
        _call.setOperationName(new javax.xml.namespace.QName("SOAP/KEGG", "get_enzymes_by_glycan"));
>>>>>>> 2eb394907b98577f1b916408cf22a2de6952b22d

        setRequestHeaders(_call);
        setAttachments(_call);
 try {        java.lang.Object _resp = _call.invoke(new java.lang.Object[] {glycan_id});

        if (_resp instanceof java.rmi.RemoteException) {
            throw (java.rmi.RemoteException)_resp;
        }
        else {
            extractAttachments(_call);
            try {
<<<<<<< HEAD
                return (String[]) _resp;
            } catch (java.lang.Exception _exception) {
                return (String[]) JavaUtils.convert(_resp, String[].class);
=======
                return (java.lang.String[]) _resp;
            } catch (java.lang.Exception _exception) {
                return (java.lang.String[]) JavaUtils.convert(_resp, java.lang.String[].class);
>>>>>>> 2eb394907b98577f1b916408cf22a2de6952b22d
            }
        }
  } catch (org.apache.axis.AxisFault axisFaultException) {
  throw axisFaultException;
}
    }

<<<<<<< HEAD
    public String[] get_enzymes_by_compound(String compound_id) throws java.rmi.RemoteException {
=======
    public java.lang.String[] get_enzymes_by_compound(java.lang.String compound_id) throws java.rmi.RemoteException {
>>>>>>> 2eb394907b98577f1b916408cf22a2de6952b22d
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[44]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("SOAP/KEGG#get_enzymes_by_compound");
        _call.setSOAPVersion(SOAPConstants.SOAP11_CONSTANTS);
<<<<<<< HEAD
        _call.setOperationName(new QName("SOAP/KEGG", "get_enzymes_by_compound"));
=======
        _call.setOperationName(new javax.xml.namespace.QName("SOAP/KEGG", "get_enzymes_by_compound"));
>>>>>>> 2eb394907b98577f1b916408cf22a2de6952b22d

        setRequestHeaders(_call);
        setAttachments(_call);
 try {        java.lang.Object _resp = _call.invoke(new java.lang.Object[] {compound_id});

        if (_resp instanceof java.rmi.RemoteException) {
            throw (java.rmi.RemoteException)_resp;
        }
        else {
            extractAttachments(_call);
            try {
<<<<<<< HEAD
                return (String[]) _resp;
            } catch (java.lang.Exception _exception) {
                return (String[]) JavaUtils.convert(_resp, String[].class);
=======
                return (java.lang.String[]) _resp;
            } catch (java.lang.Exception _exception) {
                return (java.lang.String[]) JavaUtils.convert(_resp, java.lang.String[].class);
>>>>>>> 2eb394907b98577f1b916408cf22a2de6952b22d
            }
        }
  } catch (org.apache.axis.AxisFault axisFaultException) {
  throw axisFaultException;
}
    }

<<<<<<< HEAD
    public String[] get_pathways_by_compounds(String[] compound_id_list) throws java.rmi.RemoteException {
=======
    public java.lang.String[] get_pathways_by_compounds(java.lang.String[] compound_id_list) throws java.rmi.RemoteException {
>>>>>>> 2eb394907b98577f1b916408cf22a2de6952b22d
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[45]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("SOAP/KEGG#get_pathways_by_compounds");
        _call.setSOAPVersion(SOAPConstants.SOAP11_CONSTANTS);
<<<<<<< HEAD
        _call.setOperationName(new QName("SOAP/KEGG", "get_pathways_by_compounds"));
=======
        _call.setOperationName(new javax.xml.namespace.QName("SOAP/KEGG", "get_pathways_by_compounds"));
>>>>>>> 2eb394907b98577f1b916408cf22a2de6952b22d

        setRequestHeaders(_call);
        setAttachments(_call);
 try {        java.lang.Object _resp = _call.invoke(new java.lang.Object[] {compound_id_list});

        if (_resp instanceof java.rmi.RemoteException) {
            throw (java.rmi.RemoteException)_resp;
        }
        else {
            extractAttachments(_call);
            try {
<<<<<<< HEAD
                return (String[]) _resp;
            } catch (java.lang.Exception _exception) {
                return (String[]) JavaUtils.convert(_resp, String[].class);
=======
                return (java.lang.String[]) _resp;
            } catch (java.lang.Exception _exception) {
                return (java.lang.String[]) JavaUtils.convert(_resp, java.lang.String[].class);
>>>>>>> 2eb394907b98577f1b916408cf22a2de6952b22d
            }
        }
  } catch (org.apache.axis.AxisFault axisFaultException) {
  throw axisFaultException;
}
    }

<<<<<<< HEAD
    public String[] get_pathways_by_glycans(String[] glycan_id_list) throws java.rmi.RemoteException {
=======
    public java.lang.String[] get_pathways_by_glycans(java.lang.String[] glycan_id_list) throws java.rmi.RemoteException {
>>>>>>> 2eb394907b98577f1b916408cf22a2de6952b22d
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[46]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("SOAP/KEGG#get_pathways_by_glycans");
        _call.setSOAPVersion(SOAPConstants.SOAP11_CONSTANTS);
<<<<<<< HEAD
        _call.setOperationName(new QName("SOAP/KEGG", "get_pathways_by_glycans"));
=======
        _call.setOperationName(new javax.xml.namespace.QName("SOAP/KEGG", "get_pathways_by_glycans"));
>>>>>>> 2eb394907b98577f1b916408cf22a2de6952b22d

        setRequestHeaders(_call);
        setAttachments(_call);
 try {        java.lang.Object _resp = _call.invoke(new java.lang.Object[] {glycan_id_list});

        if (_resp instanceof java.rmi.RemoteException) {
            throw (java.rmi.RemoteException)_resp;
        }
        else {
            extractAttachments(_call);
            try {
<<<<<<< HEAD
                return (String[]) _resp;
            } catch (java.lang.Exception _exception) {
                return (String[]) JavaUtils.convert(_resp, String[].class);
=======
                return (java.lang.String[]) _resp;
            } catch (java.lang.Exception _exception) {
                return (java.lang.String[]) JavaUtils.convert(_resp, java.lang.String[].class);
>>>>>>> 2eb394907b98577f1b916408cf22a2de6952b22d
            }
        }
  } catch (org.apache.axis.AxisFault axisFaultException) {
  throw axisFaultException;
}
    }

<<<<<<< HEAD
    public String[] get_compounds_by_pathway(String pathway_id) throws java.rmi.RemoteException {
=======
    public java.lang.String[] get_compounds_by_pathway(java.lang.String pathway_id) throws java.rmi.RemoteException {
>>>>>>> 2eb394907b98577f1b916408cf22a2de6952b22d
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[47]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("SOAP/KEGG#get_compounds_by_pathway");
        _call.setSOAPVersion(SOAPConstants.SOAP11_CONSTANTS);
<<<<<<< HEAD
        _call.setOperationName(new QName("SOAP/KEGG", "get_compounds_by_pathway"));
=======
        _call.setOperationName(new javax.xml.namespace.QName("SOAP/KEGG", "get_compounds_by_pathway"));
>>>>>>> 2eb394907b98577f1b916408cf22a2de6952b22d

        setRequestHeaders(_call);
        setAttachments(_call);
 try {        java.lang.Object _resp = _call.invoke(new java.lang.Object[] {pathway_id});

        if (_resp instanceof java.rmi.RemoteException) {
            throw (java.rmi.RemoteException)_resp;
        }
        else {
            extractAttachments(_call);
            try {
<<<<<<< HEAD
                return (String[]) _resp;
            } catch (java.lang.Exception _exception) {
                return (String[]) JavaUtils.convert(_resp, String[].class);
=======
                return (java.lang.String[]) _resp;
            } catch (java.lang.Exception _exception) {
                return (java.lang.String[]) JavaUtils.convert(_resp, java.lang.String[].class);
>>>>>>> 2eb394907b98577f1b916408cf22a2de6952b22d
            }
        }
  } catch (org.apache.axis.AxisFault axisFaultException) {
  throw axisFaultException;
}
    }

<<<<<<< HEAD
    public String[] get_glycans_by_pathway(String pathway_id) throws java.rmi.RemoteException {
=======
    public java.lang.String[] get_glycans_by_pathway(java.lang.String pathway_id) throws java.rmi.RemoteException {
>>>>>>> 2eb394907b98577f1b916408cf22a2de6952b22d
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[48]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("SOAP/KEGG#get_glycans_by_pathway");
        _call.setSOAPVersion(SOAPConstants.SOAP11_CONSTANTS);
<<<<<<< HEAD
        _call.setOperationName(new QName("SOAP/KEGG", "get_glycans_by_pathway"));
=======
        _call.setOperationName(new javax.xml.namespace.QName("SOAP/KEGG", "get_glycans_by_pathway"));
>>>>>>> 2eb394907b98577f1b916408cf22a2de6952b22d

        setRequestHeaders(_call);
        setAttachments(_call);
 try {        java.lang.Object _resp = _call.invoke(new java.lang.Object[] {pathway_id});

        if (_resp instanceof java.rmi.RemoteException) {
            throw (java.rmi.RemoteException)_resp;
        }
        else {
            extractAttachments(_call);
            try {
<<<<<<< HEAD
                return (String[]) _resp;
            } catch (java.lang.Exception _exception) {
                return (String[]) JavaUtils.convert(_resp, String[].class);
=======
                return (java.lang.String[]) _resp;
            } catch (java.lang.Exception _exception) {
                return (java.lang.String[]) JavaUtils.convert(_resp, java.lang.String[].class);
>>>>>>> 2eb394907b98577f1b916408cf22a2de6952b22d
            }
        }
  } catch (org.apache.axis.AxisFault axisFaultException) {
  throw axisFaultException;
}
    }

<<<<<<< HEAD
    public String[] get_compounds_by_reaction(String reaction_id) throws java.rmi.RemoteException {
=======
    public java.lang.String[] get_compounds_by_reaction(java.lang.String reaction_id) throws java.rmi.RemoteException {
>>>>>>> 2eb394907b98577f1b916408cf22a2de6952b22d
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[49]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("SOAP/KEGG#get_compounds_by_reaction");
        _call.setSOAPVersion(SOAPConstants.SOAP11_CONSTANTS);
<<<<<<< HEAD
        _call.setOperationName(new QName("SOAP/KEGG", "get_compounds_by_reaction"));
=======
        _call.setOperationName(new javax.xml.namespace.QName("SOAP/KEGG", "get_compounds_by_reaction"));
>>>>>>> 2eb394907b98577f1b916408cf22a2de6952b22d

        setRequestHeaders(_call);
        setAttachments(_call);
 try {        java.lang.Object _resp = _call.invoke(new java.lang.Object[] {reaction_id});

        if (_resp instanceof java.rmi.RemoteException) {
            throw (java.rmi.RemoteException)_resp;
        }
        else {
            extractAttachments(_call);
            try {
<<<<<<< HEAD
                return (String[]) _resp;
            } catch (java.lang.Exception _exception) {
                return (String[]) JavaUtils.convert(_resp, String[].class);
=======
                return (java.lang.String[]) _resp;
            } catch (java.lang.Exception _exception) {
                return (java.lang.String[]) JavaUtils.convert(_resp, java.lang.String[].class);
>>>>>>> 2eb394907b98577f1b916408cf22a2de6952b22d
            }
        }
  } catch (org.apache.axis.AxisFault axisFaultException) {
  throw axisFaultException;
}
    }

<<<<<<< HEAD
    public String[] get_glycans_by_reaction(String reaction_id) throws java.rmi.RemoteException {
=======
    public java.lang.String[] get_glycans_by_reaction(java.lang.String reaction_id) throws java.rmi.RemoteException {
>>>>>>> 2eb394907b98577f1b916408cf22a2de6952b22d
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[50]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("SOAP/KEGG#get_glycans_by_reaction");
        _call.setSOAPVersion(SOAPConstants.SOAP11_CONSTANTS);
<<<<<<< HEAD
        _call.setOperationName(new QName("SOAP/KEGG", "get_glycans_by_reaction"));
=======
        _call.setOperationName(new javax.xml.namespace.QName("SOAP/KEGG", "get_glycans_by_reaction"));
>>>>>>> 2eb394907b98577f1b916408cf22a2de6952b22d

        setRequestHeaders(_call);
        setAttachments(_call);
 try {        java.lang.Object _resp = _call.invoke(new java.lang.Object[] {reaction_id});

        if (_resp instanceof java.rmi.RemoteException) {
            throw (java.rmi.RemoteException)_resp;
        }
        else {
            extractAttachments(_call);
            try {
<<<<<<< HEAD
                return (String[]) _resp;
            } catch (java.lang.Exception _exception) {
                return (String[]) JavaUtils.convert(_resp, String[].class);
=======
                return (java.lang.String[]) _resp;
            } catch (java.lang.Exception _exception) {
                return (java.lang.String[]) JavaUtils.convert(_resp, java.lang.String[].class);
>>>>>>> 2eb394907b98577f1b916408cf22a2de6952b22d
            }
        }
  } catch (org.apache.axis.AxisFault axisFaultException) {
  throw axisFaultException;
}
    }

<<<<<<< HEAD
    public String[] get_compounds_by_enzyme(String enzyme_id) throws java.rmi.RemoteException {
=======
    public java.lang.String[] get_compounds_by_enzyme(java.lang.String enzyme_id) throws java.rmi.RemoteException {
>>>>>>> 2eb394907b98577f1b916408cf22a2de6952b22d
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[51]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("SOAP/KEGG#get_compounds_by_enzyme");
        _call.setSOAPVersion(SOAPConstants.SOAP11_CONSTANTS);
<<<<<<< HEAD
        _call.setOperationName(new QName("SOAP/KEGG", "get_compounds_by_enzyme"));
=======
        _call.setOperationName(new javax.xml.namespace.QName("SOAP/KEGG", "get_compounds_by_enzyme"));
>>>>>>> 2eb394907b98577f1b916408cf22a2de6952b22d

        setRequestHeaders(_call);
        setAttachments(_call);
 try {        java.lang.Object _resp = _call.invoke(new java.lang.Object[] {enzyme_id});

        if (_resp instanceof java.rmi.RemoteException) {
            throw (java.rmi.RemoteException)_resp;
        }
        else {
            extractAttachments(_call);
            try {
<<<<<<< HEAD
                return (String[]) _resp;
            } catch (java.lang.Exception _exception) {
                return (String[]) JavaUtils.convert(_resp, String[].class);
=======
                return (java.lang.String[]) _resp;
            } catch (java.lang.Exception _exception) {
                return (java.lang.String[]) JavaUtils.convert(_resp, java.lang.String[].class);
>>>>>>> 2eb394907b98577f1b916408cf22a2de6952b22d
            }
        }
  } catch (org.apache.axis.AxisFault axisFaultException) {
  throw axisFaultException;
}
    }

<<<<<<< HEAD
    public String[] get_glycans_by_enzyme(String enzyme_id) throws java.rmi.RemoteException {
=======
    public java.lang.String[] get_glycans_by_enzyme(java.lang.String enzyme_id) throws java.rmi.RemoteException {
>>>>>>> 2eb394907b98577f1b916408cf22a2de6952b22d
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[52]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("SOAP/KEGG#get_glycans_by_enzyme");
        _call.setSOAPVersion(SOAPConstants.SOAP11_CONSTANTS);
<<<<<<< HEAD
        _call.setOperationName(new QName("SOAP/KEGG", "get_glycans_by_enzyme"));
=======
        _call.setOperationName(new javax.xml.namespace.QName("SOAP/KEGG", "get_glycans_by_enzyme"));
>>>>>>> 2eb394907b98577f1b916408cf22a2de6952b22d

        setRequestHeaders(_call);
        setAttachments(_call);
 try {        java.lang.Object _resp = _call.invoke(new java.lang.Object[] {enzyme_id});

        if (_resp instanceof java.rmi.RemoteException) {
            throw (java.rmi.RemoteException)_resp;
        }
        else {
            extractAttachments(_call);
            try {
<<<<<<< HEAD
                return (String[]) _resp;
            } catch (java.lang.Exception _exception) {
                return (String[]) JavaUtils.convert(_resp, String[].class);
=======
                return (java.lang.String[]) _resp;
            } catch (java.lang.Exception _exception) {
                return (java.lang.String[]) JavaUtils.convert(_resp, java.lang.String[].class);
>>>>>>> 2eb394907b98577f1b916408cf22a2de6952b22d
            }
        }
  } catch (org.apache.axis.AxisFault axisFaultException) {
  throw axisFaultException;
}
    }

<<<<<<< HEAD
    public String convert_mol_to_kcf(String mol_text) throws java.rmi.RemoteException {
=======
    public java.lang.String convert_mol_to_kcf(java.lang.String mol_text) throws java.rmi.RemoteException {
>>>>>>> 2eb394907b98577f1b916408cf22a2de6952b22d
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[53]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("SOAP/KEGG#convert_mol_to_kcf");
        _call.setSOAPVersion(SOAPConstants.SOAP11_CONSTANTS);
<<<<<<< HEAD
        _call.setOperationName(new QName("SOAP/KEGG", "convert_mol_to_kcf"));
=======
        _call.setOperationName(new javax.xml.namespace.QName("SOAP/KEGG", "convert_mol_to_kcf"));
>>>>>>> 2eb394907b98577f1b916408cf22a2de6952b22d

        setRequestHeaders(_call);
        setAttachments(_call);
 try {        java.lang.Object _resp = _call.invoke(new java.lang.Object[] {mol_text});

        if (_resp instanceof java.rmi.RemoteException) {
            throw (java.rmi.RemoteException)_resp;
        }
        else {
            extractAttachments(_call);
            try {
<<<<<<< HEAD
                return (String) _resp;
            } catch (java.lang.Exception _exception) {
                return (String) JavaUtils.convert(_resp, String.class);
=======
                return (java.lang.String) _resp;
            } catch (java.lang.Exception _exception) {
                return (java.lang.String) JavaUtils.convert(_resp, java.lang.String.class);
>>>>>>> 2eb394907b98577f1b916408cf22a2de6952b22d
            }
        }
  } catch (org.apache.axis.AxisFault axisFaultException) {
  throw axisFaultException;
}
    }

<<<<<<< HEAD
    public String[] get_kos_by_pathway(String pathway_id) throws java.rmi.RemoteException {
=======
    public java.lang.String[] get_kos_by_pathway(java.lang.String pathway_id) throws java.rmi.RemoteException {
>>>>>>> 2eb394907b98577f1b916408cf22a2de6952b22d
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[54]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("SOAP/KEGG#get_kos_by_pathway");
        _call.setSOAPVersion(SOAPConstants.SOAP11_CONSTANTS);
<<<<<<< HEAD
        _call.setOperationName(new QName("SOAP/KEGG", "get_kos_by_pathway"));
=======
        _call.setOperationName(new javax.xml.namespace.QName("SOAP/KEGG", "get_kos_by_pathway"));
>>>>>>> 2eb394907b98577f1b916408cf22a2de6952b22d

        setRequestHeaders(_call);
        setAttachments(_call);
 try {        java.lang.Object _resp = _call.invoke(new java.lang.Object[] {pathway_id});

        if (_resp instanceof java.rmi.RemoteException) {
            throw (java.rmi.RemoteException)_resp;
        }
        else {
            extractAttachments(_call);
            try {
<<<<<<< HEAD
                return (String[]) _resp;
            } catch (java.lang.Exception _exception) {
                return (String[]) JavaUtils.convert(_resp, String[].class);
=======
                return (java.lang.String[]) _resp;
            } catch (java.lang.Exception _exception) {
                return (java.lang.String[]) JavaUtils.convert(_resp, java.lang.String[].class);
>>>>>>> 2eb394907b98577f1b916408cf22a2de6952b22d
            }
        }
  } catch (org.apache.axis.AxisFault axisFaultException) {
  throw axisFaultException;
}
    }

<<<<<<< HEAD
    public String[] get_pathways_by_kos(String[] ko_id_list, String org) throws java.rmi.RemoteException {
=======
    public java.lang.String[] get_pathways_by_kos(java.lang.String[] ko_id_list, java.lang.String org) throws java.rmi.RemoteException {
>>>>>>> 2eb394907b98577f1b916408cf22a2de6952b22d
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[55]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("SOAP/KEGG#get_pathways_by_kos");
        _call.setSOAPVersion(SOAPConstants.SOAP11_CONSTANTS);
<<<<<<< HEAD
        _call.setOperationName(new QName("SOAP/KEGG", "get_pathways_by_kos"));
=======
        _call.setOperationName(new javax.xml.namespace.QName("SOAP/KEGG", "get_pathways_by_kos"));
>>>>>>> 2eb394907b98577f1b916408cf22a2de6952b22d

        setRequestHeaders(_call);
        setAttachments(_call);
 try {        java.lang.Object _resp = _call.invoke(new java.lang.Object[] {ko_id_list, org});

        if (_resp instanceof java.rmi.RemoteException) {
            throw (java.rmi.RemoteException)_resp;
        }
        else {
            extractAttachments(_call);
            try {
<<<<<<< HEAD
                return (String[]) _resp;
            } catch (java.lang.Exception _exception) {
                return (String[]) JavaUtils.convert(_resp, String[].class);
=======
                return (java.lang.String[]) _resp;
            } catch (java.lang.Exception _exception) {
                return (java.lang.String[]) JavaUtils.convert(_resp, java.lang.String[].class);
>>>>>>> 2eb394907b98577f1b916408cf22a2de6952b22d
            }
        }
  } catch (org.apache.axis.AxisFault axisFaultException) {
  throw axisFaultException;
}
    }

<<<<<<< HEAD
    public String[] search_compounds_by_name(String name) throws java.rmi.RemoteException {
=======
    public java.lang.String[] search_compounds_by_name(java.lang.String name) throws java.rmi.RemoteException {
>>>>>>> 2eb394907b98577f1b916408cf22a2de6952b22d
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[56]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("SOAP/KEGG#search_compounds_by_name");
        _call.setSOAPVersion(SOAPConstants.SOAP11_CONSTANTS);
<<<<<<< HEAD
        _call.setOperationName(new QName("SOAP/KEGG", "search_compounds_by_name"));
=======
        _call.setOperationName(new javax.xml.namespace.QName("SOAP/KEGG", "search_compounds_by_name"));
>>>>>>> 2eb394907b98577f1b916408cf22a2de6952b22d

        setRequestHeaders(_call);
        setAttachments(_call);
 try {        java.lang.Object _resp = _call.invoke(new java.lang.Object[] {name});

        if (_resp instanceof java.rmi.RemoteException) {
            throw (java.rmi.RemoteException)_resp;
        }
        else {
            extractAttachments(_call);
            try {
<<<<<<< HEAD
                return (String[]) _resp;
            } catch (java.lang.Exception _exception) {
                return (String[]) JavaUtils.convert(_resp, String[].class);
=======
                return (java.lang.String[]) _resp;
            } catch (java.lang.Exception _exception) {
                return (java.lang.String[]) JavaUtils.convert(_resp, java.lang.String[].class);
>>>>>>> 2eb394907b98577f1b916408cf22a2de6952b22d
            }
        }
  } catch (org.apache.axis.AxisFault axisFaultException) {
  throw axisFaultException;
}
    }

<<<<<<< HEAD
    public String[] search_glycans_by_name(String name) throws java.rmi.RemoteException {
=======
    public java.lang.String[] search_glycans_by_name(java.lang.String name) throws java.rmi.RemoteException {
>>>>>>> 2eb394907b98577f1b916408cf22a2de6952b22d
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[57]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("SOAP/KEGG#search_glycans_by_name");
        _call.setSOAPVersion(SOAPConstants.SOAP11_CONSTANTS);
<<<<<<< HEAD
        _call.setOperationName(new QName("SOAP/KEGG", "search_glycans_by_name"));
=======
        _call.setOperationName(new javax.xml.namespace.QName("SOAP/KEGG", "search_glycans_by_name"));
>>>>>>> 2eb394907b98577f1b916408cf22a2de6952b22d

        setRequestHeaders(_call);
        setAttachments(_call);
 try {        java.lang.Object _resp = _call.invoke(new java.lang.Object[] {name});

        if (_resp instanceof java.rmi.RemoteException) {
            throw (java.rmi.RemoteException)_resp;
        }
        else {
            extractAttachments(_call);
            try {
<<<<<<< HEAD
                return (String[]) _resp;
            } catch (java.lang.Exception _exception) {
                return (String[]) JavaUtils.convert(_resp, String[].class);
=======
                return (java.lang.String[]) _resp;
            } catch (java.lang.Exception _exception) {
                return (java.lang.String[]) JavaUtils.convert(_resp, java.lang.String[].class);
>>>>>>> 2eb394907b98577f1b916408cf22a2de6952b22d
            }
        }
  } catch (org.apache.axis.AxisFault axisFaultException) {
  throw axisFaultException;
}
    }

<<<<<<< HEAD
    public String[] search_compounds_by_composition(String composition) throws java.rmi.RemoteException {
=======
    public java.lang.String[] search_compounds_by_composition(java.lang.String composition) throws java.rmi.RemoteException {
>>>>>>> 2eb394907b98577f1b916408cf22a2de6952b22d
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[58]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("SOAP/KEGG#search_compounds_by_composition");
        _call.setSOAPVersion(SOAPConstants.SOAP11_CONSTANTS);
<<<<<<< HEAD
        _call.setOperationName(new QName("SOAP/KEGG", "search_compounds_by_composition"));
=======
        _call.setOperationName(new javax.xml.namespace.QName("SOAP/KEGG", "search_compounds_by_composition"));
>>>>>>> 2eb394907b98577f1b916408cf22a2de6952b22d

        setRequestHeaders(_call);
        setAttachments(_call);
 try {        java.lang.Object _resp = _call.invoke(new java.lang.Object[] {composition});

        if (_resp instanceof java.rmi.RemoteException) {
            throw (java.rmi.RemoteException)_resp;
        }
        else {
            extractAttachments(_call);
            try {
<<<<<<< HEAD
                return (String[]) _resp;
            } catch (java.lang.Exception _exception) {
                return (String[]) JavaUtils.convert(_resp, String[].class);
=======
                return (java.lang.String[]) _resp;
            } catch (java.lang.Exception _exception) {
                return (java.lang.String[]) JavaUtils.convert(_resp, java.lang.String[].class);
>>>>>>> 2eb394907b98577f1b916408cf22a2de6952b22d
            }
        }
  } catch (org.apache.axis.AxisFault axisFaultException) {
  throw axisFaultException;
}
    }

<<<<<<< HEAD
    public String[] search_compounds_by_mass(float mass, float range) throws java.rmi.RemoteException {
=======
    public java.lang.String[] search_compounds_by_mass(float mass, float range) throws java.rmi.RemoteException {
>>>>>>> 2eb394907b98577f1b916408cf22a2de6952b22d
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[59]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("SOAP/KEGG#search_compounds_by_mass");
        _call.setSOAPVersion(SOAPConstants.SOAP11_CONSTANTS);
<<<<<<< HEAD
        _call.setOperationName(new QName("SOAP/KEGG", "search_compounds_by_mass"));
=======
        _call.setOperationName(new javax.xml.namespace.QName("SOAP/KEGG", "search_compounds_by_mass"));
>>>>>>> 2eb394907b98577f1b916408cf22a2de6952b22d

        setRequestHeaders(_call);
        setAttachments(_call);
 try {        java.lang.Object _resp = _call.invoke(new java.lang.Object[] {new java.lang.Float(mass), new java.lang.Float(range)});

        if (_resp instanceof java.rmi.RemoteException) {
            throw (java.rmi.RemoteException)_resp;
        }
        else {
            extractAttachments(_call);
            try {
<<<<<<< HEAD
                return (String[]) _resp;
            } catch (java.lang.Exception _exception) {
                return (String[]) JavaUtils.convert(_resp, String[].class);
=======
                return (java.lang.String[]) _resp;
            } catch (java.lang.Exception _exception) {
                return (java.lang.String[]) JavaUtils.convert(_resp, java.lang.String[].class);
>>>>>>> 2eb394907b98577f1b916408cf22a2de6952b22d
            }
        }
  } catch (org.apache.axis.AxisFault axisFaultException) {
  throw axisFaultException;
}
    }

<<<<<<< HEAD
    public String[] search_glycans_by_mass(float mass, float range) throws java.rmi.RemoteException {
=======
    public java.lang.String[] search_glycans_by_mass(float mass, float range) throws java.rmi.RemoteException {
>>>>>>> 2eb394907b98577f1b916408cf22a2de6952b22d
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[60]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("SOAP/KEGG#search_glycans_by_mass");
        _call.setSOAPVersion(SOAPConstants.SOAP11_CONSTANTS);
<<<<<<< HEAD
        _call.setOperationName(new QName("SOAP/KEGG", "search_glycans_by_mass"));
=======
        _call.setOperationName(new javax.xml.namespace.QName("SOAP/KEGG", "search_glycans_by_mass"));
>>>>>>> 2eb394907b98577f1b916408cf22a2de6952b22d

        setRequestHeaders(_call);
        setAttachments(_call);
 try {        java.lang.Object _resp = _call.invoke(new java.lang.Object[] {new java.lang.Float(mass), new java.lang.Float(range)});

        if (_resp instanceof java.rmi.RemoteException) {
            throw (java.rmi.RemoteException)_resp;
        }
        else {
            extractAttachments(_call);
            try {
<<<<<<< HEAD
                return (String[]) _resp;
            } catch (java.lang.Exception _exception) {
                return (String[]) JavaUtils.convert(_resp, String[].class);
=======
                return (java.lang.String[]) _resp;
            } catch (java.lang.Exception _exception) {
                return (java.lang.String[]) JavaUtils.convert(_resp, java.lang.String[].class);
>>>>>>> 2eb394907b98577f1b916408cf22a2de6952b22d
            }
        }
  } catch (org.apache.axis.AxisFault axisFaultException) {
  throw axisFaultException;
}
    }

<<<<<<< HEAD
    public String[] search_glycans_by_composition(String composition) throws java.rmi.RemoteException {
=======
    public java.lang.String[] search_glycans_by_composition(java.lang.String composition) throws java.rmi.RemoteException {
>>>>>>> 2eb394907b98577f1b916408cf22a2de6952b22d
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[61]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("SOAP/KEGG#search_glycans_by_composition");
        _call.setSOAPVersion(SOAPConstants.SOAP11_CONSTANTS);
<<<<<<< HEAD
        _call.setOperationName(new QName("SOAP/KEGG", "search_glycans_by_composition"));
=======
        _call.setOperationName(new javax.xml.namespace.QName("SOAP/KEGG", "search_glycans_by_composition"));
>>>>>>> 2eb394907b98577f1b916408cf22a2de6952b22d

        setRequestHeaders(_call);
        setAttachments(_call);
 try {        java.lang.Object _resp = _call.invoke(new java.lang.Object[] {composition});

        if (_resp instanceof java.rmi.RemoteException) {
            throw (java.rmi.RemoteException)_resp;
        }
        else {
            extractAttachments(_call);
            try {
<<<<<<< HEAD
                return (String[]) _resp;
            } catch (java.lang.Exception _exception) {
                return (String[]) JavaUtils.convert(_resp, String[].class);
=======
                return (java.lang.String[]) _resp;
            } catch (java.lang.Exception _exception) {
                return (java.lang.String[]) JavaUtils.convert(_resp, java.lang.String[].class);
>>>>>>> 2eb394907b98577f1b916408cf22a2de6952b22d
            }
        }
  } catch (org.apache.axis.AxisFault axisFaultException) {
  throw axisFaultException;
}
    }

<<<<<<< HEAD
    public StructureAlignment[] search_compounds_by_subcomp(String mol, int offset, int limit) throws java.rmi.RemoteException {
=======
    public StructureAlignment[] search_compounds_by_subcomp(java.lang.String mol, int offset, int limit) throws java.rmi.RemoteException {
>>>>>>> 2eb394907b98577f1b916408cf22a2de6952b22d
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[62]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("SOAP/KEGG#search_compounds_by_subcomp");
        _call.setSOAPVersion(SOAPConstants.SOAP11_CONSTANTS);
<<<<<<< HEAD
        _call.setOperationName(new QName("SOAP/KEGG", "search_compounds_by_subcomp"));
=======
        _call.setOperationName(new javax.xml.namespace.QName("SOAP/KEGG", "search_compounds_by_subcomp"));
>>>>>>> 2eb394907b98577f1b916408cf22a2de6952b22d

        setRequestHeaders(_call);
        setAttachments(_call);
 try {        java.lang.Object _resp = _call.invoke(new java.lang.Object[] {mol, new java.lang.Integer(offset), new java.lang.Integer(limit)});

        if (_resp instanceof java.rmi.RemoteException) {
            throw (java.rmi.RemoteException)_resp;
        }
        else {
            extractAttachments(_call);
            try {
                return (StructureAlignment[]) _resp;
            } catch (java.lang.Exception _exception) {
                return (StructureAlignment[]) JavaUtils.convert(_resp, StructureAlignment[].class);
            }
        }
  } catch (org.apache.axis.AxisFault axisFaultException) {
  throw axisFaultException;
}
    }

<<<<<<< HEAD
    public StructureAlignment[] search_glycans_by_kcam(String kcf, String program, String option, int offset, int limit) throws java.rmi.RemoteException {
=======
    public StructureAlignment[] search_glycans_by_kcam(java.lang.String kcf, java.lang.String program, java.lang.String option, int offset, int limit) throws java.rmi.RemoteException {
>>>>>>> 2eb394907b98577f1b916408cf22a2de6952b22d
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[63]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("SOAP/KEGG#search_glycans_by_kcam");
        _call.setSOAPVersion(SOAPConstants.SOAP11_CONSTANTS);
<<<<<<< HEAD
        _call.setOperationName(new QName("SOAP/KEGG", "search_glycans_by_kcam"));
=======
        _call.setOperationName(new javax.xml.namespace.QName("SOAP/KEGG", "search_glycans_by_kcam"));
>>>>>>> 2eb394907b98577f1b916408cf22a2de6952b22d

        setRequestHeaders(_call);
        setAttachments(_call);
 try {        java.lang.Object _resp = _call.invoke(new java.lang.Object[] {kcf, program, option, new java.lang.Integer(offset), new java.lang.Integer(limit)});

        if (_resp instanceof java.rmi.RemoteException) {
            throw (java.rmi.RemoteException)_resp;
        }
        else {
            extractAttachments(_call);
            try {
                return (StructureAlignment[]) _resp;
            } catch (java.lang.Exception _exception) {
                return (StructureAlignment[]) JavaUtils.convert(_resp, StructureAlignment[].class);
            }
        }
  } catch (org.apache.axis.AxisFault axisFaultException) {
  throw axisFaultException;
}
    }

<<<<<<< HEAD
    public LinkDBRelation[] get_linkdb_between_databases(String from_db, String to_db, int offset, int limit) throws java.rmi.RemoteException {
=======
    public LinkDBRelation[] get_linkdb_between_databases(java.lang.String from_db, java.lang.String to_db, int offset, int limit) throws java.rmi.RemoteException {
>>>>>>> 2eb394907b98577f1b916408cf22a2de6952b22d
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[64]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("SOAP/KEGG#get_linkdb_between_databases");
        _call.setSOAPVersion(SOAPConstants.SOAP11_CONSTANTS);
<<<<<<< HEAD
        _call.setOperationName(new QName("SOAP/KEGG", "get_linkdb_between_databases"));
=======
        _call.setOperationName(new javax.xml.namespace.QName("SOAP/KEGG", "get_linkdb_between_databases"));
>>>>>>> 2eb394907b98577f1b916408cf22a2de6952b22d

        setRequestHeaders(_call);
        setAttachments(_call);
 try {        java.lang.Object _resp = _call.invoke(new java.lang.Object[] {from_db, to_db, new java.lang.Integer(offset), new java.lang.Integer(limit)});

        if (_resp instanceof java.rmi.RemoteException) {
            throw (java.rmi.RemoteException)_resp;
        }
        else {
            extractAttachments(_call);
            try {
                return (LinkDBRelation[]) _resp;
            } catch (java.lang.Exception _exception) {
                return (LinkDBRelation[]) JavaUtils.convert(_resp, LinkDBRelation[].class);
            }
        }
  } catch (org.apache.axis.AxisFault axisFaultException) {
  throw axisFaultException;
}
    }

<<<<<<< HEAD
    public String[] search_drugs_by_name(String name) throws java.rmi.RemoteException {
=======
    public java.lang.String[] search_drugs_by_name(java.lang.String name) throws java.rmi.RemoteException {
>>>>>>> 2eb394907b98577f1b916408cf22a2de6952b22d
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[65]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("SOAP/KEGG#search_drugs_by_name");
        _call.setSOAPVersion(SOAPConstants.SOAP11_CONSTANTS);
<<<<<<< HEAD
        _call.setOperationName(new QName("SOAP/KEGG", "search_drugs_by_name"));
=======
        _call.setOperationName(new javax.xml.namespace.QName("SOAP/KEGG", "search_drugs_by_name"));
>>>>>>> 2eb394907b98577f1b916408cf22a2de6952b22d

        setRequestHeaders(_call);
        setAttachments(_call);
 try {        java.lang.Object _resp = _call.invoke(new java.lang.Object[] {name});

        if (_resp instanceof java.rmi.RemoteException) {
            throw (java.rmi.RemoteException)_resp;
        }
        else {
            extractAttachments(_call);
            try {
<<<<<<< HEAD
                return (String[]) _resp;
            } catch (java.lang.Exception _exception) {
                return (String[]) JavaUtils.convert(_resp, String[].class);
=======
                return (java.lang.String[]) _resp;
            } catch (java.lang.Exception _exception) {
                return (java.lang.String[]) JavaUtils.convert(_resp, java.lang.String[].class);
>>>>>>> 2eb394907b98577f1b916408cf22a2de6952b22d
            }
        }
  } catch (org.apache.axis.AxisFault axisFaultException) {
  throw axisFaultException;
}
    }

<<<<<<< HEAD
    public String[] search_drugs_by_composition(String composition) throws java.rmi.RemoteException {
=======
    public java.lang.String[] search_drugs_by_composition(java.lang.String composition) throws java.rmi.RemoteException {
>>>>>>> 2eb394907b98577f1b916408cf22a2de6952b22d
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[66]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("SOAP/KEGG#search_drugs_by_composition");
        _call.setSOAPVersion(SOAPConstants.SOAP11_CONSTANTS);
<<<<<<< HEAD
        _call.setOperationName(new QName("SOAP/KEGG", "search_drugs_by_composition"));
=======
        _call.setOperationName(new javax.xml.namespace.QName("SOAP/KEGG", "search_drugs_by_composition"));
>>>>>>> 2eb394907b98577f1b916408cf22a2de6952b22d

        setRequestHeaders(_call);
        setAttachments(_call);
 try {        java.lang.Object _resp = _call.invoke(new java.lang.Object[] {composition});

        if (_resp instanceof java.rmi.RemoteException) {
            throw (java.rmi.RemoteException)_resp;
        }
        else {
            extractAttachments(_call);
            try {
<<<<<<< HEAD
                return (String[]) _resp;
            } catch (java.lang.Exception _exception) {
                return (String[]) JavaUtils.convert(_resp, String[].class);
=======
                return (java.lang.String[]) _resp;
            } catch (java.lang.Exception _exception) {
                return (java.lang.String[]) JavaUtils.convert(_resp, java.lang.String[].class);
>>>>>>> 2eb394907b98577f1b916408cf22a2de6952b22d
            }
        }
  } catch (org.apache.axis.AxisFault axisFaultException) {
  throw axisFaultException;
}
    }

<<<<<<< HEAD
    public String[] search_drugs_by_mass(float mass, float range) throws java.rmi.RemoteException {
=======
    public java.lang.String[] search_drugs_by_mass(float mass, float range) throws java.rmi.RemoteException {
>>>>>>> 2eb394907b98577f1b916408cf22a2de6952b22d
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[67]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("SOAP/KEGG#search_drugs_by_mass");
        _call.setSOAPVersion(SOAPConstants.SOAP11_CONSTANTS);
<<<<<<< HEAD
        _call.setOperationName(new QName("SOAP/KEGG", "search_drugs_by_mass"));
=======
        _call.setOperationName(new javax.xml.namespace.QName("SOAP/KEGG", "search_drugs_by_mass"));
>>>>>>> 2eb394907b98577f1b916408cf22a2de6952b22d

        setRequestHeaders(_call);
        setAttachments(_call);
 try {        java.lang.Object _resp = _call.invoke(new java.lang.Object[] {new java.lang.Float(mass), new java.lang.Float(range)});

        if (_resp instanceof java.rmi.RemoteException) {
            throw (java.rmi.RemoteException)_resp;
        }
        else {
            extractAttachments(_call);
            try {
<<<<<<< HEAD
                return (String[]) _resp;
            } catch (java.lang.Exception _exception) {
                return (String[]) JavaUtils.convert(_resp, String[].class);
=======
                return (java.lang.String[]) _resp;
            } catch (java.lang.Exception _exception) {
                return (java.lang.String[]) JavaUtils.convert(_resp, java.lang.String[].class);
>>>>>>> 2eb394907b98577f1b916408cf22a2de6952b22d
            }
        }
  } catch (org.apache.axis.AxisFault axisFaultException) {
  throw axisFaultException;
}
    }

<<<<<<< HEAD
    public StructureAlignment[] search_drugs_by_subcomp(String mol, int offset, int limit) throws java.rmi.RemoteException {
=======
    public StructureAlignment[] search_drugs_by_subcomp(java.lang.String mol, int offset, int limit) throws java.rmi.RemoteException {
>>>>>>> 2eb394907b98577f1b916408cf22a2de6952b22d
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[68]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("SOAP/KEGG#search_drugs_by_subcomp");
        _call.setSOAPVersion(SOAPConstants.SOAP11_CONSTANTS);
<<<<<<< HEAD
        _call.setOperationName(new QName("SOAP/KEGG", "search_drugs_by_subcomp"));
=======
        _call.setOperationName(new javax.xml.namespace.QName("SOAP/KEGG", "search_drugs_by_subcomp"));
>>>>>>> 2eb394907b98577f1b916408cf22a2de6952b22d

        setRequestHeaders(_call);
        setAttachments(_call);
 try {        java.lang.Object _resp = _call.invoke(new java.lang.Object[] {mol, new java.lang.Integer(offset), new java.lang.Integer(limit)});

        if (_resp instanceof java.rmi.RemoteException) {
            throw (java.rmi.RemoteException)_resp;
        }
        else {
            extractAttachments(_call);
            try {
                return (StructureAlignment[]) _resp;
            } catch (java.lang.Exception _exception) {
                return (StructureAlignment[]) JavaUtils.convert(_resp, StructureAlignment[].class);
            }
        }
  } catch (org.apache.axis.AxisFault axisFaultException) {
  throw axisFaultException;
}
    }

<<<<<<< HEAD
    public int[] get_references_by_pathway(String pathway_id) throws java.rmi.RemoteException {
=======
    public int[] get_references_by_pathway(java.lang.String pathway_id) throws java.rmi.RemoteException {
>>>>>>> 2eb394907b98577f1b916408cf22a2de6952b22d
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[69]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("SOAP/KEGG#get_references_by_pathway");
        _call.setSOAPVersion(SOAPConstants.SOAP11_CONSTANTS);
<<<<<<< HEAD
        _call.setOperationName(new QName("SOAP/KEGG", "get_references_by_pathway"));
=======
        _call.setOperationName(new javax.xml.namespace.QName("SOAP/KEGG", "get_references_by_pathway"));
>>>>>>> 2eb394907b98577f1b916408cf22a2de6952b22d

        setRequestHeaders(_call);
        setAttachments(_call);
 try {        java.lang.Object _resp = _call.invoke(new java.lang.Object[] {pathway_id});

        if (_resp instanceof java.rmi.RemoteException) {
            throw (java.rmi.RemoteException)_resp;
        }
        else {
            extractAttachments(_call);
            try {
                return (int[]) _resp;
            } catch (java.lang.Exception _exception) {
                return (int[]) JavaUtils.convert(_resp, int[].class);
            }
        }
  } catch (org.apache.axis.AxisFault axisFaultException) {
  throw axisFaultException;
}
    }

<<<<<<< HEAD
    public String[] get_drugs_by_pathway(String pathway_id) throws java.rmi.RemoteException {
=======
    public java.lang.String[] get_drugs_by_pathway(java.lang.String pathway_id) throws java.rmi.RemoteException {
>>>>>>> 2eb394907b98577f1b916408cf22a2de6952b22d
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[70]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("SOAP/KEGG#get_drugs_by_pathway");
        _call.setSOAPVersion(SOAPConstants.SOAP11_CONSTANTS);
<<<<<<< HEAD
        _call.setOperationName(new QName("SOAP/KEGG", "get_drugs_by_pathway"));
=======
        _call.setOperationName(new javax.xml.namespace.QName("SOAP/KEGG", "get_drugs_by_pathway"));
>>>>>>> 2eb394907b98577f1b916408cf22a2de6952b22d

        setRequestHeaders(_call);
        setAttachments(_call);
 try {        java.lang.Object _resp = _call.invoke(new java.lang.Object[] {pathway_id});

        if (_resp instanceof java.rmi.RemoteException) {
            throw (java.rmi.RemoteException)_resp;
        }
        else {
            extractAttachments(_call);
            try {
<<<<<<< HEAD
                return (String[]) _resp;
            } catch (java.lang.Exception _exception) {
                return (String[]) JavaUtils.convert(_resp, String[].class);
=======
                return (java.lang.String[]) _resp;
            } catch (java.lang.Exception _exception) {
                return (java.lang.String[]) JavaUtils.convert(_resp, java.lang.String[].class);
>>>>>>> 2eb394907b98577f1b916408cf22a2de6952b22d
            }
        }
  } catch (org.apache.axis.AxisFault axisFaultException) {
  throw axisFaultException;
}
    }

<<<<<<< HEAD
    public String[] get_pathways_by_drugs(String[] drug_id_list) throws java.rmi.RemoteException {
=======
    public java.lang.String[] get_pathways_by_drugs(java.lang.String[] drug_id_list) throws java.rmi.RemoteException {
>>>>>>> 2eb394907b98577f1b916408cf22a2de6952b22d
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[71]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("SOAP/KEGG#get_pathways_by_drugs");
        _call.setSOAPVersion(SOAPConstants.SOAP11_CONSTANTS);
<<<<<<< HEAD
        _call.setOperationName(new QName("SOAP/KEGG", "get_pathways_by_drugs"));
=======
        _call.setOperationName(new javax.xml.namespace.QName("SOAP/KEGG", "get_pathways_by_drugs"));
>>>>>>> 2eb394907b98577f1b916408cf22a2de6952b22d

        setRequestHeaders(_call);
        setAttachments(_call);
 try {        java.lang.Object _resp = _call.invoke(new java.lang.Object[] {drug_id_list});

        if (_resp instanceof java.rmi.RemoteException) {
            throw (java.rmi.RemoteException)_resp;
        }
        else {
            extractAttachments(_call);
            try {
<<<<<<< HEAD
                return (String[]) _resp;
            } catch (java.lang.Exception _exception) {
                return (String[]) JavaUtils.convert(_resp, String[].class);
=======
                return (java.lang.String[]) _resp;
            } catch (java.lang.Exception _exception) {
                return (java.lang.String[]) JavaUtils.convert(_resp, java.lang.String[].class);
>>>>>>> 2eb394907b98577f1b916408cf22a2de6952b22d
            }
        }
  } catch (org.apache.axis.AxisFault axisFaultException) {
  throw axisFaultException;
}
    }

}
