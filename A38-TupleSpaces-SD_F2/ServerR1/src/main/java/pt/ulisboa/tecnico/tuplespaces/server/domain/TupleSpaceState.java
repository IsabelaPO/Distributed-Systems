package pt.ulisboa.tecnico.tuplespaces.server.domain;

import java.util.ArrayList;
//import java.util.List;

public class TupleSpaceState{
    private ArrayList<String> tuple;
    private boolean state;
    private int clientId;

    public TupleSpaceState() {
        this.tuple = new ArrayList<String>();
        this.state = false;
        this.clientId = -1;
    }

    synchronized public ArrayList<String> getTuple(){return this.tuple;}
    synchronized public void setTuple(ArrayList<String> tuple){ this.tuple = tuple;}

    synchronized public boolean getState(){return this.state;}
    synchronized public void setState(boolean state){ this.state = state;}

    synchronized public int getClientId(){return this.clientId;}
    synchronized public void setClientId(int clientId){ this.clientId = clientId;}

}