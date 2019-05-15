package org.activage.views;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import javax.portlet.PortletResponse;
import javax.servlet.http.HttpServletResponse;

import org.activage.entities.DataProperty;
import org.activage.entities.Instance;
import org.activage.entities.InstanceProperty;
import org.activage.entities.ObjProperty;
import org.activage.entities.OntologyClass;
import org.activage.utils.Utils;
import org.activage.views.helper_entities.AnnotationItem;
import org.primefaces.context.RequestContext;
import org.primefaces.event.NodeSelectEvent;
import org.primefaces.model.StreamedContent;
import org.primefaces.model.TreeNode;

import com.hp.hpl.jena.rdf.model.Model;
import com.liferay.portal.util.PortalUtil;

@ManagedBean(name = "visualizationView")
@ViewScoped
/**
 * 
 * @author stavrotheodoros
 *
 * Bean for the visualization.xhtml
 * 
 */
public class VisualizationView extends RootView{

	private String searchName;
	
	private List<String> selectedNodeSuperClassNames;
	private List<String> selectedNodeRestrictionNames;
	private List<AnnotationItem> selectedNodeAnnotationItems;
	private List<AnnotationItem> selectedDataPropertyAnnotationItems;
	private List<AnnotationItem> selectedObjectPropertyAnnotationItems;
	private List<Instance> selectedNodeInstances;
	private List<DataProperty> dataTypeProperties;
	private List<ObjProperty> objectProperties;
	private DataProperty selectedDataProperty;
	private ObjProperty selectedObjectProperty;
	private Instance selectedInstance;
	private String turtleDescription;
	private List<InstanceProperty> instanceProperties = new ArrayList<InstanceProperty>();
	


	@PostConstruct
	public void init() {
		ontologyHandler.createD3DataModel();
		root1 = ontologyHandler.createTreeNode();
	}
	
	@Override
	public void onNodeSelect(NodeSelectEvent event) {
		updateInfoGrid(event.getTreeNode());
    }
	
	/**
	 * Method called when the search button is used
	 * Updates the panel that contains the information related to the selected class and the graph
	 */
	public void search() {
		root1 = ontologyHandler.createTreeNode();
		TreeNode result = findTreeNodeByName(root1, searchName);
		if (result != null){
			selectedNode = result;
			updateInfoGrid(result);
		}
    }
	
	/**
	 * Method that updates the graph by marking the given node as red
	 * It also updates the section that contains the details of a class
	 * @param treeNode : the selected TreeNode instance
	 */
	private void updateInfoGrid(TreeNode treeNode){
		RequestContext.getCurrentInstance().execute("search('"+treeNode.toString()+"')");
		OntologyClass selectedOntologyClass = (OntologyClass) treeNode.getData();
		
		selectedNodeName = selectedOntologyClass.getName();
		selectedNodeUri = selectedOntologyClass.getUri();
		selectedNodeSuperClassNames = selectedOntologyClass.getParentNames();
		selectedNodeRestrictionNames = new ArrayList<String>();
		ontologyHandler.findClassRestrictionsNames(selectedOntologyClass.getOntClass(), selectedNodeRestrictionNames, false);
		Collections.sort(selectedNodeRestrictionNames);
		Collections.sort(selectedNodeSuperClassNames);
		
		selectedNodeAnnotationItems = ontologyHandler.getOrderedAnnotationPairs(selectedOntologyClass.getOntClass());

		dataTypeProperties = ontologyHandler.getDataPropertiesOfClass(selectedOntologyClass.getUri());
		objectProperties = ontologyHandler.getObjectPropertiesOfClass(selectedOntologyClass.getUri());
		
		selectedNodeInstances = selectedOntologyClass.listInstances();
		Collections.sort(selectedNodeInstances);

	}
	
	public void openDescription(){
		turtleDescription = "";
		try {
			turtleDescription = ontologyHandler.getOntologyDescription(selectedNodeUri);
			turtleDescription = turtleDescription.replaceAll("\n", "<br>");
			//System.out.println(turtleDescription);
			RequestContext.getCurrentInstance().execute(
					"textualDescriptionDialog.show();");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * method used to download the excel file with the usages
	 * @return
	 */
	public StreamedContent download(){   
		try {
			Model model = ontologyHandler.getOntologyDescriptionModel(selectedNodeUri);
			ByteArrayOutputStream output = new ByteArrayOutputStream();
			model.write(output, "TTL");
			
			InputStream stream = new ByteArrayInputStream(output.toByteArray());
			// 2. get Liferay's ServletResponse
			 PortletResponse portletResponse = (PortletResponse) FacesContext
			   .getCurrentInstance().getExternalContext().getResponse();
			 HttpServletResponse res = PortalUtil
			   .getHttpServletResponse(portletResponse);
			 res.setHeader("Content-Disposition", "attachment; filename=\"" + selectedNodeName +".txt" + "\"");//
			 res.setHeader("Content-Transfer-Encoding", "binary");
			 res.setContentType("application/octet-stream");
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
	
	/**
	 * A recursive method that checks if a TreeNode has a given name
	 * if not found then it searches its children
	 * when the TreeNode is found it is marked as selected and all its parent nodes are expanded
	 * @param node : the TreeNode that is checked
	 * @param name : the wanted name
	 * @return the found TreeNode or null if not found
	 */
	private TreeNode findTreeNodeByName(TreeNode node, String name){
		if (node.toString().equalsIgnoreCase(name)){
			node.setSelected(true);
			return node;
		}
		for (TreeNode child: node.getChildren()){
			if (child.toString().equalsIgnoreCase(name)){
				setSelectedNode(child);
				child.setSelected(true);
				node.setExpanded(true);
				return child;
			}
			else{
				TreeNode result = findTreeNodeByName(child, name);
				if (result != null){
					child.setExpanded(true);
					result.setExpanded(true);
					return result;
				}
			}
		}
		return null;
	}
	
	/**
	 * Action executed when a data property is selected
	 * it finds the property  and then opens a dialog box to display it
	 * @param dataPropertyURI : the uri of the selected datatype property
	 */
	public void selectDatatypeProperty(String dataPropertyURI){		
		selectedDataProperty = (DataProperty) Utils.searchByUri(dataTypeProperties, dataPropertyURI);;
		if (selectedDataProperty != null){
			selectedDataProperty.setSelectedNodeDomains(ontologyHandler.findDomainClasses(dataPropertyURI));
			selectedDataProperty.setSelectedNodeRanges(ontologyHandler.findPropertyRanges(dataPropertyURI));
			selectedDataPropertyAnnotationItems = ontologyHandler.getOrderedAnnotationPairs(selectedDataProperty.getDatatypeProperty());
			RequestContext.getCurrentInstance().execute("datapropertyDialog.show();");
		}
	}
	
	/**
	 * Action executed when an object property is selected
	 * it finds the property and then opens a dialog box to display it
	 * @param objectPropertyURI : the uri of the selected object property
	 */
	public void selectObjectProperty(String objectPropertyURI){
		selectedObjectProperty = (ObjProperty) Utils.searchByUri(objectProperties, objectPropertyURI);
		if (selectedObjectProperty != null){
			selectedObjectPropertyAnnotationItems = ontologyHandler.getOrderedAnnotationPairs(selectedObjectProperty.getObjectProperty());
			RequestContext.getCurrentInstance().execute("objectpropertyDialog.show();");
		}
	}
	
	public void selectInstance(String instanceUri){
		 selectedInstance = (Instance) Utils.searchByUri(selectedNodeInstances, instanceUri);
		 if (selectedInstance != null){
				Map<String, List<String>> properties = selectedInstance.getProperties();
				instanceProperties = new ArrayList<InstanceProperty>();
				for (Entry<String, List<String>> x : properties.entrySet()){
					for (String value : x.getValue()){
						instanceProperties.add(new InstanceProperty(x.getKey(), value));
					}
				}
			 RequestContext.getCurrentInstance().execute("instanceDialog.show();");
		 }
	}

	/*********************************     GETTERS - SETTERS          *******************************/
	public List<String> getSelectedNodeSuperClassNames() {
		return selectedNodeSuperClassNames;
	}

	public void setSelectedNodeSuperClassNames(
			List<String> selectedNodeSuperClassNames) {
		this.selectedNodeSuperClassNames = selectedNodeSuperClassNames;
	}

	public List<AnnotationItem> getSelectedDataPropertyAnnotationItems() {
		return selectedDataPropertyAnnotationItems;
	}

	public void setSelectedDataPropertyAnnotationItems(
			List<AnnotationItem> selectedDataPropertyAnnotationItems) {
		this.selectedDataPropertyAnnotationItems = selectedDataPropertyAnnotationItems;
	}

	public List<AnnotationItem> getSelectedNodeAnnotationItems() {
		return selectedNodeAnnotationItems;
	}

	public void setSelectedNodeAnnotationItems(
			List<AnnotationItem> selectedNodeAnnotationItems) {
		this.selectedNodeAnnotationItems = selectedNodeAnnotationItems;
	}

	public List<AnnotationItem> getSelectedObjectPropertyAnnotationItems() {
		return selectedObjectPropertyAnnotationItems;
	}

	public void setSelectedObjectPropertyAnnotationItems(
			List<AnnotationItem> selectedObjectPropertyAnnotationItems) {
		this.selectedObjectPropertyAnnotationItems = selectedObjectPropertyAnnotationItems;
	}

	public String getSearchName() {
		return searchName;
	}

	public void setSearchName(String searchName) {
		this.searchName = searchName;
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
	
	public String getD3Model() {
		return ontologyHandler.getD3Model();
	}

	public List<String> getSelectedNodeRestrictionNames() {
		return selectedNodeRestrictionNames;
	}

	public void setSelectedNodeRestrictionNames(
			List<String> selectedNodeRestrictionNames) {
		this.selectedNodeRestrictionNames = selectedNodeRestrictionNames;
	}

	public List<Instance> getSelectedNodeInstances() {
		return selectedNodeInstances;
	}

	public void setSelectedNodeInstances(List<Instance> selectedNodeInstances) {
		this.selectedNodeInstances = selectedNodeInstances;
	}

	public Instance getSelectedInstance() {
		return selectedInstance;
	}

	public void setSelectedInstance(Instance selectedInstance) {
		this.selectedInstance = selectedInstance;
	}

	public List<InstanceProperty> getInstanceProperties() {
		return instanceProperties;
	}

	public void setInstanceProperties(List<InstanceProperty> instanceProperties) {
		this.instanceProperties = instanceProperties;
	}

	public String getTurtleDescription() {
		return turtleDescription;
	}

	public void setTurtleDescription(String turtleDescription) {
		this.turtleDescription = turtleDescription;
	}
}
