package org.activage.views;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;

import javax.faces.bean.ManagedProperty;
import javax.faces.context.FacesContext;
import javax.portlet.PortletResponse;
import javax.servlet.http.HttpServletResponse;

import org.activage.OntologyHandler;
import org.primefaces.context.RequestContext;
import org.primefaces.event.NodeSelectEvent;
import org.primefaces.model.StreamedContent;
import org.primefaces.model.TreeNode;

import com.hp.hpl.jena.rdf.model.Model;
import com.liferay.portal.util.PortalUtil;

public abstract class RootView {
	
	@ManagedProperty(value="#{ontologyHandler}")
	protected OntologyHandler ontologyHandler;
	
	protected TreeNode root1;
	protected TreeNode selectedNode;
	
	protected String selectedNodeName = "";
	protected String selectedNodeUri = "";
	protected String turtleDescription;

	
	/**
	 * Method called when a node is selected from the tree
	 * @param event
	 */
	public abstract void onNodeSelect(NodeSelectEvent event);
	
	public void openDescription(String uri){
		turtleDescription = "";
		try {
			turtleDescription = ontologyHandler.getOntologyDescription(uri);
			turtleDescription = turtleDescription.replaceAll("\n", "<br>");
			System.out.println(turtleDescription);
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
	public StreamedContent download(String uri, String name){   
		try {
			Model model = ontologyHandler.getOntologyDescriptionModel(uri);
			ByteArrayOutputStream output = new ByteArrayOutputStream();
			model.write(output, "TTL");
			
			InputStream stream = new ByteArrayInputStream(output.toByteArray());
			// 2. get Liferay's ServletResponse
			 PortletResponse portletResponse = (PortletResponse) FacesContext
			   .getCurrentInstance().getExternalContext().getResponse();
			 HttpServletResponse res = PortalUtil
			   .getHttpServletResponse(portletResponse);
			 res.setHeader("Content-Disposition", "attachment; filename=\"" + name +".txt" + "\"");//
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

	public String getTurtleDescription() {
		return turtleDescription;
	}

	public void setTurtleDescription(String turtleDescription) {
		this.turtleDescription = turtleDescription;
	}
}
