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
import org.apache.jena.graph.GraphListener;
import org.apache.jena.reasoner.InfGraph;
import org.apache.jena.sparql.core.DatasetGraph;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author JackJackie
 * 
 *         This class hooks into the Apache Jena Fuseki (and tomcat/jetty)
 *         startup process to execute some custom code to add an event
 *         service to Plasido.
 *
 */
public class DeductionListener implements ServletContextListener {

	/**
	 * The log facility of this class.
	 */
	private static final Logger LOG = LoggerFactory.getLogger(DeductionListener.class);

	//private static final Operation newOp = Operation.register("Event", "Event service");
	//private static final String contentType = "text/plain";
	//private static final String endpointName = "event";
	//private final ActionService eventHandler = new DerivationService();
	private final GraphListener deductionHandler = new DeductionService();

	/**
	 * Add Deduction Service to this Fuseki instance.
	 */
	@Override
	public void contextInitialized(ServletContextEvent sce) {
		LOG.info("Entered deduction listener.");
		ServletContext sc = sce.getServletContext();
		DataAccessPointRegistry dapReg = DataAccessPointRegistry.get(sc);

		LOG.info("Adding the deduction service to the following datasets: ");
		for (String key : dapReg.keys()) {
			DataAccessPoint dap = dapReg.get(key);
			DataService ds = dap.getDataService();

			// enable deduction listening
			DatasetGraph dg = ds.getDataset();
			Graph g = dg.getDefaultGraph();

			LOG.trace("Type of graph: {}", g.getClass());

			InfGraph ig = null;
			if (g instanceof InfGraph) {
				ig = (InfGraph) g;
				ig.getDeductionsGraph().getEventManager().register(deductionHandler);
				LOG.info("Enabled deduction listening on default graph in dataset '{}'.", key);				
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
