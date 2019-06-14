package yggdrasil.camerasee.adpater;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.util.Size;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;

import yggdrasil.camerasee.R;

/**
 * Created by dlrud on 2017-05-10.
 */

public class List_size_C0_Adapter extends BaseAdapter {

    // Declare Variables
    Context context;
    LayoutInflater inflater;
    ArrayList<HashMap<String, Size>> data;
    SharedPreferences sharedPref;
    TextView Item_size;
    HashMap<String, Size> resultp = new HashMap<String, Size>();
    int selectedPosition;


    private RadioButton mRadioButton;

    public List_size_C0_Adapter(Context context,
                                ArrayList<HashMap<String, Size>> arraylist, SharedPreferences sharedPref) {
        this.context = context;
        data = arraylist;
        this.sharedPref = sharedPref;
            }

    @Override
    public int getCount() {
        return data.size();
    }

    @Override
    public Object getItem(int position) {
        return data.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public View getView(final int position, View convertView, ViewGroup parent) {
        // Declare Variables


        selectedPosition = sharedPref.getInt("selected_position_size_camera_0",0);

        inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View itemView = inflater.inflate(R.layout.item_size, parent, false);

        // Get the position
        resultp = data.get(position);

        // Locate the TextViews in listview_item.xml
        Item_size = (TextView) itemView.findViewById(R.id.item_size);
        mRadioButton = (RadioButton)itemView.findViewById(R.id.radio_size_camera);
        mRadioButton.setClickable(false);
        mRadioButton.setChecked(position == selectedPosition);
        itemView.setTag(position);

        // Capture position and set results to the TextViews
        Item_size.setText(String.valueOf(resultp.get("Size_camera_0")));


        itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectedPosition = (Integer)view.getTag();
                mRadioButton.setChecked(position == selectedPosition);
                notifyDataSetChanged();


                Size selectedSizecamera0 = data.get(position).get("Size_camera_0");
                Toast.makeText(context,selectedPosition + "   " + String.valueOf(selectedSizecamera0),Toast.LENGTH_SHORT).show();

                SharedPreferences.Editor editor = sharedPref.edit();
                editor.putString("selected_size_camera_0", String.valueOf(selectedSizecamera0)).apply();
                editor.putInt("selected_position_size_camera_0",selectedPosition).apply();
                Log.d("로그 사이즈",String.valueOf(selectedSizecamera0));

            }
        });


        return itemView;
    }


}