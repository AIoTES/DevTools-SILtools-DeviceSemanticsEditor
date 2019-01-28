package org.activage.entities;

import java.util.List;

import org.activage.utils.Utils;

import com.hp.hpl.jena.ontology.ObjectProperty;
import com.hp.hpl.jena.ontology.OntProperty;
import com.hp.hpl.jena.ontology.OntResource;
/**
 * 
 * @author stavrotheodoros
 * 
 *  an ObjProperty wrapper
 *  
 */
public class ObjProperty extends MyProperty {
	
	private ObjectProperty objectProperty;
	public static final String ROOT = "ObjectProperty";
	private List<OntologyClass> selectedNodeDomains;
	private List<OntologyClass> selectedNodeRanges;


	public ObjProperty(ObjectProperty objectProperty) {
		super(objectProperty.getURI(), objectProperty.getComment(null));
		this.objectProperty = objectProperty;
		this.name = objectProperty.getLocalName();
	}
	
	public ObjProperty(String name) {
		super("", "");
		this.name = name;
	}
	
	public String getRange(){
		if (objectProperty == null) return "";
		OntResource range = objectProperty.getRange();
		if (range == null) return "";
		return range.getURI();
	}

	/**
	 * Method that returns the names of the super classes concatenated with comma
	 * @return
	 */
	public String getSuperProperties(){
		if (objectProperty == null) return "";
		return Utils.getSuperProperties(objectProperty.listSuperProperties(true), name, ROOT);
	}
	
	public void setComment(String comment){
		objectProperty.setComment(comment, null);
	}
	
	public void setName(String name){
		//TODO: MUST IMPLEMENT THIS
	}
	
	public ObjProperty getInverse(){
		OntProperty inverse = objectProperty.getInverse();
		if (inverse != null){
			return new ObjProperty((ObjectProperty) inverse);
		}
		return null;
	}

	@Override
	public String toString() {
		return name;
	}

	public List<OntologyClass> getSelectedNodeDomains() {
		return selectedNodeDomains;
	}

	public void setSelectedNodeDomains(List<OntologyClass> selectedNodeDomains) {
		this.selectedNodeDomains = selectedNodeDomains;
	}

	public List<OntologyClass> getSelectedNodeRanges() {
		return selectedNodeRanges;
	}

	public void setSelectedNodeRanges(List<OntologyClass> selectedNodeRanges) {
		this.selectedNodeRanges = selectedNodeRanges;
	}

	public ObjectProperty getObjectProperty() {
		return objectProperty;
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
		ObjProperty other = (ObjProperty) obj;
		if (getUri() == null) {
			if (other.getUri() != null)
				return false;
		} else if (!getUri().equals(other.getUri()))
			return false;
		return true;
	}
	
}
