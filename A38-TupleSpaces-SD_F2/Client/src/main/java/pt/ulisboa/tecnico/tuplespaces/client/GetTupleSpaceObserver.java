package pt.ulisboa.tecnico.tuplespaces.client;

import java.util.ArrayList;

import io.grpc.stub.StreamObserver;
import pt.ulisboa.tecnico.tuplespaces.replicaXuLiskov.contract.*;
import pt.ulisboa.tecnico.tuplespaces.replicaXuLiskov.contract.TupleSpacesReplicaXuLiskov.getTupleSpacesStateResponse;


public class GetTupleSpaceObserver implements StreamObserver<getTupleSpacesStateResponse>{
    ResponseCollector collector;

    public GetTupleSpaceObserver (ResponseCollector c){
        collector = c;
    }

    @Override
    public void onNext(getTupleSpacesStateResponse r) {
        ArrayList<String> r_list = new ArrayList<>();
        for(String list : r.getTupleList()){
            r_list.add(list);
        }
        collector.addGetTupleSpacesStateResponse(r_list);
        //System.out.println("Received response" + r);
    }

    @Override
    public void onError(Throwable throwable) {
        System.out.println("Received error: " + throwable);
    }

    @Override
    public void onCompleted() {
        //System.out.println("Request completed");
    }
}

