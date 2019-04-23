package com.snowball.finder.finderskill.interceptor;

import com.amazon.ask.dispatcher.request.handler.HandlerInput;
import com.amazon.ask.dispatcher.request.interceptor.ResponseInterceptor;
import com.amazon.ask.model.Response;

import java.util.Optional;

public class InitPersistentAttributesInterceptor implements ResponseInterceptor {

    @Override
    public void process(HandlerInput input, Optional<Response> output) {
        if (input.getRequest().getType().equals("LaunchRequest"))
        input.getAttributesManager().getPersistentAttributes();
    }
}
