/**
 * 
 */
package nl.tno.ict.ds.cb.explainable;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;

import org.apache.jena.fuseki.build.FusekiBuilder;
import org.apache.jena.fuseki.embedded.FusekiServer;
import org.apache.jena.fuseki.server.DataService;
import org.apache.jena.fuseki.server.Operation;
import org.apache.jena.fuseki.servlets.ActionService;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.rdf.model.InfModel;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.reasoner.Reasoner;
import org.apache.jena.reasoner.rulesys.GenericRuleReasoner;
import org.apache.jena.reasoner.rulesys.Rule;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.DatasetGraphFactory;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author nouwtb
 *
 */
class DerivationServiceTest {

	private static final Operation newOp = Operation.register("Derivation", "Derivation service");
	private static final String endpointName = "derivation";

	private static final String contentType = "text/plain";

	private final ActionService derivationHandler = new DerivationService();
	private final int port = 3030; // FusekiLib.choosePort();
	private final String url = "http://localhost:" + port;

	/**
	 * The log facility of this class.
	 */
	private static final Logger LOG = LoggerFactory.getLogger(DerivationService.class);

	@Test
	void testDerivationServie() {

		// Create a DataService and add the endpoint -> operation association.
		// This still needs the server to have the operation registered.
		Model m = ModelFactory.createDefaultModel();

		InputStream dataIS = DerivationServiceTest.class.getResourceAsStream("/data.ttl");
		InputStream rulesIS = DerivationServiceTest.class.getResourceAsStream("/et.rules");

		m.read(dataIS, null, "TURTLE");

		List<Rule> rules = Rule
				.parseRules(Rule.rulesParserFromReader(new BufferedReader(new InputStreamReader(rulesIS))));

		Reasoner reasoner = new GenericRuleReasoner(rules);
		reasoner.setDerivationLogging(true);
		InfModel inf = ModelFactory.createInfModel(reasoner, m);

		DatasetGraph dsg = DatasetGraphFactory.create(inf.getGraph());

		DataService dataService = new DataService(dsg);
		FusekiBuilder.populateStdServices(dataService, true);
		FusekiBuilder.addServiceEP(dataService, newOp, endpointName);

		FusekiServer server = FusekiServer.create().setPort(port)
				.registerOperation(newOp, contentType, derivationHandler).add("/ds", dataService).build();

		try {
			server.start();

			Thread.sleep(120000);

		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			server.stop();
		}

		LOG.info("Server started.");

	}

}
