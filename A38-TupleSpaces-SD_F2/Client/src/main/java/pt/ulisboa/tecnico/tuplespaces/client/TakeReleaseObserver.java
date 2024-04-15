package pt.ulisboa.tecnico.tuplespaces.client;

import io.grpc.stub.StreamObserver;
import pt.ulisboa.tecnico.tuplespaces.replicaXuLiskov.contract.TupleSpacesReplicaXuLiskov.TakePhase1ReleaseResponse;
import pt.ulisboa.tecnico.tuplespaces.replicaXuLiskov.contract.TupleSpacesReplicaXuLiskov.TakePhase1Response;

public class TakeReleaseObserver implements StreamObserver<TakePhase1ReleaseResponse>{
    ResponseCollector collector;

    public TakeReleaseObserver (ResponseCollector c){
        collector = c;
    }

    @Override
    public void onNext(TakePhase1ReleaseResponse r) {
        collector.addPutResponse();
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
