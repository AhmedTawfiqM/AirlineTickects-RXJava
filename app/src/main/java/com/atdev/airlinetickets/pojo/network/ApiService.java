package com.atdev.airlinetickets.pojo.network;

import com.atdev.airlinetickets.pojo.model.Price;
import com.atdev.airlinetickets.pojo.model.Ticket;

import java.util.List;

import io.reactivex.Single;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface ApiService {

    @GET("airline-tickets.php")
    Single<List<Ticket>> searchTickets(@Query("from") String from, @Query("to") String to);

    @GET("airline-tickets-price.php")
    Single<Price> getPrice(@Query("flight_number") String flightNumber, @Query("from") String from, @Query("to") String to);


    //https://api.androidhive.info/json/airline-tickets.php?from=DEL&to=CHE
    //https://api.androidhive.info/json/airline-tickets-price.php?flight_number=6E-ARIfrom=DEL&to=CHE
}


