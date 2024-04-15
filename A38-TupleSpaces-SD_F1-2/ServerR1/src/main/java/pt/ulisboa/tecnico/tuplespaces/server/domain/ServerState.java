package pt.ulisboa.tecnico.tuplespaces.server.domain;

import java.util.ArrayList;
import java.util.List;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class ServerState {
  
  private List<String> tuples; //List of tuples initiated for this server

  private final Lock lock = new ReentrantLock();  

  public ServerState() {
    this.tuples = new ArrayList<String>();
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
      lock.lock();
      try {
        tuples.add(tuple);
      } finally {
        lock.unlock();
      }
      System.out.println("TUPLES: " + tuples);

      synchronized(this){
          this.notifyAll();
      }
    }
  }

  /* Method for getMatchingTuple function. Receives a tuple in string format 
  and uses for loop to find the matching tuple. Returns all tuples found. */
  private String getMatchingTuple(String pattern) {
    for (String tuple : this.tuples) {
      if (tuple.matches(pattern)) {
        return tuple;
      }
    }
    return null;
  }

  /* Method for take function. Receives a tuple in string format. Uses getMatchingTuples
  funtion to find the tuple and remove it from list. Returns the matching tuple*/
  public String take(String pattern) {
    if(verifyTuple(pattern)==true){ 
      String matchingTuple = null;
      // Iterate through the tuples list
        while(true){
            lock.lock(); 
            try{
              matchingTuple = getMatchingTuple(pattern);
              // If a matching tuple is found
              if (matchingTuple != null) {
                  // Remove the matching tuple from the list
                  tuples.remove(matchingTuple);
                  System.out.println("Tuple removed: " + matchingTuple);
                  System.out.println("TUPLES: " + tuples);
              }
            }finally {
                lock.unlock(); 
            }
            if (matchingTuple != null) {
              return matchingTuple;
            }
            synchronized (this) {
                // Wait for notification from other threads
                // Only if the tuple is not found
                // Release the lock while waiting
                try{
                    wait();
                }catch (InterruptedException e) {
                  e.printStackTrace();
                }
            }
        }
    }
    return "";
  }


  /*Method for read function. Receives a tuple in string format. Uses getMatchingTuples
  funtion to find the tuple and returns the matching tuple*/
  public String read(String pattern){
    if(verifyTuple(pattern)==true){ 
      // Iterate through the tuples list
      String matchingTuple = null;
      while(true){
          lock.lock();
          try{
            matchingTuple = getMatchingTuple(pattern);
            // If a matching tuple is found
            if (matchingTuple != null) {
                // Remove the matching tuple from the list
                System.out.println("Tuple read: " + matchingTuple);
                System.out.println("TUPLES: " + tuples);
            }
          }finally {
              lock.unlock(); // Always release the lock
          }
          if (matchingTuple != null) {
            return matchingTuple;
          }
          synchronized (this) {
              // Wait for notification from other threads
              // Only if the tuple is not found
              // Release the lock while waiting
              try{
                  wait();
              }catch (InterruptedException e) {
                e.printStackTrace();
              }
          }
      }
    }
    return "";
  }

  //Method for getTupleSpacesState function. Returns all server tuples. 
  public List<String> getTupleSpacesState() {
      //System.out.println("SERVER TUPLES: " + serverTuples);
      System.out.println("SERVER TUPLES: " + tuples);
      return tuples; //serverTuples;
  }
}
