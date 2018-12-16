package com.example.wini3.googlemapapiuse.model;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.util.Log;

import com.example.wini3.googlemapapiuse.R;
import com.google.android.gms.location.places.PlaceLikelihood;
import com.google.android.gms.location.places.PlaceLikelihoodBufferResponse;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

@SuppressLint("RestrictedApi")
public class PlacesDialogController {

    // Used for selecting the current place.
    private static final int M_MAX_ENTRIES = 5;
    private String[] mLikelyPlaceNames;
    private String[] mLikelyPlaceAddresses;
    private String[] mLikelyPlaceAttributions;
    private LatLng[] mLikelyPlaceLatLngs;

    private static final int DEFAULT_ZOOM = 15;

    private Context mContext;
    private GoogleMap mMap;

    public PlacesDialogController(Context context, GoogleMap map) {
        mContext = context;
        mMap = map;
    }

    public void openPlacesDialog(Task<PlaceLikelihoodBufferResponse> placeResult) {

        getLikelyPlaces(placeResult);
    }

    private void getLikelyPlaces(Task<PlaceLikelihoodBufferResponse> placeResult) {
        placeResult.addOnCompleteListener
                (new OnCompleteListener<PlaceLikelihoodBufferResponse>() {
                    @Override
                    public void onComplete(@NonNull Task<PlaceLikelihoodBufferResponse> task) {
                        if (task.isSuccessful() && task.getResult() != null) {
                            PlaceLikelihoodBufferResponse likelyPlaces = task.getResult();

                            // Заполнить массив данными
                            parseResult(likelyPlaces);

                            // Release the place likelihood buffer, to avoid memory leaks.
                            likelyPlaces.release();

                            // Отобразить диалоговое оконо
                            createPlacesDialog();
                        } else {
                            Log.e("asd", "Exception: %s", task.getException());
                        }
                    }
                });
    }

    private void parseResult(PlaceLikelihoodBufferResponse likelyPlaces) {
        // Set the count, handling cases where less than 5 entries are returned.
        int count;
        if (likelyPlaces.getCount() < M_MAX_ENTRIES) {
            count = likelyPlaces.getCount();
        } else {
            count = M_MAX_ENTRIES;
        }

        int i = 0;
        mLikelyPlaceNames = new String[count];
        mLikelyPlaceAddresses = new String[count];
        mLikelyPlaceAttributions = new String[count];
        mLikelyPlaceLatLngs = new LatLng[count];

        for (PlaceLikelihood placeLikelihood : likelyPlaces) {
            // Build a list of likely places to show the user.
            mLikelyPlaceNames[i] = (String) placeLikelihood.getPlace().getName();
            mLikelyPlaceAddresses[i] = (String) placeLikelihood.getPlace()
                    .getAddress();
            mLikelyPlaceAttributions[i] = (String) placeLikelihood.getPlace()
                    .getAttributions();
            mLikelyPlaceLatLngs[i] = placeLikelihood.getPlace().getLatLng();

            i++;
            if (i > (count - 1)) {
                break;
            }
        }
    }

    private void createPlacesDialog() {
        // Ask the user to choose the place where they are now.
        DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // The "which" argument contains the position of the selected item.
                LatLng markerLatLng = mLikelyPlaceLatLngs[which];
                String markerSnippet = mLikelyPlaceAddresses[which];
                if (mLikelyPlaceAttributions[which] != null) {
                    markerSnippet = markerSnippet + "\n" + mLikelyPlaceAttributions[which];
                }

                // Add a marker for the selected place, with an info window
                // showing information about that place.
                mMap.addMarker(new MarkerOptions()
                        .title(mLikelyPlaceNames[which])
                        .position(markerLatLng)
                        .snippet(markerSnippet));

                // Position the map's camera at the location of the marker.
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(markerLatLng,
                        DEFAULT_ZOOM));
            }
        };

        // Display the dialog.
        AlertDialog dialog = new AlertDialog.Builder(mContext)
                .setTitle(R.string.option_get_place)
                .setItems(mLikelyPlaceNames, listener)
                .show();
    }

}
