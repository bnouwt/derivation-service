package nl.tno.ict.ds.cb.explainable;

import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;

import javax.servlet.http.HttpServletRequest;

import org.apache.jena.fuseki.servlets.ActionREST;
import org.apache.jena.fuseki.servlets.HttpAction;
import org.apache.jena.fuseki.servlets.ServletOps;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Triple;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.reasoner.Derivation;
import org.apache.jena.reasoner.InfGraph;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.WebContent;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.sse.SSE;
import org.apache.jena.util.iterator.ExtendedIterator;
import org.apache.jena.web.HttpSC;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DerivationService extends ActionREST {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private static final String TRIPLE_REQUEST_PARAMETER_NAME = "triple";

	/**
	 * The log facility of this class.
	 */
	private final static Logger LOG = LoggerFactory.getLogger(DerivationService.class);
	private final static String ns = "https://www.tno.nl/ontology/knowledgeBaseExplanationPOC#";

	@Override
	protected void doDelete(HttpAction action) {
		LOG.info("DerivationService: DELETE received.");
		notSupported(action);
	}

	@Override
	protected void doGet(HttpAction action) {
		LOG.info("DerivationService: GET received.");

		// since derivations need to be enabled for this service to work
		// we need to somehow hook into the startup of the dataset and call
		// org.apache.jena.rdf.model.InfModel.setDerivationLogging(boolean)
		
		HttpServletRequest req = action.getRequest();
		String stringTriple = req.getParameter(TRIPLE_REQUEST_PARAMETER_NAME);
		LOG.info("Received triple {}.", stringTriple);
		Triple t = SSE.parseTriple(stringTriple);

		LOG.info("Parsed triple: {}", t);

		// retrieve the data set and infgraph
		DatasetGraph dsg = action.getDataset();

		Graph g = dsg.getDefaultGraph();

		LOG.info("Graph type: {}", g.getClass());

		// check if it is an inf model
		assert g instanceof InfGraph : "The graph should be an InfGraph.";

		InfGraph ig = (InfGraph) g;

		
		// check if derivations are enabled
		// apparently, we cannot check this! See
		// org.apache.jena.reasoner.rulesys.BasicForwardRuleInfGraph.shouldLogDerivations()
		
		// retrieve the derivations of the given triple
		Iterator<Derivation> derIter = ig.getDerivation(t);

		LOG.info("Iterator has items?: {}", derIter.hasNext());
		
	
		boolean contains = ig.contains(t);
		LOG.info("Graph contains triple: {}", contains);
		LOG.info("Graph has size: {}", ig.size());
		
		// print all triples
		/*
		ExtendedIterator<Triple> it = ig.find();
		while(it.hasNext()) {
			Triple triple = it.next();
			LOG.info("Next triple: {}", triple);
		}
		*/
		
		Model mortgageOntology = ModelFactory.createDefaultModel();
		InputStream is = DerivationCollector.class.getResourceAsStream("/proofOfConcept.ttl");
		mortgageOntology.read(is, ns, "TURTLE");
		DerivationCollector derivationCollector = new DerivationCollector(ig, mortgageOntology);

		Derivation d;
		while(derIter.hasNext()) {
			d = derIter.next();
			try {
				Model ontModel = derivationCollector.combine(d);
				//rdfModel.write(action.response.getOutputStream(), "TURTLE");
				LOG.info("Writing the model to outputstream");
			
				RDFDataMgr.write(action.response.getOutputStream(), ontModel, Lang.RDFXML);
				action.response.getOutputStream().flush();
			} catch ( Exception e) {
				e.printStackTrace();
			} 
			
		}
		
		action.response.setStatus(HttpSC.OK_200);
		action.response.setContentType(WebContent.contentTypeTextPlain);

	}

	@Override
	protected void doHead(HttpAction action) {
		LOG.info("DerivationService: HEAD received.");
		action.response.setStatus(HttpSC.OK_200);
		action.response.setContentType(WebContent.contentTypeTextPlain);
	}

	@Override
	protected void doOptions(HttpAction action) {
		LOG.info("DerivationService: OPTIONS received.");
		notSupported(action);
	}

	@Override
	protected void doPatch(HttpAction action) {
		LOG.info("DerivationService: PATCH received.");
		notSupported(action);
	}

	@Override
	protected void doPost(HttpAction action) {
		LOG.info("DerivationService: POST received.");
		notSupported(action);
	}

	@Override
	protected void doPut(HttpAction action) {
		LOG.info("DerivationService: PUT received.");
		notSupported(action);
	}

	@Override
	protected void validate(HttpAction action) {
		LOG.debug("DerivationService: validation requested.");
	}

	private void notSupported(HttpAction action) {
		ServletOps.errorMethodNotAllowed(action.getMethod() + " " + action.getDatasetName());
	}
}
