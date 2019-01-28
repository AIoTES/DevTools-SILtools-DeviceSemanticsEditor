package org.activage.views.helper_entities;

public class AnnotationItem implements Comparable<AnnotationItem>{
	
	private String label;
	private String value;
	
	public AnnotationItem(String label, String value) {
		super();
		this.label = label;
		this.value = value;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	@Override
	public int compareTo(AnnotationItem o) {
		return label.compareTo(o.label);
	}

	@Override
	public String toString() {
		return "AnnotationItem [label=" + label + ", value=" + value + "]";
	}
}
