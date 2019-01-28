package org.activage.controller;

import javax.faces.bean.ApplicationScoped;
import javax.faces.bean.ManagedBean;

/**
 * 
 * @author stavrotheodoros
 *
 * Controller that contains all the links  of all the images
 * every page when needs an image asks this controller
 */
@ManagedBean(name = "imageController")
@ApplicationScoped
public class ImageController {
	
	private static final String FOLDER = "/resources/images/";
	private String explorer = FOLDER + "explorer.png";
	private String deviceSemantics = FOLDER + "deviceSemantics.png";
	private String serviceSemantics = FOLDER + "serviceSemantics.png";
	private String home = FOLDER + "Home48.png";	
	private String newClass = FOLDER + "newClass.png";	
	private String delete = FOLDER + "deleteIcon.png";	
	private String download = FOLDER + "download.png";

	
	public ImageController(){
		
	}	
	
	public String fetchExplorer(){
		return explorer;
	}
	
	public String fetchDeviceSemantics(){
		return deviceSemantics;
	}
	
	public String fetchServiceSemantics(){
		return serviceSemantics;
	}
	
	public String fetchHome(){
		return home;
	}
	
	public String fetchNewClass(){
		return newClass;
	}
	
	public String fetchDelete(){
		return delete;
	}
	
	public String fetchDownload(){
		return download;
	}
}
