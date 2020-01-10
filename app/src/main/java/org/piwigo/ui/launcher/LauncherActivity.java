/*
 * Piwigo for Android
 * Copyright (C) 2016-2017 Piwigo Team http://piwigo.org
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package org.piwigo.ui.launcher;

import android.accounts.Account;
import android.content.Intent;
import androidx.databinding.DataBindingUtil;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import org.piwigo.R;
import org.piwigo.databinding.ActivityLauncherBinding;
import org.piwigo.io.restmodel.LoginResponse;
import org.piwigo.io.restrepository.RestUserRepository;
import org.piwigo.ui.shared.BaseActivity;
import org.piwigo.ui.shared.Navigator;

import javax.inject.Inject;

import dagger.android.AndroidInjection;
import io.reactivex.observers.DisposableObserver;
//import rx.Subscriber;

public class LauncherActivity extends BaseActivity {

    private static final String TAG = LauncherActivity.class.getName();
    private final Handler handler = new Handler();
    private ActivityLauncherBinding binding;

    @Inject
    RestUserRepository userRepository;

    @Override protected void onCreate(Bundle savedInstanceState) {
        AndroidInjection.inject(this);
        super.onCreate(savedInstanceState);

        binding = DataBindingUtil.setContentView(this, R.layout.activity_launcher);

        if (userManager.hasAccounts()) {

            Account a = userManager.getActiveAccount().getValue();
            userRepository.login(a)
                    .subscribe(new DisposableObserver<LoginResponse>() {
                        @Override
                        public void onComplete() {
                        }

                        @Override
                        public void onError(Throwable e) {
//                                Log.e(TAG, "Login failed: " + e.toString()); //getMessage());
                            // TODO: handle this properly... (can be triggered with empty password)
                        }


                        @Override
                        public void onNext(LoginResponse loginResponse) {
                            Log.i(TAG, "Login succeeded: " + loginResponse.pwgId);
                            userManager.setCookie(a, loginResponse.pwgId);
                            userManager.setToken(a, loginResponse.statusResponse.result.pwgToken);
                            if(loginResponse.statusResponse.result.uploadFormChunkSize != null) {
                                userManager.setChunkSize(a, loginResponse.statusResponse.result.uploadFormChunkSize * 1024);
                            }
                        }
                    });

            handler.postDelayed(this::startMain, 1000);
        } else {
            handler.postDelayed(this::startLogin, 500);
        }
    }

    @Override protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Navigator.REQUEST_CODE_LOGIN && resultCode == RESULT_OK) {
            handler.postDelayed(this::startMain, 1000);
        } else {
            finish();
        }
    }

    private void startMain() {
        navigator.startMain(this);
        finish();
    }

    private void startLogin() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            navigator.startLogin(this, binding.logo, getString(R.string.logo_transition_name));
        } else {
            navigator.startLogin(this);
        }
    }
}
