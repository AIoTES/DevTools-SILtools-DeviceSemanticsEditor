package org.activage.entities;

import org.activage.entities.interfaces.Named;

import com.hp.hpl.jena.ontology.Restriction;

public class MyRestriction implements Named, Comparable<MyRestriction>{
	
	private Restriction restriction;

	public MyRestriction(Restriction restriction) {
		super();
		this.restriction = restriction;
	}

	@Override
	public String getName() {
		return getRestrictionLabel(restriction);
	}
	
	@Override
	public String toString() {
		return "MyRestriction [name=" + getName() + "]";
	}

	public Restriction getRestriction() {
		return restriction;
	}
	
	public String getRestrictionValue(){
		return getName().split(" ")[2];
	}
	
	public String getRestrictionType(){
		return getName().split(" ")[1];
	}
	
	public String getRestrictionPropertyName(){
		return getName().split(" ")[0];
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((getName() == null) ? 0 : getName().hashCode());
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
		MyRestriction other = (MyRestriction) obj;
		if (getName() == null) {
			if (other.getName() != null)
				return false;
		} else if (!getName().equals(other.getName()))
			return false;
		return true;
	}

	/**
	 * Creates a label for a Restriction
	 */
	public static String getRestrictionLabel(Restriction restriction) {
		try {
			String propertyName = restriction.getOnProperty().getLocalName();
			if (restriction.isAllValuesFromRestriction()) {
				return propertyName
						+ " only "
						+ restriction.asAllValuesFromRestriction()
								.getAllValuesFrom().getLocalName();
			} else if (restriction.isMaxCardinalityRestriction()) {
				return propertyName
						+ " max "
						+ restriction.asMaxCardinalityRestriction()
								.getMaxCardinality();
			} else if (restriction.isMinCardinalityRestriction()) {
				return propertyName
						+ " min "
						+ restriction.asMinCardinalityRestriction()
								.getMinCardinality();
			} else if (restriction.isSomeValuesFromRestriction()) {
				return propertyName
						+ " some "
						+ restriction.asSomeValuesFromRestriction()
								.getSomeValuesFrom().getLocalName();
			}
			else if (restriction.isCardinalityRestriction()) {
				return propertyName
						+ " exactly "
						+ restriction.asCardinalityRestriction().getCardinality();
			}
		} catch (Exception e) {
			//e.printStackTrace();
		}
		return null;
	}

	@Override
	public int compareTo(MyRestriction o) {
		return getName().compareTo(o.getName());
	}
}
