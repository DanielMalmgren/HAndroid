package se.kolefors.handroid;

import android.view.View;

import java.util.List;

/**
 * Created by damal08 on 2014-01-11.
 */
public class PopulateDeviceFragment extends PopulateFragment{

    public PopulateDeviceFragment(View rootView) {
        super(rootView);
        super.setStartTag("devices");
        super.setItemTag("device");
        super.setApiIdSpecifier("deviceid");
        super.setApiFunctions("devices");
    }

    public void updateObjects(List items) {
        updateOnOffObjects(items);
    }

    public void drawObjects(List items) {
        drawOnOffObjects(items);
    }
}
