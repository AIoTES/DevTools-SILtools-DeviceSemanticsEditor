package org.activage.utils;

import java.util.List;

import org.activage.entities.interfaces.Uriable;

import com.hp.hpl.jena.ontology.OntProperty;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;

public class Utils {
	
	/**
	 * Method that returns the names of the super classes concatenated with comma
	 * @return
	 */
	public static String getSuperProperties(ExtendedIterator<? extends OntProperty> j, String name, String root){
		StringBuilder s = new StringBuilder();
        while (j.hasNext()) {
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
        if (name.equals(root)){
        	return "";
        }
        return root;
	}
	
	/**
	 * searches in a collection of uriables objects, an instance with a specific uri
	 * if not found returns null
	 */
	public static Uriable searchByUri(List<? extends Uriable> collection, String uri){
		for (Uriable p : collection){
			if (p.getUri().equals(uri)){
				return p;
			}
		}
		return null;
	}

}
