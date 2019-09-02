package at.bb.mqtt2mail;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;

/**
 * Looks up an MQTT Topic and sends an email if certain conditions are met
 * 
 * @author gue
 *
 */
public class Mqtt2MailRoute extends RouteBuilder
{

    @Override
    public void configure() throws Exception
    {

        // Decide if the parking lot is occupied or not
        from("mqtt:TheThingsNetwork?host=tcp://{{ttn.region}}.thethings.network:1883&subscribeTopicName={{ttn.app_id}}/devices/{{ttn.device_id}}/up/{{ttn.datafield}}&userName={{ttn.app_id}}&password={{ttn.app_key}}")
                .routeId("mqtt2mailroute").description("Translates MQTT states to emails")
                .convertBodyTo(String.class) // We get a byte[] and want a string
                .log("Parking device {{ttn.device_id}} reports a distance of ${body} cm")
                .choice()
                    .when().simple("${body} > 30").to("direct:occupied") // magic type conversion happens here
                    .otherwise().to("direct:free")
                .end();

        // What to do of a parking lot is free
        from("direct:free").routeId("FreeLot").log("Device {{ttn.device_id}} reports free parking lot").end();

        // Route for occupied parkingLot
        from("direct:occupied").routeId("OccupiedLot")
        .log("Device {{ttn.device_id}} reports occupied parking lot -> send mail to {{notification.emailadress}}")
        .process(new Processor()
        {
            @Override
            public void process(Exchange exchange) throws Exception
            {
                exchange.getOut().setHeader("to",      simple("{{notification.emailadress}}").evaluate(exchange, String.class));
                exchange.getOut().setHeader("subject", "Someone took your parkingplace");
                exchange.getOut().setBody("The distance to the car is " + new String(exchange.getIn().getBody(byte[].class)) + " cm.");
            }
        }).to("smtps://{{mail.server}}:{{mail.port}}?username={{mail.user}}&password={{mail.pass}}").end();
    }
}
