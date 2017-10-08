package code.name.monkey.retromusic.ui.activities;

import android.app.SearchManager;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.support.transition.TransitionManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.TextView;
import android.widget.Toast;

import com.kabouzeid.appthemehelper.ThemeStore;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import code.name.monkey.retromusic.Injection;
import code.name.monkey.retromusic.R;
import code.name.monkey.retromusic.interfaces.LoaderIds;
import code.name.monkey.retromusic.mvp.contract.SearchContract;
import code.name.monkey.retromusic.mvp.presenter.SearchPresenter;
import code.name.monkey.retromusic.ui.activities.base.AbsMusicServiceActivity;
import code.name.monkey.retromusic.ui.adapter.SearchAdapter;
import code.name.monkey.retromusic.util.Util;

public class SearchActivity extends AbsMusicServiceActivity implements SearchView.OnQueryTextListener, SearchContract.SearchView {
    public static final String TAG = SearchActivity.class.getSimpleName();
    public static final String QUERY = "query";
    private static final int LOADER_ID = LoaderIds.SEARCH_ACTIVITY;
    private static final int REQ_CODE_SPEECH_INPUT = 9002;
    @BindView(R.id.voice_search)
    View micIcon;
    @BindView(R.id.recycler_view)
    RecyclerView recyclerView;
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(android.R.id.empty)
    TextView empty;
    @BindView(R.id.search_view)
    SearchView mSearchView;
    @BindView(R.id.container)
    CoordinatorLayout mContainer;
    private SearchPresenter mSearchPresenter;
    private SearchAdapter adapter;
    private String query;

    private boolean isMicSearch = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        setDrawUnderStatusbar(true);
        ButterKnife.bind(this);

        mSearchPresenter = new SearchPresenter(Injection.provideRepository(this), this);

        setStatusbarColorAuto();
        setNavigationbarColorAuto();
        setTaskDescriptionColorAuto();

        setupRecyclerview();
        setUpToolBar();
        setupSearchView();

        if (savedInstanceState != null) {
            query = savedInstanceState.getString(QUERY);
            mSearchPresenter.search(query);
        }
    }

    private void setupRecyclerview() {
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new SearchAdapter(this, Collections.emptyList());
        adapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onChanged() {
                super.onChanged();
                empty.setVisibility(adapter.getItemCount() < 1 ? View.VISIBLE : View.GONE);
            }
        });
        recyclerView.setAdapter(adapter);

        recyclerView.setOnTouchListener((v, event) -> {
            hideSoftKeyboard();
            return false;
        });
    }

    private void setupSearchView() {
        SearchManager searchManager = (SearchManager) getSystemService(SEARCH_SERVICE);
        mSearchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        mSearchView.setQueryHint(getString(R.string.search_hint));
        mSearchView.setImeOptions(mSearchView.getImeOptions() | EditorInfo.IME_ACTION_SEARCH |
                EditorInfo.IME_FLAG_NO_EXTRACT_UI | EditorInfo.IME_FLAG_NO_FULLSCREEN);
        mSearchView.post(() -> mSearchView.setOnQueryTextListener(SearchActivity.this));
        mSearchView.onActionViewExpanded();
        mSearchView.setIconified(false);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mSearchPresenter.subscribe();
        if (!isMicSearch && getIntent().getBooleanExtra("mic_search", false)) {
            startMicSearch();
            isMicSearch = true;
            return;
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        mSearchPresenter.unsubscribe();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(QUERY, query);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
    }

    private void setUpToolBar() {
        toolbar.setBackgroundColor(ThemeStore.primaryColor(this));
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public void onEnterAnimationComplete() {
        super.onEnterAnimationComplete();
        TransitionManager.beginDelayedTransition(mContainer);
        //noinspection ConstantConditions

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }

    private void search(@NonNull String query) {
        this.query = query;
        TransitionManager.beginDelayedTransition(toolbar);
        micIcon.setVisibility(query.length() > 0 ? View.GONE : View.VISIBLE);
        mSearchPresenter.search(query);
    }

    @Override
    public void onMediaStoreChanged() {
        super.onMediaStoreChanged();
        mSearchPresenter.search(query);
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        hideSoftKeyboard();
        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        search(newText);
        return false;
    }

    private void hideSoftKeyboard() {
        Util.hideSoftKeyboard(SearchActivity.this);
        if (mSearchView != null) {
            mSearchView.clearFocus();
        }
    }

    @Override
    public void loading() {

    }

    @Override
    public void showEmptyView() {

    }

    @Override
    public void completed() {

    }

    @Override
    public void showList(ArrayList<Object> list) {
        Log.i(TAG, "showList: " + list.size());
        adapter.swapDataSet(list);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQ_CODE_SPEECH_INPUT: {
                if (resultCode == RESULT_OK && null != data) {

                    ArrayList<String> result = data
                            .getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    query = result.get(0);
                    mSearchView.setQuery(query, true);
                    mSearchPresenter.search(query);
                }
                break;
            }

        }
    }

    @OnClick(R.id.voice_search)
    void searchImageView() {
        startMicSearch();
    }

    private void startMicSearch() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, getString(R.string.speech_prompt));
        try {
            startActivityForResult(intent, REQ_CODE_SPEECH_INPUT);
        } catch (ActivityNotFoundException e) {
            e.printStackTrace();
            Toast.makeText(this, getString(R.string.speech_not_supported), Toast.LENGTH_SHORT).show();
        }
    }
}
