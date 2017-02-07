package org.egov.resolver;

import org.egov.model.User;
import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.ModelAndViewContainer;

import java.util.Optional;

@Service
public class AuthUserResolver implements org.springframework.web.method.support.HandlerMethodArgumentResolver {

    private static final String AUTH_TOKEN_HEADER = "X-AUTH-TOKEN";

    @Override
    public boolean supportsParameter(MethodParameter methodParameter) {
        return methodParameter.getParameterType().isAssignableFrom(User.class);
    }

    @Override
    public Object resolveArgument(MethodParameter methodParameter,
                                  ModelAndViewContainer modelAndViewContainer,
                                  NativeWebRequest nativeWebRequest,
                                  WebDataBinderFactory webDataBinderFactory) throws Exception {
        final String authToken = nativeWebRequest.getHeader(AUTH_TOKEN_HEADER);
        if (authToken != null) {
            return new User(authToken);
        } else {
            return null;
        }
    }

}