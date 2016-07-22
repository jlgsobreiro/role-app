package br.com.mytho.role.domain.service;

import android.content.Context;

import java.io.IOException;
import java.util.List;

import br.com.mytho.role.model.Event;
import br.com.mytho.role.security.model.AccessToken;
import br.com.mytho.role.security.model.repository.AccessTokenRepository;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.jackson.JacksonConverterFactory;
import retrofit2.http.GET;
import rx.Observable;

/**
 * Created by leonardocordeiro on 11/07/16.
 */
public interface EventService {

    @GET("event")
    Observable<List<Event>> list();

    class Builder {
        private Context context;
        private AccessToken token;

        private String API_BASE_URL = "http://role-lema.rhcloud.com/rolebackend/";
        private OkHttpClient.Builder httpClient = new OkHttpClient.Builder();
        private Retrofit.Builder builder = new Retrofit.Builder()
                        .baseUrl(API_BASE_URL)
                        .addConverterFactory(JacksonConverterFactory.create());

        public Builder context(Context context) {
            this.context = context;
            return this;
        }

        public EventService build() {
            token = new AccessTokenRepository(context).retrieve();

            httpClient.addInterceptor(new Interceptor() {
                @Override
                public Response intercept(Interceptor.Chain chain) throws IOException {
                    Request original = chain.request();

                    Request.Builder requestBuilder = original.newBuilder()
                            .header("Accept", "application/json")
                            .header("Authorization", "Bearer " + token.getCode())
                            .method(original.method(), original.body());

                    Request request = requestBuilder.build();
                    return chain.proceed(request);
                }
            });

            Retrofit retrofit = builder.client(httpClient.build()).addCallAdapterFactory(RxJavaCallAdapterFactory.create()).build();
            return retrofit.create(EventService.class);
        }
    }

}
