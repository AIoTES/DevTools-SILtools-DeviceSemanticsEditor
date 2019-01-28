package org.activage.views;

import java.util.List;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;

import org.activage.entities.DataProperty;
import org.activage.entities.OntologyClass;
import org.activage.views.helper_entities.AnnotationItem;
import org.primefaces.context.RequestContext;
import org.primefaces.event.NodeSelectEvent;
 

 
@ManagedBean(name = "dataPropertiesView")
@ViewScoped
/**
 * 
 * @author stavrotheodoros
 * 
 * Bean for the dataProperties.xhtml
 *
 */
public class DataPropertiesView extends RootView{
	
	private OntologyClass selectedClass;
	private List<OntologyClass> selectedNodeDomains;
	private List<String> selectedNodeRanges;
	private List<AnnotationItem> selectedDataPropertyAnnotationItems;
	
	@PostConstruct
	public void init() {
		root1 = ontologyHandler.createDataPropertyTreeNode();
	}
	
	@Override
	public void onNodeSelect(NodeSelectEvent event) {
		DataProperty selectedProperty = (DataProperty) event.getTreeNode().getData();
		selectedNodeName = selectedProperty.getName();
		selectedNodeUri = selectedProperty.getUri();
		selectedNodeDomains = ontologyHandler.findDomainClasses(selectedNodeUri);
		selectedNodeRanges = ontologyHandler.findPropertyRanges(selectedNodeUri);
		selectedDataPropertyAnnotationItems = ontologyHandler.getOrderedAnnotationPairs(selectedProperty.getDatatypeProperty());
    }
	
	public void selectClass(String classURI){
		selectedClass = ontologyHandler.getOntologyClassByURI(classURI);
		RequestContext.getCurrentInstance().execute("classDialog.show();");
	}
	
	public OntologyClass getSelectedClass() {
		return selectedClass;
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

	public List<AnnotationItem> getSelectedDataPropertyAnnotationItems() {
		return selectedDataPropertyAnnotationItems;
	}

	public void setSelectedDataPropertyAnnotationItems(
			List<AnnotationItem> selectedDataPropertyAnnotationItems) {
		this.selectedDataPropertyAnnotationItems = selectedDataPropertyAnnotationItems;
	}
}
