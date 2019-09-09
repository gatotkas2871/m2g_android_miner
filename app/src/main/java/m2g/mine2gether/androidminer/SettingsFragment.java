// Copyright (c) 2019, Mine2Gether.com
//
// Please see the included LICENSE file for more information.

package m2g.mine2gether.androidminer;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.Html;
import android.text.TextWatcher;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.AdapterView;
import android.widget.NumberPicker;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class SettingsFragment extends Fragment {

    private static final String LOG_TAG = "MiningSvc";

    private EditText edPass;
    private EditText edUser;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        Button click;
        Button btnFetchm2gid;
        EditText edPool;

        Spinner spPool;
        Spinner spAlgo;

        NumberPicker npCores;
        NumberPicker npThreads;
        NumberPicker npIntensity;

        PoolSpinAdapter poolAdapter;
        AlgoSpinAdapter algoAdapter;

        TextView tvM2gidlink;

        EditText edM2gid;

        View view = inflater.inflate(R.layout.fragment_settings, container, false);
        Context appContext = MainActivity.getContextOfApplication();
        click = view.findViewById(R.id.saveSettings);

        edUser = view.findViewById(R.id.username);
        edPool = view.findViewById(R.id.pool);
        edPass = view.findViewById(R.id.pass);

        spPool = view.findViewById(R.id.poolSpinner);
        spAlgo = view.findViewById(R.id.algoSpinner);

        npCores = view.findViewById(R.id.cores);
        npThreads = view.findViewById(R.id.threads);
        npIntensity = view.findViewById(R.id.intensity);

        tvM2gidlink = view.findViewById(R.id.m2gidlink);
        btnFetchm2gid = view.findViewById(R.id.fetchm2gid);

        edM2gid = view.findViewById(R.id.m2gid);

        poolAdapter = new PoolSpinAdapter(MainActivity.contextOfApplication, R.layout.spinner_text_color, Config.settings.getPools());
        spPool.setAdapter(poolAdapter);

        algoAdapter = new AlgoSpinAdapter(MainActivity.contextOfApplication, R.layout.spinner_text_color, Config.settings.getAlgos());
        spAlgo.setAdapter(algoAdapter);

        tvM2gidlink.setText(Html.fromHtml("<a href=\"https://m2gid.mine2gether.com\">GET AN ID</a>"));
        tvM2gidlink.setMovementMethod(LinkMovementMethod.getInstance());

        int cores = Runtime.getRuntime().availableProcessors();
        // write suggested cores usage into editText
        int suggested = cores / 2;
        if (suggested == 0) suggested = 1;
        ((TextView) view.findViewById(R.id.cpus)).setText(String.format("(%d)", cores));

        npCores.setMinValue(1);
        npCores.setMaxValue(cores);
        npCores.setWrapSelectorWheel(true);

        npThreads.setMinValue(1);
        npThreads.setMaxValue(16);
        npThreads.setWrapSelectorWheel(true);

        npIntensity.setMinValue(1);
        npIntensity.setMaxValue(5);
        npIntensity.setWrapSelectorWheel(true);

        if (PreferenceHelper.getName("cores").equals("") == true) {
            npCores.setValue(suggested);
        } else {
            npCores.setValue(Integer.parseInt(PreferenceHelper.getName("cores")));
        }

        if (PreferenceHelper.getName("threads").equals("") == true) {
            npThreads.setValue(1);
        } else {
            npThreads.setValue(Integer.parseInt(PreferenceHelper.getName("threads")));
        }

        if (PreferenceHelper.getName("intensity").equals("") == true) {
            npIntensity.setValue(1);
        } else {
            npIntensity.setValue(Integer.parseInt(PreferenceHelper.getName("intensity")));
        }



        if (PreferenceHelper.getName("address").equals("") == false) {
            edUser.setText(PreferenceHelper.getName("address"));
        }

        if (PreferenceHelper.getName("pass").equals("") == false) {
            edPass.setText(PreferenceHelper.getName("pass"));
        }

        if (PreferenceHelper.getName("pool").equals("") == false) {
            edPool.setText(PreferenceHelper.getName("pool"));
            int n = poolAdapter.getCount();
            String poolAddress = PreferenceHelper.getName("pool");
            for (int i = 0; i < n; i++) {
                PoolItem itemPool = (PoolItem) poolAdapter.getItem(i);
                if (itemPool.getPool().equals(poolAddress)) {
                    if (itemPool.getAlgo().equals(PreferenceHelper.getName("algo"))) {
                        spPool.setSelection(i);

                    }
                    break;
                }
            }
        }

        if (PreferenceHelper.getName("algo").equals("") == false) {
            int n = algoAdapter.getCount();
            String selectedAlgo = PreferenceHelper.getName("algo");
            for (int i = 0; i < n; i++) {
                String itemAlgo = (String) algoAdapter.getItem(i).getAlgo();
                if (itemAlgo.equals(selectedAlgo)) {
                    spAlgo.setSelection(i);
                    break;
                }
            }
        }

        if (PreferenceHelper.getName("init").equals("1") == false) {
            spPool.setSelection(Config.settings.defaultPoolIndex);
            edUser.setText(Config.settings.defaultWallet);
            edPass.setText(Config.settings.defaultPassword);
        }

        spPool.setOnItemSelectedListener(new OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {

                PoolItem item = poolAdapter.getItem(position);

                if (PreferenceHelper.getName("init").equals("1") == true) {
                    edUser.setText(PreferenceHelper.getName("keyAddress-" + item.getKey()));
                    edPass.setText(PreferenceHelper.getName("keyPassword-" + item.getKey()));
                }

                if (position == 0) return;

                edPool.setText(item.getPool());

                int n = algoAdapter.getCount();
                String selectedCoinAlgo = item.getAlgo();

                for (int i = 0; i < n; i++) {
                    String s = (String) algoAdapter.getItem(i).getAlgo();
                    if (selectedCoinAlgo.equals(s)) {
                        spAlgo.setSelection(i);
                        break;
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapter) {
            }
        });

        spAlgo.setOnItemSelectedListener(new OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
                String poolAddress = edPool.getText().toString();
                int n = poolAdapter.getCount();

                    for (int i = 0; i < n; i++) {
                        PoolItem itemPool = (PoolItem) poolAdapter.getItem(i);
                        if (itemPool.getPool().equals(poolAddress)) {
                            if (itemPool.getAlgo().equals(algoAdapter.getItem(spAlgo.getSelectedItemPosition()).getAlgo())) {
                                spPool.setSelection(i);
                                return;
                            }
                            break;
                        }
                    }
                    spPool.setSelection(0);

            }

            @Override
            public void onNothingSelected(AdapterView<?> adapter) {
            }
        });



        btnFetchm2gid.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (edM2gid.getText().toString().equals("") == false) {
                    new fetchM2Gid().execute("https://m2gid.mine2gether.com/api/fetch/" + edM2gid.getText());
                }
            }
        });

        click.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                PreferenceHelper.setName("address", edUser.getText().toString().trim());
                PreferenceHelper.setName("pool", edPool.getText().toString().trim());
                PreferenceHelper.setName("pass", edPass.getText().toString().trim());

                AlgoItem selectedAlgoItem = (AlgoItem) spAlgo.getSelectedItem();
                PreferenceHelper.setName("algo", selectedAlgoItem.getAlgo());
                PreferenceHelper.setName("assetExtension", selectedAlgoItem.getAssetExtension());

                PoolItem selectedPoolItem = (PoolItem) spPool.getSelectedItem();
                PreferenceHelper.setName("apiUrl", selectedPoolItem.getApiUrl());
                PreferenceHelper.setName("apiUrlMerged", selectedPoolItem.getApiUrlMerged());
                PreferenceHelper.setName("poolUrl", selectedPoolItem.getPoolUrl());
                PreferenceHelper.setName("statsUrl", selectedPoolItem.getStatsURL());
                PreferenceHelper.setName("startUrl", selectedPoolItem.getStartUrl());

                PreferenceHelper.setName("coin", selectedPoolItem.getCoin());

                PreferenceHelper.setName("keyAddress-" + selectedPoolItem.getKey(), edUser.getText().toString().trim());
                PreferenceHelper.setName("keyPassword-" + selectedPoolItem.getKey(), edPass.getText().toString().trim());

                PreferenceHelper.setName("cores", Integer.toString(npCores.getValue()));
                PreferenceHelper.setName("threads", Integer.toString(npThreads.getValue()));
                PreferenceHelper.setName("intensity", Integer.toString(npIntensity.getValue()));

                PreferenceHelper.setName("init", "1");

                Toast.makeText(appContext, "Settings Saved", Toast.LENGTH_SHORT).show();
            }
        });

        edPool.addTextChangedListener(new TextWatcher() {

            @Override
            public void afterTextChanged(Editable s) {
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String poolAddress = edPool.getText().toString();
                int n = poolAdapter.getCount();
                if (s.length() != 0) {
                    for (int i = 0; i < n; i++) {
                        PoolItem itemPool = (PoolItem) poolAdapter.getItem(i);
                        if (itemPool.getPool().equals(poolAddress)) {
                            if (itemPool.getAlgo().equals(algoAdapter.getItem(spAlgo.getSelectedItemPosition()).getAlgo())) {
                                spPool.setSelection(i);
                                return;
                            }
                            break;
                        }
                    }
                    spPool.setSelection(0);
                }
            }
        });

        return view;
    }

    public class PoolSpinAdapter extends ArrayAdapter<PoolItem> {

        private Context context;
        private PoolItem[] values;

        public PoolSpinAdapter(Context c, int textViewResourceId, PoolItem[] values) {
            super(c, textViewResourceId, values);
            this.context = c;
            this.values = values;
        }

        @Override
        public int getCount() {
            return values.length;
        }

        @Override
        public PoolItem getItem(int position) {
            return values[position];
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            TextView label = (TextView) super.getView(position, convertView, parent);
            label.setText(values[position].getCoin());
            return label;
        }

        @Override
        public View getDropDownView(int position, View convertView, ViewGroup parent) {
            TextView label = (TextView) super.getDropDownView(position, convertView, parent);
            label.setText(values[position].getCoin());
            label.setPadding(5, 10, 5, 10);
            return label;
        }
    }

    public class AlgoSpinAdapter extends ArrayAdapter<AlgoItem> {

        private Context context;
        private AlgoItem[] values;

        public AlgoSpinAdapter(Context c, int textViewResourceId, AlgoItem[] values) {
            super(c, textViewResourceId, values);
            this.context = c;
            this.values = values;
        }

        @Override
        public int getCount() {
            return values.length;
        }

        @Override
        public AlgoItem getItem(int position) {
            return values[position];
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            TextView label = (TextView) super.getView(position, convertView, parent);
            label.setText(values[position].getAlgo());
            return label;
        }

        @Override
        public View getDropDownView(int position, View convertView, ViewGroup parent) {
            TextView label = (TextView) super.getDropDownView(position, convertView, parent);
            label.setText(values[position].getAlgo());
            label.setPadding(5, 10, 5, 10);
            return label;
        }
    }

    private void selectSpinnerValue(Spinner spinner, String value) {
        for (int i = 0; i < spinner.getCount(); i++) {
            if (spinner.getItemAtPosition(i).toString().equals(value)) {
                spinner.setSelection(i);
                break;
            }
        }
    }

    public class fetchM2Gid extends AsyncTask<String, Void, String> {

        int Error = 0;

        @Override
        protected String doInBackground(String... url) {
            String data = "";
            try {

                URL urlFetch = new URL(url[0]);
                HttpURLConnection httpURLConnection = (HttpURLConnection) urlFetch.openConnection();
                InputStream inputStream = httpURLConnection.getInputStream();
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
                String line = "";
                while ((line = bufferedReader.readLine()) != null) {
                    data = data + line;
                }

            } catch (MalformedURLException e) {
                Error = 1;
                Log.i(LOG_TAG, e.toString());
                e.printStackTrace();
            } catch (IOException e) {
                Error = 1;
                Log.i(LOG_TAG, e.toString());
                e.printStackTrace();
            } catch (Exception e) {
                Error = 1;
                Log.i(LOG_TAG, e.toString());
            }

            return data;
        }

        @Override
        protected void onPostExecute(String result) {

            try {
                if (Error != 0) {
                    Toast.makeText(MainActivity.getContextOfApplication(), "Error fetching M2G Id", Toast.LENGTH_SHORT).show();
                    return;
                }
                JSONObject joM2Gid = new JSONObject(result);
                String sError = joM2Gid.optString("error");
                if (sError.equals("")) {
                    String sWallet = joM2Gid.optString("data1");
                    String sPassword = joM2Gid.optString("data2");
                    edUser.setText(sWallet);
                    edPass.setText(sPassword);
                } else {
                    Toast.makeText(MainActivity.getContextOfApplication(), "Error: " + sError, Toast.LENGTH_SHORT).show();
                }
            } catch (JSONException e) {
                Log.i(LOG_TAG, e.toString());
                e.printStackTrace();
            } catch (Exception e) {
                Log.i(LOG_TAG, e.toString());
            }
        }

    }
}