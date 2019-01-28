package org.activage.views;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;
 



import org.activage.OntologyHandler;
import org.primefaces.model.UploadedFile;
 
@ManagedBean(name = "fileUploadView")
@ViewScoped
/**
 * 
 * @author stavrotheodoros
 *
 * Bean for managing the view.xhtml
 */
public class FileUploadView {
     
	@ManagedProperty(value="#{ontologyHandler}")
	private OntologyHandler ontologyHandler;
	
    private UploadedFile file;
 
    public UploadedFile getFile() {
        return file;
    }
 
    public void setFile(UploadedFile file) {
        this.file = file;
    }
     
    /**
     * Method called when the 'Upload' button is pressed
     * Shows error message in case of Exceptions
     * If the ontology is ok, the next page is loaded
     * @return
     */
    public String upload() {
    	return "visualization?faces-redirect=true";
//        if(file != null) {
//        	ontologyHandler.setFile(new File("C:/Users/stavrotheodoros/Desktop/GOIoTPex.ttl"));
//        	try {
//				ontologyHandler.readOntologyModel();
//	            file = null;
//	            return "visualization?faces-redirect=true";
//			} catch (Exception e) {
//				e.printStackTrace();
//		        FacesMessage message = new FacesMessage("Could not parse the given ontology!!");
//		        FacesContext.getCurrentInstance().addMessage(null, message);
//		        return null;
//			}
//        }
//        FacesMessage message = new FacesMessage("Please select a file first");
//        FacesContext.getCurrentInstance().addMessage(null, message);
//        return null;
    }

	public OntologyHandler getOntologyHandler() {
		return ontologyHandler;
	}

	public void setOntologyHandler(OntologyHandler ontologyHandler) {
		this.ontologyHandler = ontologyHandler;
	}
}
