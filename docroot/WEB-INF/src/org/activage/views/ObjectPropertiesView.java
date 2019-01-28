package org.activage.views;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;

import org.activage.entities.ObjProperty;
import org.activage.entities.OntologyClass;
import org.activage.views.helper_entities.AnnotationItem;
import org.primefaces.context.RequestContext;
import org.primefaces.event.NodeSelectEvent;
 

 
@ManagedBean(name = "objectPropertiesView")
@ViewScoped
/**
 * 
 * @author stavrotheodoros
 *
 * Bean for the objectProperties.xhtml
 */
public class ObjectPropertiesView extends RootView{
	
	private OntologyClass selectedClass;
	private ObjProperty selectedProperty;
	Map<String, String> annotationPropertyValues;
	List<String> annotationProperties;
	private List<AnnotationItem> selectedObjectPropertyAnnotationItems;

	
	@PostConstruct
	public void init() {
		root1 = ontologyHandler.createObjectPropertyTreeNode();
	}
	
	@Override
	public void onNodeSelect(NodeSelectEvent event) {
		selectedProperty = (ObjProperty) event.getTreeNode().getData();
		selectedProperty.setSelectedNodeDomains(ontologyHandler.findDomainClasses(selectedProperty.getUri()));
		selectedProperty.setSelectedNodeRanges(ontologyHandler.findPropertyRangeClasses(selectedProperty.getUri()));
		annotationPropertyValues = ontologyHandler.getAnnotationPropertyValues(selectedProperty.getObjectProperty());
		annotationProperties = new ArrayList<String>(annotationPropertyValues.keySet());
		Collections.sort(annotationProperties);
		selectedObjectPropertyAnnotationItems = ontologyHandler.getOrderedAnnotationPairs(selectedProperty.getObjectProperty());
    }
	
	public void selectClass(String classURI){
		selectedClass = ontologyHandler.getOntologyClassByURI(classURI);
		RequestContext.getCurrentInstance().execute("classDialog.show();");
	}
	
	public OntologyClass getSelectedClass() {
		return selectedClass;
	}

	public ObjProperty getSelectedProperty() {
		return selectedProperty;
	}

	public void setSelectedProperty(ObjProperty selectedProperty) {
		this.selectedProperty = selectedProperty;
	}
	
	public String findAnnotationPropertyValue(String property){
		return annotationPropertyValues.get(property);
	}

	public List<String> getAnnotationProperties() {
		return annotationProperties;
	}

	public List<AnnotationItem> getSelectedObjectPropertyAnnotationItems() {
		return selectedObjectPropertyAnnotationItems;
	}

	public void setSelectedObjectPropertyAnnotationItems(
			List<AnnotationItem> selectedObjectPropertyAnnotationItems) {
		this.selectedObjectPropertyAnnotationItems = selectedObjectPropertyAnnotationItems;
	}
	
}
