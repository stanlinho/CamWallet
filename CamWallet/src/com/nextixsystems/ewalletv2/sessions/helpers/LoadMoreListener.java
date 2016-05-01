package com.nextixsystems.ewalletv2.sessions.helpers;

import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;

public class LoadMoreListener implements OnScrollListener {

    public interface LoadMoreCallback {
        public void onLoadMore();
    }

    private final int loadingThreshold;
    private boolean isLoadingMore;
    private LoadMoreCallback callback;

    public LoadMoreListener(LoadMoreCallback callback, int loadingThreshold) {
        this.callback = callback;
        this.loadingThreshold = loadingThreshold;
        isLoadingMore = true;
    }

    public int getLoadingThreshold() {
        return loadingThreshold;
    }

    public boolean isLoadingMore() {
        return isLoadingMore;
    }

    public void isLoadingMore(boolean isLoadingMore) {
        this.isLoadingMore = isLoadingMore;
    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem,
            int visibleItemCount, int totalItemCount) {

        if(isLoadingMore) {
            int loadingThreshold = 0;
            boolean willLoadMore = (firstVisibleItem + visibleItemCount ) >= (totalItemCount - loadingThreshold);
            if(willLoadMore &&
                    callback != null) {
                callback.onLoadMore();
                isLoadingMore = false;
            }
        }

    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
        // TODO Auto-generated method stub

    }
}
