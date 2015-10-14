package models;

public class Schedule {
	
	public enum Priority {
		LOW, MEDIUM, HIGH
	}
		
	Priority[] occupiedTime = new Priority[24];
	
}
