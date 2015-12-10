package models;

import java.util.Currency;

import agents.Person;
import jade.core.Agent;
import jade.core.behaviours.ReceiverBehaviour.TimedOut;
import jade.core.behaviours.SimpleBehaviour;
import jade.lang.acl.ACLMessage;


public class MeetingBehaviour extends SimpleBehaviour {
	private int attendees;
	private int score;
	private boolean allGood = true;

	Person agent;

	// construtor do behaviour
	public MeetingBehaviour(Agent a) {
		super(a);
		agent = (Person) a;
		this.attendees = 0;
	}

	public void action(){
		ACLMessage msg = agent.blockingReceive();
		if (msg.getPerformative() == ACLMessage.PROPOSE) {
			System.out.println(agent.getLocalName() + ": I received " + msg.getContent());

			String[] split = msg.getContent().split(",");
			Integer newScore = agent.schedule.isAvailableDuring(Integer.parseInt(split[0]), Integer.parseInt(split[1]));

			ACLMessage reply = msg.createReply();

			if(newScore == 3) {
				reply.setPerformative(ACLMessage.ACCEPT_PROPOSAL);
			} else {
				reply.setPerformative(ACLMessage.REJECT_PROPOSAL);
			}
			reply.setContent(Integer.toString(newScore));
			agent.send(reply);
		} else if(msg.getPerformative() == ACLMessage.ACCEPT_PROPOSAL) {
			//System.out.println(agent.getLocalName() + ": I received good combination from " + msg.getSender().getLocalName());
			this.attendees++;
			//System.out.println("CONTENT: " + msg.getContent());
			this.score += Integer.parseInt(msg.getContent());
			
		} else if (msg.getPerformative() == ACLMessage.REJECT_PROPOSAL){
			this.attendees++;
			this.allGood = false;
			//System.out.println("CONTENT: " + msg.getContent());
			this.score += Integer.parseInt(msg.getContent());
			//System.out.println(agent.getLocalName() + ": I received bad combination from " + msg.getSender().getLocalName());
		} 
	}

	// método done
	public boolean done() {
		if(agent.manager) {
			return this.attendees==agent.meeting.attendees.size()-1;
		} 
		return true;
	}

	public int onEnd() {
		agent.addBehaviour(new MeetingBehaviour(agent));
		if (agent.manager) {
			agent.hourlyScore.put(agent.currentHour, this.score);
			
			if(agent.currentHour == agent.endHour || this.allGood) {
				agent.printBestCombination();
				return 1;
			}
			
			agent.currentHour++;
			agent.sendMessage(agent.currentHour);
		}
		return 0;
	}

	public void reset() {
		super.reset();
		attendees = 0;
		score = 0;
	}
}