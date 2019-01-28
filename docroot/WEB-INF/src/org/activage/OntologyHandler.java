package org.activage;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.faces.bean.ApplicationScoped;
import javax.faces.bean.ManagedBean;
import javax.faces.context.FacesContext;
import javax.servlet.ServletContext;

import org.activage.entities.AnnProperty;
import org.activage.entities.DataProperty;
import org.activage.entities.MyProperty;
import org.activage.entities.MyRestriction;
import org.activage.entities.ObjProperty;
import org.activage.entities.OntologyClass;
import org.activage.views.helper_entities.AnnotationItem;
import org.primefaces.model.DefaultTreeNode;
import org.primefaces.model.TreeNode;

import com.hp.hpl.jena.ontology.AnnotationProperty;
import com.hp.hpl.jena.ontology.DatatypeProperty;
import com.hp.hpl.jena.ontology.Individual;
import com.hp.hpl.jena.ontology.ObjectProperty;
import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntProperty;
import com.hp.hpl.jena.ontology.OntResource;
import com.hp.hpl.jena.ontology.Restriction;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.NodeIterator;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;

@ManagedBean(name = "ontologyHandler", eager = true)
@ApplicationScoped
/**
 * 
 * @author stavrotheodoros
 *
 * Class for loading and managing the given ontology
 * 
 */
public class OntologyHandler {

	// the uploaded ontology file
	private File file;
	private OntModel model;
	// contains the ontology in format appropriate for the d3 lib
	private String d3Model;

	public static final String ROOT_DEVICE_URI = "http://inter-iot.eu/GOIoTP#IoTDevice";
	public static final String ROOT_SERVICE_URI = "http://inter-iot.eu/GOIoTP#Service";

	public static final String THING = "Thing";
	public static final String ROOT = "root";
	public static final String RESOURCE = "Resource";
	private String resourcesPath;

	private static final String ONTOLOGY_NAME = "activage-core.owl";

	List<String> datatypeResources = Arrays.asList(
			"http://www.w3.org/2002/07/owl#rational",
			"http://www.w3.org/2002/07/owl#real",
			"http://www.w3.org/1999/02/22-rdf-syntax-ns#PlainLiteral",
			"http://www.w3.org/1999/02/22-rdf-syntax-ns#XMLLiteral",
			"http://www.w3.org/2000/01/rdf-schema#Literal",
			"http://www/opengis.net/ont/geosparql#wktLiteral",
			"http://www.w3.org/2001/XMLSchema#anyURI",
			"http://www/w3/org/2001/XMLSchema#base64Binary",
			"http://www/w3/org/2001/XMLSchema#boolean",
			"http://www/w3/org/2001/XMLSchema#byte",
			"http://www/w3/org/2001/XMLSchema#date",
			"http://www/w3/org/2001/XMLSchema#dateTime",
			"http://www/w3/org/2001/XMLSchema#dateTimeStamp",
			"http://www/w3/org/2001/XMLSchema#decimal",
			"http://www/w3/org/2001/XMLSchema#double",
			"http://www/w3/org/2001/XMLSchema#float",
			"http://www/w3/org/2001/XMLSchema#hexBinary",
			"http://www/w3/org/2001/XMLSchema#int",
			"http://www/w3/org/2001/XMLSchema#integer",
			"http://www/w3/org/2001/XMLSchema#language",
			"http://www/w3/org/2001/XMLSchema#long",
			"http://www/w3/org/2001/XMLSchema#Name",
			"http://www/w3/org/2001/XMLSchema#NCName",
			"http://www/w3/org/2001/XMLSchema#negativeInteger",
			"http://www/w3/org/2001/XMLSchema#NMTOKEN",
			"http://www/w3/org/2001/XMLSchema#nonNegativeInteger",
			"http://www/w3/org/2001/XMLSchema#nonPositiveInteger",
			"http://www/w3/org/2001/XMLSchema#normalizedString",
			"http://www/w3/org/2001/XMLSchema#positiveInteger",
			"http://www/w3/org/2001/XMLSchema#short",
			"http://www/w3/org/2001/XMLSchema#string",
			"http://www/w3/org/2001/XMLSchema#token",
			"http://www/w3/org/2001/XMLSchema#unsignedByte",
			"http://www/w3/org/2001/XMLSchema#unsignedInt",
			"http://www/w3/org/2001/XMLSchema#unsignedLong",
			"http://www/w3/org/2001/XMLSchema#unsignedShort");

	public OntologyHandler() {
		try {
			FacesContext context = FacesContext.getCurrentInstance();
			ServletContext servletContext = ((ServletContext) context.getExternalContext().getContext());
			resourcesPath = servletContext.getRealPath("/resources");
			String filepath = resourcesPath + "/ontologies/" + ONTOLOGY_NAME;
			file = new File(filepath);
			model = ModelFactory.createOntologyModel();
			readOntologyModel();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * loads the ontology from the file
	 * 
	 * @throws IOException
	 */
	public void readOntologyModel() throws Exception {
		if (file == null) {
			throw new Exception("File null");
		}
		InputStream in = new FileInputStream(file);
		if (file.getCanonicalPath().endsWith(".ttl")) {
			model = (OntModel) model.read(in, null, "TTL");
		}
		model = (OntModel) model.read(in, null, "");
	}

	/**
	 * parses the given ontology in order to create the data model for the D3
	 * lib
	 */
	public String createD3DataModel() {
		StringBuilder s = new StringBuilder("source,target,value\n");
		List<OntologyClass> hierarchyClasses = getHierarchyClasses();
		for (OntologyClass h : hierarchyClasses) {
			s.append(h.getOntClass().getLocalName() + "," + THING + ",1\n");
		}
		Map<String, OntClass> allClasses = new HashMap<String, OntClass>();
		for (ExtendedIterator<OntClass> i = model.listClasses(); i.hasNext();) {
			OntClass ontClass = i.next();
			if (!ontClass.isAnon()) {
				allClasses.put(ontClass.getURI(), ontClass);
			}
		}
		for (OntClass ontClass : allClasses.values()) {
			for (ExtendedIterator<OntClass> j = ontClass.listSuperClasses(true); j
					.hasNext();) {
				OntClass parent = j.next();
				String parentName = parent.getLocalName();
				if (parentName != null && !parentName.equals(RESOURCE)) {
					s.append(ontClass.getLocalName() + "," + parentName
							+ ",1\n");
				}
			}
		}
		d3Model = s.toString();
		return d3Model;
	}

	public TreeNode createTreeNode() {
		TreeNode thing = new DefaultTreeNode(new OntologyClass(THING,
				new ArrayList<String>()), null);
		List<OntologyClass> hierarchyClasses = getHierarchyClasses();
		for (OntologyClass ontClass : hierarchyClasses) {
			TreeNode node = new DefaultTreeNode(ontClass, thing);
			findChildren(ontClass, node,
					ontClass.getOntClass().listSubClasses(true));
		}
		return thing;
	}

	public TreeNode createServiceTreeNode() {
		TreeNode root = new DefaultTreeNode(new OntologyClass(ROOT,
				new ArrayList<String>()), null);

		OntClass deviceClass = model.getOntClass(ROOT_SERVICE_URI);
		TreeNode deviceRoot = new DefaultTreeNode(new OntologyClass(
				deviceClass, findParentName(deviceClass)), root);
		createServiceChildrenTreeNodes(deviceClass.listSubClasses(true),
				deviceRoot, new ArrayList<String>());

		OntClass procedureClass = model
				.getOntClass("http://www.w3.org/ns/sosa/Procedure");
		TreeNode procedureRoot = new DefaultTreeNode(new OntologyClass(
				procedureClass, findParentName(procedureClass)), root);
		createServiceChildrenTreeNodes(procedureClass.listSubClasses(true),
				procedureRoot, new ArrayList<String>());

		OntClass serviceInterfaceClass = model
				.getOntClass("http://inter-iot.eu/GOIoTP#ServiceInterface");
		TreeNode serviceInterfaceRoot = new DefaultTreeNode(new OntologyClass(
				serviceInterfaceClass, findParentName(serviceInterfaceClass)),
				root);
		createServiceChildrenTreeNodes(
				serviceInterfaceClass.listSubClasses(true),
				serviceInterfaceRoot, new ArrayList<String>());

		OntClass serviceInputClass = model
				.getOntClass("http://inter-iot.eu/GOIoTP#ServiceInput");
		TreeNode serviceInputRoot = new DefaultTreeNode(new OntologyClass(
				serviceInputClass, findParentName(serviceInputClass)), root);
		createServiceChildrenTreeNodes(serviceInputClass.listSubClasses(true),
				serviceInputRoot, new ArrayList<String>());

		OntClass serviceOutputClass = model
				.getOntClass("http://inter-iot.eu/GOIoTP#ServiceOutput");
		TreeNode serviceOutputRoot = new DefaultTreeNode(new OntologyClass(
				serviceOutputClass, findParentName(serviceOutputClass)), root);
		createServiceChildrenTreeNodes(serviceOutputClass.listSubClasses(true),
				serviceOutputRoot, new ArrayList<String>());

		OntClass dataFormatClass = model
				.getOntClass("http://inter-iot.eu/GOIoTP#DataFormat");
		TreeNode dataFormatRoot = new DefaultTreeNode(new OntologyClass(
				dataFormatClass, findParentName(dataFormatClass)), root);
		createServiceChildrenTreeNodes(dataFormatClass.listSubClasses(true),
				dataFormatRoot, new ArrayList<String>());

		return root;
	}

	public TreeNode createNewServiceTreeNode(List<String> mainNodes,
			List<String> unwantedClass) {
		TreeNode root = new DefaultTreeNode(new OntologyClass(ROOT,
				new ArrayList<String>()), null);
		for (String s : mainNodes) {
			root = createNewServiceTreeNode(root, s, unwantedClass);
		}
		return root;
	}

	private TreeNode createNewServiceTreeNode(TreeNode root,
			String mainNodeUri, List<String> unwantedClasses) {
		OntologyClass serviceClass = getOntologyClassByURI(mainNodeUri);
		TreeNode serviceRoot = new DefaultTreeNode(serviceClass, root);

		ExtendedIterator<OntClass> it = serviceClass.getOntClass()
				.listSubClasses(true);
		List<OntologyClass> children = new ArrayList<OntologyClass>();
		while (it.hasNext()) {
			OntClass child = it.next();

			OntClass cl = model.getOntClass(child.getURI());
			children.add(new OntologyClass(cl, findParentName(cl)));
		}
		Collections.sort(children);
		for (OntologyClass cl : children) {
			if (unwantedClasses.contains(cl.getOntClass().getURI()))
				continue;

			TreeNode clRoot = new DefaultTreeNode(cl, serviceRoot);
			createServiceChildrenTreeNodes(cl.getOntClass()
					.listSubClasses(true), clRoot, unwantedClasses);
		}
		return root;
	}

	public void createServiceChildrenTreeNodes(ExtendedIterator<OntClass> it,
			TreeNode parentRoot, List<String> unwantedClasses) {
		while (it.hasNext()) {
			OntClass child = it.next();
			if (unwantedClasses.contains(child.getURI()))
				continue;
			TreeNode childNode = new DefaultTreeNode(new OntologyClass(child,
					findParentName(child)), parentRoot);
			createServiceChildrenTreeNodes(child.listSubClasses(true),
					childNode, unwantedClasses);
		}
	}

	public void createDeviceChildrenTreeNodes(ExtendedIterator<OntClass> it,
			TreeNode parentRoot, List<String> unwantedClasses) {
		while (it.hasNext()) {
			OntClass child = it.next();
			if (unwantedClasses.contains(child.getURI()))
				continue;
			TreeNode childNode = new DefaultTreeNode(new OntologyClass(child,
					findParentName(child)), parentRoot);
			createDeviceChildrenTreeNodes(child.listSubClasses(true),
					childNode, unwantedClasses);
		}
	}

	public TreeNode createDeviceTreeNode() {
		List<String> unwantedClasses = Arrays.asList(
				"http://inter-iot.eu/GOIoTP#SoftwarePlatform",
				"http://inter-iot.eu/GOIoTP#PlatformComponent");
		TreeNode root = new DefaultTreeNode(new OntologyClass(ROOT,
				new ArrayList<String>()), null);

		OntClass platformClass = model
				.getOntClass("http://www.w3.org/ns/sosa/Platform");
		TreeNode platformRoot = new DefaultTreeNode(new OntologyClass(
				platformClass, findParentName(platformClass)), root);
		createDeviceChildrenTreeNodes(platformClass.listSubClasses(true),
				platformRoot, unwantedClasses);

		OntClass systemClass = model
				.getOntClass("http://www.w3.org/ns/ssn/System");
		TreeNode systemRoot = new DefaultTreeNode(new OntologyClass(
				systemClass, findParentName(systemClass)), root);
		createDeviceChildrenTreeNodes(systemClass.listSubClasses(true),
				systemRoot, unwantedClasses);

		OntClass locationClass = model
				.getOntClass("http://inter-iot.eu/GOIoTP#Location");
		TreeNode locationRoot = new DefaultTreeNode(new OntologyClass(
				locationClass, findParentName(locationClass)), root);
		createDeviceChildrenTreeNodes(locationClass.listSubClasses(true),
				locationRoot, unwantedClasses);

		OntClass observationClass = model
				.getOntClass("http://www.w3.org/ns/sosa/Observation");
		TreeNode observationRoot = new DefaultTreeNode(new OntologyClass(
				observationClass, findParentName(observationClass)), root);
		createDeviceChildrenTreeNodes(observationClass.listSubClasses(true),
				observationRoot, unwantedClasses);

		OntClass actuationClass = model
				.getOntClass("http://www.w3.org/ns/sosa/Actuation");
		TreeNode actuationRoot = new DefaultTreeNode(new OntologyClass(
				actuationClass, findParentName(actuationClass)), root);
		createDeviceChildrenTreeNodes(actuationClass.listSubClasses(true),
				actuationRoot, unwantedClasses);

		OntClass unitClass = model
				.getOntClass("http://sweet.jpl.nasa.gov/2.3/reprSciUnits.owl#Unit");
		TreeNode unitRoot = new DefaultTreeNode(new OntologyClass(unitClass,
				findParentName(unitClass)), root);
		createDeviceChildrenTreeNodes(unitClass.listSubClasses(true), unitRoot,
				unwantedClasses);

		return root;
	}

	public TreeNode createNewDeviceTreeNode(List<String> unwantedClass) {
		List<String> unwantedClasses = new ArrayList<String>();
		TreeNode root = new DefaultTreeNode(new OntologyClass(ROOT,
				new ArrayList<String>()), null);

		OntologyClass systemClass = getOntologyClassByURI("http://www.w3.org/ns/ssn/System");
		ExtendedIterator<OntClass> it = systemClass.getOntClass()
				.listSubClasses(true);
		List<OntologyClass> children = new ArrayList<OntologyClass>();
		while (it.hasNext()) {
			OntClass child = it.next();
			if (child.getLocalName().equals("PlatformComponent"))
				continue;

			OntClass cl = model.getOntClass(child.getURI());
			children.add(new OntologyClass(cl, findParentName(cl)));
		}
		Collections.sort(children);
		for (OntologyClass cl : children) {
			if (unwantedClass.contains(cl.getOntClass().getURI()))
				continue;

			TreeNode clRoot = new DefaultTreeNode(cl, root);
			createDeviceChildrenTreeNodes(
					cl.getOntClass().listSubClasses(true), clRoot,
					unwantedClasses);
		}
		return root;
	}

	/**
	 * Retrieves the same hierarchy classes as Protege
	 * 
	 * @return
	 */
	private List<OntologyClass> getHierarchyClasses() {
		Set<OntologyClass> hierarchyClasses = new HashSet<OntologyClass>();
		for (ExtendedIterator<OntClass> i = model.listHierarchyRootClasses(); i
				.hasNext();) {
			OntClass ontClass = i.next();
			if (ontClass.getLocalName() != null) {
				hierarchyClasses.add(new OntologyClass(ontClass,
						findParentName(ontClass)));
			} else {
				if (ontClass.isAnon()) {
					OntClass child = ontClass.getSubClass();
					boolean hasRegularParent = false;

					for (ExtendedIterator<OntClass> j = child
							.listSuperClasses(true); j.hasNext();) {
						OntClass parent = j.next();
						if (!parent.isAnon()
								&& !parent.getLocalName().equals(RESOURCE)) {
							hasRegularParent = true;
						}
					}
					if (!hasRegularParent) {
						List<String> parentNames = findParentName(child);
						hierarchyClasses.add(new OntologyClass(child,
								parentNames));
					}
				}
			}
		}
		Map<String, OntClass> allClasses = new HashMap<String, OntClass>();
		for (ExtendedIterator<OntClass> i = model.listClasses(); i.hasNext();) {
			OntClass ontClass = i.next();
			if (!ontClass.isAnon()) {
				allClasses.put(ontClass.getURI(), ontClass);
			}
		}
		for (ExtendedIterator<OntClass> i = model.listClasses(); i.hasNext();) {
			OntClass ontClass = i.next();
			for (ExtendedIterator<OntClass> j = ontClass.listSuperClasses(true); j
					.hasNext();) {
				OntClass parent = j.next();
				if (!parent.isAnon()
						&& !allClasses.containsKey(parent.getURI())) {
					if (!parent.getLocalName().equals(RESOURCE)) {
						List<String> parentNames = findParentName(parent);
						hierarchyClasses.add(new OntologyClass(parent,
								parentNames));
					}
				}
			}

		}
		List<OntologyClass> classes = new ArrayList<OntologyClass>(
				hierarchyClasses);
		Collections.sort(classes);
		return classes;
	}

	private void findChildren(OntologyClass ontClass, TreeNode parent,
			ExtendedIterator<OntClass> i) {
		while (i.hasNext()) {
			OntClass next = i.next();
			OntologyClass child = new OntologyClass(next, findParentName(next));
			TreeNode node = new DefaultTreeNode(child, parent);
			findChildren(child, node, child.getOntClass().listSubClasses(true));
		}
	}

	// /**
	// * Returns separated with comma all parents of the given class
	// *
	// * @param ontClass
	// * @return
	// */
	// private List<String> findParentName2(OntClass ontClass) {
	// List<String> parents = new ArrayList<String>();
	// if (ontClass == null)
	// return parents;
	// for (ExtendedIterator<OntClass> j = ontClass.listSuperClasses(true);
	// j.hasNext();) {
	// OntClass parent = j.next();
	// try{
	// if (parent.isRestriction()) {
	// Restriction restriction = parent.asRestriction();
	// String restrictionName = MyRestriction.getRestrictionLabel(restriction);
	// if (restrictionName != null && !parents.contains(restrictionName)) {
	// parents.add(restrictionName);
	// }
	// } else {
	// String parentName = parent.getLocalName();
	// if (parentName != null && !parentName.equals(RESOURCE)) {
	// if (!parents.contains(parentName) && parentName != null) {
	// parents.add(parentName);
	// }
	// }
	// }
	// }
	// catch (Exception ex){ex.printStackTrace();};
	// }
	// return parents;
	// }

	/**
	 * Returns separated with comma all parents of the given class
	 * 
	 * @param ontClass
	 * @return
	 */
	private List<String> findParentName(OntClass ontClass) {
		List<String> parents = new ArrayList<String>();
		if (ontClass == null)
			return parents;
		for (ExtendedIterator<OntClass> j = ontClass.listSuperClasses(true); j
				.hasNext();) {
			OntClass parent = j.next();
			try {
				if (!parent.isRestriction()) {
					String parentName = parent.getLocalName();
					if (parentName != null && !parentName.equals(RESOURCE)) {
						if (!parents.contains(parentName) && parentName != null) {
							parents.add(parentName);
						}
					}
				}
			} catch (Exception ex) {
				ex.printStackTrace();
			}
			;
		}
		return parents;
	}

	public TreeNode createAnnotationPropertyTreeNode() {
		TreeNode thing = new DefaultTreeNode(new AnnProperty(AnnProperty.ROOT),
				null);
		Set<AnnProperty> allAnnProperties = new HashSet<AnnProperty>();
		for (ExtendedIterator<AnnotationProperty> i = model
				.listAnnotationProperties(); i.hasNext();) {
			AnnotationProperty annotationProperty = i.next();
			if (annotationProperty.getLocalName() != null) {
				allAnnProperties.add(new AnnProperty(annotationProperty));
			}
			for (ExtendedIterator<? extends OntProperty> j = annotationProperty
					.listSuperProperties(true); j.hasNext();) {
				AnnotationProperty parent = (AnnotationProperty) j.next();
				String parentName = parent.getLocalName();
				if (parentName != null) {
					allAnnProperties.add(new AnnProperty(parent));
				}
			}
		}
		List<AnnProperty> propertyList = new ArrayList<AnnProperty>(
				allAnnProperties);
		Collections.sort(propertyList);
		for (AnnProperty o : propertyList) {
			ExtendedIterator<? extends OntProperty> j = o
					.getAnnotationProperty().listSuperProperties(true);
			if (!j.hasNext()) {
				TreeNode node = new DefaultTreeNode(o, thing);
				findChildrenAnnotationProperties(o, node, o
						.getAnnotationProperty().listSubProperties(true));
			}
		}
		return thing;
	}

	private void findChildrenAnnotationProperties(
			AnnProperty annotationProperty, TreeNode parent,
			ExtendedIterator<? extends OntProperty> i) {
		while (i.hasNext()) {
			AnnProperty child = new AnnProperty((AnnotationProperty) i.next());
			TreeNode node = new DefaultTreeNode(child, parent);
			findChildrenAnnotationProperties(child, node, child
					.getAnnotationProperty().listSubProperties(true));
		}
	}

	public TreeNode createObjectPropertyTreeNode() {
		TreeNode thing = new DefaultTreeNode(new ObjProperty(ObjProperty.ROOT),
				null);
		try {
			Set<ObjProperty> allObjProperties = new HashSet<ObjProperty>();
			for (ExtendedIterator<ObjectProperty> i = model
					.listObjectProperties(); i.hasNext();) {
				ObjectProperty objectProperty = i.next();
				if (objectProperty.getLocalName() != null) {
					allObjProperties.add(new ObjProperty(objectProperty));
				}
			}
			List<ObjProperty> propertyList = new ArrayList<ObjProperty>(
					allObjProperties);
			Collections.sort(propertyList);
			for (ObjProperty o : propertyList) {
				TreeNode node = new DefaultTreeNode(o, thing);
				findChildrenObjectProperties(o, node, o.getObjectProperty()
						.listSubProperties(true));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return thing;
	}

	private void findChildrenObjectProperties(ObjProperty objectProperty,
			TreeNode parent, ExtendedIterator<? extends OntProperty> i) {
		while (i.hasNext()) {
			OntProperty p = i.next();
			try {
				ObjectProperty childObjectProperty = p.asObjectProperty();
				ObjProperty child = new ObjProperty(childObjectProperty);

				TreeNode node = new DefaultTreeNode(child, parent);
				findChildrenObjectProperties(child, node, child
						.getObjectProperty().listSubProperties(true));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public TreeNode createDataPropertyTreeNode() {
		TreeNode thing = new DefaultTreeNode(
				new DataProperty(DataProperty.ROOT), null);
		Set<DataProperty> allDataProperties = new HashSet<DataProperty>();
		for (ExtendedIterator<DatatypeProperty> i = model
				.listDatatypeProperties(); i.hasNext();) {
			DatatypeProperty datatypeProperty = i.next();
			if (datatypeProperty.getLocalName() != null) {
				allDataProperties.add(new DataProperty(datatypeProperty));
			}
		}
		List<DataProperty> propertyList = new ArrayList<DataProperty>(
				allDataProperties);
		for (DataProperty o : propertyList) {
			TreeNode node = new DefaultTreeNode(o, thing);
			findChildrenDataProperties(o, node, o.getDatatypeProperty()
					.listSubProperties(true));
		}
		return thing;
	}

	private void findChildrenDataProperties(DataProperty objectProperty,
			TreeNode parent, ExtendedIterator<? extends OntProperty> i) {
		while (i.hasNext()) {
			OntProperty p = i.next();
			try {
				DataProperty child = new DataProperty((DatatypeProperty) p);

				TreeNode node = new DefaultTreeNode(child, parent);
				findChildrenDataProperties(child, node, child
						.getDatatypeProperty().listSubProperties(true));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public String getD3Model() {
		return d3Model;
	}

	public void setD3Model(String d3Model) {
		this.d3Model = d3Model;
	}

	public File getFile() {
		return file;
	}

	public void setFile(File file) {
		this.file = file;
	}

	/**
	 * This method lists all data properties of the model Then for each property
	 * checks the domain If the given class belongs to the property's domain or
	 * there is no domain constraint (all classes belong to this domain) then it
	 * is added in a list
	 * 
	 * @param classURI
	 * @return
	 */
	public List<DataProperty> getDataPropertiesOfClass(String classURI) {
		List<DataProperty> dataProperties = new ArrayList<DataProperty>();
		try {
			for (ExtendedIterator<DatatypeProperty> i = model
					.listDatatypeProperties(); i.hasNext();) {
				DatatypeProperty datatypeProperty = i.next();
				if (datatypeProperty.getLocalName() != null) {
					List<String> domains = findDomains(datatypeProperty);
					if (domains.contains(classURI)) {
						dataProperties.add(new DataProperty(datatypeProperty));
					}
				}
			}
			List<Property> restrictionProperties = new ArrayList<Property>();
			findClassRestrictionProperties(model.getOntClass(classURI),
					restrictionProperties, false);
			for (Property p : restrictionProperties) {
				DatatypeProperty d = model.getDatatypeProperty(p.getURI());
				if (d != null) {
					DataProperty dp = new DataProperty(d);
					if (!dataProperties.contains(dp)) {
						dataProperties.add(new DataProperty(d));
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		Collections.sort(dataProperties);
		return dataProperties;
	}

	/**
	 * This method lists all data properties of the model Then for each property
	 * checks the domain If the given class belongs to the property's domain
	 * then it is added in a list
	 * 
	 * @param classURI
	 * @return
	 */
	public List<DataProperty> getExclusiveDataPropertiesOfClass(String classURI) {
		List<DataProperty> dataProperties = new ArrayList<DataProperty>();
		try {
			for (ExtendedIterator<DatatypeProperty> i = model
					.listDatatypeProperties(); i.hasNext();) {
				DatatypeProperty datatypeProperty = i.next();
				if (datatypeProperty.getLocalName() != null) {
					List<String> domains = findDomains(datatypeProperty);
					if (domains.contains(classURI)) {
						dataProperties.add(new DataProperty(datatypeProperty));
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		Collections.sort(dataProperties);
		return dataProperties;
	}

	public List<ObjProperty> getObjectPropertiesOfClass(String classURI) {
		List<ObjProperty> objectProperties = new ArrayList<ObjProperty>();
		for (ExtendedIterator<ObjectProperty> i = model.listObjectProperties(); i
				.hasNext();) {
			ObjectProperty objectProperty = i.next();
			if (objectProperty.getLocalName() != null) {
				List<String> domains = new ArrayList<String>();
				ExtendedIterator<? extends OntResource> j = objectProperty
						.listDomain();
				while (j.hasNext()) {
					OntResource domain = j.next();
					domains.add(domain.getURI());
				}
				if (domains.contains(classURI)) {
					objectProperties.add(new ObjProperty(objectProperty));
				}
			}
		}
		List<Property> restrictionProperties = new ArrayList<Property>();
		findClassRestrictionProperties(model.getOntClass(classURI),
				restrictionProperties, false);
		for (Property p : restrictionProperties) {
			ObjectProperty o = model.getObjectProperty(p.getURI());
			if (o != null) {
				ObjProperty obj = new ObjProperty(o);
				if (!objectProperties.contains(obj)) {
					objectProperties.add(obj);
				}
			}
		}
		Collections.sort(objectProperties);
		return objectProperties;
	}

	public List<ObjProperty> getExclusiveObjectPropertiesOfClass(String classURI) {
		List<ObjProperty> objectProperties = new ArrayList<ObjProperty>();
		for (ExtendedIterator<ObjectProperty> i = model.listObjectProperties(); i
				.hasNext();) {
			ObjectProperty objectProperty = i.next();
			if (objectProperty.getLocalName() != null) {
				List<String> domains = new ArrayList<String>();
				ExtendedIterator<? extends OntResource> j = objectProperty
						.listDomain();
				while (j.hasNext()) {
					OntResource domain = j.next();
					domains.add(domain.getURI());
				}
				if (domains.contains(classURI)) {
					objectProperties.add(new ObjProperty(objectProperty));
				}
			}
		}
		Collections.sort(objectProperties);
		return objectProperties;
	}

	/**
	 * Creates a new class in the ontology by using the given info
	 * 
	 * @param uri
	 *            : the uri of the class we want to create
	 * @param comment
	 *            : a comment that explains the class
	 * @param parentURI
	 *            : the uri of the parent class
	 * @return
	 * @throws Exception
	 */
	public OntClass createNewClass(String uri, String comment, String parentURI)
			throws Exception {
		if (model.getOntClass(uri) != null) {
			throw new Exception(
					"There is already a device class with the given name");
		}
		OntClass ontClass = model.createClass(uri);
		OntClass parent = model.getOntClass(parentURI);
		if (parent != null) {
			ontClass.setSuperClass(parent);
		}
		if (comment != null && !comment.isEmpty()) {
			ontClass.setComment(comment, null);
		}
		return ontClass;
	}

	/**
	 * Creates a new datatype property in the ontology by using the given info
	 * 
	 * @param uri
	 *            : the uri of the datatype property we want to create
	 * @param comment
	 *            : a comment that explains the property
	 * @param domainUri
	 *            : the uri of a class that will be added as domain
	 * @throws Exception
	 */
	public void createNewDatatypeProperty(String uri, String comment,
			String domainUri) throws Exception {
		if (model.getDatatypeProperty(uri) != null) {
			throw new Exception(
					"There is already a datatype property with the given name");
		}
		DatatypeProperty prop = model.createDatatypeProperty(uri);
		prop.setComment(comment, null);
		OntClass domain = model.getOntClass(domainUri);
		if (domain != null) {
			prop.addDomain(domain);
		}
	}

	/**
	 * Creates a new object property in the ontology by using the given info
	 * 
	 * @param uri
	 *            : the uri of the object property we want to create
	 * @param comment
	 *            : a comment that explains the property
	 * @param domainUri
	 *            : the uri of a class that will be added as domain
	 * @throws Exception
	 */
	public void createNewObjectProperty(String uri, String comment,
			String domainUri) throws Exception {
		if (model.getObjectProperty(uri) != null) {
			throw new Exception(
					"There is already an object property with the given name");
		}
		ObjectProperty prop = model.createObjectProperty(uri);
		prop.setComment(comment, null);
		OntClass domain = model.getOntClass(domainUri);
		if (domain != null) {
			prop.addDomain(domain);
		}
	}

	/**
	 * Deletes a class from our ontology
	 * 
	 * @param uri
	 *            : the uri of the class to be deleted
	 */
	public void deleteClass(String uri) {
		OntClass cl = model.getOntClass(uri);
		if (cl != null) {
			cl.remove();
		}
	}

	public void deleteDatatypeProperty(String uri) {
		DatatypeProperty p = model.getDatatypeProperty(uri);
		if (p != null) {
			model.removeAll(null, p, null);
			p.remove();
		}
	}

	public OntologyClass getOntologyClassByURI(String uri) {
		OntClass ontClass = model.createClass(uri);
		return new OntologyClass(ontClass, findParentName(ontClass));
	}

	public OntClass findClassOfIndivindual(String uri) {
		Individual ind = model.getIndividual(uri);
		OntClass ontClass = ind.getOntClass();
		return ontClass;
	}

	public List<OntologyClass> findDomainClasses(String propertyUri) {
		Property p = model.getProperty(propertyUri);
		List<String> domains = findDomains(p);
		List<OntologyClass> result = new ArrayList<OntologyClass>();
		for (String uri : domains) {
			OntClass ontClass = model.getOntClass(uri);
			if (ontClass != null) {
				result.add(new OntologyClass(ontClass, findParentName(ontClass)));
			}

		}
		return result;
	}

	public List<String> findDomains(String propertyUri) {
		DatatypeProperty p = (DatatypeProperty) model.getProperty(propertyUri);
		return findDomains(p);
	}

	/**
	 * Retrieves from the given property all its domains it also checks the
	 * equivalent annotation property
	 */
	public List<String> findDomains(Property p) {
		List<String> domains = new ArrayList<String>();
		AnnotationProperty prop = model.getOntResource(
				"http://schema.org/domainIncludes").asAnnotationProperty();
		ExtendedIterator<? extends OntResource> j;
		NodeIterator it;
		if (p instanceof DatatypeProperty) {
			j = ((DatatypeProperty) p).listDomain();
			it = ((DatatypeProperty) p).listPropertyValues(prop);
		} else if (p instanceof ObjectProperty) {
			j = ((ObjectProperty) p).listDomain();
			it = ((ObjectProperty) p).listPropertyValues(prop);
		} else {
			return domains;
		}
		while (j.hasNext()) {
			OntResource domain = j.next();
			domains.add(domain.getURI());
		}
		while (it.hasNext()) {
			RDFNode node = it.next();
			domains.add(node.asResource().getURI());
		}
		return domains;
	}

	/**
	 * Retrieves from the ontology the property with the given uri then checks
	 * if it's a datatype or an object property and then list all its range
	 * 
	 * @param propertyURI
	 *            : the URI of the property
	 * @return the URIs of all the ranges
	 */
	public List<String> findPropertyRanges(String propertyURI) {
		List<String> ranges = new ArrayList<String>();
		Property p = model.getProperty(propertyURI);
		ExtendedIterator<? extends OntResource> j;
		if (p instanceof DatatypeProperty) {
			j = ((DatatypeProperty) p).listRange();
		} else if (p instanceof ObjectProperty) {
			j = ((ObjectProperty) p).listRange();
		} else {
			return ranges;
		}
		while (j.hasNext()) {
			OntResource domain = j.next();
			ranges.add(domain.getURI());
		}
		return ranges;
	}

	/**
	 * from the given property, returns the classes that are its range
	 * 
	 * @param propertyURI
	 *            : the URI of the property
	 * @return
	 */
	public List<OntologyClass> findPropertyRangeClasses(String propertyURI) {
		List<OntologyClass> result = new ArrayList<OntologyClass>();
		for (String rangeUri : findPropertyRanges(propertyURI)) {
			OntClass ontClass = model.getOntClass(rangeUri);
			if (ontClass != null) {
				OntologyClass ont = new OntologyClass(ontClass,
						findParentName(ontClass));
				result.add(ont);
			}
		}
		return result;
	}

	/**
	 * This method uses the given resource in order to find all its annotation
	 * properties and the corresponding values
	 * 
	 * @param ontResource
	 * @return
	 */
	public Map<String, String> getAnnotationPropertyValues(
			OntResource ontResource) {
		ExtendedIterator<AnnotationProperty> it = model
				.listAnnotationProperties();
		Map<String, String> map = new HashMap<String, String>();
		while (it.hasNext()) {
			AnnotationProperty prop = it.next();
			RDFNode node = ontResource.getPropertyValue(prop);
			if (node != null) {
				String propertyShortName = getResourceShortName(prop);
				if (node.isLiteral()) {
					map.put(propertyShortName, node.asLiteral().getString());
				} else if (node.isResource()) {
					map.put(propertyShortName, node.asResource().getURI());
				} else {
					map.put(propertyShortName, node.toString());
				}
			}
		}
		String label = ontResource.getLabel(null);
		if (label != null) {
			map.put("rdfs:label", label);
		}

		String comment = ontResource.getComment(null);
		if (comment != null) {
			map.put("rdfs:comment", comment);
		}
		Resource definedBy = ontResource.getIsDefinedBy();

		if (definedBy != null) {
			map.put("rdfs:isDefinedBy", definedBy.toString());
		}
		return map;
	}

	public List<AnnotationItem> getOrderedAnnotationPairs(
			OntResource ontResource) {
		Map<String, String> map = getAnnotationPropertyValues(ontResource);
		List<AnnotationItem> list = new ArrayList<AnnotationItem>();
		for (Entry<String, String> x : map.entrySet()) {
			list.add(new AnnotationItem(x.getKey(), x.getValue()));
		}
		Collections.sort(list);
		return list;
	}

	public void findClassRestrictions(OntClass cl) {
		try {
			ExtendedIterator<OntClass> it = cl.listSuperClasses(true);
			while (it.hasNext()) {
				OntClass parent = it.next();
				if (parent.isRestriction()) {
					Restriction restriction = parent.asRestriction();
					String restrictionName = MyRestriction
							.getRestrictionLabel(restriction);
					// System.out.println(restrictionName);
					// MaxCardinalityQRestriction
					// MinCardinalityQRestriction
					// MinCardinalityRestriction
					// QualifiedRestriction
					// if (restriction.isSomeValuesFromRestriction()){
					// SomeValuesFromRestriction someValuesFromRestriction =
					// restriction.asSomeValuesFromRestriction();
					// System.out.println("isSomeValuesFromRestriction: " +
					// someValuesFromRestriction.getSomeValuesFrom());
					// }
					// else if (restriction.isAllValuesFromRestriction()){
					// AllValuesFromRestriction allValuesFromRestriction =
					// restriction.asAllValuesFromRestriction();
					// System.out.println("allValuesFromRestriction: " +
					// allValuesFromRestriction.getAllValuesFrom());
					// }
					// else if (restriction.isHasValueRestriction()){
					// HasValueRestriction hasValueRestriction =
					// restriction.asHasValueRestriction();
					// System.out.println("hasValueRestriction: " +
					// hasValueRestriction.getHasValue());
					// }
					// else if (restriction.isMaxCardinalityRestriction()){
					// MaxCardinalityRestriction maxCardinalityRestriction =
					// restriction.asMaxCardinalityRestriction();
					// System.out.println("maxCardinalityRestriction: " +
					// maxCardinalityRestriction.getMaxCardinality());
					// }
					// else if (restriction.isMinCardinalityRestriction()){
					// MinCardinalityRestriction minCardinalityRestriction =
					// restriction.asMinCardinalityRestriction();
					// System.out.println("minCardinalityRestriction: " +
					// minCardinalityRestriction.getMinCardinality());
					// }
					// else if (restriction.isCardinalityRestriction()){
					// CardinalityRestriction cardinalityRestriction =
					// restriction.asCardinalityRestriction();
					// System.out.println("cardinalityRestriction: " +
					// cardinalityRestriction.getCardinality());
					// }
				} else {
					findClassRestrictions(parent);
				}

			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void findClassRestrictionsNames(OntClass cl,
			List<String> restrictionNames, boolean direct) {
		List<MyRestriction> restrictions = new ArrayList<MyRestriction>();
		findClassRestrictions(cl, restrictions, direct);
		for (MyRestriction r : restrictions) {
			String restrictionName = MyRestriction.getRestrictionLabel(r
					.getRestriction());
			if (restrictionName != null
					&& !restrictionNames.contains(restrictionName)) {
				restrictionNames.add(restrictionName);
			}
		}
	}

	public void findClassRestrictions(OntClass cl,
			List<MyRestriction> restrictions, boolean direct) {
		try {
			ExtendedIterator<OntClass> it = cl.listSuperClasses(true);
			while (it.hasNext()) {
				OntClass parent = it.next();
				if (parent.isRestriction()) {
					Restriction restriction = parent.asRestriction();
					try {
						restriction.getOnProperty();
						restrictions.add(new MyRestriction(restriction));
					} catch (Exception e) {
					}
					;
				} else {
					if (!direct) {
						findClassRestrictions(parent, restrictions, direct);
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void findClassRestrictionProperties(OntClass cl,
			List<Property> properties, boolean direct) {
		List<MyRestriction> restrictions = new ArrayList<MyRestriction>();
		findClassRestrictions(cl, restrictions, direct);
		for (MyRestriction r : restrictions) {
			try {
				OntProperty p = r.getRestriction().getOnProperty();
				if (!properties.contains(p)) {
					properties.add(r.getRestriction().getOnProperty());
				}
			} catch (Exception e) {
			}
			;
		}
	}

	public void removeRestriction(OntClass cl, Restriction r) {
		r.remove();
	}

	public Set<String> getImportedOntologyNamespaces() {
		Set<String> namespaces = new HashSet<String>();
		namespaces.add("http://inter-iot.eu/GOIoTP#");
		namespaces.add("http://inter-iot.eu/GOIoTPex#");
		namespaces.add("http://www.w3.org/ns/ssn/");
		namespaces.add("http://www.w3.org/ns/sosa/");
		namespaces.add("http://sweet.jpl.nasa.gov/2.3/reprSciUnits.owl#");
		return namespaces;
	}

	public void removeAnnotationPropertyFromClass(String classURI,
			AnnotationItem annItem) {
		OntClass cl = model.getOntClass(classURI);
		if (cl == null)
			return;
		AnnotationProperty p = findAnnotationPropertyByShortName(annItem
				.getLabel());
		if (p != null) {
			RDFNode value = cl.getPropertyValue(p);
			cl.removeProperty(p, value);
		}
		if (annItem.getLabel().equals("rdfs:comment")) {
			cl.removeComment(annItem.getValue(), "en");
			cl.removeComment(annItem.getValue(), null);
		}
		if (annItem.getLabel().equals("rdfs:label")) {
			cl.removeLabel(annItem.getValue(), null);
			cl.removeLabel(annItem.getValue(), "en");
		}
	}

	public void addNewAnnotationPropertyFromClass(String classURI,
			AnnotationItem annItem) {
		OntClass cl = model.getOntClass(classURI);
		if (cl == null)
			return;
		AnnotationProperty p = findAnnotationPropertyByShortName(annItem
				.getLabel());
		if (p != null) {
			cl.addProperty(p, annItem.getValue());
		}
		if (annItem.getLabel().equals("rdfs:comment")) {
			cl.addComment(annItem.getValue(), null);
		}
		if (annItem.getLabel().equals("rdfs:label")) {
			cl.addLabel(annItem.getValue(), null);
		}
	}

	private AnnotationProperty findAnnotationPropertyByShortName(
			String shortName) {
		ExtendedIterator<AnnotationProperty> x = model
				.listAnnotationProperties();
		while (x.hasNext()) {
			AnnotationProperty p = x.next();
			String propertyShortName = getResourceShortName(p);
			if (propertyShortName.equals(shortName)) {
				return p;
			}
		}
		return null;
	}
	
	public String getResourceShortName(String resourceUri) {
		Resource r = model.getResource(resourceUri);
		if (r == null) return null;
		return getResourceShortName(r);
	}

	public String getResourceShortName(Resource r) {
		String prefix =  model.getNsURIPrefix(r.getNameSpace());
		if (prefix == null){
			return r.getLocalName();
		}
		return prefix + ":" + r.getLocalName();
	}

	public List<String> getAllAnnotationPropertyNames() {
		List<String> annotationPropertyNames = new ArrayList<String>();
		ExtendedIterator<AnnotationProperty> it = model
				.listAnnotationProperties();
		while (it.hasNext()) {
			AnnotationProperty prop = it.next();
			String propertyShortName = getResourceShortName(prop);
			annotationPropertyNames.add(propertyShortName);
		}
		annotationPropertyNames.add("rdfs:label");
		annotationPropertyNames.add("rdfs:comment");
		annotationPropertyNames.add("rdfs:isDefinedBy");
		Collections.sort(annotationPropertyNames);
		return annotationPropertyNames;
	}

	public List<MyProperty> findAcceptedDataPropertiesOfClass(String classUri) {
		List<MyProperty> dataProperties = new ArrayList<MyProperty>();
		try {
			for (ExtendedIterator<DatatypeProperty> i = model
					.listDatatypeProperties(); i.hasNext();) {
				DatatypeProperty datatypeProperty = i.next();
				if (datatypeProperty.getLocalName() != null) {
					List<String> domains = findDomains(datatypeProperty);
					if (domains.isEmpty() || domains.contains(classUri)) {
						dataProperties.add(new DataProperty(datatypeProperty));
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		Collections.sort(dataProperties);
		return dataProperties;
	}

	public List<MyProperty> findAcceptedObjectPropertiesOfClass(String classUri) {
		List<MyProperty> objectProperties = new ArrayList<MyProperty>();
		for (ExtendedIterator<ObjectProperty> i = model.listObjectProperties(); i
				.hasNext();) {
			ObjectProperty objectProperty = i.next();
			if (objectProperty.getLocalName() != null) {
				List<String> domains = new ArrayList<String>();
				ExtendedIterator<? extends OntResource> j = objectProperty
						.listDomain();
				while (j.hasNext()) {
					OntResource domain = j.next();
					domains.add(domain.getURI());
				}
				if (domains.isEmpty() || domains.contains(classUri)) {
					objectProperties.add(new ObjProperty(objectProperty));
				}
			}
		}
		Collections.sort(objectProperties);
		return objectProperties;
	}

	public DatatypeProperty getDatatypeProperty(String uri) {
		return model.getDatatypeProperty(uri);
	}

	public ObjectProperty getObjectProperty(String uri) {
		return model.getObjectProperty(uri);
	}

	public List<String> findObjectPropertyRanges(ObjectProperty p) {
		Set<String> ranges = new HashSet<String>();
		ExtendedIterator<? extends OntResource> j = p.listRange();
		while (j.hasNext()) {
			OntResource domain = j.next();
			if (domain instanceof OntClass) {
				OntClass cl = (OntClass) domain;
				String name = cl.getLocalName();
				if (name != null) {
					ranges.add(name);
				}
				ExtendedIterator<OntClass> it = cl.listSubClasses();
				while (it.hasNext()) {
					name = it.next().getLocalName();
					if (name != null) {
						ranges.add(name);
					}
				}
			}
		}
		if (ranges.isEmpty()) {
			ExtendedIterator<OntClass> it = model.listClasses();
			while (it.hasNext()) {
				String name = it.next().getLocalName();
				if (name != null) {
					ranges.add(name);
				}
			}
		}
		List<String> result = new ArrayList<String>(ranges);
		Collections.sort(result);
		return result;
	}

	public OntClass findClassByLocalname(String name) {
		ExtendedIterator<OntClass> it = model.listClasses();
		while (it.hasNext()) {
			OntClass cl = it.next();
			String n = cl.getLocalName();
			if (name != null && name.equals(n)) {
				return cl;
			}
		}
		return null;
	}

	public void removeParent(OntClass ontClass, String parentLocalName) {
		ExtendedIterator<OntClass> i = ontClass.listSuperClasses(true);
		OntClass parentClass = null;
		while (i.hasNext()) {
			OntClass parent = i.next();
			String parentName = parent.getLocalName();
			if (parentName != null && parentName.equals(parentLocalName)) {
				parentClass = parent;
				break;
			}
		}
		if (parentClass != null) {
			ontClass.removeSuperClass(parentClass);
		}
	}

	public void saveNewRestriction(String selectedNodeUri,
			MyProperty restrictionProperty, String selectedRestrictionType,
			String selectedRestrictionValue) {
		OntClass cl = model.getOntClass(selectedNodeUri);
		if (cl == null)
			return;
		Property p = model.getProperty(restrictionProperty.getUri());
		if (p == null)
			return;

		if (selectedRestrictionType.equals("only") || selectedRestrictionType.equals("some")) {
			Resource value = findClassByLocalname(selectedRestrictionValue);
			if (value == null){
				value = model.getResource(selectedRestrictionValue);
				if (value == null){
					return;
				}
			}
			
			Restriction r;
			if (selectedRestrictionType.equals("only")) {
				r = model.createAllValuesFromRestriction(null, p, value);
			} else {
				r = model.createSomeValuesFromRestriction(null, p, value);
			}
			cl.addSuperClass(r);
		} else if (selectedRestrictionType.equals("max") || selectedRestrictionType.equals("min") || selectedRestrictionType.equals("exactly")) {
			try {
				Integer value = Integer.parseInt(selectedRestrictionValue);
				Restriction r;
				if (selectedRestrictionType.equals("max")) {
					r = model.createMaxCardinalityRestriction(null, p, value);
				} else if (selectedRestrictionType.equals("min")) {
					r = model.createMinCardinalityRestriction(null, p, value);
				} else {
					r = model.createCardinalityRestriction(null, p, value);
				}
				cl.addSuperClass(r);
			} catch (Exception ex) {}
		}
	}

	public List<String> getDatatypeResources() {
		return datatypeResources;
	}
	
	public List<String> getDatatypeResourcesShort() {
		List<String> result = new ArrayList<String>();
		for (String s : datatypeResources){
			Resource r = model.getResource(s);
			result.add(getResourceShortName(r));
		}
		return result;
	}
	
	public String findDatatypeResourceByShortName(String shortName){
		for (String s : datatypeResources){
			Resource r = model.getResource(s);
			if (getResourceShortName(r).equals(shortName)){
				return s;
			}
		}
		return null;
	}
	
	public void createDataTypeProperty(String propertyUri, String domainUri, String comment, String rangeUri){
		DatatypeProperty p = model.createDatatypeProperty(propertyUri);
		p.addDomain( model.getOntClass( domainUri ) );
		if (rangeUri != null){
			p.addRange(model.getResource(rangeUri));
		}
		if (comment != null && !comment.isEmpty()){
			p.addComment(comment, null);
		}
	}
	
	public void updateDataTypeProperty(String propertyUri, String oldComment, String newComment, String newRangeUri){
		DatatypeProperty p = model.getDatatypeProperty(propertyUri);
		if (oldComment != null && !oldComment.isEmpty()){
			p.removeComment(oldComment, null);
		}
		if (newComment != null && !newComment.isEmpty()){
			p.addComment(newComment, null);
		}
		ExtendedIterator<? extends OntResource> it = p.listRange();
		List<Resource> ranges = new ArrayList<Resource>();
		while (it.hasNext()){
			Resource range = it.next();
			ranges.add(range);
		}
		for (Resource r : ranges){
			p.removeRange(r);
		}
		if (newRangeUri != null && !newRangeUri.isEmpty()){
			p.addRange(model.getResource(newRangeUri));
		}	
	}
	
	public void createObjectProperty(String propertyUri, String domainUri, String comment, TreeNode rangeNode){
		ObjectProperty p = model.createObjectProperty(propertyUri);
		p.addDomain( model.getOntClass( domainUri ) );
		if (rangeNode != null){
			OntologyClass range = (OntologyClass) rangeNode.getData();
			p.addRange(model.getOntClass(range.getUri()));
		}
		if (comment != null && !comment.isEmpty()){
			p.addComment(comment, null);
		}
	}
	
	public void updateObjectProperty(String propertyUri, String oldComment, String newComment, TreeNode newRange){
		ObjectProperty p = model.getObjectProperty(propertyUri);
		if (oldComment != null && !oldComment.isEmpty()){
			p.removeComment(oldComment, null);
		}
		if (newComment != null && !newComment.isEmpty()){
			p.addComment(newComment, null);
		}
		ExtendedIterator<? extends OntResource> it = p.listRange();
		List<Resource> ranges = new ArrayList<Resource>();
		while (it.hasNext()){
			Resource range = it.next();
			ranges.add(range);
		}
		for (Resource r : ranges){
			p.removeRange(r);
		}
		if (newRange != null){
			OntologyClass range = (OntologyClass) newRange.getData();
			p.addRange(model.getOntClass(range.getUri()));
		}	
	}
	
	public DatatypeProperty findDataPropertyByShortName(String shortName){
		ExtendedIterator<DatatypeProperty> it = model.listDatatypeProperties();
		while (it.hasNext()){
			DatatypeProperty dp = it.next();
			if (getResourceShortName(dp).equals(shortName)){
				return dp;
			}
		}
		return null;
	}
	
	public ObjectProperty findObjectPropertyByShortName(String shortName){
		ExtendedIterator<ObjectProperty> it = model.listObjectProperties();
		while (it.hasNext()){
			ObjectProperty dp = it.next();
			if (getResourceShortName(dp).equals(shortName)){
				return dp;
			}
		}
		return null;
	}
	
	public void removeDataProperty(String uri){
		DatatypeProperty dp = model.getDatatypeProperty(uri);
		if (dp != null){
			dp.remove();
		}	
	}
	
	public void removeObjectProperty(String uri){
		ObjectProperty dp = model.getObjectProperty(uri);
		if (dp != null){
			dp.remove();
		}	
	}

	public String getResourcesPath() {
		return resourcesPath;
	}
	
	public String saveOntology(String ontologyFileName) throws IOException{
		String newFileName = resourcesPath + "/ontologies/updated/" + ontologyFileName;
		File file = new File(newFileName);
		OutputStream out = new FileOutputStream(file);
		if (out != null && !out.toString().isEmpty()) {
			model.write(out, "TTL"); // readable// rdf/xml
		}
		out.close();
		return newFileName;
	}
}
