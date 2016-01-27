package lituchiy.max.trader;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

import static android.view.Gravity.RIGHT;

public class PictureActivity extends Activity implements View.OnClickListener {

    static final int REQUEST_TAKE_PHOTO = 2;
    static final int REQUEST_TAKE_PHOTO_FOR_BACKGROUND = 3;
    final int RQS_IMAGE1 = 1;
    private String mCurrentPhotoPath;
    public String photoImagePath;
    private String backgroundImagePath;
    Uri source1, source2;

    protected AppUtils appUtils;
    protected ImageButton backgroundChangeButton;
    protected ImageButton takePhotoButton;
    protected ImageButton okButton;
    protected ImageButton makeBackgroundPhotoButton;
    protected ImageView image;
    protected LinearLayout rightActionBar;
    protected FrameLayout layoutForPhoto;
    protected FrameLayout layoutForText;
    public Bitmap commonBitmap = null;
    Bitmap backgroundBitmap = null;
    Bitmap bitmapPhoto = null;
    protected EditText editTextBrand;
    protected EditText editTextSize;
    protected EditText editTextState;
    protected EditText editTextPrice;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        appUtils = new AppUtils(this);
        setContentView(R.layout.activity_picture);

        rightActionBar         = (LinearLayout) findViewById(R.id.rightActionBar);
        image                  = (ImageView) findViewById(R.id.image);
        layoutForPhoto         = (FrameLayout) findViewById(R.id.layoutForPhoto);
        layoutForText          = (FrameLayout) findViewById(R.id.layoutForText);
        editTextBrand          = (EditText) findViewById(R.id.editTextBrand);
        editTextSize           = (EditText) findViewById(R.id.editTextSize);
        editTextState          = (EditText) findViewById(R.id.editTextState);
        editTextPrice          = (EditText) findViewById(R.id.editTextPrice);

//        Bitmap photoBtnBitmap = null;
//        Bitmap backgroundBthBitmap = null;
//        photoBtnBitmap = BitmapFactory.decodeStream(getAssets().open("ic_camera_alt_white_18dp.png"));
//        takePhotoButton.setImageBitmap(photoBtnBitmap);
//        backgroundBthBitmap = BitmapFactory.decodeStream(getAssets().open("ic_crop_original_white_18dp.png"));
        addListenerOnImageButton();

//        FrameLayout.LayoutParams layParamsGet = (FrameLayout.LayoutParams) rightActionBar.getLayoutParams();
//        int rightActionBarWidth = layParamsGet.width;
//        rightActionBarWidth = (int) convertPixelsToDp(layParamsGet.width, getBaseContext());
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int width = size.x;
        int height = size.y;

        FrameLayout.LayoutParams lpText = new FrameLayout.LayoutParams(width / 2, height);
        FrameLayout.LayoutParams lpPhoto = new FrameLayout.LayoutParams(width / 2, height);
        lpPhoto.gravity = Gravity.LEFT;
        lpText.gravity = Gravity.RIGHT;
        layoutForText.setLayoutParams(lpText);
        //layoutForText.setBackgroundColor(Color.parseColor("#FFFF1A00"));
        layoutForPhoto.setLayoutParams(lpPhoto);
        try {
            backgroundBitmap = BitmapFactory.decodeStream(getAssets().open("background_1.jpg"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        image.setImageBitmap(backgroundBitmap);
        commonBitmap = backgroundBitmap;

        editTextBrand.setTextSize(getTextSizeAndIndent()[1] / 4);
        editTextSize.setTextSize(getTextSizeAndIndent()[1] / 4);
        editTextState.setTextSize(getTextSizeAndIndent()[1] / 4);
        editTextPrice.setTextSize(getTextSizeAndIndent()[1] / 4);

        ViewGroup group = (ViewGroup) findViewById(R.id.rightActionBar);
        View v;
        for (int i = 0; i < group.getChildCount(); i++) {
            v = group.getChildAt(i);
            if (v instanceof Button)
                v.setOnClickListener(this);
        }
        rightActionBar.setLayoutParams(new FrameLayout.LayoutParams(
                appUtils.getNavigationBarHeight(Configuration.ORIENTATION_LANDSCAPE),
                FrameLayout.LayoutParams.MATCH_PARENT, RIGHT));
    }

    @Override
    protected void onPause() {
        super.onPause();
        hindBottomNavigationBar();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();

        if (itemId == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        hindBottomNavigationBar();

    }

    private void hindBottomNavigationBar() {
        if (Build.VERSION.SDK_INT < 19) {
            View v = this.getWindow().getDecorView();
            v.setSystemUiVisibility(View.GONE);
        } else {
            View decorView = getWindow().getDecorView();
            int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
            decorView.setSystemUiVisibility(uiOptions);
        }
    }

    private void dispatchTakePictureIntent(int request) {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                // Error occurred while creating the File
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(photoFile));
                startActivityForResult(takePictureIntent, request);
            }
        }
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat(getString(R.string.Date)).format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        File image = null;
        image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        mCurrentPhotoPath = "file:" + image.getAbsolutePath();
        photoImagePath = image.getAbsolutePath();


        return image;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != RESULT_OK)
            return;
        switch (requestCode) {
            case RQS_IMAGE1:
                source1 = data.getData();
                try {
                    backgroundBitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), source1);
                    commonBitmap = backgroundBitmap;
                } catch (IOException e) {
                    e.printStackTrace();
                }
                //image.setImageURI(source1);
                image.setImageBitmap(commonBitmap);
                break;
            case REQUEST_TAKE_PHOTO:
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inPreferredConfig = Bitmap.Config.ARGB_8888;
                bitmapPhoto = BitmapFactory.decodeFile(photoImagePath, options);

                source2 = Uri.parse(photoImagePath);
                Bitmap b = ProcessingBitmap();
                image.setImageBitmap(b);
                break;
            case REQUEST_TAKE_PHOTO_FOR_BACKGROUND:
                BitmapFactory.Options options2 = new BitmapFactory.Options();
                options2.inPreferredConfig = Bitmap.Config.ARGB_8888;
                backgroundBitmap = BitmapFactory.decodeFile(photoImagePath, options2);
                commonBitmap = backgroundBitmap;
                if (bitmapPhoto != null) {
                    commonBitmap = ProcessingBitmap();
                }
                image.setImageBitmap(commonBitmap);
                break;
            default:
                return;
        }


    }

    private void addListenerOnImageButton() {
        backgroundChangeButton    = (ImageButton) findViewById(R.id.background_change_button);
        makeBackgroundPhotoButton = (ImageButton) findViewById(R.id.make_background_photo_button);
        takePhotoButton           = (ImageButton) findViewById(R.id.make_photo_button);
        okButton                  = (ImageButton) findViewById(R.id.OK);
        backgroundChangeButton.setImageResource(R.drawable.ic_crop_original_white_18dp);
        takePhotoButton.setImageResource(R.drawable.ic_camera_alt_white_18dp);
        okButton.setImageResource(R.drawable.ic_done_white_24dp);
        makeBackgroundPhotoButton.setImageResource(R.drawable.ic_image_aspect_ratio_white_36dp);
        backgroundChangeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent, RQS_IMAGE1);
            }
        });
        makeBackgroundPhotoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dispatchTakePictureIntent(REQUEST_TAKE_PHOTO_FOR_BACKGROUND);
            }
        });
        takePhotoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dispatchTakePictureIntent(REQUEST_TAKE_PHOTO);
            }
        });
        okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int textIndent = (int) getTextSizeAndIndent()[1];
                Log.d("DEBUG","textIndent: "+textIndent);
                if (editTextSize.getText().toString() != "") {
                    image.setImageBitmap(processingTextOnBitmap(editTextSize.getText().toString(), textIndent));
                }
                textIndent += (int) getTextSizeAndIndent()[1];
                if (editTextBrand.getText().toString() != "") {
                    image.setImageBitmap(processingTextOnBitmap(editTextBrand.getText().toString(), textIndent));
                }
                textIndent += (int) getTextSizeAndIndent()[1];
                if (editTextState.getText().toString() != "") {
                    image.setImageBitmap(processingTextOnBitmap(editTextState.getText().toString(), textIndent));
                }
                textIndent += (int) getTextSizeAndIndent()[1];
                if (editTextPrice.getText().toString() != "") {
                    image.setImageBitmap(processingTextOnBitmap(editTextPrice.getText().toString(), textIndent));
                }

                Intent intent2 = new Intent(getApplicationContext(), MainActivity.class);
                intent2.putExtra("picturePath", savePhotoToGalery().toString());
                startActivity(intent2);
            }
        });
    }

    public Bitmap ProcessingBitmap() {
        Bitmap secondBackgroundBitmap = null;
        Bitmap secondPhotoBitmap = null;
        Bitmap bm22 = null;
        Bitmap newBitmap = null;

        secondBackgroundBitmap = backgroundBitmap;
        //secondPhotoBitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(source2));
        secondPhotoBitmap = bitmapPhoto;
        int height = secondBackgroundBitmap.getHeight();
        int width = secondBackgroundBitmap.getWidth();
        int newBimapWidth = 0;
        int newBitmapHeight = 0;
        int alignTop = 0; //The position of the top side of the commonBitmap being drawn
        if (secondPhotoBitmap.getHeight() < secondPhotoBitmap.getWidth()) {
            Log.d("Debug", "secondBackgroundBitmap.getHeight(): " + height + " secondBackgroundBitmap.getWidth(): " + width);
            Log.d("Debug", "secondPhotoBitmap.getHeight(): " + secondPhotoBitmap.getHeight() + " secondPhotoBitmap.getWidth(): " + secondPhotoBitmap.getWidth());
            int newHeight = secondPhotoBitmap.getHeight() / (secondPhotoBitmap.getWidth() / (width / 2)); //
            Log.d("Debug", "newHeight = height )" + newHeight + "(width / (width / 2): " + (width / (width / 2) + "(width / 2): " + (width / 2)));
            bm22 = getResizedBitmap(secondPhotoBitmap, width / 2, newHeight);
            secondPhotoBitmap = null;
            secondPhotoBitmap = bm22;
            Log.d("Debug", "secondPhotoBitmap.getHeight(): " + secondPhotoBitmap.getHeight() + " secondPhotoBitmap.getWidth(): " + secondPhotoBitmap.getWidth());
            alignTop = (height - secondPhotoBitmap.getHeight()) / 2;


        } else {
            int newHeight = 0;
            int newWidth = width / 2;
            newHeight = (int) (secondPhotoBitmap.getHeight() / (secondPhotoBitmap.getWidth() / (newWidth / 1.1)));
            bm22 = getResizedBitmap(secondPhotoBitmap, newWidth, newHeight);
            secondPhotoBitmap = null;
            secondPhotoBitmap = bm22;
            alignTop = (height - secondPhotoBitmap.getHeight()) / 2;
        }

        if (secondBackgroundBitmap.getWidth() >= secondPhotoBitmap.getWidth()) {
            newBimapWidth = secondBackgroundBitmap.getWidth();
        } else {
            newBimapWidth = secondPhotoBitmap.getWidth();
        }

        if (secondBackgroundBitmap.getHeight() >= secondPhotoBitmap.getHeight()) {
            newBitmapHeight = secondBackgroundBitmap.getHeight();
        } else {
            newBitmapHeight = secondPhotoBitmap.getHeight();
        }

        Bitmap.Config config = secondBackgroundBitmap.getConfig();
        if (config == null) {
            config = Bitmap.Config.ARGB_8888;
        }

        newBitmap = Bitmap.createBitmap(newBimapWidth, newBitmapHeight, config);
        Canvas newCanvas = new Canvas(newBitmap);

        newCanvas.drawBitmap(secondBackgroundBitmap, 0, 0, null);

        Paint paint = new Paint();
        // paint.setShadowLayer(0.0f, 200.0f, 100.0f, 0xFF000000);
        newCanvas.drawBitmap(secondPhotoBitmap, 0, alignTop, paint);
        commonBitmap = newBitmap;
        return commonBitmap;
    }

    public Bitmap getResizedBitmap(Bitmap bm, int newWidth, int newHeight) {
        int width = bm.getWidth();
        int height = bm.getHeight();
        float scaleWidth = ((float) newWidth) / width;
        float scaleHeight = ((float) newHeight) / height;
        // CREATE A MATRIX FOR THE MANIPULATION
        Matrix matrix = new Matrix();
        // RESIZE THE BIT MAP
        matrix.postScale(scaleWidth, scaleHeight);

        return Bitmap.createBitmap(bm, 0, 0, width, height, matrix, false);
    }

    private String savePhotoToGalery() {
        String path = Environment.getExternalStorageDirectory().toString();
        Log.d("DEBUG","path: "+path );
        OutputStream fOut = null;
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss");
        String currentDateandTime = sdf.format(new Date());
        File file = new File(path, currentDateandTime + ".jpg"); // the File to save to
        Log.d("DEBUG", "Picture saved to1 " + file.getAbsolutePath());
        Uri imageUri = null;
        try {
            fOut = new FileOutputStream(file);
            if (commonBitmap != null) {
                Bitmap pictureBitmap = commonBitmap; // obtaining the Bitmap
                pictureBitmap.compress(Bitmap.CompressFormat.JPEG, 100, fOut);
                fOut.flush();
                fOut.close(); // do not forget to close the stream
//                MediaStore.Images.Media.insertImage(getContentResolver(), file.getAbsolutePath(), file.getName(), file.getName());
                imageUri = addImageToGallery(getApplicationContext(), file.getAbsolutePath(), file.getName(), file.getName());
                Log.d("DEBUG", "Picture saved to2 " + file.getAbsolutePath());

            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Log.d("DEBUG", "imageUri: " + String.valueOf(imageUri));
        //return file.getAbsolutePath();
        return String.valueOf(imageUri);

    }

    public Uri addImageToGallery(Context context, String filepath, String title, String description) {

        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.TITLE, title);
        values.put(MediaStore.Images.Media.DESCRIPTION, "Sone desc");
        values.put(MediaStore.Images.Media.DATE_TAKEN, System.currentTimeMillis());
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
        values.put(MediaStore.MediaColumns.DATA, filepath);

        return getApplicationContext().getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
    }

//    public static float convertPixelsToDp(float px, Context context) {
//        Resources resources = context.getResources();
//        DisplayMetrics metrics = resources.getDisplayMetrics();
//        float dp = px / (metrics.densityDpi / 160f);
//        return dp;
//    }

    private Bitmap processingTextOnBitmap(String captionString, int height) {
        Bitmap newBitmap = null;
        Bitmap.Config config = commonBitmap.getConfig();
        if (config == null) {
            config = Bitmap.Config.ARGB_8888;
        }
        newBitmap = Bitmap.createBitmap(commonBitmap.getWidth(), commonBitmap.getHeight(), config);
        Canvas newCanvas = new Canvas(newBitmap);
        newCanvas.drawBitmap(commonBitmap, 0, 0, null);
        if (captionString != null) {
            Paint paintText = new Paint(Paint.ANTI_ALIAS_FLAG);
            //paintText.setColor(Color.BLACK);
            float textSize = getTextSizeAndIndent()[0];
            paintText.setTextSize(textSize);
            paintText.setColor(Color.WHITE);
            // paintText.setStyle(Style.FILL_AND_STROKE);
            // paintText.setShadowLayer(10f, 10f, 10f, Color.BLACK);

            Rect rectText = new Rect();
            CharSequence cs = captionString;
            paintText.getTextBounds(cs.toString(), 0, captionString.length(), rectText);

            Log.d("Debug", "" + cs.toString() + rectText.height());
            newCanvas.drawText(cs, 0, captionString.length(), (float) (commonBitmap.getWidth() / 1.9), height, paintText);
        } else {
            Toast.makeText(getApplicationContext(), "caption empty!", Toast.LENGTH_LONG).show();
        }
        commonBitmap = newBitmap;
        return newBitmap;
    }

    private float [] getTextSizeAndIndent() {
        int commonBitmapWidth = commonBitmap.getWidth();
        float[] textSizeAndIndent = new  float[2];

        if (commonBitmapWidth < 1024) {
            textSizeAndIndent [0] = 18;
            textSizeAndIndent [1] = 80;
        } else if (commonBitmapWidth >= 1024 && commonBitmapWidth < 2047) {
            textSizeAndIndent [0] = 30;
            textSizeAndIndent [1] = 100; //
        } else if (commonBitmapWidth >= 2048 && commonBitmapWidth < 2591) {
            textSizeAndIndent [0] = 60;
            textSizeAndIndent [1] = 160;
        } else if (commonBitmapWidth >= 2592 && commonBitmapWidth < 3263) {
            textSizeAndIndent [0] = 90;
            textSizeAndIndent [1] = 180; //
        } else if (commonBitmapWidth >= 3264 && commonBitmapWidth < 4127) {
            textSizeAndIndent [0] = 130;
            textSizeAndIndent [1] = 200;
        } else {
            textSizeAndIndent [0] = 140;
            textSizeAndIndent [1] = 220;
        }
        return textSizeAndIndent;
    }

    @Override
    public void onClick(View v) {
    }

}
