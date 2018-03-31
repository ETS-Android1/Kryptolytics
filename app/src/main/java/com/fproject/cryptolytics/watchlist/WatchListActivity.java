package com.fproject.cryptolytics.watchlist;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.fproject.cryptolytics.AboutActivity;
import com.fproject.cryptolytics.converter.ConverterActivity;
import com.fproject.cryptolytics.topCoins.TopCoinsActivity;
import com.fproject.cryptolytics.R;
import com.fproject.cryptolytics.cryptoapi.CryptoClient;
import com.fproject.cryptolytics.cryptoapi.CryptoCoin;
import com.fproject.cryptolytics.cryptoapi.CryptoCurrency;
import com.fproject.cryptolytics.cryptoapi.CryptoData;
import com.fproject.cryptolytics.cryptoapi.CryptoCallback;
import com.fproject.cryptolytics.database.DatabaseManager;
import com.fproject.cryptolytics.details.DetailsActivity;
import com.fproject.cryptolytics.searchCoin.SearchCoinActivity;
import com.fproject.cryptolytics.searchCurrency.SearchCurrencyActivity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WatchListActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private final static int SEARCH_COIN_REQUEST      = 1;
    private final static int SEARCH_CURRENCY_REQUEST  = 2;

    // These provide data about the watched items.
    private CryptoClient     cryptoClient   = null;
    private DatabaseManager  databaseManager = null;

    // List of items that are being watched.
    private List<WatchedItem>       watchedItems     = null;

    private Map<String,CryptoCoin>    cryptoCoins      = null;
    private Map<Long,CryptoCurrency>  cryptoCurrencies = null;

    private WatchedItem       newItem         = null;
    private WatchListAdapter watchListAdapter = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_watch_list);
        //
        // Toolbar
        //
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        //
        //
        //
        watchedItems = new ArrayList<>();
        //
        //
        //
        cryptoClient = new CryptoClient(this);
        databaseManager = new DatabaseManager(this);
        //
        //
        //
        setupComponents();
        //
        // Listeners
        //
        setupListeners();
        //
        //
        //
        updateActivity();
    }

    private void setupComponents(){
        RecyclerView recyclerView = findViewById(R.id.rv_watch_list);
        recyclerView.setHasFixedSize(true);
        //
        // LayoutManager
        //
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setAutoMeasureEnabled(false);
        recyclerView.setLayoutManager(layoutManager);
        //
        // ItemDecoration
        //
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(recyclerView.getContext(),
                layoutManager.getOrientation());
        recyclerView.addItemDecoration(dividerItemDecoration);
        //
        // Adapter
        //
        watchListAdapter = new WatchListAdapter(this, watchedItems);
        recyclerView.setAdapter(watchListAdapter);
     }

    /**
     * Hook up the event listeners.
     */
    private void setupListeners(){
        //
        // DrawerLayout
        //
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        //
        // NavigationView
        //
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        //
        // FloatingActionButton
        //
        /*
        FloatingActionButton button = findViewById(R.id.fab);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openSearchCoinsActivity();
            }
        });
        */
        //
        //  OnClickListener
        //
        watchListAdapter.setOnClickListener(new WatchListAdapter.OnClickListener() {
            @Override
            public void onClick(View view, int position) {
                WatchedItem item = watchListAdapter.getWatchedItem(position);
                openWatchedItemDetails(item);
            }
        });
        //
        // SwipeRefreshLayout
        //
        SwipeRefreshLayout swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                setRefreshing(true);
                updateActivity();
            }
        });
        //
        //  ItemTouchHelper
        //
        RecyclerView recyclerView = findViewById(R.id.rv_watch_list);
        ItemTouchHelper helper = createItemTouchHelper();
        helper.attachToRecyclerView(recyclerView);
    }

    /**
     * Create the {@link ItemTouchHelper} that will be attached to the {@link RecyclerView}.
     */
    private ItemTouchHelper createItemTouchHelper(){
        ItemTouchHelper.Callback callback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                // We need this because Android is crap.
                return false;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                WatchedItem item = watchListAdapter.getWatchedItem(viewHolder.getAdapterPosition());
                removeWatchedItem(item);
            }
        };
        return new ItemTouchHelper(callback);
    }

    /**
     * Populate the activity with data.
     */
    private void updateActivity(){
        watchedItems.clear();
        watchedItems.addAll(databaseManager.getWatchListTable().getItems());
        watchListAdapter.notifyDataSetChanged();

        cryptoCoins      = new HashMap<>();
        cryptoCurrencies = new HashMap<>();

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
                onCryptoDataReceived();
            }

            @Override
            public void onFailure(String cryptoError) {
                setRefreshing(false);
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
                    onCryptoDataReceived();
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
    private void onCryptoDataReceived(){
        if ((cryptoCurrencies.size() != watchedItems.size()) || (cryptoCoins.size() == 0))
            return;

        for (WatchedItem item:watchedItems) {
            CryptoCoin cryptoCoin = cryptoCoins.get(item.getFromSymbol());
            item.setCryptoCoin(cryptoCoin);

            CryptoCurrency cryptoCurrency = cryptoCurrencies.get(item.getItemId());
            item.setCryptoCurrency(cryptoCurrency);
        }

        watchListAdapter.notifyDataSetChanged();
        setRefreshing(false);
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


    private void setRefreshing(boolean value){
       SwipeRefreshLayout swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);

       if (value != swipeRefreshLayout.isRefreshing()) {
           swipeRefreshLayout.setRefreshing(value);
       }
    }

    /**
     * Add the item to the WatchList.
     */
    private void addWatchedItem(WatchedItem item){
        long itemId = databaseManager.getWatchListTable().add(item.getFromSymbol(), item.getToSymbol());

        newItem = new WatchedItem(itemId, item.getFromSymbol(), item.getToSymbol());
        watchedItems.add(newItem);
        watchListAdapter.notifyDataSetChanged();

        RecyclerView recyclerView = findViewById(R.id.rv_watch_list);
        recyclerView.smoothScrollToPosition(watchListAdapter.getItemCount() - 1);

        updateActivity();
    }

    /**
     * Remove the item from the WatchList.
     */
    private void removeWatchedItem(WatchedItem item){
        databaseManager.getWatchListTable().remove(item.getItemId());
        watchListAdapter.remove(item);
    }

    private void openWatchedItemDetails(WatchedItem item){
        openDetailsActivity(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.watch_list, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_refresh) {
            openSearchCoinsActivity();
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

            Intent intent = new Intent(this, TopCoinsActivity.class);
            finish();
            startActivity(intent);

        } else if (id == R.id.nav_watchlist) {

            // already there

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

    /**
     * Open Activity where the user can select the FromSymbol.
     */
    public void openSearchCoinsActivity() {
        Intent intent = new Intent(getApplicationContext(), SearchCoinActivity.class);
        startActivityForResult(intent, SEARCH_COIN_REQUEST);
    }

    /**
     * Open Activity where the user can select the ToSymbol.
     */
    public void openSearchCurrencyActivity() {
        Intent intent = new Intent(getApplicationContext(), SearchCurrencyActivity.class);

        intent.putExtra("backButton", false);
        startActivityForResult(intent, SEARCH_CURRENCY_REQUEST);
    }

    /**
     * Open activity with item details.
     */
    public void openDetailsActivity(WatchedItem watchedItem) {

        Intent intent = new Intent(getApplicationContext(), DetailsActivity.class);

        intent.putExtra("fromSymbol",watchedItem.getFromSymbol());
        intent.putExtra("toSymbol",watchedItem.getToSymbol());

        startActivityForResult(intent, 3);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if ((requestCode == SEARCH_COIN_REQUEST) && resultCode == (RESULT_OK)){
            String coinName = data.getStringExtra("CoinName");

            if (coinName != null) {
                newItem = new WatchedItem();
                newItem.setFromSymbol(coinName);

                openSearchCurrencyActivity();
            }
        }

        if ((requestCode == SEARCH_CURRENCY_REQUEST) && resultCode == (RESULT_OK)){
            String currencyName = data.getStringExtra("CurrencyName");

            if (currencyName != null) {
                newItem.setToSymbol(currencyName);

                addWatchedItem(newItem);
            }
        }
    }

}
