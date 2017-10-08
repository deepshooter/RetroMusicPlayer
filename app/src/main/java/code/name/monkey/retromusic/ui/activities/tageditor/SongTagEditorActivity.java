package code.name.monkey.retromusic.ui.activities.tageditor;

import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.format.DateFormat;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.kabouzeid.appthemehelper.ThemeStore;
import com.kabouzeid.appthemehelper.util.TintHelper;
import com.kabouzeid.appthemehelper.util.ToolbarContentTintHelper;

import org.jaudiotagger.tag.FieldKey;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.EnumMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import code.name.monkey.retromusic.R;
import code.name.monkey.retromusic.lastfm.rest.LastFMRestClient;
import code.name.monkey.retromusic.lastfm.rest.model.LastFmTrack.Track;
import code.name.monkey.retromusic.lastfm.rest.model.LastFmTrack.Track.Album;
import code.name.monkey.retromusic.lastfm.rest.model.LastFmTrack.Track.Album.Attr;
import code.name.monkey.retromusic.lastfm.rest.model.LastFmTrack.Track.Toptags;
import code.name.monkey.retromusic.lastfm.rest.model.LastFmTrack.Track.Wiki;
import code.name.monkey.retromusic.loaders.SongLoader;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;


public class SongTagEditorActivity extends AbsTagEditorActivity implements TextWatcher {
    public static final String TAG = SongTagEditorActivity.class.getSimpleName();
    @BindView(R.id.title1)
    EditText songTitle;
    @BindView(R.id.title2)
    EditText albumTitle;
    @BindView(R.id.artist)
    EditText artist;
    @BindView(R.id.genre)
    EditText genre;
    @BindView(R.id.year)
    EditText year;
    @BindView(R.id.image_text)
    EditText trackNumber;
    @BindView(R.id.lyrics)
    EditText lyrics;
    @BindView(R.id.progressBar)
    ProgressBar progressBar;
    @BindView(R.id.load)
    Button loadTrackDetails;
    private LastFMRestClient lastFMRestClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ButterKnife.bind(this);

        setStatusbarColorAuto();
        setTaskDescriptionColorAuto();

        setNoImageMode();
        setUpViews();

        progressBar.setVisibility(View.GONE);
        lastFMRestClient = new LastFMRestClient(this);

        getSupportActionBar().setTitle(R.string.action_tag_editor);
        TintHelper.setTintAuto(loadTrackDetails, ThemeStore.accentColor(this), false);
    }

    @OnClick(R.id.load)
    void loadImage() {
        getImageFromLastFM();
    }

    private void setUpViews() {
        fillViewsWithFileTags();
        songTitle.addTextChangedListener(this);
        albumTitle.addTextChangedListener(this);
        artist.addTextChangedListener(this);
        genre.addTextChangedListener(this);
        year.addTextChangedListener(this);
        trackNumber.addTextChangedListener(this);
        lyrics.addTextChangedListener(this);
    }

    private void fillViewsWithFileTags() {
        songTitle.setText(getSongTitle());
        albumTitle.setText(getAlbumTitle());
        artist.setText(getArtistName());
        genre.setText(getGenreName());
        year.setText(getSongYear());
        trackNumber.setText(getTrackNumber());
        lyrics.setText(getLyrics());
    }

    @Override
    protected void loadCurrentImage() {

    }

    @Override
    protected void getImageFromLastFM() {
        String albumTitleStr = albumTitle.getText().toString();
        String albumArtistNameStr = artist.getText().toString();
        String songName = songTitle.getText().toString();
        if (albumArtistNameStr.trim().equals("") || albumTitleStr.trim().equals("")) {
            Toast.makeText(this, getResources().getString(R.string.album_or_artist_empty), Toast.LENGTH_SHORT).show();
            return;
        }

        lastFMRestClient.getApiService().getTrackInfo(albumArtistNameStr, songName)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.computation())
                .doOnSubscribe(disposable -> {
                    progressBar.setVisibility(View.VISIBLE);
                })
                .doOnComplete(() -> {
                    progressBar.setVisibility(View.GONE);
                })
                .subscribe(lastFmTrack -> {
                    Track track = lastFmTrack != null ? lastFmTrack.getTrack() : null;
                    Album albums = track != null ? track.getAlbum() : null;
                    Attr attr = null;
                    if (albums != null) attr = albums.getAttr();
                    Wiki wiki = track != null ? track.getWiki() : null;
                    List<Toptags.Tag> tags = track != null ? track.getToptags().getTag() : null;
                    if (attr != null) {
                        trackNumber.setText(attr.getPosition());
                    }
                    if (wiki != null) {
                        try {
                            Date date = new SimpleDateFormat("dd MMM YYYY, k:mm", Locale.getDefault()).parse(wiki.getPublished());
                            year.setText(DateFormat.format("yyyy", date));
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                    }
                    if (!(tags != null && tags.isEmpty())) {
                        genre.setText(tags != null ? tags.get(0).getName() : "");
                    }
                }, Throwable::printStackTrace, () -> {
                    progressBar.setVisibility(View.GONE);
                });
    }

    @Override
    protected void searchImageOnWeb() {

    }

    @Override
    protected void deleteImage() {

    }

    @Override
    protected void save() {
        Map<FieldKey, String> fieldKeyValueMap = new EnumMap<>(FieldKey.class);
        fieldKeyValueMap.put(FieldKey.TITLE, songTitle.getText().toString());
        fieldKeyValueMap.put(FieldKey.ALBUM, albumTitle.getText().toString());
        fieldKeyValueMap.put(FieldKey.ARTIST, artist.getText().toString());
        fieldKeyValueMap.put(FieldKey.GENRE, genre.getText().toString());
        fieldKeyValueMap.put(FieldKey.YEAR, year.getText().toString());
        fieldKeyValueMap.put(FieldKey.TRACK, trackNumber.getText().toString());
        fieldKeyValueMap.put(FieldKey.LYRICS, lyrics.getText().toString());
        //writeValuesToFiles(fieldKeyValueMap, deleteAlbumArt ? new ArtworkInfo(getId(), null) : albumArtBitmap == null ? null : new ArtworkInfo(getId(), albumArtBitmap));

        writeValuesToFiles(fieldKeyValueMap, null);
    }

    @Override
    protected int getContentViewLayout() {
        return R.layout.activity_song_tag_editor;
    }

    @NonNull
    @Override
    protected List<String> getSongPaths() {
        ArrayList<String> paths = new ArrayList<>(1);
        paths.add(SongLoader.getSong(this, getId()).blockingFirst().data);
        return paths;
    }

    @Override
    protected void loadImageFromFile(Uri imageFilePath) {

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
        int toolbarTitleColor = ToolbarContentTintHelper.toolbarTitleColor(this, color);
        songTitle.setTextColor(toolbarTitleColor);
        albumTitle.setTextColor(toolbarTitleColor);
    }
}
