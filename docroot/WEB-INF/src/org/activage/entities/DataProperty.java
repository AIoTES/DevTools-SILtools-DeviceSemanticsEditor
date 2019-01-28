package org.activage.entities;

import java.util.List;

import org.activage.utils.Utils;

import com.hp.hpl.jena.ontology.DatatypeProperty;
/**
 * 
 * @author stavrotheodoros
 * 
 *  a DatatypeProperty wrapper
 *  
 */
public class DataProperty extends MyProperty{
	
	private DatatypeProperty datatypeProperty;
	public static final String ROOT = "DatatypeProperty";
	private List<OntologyClass> selectedNodeDomains;
	private List<String> selectedNodeRanges;


	public DataProperty(DatatypeProperty datatypeProperty) {
		super(datatypeProperty.getURI(), datatypeProperty.getComment(null));
		this.datatypeProperty = datatypeProperty;
		this.name = datatypeProperty.getLocalName();
	}
	
	public DataProperty(String name) {
		super("", "");
		this.name = name;
	}
	
	public void setComment(String comment){
		datatypeProperty.setComment(comment, null);
	}
	
	public void setName(String name){
		//TODO: MUST IMPLEMENT THIS
		//ResourceUtils.renameResource
	}

	/**
	 * Method that returns the names of the super classes concatenated with comma
	 * @return
	 */
	public String getSuperProperties(){
		if (datatypeProperty == null) return "";
		return Utils.getSuperProperties(datatypeProperty.listSuperProperties(true), name, ROOT);
	}

	public DatatypeProperty getDatatypeProperty() {
		return datatypeProperty;
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

	public List<String> getSelectedNodeRanges() {
		return selectedNodeRanges;
	}

	public void setSelectedNodeRanges(List<String> selectedNodeRanges) {
		this.selectedNodeRanges = selectedNodeRanges;
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
		DataProperty other = (DataProperty) obj;
		if (getUri() == null) {
			if (other.getUri() != null)
				return false;
		} else if (!getUri().equals(other.getUri()))
			return false;
		return true;
	}
}
