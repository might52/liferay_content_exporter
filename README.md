This project is a possible example how to export library content from the liferay based on the successful event after
import global lar file. OSGI bundle.

Current features:

1. Possibilities to export the content after successful import the global lar file.
2. Export the content to the xml file.
3. Export the content to the zip archive.
4. Export the content based on the properties at the Liferay configuration: folder to export and keywords search.
5. Exporting type of objects:
   + document (pictures files etc.)
   + structured documents (web content)

Supported OS:

1. Windows
2. Linux

Java version 8 and 11.

Supported properties:

| Property name        | Possible values                | Description                                                                      |
|----------------------|--------------------------------|----------------------------------------------------------------------------------|
| exporter.keywords    | id(100132),test,epic(consumer) | Filter which will be executed during searching the content on the Liferay level. |
| outbound.folder.path | ./                             | Folder to export final zip archive with content.                                 |

To use them just add these properties to the one of the Liferay files and restart the bundle:

+ portal-ext.properties
+ portal-setup-wizard.properties


