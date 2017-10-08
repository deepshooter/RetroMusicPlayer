package code.name.monkey.retromusic.ui.activities.tageditor;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.content.ContextCompat;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;

import org.jaudiotagger.tag.FieldKey;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import code.name.monkey.retromusic.R;
import code.name.monkey.retromusic.glide.palette.BitmapPaletteTranscoder;
import code.name.monkey.retromusic.glide.palette.BitmapPaletteWrapper;
import code.name.monkey.retromusic.lastfm.rest.LastFMRestClient;
import code.name.monkey.retromusic.loaders.AlbumLoader;
import code.name.monkey.retromusic.model.Song;
import code.name.monkey.retromusic.util.ImageUtil;
import code.name.monkey.retromusic.util.LastFMUtil;
import code.name.monkey.retromusic.util.RetroMusicColorUtil;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;


public class AlbumTagEditorActivity extends AbsTagEditorActivity implements TextWatcher {

    public static final String TAG = AlbumTagEditorActivity.class.getSimpleName();
    private static final float SCRIM_ADJUSTMENT = 0.075f;
    @BindView(R.id.title)
    EditText albumTitle;
    @BindView(R.id.album_artist)
    EditText albumArtist;
    @BindView(R.id.genre)
    EditText genre;
    @BindView(R.id.year)
    EditText year;
    @BindView(R.id.album_collapsing_toolbar)
    CollapsingToolbarLayout albumCollapsingToolbar;
    @BindView(R.id.editables)
    LinearLayout editables;
    @BindView(R.id.progressBar)
    ProgressBar progressBar;
    private Bitmap albumArtBitmap;
    private boolean deleteAlbumArt;
    private LastFMRestClient lastFMRestClient;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ButterKnife.bind(this);
        lastFMRestClient = new LastFMRestClient(this);
        setUpViews();
    }

    private void setUpViews() {
        fillViewsWithFileTags();
        albumTitle.addTextChangedListener(this);
        albumArtist.addTextChangedListener(this);
        genre.addTextChangedListener(this);
        year.addTextChangedListener(this);
    }

    private void fillViewsWithFileTags() {
        albumTitle.setText(getAlbumTitle());
        albumArtist.setText(getAlbumArtistName());
        genre.setText(getGenreName());
        year.setText(getSongYear());
    }

    @Override
    protected void loadCurrentImage() {
        Bitmap bitmap = getAlbumArt();
        setImageBitmap(bitmap, RetroMusicColorUtil.getColor(RetroMusicColorUtil.generatePalette(bitmap), ContextCompat.getColor(this, R.color.md_grey_500)));
        deleteAlbumArt = false;
    }

    @Override
    protected void getImageFromLastFM() {
        String albumTitleStr = albumTitle.getText().toString();
        String albumArtistNameStr = albumArtist.getText().toString();
        if (albumArtistNameStr.trim().equals("") || albumTitleStr.trim().equals("")) {
            Toast.makeText(this, getResources().getString(R.string.album_or_artist_empty), Toast.LENGTH_SHORT).show();
            return;
        }
        progressBar.setVisibility(View.VISIBLE);
        lastFMRestClient.getApiService()
                .getAlbumInfo(albumTitleStr, albumArtistNameStr, null)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.computation())
                .doOnSubscribe(disposable -> {
                    progressBar.setVisibility(View.VISIBLE);
                })
                .doOnComplete(() -> {
                    progressBar.setVisibility(View.GONE);
                }).subscribe(lastFmAlbum -> {
            if (lastFmAlbum.getAlbum() != null) {

                String url = LastFMUtil.getLargestAlbumImageUrl(lastFmAlbum.getAlbum().getImage());
                if (!TextUtils.isEmpty(url) && url.trim().length() > 0) {
                    Glide.with(AlbumTagEditorActivity.this)
                            .load(url)
                            .asBitmap()
                            .transcode(new BitmapPaletteTranscoder(AlbumTagEditorActivity.this), BitmapPaletteWrapper.class)
                            .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                            .error(R.drawable.default_album)
                            .into(new SimpleTarget<BitmapPaletteWrapper>() {
                                @Override
                                public void onLoadFailed(Exception e, Drawable errorDrawable) {
                                    super.onLoadFailed(e, errorDrawable);
                                    progressBar.setVisibility(View.GONE);
                                    e.printStackTrace();
                                    Toast.makeText(AlbumTagEditorActivity.this, e.toString(), Toast.LENGTH_LONG).show();
                                }

                                @Override
                                public void onResourceReady(BitmapPaletteWrapper resource, GlideAnimation<? super BitmapPaletteWrapper> glideAnimation) {
                                    progressBar.setVisibility(View.GONE);
                                    albumArtBitmap = ImageUtil.resizeBitmap(resource.getBitmap(), 2048);
                                    setImageBitmap(albumArtBitmap, RetroMusicColorUtil.getColor(resource.getPalette(),
                                            ContextCompat.getColor(AlbumTagEditorActivity.this, R.color.md_grey_500)));
                                    deleteAlbumArt = false;
                                    dataChanged();
                                    setResult(RESULT_OK);
                                }
                            });
                    return;
                }
                if (lastFmAlbum.getAlbum().getTags().getTag().size() > 0) {
                    genre.setText(lastFmAlbum.getAlbum().getTags().getTag().get(0).getName());
                }

            }
            toastLoadingFailed();
        });
    }

    private void toastLoadingFailed() {
        Toast.makeText(AlbumTagEditorActivity.this,
                R.string.could_not_download_album_cover, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void searchImageOnWeb() {
        searchWebFor(albumTitle.getText().toString(), albumArtist.getText().toString());
    }

    @Override
    protected void deleteImage() {
        setImageBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.default_album),
                ContextCompat.getColor(AlbumTagEditorActivity.this, R.color.md_grey_500));
        deleteAlbumArt = true;
        dataChanged();
    }

    @Override
    protected void save() {
        Map<FieldKey, String> fieldKeyValueMap = new EnumMap<>(FieldKey.class);
        fieldKeyValueMap.put(FieldKey.ALBUM, albumTitle.getText().toString());
        //android seems not to recognize album_artist field so we additionally write the normal artist field
        fieldKeyValueMap.put(FieldKey.ARTIST, albumArtist.getText().toString());
        fieldKeyValueMap.put(FieldKey.ALBUM_ARTIST, albumArtist.getText().toString());
        fieldKeyValueMap.put(FieldKey.GENRE, genre.getText().toString());
        fieldKeyValueMap.put(FieldKey.YEAR, year.getText().toString());

        writeValuesToFiles(fieldKeyValueMap, deleteAlbumArt ? new ArtworkInfo(getId(), null) : albumArtBitmap == null ? null : new ArtworkInfo(getId(), albumArtBitmap));
    }

    @Override
    protected int getContentViewLayout() {
        return R.layout.activity_album_tag_editor;
    }

    @NonNull
    @Override
    protected List<String> getSongPaths() {
        ArrayList<Song> songs = AlbumLoader.getAlbum(this, getId()).blockingFirst().songs;
        ArrayList<String> paths = new ArrayList<>(songs.size());
        for (Song song : songs) {
            paths.add(song.data);
        }
        return paths;
    }

    @Override
    protected void loadImageFromFile(@NonNull final Uri selectedFileUri) {
        Glide.with(AlbumTagEditorActivity.this)
                .load(selectedFileUri)
                .asBitmap()
                .transcode(new BitmapPaletteTranscoder(AlbumTagEditorActivity.this), BitmapPaletteWrapper.class)
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .skipMemoryCache(true)
                .into(new SimpleTarget<BitmapPaletteWrapper>() {
                    @Override
                    public void onLoadFailed(Exception e, Drawable errorDrawable) {
                        super.onLoadFailed(e, errorDrawable);
                        e.printStackTrace();
                        Toast.makeText(AlbumTagEditorActivity.this, e.toString(), Toast.LENGTH_LONG).show();
                    }

                    @Override
                    public void onResourceReady(BitmapPaletteWrapper resource, GlideAnimation<? super BitmapPaletteWrapper> glideAnimation) {
                        RetroMusicColorUtil.getColor(resource.getPalette(), Color.TRANSPARENT);
                        albumArtBitmap = ImageUtil.resizeBitmap(resource.getBitmap(), 2048);
                        setImageBitmap(albumArtBitmap, RetroMusicColorUtil.getColor(resource.getPalette(),
                                ContextCompat.getColor(AlbumTagEditorActivity.this, R.color.md_grey_500)));
                        deleteAlbumArt = false;
                        dataChanged();
                        setResult(RESULT_OK);
                    }
                });
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {

    }

    @Override
    public void afterTextChanged(Editable s) {
        dataChanged();
    }

    @Override
    protected void setColors(int color) {
        super.setColors(color);
        //albumTitle.setTextColor(ToolbarContentTintHelper.toolbarTitleColor(this, color));
    }
}
