package agents;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.spi.LocaleNameProvider;

import org.json.JSONArray;
import org.json.JSONObject;

import jade.core.Agent;
import jade.core.behaviours.SimpleBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import jade.wrapper.AgentContainer;
import models.*;
import models.Schedule.Priority;

public class Person extends Agent {
	
	public Schedule schedule = new Schedule();
	public String name;
	
	public Person() {
		
		
		schedule.reset();
	}	
	
	public void addToSchedule() {
		this.name = this.getLocalName();
		JSONObject data;
		String json_str = "";
		JSONObject scheduleJSON;
		JSONArray low, high;
		try {
			json_str = readFile("src/data.json", Charset.defaultCharset());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		data = new JSONObject(json_str);
		scheduleJSON = data.getJSONObject(this.name);
		System.out.println("Loading " + this.name + "'s schedule..." );
		low = scheduleJSON.getJSONArray("LOW");
		high = scheduleJSON.getJSONArray("HIGH");
		for (int i = 0; i < low.length(); i++) {
			schedule.addToSchedule(Priority.LOW, low.getInt(i));
		}
		for (int i = 0; i < high.length(); i++) {
			schedule.addToSchedule(Priority.HIGH, high.getInt(i));
		}
		this.schedule.print();
	}
	
	public static String readFile(String path, Charset encoding) throws IOException {
        byte[] encoded = Files.readAllBytes(Paths.get(path));
        return new String(encoded, encoding);
}

	private class PingPongBehaviour extends SimpleBehaviour {
		private int n = 0;

		// construtor do behaviour
		public PingPongBehaviour(Agent a) {
			super(a);
		}

		public void action() {
			ACLMessage msg = blockingReceive();
			if (msg.getPerformative() == ACLMessage.INFORM) {
				System.out.println(++n + " " + getLocalName() + ": recebi " + msg.getContent());
				// cria resposta
				ACLMessage reply = msg.createReply();
				// preenche conteudo da mensagem
				if (msg.getContent().equals("ping"))
					reply.setContent("pong");
				else reply.setContent("ping");
				// envia mensagem
				send(reply);
			}
		}

		// método done
		public boolean done() {
			return n==10;
		}

	}
	// método setup
	protected void setup() {
		String tipo = "";
		
		// obtém argumentos
		Object[] args = getArguments();
		if (args != null && args.length > 0) {
			tipo = (String) args[0];
		} else {
			System.out.println("N�o especificou o tipo");
		}

		// regista agente no DF
		DFAgentDescription dfd = new DFAgentDescription();
		dfd.setName(getAID());
		ServiceDescription sd = new ServiceDescription();
		sd.setName(getName());
		sd.setType("Agente " + tipo);
		dfd.addServices(sd);
		try {
			DFService.register(this, dfd);
		} catch (FIPAException e) {
			e.printStackTrace();
		}

		// cria behaviour
		PingPongBehaviour b = new PingPongBehaviour(this);
		addBehaviour(b);

		// toma a iniciativa se for agente "pong"
		if(tipo.equals("pong")) {
			// pesquisa DF por agentes "ping"
			DFAgentDescription template = new DFAgentDescription();
			ServiceDescription sd1 = new ServiceDescription();
			sd1.setType("Agente ping");
			template.addServices(sd1);
			try {
				DFAgentDescription[] result = DFService.search(this, template);
				// envia mensagem "pong" inicial a todos os agentes "ping"
				ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
				for(int i=0; i<result.length; ++i)
					msg.addReceiver(result[i].getName());
				msg.setContent("pong");
				send(msg);
			} catch(FIPAException e) { e.printStackTrace(); }
		}
		
		addToSchedule();
	}

	// método takeDown
	protected void takeDown() {
		// retira registo no DF
		try {
			DFService.deregister(this);
		} catch(FIPAException e) {
			e.printStackTrace();
		}
	}
}