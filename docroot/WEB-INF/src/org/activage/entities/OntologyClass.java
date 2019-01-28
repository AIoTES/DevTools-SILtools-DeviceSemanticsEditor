package org.activage.entities;

import java.util.ArrayList;
import java.util.List;

import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.ontology.OntResource;
import com.hp.hpl.jena.ontology.impl.IndividualImpl;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;

public class OntologyClass extends AbsResource implements Comparable<OntologyClass>{
	
	private OntClass ontClass;
	private List<String> parentNames;

	public OntologyClass(OntClass ontClass, List<String> parentNames) {
		super();
		this.ontClass = ontClass;
		this.name = ontClass.getLocalName();
		this.parentNames = parentNames;
	}
	
	public OntologyClass(String name, List<String> parentNames) {
		super();
		this.name = name;
		this.parentNames = parentNames;
	}
	
	public OntClass getOntClass() {
		return ontClass;
	}

	/**
	 * returns all the instances of this class
	 * @return
	 */
	public List<Instance> listInstances(){
		List<Instance> instances = new ArrayList<Instance>();
		if (ontClass != null){
			for (ExtendedIterator<? extends OntResource> i =  ontClass.listInstances(true); i.hasNext();) {
				IndividualImpl instance = (IndividualImpl) i.next();
				if (instance.getLocalName() != null){
					instances.add(new Instance(instance));
				}
			}		
		}
		return instances;
	}
	
	public String getComment(){
		if (ontClass == null) return "";
		return ontClass.getComment(null);
	}
	
	@Override
	public String getUri(){
		if (ontClass == null) return "";
		return ontClass.getURI();
	}

	public List<String> getParentNames() {
		return parentNames;
	}

	@Override
	public String toString() {
		return name;
	}
	
	@Override
	public int compareTo(OntologyClass o) {
		return name.compareTo(o.name);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((getUri() == null) ? 0 : getUri().hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		OntologyClass other = (OntologyClass) obj;
		if (getUri() == null) {
			if (other.getUri() != null)
				return false;
		} else if (!getUri().equals(other.getUri()))
			return false;
		return true;
	}
}
