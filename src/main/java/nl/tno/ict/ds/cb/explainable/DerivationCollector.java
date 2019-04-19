package nl.tno.ict.ds.cb.explainable;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.jena.datatypes.xsd.XSDDatatype;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.reasoner.Derivation;
import org.apache.jena.reasoner.InfGraph;
import org.apache.jena.reasoner.TriplePattern;
import org.apache.jena.reasoner.rulesys.ClauseEntry;
import org.apache.jena.reasoner.rulesys.Functor;
import org.apache.jena.reasoner.rulesys.Rule;
import org.apache.jena.reasoner.rulesys.RuleDerivation;
import org.apache.jena.reasoner.rulesys.impl.MutableTriplePattern;
import org.apache.jena.shared.PrefixMapping;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import openllet.jena.PelletReasonerFactory;

public class DerivationCollector {
	/**
	 * This class collects several OWL/RDF-models of the Explanation ontology and
	 * combines them, by identifying the identical explananda/explanans.
	 * 
	 * @author Cornelis Bouter
	 */

	private static final Logger LOG = LoggerFactory.getLogger(DerivationCollector.class);


	private Model plasidoKnowledgeEngine;
	private Resource plasidoKnowledgeEngineRootNode;

	private final static String ns = "https://www.tno.nl/ontology/knowledgeBaseExplanation#";
	private final static String nsData = "https://www.tno.nl/data/knowledgeBaseExplanation#";
	private final Model domainOntology;

	// The InfGraph that produced the Derivation that is being collected.
	// Is a workaround since RuleDerivation does not have a getInfGraph-method,
	// because that would be preferred, but we want our addition to work without
	// changes to Jena.
	private InfGraph infGraph;

	public DerivationCollector(InfGraph infGraph, Model domainOntology) {
		this.infGraph = infGraph;
		this.domainOntology = domainOntology;

		// create model for the concept of plasido knowledge engine.
		plasidoKnowledgeEngine = ModelFactory.createDefaultModel();
		plasidoKnowledgeEngineRootNode = plasidoKnowledgeEngine.createResource(nsData + "PlasidoKnowledgeEngine");
		Resource ruleBasedExpertSystem = plasidoKnowledgeEngine
				.createResource(ns + "RuleBasedExpertSystemKnowledgeBase");

		plasidoKnowledgeEngine.add(plasidoKnowledgeEngineRootNode, type, ruleBasedExpertSystem);
		plasidoKnowledgeEngine.add(plasidoKnowledgeEngineRootNode, type, namedIndividual);

	}

	/**
	 * This model takes a Derivation object containing all derivations that led to a
	 * certain conclusion. It looks up the RDF-explanation of each derivation and
	 * collects these.
	 * 
	 * The problematic and hard part is that the RDF-explanation URIs have to be
	 * changed such that each Explanandum and Explanans referring to one object has
	 * the same URI.
	 * 
	 * We may need to use the Jena rules to look for the correct configuration of
	 * knowledge bases.
	 * 
	 * @param d A Derivation object that is modelled as a tree, containing each of
	 *          the derivation steps that were used to obtain a specific result.
	 * @return An RDF/OWL-model representing the Derivation.
	 * @throws Exception 
	 */
	public Model combine(Derivation d) throws Exception {
		// Model model = ModelFactory.createDefaultModel();

		// Model base =
		// ModelFactory.createOntologyModel(PelletReasonerFactory.THE_SPEC);
		Model explanationOntology = ModelFactory.createOntologyModel(OntModelSpec.OWL_DL_MEM);
		InputStream is = DerivationCollector.class.getResourceAsStream("/KnowledgeBaseExplanationNoIndividuals.owl");
		explanationOntology.read(is, ns, "RDF/XML");

		Model treeODP = ModelFactory.createOntologyModel(OntModelSpec.OWL_DL_MEM);
		InputStream is2 = DerivationCollector.class.getResourceAsStream("/ODP-TreeWorkingCopy.ttl");
		explanationOntology.read(is2, ns, "TURTLE");

		// Model model =
		// ModelFactory.createOntologyModel(PelletReasonerFactory.THE_SPEC);
		Model model = ModelFactory.createOntologyModel(OntModelSpec.OWL_DL_MEM);
		model.add(explanationOntology);
		model.add(treeODP);
		model.add(domainOntology);

		model.add(ResourceFactory.createResource(nsData + "ExplanationIndividual"), type, rootNode);
		model.setNsPrefixes(PrefixMapping.Standard);
		// model.setNsPrefix("owl", "http://www.w3.org/2002/07/owl#");
		// model.setNsPrefix("rdf", "http://www.w3.org/1999/02/22-rdf-syntax-ns/");
		// model.setNsPrefix("rdfs", "http://www.w3.org/2000/01/rdf-schema/");
		model.setNsPrefix("xml", "http://www.w3.org/XML/1998/namespace");
		model.setNsPrefix("xsd", "http://www.w3.org/2001/XMLSchema/");
		model.setNsPrefix("tno", "https://www.tno.nl/ontology/knowledgeBaseExplanation#");
		model.setNsPrefix("tnoPOC", "https://www.tno.nl/ontology/knowledgeBaseExplanationPOC#");

		LOG.info("Starting recursive combination algorithm");
		return model.add(combine((RuleDerivation) d, ""));
	}

	public Model combine(RuleDerivation d, String suffix) throws Exception {
		/** The rule which asserted this triple */
		Rule rule = d.getRule();

		/** The triple which was asserted */
		Triple conclusion = d.getConclusion();

		/** The list of triple matches that fired the rule */
		List<Triple> matches = d.getMatches();

		/** The InfGraph which produced this derivation */
		// see the field InfGraph. RuleDerivation does not have a getter.

		return combine(d, rule, conclusion, matches, suffix);
	}

	// general KnowledgeBaseExplanation ontology (also from other sources) concepts
	Resource explanationConcept = ResourceFactory.createResource(ns + "Explanation");
	Resource explanandumConcept = ResourceFactory.createResource(ns + "Explanandum");
	Resource explanansConcept = ResourceFactory.createResource(ns + "Explanans");
	Resource selfExplanatoryExplanans = ResourceFactory.createResource(ns + "SelfExplanatoryExplanans");
	Resource explanationTreeNode = ResourceFactory.createResource(ns + "ExplanationTreeNode");
	Resource leafNode = ResourceFactory.createResource("http://www.odp.org/tree#LeafNode");
	Resource rootNode = ResourceFactory.createResource("http://www.odp.org/tree#RootNode");
	Resource namedIndividual = ResourceFactory.createResource("http://www.w3.org/2002/07/owl#NamedIndividual");
	Resource fact = ResourceFactory.createResource(ns+"Fact");

	Property hasExplanans = ResourceFactory.createProperty(ns + "hasExplanans");
	Property hasExplanandum = ResourceFactory.createProperty(ns + "hasExplanandum");
	Property isConceptualizedBy = ResourceFactory.createProperty(ns + "isConceptualizedBy");
	Property subject = ResourceFactory.createProperty("http://www.w3.org/1999/02/22-rdf-syntax-ns#subject");
	Property predicate = ResourceFactory.createProperty("http://www.w3.org/1999/02/22-rdf-syntax-ns#predicate");
	Property object = ResourceFactory.createProperty("http://www.w3.org/1999/02/22-rdf-syntax-ns#object");
	Property type = ResourceFactory.createProperty("http://www.w3.org/1999/02/22-rdf-syntax-ns#type");
	Property hasExplanationChild = ResourceFactory.createProperty(ns + "hasExplanationChild");
	Property hasOutDegree = ResourceFactory.createProperty("http://www.odp.org/tree#hasOutDegree");
	Property hasFact = ResourceFactory.createProperty(ns + "hasFact");
	Property hasRule = ResourceFactory.createProperty(ns + "hasRule");
	Property hasRuleRepresentation = ResourceFactory.createProperty(ns + "hasRuleRepresentation");

	/**
	 * Method that constructs the Derivation following the ontology from a Apache
	 * Jena RuleDerivation object. It uses a numerical suffix to uniquely identify
	 * the location in the tree. Starting at the root, each character indicates the
	 * n-th edge that was followed. So, the root has no suffix, and the leftmost
	 * child of the root has suffix 0.
	 * 
	 * @param d          The RuleDerivation that is processed into the
	 * @param rule       The rule which asserted the triple.
	 * @param conclusion The Triple which was asserted.
	 * @param matches    The triples that fired the rule.
	 * @param suffix     A suffix to uniquely identify all occurrences in the tree.
	 * @return An RDF model in the structure describes by the ontology. It may need
	 *         reasoning to infer inverse relations.
	 * @throws Exception 
	 */
	public Model combine(RuleDerivation d, Rule rule, Triple conclusion, List<Triple> matches, String suffix) throws Exception {
		// the first node has the root conclusion
		// The facts resulting in deriving the root need to be passed upwards,
		// such that these facts can be unified with the explananda of the
		// facts in the subbranches.

		// The RDF for Builtin-explanations can be loaded from the DerivationBase-
		// Builtin. The RDF for rule-based linking of several KBs has to be done
		// manually in this method.

		// DONE: keep track of place in the tree to not get identical URIs.
		// build explanation/explanandum/explanans-object
		Model model = ModelFactory.createOntologyModel(OntModelSpec.OWL_DL_MEM);
		Resource explanation = model.createResource(nsData + "ExplanationIndividual" + suffix);
		Resource explanandum = model.createResource(nsData + "ExplanandumIndividual" + suffix);
		Resource explanans = model.createResource(nsData + "ExplanansIndividual" + suffix);
		Resource ruleIndividual = model.createResource(nsData + "RuleIndividual" + suffix);

		// add individuals with correct relations
		model.add(explanation, type, explanationConcept);
		model.add(explanandum, type, explanandumConcept);
		model.add(explanans, type, explanansConcept);
		model.add(explanation, type, explanationTreeNode);
		model.add(explanation, type, namedIndividual);
		model.add(explanans, type, namedIndividual);
		model.add(explanandum, type, namedIndividual);
		model.add(explanandum, type, fact);
		model.add(ruleIndividual, type, namedIndividual);

		model.add(explanation, hasExplanandum, explanandum);
		model.add(explanation, hasExplanans, explanans);
		model.add(explanans, hasRule, ruleIndividual);
		model.add(ruleIndividual, hasRuleRepresentation,
				ResourceFactory.createTypedLiteral(new String(rule.toString())));
		model.add(explanation, hasOutDegree, ResourceFactory.createTypedLiteral(new Integer(matches.size())));
		// add hasRule to show the rule that led to this derivation. A rule itself
		// can also have an explanation.

		// methode om de isConcpeutalizedBy methode op te halen.
		model.add(plasidoKnowledgeEngine);
		model.add(explanation, isConceptualizedBy, plasidoKnowledgeEngineRootNode);

		// DONE: create explanandum-fact
		if (conclusion.getSubject().isURI()) {
			model.add(explanandum, subject,
					ResourceFactory.createTypedLiteral(conclusion.getSubject().getURI(), XSDDatatype.XSDanyURI));
		} else {
			System.out.println("Something wrong, subject is not an URI.");
			throw(new Exception());
		}
		if (conclusion.getPredicate().isURI()) {
			model.add(explanandum, predicate,
					ResourceFactory.createTypedLiteral(conclusion.getPredicate().getURI(), XSDDatatype.XSDanyURI));
		} else {
			System.out.println("Something wrong, since predicate is not an URI.");
			throw(new Exception());
		}
		if (conclusion.getObject().isURI()) {
			model.add(explanandum, object,
					ResourceFactory.createTypedLiteral(conclusion.getObject().getURI(), XSDDatatype.XSDanyURI));
		} else if (conclusion.getObject().isLiteral()) {
			model.add(explanandum, object,
					ResourceFactory.createTypedLiteral(conclusion.getObject().getLiteralLexicalForm(),
							conclusion.getObject().getLiteralDatatype()));
		} else {
			System.out.println("Something wrong, object is neither URI nor literal.");
			throw(new Exception());
		}
		
		// extract explanations from Builtins
		extractBuiltinExplanations(rule, conclusion, matches, suffix, model, explanation, explanans);

		extractRuleExplanations(d, suffix, model, explanation, explanans);

		return model;
	}

	private void extractRuleExplanations(RuleDerivation d, String suffix, Model model, Resource explanation,
			Resource explanans) throws Exception {
		// loop over all Triple matches
		for (int i = 0; i < d.getMatches().size(); i++) {
			Triple match = d.getMatches().get(i);
			Iterator<Derivation> derivations = infGraph.getDerivation(match);

			LOG.info("Found a match number: {}", i);
			LOG.info("Number of matches: {}", d.getMatches().size());
			// the number of matches equals the number of items in the body.
			// Loop over these. And apply the correct thing.

			if (derivations == null || !derivations.hasNext()) {
				LOG.info("No derivation found, so either fact or builtin.");
				if (match == null) {
					LOG.info("Builtin found. Processed in another loop.");


				} else {
					// Fact
					System.out.println("Found a fact: " + match.toString());

					// Dus bouw ook een nieuw model.
					Resource explanationPrime = model.createResource(nsData + "ExplanationIndividualLeaf" + suffix + i);
					Resource explanandumPrime = model.createResource(nsData + "ExplanandumIndividualLeaf" + suffix + i);
					Resource explanansPrime = model.createResource(nsData + "ExplanansIndividualLeaf" + suffix + i);

					// add relations and instances
					model.add(explanationPrime, type, explanationConcept);
					model.add(explanationPrime, type, explanationTreeNode);
					model.add(explanationPrime, type, leafNode);
					model.add(explanandumPrime, type, explanandumConcept);
					model.add(explanansPrime, type, explanansConcept);
					model.add(explanationPrime, type, namedIndividual);
					model.add(explanandumPrime, type, namedIndividual);
					model.add(explanansPrime, type, namedIndividual);
					model.add(explanandumPrime, type, fact);

					model.add(explanationPrime, hasExplanandum, explanandumPrime);
					model.add(explanationPrime, hasExplanans, explanansPrime);
					model.add(explanationPrime, hasOutDegree, ResourceFactory.createTypedLiteral(new Integer(0)));
					model.add(explanationPrime, isConceptualizedBy, plasidoKnowledgeEngineRootNode);
					
					// add hasFact relation from the previous explanans.
					model.add(explanans, hasFact, explanandumPrime);
					
					if (match.getSubject().isURI()) {
						model.add(explanandumPrime, subject,
								ResourceFactory.createTypedLiteral(match.getSubject().getURI(), XSDDatatype.XSDanyURI));
					} else {
						System.out.println("Something wrong, subject is not an URI.");
						throw(new Exception());
					}
					if (match.getPredicate().isURI()) {
						model.add(explanandumPrime, predicate, ResourceFactory
								.createTypedLiteral(match.getPredicate().getURI(), XSDDatatype.XSDanyURI));
					}  else {
						System.out.println("Something wrong, since predicate is not an URI.");
						throw(new Exception());
					}
					if (match.getObject().isURI()) {
						model.add(explanandumPrime, object,
								ResourceFactory.createTypedLiteral(match.getObject().getURI(), XSDDatatype.XSDanyURI));
					} else if (match.getObject().isLiteral()) {
						model.add(explanandumPrime, object, ResourceFactory.createTypedLiteral(
								match.getObject().getLiteralLexicalForm(), match.getObject().getLiteralDatatype()));
					} else {
						System.out.println("Something wrong, object is neither URI nor literal.");
						throw(new Exception());
					}

					// refereer terug naar top explanandum.
					model.add(explanation, hasExplanationChild, explanationPrime);

					// maak de explanans self-explanatory
					model.add(explanansPrime, type, selfExplanatoryExplanans);
					model.add(explanansPrime, hasExplanans, explanationPrime);

				}
			} else {
				// Fact that can be explained further.
				// add item to the explanans.
				String newSuffix = suffix + i;

				LOG.info("Entering recursion for {}", match);

				// the corresponding Explanandum will be created in the recursive iteration.
				model.add(explanans, hasFact, ResourceFactory.createResource(nsData + "ExplanandumIndividual" + newSuffix));

				// add relation forward to the Explanation to be added one step downward.
				model.add(explanation, hasExplanationChild,
						ResourceFactory.createResource(nsData + "ExplanationIndividual" + newSuffix));

				// recurse down the derivation tree.
				model.add(combine((RuleDerivation) (derivations.next()), newSuffix));

			}

		}
	}

	private void extractBuiltinExplanations(Rule rule, Triple conclusion, List<Triple> matches, String suffix,
			Model model, Resource explanation, Resource explanans) {
		// loop over all Builtins
		for (int i = 0; i < rule.getBody().length; i++) {
			ClauseEntry term = rule.getBodyElement(i);
			if (term instanceof Functor) {
				LOG.info("Index {} is a builtin: {}", i, ((Functor) term).getImplementor().getName());
				if (((Functor) term).getImplementor() instanceof DerivationBaseBuiltin) {
					DerivationBaseBuiltin expBuiltin = (DerivationBaseBuiltin) ((Functor) term).getImplementor();

					LOG.info("Found a builtin: {}", expBuiltin.getName());

					Node[] builtinInstantiatedVariables = getBuiltinArguments(term, rule, matches, conclusion);
					// zo simpel als? om uitleg op te halen
					model.add(expBuiltin.getExplanation(((Functor) term).getArgs(), expBuiltin.getArgLength(),
							builtinInstantiatedVariables, explanans.getURI(), suffix));
					
					// haal KB model op
					model.add(expBuiltin.getKnowledgeBaseModel());
					model.add(explanation, isConceptualizedBy, expBuiltin.getKnowledgeBaseRootNode());

				}
			}
		}
	}

	/**
	 * Function that extracts the instantiated variables for a builtin from the
	 * available information. This is necessary since we are outside of the
	 * RuleContext when producing the explanation, compared to when reasoning.
	 * 
	 * @param term       The Functor/Builtin for which we want the instantiated
	 *                   variables.
	 * @param rule       The rule containing among other the Builtin.
	 * @param matches    List of matches with the Triples in the rule. Does not
	 *                   include an entry for match with builtin.
	 * @param conclusion Instantiated Triple of the conclusion of the rule.
	 * @return The instantiated variables for the Builtin.
	 */
	private Node[] getBuiltinArguments(ClauseEntry term, Rule rule, List<Triple> matches, Triple conclusion) {
		// make map from ClauseEntry to index in matches-list
		// The matches-list does not include the Builtins.
		Map<ClauseEntry, Integer> matchesIndexMap = new HashMap<ClauseEntry, Integer>();
		for (int i = 0, indexCounter = 0; i < rule.getBody().length; i++) {
			if (rule.getBodyElement(i) instanceof Functor) {
				matchesIndexMap.put(rule.getBodyElement(i), -1);
			} else if (rule.getBodyElement(i) instanceof TriplePattern
					|| rule.getBodyElement(i) instanceof MutableTriplePattern
					|| rule.getBodyElement(i) instanceof Rule) {
				matchesIndexMap.put(rule.getBodyElement(i), indexCounter);
				indexCounter++;
			}

		}
		//LOG.info("matchesIndexMap: {}", matchesIndexMap);

		Node[] output = new Node[((Functor) term).getImplementor().getArgLength()];
		Node[] builtinVariables = ((Functor) term).getArgs();
		for (int i = 0; i < output.length; i++) {
			Node builtinVariable = builtinVariables[i];
			//LOG.info("BuiltinVariable {}", i);

			// identify the clauseEntry of the rule the variable resides
			// first in the body
			for (int j = 0; j < rule.getBody().length; j++) {
				ClauseEntry c = rule.getBody()[j];
				//LOG.info("ClauseEntry {}", j);

				if (c instanceof Functor) {
					//LOG.info("ClauseEntry instance of Functor/Builtin.");
					continue;
				} else if (c instanceof TriplePattern) {
					//LOG.info("ClauseEntry instance of Triple.");
					if (((TriplePattern) c).getSubject().equals(builtinVariable)) {
						output[i] = matches.get(matchesIndexMap.get(c)).getSubject();
						//LOG.info("Match with subject:", ((TriplePattern) c).getSubject());
						//LOG.info("{} equals {}: {}", ((TriplePattern) c).getSubject(), builtinVariable,
						//		((TriplePattern) c).getSubject().equals(builtinVariable));
					} else if (((TriplePattern) c).getPredicate().equals(builtinVariable)) {
						output[i] = matches.get(matchesIndexMap.get(c)).getPredicate();
						//LOG.info("Match with predicate:", ((TriplePattern) c).getPredicate());
					} else if (((TriplePattern) c).getObject().equals(builtinVariable)) {
						output[i] = matches.get(matchesIndexMap.get(c)).getObject();
						//LOG.info("Match with object:", ((TriplePattern) c).getObject());
						//LOG.info("{} equals {}: {}", ((TriplePattern) c).getObject(), builtinVariable,
						//		((TriplePattern) c).getObject().equals(builtinVariable));
					} else {
						//LOG.info("No match!");
					}

				} else if (c instanceof Rule) {
					//LOG.info("Found instance of rule");
				
				}
			}

			// only works for forward rules with heads of size 1
			for (int j = 0; j < rule.getHead().length; j++) {
				ClauseEntry c = rule.getHead()[j];
				//LOG.info("ClauseEntry HEAD {}", j);

				if (c instanceof Functor) {
				//	LOG.info("ClauseEntry instance of Functor/Builtin.");
					continue;
				} else if (c instanceof TriplePattern) {
				//	LOG.info("ClauseEntry instance of Triple.");
					if (((TriplePattern) c).getSubject().equals(builtinVariable)) {
						output[i] = conclusion.getSubject();
				//		LOG.info("Match with subject:", ((TriplePattern) c).getSubject());
					} else if (((TriplePattern) c).getPredicate().equals(builtinVariable)) {
						output[i] = conclusion.getPredicate();
				//		LOG.info("Match with predicate:", ((TriplePattern) c).getPredicate());
					} else if (((TriplePattern) c).getObject().equals(builtinVariable)) {
						output[i] = conclusion.getObject();
				//		LOG.info("Match with object:", ((TriplePattern) c).getObject());
					} else {
				//		LOG.info("No match!");
					}

				} else if (c instanceof Rule) {
				//	LOG.info("Found instance of rule in head");
				}
			}

		}

		LOG.info("Builtin variable instantiations found: {}", ArrayUtils.toString(output));
		return output;
	}

}
