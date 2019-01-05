package pl.shajen.octopus.activities;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.method.LinkMovementMethod;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.TextView;

import pl.shajen.octopus.BuildConfig;
import pl.shajen.octopus.R;

public class AboutActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        final TextView textViewVersion = (TextView) findViewById(R.id.textViewVersion);
        textViewVersion.setText(String.format("%s %s", getString(R.string.app_name), BuildConfig.VERSION_NAME));

        final TextView textViewInfo = (TextView) findViewById(R.id.textViewInfo);
        textViewInfo.setMovementMethod(LinkMovementMethod.getInstance());

        final TextView textViewContact = (TextView) findViewById(R.id.textViewContact);
        textViewContact.setMovementMethod(LinkMovementMethod.getInstance());

        final TextView textViewDonate = (TextView) findViewById(R.id.textViewDonate);
        textViewDonate.setMovementMethod(LinkMovementMethod.getInstance());

        final TextView textViewAuthor = (TextView) findViewById(R.id.textViewAuthor);
        textViewAuthor.setMovementMethod(LinkMovementMethod.getInstance());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_device, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.close:
                finishAffinity();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
