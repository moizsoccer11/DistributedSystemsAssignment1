import java.io.*;
import java.net.*;
import java.util.*;

//The Server is responsible for Connecting Clients to the Rooms and Creating New ChatRooms
public class Server {
    //List  to keep track of Rooms
    static List<Room> chatRooms = new ArrayList<>();

    public static void main(String[] args) {
        //Variables
        ServerSocket serverSoc;
        Socket clientSoc;
        try {
            serverSoc =  new ServerSocket(3500);
            System.out.println("Threaded Rooms Server Started...!");
            while (true) {
                clientSoc = serverSoc.accept();
                System.out.println("New client connected");
                ClientHandler client = new ClientHandler(clientSoc);
                client.start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    static class ClientHandler extends Thread{
        //Client Socket
        Socket client;
        //Client User Name
        String userName;
        //Input and Output from Client
        PrintWriter clientOut;
        BufferedReader clientIn;

        public ClientHandler(Socket clientSoc){
            client=clientSoc;
        }

        public void run(){
            try {
                //Track if user is in room or not
                boolean clientInRoom=false;
                //Client Input for Room Name or to create new Room
                String userInput="";
                //Selected Chat Room or Created room
                Room selectedRoom = null;
                //Create Input and Output Streams for the client
                clientOut = new PrintWriter(client.getOutputStream(), true);
                clientIn = new BufferedReader(new InputStreamReader(client.getInputStream()));
                //Ask client to either create new room or join an existing room:
                while(true){
                    //Get All current room names
                    List<String> chatRoomNames = new ArrayList<>();
                    for(Room room: chatRooms){
                        chatRoomNames.add(room.getRoomName());
                    }
                    //Get User Name from Client
                    userName=clientIn.readLine();
                    //Display all available rooms to Client and prompt them to join or create new room
                    clientOut.println("Welcome "+userName+" to Threaded Rooms!");
                    if(chatRoomNames.isEmpty()){
                        clientOut.println("No available rooms, you can create a room by typing 'c'.");
                    }
                    else{
                        clientOut.println("To Join a Room, Type The Name of any of the available Rooms below, or press 'c' to create new room:");
                        for(String roomName: chatRoomNames){

                            clientOut.println("Available Room: "+roomName);
    
                        }
                    }
                    //Get Input from Client
                    while(!clientInRoom){
                        userInput = clientIn.readLine();
                        if(userInput.equals("c")){
                            clientOut.println("Enter the Name of the Room:");
                            userInput = clientIn.readLine();
                            selectedRoom = new Room(userInput);
                            chatRooms.add(selectedRoom);
                            selectedRoom.addClientToRoom(clientOut);
                            clientInRoom=true;
                        }else{
                            //Check if room exists
                            for(Room room: chatRooms){
                                if(room.getRoomName().toLowerCase().equals(userInput.toLowerCase())){
                                    selectedRoom = room;
                                    selectedRoom.addClientToRoom(clientOut);
                                    clientInRoom=true;
                                }
                            }
                            if(selectedRoom == null){
                                clientOut.println("Room does not exist!, type 'c' to create a room");
                            }
                        }
                    }
                    //
                    clientOut.println("You are now in the chat room: " + selectedRoom.getRoomName());

                    String message;
                    while ((message = clientIn.readLine()) != null) {
                        if (message.equals("exit")) {
                            clientInRoom=false;
                            sendMessageToAllMembersInRoom(selectedRoom, userName+" has left the chat...");
                            break;
                        }
                        sendMessageToAllMembersInRoom(selectedRoom, message);
                    }
                    //Remove client from the room
                    selectedRoom.removeClientFromRoom(clientOut);

                }
            } catch (IOException e) {
                
            }
        }
        private void sendMessageToAllMembersInRoom(Room room, String message){
            for (PrintWriter client : room.getClientsInRoom()) {
                if(client.equals(clientOut)){
                    //Dont send message to yourself
                }else{
                    client.println(userName+": " + message);
                }
            }
        }
    }

    static class Room {
        private String chatRoomName="";
        private List<PrintWriter> clients = new ArrayList<>();

        public Room(String name){
            chatRoomName= name;
        }

        public String getRoomName(){
            return chatRoomName;
        }

        public List<PrintWriter> getClientsInRoom (){
            return clients;
        }

        public void addClientToRoom(PrintWriter client){
            clients.add(client);
        }
        public void removeClientFromRoom(PrintWriter client){
            clients.remove(client);
        }

    }
    
}
