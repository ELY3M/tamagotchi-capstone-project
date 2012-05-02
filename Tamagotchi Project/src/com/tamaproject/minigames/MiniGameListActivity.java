package com.tamaproject.minigames;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.tamaproject.R;
import com.tamaproject.util.MiniGameListConstants;

public class MiniGameListActivity extends ListActivity implements MiniGameListConstants
{
    static final String[] MINIGAMES = new String[] { RACING, TAMANINJA };

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
	super.onCreate(savedInstanceState);

	setListAdapter(new ArrayAdapter<String>(this, R.layout.list, MINIGAMES));

	ListView lv = getListView();
	lv.setTextFilterEnabled(true);

	lv.setOnItemClickListener(new OnItemClickListener()
	{
	    public void onItemClick(AdapterView<?> parent, View view, int position, long id)
	    {
		String s = ((TextView) view).getText().toString();
		if (RACING.equals(s))
		{
		    MiniGameListActivity.this.startActivity(new Intent(MiniGameListActivity.this, MiniGame.class));
		}
		else if (TAMANINJA.equals(s))
		{
		    MiniGameListActivity.this.startActivity(new Intent(MiniGameListActivity.this, TamaNinja.class));
		}
		Toast.makeText(getApplicationContext(), ((TextView) view).getText(), Toast.LENGTH_SHORT).show();
	    }
	});
    }
}
