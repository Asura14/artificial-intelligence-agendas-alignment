package models;

import java.util.ArrayList;

public class Schedule {
	
	public enum Priority {
		FREE, LOW, HIGH
	}
	
	Priority[] occupiedTime = new Priority[24];
	
	public void addToSchedule(Priority p, int time) {
		occupiedTime[time] = p;
	}
	
	public void reset() {
		for(int i = 0; i < occupiedTime.length; i++) {
			occupiedTime[i]= Priority.FREE;
		}
	}
	
	public ArrayList<Integer> getFreeTime() {
		ArrayList<Integer> freeTime = new ArrayList<Integer>();
		for(int i = 0; i < occupiedTime.length; i++) {
			if (occupiedTime[i] == Priority.FREE) {
				freeTime.add(i);
			}
		}
		return freeTime;
	}
	
	public ArrayList<Integer> crossSchedulesFreeTime(Schedule schedule2) {
		ArrayList<Integer> freeTime = this.getFreeTime();
		ArrayList<Integer> freeTime2 = schedule2.getFreeTime();
		freeTime.retainAll(freeTime2);
		return freeTime;
	}
}
