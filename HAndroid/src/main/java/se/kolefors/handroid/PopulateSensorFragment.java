package se.kolefors.handroid;

import android.view.View;

import java.util.List;

/**
 * Created by damal08 on 2014-01-11.
 */
public class PopulateSensorFragment extends PopulateFragment{

    public PopulateSensorFragment(View rootView) {
        super(rootView);
        super.setStartTag("sensors");
        super.setItemTag("sensor");
        super.setApiIdSpecifier("id");
        super.setApiFunctions("sensors");
    }

    public void updateObjects(List items) {
        drawNoButtonObjects(items);
    }

    public void drawObjects(List items) {
        drawNoButtonObjects(items);
    }
}
