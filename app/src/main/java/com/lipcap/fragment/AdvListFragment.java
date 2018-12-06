package com.lipcap.fragment;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import com.lipcap.R;
import com.lipcap.adapter.AdvListAdapter;
import com.lipcap.main.BaseFragment;
import com.lipcap.model.input.AddAdvInputEntity;
import com.lipcap.model.input.AdvInputEntity;
import com.lipcap.model.output.AdvDetailsEntity;
import com.lipcap.model.output.AdvResponse;
import com.lipcap.model.output.CommonResponse;
import com.lipcap.services.APIRequestHandler;
import com.lipcap.ui.provider.ProviderHome;
import com.lipcap.utils.AppConstants;
import com.lipcap.utils.DialogManager;
import com.lipcap.utils.InterfaceBtnCallback;
import com.lipcap.utils.InterfaceEdtBtnCallback;
import com.lipcap.utils.InterfaceTwoBtnCallback;
import com.lipcap.utils.PreferenceUtil;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;

import static android.app.Activity.RESULT_OK;

public class AdvListFragment extends BaseFragment {


    private final int CAMERA_CAPTURE_IMAGE_REQUEST_CODE = 100;
    private final int GALLERY_IMAGE_REQUEST_CODE = 200;
    @BindView(R.id.adv_list_recycler_view)
    RecyclerView mAdvListRecyclerView;
    private Uri mPictureFileUri;
    private String mUploadImgPathStr = "";
    private File mCameraFile;


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.frag_adv_list, container, false);
        /*ButterKnife for variable initialization*/
        ButterKnife.bind(this, rootView);

        /*Keypad to be hidden when a touch made outside the edit text*/
        setupUI(rootView);

        /*For focus current fragment*/
        rootView.setOnTouchListener(new View.OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    default:
                        v.performClick();
                }
                return true;
            }
        });


        return rootView;
    }


    /*Fragment manual onResume*/
    @Override
    public void onFragmentResume() {
        if (getActivity() != null) {
            initView();
            ((ProviderHome) getActivity()).mHeaderEndImgLay.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (checkPermission()) {
                        uploadImage();
                    }
                }
            });
        }
    }

    /*InitViews*/
    private void initView() {

        AppConstants.TAG = this.getClass().getSimpleName();
        advListAPICall();

    }

    private void advListAPICall() {
        AdvInputEntity advInputEntity = new AdvInputEntity();
        advInputEntity.setUserId(PreferenceUtil.getUserId(getActivity()));
        advInputEntity.setProviderId(PreferenceUtil.getUserId(getActivity()));
        APIRequestHandler.getInstance().getUserAdvListAPICall(advInputEntity, this);
    }


    private void setAdapter(final ArrayList<AdvDetailsEntity> advDetailList) {
        if (getActivity() != null) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mAdvListRecyclerView.setLayoutManager(new GridLayoutManager(getActivity(), 4));
                    mAdvListRecyclerView.setAdapter(new AdvListAdapter(advDetailList, getActivity()));
                }
            });
        }
    }


    /*API request success and failure*/
    @Override
    public void onRequestSuccess(Object resObj) {
        super.onRequestSuccess(resObj);
        if (resObj instanceof AdvResponse) {
            AdvResponse advResponse = (AdvResponse) resObj;
            if (advResponse.getStatusCode().equals(AppConstants.SUCCESS_CODE)) {
                if (advResponse.getResult().size() > 0) {
                    setAdapter(advResponse.getResult());
                }
            } else {
                DialogManager.getInstance().showAlertPopup(getActivity(), advResponse.getMessage(), this);
            }

        } else if (resObj instanceof CommonResponse) {
            advListAPICall();
        }

    }

    @Override
    public void onRequestFailure(final Object resObj, Throwable t) {
        super.onRequestFailure(resObj, t);
        if (t instanceof IOException) {
            DialogManager.getInstance().showAlertPopup(getActivity(),
                    (t instanceof java.net.ConnectException || t instanceof java.net.UnknownHostException ? getString(R.string.no_internet) : getString(R.string
                            .connect_time_out)), new InterfaceBtnCallback() {
                        @Override
                        public void onPositiveClick() {
                        }
                    });
        }
    }

    private void uploadImage() {

        DialogManager.getInstance().showPictureUploadPopup(getActivity(), new InterfaceTwoBtnCallback() {
            @Override
            public void onNegativeClick() {
                galleryImage();
            }

            @Override
            public void onPositiveClick() {
                captureImage();
            }
        });


    }

    /*open camera*/
    private void captureImage() {

        mCameraFile = getOutputMediaFile();
        if (getActivity() != null && mCameraFile != null) {
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            mPictureFileUri = (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) ? FileProvider.getUriForFile(getActivity(), getActivity().getPackageName() + ".provider", mCameraFile) : Uri.fromFile(mCameraFile);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, mPictureFileUri);


            // start the image capture Intent
            getActivity().startActivityForResult(intent, CAMERA_CAPTURE_IMAGE_REQUEST_CODE);
        }
    }

    /*open gallery*/
    private void galleryImage() {

        if (getActivity() != null) {
            Intent j = new Intent(
                    Intent.ACTION_PICK,
                    android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);

            getActivity().startActivityForResult(j, GALLERY_IMAGE_REQUEST_CODE);

        }
    }

    private File getOutputMediaFile() {
        // External sdcard location
        File mediaStorageDir = new File(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), getString(R.string.app_name));

        // Create the storage directory if it does not exist
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                Log.e(this.getClass().getSimpleName(), "Failed to find directory");
                return null;
            }
        }
        String timeStamp = String.valueOf(System.currentTimeMillis());
        File mediaFile;
        mediaFile = new File(mediaStorageDir.getPath() + File.separator
                + timeStamp + ".jpg");

        return mediaFile;
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable("mPictureFileUri", mPictureFileUri);
    }


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (savedInstanceState != null) {
            // Restore last state for checked position.
            mPictureFileUri = savedInstanceState.getParcelable("mPictureFileUri");
        }
    }

    /*onActivityResult for image capture*/
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (getActivity() != null) {
            if (requestCode == CAMERA_CAPTURE_IMAGE_REQUEST_CODE) {
                if (resultCode == RESULT_OK) {
                    /*Image captured successfully and displayed in the image view*/
                    mUploadImgPathStr = mPictureFileUri.getPath();

                    try {
                        Bitmap bm = BitmapFactory.decodeStream(
                                getActivity().getContentResolver().openInputStream(mPictureFileUri));

                        mUploadImgPathStr = getRealPathFromURI(getImageUri(getContext(),bm));
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }

                    sysOut("CAMERA " + mUploadImgPathStr);
                    previewCapturedImage();
                } else {
                    mUploadImgPathStr = "";
                    /*image capture getting failed due to certail technical issues*/
                    DialogManager.getInstance().showToast(getActivity(), "failed to load image");
                }

            } else if (requestCode == GALLERY_IMAGE_REQUEST_CODE) {
                if (resultCode == RESULT_OK) {
                    mPictureFileUri = data.getData();
                    Cursor cursor = getActivity().getContentResolver().query(mPictureFileUri, null, null, null, null);

                    if (cursor == null) { // Source is Dropbox or other similar local file path
                        mUploadImgPathStr = mPictureFileUri.getPath();

                    } else {
                        cursor.moveToFirst();
                        int idx = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
                        mUploadImgPathStr = cursor.getString(idx);

                        cursor.close();
                    }

                    sysOut("GALLERY " + mUploadImgPathStr);
                    previewCapturedImage();

                } else {
                    mUploadImgPathStr = "";

                    /*image capture getting failed due to certail technical issues*/
                    DialogManager.getInstance().showToast(getActivity(), "failed to load image");
                }
            }

        }
    }

    public Uri getImageUri(Context inContext, Bitmap inImage) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String timeStamp = String.valueOf(System.currentTimeMillis());
        String path = MediaStore.Images.Media.insertImage(inContext.getContentResolver(), inImage, timeStamp, null);
        return Uri.parse(path);
    }

    public String getRealPathFromURI(Uri uri) {
        String pathStr = "";
        if (getActivity() != null) {
            Cursor cursor = getActivity().getContentResolver().query(uri, null, null, null, null);
            if (cursor != null) {
                cursor.moveToFirst();
                int idx = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
                pathStr = cursor.getString(idx);
                cursor.close();
            }
            return pathStr;
        } else {
            return pathStr;
        }
    }

    private File getOutputMediaFile(Bitmap bmp) {

        // External sdcard location
        File mediaStorageDir = new File(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), getString(R.string.app_name));

        // Create the storage directory if it does not exist
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                Log.e(this.getClass().getSimpleName(), "Failed to find directory");
                return null;
            }
        }
        String timeStamp = new SimpleDateFormat("dd-MM-yyyy-HH:mm:ss",
                Locale.getDefault()).format(new Date());

        File mediaFile = new File(mediaStorageDir.getPath() + File.separator
                + getString(R.string.app_name) + "_" + timeStamp + ".png");
        try {
            FileOutputStream out = new FileOutputStream(mediaFile);
            bmp.compress(Bitmap.CompressFormat.PNG, 100, out);
            try {
                out.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        return mediaFile;
    }

    private void previewCapturedImage() {

        DialogManager.getInstance().showImagePreviewDialogPopup(getActivity(), mPictureFileUri, new InterfaceEdtBtnCallback() {
            @Override
            public void onPositiveClick(String amountStr) {

                AddAdvInputEntity addAdvInputEntity = new AddAdvInputEntity();
                addAdvInputEntity.setAmount(amountStr);
                addAdvInputEntity.setUserId(PreferenceUtil.getUserId(getActivity()));

                APIRequestHandler.getInstance().profileImageUploadApiCall(addAdvInputEntity, mUploadImgPathStr, AdvListFragment.this);
            }

            @Override
            public void onNegativeClick() {

            }
        });

    }


    /* Ask for permission on Camera access*/
    private boolean checkPermission() {

        boolean addPermission = true;
        if (getActivity() != null) {
            List<String> listPermissionsNeeded = new ArrayList<>();
            if (android.os.Build.VERSION.SDK_INT >= 23) {
                int cameraPermission = ContextCompat.checkSelfPermission(getActivity(), android.Manifest.permission.CAMERA);
                int readStoragePermission = ContextCompat.checkSelfPermission(getActivity(), android.Manifest.permission.READ_EXTERNAL_STORAGE);
                int storagePermission = ContextCompat.checkSelfPermission(getActivity(), android.Manifest.permission.WRITE_EXTERNAL_STORAGE);

                if (cameraPermission != PackageManager.PERMISSION_GRANTED) {
                    listPermissionsNeeded.add(Manifest.permission.CAMERA);
                }
                if (readStoragePermission != PackageManager.PERMISSION_GRANTED) {
                    listPermissionsNeeded.add(android.Manifest.permission.READ_EXTERNAL_STORAGE);
                }
                if (storagePermission != PackageManager.PERMISSION_GRANTED) {
                    listPermissionsNeeded.add(android.Manifest.permission.WRITE_EXTERNAL_STORAGE);
                }
            }

            if (!listPermissionsNeeded.isEmpty()) {
                addPermission = askAccessPermission(listPermissionsNeeded, 1, new InterfaceTwoBtnCallback() {
                    @Override
                    public void onPositiveClick() {
                        uploadImage();
                    }

                    public void onNegativeClick() {
                    }
                });
            }
        } else {
            addPermission = false;
        }

        return addPermission;

    }

}

