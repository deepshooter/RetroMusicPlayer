package code.name.monkey.retromusic.ui.activities;

import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import code.name.monkey.retromusic.R;
import code.name.monkey.retromusic.ui.activities.base.AbsBaseActivity;
import code.name.monkey.retromusic.util.Compressor;
import code.name.monkey.retromusic.util.ImageUtil;
import code.name.monkey.retromusic.util.PreferenceUtil;
import de.hdodenhof.circleimageview.CircleImageView;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

import static code.name.monkey.retromusic.Constants.USER_PROFILE;

/**
 * Created by hemanths on 23/08/17.
 */

public class UserInfoActivity extends AbsBaseActivity {
    private static final int PICK_IMAGE_REQUEST = 9002;
    private static final int PROILE_ICON_SIZE = 400;
    @BindView(R.id.name)
    EditText mName;
    @BindView(R.id.user_image)
    CircleImageView mUserImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setDrawUnderStatusbar(true);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_info);
        ButterKnife.bind(this);

        setStatusbarColorAuto();
        setNavigationbarColorAuto();
        setTaskDescriptionColorAuto();

        mName.setText(PreferenceUtil.getInstance(this).getUserName());
        if (!PreferenceUtil.getInstance(this).getProfileImage().isEmpty()) {
            loadImageFromStorage(PreferenceUtil.getInstance(this).getProfileImage());
        }
    }

    @OnClick({R.id.next})
    void next(View view) {
        switch (view.getId()) {
            case R.id.next:
                String name = mName.getText().toString().trim();
                if (TextUtils.isEmpty(name)) {
                    Toast.makeText(this, "Umm name is empty", Toast.LENGTH_SHORT).show();
                    return;
                }
                PreferenceUtil.getInstance(this).setUserName(name);
                setResult(RESULT_OK);
                finish();
                break;
        }
    }

    @OnClick(R.id.image)
    public void onViewClicked() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri uri = data.getData();
            try {
                Bitmap bitmap = ImageUtil.getResizedBitmap(MediaStore.Images.Media.getBitmap(getContentResolver(), uri), PROILE_ICON_SIZE);
                String profileImagePath = saveToInternalStorage(bitmap);
                PreferenceUtil.getInstance(this).saveProfileImage(profileImagePath);
                loadImageFromStorage(profileImagePath);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void loadImageFromStorage(String path) {
        new Compressor(this)
                .setMaxHeight(300)
                .setMaxWidth(300)
                .setQuality(75)
                .setCompressFormat(Bitmap.CompressFormat.WEBP)
                .compressToBitmapAsFlowable(new File(path, USER_PROFILE))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(bitmap -> {
                    mUserImage.setImageBitmap(bitmap);
                });
    }

    private String saveToInternalStorage(Bitmap bitmapImage) {
        ContextWrapper cw = new ContextWrapper(getApplicationContext());
        // path to /data/data/yourapp/app_data/imageDir
        File directory = cw.getDir("imageDir", Context.MODE_PRIVATE);
        // Create imageDir
        File mypath = new File(directory, USER_PROFILE);

        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(mypath);
            // Use the compress method on the BitMap object to write image to the OutputStream
            bitmapImage.compress(Bitmap.CompressFormat.PNG, 100, fos);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                fos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return directory.getAbsolutePath();
    }
}
