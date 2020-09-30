# Ontology explorer, Device Semantics Editor and Service Semantics Editor

This application is a web application that hosts 3 tools: the Activage Ontology Explorer, the Device Semantics Editor and the Service Semantics Editor and aim at providing utilities for accessing the central ACTIVAGE ontology and functionalities of the Semantic Interoperability Layer (SIL).

It is a portal that was created using Liferay 6.1.1-ce-ga2, Java 7 and Tomcat 7. The back-end of the application is implemented in Java and for the manipulation of the Activage ontologies it utilises Apache Jena. For the user interface the application uses JSF + Primefaces + JavaScript.

## Tool desciption

### Activage Ontology Explorer

The Activage Ontology Explorer provides an interface for visualising the Activage Ontologies. Specifically, it loads the following ontologies: Activage Core Ontology, AHA Ontology, Big IoT Ontology, Fiesta IoT Ontologies, GoIoTP Ontologies and HL7 Ontology. Detailed information about these ontologies can be found in the following link: http://data-pack.activageproject.eu/.

All the classes are visualised using an interactive entity-relation diagram. The user can navigate through the diagram using pan and zoom, and on entity selection, more information regarding the selected entity are displayed in a dedicated panel. The implementation of the graph was based on the D3 Javascript library. Moreover, it provides a user-friendly way for the user to navigate through the properties (datatype - object- annotation) and the instances of the ontologies

### Device Semantics Editor

The Device Semantics Editor is a tool that enables the specification or modification of semantics related to the devices. It supports the extension of the device classes by adding new subclasses, the definition of restrictions for these new device classes and the creation of properties related to them. Any modification made is saved on the ontology instance loaded by the application, which can be exported by the user at anytime.

### Service Semantics Editor

The Service Semantics Editor is a tool that supports more or less the same functionalities with the Device Semantics Editor. Its main difference is that it targets semantic information related to services. That means that the user is able to add or modify semantic information related to service classes/properties.

## How to deploy

- Use an operating system with Java 7
- Download and install the following Liferay Portal version: liferay-portal-tomcat-6.1
- Download Liferay SDK and the provided source code. Import the project and create the corresponding WAR file
- Copy the created WAR file in the deploy folder of Liferay Portal and initialise the Tomcat

## Docker image use

You can use this application by running the corresponding docker image available on the Activage Docker Registry. First you have to log in to docker registry (described [here](https://git.activageproject.eu/Deployment/DT-AIOTES_docker)). You can run it using the following command: 
```
docker run -p 8080:8080 docker-activage.satrd.es/ont-explorer_dev-semantic_serv-semantics:0.2.0 
```

application will be available at the following URL: http://localhost:8080/web/activage/menu. In case you use a Windows machine, replace ``localhost`` with ``192.168.99.100``.

Further instructions about deploying the tools through Docker can be found at DT-AIOTES_docker repo, on the [Ontology Explorer-Device Semantics Editor - Service Semantics Editor](https://git.activageproject.eu/Deployment/DT-AIOTES_docker/src/master/Ontology%20Explorer-Device%20Semantics%20Editor%20-%20%20Service%20Semantics%20Editor)


## Deployment

In order to deploy these tools using Docker, download the `docker-compose.yml` in a local directory. Modify the environment variables and ports to reflect your configuration and then run the following command from the same directory:

```
docker-compose up -d
```

## Usage

By default, the application will be available from a web browser at the following URL: http://localhost:9082/web/activage/menu. In case you use a Windows machine, you may have to replace ``localhost`` with ``192.168.99.100``.

## License

Copyright 2020 CERTH/ITI Visual Analytics Lab. This software is licenced under EUPL v1.2, check [LICENSE](./LICENSE) for more.