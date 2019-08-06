# Shadowbot Procedures
POC Procedures for Chat Bot Application

Instructions
------------ 

This project uses maven, to build a jar-file with the procedure in this
project, simply package the project with maven:

    mvn clean package

This will produce a jar-file, `target/procedures-1.0-SNAPSHOT.jar`,
that can be copied to the `plugin` directory of your Neo4j instance.

    cp target/procedures-1.0-SNAPSHOT.jar neo4j-enterprise-3.5.8/plugins/.


Intents
-------

- Greeting
- Complete Conversation
- Category Inquiry
- Product Inquiry
- Price Inquiry
- Order Product
- Reorder Product
- Recommend Products
- Agree
- Disagree
- Order Product for me
- Order Product for member
- Order Product for unknown
- Return Product
- Cancel Order    