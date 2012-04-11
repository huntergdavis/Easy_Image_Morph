package com.hunterdavis.easyimagemorph;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Random;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.ads.AdRequest;
import com.google.ads.AdView;

public class EasyImageMorph extends Activity {

	Uri selectedImageUri1 = null;
	Uri selectedImageUri2 = null;
	public Bitmap scaled1 = null;
	public Bitmap scaled2 = null;
	public Bitmap morphedImage = null;
	public int leftRightPercentage = 50;
	public int algorithm = 0;
	Random myrandom = new Random();

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		// grab a view to the left and right images and load 1 and 2 png
		ImageView imgView1 = (ImageView) findViewById(R.id.ImageView01);
		ImageView imgView2 = (ImageView) findViewById(R.id.ImageView02);
		imgView1.setImageResource(R.drawable.one);
		imgView2.setImageResource(R.drawable.two);

		// photo on click listener
		imgView1.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				Intent intent = new Intent();
				intent.setType("image/*");
				intent.setAction(Intent.ACTION_GET_CONTENT);
				startActivityForResult(
						Intent.createChooser(intent, "Select Picture"), 2);

			}

		});

		// photo on click listener
		imgView2.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				Intent intent = new Intent();
				intent.setType("image/*");
				intent.setAction(Intent.ACTION_GET_CONTENT);
				startActivityForResult(
						Intent.createChooser(intent, "Select Picture"), 3);

			}

		});

		// Create an anonymous implementation of OnClickListener
		OnClickListener saveButtonListner = new OnClickListener() {
			public void onClick(View v) {
				// do something when the button is clicked
				if ((scaled1 != null) && (scaled2 != null)) {
					saveImage(v.getContext());
				} else {
					Toast.makeText(v.getContext(), "Please Select 2 Images ",
							Toast.LENGTH_LONG).show();
				}

			}
		};

		Button saveButton = (Button) findViewById(R.id.saveButton);
		saveButton.setOnClickListener(saveButtonListner);

		// Create an anonymous implementation of OnClickListener
		OnClickListener morphButtonListner = new OnClickListener() {
			public void onClick(View v) {
				// do something when the button is clicked
				if ((scaled1 != null) && (scaled2 != null)) {
					morphImage(v.getContext());
					Button saveButton = (Button) findViewById(R.id.saveButton);
					saveButton.setEnabled(true);
				} else {
					Toast.makeText(v.getContext(), "Please Select 2 Images ",
							Toast.LENGTH_LONG).show();
				}

			}
		};

		Button morphButton = (Button) findViewById(R.id.morphButton);
		morphButton.setOnClickListener(morphButtonListner);

		// implement a seekbarchangelistener for this class
		SeekBar.OnSeekBarChangeListener seekBarChangeListener = new SeekBar.OnSeekBarChangeListener() {

			@Override
			public void onProgressChanged(SeekBar seekBar, int progress,
					boolean fromUser) {

				leftRightPercentage = progress;
			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
				// TODO Auto-generated method stub
			}

			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
				// TODO Auto-generated method stub
			}
		};

		SeekBar onlySeekBar = (SeekBar) findViewById(R.id.leftRightWeight);
		onlySeekBar.setOnSeekBarChangeListener(seekBarChangeListener);

		// set an adapter for our spinner
		ArrayAdapter<String> m_adapterForSpinner = new ArrayAdapter<String>(
				getBaseContext(), android.R.layout.simple_spinner_item);
		m_adapterForSpinner
				.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		Spinner spinner = (Spinner) findViewById(R.id.algorithms);
		spinner.setAdapter(m_adapterForSpinner);
		spinner.setOnItemSelectedListener(new MyUnitsOnItemSelectedListener());

		// fill up our spinner item
		String algorithm = "weighted average";
		m_adapterForSpinner.add(algorithm);
		algorithm = "weighted high logical or";
		m_adapterForSpinner.add(algorithm);
		algorithm = "weighted low logical or";
		m_adapterForSpinner.add(algorithm);
		algorithm = "high logical or";
		m_adapterForSpinner.add(algorithm);
		algorithm = "low logical or";
		m_adapterForSpinner.add(algorithm);
		algorithm = "only where same";
		m_adapterForSpinner.add(algorithm);
		algorithm = "left where different";
		m_adapterForSpinner.add(algorithm);
		algorithm = "right where different";
		m_adapterForSpinner.add(algorithm);
		algorithm = "weighted random or";
		m_adapterForSpinner.add(algorithm);
		algorithm = "weighted random average";
		m_adapterForSpinner.add(algorithm);
		algorithm = "completely random";
		m_adapterForSpinner.add(algorithm);

		// Look up the AdView as a resource and load a request.
		AdView adView = (AdView) this.findViewById(R.id.adView);
		adView.loadAd(new AdRequest());

	} // end of oncreate

	// set up the listener class for spinner
	class MyUnitsOnItemSelectedListener implements OnItemSelectedListener {
		public void onItemSelected(AdapterView<?> parent, View view, int pos,
				long id) {
			// Resources res = getResources();
			algorithm = pos;
		} 

		public void onNothingSelected(AdapterView<?> parent) {
			// Do nothing.
		}
	}

	public Boolean morphImage(Context context) {
		int rgbSize = 300 * 400;
		int[] rgbValues = new int[rgbSize];
		for (int i = 0; i < rgbSize; i++) {
			rgbValues[i] = calculatePixelValue(i, 300, 400);
		}

		morphedImage = Bitmap.createBitmap(rgbValues, 300, 400,
				Bitmap.Config.RGB_565);

		// grab a handle to the image
		ImageView imgPreView = (ImageView) findViewById(R.id.MorphedImageView);
		// Bitmap mymorphedImage = Bitmap.createScaledBitmap(morphedImage, 128,
		// 128, true);
		imgPreView.setImageBitmap(morphedImage);
		return true;
	}

	public Boolean saveImage(Context context) {

		// now save out the file holmes!
		OutputStream outStream = null;
		String newFileName = null;
		String extStorageDirectory = Environment.getExternalStorageDirectory()
				.toString();

		String[] projection = { MediaStore.Images.ImageColumns.DISPLAY_NAME /* col1 */};
		Cursor c = context.getContentResolver().query(selectedImageUri1,
				projection, null, null, null);
		if (c != null && c.moveToFirst()) {
			String oldFileName = c.getString(0);
			int dotpos = oldFileName.lastIndexOf(".");
			if (dotpos > -1) {
				newFileName = oldFileName.substring(0, dotpos) + "-";
			}
		}

		c = context.getContentResolver().query(selectedImageUri2, projection,
				null, null, null);
		if (c != null && c.moveToFirst()) {
			String oldFileName = c.getString(0);
			int dotpos = oldFileName.lastIndexOf(".");
			if (dotpos > -1) {
				newFileName += oldFileName.substring(0, dotpos)
						+ "-morphed.png";
			}
		}

		if (newFileName != null) {
			File file = new File(extStorageDirectory, newFileName);
			try {
				outStream = new FileOutputStream(file);
				morphedImage
						.compress(Bitmap.CompressFormat.PNG, 100, outStream);
				try {
					outStream.flush();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					return false;
				}
				try {
					outStream.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					return false;
				}

				Toast.makeText(context, "Saved " + newFileName,
						Toast.LENGTH_LONG).show();
				new SingleMediaScanner(context, file);

			} catch (FileNotFoundException e) {
				// do something if errors out?
				return false;
			}
		}
		return true;

	}

	public int calculatePixelValue(int location, int width, int height) {
		int pixel1 = scaled1.getPixel((int) location % (width),
				(int) Math.floor(location / (width)));
		int pixel2 = scaled2.getPixel((int) location % (width),
				(int) Math.floor(location / (width)));
		int weightedLeft = (int) (pixel1 * ((100 - leftRightPercentage) * .01));
		int weightedRight = (int) (pixel2 * (leftRightPercentage * .01));

		if (algorithm == 0) {
			return (weightedLeft + weightedRight);
		} else if (algorithm == 1) {
			if (weightedLeft > weightedRight) {
				return pixel1;
			} else {
				return pixel2;
			}
		} else if (algorithm == 2) {
			if (weightedLeft < weightedRight) {
				return pixel1;
			} else {
				return pixel2;
			}
		} else if (algorithm == 3) {
			if (pixel1 > pixel2) {
				return pixel1;
			} else {
				return pixel2;
			}
		} else if (algorithm == 4) {
			if (pixel2 > pixel1) {
				return pixel1;
			} else {
				return pixel2;
			}
		} else if (algorithm == 5) {
			if (pixel1 == pixel2) {
				return pixel1;
			} else {
				return 0;
			}
		} else if (algorithm == 6) {
			if (pixel1 == pixel2) {
				return 0;
			} else {
				return pixel1;
			}
		} else if (algorithm == 7) {
			if (pixel1 == pixel2) {
				return 0;
			} else {
				return pixel2;
			}
		} else if (algorithm == 8) {
			float myrand = myrandom.nextFloat(); // random between 0 and 1

			weightedLeft = (int) (pixel1 * myrand);
			weightedRight = (int) (pixel2 * (1 - myrand));
			if (weightedLeft > weightedRight) {
				return pixel1;
			} else {
				return pixel2;
			}
		} else if (algorithm == 9) {
			float myrand = myrandom.nextFloat(); // random between 0 and 1

			weightedLeft = (int) (pixel1 * myrand);
			weightedRight = (int) (pixel2 * (1 - myrand));
			return (weightedLeft + weightedRight);

		} else {

			// else case is the last case - always completely random
			if (myrandom.nextFloat() > .50) {
				return pixel1;
			} else
				return pixel2;
		}

	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == RESULT_OK) {
			if (requestCode == 2) {
				selectedImageUri1 = data.getData();

				// grab a handle to the image
				ImageView imgPreView = (ImageView) findViewById(R.id.ImageView01);
				scaled1 = scaleURIAndDisplay(getBaseContext(),
						selectedImageUri1, imgPreView);

			} else if (requestCode == 3) {
				selectedImageUri2 = data.getData();

				// grab a handle to the image
				ImageView imgPreView = (ImageView) findViewById(R.id.ImageView02);
				scaled2 = scaleURIAndDisplay(getBaseContext(),
						selectedImageUri2, imgPreView);

			}

		}
	}

	public Bitmap scaleURIAndDisplay(Context context, Uri uri, ImageView imgview) {
		InputStream photoStream;
		try {
			photoStream = context.getContentResolver().openInputStream(uri);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inSampleSize = 2;
		Bitmap photoBitmap;

		photoBitmap = BitmapFactory.decodeStream(photoStream, null, options);
		if (photoBitmap == null) {
			return null;
		}

		Bitmap scaled = Bitmap.createScaledBitmap(photoBitmap, 300, 400, true);
		photoBitmap.recycle();
		// Bitmap scaled2 = Bitmap.createScaledBitmap(scaled, 128, 128, true);
		imgview.setImageBitmap(scaled);
		return scaled;
	}

} // end of file