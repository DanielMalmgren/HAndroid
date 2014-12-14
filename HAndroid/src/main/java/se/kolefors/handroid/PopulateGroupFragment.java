package se.kolefors.handroid;

import android.view.View;

import java.util.List;

/**
 * Created by damal08 on 2014-01-11.
 */
public class PopulateGroupFragment extends PopulateFragment{

    public PopulateGroupFragment(View rootView) {
        super(rootView);
        super.setStartTag("groups");
        super.setItemTag("group");
        super.setApiFunctions("groups");
        super.setApiIdSpecifier("groupid");
    }

    public void updateObjects(List items) {
        updateOnOffObjects(items);
    }

    public void drawObjects(List items) {
        drawOnOffObjects(items);
    }
}
