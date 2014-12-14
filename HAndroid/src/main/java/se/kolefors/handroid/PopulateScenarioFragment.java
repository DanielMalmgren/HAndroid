package se.kolefors.handroid;

import android.view.View;

import java.util.List;

/**
 * Created by damal08 on 2014-01-15.
 */
public class PopulateScenarioFragment extends PopulateFragment {

    public PopulateScenarioFragment(View rootView) {
        super(rootView);
        super.setStartTag("scenarios");
        super.setItemTag("scenario");
        super.setApiIdSpecifier("id");
        super.setApiFunctions("scenarios");
    }

    public void updateObjects(List items) {
        drawSingleButtonObjects(items);
    }

    public void drawObjects(List items) {
        drawSingleButtonObjects(items);
    }
}
