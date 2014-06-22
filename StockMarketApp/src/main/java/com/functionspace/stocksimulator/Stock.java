/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.functionspace.stocksimulator;

import java.util.Date;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Entity class which represents a individual Stock with its attributes
 * 
 * @author Chaitanya
 */
public class Stock {
    private String stockName;
    private Double maxPriceLimit, minPriceLimit;
    //This variable stores list of HashMap object which represents JSON format that needs to be generated for rendering charts
    //This will store all stock price updates for all stocks for a given day
    private static ArrayList<HashMap<String,Object>> chartFinalDataJson = new ArrayList<HashMap<String,Object>>();
    private static Date lastStockUpdateDate = null;

    public static Date getLastStockUpdateDate() {
        return lastStockUpdateDate;
    }

    public static void setLastStockUpdateDate(Date lastStockUpdateDate) {
        Stock.lastStockUpdateDate = lastStockUpdateDate;
    }
    
    public static ArrayList getChartFinalDataJson() {
        return chartFinalDataJson;
    }

    public static void addStockUpdateJSONObject(HashMap<String,Object> latestStockUpdateJsonMap) {
        Stock.chartFinalDataJson.add(latestStockUpdateJsonMap);
    }
    
    public static void setChartFinalDataJson(ArrayList chartFinalDataJson) {
        Stock.chartFinalDataJson = chartFinalDataJson;
    }
    
    public Stock(String stockName, Double minPriceLimit, Double maxPriceLimit) {
        this.stockName = stockName;
        this.maxPriceLimit = maxPriceLimit;
        this.minPriceLimit = minPriceLimit;
    }
    
    public Double getMaxPriceLimit() {
        return maxPriceLimit;
    }

    public void setMaxPriceLimit(int maxLimit) {
        this.maxPriceLimit = maxPriceLimit;
    }

    public Double getMinPriceLimit() {
        return minPriceLimit;
    }

    public void setMinPriceLimit(int minLimit) {
        this.minPriceLimit = minPriceLimit;
    }
    
    public String getStockName() {
        return stockName;
    }

    public void setStockName(String stockName) {
        this.stockName = stockName;
    }

}
