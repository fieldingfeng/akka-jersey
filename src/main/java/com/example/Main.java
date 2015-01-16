package com.example;

import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;

import scala.concurrent.duration.Duration;
import akka.actor.ActorSystem;
import akka.actor.Props;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

import java.io.IOException;
import java.net.URI;
import java.util.concurrent.TimeUnit;

import javax.annotation.PreDestroy;

/**
 * Main class.
 *
 */
public class Main {
    // Base URI the Grizzly HTTP server will listen on
    public static final String BASE_URI = "http://localhost:8080/myapp/";

    /**
     * Starts Grizzly HTTP server exposing JAX-RS resources defined in this application.
     * @return Grizzly HTTP server.
     */
    public static HttpServer startServer() {
    	
        // create a resource config that scans for JAX-RS resources and providers
        // in com.example package
        final ResourceConfig rc = new MyResourceConfig().packages("com.example");
        
        // create and start a new instance of grizzly http server
        // exposing the Jersey application at BASE_URI
        return GrizzlyHttpServerFactory.createHttpServer(URI.create(BASE_URI), rc);
    }

    /**
     * Main method.
     * @param args
     * @throws IOException
     */
    public static void main(String[] args) throws IOException {
    	
        final HttpServer server = startServer();
        System.out.println(String.format("Jersey app started with WADL available at "
                + "%sapplication.wadl\nHit enter to stop it...", BASE_URI));
        System.in.read();
        server.stop();
    }
    
    public static class MyResourceConfig extends ResourceConfig {
    	private ActorSystem system1;
    	private ActorSystem system2;
    	private ActorSystem system3;
    	
    	public static ActorSystem createActorSystem(String [] args) {
    	    final String port = args.length > 0 ? args[0] : "0";
    	    final Config config = ConfigFactory.parseString("akka.remote.netty.tcp.port=" + port).
    	      withFallback(ConfigFactory.parseString("akka.cluster.roles = [backend]")).
    	      withFallback(ConfigFactory.load("factorial"));

    	    ActorSystem system = ActorSystem.create("ClusterSystem", config);

    	    system.actorOf(Props.create(AkkaFactorialWorker.class), "worker");

    	    return system;

    	}

    	public MyResourceConfig() {
        	system1 = createActorSystem(new String [] {"2551"});
        	system2 = createActorSystem(new String [] {"2552"});
        	system3 = createActorSystem(new String [] {"0"});
        	
        	system1.actorOf(Props.create(AkkaFactorialMaster.class), "master");

    		
    		register(new AbstractBinder() {
				@Override
				protected void configure() {
					bind(system1).to(ActorSystem.class);
				}
			});
    	}
    	
    	@PreDestroy
    	private void shutdown() {
    		system1.shutdown();
    		system2.shutdown();
    		system3.shutdown();
    		system1.awaitTermination(Duration.create(15, TimeUnit.SECONDS));
    		system2.awaitTermination(Duration.create(15, TimeUnit.SECONDS));
    		system3.awaitTermination(Duration.create(15, TimeUnit.SECONDS));
    	}
    }
}

