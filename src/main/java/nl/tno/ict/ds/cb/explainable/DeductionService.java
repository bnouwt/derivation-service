/**
 * 
 */
package nl.tno.ict.ds.cb.explainable;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Iterator;
import java.util.List;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Message;
import javax.jms.MessageProducer;
import javax.jms.Queue;
import javax.jms.Session;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.broker.BrokerFactory;
import org.apache.activemq.broker.BrokerService;
import org.apache.jena.atlas.logging.Log;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.GraphListener;
import org.apache.jena.graph.Triple;
import org.apache.jena.rdf.model.InfModel;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.reasoner.Reasoner;
import org.apache.jena.reasoner.rulesys.GenericRuleReasoner;
import org.apache.jena.reasoner.rulesys.Rule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DeductionService implements GraphListener {
	
	private static final Logger LOG = LoggerFactory.getLogger(DeductionService.class);
	private BrokerService broker;
	
	public DeductionService() {
		LOG.info("New EventService initialized");
		try {
			broker = BrokerFactory.createBroker(new URI("broker:(tcp://0.0.0.0:61616)"));
			broker.start();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@Override
	public void notifyAddTriple(Graph g, Triple t) {
		// TODO Auto-generated method stub
        LOG.info(">> graph listener noticed triple added " + t);
		try {
	        ConnectionFactory connectionFactory = new ActiveMQConnectionFactory("tcp://0.0.0.0:61616");
			Connection connection = connectionFactory.createConnection();
			Session session = connection.createSession(false,Session.AUTO_ACKNOWLEDGE);
	        Queue queue = session.createQueue("DeductionsQueue");
	        String payload = "Triple added " + t.toString();
	        Message msg = session.createTextMessage(payload);
	        MessageProducer producer = session.createProducer(queue);
	        System.out.println("Sending text '" + payload + "'");
	        producer.send(msg);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void notifyAddArray(Graph g, Triple[] triples) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void notifyAddList(Graph g, List<Triple> triples) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void notifyAddIterator(Graph g, Iterator<Triple> it) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void notifyAddGraph(Graph g, Graph added) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void notifyDeleteTriple(Graph g, Triple t) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void notifyDeleteList(Graph g, List<Triple> L) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void notifyDeleteArray(Graph g, Triple[] triples) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void notifyDeleteIterator(Graph g, Iterator<Triple> it) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void notifyDeleteGraph(Graph g, Graph removed) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void notifyEvent(Graph source, Object value) {
		// TODO Auto-generated method stub
		
	}
	
	public static void main(String[] args) {
		LOG.debug("Entered event listener.");
		// default model and variables instantiation
		/* 
		Model m = ModelFactory.createDefaultModel();
		
		Statement s = m.createStatement(ResourceFactory.createResource("http://tno.nl/subject"),
				ResourceFactory.createProperty("http://tno.nl/predicate"),
				ResourceFactory.createResource("http://tno.nl/object"));
		
		// initiate reasoner
		List<Rule> rules = Rule.parseRules(
				"[test: (<http://tno.nl/subject> <http://tno.nl/predicate> <http://tno.nl/object>) -> (<http://tno.nl/subject> <http://tno.nl/new> <http://tno.nl/object>)]");
		Reasoner r = new GenericRuleReasoner(rules);

		// instantiate inference model
		InfModel infm = ModelFactory.createInfModel(r, m);

		//instantiate graphlistener and register it with the deductions model
		GraphListener L = new MyListener();				
		infm.getDeductionsModel().getGraph().getEventManager().register(L);

        // add statement to infm
		infm.add(s);

		StmtIterator iter = infm.listStatements();
		while (iter.hasNext()) {
			Statement stat = iter.next();
			System.out.println(">> found statement " + stat);
		}
		*/
	}

}
