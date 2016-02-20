package com.nex3z.examples.widget.widget;

import android.app.IntentService;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Intent;
import android.graphics.Bitmap;
import android.util.Log;
import android.widget.RemoteViews;

import com.nex3z.examples.widget.R;
import com.nex3z.examples.widget.app.App;
import com.nex3z.examples.widget.model.Movie;
import com.nex3z.examples.widget.rest.model.MovieResponse;
import com.nex3z.examples.widget.rest.service.MovieService;
import com.nex3z.examples.widget.ui.activity.MainActivity;
import com.nex3z.examples.widget.util.ImageUtility;

import java.io.IOException;
import java.util.List;
import java.util.Random;

import retrofit.Call;

public class SimpleWidgetIntentService extends IntentService {
    private static final String LOG_TAG = SimpleWidgetIntentService.class.getSimpleName();

    public SimpleWidgetIntentService() {
        super("SimpleWidgetIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.v(LOG_TAG, "Start intent service.");

        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this);
        int[] appWidgetIds = appWidgetManager.getAppWidgetIds(new ComponentName(this,
                SimpleWidgetProvider.class));

        MovieService movieService = App.getRestClient().getMovieService();
        Call<MovieResponse> call = movieService.getMovies(MovieService.SORT_BY_POPULARITY_DESC, 1);

        try {
            MovieResponse response = call.execute().body();
            List<Movie> movies = response.getMovies();

            Random random = new Random();
            int pick = random.nextInt(movies.size());
            Log.v(LOG_TAG, "onHandleIntent(): pick = " + pick);

            String posterPath = movies.get(pick).getPosterPath();
            Bitmap poster = ImageUtility.downloadBitmap(ImageUtility.getImageUrl(posterPath));

            for (int appWidgetId : appWidgetIds) {
                Log.v(LOG_TAG, "onHandleIntent(): Updating appWidgetId = " + appWidgetId);

                Intent launchIntent = new Intent(this, MainActivity.class);
                PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, launchIntent, 0);

                RemoteViews views = new RemoteViews(getPackageName(), R.layout.widget_poster);
                views.setOnClickPendingIntent(R.id.widget_movie_poster, pendingIntent);
                views.setImageViewBitmap(R.id.widget_movie_poster, poster);

                appWidgetManager.updateAppWidget(appWidgetId, views);
            }
        } catch (IOException e) {
            Log.e(LOG_TAG, e.getMessage());
        }
    }
}
