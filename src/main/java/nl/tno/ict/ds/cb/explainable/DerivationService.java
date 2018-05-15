package nl.tno.ict.ds.cb.explainable;

import java.util.Iterator;

import javax.servlet.http.HttpServletRequest;

import org.apache.jena.fuseki.servlets.ActionREST;
import org.apache.jena.fuseki.servlets.HttpAction;
import org.apache.jena.fuseki.servlets.ServletOps;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Triple;
import org.apache.jena.reasoner.Derivation;
import org.apache.jena.reasoner.InfGraph;
import org.apache.jena.riot.WebContent;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.sse.SSE;
import org.apache.jena.web.HttpSC;

public class DerivationService extends ActionREST {

	private static final String TRIPLE_REQUEST_PARAMETER_NAME = "triple";

	@Override
	protected void doDelete(HttpAction action) {
		notSupported(action);
	}

	@Override
	protected void doGet(HttpAction action) {
		// since derivations need to be enabled for this service to work
		// we need to somehow hook into the startup of the dataset and call
		// org.apache.jena.rdf.model.InfModel.setDerivationLogging(boolean)

		HttpServletRequest req = action.getRequest();
		String stringTriple = req.getParameter(TRIPLE_REQUEST_PARAMETER_NAME);
		Triple t = SSE.parseTriple(stringTriple);

		// retrieve the data set and infgraph
		DatasetGraph dsg = action.getActiveDSG();
		Graph g = dsg.getDefaultGraph();

		// check if it is an inf model
		assert g instanceof InfGraph : "The graph should be an InfGraph.";

		InfGraph ig = (InfGraph) g;

		// check if derivations are enabled
		// apparently, we cannot check this! See
		// org.apache.jena.reasoner.rulesys.BasicForwardRuleInfGraph.shouldLogDerivations()

		// retrieve the derivations of the given triple
		Iterator<Derivation> derIter = ig.getDerivation(t);

		// convert it into some JSON format
		Derivation d;
		while(derIter.hasNext()) {
			d = derIter.next();
			
			d.
		}
		
		

	}

	@Override
	protected void doHead(HttpAction action) {
		action.response.setStatus(HttpSC.OK_200);
		action.response.setContentType(WebContent.contentTypeJSON);
	}

	@Override
	protected void doOptions(HttpAction action) {
		notSupported(action);
	}

	@Override
	protected void doPatch(HttpAction action) {
		notSupported(action);
	}

	@Override
	protected void doPost(HttpAction action) {
		notSupported(action);
	}

	@Override
	protected void doPut(HttpAction action) {
		notSupported(action);
	}

	@Override
	protected void validate(HttpAction action) {
		notSupported(action);

	}

	private void notSupported(HttpAction action) {
		ServletOps.errorMethodNotAllowed(action.getMethod() + " " + action.getDatasetName());
	}
}
