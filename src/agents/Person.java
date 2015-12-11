package agents;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

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
import jade.wrapper.AgentController;
import jade.wrapper.ContainerController;
import jade.wrapper.ControllerException;
import jade.wrapper.StaleProxyException;
import models.*;
import models.Schedule.Priority;

public class Person extends Agent {

	public Schedule schedule = new Schedule();
	public String name;
	public Boolean manager;
	public Meeting meeting;
	public MeetingBehaviour b;
	public java.util.HashMap<Integer, Integer> hourlyScore = new HashMap<Integer, Integer>();
	public Integer startHour = 8;
	public Integer endHour = 20;
	public Integer currentHour;
	public Integer bestTimeOverall;
	public java.util.HashMap<Integer, ArrayList<String>> attendeesGoing = new java.util.HashMap<Integer, ArrayList<String>>();

	public void setName(String name) {
		this.name = name;
	}
	public Person() {
		this.manager = false;
		schedule.reset();
		currentHour = startHour;
	}
	public Person(boolean manager) {
		this.manager = manager;
		schedule.reset();
		currentHour = startHour;
	}	

	public void addToSchedule(String m) {

		this.name = this.getLocalName();
		String json_str = "";
		JSONArray low, high;
		try {
			json_str = readFile("src/data.json", Charset.defaultCharset());
		} catch (IOException e) {
			e.printStackTrace();
		}
		JSONObject data = new JSONObject(json_str);
		JSONObject scheduleJSON = data.getJSONObject(this.name);
		low = scheduleJSON.getJSONArray("LOW");
		high = scheduleJSON.getJSONArray("HIGH");
		for (int i = 0; i < low.length(); i++) {
			schedule.addToSchedule(Priority.LOW, low.getInt(i));
		}
		for (int i = 0; i < high.length(); i++) {
			schedule.addToSchedule(Priority.HIGH, high.getInt(i));
		}

		if(m.equals("manager")) {
			System.out.println("Loading " + this.name + "'s schedule..." );
			this.manager = true;
			for(int i = 0; data.names().length() > i; i++) {
				if(!data.names().get(i).equals(this.name)) {
					populateContainer(data.names().getString(i), data);
					System.out.println("\n Added: " + data.names().getString(i));
				}
			}
			for (int i = this.startHour; i <= this.endHour; i++) {
				hourlyScore.put(i, 0);
				attendeesGoing.put(i, new ArrayList<String>());
			}
			createMeeting(this.name);
		}
	}
	
	public void printBestCombination()
	{
		int bestScore = 0;
		int bestTime = 0;
		for (int i = this.startHour; i <= this.endHour; i++) {
			int score = hourlyScore.get(i);
			if(score > bestScore) {
				bestTime = i;
				bestScore = score;
			}
		}
		bestTimeOverall = bestTime;
		System.out.println("\n");
		System.out.println("The best time for " + meeting.getName() +  " is at " + bestTime + ".");
		System.out.println("The following people will be attending:");
		for (int i = 0; i < attendeesGoing.get(bestTime).size(); i++) {
			System.out.println(attendeesGoing.get(bestTime).get(i));
		}
		System.out.println("\n");
	}

	public void createMeeting(String manager) {
		String  meetingName;
		System.out.println("Welcome!\nPlease insert the name of the meeting: ");
		Scanner scanner = new Scanner(System.in);
		meetingName = scanner.nextLine();
		this.meeting = new Meeting(meetingName);
		System.out.println("Who will be attending this meeting?");
		ArrayList<String> attendees = addAttendees(manager);
		for(int i=0; i < attendees.size(); i++) {
			this.meeting.addPerson(attendees.get(i));
		}
		System.out.println("Meeting: " + meetingName + " has been created!");
		meeting.setMeetingLength();
		sendMessage(startHour);
	}

	public ArrayList<String> addAttendees(String manager) {
		boolean mrBoolean = true;
		ArrayList<String> attendees = new ArrayList<String>();
		while(mrBoolean) {
			System.out.println("(type: ok to end) Attendee name: ");
			Scanner scanner = new Scanner(System.in);
			String newPerson = scanner.nextLine();
			if(newPerson.equals("ok")) {
				mrBoolean = false;
			}
			String[] newString = newPerson.split("\\s+");
			if(attendees.indexOf(newString[0]) == -1) {
				attendees.add(newString[0]);
			} else {
				System.out.println(newString[0] + " was already invited.");
			}
		}
		return attendees;
	}

	public static String readFile(String path, Charset encoding) throws IOException {
		byte[] encoded = Files.readAllBytes(Paths.get(path));
		return new String(encoded, encoding);
	}

	public void populateContainer(String name, JSONObject data) {
		ContainerController ac = getContainerController();
		try {
			AgentController aController = ac.createNewAgent(name,Person.class.getName(),null);
			aController.start();
		} catch (StaleProxyException e) {
			e.printStackTrace();
		}		
	}
	
	public void sendMessage(int time) {

		// DF Search for Agents "Not Manager"
		DFAgentDescription template = new DFAgentDescription();
		ServiceDescription sd1 = new ServiceDescription();
		sd1.setType("Agente not manager");
		template.addServices(sd1);
		try {
			DFAgentDescription[] result = DFService.search(this, template);

			ACLMessage msg = new ACLMessage(ACLMessage.PROPOSE);
			for(int i=0; i<result.length; ++i) {
				if(meeting.getAttendees().contains(result[i].getName().getLocalName())) {
					msg.addReceiver(result[i].getName());
				}
			}
			msg.setContent(time + "," + Integer.toString(meeting.getDuration()));
			send(msg);
		} catch(FIPAException e) {
			e.printStackTrace();
		}		
		
	}
	
	public void sendFinalMessage(int time) {

		// DF Search for Agents "Not Manager"
		DFAgentDescription template = new DFAgentDescription();
		ServiceDescription sd1 = new ServiceDescription();
		sd1.setType("Agente not manager");
		template.addServices(sd1);
		try {
			DFAgentDescription[] result = DFService.search(this, template);

			ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
			for(int i=0; i<result.length; ++i) {
				if(meeting.getAttendees().contains(result[i].getName().getLocalName())) {
					msg.addReceiver(result[i].getName());
				}
			}
			msg.setContent(time + "," + Integer.toString(meeting.getDuration()));
			send(msg);
		} catch(FIPAException e) {
			e.printStackTrace();
		}
	}

	protected void setup() {
		String tipo = "";

		// obtem argumentos
		Object[] args = getArguments();
		if (args != null && args.length > 0) {
			tipo = (String) args[0];
		}
		if(tipo.equals("")) {
			tipo = "not manager";
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

		// Create behaviour
		this.b = new MeetingBehaviour(this);
		addBehaviour(b);


		if(args != null && args.length > 0) {
			addToSchedule("manager");
		} else {
			addToSchedule("not manager");
		}

	}

	//TakeDown method
	protected void takeDown() {
		// retira registo no DF
		try {
			DFService.deregister(this);
		} catch(FIPAException e) {
			e.printStackTrace();
		}
	}
}