package org.activage.views;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import javax.portlet.PortletResponse;
import javax.servlet.http.HttpServletResponse;

import org.activage.OntologyHandler;
import org.activage.entities.DataProperty;
import org.activage.entities.Instance;
import org.activage.entities.MyProperty;
import org.activage.entities.MyRestriction;
import org.activage.entities.ObjProperty;
import org.activage.entities.OntologyClass;
import org.activage.views.helper_entities.AnnotationItem;
import org.primefaces.context.RequestContext;
import org.primefaces.event.NodeSelectEvent;
import org.primefaces.model.StreamedContent;
import org.primefaces.model.TreeNode;

import com.hp.hpl.jena.ontology.DatatypeProperty;
import com.hp.hpl.jena.ontology.ObjectProperty;
import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.ontology.OntProperty;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;
import com.liferay.portal.util.PortalUtil;

@ManagedBean(name = "serviceSemanticsEditorView")
@ViewScoped
/**
 * 
 * @author stavrotheodoros
 * 
 * Bean for the serviceSemanticsEditor.xhtml
 *
 */
public class ServiceSemanticsEditorView {

	protected TreeNode serviceRoot;
	protected TreeNode selectedServiceNode;

	protected TreeNode serviceParentRoot;
	protected TreeNode selectedParentNode;

	protected String selectedNodeName = "";
	protected String selectedNodeUri = "";
	private List<String> selectedNodeSuperClassNames;
	private List<String> selectedNodeRestrictionNames;
	private List<AnnotationItem> selectedNodeAnnotationItems;
	private List<DataProperty> dataTypeProperties;
	private DataProperty selectedDatatypeProperty;
	private List<ObjProperty> objectProperties;
	private ObjProperty selectedObjectProperty;
	private List<Instance> selectedNodeInstances;

	private String nServiceName;
	private String nServiceComment;
	private String nServiceLabel;

	private String nAnnotationPropertyLabel;
	private String nAnnotationPropertyValue;
	private AnnotationItem selectedAnnotationProperty;
	private AnnotationItem editedAnnotationProperty;
	private String selectedRestriction;
	private List<String> allAnnotationPropertyNames;

	private String restrictionPropertyType;
	private MyProperty restrictionProperty;
	private MyProperty newRestrictionProperty;
	private String restrictionPropertyName;
	private List<MyProperty> displayedRestrictionProperties;
	private List<String> displayedRestrictionPropertiesNames;
	private List<MyProperty> dataPropertiesAcceptedForSelectedClass;
	private List<MyProperty> objectPropertiesAcceptedForSelectedClass;
	private String selectedRestrictionType;
	private String newSelectedRestrictionType;
	private String selectedRestrictionValue;
	private String newSelectedRestrictionValue;
	
	private List<String> restrictionValues;
	
	private String nPropertyName;
	private String nPropertyComment;
	private String editPropertyComment;
	private String nDatapropertyRange;
	private String editDatapropertyRange;
	private String editObjectpropertyRange;
	
	protected TreeNode allClassesRoot;
	protected TreeNode selectedRangeNode;
	protected TreeNode editSelectedRangeNode;


	List<String> restrictionTypes = Arrays.asList("exactly", "max", "min",
			"only", "some");
	List<String> mainNodes = Arrays.asList(
			"http://inter-iot.eu/GOIoTP#Service",
			"http://inter-iot.eu/GOIoTP#ServiceInput",
			"http://inter-iot.eu/GOIoTP#ServiceOutput",
			"http://inter-iot.eu/GOIoTP#ServiceInterface",
			"http://inter-iot.eu/GOIoTP#DataFormat");

	private static final String NAMESPACE = "http://www.semanticweb.org/activage/ontologies/activage-core-tool#";
	private static final String DEFAULT_SELECTED_NODE = "http://inter-iot.eu/GOIoTP#Service";

	@ManagedProperty(value = "#{ontologyHandler}")
	protected OntologyHandler ontologyHandler;

	@PostConstruct
	public void init() {
		createTreeNode();
	}
	
	private void createTreeNode(){
		serviceRoot = ontologyHandler.createNewServiceTreeNode(mainNodes,
				new ArrayList<String>());
		selectTreeNode(serviceRoot, DEFAULT_SELECTED_NODE);
		updateInfoGrid(ontologyHandler.getOntologyClassByURI(DEFAULT_SELECTED_NODE));
	}

	public void onServiceNodeSelect(NodeSelectEvent event) {
		updateInfoGrid(event.getTreeNode());
		dataPropertiesAcceptedForSelectedClass = ontologyHandler
				.findAcceptedDataPropertiesOfClass(selectedNodeUri);
		objectPropertiesAcceptedForSelectedClass = ontologyHandler
				.findAcceptedObjectPropertiesOfClass(selectedNodeUri);
	}

	private void updateInfoGrid(TreeNode treeNode) {
		OntologyClass selectedOntologyClass = (OntologyClass) treeNode
				.getData();
		updateInfoGrid(selectedOntologyClass);
	}

	private void updateInfoGrid(OntologyClass selectedOntologyClass) {
		selectedNodeName = selectedOntologyClass.getName();
		selectedNodeUri = selectedOntologyClass.getUri();
		selectedNodeSuperClassNames = selectedOntologyClass.getParentNames();
		updateRestrictions();
		Collections.sort(selectedNodeSuperClassNames);

		selectedNodeAnnotationItems = ontologyHandler
				.getOrderedAnnotationPairs(selectedOntologyClass.getOntClass());

		dataTypeProperties = ontologyHandler
				.getDataPropertiesOfClass(selectedOntologyClass.getUri());
		objectProperties = ontologyHandler
				.getObjectPropertiesOfClass(selectedOntologyClass.getUri());

		selectedNodeInstances = selectedOntologyClass.listInstances();
		Collections.sort(selectedNodeInstances);
	}

	private void updateRestrictions() {
		OntologyClass selectedClass = ontologyHandler
				.getOntologyClassByURI(selectedNodeUri);
		selectedNodeRestrictionNames = new ArrayList<String>();
		ontologyHandler.findClassRestrictionsNames(selectedClass.getOntClass(),
				selectedNodeRestrictionNames, false);
		Collections.sort(selectedNodeRestrictionNames);
	}

	public void addService() {
		nServiceName = null;
		nServiceComment = null;
		nServiceLabel = null;
		RequestContext.getCurrentInstance().execute(
				"addNewServiceDialog.show();");
	}
	
	public void addDatatypeProperty(){
		nPropertyName = null;
		nPropertyComment = null;
		nDatapropertyRange = null;
	}
	
	public void addObjectProperty(){
		nPropertyName = null;
		nPropertyComment = null;
		allClassesRoot = ontologyHandler.createTreeNode();
		selectedRangeNode = null;
	}
	
	public void saveNewObjectProperty(){
		if (nPropertyName == null || nPropertyName.isEmpty()) return;
		ontologyHandler.createObjectProperty(NAMESPACE + nPropertyName,  selectedNodeUri, nPropertyComment, selectedRangeNode);
		OntologyClass selectedClass = ontologyHandler.getOntologyClassByURI(selectedNodeUri);
		updateInfoGrid(selectedClass);
		RequestContext.getCurrentInstance().execute("addObjectPropertyDialog.hide();");
	}
	
	public void saveNewDatatypeProperty(){
		if (nPropertyName == null || nPropertyName.isEmpty()) return;
		ontologyHandler.createDataTypeProperty(NAMESPACE + nPropertyName,  selectedNodeUri, nPropertyComment, ontologyHandler.findDatatypeResourceByShortName(nDatapropertyRange));
		OntologyClass selectedClass = ontologyHandler.getOntologyClassByURI(selectedNodeUri);
		updateInfoGrid(selectedClass);
		RequestContext.getCurrentInstance().execute("addDatatypePropertyDialog.hide();");
	}
	
	public void saveDatatypeProperty(){
		ontologyHandler.updateDataTypeProperty(NAMESPACE + nPropertyName, nPropertyComment, editPropertyComment, 
					 ontologyHandler.findDatatypeResourceByShortName(editDatapropertyRange));
		RequestContext.getCurrentInstance().execute("editDataPropertyDialog.hide();");
	} 
	
	public void saveObjectProperty(){
		ontologyHandler.updateObjectProperty(NAMESPACE + nPropertyName, nPropertyComment, editPropertyComment, 
				editSelectedRangeNode);
		RequestContext.getCurrentInstance().execute("editObjectPropertyDialog.hide();");
	} 
	
	public void saveService() {
		String newUri = NAMESPACE + nServiceName;
		try {
			OntClass newClass = ontologyHandler.createNewClass(newUri,
					nServiceComment, selectedNodeUri);
			if (nServiceLabel != null) {
				newClass.addLabel(nServiceLabel, null);
			}
			serviceRoot = ontologyHandler.createNewServiceTreeNode(mainNodes,
					new ArrayList<String>());
			selectTreeNode(serviceRoot, newUri);
			OntologyClass selectedClass = ontologyHandler
					.getOntologyClassByURI(newUri);
			updateInfoGrid(selectedClass);
			dataPropertiesAcceptedForSelectedClass = ontologyHandler
					.findAcceptedDataPropertiesOfClass(newUri);
			objectPropertiesAcceptedForSelectedClass = ontologyHandler
					.findAcceptedObjectPropertiesOfClass(newUri);

			RequestContext.getCurrentInstance().execute(
					"addNewServiceDialog.hide();");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void removeDataProperty(){
		ontologyHandler.removeDataProperty(selectedDatatypeProperty.getUri());
		OntologyClass selectedClass = ontologyHandler.getOntologyClassByURI(selectedNodeUri);
		updateInfoGrid(selectedClass);
	}
	
	public void removeObjectProperty(){
		ontologyHandler.removeObjectProperty(selectedObjectProperty.getUri());
		OntologyClass selectedClass = ontologyHandler.getOntologyClassByURI(selectedNodeUri);
		updateInfoGrid(selectedClass);
	}
	
	public void editObjectProperty(){
		System.out.println("EDIT OBJECT PROPERTY");
	}
	
	public void openEditDataPropertyDialog(){
		nPropertyName = selectedDatatypeProperty.getName();
		editPropertyComment = ontologyHandler.getDatatypeProperty(selectedDatatypeProperty.getUri()).getComment(null);
		List<String> ranges = ontologyHandler.findPropertyRanges(selectedDatatypeProperty.getUri());
		editDatapropertyRange = null;
		if (ranges != null && !ranges.isEmpty()){
			editDatapropertyRange = ranges.get(0);
			editDatapropertyRange = ontologyHandler.getResourceShortName(ranges.get(0));
		}
		RequestContext.getCurrentInstance().execute("editDataPropertyDialog.show();");		
	}
	
	public void openEditObjectPropertyDialog(){
		nPropertyName = selectedObjectProperty.getName();
		editPropertyComment = ontologyHandler.getObjectProperty(selectedObjectProperty.getUri()).getComment(null);
		List<String> ranges = ontologyHandler.findPropertyRanges(selectedObjectProperty.getUri());

		allClassesRoot = ontologyHandler.createTreeNode();
		if (ranges == null || ranges.isEmpty()){
			editSelectedRangeNode = null;
		}
		else {
			editSelectedRangeNode = selectTreeNode(allClassesRoot, ranges.get(0));
		}
		RequestContext.getCurrentInstance().execute("editObjectPropertyDialog.show();");		
	}
		
	public void editDataProperty(){
		System.out.println("EDIT DATA PROPERTY");
	}
	
	public void addRestriction(){
		ontologyHandler.saveNewRestriction(selectedNodeUri, restrictionProperty, selectedRestrictionType, selectedRestrictionValue);
		updateRestrictions();
		RequestContext.getCurrentInstance().execute("addRestrictionDialog.hide();");	
	}

	public boolean isServiceEditable() {
		if (selectedNodeUri == null || selectedNodeUri.isEmpty()){
			return false;
		}
		OntologyClass selectedClass = ontologyHandler.getOntologyClassByURI(selectedNodeUri);
		if (selectedClass.getOntClass().getNameSpace().equals(NAMESPACE)){
			return true;
		}
		return false;
	}

	public boolean isRemoveParentClassEditable() {
		boolean parentIsMainNode = false;
		for (String parent : selectedNodeSuperClassNames) {
			OntClass selectedNodeSuperClass = ontologyHandler
					.findClassByLocalname(parent);
			if (mainNodes.contains(selectedNodeSuperClass.getURI())) {
				parentIsMainNode = true;
				break;
			}
		}
		return isServiceEditable() && !parentIsMainNode;
	}

	public void deleteService() {
		OntologyClass selectedClass = ontologyHandler
				.getOntologyClassByURI(selectedNodeUri);
		TreeNode parentOfselectedNode = selectedServiceNode.getParent();
		ontologyHandler.deleteClass(selectedClass.getUri());
		deleteTreeNode(serviceRoot.getChildren().iterator(), serviceRoot);
		setSelectedNode(parentOfselectedNode);
		if (selectedNodeSuperClassNames.size() > 0) {
			String parentName = selectedNodeSuperClassNames.get(0);
			OntClass parentClass = ontologyHandler
					.findClassByLocalname(parentName);
			if (parentClass != null) {
				selectTreeNode(serviceRoot, parentClass.getURI());
				selectedNodeUri = parentClass.getURI();
				updateInfoGrid(ontologyHandler
						.getOntologyClassByURI(selectedNodeUri));
			}
		}

	}

	private void deleteTreeNode(Iterator<TreeNode> it, TreeNode parent) {
		OntologyClass selectedClass = ontologyHandler
				.getOntologyClassByURI(selectedNodeUri);
		while (it.hasNext()) {
			TreeNode node = it.next();
			if (((OntologyClass) node.getData()).getUri().equals(
					selectedClass.getUri())) {
				it.remove();
				return;
			}
			deleteTreeNode(node.getChildren().iterator(), node);
		}
	}

	protected TreeNode selectTreeNode(TreeNode treeNode, String uri){
		TreeNode selectedNode = selectTreeNode(treeNode.getChildren(), uri);
		if (selectedNode != null){
			treeNode.setExpanded(true);
		}
		return selectedNode;
	}

	private TreeNode selectTreeNode(List<TreeNode> children, String uri){
		for (TreeNode node : children){
			OntologyClass deviceClass = (OntologyClass)node.getData();
			if (deviceClass.getUri().equals(uri)){
				setSelectedNode(node);
				node.setSelected(true);
				node.setExpanded(true);
				return node;
			}
			else {
				if (selectTreeNode(node.getChildren(), uri) != null){
					node.setExpanded(true);
					return node;
				}
			}
		}	
		return null;
	} 

	public void removeAnnotationProperty() {
		ontologyHandler.removeAnnotationPropertyFromClass(selectedNodeUri,
				selectedAnnotationProperty);
		OntologyClass selectedClass = ontologyHandler
				.getOntologyClassByURI(selectedNodeUri);
		selectedNodeAnnotationItems = ontologyHandler
				.getOrderedAnnotationPairs(selectedClass.getOntClass());
	}

	public void setSelectedNode(TreeNode selectedNode) {
		selectedServiceNode = selectedNode;
		OntologyClass selectedClass = ontologyHandler
				.getOntologyClassByURI(selectedNodeUri);
		ontologyHandler.findClassRestrictions(selectedClass.getOntClass());
		selectedNodeName = selectedClass.getName();
		selectedNodeUri = selectedClass.getUri();
	}

	public boolean checkRestrictionEditable(String restriction) {
		try {
			OntologyClass selectedClass = ontologyHandler
					.getOntologyClassByURI(selectedNodeUri);
			if (selectedClass == null) {
				return false;
			}
			Set<String> importedOntologyUris = ontologyHandler
					.getImportedOntologyNamespaces();
			for (String uri : importedOntologyUris) {
				if (selectedClass.getOntClass().getNameSpace().equals(uri)) {
					return false;
				}
			}
			List<MyRestriction> directRestrictions = new ArrayList<MyRestriction>();
			ontologyHandler.findClassRestrictions(selectedClass.getOntClass(),
					directRestrictions, true);
			for (MyRestriction d : directRestrictions) {
				if (d.getName().equals(restriction)) {
					return true;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}
	
	public boolean checkDataPropertyEditable(String shortName){
		DatatypeProperty dp = ontologyHandler.findDataPropertyByShortName(shortName);
		return checkPropertyEditable(dp);
	}
	
	public boolean checkObjectPropertyEditable(String shortName){
		ObjectProperty dp = ontologyHandler.findObjectPropertyByShortName(shortName);
		return checkPropertyEditable(dp);
	}
	
	private boolean checkPropertyEditable(Property p){
		if (p == null) return false;
		if (p.getNameSpace().equals(NAMESPACE)){
			return true;
		}
		return false;
	}

	public void removeParentClass(String parentName) {
		OntologyClass selectedClass = ontologyHandler
				.getOntologyClassByURI(selectedNodeUri);
		OntClass mainParent = findMainParent(selectedClass.getOntClass());
		ontologyHandler.removeParent(selectedClass.getOntClass(), parentName);
		selectedClass.getOntClass().addSuperClass(mainParent);
		serviceRoot = ontologyHandler.createNewServiceTreeNode(mainNodes,
				new ArrayList<String>());
		selectedClass = ontologyHandler.getOntologyClassByURI(selectedNodeUri);
		updateInfoGrid(selectedClass);
		dataPropertiesAcceptedForSelectedClass = ontologyHandler
				.findAcceptedDataPropertiesOfClass(selectedNodeUri);
		objectPropertiesAcceptedForSelectedClass = ontologyHandler
				.findAcceptedObjectPropertiesOfClass(selectedNodeUri);
		selectTreeNode(serviceRoot, selectedClass.getUri());
	}

	private OntClass findMainParent(OntClass cl) {
		ExtendedIterator<OntClass> x = cl.listSuperClasses();
		while (x.hasNext()) {
			OntClass p = x.next();
			if (mainNodes.contains(p.getURI())) {
				return p;
			}
		}
		return null;
	}

	public void editParentClass(String parentName) {
		OntClass parentClass = ontologyHandler.findClassByLocalname(parentName);
		if (parentClass == null)
			return;

		OntClass mainNodeClass;
		if (mainNodes.contains(parentClass.getURI())) {
			mainNodeClass = parentClass;
		} else {
			mainNodeClass = findMainParent(parentClass);
		}

		List<String> mainNodes = Arrays.asList(mainNodeClass.getURI());
		serviceParentRoot = ontologyHandler.createNewServiceTreeNode(mainNodes,
				Arrays.asList(selectedNodeUri));
		selectTreeNode(serviceParentRoot, parentClass.getURI());
		RequestContext.getCurrentInstance().execute(
				"editParentClassDialog.show();");
	}

	public void saveNewParentClass() {
		OntologyClass selectedClass = ontologyHandler
				.getOntologyClassByURI(selectedNodeUri);
		if (selectedNodeSuperClassNames.isEmpty())
			return;
		ontologyHandler.removeParent(selectedClass.getOntClass(),
				selectedNodeSuperClassNames.get(0));
		selectedClass.getOntClass().addSuperClass(
				((OntologyClass) selectedParentNode.getData()).getOntClass());
		serviceRoot = ontologyHandler.createNewServiceTreeNode(mainNodes,
				new ArrayList<String>());
		selectedClass = ontologyHandler.getOntologyClassByURI(selectedNodeUri);
		updateInfoGrid(selectedClass);
		dataPropertiesAcceptedForSelectedClass = ontologyHandler
				.findAcceptedDataPropertiesOfClass(selectedNodeUri);
		objectPropertiesAcceptedForSelectedClass = ontologyHandler
				.findAcceptedObjectPropertiesOfClass(selectedNodeUri);
		selectTreeNode(serviceRoot, selectedClass.getUri());
		RequestContext.getCurrentInstance().execute(
				"editParentClassDialog.hide();");
	}

	public void removeRestriction() {
		removeRestriction(selectedNodeUri, selectedRestriction);
		updateRestrictions();
		dataTypeProperties = ontologyHandler
				.getDataPropertiesOfClass(selectedNodeUri);
		objectProperties = ontologyHandler
				.getObjectPropertiesOfClass(selectedNodeUri);
	}

	private void removeRestriction(String classUri, String restrictionName) {
		OntologyClass selectedClass = ontologyHandler
				.getOntologyClassByURI(classUri);
		MyRestriction r = findrestriction(classUri, restrictionName, false);
		if (r != null) {
			ontologyHandler.removeRestriction(selectedClass.getOntClass(),
					r.getRestriction());
		}
	}

	private MyRestriction findrestriction(String classUri,
			String restrictionName, boolean direct) {
		OntologyClass selectedClass = ontologyHandler
				.getOntologyClassByURI(classUri);
		ArrayList<MyRestriction> classRestrictions = new ArrayList<MyRestriction>();
		ontologyHandler.findClassRestrictions(selectedClass.getOntClass(),
				classRestrictions, direct);
		for (MyRestriction r : classRestrictions) {
			if (r.getName().equals(restrictionName)) {
				return r;
			}
		}
		return null;
	}

	public void openAnnotationPropertyDialog() {
		editedAnnotationProperty = new AnnotationItem(
				selectedAnnotationProperty.getLabel(),
				selectedAnnotationProperty.getValue());
	}

	public void openEditRestrictionDialog() {
		MyRestriction r = findrestriction(selectedNodeUri, selectedRestriction,
				true);
		if (r == null)
			return;
		OntProperty prop = r.getRestriction().getOnProperty();
		restrictionPropertyType = null;
		if (prop.isDatatypeProperty()) {
			restrictionPropertyType = "Datatype property";
			restrictionProperty = new DataProperty(
					ontologyHandler.getDatatypeProperty(prop.getURI()));
			newRestrictionProperty = new DataProperty(ontologyHandler.getDatatypeProperty(prop.getURI()));
		} else if (prop.isObjectProperty()) {
			restrictionPropertyType = "Object property";
			restrictionProperty = new ObjProperty(
					ontologyHandler.getObjectProperty(prop.getURI()));
			newRestrictionProperty = new ObjProperty(ontologyHandler.getObjectProperty(prop.getURI()));
		}
		restrictionPropertyName = restrictionProperty.getName();
		selectedRestrictionType = selectedRestriction.split(" ")[1];
		newSelectedRestrictionType = selectedRestrictionType;
		onRestrictionPropertyTypeChange(true);
		onRestrictionTypeChange();
		selectedRestrictionValue = selectedRestriction.split(" ")[2];
		RequestContext.getCurrentInstance().execute(
				"editRestrictionDialog.show();");

	}
	
	public void openAddRestrictionDialog(){
		restrictionPropertyType = "Datatype property";
		selectedRestrictionType = "exactly";
		newSelectedRestrictionType = selectedRestrictionType;
		onRestrictionPropertyTypeChange(true);	
		onRestrictionTypeChange();
		selectedRestrictionValue = "1";
		RequestContext.getCurrentInstance().execute("addRestrictionDialog.show();");		
	}

	public void onRestrictionPropertyTypeChange(boolean onOpen){
		if (restrictionPropertyType.equals("Datatype property")){
			displayedRestrictionProperties = dataPropertiesAcceptedForSelectedClass;
		}
		else if (restrictionPropertyType.equals("Object property")){
			displayedRestrictionProperties = objectPropertiesAcceptedForSelectedClass;
		}
		displayedRestrictionPropertiesNames = new ArrayList<String>();
		for (MyProperty p : displayedRestrictionProperties){
			displayedRestrictionPropertiesNames.add(p.getName());
		}
		
		if (!onOpen){
			restrictionProperty = displayedRestrictionProperties.get(0);
			newRestrictionProperty = new MyProperty(restrictionProperty.getUri(), restrictionProperty.getComment());
			restrictionPropertyName = displayedRestrictionPropertiesNames.get(0);
		}
		onRestrictionTypeChange();
	}

	
	public void onRestrictionPropertyChange(){
		for (MyProperty p : displayedRestrictionProperties){
			if (p.getName().equals(restrictionPropertyName)){
				restrictionProperty = p;
				newRestrictionProperty = p;
				break;
			}
		}
	}
	
	public void onRestrictionPropertyValueChange(){
		newSelectedRestrictionValue = selectedRestrictionValue;
	}

	public void onRestrictionTypeChange(){
		newSelectedRestrictionType = selectedRestrictionType;
		try {
			if (selectedRestrictionType.equals("some") || selectedRestrictionType.equals("only")){
				if (restrictionProperty instanceof ObjProperty){

					restrictionValues = new ArrayList<String>();
					for (String s : selectedNodeRestrictionNames){
						MyRestriction r = findrestriction(selectedNodeUri, s, false);
						if (r.getRestrictionPropertyName().equals(restrictionPropertyName) && !r.getRestrictionType().equals(selectedRestrictionType)){
							String restrictionValue = r.getRestrictionValue();
							OntClass cl = ontologyHandler.findClassByLocalname(restrictionValue);
							if (cl == null) continue;
							restrictionValues.add(cl.getLocalName());
							ExtendedIterator<OntClass> it = cl.listSubClasses();
							while (it.hasNext()){
								OntClass child = it.next();
								if (child.getLocalName() != null){
									restrictionValues.add(child.getLocalName());
								}
							}
						}					
					}	
					if (restrictionValues.isEmpty()){
						ObjProperty p = (ObjProperty) restrictionProperty;
						restrictionValues = ontologyHandler.findObjectPropertyRanges(p.getObjectProperty());
					}
					if (restrictionValues.size() > 0){
						selectedRestrictionValue = restrictionValues.get(0);
						newSelectedRestrictionValue = selectedRestrictionValue;
					}
				}
				else {
					restrictionValues = ontologyHandler.getDatatypeResources();
					selectedRestrictionValue  = restrictionValues.get(0);
					newSelectedRestrictionValue = selectedRestrictionValue;
				}
			}
			else {
				if (selectedRestrictionValue == null || selectedRestrictionValue.isEmpty()){
					selectedRestrictionValue = "1";
					newSelectedRestrictionValue = selectedRestrictionValue;
					restrictionValues = new ArrayList<String>();
				}
				else {
					try {
						Integer.parseInt(selectedRestrictionValue);
					}
					catch (Exception e){
						selectedRestrictionValue = "1";
						newSelectedRestrictionValue = selectedRestrictionValue;
						restrictionValues = new ArrayList<String>();
					}
				}
				
			}
		}
		catch (Exception e){
			e.printStackTrace();
		}
	}

	public void openNewAnnotationPropertyDialog() {
		nAnnotationPropertyLabel = null;
		nAnnotationPropertyValue = null;
		allAnnotationPropertyNames = ontologyHandler
				.getAllAnnotationPropertyNames();
		List<String> toRemove = new ArrayList<String>();
		if (selectedNodeAnnotationItems != null) {
			for (AnnotationItem a : selectedNodeAnnotationItems) {
				toRemove.add(a.getLabel());
			}
		}
		allAnnotationPropertyNames.removeAll(toRemove);
	}

	public void saveAnnotationProperty() {
		OntologyClass selectedClass = ontologyHandler
				.getOntologyClassByURI(selectedNodeUri);
		ontologyHandler.removeAnnotationPropertyFromClass(selectedNodeUri,
				selectedAnnotationProperty);
		ontologyHandler.addNewAnnotationPropertyFromClass(selectedNodeUri,
				editedAnnotationProperty);
		selectedNodeAnnotationItems = ontologyHandler
				.getOrderedAnnotationPairs(selectedClass.getOntClass());
		RequestContext.getCurrentInstance().execute(
				"editAnnotationPropertyDialog.hide();");
	}

	public void saveRestriction() {
		removeRestriction(selectedNodeUri, selectedRestriction);
		ontologyHandler.saveNewRestriction(selectedNodeUri, newRestrictionProperty, newSelectedRestrictionType, newSelectedRestrictionValue);
		updateRestrictions();
		RequestContext.getCurrentInstance().execute("editRestrictionDialog.hide();");	
	}

	public void saveNewAnnotationProperty() {
		OntologyClass selectedClass = ontologyHandler
				.getOntologyClassByURI(selectedNodeUri);
		ontologyHandler.addNewAnnotationPropertyFromClass(selectedNodeUri,
				new AnnotationItem(nAnnotationPropertyLabel,
						nAnnotationPropertyValue));
		selectedNodeAnnotationItems = ontologyHandler
				.getOrderedAnnotationPairs(selectedClass.getOntClass());
		RequestContext.getCurrentInstance().execute(
				"addAnnotationPropertyDialog.hide();");
	}
	
	/**
	 * method used to download the excel file with the usages
	 * @return
	 */
	public StreamedContent download(){    
		final String ontologyFileName = "activage-core-updated.owl";
		try {
			String filepath = ontologyHandler.saveOntology(ontologyFileName);
			File file = new File(filepath);
			byte[] bytes = Files.readAllBytes(file.toPath());
			InputStream stream = new ByteArrayInputStream(bytes);
			// 2. get Liferay's ServletResponse
			 PortletResponse portletResponse = (PortletResponse) FacesContext
			   .getCurrentInstance().getExternalContext().getResponse();
			 HttpServletResponse res = PortalUtil
			   .getHttpServletResponse(portletResponse);
			 res.setHeader("Content-Disposition", "attachment; filename=\"" + ontologyFileName + "\"");//
			 res.setHeader("Content-Transfer-Encoding", "binary");
			 res.setContentType("application/rdf+xml");
			 res.flushBuffer();

			 // 3. write the file into the outputStream
			 OutputStream out = res.getOutputStream();
			 byte[] buffer = new byte[4096];
			 int bytesRead;
			 while ((bytesRead = stream.read(buffer)) != -1) {
			  out.write(buffer, 0, bytesRead);
			  buffer = new byte[4096];
			 }
		}
		catch (Exception w){
			w.printStackTrace();
		}
		return null;
	}

	public TreeNode getServiceRoot() {
		return serviceRoot;
	}

	public void setServiceRoot(TreeNode serviceRoot) {
		this.serviceRoot = serviceRoot;
	}

	public TreeNode getSelectedServiceNode() {
		return selectedServiceNode;
	}

	public void setSelectedServiceNode(TreeNode selectedServiceNode) {
		this.selectedServiceNode = selectedServiceNode;
	}

	public OntologyHandler getOntologyHandler() {
		return ontologyHandler;
	}

	public void setOntologyHandler(OntologyHandler ontologyHandler) {
		this.ontologyHandler = ontologyHandler;
	}

	public String getSelectedNodeName() {
		return selectedNodeName;
	}

	public void setSelectedNodeName(String selectedNodeName) {
		this.selectedNodeName = selectedNodeName;
	}

	public String getSelectedNodeUri() {
		return selectedNodeUri;
	}

	public void setSelectedNodeUri(String selectedNodeUri) {
		this.selectedNodeUri = selectedNodeUri;
	}

	public List<String> getSelectedNodeSuperClassNames() {
		return selectedNodeSuperClassNames;
	}

	public void setSelectedNodeSuperClassNames(
			List<String> selectedNodeSuperClassNames) {
		this.selectedNodeSuperClassNames = selectedNodeSuperClassNames;
	}

	public List<String> getSelectedNodeRestrictionNames() {
		return selectedNodeRestrictionNames;
	}

	public void setSelectedNodeRestrictionNames(
			List<String> selectedNodeRestrictionNames) {
		this.selectedNodeRestrictionNames = selectedNodeRestrictionNames;
	}

	public List<AnnotationItem> getSelectedNodeAnnotationItems() {
		return selectedNodeAnnotationItems;
	}

	public void setSelectedNodeAnnotationItems(
			List<AnnotationItem> selectedNodeAnnotationItems) {
		this.selectedNodeAnnotationItems = selectedNodeAnnotationItems;
	}

	public List<DataProperty> getDataTypeProperties() {
		return dataTypeProperties;
	}

	public void setDataTypeProperties(List<DataProperty> dataTypeProperties) {
		this.dataTypeProperties = dataTypeProperties;
	}

	public List<ObjProperty> getObjectProperties() {
		return objectProperties;
	}

	public void setObjectProperties(List<ObjProperty> objectProperties) {
		this.objectProperties = objectProperties;
	}

	public List<Instance> getSelectedNodeInstances() {
		return selectedNodeInstances;
	}

	public void setSelectedNodeInstances(List<Instance> selectedNodeInstances) {
		this.selectedNodeInstances = selectedNodeInstances;
	}

	public String getnServiceName() {
		return nServiceName;
	}

	public void setnServiceName(String nServiceName) {
		this.nServiceName = nServiceName;
	}

	public String getnServiceComment() {
		return nServiceComment;
	}

	public void setnServiceComment(String nServiceComment) {
		this.nServiceComment = nServiceComment;
	}

	public String getnServiceLabel() {
		return nServiceLabel;
	}

	public void setnServiceLabel(String nServiceLabel) {
		this.nServiceLabel = nServiceLabel;
	}

	public AnnotationItem getSelectedAnnotationProperty() {
		return selectedAnnotationProperty;
	}

	public void setSelectedAnnotationProperty(
			AnnotationItem selectedAnnotationProperty) {
		this.selectedAnnotationProperty = selectedAnnotationProperty;
	}

	public String getSelectedRestriction() {
		return selectedRestriction;
	}

	public void setSelectedRestriction(String selectedRestriction) {
		this.selectedRestriction = selectedRestriction;
	}

	public AnnotationItem getEditedAnnotationProperty() {
		return editedAnnotationProperty;
	}

	public void setEditedAnnotationProperty(
			AnnotationItem editedAnnotationProperty) {
		this.editedAnnotationProperty = editedAnnotationProperty;
	}

	public List<String> getAllAnnotationPropertyNames() {
		return allAnnotationPropertyNames;
	}

	public void setAllAnnotationPropertyNames(
			List<String> allAnnotationPropertyNames) {
		this.allAnnotationPropertyNames = allAnnotationPropertyNames;
	}

	public String getnAnnotationPropertyLabel() {
		return nAnnotationPropertyLabel;
	}

	public void setnAnnotationPropertyLabel(String nAnnotationPropertyLabel) {
		this.nAnnotationPropertyLabel = nAnnotationPropertyLabel;
	}

	public String getnAnnotationPropertyValue() {
		return nAnnotationPropertyValue;
	}

	public void setnAnnotationPropertyValue(String nAnnotationPropertyValue) {
		this.nAnnotationPropertyValue = nAnnotationPropertyValue;
	}

	public String getRestrictionPropertyType() {
		return restrictionPropertyType;
	}

	public void setRestrictionPropertyType(String restrictionPropertyType) {
		this.restrictionPropertyType = restrictionPropertyType;
	}

	public List<MyProperty> getDisplayedRestrictionProperties() {
		return displayedRestrictionProperties;
	}

	public void setDisplayedRestrictionProperties(
			List<MyProperty> displayedRestrictionProperties) {
		this.displayedRestrictionProperties = displayedRestrictionProperties;
	}

	public MyProperty getRestrictionProperty() {
		return restrictionProperty;
	}

	public void setRestrictionProperty(MyProperty restrictionProperty) {
		this.restrictionProperty = restrictionProperty;
	}

	public List<String> getRestrictionTypes() {
		return restrictionTypes;
	}

	public void setRestrictionTypes(List<String> restrictionTypes) {
		this.restrictionTypes = restrictionTypes;
	}

	public String getSelectedRestrictionType() {
		return selectedRestrictionType;
	}

	public void setSelectedRestrictionType(String selectedRestrictionType) {
		this.selectedRestrictionType = selectedRestrictionType;
	}

	public String getSelectedRestrictionValue() {
		return selectedRestrictionValue;
	}

	public void setSelectedRestrictionValue(String selectedRestrictionValue) {
		this.selectedRestrictionValue = selectedRestrictionValue;
	}

	public List<String> getRestrictionValues() {
		return restrictionValues;
	}

	public void setRestrictionValues(List<String> restrictionValues) {
		this.restrictionValues = restrictionValues;
	}

	public TreeNode getServiceParentRoot() {
		return serviceParentRoot;
	}

	public void setServiceParentRoot(TreeNode serviceParentRoot) {
		this.serviceParentRoot = serviceParentRoot;
	}

	public TreeNode getSelectedParentNode() {
		return selectedParentNode;
	}

	public void setSelectedParentNode(TreeNode selectedParentNode) {
		this.selectedParentNode = selectedParentNode;
	}

	public List<String> getDisplayedRestrictionPropertiesNames() {
		return displayedRestrictionPropertiesNames;
	}

	public void setDisplayedRestrictionPropertiesNames(
			List<String> displayedRestrictionPropertiesNames) {
		this.displayedRestrictionPropertiesNames = displayedRestrictionPropertiesNames;
	}

	public String getRestrictionPropertyName() {
		return restrictionPropertyName;
	}

	public void setRestrictionPropertyName(String restrictionPropertyName) {
		this.restrictionPropertyName = restrictionPropertyName;
		if (displayedRestrictionProperties != null) {
			for (MyProperty p : displayedRestrictionProperties) {
				if (p.getName().equals(restrictionPropertyName)) {
					setRestrictionProperty(p);
					break;
				}
			}
		}
	}
	
	public String getnPropertyName() {
		return nPropertyName;
	}

	public void setnPropertyName(String nPropertyName) {
		this.nPropertyName = nPropertyName;
	}
	
	public String getnDatapropertyRange() {
		return nDatapropertyRange;
	}

	public void setnDatapropertyRange(String nDatapropertyRange) {
		this.nDatapropertyRange = nDatapropertyRange;
	}

	public List<String> getDatatypeResources(){
		return ontologyHandler.getDatatypeResourcesShort();
	}

	public DataProperty getSelectedDatatypeProperty() {
		return selectedDatatypeProperty;
	}

	public void setSelectedDatatypeProperty(DataProperty selectedDatatypeProperty) {
		this.selectedDatatypeProperty = selectedDatatypeProperty;
	}

	public TreeNode getAllClassesRoot() {
		return allClassesRoot;
	}

	public void setAllClassesRoot(TreeNode allClassesRoot) {
		this.allClassesRoot = allClassesRoot;
	}

	public TreeNode getSelectedRangeNode() {
		return selectedRangeNode;
	}

	public void setSelectedRangeNode(TreeNode selectedRangeNode) {
		this.selectedRangeNode = selectedRangeNode;
	}

	public ObjProperty getSelectedObjectProperty() {
		return selectedObjectProperty;
	}

	public void setSelectedObjectProperty(ObjProperty selectedObjectProperty) {
		this.selectedObjectProperty = selectedObjectProperty;
	}

	public String getnPropertyComment() {
		return nPropertyComment;
	}

	public void setnPropertyComment(String nPropertyComment) {
		this.nPropertyComment = nPropertyComment;
	}

	public String getEditPropertyComment() {
		return editPropertyComment;
	}

	public void setEditPropertyComment(String editPropertyComment) {
		this.editPropertyComment = editPropertyComment;
	}

	public String getEditDatapropertyRange() {
		return editDatapropertyRange;
	}

	public void setEditDatapropertyRange(String editDatapropertyRange) {
		this.editDatapropertyRange = editDatapropertyRange;
	}

	public String getEditObjectpropertyRange() {
		return editObjectpropertyRange;
	}

	public void setEditObjectpropertyRange(String editObjectpropertyRange) {
		this.editObjectpropertyRange = editObjectpropertyRange;
	}

	public TreeNode getEditSelectedRangeNode() {
		return editSelectedRangeNode;
	}

	public void setEditSelectedRangeNode(TreeNode editSelectedRangeNode) {
		this.editSelectedRangeNode = editSelectedRangeNode;
	}
}
