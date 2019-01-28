package org.activage.controller;

import java.util.List;
import java.util.Map;

import javax.faces.bean.ApplicationScoped;
import javax.faces.bean.ManagedBean;

import org.activage.OntologyHandler;
import org.activage.entities.DataProperty;
import org.activage.entities.ObjProperty;
import org.activage.entities.OntologyClass;

import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.ontology.OntResource;

@ManagedBean(name = "serviceManager")
@ApplicationScoped
public class ServiceManager {
	
	private OntologyHandler ontologyHandler;
	
	/**
	 * Parses the ontology and creates the equivalent data model for the D3 visualization
	 * @return
	 */
	public String createD3DataModel(){
		return ontologyHandler.getD3Model();
	}
	
	/**
	 * from the given property, returns the classes that are its range
	 * @param propertyURI: the URI of the property
	 */
	public List<OntologyClass> findPropertyRangeClasses(String propertyURI){
		return ontologyHandler.findPropertyRangeClasses(propertyURI);
	}
	
	/**
	 * from the given property, returns the classes that are its domain
	 * @param propertyUri: the URI of the property
	 */
	public List<OntologyClass> findDomainClasses(String propertyUri){
		return ontologyHandler.findDomainClasses(propertyUri);
	}
	
	/**
	 * Finds and returns the class off the given individual
	 */
	public OntClass findClassOfIndivindual(String uri) {
		return ontologyHandler.findClassOfIndivindual(uri);
	}
	
	/**
	 * Deletes a class from our ontology
	 * @param uri: the uri of the class to be deleted
	 */
	public void deleteClass(String uri) {
		ontologyHandler.deleteClass(uri);
	}
	
	/**
	 * Creates a new class in the ontology by using the given info
	 * @param uri: the uri of the class we want to create
	 * @param comment: a comment that explains the class
	 * @param parentURI: the uri of the parent class
	 * @return the created ontology class
	 * @throws Exception
	 */
	public OntClass createNewClass(String uri, String comment, String parentURI) throws Exception {
		return ontologyHandler.createNewClass(uri, comment, parentURI);
	}
	
	/**
	 * Finds and returns all object properties that has as domain the given class
	 * @param classURI : the uri of the class
	 */
	public List<ObjProperty> getExclusiveObjectPropertiesOfClass(String classURI){
		return ontologyHandler.getExclusiveObjectPropertiesOfClass(classURI);
	}
	
	/**
	 * Finds and returns all object properties that has as domain the given class or have no specific domain at all
	 * @param classURI : the uri of the class
	 */
	public List<ObjProperty> getObjectPropertiesOfClass(String classURI){
		return ontologyHandler.getObjectPropertiesOfClass(classURI);
	}
	
	/**
	 * This method uses the given resource in order to find all its annotation properties and the corresponding values
	 * @param ontResource
	 * @return a map of annotation properties-values
	 */
	public Map<String, String> getAnnotationPropertyValues(OntResource ontResource){
		return ontologyHandler.getAnnotationPropertyValues(ontResource);
	}
	
	/**
	 * Finds and returns all datatype properties that has as domain the given class
	 * @param classURI : the uri of the class
	 */
	public List<DataProperty> getExclusiveDataPropertiesOfClass(String classURI) {
		return ontologyHandler.getExclusiveDataPropertiesOfClass(classURI);
	}
	
	/**
	 * Finds and returns all datatype properties that has as domain the given class or have no specific domain at all
	 * @param classURI : the uri of the class
	 */
	public List<DataProperty> getDataPropertiesOfClass(String classURI) {
		return ontologyHandler.getDataPropertiesOfClass(classURI);
	}

	public OntologyHandler getOntologyHandler() {
		return ontologyHandler;
	}

	public void setOntologyHandler(OntologyHandler ontologyHandler) {
		this.ontologyHandler = ontologyHandler;
	}
}
