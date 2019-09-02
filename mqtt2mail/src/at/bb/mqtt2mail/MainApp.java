package at.bb.mqtt2mail;

import org.apache.camel.CamelContext;
import org.apache.camel.component.properties.PropertiesComponent;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.impl.JndiRegistry;

/**
 * This is the mainclass of the project
 * 
 * @author gue
 *
 */
public class MainApp
{
    public static void main(String[] args) throws Exception
    {
      //System.out.println("(\\w+)=\"*((?<=\")[^\"]+(?=\")|(?:[^\\s]+))\"*");
        PropertiesComponent pc = new PropertiesComponent();
        pc.setLocation("classpath:at/bb/mqtt2mail/application.properties");
        
        // Global registry
        JndiRegistry registry = new JndiRegistry();
        
        // Create the camel context and announce the routes
        CamelContext context = new DefaultCamelContext(registry);
        context.addComponent("properties", pc);
        context.addRoutes(new Mqtt2MailRoute());
        
        // Start the whole thing !
        context.start();
        
        // set the shutdown strategy
        context.getShutdownStrategy().setLogInflightExchangesOnTimeout(true);

        // ... and do not immediately exit
        Thread.currentThread().join();

    }
}
