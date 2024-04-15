package pt.ulisboa.tecnico.tuplespaces.server.domain;

import java.util.ArrayList;
import java.util.List;

import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;


public class ServerState {
  
  private ArrayList<TupleSpaceState> tuples; //List of tuples initiated for this server.

  private ReadWriteLock lock = new ReentrantReadWriteLock();

  public ServerState() {
    this.tuples = new ArrayList<>();
  }

  //Method for verifying tupple. Receives a tuple in string format and checks if it is a valid tuple. 
  public boolean verifyTuple(String tuple) {
    // Check if the tuple has at least 3 characters
    if ((tuple.length() < 3) || (tuple.charAt(0) != '<') || (tuple.charAt(tuple.length() - 1) != '>')) {
      return false;
    }
    for (int i = 1; i < tuple.length() - 1; i++) {
        if (tuple.charAt(i) == ' ') {
            return false;
        }
    }
    return true;
  }

  //Method for put function. Receives a tuple in string format and adds it to the list of tuples.
  public void put(String tuple) {
    if(verifyTuple(tuple)==true){ 
      lock.writeLock().lock();
      try {
        // Create a new TupleSpaceState object with the tuple
        TupleSpaceState extra_tuple= new TupleSpaceState();
        ArrayList<String> tuple_lst = new ArrayList<>();
        tuple_lst.add(tuple);

        extra_tuple.setTuple(tuple_lst);

        tuples.add(extra_tuple);
      } finally {
        lock.writeLock().unlock();
      }
      // Notify all waiting threads that a new tuple has been added
      synchronized(this){
          this.notifyAll();
      }
    }
  }

  /* Method for getMatchingTuple function. Receives a tuple in string format 
  and uses for loop to find the matching tuple. Returns all tuples found. */
  private ArrayList<String> getMatchingTuple(String pattern) {
    for (TupleSpaceState tuple : tuples) {
        if (tuple.getTuple().get(0).matches(pattern)) {
            return tuple.getTuple();
        }
    }
    return null;
  }

  //Method that returns list of matching tuples available. Receives a patters and a clientId. 
  private ArrayList<TupleSpaceState> getMatchingTupleList(String pattern, int clientId) {
    ArrayList<TupleSpaceState> list = new ArrayList<>();
    // Initialize a counter to know the number of matching tuples
    int count = 0;
    // Iterate through each tuple in the tuple space, check if the tuple matches and increment counter. 
    for (TupleSpaceState tuple : tuples) {
        if (tuple.getTuple().get(0).matches(pattern)) { //if matches
            count++;
            //add only first tuple that is unlocked and with given clientId 
            if((!containsTuple(list, tuple.getTuple().get(0))) && (tuple.getState() == true) && (tuple.getClientId() == clientId)){
              list.add(tuple); 
            }
            // Update the tuple's state and clientId if its unlocked
            else if((!containsTuple(list, tuple.getTuple().get(0))) && (tuple.getState() == false) && (tuple.getClientId() != clientId)){
              tuple.setState(true);
              tuple.setClientId(clientId);
              list.add(tuple);
            }
        }
    }
    // Add an empty tuple to signify no matches
    if(count > 0 && list.isEmpty()){
      TupleSpaceState empty = new TupleSpaceState();
      ArrayList<String> emptyList = new ArrayList<>();
      emptyList.add("empty");
      empty.setTuple(emptyList);
      list.add(empty);
    }
    return list;
  }

  //Method that specifies if tuple already exists in list.
  private boolean containsTuple(ArrayList<TupleSpaceState> list, String tupleElement) {
    // Iterate through each TupleSpaceState object in the list and checks if a tuple already exists with same elements 
    for (TupleSpaceState tupleState : list) {
        if (tupleState.getTuple().get(0).equals(tupleElement)) {
            return true;
        }
    }
    return false;
  }

  //Method for takePhase1. Receives a patters and a clientId and retreives tuples to send to phase2. 
  public ArrayList<TupleSpaceState> takePhase1(String pattern, int clientId){
    if(verifyTuple(pattern)==true){
      ArrayList<TupleSpaceState> matchingTuples = new ArrayList<>();
      while(true){
        lock.writeLock().lock();
        try{
          //list of all tuples that contain the string pattern
          matchingTuples=getMatchingTupleList(pattern, clientId);
        } finally {
          lock.writeLock().unlock(); // Always release the lock
        }
        //not found any tuples with string pattern, wait until tuple with the pattern is put
        if(matchingTuples.size()==0){
         //No tuples were found. Waiting.
          synchronized (this) {
            try{
                wait();
            }catch (InterruptedException e) {
              e.printStackTrace();
            }
          }
        }
        //found tuple with the pattern wanted
        else if (matchingTuples.get(0).getTuple().get(0).equals("empty")){
          //the tuple was already locked
          //Empty Tuple Found.
          return new ArrayList<>();
        } else {
          //return all matching tuples
          return matchingTuples;
        } 
      }
    }
    return null;
  }

  //method for takeRelease. Receives a clientId to unlock all tuples locked by this client. 
  public void takeRelease(int clientId){
    for (TupleSpaceState tuple : tuples) {
      //if the tuple was locked by this client
      if (clientId == tuple.getClientId() && (tuple.getState() == true)){
          tuple.setState(false);
          tuple.setClientId(-1);
      }  
    }
  }

  //Method for takePhase2. Recceives a pattern and a clientId to finally remove tuples from list sent in phase1.
  public void takePhase2(String pattern, int clientId){
    TupleSpaceState removeTuple = new TupleSpaceState();
    if(verifyTuple(pattern)==true){
        lock.writeLock().lock();
        try{
          for (TupleSpaceState tuple : tuples) {
          //if the tuple was locked by this client
            if ((clientId == tuple.getClientId()) && (tuple.getState() == true)){
              if (tuple.getTuple().get(0).equals(pattern)) {
                removeTuple = tuple; // Save the tuple to be removed
              }
              //Reset the tuple to unlocked and no clientId associated with a request
              tuple.setState(false);
              tuple.setClientId(-1);
            }   
          }
          tuples.remove(removeTuple); // finally remove tuple from list in server
        }finally {
          lock.writeLock().unlock(); // Always release the lock
        }
    }
  }

  /*Method for read function. Receives a tuple in string format. Uses getMatchingTuples
  funtion to find the tuple and returns the matching tuple*/
  public String read(String pattern){
    if(verifyTuple(pattern)==true){ 
      // Iterate through the tuples list
      TupleSpaceState matchingTuple = new TupleSpaceState();    
      while(true){
          lock.readLock().lock();
          try{
            matchingTuple.setTuple(getMatchingTuple(pattern));
            // If a matching tuple is found
          }finally {
              lock.readLock().unlock(); // Always release the lock
          }
          if (matchingTuple.getTuple() != null) {
            return matchingTuple.getTuple().get(0);
          }
          synchronized (this) {
              // Wait for notification from other threads
              try{
                  wait();
              }catch (InterruptedException e) {
                e.printStackTrace();
              }
          }
      }
    }
    return null;
  }

  //Method for getTupleSpacesState function. Returns a copy of all server tuples. 
  public List<String> getTupleSpacesState() {
    //copy created of current state of server tuples
    ArrayList<String> tuples_copy = new ArrayList<>();
    lock.readLock().lock();
    try{
      for(TupleSpaceState tuple: tuples){
        tuples_copy.add(tuple.getTuple().get(0)); //add tuple to list
      }      
    } finally{
      lock.readLock().unlock();
    }
    return tuples_copy; //serverTuples;
  }
}
