package com.fproject.cryptolytics.converter;

import com.fproject.cryptolytics.cryptoapi.CryptoCoin;
import com.fproject.cryptolytics.cryptoapi.CryptoRate;

/**
 * Represents an item of the of the Converter.
 */
public class ConverterItem {
    private long itemId;

    private String symbol;
    private String value;

    private CryptoCoin crpytoCoin;
    private CryptoRate cryptoRate;

    public ConverterItem(long itemId, String symbol, String value) {
        this.itemId = itemId;
        this.symbol = symbol;
        this.value = value;
    }

    public long getItemId(){
        return this.itemId;
    }

    public String getSymbol() {
        return symbol;
    }

    public String getValue() {
        return value;
    }

    public String getValueStr(){
        if (value.equals(" - ")) {
            return value;
        }

        return  value + " " + symbol;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public CryptoRate getCryptoRate() {
        return cryptoRate;
    }

    public void setCryptoRate(CryptoRate cryptoRate) {
        this.cryptoRate = cryptoRate;
    }

    public CryptoCoin getCrpytoCoin() {
        return crpytoCoin;
    }

    public void setCrpytoCoin(CryptoCoin crpytoCoin) {
        this.crpytoCoin = crpytoCoin;
    }

    public Double getExRate(){
        if (cryptoRate != null){
            return cryptoRate.getExRate();
        }

        return Double.NaN;
    }

    public boolean isLoaded(){
        if (cryptoRate == null || crpytoCoin == null)
            return false;

        return true;
    }

}
