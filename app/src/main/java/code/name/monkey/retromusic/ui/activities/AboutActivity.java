package code.name.monkey.retromusic.ui.activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import android.support.v4.app.ShareCompat;
import android.support.v7.widget.AppCompatImageButton;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.kabouzeid.appthemehelper.ThemeStore;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.BindViews;
import butterknife.ButterKnife;
import butterknife.OnClick;
import code.name.monkey.retromusic.Constants;
import code.name.monkey.retromusic.R;
import code.name.monkey.retromusic.ui.activities.base.AbsBaseActivity;
import code.name.monkey.retromusic.ui.adapter.base.MediaEntryViewHolder;
import de.hdodenhof.circleimageview.CircleImageView;

/**
 * @author Hemanth S (h4h13)
 */
public class AboutActivity extends AbsBaseActivity {
    private static final int AVATAR_SIZE = 200;
    private static List<Contributors> sList = new ArrayList<>();

    static {
        sList.add(new Contributors(
                R.string.luis_gomez,
                R.string.spanish,
                R.drawable.luis_gmzz
        ));
    }

    @BindView(R.id.toolbar)
    Toolbar mToolbar;
    @BindViews({R.id.iconProfile,
            R.id.karim_abou_zeid_profile,
            R.id.luis_gomez_profile,
            R.id.aleksandar_tesic_profile,
            R.id.gabriel_zegarra_profile,
            R.id.material_design_city})
    List<CircleImageView> profiles;
    @BindView(R.id.made_text)
    TextView madeText;
    @BindView(R.id.app_share)
    AppCompatImageButton mAppShare;
    @BindView(R.id.google_plus_circle_btn)
    AppCompatImageButton mGooglePlusCircleBtn;
    @BindView(R.id.telegram_btn)
    AppCompatImageButton mTelegramBtn;
    @BindView(R.id.karimAbourGooglePlus)
    AppCompatImageButton mKarimAbourGooglePlus;
    @BindView(R.id.karimAbourGithub)
    AppCompatImageButton mKarimAbourGithub;
    @BindView(R.id.luisGomezGooglePlus)
    ImageButton mLuisGomezGooglePlus;
    @BindView(R.id.luisGomezTwitter)
    ImageButton mLuisGomezTwitter;
    @BindView(R.id.contributors)
    TextView mContributors;
    @BindView(R.id.recycler_view)
    RecyclerView mRecyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        ButterKnife.bind(this);

        setStatusbarColorAuto();
        setNavigationbarColorAuto();
        setTaskDescriptionColorAuto();

        setEmojiText();
        setUpToolbar();
        loadProfileImages();
        setupContributors();
    }

    private void setupContributors() {
        mContributors.setTextColor(ThemeStore.accentColor(this));
        mRecyclerView.setLayoutManager(new GridLayoutManager(this, 3));
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mRecyclerView.setAdapter(new ContributorsAdapter(sList));
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void setUpToolbar() {

        mToolbar.setBackgroundColor(ThemeStore.primaryColor(this));
        setTitle(R.string.action_about);
        setSupportActionBar(mToolbar);
        //noinspection ConstantConditions
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void loadProfileImages() {
        Glide.with(this).load(R.drawable.hemanth_s).override(AVATAR_SIZE, AVATAR_SIZE).into(profiles.get(0));
        Glide.with(this).load(R.drawable.karim_abou_zeid).override(AVATAR_SIZE, AVATAR_SIZE).into(profiles.get(1));
        Glide.with(this).load(R.drawable.luis_gmzz).override(AVATAR_SIZE, AVATAR_SIZE).into(profiles.get(2));
        Glide.with(this).load(R.drawable.aleksandar_tesic).override(AVATAR_SIZE, AVATAR_SIZE).into(profiles.get(3));
        Glide.with(this).load(R.drawable.gabriel_zegarra).override(AVATAR_SIZE, AVATAR_SIZE).into(profiles.get(4));
        Glide.with(this).load(R.drawable.img_techjuice).override(AVATAR_SIZE, AVATAR_SIZE).into(profiles.get(5));
    }

    @SuppressLint("SetTextI18n")
    private void setEmojiText() {
        int night = 0x1F303;
        int sun = 0x1F304;
        int heart = 0x1F60D;

        madeText.setText(String.format("Made with love %s in india", getEmojiByUnicode(heart)));
    }

    private void openUrl(String url) {
        Intent i = new Intent(Intent.ACTION_VIEW);
        i.setData(Uri.parse(url));
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(i);
    }


    public String getEmojiByUnicode(int unicode) {
        return new String(Character.toChars(unicode));
    }

    @OnClick({R.id.gabriel_zegarra, R.id.app_github, R.id.aleksandar_tesic,
            R.id.google_plus_circle_btn, R.id.telegram_btn,
            R.id.karimAbourGooglePlus, R.id.karimAbourGithub, R.id.luisGomezGooglePlus,
            R.id.luisGomezTwitter, R.id.app_telegram_channel, R.id.app_google_plus,
            R.id.material_design_city_wallpaper_link,
            R.id.app_translation, R.id.app_rate, R.id.app_share, R.id.material_design_link})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.app_github:
                openUrl(Constants.GITHUB_PROJECT);
                break;
            case R.id.material_design_city_wallpaper_link:
                openUrl(Constants.MATERIAL_TECHJUICE_IMAGES);
                break;
            case R.id.material_design_link:
                openUrl(Constants.MATERIAL_DESIGN_ICONS);
                break;
            case R.id.app_google_plus:
                openUrl(Constants.GOOGLE_PLUS_COMMUNITY);
                break;
            case R.id.app_telegram_channel:
                openUrl(Constants.TELEGRAM_CHANNEL);
                break;
            case R.id.google_plus_circle_btn:
                openUrl(Constants.GOOGLE_PLUS_PROFILE);
                break;
            case R.id.telegram_btn:
                openUrl(Constants.HEMANTH_TELEGRAM);
                break;
            case R.id.karimAbourGooglePlus:
                openUrl(Constants.KARIM_GOOGLE_PLUS);
                break;
            case R.id.karimAbourGithub:
                openUrl(Constants.KARIM_GITHUB);
                break;
            case R.id.luisGomezGooglePlus:
                openUrl(Constants.LUIS_GOMZ_GOOGLE_PLUS);
                break;
            case R.id.luisGomezTwitter:
                openUrl(Constants.LUIS_GOMZ_TWITTER);
                break;
            case R.id.aleksandar_tesic:
                openUrl(Constants.ALEKSANDAR_TESIC_GOOGLE_PLUS);
                break;
            case R.id.gabriel_zegarra:
                openUrl(Constants.GABRIEL_ZEGARRA_GOOGLE_PLUS);
                break;
            case R.id.app_translation:
                openUrl(Constants.TRANSLATE);
                break;
            case R.id.app_rate:
                openUrl(Constants.RATE_ON_GOOGLE_PLAY);
                break;
            case R.id.app_share:
                shareApp();
                break;
        }
    }

    private void shareApp() {
        Intent shareIntent = ShareCompat.IntentBuilder.from(this)
                .setType("text/plain")
                .setText(String.format(getString(R.string.app_share), getPackageName()))
                .getIntent();
        if (shareIntent.resolveActivity(getPackageManager()) != null) {
            startActivity(Intent.createChooser(shareIntent, getResources().getText(R.string.action_share)));
        }
        /*Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT, String.format(getString(R.string.app_share), getPackageName()));
        sendIntent.setType("text/plain");


        startActivity(Intent.createChooser(sendIntent, getResources().getText(R.string.action_share)));
*/
        /*Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT, String.format(getString(R.string.app_share), getPackageName()));
        sendIntent.setType("text/plain");
        startActivity(sendIntent);*/
    }

    public static class Contributors {
        @StringRes
        public int title;
        @StringRes
        public int text;
        @DrawableRes
        public int image;

        Contributors(@StringRes int title, @StringRes int text, @DrawableRes int image) {
            this.title = title;
            this.text = text;
            this.image = image;
        }
    }

    class ContributorsAdapter extends RecyclerView.Adapter<MediaEntryViewHolder> {
        private List<Contributors> mList = new ArrayList<>();

        ContributorsAdapter(List<Contributors> list) {
            mList = list;
        }

        @Override
        public MediaEntryViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
            return new MediaEntryViewHolder(LayoutInflater.from(AboutActivity.this)
                    .inflate(R.layout.item_contributors, viewGroup, false));
        }

        @Override
        public void onBindViewHolder(MediaEntryViewHolder holder, int i) {
            Contributors contributors = mList.get(i);
            if (holder.title != null) {
                holder.title.setText(contributors.title);
            }
            if (holder.text != null) {
                holder.text.setText(contributors.text);
            }
            if (holder.image != null) {
                Glide.with(AboutActivity.this)
                        .load(contributors.image).override(AVATAR_SIZE, AVATAR_SIZE)
                        .into(holder.image);
            }
        }

        @Override
        public int getItemCount() {
            return mList.size();
        }
    }
}
