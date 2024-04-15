package pt.ulisboa.tecnico.tuplespaces.client;

import java.util.ArrayList;
import java.util.List;

import pt.ulisboa.tecnico.tuplespaces.replicaXuLiskov.contract.TupleSpacesReplicaXuLiskov.ReadResponse;

public class ResponseCollector {
    ArrayList<String> collectedResponses;
    private List<ArrayList<String>> collectedTakeResponses;
    private ArrayList<String> collectedGetTupleSpaceResponses;
    int numResponses;

    public ResponseCollector() {
        collectedResponses = new ArrayList<>();
        collectedTakeResponses = new ArrayList<>();
        collectedGetTupleSpaceResponses = new ArrayList<>();
    }

    synchronized public void addResponse(String r) { 
        collectedResponses.add(r);
        numResponses++;    
        notifyAll();
    }

    synchronized public void addTakeResponse(ArrayList<String> r) { 
        collectedTakeResponses.add(r);
        numResponses++;    
        notifyAll();
    }

    synchronized public List<ArrayList<String>> getTakeResponse(){
        return collectedTakeResponses;
    }

    synchronized public void addPutResponse() { 
        numResponses++;    
        notifyAll();
    }

    synchronized public void addGetTupleSpacesStateResponse(ArrayList<String> r) { 
        for(String r_str : r){
            collectedGetTupleSpaceResponses.add(r_str);
        }
        numResponses++;    
        notifyAll();
    }

    synchronized public void addTakePhase2Response() { 
        numResponses++;    
        notifyAll();
    }    

    synchronized public String getStrings() {
        String res = new String();
        for (String s : collectedResponses) {
            res = res.concat(s);
            res = res + "\n";
        }
        return res;
    }

    synchronized public String getTupleSpaceStrings() {
        String res = new String();
        res = res.concat("[");
        for(String tuple : collectedGetTupleSpaceResponses){        
            res = res.concat(tuple);
            res = res.concat(", ");
        }
        // remove last ", " from string
        res = res.substring(0, res.length() - 2);
        res = res.concat("]");
        return res; 
    }


    synchronized public String getStringsRead() {
        String res = "";
        for (String s : collectedResponses) {
            res = res.concat(s);
            res = res + "\n";
            if(res!=""){
                return res;
            }
        }
        return "";
    }

    //find first common string between all replicas
    synchronized public String findCommonString() {
        if (collectedTakeResponses.isEmpty()) {
            return null; // No common string if there are no lists
        }

        // Get the first ArrayList to use as reference
        ArrayList<String> firstList = collectedTakeResponses.get(0);

        // Iterate over the strings in the first ArrayList
        for (String str : firstList) {
            // Check if the string exists in all other ArrayLists
            boolean foundInAll = true;
            for (int i = 1; i < collectedTakeResponses.size(); i++) {
                ArrayList<String> list = collectedTakeResponses.get(i);
                if (!list.contains(str)) {
                    foundInAll = false;
                    break;
                }
            }
            // If the string is found in all other ArrayLists, return it
            if (foundInAll) {
                return str;
            }
        }
        // No common string found
        return null;
    }

    synchronized public void waitUntilAllReceived(int n) throws InterruptedException {
        while (numResponses < n) 
            wait();
    }

    synchronized public int waitUntilAllReceivedTake(int n) throws InterruptedException {
        while (numResponses < n){
            wait();
        }
        return collectedResponses.size();
    }
}