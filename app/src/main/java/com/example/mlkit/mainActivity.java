package com.example.mlkit;

import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import android.content.ClipboardManager;
import com.example.mlkit.helpers.MyHelper;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.cloud.FirebaseVisionCloudDetectorOptions;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.text.FirebaseVisionCloudTextRecognizerOptions;
import com.google.firebase.ml.vision.text.FirebaseVisionText;
import com.google.firebase.ml.vision.text.FirebaseVisionTextRecognizer;

import java.util.Arrays;

public class mainActivity extends permissionActivity implements View.OnClickListener {
	public Bitmap mBitmap;
	public ImageView mImageView;
	private TextView mTextView = null;
	public Uri dataUri;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		mTextView = findViewById(R.id.text_view);
		mImageView = findViewById(R.id.image_view);
		findViewById(R.id.btn_device).setOnClickListener(this);
		findViewById(R.id.btn_cloud).setOnClickListener(this);
		findViewById(R.id.copyText).setOnClickListener(this);
		mTextView.setText("");
	}

	@Override
	public void onClick(View view) {
		switch (view.getId()) {
			case R.id.btn_device:
				if (mBitmap != null) {
					runTextRecognition();
				}else
				{
					Toast.makeText(mainActivity.this,"Click or Select a picture to detect text",Toast.LENGTH_SHORT).show();
				}
				break;
			case R.id.btn_cloud:
				if (mBitmap != null) {
					runCloudTextRecognition();
				}
				else
				{
					Toast.makeText(mainActivity.this,"Click or Select a picture to detect text",Toast.LENGTH_SHORT).show();
				}
			case R.id.copyText:
				if (mTextView.getText() != "")
				{
					//to copy text to clipboard
					ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
					ClipData clip = ClipData.newPlainText("EditText",mTextView.getText().toString());
					clipboard.setPrimaryClip(clip);
					clip.getDescription();
					Toast.makeText(mainActivity.this,"Copied",Toast.LENGTH_SHORT).show();

				}
				else
				{
					Toast.makeText(mainActivity.this,"No text available to copy",Toast.LENGTH_SHORT).show();
				}
				break;
		}
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode == RESULT_OK) {
			switch (requestCode) {
				case RC_STORAGE_PERMS1:
				case RC_STORAGE_PERMS2:
					checkStoragePermission(requestCode);
					break;
				case RC_SELECT_PICTURE:

					dataUri = data.getData();
					//Uri dataUri = super.resultUri;


					String path = MyHelper.getPath(this, dataUri);
					if (path == null) {
						mBitmap = MyHelper.resizeImage(imageFile, this, dataUri, mImageView);
					} else {
						mBitmap = MyHelper.resizeImage(imageFile, path, mImageView);



					}
					if (mBitmap != null) {
						mTextView.setText(null);
						mImageView.setImageBitmap(mBitmap);
					}
					break;
				case RC_TAKE_PICTURE:
					mBitmap = MyHelper.resizeImage(imageFile, imageFile.getPath(), mImageView);
					if (mBitmap != null) {
						mTextView.setText(null);
						mImageView.setImageBitmap(mBitmap);
					}
					break;
			}
		}
	}

	private void runTextRecognition() {

		FirebaseVisionImage image = FirebaseVisionImage.fromBitmap(mBitmap);
		FirebaseVisionTextRecognizer detector = FirebaseVision.getInstance().getOnDeviceTextRecognizer();
		detector.processImage(image).addOnSuccessListener(new OnSuccessListener<FirebaseVisionText>() {
			@Override
			public void onSuccess(FirebaseVisionText texts) {
				processTextRecognitionResult(texts);
			}
		}).addOnFailureListener(new OnFailureListener() {
			@Override
			public void onFailure(@NonNull Exception e) {
				e.printStackTrace();
			}
		});
	}

	private void runCloudTextRecognition() {
		MyHelper.showDialog(this);

		FirebaseVisionCloudTextRecognizerOptions options = new FirebaseVisionCloudTextRecognizerOptions.Builder()
				.setLanguageHints(Arrays.asList("en", "hi"))
				.setModelType(FirebaseVisionCloudDetectorOptions.LATEST_MODEL)
				.build();

		FirebaseVisionImage image = FirebaseVisionImage.fromBitmap(mBitmap);
		FirebaseVisionTextRecognizer detector = FirebaseVision.getInstance().getCloudTextRecognizer(options);

		detector.processImage(image).addOnSuccessListener(new OnSuccessListener<FirebaseVisionText>() {
			@Override
			public void onSuccess(FirebaseVisionText texts) {
				MyHelper.dismissDialog();
				processTextRecognitionResult(texts);
			}
		}).addOnFailureListener(new OnFailureListener() {
			@Override
			public void onFailure(@NonNull Exception e) {
				MyHelper.dismissDialog();
				e.printStackTrace();
			}
		});
	}

	private void processTextRecognitionResult(FirebaseVisionText firebaseVisionText) {
		mTextView.setText(null);
		if (firebaseVisionText.getTextBlocks().size() == 0) {
			mTextView.setText(R.string.error_not_found);
			return;
		}
		for (FirebaseVisionText.TextBlock block : firebaseVisionText.getTextBlocks()) {

			//In case you want to extract each line

			for (FirebaseVisionText.Line line: block.getLines()) {
				for (FirebaseVisionText.Element element: line.getElements()) {
					mTextView.append(element.getText() + " ");
				}
				mTextView.append("\n");
			}

		}
	}





	/*private void setClipboard(Context context, String text) {
		ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
		ClipData clip = android.content.ClipData.newPlainText("Copied Text", text);
		clipboard.setPrimaryClip(clip);
	}*/
}
