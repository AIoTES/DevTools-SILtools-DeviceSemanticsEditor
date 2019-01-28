package org.activage.entities;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.activage.entities.interfaces.Uriable;

import com.hp.hpl.jena.datatypes.RDFDatatype;
import com.hp.hpl.jena.datatypes.xsd.XSDDatatype;
import com.hp.hpl.jena.ontology.impl.IndividualImpl;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;

public class Instance implements Comparable<Instance>, Uriable{
	
	private IndividualImpl indivindual;

	public Instance(IndividualImpl indivindual) {
		super();
		this.indivindual = indivindual;
	}
	
	public Map<String, List<String>> getProperties(){
		Map<String, List<String>> propertiesValues = new HashMap<String, List<String>>();
		StmtIterator it = indivindual.listProperties();
		while (it.hasNext()){
			Statement stmt = it.next();
			Property prop = stmt.getPredicate();
			RDFNode rdfNode = stmt.getObject();
			if (!prop.getLocalName().equals("type")){
				String propertyName = prop.getLocalName();
				String value = "";
				if (rdfNode.isLiteral()){
					value = parseLiteralToValueNode(rdfNode.asLiteral());
				}
				else if (rdfNode.isResource()){
					value = rdfNode.asResource().getURI();
				}
				List<String> values = propertiesValues.get(propertyName);
				if (values == null){
					values = new ArrayList<String>();
				}
				values.add(value);
				propertiesValues.put(propertyName, values);
			}
		}
		return propertiesValues;
	}
	
	private String parseLiteralToValueNode(Literal literal) {
		RDFDatatype datatype = literal.getDatatype();
		if (datatype != null) {
			if (datatype.equals(XSDDatatype.XSDboolean)) {
				return String.valueOf(literal.getBoolean());
			}

			if (datatype.equals(XSDDatatype.XSDint) || datatype.equals(XSDDatatype.XSDinteger)) {
				return String.valueOf(literal.getInt());
			}

			if (datatype.equals(XSDDatatype.XSDlong)) {
				return String.valueOf(literal.getLong());
			}

			if (datatype.equals(XSDDatatype.XSDfloat)) {
				return String.valueOf(literal.getFloat());
			}

			if (datatype.equals(XSDDatatype.XSDdouble)) {
				return String.valueOf(literal.getDouble());
			}
			return literal.getString();
		}
		return literal.getValue().toString();
	}
	
	public String getName() {
		return indivindual.getLocalName();
	}
	
	@Override
	public String getUri(){
		return indivindual.getURI();
	}
	
	public String getComment(){
		return indivindual.getComment(null);
	}

	@Override
	public String toString() {
		return getName();
	}

	@Override
	public int compareTo(Instance o) {
		return getName().compareTo(o.getName());
	}
}
