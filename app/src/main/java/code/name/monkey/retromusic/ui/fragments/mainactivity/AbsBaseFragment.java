package code.name.monkey.retromusic.ui.fragments.mainactivity;

import android.support.v4.app.Fragment;

import code.name.monkey.retromusic.ui.activities.MainActivity;

/**
 * Created by hemanths on 13/08/17.
 */

public class AbsBaseFragment extends Fragment {
    protected MainActivity getMainActivity() {
        return (MainActivity) getActivity();
    }
}
