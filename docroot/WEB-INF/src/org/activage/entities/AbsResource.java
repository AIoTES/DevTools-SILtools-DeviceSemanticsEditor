package org.activage.entities;

import org.activage.entities.interfaces.Named;
import org.activage.entities.interfaces.Uriable;
/**
 * 
 * @author stavrotheodoros
 *
 * Represents an abstract ontology resource that has a name and a URI
 * 
 */
public abstract class AbsResource implements Named, Uriable{
	
	protected String name;
	
	@Override
	public String getName() {
		return name;
	}
}
