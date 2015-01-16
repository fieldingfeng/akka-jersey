package com.example;

import java.math.BigInteger;

import akka.actor.ActorRef;
import akka.actor.UntypedActor;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import akka.routing.FromConfig;

public class AkkaFactorialMaster extends UntypedActor {

	LoggingAdapter log = Logging.getLogger(getContext().system(), this);
	ActorRef backend = getContext().actorOf(FromConfig.getInstance().props(),
		      "workerRouter");

	ActorRef sender;
	
	BigInteger result;
	int slice;
	int sliceCompleted;

	
	@Override
	public void onReceive(Object msg) throws Exception {
		// TODO Auto-generated method stub
		if(msg instanceof BigInteger) {
			log.info("got partial result " + msg);
			
			if(result == null) {
				result = (BigInteger) msg;
			} else {
				result = result.multiply((BigInteger) msg);
			}
			sliceCompleted++;
			
			if(sliceCompleted == slice) {
				log.info("full result : " + result);
				sender.tell(result, sender);
				
				slice = 0;
				sliceCompleted = 0;
			}
//			backend.tell(new Work(1, 200), getSelf());
		} else if (msg instanceof Integer){
			log.info("Got job : " + msg);
			sender = getSender();
			
			slice = 0;
			sliceCompleted = 0;
			int from = 1;
			int to = from + 19;
			boolean done = false;
			
			while(to <= (int)msg) {
				backend.tell(new Work(from, to), getSelf());
				slice++;
				
				from += 20;
				to +=20;
			}
			
			if(to > (int) msg && from < (int) msg) {
				backend.tell(new Work(from, (int) msg), getSelf());
				slice++;
			}

		}
		else {
			unhandled(msg);
		}
	}

}
