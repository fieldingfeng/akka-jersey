package com.example;

import java.math.BigInteger;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

import scala.concurrent.Await;
import scala.concurrent.Future;
import scala.concurrent.duration.Duration;
import akka.actor.ActorRef;
import akka.actor.ActorSelection;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.pattern.Patterns;

/**
 * Root resource (exposed at "myresource" path)
 */
@Path("myresource")
public class MyResource {

	@Context
	ActorSystem system;

	/**
	 * Method handling HTTP GET requests. The returned object will be sent to
	 * the client as "text/plain" media type.
	 * 
	 * @return String that will be returned as a text/plain response.
	 */
	@GET
	@Produces(MediaType.TEXT_PLAIN)
	public String getIt() {
		return "Got it!";
	}

	@GET
	@Produces(MediaType.TEXT_PLAIN)
	@Path("factorial/{number}")
	public String factorial(@PathParam("number") int number) {
		String result;
		
		ActorSelection actor = system.actorSelection("/user/master");
	
//		ActorRef actor = system.actorOf(Props.create(AkkaFactorialMaster.class));
		
		Future<Object> future = Patterns.ask(actor, number, 10000);

		try {
			result = ((BigInteger) Await.result(future, Duration.create(10, "seconds"))).toString();
		} catch (Exception e) {
			return e.getMessage();
		}
		
		return result;
	}
}
