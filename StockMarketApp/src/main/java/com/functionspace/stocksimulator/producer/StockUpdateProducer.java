/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.functionspace.stocksimulator.producer;

import com.functionspace.stocksimulator.Stock;
import com.functionspace.stocksimulator.StockMarketUpdateSocket;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.websocket.EncodeException;
import java.util.*;
import org.json.JSONArray;
import org.json.JSONObject;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.text.DecimalFormat;
import java.sql.Time;
import org.json.JSONException;
/**
 *
 * @author Chaitanya
 */
public class StockUpdateProducer extends Thread
{
    private static StockUpdateProducer stockUpdateProducerInstance = null;
    StockMarketUpdateSocket marketUpdateSocket =  new StockMarketUpdateSocket();
    private Stock[] stockArrObj = null;
    private JSONArray chartColumnJsonArray = new JSONArray();
    private HashMap<String, Object> socketDataWrapper = new HashMap<String, Object>();
    /**
     * Private constructor so that cant instantiated directly, only way is to through
     * getInstance method which makes this class as a Singleton
     * 
     * Creates a chart columns json in below format
     * 
     * [
        {id: "month", label: "Month", type: "string"},
        {id: "peninfiria-id", label: "BMW", type: "number"},
        {id: "massaratip-id", label: "Massarati", type: "number"},
        {id: "volvo-id", label: "Pagani", type: "number"}
        ]
     * @param numberOfStocks
     * @param stockNameArray
     * @param minRange
     * @param maxRange 
     */
    private StockUpdateProducer(int numberOfStocks, String[] stockNameArray, Double minRange, Double maxRange) 
    {
        this.stockArrObj = new Stock[numberOfStocks];
        //initialize stock array and also create a columns JSON to show stock names on chart
        List chartColumnsEleList = new ArrayList<HashMap<String,Object>>();
        HashMap<String,Object> chartColumnsElementMap = new HashMap<String,Object>();
        chartColumnsElementMap.put("id", "month");
        chartColumnsElementMap.put("label", "Month");
        chartColumnsElementMap.put("type", "string");
        chartColumnsEleList.add(chartColumnsElementMap);
        for(int totalStocks = 0; totalStocks < numberOfStocks; totalStocks++)
        {
            stockArrObj[totalStocks] = new Stock(stockNameArray[totalStocks], minRange, maxRange);
            chartColumnsElementMap = new HashMap<String,Object>();
            chartColumnsElementMap.put("id", stockNameArray[totalStocks].toLowerCase());
            chartColumnsElementMap.put("label", stockNameArray[totalStocks]);
            chartColumnsElementMap.put("type", "number");
            chartColumnsEleList.add(chartColumnsElementMap);
        }
        //set it to a variable which will be passed agains charCollumns key in web socket data wrapper
        chartColumnJsonArray = new JSONArray(chartColumnsEleList);
        System.out.println("chartColumnJsonArray : " + chartColumnJsonArray.toString());
    }
    /**
     * Created StockUpdateProducer as a singleton class
     * 
     * @param numberOfStocks
     * @param stockNameArray
     * @param minRange
     * @param maxRange
     * @return 
     */
    public static StockUpdateProducer getInstance(int numberOfStocks, String[] stockNameArray, Double minRange, Double maxRange)
    {
        if(stockUpdateProducerInstance == null)
        {
            stockUpdateProducerInstance = new StockUpdateProducer(numberOfStocks, stockNameArray, minRange, maxRange);
        }
        return stockUpdateProducerInstance; 
    }
    
    /**
     * Thread class method overriden in this class to implement Stack Price generation logic
     */
    public void run() 
    {
        while(true)
        {
            
            System.out.println("Generating Stock values.............");
            try 
            {
                //check if current time is witin working hours of Market(09:15 to 15:15)
                if(checkIfWorkingHours())
                    marketUpdateSocket.broadcastStockUpdates(getStockUpdateJson());
                else
                {
                    //current time is not between working hours, dont generate and push actual data
                    //push data with status as MARKET_NOT_STARTED and chart data as empty
                    System.out.println("Market is not open yet....");
                    socketDataWrapper.put("message", "MARKET_NOT_STARTED");
                    socketDataWrapper.put("status", "NO_DATA");
                    socketDataWrapper.put("chartData", "");
                    socketDataWrapper.put("chartColumns", chartColumnJsonArray);
                    JSONObject chartDataJson =  new JSONObject(socketDataWrapper);
                    marketUpdateSocket.broadcastStockUpdates(chartDataJson.toString());
                    
                }
            } catch (IOException ex) {
                Logger.getLogger(StockUpdateProducer.class.getName()).log(Level.SEVERE, null, ex);
            } catch (EncodeException ex) {
                Logger.getLogger(StockUpdateProducer.class.getName()).log(Level.SEVERE, null, ex);
            }
            try 
            {
                sleep(10000);
            } 
            catch (InterruptedException e) 
            { 
                System.out.println("Thread was interrupted.............");
            }
        }
    }
    
    /**
     * Method to generate JSON required for Chart API in Angular JS
     * It also generates random price within given range using another method
     * 
     * Required Format
     * [
        {c: [
            {v: "12:12"},//time
            {v: 5},//price of Stack 1 at above time
            {v: 22},//price of Stack 2 at above time
            {v: 12}//price of Stack 3 at above time
        ]},
        {c: [
            {v: "12:14"},
            {v: 21},
            {v: 10},
            {v: 11}
        ]},
        {c: [
            {v: "12:16"},
            {v: 4},
            {v: 15},
            {v: 21}

        ]}
      ]   
     */
    private String getStockUpdateJson()
    {
        JSONObject chartDataJson =  new JSONObject();
        try {
            Date currentDate = new Date(), lastStockUpdateDate;
            ArrayList<Double> latestStockPriceList = new ArrayList<Double>();
            //generate random price for given stocks
            for(Stock stockObj: stockArrObj)
            {
                //when applicaiton starts, getLastStockUpdateDate() will return null
                lastStockUpdateDate = Stock.getLastStockUpdateDate();
                if(lastStockUpdateDate == null)
                {
                    Stock.setLastStockUpdateDate(currentDate);
                    lastStockUpdateDate = currentDate;
                }//clears yesterdays data by comparing date. At any point system will hold stock updates of current date
                if(compareDate(currentDate, lastStockUpdateDate))
                    stockObj.setChartFinalDataJson(new ArrayList<HashMap<String,Object>>());
                Double newStockPrice = generateNewStockPrice(stockObj.getMinPriceLimit(), stockObj.getMaxPriceLimit());
                latestStockPriceList.add(newStockPrice);
            }
            Stock.setLastStockUpdateDate(currentDate);
            //Generate required json object with latest price details and add it to arraylist containing 
            // containing older data
            HashMap<String,Object> rootKeyC = new HashMap<String,Object>();

            List rootKeyPriceArray = new ArrayList<HashMap<String,Object>>();
            HashMap<String,Object> chartIndividualElementMap = new HashMap<String,Object>();
            
            chartIndividualElementMap.put("v", currentDate.getHours() + ":" + currentDate.getMinutes() + ":" + currentDate.getSeconds());
            rootKeyPriceArray.add(chartIndividualElementMap);
            
            chartIndividualElementMap = new HashMap<String,Object>();
            chartIndividualElementMap.put("v", latestStockPriceList.get(0));
            rootKeyPriceArray.add(chartIndividualElementMap);

            chartIndividualElementMap = new HashMap<String,Object>();
            chartIndividualElementMap.put("v", latestStockPriceList.get(1));
            rootKeyPriceArray.add(chartIndividualElementMap);

            chartIndividualElementMap = new HashMap<String,Object>();
            chartIndividualElementMap.put("v", latestStockPriceList.get(2));
            rootKeyPriceArray.add(chartIndividualElementMap);

            rootKeyC.put("c", rootKeyPriceArray);
            Stock.addStockUpdateJSONObject(rootKeyC);
            System.out.println("Total Updates : " + Stock.getChartFinalDataJson().size());
            //put values in wrapper to push it on web socket
            socketDataWrapper.put("message", "DATA_AVAILABLE");
            socketDataWrapper.put("status", "SUCCESS");
            socketDataWrapper.put("chartData", new JSONArray(Stock.getChartFinalDataJson()));
            socketDataWrapper.put("chartColumns", chartColumnJsonArray);
            chartDataJson =  new JSONObject(socketDataWrapper);
        } catch (Exception ex) {
            Logger.getLogger(StockUpdateProducer.class.getName()).log(Level.SEVERE, null, ex);
        }
        return  chartDataJson.toString();
    }
    
    /**
     * Method to generate new price for a stock within given min and max range
     * 
     * @param minPriceLimit
     * @param maxPriceLimit
     * @return 
     */
    private Double generateNewStockPrice(Double minPriceLimit, Double maxPriceLimit)
    {
        //System.out.println(minPriceLimit + " " + maxPriceLimit);
        Double range = maxPriceLimit - minPriceLimit;
        Double scaled = new Random().nextDouble() * range;
        //System.out.println(" scaled: " + scaled);
        Double shifted = scaled + minPriceLimit;
        //System.out.println(" shifted: " + shifted);
        return Double.parseDouble(new DecimalFormat("#.##").format(shifted));
    }
    
    /**
     * Compares two dates, used to clear stock data if last stock update date is smaller than current date
     * 
     * @param currentDate
     * @param lastStockUpdateDate
     * @return 
     */
    public boolean compareDate(Date currentDate, Date lastStockUpdateDate) 
    {
        DateFormat df = new SimpleDateFormat("dd-MM-yyyy"); 
        try {
            //System.out.println(" df.parse(currentDate.toString()): " + df.parse(df.format(currentDate)));
            //System.out.println(" df.parse(lastStockUpdateDate.toString()): " + df.parse(df.format(lastStockUpdateDate)));
            if(df.parse(df.format(currentDate)).after(df.parse(df.format(lastStockUpdateDate))))
                return true;
        } catch (ParseException ex) {
            Logger.getLogger(StockUpdateProducer.class.getName()).log(Level.SEVERE, null, ex);
        }
        return false;
    }
    
    /**
     * This method checks if current time is within working hours of Share Market
     * @return 
     */
    public boolean checkIfWorkingHours()
    {
        Time marketStartTime, marketEndTime, curretnTime = new Time(new Date().getTime());
        Calendar calTime = Calendar.getInstance();
        calTime.setTime(new Date());    
        calTime.set(Calendar.HOUR,9);
        calTime.set(Calendar.MINUTE,15);
        calTime.set(Calendar.SECOND,00);
        
        marketStartTime = new Time(calTime.getTime().getTime());
        
        calTime.set(Calendar.HOUR,15);
        calTime.set(Calendar.MINUTE,15);
        marketEndTime = new Time(calTime.getTime().getTime());
        
        System.out.println(" marketStartTime: " + marketStartTime + " marketEndTime: " + marketEndTime + " curretnTime: " + curretnTime);
        
        if(curretnTime.after(marketStartTime) && curretnTime.before(marketEndTime))
            return true;
        return false;
    }
}
