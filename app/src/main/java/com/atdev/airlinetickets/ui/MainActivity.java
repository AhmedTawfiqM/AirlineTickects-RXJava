package com.atdev.airlinetickets.ui;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.res.Resources;
import android.graphics.Rect;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;

import com.atdev.airlinetickets.R;
import com.atdev.airlinetickets.databinding.ActivityMainBinding;
import com.atdev.airlinetickets.pojo.adapters.TicketAdapter;
import com.atdev.airlinetickets.pojo.model.Price;
import com.atdev.airlinetickets.pojo.model.Ticket;
import com.atdev.airlinetickets.pojo.network.ApiClient;
import com.atdev.airlinetickets.pojo.network.ApiService;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Function;
import io.reactivex.observables.ConnectableObservable;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;

//https://www.androidhive.info/RxJava/flatmap-concatmap-operators-flight-tickets-app/
public class MainActivity extends AppCompatActivity {

    ActivityMainBinding binding;


    private static final String TAG = MainActivity.class.getSimpleName();
    private static final String from = "DEL";
    private static final String to = "HYD";

    private CompositeDisposable disposable = new CompositeDisposable();

    private ApiService apiService;
    private TicketAdapter mAdapter;
    private ArrayList<Ticket> ticketsList = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(from + " > " + to);

        apiService = ApiClient.getClient().create(ApiService.class);

        mAdapter = new TicketAdapter(this, ticketsList);

        RecyclerView.LayoutManager mLayoutManager = new GridLayoutManager(this, 1);
        binding.recyclerView.setLayoutManager(mLayoutManager);
        binding.recyclerView.addItemDecoration(new MainActivity.GridSpacingItemDecoration(1, dpToPx(5), true));
        binding.recyclerView.setItemAnimator(new DefaultItemAnimator());
        binding.recyclerView.setAdapter(mAdapter);

        ConnectableObservable<List<Ticket>> ticketsObservable = getTickets(from, to).replay();

        /**
         * Fetching all tickets first
         * Observable emits List<Ticket> at once
         * All the items will be added to RecyclerView
         * */
        disposable.add(
                ticketsObservable
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribeWith(new DisposableObserver<List<Ticket>>() {

                            @Override
                            public void onNext(List<Ticket> tickets) {
                                // Refreshing list
                                ticketsList.clear();
                                ticketsList.addAll(tickets);
                                mAdapter.notifyDataSetChanged();
                            }

                            @Override
                            public void onError(Throwable e) {
                                showError(e);
                            }

                            @Override
                            public void onComplete() {

                            }
                        }));

        /**
         * Fetching individual ticket price
         * First FlatMap converts single List<Ticket> to multiple emissions
         * Second FlatMap makes HTTP call on each Ticket emission
         * */
        disposable.add(
                ticketsObservable
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        /**
                         * Converting List<Ticket> emission to single Ticket emissions
                         * */
                        .flatMap(new Function<List<Ticket>, ObservableSource<Ticket>>() {
                            @Override
                            public ObservableSource<Ticket> apply(List<Ticket> tickets) throws Exception {
                                return Observable.fromIterable(tickets);
                            }
                        })
                        /**
                         * Fetching price on each Ticket emission
                         * */
                        .flatMap(new Function<Ticket, ObservableSource<Ticket>>() {
                            @Override
                            public ObservableSource<Ticket> apply(Ticket ticket) throws Exception {
                                return getPriceObservable(ticket);
                            }
                        })
                        .subscribeWith(new DisposableObserver<Ticket>() {

                            @Override
                            public void onNext(Ticket ticket) {
                                int position = ticketsList.indexOf(ticket);

                                if (position == -1) {
                                    // TODO - take action
                                    // Ticket not found in the list
                                    // This shouldn't happen
                                    return;
                                }

                                ticketsList.set(position, ticket);
                                mAdapter.notifyItemChanged(position);
                            }

                            @Override
                            public void onError(Throwable e) {
                                showError(e);
                            }

                            @Override
                            public void onComplete() {

                            }
                        }));

        // Calling connect to start emission
        ticketsObservable.connect();
    }

    /**
     * Making Retrofit call to fetch all tickets
     */
    private Observable<List<Ticket>> getTickets(String from, String to) {
        return apiService.searchTickets(from, to)
                .toObservable()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    /**
     * Making Retrofit call to get single ticket price
     * get price HTTP call returns Price object, but
     * map() operator is used to change the return type to Ticket
     */
    private Observable<Ticket> getPriceObservable(final Ticket ticket) {
        return apiService
                .getPrice(ticket.getFlightNumber(), ticket.getFrom(), ticket.getTo())
                .toObservable()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .map(new Function<Price, Ticket>() {
                    @Override
                    public Ticket apply(Price price) throws Exception {
                        ticket.setPrice(price);
                        return ticket;
                    }
                });
    }

    public class GridSpacingItemDecoration extends RecyclerView.ItemDecoration {

        private int spanCount;
        private int spacing;
        private boolean includeEdge;

        public GridSpacingItemDecoration(int spanCount, int spacing, boolean includeEdge) {
            this.spanCount = spanCount;
            this.spacing = spacing;
            this.includeEdge = includeEdge;
        }

        @Override
        public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
            int position = parent.getChildAdapterPosition(view); // item position
            int column = position % spanCount; // item column

            if (includeEdge) {
                outRect.left = spacing - column * spacing / spanCount; // spacing - column * ((1f / spanCount) * spacing)
                outRect.right = (column + 1) * spacing / spanCount; // (column + 1) * ((1f / spanCount) * spacing)

                if (position < spanCount) { // top edge
                    outRect.top = spacing;
                }
                outRect.bottom = spacing; // item bottom
            } else {
                outRect.left = column * spacing / spanCount; // column * ((1f / spanCount) * spacing)
                outRect.right = spacing - (column + 1) * spacing / spanCount; // spacing - (column + 1) * ((1f /    spanCount) * spacing)
                if (position >= spanCount) {
                    outRect.top = spacing; // item top
                }
            }
        }
    }

    private int dpToPx(int dp) {
        Resources r = getResources();
        return Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, r.getDisplayMetrics()));
    }


    /**
     * Snackbar shows observer error
     */
    private void showError(Throwable e) {
        Log.e(TAG, "showError: " + e.getMessage());

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        disposable.dispose();
    }
}
