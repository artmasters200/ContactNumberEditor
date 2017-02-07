package com.baltazarlucas.cne.contactnumbereditor;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.joanzapata.iconify.widget.IconTextView;

import java.util.ArrayList;

public class CustomAdapter extends ArrayAdapter<ContactInfo>
{
    ArrayList<ContactInfo> modelItems = null;
    Context context;

    public CustomAdapter(Context context, ArrayList<ContactInfo> resource)
    {
        super(context,R.layout.row,resource);

        // TODO Auto-generated constructor stub
        this.context = context;
        this.modelItems = resource;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent)
    {
        // TODO Auto-generated method stub
        LayoutInflater inflater = ((Activity)context).getLayoutInflater();
        convertView = inflater.inflate(R.layout.row, parent, false);

        ImageView ivPhoto = (ImageView)convertView.findViewById(R.id.ivPhoto);
        TextView tvLabel = (TextView) convertView.findViewById(R.id.tvLabel);
        TextView tvName = (TextView) convertView.findViewById(R.id.tvName);
        TextView tvLabelNew = (TextView) convertView.findViewById(R.id.tvLabelNew);
        TextView tvNumber = (TextView) convertView.findViewById(R.id.tvNumber);
        CheckBox cb = (CheckBox) convertView.findViewById(R.id.cbName);
        IconTextView itvExchange = (IconTextView) convertView.findViewById(R.id.itvExchange);

        ContactInfo contactInfo = modelItems.get(position);

        cb.setTag(contactInfo);
        cb.setOnClickListener( new View.OnClickListener() {
            public void onClick(View v) {
                CheckBox cb = (CheckBox) v ;
                ContactInfo contactInfo = (ContactInfo) cb.getTag();
                contactInfo.IsSelected = cb.isChecked();
            }
        });

        Resources res = context.getResources();
        RoundedBitmapDrawable dr = RoundedBitmapDrawableFactory.create(res, contactInfo.Photo);
        dr.setCornerRadius( Math.min(dr.getMinimumWidth(), dr.getMinimumHeight()) );
        ivPhoto.setImageDrawable(dr);

        tvName.setText(contactInfo.Name);
        tvNumber.setText("(" + contactInfo.Number + ")");
        tvLabel.setText(contactInfo.Label);

        if (contactInfo.NewLabel != null)
        {
            if (contactInfo.NewLabel.contains("Smart"))
            {
                tvLabelNew.setTextColor(Color.parseColor("#0B984B"));
            }
            if(contactInfo.NewLabel.contains("Globe"))
            {
                tvLabelNew.setTextColor(Color.parseColor("#1278BD"));
            }
            if(contactInfo.NewLabel.contains("Sun"))
            {
                tvLabelNew.setTextColor(Color.parseColor("#FDBC40"));
            }
            itvExchange.setVisibility(View.VISIBLE);
            tvLabelNew.setText(contactInfo.NewLabel);
            cb.setChecked(contactInfo.IsSelected);
        }


        return convertView;
    }
}