package com.rteixeira.traveller.uicontrol;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.rteixeira.traveller.R;
import com.rteixeira.traveller.storage.LocationStorage;
import com.rteixeira.traveller.uicontrol.interfaces.JourneyListListener;

public class JourneyFragment extends Fragment {

    private JourneyListListener mListener;
    private RecyclerView recyclerView;

    public JourneyFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_journey_list, container, false);

        if (view instanceof RecyclerView) {
            Context context = view.getContext();
            recyclerView = (RecyclerView) view;
            recyclerView.setLayoutManager(new LinearLayoutManager(context));
            loadInformation();
        }
        return view;
    }

    public void loadInformation(){
        recyclerView.setAdapter(new SimpleJourneyViewAdapter(LocationStorage.
                getAllSavedJourneys(getActivity()), mListener, getActivity()));
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof JourneyListListener) {
            mListener = (JourneyListListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnListFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }
}
