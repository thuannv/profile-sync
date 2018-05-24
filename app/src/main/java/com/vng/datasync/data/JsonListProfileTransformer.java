package com.vng.datasync.data;

import android.text.TextUtils;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.vng.datasync.data.model.Profile;

import java.lang.reflect.Type;
import java.util.Collections;
import java.util.List;

/**
 * @author thuannv
 * @since 24/05/2018
 */
public class JsonListProfileTransformer implements Transformer<String, List<Profile>> {

    @Override
    public List<Profile> transform(String from) {
        if (TextUtils.isEmpty(from)) {
            return Collections.emptyList();
        }
        try {
            final Gson gson = new Gson();
            final Type type = new TypeToken<List<Profile>>(){}.getType();
            final List<Profile> profiles = gson.fromJson(from, type);
            return profiles;
        } catch (Exception e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }
}
