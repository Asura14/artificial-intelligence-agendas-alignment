package agents;

import jade.core.Agent;
import models.*;

public class Person extends Agent {
	
	public Schedule schedule;
	public String name;
	
	public Person(String name) {
		this.name = name;
	}
}