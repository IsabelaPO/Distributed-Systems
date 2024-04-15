package pt.ulisboa.tecnico.tuplespaces.client.grpc;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;
import pt.ulisboa.tecnico.tuplespaces.replicaXuLiskov.contract.*;
import pt.ulisboa.tecnico.tuplespaces.client.GetTupleSpaceObserver;
import pt.ulisboa.tecnico.tuplespaces.client.PutObserver;
import pt.ulisboa.tecnico.tuplespaces.client.ReadObserver;
import pt.ulisboa.tecnico.tuplespaces.client.TakeObserver;
import pt.ulisboa.tecnico.tuplespaces.client.TakePhase2Observer;
import pt.ulisboa.tecnico.tuplespaces.client.TakeReleaseObserver;
import pt.ulisboa.tecnico.tuplespaces.client.ResponseCollector;
import pt.ulisboa.tecnico.tuplespaces.client.util.OrderedDelayer;
import pt.ulisboa.tecnico.tuplespaces.replicaXuLiskov.contract.TupleSpacesReplicaXuLiskov.getTupleSpacesStateRequest;
import pt.ulisboa.tecnico.tuplespaces.replicaXuLiskov.contract.TupleSpacesReplicaXuLiskov.getTupleSpacesStateResponse;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ClientService {

    OrderedDelayer delayer;
    int num_servers;

    ManagedChannel[] channels = new ManagedChannel[3];
    TupleSpacesReplicaGrpc.TupleSpacesReplicaStub[] stubs = 
			new TupleSpacesReplicaGrpc.TupleSpacesReplicaStub[3];

    //Method for initializing ClientService class.
    public ClientService(int numServers, Map<String, String> targets) {
        num_servers = numServers;
        // Loop through the specified number of servers, which is 3
        for (int i = 0; i < numServers; i++) {
            // Get the target address for the current server (A, B or C) using ASCII. 
            String target = targets.get(Character.toString((char)('A' + i))); 
            // If a target address exists for the current server, create a channel using the target address 
            // and create a stub for the current server
            if (target != null) {
                channels[i] = ManagedChannelBuilder.forTarget(target).usePlaintext().build();
                stubs[i] = TupleSpacesReplicaGrpc.newStub(channels[i]);
            }
        }
        delayer = new OrderedDelayer(numServers);
    }

    /* This method allows the command processor to set the request delay assigned to a given server */
    public void setDelay(int id, int delay) {
        delayer.setDelay(id, delay);
    }

    //Method for operation put()
    public void put(String tuple) throws InterruptedException{
        ResponseCollector c = new ResponseCollector();        
        //send request to all servers
        TupleSpacesReplicaXuLiskov.PutRequest request = TupleSpacesReplicaXuLiskov.PutRequest.newBuilder().setNewTuple(tuple).build();
        for(Integer id: delayer){
            stubs[id].put(request, new PutObserver(c));
        }
        //wait for response of all servers
        c.waitUntilAllReceived(num_servers);
    }

    // Method for take operation
    public String take(String tuple, int clientID) throws InterruptedException {
        String answer = "";
        ResponseCollector c = new ResponseCollector();
        TupleSpacesReplicaXuLiskov.TakePhase1Request request = TupleSpacesReplicaXuLiskov.TakePhase1Request.newBuilder().setSearchPattern(tuple).setClientId(clientID).build();
        for(Integer id: delayer){
            stubs[id].takePhase1(request, new TakeObserver(c));        
        }
        c.waitUntilAllReceivedTake(num_servers);

        int totalAccepts = 0;
        for (ArrayList<String> response : c.getTakeResponse()) {
            //server accepted the request
            if(response.size() != 0){
                totalAccepts++;
            }
        }

        //all servers responded but only a minority accepted the request
        if(totalAccepts == 0 || totalAccepts == 1){
            ResponseCollector res = new ResponseCollector();
            TupleSpacesReplicaXuLiskov.TakePhase1ReleaseRequest releaseRequest = TupleSpacesReplicaXuLiskov.TakePhase1ReleaseRequest.newBuilder().setClientId(clientID).build();
            for(Integer id: delayer){
                stubs[id].takePhase1Release(releaseRequest, new TakeReleaseObserver(res));
            }
            
            res.waitUntilAllReceivedTake(num_servers);

            // Wait for the desired amount of time
            long startTime = System.currentTimeMillis();
            long elapsedTime = 0;
            while (elapsedTime < 2 * 1000) {
                elapsedTime = System.currentTimeMillis() - startTime;
            }
            answer = take(tuple, clientID);
        }
        //all servers responded but only a majority accepted the request
        else if(totalAccepts == 2){
            answer = take(tuple, clientID);
        }
        //all servers responded and accepted the request
        else{  
            String result = c.findCommonString();
            ResponseCollector res = new ResponseCollector();
            //tuple set is not empty, select tuple and go to phase 2
            TupleSpacesReplicaXuLiskov.TakePhase2Request requestPhase2 = TupleSpacesReplicaXuLiskov.TakePhase2Request.newBuilder().setTuple(result).setClientId(clientID).build();
            for(Integer id: delayer){
                stubs[id].takePhase2(requestPhase2, new TakePhase2Observer(res));
            }
            res.waitUntilAllReceived(num_servers);
            return result;
        }
        return answer;
    }

    //Method for read operation
    public String read(String tuple) throws InterruptedException {
        ResponseCollector c = new ResponseCollector();
        TupleSpacesReplicaXuLiskov.ReadRequest request = TupleSpacesReplicaXuLiskov.ReadRequest.newBuilder().setSearchPattern(tuple).build();
        for(Integer id: delayer){
            stubs[id].read(request, new ReadObserver(c));
        }
        c.waitUntilAllReceived(1);
       
        return c.getStringsRead();
      }
    
    //Method for getTupleString operation
    public String getTuppleString(int index) throws InterruptedException{
        ResponseCollector c = new ResponseCollector();
        TupleSpacesReplicaXuLiskov.getTupleSpacesStateRequest request = TupleSpacesReplicaXuLiskov.getTupleSpacesStateRequest.getDefaultInstance();
        stubs[index].getTupleSpacesState(request, new GetTupleSpaceObserver(c));
        c.waitUntilAllReceived(1);
        return c.getTupleSpaceStrings();
    }
    // Method to close channel gRPC when client is closed 
    public void shutdown() {
        for (int i = 0; i < 3; i++) {
            channels[i].shutdown();
        }
    }

}