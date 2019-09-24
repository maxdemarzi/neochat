# Neo Chat Procedures
Procedures for Neo4j Based Chat Bot Application

Instructions
------------ 

This project uses maven, to build a jar-file with the procedure in this
project, simply package the project with maven:

    mvn clean package

This will produce a jar-file, `target/procedures-1.0-SNAPSHOT.jar`,
that can be copied to the `plugin` directory of your Neo4j instance.

    cp target/procedures-1.0-SNAPSHOT.jar neo4j-enterprise-3.5.8/plugins/.

Download and Copy two additional files to your Neo4j plugins directory:

- [commons-compilter](http://central.maven.org/maven2/org/codehaus/janino/commons-compiler/3.0.15/commons-compiler-3.0.15.jar)
- [janino](http://central.maven.org/maven2/org/codehaus/janino/janino/3.0.15/janino-3.0.15.jar)

Restart your Neo4j Server.

Create the Schema by running this stored procedure:

    CALL com.maxdemarzi.schema.generate();

Copy the files in neochat/procedures/src/main/resources/data/catalog to the Neo4j import directory.
    
Create some seed data:
    
    CALL com.maxdemarzi.seed.decisions();    
    CALL com.maxdemarzi.seed.catalog()
    
Train the models:

    CALL com.maxdemarzi.train(model_directory, intents_directory);
    CALL com.maxdemarzi.train("/Users/maxdemarzi/Documents/Projects/neochat/procedures/src/main/resources/data/models/", "/Users/maxdemarzi/Documents/Projects/neochat/procedures/src/main/resources/data/training/intents")
    

Models
------

Some come from [OpenNLP](http://opennlp.sourceforge.net/models-1.5/).
Product, Category, Member, Order models are hand built alongside Intents.

Intents
-------

- Greeting
- Category Inquiry
- Product Inquiry
- Price Inquiry
- Complete Conversation
- Agree

Todo
- Order Product
- Reorder Product
- Recommend Products
- Disagree
- Order Product for me
- Order Product for member
- Order Product for unknown
- Return Product
- Cancel Order    

Data
-----



Enable file import in your neo4j.conf file inside the Neo4j Directory:

apoc.import.file.enabled=true

    CALL apoc.load.xml("file:///weapons.xml", "/chummer/categories/category") YIELD value WITH DISTINCT value.blackmarket AS top_name, value._text AS sub_name
    MERGE (top:Category {name: top_name})
    MERGE (sub:Category {name: sub_name})
    MERGE (top)<-[:IN_CATEGORY]-(sub)