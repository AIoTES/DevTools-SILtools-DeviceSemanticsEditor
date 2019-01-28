package org.activage.views;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedProperty;
import javax.faces.context.FacesContext;

import org.activage.OntologyHandler;
import org.activage.entities.DataProperty;
import org.activage.entities.MyRestriction;
import org.activage.entities.ObjProperty;
import org.activage.entities.OntologyClass;
import org.primefaces.context.RequestContext;
import org.primefaces.event.NodeSelectEvent;
import org.primefaces.model.DefaultTreeNode;
import org.primefaces.model.TreeNode;

import com.hp.hpl.jena.ontology.DatatypeProperty;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.util.ResourceUtils;

public abstract class SemanticsEditor extends RootView{
	
	@ManagedProperty(value="#{ontologyHandler}")
	protected OntologyHandler ontologyHandler;
	
	protected OntologyClass selectedClass;
	
	protected String newClassName;
	protected String newClassComment;
	
	protected String selectedClassName;
	protected String selectedClassComment;
	
	protected List<DataProperty> dataProperties;
	protected List<ObjProperty> objectProperties;
	
	protected String newDatatypePropertyName;
	protected String newDatatypePropertyComment;
	protected String newObjectPropertyName;
	protected String newObjectPropertyComment;
	
	protected String selectedDataPropertyName;
	protected String selectedDataPropertyComment;
	
	protected DataProperty selectedDataProperty;
	protected ObjProperty selectedObjectProperty;
	
	protected TreeNode dataPropertiesRoot;
	protected TreeNode selectedDataPropertyNode;

	
	protected TreeNode objectPropertiesRoot;
	protected TreeNode selectedObjectPropertyNode;
	
	protected List<MyRestriction> selectedClassRestrictions;
	private TreeNode restrictionRoot;
	private TreeNode selectedRestrictionNode;
	
	private TreeNode allPropertiesRoot;
	private TreeNode selectedPropertyNode;
	
	public SemanticsEditor() {
		super();
		
	}

	public void onNodeSelect(NodeSelectEvent event) {
		setSelectedNode(event.getTreeNode());
		updateRestrictions();
    }
	
	public void updateRestrictions(){
		selectedClassRestrictions = new ArrayList<MyRestriction>();
		ontologyHandler.findClassRestrictions(selectedClass.getOntClass(), selectedClassRestrictions, false);
		Collections.sort(selectedClassRestrictions);
		restrictionRoot = new DefaultTreeNode(new MyRestriction(null), null);
    	for (MyRestriction r : selectedClassRestrictions){
    		new DefaultTreeNode(r, restrictionRoot);
    	}
    	selectedRestrictionNode = null;
	}
	
	public boolean isRestrictionEditable(){
		if (selectedClass == null || selectedRestrictionNode == null){
			return false;
		}
		Set<String> importedOntologyUris = ontologyHandler.getImportedOntologyNamespaces();
		for (String uri : importedOntologyUris){
			if (selectedClass.getOntClass().getNameSpace().equals(uri)){
				return false;
			}
		}
		List<MyRestriction> directRestrictions = new ArrayList<MyRestriction>();
		ontologyHandler.findClassRestrictions(selectedClass.getOntClass(), directRestrictions, true);
		boolean contains = directRestrictions.contains((MyRestriction) selectedRestrictionNode.getData());
		return contains;
	}
	
	public boolean isDataPropertyDisabled(){
		return selectedDataProperty == null;
	}
	
	public boolean isObjectPropertyDisabled(){
		return selectedObjectProperty == null;
	}
	
	/**
	 * Executed when the 'save class' button is pressed
	 */
	public abstract void saveClass();
	
	/**
	 * Executed when the 'Add new class' button is pressed
	 */
	public abstract void saveNewClass();
	
	protected void saveDatatypeProperty(String prefix){
		try{
			ontologyHandler.createNewDatatypeProperty(prefix+"#"+newDatatypePropertyName, newDatatypePropertyComment, selectedClass.getUri());
			updateProperties();
			RequestContext.getCurrentInstance().execute("createNewDatatypeProperty.hide();");
		}
		catch (Exception ex){
			FacesMessage message = new FacesMessage(ex.getMessage());
	        FacesContext.getCurrentInstance().addMessage(null, message);
		}
	}
	
	protected void saveObjProperty(String prefix){
		try{
			ontologyHandler.createNewObjectProperty(prefix+"#"+newObjectPropertyName, newObjectPropertyComment, selectedClass.getUri());
			updateProperties();
			RequestContext.getCurrentInstance().execute("createNewObjectProperty.hide();");
		}
		catch (Exception ex){
			FacesMessage message = new FacesMessage(ex.getMessage());
	        FacesContext.getCurrentInstance().addMessage(null, message);
		}
	}
	
	public void addNewObjectProperty(){
		newObjectPropertyName = null;
		newObjectPropertyComment = null;
		RequestContext.getCurrentInstance().execute("createNewObjectProperty.show();");
	}
	
	public void saveDataProperty(){
		System.out.println("Save data property");
		DatatypeProperty prop = selectedDataProperty.getDatatypeProperty();
		if (!selectedDataPropertyName.equals(selectedDataProperty.getName())){	
			ResourceUtils.renameResource(prop, prop.getNameSpace() + selectedDataPropertyName);
		}
		//prop.removeComment(selectedDataProperty.getComment(), null);
		//prop.setComment(selectedDataPropertyComment, null);
		updateProperties();
	}
	
	public void deleteDataProperty(){
		System.out.println("Delete data property");
	}
	
	public void addNewDataProperty(){
		newDatatypePropertyName = null;
		newDatatypePropertyComment = null;
		RequestContext.getCurrentInstance().execute("createNewDatatypeProperty.show();");
	}
	
	public void saveObjectProperty(){
		System.out.println("Save object property");
	}
	
	public void deleteObjectProperty(){
		System.out.println("Delete object property");
	}
	
	public void setSelectedDataPropertyNode(TreeNode selectedDataPropertyNode) {
		this.selectedDataPropertyNode = selectedDataPropertyNode;
		if (selectedDataPropertyNode != null){
			selectedDataProperty = (DataProperty) selectedDataPropertyNode.getData();
			selectedDataPropertyName = selectedDataProperty.getName();
			selectedDataPropertyComment = selectedDataProperty.getComment();
		}
	}

	public void setSelectedObjectPropertyNode(TreeNode selectedObjectPropertyNode) {
		this.selectedObjectPropertyNode = selectedObjectPropertyNode;
		if (selectedObjectPropertyNode != null){
			selectedObjectProperty = (ObjProperty) selectedObjectPropertyNode.getData();
		}
	}
	
	
	/**
	 * Searches all tree nodes and finds the one with the given uri
	 * Then sets this node as selected
	 * @param uri
	 */
	protected void selectTreeNode(String uri){
		if (selectTreeNode(root1.getChildren(), uri)){
			root1.setExpanded(true);
		}
	}
	
	private boolean selectTreeNode(List<TreeNode> children, String uri){
		for (TreeNode node : children){
			OntologyClass deviceClass = (OntologyClass)node.getData();
			if (deviceClass.getUri().equals(uri)){
				setSelectedNode(node);
				node.setSelected(true);
				node.setExpanded(true);
				return true;
			}
			else {
				if (selectTreeNode(node.getChildren(), uri)){
					node.setExpanded(true);
					return true;
				}
			}
		}	
		return false;
	}
	
	public void editRestriction(){
		System.out.println("Edit restriction");
	}
	
	public void deleteRestriction(){
		ontologyHandler.removeRestriction(selectedClass.getOntClass(), ((MyRestriction) selectedRestrictionNode.getData()).getRestriction());
		updateRestrictions();
	}
	
	public void addRestriction(){
		RequestContext.getCurrentInstance().execute("createNewRestriction.show();");	
	}
	
	public void saveNewRestriction(){
		System.out.println("Save new restriction");
	}
	
	/**
	 * Action executed when 'Delete class' button is clicked
	 * Searches for the selected class in the class tree and deletes it
	 * Sets as selected class the parent class
	 * Updates also the ontology
	 */
	public void deleteClass(){
		TreeNode parentOfselectedNode = selectedNode.getParent();
		ontologyHandler.deleteClass(selectedClass.getUri());
		deleteTreeNode(root1.getChildren().iterator(), root1);
		setSelectedNode(parentOfselectedNode);
		selectTreeNode(selectedClass.getUri());
	}
	
	private void deleteTreeNode(Iterator<TreeNode> it, TreeNode parent){
		while (it.hasNext()){
			TreeNode node = it.next();
			if (((OntologyClass)node.getData()).getUri().equals(selectedClass.getUri())){
				it.remove();
				return;
			}	
			deleteTreeNode(node.getChildren().iterator(), node);
		}
	}
	
	/**
	 * Action executed when 'Add new subclass' button is clicked
	 */
	public void addSubClass(){
		newClassName = null;
		newClassComment = null;
		RequestContext.getCurrentInstance().execute("createNewSubclass.show();");
	}


	public void setSelectedNode(TreeNode selectedNode) {
		super.selectedNode = selectedNode;
		selectedClass = (OntologyClass) selectedNode.getData();
		ontologyHandler.findClassRestrictions(selectedClass.getOntClass());
		selectedClassName = selectedClass.getName();
		selectedClassComment = selectedClass.getComment();
	}

	protected boolean isEditClassDisabled(){
		if (selectedNode == null) return true;
		OntologyClass selectedOntologyClass = (OntologyClass) selectedNode.getData();
		Resource isDefinedBy = selectedOntologyClass.getOntClass().getIsDefinedBy();
		if (isDefinedBy != null) return true;
		return false;
	}
	
	protected boolean isDeleteClassDisabled(){
		if (selectedNode == null) return true;
		OntologyClass selectedOntologyClass = (OntologyClass) selectedNode.getData();
		Resource isDefinedBy = selectedOntologyClass.getOntClass().getIsDefinedBy();
		if (isDefinedBy != null) return true;
		return false;
	}

	protected void updateProperties(){
		dataProperties = ontologyHandler.getDataPropertiesOfClass(selectedClass.getUri());
		Collections.sort(dataProperties);
		objectProperties = ontologyHandler.getObjectPropertiesOfClass(selectedClass.getUri());
		Collections.sort(objectProperties);
		
		dataPropertiesRoot = new DefaultTreeNode(new DataProperty(""), null);
    	for (DataProperty dp : dataProperties){
    		new DefaultTreeNode(dp, dataPropertiesRoot);
    	}
    	selectedDataPropertyNode = null;
    	selectedDataProperty = null;
    	selectedDataPropertyName = null;
    	selectedDataPropertyComment = null;
    	
		objectPropertiesRoot = new DefaultTreeNode(new ObjProperty(""), null);
    	for (ObjProperty op : objectProperties){
    		new DefaultTreeNode(op, objectPropertiesRoot);
    	}
    	selectedObjectPropertyNode = null;
    	selectedObjectProperty = null;
	}
	
	public String getNewClassName() {
		return newClassName;
	}

	public String getNewClassComment() {
		return newClassComment;
	}

	public void setNewClassName(String newClassName) {
		this.newClassName = newClassName;
	}

	public void setNewClassComment(String newClassComment) {
		this.newClassComment = newClassComment;
	}

	public OntologyClass getSelectedClass() {
		return selectedClass;
	}

	public String getSelectedClassName() {
		return selectedClassName;
	}

	public void setSelectedClassName(String selectedClassName) {
		this.selectedClassName = selectedClassName;
	}

	public String getSelectedClassComment() {
		return selectedClassComment;
	}

	public void setSelectedClassComment(String selectedClassComment) {
		this.selectedClassComment = selectedClassComment;
	}
	
	public List<DataProperty> getDataProperties() {
		return dataProperties;
	}

	public List<ObjProperty> getObjectProperties() {
		return objectProperties;
	}

	public String getNewDatatypePropertyName() {
		return newDatatypePropertyName;
	}

	public void setNewDatatypePropertyName(String newDatatypePropertyName) {
		this.newDatatypePropertyName = newDatatypePropertyName;
	}

	public String getNewDatatypePropertyComment() {
		return newDatatypePropertyComment;
	}

	public void setNewDatatypePropertyComment(String newDatatypePropertyComment) {
		this.newDatatypePropertyComment = newDatatypePropertyComment;
	}

	public String getNewObjectPropertyName() {
		return newObjectPropertyName;
	}

	public void setNewObjectPropertyName(String newObjectPropertyName) {
		this.newObjectPropertyName = newObjectPropertyName;
	}

	public String getNewObjectPropertyComment() {
		return newObjectPropertyComment;
	}

	public void setNewObjectPropertyComment(String newObjectPropertyComment) {
		this.newObjectPropertyComment = newObjectPropertyComment;
	}
	
	public String getSelectedDataPropertyName(){
		return selectedDataPropertyName;
	}
	
	public void setSelectedDataPropertyName(String name){
		selectedDataPropertyName = name;
	}
	
	public String getSelectedDataPropertyComment(){
		return selectedDataPropertyComment;
	}
	
	public void setSelectedDataPropertyComment(String comment){
		selectedDataPropertyComment = comment;
	}

	public DataProperty getSelectedDataProperty() {
		return selectedDataProperty;
	}

	public void setSelectedDataProperty(DataProperty selectedDataProperty) {
		this.selectedDataProperty = selectedDataProperty;
	}

	public ObjProperty getSelectedObjectProperty() {
		return selectedObjectProperty;
	}

	public void setSelectedObjectProperty(ObjProperty selectedObjectProperty) {
		this.selectedObjectProperty = selectedObjectProperty;
	}
	
	public String getSelectedDataPropertyUri(){
		if (selectedDataProperty == null) return null;
		return selectedDataProperty.getUri();
	}
	
	public String getSelectedObjectPropertyName(){
		if (selectedObjectProperty == null) return null;
		return selectedObjectProperty.getName();
	}

	public String getSelectedObjectPropertyUri(){
		if (selectedObjectProperty == null) return null;
		return selectedObjectProperty.getUri();
	}
	
	public String getSelectedObjectPropertyComment(){
		if (selectedObjectProperty == null) return null;
		return selectedObjectProperty.getComment();
	}
	
	public void setSelectedObjectPropertyName(String name){
		if (selectedObjectProperty != null){
			selectedObjectProperty.setName(name);
		}
	}
	
	public void setSelectedObjectPropertyComment(String comment){
		if (selectedObjectProperty != null){
			selectedObjectProperty.setComment(comment);
		}
	}
	
	public TreeNode getDataPropertiesRoot() {
		return dataPropertiesRoot;
	}

	public TreeNode getObjectPropertiesRoot() {
		return objectPropertiesRoot;
	}

	public TreeNode getSelectedDataPropertyNode() {
		return selectedDataPropertyNode;
	}

	public TreeNode getSelectedObjectPropertyNode() {
		return selectedObjectPropertyNode;
	}

	public OntologyHandler getOntologyHandler() {
		return ontologyHandler;
	}

	public void setOntologyHandler(OntologyHandler ontologyHandler) {
		this.ontologyHandler = ontologyHandler;
	}

	public List<MyRestriction> getSelectedClassRestrictions() {
		return selectedClassRestrictions;
	}

	public void setSelectedClassRestrictions(
			List<MyRestriction> selectedClassRestrictions) {
		this.selectedClassRestrictions = selectedClassRestrictions;
	}

	public TreeNode getRestrictionRoot() {
		return restrictionRoot;
	}

	public void setRestrictionRoot(TreeNode restrictionRoot) {
		this.restrictionRoot = restrictionRoot;
	}

	public TreeNode getSelectedRestrictionNode() {
		return selectedRestrictionNode;
	}

	public void setSelectedRestrictionNode(TreeNode selectedRestrictionNode) {
		this.selectedRestrictionNode = selectedRestrictionNode;
	}

	public TreeNode getAllPropertiesRoot() {
		return allPropertiesRoot;
	}

	public void setAllPropertiesRoot(TreeNode allPropertiesRoot) {
		this.allPropertiesRoot = allPropertiesRoot;
	}

	public TreeNode getSelectedPropertyNode() {
		return selectedPropertyNode;
	}

	public void setSelectedPropertyNode(TreeNode selectedPropertyNode) {
		this.selectedPropertyNode = selectedPropertyNode;
	}
}
