package org.activage.entities;

import com.hp.hpl.jena.ontology.AnnotationProperty;
import com.hp.hpl.jena.ontology.OntProperty;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;
/**
 * 
 * @author stavrotheodoros
 * 
 *  an AnnotationProperty wrapper
 *  
 */
public class AnnProperty extends AbsResource implements Comparable<AnnProperty>{
	
	private AnnotationProperty annotationProperty;
	public static final String ROOT = "AnnotationProperty";


	public AnnProperty(AnnotationProperty annotationProperty) {
		super();
		this.annotationProperty = annotationProperty;
		this.name = annotationProperty.getLocalName();
	}
	
	public AnnProperty(String name) {
		super();
		this.name = name;
	}
	
	@Override
	public String getUri(){
		if (annotationProperty == null) return "";
		return annotationProperty.getURI();
	}

	public AnnotationProperty getAnnotationProperty() {
		return annotationProperty;
	}

	/**
	 * Method that returns the names of the super classes concatenated with comma
	 * @return
	 */
	public String getSuperProperties(){
		if (annotationProperty == null) return "";
		StringBuilder s = new StringBuilder();
        for (ExtendedIterator<? extends OntProperty> j =  annotationProperty.listSuperProperties(true); j.hasNext();) {
        	OntProperty parent = j.next();
	         String parentName = parent.getLocalName();
	         if (parentName != null){
	        	 s.append(parentName+",");
	         }
	    }
        if (s.length() > 0){
        	s.setLength(s.length() - 1);
        	return s.toString();
        }
        if (name.equals(ROOT)){
        	return "";
        }
        return ROOT;
	}

	@Override
	public String toString() {
		return name;
	}
	
	@Override
	public int compareTo(AnnProperty o) {
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
		AnnProperty other = (AnnProperty) obj;
		if (getUri() == null) {
			if (other.getUri() != null)
				return false;
		} else if (!getUri().equals(other.getUri()))
			return false;
		return true;
	}
}
