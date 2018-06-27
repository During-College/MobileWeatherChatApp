package group6.tcss450.uw.edu.uwtchat;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;


/**
 * This class defines the Fragment that displays the connections menu for selecting where to go.
 * @author Gus
 * @link Fragment
 */
public class ConnectionsFragment extends Fragment {


    public ConnectionsFragment() {
        // Required empty public constructor
    }

    /**
     * Called when creating a ConnectionsFragment. The username needs to be included in the bundle.
     * @param inflater creates the layout from XML file fragment_connections
     * @param container view that holds this fragment.
     * @param savedInstanceState saved previous state.
     * @return A ConnectionsFragment.
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_connections, container, false);

        final Bundle bundle = this.getArguments();

        //Set actions for all the button to open certain Connection fragments
        Button b = v.findViewById(R.id.viewButton);
        b.setOnClickListener(view-> {
            ViewConnectionsFragment x = new ViewConnectionsFragment();
            x.setArguments(bundle);
            FragmentTransaction transaction = getActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.mainFragmentContainer, x, getString(R.string.keys_fragment_view))
                    .addToBackStack(null);
            transaction.commit();
        });

        b = v.findViewById(R.id.searchButton);
        b.setOnClickListener(view-> {
            SearchConnections x = new SearchConnections();
            x.setArguments(bundle);
            FragmentTransaction transaction = getActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.mainFragmentContainer, x, getString(R.string.keys_fragment_search))
                    .addToBackStack(null);
            transaction.commit();
        });

        b = v.findViewById(R.id.sentButton);
        b.setOnClickListener(view-> {
            SentConnections x = new SentConnections();
            x.setArguments(bundle);
            FragmentTransaction transaction = getActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.mainFragmentContainer, x, getString(R.string.keys_fragment_sent))
                    .addToBackStack(null);
            transaction.commit();
        });

        b = v.findViewById(R.id.receivedButton);
        b.setOnClickListener(view-> {
            ReceivedConnections x = new ReceivedConnections();
            x.setArguments(bundle);
            FragmentTransaction transaction = getActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.mainFragmentContainer, x, getString(R.string.keys_fragment_received_connections))
                    .addToBackStack(null);
            transaction.commit();
        });



        b = v.findViewById(R.id.removeButton);
        b.setOnClickListener(view-> {
            RemoveConnectionsFragment x = new RemoveConnectionsFragment();
            x.setArguments(bundle);
            FragmentTransaction transaction = getActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.mainFragmentContainer, x, getString(R.string.keys_fragment_remove))
                    .addToBackStack(null);
            transaction.commit();
        });

        return v;
    }

}
