package d.hitthatcolorproversion;

import android.app.Application;

import com.google.android.gms.common.api.GoogleApiClient;

public class GlobalSlate extends Application {
    private GoogleApiClient mGoogleApiClient;

    public GoogleApiClient getGoogleApiClient() {
        return mGoogleApiClient;
    }

    public void setGoogleApiClient(GoogleApiClient mGoogleApiClient) {
        this.mGoogleApiClient = mGoogleApiClient;
    }
}

