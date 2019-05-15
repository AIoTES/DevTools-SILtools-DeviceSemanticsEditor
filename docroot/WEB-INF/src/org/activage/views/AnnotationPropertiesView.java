package org.activage.views;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;

import org.activage.entities.AnnProperty;
import org.primefaces.event.NodeSelectEvent; 
import org.primefaces.model.StreamedContent;

 
@ManagedBean(name = "annotationPropertiesView")
@ViewScoped
/**
 * 
 * @author stavrotheodoros
 * 
 * Bean for the annotationProperties.xhtml
 *
 */
public class AnnotationPropertiesView extends RootView{

	@PostConstruct
	public void init() {
		root1 = ontologyHandler.createAnnotationPropertyTreeNode();
	}
	
	@Override
	public void onNodeSelect(NodeSelectEvent event) {
		AnnProperty selectedProperty = (AnnProperty) event.getTreeNode().getData();
		selectedNodeName = selectedProperty.getName();
		selectedNodeUri = selectedProperty.getUri();
    }
	
	public void openDescription(){
		super.openDescription(selectedNodeUri);
	}
	
	public StreamedContent download(){
		return super.download(selectedNodeUri, selectedNodeName);
	}
}
