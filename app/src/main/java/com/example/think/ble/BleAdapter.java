package com.example.think.ble;

import android.bluetooth.BluetoothDevice;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

public class BleAdapter extends RecyclerView.Adapter<BleAdapter.ViewHolder> implements View.OnClickListener{
    private ArrayList<String> bluetoothInfoList;
    private OnItemClickListener itemClickListener;
    static class ViewHolder extends RecyclerView.ViewHolder{
        TextView textView;
        public ViewHolder(View view){
            super(view);
            textView = (TextView) view.findViewById(R.id.name);
        }
    }
    public BleAdapter(ArrayList<String> bluetoothInfoList){
        this.bluetoothInfoList = bluetoothInfoList;
    }
    @NonNull
    @Override
    public BleAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_dev,null,false);
        ViewHolder viewHolder = new ViewHolder(view);
        view.setOnClickListener(this);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull BleAdapter.ViewHolder viewHolder, int i) {
        String string = bluetoothInfoList.get(i);
        viewHolder.textView.setText(string);
    }

    @Override
    public int getItemCount() {
        return bluetoothInfoList.size();
    }

    @Override
    public void onClick(View v) {
        if(itemClickListener != null){
            itemClickListener.onItemClick((Integer)v.getTag());
        }
    }
    public interface OnItemClickListener{
        void onItemClick(int position);
    }

    public void setItemClickListener(OnItemClickListener itemClickListener){
        this.itemClickListener = itemClickListener;
    }
}

