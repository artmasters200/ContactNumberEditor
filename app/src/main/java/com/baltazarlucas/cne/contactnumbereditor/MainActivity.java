package com.baltazarlucas.cne.contactnumbereditor;

import android.app.AlertDialog;
import android.content.ContentProviderOperation;
import android.content.ContentUris;
import android.content.DialogInterface;
import android.content.OperationApplicationException;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.provider.ContactsContract.Contacts;
import android.graphics.Color;
import android.net.Uri;
import android.os.RemoteException;
import android.provider.ContactsContract;
import android.provider.ContactsContract.CommonDataKinds;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.baltazarlucas.cne.contactnumbereditor.helpers.globalHelpers;
import com.joanzapata.iconify.IconDrawable;
import com.joanzapata.iconify.Iconify;
import com.joanzapata.iconify.fonts.FontAwesomeIcons;
import com.joanzapata.iconify.fonts.FontAwesomeModule;


import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity
{
    Cursor cursor;
    ArrayList<ContactInfo> ContactInfoList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        Iconify.with(new FontAwesomeModule());

        setContentView(R.layout.activity_main);
        FloatingActionButton fabButtonChange = (FloatingActionButton) findViewById(R.id.ButtonChange);
        fabButtonChange.setImageDrawable(new IconDrawable(this, FontAwesomeIcons.fa_retweet));
        InitializeData();
        InitializeListView();
        InitializeSubmitButton();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);

        menu.findItem(R.id.options).setIcon(
                new IconDrawable(this, FontAwesomeIcons.fa_ellipsis_v)
                        .colorRes(R.color.colorWhite)
                        .actionBarSize());

        menu.findItem(R.id.refreshItem).setIcon(
                new IconDrawable(this, FontAwesomeIcons.fa_refresh)
                        .colorRes(R.color.colorPrimary)
                        .actionBarSize());

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.refreshItem:
            {
                ContactInfoList.clear();
                cursor = null;

                InitializeData();
                InitializeListView();

                return true;
            }
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void InitializeData()
    {
        if (cursor != null)
        {
            cursor.moveToFirst();
        }
        try
        {
            cursor = getApplicationContext().getContentResolver()
                    .query(CommonDataKinds.Phone.CONTENT_URI, null, null, null, null);
            cursor.moveToFirst();

            do {

                int Idx = cursor.getColumnIndex(CommonDataKinds.Phone.CONTACT_ID);

                String contactId = cursor.getString(Idx);
                {
                    int nameIdx = cursor.getColumnIndex(CommonDataKinds.Phone.DISPLAY_NAME);
                    int phoneNumberIdx = cursor.getColumnIndex(CommonDataKinds.Phone.NUMBER);
                    int phoneTypeIdx = cursor.getColumnIndex(CommonDataKinds.Phone.TYPE);
                    int phoneLabelIdx = cursor.getColumnIndex(CommonDataKinds.Phone.LABEL);
//                    int photoIdIdx = cursor.getColumnIndex(CommonDataKinds.Phone.PHOTO_THUMBNAIL_URI);

                    String name = cursor.getString(nameIdx);
                    String phoneNumber = cursor.getString(phoneNumberIdx);
//                    String phoneType = cursor.getString(phoneTypeIdx);
//                    String image = cursor.getString(photoIdIdx);

                    int phoneTypeId = cursor.getInt(phoneTypeIdx);
                    String phoneLabel = cursor.getString(phoneLabelIdx);


                    String phoneTypeLabel = (String)CommonDataKinds.Phone.getTypeLabel(this.getResources(), phoneTypeId, phoneLabel);

//                    Log.d("App Name", "Id--->"+contactid+"Name--->"+name);

                    if (!phoneNumber.contains("*"))
                    {
                        ContactInfo contactInfo = new ContactInfo();

                        contactInfo.Id = Integer.parseInt(contactId);
                        contactInfo.Name = name;
                        contactInfo.Number = phoneNumber;
                        contactInfo.Label = phoneTypeLabel;
//                        contactInfo.Photo = globalHelpers.loadContactPhoto(getContentResolver(), Long.valueOf(contactId));

                        Uri uri = ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, Long.valueOf(contactId));
                        InputStream inputStream = ContactsContract.Contacts.openContactPhotoInputStream(getContentResolver(), uri);
                        Bitmap bitmap = BitmapFactory.decodeStream(inputStream);

                        if (bitmap == null)
                        {
                            bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.user);
                        }

                        contactInfo.Photo = bitmap;

                        ContactInfoList.add(contactInfo);
                    }
                }

            } while (cursor.moveToNext());


        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        finally
        {
            if (cursor != null)
            {
                cursor.close();
            }
        }

        //check selection
        String stringPrefixes = globalHelpers.loadJSONFromAsset(this, "prefixes");
        try
        {
            JSONObject prefixes = new JSONObject(stringPrefixes);

            for (ContactInfo contactInfo : ContactInfoList)
            {
                if (contactInfo.Number.startsWith("+63") || contactInfo.Number.startsWith("0"))
                {
                    String contactNumber = contactInfo.Number.replace(" ", "");

                    String prefix = ""; //get the contact number prefix +63918 or 0918
                    if (contactNumber.startsWith("+63"))
                    {
                        prefix = contactNumber.substring(3, 6);
                    }
                    if (contactNumber.startsWith("0"))
                    {
                        prefix = contactNumber.substring(1, 4);
                    }

                    try
                    {
                        String carrier = prefixes.getString(prefix);

                        if (carrier != null && carrier != "" && !contactInfo.Label.contains(carrier))
                        {
                            contactInfo.IsSelected = true;
                            contactInfo.NewLabel = String.format("%1$s (%2$s)", contactInfo.Label, carrier);
                        }

                    } catch (JSONException e)
                    {
                        e.printStackTrace();
                    }
                }
            }
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }


    }

    private void InitializeListView()
    {
        CustomAdapter dataAdapter = new CustomAdapter(this, ContactInfoList);
        ListView listView = (ListView) findViewById(R.id.lvAddress);
        listView.setAdapter(dataAdapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id)
            {
                // When clicked, show a toast with the TextView text
                ContactInfo contactInfo = (ContactInfo) parent.getItemAtPosition(position);
                contactInfo.IsSelected = !contactInfo.IsSelected;

                CheckBox cb = (CheckBox) view.findViewById(R.id.cbName);
                cb.setChecked(contactInfo.IsSelected);

//                Toast.makeText(getApplicationContext(), "Clicked:" + contactInfo.Name, Toast.LENGTH_LONG).show();
            }
        });

    }


    private void InitializeSubmitButton()
    {

        FloatingActionButton myButton = (FloatingActionButton) findViewById(R.id.ButtonChange);
        myButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v)
            {
                AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext());
                builder.setMessage("Are you sure you want to continue? You might want to backup your contacts first.")
                        .setCancelable(false)
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {

                                int count = 0;

                                for(int i=0;i<ContactInfoList.size();i++){
                                    ContactInfo contactInfo = ContactInfoList.get(i);
                                    if(contactInfo.IsSelected){

                                        try
                                        {
                                            UpdateContact(Integer.toString(contactInfo.Id), contactInfo.Number, contactInfo.NewLabel);
                                            count++;
                                        } catch (RemoteException e)
                                        {
                                            e.printStackTrace();
                                        } catch (OperationApplicationException e)
                                        {
                                            e.printStackTrace();
                                        }
                                    }
                                }

                                StringBuffer responseText = new StringBuffer();
                                responseText.append("Done processing "  + count + " items");

                                Toast.makeText(getApplicationContext(), responseText, Toast.LENGTH_LONG).show();
                            }
                        })
                        .setNegativeButton("No", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });

                AlertDialog alert = builder.create();
                alert.setTitle("Confirmation");
                alert.show();
            }
        });
    }

    public void UpdateContact (String contactId, String phoneNumber, String newLabel)
            throws RemoteException, OperationApplicationException
    {
        ArrayList<ContentProviderOperation> ops = new ArrayList<>();

        String selectPhone = ContactsContract.Data.CONTACT_ID + "=? AND " +
                ContactsContract.Data.MIMETYPE + "='"  + CommonDataKinds.Phone.CONTENT_ITEM_TYPE + "'" + " AND " +
                CommonDataKinds.Phone.NUMBER + "=?";

        String[] phoneArgs = new String[]{contactId, phoneNumber};

        ops.add(ContentProviderOperation.newUpdate(ContactsContract.Data.CONTENT_URI)
                .withSelection(selectPhone, phoneArgs)
                .withValue(CommonDataKinds.Phone.TYPE, CommonDataKinds.Phone.TYPE_CUSTOM)
                .withValue(CommonDataKinds.Phone.LABEL, newLabel)
                .build());

        this.getContentResolver().applyBatch(ContactsContract.AUTHORITY, ops);
    }
}


