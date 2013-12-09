package jp.joao.nirai.sample;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import nirai.Job;
import nirai.JobService;

public class MainActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new PlaceholderFragment())
                    .commit();
        }
    }

    public static class PlaceholderFragment extends Fragment implements View.OnClickListener {

        public PlaceholderFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_main, container, false);
            rootView.findViewById(R.id.button_simple_job).setOnClickListener(this);
            rootView.findViewById(R.id.button_network_job).setOnClickListener(this);
            return rootView;
        }

        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.button_simple_job: {
                    Job job = new Job(SimpleJobRunner.class);
                    JobService.post(getActivity(), job);
                    break;
                }
                case R.id.button_network_job:
                    Job job = new Job(NetworkJobRunner.class);
                    JobService.post(getActivity(), job);
                    break;
            }
        }
    }

}
