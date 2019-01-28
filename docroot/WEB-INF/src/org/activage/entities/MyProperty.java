package org.activage.entities;

public class MyProperty extends AbsResource implements Comparable<MyProperty>{
	
	protected String uri;
	protected String comment;

	public MyProperty(String uri, String comment) {
		super();
		this.uri = uri;
		this.comment = comment;
	}

	@Override
	public String getUri() {
		return uri;
	}

	public String getComment() {
		return comment;
	}
	
	@Override
	public int compareTo(MyProperty o) {
		return name.compareTo(o.name);
	}

	@Override
	public String toString() {
		return name;
	}
	
	
}
