package com.fproject.cryptolytics.topCoins;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.ListView;

import com.fproject.cryptolytics.AboutActivity;
import com.fproject.cryptolytics.R;
import com.fproject.cryptolytics.converter.ConverterActivity;
import com.fproject.cryptolytics.cryptoapi.CryptoCallback;
import com.fproject.cryptolytics.cryptoapi.CryptoClient;
import com.fproject.cryptolytics.cryptoapi.CryptoCoin;
import com.fproject.cryptolytics.cryptoapi.CryptoCurrency;
import com.fproject.cryptolytics.cryptoapi.CryptoData;
import com.fproject.cryptolytics.database.DatabaseManager;
import com.fproject.cryptolytics.watchlist.WatchListActivity;
import com.fproject.cryptolytics.watchlist.WatchListAdapter;
import com.fproject.cryptolytics.watchlist.WatchedItem;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TopCoinsActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    // These provide data about the watched items.
    private CryptoClient cryptoClient   = null;


    // List of items that are being watched.
    private List<WatchedItem> watchedItems     = null;

    private Map<String,CryptoCoin> cryptoCoins      = null;
    private Map<Long,CryptoCurrency>  cryptoCurrencies = null;

    private WatchListAdapter watchListAdapter = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_top_coins);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        //
        // Component
        //
        cryptoClient = new CryptoClient(this);
        //
        // Listeners
        //
        setupListeners();
    }

    /**
     * Hook up the event listeners.
     */
    private void setupListeners() {
        //
        // DrawerLayout
        //
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        //
        // Navigation View
        //
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
    }

    /**
     * Populate the activity with data.
     */
    private void updateActivity(){
        if (watchedItems == null) {
            watchListAdapter = new WatchListAdapter(this, watchedItems);

            ListView listView = (ListView) findViewById(R.id.lv_watchlist);
            listView.setAdapter(watchListAdapter);
        }

        cryptoCoins      = new HashMap<String,CryptoCoin>();
        cryptoCurrencies = new HashMap<Long,CryptoCurrency>();

        getCryptoCoinsCallback();
        getCryptoCurrenciesCallback();
    }

    /**
     * Obtain the {@link CryptoCoin} data.
     */
    private void getCryptoCoinsCallback(){
        cryptoClient.getCryptoCoins(new CryptoCallback() {
            @Override
            public void onSuccess(CryptoData cryptoData) {
                cryptoCoins = cryptoData.getAsCryptoCoins();
                updateActivityData();
            }

            @Override
            public void onFailure(String cryptoError) {

            }
        });
    }

    /**
     * Obtain the {@link CryptoCurrency} data.
     */
    private void getCryptoCurrenciesCallback() {
        for(WatchedItem item:watchedItems) {
            cryptoClient.getCrytpoCurrency(item.getFromSymbol(), item.getToSymbol(), new CryptoCallback() {
                @Override
                public void onSuccess(CryptoData cryptoData) {
                    cryptoCurrencies.put(item.getItemId(), cryptoData.getAsCryptoCurrency());
                    updateActivityData();
                }

                @Override
                public void onFailure(String cryptoError) {

                }
            });
        }
    }

    /**
     * Determines weather all the requested data has arrived and updates the WatchList.
     */
    private void updateActivityData(){
        if ((cryptoCurrencies.size() != watchedItems.size()) || (cryptoCoins.size() == 0))
            return;

        for (WatchedItem item:watchedItems) {

            CryptoCoin cryptoCoin = cryptoCoins.get(item.getFromSymbol());
            item.setCryptoCoin(cryptoCoin);

            CryptoCurrency cryptoCurrency = cryptoCurrencies.get(item.getItemId());
            item.setCryptoCurrency(cryptoCurrency);
        }

        watchListAdapter.notifyDataSetChanged();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.home, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_home) {

            // already there

        } else if (id == R.id.nav_watchlist) {

            Intent intent = new Intent(this, WatchListActivity.class);
            finish();
            startActivity(intent);

        } else if (id == R.id.nav_converter) {

            Intent intent = new Intent(this, ConverterActivity.class);
            finish();
            startActivity(intent);

        } else if (id == R.id.nav_about) {

            Intent intent = new Intent(this, AboutActivity.class);
            finish();
            startActivity(intent);
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
