package org.piwigo.io.repository;

/**
 * Created by Jeff on 7/18/2017.
 */


import android.accounts.Account;
import android.support.annotation.Nullable;

import org.piwigo.accounts.UserManager;
import org.piwigo.io.RestService;
import org.piwigo.io.RestServiceFactory;
import org.piwigo.io.model.ImageInfo;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

import rx.Observable;
import rx.Scheduler;


public class ImageRepository extends BaseRepository {

    @Inject public ImageRepository(RestServiceFactory restServiceFactory, @Named("IoScheduler") Scheduler ioScheduler, @Named("UiScheduler") Scheduler uiScheduler, UserManager userManager) {
        super(restServiceFactory, ioScheduler, uiScheduler, userManager);
    }

    public Observable<List<ImageInfo>> getImages(Account account, @Nullable Integer categoryId) {
        RestService restService = restServiceFactory.createForAccount(account);

        return restService
                .getImages(categoryId)
                .map(imageListResponse -> imageListResponse.result.images)
                .compose(applySchedulers());


   }



    }


