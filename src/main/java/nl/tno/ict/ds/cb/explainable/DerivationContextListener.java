/**
 * 
 */
package nl.tno.ict.ds.cb.explainable;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.jena.fuseki.server.DataAccessPoint;
import org.apache.jena.fuseki.server.DataAccessPointRegistry;
import org.apache.jena.fuseki.server.DataService;
import org.apache.jena.fuseki.server.Operation;
import org.apache.jena.fuseki.servlets.ActionService;
import org.apache.jena.fuseki.servlets.ServiceDispatchRegistry;
import org.apache.jena.graph.Graph;
import org.apache.jena.rdf.model.InfModel;
import org.apache.jena.reasoner.InfGraph;
import org.apache.jena.sparql.core.DatasetGraph;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author nouwtb
 * 
 *         This class hooks into the APache Jena Fuseki (and tomcat/jetty)
 *         startup process to execute some custom code to add a derivation
 *         service to Plasido.
 *
 */
public class DerivationContextListener implements ServletContextListener {

	/**
	 * The log facility of this class.
	 */
	private static final Logger LOG = LoggerFactory.getLogger(DerivationContextListener.class);

	private static final Operation newOp = Operation.register("Derivation", "Derivation service");
	private static final String contentType = "text/plain";
	private static final String endpointName = "derivation";
	private final ActionService derivationHandler = new DerivationService();

	/**
	 * Add DerivationService to this Fuseki instance.
	 */
	@Override
	public void contextInitialized(ServletContextEvent sce) {
		LOG.debug("Entered Derivation context listener.");
		ServletContext sc = sce.getServletContext();

		ServiceDispatchRegistry registry = ServiceDispatchRegistry.get(sc);

		registry.register(newOp, contentType, derivationHandler);
		LOG.debug("Registered operation {} contenttype {} and handler {}.", newOp, contentType, derivationHandler);

		DataAccessPointRegistry dapReg = DataAccessPointRegistry.get(sc);

		LOG.debug("Adding the derivation service to the following datasets: ");
		for (String key : dapReg.keys()) {
			DataAccessPoint dap = dapReg.get(key);
			DataService ds = dap.getDataService();

			// enable derivation logging
			DatasetGraph dg = ds.getDataset();
			Graph g = dg.getDefaultGraph();

			LOG.trace("Type of graph: {}", g.getClass());

			InfGraph ig = null;
			if (g instanceof InfGraph) {
				ig = (InfGraph) g;
				ig.setDerivationLogging(true);
				LOG.debug("Enabled derivation logging on dataset '{}'.", key);
				ds.addEndpoint(newOp, endpointName);
				LOG.info("Derivation logging service /derivation enabled on dataset '{}'", key);
			} else {
				LOG.warn("Dataset '{}' should have an InfGraph as defaultGraph.", key);
			}

		}

	}

	/**
	 */
	@Override
	public void contextDestroyed(ServletContextEvent sce) {
		// do nothing

	}

}
