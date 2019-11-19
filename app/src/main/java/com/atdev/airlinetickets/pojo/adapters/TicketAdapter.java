package com.atdev.airlinetickets.pojo.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.atdev.airlinetickets.ListenerTicket;
import com.atdev.airlinetickets.R;
import com.atdev.airlinetickets.databinding.TicketRowBinding;
import com.atdev.airlinetickets.pojo.model.Ticket;

import java.util.List;

public class TicketAdapter extends RecyclerView.Adapter<TicketAdapter.MyViewHolder> {

    private Context context;
    private List<Ticket> tickets;

    public TicketAdapter(Context context, List<Ticket> tickets) {
        this.context = context;
        this.tickets = tickets;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        TicketRowBinding binding = DataBindingUtil.inflate(LayoutInflater.from(context), R.layout.ticket_row, parent, false);
        return new MyViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {

        Ticket ticket = tickets.get(position);
        holder.setTicket(ticket);
        //
        //
        //
        holder.getTicketID().setListner(new ListenerTicket() {
            @Override
            public void OnTicketClicked(Ticket ticket) {

                Toast.makeText(context, "Clicked", Toast.LENGTH_SHORT).show();
            }
        });

    }

    @Override
    public int getItemCount() {
        return tickets.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {

        TicketRowBinding ticketRowBinding;

        public MyViewHolder(TicketRowBinding ticketRowBinding) {
            super(ticketRowBinding.getRoot());

            this.ticketRowBinding = ticketRowBinding;
        }

        public void setTicket(Ticket ticket) {
            ticketRowBinding.setTicket(ticket);
        }

        public TicketRowBinding getTicketID() {
            return this.ticketRowBinding;
        }
    }
}
