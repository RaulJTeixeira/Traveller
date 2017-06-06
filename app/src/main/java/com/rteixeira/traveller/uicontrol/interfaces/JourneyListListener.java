package com.rteixeira.traveller.uicontrol.interfaces;


import com.rteixeira.traveller.storage.models.Journey;

/**
 * Samll interface to control selected items from the list to be handled outside of the fragment.
 */
public interface JourneyListListener {
    void onJourneySelected(Journey mItem);
}
