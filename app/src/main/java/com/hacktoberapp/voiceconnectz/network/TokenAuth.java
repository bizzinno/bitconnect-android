package com.hacktoberapp.voiceconnectz.network;

import com.squareup.okhttp.Authenticator;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import java.io.IOException;
import java.net.Proxy;

public class TokenAuth implements Authenticator {
    @Override
    public Request authenticate(Proxy proxy, Response response) throws IOException {
        return null;
    }

    @Override
    public Request authenticateProxy(Proxy proxy, Response response) throws IOException {
        return null;
    }
}
