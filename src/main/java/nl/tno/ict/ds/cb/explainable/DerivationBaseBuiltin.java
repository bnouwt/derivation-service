package nl.tno.ict.ds.cb.explainable;

import java.util.Arrays;

import org.apache.jena.graph.Node;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.reasoner.rulesys.RuleContext;
import org.apache.jena.reasoner.rulesys.builtins.BaseBuiltin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class DerivationBaseBuiltin extends BaseBuiltin {

	protected final static String ns = "https://www.tno.nl/ontology/knowledgeBaseExplanation#";
	protected final static String nsData = "https://www.tno.nl/data/knowledgeBaseExplanation#";
	protected final static String rdf = "http://www.w3.org/1999/02/22-rdf-syntax-ns#";
	protected final static String owl = "http://www.w3.org/2002/07/owl#";
	protected final static String rdfs = "http://www.w3.org/2000/01/rdf-schema#";
	protected final static String tree = "http://www.odp.org/tree#";	
	
	public final static Property rdfType = ResourceFactory.createProperty(rdf, "type");
	protected String explanansLocalName;
	
	/**
	 * Required for registering the builtin with jena. This works in combination
	 * with @{code [] ja:loadClass "nl.tno.ict.ds.cb.<classInstanceName>"} statements
	 * in the fuseki dataset configuration.
	 */
	//static {
	//	BuiltinRegistry.theRegistry.register(new DummyExplainableBuiltin());
	//}
	


	/**
	 * Abstract method that should collect the explanation in OWL/RDF-format from a web address
	 * @param input The uninstantiated variables of the Builtin
	 * @param builtinInstantiatedVariables The instantiated variables of the Builtin.
	 * @param argLength The number of arguments of the builtin
	 * @param explanansUri The uri uniquely identifying the explanans the KB explanation should be linked to.
	 * @param suffix The suffix, which is also in the explanasURI, but extracted from it. 
	 * @return An RDF-Model containing a Explanation class instance with an Explanandum and Explanans, adhering to
	 * the Explanation ontology. 
	 */
	public abstract Model getExplanation(Node[] builtinVariables, int argLength, Node[] builtinInstantiatedVariables, String explanansUri, String suffix);
	
	
	/**
	 * This function returns a representation of the knowledge base behind that builtin. 
	 * @return An RDF-model representing the knowledge base that contains the knowledge and that produced the interpretation. The
	 * output is in the format as defined by the knowledgeBaseExplanation ontology.
	 */
	public abstract Model getKnowledgeBaseModel();

	/**
	 * Method returnning the root node.
	 * @return The root node of the RDF model representing the knowledge base.
	 */
	public abstract Resource getKnowledgeBaseRootNode();

	

	
}
