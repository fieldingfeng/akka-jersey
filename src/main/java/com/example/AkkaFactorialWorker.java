package com.example;

import java.math.BigInteger;

import akka.actor.UntypedActor;
import akka.event.Logging;
import akka.event.LoggingAdapter;

public class AkkaFactorialWorker extends UntypedActor {
	LoggingAdapter log = Logging.getLogger(getContext().system(), this);

	@Override
	public void onReceive(Object msg) throws Exception {
		if (msg instanceof Work) {
			Work work = (Work) msg;
			log.info("Got work: " + work.from + "-" + work.to);
			BigInteger result = BigInteger.valueOf(work.from);
			for (int i = work.from; i <= work.to; i++) {
				result = result.multiply(BigInteger.valueOf(i));
			}

			getSender().tell(result, getSelf());
		} else {
			unhandled(msg);
		}
	}

}
