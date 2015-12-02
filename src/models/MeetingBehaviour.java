package models;

import agents.Person;
import jade.core.Agent;
import jade.core.behaviours.SimpleBehaviour;
import jade.lang.acl.ACLMessage;


public class MeetingBehaviour extends SimpleBehaviour {
    private int n = 0;

    Person agent;

    // construtor do behaviour
    public MeetingBehaviour(Agent a) {
        super(a);
        agent = (Person) a;
    }

    public void action() {
        ACLMessage msg = agent.blockingReceive();
        if (msg.getPerformative() == ACLMessage.INFORM) {
            System.out.println(++n + " " + agent.getLocalName() + ": I received " + msg.getContent());

            String[] split = msg.getContent().split(",");

            boolean isAvailable = agent.schedule.isAvailableDuring(Integer.parseInt(split[0]), Integer.parseInt(split[1]));

            ACLMessage reply = msg.createReply();

            if(isAvailable) {
                reply.setPerformative(ACLMessage.ACCEPT_PROPOSAL);
            } else {
                reply.setPerformative(ACLMessage.REJECT_PROPOSAL);
            }

           agent.send(reply);
        }
    }

    // método done
    public boolean done() {
        return n==10;
    }

}