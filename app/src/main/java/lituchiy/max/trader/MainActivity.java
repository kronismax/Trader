package lituchiy.max.trader;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import com.vk.sdk.VKUIHelper;
import com.vk.sdk.api.VKApi;
import com.vk.sdk.api.VKApiConst;
import com.vk.sdk.api.VKError;
import com.vk.sdk.api.VKParameters;
import com.vk.sdk.api.VKRequest;
import com.vk.sdk.api.VKRequest.VKRequestListener;
import com.vk.sdk.api.VKResponse;
import com.vk.sdk.api.model.VKApiPhoto;
import com.vk.sdk.api.model.VKAttachments;
import com.vk.sdk.api.model.VKPhotoArray;
import com.vk.sdk.api.model.VKWallPostResult;
import com.vk.sdk.api.photo.VKImageParameters;
import com.vk.sdk.api.photo.VKUploadImage;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends Activity {

    private static final String TAG_ID = "id";
    private static final String TAG_DEBUG = "DEBUG";
    private static final String TAG_RESPONSE = "response";
    static JSONArray responseId = null;
    static JSONArray responseAlbum = null;
    static UserInfo userInfo;
    static ArrayAdapter<String> dataAdapter;
    static Bitmap bitmapToSend;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);
        userInfo = new UserInfo();
        VKRequest getAlbumsRequest = new VKRequest("photos.getAlbums", VKParameters.from(VKApiConst.FIELDS, "TAG_ID"));
        if (getAlbumsRequest == null) return;
        getAlbumsRequest.executeWithListener(mRequestListenerAlbum);

        VKRequest getUserIdRequest = VKApi.users().get(VKParameters.from("id"));
        if (getUserIdRequest == null) return;
        getUserIdRequest.executeWithListener(mRequestListenerID);

        if (savedInstanceState == null) {
            getFragmentManager().beginTransaction().add(R.id.container, new PlaceholderFragment()).commit();
        }
        Intent intent = getIntent();
        if(getIntent().getExtras()!=null) {
            String imagePath = intent.getExtras().getString("picturePath");
            Uri imageUri = Uri.parse(imagePath);
            Log.d(TAG_DEBUG, "imagePath: "+imageUri.toString());
            try {
                bitmapToSend = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);
                Log.d(TAG_DEBUG, "bitmapToSent: OK");
            } catch (IOException e) {
                e.printStackTrace();
                Log.d(TAG_DEBUG, "bitmapToSent: NOT OK");
            }
        }

    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            View v = inflater.inflate(R.layout.fragment_test, container, false);

            final Spinner spinner = (Spinner) v.findViewById(R.id.spinner);
            spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parentView, View view, int position, long id) {
                    String selected = parentView.getItemAtPosition(position).toString();
                    Context context = parentView.getContext();
                    CharSequence text = selected;
                    int duration = Toast.LENGTH_SHORT;

                    Toast toast = Toast.makeText(context, text, duration);
                    toast.show();

                    switch (position) {
                        case 0:
                            toast.setText(spinner.getSelectedItem().toString());
                            userInfo.setSelectedAlbum(spinner.getSelectedItem().toString());
                            break;
                        case 1:
                            userInfo.setSelectedAlbum(spinner.getSelectedItem().toString());
                            toast.setText(spinner.getSelectedItem().toString());
                            break;
                        default:
                            break;
                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {
                }
            });

            List<String> albums = new ArrayList<String>();
            dataAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_1, albums);
            spinner.setAdapter(dataAdapter);
            return v;
        }

        @Override
        public void onViewCreated(View view, Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);

            view.findViewById(R.id.createPicture).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(getActivity(), PictureActivity.class);
                    startActivity(intent);
                }
            });

            view.findViewById(R.id.users_get).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    VKRequest request = VKApi.users().get(VKParameters.from(VKApiConst.FIELDS,
                            "id,first_name,last_name,sex,bdate,city,country,photo_50,photo_100," +
                                    "photo_200_orig,photo_200,photo_400_orig,photo_max,photo_max_orig,online," +
                                    "online_mobile,lists,domain,has_mobile,contacts,connections,site,education," +
                                    "universities,schools,can_post,can_see_all_posts,can_see_audio,can_write_private_message," +
                                    "status,last_seen,common_count,relation,relatives,counters"));
//                    VKRequest request = VKApi.users().get(VKParameters.from(VKApiConst.USER_IDS, "1,2"));
                    request.secure = false;
                    request.useSystemLanguage = false;
                    request.executeWithListener(mRequestListenerAlbum);
                    //Log.d("Debug VKApiPhotoAlbum ", "" + request2.response.get().json.toString());
                    startApiCall(request);
                }
            });

            view.findViewById(R.id.upload_photo).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    final Bitmap photo = bitmapToSend;

                    VKRequest request = VKApi.uploadAlbumPhotoRequest(new VKUploadImage(photo, VKImageParameters.pngImage()), Integer.valueOf(userInfo.getSelectedAlbum()), 0); //
                    request.executeWithListener(new VKRequestListener() {
                        @Override
                        public void onComplete(VKResponse response) {
                            photo.recycle();
                            VKPhotoArray photoArray = (VKPhotoArray) response.parsedModel;
                            Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(String.format("https://vk.com/photo" + userInfo.getUserId() + "_%s", photoArray.get(0).id)));
                            startActivity(i);
                        }

                        @Override
                        public void onError(VKError error) {
                            showError(error);
                        }
                    });
                }
            });

            view.findViewById(R.id.upload_photo_to_wall).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(bitmapToSend==null){
                        Toast toast = Toast.makeText(getActivity().getApplicationContext(), "Create image first", Toast.LENGTH_SHORT);
                        toast.show();
                        return;
                    }
                    final Bitmap photo = bitmapToSend;
                    VKRequest request = VKApi.uploadWallPhotoRequest(new VKUploadImage(photo, VKImageParameters.pngImage()), 0, 0);
                    request.executeWithListener(new VKRequestListener() {
                        @Override
                        public void onComplete(VKResponse response) {
                            photo.recycle();
                            VKApiPhoto photoModel = ((VKPhotoArray) response.parsedModel).get(0);
                            makePost(new VKAttachments(photoModel));
                        }

                        @Override
                        public void onError(VKError error) {
                            showError(error);
                        }
                    });
                }
            });
        }


        private void startApiCall(VKRequest request) {
            Intent i = new Intent(getActivity(), ApiCallActivity.class);
            i.putExtra("request", request.registerObject());
            startActivity(i);
        }

        private void showError(VKError error) {
            new AlertDialog.Builder(getActivity())
                    .setMessage(error.errorMessage)
                    .setPositiveButton("OK", null)
                    .show();

            if (error.httpError != null) {
                Log.w("Test", "Error in request or upload", error.httpError);
            }
        }

        private Bitmap getPhoto() {
            Bitmap b = null;

            try {
                b = BitmapFactory.decodeStream(getActivity().getAssets().open("android.jpg"));
            } catch (IOException e) {
                e.printStackTrace();
            }

            return b;
        }

        private void makePost(VKAttachments attachments) {
            makePost(attachments, null);
        }

        private void makePost(VKAttachments attachments, String message) {
            VKRequest request = VKApi.users().get(VKParameters.from("id"));
            if (request == null) return;
            request.executeWithListener(mRequestListenerAlbum);
            VKRequest post = VKApi.wall().post(VKParameters.from(VKApiConst.OWNER_ID, userInfo.getUserId(), VKApiConst.ATTACHMENTS, attachments, VKApiConst.MESSAGE, message));
            post.setModelClass(VKWallPostResult.class);
            post.executeWithListener(new VKRequestListener() {
                @Override
                public void onComplete(VKResponse response) {
                    super.onComplete(response);
                    VKWallPostResult result = (VKWallPostResult) response.parsedModel;
                    Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(String.format("https://vk.com/wall" + userInfo.getUserId() + "_%s", result.post_id)));
                    startActivity(i);
                }

                @Override
                public void onError(VKError error) {
                    showError(error.apiError != null ? error.apiError : error);
                }
            });
        }
    }

    static VKRequestListener mRequestListenerAlbum = new VKRequestListener() {
        @Override
        public void onComplete(VKResponse response) {
            Log.d("Debug request album", "" + response.json.toString());
            JSONObject jsonObj = null;
            JSONObject jsonObj2 = null;
            try {
                jsonObj = new JSONObject(response.json.toString());
                jsonObj2 = new JSONObject(jsonObj.get("response").toString());
                responseAlbum = (JSONArray) jsonObj2.getJSONArray("items");

                for (int i = 0; i < responseAlbum.length(); i++) {
                    JSONObject c = null;
                    c = responseAlbum.getJSONObject(i);
                    userInfo.setAlbums(c.getString("id"), c.getString("title"));
                    dataAdapter.add(c.getString("title"));
                    Log.d(TAG_DEBUG, "getUserAlbumId: " + userInfo.getUserAlbumId(c.getString("title")));
                }

                dataAdapter.notifyDataSetChanged();
                userInfo.showUserAlbums();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onError(VKError error) {
        }

        @Override
        public void onProgress(VKRequest.VKProgressType progressType, long bytesLoaded,
                               long bytesTotal) {
            // you can show progress of the request if you want
        }

        @Override
        public void attemptFailed(VKRequest request, int attemptNumber, int totalAttempts) {
        }
    };

    static VKRequestListener mRequestListenerID = new VKRequestListener() {
        @Override
        public void onComplete(VKResponse response) {
            JSONObject jsonObj = null;
            try {
                jsonObj = new JSONObject(response.json.toString());
                responseId = jsonObj.getJSONArray(TAG_RESPONSE);
                for (int i = 0; i < responseId.length(); i++) {
                    JSONObject c = null;
                    c = responseId.getJSONObject(i);
                    userInfo.setUserId(c.getString(TAG_ID));
                }
                Log.d("Debug mRequestAlbum", "" + response.json.toString());
                Log.d("Debug mRequestAlbum", "UserId = " + userInfo.getUserId());
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onError(VKError error) {
        }

        @Override
        public void onProgress(VKRequest.VKProgressType progressType, long bytesLoaded,
                               long bytesTotal) {
            // you can show progress of the request if you want
        }

        @Override
        public void attemptFailed(VKRequest request, int attemptNumber, int totalAttempts) {
        }
    };

    private static class UserInfo {

        public UserInfo() {
        }

        String userId;
        static Map<String, String> albums = new HashMap<>(); // album identificator & title

        public  String selectedAlbum;

        public  String getSelectedAlbum() {
            return getUserAlbumId(selectedAlbum);
        }

        public  void setSelectedAlbum(String selectedAlbum) {
            this.selectedAlbum = selectedAlbum;
        }

        public Map<String, String> returnAlbums() {
            Map<String, String> map = albums;
            return map;
        }

        public String getUserId() {
            return userId;
        }

        public int getAlbumsCount() {
            return albums.size();
        }

        public void setUserId(String userId) {
            this.userId = userId;
        }

        public Map<String, String> getAlbums() {
            return albums;
        }

        public void setAlbums(String id, String title) {
            albums.put(id, title);
        }

        public String getUserAlbumId(String title) {
            String id = "";

            for (String e : albums.keySet()) {
                if (albums.get(e).equals(title)) {
                    id = e;
                }
            }
            return id;
        }

        public void showUserAlbums() {
            for (String e : albums.keySet()) {
                String id = e;
                String value = albums.get(e);
                Log.d(TAG_DEBUG, "Album title: " + value + ", id: " + id);

            }
        }

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
        VKUIHelper.onResume(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        VKUIHelper.onDestroy(this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        VKUIHelper.onActivityResult(this, requestCode, resultCode, data);
    }

}

