package models;
import java.util.ArrayList;

public class Meeting {

	String name;
	String manager;
	ArrayList<String> attendees;

	public Meeting(String name) {
		this.name = name;	
		this.attendees = new ArrayList<String>();
	}

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

	public void addPerson(String person) {
		attendees.add(person);
	}

}
