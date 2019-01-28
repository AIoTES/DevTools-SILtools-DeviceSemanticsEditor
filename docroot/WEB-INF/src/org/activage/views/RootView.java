package org.activage.views;

import javax.faces.bean.ManagedProperty;

import org.activage.OntologyHandler;
import org.primefaces.event.NodeSelectEvent;
import org.primefaces.model.TreeNode;

public abstract class RootView {
	
	@ManagedProperty(value="#{ontologyHandler}")
	protected OntologyHandler ontologyHandler;
	
	protected TreeNode root1;
	protected TreeNode selectedNode;
	
	protected String selectedNodeName = "";
	protected String selectedNodeUri = "";
	
	/**
	 * Method called when a node is selected from the tree
	 * @param event
	 */
	public abstract void onNodeSelect(NodeSelectEvent event);
	
    public TreeNode getRoot1() {
        return root1;
    }
    
	public void setRoot1(TreeNode root1) {
		this.root1 = root1;
	}
    
    public TreeNode getSelectedNode() {
        return selectedNode;
    }
    
    public void setSelectedNode(TreeNode selectedNode) {
        this.selectedNode = selectedNode;
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
	
	public OntologyHandler getOntologyHandler() {
		return ontologyHandler;
	}

	public void setOntologyHandler(OntologyHandler ontologyHandler) {
		this.ontologyHandler = ontologyHandler;
	}
}
