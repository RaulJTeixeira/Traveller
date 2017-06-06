package com.rteixeira.traveller.uicontrol;

import android.content.Context;
import android.location.Location;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.rteixeira.traveller.R;
import com.rteixeira.traveller.storage.models.DatedGeoPoint;
import com.rteixeira.traveller.storage.models.Journey;
import com.rteixeira.traveller.uicontrol.interfaces.JourneyListListener;

import java.util.List;

public class SimpleJourneyViewAdapter extends RecyclerView.Adapter<SimpleJourneyViewAdapter.ViewHolder> {

    private final List<Journey> mValues;
    private final JourneyListListener mListener;

    private Context mContext;

    public SimpleJourneyViewAdapter(List<Journey> items, JourneyListListener listener, Context ctx) {
        mValues = items;
        mListener = listener;
        mContext = ctx;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_journey, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        holder.mItem = mValues.get(position);
        holder.mDate.setText(DateFormat.format("MM/dd/yyyy",
                mValues.get(position).getGeoPoints().get(0).getDate()).toString());

        long time = mValues.get(position).getGeoPoints().get(mValues.get(position).getGeoPoints().size() - 1).getDate()
                - mValues.get(position).getGeoPoints().get(0).getDate();

        String format;
        if (time / 1000 >= 3600){
            format = mContext.getString(R.string.DATE_FORMAT_H_M_S);
        } else if(time / 1000 < 60 ) {
            format = mContext.getString(R.string.DATE_FORMAT_S);
        } else {
            format = mContext.getString(R.string.DATE_FORMAT_M_S);
        }

        holder.mTime.setText(String.format("Done in %s", DateFormat.format(format, time)));

        float distance = 0;

        for (int i = 0; i  < mValues.get(position).getGeoPoints().size() - 2; i++){

            DatedGeoPoint current = mValues.get(position).getGeoPoints().get(i);
            DatedGeoPoint next = mValues.get(position).getGeoPoints().get(i+1);

            Location loc1 = new Location("");
            loc1.setLatitude(current.getLatitude());
            loc1.setLongitude(current.getLongitude());

            Location loc2 = new Location("");
            loc2.setLatitude(next.getLatitude());
            loc2.setLongitude(next.getLongitude());

            distance += loc1.distanceTo(loc2);
        }

        StringBuffer bf = new StringBuffer();
        if(distance < 1000){
            bf = bf.append(String.format("%.2f", distance));
            bf = bf.append(" m");
        }else {
            bf = bf.append(String.format("%.2f", distance / 1000 ));
            bf = bf.append(" km");
        }
        holder.mDistance.setText(bf.toString());


        holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != mListener) {
                    mListener.onJourneySelected(holder.mItem);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return mValues.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public final View mView;
        public final TextView mDate;
        public final TextView mTime;
        public final TextView mDistance;
        public Journey mItem;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            mDate = (TextView) view.findViewById(R.id.date);
            mTime = (TextView) view.findViewById(R.id.time);
            mDistance = (TextView) view.findViewById(R.id.distance);
        }
    }
}
