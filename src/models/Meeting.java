package models;

import jade.core.Agent;

import java.util.ArrayList;

import agents.Person;

public class Meeting {
	
	String name;
	String manager;
	ArrayList<String> attendees;
	
	//Getters and Setters
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getManager() {
		return manager;
	}
	public void setManager(String manager) {
		this.manager = manager;
	}
	public Meeting(String name) {
		this.name = name;	
	}
	
	public void addPerson(String person) {
		attendees.add(person);
	}
	
}
