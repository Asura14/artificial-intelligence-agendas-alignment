package models;
import java.util.ArrayList;
import java.util.Scanner;

public class Meeting {

	String name;
	String manager;
	ArrayList<String> attendees;
	int duration;

	public Meeting(String name) {
		this.name = name;	
		this.attendees = new ArrayList<String>();
	}

	//Getters and Setters
	
	public String getName() {
		return name;
	}
	public ArrayList<String> getAttendees() {
		return attendees;
	}

	public void setAttendees(ArrayList<String> attendees) {
		this.attendees = attendees;
	}

	public void setName(String name) {
		this.name = name;
	}
	public String getManager() {
		return manager;
	}
	
	public int getDuration() {
		return duration;
	}

	public void setDuration(int duration) {
		this.duration = duration;
	}

	public void setManager(String manager) {
		this.manager = manager;
	}

	public void addPerson(String person) {
		attendees.add(person);
	}

	public boolean isPersonInvited(String name) {
		return attendees.indexOf(name) != -1;
	}
	
	public void setMeetingLength() {
		System.out.println("\n Please insert the duration of the meeting: ");
		Scanner sc = new Scanner(System.in);
		do {
	        duration = sc.nextInt();
	    } while (duration > 24 || duration < 0);
		System.out.println("Meeting duration set to: " + duration);
	}

}
