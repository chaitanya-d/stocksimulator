/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.functionspace.stocksimulator.contextlistener;

import com.functionspace.stocksimulator.producer.StockUpdateProducer;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
/**
 * Class that implements ServletContextListener and this is the starting point
 * of the application from where Producer thread is started
 * 
 * @author Chaitanya
 */
public class StockMarketAppContextListener implements ServletContextListener
{
    String[] namesArray = {"BMW", "Massarati", "Pagani"};
    StockUpdateProducer stockUpdateProducer = StockUpdateProducer.getInstance(3, namesArray, 50.50, 150.25);
    @Override
    public void contextInitialized(ServletContextEvent sce) 
    {
        //Start Stock Update producer thread which will push data on web socket every 10 seconds
        stockUpdateProducer.start();
        System.out.println("ServletContext started, Stock Update Producer started!!!"); 
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) 
    {
        //stockUpdateProducer.stop();
        System.out.println("ServletContext stopped, Stock Update Producer stopped!!!");
    }
    
}
