# Explainable Fuseki

This repository contains a Apache Jena Fuseki service that allows the retrieval of the rule derivation for a particular triple.

Note that:
- it uses Apache Jena Fuseki version 3.12.0
- it was adapted to use the work in Cornelis Bouter his [Masters thesis](https://studenttheses.uu.nl/bitstream/handle/20.500.12932/32695/MastersThesisBouter.pdf).

Most important classes:
- *DerivationService.java*: contains the actual derivation service logic that receives a triple, calculates its derivation and sends it back. 
- *DerivationContextListener.java*: contains code to inspect and modify existing datasets in the Apache Jena Fuseki server to enable the derivation service.
- *DerivationServiceTest.java*: contains a JUnit test that registers the derivation service in an embedded Apache Jena Fuseki instance and allows you to test it _manually_.

![Powered by Apache Jena](https://www.apache.org/logos/poweredby/jena.png "Powered by Apache Jena")
