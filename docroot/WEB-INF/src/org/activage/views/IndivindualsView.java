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
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import javax.portlet.PortletResponse;
import javax.servlet.http.HttpServletResponse;

import org.activage.OntologyHandler;
import org.activage.entities.Instance;
import org.activage.entities.InstanceProperty;
import org.activage.entities.OntologyClass;
import org.primefaces.context.RequestContext;
import org.primefaces.event.NodeSelectEvent;
import org.primefaces.model.DefaultTreeNode;
import org.primefaces.model.StreamedContent;
import org.primefaces.model.TreeNode;

import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.rdf.model.Model;
import com.liferay.portal.util.PortalUtil;

@ManagedBean(name = "indivindualsView")
@ViewScoped
/**
 * 
 * @author stavrotheodoros
 * 
 * Bean for the indivindualByClass.xhtml
 *
 */
public class IndivindualsView {
	
	@ManagedProperty(value="#{ontologyHandler}")
	private OntologyHandler ontologyHandler;
	
	private TreeNode root1;
	private TreeNode selectedClassNode;
	
	private TreeNode root2;
	private TreeNode selectedIndivindualNode;
	
	private List<InstanceProperty> instanceProperties = new ArrayList<InstanceProperty>();


	
	@PostConstruct
	public void init() {
		root1 = ontologyHandler.createTreeNode();
	}
	
	public void onClassNodeSelect(NodeSelectEvent event) {
		selectedClassNode = event.getTreeNode();
		updateIndivindualList();
    }
	
	private void updateIndivindualList(){
		OntologyClass ontClass = (OntologyClass) selectedClassNode.getData();
		List<Instance> instances = ontClass.listInstances();
		Collections.sort(instances);
    	root2 = new DefaultTreeNode(new Instance(null), null);
    	for (Instance ind : instances){
    		new DefaultTreeNode(ind, root2);
    	}
    	instanceProperties = new ArrayList<InstanceProperty>();
	}
	
	public void onIndivindualNodeSelect(NodeSelectEvent event){
		selectedIndivindualNode = event.getTreeNode();
		updateIndivindualInfo();
	}
	
	private void updateIndivindualInfo(){
		Instance instance = (Instance) selectedIndivindualNode.getData();
		Map<String, List<String>> properties = instance.getProperties();
		instanceProperties = new ArrayList<InstanceProperty>();
		instanceProperties.add(new InstanceProperty("URI", instance.getUri()));
		for (Entry<String, List<String>> x : properties.entrySet()){
			for (String value : x.getValue()){
				instanceProperties.add(new InstanceProperty(x.getKey(), value));
			}
		}
	}
	
	public void selectIndivindual(String uri){
		OntClass ontClass = ontologyHandler.findClassOfIndivindual(uri);
		selectTreeNode(ontClass.getURI());
		updateIndivindualList();
		selectIndivindualTreeNode(uri);
		updateIndivindualInfo();
	}
	
	/**
	 * Searches all tree nodes and finds the one with the given uri
	 * Then sets this node as selected
	 * TODO: search all depth
	 * TODO: is expansion works for deeper nodes???
	 * @param uri
	 */
	private void selectTreeNode(String uri){
		root1 = ontologyHandler.createTreeNode();
		if (selectTreeNode(root1.getChildren(), uri)){
			root1.setExpanded(true);
		}
	}
	
	private boolean selectTreeNode(List<TreeNode> children, String uri){
		for (TreeNode node : children){
			OntologyClass deviceClass = (OntologyClass)node.getData();
			if (deviceClass.getUri().equals(uri)){
				setSelectedClassNode(node);
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
	
	/**
	 * Searches all tree nodes and finds the one with the given uri
	 * Then sets this node as selected
	 * TODO: search all depth
	 * TODO: is expansion works for deeper nodes???
	 * @param uri
	 */
	private void selectIndivindualTreeNode(String uri){
		if (selectIndivindualTreeNode(root2.getChildren(), uri)){
			root2.setExpanded(true);
		}
	}
	
	private boolean selectIndivindualTreeNode(List<TreeNode> children, String uri){
		for (TreeNode node : children){
			Instance instance = (Instance) node.getData();
			if (instance.getUri().equals(uri)){
				setSelectedIndivindualNode(node);
				node.setSelected(true);
				node.setExpanded(true);
				return true;
			}
			else {
				if (selectTreeNode(node.getChildren(), uri)){
					node.setExpanded(true);
				}
			}
		}	
		return false;
	}

	public OntologyHandler getOntologyHandler() {
		return ontologyHandler;
	}
	
    public TreeNode getRoot1() {
        return root1;
    }

	public TreeNode getRoot2() {
		return root2;
	}

	public void setOntologyHandler(OntologyHandler ontologyHandler) {
		this.ontologyHandler = ontologyHandler;
	}

	public TreeNode getSelectedClassNode() {
		return selectedClassNode;
	}

	public void setSelectedClassNode(TreeNode selectedClassNode) {
		this.selectedClassNode = selectedClassNode;
	}

	public TreeNode getSelectedIndivindualNode() {
		return selectedIndivindualNode;
	}

	public void setSelectedIndivindualNode(TreeNode selectedIndivindualNode) {
		this.selectedIndivindualNode = selectedIndivindualNode;
	}

	public List<InstanceProperty> getInstanceProperties() {
		return instanceProperties;
	}
	
	public boolean isIndivindual(String uri){
		if (uri.contains("http")) return true;
		return false;
	}
	
	public boolean isNotIndivindual(String uri){
		if (uri.contains("http")) return false;
		return true;
	}
}
